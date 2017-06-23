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
    
    public Integer munge(int cc, int pane)
    	{
    	return Integer.valueOf((cc << 8) | pane);
    	}
    	
    public int cc(Integer munge)
    	{
    	if (munge == null) return -1;
    	else return munge.intValue() >> 8;
    	}
    	
    public int pane(Integer munge)
    	{
    	if (munge == null) return -1;
    	else return munge & 255;
    	}
    
    public String getKeyForCCPane(int cc, int pane)
    	{
    	return getKeyForInteger(munge(cc, pane));
    	}
    	
    /** Returns the model key for the given CC value, or null if there is none. */
	public String getKeyForInteger(Integer munge)
		{
		return (String)map.get(munge);
		}
	
    /** Returns the model key for the given CC value, or null if there is none. */
	public Integer getIntegerForKey(String key)
		{
		return (Integer)reverseMap.get(key);
		}
	
	public int getCCForKey(String key)
		{
		return cc(getIntegerForKey(key));
		}
		
	public int getPaneForKey(String key)
		{
		return pane(getIntegerForKey(key));
		}
    
    public void setKeyForCCPane(int cc, int pane, String key)
    	{
    	setKeyForInteger(munge(cc, pane), key);
    	}
    	
    /** Sets the model key for the given CC value, and syncs the Preferences (which isn't cheap). */
	public void setKeyForInteger(Integer munge, String key)
		{
		map.put(munge, key);
		reverseMap.put(key, munge);
		
		prefs.put("" + munge.intValue(), key);
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
	    		
	    		int munge = 0;
	    		try { munge = Integer.parseInt(keys[i]); }
	    		catch (Exception e) { e.printStackTrace(); }
	    		
	    		// each Value holds a MODEL KEY STRING
	    		
	    		map.put(Integer.valueOf(munge), prefs.get(keys[i], "-"));
	    		reverseMap.put(prefs.get(keys[i], "-"), Integer.valueOf(munge));
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
