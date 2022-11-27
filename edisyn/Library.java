/***
    Copyright 2021 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn;
import javax.swing.*;
import javax.swing.table.*;
import java.util.*;
import java.io.*;
import java.awt.*;
import edisyn.util.*;
import edisyn.gui.*;

/**
   LIBRARY is the model for the librarian.  It contains a two-dimensional [bank][number] 
   array of Patches, each of which stores a single patch in the form of a series of sysex
   messages (rather than an Edisyn Model object).   All such arrays have at least two
   banks: the first "bank" is the *scratch bank* available to the musician to stick items
   temporarily while moving things around.  Banks can be writeable, and both banks and
   patch numbers have names which resemble the names used by the synthesier in question.
   The library has a backpointer to an existing Synth.  The Library also has full support
   for its own internal undo and redo.   
        
**/

public class Library extends AbstractTableModel
    {
    ArrayList<Patch[][]> undo = new ArrayList<>();
    ArrayList<Patch[][]> redo = new ArrayList<>();
    
    // The patches in the library by [bank][number]
    Patch[][] patches;                                      // Note that patches[0] is the "scratch bank"
    // The names of numbers along the left side of the library
    String[] numberNames;
    // The names of banks along the top of the library
    String[] bankNames;                                     // Note that bankNames[0] is the scratch bank name
    // Which banks are writeable
    boolean[] writeableBanks;                               // Note that writeableBanks[0] is true (for the scratch bank)
    // The names the user has set for banks along the top of the library.  If a user name is null, then the standard bankNames[] is used.
    String[] userNames;
    // The default "empty" patch
    private Patch initPatch;
    // The synth that owns this library
    Synth synth;
    // The synth number (the same thing as synth.getSynthNum())
    int synthNum;  
    // A counter which lets us provide unique patch names in the library slots when a synthesizer provides no patch names
    int nameCounter = 1;
        
    /** Builds a library model, including undo/redo, given a set of bank names,
        the size (length) of a bank, a default init patch, and a list of names
        representing numberNames of patches in bankNames (like 001 or whatnot). 
        The model will also contain one extra "bank" called "Scratch" which
        makes it easier to drag and drop.
    */
    public Library(Synth synth)
        {
        this.synth = synth;
        synthNum = synth.getSynthNum();                 // O(n) unfortunately

        // compute the init patch
        boolean originalMIDI = synth.getSendMIDI();
        synth.setSendMIDI(false);
        synth.undo.setWillPush(false);
        Model backup = (Model)(synth.model.clone());
        synth.loadDefaults();
        byte[][] data = synth.cutUpSysex(synth.flatten(synth.emitAll(synth.getModel(), false, true)));          // we're pretending we're writing to a file here
        initPatch = new Patch(synthNum, data, false);
        synth.setModel(backup);                                 // restore
        synth.undo.setWillPush(true);
        synth.setSendMIDI(originalMIDI);

        numberNames = synth.getPatchNumberNames();
        // generally this should not hapen as numberNames == null means no library support, but just to be on the safe side...
        if (numberNames == null) numberNames = new String[] { "Patch" };  
                
        // we need to add an additional writeable scratch bank to the front
        String[] b = synth.getBankNames();
        if (b == null) b = new String[] { "Bank" };
        bankNames = new String[b.length + 1];
        bankNames[0] = "<html><span color=gray>Scratch</span></html>";
        System.arraycopy(b, 0, bankNames, 1, b.length);
        boolean[] w = synth.getWriteableBanks();
        if (w == null) w = new boolean[] { false };
        writeableBanks = new boolean[w.length + 1];
        writeableBanks[0] = true;
        System.arraycopy(w, 0, writeableBanks, 1, w.length);
        userNames = new String[w.length + 1];
        
        // Note that at this point bankNames includes the scratch bankNames at front
        patches = new Patch[bankNames.length][this.numberNames.length];
        for(int i = 0; i < patches.length; i++)
            {
            for(int j = 0; j < patches[i].length; j++)
                {
                patches[i][j] = new Patch(initPatch);
                }
            }
        }
        
                        
                
                
                
    //// GETTING AND SETTING PATCHES AND BANK NAMES
        
    /** Returns whether the given bank is writeable.  To get the scratch bank, pass in SCRATCH_BANK.   */
    public boolean isWriteableBank(int bank)
        {
        return writeableBanks[bank + 1];
        }
                
    /** Returns the number of bankNames, not including the scratch bank.   */
    public int getNumBanks()
        {
        return bankNames.length - 1;
        }
                
    public int getBankSize()
        {
        return numberNames.length;
        }
                
    /** Returns the init patch */
    public Patch getInitPatch() 
        { 
        return initPatch; 
        }
    
    /** Returns a bank of patches.  To get the scratch bank, pass in SCRATCH_BANK.   */
    public Patch[] getBank(int bankNumber)
        {
        return patches[bankNumber + 1];                                                 // disregard the scratch bank
        }
                
    public static final int SCRATCH_BANK = -1;
    /** Returns a patch.  To get a scratch bank patch, pass in SCRATCH_BANK.  */
    public Patch getPatch(int bankNumber, int patchNumber)
        {
        return getBank(bankNumber)[patchNumber];
        }
    
    /** Sets the user name of the given bank. To get a scratch bank patch name, pass in SCRATCH_BANK. */
    public String setUserName(int bankNumber, String name)
        {
        return userNames[bankNumber + 1] = name;
        }

    /** Returns the user name of the given bank.  To get a scratch bank patch name, pass in SCRATCH_BANK.  Returns NULL by default. */
    public String getUserName(int bankNumber)
        {
        return userNames[bankNumber + 1];
        }

    /** Returns the name of the given bank.  To get a scratch bank patch name, pass in SCRATCH_BANK.   */
    public String getBankName(int bankNumber)
        {
        return bankNames[bankNumber + 1];
        }

    /** Returns all bank names, possibly including the scratch bank. */
    public String[] getBankNames(boolean includeScratchBank)
        {
        if (includeScratchBank) return bankNames;
        else
            {
            String[] b = new String[bankNames.length - 1];
            System.arraycopy(bankNames, 1, b, 0, b.length);
            return b;
            }
        }

    /** Return the names of the patch numberNames */
    public String[] getPatchNumberNames()
        {
        return numberNames;
        }

    /** Returns all patches, possibly including the scratch bank. */
    public Patch[][] getAllPatches(boolean includeScratchBank)
        {
        if (includeScratchBank) return patches;
        else
            {
            Patch[][] p = new Patch[patches.length - 1][];
            System.arraycopy(patches, 1, p, 0, p.length);
            return p;
            }
        }
                
    /** Places a patch in the library according to its current bank and number. */
    // protected because it should be called by other methods which do an undo/redo
    protected void setPatch(Patch patch)
        {
        patches[patch.bank + 1][patch.number] = patch;
        fireTableCellUpdated(patch.number, patch.bank + 1);
        }
        
    /** Places a bank of patch in the library at the given bank number.  To place the scratch bank, pass in SCRATCH_BANK. */
    // protected because it should be called by other methods which do an undo/redo
    protected void setBank(Patch[] bank, int bankNumber)
        {
        patches[bankNumber + 1] = bank;                                                 // disregard the scratch bank
        fireTableDataChanged();
        }
        
    /** Sets all patches, possibly including the scratch bank */
    // protected because it should be called by other methods which do an undo/redo
    protected void setAll(Patch[][] all, boolean includeScratch)
        {
        if (includeScratch) patches = all;
        else
            {
            for(int i = 0; i < all.length; i++)
                {
                patches[i + 1] = all[i];                                                // disregard the scratch bank
                }
            }
        fireTableDataChanged();
        }
        
    /** Returns the synth that owns this Library */
    public Synth getSynth() { return synth; }
    
    /** Returns the synth number of the synth that owns this library. Equivalent to getSynth().getSynthNum() but a bit faster (it's cached). */
    public int getSynthNum() { return synthNum; }      
                
                
                
    //// UNDO/REDO
        
    // Make a shallow copy of the patches
    // A shallow copy is okay because when we modify a patch we make a copy, modify it,
    // and replace the original.  [I hope]
    Patch[][] copy()
        {
        Patch[][] np = new Patch[patches.length][patches[0].length];
        for(int i = 0; i < np.length; i++)
            System.arraycopy(patches[i], 0, np[i], 0, patches[i].length);
        return np;
        }
          
    /** Copies the existing patches and pushes them onto the undo stack */
    public void pushUndo()
        {
        redo.clear();
        undo.add(copy());
        }

    /** Returns true if there is something to undo */
    public boolean hasUndo()
        {
        return undo.size() > 0;
        }

    /** Returns true if there is something to redo */
    boolean hasRedo()
        {
        return redo.size() > 0;
        }
                
    /** Performs an undo if it is possible */
    public void doUndo()
        {
        if (hasUndo())
            {
            redo.add(copy());
            patches = undo.remove(undo.size() - 1);
            fireTableDataChanged();
            }
        }

    /** Performs a redo if it is possible */
    void doRedo()
        {
        if (hasRedo())
            {
            undo.add(copy());
            patches = redo.remove(redo.size() - 1);
            fireTableDataChanged();
            }
        }
        
    // Maybe we don't need this one
    /** Clears the undo/redo stacks */
    void resetUndoRedo()
        {
        redo.clear();
        undo.clear();
        fireTableDataChanged();
        }
                
                
        
        
    ///// ABSTRACT TABLE MODEL METHOD JUNK

    
    public Class<?> getColumnClass(int columnIndex)
        {
        return Patch.class;
        }
                
    public int getColumnCount()
        {
        return bankNames.length;
        }
                
    public String getColumnName(int columnIndex)
        {
        if (userNames[columnIndex] != null)
            {
            return bankNames[columnIndex] + ": " + userNames[columnIndex];
            }
        else return bankNames[columnIndex];
        }

    public int getRowCount()
        {
        return numberNames.length;
        }

    public Object getValueAt(int rowIndex, int columnIndex)
        {
        return patches[columnIndex][rowIndex];
        }
                
    public void setValueAt(Object value, int rowIndex, int columnIndex)
        {
        patches[columnIndex][rowIndex] = (Patch) value;
        fireTableCellUpdated(rowIndex, columnIndex);
        }
        
    public boolean isCellEditable(int rowIndex, int columnIndex)
        {
        return false;
        }





    /////  RECEIVING
    
    /** Accepts a patch and places it in the appropriate slot by calling setPatch(incoming)  */
    public void receivePatch(Patch incoming)
        {
        pushUndo();
        incoming = new Patch(incoming);         // deep copy
        synth.updateNumberAndBank(incoming);
        setPatch(incoming);
        }

    /** Accepts a collection of patches and adds them to the library.  Attempts to place
        the patches in the slots specified by their banks and numbers, but if there are
        other patches in the same batch with the same bank and number, then places them
        somewhere else. */
    public void receivePatches(Patch[] incoming)
        {
        // These patches could be null because they've already been processed earlier as banks
        // and this sets the patch to null.  So let's check to see how many there are.
        int count = 0;
        for(int i = 0; i < incoming.length; i++)
            {
            if (incoming[i] != null) count++;
            }
        if (count == 0) return;
        
        pushUndo();

        // deep copy and reduce
        int pos = 0;
        Patch[] in = new Patch[count];
        for(int i = 0; i < incoming.length; i++)
            {
            if (incoming[i] != null)
                {
                in[pos++] = new Patch(incoming[i]);
                }
            }
        incoming = in;
        
        int len = incoming.length;
        int maxlen = getNumBanks() * getBankSize();
        if (len > maxlen)
            {
            synth.showSimpleError("Too Many Patches", "This file has more patches than can be loaded into the library.\nSome patches may not be loaded.");
            len = maxlen;
            }
        
        // preprocess
        for(int i = 0; i < incoming.length; i++)
        	{
	        synth.updateNumberAndBank(incoming[i]);
        	}
        
        // Break out the known and unknown patches
        ArrayList<Patch> unknown = new ArrayList<Patch>();
        Patch[][] patch = new Patch[getNumBanks()][getBankSize()];
        
        for(int i = 0; i < len; i++)
            {
            /// NOTE: in fact incoming patches from a file all will have their numbers set,
            /// even if just to zero, because they've been parsed into models first.  :-(  But I think
            /// that's okay?
            if (incoming[i].number == Patch.NUMBER_NOT_SET || 
                patch[incoming[i].bank][incoming[i].number] != null)            // someone is already there
                {
                unknown.add(incoming[i]);
                }
            else
                {
                patch[incoming[i].bank][incoming[i].number] = incoming[i];
                }
            }
        
        // Fill in the empty spaces
        int b = 0;
        int n = 0;
        for(Patch p : unknown)
            {
            while(true)
                {
                if (patch[b][n] == null)
                    {
                    patch[b][n] = p;
                    p.number = n;
                    p.bank = b;
                    break;
                    }
                else
                    {
                    n++;
                    if (n >= getBankSize())
                        {
                        n = 0;
                        b++;
                        if (b >= getNumBanks())         // uh....
                            break;
                        }
                    }
                }
                                
            if (b >= getNumBanks())     // uh....
                {
                break;
                }
            }
                        
        // Put in positions
        for(b = 0; b < patch.length; b++)
            {
            for(n = 0; n < patch[0].length; n++)
                {
                if (patch[b][n] != null)
                    {
                    // fix name if it's null
                    if (patch[b][n].name == null)
                        {
                        patch[b][n].name = "" + (getNumBanks() == 1 ? "" : (getBankName(b) + "-")) + getPatchNumberNames()[n] + " (" + (nameCounter++) + ")";
                        }
                    setPatch(patch[b][n]);
                    }
                }
            }
        }
                
    /** Reads a sysex bank message holding one or (rarely) more than one bank, 
        and loads the patches in that bank or those banks
    */
    public void readBanks(byte[] incoming, Librarian librarian)
        {
        pushUndo();

        // backup first
        boolean originalMIDI = synth.getSendMIDI();
        synth.setSendMIDI(false);
        boolean originalPrintRevised = synth.printRevised;
        synth.printRevised = false;
        boolean originalGetWillPush = synth.getUndo().getWillPush();
        synth.getUndo().setWillPush(false);
        Model original = synth.getModel();
        synth.setModel(original.copy());
                
        Model location = new Model();
        boolean hasBanks = (synth.getBankNames() != null);
        
        // We have to figure out what banks to use
        int[] banks = synth.getBanks(incoming);
        if (banks == null)     // banks unknown
            {
            banks = new int[] { 0 };
            }
        
        synth.getModel().setUpdateListeners(false);
        
        boolean showed = false;
        for(int b = 0; b < banks.length ; b++)
            {
            int bank = banks[b]; 
            for(int i = 0; i < getBankSize(); i++)
                {
                int result = synth.parseFromBank(incoming, i + b * getBankSize());
                if (result == Synth.PARSE_FAILED)
                    {
                    if (!showed)
                        {
                        synth.showSimpleError("Bad Patches in Bank", "This file has a bank with bad patches.  They will be replaced with blanks.");
                        showed = true;
                        }
                    patches[bank + 1][i] = new Patch(initPatch);
                    }
                else
                    {
                    // force the location to be correct
                    if (hasBanks)
                        {
                        location.set("bank", bank);
                        }
                    location.set("number", i);
                    String name = synth.getModel().get("name","" + synth.getPatchLocationName(synth.getModel()));
                    //byte[][] data = synth.cutUpSysex(synth.flatten(synth.emitAll(location, false, true)));          // I guess to a file so it doesn't try NRPN?
                    byte[][] data = synth.extractSysex(synth.emitAll(location, false, true));          // I guess to a file so it doesn't try NRPN?
                    Patch patch = new Patch(synthNum, data, false);
                    patch.name = name;
                    patch.bank = bank;
                    patch.number = i;
                    patches[bank + 1][i] = patch;
                    }
                }
            }
        
        // restore synth
        synth.setModel(original);
        synth.setSendMIDI(originalMIDI);
        synth.printRevised = originalPrintRevised;
        synth.getUndo().setWillPush(originalGetWillPush);
        synth.getModel().setUpdateListeners(true);
        }





    /////  EMITTING

    // A utility function: flattens the separate data[] elements into one large array
    public static final int ALL_PATCHES = -1;
        
    // A utility function: flattens the separate data[] elements into one large array
    Object[] concatenate(Object[][] data)
        {
        int size = 0;
        for(int i = 0; i < data.length; i++)
            size += data[i].length;
        Object[] result = new Object[size];
        int pos = 0;
        for(int i = 0; i < data.length; i++)
            {
            System.arraycopy(data[i], 0, result, pos, data[i].length);
            pos += data[i].length;
            }
        return result;
        }

    // Emits all the patches in the synthesizer, using bank-write functions only.
    Object[][] emitAllAsBankSysex(boolean toFile)
        {
        // This function only works if we support bank writes.  We should emit an error here.
        if (!synth.getSupportsBankWrites())
            {
            System.err.println("Library.emitAllAsBankSysex Error: emitBank() called on synthesizer " + synth.getSynthClassName() + ", which does not support bank sysex writes.");
            return new Object[0][0];
            }

        Object[][] data = new Object[getNumBanks()][];
        for(int i = 0; i < data.length; i++)
            data[i] = emitBank(i, toFile);
        return data;
        }



    // Emits all the patches in the given bank, using bank-write functions. 
    // The synthesizer MUST be able to support bank-write sysex
    // via synth.emitBank(...).  This is checked by calling
    // getSupportsBankWrites(), which must return true.
    // If bank == ALL_PATCHES, then emits the entire library, but note that
    // the library is emitted as one flattened Object[] rather than an Object[][],
    // which you want.  In this case you should call emitAllAsBankSysex(...) instead.
    Object[] emitBank(int bank, boolean toFile)
        {
        // This function only works if we support bank writes.  We should emit an error here.
        if (!synth.getSupportsBankWrites())
            {
            System.err.println("Library.emitBank Error: emitBank() called on synthesizer " + synth.getSynthClassName() + ", which does not support bank sysex writes.");
            return new Object[0];
            }

        // If we want to emit the entire synth, we need to gather it.  We do this by calling
        // ourselves recursivesly on each bank and concatenating the result.
        else if (bank == ALL_PATCHES)
            {
            return concatenate(emitAllAsBankSysex(toFile));
            }
        else
            {
            // If we have nothing to emit, and we're trying to write to the synth, we return empty
            if (!isWriteableBank(bank) && !toFile) 
                {
                return new Object[0];
                }
                        
            int validBankSize = synth.getValidBankSize(bank);
            if (validBankSize == -1) validBankSize = getBankSize();
            Model[] patches = new Model[validBankSize];
                                
            // backup
            boolean originalMIDI = synth.getSendMIDI();
            synth.setSendMIDI(false);
            boolean originalPrintRevised = synth.printRevised;
            synth.printRevised = false;
            boolean originalGetWillPush = synth.getUndo().getWillPush();
            synth.getUndo().setWillPush(false);
            Model original = synth.getModel();
            boolean hasBanks = (synth.getBankNames() != null);
                                
            synth.getModel().setUpdateListeners(false);

            boolean failed = false;
            for(int i = 0; i < getBankSize(); i++)
                {             
                if (!synth.isValidPatchLocation(bank, i)) continue;
                                  
                synth.setModel(original.copy());

                // parse
                boolean localFailed = false;
                Patch p = getPatch(bank, i);
                if (p == null) p = initPatch;
                for(int j = 0; j < p.sysex.length; j++)
                    {
                    int result = synth.parse(p.sysex[j], true);                     //dunno, from file or not?
                    if (result == Synth.PARSE_CANCELLED ||
                        result == Synth.PARSE_FAILED ||
                        (result == Synth.PARSE_INCOMPLETE && j == p.sysex.length - 1))  // last one
                        {
                        failed = true;
                        localFailed = true;
                        break;
                        }

                    if (hasBanks)
                        {
                        synth.getModel().set("bank", bank);
                        }
                    synth.getModel().set("number", i);
                    }
                                        
                if (localFailed) continue;
                                        
                patches[i] = synth.getModel();
                }
                    
            // At this point we've gathered all the patches to form the bank.                                                                
            // Now emit them as one bank sysex
            Object[] data = synth.emitBank(patches, bank, toFile);
            if (!toFile)
                {
                int pause = synth.getPauseAfterWriteBank();
                if (pause > 0) 
                    {
                    // ugh
                    Object[] data2 = new Object[data.length + 1];
                    System.arraycopy(data, 0, data2, 0, data.length);
                    data2[data2.length - 1] = Integer.valueOf(pause);
                    data = data2;
                    }
                }
                                        
            // restore
            synth.setModel(original);
            synth.setSendMIDI(originalMIDI);
            synth.printRevised = originalPrintRevised;
            synth.getUndo().setWillPush(originalGetWillPush);
            synth.getModel().setUpdateListeners(true);
                
            // should I do a change patch?
            if (failed) synth.showSimpleError("Incomplete Patch Write", "Some patches from bank " + getBankName(bank) + " were malformed and could not be written.");
            return data;
            }   
        }
        
        
    // Emits MIDI representing all the patches from start to start+len-1 in bank.
    // If bank == ALL_PATCHES, then emits the entire library.
    // If bank == ALL_PATCHES, and the synthesizer is capable of emitting bank-patch dumps, 
    // and forceIndependent is FALSE, then the patches emitted will be grouped into bank-patch 
    // sysex dumps.  Otherwise, the emitted patches will be independent patch dumps.
    // If emitRange is planning on emitting separate patches (because forceIndependent is true, 
    // or the synth does not support bank writes, or because the bank is not ALL_PATCHES), then
    // the synth MUST be capable of writing independent patches, which is checked for via
    // synth.getSupportsPatchWrites()

    Object[][] emitRange(int bank, int start, int len, boolean toFile, boolean forceIndependent)
        {
        ArrayList<Object[]> data = new ArrayList();
        
        if (bank == ALL_PATCHES && synth.getSupportsBankWrites() && !forceIndependent)
            {
            return emitAllAsBankSysex(toFile);
            }

        // backup
        boolean originalMIDI = synth.getSendMIDI();
        synth.setSendMIDI(false);
        boolean originalPrintRevised = synth.printRevised;
        synth.printRevised = false;
        boolean originalGetWillPush = synth.getUndo().getWillPush();
        synth.getUndo().setWillPush(false);
        Model original = synth.getModel();
        synth.setModel(original.copy());
                 
        Model location = new Model();
        boolean hasBanks = (synth.getBankNames() != null);
                
        // send patches
        if (bank == ALL_PATCHES)
            {
            start = 0;
            len = getNumBanks() * getBankSize();                        // if we have ragged banks, this won't be the full size
            }
            
        synth.getModel().setUpdateListeners(false);
        boolean failed = false;
        String failures = "";
        for(int i = start; i < start + len; i++)
            {
            int b = (bank == ALL_PATCHES ? i / getBankSize() : bank);
            int n = (bank == ALL_PATCHES ? i % getBankSize() : i);
            
            if (synth.isValidPatchLocation(b, n) &&               // only write if it's a real spot (remember we can be ragged)
                    (isWriteableBank(b) ||                                                    // write only if it's writeable OR
                    (toFile && (!saveAll || synth.isAppropriatePatchLocation(b, n)))))   // write to files but only if we're not saving everything or if the patch is "appropriate"
                {
                // parse
                boolean localFailed = false;
                Patch p = getPatch(b, n);
                if (p == null) p = initPatch;
                byte[][] sysex = p.sysex;
                for(int j = 0; j < sysex.length; j++)
                    {
                    int result = synth.parse(sysex[j], true);                     //dunno, from file or not?
                    if (result == Synth.PARSE_CANCELLED ||
                        result == Synth.PARSE_FAILED ||
                        (result == Synth.PARSE_INCOMPLETE && j == p.sysex.length - 1))  // last one
                        {
                        failed = true;
                        failures = failures + p.location + "    " + p.name + "<br>";
                        localFailed = true;
                        break;
                        }
                    }
                                                                                
                if (localFailed) continue;
                                                                                                                                                                
                if (hasBanks)
                    {
                    location.set("bank", b);
                    }
                location.set("number", n);

                // now emit
                Object[] objs = synth.emitAll(location, false, toFile);

                if (!toFile)
                    {
                    int pause = synth.getPauseAfterWritePatch();
                    System.err.println("PAUSE " + pause);
                    if (pause > 0) 
                        {
                        Object[] newObjs = new Object[objs.length + 1];
                        System.arraycopy(objs, 0, newObjs, 0, objs.length);
                        newObjs[newObjs.length - 1] = Integer.valueOf(pause);
                        objs = newObjs;
                        }
                    }
                data.add(objs);                    
                }
            }
                
        // restore
        synth.setModel(original);
        synth.setSendMIDI(originalMIDI);
        synth.printRevised = originalPrintRevised;
        synth.getUndo().setWillPush(originalGetWillPush);
        synth.getModel().setUpdateListeners(true);
            
        // should I do a change patch?
        if (failed) 
            synth.showSimpleError(synth, "Incomplete Patch Write", "Some patches were malformed and could not be written.", new JLabel("<html>" + failures + "</html>"));
 
        return data.toArray(new Object[0][0]);
        }

    public static final int MINIMUM_PATCHES_FOR_PROGRESS_DIALOG = 2;

    /** Writes to the synthesizer all the patches from start to start+len-1 in bank.
        If bank == ALL_PATCHES, then writes the entire library, possibly using bank sysex. */
    public void writeRange(int bank, int start, int len)
        {
        if (synth.tuple == null || synth.tuple.outReceiver == null)
            {
            if (!synth.setupMIDI())
                return;
            }
                
        Librarian librarian = synth.librarian;
        
        // We can write all patches using bank writes
        if (bank == ALL_PATCHES && synth.getSupportsBankWrites())
            {
            writeBank(bank);
            }
        // Else we write ANYTHING using patch writes if we can
        else if (synth.getSupportsPatchWrites())
            {
            if (bank != ALL_PATCHES && !isWriteableBank(bank))
                {
                synth.showSimpleError("Not Supported", "This bank cannot be written to the synthesizer.\nIt is read-only.");
                return;
                }
                                                                        
            Object[][] data = emitRange(bank, start, len, false, false);
            if (data != null)
                {
                if (synth.tuple == null || synth.tuple.outReceiver == null)
                    {
                    if (!synth.setupMIDI())
                        {
                        return;
                        }                               
                    }
                if (bank == ALL_PATCHES)
                    {
                    synth.tryToSendMIDI(concatenate(data), "Writing", "Writing All Patches to Synth");
                    }
                else if (len < MINIMUM_PATCHES_FOR_PROGRESS_DIALOG)
                    {
                    synth.tryToSendMIDI(concatenate(data));
                    }
                else
                    {
                    synth.tryToSendMIDI(concatenate(data), "Writing", "Writing Patches to Synth");
                    }
                synth.sendAllParameters();
                }
            }           
        else
            {
            synth.showSimpleError("Not Supported", "Edisyn cannot write arbitrary patches to this synthesizer.");
            }
        }


    /** Prompts the user to change the name of a patch at a given location.  */
    public void changeName(int bank, int start)
        {
        Patch patch = getPatch(bank, start);
        if (patch == null) patch = initPatch;
        String name = patch.getName();
        if (name == null) // uhm....?
            {
            synth.showSimpleError("Empty Patch", "Edisyn cannot change the name of an empty patch.");
            }
        else
            {
            Model model = getModel(bank, start);
            if (model.get("name", (String) null) == null)
                {
                synth.showSimpleError("Not Supported", "Edisyn cannot change the names of patches for this synthesizer.");
                }
            else
                {
                // Load model
                synth.getUndo().push(synth.getModel());
                synth.undo.setWillPush(false);
                boolean send = synth.getSendMIDI();
                synth.setSendMIDI(false);
                model.copyValuesTo(synth.model);
                                
                // Find the StringComponent
                boolean foundit = false;
                ArrayList listeners = synth.model.getListeners("name");
                for(int i = 0; i < listeners.size(); i++)
                    {
                    Object obj = listeners.get(i);
                    if (obj instanceof StringComponent) // hope this is it
                        {
                        foundit = true;
                        StringComponent sc = (StringComponent)obj;
                        sc.perform(synth);
                        synth.revise();
                        name = synth.model.get("name", "");
                        model.set("name", name);
                                                
                        // Now put back
                        pushUndo();
                        patch = getPatch(model);
                        // the model will have a different patch number
                        patch.bank = bank;
                        patch.number = start;
                        setPatch(patch);
                        break;
                        }
                    }
                                        
                if (!foundit)
                    {
                    synth.showSimpleError("Not Supported", "Edisyn cannot change the names of patches for this synthesizer.");
                    }
                        
                // Unload model
                synth.undo.setWillPush(true);
                synth.getUndo().undo(null);
                synth.setSendMIDI(send);
                }
            }
        }

	/** Saves a single patch */
	public void saveOne(int bank, int number, Object[] d, String fileName)
		{
		fileName = StringUtility.ensureFileEndsWith(fileName, ".syx");
		byte[] data = Synth.flatten(d);
		FileDialog fd = new FileDialog((Frame)(SwingUtilities.getRoot(synth)), "Save to Sysex File...", FileDialog.SAVE);

		fd.setFile(fileName);
		String path = synth.getLastDirectory();
		if (path != null)
			fd.setDirectory(path);
																																																																	
		synth.disableMenuBar();
		fd.setVisible(true);
		synth.enableMenuBar();
	
		File f = null; // make compiler happy
		FileOutputStream os = null;
		if (fd.getFile() != null)
			{
			synth.setLastDirectory(fd.getDirectory());
			File patchFileOrDirectory = new File(fd.getDirectory(), StringUtility.ensureFileEndsWith(fd.getFile(), ".syx"));
			FileOutputStream patch = null;
			try 
				{
				patch = new FileOutputStream(patchFileOrDirectory);
				patch.write(data);
				patch.close();
				} 
			catch (IOException e) // fail
				{
				synth.showErrorWithStackTrace(e, "File Error", "An error occurred while saving to bulk file " + (patchFileOrDirectory == null ? " " : patchFileOrDirectory.getName()));
				Synth.handleException(e);
				if (patch != null)
					try { patch.close(); }
					catch (IOException ex) { }
				}
			}
		else return;
		}

    /** Saves to disk all the patches from start to start+len-1 in bank.
        If bank == ALL_PATCHES, then writes the entire library; in this
        case, if the patches can be saved using bank sysex, the user is
        asked whether he'd prefer this.  */
    public void saveRange(int bank, int start, int len)
        {
        if (bank != ALL_PATCHES && len == 1)
        	{
            saveRangeSeparately(bank, start, len, true);
        	}    	
        else
        	{
			JComboBox combo = null;
			JComponent[] comp = new JComponent[0];
			String[] str = new String[0];

			String title = "Save Patches";
			String query = "Save the Patches...";
		
			// do we have to choose between bank and patch writes?
			// I commented things out because I only want the user to choose banks vs. patches
			// if he's saving everything.
			if (synth.getSupportsBankWrites() /* && synth.getSupportsPatchWrites()*/ &&
				(/* (start == 0 && len == getBankSize()) || */ bank == ALL_PATCHES))
				{
				combo = new JComboBox(new String[] { "Bank Sysex", "Individual Patch Sysex" });
				comp = new JComponent[] { combo };
				str = new String[] { "Format" };
				title = "Save All Patches";
				query = "Save all Patches...";
				}
			/*
			  else if (bank == ALL_PATCHES && synth.getSupportsBankWrites())
			  {
			  // we're gonna attempt bank writes for the whole synth so we should make that clear to the user
			  title = "Save All Banks";
			  query = "Save the Banks...";
			  }
			*/
			else if (bank == ALL_PATCHES)
				{
				// we're gonna attempt individual patch writes for the whole synth so we should make that clear to the user
				title = "Save All Patches";
				query = "Save All Patches...";
				}

			int result = Synth.showMultiOption(synth, str, comp, 
				new String[] { "As Separate Files", "To Bulk File", "Cancel" },
				0, title, query);
						
			saveAll = true;
			try 
				{
				if (result == 2 || result == -1) return;
				else if (result == 1)                   // To Bulk File
					{
					Object[][] d = emitRange(bank, start, len, true, (combo != null && combo.getSelectedIndex() == 1));

					if (d != null)
						{
						byte[] data = Synth.flatten(concatenate(d));

						FileDialog fd = new FileDialog((Frame)(SwingUtilities.getRoot(synth)), "Save to Bulk Sysex File...", FileDialog.SAVE);

						fd.setFile(StringUtility.reviseFileName(synth.getSynthNameLocal() + ".bulk.syx"));
						String path = synth.getLastDirectory();
						if (path != null)
							fd.setDirectory(path);
																																																																				
						synth.disableMenuBar();
						fd.setVisible(true);
						synth.enableMenuBar();
				
						File f = null; // make compiler happy
						FileOutputStream os = null;
						if (fd.getFile() != null)
							{
							synth.setLastDirectory(fd.getDirectory());
							File patchFileOrDirectory = new File(fd.getDirectory(), StringUtility.ensureFileEndsWith(fd.getFile(), ".syx"));
							FileOutputStream batchPatches = null;
							try 
								{
								batchPatches = new FileOutputStream(patchFileOrDirectory);
								batchPatches.write(data);
								batchPatches.close();
								} 
							catch (IOException e) // fail
								{
								synth.showErrorWithStackTrace(e, "File Error", "An error occurred while saving to the bulk file " + (patchFileOrDirectory == null ? " " : patchFileOrDirectory.getName()));
								Synth.handleException(e);
								if (batchPatches != null)
									try { batchPatches.close(); }
									catch (IOException ex) { }
								}
							}
						else return;
						}
					}
				else            // As Separate Files
					{
					saveRangeSeparately(bank, start, len, (combo == null || combo.getSelectedIndex() == 1));
					}
				}
			finally { saveAll = false; }
			}
        }
        

    /** Saves to disk all the patches from start to start+len-1 in bank.
        If bank == ALL_PATCHES, then writes the entire library.
        If the synth is capable of emitting bank-patch sysex, and forceIndependent is FALSE,
        then the individual files will be bank sysex.  Otherwise the individual files
        will be independent patches. */
    void saveRangeSeparately(int bank, int start, int len, boolean forceIndependent)
        {
        Object[][] d = emitRange(bank, start, len, true, forceIndependent);
        int offset = 0;

        if (d != null)
            {
            if (len == 1)
            	{
				Patch patch = getPatch(bank, start);
				if (patch == null) patch = initPatch;
				String filename = ((getNumBanks() > 1 ? (getBankName(bank) + ".") : "") + 
					getPatchNumberNames()[start] + "." + 
					(patch.name == null ? "Untitled" : patch.name.trim()));
				filename = StringUtility.makeValidFilename(filename);
            	saveOne(bank, start, d[0], filename);
            	}
            else
            	{
				File dir = synth.selectDirectory(Style.isMac() ? "Select a Directory to Save Patches..." : "Select Directory to Save Patches",
					synth.getFile() == null ? null : new File(synth.getFile().getParentFile().getPath()));
								
				if (dir != null)
					{
					synth.setLastDirectory(dir.getParent());
					if (bank == ALL_PATCHES)
						{
						start = 0;
						len = getBankSize();
						}
								
					// Basic form: single bank, range within the bank
					int startbank = bank;
					int endbank = bank + 1;
					int startnum = start;
					int endnum = start + len;
								
					if (bank == ALL_PATCHES)
						{
						if (synth.getSupportsBankWrites() && !forceIndependent)
							{
							// All patches, thus all banks but since we're saving per-bank we only do the first "number"
							startbank = 0;
							endbank = getNumBanks();
							startnum = 0;
							endnum = 1;
							}
						else
							{
							// All patches, thus all banks and all numbers
							startbank = 0;
							endbank = getNumBanks();
							startnum = 0;
							endnum = getBankSize();
							}
						}
					else
						{
						if (synth.getSupportsBankWrites() &&                                        // we can write banks
							!forceIndependent &&                                                    // we have the option of writing banks (which we will take)
							start == 0 && len == getBankSize())                                     // Our range is one entire bank
							{
							// Single bank as before, but since we're saving per-bank we only do the first "number"
							startnum = 0;
							endnum = 1;
							}
						else
							{
							// do the default
							}
						}

					for(int b = startbank; b < endbank; b++)
						{
						for(int i = startnum; i < endnum; i++)
							{
							FileOutputStream os = null;
							Patch patch = getPatch(b, i);
							if (patch == null) patch = initPatch;
							String filename = ((getNumBanks() > 1 ? (getBankName(b) + ".") : "") + 
								getPatchNumberNames()[i] + "." + 
								(patch.name == null ? "Untitled" : patch.name.trim()));
							if (synth.getSupportsBankWrites() && !forceIndependent)
								filename = (getNumBanks() > 1 ? getBankName(b) : "Bank");
							filename = StringUtility.makeValidFilename(filename);
							File f = new File(dir, filename + ".syx");

							try
								{
								os = new FileOutputStream(f);
								os.write(Synth.flatten(d[offset++]));
								os.close();
								}
							catch (IOException e) // fail
								{
								synth.showErrorWithStackTrace(e, "Patch Save Error.", "An error occurred while saving to the file " + (f == null ? " " : f.getName()));
								Synth.handleException(e);
								}
							finally
								{
								if (os != null)
									try { os.close(); }
									catch (IOException e) { }
								}
							}
						}
					}
				}
			}
		}


    // Indicates that we are presently saving *all* patches
    boolean saveAll = false;
        
    /** Writes to the synthesizer all the patches in the given bank,
        either using bank-write functions or individual patch writing functions. 
        If bank == ALL_PATCHES, then writes the entire library. */
    public void writeBank(int bank)
        {
        try
            {
            if (synth.getSupportsBankWrites())
                {
                if (bank != ALL_PATCHES && !isWriteableBank(bank))
                    {
                    synth.showSimpleError("Not Supported", "This bank cannot be written to the synthesizer.");
                    return;
                    }
                                                                                                                                                
                Object[] data = null;
                saveAll = (bank == ALL_PATCHES);
                data = emitBank(bank, false);
                if (data != null)
                    {
                    if (synth.tuple == null || synth.tuple.outReceiver == null)
                        {
                        if (!synth.setupMIDI())
                            return;
                        }
                    if (bank == ALL_PATCHES)
                        {
                        synth.tryToSendMIDI(data, "Writing", "Writing All Patches to Synth");
                        }
                    else
                        {
                        synth.tryToSendMIDI(data, "Writing", "Writing Bank to Synth");
                        }
                    synth.sendAllParameters();
                    }
                }
            else if (synth.getSupportsPatchWrites())
                {
                saveAll = (bank == ALL_PATCHES);
                int bankSize = synth.getValidBankSize(bank);
                if (bankSize == -1) bankSize = getBankSize();
                writeRange(bank, 0, bankSize);
                }
            else
                {
                // this should never happen
                synth.showSimpleError("Not Supported", "Edisyn cannot write banks to this synthesizer.");
                }
            }
        finally { saveAll = false; }
        }


    /** Saves to disk all the patches in the given bank,
        either using bank-write functions or individual patch writing functions. 
        If bank == ALL_PATCHES, then writes the entire library. */
    public void saveBank(int bank)
        {
        if (synth.getSupportsBankWrites())
            {
            Object[] d = emitBank(bank, true);

            if (d != null)
                {
                byte[] data = Synth.flatten(d);

                FileDialog fd = new FileDialog((Frame)(SwingUtilities.getRoot(synth)), "Save to Bulk Sysex File...", FileDialog.SAVE);

                fd.setFile(StringUtility.reviseFileName(bank == ALL_PATCHES ? 
                        synth.getSynthNameLocal() + ".bulk.syx" : 
                        synth.getSynthNameLocal() + "." + getBankName(bank) + ".syx"));
                String path = synth.getLastDirectory();
                if (path != null)
                    fd.setDirectory(path);
                                                                                                                                        
                synth.disableMenuBar();
                fd.setVisible(true);
                synth.enableMenuBar();
        
                File f = null; // make compiler happy
                FileOutputStream os = null;
                if (fd.getFile() != null)
                    {
                    synth.setLastDirectory(fd.getDirectory());
                    File patchFileOrDirectory = new File(fd.getDirectory(), StringUtility.ensureFileEndsWith(fd.getFile(), ".syx"));
                    FileOutputStream batchPatches = null;
                    try 
                        {
                        batchPatches = new FileOutputStream(patchFileOrDirectory);
                        batchPatches.write(data);
                        batchPatches.close();
                        } 
                    catch (IOException e) // fail
                        {
                        synth.showErrorWithStackTrace(e, "File Error", "An error occurred while saving to the bulk file " + (patchFileOrDirectory == null ? " " : patchFileOrDirectory.getName()));
                        Synth.handleException(e);
                        if (batchPatches != null)
                            try { batchPatches.close(); }
                            catch (IOException ex) { }
                        }
                    }
                else return;
                }
            }
        else if (synth.getSupportsPatchWrites())
            {
            int bankSize = synth.getValidBankSize(bank);
            if (bankSize == -1) bankSize = getBankSize();
            saveRange(bank, 0, bankSize);
            }
        else
            {
            // this should never happen
            synth.showSimpleError("Not Supported", "Edisyn cannot save banks of this type to a file.");
            }
        }    


    /** A Utility method.  Given a model, produces a Patch filled out with model data. */
    public Patch getPatch(Model model)
        {
        Synth synth = getSynth();
        int synthNum = getSynthNum();
        String name = model.get("name","" + synth.getPatchLocationName(synth.getModel()));
        int number = model.get("number", -1);
        int bank = model.get("bank", -1);
        byte[][] data = synth.cutUpSysex(synth.flatten(synth.emitAll(model, false, true)));                  // we're pretending we're writing to a file here
        Patch patch = new Patch(synthNum, data, false);
        patch.name = name;
        patch.bank = (bank == -1 ? 0 : bank);
        patch.number = (number == -1 ? Patch.NUMBER_NOT_SET : number);          // FIXME: should I use NUMBER_NOT_SET?
        return patch;
        }
                
    /** Generates a model from the given patch slot.
        If number < 0, then an init patch is generated
        If bank is < 0, then the scratch bank is assumed
    */
    public Model getModel(int bank, int number)
        {
        Patch patch = null;
                
        if (number < 0) 
            {
            patch = new Patch(getInitPatch());              // Make a copy
            }
        else
            {
            patch = getPatch(bank, number);
            if (patch == null)
                {
                patch = new Patch(getInitPatch());              // Make a copy
                }
            }
        return getModel(patch, bank, number);
        }
                        
    /** Generates a model from the Patch, with the given bank and number */
    public Model getModel(Patch patch, int bank, int number)
        {                        
        Synth synth = getSynth();
                 
        // do we need to modify the bank and number?
        synth.undo.setWillPush(false);
        boolean send = synth.getSendMIDI();
        synth.setSendMIDI(false);
        boolean shouldUpdate = synth.model.getUpdateListeners();
        synth.model.setUpdateListeners(false);
        Model backup = (Model)(synth.model.clone());
        synth.performParse(synth.flatten(patch.sysex), false);  // is this from a file?  I'm saying false
                                
        // revise the patch location to where it came from in the librarian
        if (patch.number != Patch.NUMBER_NOT_SET)
            {
            synth.getModel().set("number", patch.number);
            int b = synth.getModel().get("bank", -1);
            if (b != -1 && patch.bank >= 0)
                synth.getModel().set("bank", patch.bank);
            }
        synth.setSendMIDI(send);
        synth.undo.setWillPush(true);

        Model retval = synth.model;
        synth.model = backup;
        synth.model.setUpdateListeners(shouldUpdate);
        return retval;
        }


    }
        
        
