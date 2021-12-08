/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.rolandjv880;
import edisyn.*;

public class RolandJV880Rec extends Recognize
    {
    public static int getNextSysexPatchGroup(byte[][] sysex, int start)
        {
        // we presume we need COMMON and then FOUR TONES
        if (start + 5 > sysex.length) 
            {
            System.err.println("RolandJV880Rec.getNextSysexPatchGroup(): not enough sysex messages.");
            return start;
            }    

        if (recognizeCommon(sysex[start]))
            {
            if (sysex[start].length != 34 + 11)  // common
                {
                return start;
                }
            }
        else
            {
            System.err.println("RolandJV880Rec.getNextSysexPatchGroup(): could not find common.");
            return start;
            }
                
        for(int i = 0; i < 4; i++)
            {
            if (!recognizeTone(sysex[start + i + 1], i + 1))
                {
                System.err.println("RolandJV880Rec.getNextSysexPatchGroup(): could not find tone " + i + ".");
                return start;
                }
            }
        return start + 5;
        }

    public static boolean recognizeCommon(byte[] data)
        {
        return recognizeTone(data, 0);
        }


    static final int[] toneOffsets = { 0x00, 0x08, 0x09, 0x0A, 0x0B };
    public static boolean recognizeTone(byte[] data, int tone)
        {
        int data7 = 0x20 + toneOffsets[tone];
            
        return 
            (data[0] == (byte)0xF0) &&
            (data[1] == (byte)0x41) &&
            // don't care about data[2]
            (data[3] == (byte)0x46) &&
            (data[4] == (byte)0x12) &&
            
            // Performance Mode Temporary Patch
            ((data[5] == 0x00 && data[6] >= 0x00 && data[6] <= 0x06 && data[7] == data7 && data[8] == 0x00) ||

            // Patch Mode Temporary Patch
            (data[5] == 0x00 && data[6] == 0x08 && data[7] == data7 && data[8] == 0x0) ||
            
            // Internal patch            
            (data[5] == 0x01 && data[6] >= 0x40 && data[6] <= 0x7F && data[7]== data7 && data[8] == 0x00) ||
            
            // Card Patch
            (data[5] == 0x02 && data[6] >= 0x40 && data[6] <= 0x7F && data[7]== data7 && data[8] == 0x00)) &&
            
            (tone == 0 ? (data.length == 553 || data.length == 34 + 11) : data.length == 116 + 11);
        }

    public static boolean recognize(byte[] data)
        {
        return recognizeCommon(data) ||
            recognizeTone(data, 1) ||
            recognizeTone(data, 2) ||
            recognizeTone(data, 3) ||
            recognizeTone(data, 4);
        }

/*
  public static boolean recognize(byte[] data)
  {
  return ((data[0] == (byte)0xF0) &&
  (data[1] == (byte)0x41) &&
  // don't care about data[2]
  (data[3] == (byte)0x46) &&
  (data[4] == (byte)0x12) &&
                
  // Internal patch
  ((data[5] == 0x01 && data[6] >= 0x40 && 
  //(data[7] == 0x10 || data[7] == 0x20 || data[7] == 0x40) &&
  !(data[6] == 0x7F && data[7] == 0x40))  // rhythm setup
  ||
  // Card Patch
  (data[5] == 0x02 && data[6] >= 0x40 && 
  //(data[7] == 0x10 || data[7] == 0x20 || data[7] == 0x40) &&
  !(data[6] == 0x7F && data[7] == 0x40))  // rhythm setup
  ||
  // Performance Mode Temporary Patch
  (data[5] == 0x00 && data[6] < 0x07 
  // && (data[7] == 0x10 || data[7] == 0x20 || data[7] == 0x40)
  )
  ||
  // Patch Mode Temporary Patch
  (data[5] == 0x00 && data[6] == 0x08 
  //&& data[7]== 0x20
  )) &&

  (data.length == 553 || data.length == 34 + 11 || data.length == 116 + 11));
  }
*/
        
        
    }
