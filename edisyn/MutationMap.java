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
    
    public void printKeys()
        {
        for(String s : map)
            System.err.println(s);
        }
    
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
    
    boolean loadParameters(Synth synth, File file, String error)
        {
        LineNumberReader is = null;
        try
            {
            is = new LineNumberReader(new InputStreamReader(new FileInputStream(file)));
                                                
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
            synth.showErrorWithStackTrace(e, "Error Auto-Loading Mutation Parameters", error);                      /// "An error occurred while loading from the file.");
            Synth.handleException(e);
            return false;
            }
        finally
            {
            if (is != null)
                try { is.close(); }
                catch (IOException e) { }
            }
        return true;
        }
    
    public void autoLoadParameters(Synth synth)
        {
        String filename = synth.getLastX("MutationFile", synth.getSynthClassName());
        if (filename == null) return;
        filename = filename.trim();
        if (filename.length() == 0) return;

        // Okay, let's try it
        try
            {
            boolean retval = loadParameters(synth, new File(filename), "For the synth editor " + synth.getSynthNameLocal() + 
                "\nThere was an error auto-loading the mutation parameters file:\n        " + filename +
                "\nEdisyn will no longer try to auto-load this file.");
            if (!retval)
                {
                Synth.removeLastX("MutationFile", synth.getSynthClassName(), true);
                }
            }
        catch (Exception ex)
            {
            System.err.println(ex);
            Synth.handleException(ex);
            }
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
        boolean retval = false;
        if (fd.getFile() != null)
            {
            f = new File(fd.getDirectory(), StringUtility.ensureFileEndsWith(fd.getFile(), EXTENSION));
            retval = loadParameters(synth, f, "An error occurred while loading from the file.");
            }
        if (retval)
            {
            if (synth.showSimpleConfirm("Auto-Load Mutation Parameters",
                    "Auto-Load this mutation parameters file each time you load this patch editor?", "Auto-Load", "Don't Auto-Load"))
                {
                try
                    {
                    Synth.setLastX(f.getCanonicalPath(), "MutationFile", synth.getSynthClassName(), true);
                    }
                catch (IOException ex)
                    {
                    synth.showErrorWithStackTrace(ex, "File Error", "An error occurred while setting up auto-loading of the file\n" + (f == null ? " " : f.getName()));
                    }
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
                
                if (synth.showSimpleConfirm("Auto-Load Mutation Parameters",
                        "Auto-Load this mutation parameters file each time you load this patch editor?", "Auto-Load", "Don't Auto-Load"))
                    {
                    Synth.setLastX(f.getCanonicalPath(), "MutationFile", synth.getSynthClassName(), true);
                    }
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
