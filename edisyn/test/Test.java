/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.test;
public class Test
	{
	static boolean quiet = false;
	
	public static void main(String[] args) throws ClassNotFoundException
		{
		Main main = new Main("Test", args, null, 
			new String[] { "-v", "-c" }, new String[] { Main.FLAG, Main.STRING }, 
			new String[] { "Verbose", "Specific Class"});
		
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
			else if (synth instanceof edisyn.synth.korgwavestation.KorgWaveStationSquence)
				{
				((edisyn.synth.korgwavestation.KorgWaveStationSquence)synth).setBlockSending(true);
				 synth = Synth.instantiate(c[j], "whatever", true, false, null);
				 synth2 = Synth.instantiate(c[j], "whatever", true, false, null);
				System.err.println("\n" + c[j]);
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