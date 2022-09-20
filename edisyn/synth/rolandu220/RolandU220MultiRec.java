/**
   Copyright 2022 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.rolandu220;
import edisyn.*;

public class RolandU220MultiRec extends Recognize
    {
    public static boolean recognize(byte[] data)
        {
        return (
        	(data.length == 138 || data.length ==  42) &&
        	(data[0] == (byte)0xF0) &&
            (data[1] == (byte)0x41) &&
            (data[3] == (byte)0x2B) &&
            (data[4] == (byte)0x12) &&
			// Patch Temporary
		    ((data[5] == (byte)0x00 && data[6] >= (byte)0x06 && data[6] < (byte)0x08) ||
  			  // Patch Permanent
  		      (data[5] == (byte)0x03)));
        }

    public static int getNextSysexPatchGroup(byte[][] sysex, int start)
        {
        if (sysex.length - start >= 2 &&	// we're okay
        	sysex[start].length == 138 &&
        	sysex[start + 1].length == 42 &&
        	recognize(sysex[start]) &&
        	recognize(sysex[start + 1]))
        		return start + 2;
    	else return start;
        }
 
    }
