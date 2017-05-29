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
        
    /** Handles mutation (or not) of keys declared immutable in the Model.
        When the user is mutating (randomizing) a parameter, and it was declared
        immutable, this method is called instead to let you change the parameter
        as you like (or refuse to). */
    public abstract void immutableMutate(String key);
        
    /** Updates the model to reflect the following sysex patch dump for your synthesizer type. */
    public abstract void parse(byte[] data);
    
    /** Merges the given model into yours, replacing elements of your model with the given
    	probability. */
    public abstract void merge(Model model, double probability);
    
    /** Produces a sysex patch dump suitable to send to a remote synthesizer. 
        If you return a zero-length byte array, nothing will be sent.  
        If tempModel is non-null, then it should be used to extract meta-parameters
        such as the bank and patch number (stuff that's specified by gatherInfo(...).
        Otherwise the primary model should be used.  The primary model should be used
        for all other parameters.  */
    public abstract byte[] emit(Model tempModel);
    
    /** Produces a sysex parameter change request for the given parameter.  
        If you return a zero-length byte array, nothing will be sent.  */
    // TODO: maybe we should also permit CC and NRPN
    public abstract byte[] emit(String key);
    
    /** Produces a sysex message to send to a synthesizer to request it to initiate
        a patch dump to you.  If you return a zero-length byte array, nothing will be sent. 
        If tempModel is non-null, then it should be used to extract meta-parameters
        such as the bank and patch number (stuff that's specified by gatherInfo(...).
        Otherwise the primary model should be used.  The primary model should be used
        for all other parameters.  
        */
    public abstract byte[] requestDump(Model tempModel);
    
    /** Creates and returns new, empty synth editor window of the same type as yours.  
    	The window should not be visible.  If withMIDIConnection is FALSE,
    	then the Synth should not attempt to connect to MIDI -- it'll just be used as
    	a temporary repository, probably for merging, and then discarded.
    	See Blofeld.doNew() for inspiration. */
    public abstract Synth doNew();
    
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
    public static boolean recognize(byte[] data)
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
    
    /** Returns the name of the current patch. */
    public abstract String getPatchName();
    
        
    /** Updates the JFrame title to reflect the synthesizer type, the patch information, and the filename
        if any.  See Blofeld.updateTitle() for inspiration. */
    public void updateTitle()
        {
        JFrame frame = ((JFrame)(SwingUtilities.getRoot(this)));
        if (frame != null) 
        	frame.setTitle(getSynthName().trim() + "        " + 
        				   getPatchName().trim() + 
        				   (getFile() == null ? "" : "        " + getFile().getName()) +
        				   (in == null ? "        (DISCONNECTED)" : ""));
        }

        

    public Synth() 
        {
		try {
                System.setProperty("apple.laf.useScreenMenuBar", "true");
                System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Test");
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        	}
        catch(Exception e) { }


        model = new Model();
        model.register(Model.ALL_KEYS, this);
        random = new Random(System.currentTimeMillis());
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
        return recognize(this.getClass(), data);
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
        }
    
    /** Called by the model to update the synth whenever a parameter is changed. */
    public void update(String key, Model model)
        {
        if (getSendMIDI())
            tryToSendSysex(emit(key));
        }

    // A helper class which outputs the name of the MIDI device properly so
    // JComboBoxes in JOptionPane can display them nicely.
    static class MidiDeviceName
        {
        public MidiDevice device;
        public MidiDeviceName(MidiDevice d) { device = d; }
        public String toString() 
        	{ 
        	String desc = device.getDeviceInfo().getDescription().trim();
        	
        	// All CoreMIDI4J names begin with "CoreMIDI4J - "
        	String name = device.getDeviceInfo().getName().substring(13).trim();

        	if (name.equals(""))
	        	return desc; 
			else 
	        	return desc + ": " + name; 
        	}
        }
        
    int indexOfMidiDevice(MidiDevice dev, ArrayList list)
    	{
    	for(int i = 0; i < list.size(); i++)
    		{
    		// Sadly, new device are returned each time by the MIDI system, GARGH.
    		// So for NOW :-( we're going by comparing strings to do defaults.  This obviously will fail
    		// in a lot of cases. 
    		if ((list.get(i)) instanceof String)		// it's "None"
    			{
    			if (dev == null) return i;
    			}
    		else
    			{
	    		if ((new MidiDeviceName(dev).toString()).equals(
	    			((MidiDeviceName)(list.get(i))).toString()))
	    			return i;
	    		}
    		}
    	return -1;
    	}
    
    // Synchronize the device access.  Not that it'll make much difference
    // as I'm trying to make sure everything is accessed from the Swing Event Thread
    Object lock = new Object[0];
        
    // Our Keyboard
    MidiDevice key = null;
    MidiDeviceName keyn = null;
    Transmitter keyr = null;
    public static final int KEYCHANNEL_NONE = 0;
    int keyChannel = KEYCHANNEL_NONE;
    
    // Our input device
    MidiDevice in = null;   
    MidiDeviceName inn = null;
    Transmitter inr = null;
        
    // Our output device
    MidiDevice out = null;
    MidiDeviceName outn = null;
    Receiver outr = null;
    int sendChannel = 1;
        
    /** Returns our output device */
    public MidiDevice getReceiverDevice() 
    	{ 
    	synchronized(lock) 
    		{ return out; }
    	}

    /** Returns our input device */
    public MidiDevice getTransmitterDevice() 
    	{ 
    	synchronized(lock) 
    	{ return in; } 
    	}

	/** Returns our keyboard device */
	public MidiDevice getKeyboardDevice() 
		{ 
		synchronized(lock) 
		{ return in; } 
		}

    /** Returns our output stream */
    public Receiver getReceiver() 
    	{ 
    	synchronized(lock) 
    	{ return outr; } 
    	}

    /** Returns our input stream */
    public Transmitter getTransmitter() 
    	{ 
    	synchronized(lock) 
    	{ return inr; } 
    	}
        
	/** Returns our keyboard device */
	public Transmitter getKeyboard() 
		{ 
		synchronized(lock) 
		{ return inr; } 
		}

    /** Attaches the synth to receive sysex provided by the transmitter.  To do
        this, the transmitter must actually exist (not be null). */
    public void attachToTransmitter()
        {
        if (inr != null) 
        	inr.setReceiver(new Receiver()
            {
            public void close()
                {
                // this doesn't matter since we're not returning the Receiver, but whatever
                synchronized(lock) 
                    { 
                    if (inr != null) 
                        inr.close(); 
                    }
                inr = null;
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
                				Synth newSynth = doNew();
                				newSynth.parse(data);
                				sendMIDI = false;
                				merge(newSynth.getModel(), 0.5);
                				sendMIDI = true;
                				sendAllParameters();
                				updateTitle();
                            	}
                            else
                            	{
								sendMIDI = false;  // so we don't send out parameter updates in response to reading/changing parameters
								parse(data);
								sendMIDI = true;
                				sendAllParameters();
								file = null;
								updateTitle();
								}
                            }
                        });
                    }
                }
            });

        if (keyr != null) 
        	keyr.setReceiver(new Receiver()
            {
            public void close()
                {
                // this doesn't matter since we're not returning the Receiver, but whatever
                synchronized(lock) 
                    { 
                    if (keyr != null) 
                        keyr.close(); 
                    }
                keyr = null;
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

							tryToSendMIDI(newMessage);
							}
						catch (InvalidMidiDataException e)
							{
							e.printStackTrace();
							}

						}
					else if (message instanceof SysexMessage)
						tryToSendSysex(message.getMessage());
                	}
        		}
        	});
        }


	public boolean setupMIDI(String message)
		{
		return setupMIDI(message, null, null, null, 1, KEYCHANNEL_NONE);
		}
	
	public boolean setupMIDI(String message, MidiDevice previousTrans, MidiDevice previousRecv, MidiDevice previousKeybd, int previousOutChannel, int previousKeyChannel)
		{
		synchronized(lock)
			{
			Transmitter old_inr = inr;
			MidiDevice old_in = in;
			MidiDeviceName old_inn = inn;
			Receiver old_outr = outr;
			MidiDevice old_out = out;
			MidiDeviceName old_outn = outn;
			Transmitter old_keyr = keyr;
			MidiDevice old_key = key;
			MidiDeviceName old_keyn = keyn;
			
			boolean returnval = false;
			int result = gatherMidiDevices(message, previousTrans, previousRecv, previousKeybd, previousOutChannel, previousKeyChannel);
			if (result == Synth.FAILED || result == Synth.CANCELLED)
				{
				if (result == Synth.FAILED)
					{
                	JOptionPane.showOptionDialog(this, "An error occurred while trying to connect to the chosen MIDI devices.",  
                							 "Cannot Connect", JOptionPane.DEFAULT_OPTION, 
                							 JOptionPane.WARNING_MESSAGE, null,
                							 new String[] { "Revert" }, "Revert");
					//JOptionPane.showMessageDialog(this, "An error occurred while trying to connect to the chosen MIDI devices.  Reverting.",  "Failed to Connect", JOptionPane.ERROR_MESSAGE);
					}
				inr = old_inr;
				in = old_in;
				inn = old_inn;
				outr = old_outr;
				out = old_out;
				outn = old_outn;
				keyr = old_keyr;
				key = old_key;
				keyn = old_keyn;
				}
			else
				{
				attachToTransmitter();
				returnval = true;
				}
	        setSendMIDI(true);
	        updateTitle();
	        return returnval;
		    }
		}


	public void disconnectMIDI()
		{
		synchronized(lock)
			{
			if (in != null)
				{
				inr.close();
				in.close();
				outr.close();
				out.close();
				}
				
			inr = null;
			in = null;
			inn = null;
			outr = null;
			out = null;
			outn = null;
		    }
        setSendMIDI(true);
        updateTitle();
		}


    /** Attempts to send a NON-Sysex MIDI message. Returns false if (1) the data was empty or null (2)
        synth has turned off the ability to send temporarily (3) the sysex message is not
        valid (4) an error occurred when the receiver tried to send the data.  */
    public boolean tryToSendMIDI(ShortMessage message)
        {
        synchronized(lock)
        	{
			if (message == null) 
				return false;

			if (getSendMIDI())
				{
				Receiver receiver = getReceiver();
				if (receiver == null) return false;
				receiver.send(message, -1); 
				return true;
				}
			else
				return false;
			}
        }
                
                        

    /** Attempts to send MIDI sysex. Returns false if (1) the data was empty or null (2)
        synth has turned off the ability to send temporarily (3) the sysex message is not
        valid (4) an error occurred when the receiver tried to send the data.  */
    public boolean tryToSendSysex(byte[] data)
        {
        synchronized(lock)
        	{
			if (data == null || data.length == 0) 
				return false;

			if (getSendMIDI())
				{
				Receiver receiver = getReceiver();
				if (receiver == null) return false;
				try { receiver.send(new SysexMessage(data, data.length), -1); return true; }
				catch (InvalidMidiDataException e) { e.printStackTrace(); return false; }
				}
			else
				return false;
			}
        }
                
                        
    public JFrame sprout()
        {
        JFrame frame = new JFrame();
        JMenuBar menubar = new JMenuBar();
        frame.setJMenuBar(menubar);
        JMenu menu = new JMenu("File");
        menubar.add(menu);

        JMenuItem _new = new JMenuItem("New");
		_new.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(_new);
        _new.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
               	Synth newSynth = doNew();
        	    JFrame frame = ((JFrame)(SwingUtilities.getRoot(newSynth)));
        	    frame.setVisible(true);
        	    newSynth.setupMIDI("Choose MIDI devices to send to and receive from.", in, out, key, sendChannel, keyChannel);
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

        menu = new JMenu("MIDI");
        menubar.add(menu);
                
                
        JMenuItem receive = new JMenuItem("Request Patch and Send");
        menu.add(receive);
        receive.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                if (in == null)
                	{
                	if (!setupMIDI("You are disconnected. <p>First choose MIDI devices to send to and receive from."))
                		return;
                	}
                
                Model tempModel = new Model();
                if (gatherInfo("Request Patch and Send", tempModel))
		            tryToSendSysex(requestDump(tempModel));
                }
            });
                
        JMenu merge = new JMenu("Request Merge and Send");
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
                if (in == null)
                	{
                	if (!setupMIDI("You are disconnected. <p>First choose MIDI devices to send to and receive from."))
                		return;
                	}
                
                doMerge(0.25);
                }
            });

        merge50.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                if (in == null)
                	{
                	if (!setupMIDI("You are disconnected. <p>First choose MIDI devices to send to and receive from."))
                		return;
                	}
                
                doMerge(0.50);
                }
            });

        merge75.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                if (in == null)
                	{
                	if (!setupMIDI("You are disconnected. <p>First choose MIDI devices to send to and receive from."))
                		return;
                	}
                
                doMerge(0.75);
                }
            });
                
        JMenuItem transmit = new JMenuItem("Send Patch");
        menu.add(transmit);
        transmit.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                if (in == null)
                	{
                	if (!setupMIDI("You are disconnected. <p>First choose MIDI devices to send to and receive from."))
                		return;
                	}
                
                sendAllParameters();
                }
            });
                
        JMenuItem random = new JMenuItem("Randomize and Send");
        menu.add(random);
        random.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                if (in == null)
                	{
                	if (!setupMIDI("You are disconnected. <p>First choose MIDI devices to send to and receive from."))
                		return;
                	}
                
                mutate(1.0);
                sendAllParameters();
                }
            });


		menu.addSeparator();

        JMenuItem burn = new JMenuItem("Burn Patch");
        menu.add(burn);
        burn.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                if (in == null)
                	{
                	if (!setupMIDI("You are disconnected. <p>First choose MIDI devices to send to and receive from."))
                		return;
                	}
                
                if (gatherInfo("Burn Patch", getModel()))
	                tryToSendSysex(emit(getModel()));
                }
            });
                
		menu.addSeparator();

        JMenuItem change = new JMenuItem("Change MIDI");
        menu.add(change);
        change.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
				setupMIDI("Choose MIDI devices to send to and receive from.");
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
        menu.add(testNote);
        testNote.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                try
                	{
					tryToSendMIDI(new ShortMessage(ShortMessage.NOTE_ON, keyChannel, 60, 127));
					Thread.currentThread().sleep(500);
					tryToSendMIDI(new ShortMessage(ShortMessage.NOTE_OFF, keyChannel, 60, 127));
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
                
        frame.getContentPane().add(this);
        frame.pack();
                
        updateTitle();
        return frame;
        }
    
    void doMerge(double probability)
		{
		Model tempModel = new Model();
		if (gatherInfo("Request Merge and Send", tempModel))
			{
			Synth.this.merging = probability;
			tryToSendSysex(requestDump(tempModel));
			}
		}

    
    /** Sends all the parameters in a patch to the synth.  This differs
    	from sending a bulk patch because it likely doesn't save permanently
    	in memory.  But it's not fast. */
    public void sendAllParameters()
    	{
    	String[] keys = getModel().getKeys();
    	for(int i = 0; i < keys.length; i++)
    		{
    		tryToSendSysex(emit(keys[i]));
    		}
    	}
                
    public static String ensureFileEndsWith(String filename, String ending)
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
        FileDialog fd = new FileDialog((Frame)(SwingUtilities.getRoot(this)), "Save Simulation As...", FileDialog.SAVE);
        if (file == null)
            {
            fd.setFile("Untitled.syx");
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
                f = new File(fd.getDirectory(), ensureFileEndsWith(fd.getFile(), ".syx"));
                os = new FileOutputStream(f);
                os.write(emit((Model)null));
                os.close();
                file = f;
                } 
            catch (IOException e) // fail
                {
                JOptionPane.showMessageDialog(this, "An error occurred while saving to the file " + (f == null ? " " : f.getName()), "File Error", JOptionPane.ERROR_MESSAGE);
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
                os.write(emit((Model)null));
                os.close();
                }
            catch (Exception e) // fail
                {
                JOptionPane.showMessageDialog(this, "An error occurred while saving to the file " + file, "File Error", JOptionPane.ERROR_MESSAGE);
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


    /** Goes through the process of opening a file and loading it into this editor. 
        This does NOT open a new editor window -- it loads directly into this editor. */
    public void doOpen()
        {
        FileDialog fd = new FileDialog((Frame)(SwingUtilities.getRoot(this)), "Load Sysex File...", FileDialog.LOAD);
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
                
                if (val != getExpectedSysexLength())  // uh oh
                    throw new RuntimeException("File too short");

                sendMIDI = false;
                parse(data);
                sendMIDI = true;
                                
                file = f;
                }        
            catch (Throwable e) // fail  -- could be an Error or an Exception
                {
                JOptionPane.showMessageDialog(this, "An error occurred while loading to the file " + f, "File Error", JOptionPane.ERROR_MESSAGE);
                }
            finally
                {
                if (is != null)
                    try { is.close(); }
                    catch (IOException e) { }
                }
                
        updateTitle();
        }
    
    
    


		
		
    
    public static final int FAILED = -1;
    public static final int CANCELLED = 0;
    public static final int SUCCEEDED = 1;
    
	public int gatherMidiDevices(String message, MidiDevice oldIn, MidiDevice oldOut, MidiDevice oldKey, int oldSendChannel, int oldKeyChannel)
		{
        synchronized(lock)
            {
            // Obtain information about all the installed synthesizers.
            MidiDevice.Info[] midi = uk.co.xfactorylibrarians.coremidi4j.CoreMidiDeviceProvider.getMidiDeviceInfo();
            ArrayList transdevices = new ArrayList();
            for(int i = 0; i < midi.length; i++)
                {
                try
                    {
                    MidiDevice d = MidiSystem.getMidiDevice(midi[i]);
                    // get rid of java devices
                    if (d instanceof javax.sound.midi.Sequencer ||
                    	d instanceof javax.sound.midi.Synthesizer)
                    		continue;
                    if (d.getMaxTransmitters() != 0)
                        {
                        transdevices.add(new MidiDeviceName(d));
                        }
                    }
                catch(Exception e) { }
                }

            ArrayList recvdevices = new ArrayList();
            for(int i = 0; i < midi.length; i++)
                {
                try
                    {
                    MidiDevice d = MidiSystem.getMidiDevice(midi[i]);
                    // get rid of java devices
                    if (d instanceof javax.sound.midi.Sequencer ||
                   		d instanceof javax.sound.midi.Synthesizer)
                    		continue;
                    if (d.getMaxReceivers() != 0)
                        {
                        recvdevices.add(new MidiDeviceName(d));
                        }
                    }
                catch(Exception e) { }
                }

            ArrayList keydevices = new ArrayList();
            keydevices.add("None");
            for(int i = 0; i < midi.length; i++)
                {
                try
                    {
                    MidiDevice d = MidiSystem.getMidiDevice(midi[i]);
                    // get rid of java devices
                    if (d instanceof javax.sound.midi.Sequencer ||
                   		d instanceof javax.sound.midi.Synthesizer)
                    		continue;
                    if (d.getMaxTransmitters() != 0)
                        {
                        keydevices.add(new MidiDeviceName(d));
                        }
                    }
                catch(Exception e) { }
                }
                
            if (transdevices.size() == 0)
                {
                JOptionPane.showOptionDialog(this, "There are no MIDI devices available to receive from.",  
                							 "Cannot Connect", JOptionPane.DEFAULT_OPTION, 
                							 JOptionPane.WARNING_MESSAGE, null,
                							 new String[] { "Run Disconnected" }, "Run Disconnected");
//                JOptionPane.showMessageDialog(this, "There are no MIDI devices available to receive from.",  "Cannot Connect", JOptionPane.ERROR_MESSAGE);
                return CANCELLED;
                }
            else if (recvdevices.size() == 0)
                {
                JOptionPane.showOptionDialog(this, "There are no MIDI devices available to send to.",  
                							 "Cannot Connect", JOptionPane.DEFAULT_OPTION, 
                							 JOptionPane.WARNING_MESSAGE, null,
                							 new String[] { "Run Disconnected" }, "Run Disconnected");
                //JOptionPane.showMessageDialog(this, "There are no MIDI devices available to send to.",  "Cannot Connect", JOptionPane.ERROR_MESSAGE);
                return CANCELLED;
                }
            else
                {
                String[] kc = new String[] { "Any", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16" };
                String[] rc = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16" };
                JComboBox transcombo = new JComboBox(transdevices.toArray());
                if (oldIn != null && indexOfMidiDevice(oldIn, transdevices) >= 0)
                	transcombo.setSelectedIndex(indexOfMidiDevice(oldIn, transdevices));
                JComboBox recvcombo = new JComboBox(recvdevices.toArray());
                if (oldOut != null && indexOfMidiDevice(oldOut, recvdevices) >= 0)
                	recvcombo.setSelectedIndex(indexOfMidiDevice(oldOut, recvdevices));
                JComboBox recvchannelscombo = new JComboBox(rc);
                recvchannelscombo.setSelectedIndex(oldSendChannel - 1);
                JComboBox keycombo = new JComboBox(keydevices.toArray());
                if (oldKey != null && indexOfMidiDevice(oldKey, keydevices) >= 0)
                	keycombo.setSelectedIndex(indexOfMidiDevice(oldKey, keydevices));
                JComboBox keychannelscombo = new JComboBox(kc);
                keychannelscombo.setSelectedIndex(oldKeyChannel);
                //boolean result = doMultiOption(this, new String[] { "Send To", "Receive From" },  new JComponent[] { recvcombo, transcombo }, "MIDI Devices", message);
                boolean result = doMultiOption(this, new String[] { "Receive From", "Send To", "Send Channel", "Keyboard", "Keyboard Channel" },  new JComponent[] { transcombo, recvcombo, recvchannelscombo, keycombo, keychannelscombo }, "MIDI Devices", message);

                if (result)
                    {
                    // transmitter
                    
                    Transmitter transmitter;
                    Object choice = transcombo.getSelectedItem();
                    try { ((MidiDeviceName)choice).device.open(); }
                    catch (Exception e) { e.printStackTrace(); return FAILED; }
                    try { transmitter = ((MidiDeviceName)choice).device.getTransmitter(); }
                    catch (Exception e) 
                        { 
                        e.printStackTrace(); 
                        ((MidiDeviceName)choice).device.close(); 
                        return FAILED; 
                        }

                    if (inr != null)
                        inr.close(); 
                    if (in != null)
                        in.close();
                    inn = (MidiDeviceName) choice;
                    in = ((MidiDeviceName)choice).device;
                    inr = transmitter;

					// receiver
	
                    Receiver receiver;
                    choice = recvcombo.getSelectedItem();
                    try { ((MidiDeviceName)choice).device.open(); }
                    catch (Exception e) 
                    	{ 
                        e.printStackTrace(); 
                        in.close();
                        inr.close();
                        return FAILED; 
						}
                    try { receiver = ((MidiDeviceName)choice).device.getReceiver(); }
                    catch (Exception e) 
                        { 
                        e.printStackTrace(); 
                        ((MidiDeviceName)choice).device.close();
                        in.close();
                        inr.close();
                        return FAILED; 
						}

                    if (outr != null)
                        outr.close();
                    if (out != null)
                        out.close();
                    outn = (MidiDeviceName) choice;
                    out = ((MidiDeviceName)choice).device;
                    outr = receiver;
                    
                    // keyboard
                    
                    Transmitter keybd;
                    choice = keycombo.getSelectedItem();
                    if (!(choice instanceof String)) // it's "None"
                    	{
						try { ((MidiDeviceName)choice).device.open(); }
						catch (Exception e) 
							{ 
							e.printStackTrace(); 
							JOptionPane.showOptionDialog(this, "Could not connect to the requested keyboard.",  
							 "Cannot Connect", JOptionPane.DEFAULT_OPTION, 
							 JOptionPane.WARNING_MESSAGE, null,
							 new String[] { "Run without Keyboard" }, "Run without Keyboard");
							return SUCCEEDED;
							}
						try { transmitter = ((MidiDeviceName)choice).device.getTransmitter(); }
						catch (Exception e) 
							{ 
							e.printStackTrace(); 
							((MidiDeviceName)choice).device.close(); 
							JOptionPane.showOptionDialog(this, "Could not connect to the requested keyboard.",  
							 "Cannot Connect", JOptionPane.DEFAULT_OPTION, 
							 JOptionPane.WARNING_MESSAGE, null,
							 new String[] { "Run without Keyboard" }, "Run without Keyboard");
							return SUCCEEDED; 
							}

						if (keyr != null)
							keyr.close(); 
						if (key != null)
							key.close();
						keyn = (MidiDeviceName) choice;
						key = ((MidiDeviceName)choice).device;
						keyr = transmitter;
						}
					else
						{
						if (keyr != null)
							keyr.close(); 
						if (key != null)
							key.close();
						keyn = null;
						}
						
					keyChannel = keychannelscombo.getSelectedIndex();
					sendChannel = recvchannelscombo.getSelectedIndex() + 1;
                    return SUCCEEDED;
                    }
                else
                    {
                    return CANCELLED;
                    }
                }
            }
		}




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
                
    }
