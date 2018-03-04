/*** 
     Copyright 2017 by Sean Luke
     Licensed under the Apache License version 2.0
*/

package edisyn;

import java.util.*;
import java.io.*;
import java.util.prefs.*;

/**        
           @author Sean Luke
*/

public class MutationMap
    {
    // This is confusing, because although in the preferences
    // the data is stored as TRUE = free, FALSE = not free
    // In the map, the data is stored as STORED = not free
    // Because FREE needs to be the default, common situation
    HashSet map = new HashSet();
    
    Preferences prefs;

    /** Returns whether the parameter is free to be mutated. */
    public boolean isFree(String key)
        {
        return !map.contains(key);
        }

    public void setFree(String key, boolean free)
        {
        if (!free) map.add(key);
        else map.remove(key);

        prefs.put(key, "" + free);
        try 
            {
            prefs.sync();
            }
        catch (Exception ex)
            {
            ex.printStackTrace();
            }
        }
    
    public MutationMap(Preferences prefs)
        {        
        // do a load
        
        this.prefs = prefs;
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
            ex.printStackTrace();
            }
        }
        
    public void clear()
        {
        try
            {
            prefs.clear();
            }
        catch (Exception ex)
            {
            ex.printStackTrace();
            }
        map = new HashSet();
        }
    }
