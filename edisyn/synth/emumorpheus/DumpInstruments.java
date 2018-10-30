public class DumpInstruments
    {
    public static void main(String[] args) throws java.io.IOException
        {
        for(int i = 0; i < 7; i++) System.in.read();
        while(true)
            {
            System.out.print((int)(System.in.read() + System.in.read() * 128));
            System.out.print("\t");
            for(int i = 0; i < 12; i++)
                System.out.print(Character.toString((char)(System.in.read())));
            //System.out.print("\t" + (int)(System.in.read() + System.in.read() * 128));
            System.out.println();
            }
        }
    }
