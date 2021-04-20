/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.waldorfkyra;
import edisyn.*;

public class WaldorfKyraMultiRec extends Recognize
    {
    public static int getNextSysexPatchGroup(byte[][] sysex, int start)
        {
        if (start >= sysex.length) return start;
                
        // we assume patch is before name
        if (subRecognize(sysex[start]) == MULTI_PATCH)
            {
            if (start + 1 >= sysex.length)
                {
                System.err.println("WaldorfKyraMutiRec.getNextSysexPatchGroup(): found patch but no name before messages terminated.");
                return start;
                }
            else if (subRecognize(sysex[start + 1]) == MULTI_NAME)
                {
                return start + 2;
                }
            else
                {
                System.err.println("WaldorfKyraMutiRec.getNextSysexPatchGroup(): found patch but no name.");
                return start;
                }
            }
        else if (subRecognize(sysex[start]) == MULTI_NAME)
            {
            System.err.println("WaldorfKyraMutiRec.getNextSysexPatchGroup(): found name without a patch.");
            return start;
            }
        else return start;
        }

    public static boolean recognize(byte[] data)
        {
        byte[][] cutup = Synth.cutUpSysex(data);
        for(int i = 0; i < cutup.length; i++)
            {
            if (subRecognize(cutup[i]) == NONE) return false;
            }
        return true;
        }
        
    public static int MULTI_PATCH = 0;
    public static int MULTI_NAME = 1;
    public static int NONE = 2;

    public static int subRecognize(byte[] data)
        {
        //  multi patch
        if ((data.length == 128 + 10 &&
                data[0] == (byte)0xF0 &&
                data[1] == 0x3E &&
                data[2] == 0x22 &&
                (data[4] == 0x01 || data[4] == 0x41)))             // "Multi (whole 8 parts)" -- it's not clear why these are different
            return MULTI_PATCH;
                        
        //      multi name, which for some reason is not part of the patch
        else if ((data.length == 16 + 10 &&
                data[0] == (byte)0xF0 &&
                data[1] == 0x3E &&
                data[2] == 0x22 &&
                (data[4] == 0x19 || data[4] == 0x59)))            // "Multi name"
            return MULTI_NAME;
        else return NONE;
        }
    }
