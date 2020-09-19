/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.korgwavestation;
import edisyn.*;

public class KorgWavestationPatchRec extends Recognize
    {
    public static final int EXPECTED_SYSEX_LENGTH = 861;
    public static boolean recognize(byte[] data)
        {
        return ((data.length == EXPECTED_SYSEX_LENGTH &&
                data[0] == (byte)0xF0 &&
                data[1] == (byte)0x42 &&
                data[3] == (byte)0x28 &&
                data[4] == (byte)0x40)
            
            || recognizeBulk(data));                 
        }

    public static boolean recognizeBulk(byte[] data)
        {
        return (data.length == 29828 && // I think it's 29828?  A patch is 852...?
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x42 &&
            data[3] == (byte)0x28 &&
            data[4] == (byte)0x4C);                 
        }
    
    }
