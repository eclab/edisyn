/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.util;

import java.io.*;
import java.util.*;

public class SyxSplit
    {
    public static void main(String[] args) throws Exception
        {
        FileInputStream in = new FileInputStream(new File(args[0]));
        FileOutputStream out = null;
                
        int count = 0;
        while(true)
            {
            int val = in.read();
            if (val < 0)
                {
                if (out != null) out.close();
                System.exit(0);
                }
            else
                {
                if (out == null)
                    out = new FileOutputStream(new File(args[0] + "." + count + ".syx"));
                out.write(val);
                if (val == 0xF7)
                    {
                    out.close();
                    out = null;
                    count++;
                    }
                }
            }
        }
    }
