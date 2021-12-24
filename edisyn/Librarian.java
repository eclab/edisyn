/***
    Copyright 2021 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn;
import edisyn.gui.*;
import edisyn.util.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.dnd.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.activation.*;

/**
   LIBRARIAN is a JComponent implementing a JTable backed by a LIBRARY as its model.
**/

public class Librarian extends JPanel
    {
    JTable patchWell;
    
    JTable table;
    JScrollPane scrollPane;
    JButton undo = new JButton("Undo");
    JButton redo = new JButton("Redo");
    PushButton clearAction;
    PushButton downloadAction;
    PushButton writeAction;
    PushButton saveAction;
    PushButton stopAction;
    Box buttonBox;
//    Box downloadBox;
    
    /** Returns the current library (which is the table's model) */    
    public Library getLibrary() { return (Library)(table.getModel()); }
        
    /** Modifies a button to look proper in Edisyn. */    
    void setupButton(JButton button)
        {
        button.putClientProperty("JComponent.sizeVariant", "small");
        button.setFont(Style.SMALL_FONT());
        button.setHorizontalAlignment(SwingConstants.CENTER);
        }
        
        
    public void updateUndoRedo()
    	{
		undo.setEnabled(getLibrary().hasUndo());
		redo.setEnabled(getLibrary().hasRedo());
		getLibrary().synth.updateUndoMenus();
    	}


    public static final Color STANDARD_BACKGROUND_COLOR = new Color(250, 250, 250);
    public static final Color SELECTED_COLOR = new Color(160, 160, 160);
    public static final Color DROP_COLOR = new Color(160, 160, 200);
    public static final Color BACKGROUND_COLOR = new JTabbedPane().getBackground(); // new Color(200, 200, 200);
    public static final Color GRID_COLOR = Color.GRAY;
    public static final Color READ_ONLY_BACKGROUND_COLOR = new Color(250, 245, 255);
    public static final Color SCRATCH_COLOR = new Color(240, 250, 250);

    public Librarian(Synth synth)
        {
        Library model = new Library(synth);
        
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
          
        table = new JTable()
            {
            int selectedColumn = -1;
            public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend)
                {
                if (selectedColumn != columnIndex && extend)
                    {
                    columnIndex = selectedColumn;
                    }
                selectedColumn = columnIndex;
                super.changeSelection(rowIndex, columnIndex, toggle, extend);
                }

            public void setColumnSelectionInterval(int index0, int index1)
                {
                super.setColumnSelectionInterval(index0, index0);
                }

            public void addColumnSelectionInterval(int index0, int index1)
                {
                // do nothing
                }
            };
                
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer()
            {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
                {
                Component comp = super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);

                // Change BACKGROUND COLOR
                                
                // Are we a drop location?
                JTable.DropLocation drop = table.getDropLocation();
                if (drop != null && row == drop.getRow() && column == col(table, drop.getColumn()))
                    {
                    comp.setBackground(DROP_COLOR);
                    }
                else if (isSelected)
                    {
                    comp.setBackground(SELECTED_COLOR);
                    }
                else if (column == 0)			// scratch
                    {
                    comp.setBackground(SCRATCH_COLOR);
                    }
                else if (!Librarian.this.getLibrary().isWriteableBank(column - 1))
                    {
                    comp.setBackground(READ_ONLY_BACKGROUND_COLOR);
                    }
                else
                    {
                    comp.setBackground(STANDARD_BACKGROUND_COLOR);
                    }

                // Change FOREGROUND COLOR
                if (column == 0)
                    {
                    // No color change for now
                    }
                return comp;
                }
            });
        
        table.setModel(model);
        table.setAutoCreateRowSorter(false);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setDefaultEditor(Object.class, null);
        //table.setSelectionBackground(Color.GRAY);
        table.setGridColor(GRID_COLOR);
        table.getTableHeader().setBackground(BACKGROUND_COLOR);

		// deterimine the width of the columns
		String nm = "";
		for(int i = 0; i < synth.getPatchNameLength(); i++) nm += "M";
		int w = table.getFontMetrics(table.getFont()).stringWidth(nm);
		
		for(int i = 0; i < table.getColumnModel().getColumnCount(); i++)
			{
			TableColumn tc = table.getColumnModel().getColumn(i);
			//tc.setPreferredWidth(w * 4 / 5 + 4);	// We'll do about 3/4 the maximum theoretical limit, which seems to be a good outer bound for most patch names, plus a tiny bit of slop.
			tc.setPreferredWidth(w + 4);	// We'll do about 3/4 the maximum theoretical limit, which seems to be a good outer bound for most patch names, plus a tiny bit of slop.
			}
        
        // Enable double-clicking on table headers to select columns
        final JTable _table = table;
        _table.getTableHeader().addMouseListener(new MouseAdapter()
            {
            public void mouseClicked(MouseEvent e) 
                {
                if (e.getClickCount() == 2)
                    {
                    int c = _table.columnAtPoint(e.getPoint());
                    _table.setColumnSelectionInterval(c, c);
                    if(_table.getRowCount() > 0)
                    	{
                        _table.setRowSelectionInterval(0, table.getRowCount() - 1);
                        }
                    }
                else if (e.getClickCount() > 2)
                    {
                    String val = getLibrary().synth.reviseBankName("test");
                    if (val == null) 
                    	{
                    	getLibrary().synth.showSimpleError("Cannot Set Bank Name", "This synthesizer does not support custom bank names.");
                    	}
                    else
                    	{
						int c = _table.columnAtPoint(e.getPoint());
						if (c == 0) // scratch bank
							{
                    		getLibrary().synth.showSimpleError("Cannot Set Bank Name", "You can't change the name of the scratch bank.");
							}
						else
							{
							String bankName = getLibrary().getUserName(c - 1);
							if (bankName == null) bankName = "";
							final JTextField field = new JTextField(bankName);
							int result = getLibrary().synth.showMultiOption(
								getLibrary().synth,
								new String[] { "Bank Name" },
								new JComponent[] { field }, 
								new String[] { "Set Name", "Cancel" },
								0, 
								"Bank Name",
								"Enter a name for Bank " + getLibrary().getBankName(c - 1));
							if (result == 0)
								{
								bankName = getLibrary().synth.reviseBankName(field.getText());
								getLibrary().setUserName(c - 1, bankName);
								table.getColumnModel().getColumn(c).setHeaderValue(getLibrary().getColumnName(c));
								}
							}
						}
					}
                }
            });
            
        // Enable Drag and Drop
        table.setDragEnabled(true);
        table.setDropMode(DropMode.ON);
        table.setTransferHandler(new LibrarianTransferHandler(getLibrary().synth)); 
                

		table.addMouseListener(new MouseAdapter() 
			{
			public void mousePressed(MouseEvent mouseEvent) 
				{
				if (mouseEvent.getClickCount() == 2) 
					{
					int column = col(table, table.getSelectedColumn());
					int row = table.getSelectedRow();
    				if (column >= 0 && row >= 0)
    					{
    					copy(Librarian.this, table, column, row, 1, Librarian.this, patchWell, 0, 0, true);
    					synth.setCurrentTab(0);
    					}
					}
			}
		});  
		
		table.addKeyListener(new KeyAdapter()
			{
			public void keyPressed(KeyEvent e)
				{
				int key = e.getKeyCode();
				if (key == 8 ||		// backspace
					key == 127)		// DEL
					{
					clear();
					}
				}
			});
		
		
		              
        JTable rowNames = new javax.swing.JTable()
            {
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) 
                {
                return this.getTableHeader().getDefaultRenderer().getTableCellRendererComponent(
                    this, this.getValueAt(row, col), false, false, row, col);
                }
            };

        rowNames.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        rowNames.setColumnSelectionAllowed(false);
        rowNames.setRowSelectionAllowed(false);
        rowNames.setCellSelectionEnabled(false);
        rowNames.setEnabled(false);
        rowNames.setDragEnabled(false);
        rowNames.getTableHeader().setReorderingAllowed(false);
        rowNames.getTableHeader().setResizingAllowed(false);
        rowNames.setDefaultEditor(Object.class, null);
        rowNames.getTableHeader().setBackground(BACKGROUND_COLOR);	//BACKGROUND_COLOR);
        rowNames.setGridColor(GRID_COLOR);

        // transpose
        String[] numNames = getLibrary().getPatchNumberNames();
        String[][] numNames2 = new String[numNames.length][1];
        for(int i = 0; i < numNames.length; i++)
            numNames2[i][0] = numNames[i];
        
        rowNames.setModel(new javax.swing.table.DefaultTableModel(
                numNames2,
                new String[] { "" }));
        // this has to be after you set the rownames model
        rowNames.setPreferredScrollableViewportSize(rowNames.getPreferredSize());

        // Size properly
        Dimension d = table.getPreferredScrollableViewportSize();
        d.height = 0;
        table.setPreferredScrollableViewportSize(d);
        d = rowNames.getPreferredScrollableViewportSize();
        d.height = 0;
        rowNames.setPreferredScrollableViewportSize(d);
        
        scrollPane = new JScrollPane();
        scrollPane.setRowHeaderView(rowNames);
        scrollPane.setViewportView(table);
        scrollPane.getViewport().setBackground(BACKGROUND_COLOR);
        scrollPane.getRowHeader().setBackground(BACKGROUND_COLOR);
        scrollPane.setBackground(BACKGROUND_COLOR);
        add(scrollPane, BorderLayout.CENTER);

        buttonBox = new Box(BoxLayout.X_AXIS);

