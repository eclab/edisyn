/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.korgmicrokorg;
import edisyn.*;

public class KorgMicroKorgVocoderRec extends Recognize
{
    // converts all but last byte (F7)
    static byte[] convertTo8Bit(byte[] data, int offset)
    {
        // How big?
        int size = (data.length - offset - 1) / 8 * 7;
        if ((data.length - offset - 1) % 8 > 0)
            size += ((data.length - offset - 1) % 8 - 1);           
        byte[] newd = new byte[size];
        
        int j = 0;
        for(int i = offset; i < data.length; i += 8)
            {
                for(int x = 0; x < 7; x++)
                    {
                        if (j + x < newd.length)
                            newd[j + x] = (byte)(data[i + x + 1] | (byte)(((data[i] >>> x) & 0x1) << 7));
                    }
                j += 7;
            }
        return newd;
    }
        
    public static final int EXPECTED_SYSEX_LENGTH = 297;
    public static boolean recognize(byte[] data)
    {
        boolean v = (
                     data.length == EXPECTED_SYSEX_LENGTH &&
                     data[0] == (byte)0xF0 &&
                     data[1] == (byte)0x42 &&
                     data[3] == (byte)0x58 &&
                     data[4] == (byte)0x40);
        if (v == false) return false;
        
        // now decode.  Are we synth or vocoder?
        data = convertTo8Bit(data, 5);
        int voicemode = (data[16] >>> 4) & 3;
        return (voicemode == 3);  // vocoder
    }

}
