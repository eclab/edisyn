/**
	Copyright 2020 by Sean Luke
	Licensed under the Apache License version 2.0
*/

package edisyn.synth.dsiprophet08;
import edisyn.*;

public class DSIProphet08Rec extends Recognize
	{
    public static final byte PROPHET_08_ID = 0x23;
    public static final byte MOPHO_ID = 0x25;
    public static final byte TETRA_ID = 0x26;
    public static final byte MOPHO_KEYBOARD_ID = 0x27;      // includes SE
    public static final byte MOPHO_X4_ID = 0x29;

    public static boolean recognize(byte[] data)
        {
        return ((data.length == 446 &&
                data[0] == (byte)0xF0 &&
                data[1] == (byte) 0x01 &&       // DSI
                (data[2] == PROPHET_08_ID ||  data[2] == TETRA_ID) &&
                data[3] == (byte) 0x02) ||      // Program Data Dump
                (data.length == 444 &&
                data[0] == (byte)0xF0 &&
                data[1] == (byte) 0x01 &&       // DSI
                (data[2] == PROPHET_08_ID ||  data[2] == TETRA_ID) &&
                data[3] == (byte) 0x03) ||      // Edit Buffer Data Dump
                (data.length == 300 &&
                data[0] == (byte)0xF0 &&
                data[1] == (byte) 0x01 &&       // DSI
                (data[2] == MOPHO_ID || data[2] == MOPHO_KEYBOARD_ID || data[2] == MOPHO_X4_ID) &&
                data[3] == (byte) 0x02) ||      // Program Data Dump
                (data.length == 298 &&
                data[0] == (byte)0xF0 &&
                data[1] == (byte) 0x01 &&       // DSI
                (data[2] == MOPHO_ID || data[2] == MOPHO_KEYBOARD_ID || data[2] == MOPHO_X4_ID) &&
                data[3] == (byte) 0x03));       // Edit Buffer Data Dump        
        }
	}