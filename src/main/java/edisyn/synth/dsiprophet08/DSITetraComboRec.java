/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.dsiprophet08;
import edisyn.*;

public class DSITetraComboRec extends Recognize
{
    public static final byte TETRA_ID = 0x26;

    public static boolean recognize(byte[] data)
    {
        return ((data.length == 1177 &&
                 data[0] == (byte)0xF0 &&
                 data[1] == (byte) 0x01 &&       // DSI
                 (data[2] == TETRA_ID) &&
                 data[3] == (byte) 0x22) ||      // Combo Data Dump
                (data.length == 1176 &&
                 data[0] == (byte)0xF0 &&
                 data[1] == (byte) 0x01 &&       // DSI
                 (data[2] == TETRA_ID) &&
                 data[3] == (byte) 0x37));       // Combo Edit Buffer Data Dump        
    }
        
}
