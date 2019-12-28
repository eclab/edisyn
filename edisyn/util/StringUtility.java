/***
    Copyright 2019 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.util;

public class StringUtility
    {
    /** Returns the sole integer embedded within the string.  If
        multiple integers are embedded, they will be concatenated into a larger
        integer, which you probably don't want.  Throws NumberFormatException
        if there is no integer at all. */
    public static int getInt(String string)
        {
        return Integer.parseInt(string.replaceAll("[^0-9]+", " ").trim());
        }
                
    /** Returns the sole integer embedded within the string after a preamble.  
        Does not check to see if the preamble is real -- it better be!  If
        multiple integers are embedded after the preamble, they will be 
        concatenated into a larger
        integer, which you probably don't want.  Throws NumberFormatException
        if there is no integer at all. */
    public static int getIntAfter(String string, String preamble)
        {
        return getInt(string.substring(preamble.length()));
        }
    }

