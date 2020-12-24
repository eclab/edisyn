/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.waldorfkyra;
import edisyn.*;

public class WaldorfKyraMultiRec extends Recognize
    {
    public static int getNumSysexDumpsPerPatch(byte[] data) 
        {
        return 2;
        }

    public static boolean recognize(byte[] data)
        {
        byte[][] cutup = Synth.cutUpSysex(data);
        for(int i = 0; i < cutup.length; i++)
        	{
        	if (!subRecognize(cutup[i])) return false;
        	}
        return true;
        }
        
	public static boolean subRecognize(byte[] data)
		{
        return (
            //  multi patch
                (data.length == 128 + 10 &&
                data[0] == (byte)0xF0 &&
                data[1] == 0x3E &&
                data[2] == 0x22 &&
                (data[4] == 0x01 || data[4] == 0x41)) ||             // "Multi (whole 8 parts)" -- it's not clear why these are different
        
            //      multi name, which for some insane reason is not part of the patch
                (data.length == 22 + 10 &&
                data[0] == (byte)0xF0 &&
                data[1] == 0x3E &&
                data[2] == 0x22 &&
                (data[4] == 0x19 || data[4] == 0x59)));              // "Multi name"
        }
    }
