/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.korgmicrosampler;
import edisyn.*;

public class KorgMicrosamplerRec extends Recognize
{
    public static boolean recognize(byte[] data)
    {
        boolean val = (data.length == 780 &&
                       data[0] == (byte)0xF0 &&
                       data[1] == (byte)0x7D &&
                       data[2] == (byte)'E' &&
                       data[3] == (byte)'D' &&
                       data[4] == (byte)'I' &&
                       data[5] == (byte)'S' &&
                       data[6] == (byte)'Y' &&
                       data[7] == (byte)'N' &&
                       data[8] == (byte)' ' &&
                       data[9] == (byte)'K' &&
                       data[10] == (byte)'O' &&
                       data[11] == (byte)'R' &&
                       data[12] == (byte)'G' &&
                       data[13] == (byte)' ' &&
                       data[14] == (byte)'M' &&
                       data[15] == (byte)'I' &&
                       data[16] == (byte)'C' &&
                       data[17] == (byte)'R' &&
                       data[18] == (byte)'O' &&
                       data[19] == (byte)'S' &&
                       data[20] == (byte)'A' &&
                       data[21] == (byte)'M' &&
                       data[22] == (byte)'P' &&
                       data[23] == (byte)'L' &&
                       data[24] == (byte)'E' &&
                       data[25] == (byte)'R' &&
                       data[26] == (byte)0);
        return val;
    }
        
}
