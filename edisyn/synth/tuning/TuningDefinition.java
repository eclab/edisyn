package edisyn.synth.tuning;

import edisyn.*;
import edisyn.synth.tuning.*;
import edisyn.*;
import edisyn.gui.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

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
	static int TWO_TO_THE_14 = 16384;               // (int) Math.pow(2, 14);

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
		int ind = (int)Math.floor(hzToMidiNumber(freq));
		double base = midiNumberToHz(ind);
		double cents = centsAbove(freq, base);
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


	JTextField rootMIDINoteF = null;
	JTextField rootFrequencyF = null;
	public static final int DEFAULT_ROOT_MIDI_NOTE = 69;
	public static final double DEFAULT_ROOT_FREQUENCY = 440.0;
        
	public JComponent getRootMIDINoteComponent(Synth synth)
	{
		String name = synth.getSynthNameLocal();
		if (rootMIDINoteF == null) rootMIDINoteF = new JTextField("" + Synth.getLastXAsInt("rootMIDINote", name, DEFAULT_ROOT_MIDI_NOTE, true));
		return rootMIDINoteF;
	}

	public JComponent getRootFrequencyComponent(Synth synth)
	{
		String name = synth.getSynthNameLocal();
		getRootMIDINote();  // compute it
		rootFrequencyF = new JTextField("" + Synth.getLastXAsDouble("rootFrequency", name, DEFAULT_ROOT_FREQUENCY, true));
		JButton compute = new JButton("Compute");
		compute.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					int rmn = -1;
					try { rmn = Integer.parseInt(rootMIDINoteF.getText()); if (rmn < 0 || rmn > 127) throw new RuntimeException(); }
					catch (Exception ex)
						{
							synth.showSimpleError(rootMIDINoteF, "Cannot compute", "The root MIDI note must be an integer between 0 and 127"); 
							return;
						}
					rootFrequencyF.setText("" + TuningDefinition.midiNumberToHz(rmn));
				}
			});
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(rootFrequencyF, BorderLayout.CENTER);
		panel.add(compute, BorderLayout.EAST);
		return panel;
	}
        
	public int getRootMeanNoteValue(Synth synth)
	{
		String name = synth.getSynthNameLocal();
		int rmn = -1;
		try { rmn = Integer.parseInt(rootMIDINoteF.getText()); if (rmn < 0 || rmn > 127) throw new RuntimeException(); }
		catch (Exception ex)
			{
				synth.showSimpleError("EDO Tuning", "The root MIDI note must be an integer between 0 and 127"); 
				rootMIDINoteF.setText("" + Synth.getLastXAsInt("rootMIDINote", name, DEFAULT_ROOT_MIDI_NOTE, true));
				return -1;
			}
		return rmn;
	}

	public double getRootFrequencyValue(Synth synth)
	{
		String name = synth.getSynthNameLocal();
		double rf = -1;
		try { rf = Double.parseDouble(rootFrequencyF.getText()); if (rf <= 0.0) throw new RuntimeException(); }
		catch (Exception ex)
			{
				synth.showSimpleError("EDO Tuning", "The root frequency must be a real value greater than 0.0");
				rootFrequencyF.setText("" + Synth.getLastXAsDouble("rootFrequency", name, DEFAULT_ROOT_FREQUENCY, true));
				return -1;
			}
		return rf;
	}
        
        
	public void resetRootMIDINoteAndFrequency(Synth synth)
	{
		String name = synth.getSynthNameLocal();
		Synth.setLastX("" + DEFAULT_ROOT_MIDI_NOTE, "rootMIDINote", name, false);
		Synth.setLastX("" + DEFAULT_ROOT_FREQUENCY, "rootFrequency", name, false);
		rootMIDINoteF.setText("" + DEFAULT_ROOT_MIDI_NOTE);
		rootFrequencyF.setText("" + DEFAULT_ROOT_FREQUENCY);
	}
        
	public void setRootMIDINoteAndFrequency(Synth synth, int midiNote, double frequency)
	{
		String name = synth.getSynthNameLocal();
		Synth.setLastX("" + midiNote, "rootMIDINote", name, false);
		Synth.setLastX("" + frequency, "rootFrequency", name, false);
		// these are probably not necessary
		rootMIDINoteF.setText("" + midiNote);
		rootFrequencyF.setText("" + frequency);
	}

}
