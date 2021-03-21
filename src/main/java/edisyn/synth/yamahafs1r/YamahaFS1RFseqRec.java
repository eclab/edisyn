/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.yamahafs1r;
import edisyn.*;

public class YamahaFS1RFseqRec extends Recognize
{
    public static boolean recognize(byte[] data)
    {
        return ((
                 (data.length == 11 + 32 + 50 * 128) ||
                 (data.length == 11 + 32 + 50 * 256) ||
                 (data.length == 11 + 32 + 50 * 384) ||
                 (data.length == 11 + 32 + 50 * 512)) &&
                data[0] == (byte)0xF0 &&
                data[1] == (byte)0x43 &&
                data[3] == (byte)0x5E);
    }
               
}
