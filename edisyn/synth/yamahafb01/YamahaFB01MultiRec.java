/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.yamahafb01;
import edisyn.*;

public class YamahaFB01MultiRec extends Recognize
{
    public static boolean recognize(byte[] data)
    {
        return 
            // current configuration buffer,
            // see bottom of page 55, user manual
            (data.length == 8 + 160 + 2 + 1 &&
             data[1] == 0x43 &&
             data[2] == 0x75 &&
             data[4] == 0x00 &&
             data[5] == 0x01 &&
             data[6] == 0x00) ||
            // configuration memory xx
            // see top of page 56, user manual
            (data.length == 8 + 160 + 2 + 1 &&
             data[1] == 0x43 &&
             data[2] == 0x75 &&
             data[4] == 0x00 &&
             data[5] == 0x02) ||
            // all configuration memory,
            // see page 56, user manual
            (data.length == 8 + 16 * (160 + 2 + 1) &&
             data[1] == 0x43 &&
             data[2] == 0x75 &&
             data[4] == 0x00 &&
             data[5] == 0x03 &&
             data[6] == 0x00);
    }
}
