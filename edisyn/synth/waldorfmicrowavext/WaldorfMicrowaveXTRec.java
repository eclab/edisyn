/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.waldorfmicrowavext;
import edisyn.*;

public class WaldorfMicrowaveXTRec extends Recognize
    {
    public static boolean recognize(byte[] data)
        {
        boolean v = (
            data.length == EXPECTED_SYSEX_LENGTH &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x3E &&
            data[2] == (byte)0x0E &&
            data[4] == (byte)0x10);
        return v;
        }
        


    public static final int EXPECTED_SYSEX_LENGTH = 265;        
    }
