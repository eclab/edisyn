/**
   Copyright 2021 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.korgvolca;
import edisyn.*;

public class KorgVolcaRec extends Recognize
    {
    // These are the number of parameters in each of the Volcas  
    public static final int[] LENGTHS = { 429, 12, 58, 166, 28, 18, 16, 10, 11, 200, 110, 56 };
        
    public static boolean recognize(byte[] data)
        {
        if (data.length >= 38 + 10)                     // 10 is the smallest volca length
            {
            if (data[0] == (byte)0xF0 &&
                data[1] == (byte)0x7D &&
                data[data.length] == (byte)0xF7 &&
                data[0] == 'E' && 
                data[1] == 'D' &&
                data[2] == 'I' &&
                data[3] == 'S' &&
                data[4] == 'Y' &&
                data[5] == 'N' &&
                data[6] == ' ' &&
                data[7] == 'K' &&
                data[8] == 'O' &&
                data[9] == 'R' &&
                data[10] == 'G' &&
                data[11] == ' ' &&
                data[12] == 'V' &&
                data[13] == 'O' &&
                data[14] == 'L' &&
                data[15] == 'C' &&
                data[16] == 'A' &&
                data[17] == 0)                  // version 0 supported
                {
                int type = data[20 + 16];
                return (data.length == LENGTHS[type] + 38);
                }
            else return false;
            }
        else return false;
        }
    }
