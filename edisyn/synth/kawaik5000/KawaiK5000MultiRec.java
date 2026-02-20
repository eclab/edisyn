/**
   Copyright 2026 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.kawaik5000;
import edisyn.*;

public class KawaiK5000MultiRec extends Recognize
    {
    public static boolean recognizeBank(byte[] data)
    	{
		return ((data[0] & 0xFF) == 0xF0 &&
                data[1] == 0x40 &&
                data[3] == 0x21 &&
                data[4] == 0x00 &&
                data[5] == 0x0A &&
                data[6] == 0x20 &&
                (data[data.length - 1] & 0xFF) == 0xF7);
    	}
    	
    public static boolean recognize(byte[] data)
        {
        if (recognizeBank(data)) { System.err.println("It's a bank!"); return true; }

        return (data.length == 9 + 54 + 4 * 12 + 1 &&
            (data[0] & 0xFF) == 0xF0 &&
            data[1] == 0x40 &&
            data[3] == 0x20 &&
            data[4] == 0x00 &&
            data[5] == 0x0A &&
            data[6] == 0x20 &&
            (data[data.length - 1] & 0xFF) == 0xF7);
        }
    }
