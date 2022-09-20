/**
   Copyright 2022 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.rolandu220;
import edisyn.*;

public class RolandU220DrumRec extends Recognize
    {
    public static boolean recognize(byte[] data)
        {
        return ((data[0] == (byte)0xF0) &&
            (data[1] == (byte)0x41) &&
            (data[3] == (byte)0x2B) &&
            (data[4] == (byte)0x12) &&
			// Patch Temporary
		    ((data[5] == (byte)0x00 && data[6] >= (byte)0x20 && data[6] <= 0x2C) ||
  			  // Patch Permanent
  		      (data[5] == (byte)0x05)) &&
  		     data.length <= 138);
        }


    public static int getNextSysexPatchGroup(byte[][] sysex, int start)
        {
        // coulda done this with a for-loop but whatever
        if (sysex.length - start >= 13 &&	// we're okay
        	sysex[start].length == 138 &&
        	sysex[start+1].length == 138 &&
        	sysex[start+2].length == 138 &&
        	sysex[start+3].length == 138 &&
        	sysex[start+4].length == 138 &&
        	sysex[start+5].length == 138 &&
        	sysex[start+6].length == 138 &&
        	sysex[start+7].length == 138 &&
        	sysex[start+8].length == 138 &&
        	sysex[start+9].length == 138 &&
        	sysex[start+10].length == 138 &&
        	sysex[start+11].length == 138 &&
        	sysex[start+12].length == 42 &&
        	recognize(sysex[start]) &&
        	recognize(sysex[start + 1]) &&
        	recognize(sysex[start + 2]) &&
        	recognize(sysex[start + 3]) &&
        	recognize(sysex[start + 4]) &&
        	recognize(sysex[start + 5]) &&
        	recognize(sysex[start + 6]) &&
        	recognize(sysex[start + 7]) &&
        	recognize(sysex[start + 8]) &&
        	recognize(sysex[start + 9]) &&
        	recognize(sysex[start + 10]) &&
        	recognize(sysex[start + 11]) &&
        	recognize(sysex[start + 12]))
        		{
        		return start + 13;
        		}
    	else return start;
        }


    }
