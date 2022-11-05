/**
   Copyright 2022 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.rolandu220;
import edisyn.*;

public class RolandU220MultiRec extends Recognize
    {
    public static int getNextSysexPatchGroup(byte[][] sysex, int start)
        {
        // First things first: is this the beginning of a bulk message dump?
        // We assume a bulk dump contains all 64 patches spread over 80 messages.
        if (sysex.length >= start + 80 &&
            sysex[start].length == 138 &&
            sysex[start][5] == 0x03 &&
            sysex[start][6] == 0x00 &&
            sysex[start][7] == 0x00)
            {
            for(int i = start; i < start + 80; i++)
                if (!recognize(sysex[i]))
                    return start;           // something is broken
            return -80;
            }
        // look for single patches
        else if (sysex.length >= start + 2 &&        // we're okay
            sysex[start].length == 138 &&
            sysex[start + 1].length == 42 &&
            recognize(sysex[start]) &&
            recognize(sysex[start + 1]))
            {
            return start + 2;
            }
        else return start;
        }

    public static byte[][][] breakSysexMessageIntoPatches(byte[][] messages, int start, int expectedPatches)
        {
        if (expectedPatches != 80)
            {
            System.err.println("RolandU220MultiRec.breakSysexMessageIntoPatches ERROR: expectedPatches not 80 (was " + expectedPatches + ")");
            return new byte[][][] { messages };
            }
                
        byte[] bulkData = new byte[160 * 64];
        
        int pos = 0;
        for(int i = start; i < start + expectedPatches; i++)
            {
            System.arraycopy(messages[i], 8, bulkData, pos, 128);
            pos += 128;
            }
        
        // Now break up into 64 2-message groups per patch
        byte[][][] patches = new byte[64][2][];
        pos = 0;
        for(int i = 0; i < patches.length; i++)
            {
            patches[i][0] = new byte[138];
            patches[i][0][0] = (byte)0xF0;
            patches[i][0][1] = (byte)0x41;
            patches[i][0][2] = (byte)0x00;          // don't care
            patches[i][0][3] = (byte)0x2B;
            patches[i][0][4] = (byte)0x12;
            patches[i][0][5] = (byte)0x03;
            patches[i][0][6] = (byte)(pos / 128);
            patches[i][0][7] = (byte)(pos % 128);
            patches[i][0][136] = (byte)0x00;                // don't care
            patches[i][0][137] = (byte)0xF7;
            System.arraycopy(bulkData, pos, patches[i][0], 8, 128);
            pos += 128;
            patches[i][1] = new byte[42];
            patches[i][1][0] = (byte)0xF0;
            patches[i][1][1] = (byte)0x41;
            patches[i][1][2] = (byte)0x00;          // don't care
            patches[i][1][3] = (byte)0x2B;
            patches[i][1][4] = (byte)0x12;
            patches[i][1][5] = (byte)0x03;
            patches[i][1][6] = (byte)(pos / 128);
            patches[i][1][7] = (byte)(pos % 128);
            patches[i][1][40] = (byte)0x00;         // don't care
            patches[i][1][41] = (byte)0xF7;
            System.arraycopy(bulkData, pos, patches[i][1], 8, 32);
            pos += 32;
            }
        return patches;
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

        return (
            (data.length >= 138 || data.length == 42) &&
            (data[0] == (byte)0xF0) &&
            (data[1] == (byte)0x41) &&
            (data[3] == (byte)0x2B) &&
            (data[4] == (byte)0x12) &&
            // Patch Temporary
                ((data[5] == (byte)0x00 && data[6] >= (byte)0x06 && data[6] < (byte)0x08) ||
                // Patch Permanent
                (data[5] == (byte)0x03)));
        }
 
    }
