/***
    Copyright 2026 by Raphaël Jungers
    Licensed under the Apache License version 2.0
*/

/**
   Recognizer for the Sequential Fourm synthesizer.
*/

package edisyn.synth.sequentialfourm;

import edisyn.*;

public class SequentialFourmRec extends Recognize
    {
    static final byte FOURM_ID = 0x3B;

    // Program Data Dump:    F0 01 3B 02 <bank> <prog> <4690 packed bytes> F7 = 4697 bytes
    // Edit Buffer Data Dump: F0 01 3B 03 <4690 packed bytes> F7 = 4695 bytes
    static final int PROGRAM_DUMP_LENGTH = 4697;
    static final int EDIT_BUFFER_LENGTH  = 4695;

    public static boolean recognize(byte[] data)
        {
        if (data.length < 4680) return false;
        if (data[0] != (byte)0xF0) return false;
        if (data[1] != (byte)0x01) return false;
        if (data[2] != FOURM_ID)   return false;
        return data[3] == (byte)0x02 || data[3] == (byte)0x03;
        }

    public static boolean recognizeBank(byte[] data)
        {
        return false;
        }

    public static String getBankName(byte[] data)
        {
        return "";
        }
    }
