package edisyn.synth.tuning;
public abstract class TuningDefinition {
	public int bases[] = new int[128];
	public int detunes[] = new int[128];
	public boolean configured = false;
	private static double LOG_2 = Math.log(2);
	private static double INV_LOG_2 = 1/LOG_2;
	private static int TWOTOTHE14 = (int)Math.pow(2,14);

	public static double midiNumberToHz(int m){
		return Math.pow(2, (m - 69)/12.0)*440.0;
	}

	public static double hzToMidiNumber(double hz){
		return Math.log(hz/440)*INV_LOG_2*12+69;
	}

	public static double centsAbove(double f2, double f1) {
		return 1200*Math.log(f2/f1)*INV_LOG_2;
	}
	
	public static int centsToTicks(double c) {
		return (int)((c * TWOTOTHE14 / 100.0) + 0.5);
	}
	
	public void setNoteFreq(int note_index, double freq){
		int ind = (int)hzToMidiNumber(freq);
		double base = midiNumberToHz(ind);
		double cents = centsAbove(freq, base);
		int ticks = centsToTicks(cents);
		if (ticks == TWOTOTHE14){
			ticks = 0;
			ind++;
		}
		bases[note_index] = ind;
		detunes[note_index] = ticks;
	}

	public abstract void realize(int root_midi_note, double frequency);
	public abstract void configurationPopup(); // spawn the pop-up window to configure
	public abstract String getMenuName();
	public void reset() {
		configured = false;
	}
}
