/***
    Copyright 2018 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.casiocz;

import edisyn.*;
import edisyn.gui.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.border.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.sound.midi.*;


// velocity (line1 line2)       PMAL, PMWL, PMPL, PSAL, PSWL, PSPL
// level (dca1 dca2)    MAMD, MAMV, MWMD, MWMV
// name

/**
   A patch editor for the Casio CZ Series.
        
   @author Sean Luke
*/




/***
    ABOUT BANKS

    CZ-1    CZ-101/1K       CZ-3K/5K
    1-8             A               Preset          Preset          A
    9-16    B               Preset          Preset          B
    17-24   C                                       Preset          C
    25-32   D                                       Preset          D
    33-40   E               Internal        Internal        A
    41-48   F               Internal        Internal        B
    49-56   G                                       Internal        C
    56-64   H                                       Internal        D
    65-72                   Cartridge
    73-80                   Cartridge

***/


public class CasioCZ extends Synth
    {
    /// Various collections of parameter names for pop-up menus
        
    public static final String[] BANKS = new String[] { "A", "B", "C", "D", "E", "F", "G", "H", "CA", "CB", "SA", "SB", "SC" };
    public static final String[] OCTAVE = new String[] { "0", "+1", "-1" };        
    public static final String[] LINE_SELECT = new String[] { "1", "2", "1+1'", "1+2'"};        
    public static final String[] VIBRATO_WAVES = new String[] { "Triangle", "Saw Up", "Saw Down", "Square" };
    public static final String[] ENVELOPES = new String[] { "Oscillator and Pitch Envelope (DCO) 1", "Waveform Envelope (DCW) 1", "Amplifier Envelope (DCA) 1", "Oscillator and Pitch Envelope (DCO) 2", "Waveform Envelope (DCW) 2", "Amplifier Envelope (DCA) 2",  };
    public static final String[] MODULATION = new String[] { "Off", "Off (Again)", "Ring 2", "Noise", "Ring", "Ring (Again)", "Ring 3", "Noise 2" };  // Note that these are out of order, and 2 are missing
    public static final String[] WAVEFORMS = new String[] { "Saw", "Square", "Pulse", "Null", "Sine-Pulse", "Saw-Pulse", "Multi-Sine", "2x Pulse" };
    public static final String[] WINDOWS = new String[] { "None", "Saw", "Triangle", "Trapezoid", "Pulse", "2x Saw" };  // note that the last window is repeated 3 times

    public static final int DCO = 1;
    public static final int DCW = 2;
    public static final int DCA = 3;

    public static final int[][] VIBRATO_DELAY_TIME_ENCODING = new int[][] { { 0x00, 0x00, 0x00 }, { 0x01, 0x00, 0x01 }, { 0x02, 0x00, 0x02 }, { 0x03, 0x00, 0x03 }, { 0x04, 0x00, 0x04 }, { 0x05, 0x00, 0x05 }, { 0x06, 0x00, 0x06 }, { 0x07, 0x00, 0x07 }, { 0x08, 0x00, 0x08 }, { 0x09, 0x00, 0x09 }, { 0x0A, 0x00, 0x0A }, { 0x0B, 0x00, 0x0B }, { 0x0C, 0x00, 0x0C }, { 0x0D, 0x00, 0x0D }, { 0x0E, 0x00, 0x0E }, { 0x0F, 0x00, 0x0F }, { 0x10, 0x00, 0x10 }, { 0x11, 0x00, 0x11 }, { 0x12, 0x00, 0x12 }, { 0x13, 0x00, 0x13 }, { 0x14, 0x00, 0x14 }, { 0x15, 0x00, 0x15 }, { 0x16, 0x00, 0x16 }, { 0x17, 0x00, 0x17 }, { 0x18, 0x00, 0x18 }, { 0x19, 0x00, 0x19 }, { 0x1A, 0x00, 0x1A }, { 0x1B, 0x00, 0x1B }, { 0x1C, 0x00, 0x1C }, { 0x1D, 0x00, 0x1D }, { 0x1E, 0x00, 0x1E }, { 0x1F, 0x00, 0x1F }, { 0x20, 0x00, 0x21 }, { 0x21, 0x00, 0x23 }, { 0x22, 0x00, 0x25 }, { 0x23, 0x00, 0x27 }, { 0x24, 0x00, 0x29 }, { 0x25, 0x00, 0x2B }, { 0x26, 0x00, 0x2D }, { 0x27, 0x00, 0x2F }, { 0x28, 0x00, 0x31 }, { 0x29, 0x00, 0x33 }, { 0x2A, 0x00, 0x35 }, { 0x2B, 0x00, 0x37 }, { 0x2C, 0x00, 0x39 }, { 0x2D, 0x00, 0x3B }, { 0x2E, 0x00, 0x3D }, { 0x2F, 0x00, 0x3F }, { 0x30, 0x00, 0x43 }, { 0x31, 0x00, 0x47 }, { 0x32, 0x00, 0x4B }, { 0x33, 0x00, 0x4F }, { 0x34, 0x00, 0x53 }, { 0x35, 0x00, 0x57 }, { 0x36, 0x00, 0x5B }, { 0x37, 0x00, 0x5F }, { 0x38, 0x00, 0x63 }, { 0x39, 0x00, 0x67 }, { 0x3A, 0x00, 0x6B }, { 0x3B, 0x00, 0x6F }, { 0x3C, 0x00, 0x73 }, { 0x3D, 0x00, 0x77 }, { 0x3E, 0x00, 0x7B }, { 0x3F, 0x00, 0x7F }, { 0x40, 0x00, 0x87 }, { 0x41, 0x00, 0x8F }, { 0x42, 0x00, 0x97 }, { 0x43, 0x00, 0x9F }, { 0x44, 0x00, 0xA7 }, { 0x45, 0x00, 0xAF }, { 0x46, 0x00, 0xB7 }, { 0x47, 0x00, 0xBF }, { 0x48, 0x00, 0xC7 }, { 0x49, 0x00, 0xCF }, { 0x4A, 0x00, 0xD7 }, { 0x4B, 0x00, 0xDF }, { 0x4C, 0x00, 0xE7 }, { 0x4D, 0x00, 0xEF }, { 0x4E, 0x00, 0xF7 }, { 0x4F, 0x00, 0xFF }, { 0x50, 0x01, 0x0F }, { 0x51, 0x01, 0x1F }, { 0x52, 0x01, 0x2F }, { 0x53, 0x01, 0x3F }, { 0x54, 0x01, 0x4F }, { 0x55, 0x01, 0x5F }, { 0x56, 0x01, 0x6F }, { 0x67, 0x01, 0x7F }, { 0x58, 0x01, 0x8F }, { 0x59, 0x01, 0x9F }, { 0x5A, 0x01, 0xAF }, { 0x5B, 0x01, 0xBF }, { 0x5C, 0x01, 0xCF }, { 0x5D, 0x01, 0xDF }, { 0x5E, 0x01, 0xEF }, { 0x5F, 0x01, 0xFF }, { 0x60, 0x02, 0x1F }, { 0x61, 0x02, 0x3F }, { 0x62, 0x02, 0x5F }, { 0x63, 0x02, 0x7F } };
    public static final int[][] VIBRATO_RATE_ENCODING = new int[][] { { 0x00, 0x00, 0x20 }, { 0x01, 0x00, 0x40 }, { 0x02, 0x00, 0x60 }, { 0x03, 0x00, 0x80 }, { 0x04, 0x00, 0xA0 }, { 0x05, 0x00, 0xC0 }, { 0x06, 0x00, 0xE0 }, { 0x07, 0x00, 0x00 }, { 0x08, 0x01, 0x20 }, { 0x09, 0x01, 0x40 }, { 0x0A, 0x01, 0x60 }, { 0x0B, 0x01, 0x80 }, { 0x0C, 0x01, 0xA0 }, { 0x0D, 0x01, 0xC0 }, { 0x0E, 0x01, 0xE0 }, { 0x0F, 0x02, 0x00 }, { 0x10, 0x02, 0x20 }, { 0x11, 0x02, 0x40 }, { 0x12, 0x02, 0x60 }, { 0x13, 0x02, 0x80 }, { 0x14, 0x02, 0xA0 }, { 0x15, 0x02, 0xC0 }, { 0x16, 0x03, 0xE0 }, { 0x17, 0x03, 0x00 }, { 0x18, 0x03, 0x20 }, { 0x19, 0x03, 0x40 }, { 0x1A, 0x03, 0x60 }, { 0x1B, 0x03, 0x80 }, { 0x1C, 0x03, 0xA0 }, { 0x1D, 0x03, 0xC0 }, { 0x1E, 0x03, 0xE0 }, { 0x1F, 0x04, 0x00 }, { 0x20, 0x04, 0x60 }, { 0x21, 0x04, 0xA0 }, { 0x22, 0x04, 0xE0 }, { 0x23, 0x05, 0x20 }, { 0x24, 0x05, 0x60 }, { 0x25, 0x05, 0xA0 }, { 0x26, 0x05, 0xE0 }, { 0x27, 0x06, 0x20 }, { 0x28, 0x06, 0x60 }, { 0x29, 0x06, 0xA0 }, { 0x2A, 0x06, 0xE0 }, { 0x2B, 0x07, 0x20 }, { 0x2C, 0x07, 0x60 }, { 0x2D, 0x07, 0xA0 }, { 0x2E, 0x07, 0xE0 }, { 0x2F, 0x08, 0x20 }, { 0x30, 0x08, 0xE0 }, { 0x31, 0x09, 0x60 }, { 0x32, 0x09, 0xE0 }, { 0x33, 0x0A, 0x60 }, { 0x34, 0x0A, 0xE0 }, { 0x35, 0x0B, 0x60 }, { 0x36, 0x0B, 0xE0 }, { 0x37, 0x0C, 0x60 }, { 0x38, 0x0C, 0xE0 }, { 0x39, 0x0D, 0x60 }, { 0x3A, 0x0D, 0xE0 }, { 0x3B, 0x0E, 0x60 }, { 0x3C, 0x0E, 0xE0 }, { 0x3D, 0x0F, 0x60 }, { 0x3E, 0x0F, 0xE0 }, { 0x3F, 0x10, 0x60 }, { 0x40, 0x11, 0xE0 }, { 0x41, 0x12, 0xE0 }, { 0x42, 0x13, 0xE0 }, { 0x43, 0x14, 0xE0 }, { 0x44, 0x15, 0xE0 }, { 0x45, 0x16, 0xE0 }, { 0x46, 0x17, 0xE0 }, { 0x47, 0x18, 0xE0 }, { 0x48, 0x19, 0xE0 }, { 0x49, 0x1A, 0xE0 }, { 0x4A, 0x1B, 0xE0 }, { 0x4B, 0x1C, 0xE0 }, { 0x4C, 0x1D, 0xE0 }, { 0x4D, 0x1E, 0xE0 }, { 0x4E, 0x1F, 0xE0 }, { 0x4F, 0x20, 0xE0 }, { 0x50, 0x23, 0xE0 }, { 0x51, 0x25, 0xE0 }, { 0x52, 0x27, 0xE0 }, { 0x53, 0x29, 0xE0 }, { 0x54, 0x2B, 0xE0 }, { 0x55, 0x2D, 0xE0 }, { 0x56, 0x2F, 0xE0 }, { 0x57, 0x31, 0xE0 }, { 0x58, 0x33, 0xE0 }, { 0x59, 0x35, 0xE0 }, { 0x5A, 0x37, 0xE0 }, { 0x5B, 0x39, 0xE0 }, { 0x5C, 0x3B, 0xE0 }, { 0x5D, 0x3D, 0xE0 }, { 0x5E, 0x3F, 0xE0 }, { 0x5F, 0x41, 0xE0 }, { 0x60, 0x47, 0xE0 }, { 0x61, 0x4B, 0xE0 }, { 0x62, 0x4F, 0xE0 }, { 0x63, 0x53, 0xE0 } };
    public static final int[][] VIBRATO_DEPTH_ENCODING = new int[][] { { 0x00, 0x00, 0x01 }, { 0x01, 0x00, 0x02 }, { 0x02, 0x00, 0x03 }, { 0x03, 0x00, 0x04 }, { 0x04, 0x00, 0x05 }, { 0x05, 0x00, 0x06 }, { 0x06, 0x00, 0x07 }, { 0x07, 0x00, 0x08 }, { 0x08, 0x00, 0x09 }, { 0x09, 0x00, 0x0A }, { 0x0A, 0x00, 0x0B }, { 0x0B, 0x00, 0x0C }, { 0x0C, 0x00, 0x0D }, { 0x0D, 0x00, 0x0E }, { 0x0E, 0x00, 0x0F }, { 0x0F, 0x00, 0x10 }, { 0x10, 0x00, 0x11 }, { 0x11, 0x00, 0x12 }, { 0x12, 0x00, 0x13 }, { 0x13, 0x00, 0x14 }, { 0x14, 0x00, 0x15 }, { 0x15, 0x00, 0x16 }, { 0x16, 0x00, 0x17 }, { 0x17, 0x00, 0x18 }, { 0x18, 0x00, 0x19 }, { 0x19, 0x00, 0x1A }, { 0x1A, 0x00, 0x1B }, { 0x1B, 0x00, 0x1C }, { 0x1C, 0x00, 0x1D }, { 0x1D, 0x00, 0x1E }, { 0x1E, 0x00, 0x1F }, { 0x1F, 0x00, 0x20 }, { 0x20, 0x00, 0x23 }, { 0x21, 0x00, 0x25 }, { 0x22, 0x00, 0x27 }, { 0x23, 0x00, 0x29 }, { 0x24, 0x00, 0x2B }, { 0x25, 0x00, 0x2D }, { 0x26, 0x00, 0x2F }, { 0x27, 0x00, 0x31 }, { 0x28, 0x00, 0x33 }, { 0x29, 0x00, 0x35 }, { 0x2A, 0x00, 0x37 }, { 0x2B, 0x00, 0x39 }, { 0x2C, 0x00, 0x3B }, { 0x2D, 0x00, 0x3D }, { 0x2E, 0x00, 0x3F }, { 0x2F, 0x00, 0x41 }, { 0x30, 0x00, 0x47 }, { 0x31, 0x00, 0x4B }, { 0x32, 0x00, 0x4F }, { 0x33, 0x00, 0x53 }, { 0x34, 0x00, 0x57 }, { 0x35, 0x00, 0x5B }, { 0x36, 0x00, 0x5F }, { 0x37, 0x00, 0x63 }, { 0x38, 0x00, 0x67 }, { 0x39, 0x00, 0x6B }, { 0x3A, 0x00, 0x6F }, { 0x3B, 0x00, 0x73 }, { 0x3C, 0x00, 0x77 }, { 0x3D, 0x00, 0x7B }, { 0x3E, 0x00, 0x7F }, { 0x3F, 0x00, 0x83 }, { 0x40, 0x00, 0x8F }, { 0x41, 0x00, 0x97 }, { 0x42, 0x00, 0x9F }, { 0x43, 0x00, 0xA7 }, { 0x44, 0x00, 0xAF }, { 0x45, 0x00, 0xB7 }, { 0x46, 0x00, 0xBF }, { 0x47, 0x00, 0xC7 }, { 0x48, 0x00, 0xCF }, { 0x49, 0x00, 0xD7 }, { 0x4A, 0x00, 0xDF }, { 0x4B, 0x00, 0xE7 }, { 0x4C, 0x00, 0xEF }, { 0x4D, 0x00, 0xF7 }, { 0x4E, 0x00, 0xFF }, { 0x4F, 0x01, 0x07 }, { 0x50, 0x01, 0x1F }, { 0x51, 0x01, 0x2F }, { 0x52, 0x01, 0x3F }, { 0x53, 0x01, 0x4F }, { 0x54, 0x01, 0x5F }, { 0x55, 0x01, 0x6F }, { 0x56, 0x01, 0x7F }, { 0x57, 0x01, 0x8F }, { 0x58, 0x01, 0x9F }, { 0x59, 0x01, 0xAF }, { 0x5A, 0x01, 0xBF }, { 0x5B, 0x01, 0xCF }, { 0x5C, 0x01, 0xDF }, { 0x5D, 0x01, 0xEF }, { 0x5E, 0x01, 0xFF }, { 0x5F, 0x02, 0x0F }, { 0x60, 0x02, 0x3F }, { 0x61, 0x02, 0x5F }, { 0x62, 0x02, 0x7F }, { 0x63, 0x03, 0x00 } };

    // Notice that the ordering is backwards [maybe?].  This isn't documented
    public static final int sysexToVibratoDelayTime(int high, int med, int low)
        {
        for(int i = 0; i < VIBRATO_DELAY_TIME_ENCODING.length; i++)
            if (VIBRATO_DELAY_TIME_ENCODING[i][0] == low &&
                VIBRATO_DELAY_TIME_ENCODING[i][1] == med &&
                VIBRATO_DELAY_TIME_ENCODING[i][2] == high)
                return i;
        return -1;
        }

    // Notice that the ordering is backwards.  This isn't documented
    public static final int sysexToVibratoRate(int high, int med, int low)
        {
        for(int i = 0; i < VIBRATO_RATE_ENCODING.length; i++)
            if (VIBRATO_RATE_ENCODING[i][0] == low &&
                VIBRATO_RATE_ENCODING[i][1] == med &&
                VIBRATO_RATE_ENCODING[i][2] == high)
                return i;
        return -1;
        }

    // Notice that the ordering is backwards.  This isn't documented
    public static final int sysexToVibratoDepth(int high, int med, int low)
        {
        for(int i = 0; i < VIBRATO_DEPTH_ENCODING.length; i++)
            if (VIBRATO_DEPTH_ENCODING[i][0] == low &&
                VIBRATO_DEPTH_ENCODING[i][1] == med &&
                VIBRATO_DEPTH_ENCODING[i][2] == high)
                return i;
        return -1;
        }

/*
  public static final int[] DCA_RATE_ENCODING = new int[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0C, 0x0D, 0x0E, 0x0F, 0x10, 0x12, 0x13, 0x14, 0x15, 0x16, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1E, 0x1F, 0x20, 0x21, 0x22, 0x24, 0x25, 0x26, 0x27, 0x28, 0x2A, 0x2B, 0x2C, 0x2D, 0x2E, 0x30, 0x31, 0x32, 0x33, 0x34, 0x36, 0x37, 0x38, 0x39, 0x3A, 0x3C, 0x3D, 0x3E, 0x3F, 0x40, 0x42, 0x43, 0x44, 0x45, 0x46, 0x48, 0x49, 0x4A, 0x4B, 0x4C, 0x4E, 0x4F, 0x50, 0x51, 0x52, 0x54, 0x55, 0x56, 0x57, 0x58, 0x5A, 0x5B, 0x5C, 0x5D, 0x5E, 0x60, 0x61, 0x62, 0x63, 0x64, 0x66, 0x67, 0x68, 0x69, 0x6A, 0x6C, 0x6D, 0x6E, 0x6F, 0x70, 0x72, 0x73, 0x74, 0x75, 0x77 };
  public static final int[] DCA_LEVEL_ENCODING = new int[] { 0x00, 0x1D, 0x1E, 0x1F, 0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2A, 0x2B, 0x2C, 0x2D, 0x2E, 0x2F, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3A, 0x3B, 0x3C, 0x3D, 0x3E, 0x3F, 0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4A, 0x4B, 0x4C, 0x4D, 0x4E, 0x4F, 0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5A, 0x5B, 0x5C, 0x5D, 0x5E, 0x5F, 0x60, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6A, 0x6B, 0x6C, 0x6D, 0x6E, 0x6F, 0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7A, 0x7B, 0x7C, 0x7D, 0x7E, 0x7F }; 
  public static final int[] DCO_RATE_ENCODING = new int[] { 0x00, 0x01, 0x02, 0x03, 0x05, 0x06, 0x07, 0x08, 0x0A, 0x0B, 0x0C, 0x0E, 0x0F, 0x10, 0x11, 0x13, 0x14, 0x15, 0x17, 0x18, 0x19, 0x1A, 0x1C, 0x1D, 0x1E, 0x20, 0x21, 0x22, 0x23, 0x25, 0x26, 0x27, 0x29, 0x2A, 0x2B, 0x2C, 0x2E, 0x2F, 0x30, 0x32, 0x33, 0x34, 0x35, 0x37, 0x38, 0x39, 0x3B, 0x3C, 0x3D, 0x3E, 0x40, 0x41, 0x42, 0x44, 0x45, 0x46, 0x47, 0x49, 0x4A, 0x4B, 0x4C, 0x4E, 0x4F, 0x50, 0x52, 0x53, 0x54, 0x55, 0x57, 0x58, 0x59, 0x5B, 0x5C, 0x5D, 0x5E, 0x60, 0x61, 0x62, 0x64, 0x65, 0x66, 0x67, 0x69, 0x6A, 0x6B, 0x6D, 0x6E, 0x6F, 0x70, 0x72, 0x73, 0x74, 0x76, 0x77, 0x78, 0x79, 0x7B, 0x7C, 0x7D, 0x7F };
  public static final int[] DCO_LEVEL_ENCODING = new int[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F, 0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2A, 0x2B, 0x2C, 0x2D, 0x2E, 0x2F, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3A, 0x3B, 0x3C, 0x3D, 0x3E, 0x3F, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4A, 0x4B, 0x4C, 0x4D, 0x4E, 0x4F, 0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5A, 0x5B, 0x5C, 0x5D, 0x5E, 0x5F, 0x60, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67 }; 
  public static final int[] DCW_RATE_ENCODING = new int[] { 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0E, 0x0F, 0x10, 0x11, 0x12, 0x14, 0x15, 0x16, 0x17, 0x18, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x20, 0x21, 0x22, 0x23, 0x24, 0x26, 0x27, 0x28, 0x29, 0x2A, 0x2C, 0x2D, 0x2E, 0x2F, 0x30, 0x32, 0x33, 0x34, 0x35, 0x36, 0x38, 0x39, 0x3A, 0x3B, 0x3C, 0x3E, 0x3F, 0x40, 0x41, 0x42, 0x44, 0x45, 0x46, 0x47, 0x48, 0x4A, 0x4B, 0x4C, 0x4D, 0x4E, 0x50, 0x51, 0x52, 0x53, 0x54, 0x56, 0x57, 0x58, 0x59, 0x5A, 0x5C, 0x5D, 0x5E, 0x5F, 0x60, 0x62, 0x63, 0x64, 0x65, 0x66, 0x68, 0x69, 0x6A, 0x6B, 0x6C, 0x6E, 0x6F, 0x70, 0x71, 0x72, 0x74, 0x75, 0x76, 0x77, 0x78, 0x7A, 0x7B, 0x7C, 0x7D, 0x7F };
  public static final int[] DCW_LEVEL_ENCODING = new int[] { 0x00, 0x01, 0x02, 0x03, 0x05, 0x06, 0x07, 0x08, 0x0A, 0x0B, 0x0C, 0x0E, 0x0F, 0x10, 0x11, 0x13, 0x14, 0x15, 0x17, 0x18, 0x19, 0x1A, 0x1C, 0x1D, 0x1E, 0x20, 0x21, 0x22, 0x23, 0x25, 0x26, 0x27, 0x29, 0x2A, 0x2B, 0x2C, 0x2E, 0x2F, 0x30, 0x32, 0x33, 0x34, 0x35, 0x37, 0x38, 0x39, 0x3B, 0x3C, 0x3D, 0x3E, 0x40, 0x41, 0x42, 0x44, 0x45, 0x46, 0x47, 0x49, 0x4A, 0x4B, 0x4C, 0x4E, 0x4F, 0x50, 0x52, 0x53, 0x54, 0x55, 0x57, 0x58, 0x59, 0x5B, 0x5C, 0x5D, 0x5E, 0x60, 0x61, 0x62, 0x64, 0x65, 0x66, 0x67, 0x69, 0x6A, 0x6B, 0x6D, 0x6E, 0x6F, 0x70, 0x72, 0x73, 0x74, 0x76, 0x77, 0x78, 0x79, 0x7B, 0x7C, 0x7D, 0x7F };
  public static final int[][] LEVEL_ENCODINGS = new int[][] { DCO_LEVEL_ENCODING, DCW_LEVEL_ENCODING, DCA_LEVEL_ENCODING, DCO_LEVEL_ENCODING, DCW_LEVEL_ENCODING, DCA_LEVEL_ENCODING };
  public static final int[][] RATE_ENCODINGS = new int[][] { DCO_RATE_ENCODING, DCW_RATE_ENCODING, DCA_RATE_ENCODING, DCO_RATE_ENCODING, DCW_RATE_ENCODING, DCA_RATE_ENCODING };
*/

    public static final int[][] DCA_KEY_FOLLOW = new int[][] {{0x00, 0x00}, {0x01, 0x08}, {0x02, 0x11}, {0x03, 0x1A}, {0x04, 0x24}, {0x05, 0x2F}, {0x06, 0x3A}, {0x07, 0x45}, {0x08, 0x52}, {0x09, 0x5F}};
    public static final int[][] DCW_KEY_FOLLOW = new int[][] {{0x00, 0x00}, {0x01, 0x1F}, {0x02, 0x2C}, {0x03, 0x39}, {0x04, 0x46}, {0x05, 0x53}, {0x06, 0x60}, {0x07, 0x6E}, {0x08, 0x92}, {0x09, 0xFF}};
    public static final int[][] DCW_KEY_FOLLOW_CZ1 = new int[][] {{0x00, 0x00}, {0x01, 0x19}, {0x02, 0x33}, {0x03, 0x4E}, {0x04, 0x6A}, {0x05, 0x86}, {0x06, 0xA3}, {0x07, 0xC1}, {0x08, 0xDF}, {0x09, 0xFF}};

    public static final int NO_SUSTAIN_STEP = 0;

/// dca1 level                                  1-15    [backwards!]
/// line 1 velocity (amp)               0-15
/// line 1 velocity (wave)              0-15    [backwards!]
/// line 1 velocity (pitch)             0-15    [backwards!]
//// ... same for dca2?
//// Name                                       16 long!        A-Z 0-9 . - / SPACE


    boolean cz1;
    public static final String CZ_1_KEY = "CZ-1";
    
    public boolean isCZ1() { return cz1; }
    public void setCZ1(boolean val)
        {
        setLastX("" + (!val), CZ_1_KEY, getSynthName(), true);
        cz1 = val;
        updateTitle();
        }
    

    public CasioCZ()
        {
        int panel = 0;
        
        /*
          for(int i = 0; i < parameters.length; i++)
          {
          parametersToIndex.put(parameters[i], Integer.valueOf(i));
          }
        */
                        
        String m = getLastX(CZ_1_KEY, getSynthName());
        cz1 = (m == null ? false : !Boolean.parseBoolean(m));
        
        /// SOUND PANEL
                
        SynthPanel soundPanel = new SynthPanel(this);
        
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addGlobal(Style.COLOR_B()));
        vbox.add(hbox);
        
        vbox.add(addEnvelope(1, DCO, Style.COLOR_A()));
        vbox.add(addEnvelope(1, DCW, Style.COLOR_B()));
        vbox.add(addEnvelope(1, DCA, Style.COLOR_C()));
        
        soundPanel.add(vbox, BorderLayout.CENTER);
        soundPanel.makePasteable("line1");
        soundPanel.setSendsAllParameters(false);
        addTab("Global and Line 1", soundPanel);

        soundPanel = new SynthPanel(this);
        
        vbox = new VBox();
        
        vbox.add(addEnvelope(2, DCO, Style.COLOR_A()));
        vbox.add(addEnvelope(2, DCW, Style.COLOR_B()));
        vbox.add(addEnvelope(2, DCA, Style.COLOR_C()));
        
        soundPanel.add(vbox, BorderLayout.CENTER);
        soundPanel.makePasteable("line2");
        soundPanel.setSendsAllParameters(false);
        addTab("Line 2", soundPanel);
        
        model.set("name", "UNTITLED");
        model.set("bank", 0);
        model.set("number", 0);
                
        loadDefaults();        
        }
                
    public String getDefaultResourceFileName() { return "CasioCZ.init"; }
    public String getHTMLResourceFileName() { return "CasioCZ.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(BANKS);
        
        // figure bank
        int mb = model.get("bank", 0);
        int b = Synth.getLastXAsInt("lastbank", getSynthNameLocal(), -1, true);
        if (b == 12)	// CZ230S Writable Bank
        	{
        	bank.setSelectedIndex(b);
        	}
        else
        	{
        	bank.setSelectedIndex(mb);
        	}
        
        int num = model.get("number") + 1;
        JTextField number = new JTextField("" + num, 3);
        
        String instructions = "<html>Banks vary depending on device.<p>There are 8 patches per bank, except SC." +
            "<p><table><tr><td><b color=black>Bank</b></td><td><b color=black>CZ-1</b></td><td><b color=black>CZ-101/1K</b></td><td><b color=black>CZ-3K/5K</b></td><td><b color=black>CZ-230S</b></td></tr>" +
            "<tr><td>A</td><td>A</td><td>Preset 1-8     </td><td>Preset A  </td>0-7</tr>" +
            "<tr><td>B</td><td>B</td><td>Preset 9-16    </td><td>Preset B  </td>8-15</tr>" +
            "<tr><td>C</td><td>C</td><td>               </td><td>Preset C  </td>16-23</tr>" +
            "<tr><td>D</td><td>D</td><td>               </td><td>Preset D  </td>24-31</tr>" +
            "<tr><td>E</td><td>E</td><td>Internal 1-8   </td><td>Internal A</td>32-39</tr>" +
            "<tr><td>F</td><td>F</td><td>Internal 9-16  </td><td>Internal B</td>40-47</tr>" +
            "<tr><td>G</td><td>G</td><td>               </td><td>Internal C</td>48-55</tr>" + 
            "<tr><td>H</td><td>H</td><td>               </td><td>Internal D</td>56-63</tr>" +
            "<tr><td>CA</td><td> </td><td>Cartridge 1-8 </td><td>          </td>64-71</tr>" +
            "<tr><td>CB</td><td> </td><td>Cartridge 9-16</td><td>          </td>72-79</tr>" +
            "<tr><td>SA</td><td> </td><td>              </td><td>          </td>80-87</tr>" +
            "<tr><td>SB</td><td> </td><td>              </td><td>          </td>88-95</tr>" +
            "<tr><td>SC</td><td> </td><td>              </td><td>          </td>96-99</tr>" +
            "</table><p></html>";
                        
        while(true)
            {
            boolean result = showMultiOption(this, new String[] { "Bank", "Patch Number"}, 
                new JComponent[] { bank, number }, title, instructions);
                
            if (result == false) 
                return false;
                                
            int n;
            try { n = Integer.parseInt(number.getText()); }
            catch (NumberFormatException e)
                {
                if (bank.getSelectedIndex() == 12) // SC
                    showSimpleError(title, "In Bank SC, the Patch Number must be an integer 1...4");
                else
                    showSimpleError(title, "The Patch Number must be an integer 1...8");
                continue;
                }
            if (n < 1 || n > 8 || (bank.getSelectedIndex() == 12 && n > 4))
                {
                if (bank.getSelectedIndex() == 12) // SC
                    showSimpleError(title, "In Bank SC, the Patch Number must be an integer 1...4");
                else
                    showSimpleError(title, "The Patch Number must be an integer 1...8");
                continue;
                }
                
            n--;
                                
            int i = bank.getSelectedIndex();
                        
            change.set("bank", i);
            change.set("number", n);
        	Synth.setLastX("" + i, "lastbank", getSynthNameLocal(), true);
                        
            return true;
            }
        }
        
    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, getSynthName(), color);
        //globalCategory.makeUnresettable();
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                                
        VBox vbox = new VBox();
        
        HBox hbox2 = new HBox();
        comp = new PatchDisplay(this, 8);
        hbox2.add(comp);

        final JCheckBox check = new JCheckBox("CZ-1");
        check.setSelected(cz1);
        check.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                setCZ1(check.isSelected());
                }
            });
        check.setFont(Style.SMALL_FONT());
        check.setOpaque(false);
        check.setForeground(Style.TEXT_COLOR());
        hbox2.add(check);
        hbox.addLast(Stretch.makeHorizontalStretch());
        vbox.add(hbox2);
        
        vbox.add(hbox2);
        
        comp = new StringComponent("Patch Name [CZ-1]", this, "name", 16, "Name must be up to 16 characters.")
            {
            public String replace(String val)
                {
                return revisePatchName(val);
                }
                                
            public void update(String key, Model model)
                {
                super.update(key, model);
                updateTitle();
                }
            };
        vbox.add(comp);
        hbox.add(vbox);
        
        //hbox.add(Strut.makeHorizontalStrut(75));

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }
                
    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addGlobal(Color color)
        {
        Category globalCategory = new Category(this, "Global", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        VBox vbox = new VBox();
        params = LINE_SELECT;
        comp = new Chooser("Line Select", this, "lineselect", params);
        vbox.add(comp);

        params = OCTAVE;
        comp = new Chooser("Octave", this, "octave", params);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        params = VIBRATO_WAVES;
        comp = new Chooser("Vibrato Wave", this, "vibratowave", params);
        vbox.add(comp);
        
        params = MODULATION;
        comp = new Chooser("Modulation", this, "modulation", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Vibrato Delay", this, "vibratodelaytime", color, 0, 99);
        hbox.add(comp);

        comp = new LabelledDial("Vibrato Rate", this, "vibratorate", color, 0, 99);
        hbox.add(comp);

        comp = new LabelledDial("Vibrato Depth", this, "vibratodepth", color, 0, 99);
        hbox.add(comp);

        comp = new LabelledDial("Detune", this, "detune", color, -47, 47)
            {
            public String map(int val)
                {
                if (val == 0) return "--";
                int v = Math.abs(val);
                if (val < 0)
                    return "-" + (v / 12) + "-" + (v % 12);
                else
                    return "+" + (v / 12) + "+" + (v % 12);
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Fine", this, "fine", color, 0, 60);
        hbox.add(comp);
        
        vbox = new VBox();
        comp = new CheckBox("Mute Line 1", this, "muteline1");
        vbox.add(comp);
        hbox.add(vbox);

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }
        
    
    JComponent waveformChooserExample = null;
    /** Add wave envelope category */
    public JComponent addEnvelope(int line, int env, Color color)
        {
        final String envelope = "line" + line + "env" + env;
        
        Category category = new Category(this, ENVELOPES[(line - 1) * 3 + env - 1], color);
        category.makeDistributable(envelope);
        category.makePasteable(envelope);
                                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
 
        if (env == DCO)
            {
            VBox vbox = new VBox();
            params = WAVEFORMS;
            waveformChooserExample = new Chooser("Waveform 1", this, envelope + "waveform1", params);
            vbox.add(waveformChooserExample);
            params = WAVEFORMS;
            waveformChooserExample = new Chooser("Waveform 2", this, envelope + "waveform2", params);
            vbox.add(waveformChooserExample);
            comp = new CheckBox("Waveform 2 On", this, envelope + "waveform2on");
            vbox.add(comp);
            params = WINDOWS;
            comp = new Chooser("Window", this, envelope + "window", params);
            vbox.add(comp);
            hbox.add(vbox);
            }
        else
            {
            VBox vbox = new VBox();
            HBox h = new HBox();
            comp = new LabelledDial("Key Follow", this, envelope + "keyfollow", color, 0, 9, 0);
            h.add(comp);
            vbox.add(h);
            vbox.add(Strut.makeStrut(waveformChooserExample));
            hbox.add(vbox);
            }
                      
        VBox vbox = new VBox();                 
        comp = new LabelledDial("Velocity [CZ-1]", this, envelope + "velocity", color, 0, 15);
        vbox.add(comp);
        if (env == DCA)
            {
            comp = new LabelledDial("Level [CZ-1]", this, envelope + "mainlevel", color, 1, 15);
            vbox.add(comp);
            }
        hbox.add(vbox);

        vbox = new VBox();                 
        comp = new LabelledDial(" End Step ", this, envelope + "end", color, 0, 7, -1);
        vbox.add(comp);

        comp = new LabelledDial("Sustain Step", this, envelope + "sustain", color, 0, 8 )
            {
            public String map(int val)
                {
                if (val == NO_SUSTAIN_STEP)
                    return "None";
                else return "" + val;
                }
            };
        vbox.add(comp);
        hbox.add(vbox);
        hbox.add(Strut.makeHorizontalStrut(10));
        
        int maxRate = 99;
        int maxLevel = 99;
        
        // a few envelopes actually go above 99! Not available on the front panel and not documented of course.
        
        if (env == DCA)
            {
            maxRate = 106;
            }
        if (env == DCO)
            {
            maxLevel = 123;
            }

        for(int i = 1; i < 9; i++)
            {
            vbox = new VBox();
            comp = new LabelledDial("Rate " + i, this, envelope + "rate" + i, color, 0, maxRate);
            vbox.add(comp);
            comp = new LabelledDial("Level " + i, this, envelope + "level" + i, color, 0, maxLevel);
            vbox.add(comp);
            hbox.add(vbox);
            }

/*
  EnvelopeDisplay disp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
  new String[] { null, envelope + "rate1", envelope + "rate2", envelope + "rate3", envelope + "rate4", envelope + "rate5", envelope + "rate6", envelope + "rate7", envelope + "rate8",  },
  new String[] { null, envelope + "level1", envelope + "level2", envelope + "level3", envelope + "level4", envelope + "level5", envelope + "level6", envelope + "level7", envelope + "level8",  },
  new double[] { 0, 1.0 / 8, 1.0 / 8, 1.0 / 8, 1.0 / 8, 1.0 / 8, 1.0 / 8, 1.0 / 8, 1.0 / 8 },
  new double[] { 0, 1.0 / maxLevel, 1.0 / maxLevel, 1.0 / maxLevel, 1.0 / maxLevel, 1.0 / maxLevel, 1.0 / maxLevel, 1.0 / maxLevel, 1.0 / maxLevel },
  new double[] { 0, (Math.PI/4/maxRate), (Math.PI/4/maxRate), (Math.PI/4/maxRate), (Math.PI/4/maxRate), (Math.PI/4/maxRate), (Math.PI/4/maxRate), (Math.PI/4/maxRate), (Math.PI/4/maxRate) })
  {
  public int postProcessLoopOrStageKey(String key, int val)
  {
  if (key.equals(envelope + "end"))
  return val + 1;
  else return val;
  }
  };
  disp.setFinalStageKey(envelope + "end");
  disp.setSustainStageKey(envelope + "sustain");
  disp.setPreferredWidth((int)(disp.getPreferredWidth() * 1.5));
*/

        EnvelopeDisplay disp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, envelope + "rate1", envelope + "rate2", envelope + "rate3", envelope + "rate4", envelope + "rate5", envelope + "rate6", envelope + "rate7", envelope + "rate8",  },
            new String[] { null, envelope + "level1", envelope + "level2", envelope + "level3", envelope + "level4", envelope + "level5", envelope + "level6", envelope + "level7", envelope + "level8",  },
            new double[] { 0, 1.0 / maxRate / 8.0, 1.0 / maxRate / 8.0, 1.0 / maxRate / 8.0, 1.0 / maxRate / 8.0, 1.0 / maxRate / 8.0, 1.0 / maxRate / 8.0, 1.0 / maxRate / 8.0, 1.0 / maxRate / 8.0 },
            new double[] { 0, 1.0 / maxLevel, 1.0 / maxLevel, 1.0 / maxLevel, 1.0 / maxLevel, 1.0 / maxLevel, 1.0 / maxLevel, 1.0 / maxLevel, 1.0 / maxLevel })
            {
            public void postProcess(double[] xVals, double[] yVals)
                {
                // The CZ uses 99 for SHORT and 0 for LONG, weird
                for(int i = 1; i < 9; i++)
                    xVals[i] = 1.0 / 8 - xVals[i];
                }
            public int postProcessLoopOrStageKey(String key, int val)
                {
                if (key.equals(envelope + "end"))
                    return val + 1;
                else return val;
                }
            };
        disp.setFinalStageKey(envelope + "end");
        disp.setSustainStageKey(envelope + "sustain");
        disp.setPreferredWidth((int)(disp.getPreferredWidth() * 1.5));
            
        hbox.addLast(disp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
    
    public int dcaAllLevelToSysex(int alpha)
        {
        // The conversion is:
        // SYSEX     ACTUAL
        // 14            1
        // ...
        // 0             15
        return 15 - alpha;
        }
        
    public int sysexToDCAAllLevel(int alpha)
        {
        return 15 - alpha;
        }
                        
    public int dcoRateToSysex(int alpha)
        {
        return (alpha * 127) / 99;
        }
    
    public int sysexToDCORate(int beta)
        {
        if (beta == 0) return 0;
        else if (beta == 127) return 99;
        else return (beta * 99) / 127 + 1;
        }
        
    public int dcoLevelToSysex(int alpha)
        {
        if (alpha > 63) return alpha + 4;
        else return alpha;
        }
    
    public int sysexToDCOLevel(int beta)
        {
        if (beta > 63) return beta - 4;
        else return beta;
        }
        
    public int dcwRateToSysex(int alpha)
        {
        return (alpha * 119) / 99 + 8;
        }
        
    public int sysexToDCWRate(int beta)
        {
        if (beta == 8) return 0;
        else if (beta == 127) return 99;
        else return ((beta - 8) * 99) / 119 + 1;
        }
        
    public int dcwLevelToSysex(int alpha)
        {
        return (alpha * 127) / 99;
        }
        
    public int sysexToDCWLevel(int beta)
        {
        if (beta == 0) return 0;
        else if (beta == 127) return 99;
        else return (beta * 99) / 127 + 1;
        }

    public int dcaRateToSysex(int alpha)
        {
        return (alpha * 119) / 99;
        }
        
    public int sysexToDCARate(int beta)
        {
        if (beta == 0) return 0;
        else if (beta == 119) return 99;
        else return (beta * 99) / 119 + 1;
        }
        
    public int dcaLevelToSysex(int alpha)
        {
        // the documentation in http://www.youngmonkey.ca/nose/audio_tech/synth/Casio-CZ.html
        // appears to be wrong.   It appears that the level is simply a difference of 28 except when 0.  
        if (alpha == 0) return 0;
        else return alpha + 28;
        }
        
    public int sysexToDCALevel(int beta)
        {
        // the documentation in http://www.youngmonkey.ca/nose/audio_tech/synth/Casio-CZ.html
        // appears to be wrong.   It appears that the level is simply a difference of 28 except when 0.  
        if (beta == 0) return 0;
        else return beta - 28;
        }
    
    public static String getSynthName() { return "Casio CZ"; }
 
    public void changePatch(Model tempModel)
        {
        int bank = tempModel.get("bank");
        int number = tempModel.get("number");
        int pc = bank * 8 + number;
        tryToSendMIDI(buildPC(getChannelOut(), pc));
        model.set("bank", bank);
        model.set("number", number);
        }

    public int unnybblize(byte lsb, byte msb)
        {
        int v  = ((lsb & 0xF) | (msb << 4));
        if (v < 0) v += 256;
        // System.err.println("nyb: " + Integer.toHexString(lsb) + " " + Integer.toHexString(msb) + " -> " + v);
        return v;
        }
                
    public byte[] nybblize(int val)
        {
        byte lsb = (byte)(val & 0xF);
        byte msb = (byte)((val >>> 4) & 0xF);
        // System.err.println("nyb: " + val + " -> " + Integer.toHexString(lsb) + " " + Integer.toHexString(msb));
        return new byte[] { lsb, msb };
        }

    public int nybblize(int val, byte[] to, int pos)
        {
        byte[] nybbles = nybblize(val);
        to[pos] = nybbles[0];
        to[pos+1] = nybbles[1];
        return pos + 2;
        }

    public int parse(byte[] data, boolean fromFile)
        {
        // ugh, CZ sysex is the *worst*.
        
        boolean cz1 = false;
        int pos = 6;
        int high = 0;
        int low = 0;
        int medium = 0;
        int val = 0;
        
        // There are four possible formats, with four different lengths.  Here we recognize them by length.
        
        // CZ-1's "Send Request 1"
        // F0 44 00 00 7N 30 [BREAK] [256 bytes] F7
        // Send Request from CZ-101/1K/3K/5K
        // N = Channel
        
        // CZ-1's "Receive Request 1"
        // F0 44 00 00 7N 20 DD [BREAK] [256 bytes] F7
        // Receive Request from CZ-101/1K/3K/5K
        // N = Channel
        // DD = memory bank

        // CZ-1's "Send Request 2"
        // F0 44 00 00 7N 30 [BREAK] [288 bytes] F7
        // Send Request from CZ-1               // Note identical, but longer
        // N = Channel
        
        // CZ-1's "Receive Request 2"
        // F0 44 00 00 7N 21 DD [BREAK] [288 bytes] F7
        // Receive Request from CZ-1
        // N = Channel
        // DD = memory bank
        
        if (data.length == 7 + 144 * 2)
            {
// we assume we've already changed the bank and number
//            model.set("bank", 0);
//            model.set("number", 0);
            cz1 = true;
            }
        else if (data.length == 8 + 144 * 2)
            {
            if (data[6] <= 80)
                {
                model.set("bank", data[6] / 8);
                model.set("number", data[6] % 8);
                }
            cz1 = true;
            pos = 7;
            }
        else if (data.length == 7 + 128 * 2)
            {
// we assume we've already changed the bank and number
//            model.set("bank", 0);
//            model.set("number", 0);
            cz1 = false;
            }
        else if (data.length == 8 + 128 * 2)
            {
            if (data[6] <= 80)
                {
                model.set("bank", data[6] / 8);
                model.set("number", data[6] % 8);
                }
            pos = 7;
            cz1 = false;
            }
        else
            {
            // Uh....
            return PARSE_CANCELLED; // We do this because the CZ is chatty in response to us, and we don't want that misinterpreted as a failed parse
            }
                
        // System.err.println("CZ1: " + cz1);
                
                
        // System.err.println("PFLAG " + pos);
        // PFLAG        (Line Select and Octave)
                
        int pflag = unnybblize(data[pos], data[pos + 1]);
        pos += 2;
        // System.err.println("pflag " + pflag);
        model.set("lineselect", (pflag & 3));
        model.set("octave", ((pflag >>> 2) & 3));
                
                
        // PDS          (Detune)
                
        // System.err.println("PDS " + pos);
        // this is used in PDETL, PDETH below
        int pds = unnybblize(data[pos], data[pos + 1]);
        pos += 2;
                
                
        // PDETL, PDETH (Detune Data)

        // Casio guidebook for Midi (page 23) says "Data is transmitted high byte first", but this is wrong
                
        // System.err.println("PDETL " + pos);
        low = unnybblize(data[pos], data[pos + 1]);
        pos += 2;
        high = unnybblize(data[pos], data[pos + 1]);
        pos += 2;
                
        low = low >>> 2;
        if (low >= 49) { low -= 3; }
        else if (low >= 33) { low -= 2; }
        else if (low >= 17) { low -= 1; }
        model.set("fine", low);
        model.set("detune", (pds == 0 ? high : -high));
                
                
        // PVK  (Vibrato Wave)
                
        // System.err.println("PVK " + pos);
        int pvk = unnybblize(data[pos], data[pos + 1]);
        pos += 2;
        if (((pvk >>> 3) & 1) == 1)
            model.set("vibratowave", 0);    // triangle
        else if (((pvk >>> 2) & 1) == 1)
            model.set("vibratowave", 1);    // saw up
        else if (((pvk >>> 5) & 1) == 1)
            model.set("vibratowave", 2);    // saw down
        else if (((pvk >>> 1) & 1) == 1)
            model.set("vibratowave", 3);    // square
                        
                        
        // PVDLD, PVDLV (Vibrato Delay Time)
                
        // System.err.println("PVDLD " + pos);
        low = unnybblize(data[pos], data[pos + 1]);
        pos += 2;
        high = unnybblize(data[pos], data[pos + 1]);
        pos += 2;
        medium = unnybblize(data[pos], data[pos + 1]);
        pos += 2;
        val = sysexToVibratoDelayTime(high, medium, low);
        if (val >=0)
            model.set("vibratodelaytime", val);
        else
            System.err.println("Unknown vibrato delay time encoding " + high + " " + medium + " " + low);
                
                
        // PVSD, PVSV   (Vibrato Rate)
                
        // System.err.println("PVSD " + pos);
        low = unnybblize(data[pos], data[pos + 1]);
        pos += 2;
        high = unnybblize(data[pos], data[pos + 1]);
        pos += 2;
        medium = unnybblize(data[pos], data[pos + 1]);
        pos += 2;
        val = sysexToVibratoRate(high, medium, low);
        if (val >=0)
            model.set("vibratorate", val);
        else
            System.err.println("Unknown vibrato rate encoding " + high + " " + medium + " " + low);
                
                
        // PVDD, PVDV   (Vibrato Depth)
                
        // System.err.println("PVDD " + pos);
        low = unnybblize(data[pos], data[pos + 1]);
        pos += 2;
        high = unnybblize(data[pos], data[pos + 1]);
        pos += 2;
        medium = unnybblize(data[pos], data[pos + 1]);
        pos += 2;
        val = sysexToVibratoDepth(high, medium, low);
        if (val >=0)
            model.set("vibratodepth", val);
        else
            System.err.println("Unknown vibrato depth encoding " + high + " " + medium + " " + low);
                
                
        for(int line = 1; line < 3; line++)             // line1 and line2
            {
            // System.err.println("MFW (" + line + ") " + pos);
            // MFW          (DCO Waveform, Modulation, Line Mute)           ENV 1

            high = unnybblize(data[pos], data[pos + 1]);            //  high first.  CZ-1 manual is wrong in its asterisk.
            pos += 2;
            low = unnybblize(data[pos], data[pos + 1]);
            pos += 2;
            val = (high << 8) | low;

            if (line == 1)
                {
                model.set("modulation", (val >>> 3) & 7);               // manual is limited in discussion.  See http://www.kasploosh.com/projects/CZ/11800-spelunking/8-modulation.html
                model.set("muteline1", (val >>> 2) & 1);                // not mentioned in manual.  See http://www.kasploosh.com/projects/CZ/11800-spelunking/8-modulation.html
                }
                                
            model.set("line" + line + "env1window", (val >>> 6) & 7);               // manual is limited in discussion.  See http://www.kasploosh.com/projects/CZ/11800-spelunking/1-which_bits.html
            model.set("line" + line + "env1waveform2", (val >>> 10) & 7);           // manual is limited in discussion.  See http://www.kasploosh.com/projects/CZ/11800-spelunking/1-which_bits.html
            model.set("line" + line + "env1waveform2on", (val >>> 9) & 1);          // manual is limited in discussion.  See http://www.kasploosh.com/projects/CZ/11800-spelunking/1-which_bits.html
            model.set("line" + line + "env1waveform1", (val >>> 13) & 7);           // manual is limited in discussion.  See http://www.kasploosh.com/projects/CZ/11800-spelunking/1-which_bits.html


            // MAMD, MAMV   (DCA Key Follow and Level)              ENV 3

            // System.err.println("MAMD (" + line + ") " + pos);
            low = unnybblize(data[pos], data[pos + 1]);
            pos += 2;
            high = unnybblize(data[pos], data[pos + 1]);
            pos += 2;

            if (cz1)
                {                       
                model.set("line" + line + "env3mainlevel", sysexToDCAAllLevel(low >>> 4));
                }
            else
                {
                model.set("line" + line + "env3mainlevel", 15);
                }

            // the low byte is complicated but it doesn't actually matter for parsing
            // we really only care about high, because it contains everything we need to know
                
            model.set("line" + line + "env3keyfollow", low & 0xF);
                
                
            // MWMD, MWMV   (DCW Key Follow)                ENV 2

            // System.err.println("MWMD (" + line + ") " + pos);
            low = unnybblize(data[pos], data[pos + 1]);
            pos += 2;
            high = unnybblize(data[pos], data[pos + 1]);
            pos += 2;

            // cz1 differs from the normal here, but they're the same in their third nybble,
            // so I'l just use that.
                
            model.set("line" + line + "env2keyfollow", low & 0xF);


            // PMAL         (DCA Envelope End Step and Velocity)            ENV 3
                
            // System.err.println("PMAL (" + line + ") " + pos);
            val = unnybblize(data[pos], data[pos + 1]);
            pos += 2;
                
            if (cz1)
                {
                // Unlike the others, here the velocity is a direct mapping
                model.set("line" + line + "env3velocity", val >>> 4);
                }
            else
                {
                model.set("line" + line + "env3velocity", 0);
                }
                        
            model.set("line" + line + "env3end", val & 0xf);
                
                
            // PMA  (DCA Envelope)          ENV 3
                
            // System.err.println("PMA (" + line + ") " + pos);
            model.set("line" + line + "env3sustain", NO_SUSTAIN_STEP);
            for(int i = 0; i < 8; i++)
                {
                int rate = unnybblize(data[pos], data[pos + 1]);
                pos += 2;
                int level = unnybblize(data[pos], data[pos + 1]);
                pos += 2;
                        
                model.set("line" + line + "env3rate" + (i + 1), sysexToDCARate(rate & 127));
                        
                // The CZ has a bizarre flag indicating whether the
                // level is going up or down relative to the previous
                // level.  We can ignore it here but we'll need to set it 
                // when we're emitting.

                model.set("line" + line + "env3level" + (i + 1), sysexToDCALevel(level & 127));
                                                
                if (level >>> 7 == 1)
                    {
                    model.set("line" + line + "env3sustain", (i + 1));
                    }
                }


            // PMWL         (DCW Envelope End Step and Velocity)            ENV 2
                
            // System.err.println("PMWL (" + line + ") " + pos);
            val = unnybblize(data[pos], data[pos + 1]);
            pos += 2;
                
            if (cz1)
                {
                // Note inversion from manual -- CORRECTION, according to Icaro Ferre (icaroferre on github) manual is incorrect.
                model.set("line" + line + "env2velocity", val >>> 4); // (15 - (val >>> 4)));
                }
            else
                {
                model.set("line" + line + "env2velocity", 0);
                }
                        
            model.set("line" + line + "env2end", val & 0xf);
                
                
            // PMW  (DCW Envelope)          ENV 2
                
            // System.err.println("PMW (" + line + ") " + pos);
            model.set("line" + line + "env2sustain", NO_SUSTAIN_STEP);
            for(int i = 0; i < 8; i++)
                {
                int rate = unnybblize(data[pos], data[pos + 1]);
                pos += 2;
                int level = unnybblize(data[pos], data[pos + 1]);
                pos += 2;
                        
                model.set("line" + line + "env2rate" + (i + 1), sysexToDCWRate(rate & 127));
                        
                // The CZ has a bizarre flag indicating whether the
                // level is going up or down relative to the previous
                // level.  We can ignore it here but we'll need to set it 
                // when we're emitting.

                model.set("line" + line + "env2level" + (i + 1), sysexToDCWLevel(level & 127));
                        
                if (level >>> 7 == 1)
                    {
                    model.set("line" + line + "env2sustain", (i + 1));
                    }
                }


            // PMPL         (DCO Envelope End Step and Velocity)            ENV 1
                
            // System.err.println("PMPL (" + line + ") " + pos);
            val = unnybblize(data[pos], data[pos + 1]);
            pos += 2;
                
            if (cz1)
                {
                // Note inversion from manual -- CORRECTION, according to Icaro Ferre (icaroferre on github) manual is incorrect.
                model.set("line" + line + "env1velocity", val >>> 4);   //(15 - (val >>> 4)));
                }
            else
                {
                model.set("line" + line + "env1velocity", 0);
                }
                        
            model.set("line" + line + "env1end", val & 0xf);
                
                
            // PMP  (DCO Envelope)          ENV 1
                
            // System.err.println("PMP (" + line + ") " + pos);
            model.set("line" + line + "env1sustain", NO_SUSTAIN_STEP);
            for(int i = 0; i < 8; i++)
                {
                int rate = unnybblize(data[pos], data[pos + 1]);
                pos += 2;
                int level = unnybblize(data[pos], data[pos + 1]);
                pos += 2;
                        
                model.set("line" + line + "env1rate" + (i + 1), sysexToDCORate(rate & 127));
                        
                // The CZ has a bizarre flag indicating whether the
                // level is going up or down relative to the previous
                // level.  We can ignore it here but we'll need to set it 
                // when we're emitting.

                model.set("line" + line + "env1level" + (i + 1), sysexToDCOLevel(level & 127));
                        
                if (level >>> 7 == 1)
                    {
                    model.set("line" + line + "env1sustain", (i + 1));
                    }
                }
            }
                
                
        // NAME
                
        if (cz1)
            {
            // System.err.println("NAME " + pos);
            byte[] name = new byte[16];
            for(int i = 0; i < 16; i++)
                {
                name[i] = (byte)unnybblize(data[pos], data[pos + 1]);
                pos+=2;
                }
            try
                {
                String n = new String(name, "US-ASCII");
                String n2 = revisePatchName(n);
                if (!n.equals(n2))
                    System.err.println("Invalid patch name.  Was \"" + n + "\", revised to \"" + n2 + "\"");
                model.set("name", n2);
                }
            catch (UnsupportedEncodingException e)
                {
                // never happens
                }
            }
        else
            {
            model.set("name", "UNTITLED");
            }
                        
        revise();
                        
        return PARSE_SUCCEEDED;
        }
        
    public String getPatchLocationName(Model model)
        {
        return BANKS[model.get("bank", 0)] + (model.get("number", 0) + 1);
        }
    
    public Model getNextPatchLocation(Model model)
        {
        int bank = model.get("bank");
        int number = model.get("number");
        
        number++;
        if (number >= 8 || 
            (number >= 4 && bank == 12))            // SC
            {
            bank++;
            number = 0;
            if (bank >= 12)
                bank = 0;
            }
                
        Model newModel = buildModel();
        newModel.set("bank", bank);
        newModel.set("number", number);
        return newModel;
        }
    
   
    public String getPatchName(Model model) 
        {
        return model.get("name", "UNTITLED");
        }

    public String revisePatchName(String name)
        {
        //name = super.revisePatchName(name);  // trim first time
        name = (name + "                ").substring(0, 16);
        
        StringBuffer nameb = new StringBuffer(name.toUpperCase());                            
        for(int i = 0 ; i < nameb.length(); i++)
            {
            char c = nameb.charAt(i);
            if (c >= 'A' && c <= 'Z')
                continue;
            else if (c >= '0' && c <= '9')
                continue;
            else if (c == '*' || c == '-' || c == '/' || c == ' ' || c == '.')
                continue;
            else
                nameb.setCharAt(i, ' ');
            }
        name = nameb.toString();
        return name;  // super.revisePatchName(name);  // trim again
        }
        
        
        
    //// IMPORTANT NOTE ABOUT THE CZ-230S
    //// To send to current memory in a CZ machine, you emit to "patch" 96 (0x60), which signifies current memory rather than a real patch.
    //// However on the CZ-230S, there actually *is* a patch 96: and furthermore it's one of only four patches that you can actually write to.
    //// This means that when you send to current working memory, it'll actually write to patch 96 on the CZ-230S, overwriting its current contents.
    //// This may be very confusing to CZ-230S users.
        
    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null) 
            tempModel = model;
            
        byte[] data = (isCZ1() ? new byte[8 + 144 * 2] : new byte[8 + 128 * 2]);
        
        data[0] = (byte)0xF0;
        data[1] = (byte)0x44;
        data[2] = (byte)0x00;
        data[3] = (byte)0x00;
        data[4] = (byte)(0x70 | getChannelOut());
        data[5] = (byte)(isCZ1() ? 0x21 : 0x20);
        data[6] = (byte)(toWorkingMemory ? 0x60 : (byte)(tempModel.get("bank") * 8 + tempModel.get("number")));
        
        int pos = 7;
        int val;
        int low;
        int high;


        // PFLAG        (Line Select and Octave)
                
        // System.err.println("PFLAG " + pos);
        val = model.get("lineselect") | (model.get("octave") << 2);
        pos = nybblize(val, data, pos);
                
                
        // PDS          (Detune)
                
        // System.err.println("PDS " + pos);
        val = (model.get("detune") >= 0 ? 0 : 1);
        pos = nybblize(val, data, pos);


        // PDETL, PDETH (Detune Data)
        // System.err.println("PDETL " + pos);
                
        // Casio guidebook for Midi (page 23) says "Data is transmitted high byte first", but this is wrong

        low = model.get("fine");
        if (low >= 46) low += 3;
        else if (low >= 31) low += 2;
        else if (low >= 16) low += 1;
        pos = nybblize(low << 2, data, pos);
                
        high = model.get("detune");
        if (high < 0) high = 0 - high;
        pos = nybblize(high, data, pos);
                

                
        // PVK  (Vibrato Wave)
        // System.err.println("PVK " + pos);
                
        val = model.get("vibratowave", 0);
        if (val == 0)  // triangle
            pos = nybblize(8, data, pos);
        else if (val == 1) // saw up
            pos = nybblize(4, data, pos);
        else if (val == 2) // saw down
            pos = nybblize(32, data, pos);
        else    // square
            pos = nybblize(2, data, pos);
                

                        
        // PVDLD, PVDLV (Vibrato Delay Time)
        // System.err.println("PVDLD " + pos);
                
        val = model.get("vibratodelaytime");
        pos = nybblize(VIBRATO_DELAY_TIME_ENCODING[val][0], data, pos);  // low
        pos = nybblize(VIBRATO_DELAY_TIME_ENCODING[val][2], data, pos);  // high
        pos = nybblize(VIBRATO_DELAY_TIME_ENCODING[val][1], data, pos);  // medium
                

                
        // PVSD, PVSV   (Vibrato Rate)
        // System.err.println("PVSD " + pos);
                
        val = model.get("vibratorate");
        pos = nybblize(VIBRATO_RATE_ENCODING[val][0], data, pos);  // low
        pos = nybblize(VIBRATO_RATE_ENCODING[val][2], data, pos);  // high
        pos = nybblize(VIBRATO_RATE_ENCODING[val][1], data, pos);  // medium
                

                
        // PVDD, PVDV   (Vibrato Depth)
        // System.err.println("PVDD " + pos);
                
        val = model.get("vibratodepth");
        pos = nybblize(VIBRATO_DEPTH_ENCODING[val][0], data, pos);  // low
        pos = nybblize(VIBRATO_DEPTH_ENCODING[val][2], data, pos);  // high
        pos = nybblize(VIBRATO_DEPTH_ENCODING[val][1], data, pos);  // medium
                


        for(int line = 1; line < 3; line++)             // line1 and line2
            {
            // MFW          (DCO Waveform, Modulation, Line Mute)           ENV 1
            // System.err.println("MFW (" + line + ") " + pos);

            val =   (model.get("line" + line + "env1window") << 6) |                // manual is limited in discussion.  See http://www.kasploosh.com/projects/CZ/11800-spelunking/1-which_bits.html
                (model.get("line" + line + "env1waveform2") << 10) |    // manual is limited in discussion.  See http://www.kasploosh.com/projects/CZ/11800-spelunking/1-which_bits.html
                (model.get("line" + line + "env1waveform2on") << 9) |   // manual is limited in discussion.  See http://www.kasploosh.com/projects/CZ/11800-spelunking/1-which_bits.html
                (model.get("line" + line + "env1waveform1") << 13);             // manual is limited in discussion.  See http://www.kasploosh.com/projects/CZ/11800-spelunking/1-which_bits.html
                                                
            if (line == 1)
                {
                val |=  (model.get("modulation") << 3);         // manual is limited in discussion.  See http://www.kasploosh.com/projects/CZ/11800-spelunking/8-modulation.html
                val |=  (model.get("muteline1") << 2);          // not mentioned in manual.  See http://www.kasploosh.com/projects/CZ/11800-spelunking/8-modulation.html
                }
                        
            low = val & 255;
            high = (val >>> 8) & 255;
            pos = nybblize(high, data, pos);                //  high first.  CZ-1 manual is wrong in its asterisk.
            pos = nybblize(low, data, pos);         //  high first.  CZ-1 manual is wrong in its asterisk.

                                                                        

            // MAMD, MAMV   (DCA Key Follow and Level)              ENV 3
            // System.err.println("MAMD (" + line + ") " + pos);

            low = DCA_KEY_FOLLOW[model.get("line" + line + "env3keyfollow")][0];
            high = DCA_KEY_FOLLOW[model.get("line" + line + "env3keyfollow")][1];
            if (cz1)
                {
                low |= (dcaAllLevelToSysex(model.get("line" + line + "env3mainlevel")) << 4);
                }
            pos = nybblize(low, data, pos);
            pos = nybblize(high, data, pos);
                        
                        
                
            // MWMD, MWMV   (DCW Key Follow)                ENV 2
            // System.err.println("MWMD (" + line + ") " + pos);

            if (cz1)
                {
                low = DCW_KEY_FOLLOW_CZ1[model.get("line" + line + "env2keyfollow")][0];
                high = DCW_KEY_FOLLOW_CZ1[model.get("line" + line + "env2keyfollow")][1];
                }
            else
                {
                low = DCA_KEY_FOLLOW[model.get("line" + line + "env2keyfollow")][0];
                high = DCW_KEY_FOLLOW[model.get("line" + line + "env2keyfollow")][1];
                }
                                
            pos = nybblize(low, data, pos);
            pos = nybblize(high, data, pos);



            // PMAL         (DCA Envelope End Step and Velocity)            ENV 3
            // System.err.println("PMAL (" + line + ") " + pos);
                
            val = model.get("line" + line + "env3end");
                        
            if (cz1)
                {
                val |= (model.get("line" + line + "env3velocity") << 4);
                }
                                
            pos = nybblize(val, data, pos);
                
                
                
            // PMA  (DCA Envelope)          ENV 3
            // System.err.println("PMA (" + line + ") " + pos);

            for(int i = 0; i < 8; i++)
                {
                int rate = dcaRateToSysex(model.get("line" + line + "env3rate" + (i + 1)));
                int level = dcaLevelToSysex(model.get("line" + line + "env3level" + (i + 1)));
                        
                // The CZ has a bizarre flag indicating whether the
                // level is going up or down relative to the previous
                // level.

                if (i > 0)
                    {
                    int curLevel = model.get("line" + line + "env3level" + (i + 1));
                    int prevLevel = model.get("line" + line + "env3level" + i);
                    if (curLevel < prevLevel)
                        {
                        rate |= 128;
                        }
                    }
                                        
                if (model.get("line" + line + "env3sustain") == (i + 1))
                    {
                    level |= 128;
                    }
                                        
                pos = nybblize(rate, data, pos);
                pos = nybblize(level, data, pos);
                }



            // PMWL         (DCW Envelope End Step and Velocity)            ENV 2           
            // System.err.println("PMWL (" + line + ") " + pos);
                
            val = model.get("line" + line + "env2end");
                        
            if (cz1)
                {
                // Note inversion from manual -- CORRECTION, according to Icaro Ferre (icaroferre on github) manual is incorrect.
                val |= (model.get("line" + line + "env2velocity") << 4); // (15 - model.get("line" + line + "env2velocity") << 4);
                }
                                
            pos = nybblize(val, data, pos);
                
                
                
            // PMW  (DCW Envelope)          ENV 2
            // System.err.println("PMW (" + line + ") " + pos);
                
            for(int i = 0; i < 8; i++)
                {
                int rate = dcwRateToSysex(model.get("line" + line + "env2rate" + (i + 1)));
                int level = dcwLevelToSysex(model.get("line" + line + "env2level" + (i + 1)));
                        
                // The CZ has a bizarre flag indicating whether the
                // level is going up or down relative to the previous
                // level.

                if (i > 0)
                    {
                    int curLevel = model.get("line" + line + "env2level" + (i + 1));
                    int prevLevel = model.get("line" + line + "env2level" + i);
                    if (curLevel < prevLevel)
                        {
                        rate |= 128;
                        }
                    }
                                        
                if (model.get("line" + line + "env2sustain") == (i + 1))
                    {
                    level |= 128;
                    }
                                        
                pos = nybblize(rate, data, pos);
                pos = nybblize(level, data, pos);
                }



            // PMPL         (DCO Envelope End Step and Velocity)            ENV 1
            // System.err.println("PMPL (" + line + ") " + pos);
                
            val = model.get("line" + line + "env1end");
                        
            if (cz1)
                {
                // Note inversion from manual -- CORRECTION, according to Icaro Ferre (icaroferre on github) manual is incorrect.
                val |= (model.get("line" + line + "env1velocity") << 4);   //(15 - model.get("line" + line + "env1velocity") << 4);
                }
                                
            pos = nybblize(val, data, pos);
                
                                
                
            // PMP  (DCO Envelope)          ENV 1
            // System.err.println("PMP (" + line + ") " + pos);

            for(int i = 0; i < 8; i++)
                {
                int rate = dcoRateToSysex(model.get("line" + line + "env1rate" + (i + 1)));
                int level = dcoLevelToSysex(model.get("line" + line + "env1level" + (i + 1)));
                        
                // The CZ has a bizarre flag indicating whether the
                // level is going up or down relative to the previous
                // level.

                if (i > 0)
                    {
                    int curLevel = model.get("line" + line + "env1level" + (i + 1));
                    int prevLevel = model.get("line" + line + "env1level" + i);
                    if (curLevel < prevLevel)
                        {
                        rate |= 128;
                        }
                    }
                                        
                if (model.get("line" + line + "env1sustain") == (i + 1))
                    {
                    level |= 128;
                    }
                                        
                pos = nybblize(rate, data, pos);
                pos = nybblize(level, data, pos);
                }
                                
            }
                
                
        // NAME
        // System.err.println("NAME " + pos);
                
        if (cz1)
            {
            String n = model.get("name", "                ") + "                ";
            byte[] name = new byte[16];
            for(int i = 0; i < 16; i++)
                pos = nybblize(n.charAt(i), data, pos);
            }
                                        
                
        if (toFile)
            {
            data[pos] = (byte)0xF7;
            return new Object[] { data };
            }
        else
            {
            // break up
            byte[] header = new byte[7];
            System.arraycopy(data, 0, header, 0, header.length);
            byte[] main = new byte[isCZ1() ? 144 * 2 + 1 : 128 * 2 + 1]; // new byte[isCZ1() ? 144 * 2 : 128 * 2];
            System.arraycopy(data, 7, main, 0, main.length);
            main[main.length - 1] = (byte)0xF7;
            return new Object[] { new Midi.DividedSysex(header), new Integer(MIDI_PAUSE), new Midi.DividedSysex(main) }; 
            // Never send this, it triggers a crash in Linux/Windows
            // new Integer(MIDI_PAUSE), new Midi.DividedSysex(new byte[] { (byte)0xF7 }) };
            }
        }

    public static final int MIDI_PAUSE = 100;  // CZ
        
    public void performRequestCurrentDump()
        {
        // The CZ series has insane sysex.  You have to send the sysex message in pieces
        // and wait for the response, or it won't work.  We're doing it here by sending
        // it in pieces with pauses in the middle and relying on buffering on our end to get
        // all of the result.
        //
        // the request differs slightly depending on whether we're a CZ1 or not.
        
        byte cz1 = (byte)(isCZ1() ? 0x11 : 0x10);
        byte chan = (byte)(0x70 | getChannelOut());

        Object[] data = new Object[4];  /// new Object[5];
        data[0] = new Midi.DividedSysex(new byte[] { (byte)0xF0, 0x44, 0x00, 0x00, chan, cz1, 0x60 });
        data[1] = new Integer(MIDI_PAUSE);
        data[2] = new Midi.DividedSysex(new byte[] { chan, 0x31 });
        data[3] = new Integer(MIDI_PAUSE);
        // Never send this last one, it triggers a crash in Windows and Linux
        //data[4] = new Midi.DividedSysex(new byte[] { (byte)0xF7 });
        tryToSendMIDI(data);
        }


    public void performRequestDump(Model tempModel, boolean changePatch)
        {
        if (tempModel == null)
            tempModel = getModel();

        if (changePatch)
            performChangePatch(tempModel);

        // The CZ series has insane sysex.  You have to send the sysex message in pieces
        // and wait for the response, or it won't work.  We're doing it here by sending
        // it in pieces with pauses in the middle and relying on buffering on our end to get
        // all of the result.
        //
        // the request differs slightly depending on whether we're a CZ1 or not.
        
        byte cz1 = (byte)(isCZ1() ? 0x11 : 0x10);
        byte chan = (byte)(0x70 | getChannelOut());

        int bank = tempModel.get("bank");
        int number = tempModel.get("number");
        byte location = (byte)(bank * 8 + number);

        Object[] data = new Object[4];  /// new Object[5];
        data[0] = new Midi.DividedSysex(new byte[] { (byte)0xF0, 0x44, 0x00, 0x00, chan, cz1, location });
        data[1] = new Integer(MIDI_PAUSE);
        data[2] = new Midi.DividedSysex(new byte[] { chan, 0x31 });
        data[3] = new Integer(MIDI_PAUSE);
        // Never send this last one, it triggers a crash in Windows and Linux
        //data[4] = new Midi.DividedSysex(new byte[] { (byte)0xF7 });
        tryToSendMIDI (data);
        }

    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        if (!checkAndSet("WindowsWarning", getSynthNameLocal()))
            {
            if (Style.isWindows() || Style.isUnix())
                {
                showSimpleError("Windows / Linux Warning",
                    "The Casio CZ patch editor will not work properly\nunder Windows Linux and will likely crash Edisyn.\n\nSee The About Pane for more information.");
                }
            }
        return frame;
        }         

    public boolean testVerify(Synth synth2, 
        String key,
        Object obj1, Object obj2) 
        {
        if (isCZ1())
            {
            // CZ1 names are padded with spaces when emitted
            return (key.equals("name"));
            }
        else
            {
            // these are CZ1-only and are ignored on parsing
            return (key.equals("line1env1velocity") ||
                key.equals("line1env2velocity") ||
                key.equals("line1env3velocity") ||
                key.equals("line2env1velocity") ||
                key.equals("line2env2velocity") ||
                key.equals("line2env3velocity") ||
                key.equals("line1env3mainlevel") ||
                key.equals("line2env3mainlevel"));
            }
        }
    }
