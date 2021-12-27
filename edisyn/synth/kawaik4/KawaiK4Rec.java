/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.kawaik4;
import edisyn.*;

public class KawaiK4Rec extends Recognize
    {
    public static boolean recognize(byte[] data)
        {
        return (((data.length == EXPECTED_SYSEX_LENGTH) &&
                (data[0] == (byte)0xF0) &&
                (data[1] == (byte)0x40) &&
                (data[3] == (byte)0x20) &&
                (data[4] == (byte)0x00) &&
                (data[5] == (byte)0x04) &&
                (data[6] == (byte)0x00 || data[6] == (byte)0x02) &&
                (data[7] < (byte)64))  // that is, it's single, not multi
            
            || recognizeBank(data));
        }
        
    public static boolean recognizeBank(byte[] data)
        {
        return  ((
                // Block Single Data Dump (5-9)
            
                data.length == 8393 &&
                data[0] == (byte)0xF0 &&
                data[1] == (byte)0x40 &&
                // don't care about 2, it's the channel
                data[3] == (byte)0x21 &&    // block
                data[4] == (byte)0x00 &&
                data[5] == (byte)0x04 &&
            	((data[6] == (byte)0x00) || (data[6] == (byte)0x02)) &&		// Int/Ext for singles and multis, second bit used to distinguish from effects
                data[7] == (byte)0x00)			 // Single
            
            ||
            
                (
                // All Patch Data Dump (5-11)
            
                data.length == 15123 &&
                data[0] == (byte)0xF0 &&
                data[1] == (byte)0x40 &&
                // don't care about 2, it's the channel
                data[3] == (byte)0x22 &&    // All Patch
                data[4] == (byte)0x00 &&
                data[5] == (byte)0x04 &&
                // don't care about 6, we'll use it later
                data[7] == (byte)0x00));
        } 

    public static final int EXPECTED_SYSEX_LENGTH = 140;        


    /**
       public byte[][] convertBulkBank(byte[] data)
       {
       if (data[0] == (byte)0xF0 &&
       data[1] == (byte)0x40 &&        // Kawai
       data[3] == (byte)0x22 &&        // All Block Data Dump
       data[4] == (byte)0x00 &&        // Synthesizers
       data[5] == (byte)0x04 &&        // Kawai K4
       (data[6] == (byte)0x00 ||       // Internal
       data[6] == (byte)0x20) &&       // [or] External
       data[7] == (byte)0x00)
       {
       byte[][] syx = new byte[4][];
       int pos = 8;
                        
       // Load Singles
       int size = 131 * 64;
       syx[0] = new byte[8 + size];
       syx[0][0] = (byte)0xF0;
       syx[0][1] = (byte)0x40;
       syx[0][2] = (byte)data[2];
       syx[0][3] = (byte)0x21;
       syx[0][4] = (byte)0x00;
       syx[0][5] = (byte)0x04;
       syx[0][6] = (byte)data[6];
       syx[0][7] = (byte)0x00;                 // Single
       syx[0][8 + size - 1] = (byte)0xF7;
       System.arraycopy(data[pos], 0, syx[0][8], 0, size);
       pos += size;
                        
       // Load Multis
       size = 77 * 64;
       syx[1] = new byte[8 + size];
       syx[1][0] = (byte)0xF0;
       syx[1][1] = (byte)0x40;
       syx[1][2] = (byte)data[2];
       syx[1][3] = (byte)0x21;
       syx[1][4] = (byte)0x00;
       syx[1][5] = (byte)0x04;
       syx[1][6] = (byte)data[6];
       syx[1][7] = (byte)0x40;                 // Multi
       syx[1][8 + size - 1] = (byte)0xF7;
       System.arraycopy(data[pos], 0, syx[1][8], 0, size);
       pos += size;

       // Load Drum
       size = 682;
       syx[2] = new byte[8 + size];
       syx[2][0] = (byte)0xF0;
       syx[2][1] = (byte)0x40;
       syx[2][2] = (byte)data[2];
       syx[2][3] = (byte)0x20;                         // one patch data dump
       syx[2][4] = (byte)0x00;
       syx[2][5] = (byte)0x04;
       syx[2][6] = (byte)data[6];              // Manual says 0x03 is "external", likely error
       syx[2][7] = (byte)32;                   // Drum (decimal)
       syx[2][8 + size - 1] = (byte)0xF7;
       System.arraycopy(data[pos], 0, syx[2][8], 0, size);
       pos += size;

       // Load Effects
       size = 35 * 16;
       syx[3] = new byte[8 + size];
       syx[3][0] = (byte)0xF0;
       syx[3][1] = (byte)0x40;
       syx[3][2] = (byte)data[2];
       syx[3][3] = (byte)0x20;                         // one patch data dump
       syx[3][4] = (byte)0x00;
       syx[3][5] = (byte)0x04;
       syx[3][6] = (byte)data[6];              // Manual says 0x03 is "external", likely error
       syx[3][7] = (byte)32;                   // Drum (decimal)
       syx[3][8 + size - 1] = (byte)0xF7;
       System.arraycopy(data[pos], 0, syx[3][8], 0, size);

       return syx;
       }
       else return null;
       }
    */
    }
