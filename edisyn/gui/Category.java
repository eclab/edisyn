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
import edisyn.util.*;

/**
   A pretty container for widgets to categorize them

   @author Sean Luke
*/

public class Category extends JPanel implements Gatherable
    {        
    // we used to be a JComponent but now we're a JPanel because JPanels have concrete AccessibleContexts
         
    // even with modification for the insets, the text position value seems to be off by a bit
    final static int STRING_WIDTH_COMPENSATION = 10;
    // In Nimbus on Windows, the font widths are wrong
    final static int EXTRA_WINDOWS_COMPENSATION = 15;

    Color color;
    Synth synth;
    String name = "";
    
    String preamble;
    String preamble2;
    String distributePreamble;
    boolean pasteable = false;
    boolean distributable = false;
    boolean sendsAllParameters = false;
    Gatherable auxillary = null;
    
    JMenuItem copy = new JMenuItem("Copy Category");
    JMenuItem paste = new JMenuItem("Paste Category");
    JMenuItem distribute = new JMenuItem("Distribute");
    JMenuItem copyFromMutable = new JMenuItem("Copy Category (Mutation Parameters Only)");
    JMenuItem pasteToMutable = new JMenuItem("Paste Category (Mutation Parameters Only)");
    JMenuItem distributeToMutable = new JMenuItem("Distribute (Mutation Parameters Only)");
    JMenuItem reset = new JMenuItem("Reset Category");
    JMenuItem rand25 = new JMenuItem("Randomize by 25%");
    JMenuItem rand50 = new JMenuItem("Randomize by 50%");
    JMenuItem rand75 = new JMenuItem("Randomize by 75%");
    JMenuItem rand100 = new JMenuItem("Randomize by 100%");
        
    public void makePasteable(String preamble) { pasteable = true; this.preamble = preamble; }
    public void makePasteable(String preamble, String preamble2) { pasteable = true; this.preamble = preamble; }
    public void makeDistributable(String preamble) { distributable = true; this.distributePreamble = preamble; }
    public void makeUnresettable() 
        { 
        reset.setEnabled(false); 
        rand25.setEnabled(false);
        rand50.setEnabled(false);
        rand75.setEnabled(false);
        rand100.setEnabled(false);
        }
    public void setSendsAllParameters(boolean val) { sendsAllParameters = val; }
    public boolean getSendsAllParameters() { return sendsAllParameters; }
    
    /** Returns an auxillary component.  Sometimes a Category is broken into two pieces
        (see KawaiK5 Harmonics (DHG) category for example), and when we gather elements,
        we want to gather from the auxillary as well. */
    public Gatherable getAuxillary() { return auxillary; }
    /** Sets an auxillary component.  Sometimes a Category is broken into two pieces
        (see KawaiK5 Harmonics (DHG) category for example), and when we gather elements,
        we want to gather from the auxillary as well. */
    public void setAuxillary(Gatherable comp) { auxillary = comp; }
    
    JPopupMenu pop = new JPopupMenu();
    int stringWidth;
      
    public boolean isPasteCompatible(String preamble)
        {
        String copyPreamble = synth.getCopyPreamble();
        String myPreamble = preamble;
        if (copyPreamble == null) return false;
        if (myPreamble == null) return false;

        return (pasteable && 
            StringUtility.reduceFirstDigitsAfterPreamble(copyPreamble, "").equals(StringUtility.reduceFirstDigitsAfterPreamble(myPreamble, "")));
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
        boolean currentMIDI = synth.getSendMIDI();
        synth.setSendMIDI(false);
                
        Synth other = Synth.instantiate(synth.getClass(), true, true, synth.tuple);
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
                    System.err.println("Key missing in model : " + key);
                }
            }               

        synth.setSendMIDI(currentMIDI);
        if (sendsAllParameters)
            {
            synth.sendAllParameters();
            }
        // so we don't have independent updates in OS X
        repaint();
        }
        

    void randomizeCategory(double weight)
        {
        boolean currentMIDI = synth.getSendMIDI();
        synth.setSendMIDI(false);
        
        ArrayList keys = new ArrayList();
        
        // get all the components
        ArrayList components = new ArrayList();
        gatherAllComponents(components);
        for(int i = 0; i < components.size(); i++)
            {
            if (components.get(i) instanceof HasKey)
                {
                HasKey nc = (HasKey)(components.get(i));
                String key = nc.getKey();
                if (synth.getModel().exists(key))
                    {
                    if (synth.getModel().isString(key))
                        {
                        // do nothing
                        }
                    else
                        {
                        keys.add(key);
                        }
                    }
                else
                    System.err.println("Key missing in model : " + key);
                }
            }               

        // mutate 
        String[] k = (String[])(keys.toArray(new String[0]));
        synth.setModel(synth.getModel().mutate(synth.getRandom(), k, weight));
                
        // emit
        synth.setSendMIDI(currentMIDI);
        if (sendsAllParameters)
            {
            synth.sendAllParameters();
            }
        // so we don't have independent updates in OS X
        repaint();
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
        
    
    void pasteCategory(boolean includeImmutable)        // ugly hack    
        {
        for(int i = 0; i < synth.getNumberOfPastes(); i++)
            pasteCategory1(includeImmutable);
        }
        
    void pasteCategory1(boolean includeImmutable)
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
                String reduced = StringUtility.reduceFirstDigitsAfterPreamble(key, myPreamble);
                reduced = StringUtility.reduceDigitsInPreamble(reduced, myPreamble);
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
            String reduced = StringUtility.reduceFirstDigitsAfterPreamble(key, copyPreamble);
            reduced = StringUtility.reduceDigitsInPreamble(reduced, copyPreamble);
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
                    System.err.println("Warning (Category) 2: Key didn't exist " + mapped);
                }
            else
                System.err.println("Warning (Category) 2: Null mapping for " + key + " (reduced to " + reduced + ")");                                        
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
        
    void distributeCategory(boolean includeImmutable)
        {
        Model model = synth.getModel();
        String lastKey = model.getLastKey();

        if (lastKey != null)
            {
            boolean currentMIDI = synth.getSendMIDI();
            if (sendsAllParameters)
                {
                synth.setSendMIDI(false);
                }
                
            String lastReduced = StringUtility.reduceAllDigitsAfterPreamble(lastKey, distributePreamble);

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
                    String reduced = StringUtility.reduceAllDigitsAfterPreamble(key, distributePreamble);
                               
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
                            System.err.println("Warning (Category): Key didn't exist " + key);
                        }
                    else
                        System.err.println("Warning (Category): Null mapping for " + key + " (reduced to " + reduced + ")");                                        
                    }
                }
        
            synth.revise();

            if (sendsAllParameters)
                {
                synth.setSendMIDI(currentMIDI);
                synth.sendAllParameters();
                }
            }
        // so we don't have independent updates in OS X
        repaint();
        }

    public int MIN_EXTRA_LINE = 20;
            
    public Dimension getMinimumSize() 
        {
        Dimension d = super.getMinimumSize();
        
        Graphics2D graphics = (Graphics2D)getGraphics();
        if (graphics == null) return d;
        
        if (stringWidth == 0)
            stringWidth = STRING_WIDTH_COMPENSATION + (Style.isWindows() ? EXTRA_WINDOWS_COMPENSATION : 0) + 
                graphics.getFontMetrics(Style.CATEGORY_FONT()).stringWidth(name);

        d.width = ((int)Math.max(d.width, stringWidth + MIN_EXTRA_LINE));
        return d;
        }
    
    public Dimension getPreferredSize() 
        { 
        Dimension d = super.getPreferredSize();
        Dimension d2 = getMinimumSize();
        d.width = ((int)Math.max(d.width, d2.width));
        return d;
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
                    if (!synth.isShowingMutation())
                        {
                        if (e.getY() < 20 &&
                            (stringWidth == 0 || e.getX() < stringWidth)
                            // && (((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) || 
                            //     ((e.getModifiers() & InputEvent.SHIFT_MASK) == InputEvent.SHIFT_MASK))
                            )
                            {
                            copy.setEnabled(pasteable);
                            copyFromMutable.setEnabled(pasteable);
                            paste.setEnabled(pasteable && isPasteCompatible(preamble));
                            pasteToMutable.setEnabled(pasteable && isPasteCompatible(preamble));
                            distribute.setEnabled(distributable && canDistributeKey());
                            distributeToMutable.setEnabled(distributable && canDistributeKey());
                                                
                            // we add, then remove the popup because I've discovered (in the Korg Wavestation SR Sequence Editor)
                            // that if the popup is pre-added, then it takes quite a while to dynamically add or remove categories.
                            //Category.this.add(pop);
                            pop.show(e.getComponent(), e.getX(), e.getY());
                            //Category.this.remove(pop);
                            }
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
        
        pop.addSeparator();
        pop.add(rand25);
        rand25.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                randomizeCategory(0.5);             // sqrt(0.25)
                }
            });
        pop.add(rand50);
        rand50.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                randomizeCategory(0.7);     // ~sqrt(0.5)
                }
            });
        pop.add(rand75);
        rand75.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                randomizeCategory(0.85);    // ~sqrt(0.75)
                }
            });
        pop.add(rand100);
        rand100.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                randomizeCategory(1.0);
                }
            });
                
        copy.setEnabled(false);
        copyFromMutable.setEnabled(false);
        paste.setEnabled(false);
        pasteToMutable.setEnabled(false);
        distribute.setEnabled(false);
        distributeToMutable.setEnabled(false);
        reset.setEnabled(true);
        rand25.setEnabled(true);
        rand50.setEnabled(true);
        rand75.setEnabled(true);
        rand100.setEnabled(true);
                
        //Category.this.add(pop);
        pop.setDefaultLightWeightPopupEnabled(false);
        }
    
    public Insets getInsets() 
        { 
        Insets insets = (Insets)(super.getInsets().clone());
        insets.bottom = 0;
        return insets;
        }
    
    public String getName()
        {
        return name;
        }
        
    public void setName(String label)
        {
        if (label == null) label = "";
        getAccessibleContext().setAccessibleName(label);
        this.name = label;
        stringWidth = 0;                // reset
        
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
            (label == null || label.equals("") ? "" : " " + label + " "),
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
    
    void gatherAllComponents(Container cont, java.util.ArrayList list)
        {
        Component[] c = cont.getComponents();
        for(int i = 0; i < c.length; i++)
            {
            list.add(c[i]);
            if (c[i] instanceof Gatherable)
                ((Gatherable)c[i]).gatherAllComponents(list);
            else if (c[i] instanceof JPanel)
                gatherAllComponents(((JPanel)c[i]), list);
            else if (c[i] instanceof Box)       // just in case
                gatherAllComponents(((Box)c[i]), list);
            }
        }
        
    public void gatherAllComponents(java.util.ArrayList list)
        {
        gatherAllComponents(this, list);
        if (auxillary != null)
            {
            auxillary.gatherAllComponents(list);
            }
        }
    
    public void paintComponent(Graphics g)
        {
        Graphics2D graphics = (Graphics2D) g;
        RenderingHints oldHints = graphics.getRenderingHints();
        Style.prepareGraphics(graphics);
 
        if (stringWidth == 0)
            stringWidth = STRING_WIDTH_COMPENSATION + (Style.isWindows() ? EXTRA_WINDOWS_COMPENSATION : 0) + 
                graphics.getFontMetrics(Style.CATEGORY_FONT()).stringWidth(name);


        Rectangle rect = getBounds();
        rect.x = 0;
        rect.y = 0;
        graphics.setPaint(Style.BACKGROUND_COLOR());
        graphics.fill(rect);

        graphics.setRenderingHints(oldHints);
        }
    }
