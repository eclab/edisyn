/***
    Copyright 2020 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn;
import edisyn.util.*;

/**
   Sysex data for an atomic collection of sysex messages sufficient 
   to define a patch or a patch bank (not bulk patch sysex).
*/

public class Patch
    {
    // Is this a bank sysex message?
    public boolean isBankSysex;
    // Sysex messages
    public byte[][] sysex;
    // Which kind of synth editor is designed to edit this Patch?
    public int synth;
    // Patch name
    public String name = null;
    // Patch location
    public String location = null;
    // Does this represent an empty patch?
    public boolean empty = true;
    
    // Indicates no number at all
    public static final int NUMBER_NOT_SET = -1;
    // The patch number, if there is one set.
    public int number = NUMBER_NOT_SET;
    // The patch bank, if there is one set.
    // If there are NO banks, then the "bank" is always 0
    public int bank = 0;
    // was the bank and number set arbitrarily by the Library (a flag it uses)
    public boolean arbitrary = false;
    
    public Patch(int synth, byte[] sysex, boolean isBankSysex)
        {
        this(synth, Synth.cutUpSysex(sysex), isBankSysex);
        } 
    
    public Patch(int synth, byte[][] sysex, boolean isBankSysex)
        {
        this.synth = synth;
        this.sysex = sysex;
        this.isBankSysex = isBankSysex;
        this.empty = false;
        } 
    
    public String getName()
        {
        return name;
        }
        
    public String toString()
        {
        return name;
        }
    
    public Patch(Patch patch)
        {
        isBankSysex = patch.isBankSysex;
        synth = patch.synth;
        name = patch.name;
        location = patch.location;
        number = patch.number;
        bank = patch.bank;
        arbitrary = patch.arbitrary;
        sysex = new byte[patch.sysex.length][];
        empty = patch.empty;
        for(int i = 0; i < patch.sysex.length; i++)
            {
            sysex[i] = new byte[patch.sysex[i].length];
            System.arraycopy(patch.sysex[i], 0, sysex[i], 0, patch.sysex[i].length);
            }
        }
        
    }
        
