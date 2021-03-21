/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.preenfm2;
import edisyn.*;

public class PreenFM2Rec extends Recognize
{
    public static boolean recognize(byte[] data)
    {
        boolean val = (data.length == 473 &&
                       data[0] == (byte)0xF0 &&
                       data[1] == (byte)0x7D &&
                       data[2] == (byte)'E' &&
                       data[3] == (byte)'D' &&
                       data[4] == (byte)'I' &&
                       data[5] == (byte)'S' &&
                       data[6] == (byte)'Y' &&
                       data[7] == (byte)'N' &&
                       (data[8] == (byte)' ' || data[8] == (byte)'-') &&           // versions 1 and 0
                       data[9] == (byte)'P' &&
                       data[10] == (byte)'R' &&
                       data[11] == (byte)'E' &&
                       data[12] == (byte)'E' &&
                       data[13] == (byte)'N' &&
                       data[14] == (byte)'F' &&
                       data[15] == (byte)'M' &&
                       data[16] == (byte)'2' &&
                       (data[8] == (byte)'-' ? 
                        (data[17] == (byte)1) :         // version 1
                        true));                                        // version "0" -- who knows what value data[17] would be
        return val;
    }

}
