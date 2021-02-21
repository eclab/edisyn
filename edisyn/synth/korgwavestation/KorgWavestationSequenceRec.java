/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.korgwavestation;
import edisyn.*;

public class KorgWavestationSequenceRec extends Recognize
{
    public static final int EXPECTED_SYSEX_LENGTH = 17576;
    public static boolean recognize(byte[] data)
    {
        if (data.length > 22 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x7D &&
            data[2] == (byte)'E' &&
            data[3] == (byte)'D' &&
            data[4] == (byte)'I' &&
            data[5] == (byte)'S' &&
            data[6] == (byte)'Y' &&
            data[7] == (byte)'N' &&
            data[8] == (byte)' ' &&
            data[9] == (byte)'K' &&
            data[10] == (byte)'O' &&
            data[11] == (byte)'R' &&
            data[12] == (byte)'G' &&
            data[13] == (byte)'W' &&
            data[14] == (byte)'S' &&
            data[15] == (byte)'S' &&
            data[16] == (byte)'R' &&
            data[17] == (byte)' ' &&
            data[18] == (byte)'S' &&
            data[19] == (byte)'E' &&
            data[20] == (byte)'Q' &&
            data[21] == (byte)0)
            return true;
        else return recognizeBank(data);
    }
        
    public static boolean recognizeBank(byte[] data)
    {
        boolean b = (data.length == EXPECTED_SYSEX_LENGTH &&
                     data[0] == (byte)0xF0 &&
                     data[1] == (byte)0x42 &&
                     data[3] == (byte)0x28 &&
                     data[4] == (byte)0x54);   
        return b;              
    }
    
}
