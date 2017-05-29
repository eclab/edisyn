/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn;

/**
   Implemented by listeners registered in the model to update themselves when a given key changes.
        
   @author Sean Luke
*/

public interface Updatable
    {
    /** Updates the object in response to a change to the following key in the model. */
        
    public void update(String key, Model model);
    }
        
