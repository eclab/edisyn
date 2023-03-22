/***
    Copyright 2023 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.asmhydrasynth;
import edisyn.util.*;
import java.util.zip.*;
import java.util.*;

public class Encode
	{
	public static byte[][] encodePatch(byte[] data)
		{
		byte[][] patch = new byte[22][];
		int a = 0;
		for(int i = 0; i < data.length; i += 128)
			{
			int len = Math.min(128, data.length - i);
			byte[] payload = new byte[len + 4];
			payload[0] = (byte)0x16;
			payload[1] = (byte)0x00;
			payload[2] = (byte)a;
			payload[3] = (byte)0x16;
			System.arraycopy(data, i, payload, 4, len);
			patch[a] = encodePayload(payload);
			a++;
			}
		return patch;
		}
		
	public static byte[] encodePayload(byte[] data)
		{
		// Compute the checksum
		CRC32 crc = new CRC32();
		crc.update(data);
		int val = (int)(crc.getValue());
		byte a = (byte)(255 - ((val >>> 24) & 255));
		byte b = (byte)(255 - ((val >>> 16) & 255));
		byte c = (byte)(255 - ((val >>> 8) & 255));
		byte d = (byte)(255 - ((val >>> 0) & 255));
		
		// Load the checksum and the data into the payload
		byte[] payload = new byte[data.length + 4];
		payload[0] = d;
		payload[1] = c;
		payload[2] = b;
		payload[3] = a;
		/*
		System.err.println("Encoded CRC Bytes: " 
		+ StringUtility.toHex(payload[0]) + " " 
		+ StringUtility.toHex(payload[1]) + " " 
		+ StringUtility.toHex(payload[2]) + " " 
		+ StringUtility.toHex(payload[3])); 
		*/
		System.arraycopy(data, 0, payload, 4, data.length);
		
		// Base64 encode the payload
		byte[] encoded = base64Encode(payload);
		
		// Load the encoded data into the sysex message
		byte[] sysex = new byte[7 + encoded.length];
		sysex[0] = (byte)0xF0;
		sysex[1] = (byte)0x00;
		sysex[2] = (byte)0x20;
		sysex[3] = (byte)0x2B;
		sysex[4] = (byte)0x00;
		sysex[5] = (byte)0x6F;
		System.arraycopy(encoded, 0, sysex, 6, encoded.length);
		sysex[sysex.length - 1] = (byte)0xF7;
		
		return sysex;
		}
	
	public static byte[] base64Encode(byte[] data)
		{
		return Base64.getEncoder().encode(data);
		}
	
	public static void main(String[] args)
		{
		System.err.println("Testing \"Sawexpressive GD\"");
		System.err.println("Decoding original sysex, then re-encoding");
		byte[][] patch = encodePatch(Decode.decodePatch(Decode.SAWEXPRESSIVE));
		if (patch.length != Decode.SAWEXPRESSIVE.length)
			{
			System.err.println("Number of messages do not match");
			System.exit(-1);
			}
		else
			{
			for(int i = 0; i < patch.length; i++)
				{
				if (patch[i].length != Decode.SAWEXPRESSIVE[i].length)
					{
					System.err.println("Message " + i + " wrong length.\nGot:\n" + 
						StringUtility.toHex(patch[i]) + "\nExpected:\n" + 
						StringUtility.toHex(Decode.SAWEXPRESSIVE[i]));
					System.exit(-1);
					}
				else
					{
					for(int j = 0; j < patch[i].length; j++)
						{
						if (patch[i][j] != Decode.SAWEXPRESSIVE[i][j])
							{
							System.err.println("Message " + i + " doesn't match.\nGot:\n" + 
								StringUtility.toHex(patch[i]) + "\nExpected:\n" + 
								StringUtility.toHex(Decode.SAWEXPRESSIVE[i]));
							System.exit(-1);
							}
						}
					}
				}
			System.err.println("Patches Match");
			}
		}
	}