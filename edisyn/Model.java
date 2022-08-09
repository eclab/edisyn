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
    public static boolean debug = false;

    public static final int STATUS_UNSET = 0;                           // I haven't set a status for this yet.  For Strings this will result in IMMUTABLE and for ints, FREE
    public static final int STATUS_FREE = 1;                            // The parameter can be mutated
    public static final int STATUS_IMMUTABLE = 2;                       // The parameter cannot be mutated
    public static final int STATUS_RESTRICTED = 3;                      // The parameter cannot be mutated and shouldn't appear in getKeys()
    public static final String ALL_KEYS = "ALL_KEYS";                           // The "key" which registers a listener with all keys

    // The actual value storage.  It's a linked hash map so data iterates in a rational format that I can guess (basically the order in which it was added originally)
    LinkedHashMap<String, Node> storage = new LinkedHashMap<String, Node>();

    // The last key which was set
    String lastKey = null;
    
    // A new key was set since the last time checkIfRecentlySet() was called 
    boolean recentlySet = false;
    
    // The listeners for ALL_KEYS
    ArrayList<Updatable> allKeysListeners = new ArrayList<Updatable>();

    // The undo listener
    Undo undoListener = null;
    public double [] latentVector = null;
    
    // The fixer
    Synth fixer = null;
    /** Sets the Model's fixer. This Synth ALWAYS has its fix(String, Model) method called on every
    	key that is updated for any reason, regardless of whether updateListeners is called.  By default
    	Synth.fix(String, Model) does nothing at all.  The fixer was introduced to revise the min/max range of
    	parameters due to a change in a given parameter.  For example, in the Proteus 2000, changing a 
    	ROM parameter will radically change the min/max values of the available instruments (for example)
    	in the ROM.  Assuming the ROM parameter gets mutated or changed first, we can modify the min/max
    	values of the instruments so that they can be changed afterwards within the proper range. 
    	By default the fixer is null, so nothing is called or changed. */
    public void setFixer(Synth val) { fixer = val; }

    // Listeners should be updated when a key is modified
    boolean updateListeners = true;

    class Node
        {
        ArrayList<Updatable> listeners = null;          // when listeners is empty, we set it to null to save a tiny bit o space
        String stringValue = null;                      // if stringValue == null, this is an INTEGER VALUE, else it is a STRING VALUE
        int intValue;
        int status = STATUS_UNSET;                                              // If STATUS_UNSET, then if we're a string, we are IMMUTABLE, else we are FREE
        int min;
        int max;
        int metricMin;
        int metricMax;
        boolean hasMin = false;
        boolean hasMax = false;
        boolean hasMetricMin = false;
        boolean hasMetricMax = false;
        
        String listenersToString()
            {
            String s = "";
            if (listeners != null)
                for(int i = 0; i < listeners.size(); i++)
                    {
                    s += " " + listeners.get(i);
                    }
            return s;
            }
                
        public String toString()
            {
            return
                (stringValue == null ? 
                "[val: " + intValue +
                " min: " + (hasMin ? min : "-") +
                " max: " + (hasMin ? max : "-") +
                " mmin: " + (hasMetricMin ? metricMin : "-") +
                " mmax: " + (hasMetricMax ? metricMax : "-") +
                " stat: " + (status == STATUS_UNSET ? "UNSET" : (status == STATUS_FREE ? "FREE" : (status == STATUS_IMMUTABLE ? "IMMUT" : "RESTR"))) +
                listenersToString() 
                : "[val: \"" + stringValue + "\"" + listenersToString()) + "]";
            }
        
        public Node() { }
        
        // copy constructor
        public Node(Node node, boolean includeListeners)
            {
            intValue = node.intValue;
            stringValue = node.stringValue;
            min = node.min;
            max = node.max;
            metricMin = node.metricMin;
            metricMax = node.metricMax;
            hasMin = node.hasMin;
            hasMax = node.hasMax;
            hasMetricMin = node.hasMetricMin;
            hasMetricMax = node.hasMetricMax;
            status = node.status;
            if (includeListeners)
                {
                if (node.listeners != null) 
                    {
                    listeners = new ArrayList<Updatable>(node.listeners);
                    }
                }
            }
        
        public boolean equals(Object obj)
            {
            if (obj == null) return false;
            if (!(obj instanceof Node)) return false;
            Node other = (Node) obj;
                                
            // check values
            if (!keyAndBoundsEquals(other)) return false;

            // check status and listeners
            if (other.status != status) return false;
            int otherNum = (other.listeners == null ? 0 : other.listeners.size());
            int num = (listeners == null ? 0 : listeners.size());
            if (otherNum != num) return false;
            if (listeners != null && !listeners.equals(other.listeners)) return false;
            return true;
            }
        
        public boolean keyAndBoundsEquals(Node other)
            {
            if (!keyEquals(other)) return false;
            if (other.hasMin != hasMin) return false;
            if (other.hasMax != hasMax) return false;
            if (other.hasMetricMin != hasMetricMin) return false;
            if (other.hasMetricMax != hasMetricMax) return false;
            if (hasMin && (other.min != min)) return false;
            if (hasMax && (other.max != max)) return false;
            if (hasMetricMin && (other.metricMin != metricMin)) return false;
            if (hasMetricMax && (other.metricMax != metricMax)) return false;
            return true;
            }

        public boolean keyEquals(Node other)
            {
            // check for strings
            if (other.stringValue == null && stringValue != null) return false;
            if (other.stringValue != null && stringValue == null) return false;
            if (stringValue != null && !stringValue.equals(other.stringValue)) return false;

            // it's not a string, check int
            if (other.intValue != intValue) return false;
            return true;            
            }
        }

    /** Returns the undo listener.  This listener not really a listener: it's an Undo.
        The model is pushed onto the Undo when a value is set(...). */
    public Undo getUndoListener() { return undoListener; }
    
    /** Sets the undo listener.  This listener not really a listener: it's an Undo.
        The model is pushed onto the Undo when a value is set(...). */
    public void setUndoListener(Undo up) { undoListener = up; }

    /** Returns whether to update listeners when a key's value is set. */        
    public boolean getUpdateListeners() { return updateListeners; }

    /** Sets whether to update listeners when a key's value is set. */        
    public void setUpdateListeners(boolean val) { updateListeners = val; }
    
    /** Returns all the ALL_KEYS listeners. */        
    public ArrayList<Updatable> getAllKeysListeners() { return allKeysListeners; }





    ///// SETTING
    
    /** Adds a key with the given Integer value, or changes it to the given value. */        
    public void set(String key, int value)
        {
        if (debug)
            {
            System.err.println("Debug (Model):" + key + " --> " + value );
            if (!exists(key))
                System.err.println("Debug (Model): " + "Key " + key + " was NEW");
            }
            
        // when do we push on the undo stack?
        if (undoListener != null &&         // when we have an undo listener AND
            !key.equals(lastKey) &&         // when the key is not the lastKey AND
                (!exists(key) ||                        // the key doesn't exist OR
                !isInteger(key) ||                     // the value isn't an integer OR
                value != get(key, 0)))         // the value doesn't match the current value 
            undoListener.push(this);
            
        Node n = storage.get(key);
        if (n == null) { n = new Node(); storage.put(key, n); }
        n.stringValue = null;
        n.intValue = value;
        
        lastKey = key;
        recentlySet = true;
        updateListenersForKey(key);
        }

    /** Adds a key with the given String value, or changes it to the given value. */        
    public void set(String key, String value)
        {
        if (debug)
            {
            if (debug) System.err.println("Debug (Model): " + key + " --> " + value);
            if (!exists(key))
                System.err.println("Debug (Model): " + "Key " + key + " was NEW");
            }

        // when do we push on the undo stack?
        if (
            undoListener != null &&         // when we have an undo listener AND
            !key.equals(lastKey) &&         // when the key is not the lastKey AND
                (!exists(key) ||                        // the key doesn't exist OR
                !isString(key) ||                      // the value isn't a string OR
                !value.equals(get(key, null)))) // the value doesn't match the current value 
            undoListener.push(this);

        Node n = storage.get(key);
        if (n == null) { n = new Node(); storage.put(key, n); }
        n.stringValue = value;

        lastKey = key;
        recentlySet = true;
        updateListenersForKey(key);
        }
    
    
    /** Sets a value, but bounds it to be within min and max limits */
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

    /** Checks if any parameter in the model has been recently set.
        If so, returns true.
        In any event, resets the recently set flag. */
    public boolean checkIfRecentlySet()
        {
        boolean val = recentlySet;
        recentlySet = false;
        return val;
        }
    
    /** Clears the last key set. */
    public void clearLastKey()
        {
        lastKey = null;
        }
    
    /** Returns the last key set. */
    public String getLastKey()
        {
        return lastKey;
        }
        
    /** Sets the minimum for a given key. */        
    public void setMin(String key, int value)
        {
        Node node = storage.get(key);
        if (node == null) { System.err.println("Warning (Model): " + "min set for non-existent value " + key);  node = new Node(); storage.put(key, node); }
        node.min = value;
        node.hasMin = true;
        }

    /** Sets the maximum for a given key. */        
    public void setMax(String key, int value)
        {
        Node node = storage.get(key);
        if (node == null) { System.err.println("Warning (Model): " + "max set for non-existent value " + key); node = new Node(); storage.put(key, node); }
        node.max = value;
        node.hasMax = true;
        }
        
    /** Sets the minimum and maximum for a given key. */        
    public void setMinMax(String key, int min, int max)
        {
        Node node = storage.get(key);
        if (node == null) { System.err.println("Warning (Model): " + "min and max set for non-existent value " + key); node = new Node(); storage.put(key, node); }
        node.min = min;
        node.max = max;
        node.hasMin = true;
        node.hasMax = true;
        }
       
    /** Sets the metric minimum for a given key. */        
    public void setMetricMin(String key, int value)
        {
        Node node = storage.get(key);
        if (node == null) { System.err.println("Warning (Model): " + "metric min set for non-existent value " + key); node = new Node(); storage.put(key, node); }
        node.metricMin = value;
        node.hasMetricMin = true;
        }
                
    /** Sets the metric maximum for a given key. */        
    public void setMetricMax(String key, int value)
        {
        Node node = storage.get(key);
        if (node == null) { System.err.println("Warning (Model): " + "metric max set for non-existent value " + key); node = new Node(); storage.put(key, node); }
        node.metricMax = value;
        node.hasMetricMax = true;
        }
    
    /** Sets the metric minimum and maximum for a given key. */        
    public void setMetricMinMax(String key, int min, int max)
        {
        Node node = storage.get(key);
        if (node == null) { System.err.println("Warning (Model): " + "metric min/max set for non-existent value " + key); node = new Node(); storage.put(key, node); }
        node.metricMin = min;
        node.metricMax = max;
        node.hasMetricMin = true;
        node.hasMetricMax = true;
        }

    /** Sets the minimum and maximum and metric minimum and maximum for a given key. */        
    public void setMinMaxMetricMinMax(String key, int min, int max, int metricMin, int metricMax)
        {
        Node node = storage.get(key);
        if (node == null) { System.err.println("Warning (Model): " + "min/max and metric min/max set for non-existent value " + key); node = new Node(); storage.put(key, node); }
        node.min = min;
        node.max = max;
        node.metricMin = metricMin;
        node.metricMax = metricMax;
        node.hasMin = true;
        node.hasMax = true;
        node.hasMetricMin = true;
        node.hasMetricMax = true;
        }
            
    /** Sets the status of a key.  The default is STATUS_FREE, except for strings, which are STATUS_IMMUTABLE. */        
    public void setStatus(String key, int val)
        {
        Node node = storage.get(key);
        if (node == null) { System.err.println("Warning (Model): " + "status set for non-existent value " + key); node = new Node(); storage.put(key, node); }
        node.status = val;
        }
 
    /** Deletes the metric min and max for a key */
    public void removeMinMax(String key)
        {
        Node node = storage.get(key);
        if (node == null) { System.err.println("Warning (Model): " + "min/max removed for non-existent value " + key); return; }
        node.hasMin = false;
        node.hasMax = false;
        }

    /** Deletes the metric min and max for a key */
    public void removeMetricMinMax(String key)
        {
        Node node = storage.get(key);
        if (node == null) { System.err.println("Warning (Model): " + "metric min/max removed for non-existent value " + key); return; }
        node.hasMetricMin = false;
        node.hasMetricMax = false;
        }

               





    
    ///// GETTING
            

    /** Returns all the keys in the model as an array, except the hidden ones. */        
    public String[] getKeys()
        {
        /*
          String[] keyset = (String[])(storage.keySet().toArray(new String[0]));
          ArrayList revisedKeys = new ArrayList<String>();
          for(int i = 0; i < keyset.length; i++)
          if (getStatus(keyset[i]) != STATUS_RESTRICTED)
          revisedKeys.add(keyset[i]);
          return (String[])(revisedKeys.toArray(new String[0]));
        */
        return getDifferentKeys(null);
        }
        
    /** Returns all the keys in the model as an array, except the hidden ones,
        which are different from the ones in the other model. If other model
        is null, all keys are returned.  */        
    public String[] getDifferentKeys(Model other)
        {
        String[] keyset = (String[])(storage.keySet().toArray(new String[0]));
        ArrayList revisedKeys = new ArrayList<String>();
        for(int i = 0; i < keyset.length; i++)
            if (getStatus(keyset[i]) != STATUS_RESTRICTED)
                {
                if (other == null || (!keyEquals(keyset[i], other)))
                    revisedKeys.add(keyset[i]);
                }
        return (String[])(revisedKeys.toArray(new String[0]));
        }
        
 
    public boolean keyEquals(String key, Model other)
        {
        if (isString(key))
            {
            return get(key, "").equals(other.get(key, ""));
            }
        else
            {
            return get(key, -1) == other.get(key, -1);
            }
        }
 
    /** Returns the value associated with this
        (String) key, or ifDoesntExist if there is no such value. */        
    public String get(String key, String ifDoesntExist)
        {
        Node node = storage.get(key);
        if (node == null)
            {
            if (debug)
                System.err.println("Debug (Model): " + "Key " + key + " does not exist");
            return ifDoesntExist;
            }
        else if (node.stringValue == null)
            {
            if (debug)
                System.err.println("Debug (Model): " + "Key " + key + " is an integer, not a string");
            return ifDoesntExist;
            }
        else return node.stringValue;
        }

    /** Returns the value associated with this
        (Integer) key, or ifDoesntExist if there is no such value. */        
    public int get(String key, int ifDoesntExist)
        {
        Node node = storage.get(key);
        if (node == null)
            {
            if (debug)
                System.err.println("Debug (Model): " + "Key " + key + " does not exist");
            return ifDoesntExist;
            }
        else if (node.stringValue != null)
            {
            if (debug)
                System.err.println("Debug (Model): " + "Key " + key + " is a string, not an integer");
            return ifDoesntExist;
            }
        else return node.intValue;
        }
    
    /** Returns the value associated with this (Integer) key, or -1 if there is no such value. 
        If there is no such value, also prints (does not throw) a RuntimeError stacktrace.  */        
    public int get(String key)
        {
        return get(key, -1);
        }
              
    public Object getValue(String key) 
        {
        Node node = storage.get(key);
        if (node == null) return null;
        else if (node.stringValue == null)
            return Integer.valueOf(node.intValue);
        else
            return node.stringValue;
        }
      
    /** Returns whether the key is associated with a String. 
        If there is no key stored in the Model, then FALSE is returned. */        
    public boolean isString(String key)
        {
        Node node = storage.get(key);
        if (node == null) return false;
        else return node.stringValue != null;
        }
    
    /** Returns whether the key is associated with an integer. 
        If there is no key stored in the Model, then FALSE is returned. */        
    public boolean isInteger(String key)
        {
        Node node = storage.get(key);
        if (node == null) return false;
        else return node.stringValue == null;
        }

    /** Returns whether the key is stored in the model. */        
    public boolean exists(String key)
        {
        return storage.containsKey(key);
        }
                
    /** Returns whether a minimum is stored in the model for the key. */        
    public boolean minExists(String key)
        {
        Node node = storage.get(key);
        if (node == null) return false;
        return node.hasMin;
        }

    /** Returns whether a maximum is stored in the model for the key. */        
    public boolean maxExists(String key)
        {
        Node node = storage.get(key);
        if (node == null) return false;
        return node.hasMax;
        }

    /** Returns whether a metric minimum is stored in the model for the key. */        
    public boolean metricMinExists(String key)
        {
        Node node = storage.get(key);
        if (node == null) return false;
        return node.hasMetricMin;
        }

    /** Returns whether a metric maximum is stored in the model for the key. */        
    public boolean metricMaxExists(String key)
        {
        Node node = storage.get(key);
        if (node == null) return false;
        return node.hasMetricMax;
        }
    
    /** Returns whether a given key is declared immutable.  Strings are ALWAYS immutable by default and numbers are ALWAYS free by default. */        
    public int getStatus(String key)
        {
        Node node = storage.get(key);
        if (node == null)
            return STATUS_IMMUTABLE;
        else if (node.status == STATUS_UNSET)
            {
            if (node.stringValue != null)
                return STATUS_IMMUTABLE;
            else
                return STATUS_FREE;
            }
        else return node.status;
        }
                
    /** Returns the minimum for a given key, or 0 if no minimum is declared. */        
    public int getMin(String key)
        {
        Node d = storage.get(key);
        if (d == null) { Synth.handleException(new Throwable("Warning (Model): " + "Nonexistent node extracted for min for " + key)); return 0; }
        if (!d.hasMin) { Synth.handleException(new Throwable("Warning (Model): " + "Nonexistent min extracted for " + key)); return 0; }
        else return d.min;
        }
                
    /** Returns the maximum for a given key, or 0 if no maximum is declared. */        
    public int getMax(String key)
        {
        Node d = storage.get(key);
        if (d == null) { Synth.handleException(new Throwable("Warning (Model): " + "Nonexistent node extracted for max for " + key)); return 0; }
        if (!d.hasMax) { Synth.handleException(new Throwable("Warning (Model): " + "Nonexistent max extracted for " + key)); return 0; }
        else return d.max;
        }

    /** Returns the metric minimum for a given key, or 0 if no minimum is declared. */        
    public int getMetricMin(String key)
        {
        Node d = storage.get(key);
        if (d == null) { Synth.handleException(new Throwable("Warning (Model): " + "Nonexistent node extracted for metric min for " + key)); return 0; }
        if (!d.hasMetricMin) { Synth.handleException(new Throwable("Warning (Model): " + "Nonexistent metric min extracted for " + key)); return 0; }
        else return d.metricMin;
        }
                
    /** Returns the metric maximum for a given key, or 0 if no maximum is declared. */        
    public int getMetricMax(String key)
        {
        Node d = storage.get(key);
        if (d == null) { Synth.handleException(new Throwable("Warning (Model): " + "Nonexistent node extracted for metric max for " + key)); return 0; }
        if (!d.hasMetricMax) { Synth.handleException(new Throwable("Warning (Model): " + "Nonexistent metric max extracted for " + key)); return 0; }
        else return d.metricMax;
        }
    
    /** Returns getMax(key) - getMin(key) + 1 */
    public int getRange(String key)
        {
        if (minExists(key) && maxExists(key))
            {
            return getMax(key) - getMin(key) + 1;
            }
        else return 0;
        }

 
 
        
    //// COPYING    
        
        
    /** Exactly duplicates the model, including listeners */
    public Object clone()
        {
        Model m = null;
        try
            {
            m = (Model)(super.clone());
            }
        catch (CloneNotSupportedException ex)
            {
            // do nothing, never happens
            }
        
        // reload keys
        m.storage = new LinkedHashMap<String, Node>();
        String[] keyset = (String[])(storage.keySet().toArray(new String[0]));
        for(int i = 0; i < keyset.length; i++)
            {
            m.storage.put(keyset[i], new Node(storage.get(keyset[i]), true));
            }
                        
        m.allKeysListeners = new ArrayList<Updatable>(allKeysListeners);                // make a proper duplicate
        return m;
        }


    /** Duplicates the model without listeners, without undo listener, and without the last key, but with the fixer. */
    public Model copy()
        {
        Model m = null;
        try
            {
            m = (Model)(super.clone());
            }
        catch (CloneNotSupportedException ex)
            {
            // do nothing, never happens
            }

        // reload keys
        m.storage = new LinkedHashMap<String, Node>();
        String[] keyset = (String[])(storage.keySet().toArray(new String[0]));
        for(int i = 0; i < keyset.length; i++)
            {
            m.storage.put(keyset[i], new Node(storage.get(keyset[i]), false));              // no listeners
            }

        // clear                
        m.undoListener = null;
        m.recentlySet = false;
        m.lastKey = null;
        m.allKeysListeners = new ArrayList<Updatable>();
        if(latentVector == null){
            m.latentVector = null;
            } else {
            m.latentVector = new double[latentVector.length];
            for(int i = 0; i < latentVector.length; i++){
                m.latentVector[i] = latentVector[i];
                }
                
            }

        return m;
        }
    
    /** Copies all values to the given model.  This assumes that the models are the same; only values are transferred.
        lastKey is reset in the model, and the listeners are updated.  */
    public void copyValuesTo(Model model)
        {
        copyValuesTo(model, (String[])(storage.keySet().toArray(new String[0])));
        }


    /** Copies all keys EXCEPT the given keys to the given model.  This assumes that the models are the same; only values are transferred.
        lastKey is reset in the model, and the listeners are updated.  */
    public void copyValuesToExcept(Model model, String[] keys)
        {
        HashSet<String> allKeys = new HashSet(storage.keySet());
        for(int i = 0; i < keys.length; i++)
            {
            allKeys.remove(keys[i]);
            }
        copyValuesTo(model, (String[])(allKeys.toArray(new String[0])));
        }

    /** Copies the given keys to the given model.  This assumes that the models are the same; only values are transferred.
        lastKey is reset in the model, and the listeners are updated.  */
    public void copyValuesTo(Model model, String[] keys)
        {
        // load keys
        String[] keyset = keys;
        for(int i = 0; i < keyset.length; i++)
            {
            if (isString(keyset[i]))
                {
                model.set(keyset[i], get(keyset[i], ""));
                }
            else
                {
                model.set(keyset[i], get(keyset[i], 0));
                }
            }
        model.lastKey = null;
        model.updateAllListeners();
        }
        





    ///// EQUALITY TESTING
            
    public boolean equals(Object other)
        {
        if (other == null || !(other instanceof Model))
            return false;
        Model model = (Model) other;
        if (!storage.equals(model.storage))
            return false;
        // don't care about lastKey
        return true;
        }

    public boolean keyEquals(Model other)
        {
        if (other == null)
            return false;
        if (!storage.keySet().equals(other.storage.keySet()))
            return false;
                
        String[] keys = getKeys();
        for(int i = 0; i < keys.length; i++)
            {
            Node n = storage.get(keys[i]);
            Node n2 = other.storage.get(keys[i]);
            if (n2 == null)         // I can't be null, just the other one
                return false;
            if (!storage.get(keys[i]).keyEquals(other.storage.get(keys[i])))
                return false;
            }
        return true;
        }
    
        
        
        
    //// LISTENERS
    
    /** Removes all listeners from the model, including the undoListener. */
    public void clearListeners()
        {
        String[] keyset = (String[])(storage.keySet().toArray(new String[0]));
        for(int i = 0; i < keyset.length; i++)
            {
            Node node = storage.get(keyset[i]);
            node.listeners = null;
            }
        undoListener = null;
        }

    /** Register a listener to be notified whenever the value associated with the
        given key is updated.  If the key is ALL_KEYS, then the listener will
        be notified whenever any key is updated. */
    public void register(String key, Updatable component)
        {
        if (key.equals(ALL_KEYS))
            {
            allKeysListeners.add(component);
            }
        else
            {
            Node node = storage.get(key);
            if (node == null) { /* System.err.println("Warning (Model): " + "Listener registered for key without value " + key); */ node = new Node(); storage.put(key, node); }
            if (node.listeners == null)
                node.listeners = new ArrayList<Updatable>();
            node.listeners.add(component);
            }
        }
    
    /** Removes all listeners from the model, including the undoListener. */
    public void unregister(String key, Updatable component)
        {
        if (key.equals(ALL_KEYS))
            {
            if (!allKeysListeners.remove(component)) { System.err.println("Warning (Model): " + "Listener unregistered for ALL_KEYS but it wasn't registered");  return; }
            }
        else
            {
            Node node = storage.get(key);
            if (node == null) { System.err.println("Warning (Model): " + "Listener unregistered for key without value " + key);  return; }
            if (node.listeners == null) { System.err.println("Warning (Model): " + "Listener unregistered for key but it wasn't registered " + key);  return; }
            if (!node.listeners.remove(component)) { System.err.println("Warning (Model): " + "Listener unregistered for key but it wasn't registered " + key);  return; }
            if (node.listeners.isEmpty())
                node.listeners = null;          // let GC
            }
        }

    /** Returns all listeners for the key, but not for ALL_KEYS. */
    public ArrayList getListeners(String key)
        {
        Node node = storage.get(key);
        if (node == null) { System.err.println("Warning (Model): " + "Listeners requested for key without value " + key);  return null; }
        if (node.listeners == null)
            node.listeners = new ArrayList<Updatable>();
        return node.listeners;
        }
        
    /** Updates all listeners for the key, and for ALL_KEYS, unless updateListeners is true */
    public void updateListenersForKey(String key)
        {
		if (fixer != null) 
			fixer.fix(key, this);
		
        if (!updateListeners) return;
        
        Node node = storage.get(key);
        if (node != null && node.listeners != null)
            {
            for(int i = 0; i < node.listeners.size(); i++)
                node.listeners.get(i).update(key, this);
            }
                        
        for(int i = 0; i < allKeysListeners.size(); i++)
            {
            allKeysListeners.get(i).update(key, this);
            }
        }

    /** Updates all listeners for for keys, and for ALL_KEYS, unless updateListeners is true */
    public void updateAllListeners()
        {
		if (fixer != null)
			{
			String[] keyset = (String[])(storage.keySet().toArray(new String[0]));
			for(int j = 0; j < keyset.length; j++)
				{
				fixer.fix(keyset[j], this);
				}
			}

        if (!updateListeners) return;
        
        String[] keyset = (String[])(storage.keySet().toArray(new String[0]));
        for(int j = 0; j < keyset.length; j++)
            {
            updateListenersForKey(keyset[j]);
            }
        }
    
        
        
        
        
        
        
        
        
        
        
        
        
    ///// MUTATION    
        
        
    /** Returns TRUE with the given probability. */
    boolean coinToss(Random random, double probability)
        {
        if (probability==0.0) return false;     // fix half-open issues
        else if (probability==1.0) return true; // fix half-open issues
        else return random.nextDouble() < probability; 
        }
        
    /** Produces a random value in the fully closed range [a, b]. */
    static int randomValueWithin(Random random, int a, int b)
        {
        if (a > b) { int swap = a; a = b; b = swap; }
        if (a == b) return a;
        int range = (b - a + 1);
        return a + random.nextInt(range);
        }
       


    static final double STDDEV_CUT = 1.0/2.0;

    public static int randomValueWithin(Random random, int a, int b, int center, double weight)
        {
        if (a > b) { int swap = a; a = b; b = swap; }
        if (a == b) return a;
        if (weight == 0)
            return center;
        else if (weight == 1)
            return randomValueWithin(random, a, b);
        else
            {
            double stddev = (1.0 / (1.0 - weight)) - 1.0;
            double delta = 0.0;
                
            while(true)
                {
                double rand = (random.nextGaussian() * stddev * STDDEV_CUT) % 2.0;
                delta = rand * (b - a);
                if ((center + delta) > a - 0.5 &&
                    (center + delta) < b + 0.5)
                    break;
                }
            return (int)(Math.round(center + delta));
            }        
        }

    /** Produces a random value in the fully closed range [a, b],
        choosing from a uniform distribution of size +- 2 * weight * (b-a+1),
        centered at *center*, and rounded to the nearest integer. */
    public int randomValueWithin2(Random random, int a, int b, int center, double weight)
        {
        if (a > b) { int swap = a; a = b; b = swap; }
        if (a == b) return a;
        
        double range = b - a + 1;  // 0.5 extra on each side
        
        // pick a random number from -1...+1
        double delta = 0.0;
        while(true)
            {
            delta = Math.ceil((random.nextDouble() * 2 - 1) * weight * range);
            if ((center + delta) >= a &&
                (center + delta) <= b)
                break;
            }
        return (int)(Math.round(center + delta));
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
    public Model mutate(Random random, double weight)
        {
        return mutate(random, getKeys(), weight);
        }
        
        
    final static int VALID_RETRIES = 20;
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
    public Model mutate(Random random, String[] keys, double weight)
        {        
        if (undoListener!= null)
            {
            undoListener.push(this);
            undoListener.setWillPush(false);
            }
                
        for(int i = 0; i < keys.length; i++)
            {
            if (!exists(keys[i])) { continue; }
            // continue if the key is immutable, it's a string, or we fail the coin toss
            if (getStatus(keys[i]) == STATUS_IMMUTABLE || getStatus(keys[i]) == STATUS_RESTRICTED || isString(keys[i])) continue;
            if (minExists(keys[i]) && maxExists(keys[i]) && getMin(keys[i]) >= getMax(keys[i]))  continue;  // no range

            boolean hasMetric = false;                              // do we even HAVE a metric range?
            boolean doMetric = false;                               // are we in that range, and should mutate within it?
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
                
            if (doMetric)  // definitely do a metric mutation
                {
                int a = getMetricMin(keys[i]);
                int b = getMetricMax(keys[i]);
                double mutWeight = weight;
                double mutProb = mutWeight;
                if (random.nextDouble() < mutProb)
                    {
                    set(keys[i], reviseMutatedValue(keys[i], get(keys[i], 0),
                            randomValueWithin(random, getMetricMin(keys[i]), getMetricMax(keys[i]), get(keys[i], 0), mutWeight)));
                    }
                }
            else if (pickRandomInMetric)                    // MAYBE jump into metric
                {
                if (coinToss(random, weight))
                    {
                    set(keys[i], reviseMutatedValue(keys[i], get(keys[i], 0), 
                            randomValueWithin(random, getMetricMin(keys[i]), getMetricMax(keys[i]))));
                    }
                }
            else if (hasMetric)  // MAYBE choose a random new non-metric location
                {
                if (coinToss(random, weight))
                    {
                    for(int x = 0; x < VALID_RETRIES; i++)
                        {
                        int lowerRange = getMetricMin(keys[i]) - getMin(keys[i]);
                        if (lowerRange < 0) System.err.println("WARNING (Model.mutate): metric min is below min.  That can't be right:  " + keys[i]); 
                        int upperRange = getMax(keys[i]) - getMetricMax(keys[i]);
                        if (upperRange < 0) System.err.println("WARNING (Model.mutate): metric max is above max.  That can't be right:  " + keys[i]); 
                        int delta = random.nextInt(lowerRange + upperRange);
                        if (delta < lowerRange)
                            {
                            //if (isValid(keys[i], getMin(keys[i]) + delta))
                            //    {
                            set(keys[i], reviseMutatedValue(keys[i], get(keys[i], 0), getMin(keys[i]) + delta));
                            break;
                            //    }
                            }
                        else
                            {
                            //if (isValid(keys[i], getMetricMax(keys[i]) + 1 + (delta - lowerRange)))
                            //    {
                            set(keys[i], reviseMutatedValue(keys[i], get(keys[i], 0), getMetricMax(keys[i]) + 1 + (delta - lowerRange)));
                            break;
                            //    }
                            }
                        }
                    }
                }
            else                                                                    // MAYBE choose a random new non-metric location (easiest because there is no metric location)
                {
                if (coinToss(random, weight))
                    {
                    set(keys[i], reviseMutatedValue(keys[i], get(keys[i], 0), 
                            randomValueWithin(random, getMin(keys[i]), getMax(keys[i]))));
                    }
                }
                
            if (fixer != null)
            	fixer.fix(keys[i], this);
            }

        if (undoListener!= null)
            {
            undoListener.setWillPush(true);
            }
        return this;
        }

    /** Finds a point on the OPPOSITE side of the Model from where the provided other MODEL is located.
        Let's call the current model X and the provided model Y.
        Changes all (and only) the METRIC parameters for which both X and Y are currently in metric regions.
        This is done by identifying the value of the parameter on the OPPOSITE side of X from where Y is.
        We reduce this value Z (move it closer to X's value) by WEIGHT, bound Z to be within the metric
        region, and then choose a new random value between X and Z inclusive.  

        If model is null (which can somehow happen sometimes), it's treated as the same as the existing model.
    */
    public Model opposite(Random random, Model model, double weight)
        {
        return opposite(random, model, getKeys(), weight, false);
        }

    /** Finds a point on the OPPOSITE side of the model from where the provided other MODEL is located,
        for the given keys. Let's call the current model X and the provided model Y.
        Changes all (and only) the METRIC parameters for which both X and Y are currently in metric regions.
        This is done by identifying the value of the parameter on the OPPOSITE side of X from where Y is.
        We reduce this value Z (move it closer to X's value) by WEIGHT, bound Z to be within the metric
        region, and then choose a new random value between X and Z inclusive.  
        
        If model is null (which can somehow happen sometimes), it's treated as the same as the existing model.
    */
    public Model opposite(Random random, Model model, String[] keys, double weight, boolean fleeIfSame)
        {       
        if (undoListener!= null)
            {
            undoListener.push(this);
            undoListener.setWillPush(false);
            }
                
        for(int i = 0; i < keys.length; i++)
            {
            // return if the key doesn't exist, is immutable or is a string, or is non-metric for someone
            if (model != null && !model.exists(keys[i])) { continue; }
            if (getStatus(keys[i]) == STATUS_IMMUTABLE || getStatus(keys[i]) == STATUS_RESTRICTED || isString(keys[i])) continue;
            if (minExists(keys[i]) && maxExists(keys[i]) && getMin(keys[i]) >= getMax(keys[i]))  continue;  // no range

            if (model == null || (get(keys[i], 0) == model.get(keys[i]) && fleeIfSame))
                {
                // need to flee.  First: are we metric?
                if (metricMinExists(keys[i]) &&
                    metricMaxExists(keys[i]) &&
                    get(keys[i], 0) >= getMetricMin(keys[i]) &&
                    get(keys[i], 0) <= getMetricMax(keys[i]))
                    {
                    // since we're the same, the best we can do here is do a LITTLE
                    // mutation (mutating by 1) while staying metric, so that NEXT
                    // time if we continue to flee, we'll keep on fleeing in that
                    // direction
                    if (getMetricMin(keys[i]) == getMetricMax(keys[i])) // uh oh
                        { } // don't set anything
                    else if (get(keys[i], 0) == getMetricMax(keys[i]))
                        set(keys[i], reviseMutatedValue(keys[i], get(keys[i], 0), 
                                get(keys[i], 0) - 1));
                    else if (get(keys[i], 0) == getMetricMin(keys[i]))
                        set(keys[i], reviseMutatedValue(keys[i], get(keys[i], 0), 
                                get(keys[i], 0) + 1));
                    else
                        set(keys[i], reviseMutatedValue(keys[i], get(keys[i], 0), 
                                get(keys[i], 0) + (random.nextBoolean() ? 1 : -1)));
                    }
                else
                    {
                    if (coinToss(random, weight))
                        {
                        int val = 0;
                        for(int j = 0; j < 10; j++)  // we'll try ten times to find something new
                            {
                            val = randomValueWithin(random, getMin(keys[i]), getMax(keys[i]));
                            if (val != get(keys[i], 0)) // we want to be different
                                break;
                            }
                        set(keys[i], reviseMutatedValue(keys[i], get(keys[i], 0), val));
                        }
                    }
                }
            else if (metricMinExists(keys[i]) &&
                metricMaxExists(keys[i]) &&
                get(keys[i], 0) >= getMetricMin(keys[i]) &&
                get(keys[i], 0) <= getMetricMax(keys[i]) &&
                model.get(keys[i], 0) >= getMetricMin(keys[i]) &&
                model.get(keys[i], 0) <= getMetricMax(keys[i]))
                {
                // different but both metric.  

                int a = get(keys[i], 0);
                int b = model.get(keys[i], a);
                    
                // determine range
                double qq = a + weight * (a - b);
                int q = 0;
                
                // round away from b
                if (b > a)
                    q = (int)Math.floor(qq);
                else
                    q = (int)Math.ceil(qq);

                // bound
                if (metricMinExists(keys[i]) && q < getMetricMin(keys[i]))
                    q = getMetricMin(keys[i]);
                if (metricMaxExists(keys[i]) && q > getMetricMax(keys[i]))
                    q = getMetricMax(keys[i]);

                set(keys[i], reviseMutatedValue(keys[i], get(keys[i], 0), randomValueWithin(random, a, q)));
                }
            else
                {
                // different but someone is non-metric.  Don't change.
                continue;
                }

            if (fixer != null)
            	fixer.fix(keys[i], this);
            }

        if (undoListener!= null)
            {
            undoListener.setWillPush(true);
            }
            
        return this;
        }
    
    
    

    /** Recombines (potentially) all keys.  
        Recombination works as follows.  For each key, we first see if we're permitted to mutate it
        (no immutable status, other model doesn't have the key).  If so, if we're METRIC or both
        of us are within the metric region of a hybrid parameter, then
        we do a metric recombination.  Otherwise we do a categorical recombination.
        
        <p>
        METRIC RECOMBINATION.  Let A be me and B be the other guy.  Let C be the weighted
        average betweeen A and B (low weight favors A, high weight favors B).  Now pick
        a random position between A and C.  This means that a low weight just retains A,
        and a high weight picks a random value between A and B.  Note that there is no
        weight which *favors B*.  See crossover(), which might have a B-favoring option for you.
        
        <p>
        CATEGORICAL RECOMBINATION.
        We do a coin toss with WEIGHT probability.  If this is TRUE then we do another
        coin toss of 0.5 probability.  If this is TRUE, then we adopt B's value, else we
        retain A's value.  The 0.5 is there to make this roughly the same mutative effect
        as metric recombination.
    */
    public Model recombine(Random random, Model model, double weight)
        {
        return recombine(random, model, getKeys(), weight);
        }

    /** Recombines (potentially) only the keys provided, leaving the others alone.  
        Recombination works as follows.  For each key, we first see if we're permitted to mutate it
        (no immutable status, other model doesn't have the key).  If so, if we're METRIC or both
        of us are within the metric region of a hybrid parameter, then
        we do a metric recombination.  Otherwise we do a categorical recombination.
        
        <p>
        METRIC RECOMBINATION.  Let A be me and B be the other guy.  Let C be the weighted
        average betweeen A and B (low weight favors A, high weight favors B).  Now pick
        a random position between A and C.  This means that a low weight just retains A,
        and a high weight picks a random value between A and B.  Note that there is no
        weight which *favors B*.  See crossover(), which might have a B-favoring option for you.
        
        <p>
        CATEGORICAL RECOMBINATION.
        We do a coin toss with WEIGHT probability.  If this is TRUE then we do another
        coin toss of 0.5 probability.  If this is TRUE, then we adopt B's value, else we
        retain A's value.  The 0.5 is there to make this roughly the same mutative effect
        as metric recombination.
    */
    public Model recombine(Random random, Model model, String[] keys, double weight)
        {
        if (undoListener!= null)
            {
            undoListener.push(this);
            undoListener.setWillPush(false);
            }
                
        for(int i = 0; i < keys.length; i++)
            {
            // skip if the key doesn't exist, is immutable, is restricted, or is a string
            if (!model.exists(keys[i])) { continue; }
            if (getStatus(keys[i]) == STATUS_IMMUTABLE || isString(keys[i]) || getStatus(keys[i]) == STATUS_RESTRICTED) continue;
            if (minExists(keys[i]) && maxExists(keys[i]) && getMin(keys[i]) >= getMax(keys[i]))  continue;  // no range

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
                double qq = a - weight * (a - b);
                
                int q = 0;
                
                // round towards b
                if (b > a)
                    q = (int)Math.ceil(qq);
                else
                    q = (int)Math.floor(qq);

                set(keys[i], reviseMutatedValue(keys[i], get(keys[i], 0), randomValueWithin(random, a, q)));
                }
            else if (coinToss(random, weight))
                {
                if (coinToss(random, 0.5))
                    set(keys[i], reviseMutatedValue(keys[i], get(keys[i], 0), model.get(keys[i], 0)));
                }
 
             if (fixer != null)
            	fixer.fix(keys[i], this);
           }

        if (undoListener!= null)
            {
            undoListener.setWillPush(true);
            }
        return this;
        }


    /** Crosses over (potentially) only the keys provided, leaving the others alone.  
        Crossover works as follows.  For each key, we first see if we're permitted to mutate it
        (no immutable status, other model doesn't have the key).  If so, then:

        <p>
        We do a coin toss with WEIGHT probability.  If this is TRUE then we do another
        coin toss of 0.5 probability.  If this is TRUE, or if postCoinToss is FALSE [so we
        skip the second coin toss] then we adopt B's value, else we
        retain A's value.  The 0.5 is there to make this roughly the same mutative effect
        as recombination: crossover is effectively biased to go roughly from 0.0 to 0.5 probability
        of mutating to B, rather than than 0.0 to 1.0 probability.  If you want to go from 0.0 to 1.0
        then set postCoinToss to FALSE.
    */
    public Model crossover(Random random, Model model, String[] keys, double weight, boolean postCoinToss)
        {
        if (undoListener!= null)
            {
            undoListener.push(this);
            undoListener.setWillPush(false);
            }
                
        for(int i = 0; i < keys.length; i++)
            {
            // skip if the key doesn't exist, is immutable, is restricted, or is a string
            if (!model.exists(keys[i])) { continue; }
            if (getStatus(keys[i]) == STATUS_IMMUTABLE || isString(keys[i]) || getStatus(keys[i]) == STATUS_RESTRICTED) continue;
            
            if (coinToss(random, weight))
                {
                if (!postCoinToss || coinToss(random, 0.5))
                    set(keys[i], reviseMutatedValue(keys[i], get(keys[i], 0), model.get(keys[i], 0)));
                }
                
            if (fixer != null)
            	fixer.fix(keys[i], this);
            }

        if (undoListener!= null)
            {
            undoListener.setWillPush(true);
            }
            
        return this;
        }

    /** Crosses over (potentially) only the keys provided, leaving the others alone.  
        Crossover works as follows.  For each key, we first see if we're permitted to mutate it
        (no immutable status, other model doesn't have the key).  If so, then:

        <p>
        We do a coin toss with WEIGHT probability.  If this is TRUE then we do another
        coin toss of 0.5 probability.  If this is TRUE, then we adopt B's value, else we
        retain A's value.  The 0.5 is there to make this roughly the same mutative effect
        as recombination: crossover is effectively biased to go roughly from 0.0 to 0.5 probability
        of mutating to B, rather than than 0.0 to 1.0 probability.  If you want to go from 0.0 to 1.0
        then call crossover(random, model, keys, weight, FALSE) instead.
    */
    public Model crossover(Random random, Model model, String[] keys, double weight)
        {
		return crossover(random, model, keys, weight, true);
        }

    // used by morph to determine if a key is in a metric region for all models 
    boolean allInMetric(Model[] models, String key)
        {
        for(int i = 0; i < models.length; i++)
            {
            if (models[i].get(key) < getMetricMin(key) ||
                models[i].get(key) > getMetricMax(key)) 
                return false;
            }
        return true;
        }
    
    // used by morph to sort the models by weight (strongest first) 
    class Sort implements Comparable
        {
        public Model model;
        public double weight;
        public double previousWeight;
        public Sort(Model model, double weight, double prev) { this.model = model; this.weight = weight; this.previousWeight = prev; }
        public int compareTo(Object obj)
            {
            Sort other = (Sort) obj;
            if (weight > other.weight) return -1;
            else if (weight < other.weight) return 1;
            else return 0;
            }
        }
    
    public static final int CATEGORICAL_STRATEGY_MORPH = -3;
    public static final int CATEGORICAL_STRATEGY_STRONGEST = -2;
    public static final int CATEGORICAL_STRATEGY_DEFAULT = -1;
    
    /** This recombiner is meant to be used over and over on the same model to gradually nudge it towards
        certain models.  If you had just two models, it'd be more or less like this:
        
        <pre>
        If (weight == previousWeight)
        .   return                                                          // no change in push
        
        For each key:
        .   If the key is METRIC,
        .      average model and model2 by weight
        .   If the key is CATEGORICAL (non-metric)
        .      If weight >= previousWeight             // we're moving towards model
        .         m <- model
        .         p <- weight 
        .         pw <- previousWeight
        .      Else
        .         m <- model2                          // we're moving towards model2
        .         p <- 1 - weight
        .         pw <- 1 - previousWeight
        .      If we are different from m
        .          With a *low* p^a probability                                    // maybe use a = 3?
        .              With probability p                                              // mutator A
        .                  Set key to value in m
        .          Else
        .              With probability (p - pw)                               // mutator B
        .                  Set key to value in m
        </pre>
 
        <p>
        We have two different probabilistic weighted categorical mutators for the following reason.
        Mutator A guarantees that by the time we reach the extreme, with 1.0 probability we will have
        mutated clear to m.  But if you move SLOWLY towards the extreme, it'll change towards m much more
        rapidly than if you move QUICKLY towards the extreme.  This is because once a parameter has been
        changed, it's locked and cannot change back.   On the other hand, muator B will mutate
        at a rate proportional to how fast you move, but it does not guarantee that you'll be at 1.0
                
        <p>I have split the difference and have us using Mutator B if we're far away, but as we approach
        the extreme end (p = 1) we gravitate towards mutator A.
       
        <p>However you can have more than one model, so the algorithm is actually more complex than this.
        In this case when the key is metric, we average by the normalized weights of all the models.  
        If the key is non-metric, then sort the models by weight and go through them strongest to weakest.
        Each model gets a chance to change the value of each key.  For each key:
        - If we're moving away from the model (the previousWeight is stronger than the weight), then
        we skip this model.  The next model gets a chance and so on.
        - If the model is the same as us, then it's unchanged and we quit, nobody else gets to change it.
        - If the model is different, then with normalizeWeight^a probability we either use mutator A 
        with normalizeWeight probability, or we use mutator B with normalizeWeight probability.  If we
        wind up mutating, then we quit and nobody else gets to change this key.
        - Otherwise the next model gets a chance and so on.
                
        <p>The goal here is give mutation precedence to the strongest (closest) models in turn THAT WE ARE
        MOVING TOWARDS, and once we have moved towards a model (or are already the same as that model), 
        it's locked so we can't mutate away from it.
        
        <p>This all assumes that you're using CATEGORICAL_STRATEGY_MORPH as your categorialStrategy.  However
        there are other options for handling categorical data.  If your categoricalStrategy is CATEGORICAL_STRATEGY_STRONGEST,
        then no probabilistic morphing occurs among your categorical data at all: instead the data is directly set to the
        strongest model, that is, the one whose weight is highest.  It doesn't matter if you're moving towards
        or away from the model.  Simiarly, if your categoricalStratgegy is CATEGORICAL_STRATEGY_DEFAULT,
        then again, there's no probabilistic morphing but instead the categorical data is locked to the model passed in 
        to <tt>categoricalDefaultModel</tt>.  Finally, if your categoricalStrategy is set to a value in the range (0...models.length - 1), 
        then the data is directly set to that model number.
        
        <p>We presently handle hybrid (metric/categorical) parameters in a very crude way.  If ALL models
        are in the metric region, then we treat the parameter as metric; otherwise we treat the parameter as categorical.
           
        <p>Because morph is really designed to be used in real-time it seems unwise to include an undolistener
        in the main model, so I'd set it to null.
        
        <p> Make sure that all the models exist and contain all the keys, cause morph doesn't check.
        To initialize, I'd set the previousWeights to 0.5 each. 
    */
    
    public Model morph(Random random, Model[] models, Model categoricalDefaultModel, String[] keys, double[] weights, double[] previousWeights, int categoricalStrategy)
        {
        // prepare undo listener if any (likely it will have been deleted)
        if (undoListener!= null)
            {
            undoListener.push(this);
            undoListener.setWillPush(false);
            }
                
        // Sorts array
        Sort[] sorts = new Sort[weights.length];
                
        // Normalized weights
        double[] normalized = new double[weights.length];

        // (1) Build and sort the sorts (strongest first)
        // (2) Normalize weights
        double sum = 0;
        for(int i = 0; i < normalized.length; i++)
            {
            sorts[i] = new Sort(models[i], weights[i], previousWeights[i]);
            normalized[i] = weights[i];
            sum += normalized[i];
            }
                        
        if (sum == 0)           // make everyone have the same normalized weight
            {
            for(int i = 0; i < normalized.length; i++)
                {
                normalized[i] = 1.0 / normalized.length;        
                }
            }
        else
            {
            for(int i = 0; i < normalized.length; i++)
                {
                normalized[i] /= sum;           
                }
            }
        Arrays.sort(sorts);             
                
        /// FOR EACH KEY

        keys_label : for(int i = 0; i < keys.length; i++)
            {
            // skip if the key doesn't exist, is immutable, is restricted, or is a string
            if (!models[0].exists(keys[i])) { continue; }
            if (getStatus(keys[i]) == STATUS_IMMUTABLE || isString(keys[i]) || getStatus(keys[i]) == STATUS_RESTRICTED) continue;
            if (minExists(keys[i]) && maxExists(keys[i]) && getMin(keys[i]) >= getMax(keys[i]))  continue;  // no range

            // If the key is metric AND we're in the metric region in every one of the models
            /// EDIT: I've decided to say screw it, let hybrid parameters be considered metric
            /// all the time, otherwise we get lots of jumps for LFO stuff on the Venom for example.
            if (metricMinExists(keys[i]) &&
                metricMaxExists(keys[i]))
                // &&
                //allInMetric(models, keys[i])) 
                {
                /// TREAT AS METRIC
                
                /// Compute weighted value using normalized weights
                double val = 0;
                for(int j = 0; j < normalized.length; j++)
                    {
                    val += normalized[j] * models[j].get(keys[i]);
                    }
                
                // Round towards strongest model, which is sorts[0]
                if (sorts[0].model.get(keys[i]) > val)
                    val = (int)Math.ceil(val);
                else
                    val = (int)Math.floor(val);

                // Revise and set
                set(keys[i], reviseMutatedValue(keys[i], get(keys[i], 0), (int)val));
                }
            else 
                {
                /// TREAT AS CATEGORICAL
                
                // Go through the models strongest to weakest and let each of them have a chance to change the parameter
                // or to lock it so other models can't change it
                for(int j = 0; j < sorts.length; j++)
                    {
                    if (categoricalStrategy == CATEGORICAL_STRATEGY_MORPH)
                        {
                        // If we're moving towards this model rather than away from it (model only gets a chance if we're moving towards it)
                        if (sorts[j].weight > sorts[j].previousWeight || sorts[j].weight == 1.0)
                            {
                            //System.err.println("Towards " + sorts[j].model.get("name", "--") + " " + sorts[j].weight + " " + sorts[j].previousWeight);
                            // If the model has a different value than our value.  In this case it MIGHT change it.  Otherwise it LOCKS it.
                            if (sorts[j].model.get(keys[i]) != get(keys[i]))
                                {
                                // Which mutator do we use?  Do a coin toss under the weight squared, so we usually do mutator b when further away 
                                if (coinToss(random, sorts[j].weight * sorts[j].weight * sorts[j].weight * sorts[j].weight))              // right now we're doing p^4
                                    {
                                    // mutator a -- just mutate with weight probability
                                    if (coinToss(random, sorts[j].weight))
                                        {
                                        //System.err.println("A Updating " + keys[i] + " to " + j);
                                        set(keys[i], reviseMutatedValue(keys[i], get(keys[i]), sorts[j].model.get(keys[i])));
                                        continue keys_label;            // lock down -- nobody else gets a chance to change this
                                        }
                                    else
                                        {
                                        // do nothing, let someone else try
                                        }
                                    }       
                                else
                                    {
                                    // mutator b -- mutate by the difference between the weight and previous weight so small moves don't have much effect
                                    if (coinToss(random, sorts[j].weight - sorts[j].previousWeight))
                                        {
                                        //System.err.println("B Updating " + keys[i]);
                                        set(keys[i], reviseMutatedValue(keys[i], get(keys[i]), sorts[j].model.get(keys[i])));
                                        continue keys_label;            // lock down -- nobody else gets a chance to change this
                                        }
                                    else
                                        {
                                        // do nothing, let someone else try
                                        }
                                    }               
                                }
                            else
                                {
                                // lock down -- nobody else can change this
                                continue keys_label;
                                }
                            }
                        else
                            {
                            // do nothing, let someone else try
                            }
                        }
                    // handle other categorical strategies
                    else if (categoricalStrategy == CATEGORICAL_STRATEGY_STRONGEST)
                        {
                        set(keys[i], reviseMutatedValue(keys[i], get(keys[i]), sorts[0].model.get(keys[i])));
                        }
                    else if (categoricalStrategy == CATEGORICAL_STRATEGY_DEFAULT)
                        {
                        set(keys[i], reviseMutatedValue(keys[i], get(keys[i]), categoricalDefaultModel.get(keys[i])));
                        }
                    else
                        {
                        set(keys[i], reviseMutatedValue(keys[i], get(keys[i]), models[categoricalStrategy].get(keys[i])));
                        }
                    }
                }

            if (fixer != null)
            	fixer.fix(keys[i], this);
            }

        // update undoListener
        if (undoListener!= null)
            {
            undoListener.setWillPush(true);
            }
        
        for(int i = 0; i < weights.length; i++)
            {
            previousWeights[i] = weights[i];
            }

        return this;
        }



    /** Override this method in the model produced by synth.buildModel() to revise mutated values if the mutator sets them to invalid things. */
    public int reviseMutatedValue(String key, int old, int current) { return current; }    

    /** Override this method in the model produced by synth.buildModel() to revise mutated values if the mutator sets them to invalid things. */
    public String reviseMutatedValue(String key, String old, String current) { return current; }    





    ///// UTILITIES
        

    public void printNode(String key)
        {
        Node node = storage.get(key);
        if (node == null) System.err.println("" + key + " -> [NULL NODE]");
        else System.err.println("" + key + " -> " + node.toString());
        }


    /** Print to stderr those model parameters for which the provided "other" model
        does not have identical values. */
    public void printDiffs(Model other)
        {
        printDiffs(new PrintWriter(new OutputStreamWriter(System.err)), other);
        }
        

    /** Print to the given writer those model parameters for which the provided "other" model
        does not have identical values. */
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
        
    /** Print the model parameters to stderr.   If diffsOnly, then only the model parameters which
        differ from the default will be printed. */
    public void print()
        {
        print(new PrintWriter(new OutputStreamWriter(System.err)));
        }
    
    static final String FALSE_STRING = "<<<<false>>>>>";
    static final String TRUE_STRING = "<<<<true>>>>>";
    String getModelParameterText(String key)
        {
        if (isString(key))
            return "\"" + get(key, "") + "\"";
                
        ArrayList<Updatable> l = getListeners(key);
        if (l == null) 
            return "" + get(key, 0);
                        
        for(int i = 0; i < l.size(); i++)
            {
            Object obj = l.get(i);
            // Lots of things can be NumericalComponents so we're just going
            // to focus on the primary items
            if (obj instanceof Chooser)
                {
                return ((Chooser)obj).map(get(key, 0));
                }
            else if (obj instanceof CheckBox)
                {
                return (get(key, 0) == 0 ? FALSE_STRING : TRUE_STRING);
                }
            else if (obj instanceof LabelledDial)
                {
                return ((LabelledDial)obj).map(get(key, 0));
                }
            else if (obj instanceof NumberTextField)
                {
                return "" + ((NumberTextField)obj).getValue();
                }
            }
        return "" + get(key, 0);
        }
    
    public void dumpNodes()
        {
        String[] keys = getKeys();
        Arrays.sort(keys);
        for(int i = 0; i < keys.length; i++)
            {
            printNode(keys[i]);
            }
        }


    /** Print the model parameters to the given writer. */
    public void print(PrintWriter out)
        {
        String[] keys = getKeys();
        Arrays.sort(keys);
        for(int i = 0; i < keys.length; i++)
            {
            if (isString(keys[i]))
                out.println(keys[i] + ": " + getModelParameterText(keys[i]));
            else if (isInteger(keys[i]))
                {
                String str = getModelParameterText(keys[i]);
                String str2 = "" + get(keys[i], 0);
                if (str.equals(FALSE_STRING))
                    {
                    out.println(keys[i] + ": False");
                    }
                else if (str.equals(TRUE_STRING))
                    {
                    out.println(keys[i] + ": True");
                    }
                else if (str.equals(str2))
                    out.println(keys[i] + ": " + str);
                else
                    out.println(keys[i] + ": " + str + " (" + str2 + ")");
                }
            else
                out.println(keys[i] + ": UNKNOWN OBJECT " + get(keys[i]));
            }
        }
    }
