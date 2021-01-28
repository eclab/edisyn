/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.yamahatg33;
import edisyn.*;

public class YamahaTG33Rec extends Recognize
    {
    public static boolean recognizeSY(byte[] data)
        {
        return  ((
                data.length == 592 && 
                data[0] == (byte)0xF0 &&
                data[1] == (byte)0x43 &&
                // don't care about 2, it's the id
                data[3] == (byte)0x7E &&
                // don't care about 4, it's the MSB of the data length
                // dont' care about 5, it's the LSB of the data length
                data[6] == (byte)'P' &&
                data[7] == (byte)'K' &&
                data[8] == (byte)' ' &&
                data[9] == (byte)' ' &&
                data[10] == (byte)'2' &&
                data[11] == (byte)'2' &&
                data[12] == (byte)'0' &&
                data[13] == (byte)'3' &&
                data[14] == (byte)'A' &&
                data[15] == (byte)'E')
            );
        }

    public static boolean recognizeTG(byte[] data)
        {
        return  ((
                data.length == 15 + (587 + 3)  && 
                data[0] == (byte)0xF0 &&
                data[1] == (byte)0x43 &&
                // don't care about 2, it's the id
                data[3] == (byte)0x7E &&
                // don't care about 4, it's the MSB of the data length
                // dont' care about 5, it's the LSB of the data length
                data[6] == (byte)'L' &&
                data[7] == (byte)'M' &&
                data[8] == (byte)' ' &&
                data[9] == (byte)' ' &&
                data[10] == (byte)'0' &&
                data[11] == (byte)'0' &&
                data[12] == (byte)'1' &&
                data[13] == (byte)'2' &&
                data[14] == (byte)'V' &&
                data[15] == (byte)'E')
            );
        }


    public static boolean recognizeSYBank(byte[] data)
        {
        return  ((
                data.length == 38306 &&                         // includes multi :-(
                data[0] == (byte)0xF0 &&
                data[1] == (byte)0x43 &&
                // don't care about 2, it's the id
                data[3] == (byte)0x7E &&
                // don't care about 4, it's the MSB of the data length
                // dont' care about 5, it's the LSB of the data length
                data[6] == (byte)'P' &&
                data[7] == (byte)'K' &&
                data[8] == (byte)' ' &&
                data[9] == (byte)' ' &&
                data[10] == (byte)'2' &&
                data[11] == (byte)'2' &&
                data[12] == (byte)'0' &&
                data[13] == (byte)'3' &&
                data[14] == (byte)'V' &&
                data[15] == (byte)'M')
            );
        }

    public static boolean recognizeTGBank(byte[] data)
        {
        return  ((
                data.length == 37631  && 
                data[0] == (byte)0xF0 &&
                data[1] == (byte)0x43 &&
                // don't care about 2, it's the id
                data[3] == (byte)0x7E &&
                // don't care about 4, it's the MSB of the data length
                // dont' care about 5, it's the LSB of the data length
                data[6] == (byte)'L' &&
                data[7] == (byte)'M' &&
                data[8] == (byte)' ' &&
                data[9] == (byte)' ' &&
                data[10] == (byte)'0' &&
                data[11] == (byte)'0' &&
                data[12] == (byte)'1' &&
                data[13] == (byte)'2' &&
                data[14] == (byte)'V' &&
                data[15] == (byte)'C')
            );
        }
    
    public static boolean recognize(byte[] data)
        {
        return (recognizeTG(data) || recognizeSY(data) || recognizeBank(data));
        }


    public static boolean recognizeBank(byte[] data)
        {
        return  (recognizeTGBank(data) || recognizeSYBank(data));
        } 
               
    }
