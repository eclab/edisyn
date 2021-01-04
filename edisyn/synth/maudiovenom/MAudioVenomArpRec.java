/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.maudiovenom;
import edisyn.*;

public class MAudioVenomArpRec extends Recognize
    {
    public static int getNextSysexPatchGroup(byte[][] sysex, int start)
    	{
		if (start >= sysex.length) return start;
		
    	// we assume headers are first
    	if (messageType(sysex[start]) == MESSAGE_HEADER)
    		{
    		if (start + 1 >= sysex.length)
    			{
           	 	System.err.println("MAudioVenomArpRec.getNextSysexPatchGroup(): found header but no pattern before messages terminated.");
    			return start;
    			}
    		else if (messageType(sysex[start + 1]) == MESSAGE_PATTERN)
    			{
    			return start + 2;
    			}
    		else
    			{
           	 	System.err.println("MAudioVenomArpRec.getNextSysexPatchGroup(): found header but no pattern.");
    			return start;
    			}
    		}
    	else if (messageType(sysex[start]) == MESSAGE_PATTERN)
    		{
			System.err.println("MAudioVenomArpRec.getNextSysexPatchGroup(): found pattern without a header.");
			return start;
    		}
    	else return start;
		}
		
	public static int MESSAGE_HEADER = 0;
	public static int MESSAGE_PATTERN = 1;
	public static int MESSAGE_NONE = 2;
    public static int messageType(byte[] data)
        {
        // There are multiple possible edit dumps based on the part in a multimode patch.
        // I presume they're all the same format but am not certain.
         
            // HEADERS
                        
            if ((data.length >= 35 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte) 0x00 &&       // M-Audio
            data[2] == (byte) 0x01 &&
            data[3] == (byte) 0x05 &&
            data[4] == (byte) 0x21 &&       // Venom
            // don't care about 5
            data[6] == (byte) 0x02 &&               // Write Data Dump
            data[7] == (byte) 0x03) ||              // Arp Data [Header?] Edit Dump

            (data.length == 35 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte) 0x00 &&       // M-Audio
            data[2] == (byte) 0x01 &&
            data[3] == (byte) 0x05 &&
            data[4] == (byte) 0x21 &&       // Venom
            // don't care about 5
            data[6] == (byte) 0x02 &&               // Write Data Dump
            data[7] == (byte) 0x00 &&               // Edit Buffer Dump
                (data[8] == (byte) 0x08 ||              // Arpeggio Header Single Edit Dump
                data[8] == (byte) 0x09 ||               // Arpeggio Header Part 1 Edit Dump
                data[8] == (byte) 0x0A ||               // Arpeggio Header Part 2 Edit Dump
                data[8] == (byte) 0x0B ||               // Arpeggio Header Part 3 Edit Dump
                data[8] == (byte) 0x0C)))           // Arpeggio Header Part 4 Edit Dump
                return MESSAGE_HEADER;
                                 
                                 
            // PATTERNS
                                
        else if ((data.length > 12 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte) 0x00 &&       // M-Audio
            data[2] == (byte) 0x01 &&
            data[3] == (byte) 0x05 &&
            data[4] == (byte) 0x21 &&       // Venom
            // don't care about 5
            data[6] == (byte) 0x02 &&               // Write Data Dump
            data[7] == (byte) 0x04) ||              // Arp Pattern Dump

            (data.length > 12 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte) 0x00 &&       // M-Audio
            data[2] == (byte) 0x01 &&
            data[3] == (byte) 0x05 &&
            data[4] == (byte) 0x21 &&       // Venom
            // don't care about 5
            data[6] == (byte) 0x02 &&               // Write Data Dump
            data[7] == (byte) 0x00 &&               // Edit Buffer Dump
                (data[8] == (byte) 0x0D ||              // Arpeggio Pattern Single Edit Dump
                data[8] == (byte) 0x0E ||              // Arpeggio Pattern Part 1 Edit Dump
                data[8] == (byte) 0x0F ||      // Arpeggio Pattern Part 2 Edit Dump
                data[8] == (byte) 0x10 ||      // Arpeggio Pattern Part 3 Edit Dump
                data[8] == (byte) 0x11)))             // Arpeggio Pattern Part 4 Edit Dump
            return MESSAGE_PATTERN;
        else return MESSAGE_NONE;
        }

    public static boolean recognize(byte[] data)
    	{
    	int type = messageType(data);
    	return type == MESSAGE_HEADER || type == MESSAGE_PATTERN;
    	}
    }
