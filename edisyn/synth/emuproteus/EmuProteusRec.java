/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.emuproteus;
import edisyn.*;

public class EmuProteusRec extends Recognize
{
    public static final int NUM_PARAMETERS = 128;
        
    public static boolean recognize(byte[] data)
    {
        return  data.length == 9 + NUM_PARAMETERS * 2 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte) 0x18 &&
            data[2] == (byte) 0x04 &&
            data[4] == (byte) 0x01;
    }
}
