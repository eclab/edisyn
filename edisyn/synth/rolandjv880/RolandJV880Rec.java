/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.rolandjv880;
import edisyn.*;

public class RolandJV880Rec extends Recognize
    {
    public static int getNextSysexPatchGroup(byte[][] sysex, int start)
        {
        // we presume we need COMMON and then FOUR TONES
        if (start >= sysex.length) return start;
                
        if (recognize(sysex[start]))
            {
            if (sysex[start].length != 34 + 11)  // common
                {
                return start;
                }
            }
                
        for(int i = 0; i < 4; i++)
            {
            if (start >= sysex.length)
                {
                System.err.println("RolandJV880MultiRec.getNextSysexPatchGroup(): could not find tone " + i + " before messages terminated.");
                return start;
                }
            else if (!recognize(sysex[start + i]))
                {
                System.err.println("RolandJV880MultiRec.getNextSysexPatchGroup(): could not find tone " + i + ".");
                return start;
                }
            else if (sysex[start + i].length != 116 + 11)
                {
                System.err.println("RolandJV880MultiRec.getNextSysexPatchGroup(): could not find tone " + i + ", invalid length " + sysex[start + i].length);
                return start;
                }
            }
        return start + 5;
        }

    public static boolean recognize(byte[] data)
        {
        return ((data[0] == (byte)0xF0) &&
            (data[1] == (byte)0x41) &&
            // don't care about data[2]
            (data[3] == (byte)0x46) &&
            (data[4] == (byte)0x12) &&
                
            // Internal patch
                ((data[5] == 0x01 && (data[7] == 0x20 || data[7] == 0x28 || data[7] == 0x29 || data[7] == 0x2A || data[7] == 0x2B)) || 
                // Card Patch
                (data[5] == 0x02 && (data[7] == 0x20 || data[7] == 0x28 || data[7] == 0x29 || data[7] == 0x2A || data[7] == 0x2B)) ||
                // Patch Mode Temporary Patch or Performance Mode Temporry Patch
                (data[5] == 0x00 && data[6] == 0x08 && (data[7] == 0x20 || data[7] == 0x28 || data[7] == 0x29 || data[7] == 0x2A || data[7] == 0x2B))) &&
                 
            (data.length == 553 || data.length == 34 + 11 || data.length == 116 + 11));
        }
        
        
    }
