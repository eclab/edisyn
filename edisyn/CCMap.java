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

public class CCMap
    {
    HashMap map = new HashMap();
    HashMap reverseMap = new HashMap();
    
    Preferences prefs;
    
    /** Returns the model key for the given CC value, or null if there is none. */
	public String getKeyForCC(int cc)
		{
		return (String)map.get(Integer.valueOf(cc));
		}
	
	public int getCCForKey(String key)
		{
		Integer val = (Integer)(reverseMap.get(key));
		if (val == null) return -1;
		else return val.intValue();
		}
    
    /** Sets the model key for the given CC value, and syncs the Preferences (which isn't cheap). */
	public void setKeyForCC(int cc, String key)
		{
		map.put(Integer.valueOf(cc), key);
		reverseMap.put(key, Integer.valueOf(cc));
		
		prefs.put("" + cc, key);
        try 
            {
            prefs.sync();
            }
        catch (Exception ex)
            {
            ex.printStackTrace();
            }
		}
    
    public CCMap(Preferences prefs)
    	{
    	this.prefs = prefs;
    	
    	// do a load
    	try
    		{
	    	String[] keys = prefs.keys();
	    	for(int i = 0; i < keys.length; i++)
	    		{
	    		// each Key holds a CC INTEGER
	    		
	    		int cc = 0;
	    		try { cc = Integer.parseInt(keys[i]); }
	    		catch (Exception e) { e.printStackTrace(); }
	    		
	    		// each Value holds a MODEL KEY STRING
	    		
	    		map.put(Integer.valueOf(cc), prefs.get(keys[i], "-"));
	    		reverseMap.put(prefs.get(keys[i], "-"), Integer.valueOf(cc));
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
    	map = new HashMap();
    	reverseMap = new HashMap();
    	}
    }
