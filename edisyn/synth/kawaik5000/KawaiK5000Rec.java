/**
   Copyright 2026 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.kawaik5000;
import edisyn.*;

public class KawaiK5000Rec extends Recognize
    {
    public static boolean recognize(byte[] data)
        {
        boolean foundIt = false;
        for(int i = 0; i < PATCH_SIZES.length; i++)
        	{
        	if (PATCH_SIZES[i] + 10 == data.length)
        		{
        		foundIt = true;
        		break;
        		}
        	}
        
        if (!foundIt) return false;
        
        if (!((data[0] & 0xFF) == 0xF0 &&
                data[1] == 0x40 &&
                data[3] == 0x20 &&
                data[4] == 0x00 &&
                data[5] == 0x0A &&
                data[6] == 0x00 &&
                (data[data.length - 1] & 0xFF) == 0xF7))
                	{ 
                	return false; 
                	}

        return true;
        }

// Various legal patch sizes, according to the PATCH_DISTRIBUTIONS below
// Thanks to Markus Schlosser, author of the KnobKraft-orm librarian
// These sizes do not include the 10 bytes for the header, so we'll add that.
public static final int[] PATCH_SIZES = new int[] 
{
254, 
340, 
426, 
512, 
598, 
1060, 
1146, 
1232, 
1318, 
1404, 
1866, 
1952, 
2038, 
2124, 
2210, 
2758, 
2844, 
2930, 
3016, 
3650, 
3736, 
3822, 
4542, 
4628, 
5434, 
};

// Various combinations of PCM and ADD sources which produce the corresponding sizes 
// in PATCH_SIZES above.  Each pair is of the form { NUM_PCM, NUM_ADD }.
// Thanks to Markus Schlosser, author of the KnobKraft-orm librarian
public static final int[][] PATCH_DISTRIBUTIONS = new int[][]
{
{ 2, 0 },
{ 3, 0 },
{ 4, 0 },
{ 5, 0 },
{ 6, 0 },
{ 1, 1 },
{ 2, 1 },
{ 3, 1 },
{ 4, 1 },
{ 5, 1 },
{ 0, 2 },
{ 1, 2 },
{ 2, 2 },
{ 3, 2 },
{ 4, 2 },
{ 0, 3 },
{ 1, 3 },
{ 2, 3 },
{ 3, 3 },
{ 0, 4 },
{ 1, 4 },
{ 2, 4 },
{ 0, 5 },
{ 1, 5 },
{ 0, 6 },
};
    
    }
