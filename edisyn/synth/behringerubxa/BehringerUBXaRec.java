package edisyn.synth.behringerubxa;

import edisyn.Recognize;

import java.util.Arrays;


public class BehringerUBXaRec extends Recognize {

    public static final byte[] SysExHeader = {
            (byte) 0xF0, (byte) 0x00, (byte) 0x20, (byte) 0x32,
            (byte) 0x00, (byte) 0x01, (byte) 0x21, (byte) 0x7F, // TODO - specify device (7f means all)
            (byte)0x74,(byte)0x07
    };

    public static final byte[] EOF = {
            (byte) 0xF0, (byte) 0x7E, (byte) 0x00, (byte) 0x7B, (byte) 0x00, (byte) 0xF7
    };

    public static boolean msgStartsWith(byte[] msg, byte[] o){
        return Arrays.equals(Arrays.copyOfRange(msg, 0, o.length), o);
    }

    public static boolean recognize(byte[] data){
        return msgStartsWith(data, SysExHeader) ||
                (msgStartsWith(data, EOF) && data.length == EOF.length);
    }
}
