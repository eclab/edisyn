/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.waldorfpulse2;
import edisyn.*;

public class WaldorfPulse2Rec extends Recognize
    {
    public static boolean recognize(byte[] data)
        {
        boolean v = 
            (data.length == 9 + 128 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x3E &&
            data[2] == (byte)0x16 &&
            data[4] == (byte)0x10);
        return v;
        }
    }
