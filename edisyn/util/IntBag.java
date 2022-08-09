/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package edisyn.util;

/** Maintains a simple array (objs) of ints and the number of ints (numObjs) in the array
    (the array can be bigger than this number).  You are encouraged to access the ints directly;
    they are stored in positions [0 ... numObjs-1].  If you wish to extend the array, you should call
    the resize method.
    
    <p>IntBag is approximately to int what Bag is to Object.  However, for obvious reasons, IntBag is not
    a java.util.Collection subclass and is purposely simple (it doesn't have an Iterator for example).
    
    <p>IntBag is not synchronized, and so should not be accessed from different threads without locking on it
    or some appropriate lock int first.  IntBag also has an unusual, fast method for removing ints
    called remove(...), which removes the int simply by swapping the topmost int into its
    place.  This means that after remove(...) is called, the IntBag may no longer have the same order
    (hence the reason it's called a "IntBag" rather than some variant on "Vector" or "Array" or "List").  You can
    guarantee order by calling removeNondestructively(...) instead if you wish, but this is O(n) in the worst case.
*/

public class IntBag implements java.io.Serializable, Cloneable, Indexed
    {
    private static final long serialVersionUID = 1;

    public int[] objs;
    public int numObjs;
    
    /** Creates an IntBag with a given initial capacity. */
    public IntBag(int capacity) { numObjs = 0; objs = new int[capacity]; }

    public IntBag() { numObjs = 0; objs = new int[1]; }
    
    /** Adds the ints from the other IntBag without copying them.  The size of the
        new IntBag is the minimum necessary size to hold the ints. If the Other IntBag is
        null, a new empty IntBag is created.*/
    public IntBag(final IntBag other)
        {
        if (other==null) { numObjs = 0; objs = new int[1]; }
        else
            {
            numObjs = other.numObjs;
            objs = new int[numObjs];
            System.arraycopy(other.objs,0,objs,0,numObjs);
            }
        }
    
    /** Creates an IntBag with the given elements. If the Other array is
        null, a new empty IntBag is created.*/
    public IntBag(int[] other) { this(); if (other!=null) addAll(other); }

    public int size()
        {
        return numObjs;
        }
    
    public boolean isEmpty()
        {
        return (numObjs<=0);
        }
    
    public boolean addAll(int[] other) { return addAll(numObjs, other); }

    public boolean addAll(final int index, final int[] other)
        {
        // throws NullPointerException if other == null,
        // ArrayArrayIndexOutOfBoundsException if index < 0,
        // and ArrayIndexOutOfBoundsException if index > numObjs
        if (index > numObjs) 
            throw new ArrayIndexOutOfBoundsException(index);
        // { throwArrayIndexOutOfBoundsException(index); }
        if (other.length == 0) return false;
        // make IntBag big enough
        if (numObjs+other.length > objs.length)
            resize(numObjs+other.length);
        if (index != numObjs)   // scoot over elements if we're inserting in the middle
            System.arraycopy(objs,index,objs,index+other.length,numObjs - index);
        System.arraycopy(other,0,objs,index,other.length);
        numObjs += other.length;
        return true;
        }
    
    public boolean addAll(final IntBag other) { return addAll(numObjs,other); }

    public boolean addAll(final int index, final IntBag other)
        {
        // throws NullPointerException if other == null,
        // ArrayArrayIndexOutOfBoundsException if index < 0,
        // and ArrayIndexOutOfBoundsException if index > numObjs
        if (index > numObjs) 
            throw new ArrayIndexOutOfBoundsException(index);
        // { throwArrayIndexOutOfBoundsException(index); }
        if (other.numObjs <= 0) return false;
        // make IntBag big enough
        if (numObjs+other.numObjs > objs.length)
            resize(numObjs+other.numObjs);
        if (index != numObjs)   // scoot over elements if we're inserting in the middle
            System.arraycopy(objs,index,objs,index+other.size(),numObjs - index);
        System.arraycopy(other.objs,0,objs,index,other.numObjs);
        numObjs += other.numObjs;
        return true;
        }

    public Object clone() throws CloneNotSupportedException
        {
        IntBag b = (IntBag)(super.clone());
        b.objs = (int[]) objs.clone();
        return b;
        }
        
    public void resize(int toAtLeast)
        {
        if (objs.length >= toAtLeast)  // already at least as big as requested
            return;

        if (objs.length * 2 > toAtLeast)  // worth doubling
            toAtLeast = objs.length * 2;

        // now resize
        int[] newobjs = new int[toAtLeast];
        System.arraycopy(objs,0,newobjs,0,numObjs);
        objs=newobjs;
        }
    
    /** Resizes the objs array to max(numObjs, desiredLength), unless that value is greater than or equal to objs.length,
        in which case no resizing is done (this operation only shrinks -- use resize() instead).
        This is an O(n) operation, so use it sparingly. */
    public void shrink(int desiredLength)
        {
        if (desiredLength < numObjs) desiredLength = numObjs;
        if (desiredLength >= objs.length) return;  // no reason to bother
        int[] newobjs = new int[desiredLength];
        System.arraycopy(objs,0,newobjs,0,numObjs);
        objs = newobjs;
        }
    
    /** Returns 0 if the IntBag is empty, else returns the topmost int. */
    public int top()
        {
        if (numObjs<=0) return 0;
        else return objs[numObjs-1];
        }
    
    /** Returns 0 if the IntBag is empty, else removes and returns the topmost int. */
    public int pop()
        {
        // this curious arrangement makes me small enough to be inlined (28 bytes)
        int numObjs = this.numObjs;
        if (numObjs<=0) return 0;
        int ret = objs[--numObjs];
        this.numObjs = numObjs;
        return ret;
        }
    
    /** Synonym for add(obj) -- try to use add instead unless you
        want to think of the IntBag as a stack. */
    public boolean push(final int obj)
        {
        if (numObjs >= objs.length) doubleCapacityPlusOne();
        objs[numObjs++] = obj;
        return true;
        /*
        // this curious arrangement makes me small enough to be inlined (35 bytes)
        int numObjs = this.numObjs;
        if (numObjs >= objs.length) doubleCapacityPlusOne();
        objs[numObjs] = obj;
        this.numObjs = numObjs+1;
        return true;
        */
        }
        
    public boolean add(final int obj)
        {
        if (numObjs >= objs.length) doubleCapacityPlusOne();
        objs[numObjs++] = obj;
        return true;
        /*
        // this curious arrangement makes me small enough to be inlined (35 bytes)
        int numObjs = this.numObjs;
        if (numObjs >= objs.length) doubleCapacityPlusOne();
        objs[numObjs] = obj;
        this.numObjs = numObjs+1;
        return true;
        */
        }
        
    // private function used by add and push in order to get them below
    // 35 bytes -- always doubles the capacity and adds one
    void doubleCapacityPlusOne()
        {
        int[] newobjs = new int[numObjs*2+1];
        System.arraycopy(objs,0,newobjs,0,numObjs);
        objs=newobjs;
        }

    public boolean contains(final int o)
        {
        final int numObjs = this.numObjs;
        final int[] objs = this.objs;
        for(int x=0;x<numObjs;x++)
            if (o==objs[x]) return true;
        return false;
        }
        
    public int get(final int index)
        {
        if (index>=numObjs) // || index < 0)
            throw new ArrayIndexOutOfBoundsException(index);
        // throwArrayIndexOutOfBoundsException(index);
        return objs[index];
        }

    public Object getValue(final int index)
        {
        return Integer.valueOf(get(index));
        }

    public int set(final int index, final int element)
        {
        if (index>=numObjs) // || index < 0)
            throw new ArrayIndexOutOfBoundsException(index);
        // throwArrayIndexOutOfBoundsException(index);
        int returnval = objs[index];
        objs[index] = element;
        return returnval;
        }

    public Object setValue(final int index, final Object value)
        {
        //Integer old = new Integer(get(index));
        Integer old = Integer.valueOf(get(index));
        Integer newval = null;
        try { newval = (Integer)value; }
        catch (ClassCastException e) { throw new IllegalArgumentException("Expected an Integer"); }
        set(index,newval.intValue());
        return old;
        }

    /** Removes the int at the given index, shifting the other ints down. */
    public int removeNondestructively(final int index)
        {
        if (index>=numObjs) // || index < 0)
            throw new ArrayIndexOutOfBoundsException(index);
        // throwArrayIndexOutOfBoundsException(index);
        int ret = objs[index];
        if (index < numObjs - 1)  // it's not the topmost int, must swap down
            System.arraycopy(objs, index+1, objs, index, numObjs - index - 1);
        numObjs--;
        return ret;
        }
    
    /** Removes the int at the given index, moving the topmost int into its position. */
    public int remove(final int index)
        {
        int _numObjs = numObjs;
        if (index>=_numObjs) // || index < 0)
            throw new ArrayIndexOutOfBoundsException(index);
        // throwArrayIndexOutOfBoundsException(index);
        int[] _objs = this.objs;
        int ret = _objs[index];
        _objs[index] = _objs[_numObjs-1];
        numObjs--;
        return ret;
        }
        
    /** Sorts the ints into ascending numerical order. */
    public void sort() {java.util.Arrays.sort(objs, 0, numObjs);}

    /** Replaces all elements in the bag with the provided int. */
    public void fill(int o)
        {
        // teeny bit faster
        int[] objs = this.objs;
        int numObjs = this.numObjs;
        
        for(int x=0; x < numObjs; x++)
            objs[x] = o;
        }

    /** Shuffles (randomizes the order of) the IntBag */
    public void shuffle(java.util.Random random)
        {
        // teeny bit faster
        int[] objs = this.objs;
        int numObjs = this.numObjs;
        int obj;
        int rand;
        
        for(int x=numObjs-1; x >= 1 ; x--)
            {
            rand = random.nextInt(x+1);
            obj = objs[x];
            objs[x] = objs[rand];
            objs[rand] = obj;
            }
        }
    
    /** Shuffles (randomizes the order of) the IntBag */
/*
    public void shuffle(ec.util.MersenneTwisterFast random)
        {
        // teeny bit faster
        int[] objs = this.objs;
        int numObjs = this.numObjs;
        int obj;
        int rand;
        
        for(int x=numObjs-1; x >= 1 ; x--)
            {
            rand = random.nextInt(x+1);
            obj = objs[x];
            objs[x] = objs[rand];
            objs[rand] = obj;
            }
        }
*/
    
    /** Reverses order of the elements in the IntBag */
    public void reverse()
        {
        // teeny bit faster
        int[] objs = this.objs;
        int numObjs = this.numObjs;
        int l = numObjs / 2;
        int obj;
        for(int x=0; x < l; x++)
            {
            obj = objs[x];
            objs[x] = objs[numObjs - x - 1];
            objs[numObjs - x - 1] = obj;
            }
        }

    // protected void throwArrayIndexOutOfBoundsException(final int index)
    //     {
    //     throw new ArrayIndexOutOfBoundsException(""+index);
    //    }
        
    /** Removes all numbers in the IntBag.  This is done by clearing the internal array but 
        not replacing it with a new, smaller one. */
    public void clear()
        {
        numObjs = 0;
        }
        
    /**    
           Copies 'len' elements from the Bag into the provided array.
           The 'len' elements start at index 'fromStart' in the Bag, and
           are copied into the provided array starting at 'toStat'.
    */ 
    public void copyIntoArray(int fromStart, int[] to, int toStart, int len)
        {
        System.arraycopy(objs, fromStart, to, toStart, len);
        }

    public int[] toArray()
        {
        int[] o = new int[numObjs];
        System.arraycopy(objs,0,o,0,numObjs);
        return o;
        }

    public Integer[] toIntegerArray()
        {
        Integer[] o = new Integer[numObjs];
        for(int i = 0; i < numObjs; i++)
            o[i] = Integer.valueOf(objs[i]);
        return o;
        }

    public Long[] toLongArray()
        {
        Long[] o = new Long[numObjs];
        for(int i = 0; i < numObjs; i++)
            o[i] = Long.valueOf(objs[i]);
        return o;
        }

    public Double[] toDoubleArray()
        {
        Double[] o = new Double[numObjs];
        for(int i = 0; i < numObjs; i++)
            // o[i] = new Double(objs[i]);
            o[i] = Double.valueOf(objs[i]);
        return o;
        }

    public Class componentType()
        {
        return Integer.TYPE;
        }
    }
