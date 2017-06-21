/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn;

import java.util.*;
import java.io.*;

/**
   Storage for the various synthesizer parameters.  The parameters are each associated
   with a KEY (a string), and presently may take either INTEGER or STRING values.
   INTEGER values can be associated with a MINIMUM and a MAXIMUM to define a range.
   The MINIMUM and MAXIMUM are inclusive.  
   
   <p><b>Listeners</b>  You can register a listener to be called whenever the value
   of a specific parameter is updated.  Listeners should implement the Updatable
   interface.  A listener can also be registered to be called whenever <i>any</i>
   value is updated.
   
   <p><b>Mutation</b>  Sometimes you want to mutate or randomize parameters in some way.
   The Model provides some support to indicate how or whether parameters should be
   randomizable.  Specifically, parameters can be declared IMMUTABLE,
   meaning that they wish to resist being mutated or randomized; such parameters should
   be handled specially (via the Synth.immutableMutate() method) rather than simply
   randomized automatically by your code when the time comes.  Integer Parameters can also 
   have a list of VALUES that are declared SPECIAL.  This implies that these specific
   values should be more often chosen through randomization than other values; one approach
   is to (with 50% probability) choose one of those values, else choose any value in the range
   between minimum and maximum inclusive.
   
   <p><b>Defaults</b> You can also add a DEFAULT value for each parameter.  This allows you
   to reset all the parameters to default values, and to also print the parameters out which
   deviate from those defaults.  Note that the default value is not necessarily the same
   thing as the 'default' value that you provide when retrieve a parameter -- that is simply
   the value returned to indicate that no parameter existed in the model.
        
   @author Sean Luke
*/

