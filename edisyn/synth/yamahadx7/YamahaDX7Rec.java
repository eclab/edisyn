/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.yamahadx7;
import edisyn.*;

public class YamahaDX7Rec extends Recognize
    {
    public static boolean recognize(byte[] data)
        {
        return (
            // 1 single
                
                (data.length == 163 &&
                data[0] == (byte)0xF0 &&
                data[1] == (byte)0x43 &&
                // don't care about 2, it's the channel
                data[3] == (byte)0x00 &&
                data[4] == (byte)0x01 &&
                data[5] == (byte)0x1B) 
                
            || recognizeBulk(data));
            
        }

    public static boolean recognizeBulk(byte[] data)
        {
        return  (
            // 32 bulk
            
            data.length == 4104 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x43 &&
            // don't care about 2, it's the channel
            data[3] == (byte)0x09 &&
            //data[4] == (byte)0x20 &&          // sometimes this is 0x10 by mistake
            data[5] == (byte)0x00);
        } 
        
               
    }
