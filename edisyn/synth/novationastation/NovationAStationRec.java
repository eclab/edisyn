/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.novationastation;

import edisyn.Recognize;

public class NovationAStationRec extends Recognize
    {
    public static boolean recognize(byte[] data)
    {
        try {
            SysexMessage.parse(data);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
        
