/* 
   Copyright 2026 by Sean Luke
   Licensed under Apache 2.0
*/


package edisyn.util;
import java.util.*;

/** 
    MIDI FILE DUMP
    Sean Luke, 2026
        
    <p>This is a collection of utility functions for encoding and decoding a file into MIDI messages
    suitable for transferring using the MIDI File Dump protocol (page 41 of the MIDI spec).
    In the MIDI File Dump protocol, you create and send a HEADER describing the file, and then a 
    series of PACKETS which contain payloads, each of which is a chunk of your file.  The receiver
    can send you certain FLOW CONTROL messages to pause or cancel the dump.  The receiver can also
    issue to you a REQUEST to start a dump.

    <p>MIDI File Dump is kind of stupid in that the header doesn't tell you how many packets
    are arriving.  Indeed, the header might tell you that ZERO packets are arriving if it doesn't
    know the size of the file up front.  MidiFileDump.java assumes that when you are the sender,
    you always know the file size up front (it's not real-time, sorry).
        
    <p>As a result, when you receive and validate a header, you'll have to extract the data from
    each message as it arrives and count until the right number of bytes have arrived.  You can
    then concatenate all the data (we provide a convenience function for that).
*/
        
public class MidiFileDump
    {
/** MIDI Files */
    public static final String FILE_TYPE_MIDI = "MIDI";
/** MIDIEX Files */
    public static final String FILE_TYPE_MIDIEX = "MIEX";
/** ESEQ Files */
    public static final String FILE_TYPE_ESEQ = "ESEQ";
/** ASCII Text Files */
    public static final String FILE_TYPE_ASCII = "TEXT";
/** Raw Binary Files -- these are the most common nowadays */
    public static final String FILE_TYPE_BIN = "BIN ";
/** MacOS 9.0 files stored as MacBinary */
    public static final String FILE_TYPE_MAC = "MAC ";

    public static final int MESSAGE_TYPE_NOT_FILE_DUMP = -1;
    public static final int MESSAGE_TYPE_REQUEST = -2;
    public static final int MESSAGE_TYPE_HEADER = -3;
    public static final int MESSAGE_TYPE_HANDSHAKE_EOF = -4;
    public static final int MESSAGE_TYPE_HANDSHAKE_WAIT = -5;
    public static final int MESSAGE_TYPE_HANDSHAKE_CANCEL = -6;
    public static final int MESSAGE_TYPE_HANDSHAKE_NAK = -7;
    public static final int MESSAGE_TYPE_HANDSHAKE_ACK = -8;

// These are the actual protocol command numbers

    static final int FILE_DUMP = 0x07;
    static final int HEADER = 0x01;
    static final int PACKET = 0x02;
    static final int REQUEST = 0x03;
    static final int HANDSHAKE_EOF = 0x7B;
    static final int HANDSHAKE_WAIT = 0x7C;
    static final int HANDSHAKE_CANCEL = 0x7D;
    static final int HANDSHAKE_NAK = 0x7E;
    static final int HANDSHAKE_ACK = 0x7F;
 
// The max packet length is 137.  But we're going to assume we're packing
// no more than 119 data bytes into the packet, for a total of 136 encoded bytes

    static final int MAX_ENCODED_PACKET_LENGTH = 136;
    static final int MAX_DATA_PACKET_LENGTH = 119;


/** Requests a file dump from a SENDER to a RECEIVER (you).  Returns the resulting sysex message to send.

    <p> 
    The SENDER ID is the ID of the sender: it may not be 0x7F.   It must be 0...126
    <p>
    The RECEIVER ID is the ID of the receiver.  It should be 0...126: it is not clear if it is allowed to be 0x7F ("Everyone").
    <p>
    The TYPE is a four-character String.  It can be, but is not required to be, one of FILE_TYPE_MIDI, FILE_TYPE_MIDIEX, FILE_TYPE_ESEQ, 
    FILE_TYPE_ASCII, FILE_TYPE_BIN, or FILE_TYPE_MAC. Most likely you're looking for FILE_TYPE_BIN.
    <p>
    The NAME must be an ASCII (not unicode) name.  Do not include 0x0 at the end.
*/
    public static byte[] request(int senderID, int receiverID, String type, String name)
        {
        byte[] data = new byte[11 + name.length()];
        data[0] = (byte)0xF0;
        data[1] = 0x7E;
        data[2] = (byte)receiverID;
        data[3] = FILE_DUMP;
        data[4] = REQUEST;
        data[5] = (byte)senderID;
        data[6] = (byte)type.charAt(0);
        data[7] = (byte)type.charAt(1);
        data[8] = (byte)type.charAt(2);
        data[9] = (byte)type.charAt(3);
        data[data.length - 1] = (byte)0xF7;
        
        for(int i = 0; i < type.length(); i++)
            {
            data[i] = (byte)type.charAt(i);
            }
        
        return data;
        }
        
/** Sends a handshake message to the SENDER with regard to (or following) a given data packet.    Returns the resulting sysex message to send.
    The message can be any of 
    MESSAGE_TYPE_HANDSHAKE_EOF, MESSAGE_TYPE_HANDSHAKE_WAIT, MESSAGE_TYPE_HANDSHAKE_CANCEL, MESSAGE_TYPE_HANDSHAKE_NAK, or MESSAGE_TYPE_HANDSHAKE_ACK
*/
    public static byte[] handshake(int senderID, int handshakeMessage, int packetNumber)
        {
        int handshake = (MESSAGE_TYPE_HANDSHAKE_EOF - handshakeMessage) + HANDSHAKE_EOF;
        if (handshake < HANDSHAKE_EOF || handshake > HANDSHAKE_ACK) throw new RuntimeException("Invalid handshake message number " + handshakeMessage);
        if (senderID < 0 || senderID > 127) throw new RuntimeException("Invalid sender id " + senderID);
        if (packetNumber < 0 || packetNumber > 127) throw new RuntimeException("Invalid packet number " + packetNumber);
        return new byte[] { (byte)0xF0, 0x7E, (byte)senderID, (byte)handshake, (byte)packetNumber, (byte)0xF7 };
        }

/** Dumps a file from a sender (you) to a receiver. Returns an array of sysex message to send, one at a time.
    The first sysex message is the HEADER, and the remaining sysex messages are the PACKETS in order.
        
    <p>
    The SENDER ID is the ID of the sender: it may not be 0x7F.   It must be 0...126
    <p>
    The RECEIVER ID is the ID of the receiver (you).  It should be 0...126: it is not clear if it is allowed to be 0x7F ("Everyone").
    <p>
    The TYPE is a four-character String.  It can be, but is not required to be, one of FILE_TYPE_MIDI, FILE_TYPE_MIDIEX, FILE_TYPE_ESEQ, 
    FILE_TYPE_ASCII, FILE_TYPE_BIN, or FILE_TYPE_MAC. Most likely you're looking for FILE_TYPE_BIN.
    <p>
    The NAME must be an ASCII (not unicode) name.  Do not include 0x0 at the end.
    <p>
    The DATA contains all unencoded data bytes to dump.
*/
    public static byte[][] dump(int senderID, int receiverID, String type, String name, byte[] data)
        {
        if (senderID < 0 || senderID > 127) throw new RuntimeException("Invalid sender id " + senderID);
        if (receiverID < 0 || receiverID > 127) throw new RuntimeException("Invalid receiver id " + receiverID);

        ArrayList<byte[]> messages = new ArrayList<>(0);
        
        // build header
        byte[] header = new byte[15 + name.length()];
        header[0] = (byte)0xF0;
        header[1] = 0x7E;
        header[2] = (byte)receiverID;
        header[3] = FILE_DUMP;
        header[4] = HEADER;
        header[5] = (byte)senderID;
        header[6] = (byte)type.charAt(0);
        header[7] = (byte)type.charAt(1);
        header[8] = (byte)type.charAt(2);
        header[9] = (byte)type.charAt(3);
        header[10]= (byte)(data.length & 0x7F);
        header[11]= (byte)((data.length >>> 7) & 0x7F);
        header[12]= (byte)((data.length >>> 14) & 0x7F);
        header[13]= (byte)((data.length >>> 21) & 0x7F);
        header[header.length - 1] = (byte)0xF7;
        
        for(int i = 0; i < name.length(); i++)
            {
            header[i + 14] = (byte)name.charAt(i);
            }
        messages.add(header);
        
        // How many packets do we need?
        
        // For each packet....  
        int packetCount = 0;                    // wraps at 127
        for(int i = 0; i < data.length; i += MAX_DATA_PACKET_LENGTH)
            {               
            byte[] encoded = new byte[9 + 
                // If we have enough data for a full packet, then our encoded version is just the full packet
                    (data.length - i > MAX_DATA_PACKET_LENGTH ? MAX_DATA_PACKET_LENGTH / 7 * 8 :
                    // Else it's the integer 7th divisor, plus ... 
                    (data.length - i) / 7 * 8 + 
                    /// ... if there's anything left, take the remainder and add 1
                    ((data.length - i) % 7 > 0 ? ((data.length - i) % 7 + 1) : 0))];
                
            encoded[0] = (byte)0xF0;
            encoded[1] = 0x7E;
            encoded[2] = (byte)receiverID;
            encoded[3] = FILE_DUMP;
            encoded[4] = PACKET;
            encoded[5] = (byte)packetCount;

            packetCount++;
            if (packetCount >= 128) packetCount = 0;                // wrap around at 127

            // Go through in chunks of 8 ...
            int encodedCount = 0;
            int decodedSize = Math.min(MAX_DATA_PACKET_LENGTH, data.length - i);
            for(int j = i; j < i + decodedSize; j += 7)
                {
                int highByte = encodedCount + 7;        // jump to encoded[7], the first high byte
                encodedCount++;         // skip the header
                for(int k = j; k < j + 7; k++)
                    {
                    if (k < data.length)
                        {
                        encoded[encodedCount + 7] = (byte)(data[k] & 0x7F);
                        // This is tricky: data[k] >>> 7 will promote data[k] to an int, but because
                        // data[k] could be a negative byte, it'll turn into a negative int, which
                        // is stuffed with 1s on the top to make it negative, so >>> will push 1s
                        // down into the byte instead of 0s.  So we have to make sure that it's masked
                        // with 0xFF...
                        encoded[highByte] = (byte)((encoded[highByte] << 1) | ((data[k] & 0xFF) >>> 7));
                        }
                    else
                        {
                        encoded[highByte] = (byte)(encoded[highByte] << 1);
                        }
                    encodedCount++;
                    }
                }
                        
            encoded[6] = (byte)(decodedSize - 1);
                
            // compute checksum
            int checksum = (encoded[1] & 0xF7);
            for(int j = 2; j < encoded.length - 2; j++)             // Everything AFTER the F0 but BEFORE the checksum byte
                {
                checksum = checksum ^ (encoded[j] & 0xF7);
                }
            encoded[encoded.length - 2] = (byte)(checksum & 0xF7);
            encoded[encoded.length - 1] = (byte)0xF7;
                
            messages.add(encoded);
            }
        
        return messages.toArray(new byte[0][0]);
        }
        

/** Returns the MIDI File Dump type corresponding to the provided midiMessage,
    or if the message represents a MIDI File Dump packet, returns the packet number
    (a positive integer from 0 to 127 inclusive).
    The types are any of: MESSAGE_TYPE_HEADER, MESSAGE_TYPE_REQUEST, MESSAGE_TYPE_HANDSHAKE_EOF, MESSAGE_TYPE_HANDSHAKE_WAIT, 
    MESSAGE_TYPE_HANDSHAKE_CANCEL, MESSAGE_TYPE_HANDSHAKE_NAK, MESSAGE_TYPE_HANDSHAKE_ACK, or MESSAGE_TYPE_NOT_FILE_DUMP (if
    the message is not recognized as a MIDI File Dump message)

    <p>Note that this code does not validate the proper length or format of the midiMessage, nor does it check for null. 
*/
    public static int recognize(byte[] midiMessage)
        {
        if (midiMessage.length >= 6 &&
            midiMessage[0] == (byte)0xF0 &&
            midiMessage[1] == 0x7E &&
            midiMessage[3] == FILE_DUMP)
            {
            switch (midiMessage[4])
                {
                case HEADER: return MESSAGE_TYPE_HEADER;
                case PACKET: return midiMessage[5];
                case REQUEST: return MESSAGE_TYPE_REQUEST;
                case HANDSHAKE_EOF: return MESSAGE_TYPE_HANDSHAKE_EOF;
                case HANDSHAKE_WAIT: return MESSAGE_TYPE_HANDSHAKE_WAIT;
                case HANDSHAKE_CANCEL: return MESSAGE_TYPE_HANDSHAKE_CANCEL;
                case HANDSHAKE_NAK: return MESSAGE_TYPE_HANDSHAKE_NAK;
                case HANDSHAKE_ACK: return MESSAGE_TYPE_HANDSHAKE_ACK;
                }
            }
        return MESSAGE_TYPE_NOT_FILE_DUMP;
        }

/** Assuming that the message provided is a MIDI File Dump Request message, returns the sender ID. */
    public static int getRequestSenderID(byte[] requestMessage)
        {
        return requestMessage[2];
        }

/** Assuming that the message provided is a MIDI File Dump Request message, returns the receiver ID. */
    public static int getRequestReceiverID(byte[] requestMessage)
        {
        return requestMessage[5];
        }

    static String buildString(byte[] bytes)
        {
        try
            {
            return new String(bytes, "UTF-8"); 
            }
        catch (java.io.UnsupportedEncodingException ex)
            {
            throw new RuntimeException(ex);
            }
        }
        
/** Assuming that the message provided is a MIDI File Dump Request message, returns the file type. */
    public static String getRequestType(byte[] requestMessage)
        {
        byte[] type = new byte[4];
        System.arraycopy(requestMessage, 6, type, 0, type.length);
        return buildString(type);
        }

/** Assuming that the message provided is a MIDI File Dump Request message, returns the name. */
    public static String getRequestName(byte[] requestMessage)
        {
        byte[] name = new byte[requestMessage.length - 1 - 10];
        System.arraycopy(requestMessage, 10, name, 0, name.length);
        return buildString(name);
        }

/** Assuming that the message provided is a MIDI File Dump Header message, returns the sender ID. */
    public static int getHeaderSenderID(byte[] headerMessage)
        {
        return headerMessage[5];
        }

/** Assuming that the message provided is a MIDI File Dump Header message, returns the receiver ID. */
    public static int getHeaderReceiverID(byte[] headerMessage)
        {
        return headerMessage[2];
        }

/** Assuming that the message provided is a MIDI File Dump Header message, returns the file type. */
    public static String getHeaderType(byte[] headerMessage)
        {
        byte[] type = new byte[4];
        System.arraycopy(headerMessage, 6, type, 0, type.length);
        return buildString(type);
        }

/** Assuming that the message provided is a MIDI File Dump Header message, returns the data length. */
    public static int getHeaderLength(byte[] headerMessage)
        {
        return headerMessage[10] | (headerMessage[11] << 7) | (headerMessage[12] << 14) | (headerMessage[13] << 21);
        }

/** Assuming that the message provided is a MIDI File Dump Header message, returns the name. */
    public static String getHeaderName(byte[] headerMessage)
        {
        byte[] name = new byte[headerMessage.length - 1 - 14];
        System.arraycopy(headerMessage, 14, name, 0, name.length);
        return buildString(name);
        }

/** Assuming that the message provided is a MIDI File Dump Packet message, returns the receiver ID. */
    public static int getPacketReceiverID(byte[] packetMessage)
        {
        return packetMessage[2];
        }

/** Assuming that the message provided is a MIDI File Dump Packet message, returns the packet number. */
    public static int getPacketNumber(byte[] packetMessage)
        {
        return packetMessage[5];
        }
        
/** Assuming that the message provided is a MIDI File Dump Packet message, returns whether the checksum is valid. */
    public static boolean verifyPacketChecksum(byte[] packetMessage)
        {
        int checksum = (packetMessage[1] & 0xF7);
        for(int i = 2; i < packetMessage.length - 2; i++)
            {
            checksum = checksum ^ (packetMessage[i] & 0xF7);
            }
        return (checksum == (packetMessage[packetMessage.length - 2] & 0xF7));
        }
        
/** Assuming that the message provided is a MIDI File Dump Packet message, returns the decoded packet data. */
    public static byte[] getPacketData(byte[] packetMessage)
        {
        int len = (packetMessage.length - 2 - 7);                       // encoded byte length
        byte[] data = new byte[
            len / 8 * 7 + 
            (len % 8 == 0 ? 0 : len % 8 - 1)];
        
        int highByte = 7;               // position of high byte in message
        int current = 0;                // position of byte in decoded data
        int pos = 6;                    // where am I in the high byte
        for(int i = highByte + 1; i < packetMessage.length - 2; i++)                    // go over by 7 potentially
            {
            data[current] = (byte)(packetMessage[i] | ((packetMessage[highByte] >>> pos) << 7));
            current++;
            pos--;
            if (pos < 0)
                {
                highByte += 8;
                i++;            // skip
                pos = 6;
                }
            }
        return data;
        }
        
/** Concatenate many byte arrays into a single array.  
    This is useful for rebuilding the final file out of a bunch of payloads. */
    public static byte[] concatenate(ArrayList<byte[]> data)
        {
        // Compute necessary final size
        int size = 0;
        for(byte[] d : data)
            {
            size += d.length;
            }
        
        // copy into new array
        byte[] concat = new byte[size];
        int pos = 0;
        for(byte[] d : data)
            {
            System.arraycopy(d, 0, concat, pos, d.length);
            pos += d.length;
            }
                
        return concat;
        }

/** Sanity Test: Generate a MIDI File Dump, read it back in, compare the two. */
    public static void main(String[] args)
        {
        byte[] payload = new byte[992933];              // some random big value
        for(int i = 0; i < payload.length; i++) payload[i] = (byte)(i % 256);
        String name = "Hello, World!";
        String type = FILE_TYPE_BIN;
        int senderID = 92;		// random number
        int receiverID = 41;	// random number

        byte[][] messages = dump(senderID, receiverID, type, name, payload);
        
        ArrayList<byte[]> data = new ArrayList();
        
        // test header
        if (recognize(messages[0]) != MESSAGE_TYPE_HEADER) { System.err.println("Header not recognized, was: " + recognize(messages[0])); return; }
        if (senderID != getHeaderSenderID(messages[0])) { System.err.println("Header SenderID wrong, was: " + getHeaderSenderID(messages[0])); return; }
        if (receiverID != getHeaderReceiverID(messages[0])) { System.err.println("Header ReceiverID wrong, was: " + getHeaderReceiverID(messages[0])); return; }
        if (!type.equals(getHeaderType(messages[0]))) { System.err.println("Header Type wrong, was: " + getHeaderType(messages[0])); return; }
        if (!name.equals(getHeaderName(messages[0]))) { System.err.println("Header Name wrong, was: " + getHeaderName(messages[0])); return; }
        if (payload.length != getHeaderLength(messages[0])) { System.err.println("Header Length wrong, was: " + getHeaderLength(messages[0])); return; }

        // test packet metadata
        for(int i = 1; i < messages.length; i++)
            {
            if (recognize(messages[i]) != (i - 1) % 128) { System.err.println("Packet " + (i - 1) + " has wrong ID, was: " + recognize(messages[i])); return; }
            if (receiverID != getPacketReceiverID(messages[i])) { System.err.println("Packet " + (i - 1) + " ReceiverID wrong, was: " + getPacketReceiverID(messages[i])); return; }
            if (!verifyPacketChecksum(messages[i])) { System.err.println("Packet " + (i - 1) + " Checksum invalid."); return; }
            data.add(getPacketData(messages[i]));
            }

        // test concatenated packet data
        byte[] decoded = concatenate(data);
        
        if (decoded.length != payload.length)
            {
            System.err.println("Decoded data wrong length, was: " + decoded.length + ", should have been: " + payload.length);
            return;
            }

        for(int i = 0; i < decoded.length; i++)
            {
            if (decoded[i] != payload[i])
                {
                System.err.println("Decoded data wrong starting at position: " + i + " ( " + decoded[i] + " ) ");
                return;
                }
            }
                
        System.err.println("Test passed!");
        }
        
        
    }


