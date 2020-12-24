/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.test;
import edisyn.*;

public class SanityCheck
    {
    static boolean quiet = false;
    static boolean dump  = false;
        
    public static void main(String[] args) throws ClassNotFoundException
        {
        Main main = new Main("java edisyn.synth.SanityCheck", 
            args, 
            null, 
            new String[] { "-v", "-c", "-n", "-d" }, 
            new String[] { Main.FLAG, Main.STRING, Main.INT, Main.FLAG }, 
            new String[] { "Verbose", "Specific Class", "Number of Times", "Dump Test Sysex on Failure" },
            "SanityCheck is essentially a fuzzing tester.\n\n" +
            "SanityCheck goes through all of the synthesizers, or a specific one, and one by one it\n" +
            "does a simple sanity check on them.  First, it creates a synthesizer, randomizes\n" +
            "its parameters, and emits them to a data stream.  Then it creates a second copy of the\n" +
            "synthesizer, reads the parameters in from that data stream, and compares the two.\n" + 
            "They should be identical.  Any parameters that are not identical are referred to the\n" +
            "method Synth.testVerify(...) to determine if being non-identical is acceptable.  If not, the\n" +
            "parameter is noted as [FAIL] or [NULL] (indicating that one of the parameter values is\n" +
            "null but not the other one).",
            true);
                
        quiet = !main.hasFlag("-v");
        dump = main.hasFlag("-d");
        
        System.err.println("For help, try:  java edisyn.synth.SanityCheck -h");
          
        String[] c = Synth.getClassNames();          
        if (main.getString("-c") != null)
            c = new String[] { main.getString("-c").trim() };
        
        int num = main.getInt("-n", 1);
        if (num < 0)
            {
            System.err.println("-n:  number must be >= 1");
            System.exit(1);
            }
                
        for(int n = 0; n < num; n++)
            {
            if (num > 1) System.err.println("Round " + (n + 1) + "\n");
                
            for(int j = 0; j < c.length; j++)
                {
                Synth synth = Synth.instantiate(c[j], true, false, null);
                Synth synth2 = Synth.instantiate(c[j], true, false, null);
                if (synth instanceof edisyn.synth.casiocz.CasioCZ)
                    {
                    ((edisyn.synth.casiocz.CasioCZ)synth).setCZ1(false);
                    ((edisyn.synth.casiocz.CasioCZ)synth2).setCZ1(false);
                    System.err.println(c[j]);
                    test(synth, synth2);
                    synth = Synth.instantiate(c[j], true, false, null);
                    synth2 = Synth.instantiate(c[j], true, false, null);
                    ((edisyn.synth.casiocz.CasioCZ)synth).setCZ1(true);
                    ((edisyn.synth.casiocz.CasioCZ)synth2).setCZ1(true);
                    System.err.println(c[j] + " (CZ1)");
                    test(synth, synth2);
                    }
                else if (synth instanceof edisyn.synth.korgwavestation.KorgWavestationSequence)
                    {
                    ((edisyn.synth.korgwavestation.KorgWavestationSequence)synth).setBlockSending(true);
                    synth = Synth.instantiate(c[j], true, false, null);
                    synth2 = Synth.instantiate(c[j], true, false, null);
                    System.err.println(c[j]);
                    test(synth, synth2);
                    }
                else if (synth instanceof edisyn.synth.alesisd4.AlesisD4)
                    {
                    ((edisyn.synth.alesisd4.AlesisD4)synth).setDM5(false, false);
                    ((edisyn.synth.alesisd4.AlesisD4)synth2).setDM5(false, false);
                    System.err.println(c[j]);
                    test(synth, synth2);
                    synth = Synth.instantiate(c[j], true, false, null);
                    synth2 = Synth.instantiate(c[j], true, false, null);
                    ((edisyn.synth.alesisd4.AlesisD4)synth).setDM5(true, false);
                    ((edisyn.synth.alesisd4.AlesisD4)synth2).setDM5(true, false);
                    System.err.println(c[j] + " (DM5)");
                    test(synth, synth2);
                    }
                else if (synth instanceof edisyn.synth.dsiprophet08.DSIProphet08)
                    {
                    ((edisyn.synth.dsiprophet08.DSIProphet08)synth).setType(edisyn.synth.dsiprophet08.DSIProphet08.SYNTH_TYPE_PROPHET_08, false);
                    ((edisyn.synth.dsiprophet08.DSIProphet08)synth2).setType(edisyn.synth.dsiprophet08.DSIProphet08.SYNTH_TYPE_PROPHET_08, false);
                    System.err.println(c[j]);
                    test(synth, synth2);
                    synth = Synth.instantiate(c[j], true, false, null);
                    synth2 = Synth.instantiate(c[j], true, false, null);
                    ((edisyn.synth.dsiprophet08.DSIProphet08)synth).setType(edisyn.synth.dsiprophet08.DSIProphet08.SYNTH_TYPE_TETRA, false);
                    ((edisyn.synth.dsiprophet08.DSIProphet08)synth2).setType(edisyn.synth.dsiprophet08.DSIProphet08.SYNTH_TYPE_TETRA, false);
                    System.err.println(c[j] + " (Tetra)");
                    test(synth, synth2);
                    synth = Synth.instantiate(c[j], true, false, null);
                    synth2 = Synth.instantiate(c[j], true, false, null);
                    ((edisyn.synth.dsiprophet08.DSIProphet08)synth).setType(edisyn.synth.dsiprophet08.DSIProphet08.SYNTH_TYPE_MOPHO, false);
                    ((edisyn.synth.dsiprophet08.DSIProphet08)synth2).setType(edisyn.synth.dsiprophet08.DSIProphet08.SYNTH_TYPE_MOPHO, false);
                    System.err.println(c[j] + " (Mopho)");
                    test(synth, synth2);
                    synth = Synth.instantiate(c[j], true, false, null);
                    synth2 = Synth.instantiate(c[j], true, false, null);
                    ((edisyn.synth.dsiprophet08.DSIProphet08)synth).setType(edisyn.synth.dsiprophet08.DSIProphet08.SYNTH_TYPE_MOPHO_KEYBOARD, false);
                    ((edisyn.synth.dsiprophet08.DSIProphet08)synth2).setType(edisyn.synth.dsiprophet08.DSIProphet08.SYNTH_TYPE_MOPHO_KEYBOARD, false);
                    System.err.println(c[j] + " (Mopho Keyboard and SE)");
                    test(synth, synth2);
                    synth = Synth.instantiate(c[j], true, false, null);
                    synth2 = Synth.instantiate(c[j], true, false, null);
                    ((edisyn.synth.dsiprophet08.DSIProphet08)synth).setType(edisyn.synth.dsiprophet08.DSIProphet08.SYNTH_TYPE_MOPHO_X4, false);
                    ((edisyn.synth.dsiprophet08.DSIProphet08)synth2).setType(edisyn.synth.dsiprophet08.DSIProphet08.SYNTH_TYPE_MOPHO_X4, false);
                    System.err.println(c[j] + " (Mopho x4)");
                    test(synth, synth2);
                    }
                else if (synth instanceof edisyn.synth.yamahatg33.YamahaTG33)
                    {
                    ((edisyn.synth.yamahatg33.YamahaTG33)synth).setSynthType(edisyn.synth.yamahatg33.YamahaTG33.TYPE_TG33, false);
                    ((edisyn.synth.yamahatg33.YamahaTG33)synth2).setSynthType(edisyn.synth.yamahatg33.YamahaTG33.TYPE_TG33, false);
                    System.err.println(c[j]);
                    test(synth, synth2);
                    synth = Synth.instantiate(c[j], true, false, null);
                    synth2 = Synth.instantiate(c[j], true, false, null);
                    ((edisyn.synth.yamahatg33.YamahaTG33)synth).setSynthType(edisyn.synth.yamahatg33.YamahaTG33.TYPE_SY22, false);
                    ((edisyn.synth.yamahatg33.YamahaTG33)synth2).setSynthType(edisyn.synth.yamahatg33.YamahaTG33.TYPE_SY22, false);
                    System.err.println(c[j] + " (SY22)");
                    test(synth, synth2);
                    synth = Synth.instantiate(c[j], true, false, null);
                    synth2 = Synth.instantiate(c[j], true, false, null);
                    ((edisyn.synth.yamahatg33.YamahaTG33)synth).setSynthType(edisyn.synth.yamahatg33.YamahaTG33.TYPE_SY35, false);
                    ((edisyn.synth.yamahatg33.YamahaTG33)synth2).setSynthType(edisyn.synth.yamahatg33.YamahaTG33.TYPE_SY35, false);
                    System.err.println(c[j] + " (SY35)");
                    test(synth, synth2);
                    }
                else if (synth instanceof edisyn.synth.yamaha4op.Yamaha4Op)
                    {
                    ((edisyn.synth.yamaha4op.Yamaha4Op)synth).setSynthType(edisyn.synth.yamaha4op.Yamaha4Op.TYPE_TQ5_YS100_YS200_B200, false);
                    ((edisyn.synth.yamaha4op.Yamaha4Op)synth2).setSynthType(edisyn.synth.yamaha4op.Yamaha4Op.TYPE_TQ5_YS100_YS200_B200, false);
                    System.err.println(c[j]);
                    test(synth, synth2);
                    synth = Synth.instantiate(c[j], true, false, null);
                    synth2 = Synth.instantiate(c[j], true, false, null);
                    ((edisyn.synth.yamaha4op.Yamaha4Op)synth).setSynthType(edisyn.synth.yamaha4op.Yamaha4Op.TYPE_V50, false);
                    ((edisyn.synth.yamaha4op.Yamaha4Op)synth2).setSynthType(edisyn.synth.yamaha4op.Yamaha4Op.TYPE_V50, false);
                    System.err.println(c[j] + " (V50)");
                    test(synth, synth2);
                    }
                else if (synth instanceof edisyn.synth.maudiovenom.MAudioVenomArp)
                    {
                    edisyn.synth.maudiovenom.MAudioVenomArp.truncateAndSortOnEmit = false;
                    synth = Synth.instantiate(c[j], true, false, null);
                    synth2 = Synth.instantiate(c[j], true, false, null);
                    System.err.println(c[j]);
                    test(synth, synth2);
                    edisyn.synth.maudiovenom.MAudioVenomArp.truncateAndSortOnEmit = true;
                    }
                else
                    {
                    synth = Synth.instantiate(c[j], true, false, null);
                    synth2 = Synth.instantiate(c[j], true, false, null);
                    System.err.println(c[j]);
                    test(synth, synth2);
                    }
                }
            }
        }
        
    public static void test(Synth synth, Synth synth2)
        {
        boolean failed = false;
        byte[] data = new byte[0];
        
        try
            {
            // prepare so raw parsing doesn't try to push a million things on the undo stack and write stuff
            synth.setSendMIDI(false);
            synth.getUndo().setWillPush(false);
            synth2.setSendMIDI(false);
            synth2.getUndo().setWillPush(false);
                
            synth.doMutate(1.0);
            data = synth.flatten(synth.emitAll((Model)null, false, true));
            if (!synth2.recognizeLocal(data))
                {
                System.err.println("\t [FAIL] Not Recognized");
                for(int i = 0; i < data.length; i++)
                	{
                	System.err.println("" + i + " " + toHex(data[i]));
                	}
                failed = true;
                }
            synth2.parse(data, true);
            String[] keys = synth.getModel().getKeys();
            for(int i = 0; i < keys.length; i++)
                {
                Object obj1 = synth.getModel().getValue(keys[i]);
                Object obj2 = synth2.getModel().getValue(keys[i]);
                if (obj1 == null || obj2 == null)
                    {
                    boolean res = synth.testVerify(synth2, keys[i], obj1, obj2);
                    if (!quiet || !res ) System.err.println((res ? "\t[OKAY] " : "\t[NULL] ") + keys[i] + " is " + obj1 + " vs " + obj2);
                    failed = failed || !res;
                    }
                else if (!(obj1.equals(obj2)))
                    {
                    boolean res = synth.testVerify(synth2, keys[i], obj1, obj2);
                    if (!quiet || !res ) System.err.println((res ? "\t[OKAY] " : "\t[FAIL] ") + keys[i] + " is [" + obj1 + "] vs [" + obj2 + "]");
                    failed = failed || !res;
                    }
                }
            }
        catch (Exception ex)
            {
            ex.printStackTrace();
            failed = true;
            }
        
        if (failed && dump)
            {
            System.err.println("DUMP");
            System.err.println("Length: " +  data.length);
            for(int i = 0; i < data.length; i++)
                {
                System.err.println("" + i + " " + toHex(data[i]) + " " + data[i] + " " + ((data[i] & 0xFF) < 32 ? "" : (char)(data[i] & 0xFF)));
                }
            System.err.println("---------");
            System.err.println();
            }
        }

    public static String toHex(int val)
        {
        return String.format("0x%08X", val);
        }

    }
