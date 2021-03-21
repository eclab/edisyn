/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn;

import edisyn.gui.*;
import edisyn.synth.*;
import javax.swing.*;
//import com.apple.eawt.*;
//import com.apple.eawt.event.*;

/**** 
      Top-level launcher class.  For the moment, run as 
      java edisyn.Edisyn

      @author Sean Luke
*/

public class Edisyn 
{
    public static final int VERSION = 27;
    
    public static void main(String[] args)
    {
        try {
            System.setProperty("apple.awt.graphics.EnableQ2DX", "true");
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            
            // This no longer works as of Java 7
            //System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Edisyn");
            // This DOES work, but it's not necessary as the menu says "Edisyn" anyway
            // System.setProperty("apple.awt.application.name", "Edisyn");
            
            // This makes sure that windows uses the default windows look and feel, not the old Sun one
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch(Exception e) { }
        
        
        
        String lastSynth = Synth.getLastSynth();
        boolean showSynth = Synth.getLastXAsBoolean("ShowSynth", null, true, false);
        Synth synth = null;
        if (lastSynth != null && showSynth)
            {
                synth = Synth.instantiate(lastSynth, false, true, null);
            }
        if (synth == null)
            {
                if (Synth.doNewSynthPanel() == null)
                    {
                        System.exit(0);
                    }
            }
    }
}
