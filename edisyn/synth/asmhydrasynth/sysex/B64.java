import java.util.*;
import java.io.*;

public class B64
	{
	static byte[] base64DecodeChunk(byte[] chars, int start)
		{
		int result = 0;
		int equals = 0;
		for(int i = start; i < start + 4; i++)
			{
			int c = chars[i];
			if (c >= 'A' && c <= 'Z')
				{
				c = c - 'A' + 0;
				}
			else if (c >= 'a' && c <= 'z')
				{
				c = c - 'a' + 26;
				}
			else if (c >= '0' && c <= '9')
				{
				c = c - '0' + 52;
				}
			else if (c == '+')
				{
				c = 62;
				}
			else if (c == '/')
				{
				c = 63;
				}
			else if (c == '=')
				{
				c = 0;
				equals++;
				}
			else 
				{
				System.err.println("Unknown encoding char " + ((char)c) + "(" + c + "), setting to 0");
				c = 0;
				}
			
			result = (result << 6) | c;
			}
			
		if (equals == 0)
			{
			return new byte[]
				{
				(byte)((result >>> 16) & 255),
				(byte)((result >>> 8) & 255),
				(byte)((result >>> 0) & 255),
				};
			}
		else if (equals == 1)
			{
			return new byte[]
				{
				(byte)((result >>> 8) & 255),
				(byte)((result >>> 0) & 255),
				};
			}
		else if (equals == 2)
			{
			return new byte[]
				{
				(byte)((result >>> 0) & 255),
				};
			}
		else
			{
			// uh...
			System.err.println("Too many Equals signs!");
			return new byte[0];
			}
		}

	public static byte[] base64Decode(byte[] chars)
		{
		int pos = 0;
		byte[] data = new byte[chars.length];	// should be enough
		
		outer: for(int i = 0; i < chars.length; i+=4)
			{
			for(int j = i; j < i + 4; j++)
				{
				byte c = chars[i];
				if (!(	(c >= 'A' && c <= 'Z') || 
						(c >= 'a' && c <= 'z') || 
						(c >= '0' && c <= '9') || 
						c == '+' || 
						c == '/' || 
						c == '='))
					continue outer;
				}
			// at this point we have a legal 4-byte chunk
			byte[] vals = base64DecodeChunk(chars, i);
			System.arraycopy(vals, 0, data, pos, vals.length);
			pos += vals.length;
			}
			
		// Now reduce to the right size
		byte[] result = new byte[pos];
		System.arraycopy(data, 0, result, 0, pos);
		return result;
		}
	
	
	
	public static void main(String[] args) throws IOException
		{
		if (args.length == 1)
			{
			read(new FileInputStream(new File(args[0])), false);
			}
		else if (args.length == 0)
			{
			read(System.in, false);
			}
		else
			{
			if (args[0].equals("-v"))
				{
				byte[] c = new byte[args.length - 1];
				for(int i = 1; i < args.length; i++)
					{
					c[i - 1] = (byte)(Integer.parseInt(args[i], 16));
					}
				read(new ByteArrayInputStream(c), false);
				}
			else
				{
				byte[] c = new byte[args.length];
				for(int i = 0; i < args.length; i++)
					{
					c[i] = (byte)(Integer.parseInt(args[i], 16));
					}
				read(new ByteArrayInputStream(c), true);
				System.out.println();
				}
			}
		}
		
	public static void read(InputStream stream, boolean inline)
		{
		int[] chars = new int[4];
		int counter = 0;
		int line = 0;
		
		try
			{
			// Skip first six bytes F0 00 20 2B 00 6F
			for(int i = 0; i < 6; i++) stream.read();
			
			int c = 0;
			while(true)
				{
				c = stream.read();
				if (c < 0) break;
				if ((c >= 'A' && c <= 'Z') || 
					(c >= 'a' && c <= 'z') || 
					(c >= '0' && c <= '9') || 
					c == '+' || 
					c == '/' || 
					c == '=')
					{
					chars[counter++] = c;
					if (counter == 4)
						{
						counter = 0;
						line = process(line, chars, inline);
						}
					}
				else
					{
					// ignore
					}
				}
			}
		catch (IOException ex)
			{
			try { stream.close(); }
			catch (IOException ex2) { }
			}
		}
		
	public static int process(int line, int[] chars, boolean inline)
		{
		int result = 0;
		int equals = 0;
		for(int i = 0; i < chars.length; i++)
			{
			int c = chars[i];
			if (c >= 'A' && c <= 'Z')
				{
				c = c - 'A' + 0;
				}
			else if (c >= 'a' && c <= 'z')
				{
				c = c - 'a' + 26;
				}
			else if (c >= '0' && c <= '9')
				{
				c = c - '0' + 52;
				}
			else if (c == '+')
				{
				c = 62;
				}
			else if (c == '/')
				{
				c = 63;
				}
			else if (c == '=')
				{
				c = 0;
				equals++;
				}
			else 
				{
				System.err.println("Unknown encoding char " + ((char)c) + "(" + c + "), setting to 0");
				c = 0;
				}
			
			result = (result << 6) | c;
			}
			
		if (equals == 0)
			{
			int a0 = (result & 255);
			int a1 = ((result >>> 8) & 255);
			int a2 = ((result >>> 16) & 255);
			line = print(line, a2, inline);
			line = print(line, a1, inline);
			line = print(line, a0, inline);
			return line;
			}
		else if (equals == 1)
			{
			int a1 = ((result >>> 8) & 255);
			int a2 = ((result >>> 16) & 255);
			line = print(line, a2, inline);
			line = print(line, a1, inline);
			return line;
			}
		else if (equals == 2)
			{
			int a2 = ((result >>> 16) & 255);
			line = print(line, a2, inline);
			return line;
			}
		else
			{
			// uh...
			System.err.println("Too many Equals signs");
			return line;
			}
		}
		
	public static int print(int line, int val, boolean inline)
		{
		if (inline)
			{
			System.out.print(String.format("%02X", val) + " ");
			return line + 1;
			}
		else
			{
			System.out.print("" + line + " " + String.format("%02X", val));
			if (val == 32) System.out.println("\tSPACE");
			else if (val >= 32 && val < 127) System.out.println("\t" + (char)val);
			else System.out.println();
			return line + 1;
			}
		}
	}
	