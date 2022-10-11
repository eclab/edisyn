/**
   Copyright 2022 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.rolandu110;
import edisyn.*;

public class RolandU110Rec extends Recognize
    {
    public static int getNextSysexPatchGroup(byte[][] sysex, int start)
        {
        if (sysex.length > start + 1 &&
			recognize(sysex[start]) && 
			recognize(sysex[start + 1]) &&
        	// current memory
        	((sysex[start][5] == 0x01 &&
        	 sysex[start][6] == 0x01 &&
        	 sysex[start + 1][5] == 0x01 &&
        	 sysex[start + 1][6] == 0x02) ||
        	 // stored patches
        	 (sysex[start][5] == 0x02 && 
        	  sysex[start][6] % 2 == 0 &&	// even
        	  sysex[start + 1][5] == 0x02 &&
        	  sysex[start + 1][6] == sysex[start][6] + 1)))
        	return start + 2;
        else return start;
        }

    public static boolean recognize(byte[] data)
        {
        if (Synth.numSysexMessages(data) > 1)
        	{
        	byte[][] d = Synth.cutUpSysex(data);
        	for(int i = 0; i < d.length; i++)
        		if (!recognize(d[i])) return false;
        	return true;
        	}

       return (data.length <= 138 &&			// data.length >= 114 && 
            (data[0] == (byte)0xF0) &&
            (data[1] == (byte)0x41) &&
            (data[3] == (byte)0x23) &&
            (data[4] == (byte)0x12) &&
            ((data[5] == (byte)0x01) || (data[5] == (byte)0x02)));
        }
    }
