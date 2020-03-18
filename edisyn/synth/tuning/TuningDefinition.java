package edisyn.synth.tuning;
public interface TuningDefinition {
	public MTS realize(int root_midi_note, double frequency);
	public void configurationPopup(); // spawn the pop-up window to configure
	public String getMenuName();
	public void reset();
}
