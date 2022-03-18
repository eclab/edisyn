/**
   Copyright 2022 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.waldorfmicrowave;
import edisyn.*;

public class WaldorfMicrowaveMultiRec extends Recognize
    {
    public static boolean recognize(byte[] data)
        {
        return (
            data.length == EXPECTED_SYSEX_LENGTH &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x3E &&
            data[2] == (byte)0x00 &&
            data[4] == (byte)0x43) ||           // ARPD Dump
            
            recognizeBank(data);
        }
        
    
    public static boolean recognizeBank(byte[] data)
        {
        return  (
            data.length == EXPECTED_BULK_LENGTH &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x3E &&
            data[2] == (byte)0x00 &&
            data[4] == (byte)0x51);             // APBD Bank Dump
        } 

    public static final int EXPECTED_SYSEX_LENGTH = 233;
    public static final int EXPECTED_BULK_LENGTH = 14471;
    }
