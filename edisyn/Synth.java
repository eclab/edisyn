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

        
    /** Handles mutation (or not) of keys declared immutable in the Model.
        When the user is mutating (randomizing) a parameter, and it was declared
        immutable, this method is called instead to let you change the parameter
        as you like (or refuse to). */
    public abstract void immutableMutate(String key);
        
    /** Updates the model to reflect the following sysex patch dump for your synthesizer type. 
    	If the patch is from current working memory and so doesn't need to be sent to the synth
    	to update it, return FALSE, else return TRUE. */
    public abstract boolean parse(byte[] data);
    
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
    
    /** Return true if the window can be closed and disposed of. You should do some cleanup
        as necessary (the system will handle cleaning up the receiver and transmitters. */
    public abstract boolean requestCloseWindow();
    
        
    /** Updates the JFrame title to reflect the synthesizer type, the patch information, and the filename
        if any.  See Blofeld.updateTitle() for inspiration. */
    public void updateTitle()
        {
        JFrame frame = ((JFrame)(SwingUtilities.getRoot(this)));
        if (frame != null) 
            frame.setTitle(getSynthName().trim() + "        " + 
                getPatchName().trim() + 
                (getFile() == null ? "" : "        " + getFile().getName()) +
                ((tuple == null || tuple.in == null) ? "        (DISCONNECTED)" : ""));
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
            {
            tryToSendSysex(emit(key));
            }
        }


    /** Builds a receiver to attach to the current IN transmitter.  The receiver
        can handle merging and patch reception. */
    Receiver buildInReceiver()
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
                                Synth newSynth = instantiate(Synth.this.getClass(), getSynthName(), true, false, tuple);
                                newSynth.parse(data);
                                setSendMIDI(false);
                                merge(newSynth.getModel(), 0.5);
                                setSendMIDI(true);
                                sendAllParameters();
                                updateTitle();
                                }
                            else
                                {
                                sendMIDI = false;  // so we don't send out parameter updates in response to reading/changing parameters
                                boolean sendParameters = parse(data);
                                sendMIDI = true;
                                if (sendParameters == false) 
                                	sendAllParameters();
                                file = null;
                                updateTitle();
                                }
                            }
                        });
                    }
                else					// Maybe it's a local Parameter change in sysex or CC?
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

                            tryToSendMIDI(newMessage);
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
            tuple = null;
            }
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
        if (data == null || data.length == 0) 
            return false;

        if (getSendMIDI())
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
           
           
    static Synth instantiate(Class _class, String name, boolean throwaway, boolean setupMIDI, Midi.Tuple tuple)
    	{
		try
			{
			Synth synth = (Synth)(_class.newInstance());
			if (!throwaway)
				{
				synth.sprout();
				JFrame frame = ((JFrame)(SwingUtilities.getRoot(synth)));
				frame.setVisible(true);
				if (setupMIDI) 
					synth.setupMIDI("Choose MIDI devices to send to and receive from.", tuple);
				}
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


	public static final Class[] synths = new Class[] { Blofeld.class, MicrowaveXT.class };
	public static final String[] synthNames = { "Waldorf Blofeld (Single)", "Waldorf Microwave II/XT/XTk (Single)" };
          
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

        JMenu newSynth = new JMenu("New Synth...");
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

        menu.addSeparator();

        JMenuItem resetToDefault = new JMenuItem("Reset to Default");
        menu.add(resetToDefault);
        resetToDefault.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                getModel().resetToDefaults();
                }
            });

        JMenuItem exportTo = new JMenuItem("Export Diff To Text...");
        menu.add(exportTo);
        exportTo.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doExport(true);
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
                    }
                
                tryToSendSysex(requestCurrentDump(null));
                }
            });

        JMenuItem receive = new JMenuItem("Request Patch...");
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
                if (tuple == null || tuple.out == null)
                    {
                    if (!setupMIDI("You are disconnected. Choose MIDI devices to send to and receive from."))
                        return;
                    }
                
                sendAllParameters();
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


        menu.addSeparator();

        JMenuItem burn = new JMenuItem("Write Patch...");
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
                    }
                
                if (gatherInfo("Write Patch", getModel()))
                    {
                    tryToSendSysex(emit(getModel(), false));
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
        testNote.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(testNote);
        testNote.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                try
                    {
                    int channel = 1;
                    if (tuple != null)
                        channel = tuple.outChannel;
                    tryToSendMIDI(new ShortMessage(ShortMessage.NOTE_ON, channel, 60, 127));
                    Thread.currentThread().sleep(500);
                    tryToSendMIDI(new ShortMessage(ShortMessage.NOTE_OFF, channel, 60, 127));
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
                
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); 
        frame.addWindowListener(new java.awt.event.WindowAdapter() 
            {
            public void windowClosing(java.awt.event.WindowEvent windowEvent) 
                {
                doCloseWindow();
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

    
    boolean sendsAllParametersInBulk = false;
    public void setSendsAllParametersInBulk(boolean val) { sendsAllParametersInBulk = val; }
    public boolean getSendsAllParametersInBulk() { return sendsAllParametersInBulk; }

    /** Sends all the parameters in a patch to the synth.  
    	If sendsAllParametersInBulk was set to TRUE, then this is done by sending
    	a single patch write to working memory, which may not be supported by all synths.
    	
    	Otherwise this is done by sending each parameter separately, which isn't as fast.
    	The default sends each parameter separately.
	*/    
    public void sendAllParameters()
        {
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
                os.write(emit((Model)null, false));
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
                os.write(emit((Model)null, false));
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


    /** Goes through the process of dumping to a text file. */
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
                }
            finally
                {
                if (os != null)
                    try { os.close(); }
                    catch (IOException e) { }
                }

        updateTitle();
        }

    public void doCloseWindow()
        {
        if (requestCloseWindow())
            {
            if (tuple != null)
                tuple.dispose();
            JFrame frame = (JFrame)(SwingUtilities.getRoot(this));
            if (frame != null)
                {
                frame.setVisible(false);
                frame.dispose();
                }
                                
            numOpenWindows--;
            if (numOpenWindows <= 0)
                {
                System.exit(0);
                }
            }
                
        }
                
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

                setSendMIDI(false);
                parse(data);
                setSendMIDI(true);
                                
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
		p2.add(combo, BorderLayout.CENTER);
        
        int result = JOptionPane.showOptionDialog(null, p2, "Edisyn", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[] { "Run", "Cancel", "Disconnected" }, "Run");
        if (result == 1) return null;
        else return instantiate(synths[combo.getSelectedIndex()], synthNames[combo.getSelectedIndex()], false, (result == 0), null);
        }
                
    }
