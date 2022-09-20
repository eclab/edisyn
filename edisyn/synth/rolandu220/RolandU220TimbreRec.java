/**
   Copyright 2022 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.rolandu220;
import edisyn.*;

public class RolandU220TimbreRec extends Recognize
    {
    public static boolean recognize(byte[] data)
        {
        return (
       	data.length <= 138 && data.length >= 74 &&
        (data[0] == (byte)0xF0) &&
            (data[1] == (byte)0x41) &&
            (data[3] == (byte)0x2B) &&
            (data[4] == (byte)0x12) &&
			// Timbre Temporary
		    ((data[5] == (byte)0x00) && (data[6] == (byte)0x11 || data[6] == (byte)0x12 || data[6] == (byte)0x13 ||
            							data[6] == (byte)0x14 || data[6] == (byte)0x15 || data[6] == (byte)0x16) ||
  			  // Timbre Permanent
  		      (data[5] == (byte)0x02)));
        }
    }
