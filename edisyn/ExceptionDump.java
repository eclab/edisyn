package edisyn;
import java.awt.*;
import java.io.*;

public class ExceptionDump
	{
	static Throwable lastThrowable = null;
	static String auxillary = null;
	
	public static boolean lastThrowableExists() { return (lastThrowable != null); }
	
	public static void postThrowable(Throwable th)
		{
		postThrowable(th, null);
		}
		
	public static void postThrowable(Throwable th, String aux)
		{
		lastThrowable = th;
		auxillary = aux;
		}
		
	public static void saveThrowable(Synth synth, Throwable th)
		{
		postThrowable(th);
		saveThrowable(synth);
		}

	public static void saveThrowable(Synth synth, Throwable th, String auxillary)
		{
		postThrowable(th, auxillary);
		saveThrowable(synth);
		}

	public static void saveThrowable(Throwable th)
		{
		postThrowable(th);
		saveThrowable();
		}

	public static void saveThrowable(Throwable th, String auxillary)
		{
		postThrowable(th, auxillary);
		saveThrowable();
		}

	public static void saveThrowable()
		{
		saveThrowable((Synth)null);
		}

	public static void saveThrowable(Synth synth)
		{
		if (lastThrowable != null)
			{
        	FileDialog fd = new FileDialog((Frame)null, "Save Error File...", FileDialog.SAVE);
            fd.setFile("EdisynError.txt");
            
			if (synth != null)
				synth.disableMenuBar();
			fd.setVisible(true);
			if (synth != null)
				synth.enableMenuBar();
			
			File f = null; // make compiler happy
			PrintStream ps = null;
			if (fd.getFile() != null)
				{
				try
					{
					f = new File(fd.getDirectory(), Synth.ensureFileEndsWith(fd.getFile(), ".txt"));
					ps = new PrintStream(new FileOutputStream(f));
					if (auxillary != null)
						ps.println(auxillary);
					lastThrowable.printStackTrace(ps);
					ps.close();
					} 
				catch (IOException e) // fail
					{
					if (synth != null) synth.showSimpleError("File Error", "An error occurred while saving to the file " + (f == null ? " " : f.getName()));
					e.printStackTrace();
					}
				finally
					{
					if (ps != null)
						ps.close();
					}
				}
			}
		lastThrowable = null;
		}
	}
	
	