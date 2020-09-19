/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.yamaha4op;
import edisyn.*;

public class Yamaha4OpRec extends Recognize
    {
    public static int getNumSysexDumpsPerPatch(byte[] data) 
        {
        int i = recognizeBasic(data);           // we'll try here to guess how many patches are in a chunk in this file
        if (i > 0) return i;
        else return 1;
        }

    /*
      public static String toHex(int val)
      {
      return String.format("0x%08X", val);
      }
    */

    public static boolean recognizeBulk(byte[] data)
        {
        // VMEM
        boolean b = (data.length == 4104 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x43 &&
            // don't care about 2, it's the channel
            data[3] == (byte)0x04 &&
            data[4] == (byte)0x20 &&        // manual says 10 but this is wrong
            data[5] == (byte)0x00);
        return b;
        }

// returns a guess as to the number of sysex commands this file is trying to load per patch.
// Or if we don't recognize it, then 0
    static int recognizeBasic(byte[] data)
        {
        // ACED3
        if (data.length >= 38 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x43 &&
            // don't care about 2, it's the channel
            data[3] == 0x7E &&
            data[4] == 0x00 &&
            data[5] == 0x1E &&
            // next it spits out the header "LM  8073AE"
            data[6] == 'L' &&
            data[7] == 'M' &&
            data[8] == ' ' &&
            data[9] == ' ' &&
            data[10] == '8' &&
            data[11] == '0' &&
            data[12] == '7' &&
            data[13] == '3' &&
            data[14] == 'A' &&
            data[15] == 'E')
            return 4;                           // VCED + ACED + ACED2 + ACED3

        // EFEDS
        if (data.length >= 21 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x43 &&
            // don't care about 2, it's the channel
            data[3] == 0x7E &&
            data[4] == 0x00 &&
            data[5] == 0x0D &&
            // next it spits out the header "LM  8036EF"
            data[6] == 'L' &&
            data[7] == 'M' &&
            data[8] == ' ' &&
            data[9] == ' ' &&
            data[10] == '8' &&
            data[11] == '0' &&
            data[12] == '3' &&
            data[13] == '6' &&
            data[14] == 'E' &&
            data[15] == 'F')
            return 4;                                   // VCED + ACED + ACED2 + EFEDS

        // ACED2
        if (data.length >= 28 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x43 &&
            // don't care about 2, it's the channel
            data[3] == 0x7E &&
            data[4] == 0x00 &&
            data[5] == 0x14 &&
            // next it spits out the header "LM  8023AE"
            data[6] == 'L' &&
            data[7] == 'M' &&
            data[8] == ' ' &&
            data[9] == ' ' &&
            data[10] == '8' &&
            data[11] == '0' &&
            data[12] == '2' &&
            data[13] == '3' &&
            data[14] == 'A' &&
            data[15] == 'E')
            return 3;                                   // VCED + ACED + ACED2

        // ACED
        if (data.length >= 41 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x43 &&
            // don't care about 2, it's the channel
            data[3] == 0x7E &&
            data[4] == 0x00 &&
            data[5] == 0x21 &&
            // next it spits out the header "LM  8976AE"
            data[6] == 'L' &&
            data[7] == 'M' &&
            data[8] == ' ' &&
            data[9] == ' ' &&
            data[10] == '8' &&
            data[11] == '9' &&
            data[12] == '7' &&
            data[13] == '6' &&
            data[14] == 'A' &&
            data[15] == 'E')
            return 2;                                   // VCED + ACED
        
        // VCED
        if (data.length == 101 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x43 &&
            // don't care about 2, it's the channel
            data[3] == (byte)0x03 &&
            data[4] == (byte)0x00 &&
            data[5] == (byte)0x5D)
            return 1;                                   // VCED alone

        else return 0;
        }
        
    public static boolean recognize(byte[] data)
        {
        return (recognizeBasic(data) > 0) || recognizeBulk(data);
        }
    }
