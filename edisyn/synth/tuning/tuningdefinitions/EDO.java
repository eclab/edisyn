package edisyn.synth.tuning.tuningdefinitions;
import edisyn.synth.tuning.*;
import javax.swing.JOptionPane;

public class EDO extends TuningDefinition {
	private int divisions_per_octave;
	public static double edoNumberToHz(int m,
	                                   int divisions_per_octave,
	                                   double base_freq,
	                                   int base_note){
		return Math.pow(2, (m-base_note)/(divisions_per_octave*1.0))*base_freq;
	}
	public void realize(int root_midi_note, double frequency) {
		if(!configured) {
			throw new RuntimeException("NOT CONFIGURED!");
		}
		for(int i = 0; i < 128; i++){
			setNoteFreq(i, edoNumberToHz(i,
			                             divisions_per_octave,
			                             frequency,
			                             root_midi_note));
		}
	}
	public void manuallyConfigure(int divisions_per_octave){
		configured = true;
		this.divisions_per_octave = divisions_per_octave;
	}
	public void configurationPopup() {
		configured = true;
		divisions_per_octave =
			Integer.parseInt(JOptionPane.showInputDialog("Divisions Per Octave"));
	}
	public String getMenuName() {
		return "EDO";
	}
}
