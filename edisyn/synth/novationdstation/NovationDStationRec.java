/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.novationdstation;
import edisyn.*;

public class NovationDStationRec extends Recognize
    {
    public static boolean recognize(byte[] data)
        {
        return (data.length == 288 &&
            data[0] == (byte)0xF0 &&
            data[1] == 0x00 &&
            data[2] == 0x20 &&
            data[3] == 0x29 &&
            data[4] == 0x02 &&
            data[5] == 0x01 &&
            data[6] == 0x22) || recognizeBank(data);
        }

    public static boolean recognizeBank(byte[] data)
        {
        return (data.length == 4104 &&
            data[0] == (byte)0xF0 &&
            data[1] == 0x00 &&
            data[2] == 0x20 &&
            data[3] == 0x29 &&
            data[4] == 0x02 &&
            data[5] == 0x01 &&
            data[6] == 0x11);
        }

    }