/*
        setupButton(undo);
        undo.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                getLibrary().doUndo();
                updateUndoRedo();
                synth.updateUndoMenus();
                }
            });
        buttonBox.add(undo);

        setupButton(redo);
        redo.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                getLibrary().doRedo();
                updateUndoRedo();
                synth.updateUndoMenus();
                }
            });
        buttonBox.add(redo);
*/

		updateUndoRedo();

/*
        clearAction = new PushButton("Clear...",
        	new String[] { "Clear Selected Patches", "Clear Bank", "Clear All Patches" })
        		{
        		public void perform(int val)
        			{
        			 if (val == 0) clear();
        			else if (val == 1) clearBank();
        			else if (val == 2) clearAll();
        			}
        		};
        buttonBox.add(clearAction.getButton());

        downloadBox = new Box(BoxLayout.X_AXIS);
        downloadAction = new PushButton("Download...",
        	new String[] { "Download Selected Patches from Synth", "Download Bank from Synth", "Download All Patches from Synth" })
        		{
        		public void perform(int val)
        			{
        			if (val == 0) download();
        			else if (val == 1) downloadBank();
        			else if (val == 2) downloadAll();
        			}
        		};
        setupButton(downloadAction.getButton());
  */      
        stopAction = new PushButton("Stop Download")
        		{
        		public void perform()
        			{
        			getLibrary().getSynth().stopBatchDownload();
        			}
        		};
        setupButton(stopAction.getButton());
        if (getLibrary().getSynth().patchTimer == null)  // we're not running
        	stopAction.getButton().setEnabled(false);
		buttonBox.add(stopAction.getButton());
		
