/**
   Copyright 2021 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.rolandjv880;
import edisyn.*;

public class RolandJV880DrumRec extends Recognize
    {
    public static final int EXPECTED_SYSEX_LENGTH = 52 + 11;
    public static final int EXPECTED_FULL_LENGTH = 3843;
    
    public static boolean recognize(byte[] data)
        {
        return ((data.length == EXPECTED_SYSEX_LENGTH || data.length == EXPECTED_FULL_LENGTH) &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x41 &&
            // don't care about data[2]
            data[3] == (byte)0x46 &&
            data[4] == (byte)0x12 &&

            // Temporary:       00 07 4x 00             x between 0x40 and 0x40+0x3C
                ((data[5] == (byte)0x00 && data[6] == (byte)0x07 && data[7] >= (byte)0x40 && data[7] <= (byte)(0x40 + 0x3C) && data[8] == 0x00) ||
                // Internal:        01 7F 4x 00
                (data[5] == (byte)0x01 && data[6] == (byte)0x7F && data[7] >= (byte)0x40 && data[7] <= (byte)(0x40 + 0x3C) && data[8] == 0x00) ||
                // Card:            02 7F 4x 00
                (data[5] == (byte)0x02 && data[6] == (byte)0x7F && data[7] >= (byte)0x40 && data[7] <= (byte)(0x40 + 0x3C) && data[8] == 0x00))           
            );
        }

    public static int getNextSysexPatchGroup(byte[][] sysex, int start)
        {
        // we need 61 messages
        for(int i = 0; i < 61; i++)
            {
            if (!recognize(sysex[start + i]))
                {
                System.err.println("RolandJV880DrumRec.getNextSysexPatchGroup(): could not find drum " + i + ".");
                return start;
                }
            }
        return start + 61;
        }

    }
