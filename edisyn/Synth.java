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
    // Counter for total number of open windows.  When this drops to zero,
    // the program will quit automatically.
    static int numOpenWindows = 0;
    
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
    
    public JTabbedPane tabs = new JTabbedPane();

    public static final int MAX_FILE_LENGTH = 64 * 1024;        // so we don't go on forever

    public Midi.Tuple tuple;
    
    public Midi midi = new Midi();
    
    public JMenuItem transmit;
    public JMenuItem transmitTo;
    public JMenuItem transmitCurrent;
    public JMenuItem writeTo;
    public JMenuItem undoMenu;
    public JMenuItem redoMenu;
    public JMenuItem receiveCurrent;
    public JCheckBoxMenuItem passThroughCCMenuItem;

    // counter for note-on messages so we don't have a million note-off messages in a row
    int noteOnTick;
    
    Undo undo = new Undo(this);
        
    CCMap ccmap;
    int lastCC = -1;
        
    boolean passThroughCC;
    public Object passThroughCCLock = new Object[0];
    public boolean getPassThroughCC() { synchronized(passThroughCCLock) { return passThroughCC; } }
    public void setPassThroughCC(final boolean val) 
    	{ 
    	synchronized(passThroughCCLock) 
    		{ 
    		passThroughCC = val; 
    		setLastX("" + val, "PassThroughCC", getSynthNameLocal());
			SwingUtilities.invokeLater(new Runnable()
				{
				public void run()
					{
					passThroughCCMenuItem.setState(val);
					}
				});
			if (val == true && getLearningCC())
				setLearningCC(false);
    		} 
    	}





	//////// SYNTHESIZER EDIT PANES
	
	/** All synthesizer editor pane classes in Edisyn */
    public static final Class[] synths = new Class[] { YamahaTX81Z.class, YamahaTX81ZMulti.class, KawaiK4.class, KawaiK4Multi.class, Blofeld.class, BlofeldMulti.class, MicrowaveXT.class, MicrowaveXTMulti.class, Matrix1000.class, PreenFM2.class };
    
    /** All synthesizer names in Edisyn, one per class in synths */
    public static String[] getSynthNames()
        {
        String[] synthNames = new String[synths.length];
        for(int i = 0; i < synths.length; i++)
            {
            try
                {
                synthNames[i] = "Synth with no getSynthName() method, oops";
                Method method = synths[i].getMethod("getSynthName", new Class[] { });
                synthNames[i] = (String)(method.invoke(null, new Object[] { } ));
                }
            catch (Exception e)
                {
                e.printStackTrace();
                }
            }
        return synthNames;
        }
                
                
                
                
    /////// SYNCING AND UNDO
                  
    boolean synced = false;
    
    /** Set the synth to believe it is synced (or not). */
    public void setSynced(boolean val)
        {
        synced = val;
        updateTitle();
        }
        
    /** Returns whether the synth believes it is synced. */
    public boolean isSynced() { return synced; }
    
    /** Update the Undo and Redo menus to be enabled or disabled. */
    public void updateUndoMenus()
        {
        if (undo == null)               // we could just be a scratch synth, not one with a window
            return;  
                
        if (redoMenu != null)
            redoMenu.setEnabled(
                undo.shouldShowRedoMenu());
        if (undoMenu != null)
            undoMenu.setEnabled(
                undo.shouldShowUndoMenu());
        }










	////// COMMUNICATION WITH THE SYNTHESIZER

	/** Changes the patch and bank to reflect the information in tempModel. */
    public void changePatch(Model tempModel) { }

    /** Updates the model to reflect the following sysex patch dump for your synthesizer type.
    	If ignorePatch is TRUE, then you should NOT attempt to change the patch number and bank
    	to reflect new information, but should retain the old number and bank.
        If the patch contains information sufficient to sync, return TRUE, else return FALSE. */
    public boolean parse(byte[] data, boolean ignorePatch) { return false; }
    
    /** Updates the model to reflect the following sysex or CC (or other!) message from your synthesizer. 
        You are free to IGNORE this message entirely.  Patch dumps will generally not be sent this way; 
        and furthermore it is possible that this is crazy sysex from some other synth so you need to check for it.  */
    public void parseParameter(byte[] data) { return; }
    
    /** Produces a sysex patch dump suitable to send to a remote synthesizer as one
        OR MORE sysex dumps or other MIDI messages.  Each sysex dump is a separate byte array,
        and other midi messages are MIDI message objects.
        If you return a zero-length array, nothing will be sent.  
        If tempModel is non-null, then it should be used to extract meta-parameters
        such as the bank and patch number (stuff that's specified by gatherInfo(...).
        Otherwise the primary model should be used.  The primary model should be used
        for all other parameters.  toWorkingMemory indicates whether the patch should
        be directed to working memory of the synth or to the patch number in tempModel. 
        
        <p>It is assumed that the NON byte-array elements may be stripped out if this
        emit is done to a file.
        
        <p>The default version of this method simply calls emit() and returns its
        value as the first subarray.  If you have a synthesizer (like the TX81Z) which
        dumps a single patch as multiple sysex dumps, override this to send the patch
        properly.
    */
    public Object[] emitAll(Model tempModel, boolean toWorkingMemory)
        {
        byte[]result = emit(tempModel, toWorkingMemory);
        if (result == null ||
            result.length == 0)
            return new Object[0];
        else
            return new Object[] { result };
        }

    /** Produces a sysex patch dump suitable to send to a remote synthesizer. 
        If you return a zero-length byte array, nothing will be sent.  
        If tempModel is non-null, then it should be used to extract meta-parameters
        such as the bank and patch number (stuff that's specified by gatherInfo(...).
        Otherwise the primary model should be used.  The primary model should be used
        for all other parameters.  toWorkingMemory indicates whether the patch should
        be directed to working memory of the synth or to the patch number in tempModel. 
        
        <p>Note that this method will only be called by emitAll(...).  So if you 
        have overridden emitAll(...) you don't need to implement this method. */
    public byte[] emit(Model tempModel, boolean toWorkingMemory) { return new byte[0]; }
    
    /** Produces one or more sysex parameter change requests for the given parameter as one
        OR MORE sysex dumps or other MIDI messages.  Each sysex dump is a separate byte array,
        and other midi messages are MIDI message objects.
        
        If you return a zero-length byte array, nothing will be sent.  */
    public Object[] emitAll(String key)
        {
        byte[] result = emit(key);
        if (result == null ||
            result.length == 0)
            return new Object[0];
        else
            return new Object[] { result };
        }

    /** Produces a sysex parameter change request for the given parameter.  
        If you return a zero-length byte array, nothing will be sent.
        
        <p>Note that this method will only be called by emitAll(...).  So if you 
        have overridden emitAll(...) you don't need to implement this method. */        
    public byte[] emit(String key) { return new byte[0]; }
    
    /** Produces a sysex message to send to a synthesizer to request it to initiate
        a patch dump to you.  If you return a zero-length byte array, nothing will be sent. 
        If tempModel is non-null, then it should be used to extract meta-parameters
        such as the bank and patch number or machine ID (stuff that's specified by gatherInfo(...).
        Otherwise the primary model should be used.  The primary model should be used
        for all other parameters.  
    */
    public byte[] requestDump(Model tempModel) { return new byte[0]; }
    
    /** Produces a sysex message to send to a synthesizer to request it to initiate
        a patch dump to you for the CURRENT PATCH.  If you return a zero-length byte array, 
        nothing will be sent.  If tempModel is non-null, then it should be used to extract 
        meta-parameters such as the machine ID (stuff that's specified by gatherInfo(...).
        Otherwise the primary model should be used.  The primary model should be used
        for all other parameters.  
    */
    public byte[] requestCurrentDump() { return new byte[0]; }

	/** Performs a request for a dump of the patch indicated in tempModel.
		This method by default calls requestDump(...) and submits it to tryToSendSysex(...), 
		but you can override it to do something more sophisticated. */    
    public void performRequestDump(Model tempModel)
        {
        tryToSendSysex(requestDump(tempModel));
        }
    
	/** Performs a request for a dump of the patch indicated in the current model.
		This method by default calls requestCurrentDump(...) and submits it to tryToSendSysex(...), 
		but you can override it to do something more sophisticated. */    
    public void performRequestCurrentDump()
    	{
        tryToSendSysex(requestCurrentDump());
    	}
    
    /** Gathers meta-parameters from the user via a JOptionPane, such as 
        patch number and bank number, which are used to specify where a patch
        should be saved to or loaded from.  These are typically also stored in
        the primary model, but the user may want to change them so as to 
        write out to a different location for example.  The model should not be
        revised to hold the new values; but rather they should be placed into tempModel.
        This method returns TRUE if the user provided the values, and FALSE
        if he cancelled.
        
        <p>If writing is TRUE, then the purpose of this info-gathering is to find
        a place to write or send a patch TO.  Otherwise its purpose is to read a patch FROM
        somewhere.  Some synths allow patches to be read from many locations but written only
        to specific ones (because the others are read-only).
    */
    public abstract boolean gatherInfo(String title, Model tempModel, boolean writing);

    /** Create your own Synth-specific class version of this static method.
        It will be called when the system wants to know if the given sysex patch dump
        is for your kind of synthesizer.  Return true if so, else false. */
    private static boolean recognize(byte[] data)
        {
        return false;
        }


	public Object[] buildNRPN(int channel, int parameter, int value)
		{
		try
			{
			int p_msb = (parameter >> 7);
			int p_lsb = (parameter & 127);
			int v_msb = (value >> 7);
			int v_lsb = (value & 127);
			return new Object[]
				{
				new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 99, (byte)p_msb),
				new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 98, (byte)p_lsb),
				new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 6, (byte)v_msb),
				new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 38, (byte)v_lsb),

				// can't have these right now, it freaks out the PreenFM2
				//new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 101, (byte)127),
				//new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 100, (byte)127),
				};
			}
		catch (InvalidMidiDataException e)
			{
			e.printStackTrace();
			return new Object[0];
			}
		}

	public Object[] buildLongCC(int channel, int parameter, int value)
		{
		try
			{
			int v_msb = (value >> 7);
			int v_lsb = (value & 127);
			return new Object[]
				{
				new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, parameter, (byte)v_msb),
				new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, parameter + 32, (byte)v_lsb)
				};
			}
		catch (InvalidMidiDataException e)
			{
			e.printStackTrace();
			return new Object[0];
			}
		}

	public Object[] buildPC(int channel, int program)
		{
		try
			{
			return new Object[]
				{
				new ShortMessage(ShortMessage.PROGRAM_CHANGE, channel, program)
				};
			}
		catch (InvalidMidiDataException e)
			{
			e.printStackTrace();
			return new Object[0];
			}
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
        
    /** Returns the name of the synthesizer.  You should make your own
        static version of this method in your synth panel subclass.  */
    public static String getSynthName() { return "Override Me"; }
    
    /** Returns the name of this synth, by calling getSynthName(). */
    public final String getSynthNameLocal()
        {
        // This code is basically a copy of getSynthNames().
        // But we can't easily merge them, one has to be static and the other non-static
        try
            {
            Method method = this.getClass().getMethod("getSynthName", new Class[] { });
            return (String)(method.invoke(null, new Object[] { } ));
            }
        catch (Exception e)
            {
            e.printStackTrace();
            }
        return "Synth with no getSynthName() method, oops";
        }
    
    /** Returns the name of the current patch, or null if there is no such thing. */
    public abstract String getPatchName();
    
    /** Return true if the window can be closed and disposed of. You should do some cleanup
        as necessary (the system will handle cleaning up the receiver and transmitters. 
        By default this just returns true.  */
    public boolean requestCloseWindow() { return true; }
    
        
    String nameForCC(int cc)
    	{
    	if (cc < 256)
    		return "CC " + cc;
    	else return "NRPN " + (cc - 256);
    	}
    	
    	
    /** Updates the JFrame title to reflect the synthesizer type, the patch information, and the filename
        if any.  See Blofeld.updateTitle() for inspiration. */
    public void updateTitle()
        {
        JFrame frame = ((JFrame)(SwingUtilities.getRoot(this)));
        if (frame != null) 
            {
            String synthName = getSynthNameLocal().trim();
            String patchName = "        " + (getPatchName() == null ? "" : getPatchName().trim());
            String fileName = (getFile() == null ? "        Untitled" : "        " + getFile().getName());
            String disconnectedWarning = ((tuple == null || tuple.in == null) ? "  DISCONNECTED" : "");
            String outOfSyncWarning = (isSynced() ? "" : "  UNSYNCED");
            String learningWarning = (learning ? "  LEARNING" +
                    (model.getLastKey() != null ? " " + model.getLastKey() + 
                    (model.getRange(model.getLastKey()) > 0 ? "[" + model.getRange(model.getLastKey()) + "]" : "") + 
                    (ccmap.getCCForKey(model.getLastKey()) >= 0 ? "=" + nameForCC(ccmap.getCCForKey(model.getLastKey())) : "")
                    : "") : "");
        
            frame.setTitle(synthName + fileName + "        " + disconnectedWarning + outOfSyncWarning + learningWarning);
            }
        }

    public Synth() 
        {
        model = new Model();
        model.register(Model.ALL_KEYS, this);
        model.setUndoListener(undo);
        undo.setWillPush(false);  // instantiate undoes this
        random = new Random(System.currentTimeMillis());
        ccmap = new CCMap(Prefs.getAppPreferences(getSynthNameLocal(), "CC"));
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
        
    
    /** Called by the model to update the synth whenever a parameter is changed. */
    public void update(String key, Model model)
        {
        if (learning)
            updateTitle();
                
        if (getSendMIDI() && isSynced())
            {
            tryToSendSysex(emitAll(key));
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
                if (message instanceof SysexMessage)
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
									merge(data, merging);
									merging = 0.0;
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
									changePatch(getModel());
								
									if (canSync)
										{
										setSynced(true);
										}
									else 
										{
										setSynced(false);
										}
									file = null;
									}

								// this last statement fixes a mystery.  When I call Randomize or Reset on
								// a Blofeld or on a Microwave, all of the widgets update simultaneously.
								// But on a Blofeld Multi or Microwave Multi they update one at a time.
								// I've tried a zillion things, even moving all the widgets from the Blofeld Multi
								// into the Blofeld, and it makes no difference!  For some reason the OS X
								// repaint manager is refusing to coallesce their repaint requests.  So I do it here.
								repaint();
								
								updateTitle();
								}
							});
						}
					else	// Maybe it's a local Parameter change in sysex?
						{
						SwingUtilities.invokeLater(new Runnable()
							{
							public void run()
								{
								sendMIDI = false;  // so we don't send out parameter updates in response to reading/changing parameters
								parseParameter(data);
								sendMIDI = true;
								updateTitle();
								}
							});
						}
					}
				else if (message instanceof ShortMessage)
					{
					ShortMessage sm = (ShortMessage)message;
					if (sm.getCommand() == ShortMessage.CONTROL_CHANGE)
						{
						SwingUtilities.invokeLater(new Runnable()
							{
							public void run()
								{
								sendMIDI = false;  // so we don't send out parameter updates in response to reading/changing parameters
								// let's try parsing it
								handleInRawCC(sm);
								sendMIDI = true;
								updateTitle();
								}
							});
						}
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
                                
                            // we intercept a message if:
                            // 1. It's a CC (maybe NRPN)
                            // 2. We're not passing through CC
                            // 3. It's the right channel OR our key channel is OMNI
                            if (!getPassThroughCC() && 
                            	newMessage.getCommand() == ShortMessage.CONTROL_CHANGE &&
                            	(newMessage.getChannel() == tuple.keyChannel || tuple.keyChannel == tuple.KEYCHANNEL_OMNI))
                                {
                                // we intercept this
                                handleKeyRawCC(newMessage);
                                }
                            else
                                {
                                // pass it on!
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


    /** Same as setupMIDI(message, null), with a default "you are disconnected" message. */
    public boolean setupMIDI() { return setupMIDI("You are disconnected. Choose MIDI devices to send to and receive from.", null); }
        
    /** Lets the user set up the MIDI in/out/key devices.  The old devices are provided in oldTuple,
        or you may pass null in if there are no old devices.  Returns TRUE if a new tuple was set up. */
    public boolean setupMIDI(String message, Midi.Tuple oldTuple)
        {
        Midi.Tuple result = Midi.getNewTuple(oldTuple, this, message, buildInReceiver(), buildKeyReceiver());
        boolean retval = false;
                
        if (result == Midi.FAILED)
            {
            JOptionPane.showOptionDialog(this, "An error occurred while trying to connect to the chosen MIDI devices.",  
                "Cannot Connect", JOptionPane.DEFAULT_OPTION, 
                JOptionPane.WARNING_MESSAGE, null,
                new String[] { "Revert" }, "Revert");
            }
        else if (result == Midi.CANCELLED)
            {
            // nothing
            }
        else
            {
            if (tuple != null)
                tuple.dispose();            
            tuple = result;
            retval = true;
            setSendMIDI(true);
            updateTitle();
            }
                
        return retval;
        }


    /** Removes the in/out/key devices. */
    public void doDisconnectMIDI()
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


    /** Override this to slow down MIDI transmission via a thread sleep.  Returns ms. */
    public int getPauseBetweenMIDISends() { return 0; }
    
    public long lastMIDISend = 0;
    
    /** Attempts to send a NON-Sysex MIDI message. Returns false if (1) the data was empty or null (2)
        synth has turned off the ability to send temporarily (3) the sysex message is not
        valid (4) an error occurred when the receiver tried to send the data.  */
    public boolean tryToSendMIDI(MidiMessage message)
        {
        if (message == null) 
            return false;

        if (getSendMIDI())
            {
            if (tuple == null) return false;
            Receiver receiver = tuple.out;
            if (receiver == null) return false;
            
            // compute pause
            long pauseSoFar = System.currentTimeMillis() - lastMIDISend;
            long expectedPause = getPauseBetweenMIDISends();
            if (pauseSoFar >= 0 && pauseSoFar < expectedPause)
                {
                try { Thread.currentThread().sleep(expectedPause - pauseSoFar); }
                catch (Exception e) { e.printStackTrace(); }
                }
                        
            receiver.send(message, -1); 
            lastMIDISend = System.currentTimeMillis();
            return true;
            }
        else
            return false;
        }
                
                        

    /** Attempts to send a single MIDI sysex message. Returns false if (1) the data was empty or null (2)
        synth has turned off the ability to send temporarily (3) the sysex message is not
        valid (4) an error occurred when the receiver tried to send the data.  */
    public boolean tryToSendSysex(byte[] data)
        {
        if (data == null || data.length == 0) 
            return false;

        if (getSendMIDI())
            {
            if (tuple == null) return false;
            Receiver receiver = tuple.out;
            if (receiver == null) return false;

            // compute pause
            long pauseSoFar = System.currentTimeMillis() - lastMIDISend;
            long expectedPause = getPauseBetweenMIDISends();
            if (pauseSoFar >= 0 && pauseSoFar < expectedPause)
                {
                try { Thread.currentThread().sleep(expectedPause - pauseSoFar); }
                catch (Exception e) { e.printStackTrace(); }
                }
                        
            try { 
                SysexMessage m = new SysexMessage(data, data.length);
                receiver.send(m, -1); 
                lastMIDISend = System.currentTimeMillis();
                return true; 
                }
            catch (InvalidMidiDataException e) { e.printStackTrace(); return false; }
            }
        else
            return false;
        }
    
    /** Attempts to send several MIDI sysex or other kinds of messages. Returns false if (1) the data was empty or null (2)
        synth has turned off the ability to send temporarily (3) the sysex message is not
        valid (4) an error occurred when the receiver tried to send the data.  */
    public boolean tryToSendSysex(Object[] data)
        {
        for(int i = 0; i < data.length; i++)
            {
            if (data[i] == null) return false;
            else if (data[i] instanceof byte[])
                {
                byte[] sysex = (byte[])(data[i]);
                if (!tryToSendSysex(sysex))
                    return false;
                }
            else if (data[i] instanceof MidiMessage)
                {
                MidiMessage message = (MidiMessage)(data[i]);
                if (!tryToSendMIDI(message))
                    return false;
                }
            }
        return true;
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
        catch (Exception e2)
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

        JMenuItem _new = new JMenuItem("New " + getSynthNameLocal());
        _new.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(_new);
        _new.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                doNew();
                }
            });

        JMenu newSynth = new JMenu("New Synth");
        menu.add(newSynth);
        String[] synthNames = getSynthNames();
        for(int i = 0; i < synths.length; i++)
            {
            final int _i = i;
            JMenuItem synthMenu = new JMenuItem(synthNames[i]);
            synthMenu.addActionListener(new ActionListener()
                {
                public void actionPerformed(ActionEvent e)
                    {
                    doNewSynth(_i);
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
                doDuplicateSynth();
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
                
        undoMenu = new JMenuItem("Undo");
        undoMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(undoMenu);
        undoMenu.setEnabled(false);
        undoMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doUndo();
                }
            });
            
        redoMenu = new JMenuItem("Redo");
        redoMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(redoMenu);
        redoMenu.setEnabled(false);
        redoMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doRedo();
                }
            });            
            
        menu = new JMenu("MIDI");
        menubar.add(menu);
                
        
     	receiveCurrent = new JMenuItem("Request Current Patch");
        receiveCurrent.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(receiveCurrent);
        receiveCurrent.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doRequestCurrentPatch();
                }
            });

        JMenuItem receive = new JMenuItem("Request Patch...");
        //receive.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(receive);
        receive.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doRequestPatch();
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
        JMenuItem merge100 = new JMenuItem("Merge in 100%");
        merge.add(merge100);
        
        merge25.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doRequestMerge(0.25);
                }
            });

        merge50.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doRequestMerge(0.50);
                }
            });

        merge75.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doRequestMerge(0.75);
                }
            });

        merge100.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doRequestMerge(1.0);
                }
            });
                
        menu.addSeparator();

        transmit = new JMenuItem("Send Patch");
        menu.add(transmit);
        transmit.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doSendPatch();
                }
            });

        transmitTo = new JMenuItem("Send to Patch...");
        menu.add(transmitTo);
        transmitTo.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doSendToPatch();
                }
            });
                
        transmitCurrent = new JMenuItem("Send to Current Patch");
        menu.add(transmitCurrent);
        transmitCurrent.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doSendToCurrentPatch();
                }
            });

        JMenu randomize = new JMenu("Randomize");
        menu.add(randomize);
        JMenuItem randomize5 = new JMenuItem("Randomize by 5%");
        randomize.add(randomize5);
        JMenuItem randomize10 = new JMenuItem("Randomize by 10%");
        randomize.add(randomize10);
        JMenuItem randomize25 = new JMenuItem("Randomize by 25%");
        randomize.add(randomize25);
        JMenuItem randomize50 = new JMenuItem("Randomize by 50%");
        randomize.add(randomize50);
        JMenuItem randomize100 = new JMenuItem("Randomize by 100%");
        randomize.add(randomize100);
        
        randomize5.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doMutate(0.05);
                }
            });

        randomize10.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doMutate(0.1);
                }
            });

        randomize25.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doMutate(0.25);
                }
            });

        randomize50.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doMutate(0.5);
                }
            });

        randomize100.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doMutate(1.0);
                }
            });


        JMenuItem reset = new JMenuItem("Reset");
        menu.add(reset);
        reset.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doReset();
                }
            });


        menu.addSeparator();

        writeTo = new JMenuItem("Write to Patch...");
        writeTo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(writeTo);
        writeTo.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doWriteToPatch();
                }
            });
                
        menu.addSeparator();

        JMenuItem change = new JMenuItem("Change MIDI");
        menu.add(change);
        change.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doChangeMIDI();
                }
            });
            
        JMenuItem disconnect = new JMenuItem("Disconnect MIDI");
        menu.add(disconnect);
        disconnect.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doDisconnectMIDI();
                }
            });

        menu.addSeparator();

        JMenuItem allSoundsOff = new JMenuItem("Send All Sounds Off");
        menu.add(allSoundsOff);
        allSoundsOff.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doSendAllSoundsOff();
                }
            });
                
        JMenuItem testNote = new JMenuItem("Send Test Note");
        testNote.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(testNote);
        testNote.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doSendTestNote();
                }
            });
            
        menu.addSeparator();

        passThroughCCMenuItem = new JCheckBoxMenuItem("Pass Through All CCs");
        menu.add(passThroughCCMenuItem);
        passThroughCCMenuItem.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doPassThroughCC(passThroughCCMenuItem.getState());
                }
            });
       	String val = getLastX("PassThroughCC", getSynthNameLocal());
       	setPassThroughCC(val != null && val.equalsIgnoreCase("true"));

        learningMenuItem = new JMenuItem("Map CC");
        learningMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(learningMenuItem);
        learningMenuItem.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doMapCC();
                }
            });
                
        JMenuItem clearAllCC = new JMenuItem("Clear all CCs");
        menu.add(clearAllCC);
        clearAllCC.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doClearAllCC();
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
                doPreviousTab();
                }
            });


        JMenuItem next = new JMenuItem("Next Tab");
        next.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(next);
        next.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doNextTab();
                }
            });


        JMenuItem taba = new JMenuItem("Tab 1");
        taba.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(taba);
        taba.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doTab(0);
                }
            });

        taba = new JMenuItem("Tab 2");
        taba.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(taba);
        taba.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doTab(1);
                }
            });

        taba = new JMenuItem("Tab 3");
        taba.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(taba);
        taba.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doTab(2);
                }
            });

        taba = new JMenuItem("Tab 4");
        taba.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(taba);
        taba.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doTab(3);
                }
            });

        taba = new JMenuItem("Tab 5");
        taba.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(taba);
        taba.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doTab(4);
                }
            });

        taba = new JMenuItem("Tab 6");
        taba.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_6, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(taba);
        taba.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doTab(5);
                }
            });

        taba = new JMenuItem("Tab 7");
        taba.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_7, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(taba);
        taba.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doTab(6);
                }
            });

        taba = new JMenuItem("Tab 8");
        taba.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_8, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(taba);
        taba.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doTab(7);
                }
            });
        
        
        // Set up Windows  
        if (Style.isWindows() || Style.isUnix())
        	{
        	JMenu help = new JMenu("Help");
        	JMenu about = new JMenu("About Edisyn");
        	about.addActionListener(new ActionListener()
        		{
        		public void actionPerformed(ActionEvent e)
        			{
        			doAbout();
        			}
        		});
        	}
        	
        	//	-XDignore.symbol.file
        	
        // Set up Mac
        Mac.setup(this);

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

    /** Only revises / issues warnings on out-of-bounds numerical parameters. 
        You probably want to override this to check more stuff. */
    public void revise()
        {
        String[] keys = model.getKeys();
        for(int i = 0; i < keys.length; i++)
            {
            String key = keys[i];
            if (!model.isString(key) &&
                model.minExists(key) && 
                model.maxExists(key))
                {
                // verify
                int val = model.get(key, 0);
                if (val < model.getMin(key))
                    { model.set(key, model.getMin(key)); System.err.println("Warning: Revised " + key + " from " + val + " to " + model.get(key, 0));}
                if (val > model.getMax(key))
                    { model.set(key, model.getMax(key)); System.err.println("Warning: Revised " + key + " from " + val + " to " + model.get(key, 0));}
                }
            }
        }

    public void merge(byte[] data, double probability)
        {
        setSendMIDI(false);
        undo.setWillPush(false);
        Model backup = (Model)(model.clone());

        Synth newSynth = instantiate(Synth.this.getClass(), getSynthNameLocal(), true, false, tuple);
        newSynth.parse(data, true);
        model.recombine(random, newSynth.getModel(), probability);
        revise();  // just in case
                
        undo.setWillPush(true);
        if (!backup.keyEquals(getModel()))  // it's changed, do an undo push
            undo.push(backup);
        setSendMIDI(true);
        sendAllParameters();

        // this last statement fixes a mystery.  When I call Randomize or Reset on
        // a Blofeld or on a Microwave, all of the widgets update simultaneously.
        // But on a Blofeld Multi or Microwave Multi they update one at a time.
        // I've tried a zillion things, even moving all the widgets from the Blofeld Multi
        // into the Blofeld, and it makes no difference!  For some reason the OS X
        // repaint manager is refusing to coallesce their repaint requests.  So I do it here.
        repaint();
        }

    public void doMutate(double probability)
        {
        setSendMIDI(false);
        undo.setWillPush(false);
        Model backup = (Model)(model.clone());
                
        model.mutate(random, probability);
        revise();  // just in case
                
        undo.setWillPush(true);
        if (!backup.keyEquals(getModel()))  // it's changed, do an undo push
            undo.push(backup);
        setSendMIDI(true);
        sendAllParameters();

        // this last statement fixes a mystery.  When I call Randomize or Reset on
        // a Blofeld or on a Microwave, all of the widgets update simultaneously.
        // But on a Blofeld Multi or Microwave Multi they update one at a time.
        // I've tried a zillion things, even moving all the widgets from the Blofeld Multi
        // into the Blofeld, and it makes no difference!  For some reason the OS X
        // repaint manager is refusing to coallesce their repaint requests.  So I do it here.
        repaint();
        }

    /** Perform a patch merge */
    void doMerge(double probability)
        {
        Model tempModel = new Model();
        if (gatherInfo("Request Merge", tempModel, false))
            {
            Synth.this.merging = probability;
            performRequestDump(tempModel);
            }
        }

	void doPassThroughCC(boolean val)
		{
		setPassThroughCC(val);
		}

    public int getChannelOut()
        {
        int channel = 1;
        if (tuple != null)
            channel = tuple.outChannel;
        return channel;
        }
    
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
        if (isSynced() || !changePatch)         // if we're synced, or if we're not changing patches....
            {
            if (changePatch)
                changePatch(getModel());        
                
            if (sendsAllParametersInBulk)
                {
                tryToSendSysex(emitAll(getModel(), true));
                }
            else
                {
                String[] keys = getModel().getKeys();
                for(int i = 0; i < keys.length; i++)
                    {
                    tryToSendSysex(emitAll(keys[i]));
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
                os.write(flatten(emitAll((Model)null, false)));
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


    // Flattens a two-dimensional array to a one-dimensional array,
    // stripping out the non-sysex elements
    byte[] flatten(Object[] data)
        {
        if (data == null)
            return null;
        if (data.length == 0)
            return new byte[0];
        if (data.length == 1 && data[0] instanceof byte[])
            return (byte[])(data[0]);
                
        // otherwise flatten
                
        int len = 1;
        for(int i = 0; i < data.length; i++)
            {
            if (data[i] instanceof byte[])
                {
                len *= ((byte[])data[i]).length;
                }
            }
                        
        byte[] result = new byte[len];
        int start = 0;
        for(int i = 0; i < data.length; i++)
            {
            if (data[i] instanceof byte[])
                {
                byte[] b = (byte[])(data[i]);
                System.arraycopy(b, 0, result, start, b.length);
                start += b.length;
                }
            }
        return result;
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
                os.write(flatten(emitAll((Model)null, false)));
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
            // send all sounds off
            try
                {
                for(int i = 0; i < 16; i++)
                    tryToSendMIDI(new ShortMessage(ShortMessage.CONTROL_CHANGE, i, 120, 0));
                }
            catch (Exception e)
                {
                e.printStackTrace();
                }
                                
            // get rid of MIDI connection
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
				Synth result = doNewSynthPanel();
				if (result == null)
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
        
    void setLastDirectory(String path) { setLastX(path, "LastDirectory", getSynthNameLocal()); }
    String getLastDirectory() { return getLastX("LastDirectory", getSynthNameLocal()); }
    
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
                if (f.length() > MAX_FILE_LENGTH)
                    JOptionPane.showMessageDialog(this, "File is too long and cannot be loaded.", "File Error", JOptionPane.ERROR_MESSAGE);
                else
                    {
                    byte[] data = new byte[(int)f.length()];

                    int val = is.read(data, 0, (int)f.length());
                    is.close();
                                
                    if (!recognizeLocal(data))
                        JOptionPane.showMessageDialog(this, "File does not contain proper sysex data for the " + getSynthNameLocal(), "File Error", JOptionPane.ERROR_MESSAGE);
                    else
                        {
                        setSendMIDI(false);
                        undo.setWillPush(false);
                        Model backup = (Model)(model.clone());
                        parse(data, true);
                        undo.setWillPush(true);
                        if (!backup.keyEquals(getModel()))  // it's changed, do an undo push
                            undo.push(backup);
                        setSendMIDI(true);
                        file = f;
                        setLastDirectory(fd.getDirectory());

                        // this last statement fixes a mystery.  When I call Randomize or Reset on
                        // a Blofeld or on a Microwave, all of the widgets update simultaneously.
                        // But on a Blofeld Multi or Microwave Multi they update one at a time.
                        // I've tried a zillion things, even moving all the widgets from the Blofeld Multi
                        // into the Blofeld, and it makes no difference!  For some reason the OS X
                        // repaint manager is refusing to coallesce their repaint requests.  So I do it here.
                        repaint();
                        }
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
        p.add(new JLabel("Select a Synthesizer to Edit:"), BorderLayout.CENTER);
        p.add(new JLabel("    "), BorderLayout.SOUTH);

        JPanel p2 = new JPanel();
        p2.setLayout(new BorderLayout());
        p2.add(p, BorderLayout.NORTH);
        String[] synthNames = getSynthNames();
        JComboBox combo = new JComboBox(synthNames);
        combo.setMaximumRowCount(32);
                
        // Note: Java classdocs are wrong: if you set a selected item to null (or to something not in the list)
        // it doesn't just not change the current selected item, it sets it to some blank item.
        String synth = getLastSynth();
        if (synth != null) combo.setSelectedItem(synth);
        p2.add(combo, BorderLayout.CENTER);
                
        int result = JOptionPane.showOptionDialog(null, p2, "Edisyn", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[] { "Run", "Quit", "Disconnected" }, "Run");
        if (result == 1 || 	// cancel
        	result < 0)		// window closed
        	 return null;
        else 
            {
            setLastSynth("" + combo.getSelectedItem());
            return instantiate(synths[combo.getSelectedIndex()], synthNames[combo.getSelectedIndex()], false, (result == 0), null);
            }
        }
    
    public abstract String getDefaultResourceFileName();
    
    boolean learning = false;
    
    JMenuItem learningMenuItem;
    
    public boolean getLearningCC() { return learning; }
    
    public void setLearningCC(boolean val)
        {
        learning = val;
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
        setLearningCC(false);
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
    
    public void handleSynthCCOrNRPN(Midi.CCData data)
    	{
    	// do nothing by default
    	}
    
    void handleInRawCC(ShortMessage message)
    	{
        Midi.CCData ccdata = midi.synthParser.processCC(message, true, false);
        if (ccdata != null)
        	{
        	handleSynthCCOrNRPN(ccdata);
        	}
    	}
    	
 	void handleKeyRawCC(ShortMessage message)
        {
        Midi.CCData ccdata = midi.controlParser.processCC(message, true, false);
        if (ccdata != null)
        	{
        	if (ccdata.type == Midi.CCDATA_TYPE_NRPN)
        		ccdata.number += 256;
			if (learning)
				{
				String key = model.getLastKey();
				if (key != null)
					{
					ccmap.setKeyForCCPane(ccdata.number, getCurrentTab(), key);
					setLearningCC(false);
					}
				}
			else
				{
				String key = ccmap.getKeyForCCPane(ccdata.number, getCurrentTab());
				if (key != null)
					{
					// handle increment/decrement
					if (ccdata.increment)
						{
						ccdata.value = ccdata.value + model.get(key, 0);
						}

					// handle the situation where the range is larger than the CC/NRPN message,
					// else bump it to min
					if (model.minExists(key) && model.maxExists(key))
						{
						if (ccdata.type == Midi.CCDATA_TYPE_RAW_CC)
							{
							int min = model.getMin(key);
							int max = model.getMax(key);
							if (max - min + 1 > 127)  // uh oh
								{
								ccdata.value = (int)(((max - min + 1) / (double) 127) * ccdata.value);
								}
							else
								{
								ccdata.value = min + ccdata.value;
								}
							}
						else if (ccdata.type == Midi.CCDATA_TYPE_NRPN)
							{
							int min = model.getMin(key);
							int max = model.getMax(key);
							if (max - min + 1 > 16383)  // uh oh, but very unlikely
								{
								ccdata.value = (int)(((max - min + 1) / (double) 16383) * ccdata.value);
								}
							else
								{
								ccdata.value = min + ccdata.value;
								}
							}
						}

					model.setBounded(key, ccdata.value);
					}
				}
			}
        }
    
    // Note that this isn't wrapped in undo, so we can block it at instantiation
    public void loadDefaults()
    	{
    	loadDefaults(false);
    	}

    public void loadDefaults(boolean ignorePatch)
        {
        InputStream stream = getClass().getResourceAsStream(getDefaultResourceFileName());
        if (stream != null)
            {
            try 
                {
                byte[] buffer = new byte[MAX_FILE_LENGTH];   // better not be longer than this
                int size = stream.read(buffer, 0, MAX_FILE_LENGTH);

                // now shorten
                byte[] data = new byte[size];
                System.arraycopy(buffer, 0, data, 0, size);

                // parse                        
                setSendMIDI(false);
                parse(data, ignorePatch);
                setSendMIDI(true);
                model.setUndoListener(undo);    // okay, redundant, but that way the pattern stays the same

                // this last statement fixes a mystery.  When I call Randomize or Reset on
                // a Blofeld or on a Microwave, all of the widgets update simultaneously.
                // But on a Blofeld Multi or Microwave Multi they update one at a time.
                // I've tried a zillion things, even moving all the widgets from the Blofeld Multi
                // into the Blofeld, and it makes no difference!  For some reason the OS X
                // repaint manager is refusing to coallesce their repaint requests.  So I do it here.
                repaint();
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
            System.err.println("Warning: Didn't Parse");
            }
        }  

        
    public void doRequestCurrentPatch()
        {
        if (tuple == null || tuple.out == null)
            {
            if (!setupMIDI())
                return;
            else
                setSynced(false);
            }
                
        Synth.this.merging = 0.0;
        performRequestCurrentDump();
        }  
    
    public void doRequestPatch()
        {
        if (tuple == null || tuple.out == null)
            {
            if (!setupMIDI())
                return;
            else
                setSynced(false);
            }
                
        Model tempModel = new Model();
        if (gatherInfo("Request Patch", tempModel, false))
            {
            Synth.this.merging = 0.0;
            performRequestDump(tempModel);
            }
        } 
        
    public void doRequestMerge(double percentage)
        {
        if (tuple == null || tuple.out == null)
            {
            if (!setupMIDI())
                return;
            else
                setSynced(false);
            }
                
        doMerge(percentage);
        }
                
    public void doSendPatch()
        {
        if (tuple == null || tuple.out == null)
            {
            if (!setupMIDI())
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
            if (gatherInfo("You aren't synced.  ", getModel(), true))
                {
                setSynced(true);
                sendAllParameters();
                }
            }
        }
        
    public void doSendToPatch()
        {
        if (tuple == null || tuple.out == null)
            {
            if (!setupMIDI())
                return;
            else
                setSynced(false);
            }
                
        if (gatherInfo("Send Patch To...", getModel(), true))
            {
            setSynced(true);
            sendAllParameters();
            }
        }       
                
    public void doSendToCurrentPatch()
        {
        if (tuple == null || tuple.out == null)
            {
            if (!setupMIDI())
                return;
            else
                setSynced(false);
            }
                
        sendAllParameters(false);
        }
                
    public void doReset()
        {
        setSendMIDI(false);
        // because loadDefaults isn't wrapped in an undo, we have to
        // wrap it manually here
        undo.setWillPush(false);
        Model backup = (Model)(model.clone());
        loadDefaults(true);
        undo.setWillPush(true);
        if (!backup.keyEquals(getModel()))  // it's changed, do an undo push
            undo.push(backup);
        setSendMIDI(true);
        sendAllParameters();
                
        // this last statement fixes a mystery.  When I call Randomize or Reset on
        // a Blofeld or on a Microwave, all of the widgets update simultaneously.
        // But on a Blofeld Multi or Microwave Multi they update one at a time.
        // I've tried a zillion things, even moving all the widgets from the Blofeld Multi
        // into the Blofeld, and it makes no difference!  For some reason the OS X
        // repaint manager is refusing to coallesce their repaint requests.  So I do it here.
        repaint();
        }
                
    public void doWriteToPatch()
        {
        if (tuple == null || tuple.out == null)
            {
            if (!setupMIDI())
                return;
            else
                setSynced(false);
            }
                
        if (gatherInfo("Write Patch To...", getModel(), true))
            {
            changePatch(getModel());
            if (tryToSendSysex(emitAll(getModel(), false)))
                {
                setSynced(true);
                }
            }
        }
                
    public void doChangeMIDI()
        {
        if (!setupMIDI("Choose new MIDI devices to send to and receive from.", tuple))
            return;
        else
            setSynced(false);
        }
                
    public void doSendAllSoundsOff()
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
                
    public void doSendTestNote()
        {
        try
            {
            int channel = getChannelOut();
            tryToSendMIDI(new ShortMessage(ShortMessage.NOTE_ON, channel - 1, 60, 127));
                                        
            // schedule a note off
            final int myNoteOnTick = ++noteOnTick;
            javax.swing.Timer timer = new javax.swing.Timer(500,
                new ActionListener()
                    {
                    public void actionPerformed(ActionEvent e)
                        {
                        if (noteOnTick == myNoteOnTick)  // no more note on messages
                            try
                                {
                                tryToSendMIDI(new ShortMessage(ShortMessage.NOTE_OFF, channel - 1, 60, 127));
                                }
                            catch (Exception e3)
                                {
                                e3.printStackTrace();
                                }
                        }
                    });
            timer.setRepeats(false);
            timer.start();
            }
        catch (Exception e2)
            {
            e2.printStackTrace();
            }           
        }

    public void doMapCC()
        {
        // has to be done first because doPassThroughCC(false) may turn it off
        setLearningCC(!getLearningCC());
		doPassThroughCC(false);
        }
                
    public void doClearAllCC()
        {
        if (JOptionPane.showConfirmDialog(Synth.this, "Are you sure you want to clear all CCs?", "Clear CCs", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null) == JOptionPane.OK_OPTION)
            {
            clearLearned();
            }
        }
                
    public void doPreviousTab()
        {
        setCurrentTab(getCurrentTab() - 1);
        }
                
    public void doNextTab()
        {
        setCurrentTab(getCurrentTab() + 1);
        }
                
    public void doTab(int tab)
        {
        setCurrentTab(tab);
        }
                
    public void doNew()
        {
        instantiate(Synth.this.getClass(), getSynthNameLocal(), false, true, tuple);
        }
                
    public void doNewSynth(int synth)
        {
        String[] synthNames = getSynthNames();
        instantiate(synths[synth], synthNames[synth], false, true, tuple);
        }
                
    public void doDuplicateSynth()
        {
        Synth newSynth = instantiate(Synth.this.getClass(), getSynthNameLocal(), false, true, tuple);
        newSynth.setSendMIDI(false);
        model.copyValuesTo(newSynth.model);
        newSynth.setSynced(false);
        newSynth.setSendMIDI(true);
        }
                
    public void doUndo()
        {
        if (model.equals(undo.top()))
            model = undo.undo(null);  // don't push into the redo stack
        model = undo.undo(model);
        model.updateAllListeners();
        setSynced(false);
        }
                
    public void doRedo()
        {
        model = (Model)(undo.redo(getModel()));
        model.updateAllListeners();
        }
        
    public void doQuit()
    	{
    	JOptionPane.showMessageDialog(null, 
                                  "quit", 
                                  "quit", 
                                  JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    	}
    
    public void doPrefs()
    	{
    	// do nothing
    	}
    
    public void doAbout()
    	{
    	JOptionPane.showMessageDialog(null, 
                                  "about", 
                                  "about", 
                                  JOptionPane.INFORMATION_MESSAGE);
    	}

	/** Function for tweaking a name to make it valid for display in the editor.
		The default version just does a right-trim of whitespace on the name.  You
		may wish to override this to also restrict the valid characters and the name
		length. */
    public String reviseName(String name)
    	{
    	// right-trim the name
        int i = name.length()-1;
        while (i >= 0 && Character.isWhitespace(name.charAt(i))) i--;
        name = name.substring(0, i+1);
        return name;
    	}
 
    }
