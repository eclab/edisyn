/**
   Copyright 2020 by Sean Luke
   Licensed under the Apache License version 2.0
*/

package edisyn;


public class Recognize
    {
    /** Create your own Synth-specific class version of this static method.
        It will be called when the system wants to know if the given sysex patch dump
        is for your kind of synthesizer.  Return true if so, else false. */
    private static boolean recognize(byte[] data)
        {
        // The Synth.java version of this method is obviously never called.
        // But your subclass's version will be called.
        return false;
        }

    /** Create your own Synth-specific class version of this static method.
        It will be called when the system wants to know if the given sysex patch dump
        is a bank (multi-patch dump) for your kind of synthesizer.  Return true if so, else false. 
        If you don't handle bank sysex dumps, by default it returns false.  */
    private static boolean recognizeBank(byte[] data)
        {
        // The Synth.java version of this method is obviously never called.
        // But your subclass's version will be called.
        return false;
        }
        
    /** Create your own Synth-specific class version of this static method.
        Return end just beyond the sysex messages which comprise a single patch starting at sysex[start].
        If there are no such messages, return start. */
    private static int getNextSysexPatchGroup(byte[][] sysex, int start)
        {
        return start;
        }

    /** Create your own Synth-specific class version of this static method.
        It will be called when the system wants to know what the name is for a patch bank
        stored as bank sysex. This is quite rare -- only the Yamaha FB-01 has names for its
        banks.  If you don't implement this method, by default it returns the empty string, which is a
        reasonable default. */
    private static String getBankName(byte[] data)
        {
        return "";
        }
        
    /** A few synthesizers, such as the Kawai K4, have mega-bank sysex which consists of different kinds
        of patches, such as single patches and multi patches.  This method is called
        to break these into multiple individual or multiple bank patches that can
        be parsed by different editors.  If you do not support this, return null
        (the default). */
/*      private byte[][] convertBulkBank(byte[] data)
        {
        return null;
        }
*/
    }
        
