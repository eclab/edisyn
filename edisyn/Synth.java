/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn;

import edisyn.gui.*;
import edisyn.synth.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.border.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.lang.reflect.*;
import javax.sound.midi.*;
import java.io.*;


/**** 
      Abstract super class of synth editor panes.  To implement an editor, you'll need
      to write several methods.  Note that one method you need to write is <i>static</i>,
      but you'll still need to write a static version for your particular subclass:
      <tt> public static boolean recognize(byte[] data)

      <p>This class contains a number of constants which dictate the look and feel of the
      system, as well as the file associated with your synth, the underlying parameter model,
      the MIDI transmitter and receiver, the menu facility, and other stuff you'll need to
      build your editor.

      @author Sean Luke
*/

public abstract class Synth extends JComponent implements Updatable
    {
    public static int numOpenWindows = 0;
    
    // The model proper
    protected Model model;
    // Our own private random number generator
    protected Random random;
    // flag for whether we send midi when requested
    boolean sendMIDI = false;
    // The file associated with the synth
    File file;
    // will the next load be a merge?  If 0, we're not merging.  Else it's the merge probability.
    double merging = 0.0;

    public Midi.Tuple tuple;
    
    public JMenuItem transmit;
    public JMenuItem transmitTo;
    public JMenuItem transmitCurrent;
    public JMenuItem receive;
    
    public Undo undo = new Undo();
    
    public CCMap ccmap;
    public int lastCC = -1;
        
    //public static final Class[] synths = new Class[] { Matrix1000.class, Blofeld.class, BlofeldMulti.class, MicrowaveXT.class, MicrowaveXTMulti.class };
    //public static final String[] synthNames = { "Oberheim Matrix 1000", "Waldorf Blofeld", "Waldorf Blofeld [Multi]", "Waldorf Microwave II/XT/XTk", "Waldorf Microwave II/XT/XTk [Multi]" };

    public static final Class[] synths = new Class[] { Blofeld.class, BlofeldMulti.class, MicrowaveXT.class, MicrowaveXTMulti.class };
    public static final String[] synthNames = { "Waldorf Blofeld", "Waldorf Blofeld [Multi]", "Waldorf Microwave II/XT/XTk", "Waldorf Microwave II/XT/XTk [Multi]" };
                  
    boolean synced = false;
    
    public void setSynced(boolean val)
        {
        synced = val;
        for(int i = 0; i < patchDisplays.size(); i++)
            ((PatchDisplay)(patchDisplays.get(i))).update("whatever", getModel());
        updateTitle();
        }
    public boolean isSynced() { return synced; }
        
    ArrayList patchDisplays = new ArrayList();
    public void addPatchDisplay(PatchDisplay display)
        {
        patchDisplays.add(display);
        }
    
    /** Handles mutation (or not) of keys declared immutable in the Model.
        When the user is mutating (randomizing) a parameter, and it was declared
        immutable, this method is called instead to let you change the parameter
        as you like (or refuse to). */
    public abstract void immutableMutate(String key);
        
    /** Updates the model to reflect the following sysex patch dump for your synthesizer type. 
        If the patch contains information sufficient to sync, return TRUE, else return FALSE. */
    public abstract boolean parse(byte[] data, boolean ignorePatch);
    
    /** Updates the model to reflect the following sysex or CC (or other!) message from your synthesizer. 
        You are free to IGNORE this message entirely.  Patch dumps will generally not be sent this way; 
        and furthermore it is possible that this is crazy sysex from some other synth so you need to check for it.  */
    public abstract void parseParameter(byte[] data);
    
    /** Merges the given model into yours, replacing elements of your model with the given
        probability. */
    public abstract void merge(Model model, double probability);
    
    /** Produces a sysex patch dump suitable to send to a remote synthesizer. 
        If you return a zero-length byte array, nothing will be sent.  
        If tempModel is non-null, then it should be used to extract meta-parameters
        such as the bank and patch number (stuff that's specified by gatherInfo(...).
        Otherwise the primary model should be used.  The primary model should be used
        for all other parameters.  toWorkingMemory indicates whether the patch should
        be directed to working memory of the synth or to the patch number in tempModel. */
    public abstract byte[] emit(Model tempModel, boolean toWorkingMemory);
    
    /** Produces a sysex parameter change request for the given parameter.  
        If you return a zero-length byte array, nothing will be sent.  */
    // TODO: maybe we should also permit CC and NRPN
    public abstract byte[] emit(String key);
    
    /** Produces a sysex message to send to a synthesizer to request it to initiate
        a patch dump to you.  If you return a zero-length byte array, nothing will be sent. 
        If tempModel is non-null, then it should be used to extract meta-parameters
        such as the bank and patch number or machine ID (stuff that's specified by gatherInfo(...).
        Otherwise the primary model should be used.  The primary model should be used
        for all other parameters.  
    */
    public abstract byte[] requestDump(Model tempModel);
    
    /** Produces a sysex message to send to a synthesizer to request it to initiate
        a patch dump to you for the CURRENT PATCH.  If you return a zero-length byte array, 
        nothing will be sent.  If tempModel is non-null, then it should be used to extract 
        meta-parameters such as the machine ID (stuff that's specified by gatherInfo(...).
        Otherwise the primary model should be used.  The primary model should be used
        for all other parameters.  
    */
    public abstract byte[] requestCurrentDump(Model tempModel);
    
    /** Returns the expected length of a sysex patch dump for your type of synthesizer. */
    public abstract int getExpectedSysexLength();

    /** Gathers meta-parameters from the user via a JOptionPane, such as 
        patch number and bank number, which are used to specify where a patch
        should be saved to or loaded from.  These are typically also stored in
        the primary model, but the user may want to change them so as to 
        write out to a different location for example.  The model should not be
        revised to hold the new values; but rather they should be placed into tempModel.
        This method returns TRUE if the user provided the values, and FALSE
        if he cancelled.
    */
    public abstract boolean gatherInfo(String title, Model tempModel);

    /** Create your own Synth-specific class version of this static method.
        It will be called when the system wants to know if the given sysex patch dump
        is for your kind of synthesizer.  Return true if so, else false. */
    private static boolean recognize(byte[] data)
        {
        return false;
        }
        
    /** Returns the current file associated with this editor, or null if there is none. */
    public File getFile() { return file; }
        
    /** Sets whether sysex parameter changes should be sent in response to changes to the model.
        You can set this to temporarily paralleize your editor when updating parameters. */
    public void setSendMIDI(boolean val) { sendMIDI = val; }

    /** Gets whether sysex parameter changes should be sent in response to changes to the model. */
    public boolean getSendMIDI() { return sendMIDI; }
        
    /** Returns the model associated with this editor. */
    public Model getModel() { return model; }
        
    /** Returns the name of the synthesizer */
    public abstract String getSynthName();
    
    /** Returns the name of the current patch, or null if there is no such thing. */
    public abstract String getPatchName();
    
    /** Return true if the window can be closed and disposed of. You should do some cleanup
        as necessary (the system will handle cleaning up the receiver and transmitters. */
    public abstract boolean requestCloseWindow();
    
        
    /** Updates the JFrame title to reflect the synthesizer type, the patch information, and the filename
        if any.  See Blofeld.updateTitle() for inspiration. */
    public void updateTitle()
        {
        JFrame frame = ((JFrame)(SwingUtilities.getRoot(this)));
        if (frame != null) 
            {
            String synthName = getSynthName().trim();
            String patchName = "        " + (getPatchName() == null ? "" : getPatchName().trim());
            String fileName = (getFile() == null ? "        Untitled" : "        " + getFile().getName());
            String disconnectedWarning = ((tuple == null || tuple.in == null) ? "  DISCONNECTED" : "");
            String outOfSyncWarning = (isSynced() ? "" : "  UNSYNCED");
            String learningWarning = (learning ? "  LEARNING" +
                    (model.getLastKey() != null ? " " + model.getLastKey() + 
                    (model.getRange(model.getLastKey()) > 0 ? "[" + model.getRange(model.getLastKey()) + "]" : "") + 
                    (ccmap.getCCForKey(model.getLastKey()) >= 0 ? "=" + ccmap.getCCForKey(model.getLastKey()) : "")
                    : "") : "");
        
            frame.setTitle(synthName + fileName + "        " + disconnectedWarning + outOfSyncWarning + learningWarning);
            }
        }

    public JTabbedPane tabs = new JTabbedPane();

    public Synth() 
        {
        model = new Model();
        model.register(Model.ALL_KEYS, this);
        model.setUndoListener(undo);
        undo.setWillPush(false);  // instantiate undoes this
        random = new Random(System.currentTimeMillis());
        ccmap = new CCMap(Prefs.getAppPreferences(getSynthName(), "CC"));
        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
        }
        
    
    /** Updates the graphics rendering hints before drawing.  */
    public static void prepareGraphics(Graphics g)
        {
        Graphics2D graphics = (Graphics2D) g;
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        }
        
    /** Returns whether the given sysex patch dump data is of the type for this particular synth.
        This is done by ultimately calling the CLASS method <tt>public static boolean recognize(data)</tt> 
        that your synthesizer subclass is asked to implement. */
    public boolean recognizeLocal(byte[] data)
        {
        return recognize(Synth.this.getClass(), data);
        }
                
    /** Returns whether the given sysex patch dump data is of the type for a synth of the given
        class.  This is done by ultimately calling the CLASS method 
        <tt>public static boolean recognize(data)</tt> that each synthesizer subclass is asked to implement. */
    public static boolean recognize(Class synthClass, byte[] data)
        {
        try
            {
            Method method = synthClass.getMethod("recognize", new Class[] { byte[].class });
            Object obj = method.invoke(null, data);
            return ((Boolean)obj).booleanValue();
            }
        catch (Exception e)
            {
            e.printStackTrace();
            return false;
            }
        }
        
    /** Returns TRUE with the given probability. */
    public boolean coinToss(double probability)
        {
        if (probability==0.0) return false;     // fix half-open issues
        else if (probability==1.0) return true; // fix half-open issues
        else return random.nextDouble() < probability; 
        }

    /** Independently randomizes each parameter with the given probability. 
        If certain parameter values have been declared SPECIAL in the model, then if the
        parameter is to be randomized, then with a 50% one of those values will
        be chosen at random, otherwise one of *any* value will be chosen at random.
        This allows you to bias certain critical parameter values (like "off" versus
        1...100).  Additionally, if a parameter has been declared IMMUTABLE in the model,
        OR if the parameter is a STRING,
        then instead of randomizing the parameter, the method immutableMutate() is called
        instead to allow you to do what you wish with it. */
    public void mutate(double probability)
        {
        undo.push(getModel());
        undo.setWillPush(false);
        String[] keys = model.getKeys();
        for(int i = 0; i < keys.length; i++)
            {
            if (coinToss(probability))
                {
                String key = keys[i];
                if (model.isImmutable(key))
                    immutableMutate(key);
                else if (model.minExists(key) && model.maxExists(key))
                    {
                    int min = model.getMin(key);
                    int max = model.getMax(key);
                    int[] special = model.getSpecial(key);
                    if (special != null && coinToss(0.5))
                        {
                        model.set(key, special[random.nextInt(special.length)]);
                        }
                    else 
                        {
                        model.set(key, random.nextInt(max - min + 1) + min);
                        }
                    }
                else if (model.isString(key))
                    {
                    // can't handle a string
                    immutableMutate(key);
                    }
                }
            }
        undo.setWillPush(true);
        }
    
    /** Called by the model to update the synth whenever a parameter is changed. */
    public void update(String key, Model model)
        {
        if (learning)
            updateTitle();
                
        if (getSendMIDI() && isSynced())
            {
            tryToSendSysex(emit(key));
            }
        }


    /** Builds a receiver to attach to the current IN transmitter.  The receiver
        can handle merging and patch reception. */
    public Receiver buildInReceiver()
        {
        return new Receiver()
            {
            public void close()
                {
                }
                                
            public void send(MidiMessage message, long timeStamp)
                {
                final byte[] data = message.getMessage();
                
                if (SwingUtilities.getRoot(Synth.this) == javax.swing.FocusManager.getCurrentManager().getActiveWindow() &&                
                    recognizeLocal(data))
                    {
                    // I'm doing this in the Swing event thread because I figure it's multithreaded
                    SwingUtilities.invokeLater(new Runnable()
                        {
                        public void run()
                            {
                            if (merging != 0.0)
                                {
                                merging = 0.0;
                                setSendMIDI(false);
                                Synth newSynth = instantiate(Synth.this.getClass(), getSynthName(), true, false, tuple);
                                newSynth.parse(data, true);
                                undo.setWillPush(false);
                                Model backup = (Model)(model.clone());
                                merge(newSynth.getModel(), 0.5);
                                undo.setWillPush(true);
                                if (!backup.keyEquals(getModel()))  // it's changed, do an undo push
                                    undo.push(backup);
                                setSendMIDI(true);
                                sendAllParameters();
                                updateTitle();
                                }
                            else
                                {
                                // we turn off MIDI because parse() calls revise() which triggers setParameter() with its changes
                                setSendMIDI(false);
                                undo.setWillPush(false);
                                Model backup = (Model)(model.clone());
                                boolean canSync = parse(data, false);
                                undo.setWillPush(true);
                                if (!backup.keyEquals(getModel()))  // it's changed, do an undo push
                                    undo.push(backup);
                                setSendMIDI(true);
                                if (canSync)
                                    {
                                    changePatch(getModel());
                                    setSynced(true);
                                    }
                                else 
                                    {
                                    setSynced(false);
                                    }
                                file = null;
                                updateTitle();
                                }
                            }
                        });
                    }
                else                                    // Maybe it's a local Parameter change in sysex or CC?
                    {
                    sendMIDI = false;  // so we don't send out parameter updates in response to reading/changing parameters
                    parseParameter(data);
                    sendMIDI = true;
                    updateTitle();
                    }
                }
            };
        }
        
    /** Builds a receiver to attach to the current KEY transmitter.  The receiver
        can resend all incoming requests to the OUT receiver. */
    public Receiver buildKeyReceiver()
        {
        return new Receiver()
            {
            public void close()
                {
                }
                                
            public void send(MidiMessage message, long timeStamp)
                {
                if (SwingUtilities.getRoot(Synth.this) == javax.swing.FocusManager.getCurrentManager().getActiveWindow() && sendMIDI)
                    {
                    if (message instanceof ShortMessage)
                        {
                        ShortMessage newMessage = null;
                                                
                        // stupidly, ShortMessage has no way of changing its channel, so we have to rebuild
                        ShortMessage s = (ShortMessage)message;
                        int status = s.getStatus();
                        int channel = s.getChannel();
                        int command = s.getCommand();
                        int data1 = s.getData1();
                        int data2 = s.getData2();
                        boolean voiceMessage = ( status < 0xF0 );
                        try
                            {
                            if (voiceMessage)
                                newMessage = new ShortMessage(status, channel, data1, data2);
                            else
                                newMessage = new ShortMessage(status, data1, data2);
                            if (newMessage.getCommand() == ShortMessage.CONTROL_CHANGE)
                                {
                                // we intercept this
                                handleCC(newMessage);
                                }
                            else
                                {
                                tryToSendMIDI(newMessage);
                                }
                            }
                        catch (InvalidMidiDataException e)
                            {
                            e.printStackTrace();
                            }
                        }
                    else if (message instanceof SysexMessage)
                        {
                        tryToSendSysex(message.getMessage());
                        }
                    }
                }
            };
        }


    /** Same as setupMIDI(message, null); */
    public boolean setupMIDI(String message)
        {
        return setupMIDI(message, null);
        }
        
    /** Lets the user set up the MIDI in/out/key devices.  The old devices are provided in oldTuple,
        or you may pass null in if there are no old devices.  Returns TRUE if a new tuple was set up. */
    public boolean setupMIDI(String message, Midi.Tuple oldTuple)
        {
        Midi.Tuple result = Midi.getNewTuple(tuple, this, message, buildInReceiver(), buildKeyReceiver());
        boolean retval = false;
                
        if (result == Midi.FAILED)
            {
            JOptionPane.showOptionDialog(this, "An error occurred while trying to connect to the chosen MIDI devices.",  
                "Cannot Connect", JOptionPane.DEFAULT_OPTION, 
                JOptionPane.WARNING_MESSAGE, null,
                new String[] { "Revert" }, "Revert");
            result = oldTuple;
            }
        else if (result == Midi.CANCELLED)
            {
            // nothing
            }
        else
            {
            retval = true;
            }
                
        if (tuple != null)
            tuple.dispose();
                        
        tuple = result;
        
        setSendMIDI(true);
        updateTitle();
        return retval;
        }


    /** Removes the in/out/key devices. */
    public void disconnectMIDI()
        {
        if (tuple != null)
            {
            if (tuple.in != null && tuple.inReceiver != null)
                tuple.in.removeReceiver(tuple.inReceiver);
            if (tuple.key != null && tuple.keyReceiver != null)
                tuple.key.removeReceiver(tuple.keyReceiver);
            tuple.dispose();
            }
        tuple = null;
        setSendMIDI(true);
        updateTitle();
        }


    /** Attempts to send a NON-Sysex MIDI message. Returns false if (1) the data was empty or null (2)
        synth has turned off the ability to send temporarily (3) the sysex message is not
        valid (4) an error occurred when the receiver tried to send the data.  */
    public boolean tryToSendMIDI(ShortMessage message)
        {
        if (message == null) 
            return false;

        if (getSendMIDI())
            {
            if (tuple == null) return false;
            Receiver receiver = tuple.out;
            if (receiver == null) return false;
            receiver.send(message, -1); 
            return true;
            }
        else
            return false;
        }
                
                        

    /** Attempts to send MIDI sysex. Returns false if (1) the data was empty or null (2)
        synth has turned off the ability to send temporarily (3) the sysex message is not
        valid (4) an error occurred when the receiver tried to send the data.  */
    public boolean tryToSendSysex(byte[] data)
        {
        return tryToSendSysex(data, false);
        }
    
    /** Attempts to send MIDI sysex, possibly forcing sending MIDI even if getSendMIDI() is false. 
        Returns false if (1) the data was empty or null (2)
        synth has turned off the ability to send temporarily (3) the sysex message is not
        valid (4) an error occurred when the receiver tried to send the data.  */
    public boolean tryToSendSysex(byte[] data, boolean forceSendMIDI)
        {
        if (data == null || data.length == 0) 
            return false;

        if (forceSendMIDI || getSendMIDI())
            {
            if (tuple == null) return false;
            Receiver receiver = tuple.out;
            if (receiver == null) return false;
            try { SysexMessage m = new SysexMessage(data, data.length);
                receiver.send(m, -1); return true; }
            catch (InvalidMidiDataException e) { e.printStackTrace(); return false; }
            }
        else
            return false;
        }
        
    
    /** Builds a synth of the given CLASS, with the given synth NAME.
        If THROWAWAY is true, then the window won't be sprouted and MIDI won't be set up.
        If SETUPMIDI is false, then IDI won't be set up.  The TUPLE provides the default
        MIDI devices. */
        
    static Synth instantiate(Class _class, String name, boolean throwaway, boolean setupMIDI, Midi.Tuple tuple)
        {
        try
            {
            Synth synth = (Synth)(_class.newInstance()); // this will setWillPush(false);
            if (!throwaway)
                {
                synth.sprout();
                JFrame frame = ((JFrame)(SwingUtilities.getRoot(synth)));
                frame.setVisible(true);
                if (setupMIDI) 
                    synth.setupMIDI("Choose MIDI devices to send to and receive from.", tuple);

                // we call this here even though it's already been called as a result of frame.setVisible(true)
                // because it's *after* setupMidi(...) and so it gives synths a chance to send
                // a MIDI sysex message in response to the window becoming front.
                synth.windowBecameFront();                              
                }
            synth.undo.setWillPush(true);
            return synth;
            }
        catch (IllegalAccessException e2)
            {
            e2.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred while creating the synth editor for \n" + name, "Creation Error", JOptionPane.ERROR_MESSAGE);
            }
        catch (InstantiationException e2)
            {
            e2.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred while creating the synth editor for \n" + name, "Creation Error", JOptionPane.ERROR_MESSAGE);
            }
        return null;
        }    


    public JFrame sprout()
        {
        JFrame frame = new JFrame();
        JMenuBar menubar = new JMenuBar();
        frame.setJMenuBar(menubar);
        JMenu menu = new JMenu("File");
        menubar.add(menu);

        JMenuItem _new = new JMenuItem("New " + getSynthName());
        _new.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(_new);
        _new.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                instantiate(Synth.this.getClass(), getSynthName(), false, true, tuple);
                }
            });

        JMenu newSynth = new JMenu("New Synth");
        menu.add(newSynth);
        for(int i = 0; i < synths.length; i++)
            {
            final int _i = i;
            JMenuItem synthMenu = new JMenuItem(synthNames[i]);
            synthMenu.addActionListener(new ActionListener()
                {
                public void actionPerformed(ActionEvent e)
                    {
                    instantiate(synths[_i], synthNames[_i], false, true, tuple);
                    }
                });
            newSynth.add(synthMenu);
            }
        
        JMenuItem _copy = new JMenuItem("Duplicate Synth");
        _copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(_copy);
        _copy.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                Synth newSynth = instantiate(Synth.this.getClass(), getSynthName(), false, true, tuple);
                newSynth.setSendMIDI(false);
                model.copyValuesTo(newSynth.model);
                newSynth.setSynced(false);
                newSynth.setSendMIDI(true);
                }
            });


                
        JMenuItem open = new JMenuItem("Load...");
        open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(open);
        open.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doOpen();
                }
            });

        menu.addSeparator();

        JMenuItem close = new JMenuItem("Close Window");
        close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(close);
        close.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doCloseWindow();
                }
            });
                
        menu.addSeparator();

        JMenuItem save = new JMenuItem("Save");
        save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(save);
        save.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doSave();
                }
            });
                
        JMenuItem saveAs = new JMenuItem("Save As...");
        menu.add(saveAs);
        saveAs.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doSaveAs();
                }
            });

        menu = new JMenu("Edit");
        menubar.add(menu);
                
                
        JMenuItem _undo = new JMenuItem("Undo");
        _undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(_undo);
        _undo.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                if (model.equals(undo.top()))
                    model = undo.undo(null);  // don't push into the redo stack
                model = undo.undo(model);
                model.updateAllListeners();
                setSynced(false);
                }
            });
            
        JMenuItem _redo = new JMenuItem("Redo");
        _redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(_redo);
        _redo.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                model = (Model)(undo.redo(getModel()));
                model.updateAllListeners();
                }
            });            
            
        menu = new JMenu("MIDI");
        menubar.add(menu);
                
                
        JMenuItem receiveCurrent = new JMenuItem("Request Current Patch");
        receiveCurrent.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(receiveCurrent);
        receiveCurrent.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                if (tuple == null || tuple.out == null)
                    {
                    if (!setupMIDI("You are disconnected. Choose MIDI devices to send to and receive from."))
                        return;
                    else
                        setSynced(false);
                    }
                
                tryToSendSysex(requestCurrentDump(null));
                }
            });

        receive = new JMenuItem("Request Patch...");
        //receive.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(receive);
        receive.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                if (tuple == null || tuple.out == null)
                    {
                    if (!setupMIDI("You are disconnected. Choose MIDI devices to send to and receive from."))
                        return;
                    else
                        setSynced(false);
                    }
                
                Model tempModel = new Model();
                if (gatherInfo("Request Patch", tempModel))
                    {
                    tryToSendSysex(requestDump(tempModel));
                    }
                }
            });
                
        JMenu merge = new JMenu("Request Merge");
        menu.add(merge);
        JMenuItem merge25 = new JMenuItem("Merge in 25%");
        merge.add(merge25);
        JMenuItem merge50 = new JMenuItem("Merge in 50%");
        merge.add(merge50);
        JMenuItem merge75 = new JMenuItem("Merge in 75%");
        merge.add(merge75);
        
        merge25.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                if (tuple == null || tuple.out == null)
                    {
                    if (!setupMIDI("You are disconnected. Choose MIDI devices to send to and receive from."))
                        return;
                    else
                        setSynced(false);
                    }
                
                doMerge(0.25);
                }
            });

        merge50.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                if (tuple == null || tuple.out == null)
                    {
                    if (!setupMIDI("You are disconnected. Choose MIDI devices to send to and receive from."))
                        return;
                    else
                        setSynced(false);
                    }
                
                doMerge(0.50);
                }
            });

        merge75.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                if (tuple == null || tuple.out == null)
                    {
                    if (!setupMIDI("You are disconnected. Choose MIDI devices to send to and receive from."))
                        return;
                    else
                        setSynced(false);
                    }
                
                doMerge(0.75);
                }
            });
                
        menu.addSeparator();

        transmit = new JMenuItem("Send Patch");
        menu.add(transmit);
        transmit.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                if (tuple == null || tuple.out == null)
                    {
                    if (!setupMIDI("You are disconnected. Choose MIDI devices to send to and receive from."))
                        return;
                    else
                        setSynced(false);
                    }
                
                if (isSynced())
                    {
                    sendAllParameters();
                    }
                else
                    {
                    if (gatherInfo("You aren't synced.  ", getModel()))
                        {
                        setSynced(true);
                        sendAllParameters();
                        }
                    }
                }
            });

        transmitTo = new JMenuItem("Send To...");
        menu.add(transmitTo);
        transmitTo.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                if (tuple == null || tuple.out == null)
                    {
                    if (!setupMIDI("You are disconnected. Choose MIDI devices to send to and receive from."))
                        return;
                    else
                        setSynced(false);
                    }
                
                if (gatherInfo("Send Patch To...", getModel()))
                    {
                    setSynced(true);
                    sendAllParameters();
                    }
                }
            });
                
        transmitCurrent = new JMenuItem("Send to Current Patch");
        menu.add(transmitCurrent);
        transmitCurrent.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                if (tuple == null || tuple.out == null)
                    {
                    if (!setupMIDI("You are disconnected. Choose MIDI devices to send to and receive from."))
                        return;
                    else
                        setSynced(false);
                    }
                
                sendAllParameters(false);
                }
            });

        JMenuItem random = new JMenuItem("Randomize");
        menu.add(random);
        random.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                mutate(1.0);
                sendAllParameters();
                }
            });

        JMenuItem reset = new JMenuItem("Reset");
        menu.add(reset);
        reset.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                // because loadDefaults isn't wrapped in an undo, we have to
                // wrap it manually here
                undo.setWillPush(false);
                Model backup = (Model)(model.clone());
                loadDefaults();
                undo.setWillPush(true);
                if (!backup.keyEquals(getModel()))  // it's changed, do an undo push
                    undo.push(backup);

                sendAllParameters();
                }
            });


        menu.addSeparator();

        JMenuItem burn = new JMenuItem("Write Patch To...");
        burn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(burn);
        burn.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                if (tuple == null || tuple.out == null)
                    {
                    if (!setupMIDI("You are disconnected. Choose MIDI devices to send to and receive from."))
                        return;
                    else
                        setSynced(false);
                    }
                
                if (gatherInfo("Write Patch To...", getModel()))
                    {
                    if (tryToSendSysex(emit(getModel(), false)))
                        {
                        changePatch(getModel());
                        setSynced(true);
                        }
                    }
                }
            });
                
        menu.addSeparator();

        JMenuItem change = new JMenuItem("Change MIDI");
        menu.add(change);
        change.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                if (!setupMIDI("Choose MIDI devices to send to and receive from."))
                    return;
                else
                    setSynced(false);
                }
            });
            
        JMenuItem disconnect = new JMenuItem("Disconnect MIDI");
        menu.add(disconnect);
        disconnect.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                disconnectMIDI();
                }
            });

        menu.addSeparator();

        JMenuItem allSoundsOff = new JMenuItem("Send All Sounds Off");
        menu.add(allSoundsOff);
        allSoundsOff.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                try
                    {
                    for(int i = 0; i < 16; i++)
                        tryToSendMIDI(new ShortMessage(ShortMessage.CONTROL_CHANGE, i, 120, 0));
                    }
                catch (InvalidMidiDataException e2)
                    {
                    e2.printStackTrace();
                    }
                }
            });
                
        JMenuItem testNote = new JMenuItem("Send Test Note");
        testNote.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(testNote);
        testNote.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                try
                    {
                    int channel = getChannelOut();
                    tryToSendMIDI(new ShortMessage(ShortMessage.NOTE_ON, channel - 1, 60, 127));
                    Thread.currentThread().sleep(500);
                    tryToSendMIDI(new ShortMessage(ShortMessage.NOTE_OFF, channel - 1, 60, 127));
                    }
                catch (InterruptedException e2)
                    {
                    e2.printStackTrace();
                    }
                catch (InvalidMidiDataException e2)
                    {
                    e2.printStackTrace();
                    }
                }
            });
            
        menu.addSeparator();

        learningMenuItem = new JMenuItem("Map CC");
        learningMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(learningMenuItem);
        learningMenuItem.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                toggleLearning();
                }
            });
                
        JMenuItem clearAllCC = new JMenuItem("Clear all CCs");
        menu.add(clearAllCC);
        clearAllCC.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                if (JOptionPane.showConfirmDialog(Synth.this, "Are you sure you want to clear all CCs?", "Clear CCs", 
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null) == JOptionPane.OK_OPTION)
                    {
                    clearLearned();
                    }
                }
            });
            
            
        menu = new JMenu("Tabs");
        menubar.add(menu);
                
                
        JMenuItem prev = new JMenuItem("Previous Tab");
        prev.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(prev);
        prev.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setCurrentTab(getCurrentTab() - 1);
                }
            });


        JMenuItem next = new JMenuItem("Next Tab");
        next.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(next);
        next.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setCurrentTab(getCurrentTab() + 1);
                }
            });


        JMenuItem taba = new JMenuItem("Tab 1");
        taba.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(taba);
        taba.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setCurrentTab(0);
                }
            });

        taba = new JMenuItem("Tab 2");
        taba.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(taba);
        taba.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setCurrentTab(1);
                }
            });

        taba = new JMenuItem("Tab 3");
        taba.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(taba);
        taba.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setCurrentTab(2);
                }
            });

        taba = new JMenuItem("Tab 4");
        taba.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(taba);
        taba.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setCurrentTab(3);
                }
            });

        taba = new JMenuItem("Tab 5");
        taba.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(taba);
        taba.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setCurrentTab(4);
                }
            });

        taba = new JMenuItem("Tab 6");
        taba.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_6, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(taba);
        taba.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setCurrentTab(5);
                }
            });

        taba = new JMenuItem("Tab 7");
        taba.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_7, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(taba);
        taba.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setCurrentTab(6);
                }
            });

        taba = new JMenuItem("Tab 8");
        taba.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_8, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(taba);
        taba.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setCurrentTab(7);
                }
            });


        frame.getContentPane().add(this);
        frame.pack();
                
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); 
        frame.addWindowListener(new java.awt.event.WindowAdapter() 
            {
            public void windowClosing(java.awt.event.WindowEvent windowEvent) 
                {
                doCloseWindow();
                }

            public void windowActivated(WindowEvent e)
                {
                windowBecameFront();
                }

            });
    
        updateTitle();
        numOpenWindows++;        
        
        return frame;
        }
    
    /** Perform a patch merge */
    void doMerge(double probability)
        {
        Model tempModel = new Model();
        if (gatherInfo("Request Merge", tempModel))
            {
            Synth.this.merging = probability;
            tryToSendSysex(requestDump(tempModel));
            }
        }

    public int getChannelOut()
        {
        int channel = 1;
        if (tuple != null)
            channel = tuple.outChannel;
        return channel;
        }
    
    public void changePatch(Model tempModel) { }

    boolean sendsAllParametersInBulk = false;
    
    /** Sets whether the synth sends its patch dump (TRUE) as one single sysex dump or by
        sending multiple separate parameter change requests (FALSE).  By default this is FALSE. */
    public void setSendsAllParametersInBulk(boolean val) { sendsAllParametersInBulk = val; }

    /** Returns whether the synth sends its patch dump (TRUE) as one single sysex dump or by
        sending multiple separate parameter change requests (FALSE).  By default this is FALSE. */
    public boolean getSendsAllParametersInBulk() { return sendsAllParametersInBulk; }

    /** Switches to the current patch, then sends all the parameters in a patch to the synth.
        If not synced, does nothing.

		<p>If sendsAllParametersInBulk was set to TRUE, then this is done by sending
        a single patch write to working memory, which may not be supported by all synths.
        
        Otherwise this is done by sending each parameter separately, which isn't as fast.
        The default sends each parameter separately.
    */   
    public void sendAllParameters() { sendAllParameters(true); } 
     
    /** If changePatch is TRUE, switches to the current patch, then sends all the parameters in a patch to the synth;
        if not synced, does nothing.

    	If changePatch is FALSE, just sends the parameters to whatever is the current patch, even if not synced.
    	
        <P>If sendsAllParametersInBulk was set to TRUE, then this is done by sending
        a single patch write to working memory, which may not be supported by all synths.
        
        Otherwise this is done by sending each parameter separately, which isn't as fast.
        The default sends each parameter separately.
    */   
    public void sendAllParameters(boolean changePatch)
        {
        if (isSynced() || !changePatch)		// if we're synced, or if we're not changing patches....
            {
            if (changePatch)
            	changePatch(getModel());	
                
            if (sendsAllParametersInBulk)
                {
                tryToSendSysex(emit(getModel(), true));
                }
            else
                {
                String[] keys = getModel().getKeys();
                for(int i = 0; i < keys.length; i++)
                    {
                    tryToSendSysex(emit(keys[i]));
                    }
                }
            }
        }
            
    /** Guarantee that the given filename ends with the given ending. */    
    static String ensureFileEndsWith(String filename, String ending)
        {
        // do we end with the string?
        if (filename.regionMatches(false,filename.length()-ending.length(),ending,0,ending.length()))
            return filename;
        else return filename + ending;
        }
                
    /** Goes through the process of saving to a new sysex file and associating it with
        the editor. */
    public void doSaveAs()
        {
        FileDialog fd = new FileDialog((Frame)(SwingUtilities.getRoot(this)), "Save Patch to Sysex File...", FileDialog.SAVE);

        if (file != null)
            {
            fd.setFile(file.getName());
            fd.setDirectory(file.getParentFile().getPath());
            }
        else
            {
            if (getPatchName() != null)
                fd.setFile(getPatchName().trim() + ".syx");
            else
                fd.setFile("Untitled.syx");
            String path = getLastDirectory();
            if (path != null)
                fd.setDirectory(path);
            }            
            
        fd.setVisible(true);
        File f = null; // make compiler happy
        FileOutputStream os = null;
        if (fd.getFile() != null)
            try
                {
                f = new File(fd.getDirectory(), ensureFileEndsWith(fd.getFile(), ".syx"));
                os = new FileOutputStream(f);
                os.write(emit((Model)null, false));
                os.close();
                file = f;
                setLastDirectory(fd.getDirectory());
                } 
            catch (IOException e) // fail
                {
                JOptionPane.showMessageDialog(this, "An error occurred while saving to the file " + (f == null ? " " : f.getName()), "File Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                }
            finally
                {
                if (os != null)
                    try { os.close(); }
                    catch (IOException e) { }
                }

        updateTitle();
        }


    /** Goes through the process of saving to an existing sysex file associated with
        the editor, else it calls doSaveAs(). */
    public void doSave()
        {
        if (file == null)
            {
            doSaveAs();
            } 
        else
            {
            FileOutputStream os = null;
            try
                {
                os = new FileOutputStream(file);
                os.write(emit((Model)null, false));
                os.close();
                }
            catch (Exception e) // fail
                {
                JOptionPane.showMessageDialog(this, "An error occurred while saving to the file " + file, "File Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                }
            finally
                {
                if (os != null)
                    try { os.close(); }
                    catch (IOException e) { }
                }
            }

        updateTitle();
        }


    /** Goes through the process of dumping to a text file. */
    /*
      public void doExport(boolean doDiff)
      {
      FileDialog fd = new FileDialog((Frame)(SwingUtilities.getRoot(this)), "Export Patch" + (doDiff ? " Difference" : "") + " As Text...", FileDialog.SAVE);
      if (file == null)
      {
      fd.setFile("Untitled.txt");
      }
      else
      {
      fd.setFile(file.getName());
      fd.setDirectory(file.getParentFile().getPath());
      }
      fd.setVisible(true);
      File f = null; // make compiler happy
      FileOutputStream os = null;
      if (fd.getFile() != null)
      try
      {
      f = new File(fd.getDirectory(), ensureFileEndsWith(fd.getFile(), ".txt"));
      os = new FileOutputStream(f);
      PrintWriter pw = new PrintWriter(os);
      getModel().print(pw, doDiff);
      pw.close();
      file = f;
      } 
      catch (IOException e) // fail
      {
      JOptionPane.showMessageDialog(this, "An error occurred while exporting to the file " + (f == null ? " " : f.getName()), "File Error", JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
      }
      finally
      {
      if (os != null)
      try { os.close(); }
      catch (IOException e) { }
      }

      updateTitle();
      }
    */
        
    /** Override this as you see fit to do something special when your window becomes front. */
    public void windowBecameFront()
        {
        }

    public void doCloseWindow()
        {
        if (requestCloseWindow())
            {
            if (tuple != null)
                tuple.dispose();
            tuple = null;
            
            JFrame frame = (JFrame)(SwingUtilities.getRoot(this));
            if (frame != null)
                {
                frame.setVisible(false);
                frame.dispose();
                }
            frame = null;
                                
            numOpenWindows--;
            if (numOpenWindows <= 0)
                {
                System.exit(0);
                }
            }
                
        }
    
    public static void setLastX(String path, String x, String synthName)
        {
        if (synthName != null)
            {
            java.util.prefs.Preferences app_p = Prefs.getAppPreferences(synthName, "Edisyn");
            app_p.put(x, path);
            Prefs.save(app_p);
            }
        java.util.prefs.Preferences global_p = Prefs.getGlobalPreferences("Data");
        global_p.put(x, path);
        Prefs.save(global_p);
        }
        
    public static String getLastX(String x, String synthName)
        {
        String lastDir = null;
        if (synthName != null)
            {
            lastDir = Prefs.getAppPreferences(synthName, "Edisyn").get(x, null);
            }
        
        if (lastDir == null)
            {
            lastDir = Prefs.getGlobalPreferences("Data").get(x, null);
            }
                
        return lastDir;         
        }
        
    void setLastDirectory(String path) { setLastX(path, "LastDirectory", getSynthName()); }
    String getLastDirectory() { return getLastX("LastDirectory", getSynthName()); }
    
    static void setLastSynth(String path) { setLastX(path, "Synth", null); }
    static String getLastSynth() { return getLastX("Synth", null); }


                
    /** Goes through the process of opening a file and loading it into this editor. 
        This does NOT open a new editor window -- it loads directly into this editor. */
    public void doOpen()
        {
        FileDialog fd = new FileDialog((Frame)(SwingUtilities.getRoot(this)), "Load Sysex Patch File...", FileDialog.LOAD);
        fd.setFilenameFilter(new FilenameFilter()
            {
            public boolean accept(File dir, String name)
                {
                return ensureFileEndsWith(name, ".syx").equals(name);
                }
            });

        if (file != null)
            {
            fd.setFile(file.getName());
            fd.setDirectory(file.getParentFile().getPath());
            }
        else
            {
            String path = getLastDirectory();
            if (path != null)
                fd.setDirectory(path);
            }
                
                
        boolean failed = true;
                
        fd.setVisible(true);
        File f = null; // make compiler happy
        FileInputStream is = null;
        if (fd.getFile() != null)
            try
                {
                f = new File(fd.getDirectory(), fd.getFile());
                
                is = new FileInputStream(f);
                byte[] data = new byte[getExpectedSysexLength()];

                int val = is.read(data, 0, getExpectedSysexLength());
                is.close();
                
                if (!recognizeLocal(data))
                    JOptionPane.showMessageDialog(this, "File does not contain sysex data for the " + getSynthName(), "File Error", JOptionPane.ERROR_MESSAGE);
                else if (val != getExpectedSysexLength())
                    JOptionPane.showMessageDialog(this, "File data is not the right length for " + getSynthName(), "File Error", JOptionPane.ERROR_MESSAGE);
                else
                    {
                    setSendMIDI(false);
                    undo.setWillPush(false);
                    Model backup = (Model)(model.clone());
                    parse(data, false);
                    undo.setWillPush(true);
                    if (!backup.keyEquals(getModel()))  // it's changed, do an undo push
                        undo.push(backup);
                    setSendMIDI(true);
                    file = f;
                    setLastDirectory(fd.getDirectory());
                    }
                }        
            catch (Throwable e) // fail  -- could be an Error or an Exception
                {
                JOptionPane.showMessageDialog(this, "An error occurred while loading from the file.", "File Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                }
            finally
                {
                if (is != null)
                    try { is.close(); }
                    catch (IOException e) { }
                }
                
        setSynced(false);
        updateTitle();
        }

                
    /** Perform a JOptionPane confirm dialog with MUTLIPLE widgets that the user can select.  The widgets are provided
        in the array WIDGETS, and each has an accompanying label in LABELS.   Returns TRUE if the user performed
        the operation, FALSE if cancelled. */
    public static boolean doMultiOption(JComponent root, String[] labels, JComponent[] widgets, String title, String message)
        {
        JPanel panel = new JPanel();
        
        int max = 0;
        JLabel[] jlabels = new JLabel[labels.length];
        for(int i = 0; i < labels.length; i++)
            {
            jlabels[i] = new JLabel(labels[i] + " ", SwingConstants.RIGHT);
            int width = (int)(jlabels[i].getPreferredSize().getWidth());
            if (width > max) max = width;   
            }

        Box vbox = new Box(BoxLayout.Y_AXIS);
        for(int i = 0; i < labels.length; i++)
            {
            jlabels[i].setPreferredSize(new Dimension(
                    max, (int)(jlabels[i].getPreferredSize().getHeight())));
            jlabels[i].setMinimumSize(jlabels[i].getPreferredSize());
            // for some reason this has to be set as well
            jlabels[i].setMaximumSize(jlabels[i].getPreferredSize());
            Box hbox = new Box(BoxLayout.X_AXIS);
            hbox.add(jlabels[i]);
            hbox.add(widgets[i]);
            vbox.add(hbox);
            }
                
        panel.setLayout(new BorderLayout());
        panel.add(vbox, BorderLayout.SOUTH);
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(new JLabel("    "), BorderLayout.NORTH);
        p.add(new JLabel(message), BorderLayout.CENTER);
        p.add(new JLabel("    "), BorderLayout.SOUTH);
        panel.add(p, BorderLayout.NORTH);
        return (JOptionPane.showConfirmDialog(root, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null) == JOptionPane.OK_OPTION);
        }
           
           
    /** Pops up at the start of the program to ask the user what synth he wants. */
    public static Synth doNewSynthPanel()
        {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(new JLabel("    "), BorderLayout.NORTH);
        p.add(new JLabel("Select a Synthesizer to Edit"), BorderLayout.CENTER);
        p.add(new JLabel("    "), BorderLayout.SOUTH);

        JPanel p2 = new JPanel();
        p2.setLayout(new BorderLayout());
        p2.add(p, BorderLayout.NORTH);
        JComboBox combo = new JComboBox(synthNames);
                
        // Note: Java classdocs are wrong: if you set a selected item to null (or to something not in the list)
        // it doesn't just not change the current selected item, it sets it to some blank item.
        String synth = getLastSynth();
        if (synth != null) combo.setSelectedItem(synth);
        p2.add(combo, BorderLayout.CENTER);
                
        int result = JOptionPane.showOptionDialog(null, p2, "Edisyn", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[] { "Run", "Cancel", "Disconnected" }, "Run");
        if (result == 1) return null;
        else 
            {
            setLastSynth("" + combo.getSelectedItem());
            return instantiate(synths[combo.getSelectedIndex()], synthNames[combo.getSelectedIndex()], false, (result == 0), null);
            }
        }
    
    public abstract String getDefaultResourceFileName();
    
    boolean learning = false;
    
    JMenuItem learningMenuItem;
    
    public void toggleLearning()
        {
        learning = !learning;
        model.clearLastKey();
        lastCC = -1;  // clear
        updateTitle();
        if (learning)
            {
            learningMenuItem.setText("End Map CC");
            }
        else
            {
            learningMenuItem.setText("Map CC");
            }
        }
    
    public void clearLearned()
        {
        ccmap.clear();
        learning = false;
        toggleLearning();
        toggleLearning();
        }
        
    public int getCurrentTab()
        {
        return tabs.getSelectedIndex();
        }
                
    public void setCurrentTab(int tab)
        {
        int len = tabs.getTabCount();
        if (tab >= tabs.getTabCount())
            return;
        if (tab < 0)
            return;
        tabs.setSelectedIndex(tab);
        }
        
    public void handleCC(ShortMessage message)
        {
        lastCC = message.getData1();
        if (learning)
            {
            String key = model.getLastKey();
            if (key != null)
                {
                ccmap.setKeyForCCPane(lastCC, getCurrentTab(), key);
                toggleLearning();  // we're done
                }
            }
        else
            {
            String key = ccmap.getKeyForCCPane(lastCC, getCurrentTab());
            int val = message.getData2();
            if (key != null)
                {
                if (model.minExists(key))
                    {
                    val += model.getMin(key);
                    }
                else if (model.maxExists(key))
                    {
                    val = val - 127 + model.getMax(key);
                    }
                model.setBounded(key, val);
                }
            }
        }
    
    // Note that this isn't wrapped in undo, so we can block it at instantiation
    public void loadDefaults()
        {
        InputStream stream = getClass().getResourceAsStream(getDefaultResourceFileName());
        if (stream != null)
            {
            try 
                {
                byte[] data = new byte[getExpectedSysexLength()];
                int val = stream.read(data, 0, getExpectedSysexLength());
                setSendMIDI(false);
                parse(data, true);
                setSendMIDI(true);
                model.setUndoListener(undo);    // okay, redundant, but that way the pattern stays the same
                }
            catch (Exception e)
                {
                e.printStackTrace();
                }
            finally
                {
                try { stream.close(); }
                catch (IOException e) { }
                }
            }
        else
            {
            System.err.println("Didn't Parse");
            }
        }       
    }
