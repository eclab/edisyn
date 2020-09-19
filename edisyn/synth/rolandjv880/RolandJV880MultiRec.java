/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.rolandjv880;
import edisyn.*;

public class RolandJV880MultiRec extends Recognize
    {
    public static int getNumSysexDumpsPerPatch(byte[] data) 
        {
        return 9;
        }

    public static boolean recognize(byte[] data)
        {
        return ((data[0] == (byte)0xF0) &&
            (data[1] == (byte)0x41) &&
            (data[3] == (byte)0x46) &&
            (data[4] == (byte)0x12) &&
                
            // Internal performance
                ((data[5] == 0x01 && (data[7] == 0x10 || data[7] == 0x18 || data[7] == 0x19 || data[7] == 0x1A || data[7] == 0x1B || data[7] == 0x1C || data[7] == 0x1D || data[7] == 0x1E || data[7] == 0x1F)) || 
                // Card performance
                (data[5] == 0x02 && (data[7] == 0x10 || data[7] == 0x18 || data[7] == 0x19 || data[7] == 0x1A || data[7] == 0x1B || data[7] == 0x1C || data[7] == 0x1D || data[7] == 0x1E || data[7] == 0x1F)) ||
                // Temporary Performance
                (data[5] == 0x00 && data[6] == 0x00 && (data[7] == 0x10 || data[7] == 0x18 || data[7] == 0x19 || data[7] == 0x1A || data[7] == 0x1B || data[7] == 0x1C || data[7] == 0x1D || data[7] == 0x1E || data[7] == 0x1F))) &&
                 
            (data.length == 410 || data.length == 31 + 11 || data.length == 35 + 11));
        }
    }
