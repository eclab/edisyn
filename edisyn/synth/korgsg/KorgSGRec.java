/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.korgsg;
import edisyn.*;

public class KorgSGRec extends Recognize
    {
    public static final int EXPECTED_SYSEX_LENGTH = 64;
    public static boolean recognize(byte[] data)
        {
        boolean v = (
            data.length == EXPECTED_SYSEX_LENGTH &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x42 &&
            // don't care
            //data[2] == (byte)(48 + getChannelOut()) &&
            data[3] == (byte)0x4A &&
            data[4] == (byte)0x40);
        return v;
        }
    }
