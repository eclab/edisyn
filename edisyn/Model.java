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
   
   <p><b>Mutation</b>.  To assist in mutating or crossing over parameters, the model
        
   @author Sean Luke
*/

public class Model implements Cloneable
    {
    LinkedHashMap storage = new LinkedHashMap();
    HashMap min = new HashMap();
    HashMap max = new HashMap();
    HashMap listeners = new HashMap();
    HashMap metricMin = new HashMap();
    HashMap metricMax = new HashMap();
    HashMap status = new HashMap();
    Undo undoListener = null;
    
    String lastKey = null;

    public static final String ALL_KEYS = "ALL_KEYS";
    
    public Undo getUndoListener() { return undoListener; }
    public void setUndoListener(Undo up) { undoListener = up; }
    
    /** Returns TRUE with the given probability. */
    boolean coinToss(Random random, double probability)
        {
        if (probability==0.0) return false;     // fix half-open issues
        else if (probability==1.0) return true; // fix half-open issues
        else return random.nextDouble() < probability; 
        }
        
    /** Produces a random value in the fully closed range [a, b]. */
    int randomValueWithin(Random random, int a, int b)
        {
        if (a > b) { int swap = a; a = b; b = swap; }
        if (a == b) return a;
        int range = (b - a + 1);
        return a + random.nextInt(range);
        }

    /** Produces a random value in the fully closed range [a, b],
        choosing from a uniform distribution of size +- weight * (a-b),
        centered at *center*, and rounded to the nearest integer. */
    int randomValueWithin(Random random, int a, int b, int center, double weight)
        {
        if (a > b) { int swap = a; a = b; b = swap; }
        if (a == b) return a;
        
        double range = (b - a + 1);  // 0.5 on each side
        
        // pick a random number from -1...+1
        double delta = 0.0;
        while(true)
            {
            delta = (random.nextDouble() * 2 - 1) * weight * range / 2.0;
            if ((center + delta) > (a - 0.5) &&
                (center + delta) < (b + 0.5))
                break;
            }
        int result = (int)(Math.round(center + delta));
            
        // slight tweak if we're at the same result
        if (result == center && coinToss(random, weight))
            {
            while(true)
                {
                int delta2 = (coinToss(random, 0.5) ? 1 : -1);
                if ((center + delta2) >= a && 
                    (center + delta2) <= b)
                    { result = center + delta2; break; }
                }
            }
        return result;
        }


    /** Mutates (potentially) all keys.
    	Mutation works as follows.  For each key, we first see if we're permitted to mutate it
        (no immutable status, no strings).  If so, we divide the range into the METRIC and NON-METRIC
        regions.  If it's all NON-METRIC, then with WEIGHT probability we will pick a new
        value at random (else stay).  Else if we're in a non-metric region, with 0.5 chance we'll
        pick a new random non-metric value with WEIGHT probability (else stay), and with 0.5 chance we'll 
        pick a completely random metric value with WEIGHT probability (else stay). Else if we're in a metric 
        region, with 0.5 chance we will pick a non-metric value with WEIGHT probability (else stay), and with 
        0.5 chance we will do a METRIC MUTATION.
                
        <p>A metric mutation selects under a uniform rectangular distribution centered at the current value.
        The rectangular distribution is the delta function when WEIGHT is 0.0 and is the full range from
        metric min to metric max inclusive when WEIGHT is 1.0.  We repeat this selection until we get a value
        within metric min and metric max.
    */
    public void mutate(Random random, double weight)
    	{
		mutate(random, getKeys(), weight);
    	}
        
    /** Mutates (potentially) the keys provided.
    	Mutation works as follows.  For each key, we first see if we're permitted to mutate it
        (no immutable status, no strings).  If so, we divide the range into the METRIC and NON-METRIC
        regions.  If it's all NON-METRIC, then with WEIGHT probability we will pick a new
        value at random (else stay).  Else if we're in a non-metric region, with 0.5 chance we'll
        pick a new random non-metric value with WEIGHT probability (else stay), and with 0.5 chance we'll 
        pick a completely random metric value with WEIGHT probability (else stay). Else if we're in a metric 
        region, with 0.5 chance we will pick a non-metric value with WEIGHT probability (else stay), and with 
        0.5 chance we will do a METRIC MUTATION.
                
        <p>A metric mutation selects under a uniform rectangular distribution centered at the current value.
        The rectangular distribution is the delta function when WEIGHT is 0.0 and is the full range from
        metric min to metric max inclusive when WEIGHT is 1.0.  We repeat this selection until we get a value
        within metric min and metric max.
    */
    public void mutate(Random random, String[] keys, double weight)
        {
        if (undoListener!= null)
        	{
        	undoListener.push(this);
        	undoListener.setWillPush(false);
        	}
        	
        for(int i = 0; i < keys.length; i++)
            {
            // continue if the key is immutable, it's a string, or we fail the coin toss
            if (getStatus(keys[i]) == STATUS_IMMUTABLE) continue;

            
            boolean hasMetric = false;                                      // do we even HAVE a metric range?
            boolean doMetric = false;                                       // are we in that range, and should mutate within it?
            boolean pickRandomInMetric = false;                     // are we NOT in that range, but should maybe go to a random value in it?
                        
            if (metricMinExists(keys[i]) &&
                metricMaxExists(keys[i]))
                {
                hasMetric = true;
                if (getMetricMax(keys[i]) == getMax(keys[i]) &&
                    getMetricMin(keys[i]) == getMin(keys[i])) // has no non-metric
                    {
                    doMetric = true;
                    }
                else if (get(keys[i], 0) >= getMetricMin(keys[i]) &&
                    get(keys[i], 0) <= getMetricMax(keys[i]))               // we're within metric range
                    {
                    if (coinToss(random, 0.5))
                        doMetric = true;                                // we will stay in the metric range and mutate within it (versus jump out)
                    }
                else    // we're outside the metric range
                    {
                    if (coinToss(random, 0.5))
                        pickRandomInMetric = true;              // we are out of the metric range but may go inside it (versus stay outside)
                    }
                }
            else    // there is no metric range
                {
                // do nothing.
                }
                                
            // now perform the operation
                
            if (doMetric)                                                   // definitely do a metric mutation
                {
                int a = getMetricMin(keys[i]);
                int b = getMetricMax(keys[i]);
                set(keys[i], randomValueWithin(random, getMetricMin(keys[i]), getMetricMax(keys[i]), get(keys[i], 0), weight));
                }
            else if (pickRandomInMetric)                    // MAYBE jump into metric
                {
                if (coinToss(random, weight))
                    {
                    set(keys[i], randomValueWithin(random, getMetricMin(keys[i]), getMetricMax(keys[i])));
                    }
                }
            else if (hasMetric)                                             // MAYBE choose a random new non-metric location
                {
                if (coinToss(random, weight))
                    {
                    int lowerRange = getMetricMin(keys[i]) - getMin(keys[i]);
                    int upperRange = getMax(keys[i]) - getMetricMax(keys[i]);
                    int delta = random.nextInt(lowerRange + upperRange);
                    if (delta < lowerRange)
                        {
                        set(keys[i], getMin(keys[i]) + delta);
                        }
                    else
                        {
                        set(keys[i], getMetricMax(keys[i]) + 1 + (delta - lowerRange));
                        }
                    }
                }
            else                                                                    // MAYBE choose a random new non-metric location (easiest because there is no metric location)
                {
                if (coinToss(random, weight))
                    {
                    set(keys[i], randomValueWithin(random, getMin(keys[i]), getMax(keys[i])));
                    }
                }
            }

        if (undoListener!= null)
        	{
        	undoListener.setWillPush(true);
        	}
        }



    /** Recombines (potentially) all keys.  
    	Recombination works as follows.  For each key, we first see if we're permitted to mutate it
        (no immutable status, other model doesn't have the key).  Next with 1.0 - WEIGHT probability 
        we don't recombine at all. Otherwise we recombine:
                
        <p>If the parameter is a string, we keep our value.  
        If the parameter is an integer, and we have a metric range,
        and BOTH our value AND the other model's value are within that range, then we do 
        metric crossover: we pick a random new value between the two values inclusive.
        Otherwise with 0.5 probability we select our parameter, else the other model's parameter.
    */
    public void recombine(Random random, Model model, double weight)
    	{
    	recombine(random, model, getKeys(), weight);
    	}

    /** Recombines (potentially the keys provided.  
    	Recombination works as follows.  For each key, we first see if we're permitted to mutate it
        (no immutable status, other model doesn't have the key).  Next with 1.0 - WEIGHT probability 
        we don't recombine at all. Otherwise we recombine:
                
        <p>If the parameter is a string, we keep our value.  
        If the parameter is an integer, and we have a metric range,
        and BOTH our value AND the other model's value are within that range, then we do 
        metric crossover: we pick a random new value between the two values inclusive.
        Otherwise with 0.5 probability we select our parameter, else the other model's parameter.
    */
    public void recombine(Random random, Model model, String[] keys, double weight)
        {
        if (undoListener!= null)
        	{
        	undoListener.push(this);
        	undoListener.setWillPush(false);
        	}
        	
        for(int i = 0; i < keys.length; i++)
            {
            // return if the key doesn't exist, is immutable or is a string, or we fail the coin toss
            if (!model.exists(keys[i])) continue;
            if (getStatus(keys[i]) == STATUS_IMMUTABLE) continue;
            if (coinToss(random, 1.0 - weight)) continue;
            
            // is it a string?
            if (isString(keys[i]))
                { 
                if (coinToss(random, 0.5))
                    set(keys[i], model.get(keys[i], ""));
                }
            else
                {
                // we cross over metrically if we're both within the metric range
                if (metricMinExists(keys[i]) &&
                    metricMaxExists(keys[i]) &&
                    get(keys[i], 0) >= getMetricMin(keys[i]) &&
                    get(keys[i], 0) <= getMetricMax(keys[i]) &&
                    model.get(keys[i], 0) >= getMetricMin(keys[i]) &&
                    model.get(keys[i], 0) <= getMetricMax(keys[i])) 
                    {
                    int a = get(keys[i], 0);
                    int b = model.get(keys[i], a);
                    set(keys[i], randomValueWithin(random, a, b));
                    }
                else // we pick one or the other
                    {
                    if (coinToss(random, 0.5))
                        set(keys[i], model.get(keys[i], 0));
                    }
                }
            }

        if (undoListener!= null)
        	{
        	undoListener.setWillPush(true);
        	}
        }
    
    
    public Object clone()
        {
        Model model = null;
        try { model = (Model)(super.clone()); }
        catch (Exception e) { e.printStackTrace(); }  // never happens
        model.storage = (LinkedHashMap)(storage.clone());
        model.min = (HashMap)(min.clone());
        model.max = (HashMap)(max.clone());
        model.listeners = (HashMap)(listeners.clone());
        model.status = (HashMap)(status.clone());
        model.metricMin = (HashMap)(metricMin.clone());
        model.metricMax = (HashMap)(metricMax.clone());
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
        if (!status.equals(model.status))
            return false;
        if (!metricMin.equals(model.metricMin))
            return false;
        if (!metricMax.equals(model.metricMax))
            return false;
        // don't care about lastKey
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

    /** Returns all the keys in the model as an array, except the hidden ones. */        
    public String[] getKeys()
        {
        String[] keyset = (String[])(storage.keySet().toArray(new String[0]));
        ArrayList revisedKeys = new ArrayList<String>();
        for(int i = 0; i < keyset.length; i++)
        	if (getStatus(keyset[i]) != STATUS_HIDDEN)
        		revisedKeys.add(keyset[i]);
        return (String[])(revisedKeys.toArray(new String[0]));
        }
        
    public void clearLastKey()
        {
        lastKey = null;
        }
        
    public String getLastKey()
        {
        return lastKey;
        }
    
    boolean debugSet = false; // true;
    
    /** Adds a key with the given Integer value, or changes it to the given value. */        
    public void set(String key, int value)
        {
        if (debugSet) System.err.println(key + " --> " + value);
        // when do we push on the undo stack?
        if (
            undoListener != null &&         // when we have an undo listener AND
            !key.equals(lastKey) &&         // when the key is not the lastKey AND
                (!exists(key) ||                        // the key doesn't exist OR
                !isInteger(key) ||                     // the value isn't an integer OR
                value != get(key, 0)))         // the value doesn't match the current value 
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
        if (debugSet) System.err.println(key + " --> " + value);

        // when do we push on the undo stack?
        if (
            undoListener != null &&         // when we have an undo listener AND
            !key.equals(lastKey) &&         // when the key is not the lastKey AND
                (!exists(key) ||                        // the key doesn't exist OR
                !isString(key) ||                      // the value isn't a string OR
                !value.equals(get(key, null)))) // the value doesn't match the current value 
            undoListener.push(this);
        storage.put(key, value);
        lastKey = key;
        updateListenersForKey(key);
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
    
    /** Returns the value associated with this (Integer) key, or -1 if there is no such value. 
    	If there is no such value, also prints (does not throw) a RuntimeError stacktrace.  */        
    public int get(String key)
    	{
        Integer d = (Integer) (storage.get(key));
        if (d == null)	
        	{
        	new RuntimeException("No Value stored for key " + key + ", returning -1, which is certainly wrong.").printStackTrace();
            return -1;
        	}
        else return d.intValue();
    	}
              
    Object getValue(String key) { return storage.get(key); }
      
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
        
    /** Returns whether a metric minimum is stored in the model for the key. */        
    public boolean metricMinExists(String key)
        {
        return metricMin.containsKey(key);
        }

    /** Returns whether a metric maximum is stored in the model for the key. */        
    public boolean metricMaxExists(String key)
        {
        return metricMax.containsKey(key);
        }

    /** Sets the metric minimum for a given key. */        
    public void setMetricMin(String key, int value)
        {
        metricMin.put(key, Integer.valueOf(value));
        }
                
    /** Sets the metric maximum for a given key. */        
    public void setMetricMax(String key, int value)
        {
        metricMax.put(key, Integer.valueOf(value));
        }
    
    public static final int STATUS_FREE = 0;
    public static final int STATUS_IMMUTABLE = 1;
    public static final int STATUS_RESTRICTED = 2;
    public static final int STATUS_HIDDEN = 3;

    /** Sets the status of a key.  The default is STATUS_FREE, except for strings, which are STATUS_IMMUTABLE. */        
    public void setStatus(String key, int val)
        {
        status.put(key, Integer.valueOf(val));
        }
                
    /** Returns whether a given key is declared immutable.  Strings are ALWAYS immutable and you don't need to set them. */        
    public int getStatus(String key)
        {
        if (status.containsKey(key))
        	return ((Integer)(status.get(key))).intValue();
        else if (!exists(key))
        	return STATUS_IMMUTABLE;
        else if (isString(key))
			return STATUS_IMMUTABLE;
		else // it's a number
			return STATUS_FREE;
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

    /** Returns the metric minimum for a given key, or 0 if no minimum is declared. */        
    public int getMetricMin(String key)
        {
        Integer d = (Integer) (metricMin.get(key));
        if (d == null) { System.err.println("Nonexistent metricMin extracted for " + key); return 0; }
        else return d.intValue();
        }
                
    /** Returns the metric maximum for a given key, or 0 if no maximum is declared. */        
    public int getMetricMax(String key)
        {
        Integer d = (Integer) (metricMax.get(key));
        if (d == null) { System.err.println("Nonexistent metricMax extracted for " + key); return 0; }
        else return d.intValue();
        }
        
    /** Deletes the metric min and max for a key */
    public void removeMetricMinMax(String key)
    	{
    	metricMin.remove(key);
    	metricMax.remove(key);
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
                    other.getValue(keys[i]).equals(getValue(keys[i])))  // they're the same
                    continue;
                String str =  get(keys[i], "");
                out.println(keys[i] + ": \"" + get(keys[i], "") + "\"    ");
                }
            else if (isInteger(keys[i]))
                {
                if (other.isInteger(keys[i]) &&
                    other.getValue(keys[i]).equals(getValue(keys[i])))  // they're the same
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
