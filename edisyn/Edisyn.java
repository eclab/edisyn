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
    public static final int VERSION = 17;
    
    public static void main(String[] args)
        {
        try {
            System.setProperty("apple.awt.graphics.EnableQ2DX", "true");
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            
            // This no longer works as of Java 7
            //System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Edisyn");
            // This DOES work, but it's not necessary as the menu says "Edisyn" anyway
            // System.setProperty("apple.awt.application.name", "Edisyn");
            
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        catch(Exception e) { }
        
        

        if (Synth.doNewSynthPanel() == null)
            System.exit(0);
        }
    }
