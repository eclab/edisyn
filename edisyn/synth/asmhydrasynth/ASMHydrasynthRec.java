/**
   Copyright 2023 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.asmhydrasynth;
import edisyn.*;

public class ASMHydrasynthRec extends Recognize
    {
    public static boolean recognize(byte[] data)
        {
        if (data[0] == (byte)0xF0 &&
        	data[1] == (byte)0x00 &&
        	data[2] == (byte)0x20 &&
        	data[3] == (byte)0x2B &&
        	data[4] == (byte)0x00 &&
        	data[5] == (byte)0x6F) return true;
        	
        boolean val = (data.length == 2259 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x7D &&
            data[2] == (byte)'E' &&
            data[3] == (byte)'D' &&
            data[4] == (byte)'I' &&
            data[5] == (byte)'S' &&
            data[6] == (byte)'Y' &&
            data[7] == (byte)'N' &&
            data[8] == (byte)'-' &&
            data[9] == (byte)'H' &&
            data[10] == (byte)'Y' &&
            data[11] == (byte)'D' &&
            data[12] == (byte)'R' &&
            data[13] == (byte)'A' &&
            data[14] == (byte)'S' &&
            data[15] == (byte)'Y' &&
            data[16] == (byte)'N' &&
            data[17] == (byte)'T' &&
            data[18] == (byte)'H' &&
            data[19] == (byte)0);            // sysex version
        return val;
        }
    }
