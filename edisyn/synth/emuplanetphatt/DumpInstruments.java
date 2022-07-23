import java.io.*;

public class DumpInstruments
    {
    public static void main(String[] args) throws IOException
        {
        BufferedInputStream str = new BufferedInputStream(System.in);
        for(int i = 0; i < 5; i++) 
            str.read();     // get rid of header
                
        int inst = 0;
        while(true)
            {
            int lsb = str.read();
            if (lsb == 0xF7) 
                break;  // we're done
            int msb = str.read();
            byte[] nm = new byte[11];
            str.read(nm);
            char[] nmc = new char[11];
            for(int i = 0; i < 11; i++)
                nmc[i] = (char) nm[i];
            String name = new String(nmc);
            int pos = (msb << 7) | lsb;
            System.out.println("" + (++inst) + "\t" + pos + "\t" + name);
            str.read();             // null-terminated
            }
        }
    }
