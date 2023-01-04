/*** 
     Copyright 2017 by Sean Luke
     Licensed under the Apache License version 2.0
*/

package edisyn;

import java.util.*;
import java.io.*;
import java.util.prefs.*;
import java.awt.*;
import javax.swing.*;
import edisyn.util.*;

/**        
           @author Sean Luke
*/

public class MutationMap
    {
    // This is confusing, because although in the preferences
    // the data is stored as TRUE = free, FALSE = not free
    // In the map, the data is stored as STORED = not free
    // Because FREE needs to be the default, common situation
    
    HashSet<String> map = new HashSet<>();    
    Preferences prefs;
    public static final String EXTENSION = ".emu";
        
    // sets the last directory used by load, save, or save as
    public void setLastDirectory(String path, Synth synth) { synth.setLastX(path, "LastMutationDirectory", synth.getSynthClassName(), false); }
    // sets the last directory used by load, save, or save as
    public String getLastDirectory(Synth synth) { return synth.getLastX("LastMutationDirectory", synth.getSynthClassName(), false); }
    
    public boolean isUsingPrefs() { return prefs != null; }
    
    /** Returns whether the parameter is free to be mutated. */
    public boolean isFree(String key)
        {
        return !map.contains(key);
        }

    public void setFree(String key, boolean free)
        {
        setFree(key, free, true);
        }

    public void sync()
        {
        if (isUsingPrefs())
            {
            try 
                {
                prefs.sync();
                }
            catch (Exception ex)
                {
                Synth.handleException(ex);
                }
            }
        }
                
    public void setFree(String key, boolean free, boolean sync)
        {
        if (!free) map.add(key);
        else map.remove(key);

        if (isUsingPrefs())
            {
            prefs.put(key, "" + free);
            if (sync)
                {
                sync();
                }
            }
        }
    
    public MutationMap(MutationMap other)
        {
        this.prefs = other.prefs;
        this.map = (HashSet<String>)(other.map.clone());
        }

    public MutationMap(Preferences prefs)
        {      
        // do a load
        
        this.prefs = prefs;
        if (prefs != null)
            {
            try
                {
                String[] keys = prefs.keys();
                for(int i = 0; i < keys.length; i++)
                    {
                    // each String holds a PARAMETER                        
                    // each Value holds a BOOLEAN
                                
                    if (prefs.get(keys[i], "true").equals("false"))
                        {
                        map.add(keys[i]);
                        }
                    }
                }
            catch (Exception ex)
                {
                Synth.handleException(ex);
                }
            }
        }
        
    public void clear()
        {
        if (isUsingPrefs())
            {
            try
                {
                prefs.clear();
                }
            catch (Exception ex)
                {
                Synth.handleException(ex);
                }
            }
        map = new HashSet<String>();
        }

    public void loadParameters(Synth synth)
        {
        FileDialog fd = new FileDialog((Frame)(SwingUtilities.getRoot(synth)), "Load Mutation Parameters from File...", FileDialog.LOAD);
        fd.setFilenameFilter(new FilenameFilter()
            {
            public boolean accept(File dir, String name)
                {
                return StringUtility.ensureFileEndsWith(name, EXTENSION).equals(name);
                }
            });

        if (getLastDirectory(synth) != null)
            {
            fd.setDirectory(getLastDirectory(synth));
            }
        fd.setFile("" + synth.getSynthNameLocal() + EXTENSION);
                
        synth.disableMenuBar();
        fd.setVisible(true);
        synth.enableMenuBar();
        File f = null; // make compiler happy
        LineNumberReader is = null;
        if (fd.getFile() != null)
            {
            try
                {
                f = new File(fd.getDirectory(), fd.getFile());
                is = new LineNumberReader(new InputStreamReader(new FileInputStream(f)));
                                
                HashSet<String> keysin = new HashSet<>();
                while(true)
                    {
                    String line = is.readLine();
                    if (line == null) break;
                    line = line.trim();
                    if (line.length() == 0) continue;
                    keysin.add(line);
                    }
                                
                clear();
                String[] keys = synth.getModel().getKeys();
                for(int i = 0; i < keys.length; i++)
                    {
                    setFree(keys[i], keysin.contains(keys[i]), false);              // don't sync yet
                    }
                sync();
                }        
            catch (Throwable e) // fail  -- could be an Error or an Exception
                {
                synth.showErrorWithStackTrace(e, "File Error", "An error occurred while loading from the file.");
                Synth.handleException(e);
                }
            finally
                {
                if (is != null)
                    try { is.close(); }
                    catch (IOException e) { }
                }
            }
                
        synth.updateTitle();
        }
        
    public void saveParameters(Synth synth)
        {
        FileDialog fd = new FileDialog((Frame)(SwingUtilities.getRoot(synth)), "Save Mutation Parameters to File...", FileDialog.SAVE);
        
        if (getLastDirectory(synth) != null)
            {
            fd.setDirectory(getLastDirectory(synth));
            }
        fd.setFile("" + synth.getSynthNameLocal() + EXTENSION);
                    
        synth.disableMenuBar();
        fd.setVisible(true);
        synth.enableMenuBar();
        File f = null; // make compiler happy
        PrintWriter os = null;
        if (fd.getFile() != null)
            try
                {
                f = new File(fd.getDirectory(), StringUtility.ensureFileEndsWith(fd.getFile(), EXTENSION));
                os = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f)));

                String[] keys = synth.getModel().getKeys();
                for(int i = 0; i < keys.length; i++)
                    {
                    if (isFree(keys[i]))
                        os.println(keys[i]);
                    }

                setLastDirectory(fd.getDirectory(), synth);
                } 
            catch (IOException e) // fail
                {
                synth.showErrorWithStackTrace(e, "File Error", "An error occurred while saving to the file " + (f == null ? " " : f.getName()));
                Synth.handleException(e);
                }
            finally
                {
                if (os != null)
                    { 
                    os.close(); 
                    }
                }
        synth.updateTitle();
        }
    }
