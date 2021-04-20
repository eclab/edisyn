/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.yamahatg33;
import edisyn.*;

public class YamahaTG33MultiRec extends Recognize
    {
    public static boolean recognize(byte[] data)
        {
        return  (
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x43 &&
            // don't care about 2, it's the id
            data[3] == (byte)0x7E &&
            data[6] == (byte)'L' &&
            data[7] == (byte)'M' &&
            data[8] == (byte)' ' &&
            data[9] == (byte)' ' &&
            data[10] == (byte)'0' &&
            data[11] == (byte)'0' &&
            data[12] == (byte)'1' &&
            data[13] == (byte)'2' &&
            data[14] == (byte)'M' &&
            data[15] == (byte)'E');
        }
    }
