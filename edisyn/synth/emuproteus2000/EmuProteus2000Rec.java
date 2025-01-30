/**
   Copyright 2022 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.emuproteus2000;
import edisyn.*;

public class EmuProteus2000Rec extends Recognize
    {
    public static int getNextSysexPatchGroup(byte[][] sysex, int start)
        {
        if (!EmuProteus2000Rec.recognizeHeader(sysex[start]))
        	{
            System.err.println("Didn't recognize header");
            return start;
            }
               
        for(int i = start + 1; i < sysex.length; i++)
            {
            if (EmuProteus2000Rec.recognizeHeader(sysex[i]))                // this might vary?  What a mess
                { 
                System.err.println("Next Header at " + i); 
                return i; 
                }
                        
            else if (!EmuProteus2000Rec.recognizeData(sysex[i]))    // ugh
                { 
                System.err.println("not data at " + i); 
                return start; 
                }
            }
                
        // maybe we're done?    
        return sysex.length;
        }


    public static boolean recognize(byte[] data)
        {
        System.err.println("Testing");
        // Generic Name Info
        if  (data[0] == (byte)0xF0 &&
            data[1] == 0x18 &&
            data[2] == 0x0F &&
            data[4] == 0x55 &&
            data[5] == 0x0B) return true;
        
        // Lengths vary widely unfortunately
        boolean val = (data.length > 7 &&
            data[0] == (byte)0xF0 &&
            data[1] == 0x18 &&
            data[2] == 0x0F &&
            data[4] == 0x55 &&
                ((data[5] == 0x10 && (data[6] == 0x03 || data[6] == 0x04 || data[6] == 0x01 || data[6] == 0x02)) ||       //  preset dump
                (data[5] == 0x09)));    
                                                                                                    // configuration response
		if (!val) System.err.println("Length " + data.length + " " + (data[0] == (byte)0xF0) + " " + (data[1] == 0x18) +
			" " + (data[2] == 0x0F) + " " + (data[4] == 0x55) + " " + (data[5] == 0x10) + " " + 
				(data[6] == 0x03 || data[6] == 0x04 || data[6] == 0x01 || data[6] == 0x02) + " " + (data[5] == 0x09));
        else System.err.println("Succeeded");
        return val;
        }

    public static boolean recognizeHeader(byte[] data)
        {
        // Lengths vary widely unfortunately
        return (data.length > 7 &&
            data[0] == (byte)0xF0 &&
            data[1] == 0x18 &&
            data[2] == 0x0F &&
            data[4] == 0x55 &&
            data[5] == 0x10 && 
            (data[6] == 0x03 || data[6] == 0x01));
        }

    public static boolean recognizeData(byte[] data)
        {
        // Lengths vary widely unfortunately
        return (data.length > 7 &&
            data[0] == (byte)0xF0 &&
            data[1] == 0x18 &&
            data[2] == 0x0F &&
            data[4] == 0x55 &&
            data[5] == 0x10 && 
            (data[6] == 0x04 || data[6] == 0x02));
        }

    public static boolean recognizeConfiguration(byte[] data)
        {
        // Lengths vary widely unfortunately
        return (data.length > 7 &&
            data[0] == (byte)0xF0 &&
            data[1] == 0x18 &&
            data[2] == 0x0F &&
            data[4] == 0x55 &&
            data[5] == 0x09);
        }
    }
