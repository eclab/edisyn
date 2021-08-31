/**
   Copyright 2021 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.korgvolca;
import edisyn.*;

public class KorgVolcaRec extends Recognize
    {
    // These are the number of parameters in each of the Volcas  
    public static final int[] LENGTHS = { 567, 12, 20, 58, 166, 18, 16, 10, 11, 200, 56 };
        
    public static boolean recognize(byte[] data)
        {
        if (data.length >= 38 + 10)                     // 10 is the smallest volca length
            {
            if (data[0] == (byte)0xF0 &&
                data[1] == (byte)0x7D &&
                data[data.length - 1] == (byte)0xF7 &&
                data[2] == 'E' && 
                data[3] == 'D' &&
                data[4] == 'I' &&
                data[5] == 'S' &&
                data[6] == 'Y' &&
                data[7] == 'N' &&
                data[8] == ' ' &&
                data[9] == 'K' &&
                data[10] == 'O' &&
                data[11] == 'R' &&
                data[12] == 'G' &&
                data[13] == ' ' &&
                data[14] == 'V' &&
                data[15] == 'O' &&
                data[16] == 'L' &&
                data[17] == 'C' &&
                data[18] == 'A' &&
                data[19] == 0)                  // version 0 supported
                {
                int type = data[20 + 16];
                return (type < data.length && data.length == LENGTHS[type] + 38);
                }
            else return false;
            }
        else return false;
        }
    }
