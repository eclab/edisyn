/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.kawaik5;
import edisyn.*;

public class KawaiK5Rec extends Recognize
    {
    public static boolean recognize(byte[] data)
        {
        return ((data.length == EXPECTED_SYSEX_LENGTH) &&
            (data[0] == (byte)0xF0) &&
            (data[1] == (byte)0x40) &&
            ((data[3] == (byte)0x20) || (data[3] == (byte)0x21)) &&
            (data[4] == (byte)0x00) &&
            (data[5] == (byte)0x02) &&  // K5
            (data[6] == (byte)0x00));
        }

    public static final int EXPECTED_SYSEX_LENGTH = 993;        
    }