//        downloadBox.add(downloadAction.getButton());
//       buttonBox.add(downloadBox);
        
/*        writeAction = new PushButton("Write...",
        	new String[] { "Write Selected Patches to Synth", "Write Bank to Synth", "Write All Patches to Synth" })
        		{
        		public void perform(int val)
        			{
        			if (val == 0) write();
        			else if (val == 1) writeBank();
        			else if (val == 2) writeAll();
        			}
        		};
        buttonBox.add(writeAction.getButton());

        saveAction = new PushButton("Save...",
        	new String[] { "Save Selected Patches to File", "Save Bank to File", "Save All Patches to File" })
        		{
        		public void perform(int val)
        			{
        			if (val == 0) save();
        			else if (val == 1) saveBank();
        			else if (val == 2) saveAll();
        			}
        		};
        buttonBox.add(saveAction.getButton());
*/
        	        
        patchWell = new JTable()
            {
            public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend)
                {
                // do nothing
                }

            public void setColumnSelectionInterval(int index0, int index1)
                {
                super.setColumnSelectionInterval(index0, index0);
                }

            public void addColumnSelectionInterval(int index0, int index1)
                {
                // do nothing
                }
            };
                
        patchWell.setDefaultRenderer(Object.class, new DefaultTableCellRenderer()
            {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
                {
                Component comp = super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);

                // Change BACKGROUND COLOR
                                
                // Are we a drop location?
                JTable.DropLocation drop = table.getDropLocation();
                if (drop != null && row == drop.getRow() && column == col(table, drop.getColumn()))
                    {
                    comp.setBackground(DROP_COLOR);
                    }
                else
                    {
                    comp.setBackground(STANDARD_BACKGROUND_COLOR);
                    }

                return comp;
                }
            });
        
        patchWell.setModel(new AbstractTableModel() 
            {
            public int getRowCount() { return 1; }
            public int getColumnCount() { return 1; }
            public boolean isCellEditable(int row, int col) { return false; }

            public Object getValueAt(int row, int col) 
                {
                Synth synth = getLibrary().getSynth();
                int synthNum = getLibrary().getSynthNum();
                String name = synth.getModel().get("name","" + synth.getPatchLocationName(synth.getModel()));
                int number = synth.getModel().get("number", -1);
                int bank = synth.getModel().get("bank", -1);
                byte[][] data = synth.cutUpSysex(synth.flatten(synth.emitAll(synth.getModel(), false, true)));
                Patch patch = new Patch(synthNum, data, false);
                patch.name = name;
                patch.bank = (bank == -1 ? 0 : bank);
                patch.number = (number == -1 ? Patch.NUMBER_NOT_SET : number);          // FIXME: should I use NUMBER_NOT_SET?
                return patch;
                }
                                
            public void setValueAt(Object value, int row, int col) 
                {
                Patch patch = (Patch)value;     
                
                if (patch == null) return;			// I don't like the idea of dragging null to a patch, and it "transforms" into "UNTITLED"
                	// patch = getLibrary().getInitPatch();
                	
				// do we need to modify the bank and number?
				
				synth.undo.push(synth.model);
				synth.undo.setWillPush(false);
				boolean send = synth.getSendMIDI();
				synth.setSendMIDI(false);
				synth.performParse(synth.flatten(patch.sysex), false);  // is this from a file?  I'm saying false
				synth.setSendMIDI(send);
				synth.undo.setWillPush(true);
				synth.sendAllParameters();
				
				fireTableCellUpdated(row, col);
                }
            });
                        
        patchWell.setTableHeader(null);
        patchWell.setRowSelectionAllowed(false);
        patchWell.setColumnSelectionAllowed(false);
        patchWell.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        patchWell.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        patchWell.setDefaultEditor(Object.class, null);
        
        // Enable Drag and Drop
        patchWell.setDragEnabled(true);
        patchWell.setDropMode(DropMode.ON);
        patchWell.setTransferHandler(new LibrarianTransferHandler(getLibrary().synth)); 
        
        JLabel patchWellLabel = new JLabel("Current Patch: ");
        patchWellLabel.putClientProperty("JComponent.sizeVariant", "small");
        patchWellLabel.setFont(Style.SMALL_FONT());

        buttonBox.add(patchWellLabel);
        buttonBox.add(patchWell);
        
        buttonBox.add(Box.createGlue());
        add(buttonBox, BorderLayout.SOUTH);
        }


