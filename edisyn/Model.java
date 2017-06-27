/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn;

import java.util.*;
import java.io.*;
import edisyn.gui.*;

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

public class Model implements Cloneable
    {
    LinkedHashMap storage = new LinkedHashMap();
    HashMap min = new HashMap();
    HashMap max = new HashMap();
    HashMap listeners = new HashMap();
    HashSet immutable = new HashSet();
    HashMap special = new HashMap();
    Undo undoListener = null;
    
    String lastKey = null;

    public static final String ALL_KEYS = "ALL_KEYS";
    
    public Undo getUndoListener() { return undoListener; }
    public void setUndoListener(Undo up) { undoListener = up; }
    
    
    public Object clone()
    	{
    	Model model = null;
    	try { model = (Model)(super.clone()); }
    	catch (Exception e) { }
    	model.storage = (LinkedHashMap)(storage.clone());
    	model.min = (HashMap)(min.clone());
    	model.max = (HashMap)(max.clone());
    	model.listeners = (HashMap)(listeners.clone());
    	model.immutable = (HashSet)(immutable.clone());
    	model.special = (HashMap)(special.clone());
    	model.lastKey = null;
    	return model;
    	}
    
    public boolean equals(Object other)
    	{
    	if (other == null || !(other instanceof Model))
    		return false;
    	Model model = (Model) other;
    	if (!storage.equals(model.storage))
    		return false;
    	if (!min.equals(model.min))
    		return false;
    	if (!max.equals(model.max))
    		return false;
    	if (!listeners.equals(model.listeners))
    		return false;
    	if (!immutable.equals(model.immutable))
    		return false;
    	if (!special.equals(model.special))
    		return false;
    	if (!lastKey.equals(model.lastKey))
    		return false;
    	return true;
    	}


    public boolean keyEquals(Model other)
    	{
    	if (other == null)
    		return false;
    	if (!storage.equals(other.storage))
    		return false;
    	return true;
    	}
    
    public void updateAllListeners()
    	{
     	String[] keys = getKeys();
    	for(int i = 0; i < keys.length; i++)
			{
			updateListenersForKey(keys[i]);
			}
	   	}
    
    public void copyValuesTo(Model model)
    	{
    	model.storage.clear();
    	model.storage.putAll(storage);
    	model.updateAllListeners();
    	model.lastKey = null;
    	}
    	
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
        
    public void clearLastKey()
    	{
    	lastKey = null;
    	}
    	
    public String getLastKey()
    	{
    	return lastKey;
    	}
            
    /** Adds a key with the given Integer value, or changes it to the given value. */        
    public void set(String key, int value)
        {
        // when do we push on the undo stack?
        if (
        	undoListener != null && 	// when we have an undo listener AND
        	!key.equals(lastKey) &&		// when the key is not the lastKey AND
        	(!exists(key) ||			// the key doesn't exist OR
        	 !isInteger(key) ||			// the value isn't an integer OR
        	 value != get(key, 0))) 	// the value doesn't match the current value 
	        	undoListener.push(this);
        storage.put(key, Integer.valueOf(value));
        lastKey = key;
        updateListenersForKey(key);
        }
        
    public void setBounded(String key, int value)
    	{
    	if (isString(key))
    		return;
    		
    	if (minExists(key))
    		{
    		int min = getMin(key);
    		if (value < min)
    			value = min;
    		}
    		
    	if (maxExists(key))
    		{
    		int max = getMax(key);
    		if (value > max)
    			value = max;
    		}
    	
    	set(key, value);
    	}
    
    public void updateListenersForKey(String key)
    	{
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
        // when do we push on the undo stack?
        if (
        	undoListener != null && 	// when we have an undo listener AND
        	!key.equals(lastKey) &&		// when the key is not the lastKey AND
        	(!exists(key) ||			// the key doesn't exist OR
        	 !isString(key) ||			// the value isn't a string OR
        	 !value.equals(get(key, null)))) // the value doesn't match the current value 
	        	undoListener.push(this);
        storage.put(key, value);
        lastKey = key;
        updateListenersForKey(key);
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
    

    /** Returns whether the key is associated with an integer. 
        If there is no key stored in the Model, then FALSE is returned. */        
    public boolean isInteger(String key)
        {
        if (!exists(key)) 
            return false;
        else return (storage.get(key) instanceof Integer);
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

	public int getRange(String key)
		{
		if (minExists(key) && maxExists(key))
			{
			return getMax(key) - getMin(key) + 1;
			}
		else return 0;
		}

	/** Print to the given writer those model parameters for which the provided "other" model
		does not have identical values.
	 */
    public void printDiffs(PrintWriter out, Model other)
        {
        String[] keys = getKeys();
        for(int i = 0; i < keys.length; i++)
            {
            if (isString(keys[i]))
                {
                if (other.isString(keys[i]) &&
                	other.get(keys[i]).equals(get(keys[i])))  // they're the same
                		continue;
                String str =  get(keys[i], "");
                out.println(keys[i] + ": \"" + get(keys[i], "") + "\"    ");
                }
            else if (isInteger(keys[i]))
                {
                if (other.isInteger(keys[i]) &&
                	other.get(keys[i]).equals(get(keys[i])))  // they're the same
                		continue;
                int j = get(keys[i], 0);
                out.println(keys[i] + ": " + j + "    ");
                }
            else
            	{
            	out.println(keys[i] + ": FOREIGN OBJECT " + get(keys[i]));
            	}
            }
        }
	/** Print the model parameters to the given writer.   If diffsOnly, then only the model parameters which
		differ from the default will be printed. */
    public void print(PrintWriter out)
        {
        String[] keys = getKeys();
        for(int i = 0; i < keys.length; i++)
            {
            if (isString(keys[i]))
                {
                String str =  get(keys[i], "");
                out.println(keys[i] + ": \"" + get(keys[i], "") + "\"    ");
                }
            else if (isInteger(keys[i]))
                {
                int j = get(keys[i], 0);
                out.println(keys[i] + ": " + j + "    ");
                }
            else
            	{
            	out.println(keys[i] + ": FOREIGN OBJECT " + get(keys[i]));
            	}
            }
        }
    }
