/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.test;
import edisyn.*;

public class SanityCheck
    {
    static boolean quiet = false;
        
    public static void main(String[] args) throws ClassNotFoundException
        {
        Main main = new Main("java edisyn.synth.SanityCheck", 
            args, 
            null, 
            new String[] { "-v", "-c" }, 
            new String[] { Main.FLAG, Main.STRING }, 
            new String[] { "Verbose", "Specific Class" },
            "SanityCheck goes through all of the synthesizers, or a specific one, and one by one it\n" +
            "does a simple sanity check on them.  First, it creates a synthesizer, randomizes\n" +
            "its parameters, and emits them to a data stream.  Then it creates a second copy of the\n" +
            "synthesizer, reads the parameters in from that data stream, and compares the two.\n" + 
            "They should be identical.  Any parameters that are not identical are referred to the\n" +
            "method Synth.testVerify(...) to determine if being kosher is acceptable.  If not, the\n" +
            "parameter is noted as [FAIL] or [NULL] (indicating that one of the parameter values is\n" +
            "null but not the other one).",
            true);
                
        quiet = !main.hasFlag("-v");
                                                
        Class[] c = Synth.getSynths();
        if (main.getString("-c") != null)
            c = new Class[] { Class.forName(main.getString("-c")) };
                
        for(int j = 0; j < c.length; j++)
            {
            Synth synth = Synth.instantiate(c[j], "whatever", true, false, null);
            Synth synth2 = Synth.instantiate(c[j], "whatever", true, false, null);
            if (synth instanceof edisyn.synth.casiocz.CasioCZ)
                {
                ((edisyn.synth.casiocz.CasioCZ)synth).setCZ1(false);
                ((edisyn.synth.casiocz.CasioCZ)synth2).setCZ1(false);
                System.err.println("\n" + c[j]);
                test(synth, synth2);
                synth = Synth.instantiate(c[j], "whatever", true, false, null);
                synth2 = Synth.instantiate(c[j], "whatever", true, false, null);
                ((edisyn.synth.casiocz.CasioCZ)synth).setCZ1(true);
                ((edisyn.synth.casiocz.CasioCZ)synth2).setCZ1(true);
                System.err.println("\n" + c[j] + " (CZ1)");
                test(synth, synth2);
                }
            else if (synth instanceof edisyn.synth.korgwavestation.KorgWavestationSequence)
                {
                ((edisyn.synth.korgwavestation.KorgWavestationSequence)synth).setBlockSending(true);
                synth = Synth.instantiate(c[j], "whatever", true, false, null);
                synth2 = Synth.instantiate(c[j], "whatever", true, false, null);
                System.err.println("\n" + c[j]);
                test(synth, synth2);
                }
            if (synth instanceof edisyn.synth.alesisd4.AlesisD4)
                {
                ((edisyn.synth.alesisd4.AlesisD4)synth).setDM5(false, false);
                ((edisyn.synth.alesisd4.AlesisD4)synth2).setDM5(false, false);
                System.err.println("\n" + c[j]);
                test(synth, synth2);
                synth = Synth.instantiate(c[j], "whatever", true, false, null);
                synth2 = Synth.instantiate(c[j], "whatever", true, false, null);
                ((edisyn.synth.alesisd4.AlesisD4)synth).setDM5(true, false);
                ((edisyn.synth.alesisd4.AlesisD4)synth2).setDM5(true, false);
                System.err.println("\n" + c[j] + " (DM5)");
                test(synth, synth2);
                }
            if (synth instanceof edisyn.synth.dsiprophet08.DSIProphet08)
                {
                ((edisyn.synth.dsiprophet08.DSIProphet08)synth).setType(edisyn.synth.dsiprophet08.DSIProphet08.SYNTH_TYPE_PROPHET_08, false);
                ((edisyn.synth.dsiprophet08.DSIProphet08)synth2).setType(edisyn.synth.dsiprophet08.DSIProphet08.SYNTH_TYPE_PROPHET_08, false);
                System.err.println("\n" + c[j]);
                test(synth, synth2);
                synth = Synth.instantiate(c[j], "whatever", true, false, null);
                synth2 = Synth.instantiate(c[j], "whatever", true, false, null);
                ((edisyn.synth.dsiprophet08.DSIProphet08)synth).setType(edisyn.synth.dsiprophet08.DSIProphet08.SYNTH_TYPE_TETRA, false);
                ((edisyn.synth.dsiprophet08.DSIProphet08)synth2).setType(edisyn.synth.dsiprophet08.DSIProphet08.SYNTH_TYPE_TETRA, false);
                System.err.println("\n" + c[j] + " (Tetra)");
                test(synth, synth2);
                synth = Synth.instantiate(c[j], "whatever", true, false, null);
                synth2 = Synth.instantiate(c[j], "whatever", true, false, null);
                ((edisyn.synth.dsiprophet08.DSIProphet08)synth).setType(edisyn.synth.dsiprophet08.DSIProphet08.SYNTH_TYPE_MOPHO, false);
                ((edisyn.synth.dsiprophet08.DSIProphet08)synth2).setType(edisyn.synth.dsiprophet08.DSIProphet08.SYNTH_TYPE_MOPHO, false);
                System.err.println("\n" + c[j] + " (Mopho)");
                test(synth, synth2);
                synth = Synth.instantiate(c[j], "whatever", true, false, null);
                synth2 = Synth.instantiate(c[j], "whatever", true, false, null);
                ((edisyn.synth.dsiprophet08.DSIProphet08)synth).setType(edisyn.synth.dsiprophet08.DSIProphet08.SYNTH_TYPE_MOPHO_KEYBOARD, false);
                ((edisyn.synth.dsiprophet08.DSIProphet08)synth2).setType(edisyn.synth.dsiprophet08.DSIProphet08.SYNTH_TYPE_MOPHO_KEYBOARD, false);
                System.err.println("\n" + c[j] + " (Mopho Keyboard and SE)");
                test(synth, synth2);
                synth = Synth.instantiate(c[j], "whatever", true, false, null);
                synth2 = Synth.instantiate(c[j], "whatever", true, false, null);
                ((edisyn.synth.dsiprophet08.DSIProphet08)synth).setType(edisyn.synth.dsiprophet08.DSIProphet08.SYNTH_TYPE_MOPHO_X4, false);
                ((edisyn.synth.dsiprophet08.DSIProphet08)synth2).setType(edisyn.synth.dsiprophet08.DSIProphet08.SYNTH_TYPE_MOPHO_X4, false);
                System.err.println("\n" + c[j] + " (Mopho x4)");
                test(synth, synth2);
                }
            else
                {
                synth = Synth.instantiate(c[j], "whatever", true, false, null);
                synth2 = Synth.instantiate(c[j], "whatever", true, false, null);
                System.err.println("\n" + c[j]);
                test(synth, synth2);
                }
            }
        }
        
    public static void test(Synth synth, Synth synth2)
        {
        try
            {
            synth.doMutate(1.0);
            byte[] data = synth.flatten(synth.emitAll((Model)null, false, true));
            synth2.parse(data, true);
            String[] keys = synth.getModel().getKeys();
            for(int i = 0; i < keys.length; i++)
                {
                Object obj1 = synth.getModel().getValue(keys[i]);
                Object obj2 = synth2.getModel().getValue(keys[i]);
                if (obj1 == null || obj2 == null)
                    {
                    boolean res = synth.testVerify(synth2, keys[i], obj1, obj2);
                    if (!quiet || !res ) System.err.println((res ? "[OKAY] " : "[NULL] ") + keys[i] + " is " + obj1 + " vs " + obj2);
                    }
                else if (!(obj1.equals(obj2)))
                    {
                    boolean res = synth.testVerify(synth2, keys[i], obj1, obj2);
                    if (!quiet || !res ) System.err.println((res ? "[OKAY] " : "[FAIL] ") + keys[i] + " is [" + obj1 + "] vs [" + obj2 + "]");
                    }
                }
            }
        catch (Exception ex)
            {
            ex.printStackTrace();
            }
        }

    }
