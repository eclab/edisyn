/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.waldorfkyra;
import edisyn.*;

public class WaldorfKyraRec extends Recognize
{
    public static boolean recognize(byte[] data)
    {
        return (data.length == 224 + 10 &&
                data[0] == (byte)0xF0 &&
                data[1] == 0x3E &&
                data[2] == 0x22 &&
                (data[4] == 0x00 || data[4] == 0x40));                              // "Patch (whole)" -- it's not clear why these are different
    }
}
