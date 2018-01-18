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
   A pretty panel for a synthesizer

   @author Sean Luke
*/

public class SynthPanel extends JPanel implements Gatherable
    {             
    public Synth synth;
    
    public SynthPanel(Synth synth)
        {
        this.synth = synth;
        setLayout(new BorderLayout());
        setBackground(Style.BACKGROUND_COLOR());
        setBorder(BorderFactory.createMatteBorder(2, 2, 0, 4, Style.BACKGROUND_COLOR()));
        }

    public Insets getInsets() { return Style.SYNTH_PANEL_INSETS(); }

    String preamble;        
	PopupMenu pop = new PopupMenu();
	boolean pasteable = true;
    public void makePasteable(String preamble) { pasteable = true; this.preamble = preamble; }
      
    boolean isPasteCompatibleCategory()
    	{
    	return (pasteable &&
    			preamble != null &&
    			synth.getCopyPreamble() != null &&
    			synth.getCopyPreamble().equals(preamble));
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

     public void resetPanel()
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
    	
     public void copyPanel(boolean includeImmutable)
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
    	
    	
     public void pastePanel(boolean includeImmutable)
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
        }
