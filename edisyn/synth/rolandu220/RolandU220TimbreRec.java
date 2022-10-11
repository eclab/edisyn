/**
   Copyright 2022 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.rolandu220;
import edisyn.*;
import edisyn.util.*;

public class RolandU220TimbreRec extends Recognize
    {
    // Irritatingly, in bulk dumps Timbres are organized TWO TO A SYSEX MESSAGE.  
    // So we need to return -1 to trigger a call to breakSysexMessageIntoPatches
    public static int getNextSysexPatchGroup(byte[][] sysex, int start)
        {
        if (sysex[start].length == 138) return -1;
        else if (sysex[start].length == 74) return start + 1;
        else return start;
        }

    public static byte[][][] breakSysexMessageIntoPatches(byte[][] messages, int start, int expectedPatches)
        {
        byte[] message = messages[start];
        if (expectedPatches != 1) 
            System.err.println("RolandU220TimbreRec.breakSysexMessageIntoPatches ERROR: expectedPatches not 1 (was " + expectedPatches + ")");
        byte[][][] groups = new byte[2][1][74];
        groups[0][0][0] = message[0];
        groups[0][0][1] = message[1];
        groups[0][0][2] = message[2];
        groups[0][0][3] = message[3];
        groups[0][0][4] = message[4];
        groups[0][0][5] = message[5];
        groups[0][0][6] = message[6];
        groups[0][0][7] = message[7];
        groups[0][0][groups[0][0].length - 1] = (byte)0xF7;   // we don't care about checksum
        System.arraycopy(message, 8, groups[0][0], 8, 64);
        groups[1][0][0] = message[0];
        groups[1][0][1] = message[1];
        groups[1][0][2] = message[2];
        groups[1][0][3] = message[3];
        groups[1][0][4] = message[4];
        groups[1][0][5] = message[5];
        groups[1][0][6] = message[6];
        groups[1][0][7] = message[7 + 64];
        groups[1][0][groups[1][0].length - 1] = (byte)0xF7;   // we don't care about checksum
        System.arraycopy(message, 8 + 64, groups[1][0], 8, 64);
        return groups;
        }

    public static boolean recognize(byte[] data)
        {
        if (Synth.numSysexMessages(data) > 1)
        	{
        	byte[][] d = Synth.cutUpSysex(data);
        	for(int i = 0; i < d.length; i++)
        		if (!recognize(d[i])) return false;
        	return true;
        	}

        boolean b = (
            (data.length == 138 || data.length == 74 || data.length == 138 + 74) && // && data.length >= 74 &&
            (data[0] == (byte)0xF0) &&
            (data[1] == (byte)0x41) &&
            (data[3] == (byte)0x2B) &&
            (data[4] == (byte)0x12) &&
            // Timbre Temporary
                ((data[5] == (byte)0x00) && 
                    (data[6] == (byte)0x10 || data[6] == (byte)0x11 || data[6] == (byte)0x12 ||
                    data[6] == (byte)0x13 || data[6] == (byte)0x14 || data[6] == (byte)0x15) ||
                // Timbre Permanent
                (data[5] == (byte)0x02)));
        return b;
        }
    }
