/**
   Copyright 2026 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.novationbassstation2;
import edisyn.*;

public class NovationBassStation2Rec extends Recognize
    {
    public static boolean recognize(byte[] data)
        {
        return (data.length == 154 &&
            data[0] == (byte)0xF0 &&
            data[1] == 0x00 &&
            data[2] == 0x20 &&
            data[3] == 0x29 &&
            data[4] == 0x00 &&
            data[5] == 0x33 &&
            data[6] == 0x00);
        }
    }

