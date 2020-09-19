/**
	Copyright 2020 by Sean Luke
	Licensed under the Apache License version 2.0
*/

package edisyn.synth.kawaik1;
import edisyn.*;

public class KawaiK1MultiRec extends Recognize
	{
    public static boolean recognize(byte[] data)
        {
        return ((data.length == EXPECTED_SYSEX_LENGTH &&
                data[0] == (byte)0xF0 &&
                data[1] == (byte)0x40 &&
                data[3] == (byte)0x20 &&
                data[4] == (byte)0x00 &&
                data[5] == (byte)0x03 &&
                (data[6] == (byte)0x00 || data[6] == (byte)0x01) &&
                data[7] >= 64)  // that is, it's multi, not single

            || recognizeBulk(data));
        }
        
    public static boolean recognizeBulk(byte[] data)
        {
        return (
            // Block Multi Data Dump
            
            data.length == 2441 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x40 &&
            // don't care about 2, it's the channel
            data[3] == (byte)0x21 &&    // block
            data[4] == (byte)0x00 &&
            data[5] == (byte)0x03 &&
            // don't care about 6, we'll use it later
            data[7] == (byte)0x40);    // multi
        } 


    public static final int EXPECTED_SYSEX_LENGTH = 85;        
    
	}