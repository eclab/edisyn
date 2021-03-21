/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.alesisd4;
import edisyn.*;

public class AlesisD4Rec extends Recognize
{
    public static boolean recognize(byte[] data)
    {
        return data.length == 343 && 
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x00 &&
            data[2] == (byte)0x00 &&
            data[3] == (byte)0x0E &&
            (data[4] == (byte)0x06 || data[4] == (byte)0x13) &&           
            (data[6] == (byte)0x01 || (data[6] >= 32 && data[6] <= (32 + 20)));
    }
}
