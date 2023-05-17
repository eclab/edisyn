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
import java.io.*;

/**
   LIBRARIAN is a JComponent implementing a JTable backed by a LIBRARY as its model.
**/

public class Librarian extends JPanel
    {
    // The current patch 
    JTable patchWell;
    // The librarian patch table
    JTable table;
    // Scroll pane holding the table
    JScrollPane scrollPane;
    // The "Stop Download" button
    PushButton stopAction;
    JPanel bottomPanel;
    
    /** Returns the current library (which is the table's model) */    
    public Library getLibrary() { return (Library)(table.getModel()); }
        
    /** Modifies a button to look proper in Edisyn. */    
    void setupButton(JButton button)
        {
        button.putClientProperty("JComponent.sizeVariant", "small");
        button.setFont(Style.SMALL_FONT());
        button.setHorizontalAlignment(SwingConstants.CENTER);
        }
        
    
    /** Returns the selected table column, or -1 if none */
    public int getSelectedColumn()
        {
        return table.getSelectedColumn();
        }
        
    public void updateUndoRedo()
        {
        getLibrary().synth.updateUndoMenus();
        }
        
    public JTable getTable()
        {
        return table;
        }


    public static final Color STANDARD_BACKGROUND_COLOR = new Color(250, 250, 250);
    public static final Color SELECTED_COLOR = new Color(160, 160, 160);
    public static final Color DROP_COLOR = new Color(160, 160, 200);
    public static final Color BACKGROUND_COLOR = new Color(240, 240, 240);              // new JLabel().getBackground(); 
    public static final Color GRID_COLOR = Style.isNimbus() ? new Color(192, 192, 192) : Color.GRAY;
    public static final Color READ_ONLY_BACKGROUND_COLOR = new Color(250, 245, 255);
    public static final Color SCRATCH_COLOR = new Color(240, 250, 250);
    public static final Color INVALID_COLOR = new Color(220, 220, 220);
    public static final Color INVALID_TEXT_COLOR = new Color(255, 0, 0);
    public static final Color STANDARD_TEXT_COLOR = new Color(0, 0, 0);

    public Librarian(Synth synth)
        {
        warnLibrarian(synth);
        
        Library model = new Library(synth);
        
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
          
        table = new JTable()
            {
            int selectedColumn = -1;
            
            public void selectAll() 
                { 
                int column = col(table, table.getSelectedColumn());
                if (column >= 0)
                    {
                    changeSelection(0, column, false, false);
                    changeSelection(getLibrary().getBankSize() - 1, column, true, true);
                    }
                else
                    {
                    changeSelection(0, 0, false, false);
                    changeSelection(getLibrary().getBankSize() - 1, 0, true, true);
                    }
                }
            
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
        
        table.addKeyListener(new KeyListener()
            {
            public void keyPressed(KeyEvent e)
                {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    { 
                    e.consume(); 

                    int column = col(table, table.getSelectedColumn());
                    int row = table.getSelectedRow();
                    int len = table.getSelectedRowCount();
                                                
                    if (column < 0 || row < 0 || len == 0) // nope
                        {
                        getLibrary().synth.showSimpleError("Cannot Change Name", "Please select a patch to change first.");
                        return;
                        }
                        
                    if (len > 1)
                        {
                        getLibrary().synth.showSimpleError("Cannot Change Name", "Please select only a single patch.");
                        return;
                        }
                        
                    getLibrary().changeName(column - 1, row);
                    }
                }
                        
            public void keyReleased(KeyEvent e)
                {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    { e.consume(); }
                }
                
            public void keyTyped(KeyEvent e)
                {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    { e.consume(); }
                }
            });
        
        if (Style.isNimbus())
            {
            table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer()
                {
                protected void setValue(Object value) 
                    {
                    if (value == null) super.setValue(value);
                    else if (getLibrary().getBankName(Library.SCRATCH_BANK) == value.toString()) super.setValue(value);  // can't put a space before <html>
                    else super.setValue((Style.isNimbus() ? " " : "") + value.toString());
                    }

                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
                    {
                    Component comp = super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);

                    ((JComponent)comp).setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, GRID_COLOR));
                                
                    return comp;
                    }
                });
            }
                
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer()
            {
            protected void setValue(Object value) 
                {
                if (value == null) 
                    super.setValue("");
                else if (value instanceof Patch && ((Patch)value).getName() == null)
                    super.setValue("");
                else super.setValue((Style.isNimbus() ? " " : "") + value.toString());
                }

            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
                {
                Component comp = super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);

                // Nimbus is broken grid-wise in JTables. On Linux, this is the only thing that fixes it: overriding completely
                if (Style.isNimbus())
                    {
                    ((JComponent)comp).setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, GRID_COLOR));
                    }

                // Note to get the underlying column number, you have to get the column model and pull out the column.
                // We have stored the original column number in the column's "identifier", so you can grab that as shown here.
                // Note that you CANNOT use table.getColumn(), as this grabs the column by identifier, which is the number
                // we set, so that'll be the wrong column.
                int originalColumn = ((Integer)(table.getColumnModel().getColumn(column).getIdentifier())).intValue();

                // Change BACKGROUND COLOR
                                
                // Are we a drop location?
                JTable.DropLocation drop = table.getDropLocation();
                if (drop != null && row == drop.getRow() && column == drop.getColumn()) // col(table, drop.getColumn()))
                    {
                    comp.setBackground(DROP_COLOR);
                    }
                else if (isSelected)
                    {
                    comp.setBackground(SELECTED_COLOR);
                    }
                else if (originalColumn == 0)                   // scratch
                    {
                    comp.setBackground(SCRATCH_COLOR);
                    }
                else if (originalColumn > 0 && (!synth.isValidPatchLocation(originalColumn - 1, row) || !synth.isAppropriatePatchLocation(originalColumn - 1, row)))                    // invalid cell
                    {
                    comp.setBackground(INVALID_COLOR);
                    }
                else if (!Librarian.this.getLibrary().isWriteableBank(originalColumn - 1))
                    {
                    comp.setBackground(READ_ONLY_BACKGROUND_COLOR);
                    }
                else
                    {
                    comp.setBackground(STANDARD_BACKGROUND_COLOR);
                    }

                
                // Change FOREGROUND COLOR
                if (originalColumn > 0 && !synth.isValidPatchLocation(originalColumn - 1, row))                 // invalid cell
                    {
                    comp.setForeground(INVALID_TEXT_COLOR);
                    }
                else
                    {
                    comp.setForeground(STANDARD_TEXT_COLOR);
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
        table.setGridColor(GRID_COLOR);
        table.getTableHeader().setBackground(BACKGROUND_COLOR);

        // deterimine the width of the columns
        String nm = "";
        for(int i = 0; i < synth.getPatchNameLength(); i++) nm += "M";
        if (Style.isNimbus())
            nm += " ";              // we're gonna pad at the beginning for Nimbus
        int w = table.getFontMetrics(table.getFont()).stringWidth(nm);
                
        // Give the columns unique names
        for(int i = 0; i < table.getColumnModel().getColumnCount(); i++)
            {
            TableColumn tc = table.getColumnModel().getColumn(i);
            tc.setIdentifier(Integer.valueOf(i));
            }

        // Give the columns widths
        for(int i = 0; i < table.getColumnModel().getColumnCount(); i++)
            {
            TableColumn tc = table.getColumnModel().getColumn(i);
            tc.setPreferredWidth(w + 4);    // We'll do about 3/4 the maximum theoretical limit, which seems to be a good outer bound for most patch names, plus a tiny bit of slop.
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
                                table.getColumnModel().getColumn(c).setHeaderValue((Style.isNimbus() ? " " : "") + getLibrary().getColumnName(c));
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
                    loadOneInternal();
                    }
                }
            });  
                
        table.addKeyListener(new KeyAdapter()
            {
            public void keyPressed(KeyEvent e)
                {
                int key = e.getKeyCode();
                if (key == 8 ||         // backspace
                    key == 127)             // DEL
                    {
                    clear();
                    }
                }
            });
                
                
                              
        JTable rowNames = new javax.swing.JTable();

        rowNames.setDefaultRenderer(Object.class, new DefaultTableCellRenderer()
            {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
                {
                JComponent comp = (JComponent)(super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column));
                comp.setBackground(BACKGROUND_COLOR);
                comp.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, GRID_COLOR));
                return comp;
                }
            });

        rowNames.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        rowNames.setColumnSelectionAllowed(false);
        rowNames.setRowSelectionAllowed(false);
        rowNames.setCellSelectionEnabled(false);
        rowNames.setEnabled(false);
        rowNames.setDragEnabled(false);
        rowNames.getTableHeader().setReorderingAllowed(false);
        rowNames.getTableHeader().setResizingAllowed(false);
        rowNames.setDefaultEditor(Object.class, null);
        rowNames.setShowGrid(true);
        rowNames.setIntercellSpacing(new Dimension(0, 0));
        rowNames.setGridColor(GRID_COLOR);

        // transpose
        String[] numNames = getLibrary().getPatchNumberNames();
        String[][] numNames2 = new String[numNames.length][1];
        for(int i = 0; i < numNames.length; i++)
            numNames2[i][0] = " " + numNames[i];
        
        rowNames.setModel(new javax.swing.table.DefaultTableModel(
                numNames2,
                new String[] { "" }));
        
        // Give the columns widths
		double maxWidth = 0;
        for(int i = 0; i < numNames2.length; i++)
        	maxWidth = Math.max(maxWidth, rowNames.getFontMetrics(rowNames.getFont()).stringWidth(numNames2[i][0]));
		rowNames.getColumnModel().getColumn(0).setPreferredWidth(8 + (int)maxWidth);    // We'll do about 3/4 the maximum theoretical limit, which seems to be a good outer bound for most patch names, plus a tiny bit of slop.

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

        updateUndoRedo();

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
                return getLibrary().getPatch(getLibrary().getSynth().getModel());
                }
                                
            public void setValueAt(Object value, int row, int col) 
                {
                Patch patch = (Patch)value;     
                
                if (patch == null) return;                      // I don't like the idea of dragging null to a patch, and it "transforms" into "UNTITLED"
                // patch = getLibrary().getInitPatch();
                        
                // do we need to modify the bank and number?
                synth.undo.push(synth.model);
                synth.undo.setWillPush(false);
                boolean send = synth.getSendMIDI();
                synth.setSendMIDI(false);
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
                synth.sendAllParameters();
                                
                fireTableCellUpdated(row, col);
                }
            });
                        
        patchWell.setTableHeader(null);
        patchWell.setRowSelectionAllowed(false);
        patchWell.setColumnSelectionAllowed(false);
        patchWell.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        patchWell.setDefaultEditor(Object.class, null);
        
        // Enable Drag and Drop
        patchWell.setDragEnabled(true);
        patchWell.setDropMode(DropMode.ON);
        patchWell.setTransferHandler(new LibrarianTransferHandler(getLibrary().synth)); 
        
        JLabel patchWellLabel = new JLabel("Current Patch: ");
        patchWellLabel.putClientProperty("JComponent.sizeVariant", "small");
        patchWellLabel.setFont(Style.SMALL_FONT());

        HBox box = new HBox();
        box.setBackground(getBackground());
                
        stopAction = new PushButton("Stop Download")
            {
            public void perform()
                {
                getLibrary().getSynth().stopBatchDownload();
                }
            };
        stopAction.getButton().setEnabled(false);
        setupButton(stopAction.getButton());
        box.add(stopAction.getButton());
                        
        box.add(patchWellLabel);
    
        Box vbox = new Box(BoxLayout.Y_AXIS);
        vbox.setBackground(getBackground());
        JComponent glue1 = Stretch.makeStretch();
        glue1.setBackground(getBackground());
        JComponent glue2 = Stretch.makeStretch();
        glue2.setBackground(getBackground());
        vbox.add(glue1);
        vbox.add(patchWell);
        vbox.add(glue2);

        box.addLast(vbox);
        add(box, BorderLayout.SOUTH);
        
        if (getLibrary().getSynth().patchTimer != null)  // we're running!
            {
            //bottomPanel.add(stopAction.getButton(), BorderLayout.WEST);
            stopAction.getButton().setEnabled(true);
            }
            
        /// Fix Nimbus, which doesn't display grid lines properly
        /// See https://forums.oracle.com/ords/apexds/post/linux-nimbus-lookandfeel-table-grid-lines-are-not-coming-2197
        /// NOTE: This doesn't work on Linux.  I have to override the grid lines entirely, see elsewhere
        if (Style.isNimbus())
            {
            try 
                {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) 
                    {
                    if (info.getName().equals("Nimbus")) 
                        {
                        UIManager.setLookAndFeel(info.getClassName());
                        UIDefaults defaults = UIManager.getLookAndFeelDefaults();
                        // This is supposed to work but doesn't
                        defaults.put("Table.gridColor", new javax.swing.plaf.ColorUIResource(GRID_COLOR));
                        // This seems to work okay
                        defaults.put("Table.background", new javax.swing.plaf.ColorUIResource(BACKGROUND_COLOR));
                        defaults.put("Table.disabled", false);
//                                              defaults.put("Table.showGrid", false);
//                                              defaults.put("Table.intercellSpacing", new Dimension(1, 1));
/*
  defaults.put("TableHeader.background", new javax.swing.plaf.ColorUIResource(BACKGROUND_COLOR));
  defaults.put("TableHeader.cellBorder", BorderFactory.createMatteBorder(0,0,1,0, GRID_COLOR));
*/
                        break;
                        }
                    }
                } 
            catch (Exception e) { }
            }
        else
            {
            UIManager.put("TableHeader.cellBorder",
                BorderFactory.createMatteBorder(0,0,1,0, GRID_COLOR));
            }    
        }    
        
        
    public void loadOneInternal()
        {
        int column = col(table, table.getSelectedColumn());
        int row = table.getSelectedRow();
        if (column >= 0 && row >= 0)
            {
            copy(Librarian.this, table, column, row, 1, Librarian.this, patchWell, 0, 0, true);
            getLibrary().getSynth().setCurrentTab(0);
            }
        }

    public void loadOneExternal()
        {
        int column = col(table, table.getSelectedColumn());
        int row = table.getSelectedRow();
        if (column >= 0 && row >= 0)
            {
            Synth synth = getLibrary().getSynth();
            Patch p = getLibrary().getPatch(column - 1, row);
            if (p == null) p = getLibrary().getInitPatch();
                        
            Synth otherSynth = synth.instantiate(synth.getClass(), false, true, null);
            otherSynth.setSendMIDI(false);
            otherSynth.undo.setWillPush(false);

            // this last statement fixes a mystery.  When I call Randomize or Reset on
            // a Blofeld or on a Microwave, all of the widgets update simultaneously.
            // But on a Blofeld Multi or Microwave Multi they update one at a time.
            // I've tried a zillion things, even moving all the widgets from the Blofeld Multi
            // into the Blofeld, and it makes no difference!  For some reason the OS X
            // repaint manager is refusing to coallesce their repaint requests.  So I do it here.
            otherSynth.repaint();
                
            try
                {
                otherSynth.performParse(synth.flatten(p.sysex), true);
                                
                int bank = (column - 1);
                int number = row;
                if (bank != -1)                 // don't revise the patch location if it's the scratch bank
                    {
                    // revise the patch location to where it came from in the librarian
                    synth.getModel().set("number", number);
                    int b = synth.getModel().get("bank", -1);
                    if (b != -1)
                        synth.getModel().set("bank", bank);
                    }
                }
            catch (Exception ex)
                {
                Synth.handleException(ex);
                }

            otherSynth.undo.setWillPush(true);
            otherSynth.setSendMIDI(true);

            if (otherSynth.getSendsParametersAfterLoad()) // we'll need to do this
                otherSynth.sendAllParameters();

            otherSynth.updateTitle();       // so it shows the right filename
            }
        }

    public static void setLibrarianMenuSelected(JMenu menu, boolean val, Synth synth)
        {
        // ugh, accessign subelements is so convoluted
        MenuElement[] m = menu.getSubElements()[0].getSubElements();
        for(int i = 0; i < m.length; i++)
            {
            if (m[i] instanceof JMenuItem)
                {
                if ((m[i] == synth.downloadMenu ||
                        m[i] == synth.downloadAllMenu ||
                        m[i] == synth.downloadBankMenu) && !synth.getSupportsDownloads())
                    continue;
                if ((m[i] == synth.writeMenu) && !synth.getSupportsPatchWrites())
                    continue;
                                        
                if (((JMenuItem)m[i]) != synth.mixAgainMenu &&
                    ((JMenuItem)m[i]) != synth.hideLibrarianMenu)
                    ((JMenuItem)m[i]).setEnabled(val);
                }
            }
        }
        
    public static JMenu buildLibrarianMenu(Synth synth)
        {
        JMenu menu = new JMenu("Librarian");

//      menu.add(openMenu);

        // menu.addSeparator();

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
        synth.downloadMenu = item;
        item.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt) { synth.librarian.download(); }
            });
        menu.add(item);
        item.setEnabled(false);
                
        item = new JMenuItem("Download Bank from Synth");
        synth.downloadBankMenu = item;
        item.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt) { synth.librarian.downloadBank(); }
            });
        menu.add(item);
        item.setEnabled(false);
                
        item = new JMenuItem("Download All Patches from Synth");
        synth.downloadAllMenu = item;
        item.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt) { synth.librarian.downloadAll(); }
            });
        menu.add(item);
        item.setEnabled(false);
                
        menu.addSeparator();

        boolean requests = false;
        item = new JMenuItem("Request Bank from Synth");
        item.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt) 
                {
                int requestableBank = synth.getRequestableBank();
                if (requestableBank == -1)              // "All Banks can be Requestable"
                    {
                    String[] bankNames = synth.getBankNames();
                    int bankIndex = 0;
                    if (bankNames == null || bankNames.length == 1)                 // there's only one bank
                        {
                        if (!synth.showSimpleConfirm("Bank Request", "Request Bank?", "Request")) return;
                        }
                    else                    // multiple banks, must chooes
                        {
                        JComboBox bank = new JComboBox(bankNames); 
                        Librarian lib = synth.librarian;
                                
                        // Let's make a good guess as to which bank he wants
                        int index = 0;
                        int column = lib.col(lib.table, lib.table.getSelectedColumn());
                        int row = lib.table.getSelectedRow();
                        int len = lib.table.getSelectedRowCount();
                                                
                        if (column < 0 || row < 0 || len == 0) // nope
                            {
                            index = 0;
                            }
                        else if (column == 0)   // scratch
                            {
                            index = 0;
                            }
                        else
                            {
                            index = column - 1;
                            }

                        bank.setSelectedIndex(index);
                                
                        boolean result = Synth.showMultiOption(synth, new String[] { "Bank" }, 
                            new JComponent[] { bank }, "Bank Request", "Enter the Bank.");
                        
                        if (result == false) return;
                        else bankIndex = bank.getSelectedIndex();
                        }
                                                        
                    byte[] data = synth.requestBankDump(bankIndex);
                    if (data != null)               // this should always be true
                        {
                        synth.tryToSendSysex(data);
                        }
                    }
                else                            // "Only a specific Bank" -- must let the user know
                    {
                    if (synth.showSimpleConfirm("Bank Request", "Request Bank " + synth.librarian.getLibrary().getBankName(requestableBank) + "?\nOnly this bank can be requested.", "Request"))
                        {
                        byte[] data = synth.requestBankDump(requestableBank);
                        if (data != null)
                            {
                            synth.tryToSendSysex(data);
                            }
                        }
                    }
                }
            });
        
        if (synth.requestBankDump(0) != null)   // we have bank dump requests
            {
            menu.add(item);
            item.setEnabled(false);
            requests = true;
            }
                        
        item = new JMenuItem("Request All Patches from Synth");
        item.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt) 
                {
                if (synth.showSimpleConfirm("All Patches", "Request All Patches from the Synth?", "Request"))
                    {
                    byte[] data = synth.requestAllDump();
                    if (data != null)
                        {
                        synth.tryToSendSysex(data);
                        }
                    }
                }
            });
        
        if (synth.requestAllDump() != null)     // we have all-dump requests
            {
            menu.add(item);
            item.setEnabled(false);
            requests = true;
            }

        if (requests)
            {
            menu.addSeparator();
            }
                        

        item = new JMenuItem("Write Selected Patches to Synth");
        synth.writeMenu = item;
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
        
        item = new JMenuItem("Save Selected Patches...");
        item.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt) { synth.librarian.save(); }
            });
        menu.add(item);
        item.setEnabled(false);
                
        item = new JMenuItem("Save Bank...");
        item.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt) { synth.librarian.saveBank(); }
            });
        menu.add(item);
        item.setEnabled(false);
                
        item = new JMenuItem("Save All Patches...");
        item.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt) { synth.librarian.saveAll(); }
            });
        menu.add(item);
        item.setEnabled(false);
                
        item = new JMenuItem("Export Names to Text...");
        item.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt) 
                { 
                doSaveNamesToText(synth);
                }
            });
        menu.add(item);
        item.setEnabled(false);

        menu.addSeparator();            

        item = new JMenuItem("Edit Patch in This Editor");
        item.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt) 
                { 
                synth.librarian.loadOneInternal(); 
                }
            });
        menu.add(item);
        item.setEnabled(false);

        item = new JMenuItem("Edit Patch in a New Editor");
        item.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt) 
                { 
                synth.librarian.loadOneExternal(); 
                }
            });
        menu.add(item);
        item.setEnabled(false);
        
        menu.addSeparator();
        
        item = new JMenuItem("To Nudge Targets");
        item.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt) 
                { 
                synth.librarian.toNudgeTargets(); 
                }
            });
        menu.add(item);
        item.setEnabled(false);

        item = new JMenuItem("To Morph Targets");
        item.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt) 
                { 
                synth.librarian.toMorphTargets(); 
                }
            });
        menu.add(item);
        item.setEnabled(false);

        item = new JMenuItem("To Hill Climber");
        item.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt) 
                { 
                synth.librarian.toHillClimber(); 
                }
            });
        menu.add(item);
        item.setEnabled(false);

        menu.addSeparator();
        item = new JMenuItem("Mix Uniformly");
        item.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt) 
                { 
                synth.librarian.mix(Librarian.MIX_TYPE_UNIFORM); 
                }
            });
        menu.add(item);
        item.setEnabled(false);

        item = new JMenuItem("Mix Light Bias");
        item.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt) 
                { 
                synth.librarian.mix(Librarian.MIX_TYPE_ONE_THIRD); 
                }
            });
        menu.add(item);
        item.setEnabled(false);

        item = new JMenuItem("Mix Medium Bias");
        item.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt) 
                { 
                synth.librarian.mix(Librarian.MIX_TYPE_ONE_HALF); 
                }
            });
        menu.add(item);
        item.setEnabled(false);

        item = new JMenuItem("Mix Heavy Bias");
        item.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt) 
                { 
                synth.librarian.mix(Librarian.MIX_TYPE_TWO_THIRDS); 
                }
            });
        menu.add(item);
        item.setEnabled(false);

        item = new JMenuItem("Do Mix Again");
        item.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt) 
                { 
                synth.librarian.mixAgain(); 
                }
            });
        menu.add(item);
        synth.mixAgainMenu = item;
        item.setEnabled(true);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | InputEvent.SHIFT_MASK));

              
        item = new JMenuItem("Remix Bank");
        item.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt) 
                { 
                synth.librarian.mixBank(Librarian.MIX_TYPE_ONE_HALF); 
                }
            });
        menu.add(item);
        item.setEnabled(false);

        menu.addSeparator();      
        
        item = new JMenuItem("Hide Librarian");
        item.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt) 
                { 
                synth.hideLibrarian();
                }
            });
        menu.add(item);
        synth.hideLibrarianMenu = item;
        item.setEnabled(true);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | InputEvent.SHIFT_MASK));
        return menu;
        }
        
        
    public static void doSaveNamesToText(Synth synth)
        {
        FileDialog fd = new FileDialog((Frame)(SwingUtilities.getRoot(synth)), "Write All Names to Text File...", FileDialog.SAVE);
        
        String str = synth.getSynthNameLocal();
        if (str == null) str = "Untitled.txt";
        else str = "PatchNames"  + str + ".txt";

        fd.setFile(StringUtility.reviseFileName(str));

        String path = synth.getLastDirectory();
        if (path != null)
            fd.setDirectory(path);
                
        synth.disableMenuBar();
        fd.setVisible(true);
        synth.enableMenuBar();
        File f = null; // make compiler happy
        PrintWriter os = null;
        if (fd.getFile() != null)
            try
                {
                f = new File(fd.getDirectory(), StringUtility.ensureFileEndsWith(fd.getFile(), ".txt"));
                os = new PrintWriter(new FileOutputStream(f));
                String[] names = synth.librarian.getLibrary().getNames(Library.ALL_PATCHES);
                String[] locations = synth.librarian.getLibrary().getPatchLocationNames(Library.ALL_PATCHES);
                for(int i = 0; i < names.length; i++)
                    {
                    os.println(locations[i] + "\t" + names[i]);
                    }
                os.close();
                synth.setLastDirectory(fd.getDirectory());
                } 
            catch (IOException e) // fail
                {
                synth.showErrorWithStackTrace(e, "File Error", "An error occurred while saving to the file " + (f == null ? " " : f.getName()));
                Synth.handleException(e);
                }
            finally
                {
                if (os != null)
                    os.close();
                }
        }
        
        
    /* JTable says that it maintains the proper column values even if the columns are rearranged,
       but this is a lie.  You have to do it manually.  This function does the proper conversion table -> model. */
    static int col(JTable table, int column)
        {
        if (column < 0) return column;
        else return table.getColumnModel().getColumn(column).getModelIndex();
        }


    /** JTable says that it maintains the proper column values even if the columns are rearranged,
        but this is a lie.  You have to do it manually.  This function does the proper conversion model -> table. */
    static int antiCol(JTable table, int index)
        {
        if (index < 0) return index;
        /// UGH, this is O(n).  Seriously, Java has no way to do this?
        TableColumnModel tableModel = table.getColumnModel();
        int numColumns = tableModel.getColumnCount();
        for(int i = 0; i < numColumns; i++)
            {
            if (tableModel.getColumn(i).getModelIndex() == index)
                return i;
            }
        return -1;                      // maybe?
        }

    public void downloadRange()
        {
        getLibrary().getSynth().doGetAllPatches();
        }
                
    /** Downloads all locations. */
    public void downloadAll()
        {
        performDownload(0, 0, getLibrary().getNumBanks() - 1, getLibrary().getBankSize() - 1 );         // wrap-around
        }

    public int getCurrentColumn()
        {
        return col(table, table.getSelectedColumn());
        }
                
    public int getCurrentRow()
        {
        return table.getSelectedRow();
        }
                
    public int getCurrentLength()
        {
        return table.getSelectedRowCount();
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
                column = 1;             // assume it's "the" bank
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

        int bankSize = getLibrary().synth.getValidBankSize(column - 1);
        if (bankSize == -1) bankSize = getLibrary().getBankSize();
        performDownload(column - 1, 0, column - 1, bankSize - 1 );
        }

    public void toNudgeTargets()
        {
        int column = col(table, table.getSelectedColumn());
        int row = table.getSelectedRow();
        int len = table.getSelectedRowCount();
                
        if (column < 0 || row < 0 || len == 0) // nope
            {
            getLibrary().synth.showSimpleError("Cannot Send to Nudge Targets", "Please select up to four patches first.");
            return;
            }

        // First clear all the nudge targets
        for(int i = 0; i < 4; i++)
            {
            getLibrary().synth.doSetNudgeEmpty(i, getLibrary().getModel(0, -1));            // init patch
            }
                        
        // Next load the targets
        for(int i = 0; i < Math.min(4, len); i++)
            {
            Patch patch = getLibrary().getPatch(column - 1, row + i);
            getLibrary().synth.doSetNudge(i, getLibrary().getModel(column - 1, row + i));
            }

        getLibrary().getSynth().setCurrentTab(0);
        }

    public void toMorphTargets()
        {
        int column = col(table, table.getSelectedColumn());
        int row = table.getSelectedRow();
        int len = table.getSelectedRowCount();
        Synth synth = getLibrary().synth;
        
        if (column < 0 || row < 0 || len == 0) // nope
            {
            synth.showSimpleError("Cannot Send to Morph Targets", "Please select up to four patches first.");
            return;
            }
            
        // First make sure the morph tab is open
        if (!synth.morphing) synth.doMorph();
        
        Morph morph = synth.morph;

        // Now clear the buttons
        for(int i = 0; i < 4; i++)
            morph.clear(i);

        // Now set the buttons
        for(int i = 0; i < Math.min(4, len); i++)
            morph.set(i, getLibrary().getModel(column - 1, row + i));

        synth.tabs.setSelectedComponent(synth.morphPane);
        }


    public void toHillClimber()
        {
        int column = col(table, table.getSelectedColumn());
        int row = table.getSelectedRow();
        int len = table.getSelectedRowCount();
        Synth synth = getLibrary().synth;
        
        if (column < 0 || row < 0 || len == 0) // nope
            {
            synth.showSimpleError("Cannot Send to Hill Climber", "Please select some patches first.");
            return;
            }
            
        // First make sure the hill-climb tab is open
        if (!synth.hillClimbing) synth.doHillClimb();
        
        synth.hillClimb.initialize(null, HillClimb.OPERATION_SEED_FROM_LIBRARIAN);
        
        synth.tabs.setSelectedComponent(synth.hillClimbPane);
        }

    public static final int MIX_TYPE_UNIFORM = 0;
    public static final int MIX_TYPE_ONE_THIRD = 1;
    public static final int MIX_TYPE_ONE_HALF = 2;
    public static final int MIX_TYPE_TWO_THIRDS = 3;

    int lastMixType = -1;
        
    public void mixAgain()
        {
        if (lastMixType == -1)
            {
            getLibrary().synth.showSimpleError("Cannot Mix A Second Time", "Choose a first-time mix type first.");
            return;
            }
        mix(lastMixType);
        }
                
    public void mix(int mixType)
        {
        int column = col(table, table.getSelectedColumn());
        int row = table.getSelectedRow();
        int len = table.getSelectedRowCount();
        Synth synth = getLibrary().synth;
        
        if (column < 0 || row < 0 || len == 0) // nope
            {
            synth.showSimpleError("Cannot Mix", "Please select some patches first.");
            return;
            }

        if (len == 1) // nope
            {
            synth.showSimpleError("Cannot Mix", "Mixing a single patch does nothing.  Mix two or more!");
            return;
            }
 
        // which cells do we use?
        int[] rows = new int[len];
        int[] columns = new int[len];
        for(int i = 0; i < rows.length; i++)
            {
            rows[i] = row + i;
            columns[i] = column;
            }
                        
        Model model = performMix(synth, mixType, columns, rows);
                 
        // Load model
        synth.getUndo().push(synth.getModel());
        synth.undo.setWillPush(false);
        boolean send = synth.getSendMIDI();
        synth.setSendMIDI(false);
        model.copyValuesTo(synth.model);
        synth.setSendMIDI(send);
        synth.undo.setWillPush(true);
        synth.sendAllParameters();

        // Switch to patch editor
        getLibrary().getSynth().setCurrentTab(0);
        lastMixType = mixType;
        }
    
    public static final int NUM_MIX_ROWS = 8;
    
    public void mixBank(int mixType)
        {
        int column = col(table, table.getSelectedColumn());
        int row = table.getSelectedRow();
        int len = table.getSelectedRowCount();
        Library library = getLibrary();
        Synth synth = library.synth;

        if (column < 0 || row < 0 || len == 0)
            {
            synth.showSimpleError("Cannot Remix Bank", "Please select a patch in a bank (except the Scratch bank).");
            return;
            }
        else if (column < 1)
            {
            synth.showSimpleError("Select Another Bank", "Please select a patch in a bank (except the Scratch bank).");
            return;
            }
        
        library.pushUndo();
        int banksize = library.patches[0].length;
        String[] names = library.getPatchNumberNames();
        for(int i = 0; i < banksize; i++)
            {
            int[] columns = new int[banksize < NUM_MIX_ROWS ? banksize : NUM_MIX_ROWS];
            int[] rows = new int[columns.length];
            for(int j = 0; j < columns.length; j++)
                {
                columns[j] = column;
                while(true)
                    {
                    rows[j] = synth.random.nextInt(banksize);
                                
                    // make sure it's not the same as past rows
                    boolean passed = true;
                    for(int k = 0; k < j; k++)
                        {
                        if (rows[j] == rows[k])
                            {
                            passed = false;
                            break;
                            }
                        }
                    if (passed)
                        {
                        break;
                        }
                    }
                }
                
            Model result = performMix(synth, mixType, columns, rows);
                
            // can we set a name even if the synth doesn't support it?
            if (result.exists("name"))
                {
                result.set("name", "" + names[i] + "Remix"); 
                        
                // revise name
                synth.undo.setWillPush(false);
                boolean send = synth.getSendMIDI();
                synth.setSendMIDI(false);
                boolean shouldUpdate = synth.model.getUpdateListeners();
                synth.model.setUpdateListeners(false);
                Model backup = synth.model;
                        
                synth.model = result;
                synth.revise();
                
                synth.model = backup;
                synth.model.setUpdateListeners(shouldUpdate);
                synth.setSendMIDI(send);
                synth.undo.setWillPush(true);
                }
                        
            library.setPatch(library.getPatch(result), Library.SCRATCH_BANK, i);
            }
        }
   
    public Model performMix(Synth synth, int mixType, int[] columns, int[] rows)
        {
        Model model = null;
        String[] mutationKeys = synth.getMutationKeys();
        double probability = 1.0;
        
        // Recombination is odd: we pick a weighted point p between the original a and the new b, and
        // then pick a random point BETWEEN a and p.  Thus if we weight ALL the way to b, we're still
        // only doing 50% recombination.  But below I want to have specific probabilities of "recombination",
        // so I'm using crossover instead, passing in 'false' so it uses the true crossover probability and not
        // halving it.
        
        if (mixType == MIX_TYPE_UNIFORM)
            {
            // descending -- we start with the top patch
            int elt = 0;
            model = getLibrary().getModel(columns[0] - 1, rows[0]).copy();          // we don't want the listeners etc.
            for(int i = 1; i < rows.length; i++)
                {
                if (i == rows.length - 1) // last one, mix half/half
                    probability /= 2.0; 
                else            // 1/2 -> 1/3 -> 1/4 -> 1/5 etc.
                    probability = 1.0 / ((1.0 / probability) + 1);
                model = model.crossover(synth.random, getLibrary().getModel(columns[i] - 1, rows[i]), mutationKeys, probability, false);
                }
            }
        else
            {
            // ascending -- start with the bottom patch and recombine till we get to the big patch up top
            model = getLibrary().getModel(columns[rows.length - 1] - 1, rows[rows.length - 1]).copy();                // we don't want the listeners etc.
            for(int i = rows.length - 2; i >= 0; i--)
                {
                if (i == rows.length - 2) // first one, mix half/half
                    probability = 1.0 / 2.0;
                else if (mixType == MIX_TYPE_ONE_THIRD)
                    probability = 1.0 / 3.0;
                else if (mixType == MIX_TYPE_ONE_HALF)
                    probability = 1.0 / 2.0;
                else if (mixType == MIX_TYPE_TWO_THIRDS)
                    probability = 2.0 / 3.0;
                model = model.crossover(synth.random, getLibrary().getModel(columns[i] - 1, rows[i]), mutationKeys, probability, false);
                }
            }
        return model;
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
        Synth synth = getLibrary().getSynth();
        
        if (!synth.getSupportsDownloads())
            {
            synth.showSimpleError("Cannot Download", "Edisyn cannot request patches from this synthesizer.");
            return;
            }
                
        boolean hasBanks = (synth.getBankNames() != null);
        if (hasBanks)
            {
            synth.doGetPatchesForLibrarian(bank1, patch1, bank2, patch2);
            }
        else
            {       
            synth.doGetPatchesForLibrarian(PatchLocation.NO_BANK, patch1, PatchLocation.NO_BANK, patch2);
            }
        }


    /** Writes all locations. */
    public void writeAll()
        {
        Synth synth = getLibrary().getSynth();
        
        if (synth.showSimpleConfirm("Write All", "Write All Patches to Synthesizer?", "Write"))
            {
            getLibrary().writeRange(Library.ALL_PATCHES, 0, 0);
            }
        }

    /** Writes all locations in bank. */
    public void writeBank()
        {
        Synth synth = getLibrary().getSynth();
        
        int column = col(table, table.getSelectedColumn());
        int row = table.getSelectedRow();
        int len = table.getSelectedRowCount();
                        
        if (column < 0 || row < 0 || len == 0) // nope
            {
            if (getLibrary().getNumBanks() == 1)
                {
                column = 1;             // assume it's "the" bank
                }
            else
                {
                synth.showSimpleError("Cannot Write", "Please select a patch in the bank to write first.");
                return;
                }
            }
            
        if (column == 0)
            {
            synth.showSimpleError("Cannot Write", "Edisyn cannot write patches from the scratch bank.\nSelect another bank.");
            return;
            }
                        
        if (synth.showSimpleConfirm("Write Bank", "Write Bank to Synthesizer?", "Write"))
            {
            getLibrary().writeBank(column - 1);
            }
        }


    public static final int LARGE_REGION = 10;
        
    /** Writes the selected locations. */
    public void write()
        {
        Synth synth = getLibrary().getSynth();
        
        int column = col(table, table.getSelectedColumn());
        int row = table.getSelectedRow();
        int len = table.getSelectedRowCount();
                        
        if (column < 0 || row < 0 || len == 0) // nope
            {
            synth.showSimpleError("Cannot Write", "Please select a patch or patch range to write first.");
            return;
            }
            
        if (column == 0)
            {
            synth.showSimpleError("Cannot Write", "Edisyn cannot write patches from the scratch bank.\nSelect another bank.");
            return;
            }

        if (!synth.getSupportsPatchWrites())
            {
            synth.showSimpleError("Not Supported", "Edisyn cannot write arbitrary patches to this synthesizer.");
            }
        else if (len == 1)
            {
            if (synth.showSimpleConfirm("Write Patch", "Write Patch to Synthesizer?", "Write"))
                getLibrary().writeRange(column - 1, row, len);
            }
        else if (len < LARGE_REGION)
            {
            if (synth.showSimpleConfirm("Write Selected Region", "Write Selected Region to Synthesizer?", "Write"))
                getLibrary().writeRange(column - 1, row, len);
            }
        else
            {
            if (synth.showSimpleConfirm("Write Selected Region", "Write Selected Region to Synthesizer?", "Write"))
                getLibrary().writeRange(column - 1, row, len);
            }
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
                column = 1;             // assume it's "the" bank
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
                                
        fill(table, column, row, len, null);            // getLibrary().getInitPatch());
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
                column = 1;             // assume it's "the" bank
                }
            else
                {
                getLibrary().synth.showSimpleError("Cannot Clear", "Please select a patch in the bank to clear first.");
                return;
                }
            }

        // we clear all the bank column even if some of it is invalid...                                
        fill(table, column, 0, getLibrary().getBankSize(), null);               // getLibrary().getInitPatch());
        }

    public void pushUndo()
        {
        getLibrary().pushUndo();
        updateUndoRedo();
        }

    /** Clears all. */
    public void clearAll()
        {
        clearAll(true);
        }
        
    public void clearAll(boolean pushUndo)
        {
        if (pushUndo)
            {
            getLibrary().pushUndo();
            updateUndoRedo();
            }

        for(int i = 0; i < getLibrary().getNumBanks() + 1; i++)                     
            {
            fill(table, i, 0, getLibrary().getBankSize(), null);            // getLibrary().getInitPatch());
            }
        }

    /** Sets all the values of a given set of locations to copies of a certain value. */
    public void fill(JTable table, int col, int row, int len, Patch val)
        {
        int anticol = antiCol(table, col);
                
        for(int i = row; i < row + len; i++)
            {
            table.setValueAt(val == null ? null : new Patch(val), i, anticol);
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
            Synth toSynth = to.getLibrary().getSynth();
            toSynth.getUndo().push(toSynth.getModel());
            }
        
        int antiFrom = antiCol(fromTable, fromCol);
        int antiTo = antiCol(toTable, toCol);

        Patch p = null;
        for(int i = 0; i < len; i++)
            {
            Patch _from = (Patch)(fromTable.getValueAt(fromRow + i, antiFrom));
            if (duplicate)
                {
                p = (_from == null ? null : new Patch(_from));
                }
            else
                {
                p = _from;  
                }
            
            if (!isPatchWell(fromTable) && p != null)
                {
                // assign to the new location
                int bank = (toCol - 1);
                int number = toRow;
                
                // but if we're transferring out, use the from location
                if (isPatchWell(toTable))
                    {
                    bank = (fromCol - 1);
                    number = fromRow;
                    }
                    
                if (bank != -1)                 // don't revise the patch location if it's the scratch bank
                    {
                    // revise the patch location to where it came from in the librarian
                    p.number = number;
                    p.bank = bank;
                    }
                }
        
            // Now that the patch number and bank are set, we can copy them over
            // (We have to set them first or else they won't show up if we are double-clicking or
            // dragging to the patch well).
            toTable.setValueAt(p, toRow + i, antiTo);
            }
 
           
        // Change the selection
        fromTable.clearSelection();
        toTable.clearSelection();
        toTable.changeSelection(toRow, antiTo, false, false);
        toTable.changeSelection(toRow + len - 1, antiTo, false, true);          // not sure why we need to do -1, but we do
        } 
                
    /** Moves one set of locations to another, clearing the original locations. */
    public void move(Librarian from, JTable fromTable, int fromCol, int fromRow, int len, Librarian to, JTable toTable, int toCol, int toRow)
        {
        if (!isPatchWell(fromTable))            // as opposed to patchWell
            {
            if (fromTable != toTable)               // don't double-undo
                {
                from.getLibrary().pushUndo();
                from.updateUndoRedo();
                }
            }

        copy(from, fromTable, fromCol, fromRow, len, to, toTable, toCol, toRow, false);
        fill(fromTable, fromCol, fromRow, len, null);

        int antiTo = antiCol(toTable, toCol);

        // Change the selection
        fromTable.clearSelection();
        toTable.clearSelection();
        toTable.changeSelection(toRow, antiTo, false, false);
        toTable.changeSelection(toRow + len - 1, antiTo, false, true);          // not sure why we need to do -1, but we do
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
            Synth toSynth = to.getLibrary().getSynth();
            toSynth.getUndo().push(toSynth.getModel());
            }

        if (!isPatchWell(fromTable))            // as opposed to patchWell
            {
            if (fromTable != toTable)               // don't double-undo
                {
                from.getLibrary().pushUndo();
                from.updateUndoRedo();
                }
            }
                        
        int antiTo = antiCol(toTable, toCol);
        int antiFrom = antiCol(fromTable, fromCol);
                
        for(int i = 0; i < len; i++)
            {
            Object obj = toTable.getValueAt(toRow + i, antiTo);
            Object with = fromTable.getValueAt(fromRow + i, antiFrom);
            toTable.setValueAt(with, toRow + i, antiTo);
            fromTable.setValueAt(obj, fromRow + i, antiFrom);
            }

        // Change the selection
        fromTable.clearSelection();
        toTable.clearSelection();
        toTable.changeSelection(toRow, antiTo, false, false);
        toTable.changeSelection(toRow + len - 1, antiTo, false, true);          // not sure why we need to do -1, but we do
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

        public static Transferable buildTransferable(final PatchLocationSet locationSet)
            {
            return new Transferable()
                {
                public Object getTransferData(DataFlavor flavor) 
                    {
                    if (flavor.equals(localObjectFlavor))
                        return locationSet;
                    else
                        return null;
                    }
                                
                public DataFlavor[] getTransferDataFlavors() 
                    {
                    return new DataFlavor[] { localObjectFlavor };
                    }

                public boolean isDataFlavorSupported(DataFlavor flavor) 
                    {
                    return (flavor.equals(localObjectFlavor));
                    }
                };
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
                //return new DataHandler(new PatchLocationSet(synth, table, 0, 0, 1), localObjectFlavor.getMimeType());
                return buildTransferable(new PatchLocationSet(synth, table, 0, 0, 1));
                }
            else
                {
                //return new DataHandler(new PatchLocationSet(synth, table, Librarian.col(table, table.getSelectedColumn()), table.getSelectedRow(), table.getSelectedRowCount()), localObjectFlavor.getMimeType());
                return buildTransferable(new PatchLocationSet(synth, table, Librarian.col(table, table.getSelectedColumn()), table.getSelectedRow(), table.getSelectedRowCount()));
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
            int toRow = dl.getRow() - (fromTable.getSelectionModel().getLeadSelectionIndex() - fromRow);                // gotta offset by where we dragged
            if (isPatchWell(fromTable)) toRow = dl.getRow();                        // because its selectionIndex is -1
      
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
            // find the to-librarian
            JTable toTable = (JTable)(info.getComponent());
            JComponent c = toTable;
            while(!(c instanceof Librarian))
                c = (JComponent)(c.getParent());
            Librarian to = (Librarian) c;

            // reset the cursor
            toTable.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

            // let's just call this again to double check
            if (!canImport(info)) return false;
          
            JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();

            try 
                {
                PatchLocationSet pls = (PatchLocationSet)(info.getTransferable().getTransferData(localObjectFlavor));
                          
                // find the from-librarian
                JTable fromTable = pls.table;
                c = fromTable;
                while(!(c instanceof Librarian))
                    c = (JComponent)(c.getParent());
                Librarian from = (Librarian) c;
                int fromCol = pls.column;
                int fromRow = pls.row;
                int len = pls.length;
                int toCol = Librarian.col(toTable, dl.getColumn());
                int toRow = dl.getRow() - (fromTable.getSelectionModel().getLeadSelectionIndex() - fromRow);            // gotta offset by where we dragged
                if (isPatchWell(fromTable)) toRow = dl.getRow();                // because its selectionIndex is -1 

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

    void warnLibrarian(Synth synth)
        {
        if (synth.librarianTested()) return;
        
        if (!synth.getLastXAsBoolean("LibrarianWarned", synth.getSynthClassName(), false, true))
            {
            synth.showSimpleMessage("Librarian Untested", 
                "The Librarian for the " + synth.getSynthNameLocal() + " has not yet been tested because\n" +
                "I no longer have a unit.  It may not work. If you have this synth, help me test it.\n" + 
                "Send mail to sean@cs.gmu.edu");
            synth.setLastX("" + true, "LibrarianWarned", synth.getSynthClassName(), true);
            }
        }
    }
