/**
   Copyright 2021 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.waldorfmicrowave;
import edisyn.*;

public class WaldorfMicrowaveMultiRec extends Recognize
    {
    public static boolean recognize(byte[] data)
        {
        boolean v = (
            data.length == EXPECTED_SYSEX_LENGTH &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x3E &&
            data[2] == (byte)0x00 &&
            data[4] == (byte)0x43) ||		// ARPD Dump
            (
            data.length == 14471 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x3E &&
            data[2] == (byte)0x00 &&
            data[4] == (byte)0x51);		// APBD Bank Dump
        return v;
        }
        
    
    public static final int EXPECTED_SYSEX_LENGTH = 233;
    }
