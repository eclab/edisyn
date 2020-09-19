/**
	Copyright 2020 by Sean Luke
	Licensed under the Apache License version 2.0
*/

package edisyn.synth.yamaha4op;
import edisyn.*;

public class Yamaha4OpMultiRec extends Recognize
	{
    public static boolean recognize(byte[] data)
        {
        // PCED
        return ((data.length == 128 &&        
                data[0] == (byte)0xF0 &&
                data[1] == (byte)0x43 &&
                // don't care about 2, it's the channel
                data[3] == 0x7E &&
                data[4] == 0x00 &&
                data[5] == 0x78 &&
                // next it spits out the header "LM  8976PE"
                data[6] == 'L' &&
                data[7] == 'M' &&
                data[8] == ' ' &&
                data[9] == ' ' &&
                data[10] == '8' &&
                data[11] == '9' &&
                data[12] == '7' &&
                data[13] == '6' &&
                data[14] == 'P' &&
                data[15] == 'E')

            || recognizeBulk(data));
        
        }
        
    public static boolean recognizeBulk(byte[] data)
        {
        // PMEM
        boolean b = (data.length == 2450 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x43 &&
            // don't care about 2, it's the channel
            data[3] == (byte)0x7E &&
            data[4] == (byte)0x13 &&        // manual says 10 but this is wrong
            data[5] == (byte)0x0A &&
            // next it spits out the header "LM  8976PM"
            data[6] == 'L' &&
            data[7] == 'M' &&
            data[8] == ' ' &&
            data[9] == ' ' &&
            data[10] == '8' &&
            data[11] == '9' &&
            data[12] == '7' &&
            data[13] == '6' &&
            data[14] == 'P' &&
            data[15] == 'M');

        return b;
        }
	}