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
    
    Patch[][] patches;                                      // Note that patches[0] is the "scratch bank"
    String[] numberNames;
    String[] bankNames;                                     // Note that bankNames[0] is the scratch bank name
    boolean[] writeableBanks;                               // Note that writeableBanks[0] is true (for the scratch bank)
    String[] userNames;
    private Patch initPatch;
    Synth synth;
    int synthNum;  
    
    int nameCounter = 1;                                                                                     // integer referring to the synth class from Synth.getSynthNum()
        
    /** Builds a library model, including undo/redo, given a set of bank names,
        the size (length) of a bank, a default init patch, and a list of names
        representing numberNames of patches in bankNames (like 001 or whatnot). 
        The model will also contain one extra "bank" called "[Scratch]" which
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
        byte[][] data = synth.cutUpSysex(synth.flatten(synth.emitAll(synth.getModel(), false, true)));
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
        bankNames[0] = "<html><span color=gray>[Scratch]</span></html>";
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
        
          
    public Synth getSynth() { return synth; }
    public int getSynthNum() { return synthNum; }      
                
                
                
    //// UNDO/REDO
        
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
            redo.add(patches);
            patches = undo.remove(undo.size() - 1);
            fireTableDataChanged();
            }
        }

    /** Performs a redo if it is possible */
    void doRedo()
        {
        if (hasRedo())
            {
            undo.add(patches);
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
        
    public void receivePatch(Patch incoming)
        {
        pushUndo();
        incoming = new Patch(incoming);         // deep copy
        // synth.updateNumberAndBank(incoming);
        setPatch(incoming);
        }


    public void receivePatch(Patch incoming, int bank, int number)
        {
        pushUndo();
        incoming = new Patch(incoming);         // deep copy
        // synth.setNumberAndBank(incoming, number, bank);
        setPatch(incoming);
        }

    public void receiveBank(Patch[] incoming, int bank)
        {
        pushUndo();

        // deep copy
        Patch[] in = new Patch[incoming.length];
        for(int i = 0; i < in.length; i++)
            in[i] = new Patch(incoming[i]);
        incoming = in;

        for(int i = 0; i < incoming.length; i++)
            {
            // synth.updateNumberAndBank(incoming[i]);
            // synth.setNumberAndBank(incoming[i], incoming[i].number, bank);
            setPatch(incoming[i]);
            }
        }

    public void receivePatches(Patch[] incoming)
        {
        pushUndo();
        
        // deep copy
        Patch[] in = new Patch[incoming.length];
        for(int i = 0; i < in.length; i++)
            in[i] = new Patch(incoming[i]);
        incoming = in;
        
        int len = incoming.length;
        int maxlen = getNumBanks() * getBankSize();
        if (len > maxlen)
            {
            synth.showSimpleError("Too Many Patches", "This file has more patches than can be loaded into the library.\nSome patches may not be loaded.");
            len = maxlen;
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
            	patch[incoming[i].bank][incoming[i].number] != null)		// someone is already there
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
						if (b >= getNumBanks()) 	// uh....
							break;
						}
					}
				}
				
			if (b >= getNumBanks()) 	// uh....
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
                
                
    public void readBank(byte[] incoming, Librarian librarian)
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
        
        // We have to figure out what bank to use
        int bank = 0;
        int b = synth.getBank(incoming);
        if (b >= 0)	// bank known
        	{
        	bank = b + 1;
        	}
        else 	// bank unknown
        	{
        	/*
        	// Check the librarian to see which bank is selected
        	int col = librarian.getSelectedColumn();
        	if (col >= 0)
        		{
        		bank = col + 1;
        		}
        	else
        	*/
        		{
        		// Use the first bank
        		bank = 1;
        		}
        	}
        
        synth.getModel().setUpdateListeners(false);
        for(int i = 0; i < getBankSize(); i++)
            {
            boolean showed = false;
            int result = synth.parseFromBank(incoming, i);
            if (result == Synth.PARSE_FAILED)
                {
                if (!showed)
                    {
                    synth.showSimpleError("Bad Patches in Bank", "This file has a bank with bad patches.  They will be replaced with blanks.");
                    showed = true;
                    }
                patches[bank][i] = new Patch(initPatch);
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
                patches[bank][i] = patch;
                }
            }
        synth.getModel().setUpdateListeners(true);
        
        // restore synth
        synth.setModel(original);
        synth.setSendMIDI(originalMIDI);
        synth.printRevised = originalPrintRevised;
        synth.getUndo().setWillPush(originalGetWillPush);
        }










    /////  EMITTING

	public static final int ALL_PATCHES = -1;
	
	// Emits MIDI representing all the patches from start to start+len-1 in bank.
	// If bank == ALL_PATCHES, then emits the entire library
	// if forceIndependent, then emitRange will NEVER call emitBank
    Object[] emitRange(int bank, int start, int len, boolean toFile, boolean forceIndependent)
        {
        ArrayList data = new ArrayList();
        
        if (bank == ALL_PATCHES && synth.getSupportsBankWrites() && !forceIndependent)
        	{
        	return emitBank(bank, toFile);
        	}
        
        /*if (toFile || synth.showSimpleConfirm(
        	//toFile ?
	        //	(bank == ALL_PATCHES ? "Save All Patches" : 
	        //		(len == 1 ? "Save Patch" : "Save Patches")) :
	        	(bank == ALL_PATCHES ? "Write All Patches" : 
	        		(len == 1 ? "Write Patch" : "Write Patches")),
        	//toFile ?
	        //	(bank == ALL_PATCHES ? "Save all patches to file?" :
	        //		(len == 1 ? "Save patch to file?" : "Save selected patches to file?")) : 
	        	(bank == ALL_PATCHES ? "Write all patches to the synthesizer?" :
	        		(len == 1 ? "Write patch to the synthesizer?" : "Write selected patches to the synthesizer?"))))
*/
	        {
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
            	len = getNumBanks() * getBankSize();
            	}
            
        	synth.getModel().setUpdateListeners(false);
            boolean failed = false;
            for(int i = start; i < start + len; i++)
                {
                int b = (bank == ALL_PATCHES ? i / getBankSize() : bank);
                int n = (bank == ALL_PATCHES ? i % getBankSize() : i);
                
                if (hasBanks)
                    {
                    location.set("bank", b);
                    }
                location.set("number", n);

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
                        localFailed = true;
                        break;
                        }
                    }
                    
                if (localFailed) continue;
                                        
                // now emit
                Object[] objs = synth.emitAll(location, false, toFile);
                for(int o = 0; o < objs.length; o++)
                	data.add(objs[o]);
                	
                if (!toFile)
                	{
                	int pause = synth.getPauseAfterWritePatch();
                	if (pause > 0) data.add(Integer.valueOf(pause));
                	}
                }
        	synth.getModel().setUpdateListeners(true);
                
            // restore
            synth.setModel(original);
            synth.setSendMIDI(originalMIDI);
            synth.printRevised = originalPrintRevised;
            synth.getUndo().setWillPush(originalGetWillPush);
            
            // should I do a change patch?
            if (failed) synth.showSimpleError("Incomplete Patch Write", "Some patches were malformed and could not be written.");
 
             return data.toArray();
            }
