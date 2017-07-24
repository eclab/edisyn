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
    
    public int getCCForKey(String key)
        {
    	String ckey = (String) reverseMap.get(key);
    	if (ckey == null) return 0;
    	String[] parts = ckey.split("_");
    	
    	return Integer.parseInt(parts[1]); // return cc value
    	}

    public String getKeyForCCChannelPane(int cc, int channel, int pane)
    {
    	String skey = ""+cc+"_"+channel+"_"+pane;
		return (String) map.get(skey);
	}
    
    public void setKeyForCCChannelPane(int cc, int channel, int pane, String key)
        {
    	String ckey = ""+cc+"_"+channel+"_"+pane;
        map.put(ckey, key);
        reverseMap.put(key, ckey);
    	
        prefs.put(ckey, key);
        }
    
    public CCMap(Preferences prefs)
        {
        this.prefs = prefs;

        try {	        
	        for(String key : prefs.keys()) {
	            map.put(key, prefs.get(key, "-"));
	            reverseMap.put(prefs.get(key, "-"), key);	        	
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