public static void setLibrarianMenuSelected(JMenu menu, boolean val)
	{
	// ugh, accessign subelements is so convoluted
	MenuElement[] m = menu.getSubElements()[0].getSubElements();
	for(int i = 1; i < m.length; i++)		// skip "Open Librarian"
		{
		if (m[i] instanceof JMenuItem)
			{
			((JMenuItem)m[i]).setEnabled(val);
			}
		}
	}
	
public static JMenu buildLibrarianMenu(JMenuItem openMenu, Synth synth)
	{
	JMenu menu = new JMenu("Librarian");

	menu.add(openMenu);
//	menu.add(stopDownloadingMenu);

	menu.addSeparator();

	JMenuItem item = new JMenuItem("Clear Selected Patches");
	item.addActionListener(new ActionListener()
		{
		public void actionPerformed(ActionEvent evt) { synth.librarian.clear(); }
		});
	menu.add(item);
	item.setEnabled(false);
	
	item = new JMenuItem("Clear Bank");
	item.addActionListener(new ActionListener()
		{
		public void actionPerformed(ActionEvent evt) { synth.librarian.clearBank(); }
		});
	menu.add(item);
	item.setEnabled(false);
		
	item = new JMenuItem("Clear All Patches");
	item.addActionListener(new ActionListener()
		{
		public void actionPerformed(ActionEvent evt) { synth.librarian.clearAll(); }
		});
	menu.add(item);
	item.setEnabled(false);
		
	menu.addSeparator();
	
	item = new JMenuItem("Download Selected Patches from Synth");
	item.addActionListener(new ActionListener()
		{
		public void actionPerformed(ActionEvent evt) { synth.librarian.download(); }
		});
	menu.add(item);
	item.setEnabled(false);
		
/*	item = new JMenuItem("Download Range from Synth");
	item.addActionListener(new ActionListener()
		{
		public void actionPerformed(ActionEvent evt) { synth.librarian.downloadRange(); }
		});
	menu.add(item);
	item.setEnabled(false);
*/
		
	item = new JMenuItem("Download Bank from Synth");
	item.addActionListener(new ActionListener()
		{
		public void actionPerformed(ActionEvent evt) { synth.librarian.downloadBank(); }
		});
	menu.add(item);
	item.setEnabled(false);
		
	item = new JMenuItem("Download All Patches from Synth");
	item.addActionListener(new ActionListener()
		{
		public void actionPerformed(ActionEvent evt) { synth.librarian.downloadAll(); }
		});
	menu.add(item);
	item.setEnabled(false);
		
	menu.addSeparator();
	
	item = new JMenuItem("Write Selected Patches to Synth");
	item.addActionListener(new ActionListener()
		{
		public void actionPerformed(ActionEvent evt) { synth.librarian.write(); }
		});
	menu.add(item);
	item.setEnabled(false);
		
	item = new JMenuItem("Write Bank to Synth");
	item.addActionListener(new ActionListener()
		{
		public void actionPerformed(ActionEvent evt) { synth.librarian.writeBank(); }
		});
	menu.add(item);
	item.setEnabled(false);
		
	item = new JMenuItem("Write All Patches to Synth");
	item.addActionListener(new ActionListener()
		{
		public void actionPerformed(ActionEvent evt) { synth.librarian.writeAll(); }
		});
	menu.add(item);
	item.setEnabled(false);
		
	menu.addSeparator();
	
	item = new JMenuItem("Save Selected Patches");
	item.addActionListener(new ActionListener()
		{
		public void actionPerformed(ActionEvent evt) { synth.librarian.save(); }
		});
	menu.add(item);
	item.setEnabled(false);
		
	item = new JMenuItem("Save Bank");
	item.addActionListener(new ActionListener()
		{
		public void actionPerformed(ActionEvent evt) { synth.librarian.saveBank(); }
		});
	menu.add(item);
	item.setEnabled(false);
		
	item = new JMenuItem("Save All Patches");
	item.addActionListener(new ActionListener()
		{
		public void actionPerformed(ActionEvent evt) { synth.librarian.saveAll(); }
		});
	menu.add(item);
	item.setEnabled(false);
		
	return menu;
	}
	
	
	
	
	/* JTable says that it maintains the proper column values even if the columns are rearranged,
		but this is a lie.  You have to do it manually.  This function does the proper conversion. */
	static int col(JTable table, int column)
		{
		if (column < 0) return column;
		else return table.getColumnModel().getColumn(column).getModelIndex();
		}


	public void downloadRange()
		{
		getLibrary().getSynth().doGetAllPatches();
		}
		
    /** Downloads all locations. */
    public void downloadAll()
        {
        performDownload(0, 0, getLibrary().getNumBanks() - 1, getLibrary().getBankSize() - 1 );		// wrap-around
        }

    /** Downloads all locations in bank. */
    public void downloadBank()
        {
        int column = col(table, table.getSelectedColumn());
        int row = table.getSelectedRow();
        int len = table.getSelectedRowCount();
                        
        if (column < 0 || row < 0 || len == 0) // nope
        	{
        	if (getLibrary().getNumBanks() == 1)
        		{
        		column = 1;		// assume it's "the" bank
        		}
        	else
        		{
	            getLibrary().synth.showSimpleError("Cannot Download", "Please select a patch in the bank to download first.");
            	return;
            	}
            }
            
        if (column == 0)
        	{
            getLibrary().synth.showSimpleError("Cannot Download", "Edisyn cannot download patches to the scratch bank.\nSelect another bank.");
			return;
			}

        performDownload(column - 1, 0, column - 1, getLibrary().getBankSize() - 1 );
        }


    /** Downloads the selected locations. */
    public void download()
        {
        int column = col(table, table.getSelectedColumn());
        int row = table.getSelectedRow();
        int len = table.getSelectedRowCount();
                        
        if (column < 0 || row < 0 || len == 0) // nope
        	{
            getLibrary().synth.showSimpleError("Cannot Download", "Please select a patch or patch range to download first.");
            return;
            }
            
        if (column == 0)
        	{
            getLibrary().synth.showSimpleError("Cannot Download", "Edisyn cannot download patches from the scratch bank.\nSelect another bank.");
			return;
			}
    	
        performDownload(column - 1, row, column - 1, row + len - 1);
        }
        
    void performDownload(int bank1, int patch1, int bank2, int patch2)
    	{
    	if (!getLibrary().getSynth().getSupportsDownloads())
    		{
            getLibrary().synth.showSimpleError("Cannot Download", "Edisyn cannot request patches from this synthesizer.");
    		return;
    		}
    		
    	boolean hasBanks = (getLibrary().getSynth().getBankNames() != null);
    	if (hasBanks)
    		{
    		getLibrary().getSynth().doGetPatchesForLibrarian(bank1, patch1, bank2, patch2);
    		}
		else
			{    	
    		getLibrary().getSynth().doGetPatchesForLibrarian(PatchLocation.NO_BANK, patch1, PatchLocation.NO_BANK, patch2);
    		}
    	}


    /** Writes all locations. */
    public void writeAll()
        {
        getLibrary().writeRange(Library.ALL_PATCHES, 0, 0);
        }

    /** Writes all locations in bank. */
    public void writeBank()
        {
        int column = col(table, table.getSelectedColumn());
        int row = table.getSelectedRow();
        int len = table.getSelectedRowCount();
                        
        if (column < 0 || row < 0 || len == 0) // nope
        	{
        	if (getLibrary().getNumBanks() == 1)
        		{
        		column = 1;		// assume it's "the" bank
        		}
        	else
        		{
	            getLibrary().synth.showSimpleError("Cannot Write", "Please select a patch in the bank to write first.");
            	return;
            	}
            }
            
        if (column == 0)
        	{
            getLibrary().synth.showSimpleError("Cannot Write", "Edisyn cannot write patches from the scratch bank.\nSelect another bank.");
			return;
			}
        getLibrary().writeBank(column - 1);
        }


    /** Writes the selected locations. */
    public void write()
        {
        int column = col(table, table.getSelectedColumn());
        int row = table.getSelectedRow();
        int len = table.getSelectedRowCount();
                        
        if (column < 0 || row < 0 || len == 0) // nope
        	{
            getLibrary().synth.showSimpleError("Cannot Write", "Please select a patch or patch range to write first.");
            return;
            }
            
        if (column == 0)
        	{
            getLibrary().synth.showSimpleError("Cannot Write", "Edisyn cannot write patches from the scratch bank.\nSelect another bank.");
			return;
			}
            
        getLibrary().writeRange(column - 1, row, len);
        }

	public void saveAll()
		{
        getLibrary().saveRange(Library.ALL_PATCHES, 0, 0);
		}
		
		
    /** Writes all locations in bank. */
    public void saveBank()
        {
        int column = col(table, table.getSelectedColumn());
        int row = table.getSelectedRow();
        int len = table.getSelectedRowCount();
                        
        if (column < 0 || row < 0 || len == 0) // nope
            {
        	if (getLibrary().getNumBanks() == 1)
        		{
        		column = 1;		// assume it's "the" bank
        		}
        	else
        		{
	            getLibrary().synth.showSimpleError("Cannot Save", "Please select a patch in the bank to save first.");
            	return;
            	}
            }
            
        if (column == 0)
        	{
            getLibrary().synth.showSimpleError("Cannot Write", "Edisyn cannot save patches from the scratch bank.\nSelect another bank.");
			return;
			}
        getLibrary().saveBank(column - 1);
        }


    /** Writes the selected locations to a file. */
    public void save()
        {
        int column = col(table, table.getSelectedColumn());
        int row = table.getSelectedRow();
        int len = table.getSelectedRowCount();
                        
        if (column < 0 || row < 0 || len == 0) // nope
        	{
            getLibrary().synth.showSimpleError("Cannot Save", "Please select a patch or patch range to save first.");
            return;
            }
            
            
        if (column == 0)
        	{
            getLibrary().synth.showSimpleError("Cannot Save", "Edisyn cannot save patches from the scratch bank.\nSelect another bank.");
			return;
			}
            
        getLibrary().saveRange(column - 1, row, len);
        }


    /** Clears the selected locations. */
    public void clear()
        {
        getLibrary().pushUndo();
		updateUndoRedo();

        int column = col(table, table.getSelectedColumn());
        int row = table.getSelectedRow();
        int len = table.getSelectedRowCount();
                        
        if (column < 0 || row < 0 || len == 0) // nope
        	{
            getLibrary().synth.showSimpleError("Cannot Clear", "Please select a patch or patch range to clear first.");
            return;
            }
                                
        fill(table, column, row, len, null);		// getLibrary().getInitPatch());

        // Change the selection
        //table.clearSelection();
        }

    /** Clears current selected bank. */
    public void clearBank()
        {
        getLibrary().pushUndo();
		updateUndoRedo();

        int column = col(table, table.getSelectedColumn());
        int row = table.getSelectedRow();
        int len = table.getSelectedRowCount();
                        
        if (column < 0 || row < 0 || len == 0) // nope
        	{
        	if (getLibrary().getNumBanks() == 1)
        		{
        		column = 1;		// assume it's "the" bank
        		}
        	else
        		{
	            getLibrary().synth.showSimpleError("Cannot Clear", "Please select a patch in the bank to clear first.");
	            return;
	            }
            }
                                
        fill(table, column, 0, getLibrary().getBankSize(), null);		// getLibrary().getInitPatch());

        // Change the selection
        //table.clearSelection();
        }

    /** Clears all. */
    public void clearAll()
        {
        getLibrary().pushUndo();
		updateUndoRedo();

        for(int i = 0; i < getLibrary().getNumBanks() + 1; i++)                     
        	{
        	fill(table, i, 0, getLibrary().getBankSize(), null);		// getLibrary().getInitPatch());
        	}

        // Change the selection
        //table.clearSelection();
        }

    /** Sets all the values of a given set of locations to copies of a certain value. */
    public void fill(JTable table, int col, int row, int len, Patch val)
        {
        for(int i = row; i < row + len; i++)
            {
            table.setValueAt(val == null ? null : new Patch(val), i, col);
            }
        }

    /** Copies one set of locations to another. */
    public void copy(Librarian from, JTable fromTable, int fromCol, int fromRow, int len, Librarian to, JTable toTable, int toCol, int toRow, boolean duplicate)
        {
        if (!isPatchWell(toTable))              // as opposed to patchWell
            {
            to.getLibrary().pushUndo();
            to.updateUndoRedo();
            }
        else
            {
            to.getLibrary().getSynth().getUndo().push(to.getLibrary().getSynth().getModel());
            }
        
        Patch p = null;
        for(int i = 0; i < len; i++)
            {
            Patch _from = (Patch)(fromTable.getValueAt(fromRow + i, fromCol));
            if (duplicate)
                {
                toTable.setValueAt(p = (_from == null ? null : new Patch(_from)), toRow + i, toCol);
                }
            else
            	{
                toTable.setValueAt(_from, toRow + i, toCol);  
                }             
            }
            
        // Change the selection
        fromTable.clearSelection();
        toTable.clearSelection();
        toTable.changeSelection(toRow, toCol, false, false);
        toTable.changeSelection(toRow + len - 1, toCol, false, true);		// not sure why we need to do -1, but we do
        } 
                
    /** Moves one set of locations to another, clearing the original locations. */
    public void move(Librarian from, JTable fromTable, int fromCol, int fromRow, int len, Librarian to, JTable toTable, int toCol, int toRow)
        {
        copy(from, fromTable, fromCol, fromRow, len, to, toTable, toCol, toRow, false);
        
        if (!isPatchWell(fromTable))            // as opposed to patchWell
            {
            if (fromTable != toTable)               // don't double-undo
                {
                from.getLibrary().pushUndo();
                from.updateUndoRedo();
                }
            }
        fill(fromTable, fromCol, fromRow, len, null);		// to.getLibrary().getInitPatch());

        // Change the selection
        fromTable.clearSelection();
        toTable.clearSelection();
        toTable.changeSelection(toRow, toCol, false, false);
        toTable.changeSelection(toRow + len - 1, toCol, false, true);		// not sure why we need to do -1, but we do
        }
        
    /** Swaps one set of locations with another. */
    public void swap(Librarian from, JTable fromTable, int fromCol, int fromRow, int len, Librarian to, JTable toTable, int toCol, int toRow)
        {
        if (!isPatchWell(toTable))              // as opposed to patchWell
            {
            to.getLibrary().pushUndo();
            to.updateUndoRedo();
            }
        else
            {
            to.getLibrary().getSynth().getUndo().push(to.getLibrary().getSynth().getModel());
            }

        if (!isPatchWell(fromTable))            // as opposed to patchWell
            {
            if (fromTable != toTable)               // don't double-undo
                {
                from.getLibrary().pushUndo();
                from.updateUndoRedo();
                }
            }
                        
        for(int i = 0; i < len; i++)
            {
            Object obj = toTable.getValueAt(toRow + i, toCol);
            Object with = fromTable.getValueAt(fromRow + i, fromCol);
            toTable.setValueAt(with, toRow + i, toCol);
            fromTable.setValueAt(obj, fromRow + i, fromCol);
            }

        // Change the selection
        fromTable.clearSelection();
        toTable.clearSelection();
        toTable.changeSelection(toRow, toCol, false, false);
        toTable.changeSelection(toRow + len - 1, toCol, false, true);		// not sure why we need to do -1, but we do
        }

    static boolean isPatchWell(JTable table) { return table.getTableHeader() == null; }

        
    //// DRAG AND DROP JUNK


    // This TransferHandler handles the entire drag and drop operation
    static class LibrarianTransferHandler extends TransferHandler 
        {
        Synth synth;
        public LibrarianTransferHandler(Synth synth) { this.synth = synth; }
        
        // The DataFlavor is the type of data being dragged.  Ours is simply an instance of PatchLocationSet 
        static DataFlavor localObjectFlavor;
        static 
            {
            try
                {
                localObjectFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=edisyn.PatchLocationSet");
                }
            catch (ClassNotFoundException ex)
                {
                ex.printStackTrace();
                }
            }

        // We allow all three kinds of drags
        public int getSourceActions(JComponent c) 
            {
            JTable table = (JTable)c;
            if (isPatchWell(table)) // patchWell only permits copies
                return TransferHandler.COPY;
            else
                return TransferHandler.MOVE | TransferHandler.COPY | TransferHandler.LINK;
            }
                
        // This is at the start of a drag to create the wrapper for the dragged data.  
        // We build a DataHandler which holds a PatchLocationSet
        protected Transferable createTransferable(JComponent c) 
            {
            JTable table = (JTable)c;
            if (isPatchWell(table))     // it's the current patch
                {
                return new DataHandler(new PatchLocationSet(synth, table, 0, 0, 1), localObjectFlavor.getMimeType());
                }
            else
                {
                return new DataHandler(new PatchLocationSet(synth, table, Librarian.col(table, table.getSelectedColumn()), table.getSelectedRow(), table.getSelectedRowCount()),
                    localObjectFlavor.getMimeType());
                }
            }

        // This is called to determine if the drop operation is permitted.  It's called
        // when the user has dragged to the table but has not yet let go, and is used
        // to determine whether to change the cursor etc.  The TransferSupport contains useful info:
        // 1. The drop action in question
        // 2. The table on which the drop is being performed
        // 3. The data being dropped (from which we can get the table which initiated the drop and other stuff)
        public boolean canImport(TransferHandler.TransferSupport info) 
            { 
            int dropAction = info.getDropAction();
            Cursor allowed = (dropAction == TransferHandler.MOVE ? DragSource.DefaultMoveDrop :
                    (dropAction == TransferHandler.COPY ? DragSource.DefaultCopyDrop :
                    DragSource.DefaultLinkDrop));
            Cursor denied = (dropAction == TransferHandler.MOVE ? DragSource.DefaultMoveNoDrop :
                    (dropAction == TransferHandler.COPY ? DragSource.DefaultCopyNoDrop :
                    DragSource.DefaultLinkNoDrop));

            JTable toTable = (JTable)(info.getComponent());
            
            if (isPatchWell(toTable))
                {
                if (dropAction == TransferHandler.LINK || dropAction == TransferHandler.COPY) 
                    return false;
                }

            // Fail if we're not a drag-and-drop operation for some reason
            // or if this isn't a PatchLocationSet.
            if ((!info.isDrop()) ||                                                 // this isn't a drop operation
                (!info.isDataFlavorSupported(localObjectFlavor))) 
                {
                toTable.setCursor(denied); 
                return false;
                }
          
            // Now let's gather information about or tables
            JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
            PatchLocationSet pls = null;
      
            try 
                {
                // This can generate a variety of silly exceptions
                pls = (PatchLocationSet)(info.getTransferable().getTransferData(localObjectFlavor));
                }
            catch (Exception ex)
                {
                ex.printStackTrace();
                toTable.setCursor(denied); 
                return false;
                }
        
            // find the to-librarian
            JComponent c = toTable;
            while(!(c instanceof Librarian))
                c = (JComponent)(c.getParent());
            Librarian to = (Librarian) c;

            // we fail if the synths are different of course         
			if (pls.synth.getClass() != synth.getClass())
				return false;                

            int toNumRows = toTable.getModel().getRowCount();
            JTable fromTable = pls.table;
            int fromCol = pls.column;
            int fromRow = pls.row;
            int len = pls.length;
            int toCol = Librarian.col(toTable, dl.getColumn());
            int toRow = dl.getRow() - (fromTable.getSelectionModel().getLeadSelectionIndex() - fromRow);		// gotta offset by where we dragged
			if (isPatchWell(fromTable)) toRow = dl.getRow();           		// because its selectionIndex is -1
      
            // We fail under any of the following conditions:
            if ((toCol < 0) || (toRow < 0)  ||
                (toRow + len > toNumRows)  ||                         // We're too far down to fit the drag data
                    (toTable == fromTable && toCol == fromCol && 
                    !(fromRow + len <= toRow || toRow + len <= fromRow)))             // We're self-overlapping
                {
                toTable.setCursor(denied); 
                return false;
                }        
                
            // We also fail if we're read-only
            //if (!to.getLibrary().isWriteableBank(toCol - 1))
            //    return false;
                
            // okay, we're good to go
            toTable.setCursor(allowed); 
            return true;
            }

        // This is called after we have determined that a drag is permitted and the user
        // has released the mouse button and performed the drop.  The TransferSupport contains useful info:
        // 1. The drop action in question
        // 2. The table on which the drop is being performed
        // 3. The data being dropped (from which we can get the table which initiated the drop and other stuff)
        public boolean importData(TransferHandler.TransferSupport info) 
            {
            JTable target = (JTable) info.getComponent();

            // find the from-librarian
            JComponent c = target;
            while(!(c instanceof Librarian))
                c = (JComponent)(c.getParent());
            Librarian from = (Librarian) c;

            // reset the cursor
            target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

            // let's just call this again to double check
            if (!canImport(info)) return false;
          
            JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
            JTable toTable = (JTable)(info.getComponent());
            int col = Librarian.col(toTable, dl.getColumn());
            int row = dl.getRow();

            try 
                {
                PatchLocationSet pls = (PatchLocationSet)(info.getTransferable().getTransferData(localObjectFlavor));
                          
                // find the to-librarian
                c = toTable;
                while(!(c instanceof Librarian))
                    c = (JComponent)(c.getParent());
                Librarian to = (Librarian) c;
                          
				JTable fromTable = pls.table;
				int fromCol = pls.column;
				int fromRow = pls.row;
				int len = pls.length;
				int toCol = Librarian.col(toTable, dl.getColumn());
				int toRow = dl.getRow() - (fromTable.getSelectionModel().getLeadSelectionIndex() - fromRow);		// gotta offset by where we dragged
				if (isPatchWell(fromTable)) toRow = dl.getRow();     		// because its selectionIndex is -1 

                // perform the appropriate action
                
                if (isPatchWell(fromTable) ||
                    isPatchWell(toTable))                   // it's patchWell, we only allow copies
                    {
                    from.copy(from, fromTable, fromCol, fromRow, len, to, toTable, toCol, toRow, true);
                    }
                else if (info.getDropAction() == TransferHandler.COPY)
                    {
                    from.copy(from, fromTable, fromCol, fromRow, len, to, toTable, toCol, toRow, true);
                    }
                else if (info.getDropAction() == TransferHandler.MOVE)
                    {
                    from.move(from, fromTable, fromCol, fromRow, len, to, toTable, toCol, toRow);
                    }
                else if (info.getDropAction() == TransferHandler.LINK)
                    {
                    from.swap(from, fromTable, fromCol, fromRow, len, to, toTable, toCol, toRow);
                    }
                else
                    {
                    System.err.println("Librarian.importData() WARNING in valid drop action");
                    return false;           // should never happen
                    }
                                
                return true;
                }
            catch (Exception e) 
                {
                e.printStackTrace();
                return false;
                }
            }

        // This is called after the operation has completed.  Not sure if we need this,
        // but there you go.  We're resetting the cursor.
        protected void exportDone(JComponent c, Transferable t, int act) 
            {
            ((JTable)c).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }

        }
    }
