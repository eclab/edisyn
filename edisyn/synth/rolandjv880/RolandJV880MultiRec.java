/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.rolandjv880;
import edisyn.*;

public class RolandJV880MultiRec extends Recognize
    {
    public static int getNextSysexPatchGroup(byte[][] sysex, int start)
        {
        // we presume we need COMMON and then EIGHT PARTS
        if (start + 9 > sysex.length) 
            {
            System.err.println("RolandJV880Rec.getNextSysexPatchGroup(): not enough sysex messages.");
            return start;
            }    

        if (recognizeCommon(sysex[start]))
            {
            if (sysex[start].length != 31 + 11)  // common
                {
                return start;
                }
            }
        else
            {
            System.err.println("RolandJV880MultiRec.getNextSysexPatchGroup(): could not find common.");
            return start;
            }
                
        for(int i = 0; i < 4; i++)
            {
            if (!recognizePart(sysex[start + i + 1], i + 1))
                {
                System.err.println("RolandJV880MultiRec.getNextSysexPatchGroup(): could not find part " + i + ".");
                return start;
                }
            }
        return start + 9;
        }

    public static boolean recognizeCommon(byte[] data)
        {
        return recognizePart(data, 0);
        }
    
    static final int[] partOffsets = { 0x00, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F };
    public static boolean recognizePart(byte[] data, int part)
        {
        int data7 = 0x10 + partOffsets[part];
            
        return 
            (data[0] == (byte)0xF0) &&
            (data[1] == (byte)0x41) &&
            // don't care about data[2]
            (data[3] == (byte)0x46) &&
            (data[4] == (byte)0x12) &&
            
            // Temporary Performance
            ((data[5] == 0x00 && data[6] == 0x00 && data[7] == data7 && data[8] == 0x00) ||

            // Internal Performance            
            (data[5] == 0x01 && data[6] >= 0x00 && data[6] <= 0x0F && data[7] == data7 && data[8] == 0x00) ||
            
            // Card Performance
            (data[5] == 0x02 && data[6] >= 0x00 && data[6] <= 0x0F && data[7] == data7 && data[8] == 0x00)) &&
            
            (part == 0 ? (data.length == 410 || data.length == 31 + 11) : data.length == 35 + 11);
        }

    public static boolean recognize(byte[] data)
        {
        return recognizeCommon(data) ||
            recognizePart(data, 1) ||
            recognizePart(data, 2) ||
            recognizePart(data, 3) ||
            recognizePart(data, 4) ||
            recognizePart(data, 5) ||
            recognizePart(data, 6) ||
            recognizePart(data, 7) ||
            recognizePart(data, 8);
        }

/*
  public static boolean recognize(byte[] data)
  {
  return ((data[0] == (byte)0xF0) &&
  (data[1] == (byte)0x41) &&
  (data[3] == (byte)0x46) &&
  (data[4] == (byte)0x12) &&
                
  // Internal performance
  ((data[5] == 0x01 && (data[7] == 0x10 || data[7] == 0x18 || data[7] == 0x19 || data[7] == 0x1A || data[7] == 0x1B || data[7] == 0x1C || data[7] == 0x1D || data[7] == 0x1E || data[7] == 0x1F)) || 
  // Card performance
  (data[5] == 0x02 && (data[7] == 0x10 || data[7] == 0x18 || data[7] == 0x19 || data[7] == 0x1A || data[7] == 0x1B || data[7] == 0x1C || data[7] == 0x1D || data[7] == 0x1E || data[7] == 0x1F)) ||
  // Temporary Performance
  (data[5] == 0x00 && data[6] == 0x00 && (data[7] == 0x10 || data[7] == 0x18 || data[7] == 0x19 || data[7] == 0x1A || data[7] == 0x1B || data[7] == 0x1C || data[7] == 0x1D || data[7] == 0x1E || data[7] == 0x1F))) &&
                 
  (data.length == 410 || data.length == 31 + 11 || data.length == 35 + 11));
  }
*/
    }
