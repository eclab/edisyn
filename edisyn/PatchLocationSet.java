/***
    Copyright 2021 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn;
import edisyn.gui.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.dnd.*;
import javax.swing.*;
import javax.swing.table.*;

// This is the data that's transferred in a drag and drop operation from the Librarian
public class PatchLocationSet
    {
    public Synth synth;                    // The synth FROM WHICH the operation is being dragged
    public JTable table;                    // The table FROM WHICH the operation is being dragged
    public int column;                      // The column FROM WHICH the operation is being dragged
    public int row;                         // The start row FROM WHICH the operation is being dragged
    public int length;                      // The number of rows FROM WHICH the operation is being dragged
        
    public PatchLocationSet(Synth synth, JTable table, int column, int row, int length) 
        { 
        this.synth = synth;
        this.table = table; 
        this.column = column;
        this.row = row;
        this.length = length;
        }
    }


