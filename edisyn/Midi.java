/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn;

import edisyn.gui.*;
import edisyn.synth.*;
import javax.sound.midi.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;

/**** 
      Static class which contains methods for handling the global MIDI device facility.
      Edisyn uses a single MIDI device repository created (presently) at launch time
      to which synth panels may retrieve transmitters and receivers. This is done because
      the original approach (letting each synth panel build its own devices) triggers
      low-level bugs in the OS X Java CoreMIDI4J implementation which hangs the system.
      The disadvnatage of the current approach is that (presnetliy) you must have your
      devices' USB connections plugged in BEFORE you launch Edisyn.  Otherwise it's not too bad.

      @author Sean Luke
*/



public class Midi
	{
	
	/** A MIDI pipe.  Thru is a Receiver which attaches to other
		Receivers.  when it gets a message, it forwards it to ALL
		the other receivers.  Additionally, sending is synchronized,
		so you can guaranted that if multiple transmitters send to
		the Thru, they won't have a race condition. */
		
	public static class Thru implements Receiver
		{
		ArrayList receivers = new ArrayList();
		
		public void close()
			{
			for(int i = 0; i < receivers.size(); i++)
				{
				((Receiver)(receivers.get(i))).close();
				}
			receivers = new ArrayList();
			}
			
		public synchronized void send(MidiMessage message, long timeStamp)
			{
			for(int i = 0; i < receivers.size(); i++)
				{
				((Receiver)(receivers.get(i))).send(message, timeStamp);
				}
			}
			
		/** Add a receiver to get routed to. */
		public void addReceiver(Receiver receiver)
			{
			receivers.add(receiver);
			}
			
		/** Remove a receiver that was routed to. */
		public void removeReceiver(Receiver receiver)
			{
			receivers.remove(receiver);
			}
		}


	/** A wrapper for a MIDI device which displays its name in a pleasing and
		useful format for the user.  Additionally the wrapper also can provide
		a threadsafe receiver for the device (opening the device and building
		it as needed) and also a Thru for the device's transmitter. */
		
	static class MidiDeviceWrapper
		{
		public MidiDevice device;
		
		public MidiDeviceWrapper(MidiDevice device)
			{
			this.device = device;
			}
			
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
		
		Transmitter transmitter;
		Receiver receiver;
		Thru thru;
		

		/** Returns a Thru representing the Transmitter of this device.  You provide
			a Receiver to attach to the Thru.  The Thru is then attached to the Transmitter.
			This design allows multiple receivers to attach to the same Thru and thus to the
			same Transmitter so we don't have to build multiple Transmitters (triggering bugs).
			*/
		public Thru getThru(Receiver receiver) 
			{ 
			if (thru == null) 
				try
					{
					// we use a thru here so we can add many receivers to it
					if (!device.isOpen()) 
						device.open();
					transmitter = device.getTransmitter();
					thru = new Thru();
					transmitter.setReceiver(thru);
					}
				catch(Exception e) { e.printStackTrace(); }
			
			if (thru != null)
				{
				thru.addReceiver(receiver);
				}
			return thru;
			}
			
		/** Returns a threadsafe Receiver. s*/
		public Receiver getReceiver() 
			{ 
			if (receiver == null) 
				try
					{
					// we use a secret Thru here so it's lockable
					if (!device.isOpen()) 
						device.open();
					Thru recv = new Thru();
					recv.addReceiver(device.getReceiver());
					receiver = recv;
					}
				catch(Exception e) { e.printStackTrace(); }
			return receiver; 
			}
		}


	static ArrayList allDevices;
	static ArrayList inDevices;
	static ArrayList outDevices;
	static ArrayList keyDevices;
	
	static
		{
        MidiDevice.Info[] midiDevices = uk.co.xfactorylibrarians.coremidi4j.CoreMidiDeviceProvider.getMidiDeviceInfo();

		allDevices = new ArrayList();
		for(int i = 0; i < midiDevices.length; i++)
			{
			try
				{
				MidiDevice d = MidiSystem.getMidiDevice(midiDevices[i]);
				// get rid of java devices
				if (d instanceof javax.sound.midi.Sequencer ||
					d instanceof javax.sound.midi.Synthesizer)
						continue;
				if (d.getMaxTransmitters() != 0 || d.getMaxReceivers() != 0)
					{
					allDevices.add(new MidiDeviceWrapper(d));
					}
				}
			catch(Exception e) { }
			}

    	inDevices = new ArrayList();
    	keyDevices = new ArrayList();
		keyDevices.add("None");
		for(int i = 0; i < allDevices.size(); i++)
			{
			try
				{
				MidiDeviceWrapper mdn = (MidiDeviceWrapper)(allDevices.get(i));
				if (mdn.device.getMaxTransmitters() != 0)
					{
					inDevices.add(mdn);
					keyDevices.add(mdn);
					}
				}
			catch(Exception e) { }
			}

    	outDevices = new ArrayList();
		for(int i = 0; i < allDevices.size(); i++)
			{
			try
				{
				MidiDeviceWrapper mdn = (MidiDeviceWrapper)(allDevices.get(i));
				if (mdn.device.getMaxReceivers() != 0)
					{
					outDevices.add(mdn);
					}
				}
			catch(Exception e) { }
			}
		}



	public static class Tuple
		{
		/** Represents "any channel" in the Tuple. */
		public static final int KEYCHANNEL_OMNI = 0;

		/** The current output */
		public Receiver out;
		/** The current output device wrapper */
		public MidiDeviceWrapper outWrap;
		/** The channel to send voiced messages to on the output. */
		public int outChannel = 1;
		
		/** The current input */
		public Thru in;
		/** The current input device's wrapper */
		public MidiDeviceWrapper inWrap;
		/** The current receiver which is attached to the input to perform its
			commands.  Typically generated with Synth.buildInReceiver() */
		public Receiver inReceiver;
		
		/** The current keyboard/controller input */
		public Thru key;
		/** The current keyboard/controller input device's wrapper */
		public MidiDeviceWrapper keyName;
		/** The current receiver which is attached to the keyboard/controller input
			to perform its commands.  Typically generated with Synth.buildKeyReceiver() */
		public Receiver keyReceiver;
		/** The channel to receive voiced messages from on the keyboard/controller input. */
		public int keyChannel = KEYCHANNEL_OMNI;
		
		void dispose()
			{
			if (key != null && keyReceiver != null)
				key.removeReceiver(keyReceiver);
			if (in != null && inReceiver!= null)
				in.removeReceiver(inReceiver);
			}	
		}

	public static final Tuple CANCELLED = new Tuple();
	public static final Tuple FAILED = new Tuple();
	
	/** Works with the user to generate a new Tuple holding new MIDI connections.
		You may provide the old tuple for defaults or pass in null.  You also
		provide the inReceiver and keyReceiver to be attached to the input and keyboard/controller
		input.  You get these with Synth.buildKeyReceiver() and Synth.buildInReceiver() */ 
	public static Tuple getNewTuple(Tuple old, JComponent root, String message, Receiver inReceiver, Receiver keyReceiver)
		{
		if (inDevices.size() == 0)
			{
			JOptionPane.showOptionDialog(root, "There are no MIDI devices available to receive from.",  
										 "Cannot Connect", JOptionPane.DEFAULT_OPTION, 
										 JOptionPane.WARNING_MESSAGE, null,
										 new String[] { "Run Disconnected" }, "Run Disconnected");
			return CANCELLED;
			}
		else if (outDevices.size() == 0)
			{
			JOptionPane.showOptionDialog(root, "There are no MIDI devices available to send to.",  
										 "Cannot Connect", JOptionPane.DEFAULT_OPTION, 
										 JOptionPane.WARNING_MESSAGE, null,
										 new String[] { "Run Disconnected" }, "Run Disconnected");
			return CANCELLED;
			}
		else
			{
			String[] kc = new String[] { "Any", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16" };
			String[] rc = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16" };

			JComboBox inCombo = new JComboBox(inDevices.toArray());
			if (old != null && old.inWrap != null && inDevices.indexOf(old.inWrap) != -1)
				inCombo.setSelectedIndex(inDevices.indexOf(old.inWrap));

			JComboBox outCombo = new JComboBox(outDevices.toArray());
			if (old != null && old.outWrap != null && outDevices.indexOf(old.outWrap) != -1)
				outCombo.setSelectedIndex(outDevices.indexOf(old.outWrap));

			JComboBox keyCombo = new JComboBox(keyDevices.toArray());
			keyCombo.setSelectedIndex(0);  // "none"
			if (old != null && old.keyName != null && keyDevices.indexOf(old.keyName) != -1)
				keyCombo.setSelectedIndex(keyDevices.indexOf(old.keyName));

			JComboBox outChannelsCombo = new JComboBox(rc);
			if (old != null)
				outChannelsCombo.setSelectedIndex(old.outChannel - 1);
				
			JComboBox keyChannelsCombo = new JComboBox(kc);
			if (old != null)
				keyChannelsCombo.setSelectedIndex(old.keyChannel);

			boolean result = Synth.doMultiOption(root, new String[] { "Receive From", "Send To", "Send Channel", "Keyboard", "Keyboard Channel" },  new JComponent[] { inCombo, outCombo, outChannelsCombo, keyCombo, keyChannelsCombo }, "MIDI Devices", message);

			if (result)
				{
				// we need to build a tuple
				
				Tuple tuple = new Tuple();
				
				tuple.keyChannel = keyChannelsCombo.getSelectedIndex();
				tuple.outChannel = outChannelsCombo.getSelectedIndex() + 1;
				
				tuple.inWrap = ((MidiDeviceWrapper)(inCombo.getSelectedItem()));
				tuple.in = tuple.inWrap.getThru(inReceiver);
				if (tuple.in == null)
					{
					JOptionPane.showOptionDialog(root, "An error occurred while connecting to the incoming MIDI Device.",  
												 "Cannot Connect", JOptionPane.DEFAULT_OPTION, 
												 JOptionPane.WARNING_MESSAGE, null,
												 new String[] { "Run Disconnected" }, "Run Disconnected");
					return FAILED;
					}

				tuple.outWrap = ((MidiDeviceWrapper)(outCombo.getSelectedItem()));
				tuple.out = tuple.outWrap.getReceiver();
				if (tuple.out == null)
					{
					JOptionPane.showOptionDialog(root, "An error occurred while connecting to the outgoing MIDI Device.",  
												 "Cannot Connect", JOptionPane.DEFAULT_OPTION, 
												 JOptionPane.WARNING_MESSAGE, null,
												 new String[] { "Run Disconnected" }, "Run Disconnected");
					return FAILED;
					}

				if (keyCombo.getSelectedItem() instanceof String)
					{
					tuple.keyName = null;
					tuple.key = null;
					}
				else
					{
					tuple.keyName = ((MidiDeviceWrapper)(keyCombo.getSelectedItem()));
					tuple.key = tuple.keyName.getThru(keyReceiver);
					if (tuple.key == null)
						{
						JOptionPane.showOptionDialog(root, "An error occurred while connecting to the Key Controller MIDI Device.",  
													 "Cannot Connect", JOptionPane.DEFAULT_OPTION, 
													 JOptionPane.WARNING_MESSAGE, null,
													 new String[] { "Run without Keyboard Controller" }, "Run without Keyboard Controller");
						tuple.keyName = null;
						tuple.key = null;
						}
					}
				return tuple;
				}
			else
				{
				return CANCELLED;
				}
			}	
		}
	}