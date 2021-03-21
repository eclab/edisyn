/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.kawaik4;
import edisyn.*;

public class KawaiK4MultiRec extends Recognize
{
    public static boolean recognize(byte[] data)
    {
        return ((data.length == EXPECTED_SYSEX_LENGTH &&
                 data[0] == (byte)0xF0 &&
                 data[1] == (byte)0x40 &&
                 data[3] == (byte)0x20 &&
                 data[4] == (byte)0x00 &&
                 data[5] == (byte)0x04 &&
                 (data[6] == (byte)0x00 || data[6] == (byte)0x02) &&
                 data[7] >= 64)  // that is, it's multi, not single
            
                || recognizeBank(data));
    }
        
    public static boolean recognizeBank(byte[] data)
    {
        return  (
                 // Block Multi Data Dump (5-9).
                 // We don't do All-Patch data Dump as that would conflict with the K4 Single editor
            
                 data.length == 4937 &&
                 data[0] == (byte)0xF0 &&
                 data[1] == (byte)0x40 &&
                 // don't care about 2, it's the channel
                 data[3] == (byte)0x21 &&    // block
                 data[4] == (byte)0x00 &&
                 data[5] == (byte)0x04 &&
                 // don't care about 6, we'll use it later
                 data[7] == (byte)0x40);  // Multi
    }

    public static final int EXPECTED_SYSEX_LENGTH = 77 + 9;        
    
    
}
