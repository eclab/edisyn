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
    public boolean bank;
    // Sysex messages
    public byte[][] sysex;
    // Which kind of synth editor is designed for this Patch?
    public int synth;
    // Patch name
    public String name = null;
    // Patch location
    public String location = null;
    
    public Patch(int synth, byte[][] sysex, boolean bank)
        {
        this.synth = synth;
        this.sysex = sysex;
        this.bank = bank;
        } 
    
    public String toString()
    	{
    	return name;
    	}
    /*
    public String toString()
        {
        String s = "Patch[" + (location == null ? "" : location) + " " + name + ", " + synth + ", " + bank + ", " + sysex.length + "\n";
        for(int i = 0; i < sysex.length; i++)
            {
            s = s + ("" + i + " (" + sysex[i].length + ") ->");
            for(int j = 0; j < sysex[i].length; j++)
                {
                s = s + (" " + StringUtility.toHex(sysex[i][j]));
                }
            s = s + "\n";
            }
        return s + "\n] ";
        }
    */
        
    }
        
