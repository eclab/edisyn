/**
   Copyright 2022 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.rolandu220;
import edisyn.*;
import edisyn.util.*;

public class RolandU220DrumRec extends Recognize
    {
    public static boolean recognize(byte[] data)
        {
        if (Synth.numSysexMessages(data) > 1)
            {
            byte[][] d = Synth.cutUpSysex(data);
            for(int i = 0; i < d.length; i++)
                if (!recognize(d[i])) return false;
            return true;
            }

        return ((data[0] == (byte)0xF0) &&
            (data[1] == (byte)0x41) &&
            (data[3] == (byte)0x2B) &&
            (data[4] == (byte)0x12) &&
            // Patch Temporary
                ((data[5] == (byte)0x00 && data[6] >= (byte)0x20 && data[6] <= 0x2C) ||
                // Patch Permanent
                (data[5] == (byte)0x05)) &&
            data.length <= 138 || data.length == 6272 || data.length == 1698);
        }


    public static int getNextSysexPatchGroup(byte[][] sysex, int start)
        {
        // First things first: is this the beginning of a bulk message dump?
        // We assume a bulk dump contains all 4 patches spread over 49 messages.
        if (sysex.length >= start + 49 &&
            sysex[start].length == 138 &&
            sysex[start][5] == 0x05 &&
            sysex[start][6] == 0x00 &&
            sysex[start][7] == 0x00)
            {
            for(int i = start; i < start + 49; i++)
                {
                if (!recognize(sysex[i]))
                    return start;           // something is broken
                }
            return -49;
            }
        // coulda done this with a for-loop but whatever
        else if (sysex.length - start >= 13 &&       // we're okay
            sysex[start].length == 138 &&
            sysex[start+1].length == 138 &&
            sysex[start+2].length == 138 &&
            sysex[start+3].length == 138 &&
            sysex[start+4].length == 138 &&
            sysex[start+5].length == 138 &&
            sysex[start+6].length == 138 &&
            sysex[start+7].length == 138 &&
            sysex[start+8].length == 138 &&
            sysex[start+9].length == 138 &&
            sysex[start+10].length == 138 &&
            sysex[start+11].length == 138 &&
            sysex[start+12].length == 42 &&
            recognize(sysex[start]) &&
            recognize(sysex[start + 1]) &&
            recognize(sysex[start + 2]) &&
            recognize(sysex[start + 3]) &&
            recognize(sysex[start + 4]) &&
            recognize(sysex[start + 5]) &&
            recognize(sysex[start + 6]) &&
            recognize(sysex[start + 7]) &&
            recognize(sysex[start + 8]) &&
            recognize(sysex[start + 9]) &&
            recognize(sysex[start + 10]) &&
            recognize(sysex[start + 11]) &&
            recognize(sysex[start + 12]))
            {
            return start + 13;
            }
        else return start;
        }

    public static byte[][][] breakSysexMessageIntoPatches(byte[][] messages, int start, int expectedPatches)
        {
        if (expectedPatches != 49)
            {
            System.err.println("RolandU220DrumRec.breakSysexMessageIntoPatches ERROR: expectedPatches not 49 (was " + expectedPatches + ")");
            return new byte[][][] { messages };
            }
                
        byte[] bulkData = new byte[1568 * 4];
        
        int pos = 0;
        for(int i = start; i < start + expectedPatches; i++)
            {
            System.arraycopy(messages[i], 8, bulkData, pos, 128);
            pos += 128;
            }
        
        // Now break up into 4 13-message groups per patch
        byte[][][] patches = new byte[4][13][];
        pos = 0;
        for(int i = 0; i < patches.length; i++)
            {
            for(int j = 0; j < 12; j++)
                {
                patches[i][j] = new byte[138];
                patches[i][j][0] = (byte)0xF0;
                patches[i][j][1] = (byte)0x41;
                patches[i][j][2] = (byte)0x00;          // don't care
                patches[i][j][3] = (byte)0x2B;
                patches[i][j][4] = (byte)0x12;
                patches[i][j][5] = (byte)0x05;
                patches[i][j][6] = (byte)(pos / 128);
                patches[i][j][7] = (byte)(pos % 128);
                patches[i][j][136] = (byte)0x00;                // don't care
                patches[i][j][137] = (byte)0xF7;
                System.arraycopy(bulkData, pos, patches[i][j], 8, 128);
                pos += 128;
                }
            patches[i][12] = new byte[42];
            patches[i][12][0] = (byte)0xF0;
            patches[i][12][1] = (byte)0x41;
            patches[i][12][2] = (byte)0x00;         // don't care
            patches[i][12][3] = (byte)0x2B;
            patches[i][12][4] = (byte)0x12;
            patches[i][12][5] = (byte)0x05;
            patches[i][12][6] = (byte)(pos / 128);
            patches[i][12][7] = (byte)(pos % 128);
            patches[i][12][40] = (byte)0x00;                // don't care
            patches[i][12][41] = (byte)0xF7;
            System.arraycopy(bulkData, pos, patches[i][12], 8, 32);
            pos += 32;
            }
        return patches;
        }


    }
