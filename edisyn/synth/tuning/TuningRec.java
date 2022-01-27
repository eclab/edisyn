/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.tuning;
import edisyn.*;

public class TuningRec extends Recognize
    {
    public static boolean recognize(byte[] data) 
        {
        return 
        	(
        	 data[0] == (byte) 0xF0 && 
        	 data[1] == 0x7E && 
        	 data[3] == 0x08 && 
        	 data[4] == 0x01 && 
        	 data.length == 8 + 16 + 3 * 128)
        	||
        	// TX81Z 
        	(
			data[0] == (byte) 0xF0 && 
        	 data[1] == 0x43 && 
        	 data[3] == 0x7E && 
        	 data[4] == 0x00 && 
        	 data[5] == 0x22 && 
        	 data[6] == 'L' && 
        	 data[7] == 'M' && 
        	 data[8] == ' ' && 
        	 data[9] == ' ' && 
        	 data[10] == 'M' && 
        	 data[11] == 'C' && 
        	 data[12] == 'R' && 
        	 data[13] == 'T' && 
        	 data[14] == 'E' && 
        	 data[15] == '1' && 
        	 data.length == 274);
            }

    }
