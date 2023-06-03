/**
   Copyright 2023 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.dsiprophet12;
import edisyn.*;

public class DSIProphet12Rec extends Recognize
    {
    public static boolean recognize(byte[] data)
        {
        return (data.length == 1171 + 5 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x01 &&                // DSI
            data[2] == (byte)0x2A &&                // Prophet 12
            data[3] == (byte)0x03) ||               // Edit Buffer Data Dump
            (data.length == 1171 + 7 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x01 &&                // DSI
            data[2] == (byte)0x2A &&                // Prophet 12
            data[3] == (byte)0x02);                 // Patch Data Dump
        }
    }
