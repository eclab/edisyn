/**
   Copyright 2021 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.waldorfrocket;
import edisyn.*;

public class WaldorfRocketRec extends Recognize
{
	public static final int HEADER = 16;
	
    public static boolean recognize(byte[] data)
    {
    	return (data.length == HEADER + 22 &&		// 21 parameters plus 0xF7
				data[0] == (byte)0xF0 &&
				data[1] == (byte)0x7D &&
				data[2] == (byte)'E' &&
				data[3] == (byte)'D' &&
				data[4] == (byte)'I' &&
				data[5] == (byte)'S' &&
				data[6] == (byte)'Y' &&
				data[7] == (byte)'N' &&
				data[8] == (byte)'-' &&
				data[9] == (byte)'R' &&
				data[10] == (byte)'O' &&
				data[11] == (byte)'C' &&
				data[12] == (byte)'K' &&
				data[13] == (byte)'E' &&
				data[14] == (byte)'T' &&
				data[15] == (byte)0);            // I only recognize sysex version 0
    }
}
