/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.maudiovenom;
import edisyn.*;

public class MAudioVenomArpRec extends Recognize
    {
    public static int getNumSysexDumpsPerPatch(byte[] data)
        {
        // This SHOULD force file loads to give us all the data, but I'm not positive
        return 2;
        }

    public static boolean recognize(byte[] data)
        {
        // There are multiple possible edit dumps based on the part in a multimode patch.
        // I presume they're all the same format but am not certain.
        return 
            // HEADERS
                        
            (data.length >= 35 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte) 0x00 &&       // M-Audio
            data[2] == (byte) 0x01 &&
            data[3] == (byte) 0x05 &&
            data[4] == (byte) 0x21 &&       // Venom
            // don't care about 5
            data[6] == (byte) 0x02 &&               // Write Data Dump
            data[7] == (byte) 0x03) ||              // Arp Data [Header?] Edit Dump

            (data.length == 35 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte) 0x00 &&       // M-Audio
            data[2] == (byte) 0x01 &&
            data[3] == (byte) 0x05 &&
            data[4] == (byte) 0x21 &&       // Venom
            // don't care about 5
            data[6] == (byte) 0x02 &&               // Write Data Dump
            data[7] == (byte) 0x00 &&               // Edit Buffer Dump
                (data[8] == (byte) 0x08 ||              // Arpeggio Header Single Edit Dump
                data[8] == (byte) 0x09 ||               // Arpeggio Header Part 1 Edit Dump
                data[8] == (byte) 0x0A ||               // Arpeggio Header Part 2 Edit Dump
                data[8] == (byte) 0x0B ||               // Arpeggio Header Part 3 Edit Dump
                data[8] == (byte) 0x0C)) ||             // Arpeggio Header Part 4 Edit Dump
                                 
                                 
            // PATTERNS
                                
            (data.length > 12 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte) 0x00 &&       // M-Audio
            data[2] == (byte) 0x01 &&
            data[3] == (byte) 0x05 &&
            data[4] == (byte) 0x21 &&       // Venom
            // don't care about 5
            data[6] == (byte) 0x02 &&               // Write Data Dump
            data[7] == (byte) 0x04) ||              // Arp Pattern Dump

            (data.length > 12 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte) 0x00 &&       // M-Audio
            data[2] == (byte) 0x01 &&
            data[3] == (byte) 0x05 &&
            data[4] == (byte) 0x21 &&       // Venom
            // don't care about 5
            data[6] == (byte) 0x02 &&               // Write Data Dump
            data[7] == (byte) 0x00 &&               // Edit Buffer Dump
                (data[8] == (byte) 0x0D ||              // Arpeggio Pattern Single Edit Dump
                data[8] == (byte) 0x0E ||              // Arpeggio Pattern Part 1 Edit Dump
                data[8] == (byte) 0x0F ||      // Arpeggio Pattern Part 2 Edit Dump
                data[8] == (byte) 0x10 ||      // Arpeggio Pattern Part 3 Edit Dump
                data[8] == (byte) 0x11));              // Arpeggio Pattern Part 4 Edit Dump
        }
    }
