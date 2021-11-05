/***
    Copyright 2020 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn;

/**
   Patch location information
*/

public class PatchLocation
    {
    public static final int NO_BANK = -1;
    public static final int NO_NUMBER = -1;
    public int bank;
    public int number;
    public PatchLocation(int bank, int number) { this.bank = bank; this.number = number; }
    public PatchLocation(Model model) { this(model.get("bank", NO_BANK), model.get("number", NO_NUMBER)); }
    
    public boolean equals(Object obj)
        {
        if (obj == null) return false;
        if (!(obj instanceof PatchLocation)) return false;
        PatchLocation p = (PatchLocation)obj;
        if (p.bank != bank) return false;
        if (p.number != number) return false;
        return true;
        }
        
    Model assignLocations(Model model)
        {
        if (number != NO_NUMBER)
            model.set("number", number);
        if (bank != NO_BANK)
            model.set("bank", bank);
        return model;
        }
        
    public String toString()
        {
        return "PatchLocation[" + bank + ", " + number + "]";
        }
    }
        
