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
        return (data[0] == (byte) 0xF0 && data[1] == 0x7E && data[3] == 0x08 && data[4] == 0x01
            && data.length == 8 + 16 + 3 * 128);
        }

	}