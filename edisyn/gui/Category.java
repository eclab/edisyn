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
   A pretty container for widgets to categorize them

   @author Sean Luke
*/

public class Category extends JComponent implements Gatherable
    {             
    Color color;
    Synth synth;
    
    String preamble;
    boolean pasteable = false;
    boolean distributable = false;
    
    MenuItem copy = new MenuItem("Copy Category");
    MenuItem paste = new MenuItem("Paste Category");
    MenuItem distribute = new MenuItem("Distribute");
    MenuItem copyFromMutable = new MenuItem("Copy Category (Mutation Parameters Only)");
    MenuItem pasteToMutable = new MenuItem("Paste Category (Mutation Parameters Only)");
    MenuItem distributeToMutable = new MenuItem("Distribute (Mutation Parameters Only)");
    MenuItem reset = new MenuItem("Reset Category");
    
    public void makePasteable(String preamble) { copy.setEnabled(true); copyFromMutable.setEnabled(true); paste.setEnabled(true); pasteToMutable.setEnabled(true); pasteable = true; this.preamble = preamble; }
    public void makeDistributable(String preamble) { distribute.setEnabled(true); distributeToMutable.setEnabled(true); distributable = true; this.preamble = preamble; }
    public void makeUnresettable() { reset.setEnabled(false); }
    
	PopupMenu pop = new PopupMenu();
      
    boolean isPasteCompatibleCategory()
    	{
    	return (pasteable &&
    			preamble != null &&
    			synth.getCopyPreamble() != null &&
    			synth.getCopyPreamble().equals(preamble));
    	}
    	
   boolean canDistributeKey()	
		{
		String lastKey = synth.getModel().getLastKey();
		if (lastKey == null) return false;
		
		ArrayList components = new ArrayList();
		gatherAllComponents(components);
		for(int i = 0; i < components.size(); i++)
			{
			if (components.get(i) instanceof HasKey)
				{
				HasKey nc = (HasKey)(components.get(i));
				String key = nc.getKey();
				if (key.equals(lastKey))
					return true;
				}
			}
		return false;
		}
      
     void resetCategory()
    	{
        Synth other = Synth.instantiate(synth.getClass(), synth.getSynthNameLocal(), true, true, synth.tuple);
		ArrayList components = new ArrayList();
		gatherAllComponents(components);
		for(int i = 0; i < components.size(); i++)
			{
			if (components.get(i) instanceof HasKey)
				{
				HasKey nc = (HasKey)(components.get(i));
				String key = nc.getKey();
				if (synth.getModel().exists(key) && other.getModel().exists(key))
					{
					if (synth.getModel().isString(key))
						{
						synth.getModel().set(key, other.getModel().get(key, ""));
						}
					else
						{
						synth.getModel().set(key, other.getModel().get(key, 0));
						}
					}
				else
					System.err.println("key missing in model : " + key);
				}
			}    		
    	}
    	
     void copyCategory(boolean includeImmutable)
    	{
		String[] mutationKeys = synth.getMutationKeys();
		if (mutationKeys == null) mutationKeys = new String[0];
		HashSet mutationSet = new HashSet(Arrays.asList(mutationKeys));
    		
    	ArrayList keys = new ArrayList();
		ArrayList components = new ArrayList();
		gatherAllComponents(components);
		for(int i = 0; i < components.size(); i++)
			{
			if (components.get(i) instanceof HasKey)
				{
				HasKey nc = (HasKey)(components.get(i));
				String key = nc.getKey();
				if (mutationSet.contains(key) || includeImmutable)
					keys.add(key);
				}
			} 
		synth.setCopyKeys(keys);   
		synth.setCopyPreamble(preamble);		
    	}
    	
    	
     void pasteCategory(boolean includeImmutable)
    	{
    	ArrayList copyKeys = synth.getCopyKeys();
    	if (copyKeys == null || copyKeys.size() == 0)
    		return;  // oops
    	
    		// First we need to map OUR keys
    		HashMap keys = new HashMap();
    		ArrayList components = new ArrayList();
    		gatherAllComponents(components);
			for(int i = 0; i < components.size(); i++)
				{
				if (components.get(i) instanceof HasKey)
					{
					String key = (String)(((HasKey)(components.get(i))).getKey());
					String reduced = reduceFirstDigitsAfterPreamble(key, preamble);
					keys.put(reduced, key);
					}    
				}		
    		
    		String[] mutationKeys = synth.getMutationKeys();
			if (mutationKeys == null) mutationKeys = new String[0];
    		HashSet mutationSet = new HashSet(Arrays.asList(mutationKeys));
    		
			// Now we change keys as appropriate
			for(int i = 0; i < copyKeys.size(); i++)
				{
				String key = (String)(copyKeys.get(i));
				String reduced = reduceFirstDigitsAfterPreamble(key, synth.getCopyPreamble());
				String mapped = (String)(keys.get(reduced));
				if (mapped != null)
					{
					Model model = synth.getModel();
					if (model.exists(mapped) && (mutationSet.contains(mapped) || includeImmutable))
						{
						if (model.isString(mapped))
							{
							model.set(mapped, model.get(key, model.get(mapped, "")));
							}
						else
							{
							model.set(mapped, model.get(key, model.get(mapped, 0)));
							}
						}
					else
						System.err.println("Key didn't exist " + mapped);
					}
				else
					System.err.println("Null mapping for " + key + " (reduced to " + reduced + ")");					
				}
    		}
    	
     void distributeCategory(boolean includeImmutable)
    	{
    	Model model = synth.getModel();
    	String lastKey = model.getLastKey();

    	if (lastKey != null)
    		{
    		String lastReduced = (pasteable ? reduceSecondDigitsAfterPreamble(lastKey, preamble) : reduceFirstDigitsAfterPreamble(lastKey, preamble));

    		String[] mutationKeys = synth.getMutationKeys();
			if (mutationKeys == null) mutationKeys = new String[0];
    		HashSet mutationSet = new HashSet(Arrays.asList(mutationKeys));

    		// Now we change keys as appropriate
    		ArrayList components = new ArrayList();
    		gatherAllComponents(components);
			for(int i = 0; i < components.size(); i++)
				{
				if (components.get(i) instanceof HasKey)
					{
					HasKey nc = (HasKey)(components.get(i));
					String key = nc.getKey();
    				String reduced = (pasteable ? reduceSecondDigitsAfterPreamble(key, preamble) : reduceFirstDigitsAfterPreamble(key, preamble));
    				
    				if (reduced.equals(lastReduced))
    					{
						if (model.exists(key) && (mutationSet.contains(key) || includeImmutable))
							{
							if (model.isString(key))
								{
								model.set(key, model.get(lastKey, model.get(key, "")));
								}
							else
								{
								model.set(key, model.get(lastKey, model.get(key, 0)));
								}
							}
						else
							System.err.println("Key didn't exist " + key);
						}
					else
						System.err.println("Null mapping for " + key + " (reduced to " + reduced + ")");					
					}
				}
    		}
    	}
    
    final static int STATE_FIRST_NUMBER = 0;
	final static int STATE_FIRST_STRING = 1;
	final static int STATE_NUMBER = 2;
	final static int STATE_FINISHED = 3;

    /** This function removes the FIRST string of digits in a name after a preamble, returns the resulting name. */
     static String reduceFirstDigitsAfterPreamble(String name, String preamble)
    	{
    	char[] n = name.toCharArray();
	    StringBuilder sb = new StringBuilder();

		for(int i = 0; i < preamble.length(); i++)
			{
			sb.append(n[i]);
			}
			
    	int state = STATE_FIRST_STRING;
		for(int i = preamble.length(); i < n.length; i++)
			{
			if (state == STATE_FIRST_STRING)
				{
				if (Character.isDigit(n[i]))
					{
					state = STATE_NUMBER;
					}
				else
					{
					sb.append(n[i]);
					}
				}
			else if (state == STATE_NUMBER)
				{
				if (!Character.isDigit(n[i]))
					{
					sb.append(n[i]);
					state = STATE_FINISHED;
					}
				}
			else // state == STATE_FINISHED
				{
				sb.append(n[i]);
				}
			}
		return sb.toString();
    	}

    /** This function removes the SECOND string of digits in a name after a preamble, returns the resulting name. */
     static String reduceSecondDigitsAfterPreamble(String name, String preamble)
    	{
    	char[] n = name.toCharArray();
	    StringBuilder sb = new StringBuilder();

		for(int i = 0; i < preamble.length(); i++)
			{
			sb.append(n[i]);
			}
			
    	int state = STATE_FIRST_NUMBER;
		for(int i = preamble.length(); i < n.length; i++)
			{
			if (state == STATE_FIRST_NUMBER)
				{
				if (!Character.isDigit(n[i]))
					{
					// add it and jump to next state
					sb.append(n[i]);
					state = STATE_FIRST_STRING;
					}
				else
					{
					sb.append(n[i]);
					}
				}
			else if (state == STATE_FIRST_STRING)
				{
				if (Character.isDigit(n[i]))
					{
					// skip it and jump to next state
					state = STATE_NUMBER;
					}
				else
					{
					sb.append(n[i]);
					}
				}
			else if (state == STATE_NUMBER)
				{
				if (!Character.isDigit(n[i]))
					{
					// add it and jump to next state
					sb.append(n[i]);
					state = STATE_FINISHED;
					}
				}
			else  // state == STATE_FINISHED
				{
				sb.append(n[i]);
				}
			}
		return sb.toString();
    	}

    	    
    /** If synth is non-null, then double-clicking on the category will select or deselect all the
        components inside it for mutation purposes. */
    public Category(final Synth synth, String label, Color color)
        {
        this.synth = synth;
        setLayout(new BorderLayout());
        this.color = color;     
        setName(label);

        if (synth != null)
            {
            addMouseListener(new MouseAdapter()
                {
                public void mousePressed(MouseEvent e)
                	{
					if (e.getY() < 20 &&
						(((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) || 
                         ((e.getModifiers() & InputEvent.SHIFT_MASK) == InputEvent.SHIFT_MASK)))
                        {
                        paste.setEnabled(pasteable && isPasteCompatibleCategory());
                        pasteToMutable.setEnabled(pasteable && isPasteCompatibleCategory());
                        distribute.setEnabled(distributable && canDistributeKey());
                        distributeToMutable.setEnabled(distributable && canDistributeKey());
                        pop.show(Category.this, e.getX(), e.getY());
                        }
                    }
                public void mouseClicked(MouseEvent e)
                    {
                    if (synth.isShowingMutation())
                        {
                        boolean inBorder = ( e.getPoint().y < getInsets().top);
                        if (e.getClickCount() == 2 && inBorder)
                            {
                            boolean turnOn = true;
                            ArrayList comps = new ArrayList();
                            gatherAllComponents(comps);
                            for(int i = 0; i < comps.size(); i++)
                                {
                                if (comps.get(i) instanceof NumericalComponent)
                                    {
                                    NumericalComponent nc = (NumericalComponent)(comps.get(i));
                                    String key = nc.getKey();
                                    if (synth.mutationMap.isFree(key) && synth.getModel().getStatus(key) != Model.STATUS_IMMUTABLE)
                                        { turnOn = false; break; }
                                    }
                                }
                                                                        
                            for(int i = 0; i < comps.size(); i++)
                                {
                                if (comps.get(i) instanceof NumericalComponent)
                                    {
                                    NumericalComponent nc = (NumericalComponent)(comps.get(i));
                                    String key = nc.getKey();
                                    if (synth.getModel().getStatus(key) != Model.STATUS_IMMUTABLE)
                                        synth.mutationMap.setFree(key, turnOn);
                                    }
                                }
                            repaint();
                            }
                        }
                    }
                });
            }
                        
		pop.add(copy);
		copy.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                copyCategory(true);
                }
            });

		pop.add(paste);
		paste.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                synth.getUndo().push(synth.getModel());
                synth.getUndo().setWillPush(false);
                synth.setSendMIDI(false);
                pasteCategory(true);
                synth.setSendMIDI(true);
                // We do this TWICE because for some synthesizers, updating a parameter
                // will reveal other parameters which also must be updated but aren't yet
                // in the mapping.
                pasteCategory(true);
                synth.getUndo().setWillPush(true);
                }
            });

		pop.add(distribute);
		distribute.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                synth.getUndo().push(synth.getModel());
                synth.getUndo().setWillPush(false);
                distributeCategory(true);
                synth.getUndo().setWillPush(true);
                }
            });
            
        pop.addSeparator();

		pop.add(copyFromMutable);
		copyFromMutable.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                copyCategory(false);
                }
            });

		pop.add(pasteToMutable);
		pasteToMutable.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                synth.getUndo().push(synth.getModel());
                synth.getUndo().setWillPush(false);
                synth.setSendMIDI(false);
                pasteCategory(false);
                synth.setSendMIDI(true);
                // We do this TWICE because for some synthesizers, updating a parameter
                // will reveal other parameters which also must be updated but aren't yet
                // in the mapping.
                pasteCategory(false);
                synth.getUndo().setWillPush(true);
                }
            });

		pop.add(distributeToMutable);
		distributeToMutable.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                synth.getUndo().push(synth.getModel());
                synth.getUndo().setWillPush(false);
                distributeCategory(false);
                synth.getUndo().setWillPush(true);
                }
            });

        pop.addSeparator();

		pop.add(reset);
		reset.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                synth.getUndo().push(synth.getModel());
                synth.getUndo().setWillPush(false);
                resetCategory();
                synth.getUndo().setWillPush(true);
                }
            });
		
		copy.setEnabled(false);
		copyFromMutable.setEnabled(false);
		paste.setEnabled(false);
		pasteToMutable.setEnabled(false);
		distribute.setEnabled(false);
		distributeToMutable.setEnabled(false);
		reset.setEnabled(true);
		
		Category.this.add(pop);
        }
    
    public Insets getInsets() 
        { 
        Insets insets = (Insets)(super.getInsets().clone());
        insets.bottom = 0;
        return insets;
        }
    
    public void setName(String label)
        {
        
        // here we're going to do a little hack.  TitledBorder doesn't put the title
        // on the FAR LEFT of the line, so when we draw the border we get a little square
        // dot to the left of the title which looks really annoying.  Rather than build a
        // totally new border, we're just going to change the insets.  Titled border uses
        // the insets of the underlying border as part of its calculation of where to put
        // the border, so if we subtract 5 from the insets of the underlying border this
        // counteracts the 5 pixels that titledBorder adds in to shift the title over to
        // the right annoyingly.  So during paintBorder, we turn off a flag, then when
        // super.paintBorder goes to grab the underlying border's insets, it gets a special
        // insets which are off by 5.  But other times the insets are requested (such as
        // in paintComponent) they return normal.
        
        final boolean[] paintingBorder = new boolean[1];
        
        final MatteBorder matteBorder = new MatteBorder(Style.CATEGORY_STROKE_WIDTH(), 0, 0, 0, color)
            {
            public Insets getBorderInsets(Component c, Insets insets)
                {
                Insets ins = super.getBorderInsets(c, insets);
                if (paintingBorder[0]) 
                    ins.left = -5;
                return ins;
                }
            };
        
        TitledBorder titledBorder = new TitledBorder(
            matteBorder,
            " " + label + " ",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            Style.CATEGORY_FONT(),
            color)
            {
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) 
                {
                paintingBorder[0] = true;
                super.paintBorder(c, g, x, y, width, height);
                paintingBorder[0] = false;
                }
            };
                
        Border b = BorderFactory.createCompoundBorder(Style.CATEGORY_BORDER(), titledBorder);
        setBorder(b);
        repaint();
        }
    
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
    
    public void paintComponent(Graphics g)
        {
        Graphics2D graphics = (Graphics2D) g;

        Style.prepareGraphics(g);

        Rectangle rect = getBounds();
        rect.x = 0;
        rect.y = 0;
        graphics.setPaint(Style.BACKGROUND_COLOR());
        graphics.fill(rect);
        }
    }
