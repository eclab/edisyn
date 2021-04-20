/**
   Copyright 2021 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.novationsl;
import edisyn.*;

public class NovationSLRec extends Recognize
    {
    public static boolean recognize(byte[] data)
        {
        return (data.length == 4112 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x00 &&
            data[2] == (byte)0x20 &&
            data[3] == (byte)0x29 &&
            data[4] == (byte)0x02 &&
            data[5] == (byte)0x03 &&
            data[6] == (byte)0x7F &&
            
            // There are three possibilities at this point:
            // WRITE:               F0 00 20 29 02 03 7F 01/00 00 09 06 00 PATCHNUM <DATA...> 12 34 F7
            //      This message is sent to the SL from the software editor; I believe it is for
            //      patches which have previously been assigned a patch number, such as ones which the
            //      editor had previously received from the SL and is now sending back to it edited.
            //      
            // UPLOAD:              F0 00 20 29 02 03 7F 00 00 11 02 00 01 <DATA...> 12 34 F7
            //      This message is sent to the SL from the software editor for brand new patches'
            //      created from scratch.  Why we cannot specify the patch number, I have no idea.
            //
            // RECEIVED:    F0 00 20 29 02 03 7F 00 00 0B 0E 00 PATCHNUM <DATA...> 12 34 F7
            //      This message is received by the software editor from the SL.

            // WRITE
                ((  //data[7] == (byte)0x01 &&
                    data[8] == (byte)0x00 &&
                    data[9] == (byte)0x09 &&
                    data[10] == (byte)0x06 &&
                    data[11] == (byte)0x00 ) ||

                // UPLOAD
                    (  //data[7] == (byte)0x00 &&
                    data[8] == (byte)0x00 &&
                    data[9] == (byte)0x11 &&
                    data[10] == (byte)0x02 &&
                    data[11] == (byte)0x00 &&
                    data[12] == (byte)0x01 ) ||

                // RECEIVED
                    (  //data[7] == (byte)0x00 &&
                    data[8] == (byte)0x00 &&
                    data[9] == (byte)0x0B &&
                    data[10] == (byte)0x0E &&
                    data[11] == (byte)0x00 )));
        }
    }
