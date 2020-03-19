package edisyn.synth.tuning;

import edisyn.*;

public abstract class TuningDefinition 
{
	int bases[] = new int[128];
	int detunes[] = new int[128];
	int rootMIDINote = 0;
	double rootFrequency = 1;
	boolean configured; 
	
	public void setConfigured(boolean val) { configured = val; }
	public boolean isConfigured() { return configured; }
	public int[] getBases() { return bases; }
	public int[] getDetunes() { return detunes; }
	public int getRootMIDINote() { return rootMIDINote; }
	public void setRootMIDINote(int val) { rootMIDINote = val; }
	public double getRootFrequency() { return rootFrequency; }
	public void setRootFrequency(double val) { rootFrequency = val; }
	
	static double LOG_2 = Math.log(2);
	static double INV_LOG_2 = 1/LOG_2;
	static int TWO_TO_THE_14 = 16384; 		// (int) Math.pow(2, 14);

	public static double midiNumberToHz(int m)
	{
		return Math.pow(2, (m - 69) / 12.0) * 440.0;
	}

	public static double hzToMidiNumber(double hz)
	{
		return Math.log(hz/440) * INV_LOG_2 * 12 + 69;
	}

	public static double centsAbove(double f2, double f1) 
	{
		return 1200 * Math.log(f2/f1) * INV_LOG_2;
	}
	
	public static int centsToTicks(double c) 
	{
		return (int)((c * TWO_TO_THE_14 / 100.0) + 0.5);
	}
	
	public void setNoteFrequency(int note_index, double freq)
	{
		int ind = (int)hzToMidiNumber(freq);
		double base = midiNumberToHz(ind);
		double cents = centsAbove(freq, base);
		while(cents < 0) {
			ind--;
			cents += 100;
		}
		int ticks = centsToTicks(cents);
		if (ticks == TWO_TO_THE_14)	
			   {
				   ticks = 0;
				   ind++;
			   }
		bases[note_index] = ind;
		detunes[note_index] = ticks;
	}
	public abstract void popup(Synth synth);
	public abstract String getMenuName();
}
