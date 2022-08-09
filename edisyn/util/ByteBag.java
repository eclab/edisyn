/*
  Copyright 2022 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package edisyn.util;

/** Maintains a simple array (objs) of bytes and the number of bytes (numObjs) in the array
    (the array can be bigger than this number).  You are encouraged to access the bytes directly;
    they are stored in positions [0 ... numObjs-1].  If you wish to extend the array, you should call
    the resize method.
    
    <p>ByteBag is approximately to byte what Bag is to Object.  However, for obvious reasons, ByteBag is not
    a java.util.Collection subclass and is purposely simple (it doesn't have an Iterator for example).
    
    <p>ByteBag is not synchronized, and so should not be accessed from different threads without locking on it
    or some appropriate lock object first.  ByteBag also has an unusual, fast method for removing bytes
    called remove(...), which removes the byte simply by swapping the topmost byte into its
    place.  This means that after remove(...) is called, the ByteBag may no longer have the same order
    (hence the reason it's called a "ByteBag" rather than some variant on "Vector" or "Array" or "List").  You can
    guarantee order by calling removeNondestructively(...) instead if you wish, but this is O(n) in the worst case.
*/

public class ByteBag implements java.io.Serializable, Cloneable, Indexed
    {
    private static final long serialVersionUID = 1;

    public byte[] objs;
    public int numObjs;
    
    /** Creates an ByteBag with a given initial capacity. */
    public ByteBag(int capacity) { numObjs = 0; objs = new byte[capacity]; }

    public ByteBag() { numObjs = 0; objs = new byte[1]; }
    
    /** Adds the ints from the other ByteBag without copying them.  The size of the
        new ByteBag is the minimum necessary size to hold the ints. If the Other ByteBag is
        null, a new empty ByteBag is created.*/
    public ByteBag(final ByteBag other)
        {
        if (other==null) { numObjs = 0; objs = new byte[1]; }
        else
            {
            numObjs = other.numObjs;
            objs = new byte[numObjs];
            System.arraycopy(other.objs,0,objs,0,numObjs);
            }
        }
    
    /** Creates an ByteBag with the given elements. If the Other array is
        null, a new empty ByteBag is created.*/
    public ByteBag(byte[] other) { this(); if (other!=null) addAll(other); }

    public int size()
        {
        return numObjs;
        }
    
    public boolean isEmpty()
        {
        return (numObjs<=0);
        }
    
    public boolean addAll(byte[] other) { return addAll(numObjs, other); }

    public boolean addAll(final int index, final byte[] other)
        {
        // throws NullPointerException if other == null,
        // ArrayArrayIndexOutOfBoundsException if index < 0,
        // and ArrayIndexOutOfBoundsException if index > numObjs
        if (index > numObjs) 
            throw new ArrayIndexOutOfBoundsException(index);
        // { throwArrayIndexOutOfBoundsException(index); }
        if (other.length == 0) return false;
        // make ByteBag big enough
        if (numObjs+other.length > objs.length)
            resize(numObjs+other.length);
        if (index != numObjs)   // scoot over elements if we're inserting in the middle
            System.arraycopy(objs,index,objs,index+other.length,numObjs - index);
        System.arraycopy(other,0,objs,index,other.length);
        numObjs += other.length;
        return true;
        }
    
    public boolean addAll(final ByteBag other) { return addAll(numObjs,other); }

    public boolean addAll(final int index, final ByteBag other)
        {
        // throws NullPointerException if other == null,
        // ArrayArrayIndexOutOfBoundsException if index < 0,
        // and ArrayIndexOutOfBoundsException if index > numObjs
        if (index > numObjs) 
            throw new ArrayIndexOutOfBoundsException(index);
        // { throwArrayIndexOutOfBoundsException(index); }
        if (other.numObjs <= 0) return false;
        // make ByteBag big enough
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
        ByteBag b = (ByteBag)(super.clone());
        b.objs = (byte[]) objs.clone();
        return b;
        }
        
    public void resize(int toAtLeast)
        {
        if (objs.length >= toAtLeast)  // already at least as big as requested
            return;

        if (objs.length * 2 > toAtLeast)  // worth doubling
            toAtLeast = objs.length * 2;

        // now resize
        byte[] newobjs = new byte[toAtLeast];
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
        byte[] newobjs = new byte[desiredLength];
        System.arraycopy(objs,0,newobjs,0,numObjs);
        objs = newobjs;
        }
    
    /** Returns 0 if the ByteBag is empty, else returns the topmost byte. */
    public byte top()
        {
        if (numObjs<=0) return 0;
        else return objs[numObjs-1];
        }
    
    /** Returns 0 if the ByteBag is empty, else removes and returns the topmost byte. */
    public byte pop()
        {
        // this curious arrangement makes me small enough to be inlined (28 bytes)
        int numObjs = this.numObjs;
        if (numObjs<=0) return 0;
        byte ret = objs[--numObjs];
        this.numObjs = numObjs;
        return ret;
        }
    
    /** Synonym for add(obj) -- try to use add instead unless you
        want to think of the ByteBag as a stack. */
    public boolean push(final byte obj)
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
        
    public boolean add(final byte obj)
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
        byte[] newobjs = new byte[numObjs*2+1];
        System.arraycopy(objs,0,newobjs,0,numObjs);
        objs=newobjs;
        }

    public boolean contains(final byte o)
        {
        final int numObjs = this.numObjs;
        final byte[] objs = this.objs;
        for(int x=0;x<numObjs;x++)
            if (o==objs[x]) return true;
        return false;
        }
        
    public byte get(final int index)
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

    public byte set(final int index, final byte element)
        {
        if (index>=numObjs) // || index < 0)
            throw new ArrayIndexOutOfBoundsException(index);
        // throwArrayIndexOutOfBoundsException(index);
        byte returnval = objs[index];
        objs[index] = element;
        return returnval;
        }

    public Object setValue(final int index, final Object value)
        {
        //Byte old = new Byte(get(index));
        Byte old = Byte.valueOf(get(index));
        Byte newval = null;
        try { newval = (Byte)value; }
        catch (ClassCastException e) { throw new IllegalArgumentException("Expected a Byte"); }
        set(index,newval.byteValue());
        return old;
        }

    /** Removes the byte at the given index, shifting the other bytes down. */
    public byte removeNondestructively(final int index)
        {
        if (index>=numObjs) // || index < 0)
            throw new ArrayIndexOutOfBoundsException(index);
        // throwArrayIndexOutOfBoundsException(index);
        byte ret = objs[index];
        if (index < numObjs - 1)  // it's not the topmost byte, must swap down
            System.arraycopy(objs, index+1, objs, index, numObjs - index - 1);
        numObjs--;
        return ret;
        }
    
    /** Removes the byte at the given index, moving the topmost byte into its position. */
    public byte remove(final int index)
        {
        int _numObjs = numObjs;
        if (index>=_numObjs) // || index < 0)
            throw new ArrayIndexOutOfBoundsException(index);
        // throwArrayIndexOutOfBoundsException(index);
        byte[] _objs = this.objs;
        byte ret = _objs[index];
        _objs[index] = _objs[_numObjs-1];
        numObjs--;
        return ret;
        }
        
    /** Sorts the ints into ascending numerical order. */
    public void sort() {java.util.Arrays.sort(objs, 0, numObjs);}

    /** Replaces all elements in the bag with the provided int. */
    public void fill(byte o)
        {
        // teeny bit faster
        byte[] objs = this.objs;
        int numObjs = this.numObjs;
        
        for(int x=0; x < numObjs; x++)
            objs[x] = o;
        }

    /** Shuffles (randomizes the order of) the ByteBag */
    public void shuffle(java.util.Random random)
        {
        // teeny bit faster
        byte[] objs = this.objs;
        int numObjs = this.numObjs;
        byte obj;
        int rand;
        
        for(int x=numObjs-1; x >= 1 ; x--)
            {
            rand = random.nextInt(x+1);
            obj = objs[x];
            objs[x] = objs[rand];
            objs[rand] = obj;
            }
        }
    
    /** Shuffles (randomizes the order of) the ByteBag */
/*
    public void shuffle(ec.util.MersenneTwisterFast random)
        {
        // teeny bit faster
        byte[] objs = this.objs;
        int numObjs = this.numObjs;
        byte obj;
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
    
    /** Reverses order of the elements in the ByteBag */
    public void reverse()
        {
        // teeny bit faster
        byte[] objs = this.objs;
        int numObjs = this.numObjs;
        int l = numObjs / 2;
        byte obj;
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
        
    /** Removes all numbers in the ByteBag.  This is done by clearing the internal array but 
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
    public void copyIntoArray(int fromStart, byte[] to, int toStart, int len)
        {
        System.arraycopy(objs, fromStart, to, toStart, len);
        }

    public byte[] toArray()
        {
        byte[] o = new byte[numObjs];
        System.arraycopy(objs,0,o,0,numObjs);
        return o;
        }

    public Byte[] toByteArray()
        {
        Byte[] o = new Byte[numObjs];
        for(int i = 0; i < numObjs; i++)
            o[i] = Byte.valueOf(objs[i]);
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
        return Byte.TYPE;
        }
    }
