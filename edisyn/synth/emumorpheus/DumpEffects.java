public class DumpEffects
    {
    public static void main(String[] args) throws java.io.IOException
        {
        for(int i = 0; i < 5; i++) System.in.read();
        int numA = (int)(System.in.read() + System.in.read() * 128);
        System.out.println("----A----");
        for(int a = 0; a < numA; a++)
            {
            System.out.print((int)(System.in.read() + System.in.read() * 128));
            System.out.print("\t");
            for(int i = 0; i < 12; i++)
                System.out.print(Character.toString((char)(System.in.read())));
            //System.in.read();
            int num = (int)(System.in.read() + System.in.read() * 128);
            System.out.print("\t");
            for(int q = 0; q < num; q++)
                {
                for(int i = 0; i < 11; i++)
                    System.out.print(Character.toString((char)(System.in.read())));
                //System.in.read();
                System.out.print("\t");
                System.out.print((int)(System.in.read() + System.in.read() * 128));
                System.out.print("\t");
                System.out.print((int)(System.in.read() + System.in.read() * 128));
                System.out.print("\t");
                System.out.print((int)(System.in.read() + System.in.read() * 128));
                System.out.print("\t");
                }
            System.out.println();
            }
            
        int numB = (int)(System.in.read() + System.in.read() * 128);
        System.out.println("----B----");
        for(int a = 0; a < numB; a++)
            {
            System.out.print((int)(System.in.read() + System.in.read() * 128));
            System.out.print("\t");
            for(int i = 0; i < 12; i++)
                System.out.print(Character.toString((char)(System.in.read())));
            //System.in.read();
            int num = (int)(System.in.read() + System.in.read() * 128);
            System.out.print("\t");
            for(int q = 0; q < num; q++)
                {
                for(int i = 0; i < 11; i++)
                    System.out.print(Character.toString((char)(System.in.read())));
                //System.in.read();
                System.out.print("\t");
                System.out.print((int)(System.in.read() + System.in.read() * 128));
                System.out.print("\t");
                System.out.print((int)(System.in.read() + System.in.read() * 128));
                System.out.print("\t");
                System.out.print((int)(System.in.read() + System.in.read() * 128));
                System.out.print("\t");
                }
            System.out.println();
            }
        }
    }
