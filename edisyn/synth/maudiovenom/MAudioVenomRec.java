/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.maudiovenom;
import edisyn.*;

public class MAudioVenomRec extends Recognize
{
    public static boolean recognize(byte[] data)
    {
        // There is a major bug in single edit (non-current) dumps, which inserts an extra spurious byte
        // so it's 240 rather than 239
        
        // Because of this bug, I am not certain what the output format looks like for the multi part
        // dumps, but I'm including them below on the assumption that they're properly 239.
        
        return (data.length == 240 &&
                data[0] == (byte)0xF0 &&
                data[1] == (byte) 0x00 &&       // M-Audio
                data[2] == (byte) 0x01 &&
                data[3] == (byte) 0x05 &&
                data[4] == (byte) 0x21 &&       // Venom
                // don't care about 5
                data[6] == (byte) 0x02 &&               // Write Data Dump
                (data[7] == (byte) 0x01 || data[7] == (byte) 0x00)) ||              // Single Edit Dump

            (data.length == 239 &&
             data[0] == (byte)0xF0 &&
             data[1] == (byte) 0x00 &&       // M-Audio
             data[2] == (byte) 0x01 &&
             data[3] == (byte) 0x05 &&
             data[4] == (byte) 0x21 &&       // Venom
             // don't care about 5
             data[6] == (byte) 0x02 &&               // Write Data Dump
             data[7] == (byte) 0x00 &&               // Edit Buffer Dump
             (data[8] == (byte) 0x01 ||              // Single Edit Dump
              data[8] == (byte) 0x03 ||              // Multi Part 1
              data[8] == (byte) 0x04 ||      // Multi Part 2
              data[8] == (byte) 0x05 ||      // Multi Part 3
              data[8] == (byte) 0x06)                // Multi Part 4
                                 
             );     // Single Edit Dump

    }
}
