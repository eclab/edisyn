/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.yamaha4op;
import edisyn.*;

public class Yamaha4OpRec extends Recognize
    {
    public static int getNextSysexPatchGroup(byte[][] sysex, int start)
        {
        if (recognizeBlockHeader(sysex[start]))
			{
			// Do we have all four blocks?  If so, it's an entire "bank"
			if (start == 0 && sysex.length == 8)
				return 8;
			
			// Otherwise we'll grab them 2 at a time
			if (start + 1 >= sysex.length)	// failed
				return start;
			else return getNextSysexPatchGroup(sysex, start + 1);
			}
        
        if (recognizeVMEM(sysex[start]))
            return start + 1;
                
        for(int i = start; i < sysex.length; i++)
            {
            int rec = recognizeBasic(sysex[i]);
            // We're looking for VCED               
            if (rec == RECOGNIZE_BASIC_VCED)        
                {
                return i + 1;  // all done
                }
            else if (rec == RECOGNIZE_BASIC_NONE) // uh oh
                {
                return start;
                }
            }
        // if we're here we never found VCED
        return start;
        }


     public static boolean recognizeBlockHeader(byte[] data)
        {
        boolean b = (data.length == 7 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x43 &&
            // don't care about 2, it's the channel
            data[3] == (byte)0x24 &&
            data[4] == (byte)0x07);
        return b;
        }


   public static boolean recognizeVMEM(byte[] data)
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

     public static boolean recognizeBank(byte[] data)
        {
        return recognizeBlockHeader(data) || recognizeVMEM(data);
        }

    // returns a guess as to the number of sysex commands this file is trying to load per patch.
    // Or if we don't recognize it, then 0

    public static final int RECOGNIZE_BASIC_ACED3 = 5;
    public static final int RECOGNIZE_BASIC_EFEDS = 4;
    public static final int RECOGNIZE_BASIC_ACED2 = 3;
    public static final int RECOGNIZE_BASIC_ACED = 2;
    public static final int RECOGNIZE_BASIC_VCED = 1;
    public static final int RECOGNIZE_BASIC_NONE = 0;
        
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
            return RECOGNIZE_BASIC_ACED3;                           // VCED + ACED + ACED2 + ACED3

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
            return RECOGNIZE_BASIC_EFEDS;                                   // VCED + ACED + ACED2 + EFEDS

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
            return RECOGNIZE_BASIC_ACED2;                                   // VCED + ACED + ACED2

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
            return RECOGNIZE_BASIC_ACED;                                   // VCED + ACED
        
        // VCED
        if (data.length == 101 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x43 &&
            // don't care about 2, it's the channel
            data[3] == (byte)0x03 &&
            data[4] == (byte)0x00 &&
            data[5] == (byte)0x5D)
            return RECOGNIZE_BASIC_VCED;                                   // VCED alone

        else return RECOGNIZE_BASIC_NONE;
        }
        
    public static boolean recognize(byte[] data)
        {
        return (recognizeBasic(data) != RECOGNIZE_BASIC_NONE) || recognizeBank(data);
        }
    }
