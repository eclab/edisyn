/**
   Copyright 2021 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.waldorfmicrowave;
import edisyn.*;

public class WaldorfMicrowaveRec extends Recognize
    {
    public static final int EXPECTED_SYSEX_LENGTH = 187;        

    public static boolean recognize(byte[] data)
        {
        boolean v = (
            data.length == EXPECTED_SYSEX_LENGTH &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x3E &&
            data[2] == (byte)0x00 &&
            data[4] == (byte)0x42) ||           // BPRD Dump
            (
            data.length == EXPECTED_SYSEX_LENGTH + 1 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x3E &&
            data[2] == (byte)0x00 &&
            data[4] == (byte)0x4B) ||           // ABPD Dump, not likely to happen
            (
            data.length == 11527 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x3E &&
            data[2] == (byte)0x00 &&
            data[4] == (byte)0x50);             // BPBD Bank Dump
        return v;
        }
    }
