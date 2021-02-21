/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.yamahafb01;
import edisyn.*;

public class YamahaFB01Rec extends Recognize
{
    public static String getBankName(byte[] data)
    {
        int pos = (data[2] == 0x75 ? 9 : 6);
        char[] name = new char[8];
        for(int i = 0; i < 8; i++)
            {
                name[i] = (char)(data[pos + i * 2] | (data[pos + i * 2 + 1] << 4));
            }
        return new String(name).trim();
    }

    public static boolean recognizeBank(byte[] data)
    {
        return 
            // Voice Bank 0, see top of page 55, user manual
            (data.length == 8 + 16 + 48 + 48 * (64 * 2 + 3) &&
             data[0] == (byte)0xF0 &&
             data[1] == 0x43 &&
             data[3] == 0x0C) ||
            // Voice Bank x, see page 55, user manual
            (data.length == 11 + 16 + 48 + 48 * (64 * 2 + 3) &&
             data[0] == (byte)0xF0 &&
             data[1] == 0x43 &&
             data[2] == 0x75 &&
             data[4] == 0x00 &&
             data[5] == 0x00);
    }

    public static boolean recognize(byte[] data)
    {
        return recognizeBank(data) ||
            // Instrument i voice data, see page 57, user manual
            (data.length == 8 + (64 * 2 + 3) &&
             data[0] == (byte)0xF0 &&
             data[1] == 0x43 &&
             data[2] == 0x75 &&
             data[5] == 0x00 &&
             data[6] == 0x00);
    }
}
