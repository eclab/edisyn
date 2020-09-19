/**
	Copyright 2020 by Sean Luke
	Licensed under the Apache License version 2.0
*/

package edisyn.synth.kawaik4;
import edisyn.*;

public class KawaiK4DrumRec extends Recognize
	{
    public static final int EXPECTED_SYSEX_LENGTH = 682 + 9;
    
    public static boolean recognize(byte[] data)
        {
        return (data.length == EXPECTED_SYSEX_LENGTH &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x40 &&
            data[3] == (byte)0x20 &&
            data[4] == (byte)0x00 &&
            data[5] == (byte)0x04 &&
            (data[6] == (byte)0x01 || data[6] == (byte)0x03) &&
            data[7] == 32);
        }

	}