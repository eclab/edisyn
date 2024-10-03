/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn.synth.novationastation;
import edisyn.*;

public class NovationAStationRec
    {
    /** Create your own Synth-specific class version of this static method.
        It will be called when the system wants to know if the given sysex patch dump
        is for your kind of synthesizer.  Return true if so, else false. */
    public static boolean recognize(byte[] data)
        {
        // The Synth.java version of this method is obviously never called.
        // But your subclass's version will be called.
        return false;
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
    public static int getNextSysexPatchGroup(byte[][] sysex, int start)
        {
        // Starting with sysex[start], return the start of the NEXT patch.
        // For example, if your synthesizer uses three sysex messages for a patch,
        // and the the messages may be found in sysex[start], sysex[start+1],
        // and sysex[start+2], then return start+3  If no patch for your synthesizer
        // begins at sysex[start], or if it is incomplete before the sysex
        // messages are finished then return start.   In rare cases, such as 
        // the Roland U-220, there may be more than one patch embedded in a single 
        // sysex message, or an unusual number embedded in several messages (such as 20 embedeed
        // in 10 messages). In this case, you should return  0 - N, where N is the number of messages to skip.
        return start;
        }

    public byte[][][] breakSysexMessageIntoPatches(byte[][] messages, int start, int expectedMessagesToSkip)
        {
        // In bulk dumps, the U-220 can embed multiple (non-bank) patches into
        // the same sysex message.  This method is called to break those patches
        // into separate sysex messages.
        return new byte[][][] { messages }; // a reasonable default, but shouldn't be used
        }
                
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
        
