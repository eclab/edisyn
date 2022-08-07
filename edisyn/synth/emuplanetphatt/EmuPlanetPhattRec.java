/**
   Copyright 2022 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.emuplanetphatt;
import edisyn.*;

public class EmuPlanetPhattRec extends Recognize
    {
    public static boolean recognize(byte[] data)
        {
        return  data.length == 281 &&
            data[0] == (byte) 0xF0 &&
            data[1] == (byte) 0x18 &&
            data[2] == (byte) 0x0A &&
            data[4] == (byte) 0x01;
        }
    }
