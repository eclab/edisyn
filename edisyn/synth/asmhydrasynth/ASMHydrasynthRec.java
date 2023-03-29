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
        else
        	return false;

        	/*
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
        */
        }

    public static int getNextSysexPatchGroup(byte[][] sysex, int start)
        {
        // Look for the next valid message which is exactly 155 bytes in length
        for(int i = start; i < sysex.length; i++)
        	{
        	if (recognize(sysex[i]))
        		{
        		if (sysex[i].length == 155)  // probably right
        			{
        			// Now look for the next message that's exactly 191 bytes in length
        			for(int j = i + 1; j < sysex.length; j++)
        				{
        				if (recognize(sysex[j]))
        					{
        					if (sysex[j].length == 191)	// probably right
        						{
        						return j;
        						}
        					}
        				else 
        					{
        					return start;
        					}
        				}
        			}
           		}
        	else 
        		{
        		return start;
        		}
        	}
        
        // Looks like we are out?
        return sysex.length;
        }
    }
