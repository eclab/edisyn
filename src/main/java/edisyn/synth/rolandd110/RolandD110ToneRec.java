/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.rolandd110;
import edisyn.*;

public class RolandD110ToneRec extends Recognize
{
    // Sysex dumps from the emitLocation are TEMP_TONE_LENGTH long
    public static final int TEMP_TONE_LENGTH = 256;  // 10 bytes + 246 data bytes
    // Sysex dumps from a RAM slot are MEMORY_TONE_LENGTH long
    public static final int MEMORY_TONE_LENGTH = 266;  // 10 bytes + 256 data bytes

    public static boolean recognize(byte[] data)
    {
        return ((data[0] == (byte)0xF0) &&
                (data[1] == (byte)0x41) &&
                (data[3] == (byte)0x16) &&
                (data[4] == (byte)0x12) &&
                (data[5] == 0x02 || data[5] == (byte)0x04 || data[5] == (byte)0x08)) &&  // tones
            // tone temporary areas
            (data.length == TEMP_TONE_LENGTH || data.length == MEMORY_TONE_LENGTH);
    }
        
}
