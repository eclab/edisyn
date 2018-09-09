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

    String preamble = "";
    PopupMenu pop = new PopupMenu();

    boolean unresettable = false;
    boolean pasteable = false;
    boolean sendsAllParameters = true;
    public void makePasteable(String preamble) { pasteable = true; this.preamble = preamble; }
    public boolean isPasteable() { return pasteable; }
    public void makeUnresettable() { unresettable = true; }
    public boolean isUnresettable() { return unresettable; }
    public void setSendsAllParameters(boolean val) { sendsAllParameters = val; }
    public boolean getSendsAllParameters() { return sendsAllParameters; }
    
    public boolean isPasteCompatible(String preamble)
        {
        String copyPreamble = synth.getCopyPreamble();
        String myPreamble = preamble;
        if (copyPreamble == null) return false;
        if (myPreamble == null) return false;

        return (pasteable && 
            Category.reduceAllDigitsAfterPreamble(copyPreamble, "").equals(Category.reduceAllDigitsAfterPreamble(myPreamble, "")));
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
        boolean currentMIDI = synth.getSendMIDI();
        if (sendsAllParameters)
            {
            synth.setSendMIDI(false);
            }
                
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
                    System.err.println("Warning (SynthPanel): Key missing in model : " + key);
                }
            }               

        if (sendsAllParameters)
            {
            synth.setSendMIDI(currentMIDI);
            synth.sendAllParameters();
            }
        // so we don't have independent updates in OS X
        repaint();
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
        String copyPreamble = synth.getCopyPreamble();
        String myPreamble = preamble;
        if (copyPreamble == null) return;
        if (myPreamble == null) return;

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
                String reduced = Category.reducePreamble(key, myPreamble);
                keys.put(reduced, key);
                }    
            }               
        
        boolean currentMIDI = synth.getSendMIDI();
        if (sendsAllParameters)
            {
            synth.setSendMIDI(false);
            }
                
        String[] mutationKeys = synth.getMutationKeys();
        if (mutationKeys == null) mutationKeys = new String[0];
        HashSet mutationSet = new HashSet(Arrays.asList(mutationKeys));
                
        // Now we change keys as appropriate
        for(int i = 0; i < copyKeys.size(); i++)
            {
            String key = (String)(copyKeys.get(i));
            String reduced = Category.reducePreamble(key, copyPreamble);
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
                    System.err.println("Warning (SynthPanel): Key didn't exist " + mapped);
                }
            else
                System.err.println("Warning (SynthPanel): Null mapping for " + key + " (reduced to " + reduced + ")");                                        
            }
        
        synth.revise();

        if (sendsAllParameters)
            {
            synth.setSendMIDI(currentMIDI);
            synth.sendAllParameters();
            }
        // so we don't have independent updates in OS X
        repaint();
        }
    }
