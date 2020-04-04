package edisyn.synth.tuning.tuningdefinitions;

import edisyn.synth.tuning.*;
import edisyn.*;
import edisyn.gui.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

public class RepeatingScale extends TuningDefinition 
{

	double[] offsets;
	double equivalencyRatio;
	String name;
	public RepeatingScale(double[] offsets, String name)
	{

		this.offsets = new double[offsets.length-1];
		for(int i = 0; i < offsets.length-1; i++){
			this.offsets[i] = offsets[i];
		}
		this.equivalencyRatio = frequencyAbove(offsets[offsets.length-1], 1);
		this.name = name;
	}
	double frequencyAbove(double c2, double f1)
	{
		return f1 * Math.pow(2, c2/1200);
	}
	double getFrequency(int midiNum, int rootMIDINote, double rootFrequency)
	{
		if(midiNum == rootMIDINote)
			{
				return frequencyAbove(offsets[0], rootFrequency);
			}
		if(midiNum < rootMIDINote)
			{
				int diff = rootMIDINote - midiNum;
				int idx = offsets.length - (diff % offsets.length);
				if(idx == offsets.length) {
					idx = 0;
				}
				int rootsBelow = (int)Math.ceil((double)diff / offsets.length);
				double baseFreq = rootFrequency/Math.pow(equivalencyRatio, rootsBelow);
				return frequencyAbove(offsets[idx], baseFreq);
			}
		if(midiNum > rootMIDINote)
			{
				int diff = midiNum - rootMIDINote;
				int idx = diff % offsets.length;
				int rootsAbove = diff/offsets.length;
				double baseFreq = rootFrequency * Math.pow(equivalencyRatio, rootsAbove);
				return frequencyAbove(offsets[idx], baseFreq);
			}
		return -1;
	}
        
	void realize(int rootMIDINote, double rootFrequency) 
	{
		for(int i = 0; i < 128; i++)
			{
				setNoteFrequency(i, getFrequency(i,
				                                 rootMIDINote,
				                                 rootFrequency));
			}
	}

	public String getMenuName() { return name; }


	public void popup(Synth synth)
	{
		String name = synth.getSynthNameLocal();
		setConfigured(false);
                
		JComponent rootMIDINote = getRootMIDINoteComponent(synth);
		JComponent rootFrequency = getRootFrequencyComponent(synth);
                
		while(true)
			{
				int res = Synth.showMultiOption(synth,
				                                new String[] {  "Root MIDI Note (0...127)",
				                                                "Root Frequency"},
				                                new JComponent[] { rootMIDINote, rootFrequency },
				                                new String[] { "Okay", "Cancel", "Reset" }, 0, 
				                                "Tuning",
				                                "Enter Tuning Information.  MIDI note 69 is classically A-440.");
                        
				if (res == 2)  // reset
					{
						resetRootMIDINoteAndFrequency(synth);
						continue;
					}
				else if (res == 1) // cancel
					return;
                        
				int rmn = getRootMeanNoteValue(synth);
				if (rmn < 0) continue;

				double rf = getRootFrequencyValue(synth);
				if (rf < 0) continue;

				setRootMIDINoteAndFrequency(synth, rmn, rf);
				realize(rmn, rf);
				setConfigured(true);
				return;
			}
	}
   
}
