package edisyn.synth.tuning.tuningdefinitions;

import edisyn.synth.tuning.*;
import edisyn.*;
import edisyn.gui.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

public class EDO extends TuningDefinition 
    {
    public void popup(Synth synth)
        {
        String name = synth.getSynthNameLocal();
        setConfigured(false);
                
        JComponent rootMIDINote = getRootMIDINoteComponent(synth);
        JComponent rootFrequency = getRootFrequencyComponent(synth);
        JTextField divisionsPerOctave = new JTextField("" + Synth.getLastXAsInt("EDODivisionsPerOctive", name, 12, true));
                
        while(true)
            {
            int res = Synth.showMultiOption(synth,
                new String[] {  "Root MIDI Note (0...127)", "Root Frequency", "Divisions Per Octave" },
                new JComponent[] { rootMIDINote, rootFrequency, divisionsPerOctave },
                new String[] { "Okay", "Cancel", "Reset" }, 
                0, "EDO Tuning", "Enter EDO Tuning Information.  MIDI note 69 is classically A-440.");
                        
            if (res == 2)  // reset
                {
                resetRootMIDINoteAndFrequency(synth);
                Synth.setLastX("" + 12, "EDODivisionsPerOctive", name, false);
                divisionsPerOctave.setText("" + 12);            // reset it
                continue;
                }
            else if (res == 1) // cancel
                {
                return;
                }
                        
            int rmn = getRootMeanNoteValue(synth);
            if (rmn < 0) continue;

            double rf = getRootFrequencyValue(synth);
            if (rf < 0) continue;

            int dpo = -1;
            try 
                { 
                dpo = Integer.parseInt(divisionsPerOctave.getText()); 
                if (dpo <= 0) throw new RuntimeException(); 
                }
            catch (Exception ex)
                {
                synth.showSimpleError("EDO Tuning", "Divisions must be >= 1"); 
                divisionsPerOctave.setText("" + Synth.getLastXAsDouble("EDODivisionsPerOctive", name, 12, true));
                continue;
                }

            setRootMIDINoteAndFrequency(synth, rmn, rf);
            Synth.setLastX("" + dpo, "EDODivisionsPerOctive", name, false);
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
            setNoteFrequency(i, edoNumberToHz(i, divisionsPerOctave, rootFrequency, rootMIDINote));
            }
        }

    public String getMenuName() { return "EDO"; }
    }
