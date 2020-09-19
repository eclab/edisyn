/**
	Copyright 2020 by Sean Luke
	Licensed under the Apache License version 2.0
*/

package edisyn.synth.casiocz;
import edisyn.*;

public class CasioCZRec extends Recognize
	{
    public static boolean recognize(byte[] data)
        {
        // We need to recognize four formats
        
        // F0 44 00 00 7N 20 DD [BREAK] [256 bytes] F7
        // Receive Request from CZ-101/1K/3K/5K
        // N = Channel
        // DD = memory bank

        // F0 44 00 00 7N 21 DD [BREAK] [288 bytes] F7
        // Receive Request from CZ-1
        // N = Channel
        // DD = memory bank

        // These two may be in response to me doing a send, so we need to interept them as well

        // F0 44 00 00 7N 30 [BREAK] [256 bytes] F7
        // Send Request from CZ-101/1K/3K/5K
        // N = Channel
        
        // F0 44 00 00 7N 30 [BREAK] [288 bytes] F7
        // Send Request from CZ-1               // Note identical, but longer
        // N = Channel
        

        return (
            data[1] == 0x44 &&
            data[2] == 0x00 &&
            data[3] == 0x00 &&
            (data[4] >= 0x70 && data[4] <= 0x7F) &&
            (data[5] == 0x20 || data[5] == 0x21 || data[5] == 0x30));
        }

	}