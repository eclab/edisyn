/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.oberheimmatrix1000;
import edisyn.*;

public class OberheimMatrix1000Rec extends Recognize
{
    public static final int EXPECTED_SYSEX_LENGTH = 275;        
        
    public static boolean recognize(byte[] data)
    {
        boolean v = (
                     // The Matrix 1000 doesn't transmit the checksum!
                     // So it could be one of two lengths:
                     (data.length == EXPECTED_SYSEX_LENGTH ||
                      data.length == EXPECTED_SYSEX_LENGTH - 1) &&
                     data[0] == (byte)0xF0 &&
                     data[1] == (byte)0x10 &&
                     data[2] == (byte)0x06 &&
                     (data[3] == (byte)0x01 || data[3] == (byte)0x0d));
        return v;
    }
}
