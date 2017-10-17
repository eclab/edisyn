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
    HashMap typeMap = new HashMap();
    
    Preferences keyPrefs;
    Preferences typePrefs;
    
    public static final int NRPN_OFFSET = 256;
    
    public static final int TYPE_ABSOLUTE_CC = 0;
    public static final int TYPE_RELATIVE_CC_64 = 1;
    public static final int TYPE_RELATIVE_CC_0 = 2;
    public static final int TYPE_NRPN = 3;
    
    public Integer munge(int cc, int pane)
        {
        return Integer.valueOf((cc << 8) | pane);
        }
        
    public int cc(Integer munge)
        {
        if (munge == null) return -1;
        else return munge.intValue() >>> 8;
        }
        
    public int pane(Integer munge)
        {
        if (munge == null) return -1;
        else return munge & 255;
        }
        
    public int getTypeForCCPane(int cc, int pane)
        {
        Integer val = (Integer)(typeMap.get(munge(cc, pane)));
        if (val == null) return -1;
        else return (val.intValue());
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
        
    public void setTypeForCCPane(int cc, int pane, int type)
        {
        typeMap.put(munge(cc, pane), Integer.valueOf(type));
        typePrefs.put("" + munge(cc, pane).intValue(), "" + type);
        try 
            {
            typePrefs.sync();
            }
        catch (Exception ex)
            {
            ex.printStackTrace();
            }
        }
    
    /** Sets the model key for the given CC value, and syncs the Preferences (which isn't cheap). */
    public void setKeyForInteger(Integer munge, String key)
        {
        map.put(munge, key);
        reverseMap.put(key, munge);
                
        keyPrefs.put("" + munge.intValue(), key);
        try 
            {
            keyPrefs.sync();
            }
        catch (Exception ex)
            {
            ex.printStackTrace();
            }
        }

    
    public CCMap(Preferences keyPrefs, Preferences typePrefs)
        {
        this.keyPrefs = keyPrefs;
        this.typePrefs = typePrefs;
        
        // do a load
        try
            {
            String[] keys = keyPrefs.keys();
            for(int i = 0; i < keys.length; i++)
                {
                // each Key holds a CC INTEGER
                        
                int munge = 0;
                try { munge = Integer.parseInt(keys[i]); }
                catch (Exception e) { e.printStackTrace(); }
                        
                // each Value holds a MODEL KEY STRING
                        
                map.put(Integer.valueOf(munge), keyPrefs.get(keys[i], "-"));
                reverseMap.put(keyPrefs.get(keys[i], "-"), Integer.valueOf(munge));
                }
            }
        catch (Exception ex)
            {
            ex.printStackTrace();
            }

        try
            {
            String[] keys = typePrefs.keys();
            for(int i = 0; i < keys.length; i++)
                {
                // each Key holds a CC INTEGER
                        
                int munge = 0;
                try { munge = Integer.parseInt(keys[i]); }
                catch (Exception e) { e.printStackTrace(); }
                        
                // each Value holds a TYPE INTEGER

                int type = 0;
                try { type = Integer.parseInt(typePrefs.get(keys[i], "0")); }
                catch (Exception e) { e.printStackTrace(); }
                                                
                typeMap.put(Integer.valueOf(munge), Integer.valueOf(type));
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
            keyPrefs.clear();
            typePrefs.clear();
            }
        catch (Exception ex)
            {
            ex.printStackTrace();
            }
        map = new HashMap();
        reverseMap = new HashMap();
        }
    }
