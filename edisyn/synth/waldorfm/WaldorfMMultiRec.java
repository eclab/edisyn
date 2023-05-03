/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.waldorfm;
import edisyn.*;

public class WaldorfMMultiRec extends Recognize
    {
    public static boolean recognize(byte[] data)
        {
        return (data.length == 320 &&
            data[0] == (byte)0xF0 &&
            data[1] == 0x3E &&
            data[2] == 0x30 &&
            // skip data[3], it might be the ID later
            data[4] == 0x73);
        }
    }
