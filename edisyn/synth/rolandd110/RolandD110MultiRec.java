/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.rolandd110;
import edisyn.*;

public class RolandD110MultiRec extends Recognize
    {
    public static boolean recognize(byte[] data)
        {
        return ((data[0] == (byte)0xF0) &&
            (data[1] == (byte)0x41) &&
            (data[3] == (byte)0x16) &&
            (data[4] == (byte)0x12) &&
            (data[5] == 0x06)) &&  // patches
            (data.length == 138);
        }
                
    }
