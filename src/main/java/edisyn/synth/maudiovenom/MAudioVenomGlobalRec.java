/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.maudiovenom;
import edisyn.*;

public class MAudioVenomGlobalRec extends Recognize
{
    public static boolean recognize(byte[] data)
    {
        return (data.length == 36 &&
                data[0] == (byte)0xF0 &&
                data[1] == (byte) 0x00 &&       // M-Audio
                data[2] == (byte) 0x01 &&
                data[3] == (byte) 0x05 &&
                data[4] == (byte) 0x21 &&       // Venom
                // don't care about 5
                data[6] == (byte) 0x02 &&               // Write Data Dump
                data[7] == (byte) 0x00 &&
                data[8] == (byte) 0x00);
    }
}
