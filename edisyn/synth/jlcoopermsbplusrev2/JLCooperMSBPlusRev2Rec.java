/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.jlcoopermsbplusrev2;
import edisyn.*;

public class JLCooperMSBPlusRev2Rec extends Recognize
    {
    public static boolean recognize(byte[] data)
        {
        if (data.length < 3) return false;
        if (!(data[0] == (byte)0xF0 && data[1] == 0x15 && data[2] == 0x0b)) return false;
        
        return
            (data.length == 45 && data[3] == 0x14) ||
            (data.length == 46 && data[3] == 0x12) ||
            (data.length == 21 && data[3] == 0x04) ||
            (data.length == 22 && data[3] == 0x02);
        }
    }
