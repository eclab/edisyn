/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package edisyn.util;

/** A simple interface (simpler than List) for accessing random-access objects without changing their size.  Adhered to by Bag, IntBag, and DoubleBag */

public interface Indexed
    {
    /** Should return the base component type for this Indexed object, or
        null if the component type should be queried via getValue(index).getClass.getComponentType() */
    public Class componentType();
    public int size();
    /** Throws an IndexOutOfBoundsException if index is inappropriate, and IllegalArgumentException
        if the value is inappropriate.  Not called set() in order to be consistent with getValue(...)*/
    public Object setValue(final int index, final Object value)
        throws IndexOutOfBoundsException, IllegalArgumentException;
    /** Throws an IndexOutOfBoundsException if index is inappropriate.  Not called get() because
        this would conflict with get() methods in IntBag etc. which don't return objects. */
    public Object getValue(final int index)
        throws IndexOutOfBoundsException;
    }
