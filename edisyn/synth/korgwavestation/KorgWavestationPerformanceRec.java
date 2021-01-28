/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.korgwavestation;
import edisyn.*;

public class KorgWavestationPerformanceRec extends Recognize
    {
    public static final int EXPECTED_SYSEX_LENGTH = 371;
    public static boolean recognize(byte[] data)
        {
        return ((data.length == EXPECTED_SYSEX_LENGTH &&
                data[0] == (byte)0xF0 &&
                data[1] == (byte)0x42 &&
                data[3] == (byte)0x28 &&
                data[4] == (byte)0x49)
            
            || recognizeBank(data));              
        }
    
    public static boolean recognizeBank(byte[] data)
        {
        return ((data.length == 18108 &&
                data[0] == (byte)0xF0 &&
                data[1] == (byte)0x42 &&
                data[3] == (byte)0x28 &&
                data[4] == (byte)0x4D));
            
        }    
    
    }
