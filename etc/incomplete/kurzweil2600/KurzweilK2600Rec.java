/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.kurzweilk2600;
import edisyn.*;

public class KurzweilK2600Rec extends Recognize
    {
    /** Create your own Synth-specific class version of this static method.
        It will be called when the system wants to know if the given sysex patch dump
        is for your kind of synthesizer.  Return true if so, else false. */
    public static boolean recognize(byte[] data)
        {
        // The Synth.java version of this method is obviously never called.
        // But your subclass's version will be called.
        return ((data[0] == (byte)0xF0 &&
                data[1] == (byte) 0x07 &&       // Kurzweil
                data[2] == (byte) 0x00 &&               // device ID
                data[3] == (byte) 0x78 &&               // K2600
                data[4] == (byte) 0x09));       // Write
        }

    /** Create your own Synth-specific class version of this static method.
        It will be called when the system wants to know if the given sysex patch dump
        is a bank (multi-patch dump) for your kind of synthesizer.  Return true if so, else false. 
        If you don't handle bank sysex dumps, by default it returns false.  */
    public static boolean recognizeBank(byte[] data)
        {
        // The Synth.java version of this method is obviously never called.
        // But your subclass's version will be called.
        return false;
        }
        
    /** Create your own Synth-specific class version of this static method.
        Return end just beyond the sysex messages which comprise a single patch starting at sysex[start].
        If there are no such messages, return start. */
    /*
      public static int getNextSysexPatchGroup(byte[][] sysex, int start)
      {
      return start;
      } */

    /** Create your own Synth-specific class version of this static method.
        It will be called when the system wants to know what the name is for a patch bank
        stored as bank sysex. This is quite rare -- only the Yamaha FB-01 has names for its
        banks.  If you don't implement this method, by default it returns the empty string, which is a
        reasonable default. */
    public static String getBankName(byte[] data)
        {
        return "";
        }
    }
        
