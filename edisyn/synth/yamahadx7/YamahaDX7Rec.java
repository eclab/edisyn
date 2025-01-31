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
            || recognizeParam(data) 
            || recognizeBank(data));
            
        }

    public static boolean recognizeParam(byte[] data)
        {
        boolean val =  (
            data.length == 7 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x43 &&
            (data[2] & (16 + 32 + 64)) == 16);
        return val;
        }
                
    public static boolean recognizeBank(byte[] data)
        {
        return  (
            // 32 Bank
            
            data.length == 4104 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x43 &&
            // don't care about 2, it's the channel
            data[3] == (byte)0x09 &&
            //data[4] == (byte)0x20 &&          // sometimes this is 0x10 by mistake
            data[5] == (byte)0x00);
        } 
        
               
    }
