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
import java.lang.reflect.*;
import edisyn.util.*;

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
        Receivers.  When it gets a message, it forwards it to ALL
        the other receivers.  Additionally, sending is synchronized,
        so you can guaranted that if multiple transmitters send to
        the Thru, they won't have a race condition. */
                
    public static class Thru implements Receiver
        {
        ArrayList receivers = new ArrayList();
                
        public void close()
            {
            removeAllReceivers();
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
                        
        /** Sets the only receiver to get routed to. */
        public void setReceiver(Receiver receiver)
            {
            removeAllReceivers();
            receivers.add(receiver);
            }
                        
        /** Remove a receiver that was routed to. */
        public void removeReceiver(Receiver receiver)
            {
            receivers.remove(receiver);
            }

        /** Remove all receivers. */
        public void removeAllReceivers()
            {
            for(int i = 0; i < receivers.size(); i++)
                {
                ((Receiver)(receivers.get(i))).close();
                }
            receivers = new ArrayList();
            }
        }


    /** A wrapper for a MIDI device which displays its name in a pleasing and
        useful format for the user.  Additionally the wrapper also can provide
        a threadsafe receiver for the device (opening the device and building
        it as needed) and also a Thru for the device's transmitter. */
                
    public static class MidiDeviceWrapper
        {
        public MidiDevice device;
                
        public MidiDeviceWrapper(MidiDevice device)
            {
            this.device = device;
            }
                                    
        public String toString() 
            { 
            String desc = device.getDeviceInfo().getDescription().trim();
            String name = device.getDeviceInfo().getName();
            if (name == null) 
                name = "";
            if (desc == null || desc.equals("")) 
                desc = "MIDI Device";
            
            // All CoreMIDI4J names begin with "CoreMIDI4J - "
            if (name.startsWith("CoreMIDI4J - "))
                name = name.substring(13).trim();
            else
                name = name.trim();

            if (name.equals(""))
                return desc.trim(); 
            else 
                return name;
            }
                
        Thru in;
        Thru out;
        Transmitter transmitter;                

        public MidiDevice getDevice() { return device; }

        /** 
            You provide a Receiver to attach to the Transmitter.  Returns true if successful
        */
        public boolean addToTransmitter(Receiver receiver) 
            { 
            if (in == null) 
                {
                try
                    {
                    // we use a thru here so we can add many receivers to it
                    if (!device.isOpen()) 
                        device.open();
                    Thru _in = new Thru();
                    Transmitter _transmitter = device.getTransmitter();
                    _transmitter.setReceiver(_in);
                    transmitter = _transmitter;         // we set it last in case of an exception
                    in = _in;                                           // we set it last in case of an exception
                    }
                catch(Exception e) { Synth.handleException(e); return false; }
                }
            
            //System.err.println("Adding " + receiver + " to " + in);
            in.addReceiver(receiver);
            return true;
            }

         public boolean removeFromTransmitter(Receiver receiver) 
            {
            if (in == null) 
                {
                try
                    {
                    // we use a thru here so we can add many receivers to it
                    if (!device.isOpen()) 
                        device.open();
                    Thru _in = new Thru();
                    Transmitter _transmitter = device.getTransmitter();
                    _transmitter.setReceiver(_in);
                    transmitter = _transmitter;         // we set it last in case of an exception
                    in = _in;                                           // we set it last in case of an exception
                    }
                catch(Exception e) { Synth.handleException(e); return false; }
                }
            
            //System.err.println("Removing " + receiver + " from " + in);
            in.removeReceiver(receiver);
            return true;
            }

       public boolean removeAllFromTransmitter() 
            {
            if (in == null) 
                {
                try
                    {
                    // we use a thru here so we can add many receivers to it
                    if (!device.isOpen()) 
                        device.open();
                    Thru _in = new Thru();
                    Transmitter _transmitter = device.getTransmitter();
                    _transmitter.setReceiver(_in);
                    transmitter = _transmitter;         // we set it last in case of an exception
                    in = _in;                                           // we set it last in case of an exception
                    }
                catch(Exception e) { Synth.handleException(e); return false; }
                }
            
            in.removeAllReceivers();
            return true;
            }

       /** 
            You provide a Receiver to solely attach to the Transmitter.  Returns true if successful
        */
        public boolean connectToTransmitter(Receiver receiver) 
            {
            if (in == null) 
                {
                try
                    {
                    // we use a thru here so we can add many receivers to it
                    if (!device.isOpen()) 
                        device.open();
                    Thru _in = new Thru();
                    Transmitter _transmitter = device.getTransmitter();
                    _transmitter.setReceiver(_in);
                    transmitter = _transmitter;         // we set it last in case of an exception
                    in = _in;                                           // we set it last in case of an exception
                    }
                catch(Exception e) { Synth.handleException(e); return false; }
                }
            
            in.removeAllReceivers();
            in.addReceiver(receiver);
            return true;
            }
                        
        /** Returns a threadsafe Receiver, or null if not successful. */
        public Receiver getReceiver() 
            { 
            if (out == null) 
                {
                try
                    {
                    // we use a secret Thru here so it's lockable
                    if (!device.isOpen()) 
                        device.open();
                    Thru _out = new Thru();
                    _out.addReceiver(device.getReceiver());
                    out = _out;         // we set it last in case of an exception
                    }
                catch(Exception e) { Synth.handleException(e); return null; }
                }

            return out; 
            }
        
        public void close()
            {
            //new Throwable().printStackTrace();
            if (transmitter != null) transmitter.close();
            if (out != null) out.close();
            // don't close in(), it'll just close all my own receivers
            }
        }


    static Object findDevice(String name, ArrayList devices)
        {
        if (name == null) return null;
        for(int i = 0; i < devices.size(); i++)
            {
            if (devices.get(i) instanceof String)
                {
                if (((String)devices.get(i)).equals(name))
                    return devices.get(i);
                }
            else
                {
                MidiDeviceWrapper mdn = (MidiDeviceWrapper)(devices.get(i));
                if (mdn.toString().equals(name))
                    return mdn;
                }
            }
        return null;
        }


    static void updateDevices()
        {
        MidiDevice.Info[] midiDevices;
        
        if (Style.isMac())
            //if (true)
            {
            try
                {
                Class c = Class.forName("uk.co.xfactorylibrarians.coremidi4j.CoreMidiDeviceProvider");
                Method m = c.getMethod("getMidiDeviceInfo", new Class[0]);
                midiDevices = (MidiDevice.Info[])(m.invoke(null));
                //                midiDevices = uk.co.xfactorylibrarians.coremidi4j.CoreMidiDeviceProvider.getMidiDeviceInfo();
                }
            catch (Throwable ex)
                {
                System.err.println("WARNING (Midi.java): error on obtaining CoreMIDI4J, but we think we're a Mac.  This should never happen.");
                Synth.handleException(ex);
                midiDevices = MidiSystem.getMidiDeviceInfo();
                }
            }
        else
            {
            midiDevices = MidiSystem.getMidiDeviceInfo();
            }

        ArrayList allDevices = new ArrayList();
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
            catch(Exception e) { ExceptionDump.postThrowable(e); }
            }
            
        // Do they hold the same exact devices?
        if (Midi.allDevices != null && Midi.allDevices.size() == allDevices.size())
            {
            Set set = new HashSet();
            for(int i = 0; i < Midi.allDevices.size(); i++)
                {
                set.add(((MidiDeviceWrapper)(Midi.allDevices.get(i))).device);
                }
                
            boolean same = true;
            for(int i = 0; i < allDevices.size(); i++)
                {
                if (!set.contains(((MidiDeviceWrapper)(allDevices.get(i))).device))
                    {
                    same = false;  // something's different
                    break;
                    }
                }
                
            if (same)
                {
                return;  // they're identical
                }
            }

        Midi.allDevices = allDevices;

        inDevices = new ArrayList();
        keyDevices = new ArrayList();
        keyDevices.add("None");
        key2Devices = new ArrayList();
        key2Devices.add("None");
        for(int i = 0; i < allDevices.size(); i++)
            {
            try
                {
                MidiDeviceWrapper mdn = (MidiDeviceWrapper)(allDevices.get(i));
                if (mdn.device.getMaxTransmitters() != 0)
                    {
                    inDevices.add(mdn);
                    keyDevices.add(mdn);
                    key2Devices.add(mdn);
                    }
                }
            catch(Exception e) { ExceptionDump.postThrowable(e); }
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
            catch(Exception e) { ExceptionDump.postThrowable(e); }
            }
        }

    static ArrayList allDevices;
    static ArrayList inDevices;
    static ArrayList outDevices;
    static ArrayList keyDevices;
    static ArrayList key2Devices;
        
    static
        {
        updateDevices();
        }

    public static class Tuple
        {
        /** Represents "any channel" in the Tuple. */
        public static final int KEYCHANNEL_OMNI = 0;

        /** The current output device wrapper */
        public MidiDeviceWrapper outWrap;
        /** The current output device receiver */
        public Receiver outReceiver;
        /** The channel to send voiced messages to on the output. */
        public int outChannel = 1;
        public String id = "0";
                
        /** The current input device's wrapper */
        public MidiDeviceWrapper inWrap;
                
        /** The current keyboard/controller input device's wrapper */
        public MidiDeviceWrapper keyWrap;
        /** The channel to receive voiced messages from on the keyboard/controller input. */
        public int keyChannel = KEYCHANNEL_OMNI;
        
        /** The secondary keyboard/controller input device's wrapper */
        public MidiDeviceWrapper key2Wrap;
        /** The secondary to receive voiced messages from on the keyboard/controller input. */
        public int key2Channel = KEYCHANNEL_OMNI;
        
        public Receiver inReceiver;
        public Receiver keyReceiver;
        public Receiver key2Receiver;
        
        public Tuple() { }
        
        public Tuple(Tuple other, Receiver inReceiver, Receiver keyReceiver, Receiver key2Receiver)
            {
            outWrap = other.outWrap;
            inWrap = other.inWrap;
            keyWrap = other.keyWrap;
            key2Wrap = other.key2Wrap;
            outReceiver = other.outReceiver;
            outChannel = other.outChannel;
            id = other.id;
            keyChannel = other.keyChannel;
            key2Channel = other.key2Channel;
            this.inReceiver = inReceiver;
            this.keyReceiver = keyReceiver;
            this.key2Receiver = key2Receiver;
                
            inWrap.connectToTransmitter(inReceiver);
            if (keyWrap != null) keyWrap.connectToTransmitter(keyReceiver);
            if (key2Wrap != null) key2Wrap.connectToTransmitter(key2Receiver);
            }
        
        public void dispose()
            {
            // dunno if we should even do this, the memory leak is well worth the nasty
            // bugs on disposal...

            //if (outWrap != null) outWrap.close();
            //if (inWrap != null) inWrap.close();
            //if (keyWrap != null) keyWrap.close();
            //if (key2Wrap != null) key2Wrap.close();
            }
        }

    static void setLastTupleIn(String path, Synth synth) { synth.setLastX(path, "LastTupleIn", synth.getSynthClassName(), false); }
    static String getLastTupleIn(Synth synth) { return Synth.getLastX("LastTupleIn", synth.getSynthClassName(), false); }
    
    static void setLastTupleOut(String path, Synth synth) { synth.setLastX(path, "LastTupleOut", synth.getSynthClassName(), false); }
    static String getLastTupleOut(Synth synth) { return Synth.getLastX("LastTupleOut", synth.getSynthClassName(), false); }
    
    static void setLastTupleKey(String path, Synth synth) { synth.setLastX(path, "LastTupleKey", synth.getSynthClassName(), false); }
    static String getLastTupleKey(Synth synth) { return Synth.getLastX("LastTupleKey", synth.getSynthClassName(), false); }
    
    static void setLastTupleKey2(String path, Synth synth) { synth.setLastX(path, "LastTupleKey2", synth.getSynthClassName(), false); }
    static String getLastTupleKey2(Synth synth) { return Synth.getLastX("LastTupleKey2", synth.getSynthClassName(), false); }
    
    static void setLastTupleOutChannel(int channel, Synth synth) { synth.setLastX("" + channel, "LastTupleOutChannel", synth.getSynthClassName(), false); }
    static int getLastTupleOutChannel(Synth synth) 
        { 
        return Synth.getLastXAsInt("LastTupleOutChannel", synth.getSynthClassName(), -1, false);
        }
    
    static void setLastTupleKeyChannel(int channel, Synth synth) { synth.setLastX("" + channel, "LastTupleKeyChannel", synth.getSynthClassName(), false); }
    static int getLastTupleKeyChannel(Synth synth) 
        {
        return Synth.getLastXAsInt("LastTupleKeyChannel", synth.getSynthClassName(), -1, false);
        }
    
    static void setLastTupleKey2Channel(int channel, Synth synth) { synth.setLastX("" + channel, "LastTupleKey2Channel", synth.getSynthClassName(), false); }
    static int getLastTupleKey2Channel(Synth synth) 
        {
        return Synth.getLastXAsInt("LastTupleKey2Channel", synth.getSynthClassName(), -1, false);
        }

    static void setLastTupleID(String id, Synth synth) { synth.setLastX(id, "LastTupleID", synth.getSynthClassName(), false); }
    static String getLastTupleID(Synth synth) { return Synth.getLastX("LastTupleID", synth.getSynthClassName(), false); }
    

    public static final Tuple CANCELLED = new Tuple();
    public static final Tuple FAILED = new Tuple();

    static final String[] kc = new String[] { "Any", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16" };
    static final String[] k2c = new String[] { "Any", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16" };
    static final String[] rc = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16" };


    /** Works with the user to generate a new Tuple holding new MIDI connections.
        You may provide the old tuple for defaults or pass in null.  You also
        provide the inReceiver and keyReceiver and key2Receiver to be attached to the input and keyboard/controller
        input.  You get these with Synth.buildKeyReceiver() and Synth.buildInReceiver() 
        If the old Tuple is the previous tuple of
        this synthesizer, then you will want to set removeReceiversFromOldTuple to TRUE so that when
        new receivers are attached, the old ones are eliminated.  However if old Tuple is from another
        active synthesizer editor, and so is just being used to provide defaults, then you should set
        removeReceiversFromOldTuple to FALSE so it doesn't muck with the previous synthesizer.
    */ 
    public static Tuple getNewTuple(Tuple old, Synth synth, String message, Receiver inReceiver, Receiver keyReceiver, Receiver key2Receiver, boolean removeReceiversFromOldTuple)
        {
        updateDevices();
        
        if (inDevices.size() == 0)
            {
            synth.disableMenuBar();
            JOptionPane.showOptionDialog(synth, "There are no MIDI devices available to receive from.",  
                "Cannot Connect", JOptionPane.DEFAULT_OPTION, 
                JOptionPane.WARNING_MESSAGE, null,
                new String[] { "Run Disconnected" }, "Run Disconnected");
            synth.enableMenuBar();
            return CANCELLED;
            }
        else if (outDevices.size() == 0)
            {
            synth.disableMenuBar();
            JOptionPane.showOptionDialog(synth, "There are no MIDI devices available to send to.",  
                "Cannot Connect", JOptionPane.DEFAULT_OPTION, 
                JOptionPane.WARNING_MESSAGE, null,
                new String[] { "Run Disconnected" }, "Run Disconnected");
            synth.enableMenuBar();
            return CANCELLED;
            }
        else
            {
            JComboBox inCombo = new JComboBox(inDevices.toArray());
            inCombo.setMaximumRowCount(32);
            if (old != null && old.inWrap != null && inDevices.indexOf(old.inWrap) != -1)
                inCombo.setSelectedIndex(inDevices.indexOf(old.inWrap));
            else if (findDevice(getLastTupleIn(synth), inDevices) != null)
                inCombo.setSelectedItem(findDevice(getLastTupleIn(synth), inDevices));

            JComboBox outCombo = new JComboBox(outDevices.toArray());
            outCombo.setMaximumRowCount(32);
            if (old != null && old.outWrap != null && outDevices.indexOf(old.outWrap) != -1)
                outCombo.setSelectedIndex(outDevices.indexOf(old.outWrap));
            else if (findDevice(getLastTupleOut(synth), outDevices) != null)
                outCombo.setSelectedItem(findDevice(getLastTupleOut(synth), outDevices));

            JComboBox keyCombo = new JComboBox(keyDevices.toArray());
            keyCombo.setMaximumRowCount(32);
            keyCombo.setSelectedIndex(0);  // "none"
            if (old != null && old.keyWrap != null && keyDevices.indexOf(old.keyWrap) != -1)
                keyCombo.setSelectedIndex(keyDevices.indexOf(old.keyWrap));
            else if (findDevice(getLastTupleKey(synth), keyDevices) != null)
                keyCombo.setSelectedItem(findDevice(getLastTupleKey(synth), keyDevices));

            JComboBox key2Combo = new JComboBox(key2Devices.toArray());
            key2Combo.setMaximumRowCount(32);
            key2Combo.setSelectedIndex(0);  // "none"
            if (old != null && old.key2Wrap != null && key2Devices.indexOf(old.key2Wrap) != -1)
                key2Combo.setSelectedIndex(key2Devices.indexOf(old.key2Wrap));
            else if (findDevice(getLastTupleKey2(synth), key2Devices) != null)
                key2Combo.setSelectedItem(findDevice(getLastTupleKey2(synth), key2Devices));

            JTextField outID = null;
            String initialID = synth.reviseID(getLastTupleID(synth));
            if (initialID != null)
                outID = new JTextField(initialID);      // synth.reviseID(null));

            JComboBox outChannelsCombo = new JComboBox(rc);
            outChannelsCombo.setMaximumRowCount(17);
            if (old != null)
                outChannelsCombo.setSelectedIndex(old.outChannel - 1);
            else if (getLastTupleOutChannel(synth) > 0)
                outChannelsCombo.setSelectedIndex(getLastTupleOutChannel(synth) - 1);
            else 
                outChannelsCombo.setSelectedIndex(0);
                                
            JComboBox keyChannelsCombo = new JComboBox(kc);
            keyChannelsCombo.setMaximumRowCount(17);
            if (old != null)
                keyChannelsCombo.setSelectedIndex(old.keyChannel);
            else if (getLastTupleKeyChannel(synth) > 0)
                keyChannelsCombo.setSelectedIndex(getLastTupleKeyChannel(synth));
            else 
                keyChannelsCombo.setSelectedIndex(0);

            JComboBox key2ChannelsCombo = new JComboBox(kc);
            key2ChannelsCombo.setMaximumRowCount(17);
            if (old != null)
                key2ChannelsCombo.setSelectedIndex(old.key2Channel);
            else if (getLastTupleKey2Channel(synth) > 0)
                key2ChannelsCombo.setSelectedIndex(getLastTupleKey2Channel(synth));
            else 
                key2ChannelsCombo.setSelectedIndex(0);
                        
            boolean result = false;
            synth.disableMenuBar();
            if (initialID != null)
                result = Synth.showMultiOption(synth, new String[] { "Receive From", "Send To", "Send/Receive Channel", "Synth ID", "Controller", "Controller Channel", "Controller 2", "Controller 2 Channel" },  new JComponent[] { inCombo, outCombo, outChannelsCombo, outID, keyCombo, keyChannelsCombo, key2Combo, key2ChannelsCombo }, "MIDI Devices", message);
            else
                result = Synth.showMultiOption(synth, new String[] { "Receive From", "Send To", "Send/Receive Channel", "Controller", "Controller Channel", "Controller 2", "Controller 2 Channel" },  new JComponent[] { inCombo, outCombo, outChannelsCombo, keyCombo, keyChannelsCombo, key2Combo, key2ChannelsCombo }, "MIDI Devices", message);
            synth.enableMenuBar();
                             
            if (result)
                {
                // we need to build a tuple
                                
                Tuple tuple = new Tuple();
                                
                tuple.keyChannel = keyChannelsCombo.getSelectedIndex();
                tuple.key2Channel = key2ChannelsCombo.getSelectedIndex();
                tuple.outChannel = outChannelsCombo.getSelectedIndex() + 1;
                
                if (initialID != null)
                    {
                    String prospectiveID = outID.getText();
                    tuple.id = synth.reviseID(prospectiveID);
                    if (!tuple.id.equals(prospectiveID))
                        {
                        synth.disableMenuBar();
                        JOptionPane.showMessageDialog(synth, "The ID was revised to: " + tuple.id, "Device ID", JOptionPane.WARNING_MESSAGE);
                        synth.enableMenuBar();
                        }
                    }
                                
                tuple.outWrap = ((MidiDeviceWrapper)(outCombo.getSelectedItem()));
                tuple.outReceiver = tuple.outWrap.getReceiver();
                if (tuple.outReceiver == null)
                    {
                    synth.showErrorWithStackTrace("Cannot Connect", "An error occurred while connecting to the outgoing MIDI Device.");
                    return FAILED;
                    }



			//// FIXME: If you (1) create a synth connected to MidiKeys as the Key receiver
			//// (2) make another synth with the same
			///  then the first synth is disconnected from the key receiver
			
			
			
			
			
			

				// first we delete all the existing receiving device connections.
				// This is redundant to some degree
				if (old != null && removeReceiversFromOldTuple)
					{
                	if (old.inWrap != null) old.inWrap.removeFromTransmitter(old.inReceiver);
                	if (old.keyWrap != null) old.keyWrap.removeFromTransmitter(old.keyReceiver);
                	if (old.key2Wrap != null) old.key2Wrap.removeFromTransmitter(old.key2Receiver);
					}
				
				// now we reestablish them
				tuple.inReceiver = inReceiver;
                tuple.inWrap = ((MidiDeviceWrapper)(inCombo.getSelectedItem()));
                if (!tuple.inWrap.addToTransmitter(inReceiver))
                    {
                    synth.showErrorWithStackTrace("Cannot Connect", "An error occurred while connecting to the incoming MIDI Device.");
                    return FAILED;
                    }

                if (keyCombo.getSelectedItem() instanceof String)
                    {
                    }
                else
                    {
					tuple.keyReceiver = keyReceiver;
                    tuple.keyWrap = ((MidiDeviceWrapper)(keyCombo.getSelectedItem()));
                    if (!tuple.keyWrap.addToTransmitter(keyReceiver))
                        {
                        synth.showErrorWithStackTrace("Cannot Connect", "An error occurred while connecting to the Controller 1 MIDI Device.");
                        tuple.keyWrap = null;
                        }
                    //System.err.println(tuple.keyWrap);
                    }

                if (key2Combo.getSelectedItem() instanceof String)
                    {
                    }
                else
                    {
					tuple.key2Receiver = key2Receiver;
                    tuple.key2Wrap = ((MidiDeviceWrapper)(key2Combo.getSelectedItem()));
                    if (!tuple.key2Wrap.addToTransmitter(key2Receiver))
                        {
                        synth.showErrorWithStackTrace("Cannot Connect", "An error occurred while connecting to the Controller 2 MIDI Device.");
                        tuple.key2Wrap = null;
                        }
                    }

                    
                setLastTupleIn(tuple.inWrap.toString(), synth);
                setLastTupleOut(tuple.outWrap.toString(), synth);
                if (tuple.keyWrap == null)
                    setLastTupleKey("None", synth);
                else
                    setLastTupleKey(tuple.keyWrap.toString(), synth);
                if (tuple.key2Wrap == null)
                    setLastTupleKey2("None", synth);
                else
                    setLastTupleKey2(tuple.key2Wrap.toString(), synth);
                setLastTupleOutChannel(tuple.outChannel, synth);
                setLastTupleKeyChannel(tuple.keyChannel, synth);
                setLastTupleKey2Channel(tuple.key2Channel, synth);

                if (initialID != null)
                    {
                    setLastTupleID(tuple.id, synth);
                    }
                        
                return tuple;
                }
            else
                {
                return CANCELLED;
                }
            }       
        }


    public static final int CCDATA_TYPE_RAW_CC = 0;      
    public static final int CCDATA_TYPE_NRPN = 1;      
    public static final int CCDATA_TYPE_RPN = 2;      




    public static class CCData
        {
        public int type;
        public int number;
        public int value;
        public int channel;
        public boolean increment;
        public CCData(int type, int number, int value, int channel, boolean increment)
            { this.type = type; this.number = number; this.value = value; this.increment = increment; this.channel = channel; }
        }
        

        
        
    public static class Parser
        {


        ///// INTRODUCTION TO THE CC/RPN/NRPN PARSER
        ///// The parser is located in handleGeneralControlChange(...), which
        ///// can be set up to be the handler for CC messages by the MIDI library.
        /////
        ///// CC messages take one of a great many forms, which we handle in the parser
        /////
        ///// 7-bit CC messages:
        ///// 1. number >=64 and < 96 or >= 102 and < 120, with value
        /////           -> handleControlChange(channel, number, value, VALUE_7_BIT_ONLY)
        /////
        ///// Potentially 7-bit CC messages, with MSB:
        ///// 1. number >= 0 and < 32, other than 6, with value
        /////           -> handleControlChange(channel, number, value * 128 + 0, VALUE_MSB_ONLY)
        /////
        ///// Full 14-bit CC messages:
        ///// 1. number >= 0 and < 32, other than 6, with MSB
        ///// 2. same number + 32, with LSB
        /////           -> handleControlChange(channel, number, MSB * 128 + LSB, VALUE)
        /////    NOTE: this means that a 14-bit CC message will have TWO handleControlChange calls.
        /////          There's not much we can do about this, as we simply don't know if the LSB will arrive.  
        /////
        ///// Continuing 14-bit CC messages:
        ///// 1. number >= 32 and < 64, other than 38, with LSB, where number is 32 more than the last MSB.
        /////           -> handleControlChange(channel, number, former MSB * 128 + LSB, VALUE)
        /////
        ///// Lonely 14-bit CC messages (LSB only)
        ///// 1. number >= 32 and < 64, other than 38, with LSB, where number is NOT 32 more than the last MSB.
        /////           -> handleControlChange(channel, number, 0 + LSB, VALUE)
        /////           
        /////
        ///// NRPN Messages:
        ///// All NRPN Messages start with:
        ///// 1. number == 99, with MSB of NRPN parameter
        ///// 2. number == 98, with LSB of NRPN parameter
        /////           At this point NRPN MSB is set to 0
        /////
        ///// NRPN Messages then may have any sequence of:
        ///// 3.1 number == 6, with value   (MSB)
        /////           -> handleNRPN(channel, parameter, value * 128 + 0, VALUE_MSB_ONLY)
        /////                           At this point we set the NRPN MSB
        ///// 3.2 number == 38, with value   (LSB)
        /////           -> handleNRPN(channel, parameter, current NRPN MSB * 128 + value, VALUE_MSB_ONLY)
        ///// 3.3 number == 96, with value   (Increment)
        /////       If value == 0
        /////                   -> handleNRPN(channel, parameter, 1, INCREMENT)
        /////       Else
        /////                   -> handleNRPN(channel, parameter, value, INCREMENT)
        /////       Also reset current NRPN MSB to 0
        ///// 3.4 number == 97, with value
        /////       If value == 0
        /////                   -> handleNRPN(channel, parameter, 1, DECREMENT)
        /////       Else
        /////                   -> handleNRPN(channel, parameter, value, DECREMENT)
        /////       Also reset current NRPN MSB to 0
        /////
        /////
        ///// RPN Messages:
        ///// All RPN Messages start with:
        ///// 1. number == 99, with MSB of RPN parameter
        ///// 2. number == 98, with LSB of RPN parameter
        /////           At this point RPN MSB is set to 0
        /////
        ///// RPN Messages then may have any sequence of:
        ///// 3.1 number == 6, with value   (MSB)
        /////           -> handleRPN(channel, parameter, value * 128 + 0, VALUE_MSB_ONLY)
        /////                           At this point we set the RPN MSB
        ///// 3.2 number == 38, with value   (LSB)
        /////           -> handleRPN(channel, parameter, current RPN MSB * 128 + value, VALUE_MSB_ONLY)
        ///// 3.3 number == 96, with value   (Increment)
        /////       If value == 0
        /////                   -> handleRPN(channel, parameter, 1, INCREMENT)
        /////       Else
        /////                   -> handleRPN(channel, parameter, value, INCREMENT)
        /////       Also reset current RPN MSB to 0
        ///// 3.4 number == 97, with value
        /////       If value == 0
        /////                   -> handleRPN(channel, parameter, 1, DECREMENT)
        /////       Else
        /////                   -> handleRPN(channel, parameter, value, DECREMENT)
        /////       Also reset current RPN MSB to 0
        /////

        ///// NULL messages:            [RPN 127 with value of 127]
        ///// 1. number == 101, value = 127
        ///// 2. number == 100, value = 127
        /////           [nothing happens, but parser resets]
        /////
        /////
        ///// The big problem we have is that the MIDI spec allows a bare MSB or LSB to arrive and that's it!
        ///// We don't know if another one is coming.  If a bare LSB arrives we're supposed to assume the MSB is 0.
        ///// But if the bare MSB comes we don't know if the LSB is next.  So we either have to ignore it when it
        ///// comes in (bad bad bad) or send two messages, one MSB-only and one MSB+LSB.  
        ///// This happens for CC, RPN, and NRPN.
        /////
        /////
        ///// Our parser maintains four bytes in a struct called ControlParser:
        /////
        ///// 0. status.  This is one of:
        /////             INVALID: the struct holds junk.  CC: the struct is building a CC.  
        /////                     RPN_START, RPN_END: the struct is building an RPN.
        /////                     NRPN_START, NRPN_END: the struct is building an NRPN.
        ///// 1. controllerNumberMSB.  In the low 7 bits.
        ///// 2. controllerNumberLSB.  In the low 7 bits.
        ///// 3. controllerValueMSB.  In the low 7 bits. This holds the previous MSB for potential "continuing" messages.

        // Parser status values
        public static final int  INVALID = 0;
        public static final int  NRPN_START = 1;
        public static final int  NRPN_END = 2;
        public static final int  RPN_START = 2;
        public static final int  RPN_END = 3;

        int[] status = new int[16];  //  = INVALID;
                
        // The high bit of the controllerNumberMSB is either
        // NEITHER_RPN_NOR_NRPN or it is RPN_OR_NRPN. 
        int[] controllerNumberMSB = new int[16];
                
        // The high bit of the controllerNumberLSB is either
        // RPN or it is NRPN
        int[] controllerNumberLSB = new int[16];
                
        // The controllerValueMSB[channel] is either a valid MSB or it is (-1).
        int[] controllerValueMSB = new int[16];

        // The controllerValueLSB is either a valid LSB or it is  (-1).
        int[] controllerValueLSB = new int[16];
  

        // we presume that the channel never changes
        CCData parseCC(int channel, int number, int value, boolean requireLSB, boolean requireMSB)
            {
            // BEGIN PARSER

            // Start of NRPN
            if (number == 99)
                {
                status[channel] = NRPN_START;
                controllerNumberMSB[channel] = value;
                return null;
                }

            // End of NRPN
            else if (number == 98)
                {
                controllerValueMSB[channel] = 0;
                if (status[channel] == NRPN_START)
                    {
                    status[channel] = NRPN_END;
                    controllerNumberLSB[channel] = value;
                    controllerValueLSB[channel]  = -1;
                    controllerValueMSB[channel]  = -1;
                    }
                else status[channel] = INVALID;
                return null;
                }
                
            // Start of RPN or NULL
            else if (number == 101)
                {
                if (value == 127)  // this is the NULL termination tradition, see for example http://www.philrees.co.uk/nrpnq.htm
                    {
                    status[channel] = INVALID;
                    }
                else
                    {
                    status[channel] = RPN_START;
                    controllerNumberMSB[channel] = value;
                    }
                return null;
                }

            // End of RPN or NULL
            else if (number == 100)
                {
                controllerValueMSB[channel] = 0;
                if (value == 127)  // this is the NULL termination tradition, see for example http://www.philrees.co.uk/nrpnq.htm
                    {
                    status[channel] = INVALID;
                    }
                else if (status[channel] == RPN_START)
                    {
                    status[channel] = RPN_END;
                    controllerNumberLSB[channel] = value;
                    controllerValueLSB[channel]  = -1;
                    controllerValueMSB[channel]  = -1;
                    }
                return null;
                }

            else if ((number == 6 || number == 38 || number == 96 || number == 97) && (status[channel] == NRPN_END || status[channel] == RPN_END))  // we're currently parsing NRPN or RPN
                {
                int controllerNumber =  (((int) controllerNumberMSB[channel]) << 7) | controllerNumberLSB[channel] ;
                        
                if (number == 6)
                    {
                    controllerValueMSB[channel] = value;
                    if (requireLSB && controllerValueLSB[channel] == -1)
                        return null;
                    if (status[channel] == NRPN_END)
                        return handleNRPN(channel, controllerNumber, controllerValueLSB[channel] == -1 ? 0 : controllerValueLSB[channel], controllerValueMSB[channel]);
                    else
                        return handleRPN(channel, controllerNumber, controllerValueLSB[channel] == -1 ? 0 : controllerValueLSB[channel], controllerValueMSB[channel]);
                    }
                                                                                                                        
                // Data Entry LSB for RPN, NRPN
                else if (number == 38)
                    {
                    controllerValueLSB[channel] = value;
                    if (requireMSB && controllerValueMSB[channel] == -1)
                        return null;          
                    if (status[channel] == NRPN_END)
                        return handleNRPN(channel, controllerNumber, controllerValueLSB[channel], controllerValueMSB[channel] == -1 ? 0 : controllerValueMSB[channel]);
                    else
                        return handleRPN(channel, controllerNumber, controllerValueLSB[channel], controllerValueMSB[channel] == -1 ? 0 : controllerValueMSB[channel]);
                    }
                                                                                                                        
                // Data Increment for RPN, NRPN
                else if (number == 96)
                    {
                    if (value == 0)
                        value = 1;
                    if (status[channel] == NRPN_END)
                        return handleNRPNIncrement(channel, controllerNumber, value);
                    else
                        return handleRPNIncrement(channel, controllerNumber, value);
                    }

                // Data Decrement for RPN, NRPN
                else // if (number == 97)
                    {
                    if (value == 0)
                        value = -1;
                    if (status[channel] == NRPN_END)
                        return handleNRPNIncrement(channel, controllerNumber, -value);
                    else
                        return handleRPNIncrement(channel, controllerNumber, -value);
                    }
                                
                }
                        
            else  // Some other CC
                {
                // status[channel] = INVALID;           // I think it's fine to send other CC in the middle of NRPN or RPN
                return handleRawCC(channel, number, value);
                }
            }
        
        public CCData processCC(ShortMessage message, boolean requireLSB, boolean requireMSB)
            {
            int num = message.getData1();
            int val = message.getData2();
            int channel = message.getChannel();
            return parseCC(channel, num, val, requireLSB, requireMSB);
            }
        
        public CCData handleNRPN(int channel, int controllerNumber, int _controllerValueLSB, int _controllerValueMSB)
            {
            if (_controllerValueLSB < 0 || _controllerValueMSB < 0)
                System.err.println("Warning (Midi): " + "LSB or MSB < 0.  RPN: " + controllerNumber + "   LSB: " + _controllerValueLSB + "  MSB: " + _controllerValueMSB);
            return new CCData(CCDATA_TYPE_NRPN, controllerNumber, _controllerValueLSB | (_controllerValueMSB << 7), channel, false);
            }
        
        public CCData handleNRPNIncrement(int channel, int controllerNumber, int delta)
            {
            return new CCData(CCDATA_TYPE_NRPN, controllerNumber, delta, channel, true);
            }

        public CCData handleRPN(int channel, int controllerNumber, int _controllerValueLSB, int _controllerValueMSB)
            {
            if (_controllerValueLSB < 0 || _controllerValueMSB < 0)
                System.err.println("Warning (Midi): " + "LSB or MSB < 0.  RPN: " + controllerNumber + "   LSB: " + _controllerValueLSB + "  MSB: " + _controllerValueMSB);
            return new CCData(CCDATA_TYPE_RPN, controllerNumber, _controllerValueLSB | (_controllerValueMSB << 7), channel, false);
            }
        
        public CCData handleRPNIncrement(int channel, int controllerNumber, int delta)
            {
            return new CCData(CCDATA_TYPE_RPN, controllerNumber, delta, channel, true);
            }

        public CCData handleRawCC(int channel, int controllerNumber, int value)
            {
            return new CCData(CCDATA_TYPE_RAW_CC, controllerNumber, value, channel, false);
            }
        }
                
    public Parser controlParser = new Parser();
    public Parser synthParser = new Parser();
    
    
    /** A DividedSysex message is a Sysex MidiMessage which has been broken into chunks.
        This allows us to send the Sysex as multiple messages, with pauses in-between each
        message, so as not to overflow a MIDI buffer in machines such as a Kawai K1 whose
        processors are not powerful enough to keep up with a large sysex dump. 
        
        <p>The critical method is the factory method divide(), which creates the array
        of divided sysex chunks. 
    */
        

    public static String format(MidiMessage message)
        {
        if (message == null)
            {
            return "null";
            }
        else if (message instanceof DividedSysex)
            {
            byte[] d = ((DividedSysex)message).getData();
            String s = "DividedSysex";
            for(int i = 0; i < d.length; i++)
                s += (" " + String.format("%02x", d[i]));
            return s;
            }
        else if (message instanceof MetaMessage)
            {
            return "A MIDI File MetaMessage (shouldn't happen)";
            }
        else if (message instanceof SysexMessage)
            {
            if (((SysexMessage)message).getMessage()[0] == 0xF0)  // First one
            	{
	            return "Sysex (" + getManufacturerForSysex(((SysexMessage)message).getData()) + ")";
	            }
	        else
	        	{
	            return "Sysex Fragment";
	            }
            }
        else // ShortMessage
            {
            ShortMessage s = (ShortMessage) message;
            int c = s.getChannel();
            String type = "Unknown";
            switch(s.getStatus())
                {
                case ShortMessage.ACTIVE_SENSING: type = "Active Sensing"; c = -1; break;
                case ShortMessage.CHANNEL_PRESSURE: type = "Channel Pressure"; break;
                case ShortMessage.CONTINUE: type = "Continue"; c = -1; break;
                case ShortMessage.CONTROL_CHANGE: type = "Control Change"; break;
                case ShortMessage.END_OF_EXCLUSIVE: type = "End of Sysex Marker"; c = -1; break;
                case ShortMessage.MIDI_TIME_CODE: type = "Midi Time Code"; c = -1; break;
                case ShortMessage.NOTE_OFF: type = "Note Off"; break;
                case ShortMessage.NOTE_ON: type = "Note On"; break;
                case ShortMessage.PITCH_BEND: type = "Pitch Bend"; break;
                case ShortMessage.POLY_PRESSURE: type = "Poly Pressure"; break;
                case ShortMessage.PROGRAM_CHANGE: type = "Program Change"; break;
                case ShortMessage.SONG_POSITION_POINTER: type = "Song Position Pointer"; c = -1; break;
                case ShortMessage.SONG_SELECT: type = "Song Select"; c = -1; break;
                case ShortMessage.START: type = "Start"; c = -1; break;
                case ShortMessage.STOP: type = "Stop"; c = -1; break;
                case ShortMessage.SYSTEM_RESET: type = "System Reset"; c = -1; break;
                case ShortMessage.TIMING_CLOCK: type = "Timing Clock"; c = -1; break;
                case ShortMessage.TUNE_REQUEST: type = "Tune Request"; c = -1; break;
                }
            return type + (c == -1 ? "" : (" (Channel " + c + ")"));
            }
        }

    static HashMap manufacturers = null;
    
    static HashMap getManufacturers()
        {
        if (manufacturers != null)
            return manufacturers;
                        
        manufacturers = new HashMap();
        Scanner scan = new Scanner(Midi.class.getResourceAsStream("Manufacturers.txt"));
        while(scan.hasNextLine())
            {
            String nextLine = scan.nextLine().trim();
            if (nextLine.equals("")) continue;
            if (nextLine.startsWith("#")) continue;
                        
            int id = 0;
            Scanner scan2 = new Scanner(nextLine);
            int one = scan2.nextInt(16);  // in hex
            if (one == 0x00)  // there are two more to read
                {
                id = id + (scan2.nextInt(16) << 8) + (scan2.nextInt(16) << 16);
                }
            else
                {
                id = one;
                }
            manufacturers.put(new Integer(id), scan.nextLine().trim());
            }
        return manufacturers;
        }

    /** This works with or without F0 as the first data byte */
    public static String getManufacturerForSysex(byte[] data)
        {
        int offset = 0;
        if (data[0] == (byte)0xF0)
            offset = 1;
        HashMap map = getManufacturers();
        if (data[0 + offset] == (byte)0x7D)             // educational use
            {
            return (String)(map.get(new Integer(data[0 + offset]))) + 
                "\n\nNote that unregistered manufacturers or developers typically\n use this system exclusive region.";
            }
        else if (data[0 + offset] == (byte)0x00)
            {
            return (String)(map.get(new Integer(
                        0x00 + 
                        ((data[1 + offset] < 0 ? data[1 + offset] + 256 : data[1 + offset]) << 8) + 
                        ((data[2 + offset] < 0 ? data[2 + offset] + 256 : data[2 + offset]) << 16))));
            }
        else
            {
            return (String)(map.get(new Integer(data[0 + offset])));
            }
        }
    
    
    byte[] inSysex = null;
    byte[] keySysex = null;
    byte[] key2Sysex = null;
    
    public void resetInSysexData()
        {
        inSysex = null;
        }
        
    public void resetKeySysexData()
        {
        keySysex = null;
        }
        
    public void resetKey2SysexData()
        {
        key2Sysex = null;
        }
        
    public byte[] gatherInSysexData(byte[] data, int messageLen)
        {
        if (messageLen == 0) // uh...
            {
            return null;
            }
                
        if (data.length == 0) // uh...
            {
            return null;
            }
                                
        if (data[0] == (byte)0xF0)  // it's a new message
            {
            //System.err.println("0xF0");
            inSysex = new byte[messageLen];
            System.arraycopy(data, 0, inSysex, 0, messageLen);
            }
        else if (data[0] == (byte)0xF7)  // it's a continuation of a message
            {
            //System.err.println("0xF7");
            if (inSysex == null) // uh...
                return null;
            byte[] temp = new byte[inSysex.length + messageLen - 1];
                
            // yeah yeah, O(n^2), I could obviously do much, much better, maybe later
            System.arraycopy(inSysex, 0, temp, 0, inSysex.length);
            System.arraycopy(data, 1, temp, inSysex.length, messageLen - 1);        // skip the 0xF7
            inSysex = temp;
            }
        /*
          else                // May be missing the 0xF7?
          {
          if (inSysex == null) // uh...
          return null;
          byte[] temp = new byte[inSysex.length + messageLen];
          // yeah yeah, O(n^2), I could obviously do much better, maybe later
          System.arraycopy(inSysex, 0, temp, 0, inSysex.length);
          System.arraycopy(data, 0, temp, inSysex.length, messageLen);
          }
        */
        /*
          for(int i = 0; i < inSysex.length; i++)
          System.err.print(" " + inSysex[i]);
          System.err.println();
        */
        if (inSysex != null && inSysex.length != 0 && inSysex[inSysex.length - 1] == (byte)0xF7)  // completed
            {
            //System.err.println("0xF7 END");
            byte[] temp = inSysex;
            inSysex = null;
            return temp;
            }
        else return null;
        }



    public byte[] gatherInSysexData(SysexMessage message)
        {
        int messageLen = message.getLength();
        
        if (messageLen == 0) // uh...
            {
            return null;
            }
                
        byte[] data = message.getMessage();

        if (data.length == 0) // uh...
            {
            return null;
            }
                                
        if (data[0] == (byte)0xF0)  // it's a new message
            {
            inSysex = new byte[messageLen];
            System.arraycopy(data, 0, inSysex, 0, inSysex.length);
            }
        else if (data[0] == (byte)0xF7)  // it's a continuation of a message
            {
            if (inSysex == null) // uh...
                return null;
            byte[] temp = new byte[inSysex.length + messageLen - 1];
                
            // yeah yeah, O(n^2), I could obviously do much, much better, maybe later
            System.arraycopy(inSysex, 0, temp, 0, inSysex.length);
            System.arraycopy(data, 1, temp, inSysex.length, messageLen - 1);        // skip the 0xF7
            inSysex = temp;
            }
        /*
          else                // May be missing the 0xF7?
          {
          if (inSysex == null) // uh...
          return null;
          byte[] temp = new byte[inSysex.length + messageLen];
          // yeah yeah, O(n^2), I could obviously do much better, maybe later
          System.arraycopy(inSysex, 0, temp, 0, inSysex.length);
          System.arraycopy(data, 0, temp, inSysex.length, messageLen);
          }
        */

        if (inSysex != null && inSysex.length != 0 && inSysex[inSysex.length - 1] == (byte)0xF7)  // completed
            {
            byte[] temp = inSysex;
            inSysex = null;
            return temp;
            }
        else return null;
        }

    public byte[] gatherKeySysexData(SysexMessage message)
        {
        int messageLen = message.getLength();

        if (messageLen == 0) // uh...
            return null;
        
        byte[] data = message.getMessage();

        if (data.length == 0) // uh...
            return null;
                
        if (data[0] == 0xF0)  // it's a new message
            {
            keySysex = new byte[messageLen];
            System.arraycopy(data, 0, keySysex, 0, keySysex.length);
            }
        else if (data[0] == 0xF7)  // it's a continuation of a message
            {
            if (keySysex == null) // uh...
                return null;
            byte[] temp = new byte[keySysex.length + messageLen - 1];
                
            // yeah yeah, O(n^2), I could obviously do much, much better, maybe later
            System.arraycopy(keySysex, 0, temp, 0, keySysex.length);
            System.arraycopy(data, 1, temp, keySysex.length, messageLen - 1);       // skip the 0xF7
            }
        /*
          else                // May be missing the 0xF7?
          {
          if (keySysex == null) // uh...
          return null;
          byte[] temp = new byte[keySysex.length + messageLen];
          // yeah yeah, O(n^2), I could obviously do much better, maybe later
          System.arraycopy(keySysex, 0, temp, 0, keySysex.length);
          System.arraycopy(data, 0, temp, keySysex.length, messageLen);
          }
        */

        if (keySysex != null && keySysex.length != 0 && keySysex[keySysex.length - 1] == 0xF7)  // completed
            {
            byte[] temp = keySysex;
            keySysex = null;
            return temp;
            }
        else return null;
        }

    public byte[] gatherKey2SysexData(SysexMessage message)
        {
        int messageLen = message.getLength();

        if (messageLen == 0) // uh...
            return null;
        
        byte[] data = message.getMessage();

        if (data.length == 0) // uh...
            return null;
                
        if (data[0] == 0xF0)  // it's a new message
            {
            key2Sysex = new byte[messageLen];
            System.arraycopy(data, 0, key2Sysex, 0, key2Sysex.length);
            }
        else if (data[0] == 0xF7)  // it's a continuation of a message
            {
            if (key2Sysex == null) // uh...
                return null;
            byte[] temp = new byte[key2Sysex.length + messageLen - 1];
                
            // yeah yeah, O(n^2), I could obviously do much, much better, maybe later
            System.arraycopy(key2Sysex, 0, temp, 0, key2Sysex.length);
            System.arraycopy(data, 1, temp, key2Sysex.length, messageLen - 1);      // skip the 0xF7
            }
        /*
          else                // May be missing the 0xF7?
          {
          if (key2Sysex == null) // uh...
          return null;
          byte[] temp = new byte[key2Sysex.length + messageLen];
          // yeah yeah, O(n^2), I could obviously do much better, maybe later
          System.arraycopy(key2Sysex, 0, temp, 0, key2Sysex.length);
          System.arraycopy(data, 0, temp, key2Sysex.length, messageLen);
          }
        */

        if (key2Sysex != null && key2Sysex.length != 0 && key2Sysex[key2Sysex.length - 1] == 0xF7)  // completed
            {
            byte[] temp = key2Sysex;
            key2Sysex = null;
            return temp;
            }
        else return null;
        }
    }
