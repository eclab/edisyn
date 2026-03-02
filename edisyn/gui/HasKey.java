/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.gui;

import java.util.*;


public interface HasKey
    {
    // If the class has a key, it is returned.  If the class has more than one key, and shouldn't
    // be used to distribute, then null should be returned.
    public String getKey();
    // If the class has one or more keys, they are returned as an array
    public String[] getKeys();
    }
