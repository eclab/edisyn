/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn;

import java.io.*;
import java.util.*;

/** This little java file is a utility which prints out diffs of sysex files
    for debugging.  You provide three arguments: the two file names, and the
    number of bytes to skip at the beginning.  Thereafter the columns are:
        
    1.      Byte index
    2.      Byte index if the zeroth and every consecutive eighth byte are skipped, Korg 8-bit packing style
    3.      Byte value in first file
    4.      Byte value in second file
    5.      ASCII value in first file
    6.      ASCII value in second file
    7.      "<--" if the bytes differ
*/

public class SyxDiff
    {
    public static void main(String[] args) throws Exception
        {
        FileInputStream scan1 = new FileInputStream(new File(args[0]));
        FileInputStream scan2 = new FileInputStream(new File(args[1]));
                
        int index = 0;
        int foo = 0;
        for(int i = 0; i < Integer.valueOf(args[2]); i++)
            { scan1.read(); scan2.read(); }
                        
        while(true)
            {
            int b1 = scan1.read();
            int b2 = scan2.read();
            if (b1 < 0 || b2 < 0) break;
                        
            if (index % 8 == 1) foo--;

            System.out.println("" + index + "\t" + 
                (index % 8 == 0 ? "-" : foo) + "\t" + b1 + "\t" + b2 + "\t" +
                (b1 > 31 && b1 < 127 ? (char)b1 : "?") + "\t" + 
                (b2 > 31 && b2 < 127 ? (char)b2 : "?") + "\t" +
                (b1 == b2 ? "" : "\t<--"));
            index++;
            foo++;
            }
        }
    }
