/***
    Copyright 2019 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.gui;

import edisyn.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.prefs.*;

public class Favorites
    {
    public static final int DEFAULT_MAXIMUM = 10;
    int maximum;
    ArrayList<String> top = new ArrayList<String>();
        
    public boolean contains(String synthName)
        {
        for(int i = 0; i < top.size(); i++)
            {
            if (synthName.equals(top.get(i)))
                return true;
            }
        return false;
        }

    void buildFromPreferences()
        {
        top = new ArrayList<String>();
        Preferences prefs = Prefs.getGlobalPreferences("Top");
        for(int i = maximum - 1; i >= 0; i--)
            {
            String t = prefs.get(("" + i), null);
            if (t != null)
                {
                t = t.trim();
                if (t.length() != 0)
                    {
                    _add(t);
                    }
                }
            }    
        }       
    
    void dumpToPreferences()
        {
        Preferences global_p = Prefs.getGlobalPreferences("Top");
                
        for(int i = maximum - 1; i >= 0; i--)
            {
            global_p.remove(("" + i));
            }
                        
        for(int i = top.size() - 1; i >= 0; i--)
            {
            global_p.put(("" + i), top.get(i));
            }
                
//              try { global_p.flush(); global_p.sync(); } catch (Exception ex) { Synth.handleException(ex); }
        Prefs.save(global_p);
        }       

    void _add(String synthName)
        {
        top.remove(synthName);                  // if it's already there
        if (top.size() == maximum)
            top.remove(top.size() - 1);  // remove top element
        top.add(0, synthName);
        }

    public void store(String synthName)
        {
        buildFromPreferences();
        _add(synthName);
        dumpToPreferences();
        }
        
    public void clearAll()
        {
        top = new ArrayList<String>();
        dumpToPreferences();
        }

    public void clear(String synthName)
        {
        buildFromPreferences();
        top.remove(synthName);
        dumpToPreferences();
        }
        
    public ArrayList<String> getAll()
        {
        return (ArrayList<String>)(top.clone());
        }
        
    public static Synth doNewSynthDialog()
        {
        Favorites f = new Favorites();
                
        final String[] synthNames = Synth.getSynthNames();
        final JComboBox combo2 = new JComboBox(new String[0]);   
        combo2.setMaximumRowCount(24);
        
        final ArrayList<String> sortedTop = (ArrayList<String>)(f.top.clone());
        Collections.sort(sortedTop);
        
        int[] synthIndices = new int[sortedTop.size()];
        String[] synthFavs = new String[sortedTop.size() + 1];
        if (sortedTop.size() == 0)
            synthFavs[sortedTop.size()] = "<html><i>Select a synthesizer below...<i></html>";
        else
            synthFavs[sortedTop.size()] = "<html><i>Select another synthesizer...<i></html>";
        for(int j = 0; j < sortedTop.size(); j++)
            {
            for(int k = 0; k < synthNames.length; k++)              // yeah yeah, O(n^2)
                {
                final String fav = sortedTop.get(j);
                if (synthNames[k].equals(fav))
                    {
                    synthIndices[j] = k;
                    synthFavs[j] = fav;
                    }
                }
            }
        final JComboBox combo1 = new JComboBox(synthFavs);
        combo1.setMaximumRowCount(synthFavs.length);
        combo1.addItemListener(new ItemListener()
            {
            public void itemStateChanged(ItemEvent event) 
                {
                if (event.getStateChange() == ItemEvent.SELECTED)       // not interested in deselection events
                    {
                    if (combo1.getSelectedIndex() == sortedTop.size())
                        {
                        combo2.setModel(new DefaultComboBoxModel(synthNames));
                        combo2.setEnabled(true);  // it's select another synth
                        }
                    else
                        {
                        combo2.setModel(new DefaultComboBoxModel(new String[0]));
                        combo2.setEnabled(false);  // it's select another synth
                        }
                    }
                }
            });
                        
        if (f.top.size() > 0)
            {
            // find the last synth
            for(int i = 0; i < sortedTop.size(); i++)
                {
                if (sortedTop.get(i).equals(f.top.get(0)))
                    {
                    combo1.setSelectedIndex(i);
                    break;
                    }
                }
            combo2.setModel(new DefaultComboBoxModel(new String[0]));
            combo2.setEnabled(false);  // it's select another synth
            }
        else
            {
            combo2.setModel(new DefaultComboBoxModel(synthNames));
            combo2.setEnabled(true);  // it's select another synth
            }

        int result = Synth.showMultiOption(null,
            new String[] { "Recent", "All Synths" },
            new JComboBox[] { combo1, combo2 },
            new String[] { "Open", "Quit", "Disconnected" },
            0, 
            "Edisyn",
            "Select a synthesizer to edit");
        if (result == -1 || result == 1)                // cancelled
            return null;
                        
        int synthnum = combo2.getSelectedIndex();
        if (combo1.getSelectedIndex() < sortedTop.size())
            {
            synthnum = synthIndices[combo1.getSelectedIndex()];
            }
                
        f.store(synthNames[synthnum]);
        return Synth.instantiate(Synth.getClassNames()[synthnum], false, (result == 0), null);
        }
        
        
    public JMenu buildNewSynthMenu(final Synth synth)
        {
        JMenu newSynth = new JMenu("New Synth");
        loadNewSynthMenu(newSynth, synth);
        return newSynth;
        }
        
        
    void loadNewSynthMenu(final JMenu newSynth, final Synth synth)
        {
        newSynth.removeAll();
                
        ArrayList<String> sortedTop = (ArrayList<String>)(top.clone());
        Collections.sort(sortedTop);
        
        boolean hasFavorite = false;
        final String[] synthNames = synth.getSynthNames();
        for(int j = 0; j < sortedTop.size(); j++)
            {
            for(int k = 0; k < synthNames.length; k++)              // yeah yeah, O(n^2)
                {
                final String fav = sortedTop.get(j);
                if (synthNames[k].equals(fav))
                    {
                    hasFavorite = true;
                    final int _k = k;
                    JMenuItem synthMenu = new JMenuItem(synthNames[k]);
                    synthMenu.addActionListener(new ActionListener()
                        {
                        public void actionPerformed(ActionEvent e)
                            {
                            store(fav);
                            synth.doNewSynth(_k);
                            }
                        });
                    newSynth.add(synthMenu);
                    }
                }
            }
        
        if (hasFavorite)
            newSynth.addSeparator();

		JMenu synthMakeMenu = null;
        final String[] synthMakes = synth.getSynthMakes();
		
        for(int i = 0; i < synthNames.length; i++)
            {
            final int _i = i;
            if (synthMakeMenu == null || !synthMakes[i].equals(synthMakeMenu.getText()))
            	synthMakeMenu = new JMenu(synthMakes[i]);
            JMenuItem synthMenu = new JMenuItem(synthNames[i]);
            synthMenu.addActionListener(new ActionListener()
                {
                public void actionPerformed(ActionEvent e)
                    {
                    store(synthNames[_i]);
                    synth.doNewSynth(_i);
                    }
                });
            synthMakeMenu.add(synthMenu);
            newSynth.add(synthMakeMenu);		 // will be added multiple times but that's okay?
            }

        newSynth.addSeparator();
        
        JMenuItem clearOne = new JMenuItem("Clear " + synth.getSynthNameLocal());
        clearOne.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                clear(synth.getSynthNameLocal());
                loadNewSynthMenu(newSynth, synth);          // reload menu
                }
            });
        newSynth.add(clearOne);
        JMenuItem clearAll = new JMenuItem("Clear All Recent Synths");
        clearAll.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                clearAll();
                loadNewSynthMenu(newSynth, synth);          // reload menu
                }
            });
        newSynth.add(clearAll);
        }
        
    public Favorites()
        {
        this(DEFAULT_MAXIMUM);
        }

    public Favorites(int maximum)
        {
        this.maximum = maximum;
        buildFromPreferences();
        }
    }
