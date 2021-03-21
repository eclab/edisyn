/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.generic;
import edisyn.*;

public class GenericRec extends Recognize
{
    public static final int HEADER = 12;
    public static boolean recognize(byte[] data)
    {
        return (data.length == HEADER + 1 + (18 * 19 * 3 + 128 * 21) + 7) &&
            data[0] == (byte)0xF0 &&
            data[1] == 0x7D &&
            data[2] =='E' &&
            data[3] =='D' &&
            data[4] =='I' &&
            data[5] =='S' &&
            data[6] =='Y' &&
            data[7] =='N' &&
            data[8] ==' ' &&
            data[9] =='C' &&
            data[10] =='C' &&
            data[11] == 0;                      // version number.  We recognize only 0
    }
}
