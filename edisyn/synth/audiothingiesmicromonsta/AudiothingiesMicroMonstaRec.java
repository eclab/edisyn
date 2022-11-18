/**
   Copyright 2022 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.audiothingiesmicromonsta;
import edisyn.*;

public class AudiothingiesMicroMonstaRec extends Recognize
    {
    public static boolean recognize(byte[] data)
        {
        boolean v = (data.length == 191 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x00 &&
            data[2] == (byte)0x21 &&
            data[3] == (byte)0x22 &&
            data[4] == (byte)0x4D &&
            data[5] == (byte)0x4D &&
            data[6] == (byte)0x03);
        return v;
        }
    }