//        return null;
        }
        
	/** Writes to the synthesizer all the patches from start to start+len-1 in bank.
	If bank == ALL_PATCHES, then writes the entire library */
    boolean writingRange = false;		// breaks cycles
    public void writeRange(int bank, int start, int len)
    	{
    	if (writingRange) return;
    	writingRange = true;

    	Librarian librarian = synth.librarian;
    	
        if (!synth.getSupportsPatchWrites())
            {
            // Let's try writing via banks
            if (bank == ALL_PATCHES)
            	writeBank(bank);
            else
            	synth.showSimpleError("Not Supported", "Edisyn cannot write arbitrary patches to this synthesizer.");
            }
        else
        	{
			if (bank != ALL_PATCHES && !isWriteableBank(bank))
				{
            	synth.showSimpleError("Not Supported", "This bank cannot be written to the synthesizer.\nIt is read-only.");
				writingRange = false;
				return;
				}
									
        	Object[] data = emitRange(bank, start, len, false, false);
        	if (data != null)
        		{
				if (synth.tuple == null || synth.tuple.outReceiver == null)
					{
					if (!synth.setupMIDI())
						{
						writingRange = false;
						return;
						}				
					}
				synth.tryToSendMIDI(data, librarian.writeProgress);
				synth.sendAllParameters();
				}
        	}           
		writingRange = false;
    	}

	/** Saves to disk all the patches from start to start+len-1 in bank.
	If bank == ALL_PATCHES, then writes the entire library */
    public void saveRange(int bank, int start, int len)
    	{
		int result = Synth.showMultiOption(synth, new String[0], new JComponent[0], 
			new String[] { "As Individual Files", "To Bulk File", "Cancel" },
			0, "Save Patches", "Save the Patches...");
			
		if (result == 2) return;
		else if (result == 1)
        	{
			Object[] d = emitRange(bank, start, len, true, false);

			if (d != null)
				{
				byte[] data = Synth.flatten(d);

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
        else
        	{
        	saveRangeIndependently(bank, start, len);
        	}
    	}
    	

	/** Saves to disk all the patches from start to start+len-1 in bank.
	If bank == ALL_PATCHES, then writes the entire library */
    public void saveRangeIndependently(int bank, int start, int len)
    	{
        Object[] d = emitRange(bank, start, len, true, true);		// force independent patch saves
        int offset = 0;

        if (d != null)
        	{
			File dir = synth.selectDirectory("Select Directory for Patches",
				synth.getFile() == null ? null : new File(synth.getFile().getParentFile().getPath()), 
				true);
				
			if (dir != null)
				{
				synth.setLastDirectory(dir.getParent());
				if (bank == ALL_PATCHES)
					{
					start = 0;
					len = getBankSize();
					}
				
				// If we're not doing ALL_PATCHES, we have a single bank and our numbers go from start to start + len
				int startbank = bank;
				int endbank = bank + 1;
				int startnum = start;
				int endnum = start + len;
				
				// If we're doing ALL_PATCHES, we have multiple banks and mutiple numbers
				if (bank == ALL_PATCHES)
					{
					startbank = 0;
					endbank = getNumBanks();
					startnum = 0;
					endnum = getBankSize();
					}
					
				for(int b = startbank; b < endbank; b++)
					{
					for(int i = startnum; i < endnum; i++)
						{
						FileOutputStream os = null;
						Patch patch = getPatch(b, i);
						if (patch == null) patch = initPatch;
						String filename = ((getNumBanks() > 1 ? (getBankName(b) + ".") : "") + getPatchNumberNames()[i] + "." + patch.name.trim());
						filename = StringUtility.makeValidFilename(filename);
						File f = new File(dir, filename + ".syx");

						try
							{
							os = new FileOutputStream(f);
							byte[] data = (byte[])d[offset++];
						
							for(int j = 0; j < data.length; j++)
								{
								os.write(data[j]);
								}
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



	/** Writes to the synthesizer all the patches in the given bank,
		either using bank-write functions or individual patch writing functions. 
		If bank == ALL_PATCHES, then writes the entire library. */
    public void writeBank(int bank)
    	{
    	if (synth.getSupportsBankWrites())
    		{
			if (bank != ALL_PATCHES && !isWriteableBank(bank))
				{
            	synth.showSimpleError("Not Supported", "This bank cannot be written to the synthesizer.");
				return;
				}
									
			//if (synth.showSimpleConfirm(
			//		(bank == ALL_PATCHES ? "Write All Patches" : "Write Bank"),
			//		(bank == ALL_PATCHES ? "Write all patches to the synthesizer?" : "Write bank to the synthesizer?")))
				{
				Object[] data = null;
				data = emitBank(bank, false);
				if (data != null)
					{
					if (synth.tuple == null || synth.tuple.outReceiver == null)
						{
						if (!synth.setupMIDI())
							return;
						}
					synth.tryToSendMIDI(data);
					synth.sendAllParameters();
					}
    			}
    		}
    	else if (synth.getSupportsPatchWrites())
            {
            writeRange(bank, 0, getBankSize());
            }
        else
            {
            // this should never happen
            synth.showSimpleError("Not Supported", "Edisyn cannot write banks to this synthesizer.");
            }
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
            saveRange(bank, 0, getBankSize());
            }
        else
            {
            // this should never happen
            synth.showSimpleError("Not Supported", "Edisyn cannot save banks of this type to a file.");
            }
    	}

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

	/** Emits all the patches in the given bank, using bank-write functions. 
		If bank == ALL_PATCHES, then writes the entire library. */
    Object[] emitBank(int bank, boolean toFile)
        {
			if (bank == ALL_PATCHES)
				{
				Object[][] data = new Object[getNumBanks()][];
				for(int i = 0; i < data.length; i++)
					data[i] = emitBank(i, toFile);
				return concatenate(data);
				}
			else
				{
				if (!isWriteableBank(bank)) return new Object[0];
									
				Model[] patches = new Model[getBankSize()];
				
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
        		synth.getModel().setUpdateListeners(true);
										
				// now emit
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
		
				// should I do a change patch?
				if (failed) synth.showSimpleError("Incomplete Patch Write", "Some patches from bank " + getBankName(bank) + " were malformed and could not be written.");
				return data;
				}	
        }
    
    }
        
        
