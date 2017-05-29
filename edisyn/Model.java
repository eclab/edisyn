/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn;

import java.util.*;

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
   
        
   @author Sean Luke
*/

public class Model
    {
     HashMap storage = new HashMap();
     HashMap min = new HashMap();
     HashMap max = new HashMap();
     HashMap listeners = new HashMap();
     HashSet immutable = new HashSet();
     HashMap special = new HashMap();

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
        
	/** Adds a key with the given Integer value, or changes it to the given value. */        
    public void set(String key, int value)
        {
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
        }
        

	/** Adds a key with the given String value, or changes it to the given value. */        
    public void set(String key, String value)
        {
        //System.err.println("" + key + " --> " + value);
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
	    (String) key, or _default if there is no such value. */        
    public String get(String key, String _default)
        {
        String d = (String) (storage.get(key));
        if (d == null) return _default;
        else return d;
        }

	/** Returns the value associated with this
	    (Integer) key, or _default if there is no such value. */        
    public int get(String key, int _default)
        {
        Integer d = (Integer) (storage.get(key));
        if (d == null) return _default;
        else return d.intValue();
        }
                
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

	/** Prints the model to stderr for debugging. */        
    public void print()
        {
        String[] keys = getKeys();
        for(int i = 0; i < keys.length; i++)
            {
            System.err.print(keys[i] + ": ");
            if (isString(keys[i]))
                {
                System.err.print("\"" + get(keys[i], "") + "\"    ");
                }
            else
                {
                System.err.print(get(keys[i], 0) + "    ");
                if (minExists(keys[i]) || maxExists(keys[i]))
                    {
                    System.err.print("(");
                    if (minExists(keys[i]))
                        System.err.print(getMin(keys[i]));
                    System.err.print(" - ");
                    if (maxExists(keys[i]))
                        System.err.print(getMax(keys[i]));
                    System.err.print(")    ");
                    }
                }
            if (isImmutable(keys[i]))
                System.err.print("I    ");
            if (getSpecial(keys[i]) != null)
                {
                int[] s = getSpecial(keys[i]);
                System.err.print("S: ");
                for(int j = 0; j < s.length; j++)
                    {
                    System.err.print(s[j]);
                    System.err.print(" ");
                    }
                }
            System.err.println();
            }
        }
    }
