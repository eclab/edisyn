/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.korgmicrokorg;
import edisyn.*;

public class KorgMicroKorgRec extends Recognize
    {
    // converts all but last byte (F7)
    public static byte[] convertTo8Bit(byte[] data, int offset)
        {
        // How big?
        int size = (data.length - offset - 1) / 8 * 7;
        if ((data.length - offset - 1) % 8 > 0)
            size += ((data.length - offset - 1) % 8 - 1);           
        byte[] newd = new byte[size];
        
        int j = 0;
        for(int i = offset; i < data.length; i += 8)
            {
            for(int x = 0; x < 7; x++)
                {
                if (j + x < newd.length)
                    newd[j + x] = (byte)(data[i + x + 1] | (byte)(((data[i] >>> x) & 0x1) << 7));
                }
            j += 7;
            }
        return newd;
        }
        
    public static final int EXPECTED_SYSEX_LENGTH = 297;
    public static final int EXPECTED_BANK_SYSEX_LENGTH = 37163; // 128 patches * 254 bytes * 8/7 + 6
    
    public static boolean recognize(byte[] data)
        {
        // If this is a large message, it's a bank dump - return true so parse() handles it
        if (data.length >= 37150 && data.length <= 37400) {
            boolean bank = recognizeBank(data);
            return true;  // Return true so parse() method handles bank dumps
        }
        boolean v = (
            data.length == EXPECTED_SYSEX_LENGTH &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x42 &&
            data[3] == (byte)0x58 &&
            data[4] == (byte)0x40);
        if (v == false) return false;
        data = convertTo8Bit(data, 5);
        int voicemode = (data[16] >>> 4) & 3;
        boolean modeOK = (voicemode == 0 || voicemode == 2);
        return modeOK;
        }
        
    public static boolean recognizeBank(byte[] data)
        {
        // Accept a small range for bank dump length
        boolean lengthOK = (data.length >= 37150 && data.length <= 37400);
        boolean headerOK = (
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x42 &&
            data[3] == (byte)0x58 &&
            data[4] == (byte)0x50);
        boolean v = lengthOK && headerOK;
        return v;
        }
    }