public class Model
    {
    LinkedHashMap storage = new LinkedHashMap();
    HashMap min = new HashMap();
    HashMap max = new HashMap();
    HashMap listeners = new HashMap();
    HashSet immutable = new HashSet();
    HashMap special = new HashMap();
    HashMap defaults = new HashMap();
    
    String lastKey = null;

    public static final String ALL_KEYS = "ALL_KEYS";
    
    /** Register a listener to be notified whenever the value associated with the
        given key is updated.  If the key is ALL_KEYS, then the listener will
        be notified whenever any key is updated. */
    public void register(String key, Updatable component)
        {
        ArrayList list = (ArrayList)(listeners.get(key));
        if (list == null) 
            list = new ArrayList();
        list.add(component);
        listeners.put(key, list);
        }

    /** Returns all the keys in the model as an array. */        
    public String[] getKeys()
        {
        return (String[])(storage.keySet().toArray(new String[0]));
        }
        
    public String getLastKey()
    	{
    	return lastKey;
    	}
    
    /** Add the given integer as a default for the key. */
    public void addDefault(String key, int value)
        {
        defaults.put(key, Integer.valueOf(value));
        }

    /** Add the given String as a default for the key. */
    public void addDefault(String key, String value)
        {
        defaults.put(key, value);
        }
        
    /** Return the given default for the key (as an Integer or as a String), or null if there is none. */
    public Object getDefault(String key)
        {
        return defaults.get(key);
        }
        
    /** Return whether a default has been entered for the given key. */
    public boolean defaultExists(String key)
        {
        return (defaults.containsKey(key));
        }
        
    /** Adds a key with the given Integer value, or changes it to the given value. */        
    public void set(String key, int value)
        {
        if (key.equals("name"))
        	new Throwable().printStackTrace();
        storage.put(key, Integer.valueOf(value));
        ArrayList list = (ArrayList)(listeners.get(key));
        if (list != null)
            {
            for(int i = 0; i < list.size(); i++)
                {
                ((Updatable)(list.get(i))).update(key, this);
                }
            }
        list = (ArrayList)(listeners.get(ALL_KEYS));
        if (list != null)
            {
            for(int i = 0; i < list.size(); i++)
                {
                ((Updatable)(list.get(i))).update(key, this);
                }
            }
        lastKey = key;
        }
        

    /** Adds a key with the given String value, or changes it to the given value. */        
    public void set(String key, String value)
        {
        storage.put(key, value);
        ArrayList list = (ArrayList)(listeners.get(key));
        if (list != null)
            {
            for(int i = 0; i < list.size(); i++)
                {
                ((Updatable)(list.get(i))).update(key, this);
                }
            }
        list = (ArrayList)(listeners.get(ALL_KEYS));
        if (list != null)
            {
            for(int i = 0; i < list.size(); i++)
                {
                ((Updatable)(list.get(i))).update(key, this);
                }
            }
        lastKey = key;
        }
        
    public void resetToDefaults()
        {
        String[] keys = getKeys();
        for(int i = 0; i < keys.length; i++)
            {
            if (defaultExists(keys[i]))
                {
                if (isString(keys[i]))
                    {
                    set(keys[i], (String)getDefault(keys[i]));
                    }
                else
                    {
                    set(keys[i], ((Integer)getDefault(keys[i])).intValue());
                    }
                }
            }
        lastKey = null;
        }
                
    /** Returns an array of integer values associated with this
        (Integer) key which have been declared SPECIAL, meaning that they
        should be more commonly mutated to than other values. */        
    public int[] getSpecial(String key)
        {
        return (int[])special.get(key);
        }
                
    /** Sets an array of integer values associated with this
        (Integer) key which have been declared SPECIAL, meaning that they
        should be more commonly mutated to than other values. */        
    public void setSpecial(String key, int[] stuff)
        {
        special.put(key, stuff);
        }
                
    /** Sets a single value associated with this
        (Integer) key which has been declared SPECIAL, meaning that they
        it be more commonly mutated to than other values. */        
    public void setSpecial(String key, int stuff)
        {
        special.put(key, new int[] { stuff });
        }

    /** Returns the value associated with this
        (String) key, or ifDoesntExist if there is no such value. */        
    public String get(String key, String ifDoesntExist)
        {
        String d = (String) (storage.get(key));
        if (d == null) return ifDoesntExist;
        else return d;
        }

    /** Returns the value associated with this
        (Integer) key, or ifDoesntExist if there is no such value. */        
    public int get(String key, int ifDoesntExist)
        {
        Integer d = (Integer) (storage.get(key));
        if (d == null) return ifDoesntExist;
        else return d.intValue();
        }
              
              
    public Object get(String key) { return storage.get(key); }
      
    /** Returns whether the key is associated with a String. 
        If there is no key stored in the Model, then FALSE is returned. */        
    public boolean isString(String key)
        {
        if (!exists(key)) 
            return false;
        else return (storage.get(key) instanceof String);
        }
    
    /** Returns whether the key is stored in the model. */        
    public boolean exists(String key)
        {
        return storage.containsKey(key);
        }
                
    /** Returns whether a minimum is stored in the model for the key. */        
    public boolean minExists(String key)
        {
        return min.containsKey(key);
        }

    /** Returns whether a maximum is stored in the model for the key. */        
    public boolean maxExists(String key)
        {
        return max.containsKey(key);
        }

    /** Sets the minimum for a given key. */        
    public void setMin(String key, int value)
        {
        min.put(key, Integer.valueOf(value));
        }
                
    /** Sets the maximum for a given key. */        
    public void setMax(String key, int value)
        {
        max.put(key, Integer.valueOf(value));
        }
        
    /** Sets whether a given key is declared immutable. */        
    public void setImmutable(String key, boolean val)
        {
        if (val)
            immutable.add(key);
        else
            immutable.remove(key);
        }
                
    /** Returns whether a given key is declared immutable. */        
    public boolean isImmutable(String key)
        {
        return immutable.contains(key);
        }
                
    /** Returns the minimum for a given key, or 0 if no minimum is declared. */        
    public int getMin(String key)
        {
        Integer d = (Integer) (min.get(key));
        if (d == null) { System.err.println("Nonexistent min extracted for " + key); return 0; }
        else return d.intValue();
        }
                
    /** Returns the maximum for a given key, or 0 if no maximum is declared. */        
    public int getMax(String key)
        {
        Integer d = (Integer) (max.get(key));
        if (d == null) { System.err.println("Nonexistent max extracted for " + key); return 0; }
        else return d.intValue();
        }

	/** Print all the model parameters to stderr. */
    public void print()
        {
        PrintWriter pw = new PrintWriter(System.err);
        print(pw, false);
        pw.flush();
        }
                
	/** Print the model parameters to the given writer.   If diffsOnly, then only the model parameters which
		differ from the default will be printed. */
    public void print(PrintWriter out, boolean diffsOnly)
        {
        String[] keys = getKeys();
        for(int i = 0; i < keys.length; i++)
            {
            if (isString(keys[i]))
                {
                String str =  get(keys[i], "");
                if (diffsOnly && str.equals(getDefault(keys[i])))
                    continue;
                out.println(keys[i] + ": \"" + get(keys[i], "") + "\"    ");
                }
            else
                {
                int j = get(keys[i], 0);
                if (diffsOnly && getDefault(keys[i]) != null &&
                    j == ((Integer)(getDefault(keys[i]))).intValue())
                    continue;
                out.println(keys[i] + ": " + j + "    ");
                }
            }
        }
    }
