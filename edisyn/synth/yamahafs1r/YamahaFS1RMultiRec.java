/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.yamahafs1r;
import edisyn.*;

public class YamahaFS1RMultiRec extends Recognize
    {
    public static boolean recognize(byte[] data)
        {
        final int BYTE_COUNT = 400;
        return (data.length == BYTE_COUNT + 11 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x43 &&
            // don't care about 2, it's the channel
            data[3] == (byte)0x5E);
        }
               
    }
