/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.redsounddarkstar;
import edisyn.*;

public class RedSoundDarkStarRec extends Recognize
{
    public static boolean recognize(byte[] data)
    {
        // DarkStar data comes in the following forms
        // VOICEDATA is 100 bytes
        // Others are 1 byte each
        
        // DarkStar or XP2 Single Voice: 108 bytes
        // F0 00 20 3B 02 01 03 VOICEDATA f7
        
        // DarkStar Single Performance: 512 bytes
        // F0 00 20 3B 02 01 01 VOICEDATA(x5) 00 EDITPART 00 00 f7
        
        // XP2 Single Performance: 516 bytes
        // F0 00 20 3B 02 01 01 VOICEDATA(x5) FX1 FX2 FX3 FX4 FX5 CHORUS DEPTH EDITPART f7
        
        // DarkStar Bulk Dump Single Performance: 514 bytes
        // F0 00 20 3B 02 01 02 PERFNUM(x2) VOICEDATA(x5) 00 EDITPART 00 00 f7
        
        // XP2 Bulk Dump Single Performance: 518 bytes
        // F0 00 20 3B 02 01 02 PERFNUM(x2) VOICEDATA(x5) FX1 FX2 FX3 FX4 FX5 CHORUS DEPTH EDITPART f7

        // We're gonna try to recognize all of them
        
        // NOTE: this data is wrong -- it appears that the XP2 Single Performance is *520* bytes,
        // and the XP2 Bulk Dump Single Performance is *522* bytes
        
                
        return (
                (data.length == 108 ||
                 data.length == 512 ||
                 data.length == 516 ||
                 data.length == 514 ||
                 data.length == 520 ||
                 data.length == 522) &&
                data[0] == (byte)0xF0 &&
                data[1] == (byte)0x00 &&
                data[2] == (byte)0x20 &&
                data[3] == (byte)0x3B &&
                data[4] == (byte)0x02 &&
                data[5] == (byte)0x01);        
    }
}
