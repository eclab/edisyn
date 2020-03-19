package edisyn.synth.tuning.tuningdefinitions;

import edisyn.synth.tuning.*;
import edisyn.*;
import edisyn.gui.*;
import javax.swing.*;
import java.awt.*;

public class EDO extends TuningDefinition 
	{
	public void popup(Synth synth)
		{
		String name = synth.getSynthNameLocal();
		setConfigured(false);
		
		final JTextField rootMIDINote = new JTextField("" + Synth.getLastXAsInt("rootMIDINote", name, 69, true));
		JTextField rootFrequency = new JTextField("" + Synth.getLastXAsDouble("rootFrequency", name, 440.0, true));
		JTextField divisionsPerOctave = new JTextField("" + Synth.getLastXAsInt("EDODivisionsPerOctive", name, 12, true));
		
		PushButton compute = new PushButton("Compute")
			{
			public void perform()
				{
				int rmn = -1;
				try { rmn = Integer.parseInt(rootMIDINote.getText()); if (rmn < 0 || rmn > 127) throw new RuntimeException(); }
				catch (Exception ex)
					{
					synth.showSimpleError("Cannot compute", "The root MIDI note must be an integer between 0 and 127"); 
					return;
					}
				rootFrequency.setText("" + TuningDefinition.midiNumberToHz(rmn));
				}
			};
		compute.setBackground(new JButton().getBackground());
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(rootFrequency, BorderLayout.CENTER);
		panel.add(compute, BorderLayout.EAST);
		
		while(true)
			{
			boolean res = Synth.showMultiOption(synth,
				new String[] { 	"Root MIDI Note (0...127)",
								"Root Frequency", 
								"Divisions Per Octave" },
				new JComponent[] { rootMIDINote, panel, divisionsPerOctave },
				"EDO Tuning",
				"Enter EDO Tuning Information.  MIDI note 69 is classically A-440.");
			
			if (!res) return;
			
			int rmn = -1;
			try { rmn = Integer.parseInt(rootMIDINote.getText()); if (rmn < 0 || rmn > 127) throw new RuntimeException(); }
			catch (Exception ex)
				{
				synth.showSimpleError("EDO Tuning", "The root MIDI note must be an integer between 0 and 127"); 
				continue;
				}

			double rf = -1;
			try { rf = Double.parseDouble(rootFrequency.getText()); if (rf <= 0) throw new RuntimeException(); }
			catch (Exception ex)
				{
				synth.showSimpleError("EDO Tuning", "The root frequency must be a real value greater than 0.0");
				continue;
				}

			int dpo = -1;
			try { dpo = Integer.parseInt(divisionsPerOctave.getText()); if (dpo <= 0) throw new RuntimeException(); }
			catch (Exception ex)
				{
				synth.showSimpleError("EDO Tuning", "The root MIDI note must be an integer between 0 and 127"); 
				continue;
				}

			Synth.setLastX("rootMIDINote", "" + rmn, name, false);
			Synth.setLastX("rootFrequency", "" + rf, name, false);
			Synth.setLastX("EDODivisionsPerOctive", "" + dpo, name, false);
			realize(rmn, rf, dpo);
			setConfigured(true);
			return;
			}
		}
		
	public static double edoNumberToHz(int m,
	                                   int divisionsPerOctave,
	                                   double baseFreq,
	                                   int baseNote)
	{
		return Math.pow(2, (m - baseNote) / (divisionsPerOctave * 1.0)) * baseFreq;
	}
	
	void realize(int rootMIDINote, double rootFrequency, int divisionsPerOctave) 
	{
		for(int i = 0; i < 128; i++)
			{
			setNoteFrequency(i, edoNumberToHz(i,
			                          divisionsPerOctave,
			                          rootFrequency,
			                          rootMIDINote));
			}
	}

	public String getMenuName() { return "EDO"; }
}
