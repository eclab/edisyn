/***
    Copyright 2022 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn;
import edisyn.gui.*;
import java.util.*;
import javax.sound.midi.*;

/** Breaks up Sysex data into fragmented chunks suitable to send one at a time.
	Some synthesizers cannot receive a large sysex message due to limited buffers,
	or require that it be sent piecemeal.  To accommodate this, Java has a mechanism
	which permits you to send a sysex message as a stream of SysexMessages.  The first
	message starts with an 0xF0.  Successive messages start with 0xF7, which is supposed
	to be stripped off.  The final message starts with an 0xF7 and concludes with an 0xF7.
	
	<p>However two bugs complicate this.  In Windows and Linux, you cannot send a (proper) sysex
	message fragment which starts with an 0xF7: it will bomb your program.  Similarly, you cannot
	send a message fragment which contains nothing but a bare concluding 0xF7.  On the Mac we use
	CoreMidi4J, which properly handles sysex message fragments starting with 0xF7.
	
	<p>To deal with this, DividedSysex will break the sysex message into fragments handled either
	by SysexMessage (in the case of the Mac) or by DividedSysex messages (in the case of Windows
	and Linux).
*/
	
public class DividedSysex extends MidiMessage
	{
	public byte[] getData() { return data; }
	
	public Object clone()
		{
		return new DividedSysex(getMessage());
		}
	
	public DividedSysex(byte[] data)
		{
		super(data.clone());
		}
	
	
	/** Builds an array of either SysexMessage or DividedSysex messages, depending on
		the platform (windows and linux have a bug which causes them to bomb).
		These messages break up the given sysex message into objects of either
		*chunksize* or *chunksize - 1* (varying to work around another windows/linux bug),
		which can be sent in multiple pieces and can be separated by pauses.  Do not
		insert other messages in-between them: it'll break things. Chunksize must be
		greater than 3. */
	public static MidiMessage[] create(SysexMessage sysex, int chunksize)
		{
		return create(sysex, chunksize, !Style.isMac());
		}

	static MidiMessage[] create(SysexMessage sysex, int chunksize, boolean hack)
		{
		return create(sysex.getMessage(), chunksize, hack);
		}            
		
	/** Builds an array of either SysexMessage or DividedSysex messages, depending on
		the platform (windows and linux have a bug which causes them to bomb).
		These messages break up the given sysex message into objects of either
		*chunksize* or *chunksize - 1* (varying to work around another windows/linux bug),
		which can be sent in multiple pieces and can be separated by pauses.  Do not
		insert other messages in-between them: it'll break things.  Chunksize must be
		greater than 3. */

	 public static MidiMessage[] create(byte[] sysex, int chunksize)
		{
		return create(sysex, chunksize, !Style.isMac());
		}

	static MidiMessage[] create(byte[] sysex, int chunksize, boolean hack)
		{
		// we need a little headroom to guarantee we can't reduce chunksize to 1, not including the header.
		if (chunksize <= 3) // uh... ? 
			throw new RuntimeException("Illegal chunksize in DivideSysex.create(), must be > 3, was " + chunksize);

		// adjust chunksize if necessary
		if (sysex.length % chunksize == 1)		// the last chunk would be a bare F7, which bombs windows
			{
			chunksize--;
			}
		
		// build the sysex chunks
		byte[][] newsysex = new byte[sysex.length / chunksize][];
		for(int i = 0; i < newsysex.length; i++)
			{
			System.arraycopy(sysex, chunksize * i, newsysex, 0, chunksize);
			}

		return create(newsysex, hack);
		}
		

	/** Given a sysex message which is broken into multiple fragments, builds an 
		array of either SysexMessage or DividedSysex messages, depending on
		the platform (windows and linux have a bug which causes them to bomb).
		Each of these messages encapsulate one provided fragment.  Each fragment size
		must be at least 3.  These messages can be separated by pauses.  Do not
		insert other messages in-between them: it'll break things. */
	 public static MidiMessage[] create(byte[][] sysex)
		{
		return create(sysex, !Style.isMac());        	
		}

	 static MidiMessage[] create(byte[][] sysex, boolean hack)
		{
		if (hack)		// Windows and Linux
			{
			// We just break up the message into pieces and assign a DividedSysex
			// to each piece.
			DividedSysex[] div = new DividedSysex[sysex.length];
			for(int i = 0; i < sysex.length; i++)
				{
				div[i] = new DividedSysex(sysex[i]);
				}
			return div;
			}
		else			// MacOS
			{
			try
				{
				// We use SysexMessages.  I hope this will work?  Otherwise we can use
				// DividedSysex messages instead.  In the first case  break up the message into pieces and assign a DividedSysex
				// to each piece.  For the first message (which contains an 0xF0) we build
				// a standard Sysex message that does NOT end in 0xF7.  In the successive
				// messages, we make room for an extra 0xF7 at the beginning.
				SysexMessage[] div = new SysexMessage[sysex.length];
				for(int i = 0; i < sysex.length; i++)
					{
					//System.err.println(edisyn.util.StringUtility.toHex(sysex[i]));
					if (i == 0)
						{
						div[i] = new SysexMessage(sysex[i], sysex[i].length);
						}
					else 
						{
						byte[] sysex2 = new byte[sysex[i].length + 1];
						System.arraycopy(sysex[i], 0, sysex2, 1, sysex[i].length);
						sysex2[0] = (byte)0xF7;
						div[i] = new SysexMessage(sysex2, sysex2.length);
						}
					}
				return div;
				}
			catch (InvalidMidiDataException ex)
				{
				ex.printStackTrace();
				return new MidiMessage[0];
				}
			}
		}
	}
