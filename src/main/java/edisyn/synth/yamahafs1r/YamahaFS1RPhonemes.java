/***
    Copyright 2020 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.yamahafs1r;

import edisyn.*;
import edisyn.gui.*;
import edisyn.util.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.border.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.sound.midi.*;


/**
   A utility class for making text.
   
   @author Sean Luke
*/

public class YamahaFS1RPhonemes
{
    public static final int MAX_PHONEMES = 25;
    public static final int CROSSFADE = 4;
    public static final int PRIMARY_A = 8;
    public static final int PRIMARY_B = 8;

    public static void buildModel(YamahaFS1RFseq synth)
    {
        JTextField field = new JTextField("");
        boolean result = synth.showMultiOption(synth, new String[] { "Phoneme String" }, new JComponent[] { field }, "Phonemes", "Enter a phoneme string.");
        if (result)
            {
                Model backup = (Model)(synth.getModel().clone());
                synth.setSendMIDI(false);
                synth.getUndo().setWillPush(false);
                        
                boolean res = buildModel(synth, field.getText());
                synth.getUndo().setWillPush(true);

                if (res)
                    {
                        if (!backup.keyEquals(synth.getModel()))  // it's changed, do an undo push
                            synth.getUndo().push(backup);
                    }
                synth.repaint();    // generally forces repaints to all happen at once
                synth.setSendMIDI(true);
                if (res)
                    {
                        synth.sendAllParameters();
                    }                                                                                
            }
    }
                
    public static boolean buildModel(YamahaFS1RFseq synth, String phonemes)
    {
        try
            {
                int[][][][] dat = getData(phonemes);
                if (dat.length > MAX_PHONEMES)
                    {
                        synth.showSimpleError("Phoneme Error", "String must be no more than " + MAX_PHONEMES + " phonemes.");
                        return false;
                    }
                        
                int frame = 1;
                Model model = synth.getModel();
                for(int ph = 0; ph < dat.length; ph++)
                    {
                        // crossfade in
                        for(int i = 0; i < CROSSFADE; i++)
                            {
                                double alpha = (i / (double)(CROSSFADE));
                                double beta = 1.0 - alpha;
                                for(int op = 0; op < 8; op++)
                                    {
                                        if (ph == 0 || dat[ph-1][0][op][4] == ' ')  // from start or from a space
                                            {
                                                model.set("frame" + frame + "voicedfrequency" + (op + 1), (int)(
                                                                                                                alpha * (dat[ph][1][op][0] - dat[ph][0][op][0]) + dat[ph][0][op][0] ));
                                                model.set("frame" + frame + "unvoicedfrequency" + (op + 1), (int)(
                                                                                                                  alpha * (dat[ph][1][op][2] - dat[ph][0][op][2]) + dat[ph][0][op][2] ));
                                                model.set("frame" + frame + "voicedlevel" + (op + 1), (int)(
                                                                                                            alpha * dat[ph][1][op][1] ));
                                                model.set("frame" + frame + "unvoicedlevel" + (op + 1), (int)(
                                                                                                              alpha * dat[ph][1][op][3] ));
                                            }                       
                                        else if (dat[ph][0][op][4] == ' ')      // to a space ... crossfade out
                                            {
                                                model.set("frame" + frame + "voicedfrequency" + (op + 1), (int)(
                                                                                                                beta  * (dat[ph - 1][3][op][0] - dat[ph - 1][4][op][0]) + dat[ph - 1][4][op][0]));
                                                model.set("frame" + frame + "unvoicedfrequency" + (op + 1), (int)(
                                                                                                                  beta  * (dat[ph - 1][3][op][2] - dat[ph - 1][4][op][2]) + dat[ph - 1][4][op][2]));
                                                model.set("frame" + frame + "voicedlevel" + (op + 1), (int)(
                                                                                                            beta * dat[ph - 1][3][op][1]));
                                                model.set("frame" + frame + "unvoicedlevel" + (op + 1), (int)(
                                                                                                              beta * dat[ph - 1][3][op][3]));
                                            }
                                        else            // phoneme to phoneme
                                            {
                                                model.set("frame" + frame + "voicedfrequency" + (op + 1), (int)(
                                                                                                                alpha * (alpha * (dat[ph][1][op][0] - dat[ph][0][op][0]) + dat[ph][0][op][0]) + 
                                                                                                                beta * (beta  * (dat[ph - 1][3][op][0] - dat[ph - 1][4][op][0]) + dat[ph - 1][4][op][0] )));
                                                model.set("frame" + frame + "unvoicedfrequency" + (op + 1), (int)(
                                                                                                                  alpha * (alpha * (dat[ph][1][op][2] - dat[ph][0][op][2]) + dat[ph][0][op][2]) + 
                                                                                                                  beta * (beta  * (dat[ph - 1][3][op][2] - dat[ph - 1][4][op][2]) + dat[ph - 1][4][op][2] )));
                                                model.set("frame" + frame + "voicedlevel" + (op + 1), (int)(
                                                                                                            alpha * dat[ph][1][op][1] + 
                                                                                                            beta * dat[ph - 1][3][op][1] ));
                                                model.set("frame" + frame + "unvoicedlevel" + (op + 1), (int)(
                                                                                                              alpha * dat[ph][1][op][3] + 
                                                                                                              beta * dat[ph - 1][3][op][3] ));
                                            }
                                    }
                                model.set("frame" + frame + "pitch", getPitch());
                                frame++;
                            }
                        // steady state 1
                        for(int i = 0; i < PRIMARY_A; i++)
                            {
                                double alpha = (i / (double)(PRIMARY_A));
                                for(int op = 0; op < 8; op++)
                                    {
                                        model.set("frame" + frame + "voicedfrequency" + (op + 1), (int)(
                                                                                                        alpha * (dat[ph][2][op][0] - dat[ph][1][op][0]) + dat[ph][1][op][0]));
                                        model.set("frame" + frame + "unvoicedfrequency" + (op + 1), (int)(
                                                                                                          alpha * (dat[ph][2][op][2] - dat[ph][1][op][2]) + dat[ph][1][op][2]));
                                        model.set("frame" + frame + "voicedlevel" + (op + 1), (int)(
                                                                                                    alpha * (dat[ph][2][op][1] - dat[ph][1][op][1]) + dat[ph][1][op][1]));
                                        model.set("frame" + frame + "unvoicedlevel" + (op + 1), (int)(
                                                                                                      alpha * (dat[ph][2][op][3] - dat[ph][1][op][3]) + dat[ph][1][op][3]));
                                    }
                                model.set("frame" + frame + "pitch", getPitch());
                                frame++;
                            }

                        // continuations -- just continue the phoneme though to the next one at peak
                        while (ph + 1 < dat.length && (dat[ph][2][0][4] == dat[ph + 1][2][0][4]))  // continuation
                            {
                                for(int i = 0; i < PRIMARY_A + PRIMARY_B + CROSSFADE; i++)
                                    {
                                        for(int op = 0; op < 8; op++)
                                            {
                                                model.set("frame" + frame + "voicedfrequency" + (op + 1), (int)(dat[ph][2][op][0] ));
                                                model.set("frame" + frame + "unvoicedfrequency" + (op + 1), (int)(dat[ph][2][op][2] ));
                                                model.set("frame" + frame + "voicedlevel" + (op + 1), (int)(dat[ph][2][op][1] ));
                                                model.set("frame" + frame + "unvoicedlevel" + (op + 1), (int)(dat[ph][2][op][3] ));
                                            }
                                        model.set("frame" + frame + "pitch", getPitch());
                                        frame++;
                                    }
                                ph++;           // shift to next phoneme in continuation
                            }
                                
                        // steady state 2
                        for(int i = 0; i < PRIMARY_B; i++)
                            {
                                double alpha = (i / (double)(PRIMARY_B));
                                for(int op = 0; op < 8; op++)
                                    {
                                        model.set("frame" + frame + "voicedfrequency" + (op + 1), (int)(
                                                                                                        alpha * (dat[ph][3][op][0] - dat[ph][2][op][0]) + dat[ph][2][op][0]));
                                        model.set("frame" + frame + "unvoicedfrequency" + (op + 1), (int)(
                                                                                                          alpha * (dat[ph][3][op][2] - dat[ph][2][op][2]) + dat[ph][2][op][2]));
                                        model.set("frame" + frame + "voicedlevel" + (op + 1), (int)(
                                                                                                    alpha * (dat[ph][3][op][1] - dat[ph][2][op][1]) + dat[ph][2][op][1]));
                                        model.set("frame" + frame + "unvoicedlevel" + (op + 1), (int)(
                                                                                                      alpha * (dat[ph][3][op][3] - dat[ph][2][op][3]) + dat[ph][2][op][3]));
                                    }
                                model.set("frame" + frame + "pitch", getPitch());
                                frame++;
                            }
                    }
                        
                if (dat.length > 0)
                    { 
                        int ph = dat.length;
                        // final fade out
                        for(int i = 0; i < CROSSFADE; i++)
                            {
                                double alpha = (i / (double)(CROSSFADE));
                                double beta = 1.0 - alpha;
                                for(int op = 0; op < 8; op++)
                                    {
                                        model.set("frame" + frame + "voicedfrequency" + (op + 1), (int)(
                                                                                                        beta  * (dat[ph - 1][3][op][0] - dat[ph - 1][4][op][0]) + dat[ph - 1][4][op][0]));
                                        model.set("frame" + frame + "unvoicedfrequency" + (op + 1), (int)(
                                                                                                          beta  * (dat[ph - 1][3][op][2] - dat[ph - 1][4][op][2]) + dat[ph - 1][4][op][2]));
                                        model.set("frame" + frame + "voicedlevel" + (op + 1), (int)(
                                                                                                    beta * dat[ph - 1][3][op][1]));
                                        model.set("frame" + frame + "unvoicedlevel" + (op + 1), (int)(
                                                                                                      beta * dat[ph - 1][3][op][3]));
                                    }
                                model.set("frame" + frame + "pitch", getPitch());
                                frame++;
                            }
                    }

                // zero out remainder
                for(int i = frame; i <= 512; i++)
                    {
                        for(int op = 0; op < 8; op++)
                            {
                                model.set("frame" + i + "voicedfrequency" + (op + 1), 0);
                                model.set("frame" + i + "unvoicedfrequency" + (op + 1), 0);
                                model.set("frame" + i + "voicedlevel" + (op + 1), 0);
                                model.set("frame" + i + "unvoicedlevel" + (op + 1), 0);
                            }
                        model.set("frame" + i + "pitch", 0);
                    }
                return true;
            }
        catch (Exception ex)
            {
                synth.showSimpleError("Phoneme Error", ex.getMessage());
                return false;
            }
    }       
    
    public static final char[] PHONEMES = new char[]
        {
            'I', 'U', 'V', '0', '@', 'e', '&', 'i', 'u', 'A', 'O', '3',
            'X', 'Y', 'K', 'L', 'P', 'F', 'W', 'E',
            'p', 't', 'C', 'k', 'f', 'T', 's', 'S',
            'b', 'd', 'J', 'g', 'v', 'D', 'z', 'Z',
            'm', 'n', 'N', 'h', 'l', 'R', 'w', 'j', ' '
        };

    /// FIXME: I need to handle spaces

    /** Returns data in the form <br>
        int[PHONEME][POSITION][OPERATOR][0] for voiced frequency        <br>
        int[PHONEME][POSITION][OPERATOR][1] for voiced level    <br>
        int[PHONEME][POSITION][OPERATOR][2] for unvoiced frequency      <br>
        int[PHONEME][POSITION][OPERATOR][3] for unvoiced level  <br>
        int[PHONEME][POSITION][OPERATOR][4] original character  <br>
        <p>
        PHONEME is the index of the phoneme in the string, and POS is a value 0...4 representing the 5 sampled positions of the data.
        The sampled positions are roughly near the start of the climb, near the end of the climb, at the steady state, near the start
        of the fall, and near the end of the fall.  This might be useful for doing cross-fading. 
    */
    public static final int[][][][] getData(String phonemes)
    {
        if (phonemes == null) return new int[0][0][0][0];
        char[] c = phonemes.toCharArray();
        int[][][][] dat = new int[c.length][][][];
        
        for(int i = 0; i < c.length; i++)
            {
                int phoneme = -1;
                for(int j = 0; j < PHONEMES.length; j++)
                    {
                        if (PHONEMES[j] == c[i]) { phoneme = j; break; }
                    }
                if (phoneme == -1)
                    throw new RuntimeException("Character #" + (i + 1) + "(" + c[i] + ")" + " is not a valid phoneme character.");
                dat[i] = new int[5][8][5];
            
                for(int pos = 0; pos < 5; pos++)
                    {
                        for(int op = 0; op < 8; op++)
                            {
                                dat[i][pos][op][0] = getVoicedFreq(phoneme, op, pos);
                                dat[i][pos][op][1] = getVoicedLevel(phoneme, op, pos);
                                dat[i][pos][op][2] = getUnvoicedFreq(phoneme, op, pos);
                                dat[i][pos][op][3] = getUnvoicedLevel(phoneme, op, pos);
                                dat[i][pos][op][4] = (int)c[i];
                            };
                    }
            }
        return dat;
    }

    public static final int getVoicedFreq(int phoneme, int operator, int pos)
    {
        return PHONEME_DATA[phoneme * 5 + pos][operator][0];
    }

    public static final int getVoicedLevel(int phoneme, int operator, int pos)
    {
        return PHONEME_DATA[phoneme * 5 + pos][operator][1];
    }

    public static final int getUnvoicedFreq(int phoneme, int operator, int pos)
    {
        return PHONEME_DATA[phoneme * 5 + pos][operator][2];
    }

    public static final int getUnvoicedLevel(int phoneme, int operator, int pos)
    {
        return PHONEME_DATA[phoneme * 5 + pos][operator][3];
    }

    public static final int getPitch()
    {
        /// typical pitch is 12651
        return 12651;
    }

    public static void printData(int phoneme, int pos)
    {
        for(int i = 0; i < 8; i++)
            System.err.println("" + getVoicedFreq(phoneme, i, pos) + 
                               " " + getVoicedLevel(phoneme, i, pos) + 
                               " " + getUnvoicedFreq(phoneme, i, pos) + 
                               " " + getUnvoicedLevel(phoneme, i, pos) );
    }

    public static final int[][][] PHONEME_DATA = new int[][][]
        {
            {
                {11606, 78, 11239, 25},
                {14048, 44, 13937, 25},
                {14812, 21, 14731, 18},
                {15223, 21, 15225, 14},
                {10661, 23, 9590, 16},
                {7609, 23, 6964, 11},
                {8389, 0, 12885, 0},
                {9889, 1, 11145, 0},
            },

            {
                {12118, 91, 11239, 44},
                {14025, 83, 13937, 44},
                {14792, 38, 14774, 35},
                {15223, 46, 15225, 32},
                {12185, 47, 9590, 36},
                {7438, 45, 6978, 29},
                {9566, 0, 9654, 0},
                {8558, 10, 11157, 0},
            },

            {
                {12217, 97, 11239, 55},
                {14004, 98, 13937, 55},
                {14749, 46, 14769, 46},
                {15224, 52, 15225, 35},
                {12718, 50, 9590, 38},
                {7821, 42, 6978, 30},
                {12281, 0, 9445, 0},
                {8145, 15, 11172, 0},
            },

            {
                {12169, 93, 11239, 41},
                {13970, 73, 13938, 41},
                {14793, 34, 14724, 34},
                {15225, 41, 15225, 24},
                {13228, 37, 9590, 26},
                {8822, 24, 6969, 15},
                {6285, 0, 11072, 0},
                {8125, 4, 11175, 0},
            },

            {
                {11878, 84, 11239, 24},
                {14008, 47, 13938, 24},
                {14822, 20, 14720, 18},
                {15225, 23, 15225, 13},
                {12103, 22, 9590, 13},
                {7370, 12, 6973, 6},
                {3753, 0, 11498, 0},
                {8120, 0, 11176, 0},
            },

            {
                {11755, 77, 11239, 25},
                {14064, 41, 13936, 25},
                {14809, 26, 14708, 22},
                {15224, 8, 15225, 2},
                {11168, 10, 9590, 0},
                {7754, 10, 6968, 3},
                {6415, 0, 12534, 0},
                {8120, 0, 11118, 0},
            },

            {
                {12262, 86, 11239, 49},
                {14125, 74, 13936, 49},
                {14761, 52, 14708, 44},
                {15224, 17, 15225, 6},
                {12291, 23, 9590, 2},
                {5992, 23, 6959, 10},
                {10269, 0, 12031, 0},
                {8127, 1, 10757, 0},
            },

            {
                {12634, 97, 11239, 69},
                {14119, 103, 13936, 69},
                {14709, 75, 14708, 60},
                {15224, 17, 15225, 6},
                {12569, 26, 9590, 2},
                {7486, 33, 6951, 13},
                {13973, 0, 11226, 0},
                {8145, 0, 8772, 0},
            },

            {
                {12209, 96, 11239, 56},
                {14093, 83, 13936, 56},
                {14712, 66, 14708, 46},
                {15225, 15, 15225, 1},
                {12841, 13, 9590, 0},
                {8803, 15, 6982, 2},
                {14293, 0, 11184, 0},
                {8148, 0, 8104, 0},
            },

            {
                {11489, 83, 11239, 23},
                {14095, 43, 13936, 23},
                {14819, 32, 14708, 19},
                {15225, 6, 15225, 1},
                {10534, 1, 9590, 0},
                {8441, 1, 7020, 0},
                {9758, 0, 11190, 0},
                {8148, 0, 8104, 0},
            },

            {
                {11509, 76, 11239, 27},
                {13965, 35, 13936, 27},
                {14857, 29, 14708, 25},
                {15225, 16, 15225, 13},
                {10798, 13, 9586, 6},
                {8538, 14, 7276, 4},
                {6476, 0, 11194, 0},
                {8147, 0, 8104, 0},
            },

            {
                {11806, 82, 11239, 52},
                {13957, 62, 13938, 52},
                {14791, 60, 14708, 50},
                {15225, 34, 15225, 28},
                {11957, 28, 9583, 14},
                {8274, 31, 7528, 13},
                {8599, 0, 11180, 0},
                {8144, 0, 8104, 0},
            },

            {
                {12164, 90, 11239, 71},
                {13994, 83, 13952, 71},
                {14798, 85, 14708, 70},
                {15225, 50, 15225, 38},
                {13254, 37, 9583, 19},
                {6491, 45, 7052, 26},
                {6477, 0, 11278, 0},
                {8198, 0, 8104, 0},
            },

            {
                {12115, 92, 11239, 54},
                {13965, 64, 13989, 54},
                {14781, 65, 14709, 53},
                {15225, 42, 15225, 31},
                {13646, 21, 9575, 11},
                {8888, 26, 5780, 13},
                {8003, 1, 11959, 0},
                {9369, 0, 8104, 0},
            },

            {
                {11579, 82, 11239, 23},
                {14020, 28, 13973, 23},
                {14817, 30, 14709, 21},
                {15225, 17, 15225, 10},
                {11248, 5, 9582, 1},
                {5166, 5, 6204, 1},
                {5568, 0, 11415, 0},
                {8593, 0, 8104, 0},
            },

            {
                {11388, 75, 11239, 21},
                {13991, 29, 13936, 21},
                {14815, 23, 14720, 18},
                {15223, 10, 15224, 6},
                {10293, 8, 9590, 5},
                {5498, 8, 6961, 3},
                {5954, 0, 10803, 0},
                {8209, 0, 8105, 0},
            },

            {
                {11734, 85, 11239, 54},
                {14034, 68, 13936, 54},
                {14768, 64, 14725, 51},
                {15223, 30, 15230, 22},
                {11422, 27, 9590, 18},
                {7379, 27, 6965, 16},
                {8564, 4, 11193, 2},
                {9031, 2, 8134, 1},
            },

            {
                {12087, 92, 11239, 75},
                {14030, 89, 13936, 75},
                {14741, 89, 14723, 70},
                {15223, 37, 15235, 29},
                {12451, 32, 9590, 17},
                {8064, 32, 6965, 17},
                {9913, 11, 11633, 2},
                {10806, 2, 8178, 1},
            },

            {
                {12005, 93, 11239, 52},
                {14019, 64, 13936, 52},
                {14708, 68, 14708, 46},
                {15223, 17, 15223, 9},
                {13005, 16, 9590, 3},
                {5284, 14, 6984, 1},
                {13390, 3, 11221, 0},
                {10873, 0, 8146, 0},
            },

            {
                {11620, 86, 11239, 29},
                {13997, 37, 13936, 29},
                {14708, 41, 14708, 25},
                {15223, 4, 15223, 1},
                {11669, 4, 9590, 0},
                {7717, 6, 6964, 0},
                {14702, 0, 11248, 0},
                {10716, 0, 8146, 0},
            },

            {
                {11430, 76, 11239, 23},
                {13969, 33, 13948, 23},
                {14734, 24, 14719, 21},
                {15223, 15, 15223, 11},
                {10140, 14, 9590, 7},
                {9040, 13, 6304, 7},
                {13079, 0, 11107, 0},
                {9936, 3, 8138, 0},
            },

            {
                {12032, 88, 11239, 52},
                {14001, 75, 13948, 52},
                {14786, 54, 14738, 49},
                {15223, 40, 15223, 28},
                {11843, 40, 9590, 21},
                {8400, 35, 6309, 21},
                {7789, 0, 10999, 0},
                {8758, 12, 8118, 0},
            },

            {
                {12572, 96, 11239, 65},
                {14104, 93, 13941, 65},
                {14807, 64, 14721, 61},
                {15223, 54, 15223, 35},
                {12978, 53, 9590, 23},
                {7981, 44, 7047, 26},
                {4871, 3, 11403, 0},
                {8381, 14, 8099, 0},
            },

            {
                {12198, 92, 11239, 46},
                {14283, 68, 14026, 46},
                {14811, 55, 14749, 43},
                {15224, 52, 15224, 27},
                {12690, 34, 9590, 15},
                {5010, 23, 8170, 11},
                {4320, 8, 8830, 0},
                {9589, 5, 8096, 0},
            },

            {
                {11753, 85, 11239, 26},
                {14359, 41, 14104, 26},
                {14818, 38, 14794, 25},
                {15224, 36, 15225, 15},
                {11452, 16, 9590, 7},
                {3222, 10, 10378, 3},
                {3976, 3, 5128, 0},
                {10282, 1, 8096, 0},
            },

            {
                {11239, 75, 11239, 0},
                {14063, 0, 13936, 0},
                {14837, 0, 14709, 0},
                {15225, 0, 15225, 0},
                {9589, 0, 9590, 0},
                {558, 0, 6958, 0},
                {4516, 0, 11236, 0},
                {8072, 0, 8104, 0},
            },

            {
                {11239, 75, 11239, 0},
                {14064, 0, 13936, 0},
                {14837, 0, 14709, 0},
                {15225, 0, 15225, 0},
                {9590, 0, 9590, 0},
                {558, 0, 6958, 0},
                {4516, 0, 11236, 0},
                {8072, 0, 8104, 0},
            },

            {
                {11239, 74, 11239, 0},
                {14063, 0, 13936, 0},
                {14837, 0, 14709, 0},
                {15225, 0, 15225, 0},
                {9590, 0, 9590, 0},
                {573, 0, 6958, 0},
                {4516, 0, 11236, 0},
                {8072, 0, 8104, 0},
            },

            {
                {11243, 72, 11239, 2},
                {14024, 4, 13936, 2},
                {14835, 2, 14709, 2},
                {15224, 0, 15224, 0},
                {9618, 0, 9590, 0},
                {2533, 0, 6958, 0},
                {4590, 0, 11168, 0},
                {8072, 0, 8103, 0},
            },

            {
                {11290, 72, 11239, 10},
                {13991, 15, 13936, 10},
                {14828, 10, 14715, 8},
                {15224, 3, 15224, 1},
                {9863, 2, 9590, 1},
                {4513, 2, 6958, 0},
                {5095, 0, 10937, 0},
                {8098, 0, 8103, 0},
            },

            {
                {11625, 77, 11239, 26},
                {14008, 33, 13969, 26},
                {14750, 29, 14780, 24},
                {15256, 27, 15223, 18},
                {11056, 18, 9586, 13},
                {8522, 15, 7132, 7},
                {11100, 2, 13972, 1},
                {10709, 4, 13295, 0},
            },

            {
                {12266, 88, 11239, 58},
                {14043, 67, 14061, 58},
                {14780, 69, 14725, 56},
                {15287, 67, 15223, 48},
                {12967, 45, 9586, 34},
                {7225, 43, 8085, 25},
                {7702, 11, 11865, 8},
                {9902, 15, 11265, 5},
            },

            {
                {12455, 94, 11239, 72},
                {14080, 81, 14080, 72},
                {14803, 86, 14708, 70},
                {15315, 82, 15223, 62},
                {13491, 53, 9586, 41},
                {5138, 48, 9410, 29},
                {5642, 11, 11199, 9},
                {9684, 19, 8527, 5},
            },

            {
                {12268, 96, 11239, 52},
                {14088, 64, 14041, 52},
                {14808, 61, 14708, 49},
                {15294, 57, 15223, 41},
                {13631, 32, 9588, 23},
                {5433, 23, 8804, 12},
                {6403, 0, 11223, 0},
                {10180, 3, 8070, 0},
            },

            {
                {11876, 86, 11239, 23},
                {14101, 34, 13990, 23},
                {14823, 27, 14709, 20},
                {15233, 26, 15224, 16},
                {12123, 9, 9589, 5},
                {5154, 5, 7291, 1},
                {7596, 0, 11254, 0},
                {10539, 0, 8070, 0},
            },

            {
                {12141, 89, 11239, 30},
                {14013, 61, 13936, 30},
                {14738, 20, 14852, 21},
                {15294, 18, 15224, 7},
                {12363, 31, 9590, 14},
                {9473, 39, 6961, 24},
                {12628, 11, 8886, 0},
                {11164, 17, 8075, 5},
            },

            {
                {12812, 100, 11239, 40},
                {14054, 84, 13937, 40},
                {14720, 28, 14872, 28},
                {15327, 21, 15296, 12},
                {10857, 39, 9590, 22},
                {6401, 42, 6965, 27},
                {12467, 14, 9660, 0},
                {8517, 20, 7443, 4},
            },

            {
                {12370, 98, 11239, 34},
                {13992, 74, 13937, 34},
                {14708, 23, 14878, 22},
                {15336, 14, 15352, 6},
                {10929, 33, 9590, 11},
                {7840, 37, 6994, 20},
                {14342, 6, 8976, 0},
                {7979, 16, 6939, 1},
            },

            {
                {12355, 95, 11239, 31},
                {13987, 66, 13937, 31},
                {14736, 21, 14946, 20},
                {15351, 13, 15353, 2},
                {14041, 25, 9590, 16},
                {6470, 44, 6977, 28},
                {13321, 5, 3761, 0},
                {7812, 9, 6932, 0},
            },

            {
                {11711, 89, 11239, 18},
                {13993, 48, 13936, 18},
                {14782, 12, 14873, 11},
                {15351, 5, 15353, 1},
                {11690, 15, 9590, 6},
                {5602, 14, 6980, 7},
                {14005, 0, 9907, 0},
                {10001, 1, 6932, 0},
            },

            {
                {12071, 83, 11239, 28},
                {14072, 49, 13936, 28},
                {14708, 26, 14748, 24},
                {15269, 14, 15265, 6},
                {9971, 15, 9590, 0},
                {9520, 10, 6958, 0},
                {13422, 0, 11785, 0},
                {9644, 3, 7714, 0},
            },

            {
                {12966, 101, 11239, 57},
                {14194, 102, 13936, 57},
                {14708, 54, 14708, 51},
                {15255, 25, 15225, 20},
                {8772, 32, 9590, 9},
                {7571, 22, 6958, 7},
                {12579, 0, 11585, 0},
                {11662, 12, 8091, 0},
            },

            {
                {12791, 101, 11239, 57},
                {14149, 96, 13936, 57},
                {14708, 53, 14708, 49},
                {15225, 21, 15225, 17},
                {8544, 27, 9590, 4},
                {10381, 17, 6958, 0},
                {12790, 0, 11227, 0},
                {16323, 6, 8104, 0},
            },

            {
                {12384, 94, 11239, 41},
                {14158, 81, 13936, 41},
                {14708, 36, 14708, 31},
                {15225, 5, 15225, 0},
                {14198, 11, 9590, 0},
                {6200, 0, 6999, 0},
                {12664, 0, 11253, 0},
                {16327, 0, 8104, 0},
            },

            {
                {11803, 87, 11239, 23},
                {14134, 46, 13936, 23},
                {14709, 23, 14708, 18},
                {15223, 0, 15223, 0},
                {12363, 0, 9590, 0},
                {8274, 0, 6967, 0},
                {12873, 0, 11245, 0},
                {15809, 0, 8146, 0},
            },

            {
                {11439, 76, 11239, 26},
                {14024, 31, 13958, 26},
                {14754, 27, 14708, 23},
                {15223, 14, 15223, 12},
                {10451, 11, 9578, 6},
                {7152, 11, 7023, 3},
                {11330, 0, 11550, 0},
                {9689, 0, 8658, 0},
            },

            {
                {12141, 90, 11239, 67},
                {14037, 75, 13973, 67},
                {14815, 78, 14708, 65},
                {15223, 46, 15223, 42},
                {12757, 38, 9565, 26},
                {6100, 36, 6986, 19},
                {7145, 0, 12607, 0},
                {9679, 0, 8658, 0},
            },

            {
                {12217, 94, 11239, 68},
                {14056, 79, 13936, 68},
                {14817, 81, 14708, 67},
                {15224, 46, 15223, 43},
                {12625, 36, 9587, 25},
                {5852, 34, 6957, 20},
                {5871, 0, 12413, 0},
                {9741, 0, 8658, 0},
            },

            {
                {11239, 75, 11239, 0},
                {14064, 0, 13936, 0},
                {14709, 0, 14836, 0},
                {15353, 0, 15353, 0},
                {9546, 0, 9590, 0},
                {609, 0, 6977, 0},
                {15760, 0, 13086, 0},
                {10000, 0, 6932, 0},
            },

            {
                {11254, 72, 11239, 2},
                {14024, 5, 13936, 2},
                {14709, 2, 14832, 2},
                {15349, 0, 15349, 0},
                {9623, 0, 9590, 0},
                {3293, 0, 6970, 0},
                {15699, 0, 13049, 0},
                {9993, 0, 6952, 0},
            },

            {
                {12528, 100, 11239, 54},
                {14140, 91, 13936, 54},
                {14708, 49, 14708, 47},
                {15225, 18, 15225, 13},
                {10603, 22, 9590, 1},
                {9524, 13, 6958, 0},
                {12459, 0, 11237, 0},
                {16321, 4, 8104, 0},
            },

            {
                {12382, 97, 11239, 46},
                {14157, 85, 13936, 46},
                {14708, 39, 14708, 36},
                {15225, 10, 15225, 2},
                {14088, 15, 9590, 0},
                {5082, 1, 6984, 0},
                {12582, 0, 11252, 0},
                {16326, 0, 8104, 0},
            },

            {
                {11803, 87, 11239, 23},
                {14134, 46, 13936, 23},
                {14709, 23, 14708, 18},
                {15223, 0, 15223, 0},
                {12363, 0, 9590, 0},
                {8274, 0, 6967, 0},
                {12873, 0, 11245, 0},
                {15809, 0, 8146, 0},
            },

            {
                {11244, 75, 11239, 0},
                {14191, 0, 13936, 0},
                {14709, 0, 14708, 0},
                {15223, 0, 15223, 0},
                {9614, 0, 9590, 0},
                {11714, 0, 6958, 0},
                {15782, 0, 11166, 0},
                {9706, 0, 8656, 0},
            },

            {
                {11239, 75, 11239, 0},
                {14192, 0, 13936, 0},
                {14710, 0, 14708, 0},
                {15223, 0, 15223, 0},
                {9590, 0, 9590, 0},
                {11758, 0, 6958, 0},
                {15791, 0, 11166, 0},
                {9690, 0, 8658, 0},
            },

            {
                {11320, 77, 11239, 1},
                {14059, 6, 13936, 1},
                {14718, 1, 14836, 0},
                {15352, 0, 15353, 0},
                {9884, 0, 9590, 0},
                {900, 0, 6978, 0},
                {15633, 0, 13006, 0},
                {9999, 0, 6932, 0},
            },

            {
                {11240, 75, 11239, 0},
                {14065, 0, 13936, 0},
                {14709, 0, 14836, 0},
                {15353, 0, 15353, 0},
                {9546, 0, 9590, 0},
                {608, 0, 6978, 0},
                {15760, 0, 13086, 0},
                {10000, 0, 6932, 0},
            },

            {
                {11245, 72, 11239, 1},
                {14036, 3, 13936, 1},
                {14709, 1, 14834, 1},
                {15351, 0, 15351, 0},
                {9583, 0, 9590, 0},
                {2469, 0, 6972, 0},
                {15735, 0, 13071, 0},
                {9996, 0, 6940, 0},
            },

            {
                {12869, 98, 11239, 53},
                {14164, 94, 13936, 53},
                {14708, 49, 14710, 47},
                {15255, 23, 15226, 15},
                {8805, 30, 9590, 5},
                {8632, 22, 6958, 5},
                {12481, 0, 11411, 0},
                {10188, 10, 8067, 0},
            },

            {
                {13005, 102, 11239, 58},
                {14209, 104, 13936, 58},
                {14708, 57, 14708, 52},
                {15228, 26, 15225, 22},
                {8226, 31, 9590, 11},
                {9203, 18, 6958, 4},
                {12850, 0, 11546, 0},
                {15973, 11, 8102, 0},
            },

            {
                {11892, 80, 11239, 22},
                {14010, 48, 13936, 22},
                {14863, 15, 14850, 17},
                {15350, 17, 15350, 9},
                {10688, 19, 9590, 12},
                {9365, 20, 8234, 12},
                {5282, 0, 2851, 0},
                {5633, 6, 10011, 0},
            },

            {
                {12771, 98, 11239, 49},
                {14013, 100, 13937, 49},
                {14754, 36, 14839, 40},
                {15320, 46, 15332, 29},
                {11235, 46, 9590, 33},
                {7868, 43, 6994, 30},
                {9801, 5, 5560, 0},
                {7761, 18, 9550, 2},
            },

            {
                {12354, 99, 11239, 52},
                {13992, 93, 13936, 52},
                {14814, 42, 14731, 44},
                {15277, 54, 15295, 35},
                {13172, 47, 9590, 32},
                {7461, 36, 6981, 21},
                {5135, 9, 10538, 0},
                {9893, 5, 8947, 0},
            },

            {
                {11991, 94, 11239, 49},
                {14009, 69, 13936, 49},
                {14727, 47, 14708, 43},
                {15225, 51, 15223, 30},
                {12614, 27, 9590, 18},
                {7170, 19, 6975, 6},
                {12535, 9, 11211, 0},
                {10733, 0, 8148, 0},
            },

            {
                {11743, 88, 11239, 29},
                {14083, 47, 13936, 29},
                {14740, 34, 14708, 23},
                {15225, 32, 15223, 10},
                {12265, 11, 9590, 3},
                {6099, 4, 6952, 0},
                {9891, 0, 11234, 0},
                {8753, 0, 8146, 0},
            },

            {
                {11896, 82, 11239, 24},
                {14136, 41, 13936, 24},
                {14708, 22, 14708, 21},
                {15237, 6, 15223, 1},
                {9146, 4, 9590, 0},
                {4659, 11, 6947, 2},
                {12397, 0, 11207, 0},
                {8255, 7, 8146, 0},
            },

            {
                {12482, 97, 11239, 57},
                {14189, 99, 13936, 57},
                {14708, 55, 14708, 50},
                {15314, 21, 15223, 10},
                {10924, 14, 9590, 4},
                {8023, 24, 6959, 7},
                {13061, 2, 11180, 0},
                {8898, 14, 8146, 0},
            },

            {
                {12433, 98, 11239, 56},
                {14189, 90, 13936, 56},
                {14724, 57, 14708, 53},
                {15279, 24, 15223, 16},
                {13721, 20, 9590, 4},
                {10210, 14, 6971, 1},
                {10898, 2, 11182, 0},
                {9123, 3, 8146, 0},
            },

            {
                {12158, 89, 11239, 49},
                {14031, 71, 13936, 49},
                {14708, 50, 14708, 45},
                {15225, 31, 15223, 22},
                {13777, 22, 9587, 6},
                {9391, 16, 6961, 1},
                {11953, 0, 11217, 0},
                {8880, 0, 8146, 0},
            },

            {
                {11741, 80, 11239, 25},
                {14034, 42, 13936, 26},
                {14708, 29, 14708, 23},
                {15225, 18, 15223, 10},
                {11969, 11, 9587, 1},
                {5289, 7, 6956, 0},
                {11345, 0, 11236, 0},
                {10114, 0, 8146, 0},
            },

            {
                {11656, 76, 11239, 28},
                {14039, 35, 13952, 28},
                {14749, 33, 14709, 27},
                {15225, 17, 15223, 16},
                {10804, 14, 9580, 9},
                {5277, 15, 6674, 7},
                {9399, 0, 11142, 0},
                {10720, 0, 8146, 0},
            },

            {
                {12497, 90, 11240, 66},
                {14105, 76, 13942, 66},
                {14771, 77, 14724, 65},
                {15224, 47, 15223, 44},
                {13307, 33, 9569, 23},
                {5157, 38, 7915, 21},
                {8832, 0, 10519, 0},
                {10720, 0, 8146, 0},
            },

            {
                {12311, 94, 11239, 66},
                {14144, 74, 13946, 67},
                {14794, 77, 14708, 64},
                {15224, 55, 15223, 46},
                {13548, 31, 9587, 23},
                {6247, 34, 7148, 21},
                {9789, 0, 11599, 0},
                {10700, 2, 8146, 0},
            },

            {
                {12009, 92, 11239, 43},
                {14024, 70, 13936, 43},
                {14814, 36, 14715, 35},
                {15224, 42, 15223, 23},
                {13546, 32, 9590, 22},
                {7059, 17, 6972, 10},
                {7206, 3, 11787, 0},
                {10901, 0, 8146, 0},
            },

            {
                {11789, 88, 11239, 26},
                {14003, 61, 13937, 26},
                {14829, 20, 14776, 19},
                {15224, 22, 15223, 10},
                {12908, 23, 9590, 12},
                {9518, 11, 6973, 4},
                {5732, 0, 8355, 0},
                {8763, 0, 8146, 0},
            },

            {
                {11498, 76, 11239, 20},
                {14118, 28, 13936, 20},
                {14740, 23, 14822, 17},
                {15224, 5, 15223, 3},
                {10334, 5, 9590, 1},
                {7364, 6, 6970, 2},
                {11363, 0, 3223, 0},
                {8165, 1, 8146, 0},
            },

            {
                {12502, 96, 11239, 65},
                {13968, 89, 13936, 65},
                {14738, 82, 14754, 56},
                {15224, 27, 15223, 17},
                {13338, 27, 9590, 13},
                {9326, 26, 6978, 13},
                {11220, 0, 9886, 0},
                {8148, 6, 8146, 0},
            },

            {
                {12467, 98, 11239, 64},
                {14070, 91, 13936, 64},
                {14732, 66, 14766, 59},
                {15224, 40, 15224, 27},
                {14015, 35, 9590, 20},
                {9269, 33, 6974, 15},
                {11370, 2, 8276, 0},
                {8174, 3, 8146, 0},
            },

            {
                {12385, 92, 11239, 36},
                {14041, 78, 13937, 36},
                {14766, 24, 14731, 26},
                {15231, 35, 15224, 21},
                {14161, 34, 9590, 21},
                {4951, 20, 6968, 11},
                {10899, 1, 11797, 0},
                {8505, 1, 8146, 0},
            },

            {
                {11764, 88, 11239, 20},
                {13962, 53, 13937, 20},
                {14774, 15, 14834, 14},
                {15346, 8, 15224, 0},
                {12095, 11, 9590, 1},
                {6989, 4, 6978, 0},
                {11062, 0, 1645, 0},
                {5650, 0, 8146, 0},
            },

            {
                {11241, 75, 11239, 0},
                {14063, 1, 13936, 0},
                {14709, 0, 14708, 0},
                {15225, 0, 15223, 0},
                {9552, 0, 9590, 0},
                {568, 0, 6957, 0},
                {11144, 0, 11242, 0},
                {10720, 0, 8146, 0},
            },

            {
                {11239, 75, 11239, 0},
                {14063, 0, 13936, 0},
                {14709, 0, 14708, 0},
                {15225, 0, 15223, 0},
                {9581, 0, 9590, 0},
                {560, 0, 6958, 0},
                {11144, 0, 11242, 0},
                {10720, 0, 8146, 0},
            },

            {
                {12044, 82, 11239, 47},
                {14067, 56, 13952, 47},
                {14764, 55, 14714, 46},
                {15225, 32, 15223, 29},
                {11949, 25, 9572, 17},
                {5549, 26, 7207, 13},
                {8845, 0, 10926, 0},
                {10720, 0, 8146, 0},
            },

            {
                {12338, 92, 11239, 68},
                {14132, 76, 13939, 68},
                {14795, 79, 14709, 66},
                {15224, 52, 15223, 46},
                {13335, 30, 9583, 23},
                {4608, 38, 7053, 23},
                {9868, 0, 11547, 0},
                {10721, 0, 8146, 0},
            },

            {
                {12376, 96, 11239, 61},
                {14065, 71, 13952, 61},
                {14796, 71, 14708, 58},
                {15224, 60, 15223, 43},
                {14058, 33, 9589, 25},
                {8541, 26, 7272, 16},
                {8141, 0, 11461, 0},
                {10259, 3, 8146, 0},
            },

            {
                {11250, 75, 11239, 0},
                {14315, 1, 13938, 0},
                {14709, 0, 14836, 0},
                {15224, 0, 15223, 0},
                {9624, 0, 9590, 0},
                {4970, 0, 6982, 0},
                {12634, 0, 1439, 0},
                {8166, 0, 8146, 0},
            },

            {
                {11239, 75, 11239, 0},
                {14314, 0, 13937, 0},
                {14709, 0, 14836, 0},
                {15224, 0, 15223, 0},
                {9547, 0, 9590, 0},
                {5057, 0, 6982, 0},
                {12685, 0, 1440, 0},
                {8166, 0, 8146, 0},
            },

            {
                {12042, 85, 11239, 47},
                {13995, 64, 13936, 47},
                {14756, 58, 14783, 41},
                {15224, 17, 15223, 10},
                {11958, 17, 9590, 7},
                {9544, 18, 6973, 8},
                {10421, 0, 7078, 0},
                {8160, 4, 8146, 0},
            },

            {
                {12620, 98, 11239, 71},
                {14019, 94, 13936, 71},
                {14711, 85, 14794, 61},
                {15224, 31, 15223, 21},
                {13807, 33, 9590, 17},
                {8336, 32, 6972, 14},
                {13233, 0, 8693, 0},
                {8130, 2, 8146, 0},
            },

            {
                {12439, 98, 11239, 61},
                {14060, 90, 13936, 61},
                {14738, 61, 14756, 56},
                {15224, 43, 15224, 29},
                {14058, 36, 9590, 23},
                {9148, 32, 6976, 15},
                {10903, 3, 8547, 0},
                {8212, 4, 8146, 0},
            },

            {
                {11240, 74, 11239, 0},
                {13936, 0, 13936, 0},
                {14708, 0, 14836, 0},
                {15353, 0, 15224, 0},
                {9598, 0, 9590, 0},
                {12035, 0, 7061, 0},
                {15768, 0, 1438, 0},
                {5472, 0, 8146, 0},
            },

            {
                {11289, 73, 11239, 8},
                {14017, 13, 13936, 8},
                {14735, 9, 14822, 7},
                {15352, 7, 15224, 4},
                {9735, 4, 9590, 1},
                {10651, 4, 8935, 1},
                {13777, 0, 2595, 0},
                {5472, 0, 8146, 0},
            },

            {
                {12446, 94, 11239, 65},
                {14176, 92, 13936, 65},
                {14731, 63, 14722, 60},
                {15312, 50, 15224, 36},
                {13955, 38, 9590, 19},
                {7158, 38, 6957, 24},
                {11171, 4, 10833, 0},
                {6243, 13, 8146, 0},
            },

            {
                {11499, 74, 11239, 20},
                {14033, 25, 13950, 20},
                {14739, 23, 14708, 19},
                {15225, 11, 15223, 10},
                {10343, 9, 9583, 6},
                {4730, 9, 6585, 4},
                {9842, 0, 11194, 0},
                {10720, 0, 8146, 0},
            },

            {
                {12551, 92, 11240, 71},
                {14105, 80, 13936, 71},
                {14791, 82, 14722, 69},
                {15224, 51, 15223, 47},
                {13547, 32, 9574, 24},
                {4614, 40, 7495, 25},
                {8752, 0, 10756, 0},
                {10720, 0, 8146, 0},
            },

            {
                {11689, 77, 11239, 24},
                {13955, 35, 13936, 24},
                {14892, 25, 14708, 21},
                {15351, 25, 15223, 16},
                {10918, 20, 9590, 15},
                {8340, 18, 6970, 10},
                {2548, 0, 13873, 0},
                {3497, 2, 13261, 0},
            },

            {
                {12192, 85, 11239, 46},
                {13965, 64, 13936, 46},
                {14843, 48, 14708, 41},
                {15351, 50, 15223, 32},
                {12402, 41, 9590, 32},
                {7113, 37, 6975, 23},
                {3552, 0, 12824, 0},
                {3524, 9, 13251, 1},
            },

            {
                {12187, 94, 11239, 59},
                {13979, 89, 13937, 59},
                {14799, 57, 14712, 52},
                {15295, 57, 15224, 39},
                {12516, 52, 9590, 41},
                {6416, 46, 6980, 34},
                {7489, 2, 13845, 0},
                {5539, 17, 13222, 3},
            },

            {
                {12263, 94, 11239, 36},
                {14018, 78, 13938, 36},
                {14773, 23, 14817, 26},
                {15224, 31, 15225, 19},
                {13695, 36, 9590, 24},
                {6862, 29, 6979, 18},
                {9277, 3, 6545, 0},
                {9211, 8, 13220, 0},
            },

            {
                {12343, 88, 11239, 26},
                {14078, 65, 13938, 26},
                {14776, 13, 14834, 16},
                {15224, 10, 15225, 2},
                {14206, 25, 9590, 10},
                {4864, 18, 6981, 8},
                {10966, 0, 8227, 0},
                {8974, 0, 13220, 0},
            },

            {
                {11242, 78, 11239, 17},
                {14049, 26, 13936, 17},
                {14753, 18, 14820, 14},
                {15240, 11, 15223, 4},
                {9582, 7, 9585, 1},
                {6571, 9, 7078, 2},
                {11272, 2, 9000, 0},
                {8699, 2, 12825, 0},
            },

            {
                {11268, 90, 11239, 27},
                {14016, 40, 13936, 27},
                {14805, 34, 14780, 24},
                {15250, 24, 15223, 12},
                {9749, 17, 9578, 7},
                {7049, 19, 7445, 6},
                {7662, 7, 9523, 0},
                {8755, 6, 11626, 0},
            },

            {
                {11352, 104, 11239, 31},
                {14056, 44, 13985, 31},
                {14813, 46, 14761, 29},
                {15231, 34, 15223, 20},
                {10340, 28, 9582, 13},
                {5261, 27, 8355, 11},
                {5562, 15, 9571, 0},
                {10468, 6, 8735, 0},
            },

            {
                {11410, 87, 11239, 23},
                {14068, 36, 14030, 23},
                {14817, 34, 14828, 23},
                {15223, 25, 15223, 14},
                {10793, 18, 9589, 6},
                {6926, 18, 7497, 6},
                {3846, 7, 6479, 0},
                {12648, 2, 8228, 0},
            },

            {
                {11414, 78, 11239, 14},
                {14004, 23, 13993, 14},
                {14831, 19, 14839, 14},
                {15223, 14, 15223, 6},
                {10847, 7, 9589, 1},
                {7700, 8, 6991, 1},
                {2740, 2, 4217, 0},
                {13169, 0, 8174, 0},
            },

            {
                {11239, 73, 11239, 13},
                {14017, 24, 13982, 13},
                {14824, 14, 14825, 11},
                {15224, 16, 15223, 10},
                {9574, 16, 9574, 9},
                {8432, 21, 6921, 14},
                {5181, 17, 3970, 13},
                {12279, 15, 8490, 8},
            },

            {
                {11251, 75, 11239, 26},
                {14096, 46, 14153, 26},
                {14796, 31, 14764, 26},
                {15247, 50, 15223, 31},
                {9577, 48, 9565, 28},
                {7468, 56, 5457, 43},
                {8077, 51, 8553, 40},
                {9614, 46, 9704, 32},
            },

            {
                {11333, 70, 11239, 28},
                {14070, 45, 14194, 28},
                {14780, 37, 14723, 27},
                {15265, 54, 15223, 32},
                {9883, 49, 9582, 29},
                {5337, 52, 5175, 39},
                {9397, 47, 10974, 36},
                {8189, 41, 10096, 28},
            },

            {
                {11375, 70, 11239, 21},
                {14023, 30, 14089, 21},
                {14755, 30, 14748, 20},
                {15260, 34, 15223, 19},
                {10058, 27, 9584, 16},
                {5622, 29, 7037, 18},
                {11346, 22, 10661, 14},
                {8029, 19, 9554, 10},
            },

            {
                {11307, 73, 11239, 11},
                {13976, 16, 14045, 11},
                {14734, 16, 14794, 10},
                {15244, 15, 15223, 8},
                {9774, 11, 9577, 6},
                {8122, 12, 8317, 6},
                {12826, 7, 9763, 3},
                {8596, 6, 8743, 2},
            },

            {
                {11437, 75, 11239, 9},
                {14139, 22, 14170, 9},
                {14822, 25, 14873, 13},
                {15247, 32, 15243, 19},
                {11054, 30, 9587, 21},
                {7868, 31, 7547, 21},
                {9741, 25, 7267, 15},
                {10973, 21, 9005, 13},
            },

            {
                {11358, 74, 11239, 14},
                {14180, 30, 14335, 14},
                {14841, 36, 14862, 22},
                {15301, 59, 15275, 42},
                {10456, 63, 9581, 49},
                {7897, 61, 6882, 48},
                {7384, 52, 8111, 40},
                {8020, 51, 9739, 38},
            },

            {
                {11272, 71, 11239, 31},
                {14182, 50, 14324, 31},
                {14827, 44, 14763, 36},
                {15343, 69, 15323, 56},
                {9779, 75, 9552, 58},
                {5652, 68, 4703, 55},
                {5137, 59, 12282, 45},
                {5122, 61, 9712, 47},
            },

            {
                {11330, 72, 11239, 23},
                {14171, 37, 14213, 23},
                {14796, 31, 14749, 24},
                {15278, 42, 15318, 32},
                {10216, 43, 9576, 31},
                {7317, 39, 6067, 28},
                {5913, 29, 12844, 17},
                {8251, 30, 9472, 20},
            },

            {
                {11318, 73, 11239, 11},
                {14188, 20, 14084, 11},
                {14807, 16, 14783, 11},
                {15239, 22, 15326, 15},
                {10120, 20, 9587, 13},
                {9839, 19, 7160, 12},
                {6255, 12, 12541, 5},
                {9806, 12, 9641, 7},
            },

            {
                {11255, 75, 11239, 14},
                {13992, 22, 13985, 14},
                {14807, 14, 14807, 12},
                {15227, 12, 15333, 7},
                {9683, 9, 9588, 4},
                {8273, 5, 6411, 2},
                {8073, 4, 12486, 2},
                {10507, 3, 10538, 1},
            },

            {
                {11326, 80, 11239, 32},
                {14022, 46, 14084, 32},
                {14756, 40, 14748, 31},
                {15258, 43, 15279, 30},
                {10204, 34, 9586, 24},
                {8213, 25, 5426, 16},
                {11435, 23, 11949, 15},
                {8906, 18, 11971, 9},
            },

            {
                {11380, 85, 11239, 42},
                {14019, 62, 14136, 42},
                {14740, 54, 14712, 42},
                {15281, 64, 15245, 45},
                {10604, 51, 9585, 41},
                {9558, 39, 5122, 29},
                {11714, 37, 11303, 25},
                {7754, 31, 12165, 16},
            },

            {
                {11358, 85, 11239, 27},
                {14067, 49, 14072, 27},
                {14719, 34, 14708, 26},
                {15231, 44, 15307, 28},
                {10185, 33, 9575, 26},
                {9032, 21, 5850, 14},
                {13049, 19, 11323, 10},
                {10850, 16, 10565, 6},
            },

            {
                {11413, 75, 11239, 13},
                {14077, 27, 13964, 13},
                {14708, 16, 14708, 10},
                {15223, 17, 15343, 9},
                {10089, 11, 9582, 8},
                {6935, 5, 6735, 2},
                {13973, 3, 11323, 0},
                {11266, 2, 10180, 0},
            },

            {
                {11239, 72, 11239, 10},
                {13994, 18, 13964, 10},
                {14828, 10, 14830, 8},
                {15223, 10, 15223, 6},
                {9579, 10, 9578, 6},
                {8260, 14, 6999, 9},
                {4488, 11, 3524, 8},
                {12598, 10, 8347, 5},
            },

            {
                {11241, 75, 11239, 22},
                {14081, 40, 14078, 22},
                {14805, 26, 14794, 21},
                {15235, 38, 15223, 24},
                {9555, 37, 9563, 22},
                {8259, 45, 6195, 33},
                {7270, 40, 6465, 31},
                {10795, 36, 9201, 24},
            },

            {
                {11333, 70, 11239, 28},
                {14070, 45, 14194, 28},
                {14780, 37, 14723, 27},
                {15265, 54, 15223, 32},
                {9883, 49, 9582, 29},
                {5337, 52, 5175, 39},
                {9397, 47, 10974, 36},
                {8189, 41, 10096, 28},
            },

            {
                {11307, 73, 11239, 11},
                {13976, 16, 14045, 11},
                {14734, 16, 14794, 10},
                {15244, 15, 15223, 8},
                {9774, 11, 9577, 6},
                {8122, 12, 8317, 6},
                {12826, 7, 9763, 3},
                {8596, 6, 8743, 2},
            },

            {
                {11247, 75, 11241, 1},
                {13940, 3, 14055, 1},
                {14712, 2, 14830, 1},
                {15225, 1, 15224, 0},
                {9504, 0, 9570, 0},
                {11214, 1, 9101, 0},
                {14045, 0, 9082, 0},
                {9508, 0, 8166, 0},
            },

            {
                {11318, 72, 11239, 27},
                {14168, 42, 14243, 27},
                {14796, 35, 14741, 27},
                {15294, 49, 15320, 38},
                {10129, 51, 9570, 37},
                {6677, 45, 5578, 34},
                {5725, 35, 12915, 22},
                {7589, 36, 9480, 25},
            },

            {
                {11285, 73, 11239, 5},
                {14196, 10, 14005, 5},
                {14818, 8, 14808, 5},
                {15227, 11, 15338, 6},
                {9852, 9, 9589, 5},
                {11914, 9, 7300, 5},
                {6305, 4, 12327, 1},
                {10359, 4, 9815, 2},
            },

            {
                {11240, 74, 11239, 0},
                {14187, 0, 13936, 0},
                {14835, 0, 14835, 0},
                {15223, 0, 15351, 0},
                {9498, 0, 9590, 0},
                {15121, 0, 6979, 0},
                {5958, 0, 12148, 0},
                {10689, 0, 9989, 0},
            },

            {
                {11239, 72, 11239, 4},
                {14051, 8, 13939, 4},
                {14832, 3, 14832, 3},
                {15223, 0, 15350, 0},
                {9553, 0, 9589, 0},
                {10822, 0, 6925, 0},
                {6181, 0, 12277, 0},
                {10693, 0, 10019, 0},
            },

            {
                {11268, 77, 11239, 18},
                {13997, 27, 14009, 18},
                {14795, 20, 14794, 16},
                {15232, 18, 15323, 12},
                {9775, 14, 9587, 8},
                {8062, 9, 6158, 5},
                {8945, 8, 12439, 5},
                {10287, 5, 10849, 2},
            },

            {
                {11239, 75, 11239, 0},
                {13936, 0, 13936, 0},
                {14836, 0, 14836, 0},
                {15224, 0, 15223, 0},
                {9590, 0, 9590, 0},
                {6958, 0, 6958, 0},
                {2995, 0, 3034, 0},
                {13206, 0, 8166, 0},
            },

            {
                {11239, 74, 11239, 0},
                {13936, 0, 13936, 0},
                {14836, 0, 14836, 0},
                {15223, 0, 15223, 0},
                {9589, 0, 9589, 0},
                {7018, 0, 6959, 0},
                {2998, 0, 3032, 0},
                {13203, 0, 8165, 0},
            },

            {
                {11239, 73, 11239, 13},
                {14017, 24, 13982, 13},
                {14824, 14, 14825, 11},
                {15224, 16, 15223, 10},
                {9574, 16, 9574, 9},
                {8432, 21, 6921, 14},
                {5181, 17, 3970, 13},
                {12279, 15, 8490, 8},
            },

            {
                {11358, 71, 11239, 18},
                {14007, 25, 14067, 18},
                {14748, 26, 14763, 16},
                {15255, 28, 15223, 15},
                {9987, 21, 9582, 13},
                {6309, 23, 7530, 13},
                {11846, 16, 10368, 10},
                {8149, 14, 9288, 7},
            },

            {
                {11242, 74, 11241, 0},
                {13936, 0, 14060, 0},
                {14709, 0, 14835, 0},
                {15223, 0, 15223, 0},
                {9481, 0, 9570, 0},
                {11897, 0, 9202, 0},
                {14209, 0, 8991, 0},
                {9693, 0, 8108, 0},
            },

            {
                {11506, 77, 11239, 2},
                {14243, 18, 14160, 2},
                {14806, 14, 14843, 4},
                {15265, 28, 15296, 19},
                {10916, 35, 9589, 23},
                {9557, 28, 5174, 21},
                {6947, 22, 8654, 13},
                {9750, 21, 8702, 14},
            },

            {
                {11379, 82, 11239, 3},
                {14263, 22, 14408, 3},
                {14819, 22, 14851, 12},
                {15283, 52, 15272, 40},
                {10514, 65, 9582, 48},
                {11111, 56, 3211, 46},
                {9875, 48, 10110, 35},
                {8780, 49, 8991, 35},
            },

            {
                {11303, 82, 11239, 9},
                {14191, 27, 14428, 9},
                {14804, 25, 14847, 18},
                {15305, 59, 15315, 45},
                {10029, 72, 9565, 53},
                {10256, 64, 3039, 52},
                {11272, 54, 9791, 42},
                {7976, 57, 8880, 42},
            },

            {
                {11392, 79, 11239, 18},
                {14069, 38, 14326, 18},
                {14798, 28, 14817, 21},
                {15282, 53, 15321, 42},
                {10705, 66, 9582, 47},
                {8490, 54, 3556, 42},
                {7649, 44, 10288, 30},
                {8521, 43, 9187, 31},
            },

            {
                {11335, 75, 11239, 11},
                {14074, 25, 14094, 11},
                {14783, 16, 14735, 11},
                {15260, 29, 15321, 19},
                {10263, 31, 9589, 20},
                {6028, 24, 5542, 15},
                {6754, 18, 11251, 9},
                {10939, 16, 9559, 9},
            },

            {
                {11880, 80, 11239, 17},
                {14036, 35, 13936, 17},
                {14822, 11, 14881, 11},
                {15224, 3, 15351, 0},
                {10682, 1, 9590, 0},
                {5441, 0, 6958, 0},
                {4915, 0, 5691, 0},
                {16314, 0, 10066, 0},
            },

            {
                {12294, 92, 11239, 31},
                {14040, 55, 13936, 31},
                {14807, 26, 14891, 24},
                {15224, 13, 15351, 10},
                {13315, 9, 9590, 3},
                {6892, 10, 6954, 5},
                {6414, 0, 4863, 0},
                {16037, 0, 10066, 0},
            },

            {
                {12478, 96, 11239, 61},
                {14011, 88, 13948, 61},
                {14734, 64, 14754, 56},
                {15225, 46, 15351, 35},
                {12208, 34, 9590, 14},
                {8793, 33, 6771, 19},
                {12114, 8, 9408, 0},
                {11539, 2, 10066, 0},
            },

            {
                {11986, 85, 11239, 35},
                {13996, 55, 13945, 35},
                {14784, 40, 14723, 31},
                {15264, 25, 15351, 16},
                {12192, 15, 9590, 5},
                {8991, 13, 6798, 6},
                {6344, 5, 10404, 0},
                {9333, 0, 10066, 0},
            },

            {
                {11588, 78, 11239, 17},
                {14000, 31, 13938, 17},
                {14784, 19, 14713, 14},
                {15310, 9, 15351, 4},
                {11223, 4, 9590, 0},
                {7687, 3, 6901, 0},
                {5559, 1, 10924, 0},
                {8706, 0, 10066, 0},
            },

            {
                {11650, 77, 11239, 23},
                {14058, 35, 13965, 23},
                {14747, 21, 14751, 20},
                {15310, 23, 15294, 15},
                {10586, 19, 9590, 13},
                {7278, 18, 7529, 11},
                {10044, 14, 8401, 8},
                {8887, 8, 9229, 0},
            },

            {
                {12193, 86, 11239, 46},
                {14214, 69, 13962, 46},
                {14768, 45, 14764, 42},
                {15264, 48, 15250, 31},
                {11042, 39, 9590, 26},
                {5491, 36, 7460, 24},
                {9685, 27, 7537, 14},
                {9832, 13, 8571, 0},
            },

            {
                {12565, 96, 11239, 57},
                {14251, 84, 13941, 57},
                {14757, 60, 14735, 52},
                {15228, 61, 15224, 39},
                {11486, 45, 9590, 30},
                {5238, 40, 7080, 27},
                {10682, 27, 9473, 13},
                {10302, 10, 8184, 0},
            },

            {
                {12069, 88, 11239, 32},
                {14053, 48, 13936, 32},
                {14743, 37, 14709, 28},
                {15224, 35, 15223, 17},
                {12179, 19, 9590, 11},
                {5859, 16, 6971, 8},
                {13695, 9, 11252, 3},
                {10581, 1, 8146, 0},
            },

            {
                {11491, 78, 11239, 10},
                {14033, 18, 13936, 10},
                {14795, 12, 14708, 9},
                {15224, 11, 15223, 3},
                {10525, 2, 9590, 1},
                {2892, 2, 6960, 0},
                {14515, 0, 11250, 0},
                {10698, 0, 8146, 0},
            },

            {
                {11877, 84, 11239, 18},
                {14065, 45, 13955, 18},
                {14835, 13, 14735, 11},
                {15232, 3, 15227, 1},
                {12235, 4, 9590, 2},
                {6238, 4, 6732, 2},
                {12676, 1, 10418, 0},
                {10433, 1, 8224, 0},
            },

            {
                {12322, 90, 11239, 27},
                {14068, 53, 14007, 27},
                {14828, 29, 14845, 22},
                {15290, 31, 15292, 23},
                {14021, 39, 9590, 28},
                {7328, 37, 6615, 27},
                {7204, 26, 8480, 17},
                {8532, 28, 9158, 18},
            },

            {
                {12560, 97, 11239, 49},
                {14237, 84, 13977, 49},
                {14748, 47, 14739, 42},
                {15309, 57, 15296, 43},
                {12566, 50, 9590, 38},
                {5504, 48, 6694, 32},
                {9982, 28, 11028, 15},
                {7646, 29, 8387, 13},
            },

            {
                {12207, 96, 11239, 37},
                {14127, 64, 13936, 37},
                {14772, 39, 14708, 30},
                {15239, 41, 15231, 24},
                {12471, 25, 9590, 16},
                {5485, 24, 6954, 9},
                {7091, 8, 11186, 2},
                {9816, 4, 8176, 0},
            },

            {
                {11612, 86, 11239, 16},
                {14081, 34, 13936, 16},
                {14769, 18, 14708, 11},
                {15223, 19, 15223, 6},
                {10966, 7, 9590, 2},
                {5817, 6, 6960, 0},
                {8570, 0, 11210, 0},
                {10600, 0, 8148, 0},
            },

            {
                {11865, 87, 11239, 22},
                {14013, 51, 13937, 22},
                {14708, 16, 14708, 13},
                {15223, 11, 15223, 1},
                {12072, 0, 9590, 0},
                {9377, 0, 6997, 0},
                {12671, 0, 11209, 0},
                {10525, 0, 8194, 0},
            },

            {
                {11601, 96, 11239, 38},
                {14041, 61, 13962, 38},
                {14708, 35, 14708, 32},
                {15240, 29, 15223, 21},
                {10621, 18, 9590, 14},
                {8698, 14, 7470, 10},
                {12024, 11, 11251, 5},
                {8853, 6, 9778, 1},
            },

            {
                {11979, 101, 11239, 62},
                {14171, 90, 13945, 62},
                {14710, 64, 14708, 57},
                {15258, 63, 15223, 45},
                {10106, 44, 9590, 27},
                {4778, 39, 7166, 26},
                {12860, 26, 11671, 11},
                {7279, 6, 9396, 0},
            },

            {
                {11790, 97, 11239, 44},
                {14158, 66, 13936, 44},
                {14722, 48, 14708, 38},
                {15227, 45, 15223, 27},
                {10835, 29, 9590, 12},
                {4788, 22, 6986, 13},
                {11769, 17, 11728, 7},
                {10026, 2, 8227, 0},
            },

            {
                {11424, 92, 11239, 21},
                {14068, 36, 13936, 21},
                {14718, 23, 14708, 15},
                {15224, 18, 15223, 7},
                {10231, 7, 9590, 1},
                {6246, 5, 7010, 1},
                {12935, 4, 11331, 0},
                {11055, 0, 8174, 0},
            },

            {
                {12218, 91, 11239, 38},
                {14083, 57, 13936, 38},
                {14739, 43, 14711, 34},
                {15224, 42, 15223, 22},
                {12320, 25, 9590, 15},
                {6152, 22, 6976, 11},
                {13202, 13, 11134, 5},
                {10520, 2, 8146, 0},
            },

            {
                {11491, 78, 11239, 10},
                {14033, 18, 13936, 10},
                {14795, 12, 14708, 9},
                {15224, 11, 15223, 3},
                {10525, 2, 9590, 1},
                {2892, 2, 6960, 0},
                {14515, 0, 11250, 0},
                {10698, 0, 8146, 0},
            },

            {
                {11248, 76, 11239, 0},
                {14061, 1, 13936, 0},
                {14833, 0, 14708, 0},
                {15224, 0, 15223, 0},
                {9579, 0, 9590, 0},
                {705, 0, 6958, 0},
                {14602, 0, 11205, 0},
                {10710, 0, 8146, 0},
            },

            {
                {11282, 76, 11239, 0},
                {14068, 4, 13936, 0},
                {14836, 0, 14708, 0},
                {15224, 0, 15223, 0},
                {9717, 0, 9590, 0},
                {1004, 0, 6957, 0},
                {14599, 0, 11204, 0},
                {10709, 0, 8146, 0},
            },

            {
                {11547, 79, 11239, 9},
                {14070, 27, 13938, 9},
                {14836, 4, 14711, 5},
                {15224, 0, 15223, 0},
                {10783, 0, 9590, 0},
                {3508, 0, 6925, 0},
                {14207, 0, 11080, 0},
                {10666, 0, 8150, 0},
            },

            {
                {11240, 75, 11239, 0},
                {14065, 0, 13937, 0},
                {14708, 0, 14708, 0},
                {15224, 0, 15223, 0},
                {9548, 0, 9590, 0},
                {5150, 0, 6960, 0},
                {15783, 0, 11214, 0},
                {10706, 0, 8146, 0},
            },

            {
                {11306, 75, 11239, 0},
                {14065, 2, 13936, 0},
                {14708, 0, 14708, 0},
                {15224, 0, 15223, 0},
                {9893, 0, 9590, 0},
                {5251, 0, 6959, 0},
                {15759, 0, 11214, 0},
                {10706, 0, 8146, 0},
            },

            {
                {11788, 80, 11239, 13},
                {14036, 35, 13936, 13},
                {14708, 9, 14708, 7},
                {15223, 5, 15223, 0},
                {11913, 0, 9590, 0},
                {7716, 0, 6963, 0},
                {14317, 0, 11212, 0},
                {10687, 0, 8146, 0},
            },

            {
                {11777, 91, 11239, 27},
                {14008, 55, 13941, 27},
                {14708, 20, 14708, 18},
                {15224, 14, 15223, 5},
                {11612, 3, 9590, 3},
                {9803, 2, 7085, 2},
                {11890, 1, 11206, 1},
                {10212, 1, 8398, 0},
            },

            {
                {11621, 95, 11239, 34},
                {14027, 58, 13956, 34},
                {14708, 30, 14708, 28},
                {15233, 24, 15223, 16},
                {10811, 13, 9590, 11},
                {9183, 10, 7369, 7},
                {11796, 8, 11227, 4},
                {9291, 4, 9337, 1},
            },

            {
                {11239, 74, 11239, 0},
                {13937, 0, 13936, 0},
                {14709, 0, 14710, 0},
                {15225, 0, 15223, 0},
                {9561, 0, 9590, 0},
                {7144, 0, 7029, 0},
                {14244, 0, 11183, 0},
                {11200, 0, 8174, 0},
            },

            {
                {11268, 72, 11239, 4},
                {13960, 13, 13936, 4},
                {14708, 5, 14709, 1},
                {15225, 0, 15223, 0},
                {9801, 0, 9590, 0},
                {8457, 0, 6981, 0},
                {14242, 0, 11184, 0},
                {11198, 0, 8174, 0},
            },

            {
                {11606, 79, 11239, 17},
                {14030, 40, 13936, 17},
                {14708, 17, 14709, 11},
                {15235, 4, 15224, 2},
                {11044, 5, 9590, 0},
                {7816, 1, 6959, 0},
                {13986, 0, 11473, 0},
                {10324, 0, 8131, 0},
            },

            {
                {12280, 93, 11239, 45},
                {14207, 74, 13936, 45},
                {14712, 45, 14753, 39},
                {15248, 32, 15268, 25},
                {13338, 29, 9590, 15},
                {8095, 24, 6963, 13},
                {12411, 12, 9406, 2},
                {9170, 10, 6686, 0},
            },

            {
                {12367, 95, 11239, 43},
                {14189, 67, 13936, 43},
                {14737, 47, 14747, 38},
                {15288, 34, 15227, 25},
                {14071, 30, 9590, 17},
                {8485, 24, 6975, 13},
                {11135, 10, 10765, 1},
                {5761, 10, 8129, 0},
            },

            {
                {11423, 77, 11239, 16},
                {14079, 31, 14033, 16},
                {14794, 15, 14771, 13},
                {15283, 13, 15240, 8},
                {10636, 13, 9590, 6},
                {8637, 12, 6325, 6},
                {9579, 4, 9931, 0},
                {8370, 4, 12729, 0},
            },

            {
                {12105, 90, 11239, 18},
                {14253, 64, 14231, 18},
                {14844, 24, 14896, 13},
                {15305, 35, 15330, 20},
                {13058, 52, 9590, 36},
                {5466, 48, 4365, 36},
                {11462, 31, 7144, 18},
                {7584, 34, 10308, 22},
            },

            {
                {12377, 96, 11239, 40},
                {14092, 76, 13999, 40},
                {14749, 40, 14752, 31},
                {15278, 53, 15252, 39},
                {13937, 54, 9590, 37},
                {7337, 43, 6470, 31},
                {11206, 27, 10206, 15},
                {8562, 24, 8662, 14},
            },

            {
                {12279, 93, 11239, 31},
                {13996, 55, 13936, 31},
                {14756, 35, 14708, 24},
                {15228, 32, 15223, 17},
                {13859, 26, 9590, 12},
                {5897, 16, 6998, 8},
                {10160, 9, 11173, 0},
                {10177, 1, 8152, 0},
            },

            {
                {11698, 81, 11239, 14},
                {14005, 29, 13936, 14},
                {14789, 15, 14708, 10},
                {15224, 9, 15223, 2},
                {11707, 4, 9590, 0},
                {4836, 1, 6972, 0},
                {9123, 0, 11166, 0},
                {10645, 0, 8146, 0},
            },

            {
                {11689, 83, 11239, 20},
                {14151, 40, 13985, 20},
                {14808, 22, 14708, 17},
                {15225, 15, 15224, 10},
                {10924, 13, 9590, 8},
                {7482, 10, 6575, 2},
                {9302, 0, 11840, 0},
                {10590, 0, 8146, 0},
            },

            {
                {12339, 96, 11239, 35},
                {14199, 68, 14059, 35},
                {14749, 37, 14708, 29},
                {15225, 34, 15224, 23},
                {12866, 28, 9590, 19},
                {6783, 18, 5998, 3},
                {12081, 2, 11779, 0},
                {10304, 0, 8146, 0},
            },

            {
                {12530, 99, 11239, 38},
                {14091, 79, 13936, 38},
                {14778, 40, 14709, 30},
                {15225, 33, 15224, 21},
                {11190, 23, 9590, 13},
                {8414, 3, 6980, 0},
                {11695, 0, 11748, 0},
                {10320, 0, 8146, 0},
            },

            {
                {12331, 98, 11239, 29},
                {14086, 56, 13936, 29},
                {14739, 31, 14709, 21},
                {15225, 24, 15224, 12},
                {13778, 15, 9590, 5},
                {8246, 1, 7001, 0},
                {13109, 0, 11764, 0},
                {10201, 0, 8146, 0},
            },

            {
                {12114, 92, 11239, 19},
                {14114, 49, 13936, 19},
                {14710, 20, 14709, 11},
                {15225, 12, 15224, 3},
                {13073, 5, 9590, 0},
                {6896, 0, 6974, 0},
                {12850, 0, 11804, 0},
                {8879, 0, 8146, 0},
            },

            {
                {11820, 81, 11239, 24},
                {13966, 40, 13936, 24},
                {14733, 24, 14709, 18},
                {15224, 16, 15224, 12},
                {10614, 18, 9590, 9},
                {7062, 13, 6962, 2},
                {10986, 6, 11240, 0},
                {8608, 0, 8146, 0},
            },

            {
                {12605, 98, 11239, 44},
                {13948, 78, 13936, 44},
                {14787, 49, 14709, 35},
                {15224, 33, 15224, 24},
                {10699, 36, 9590, 22},
                {7584, 23, 6964, 6},
                {8538, 9, 11255, 0},
                {8569, 0, 8146, 0},
            },

            {
                {12774, 101, 11239, 44},
                {13975, 82, 13936, 44},
                {14821, 52, 14709, 33},
                {15224, 28, 15224, 18},
                {10369, 23, 9590, 11},
                {8628, 7, 6965, 1},
                {10355, 5, 11518, 0},
                {9742, 0, 8146, 0},
            },

            {
                {12401, 98, 11239, 34},
                {13987, 65, 13936, 34},
                {14815, 39, 14709, 24},
                {15224, 24, 15224, 12},
                {14066, 17, 9590, 5},
                {7941, 2, 6949, 0},
                {12006, 1, 11921, 0},
                {9897, 0, 8146, 0},
            },

            {
                {12277, 91, 11239, 21},
                {14028, 53, 13936, 21},
                {14797, 22, 14709, 12},
                {15224, 13, 15224, 4},
                {13986, 4, 9590, 0},
                {7840, 0, 6947, 0},
                {12851, 0, 13402, 0},
                {9708, 0, 8146, 0},
            },

            {
                {11689, 83, 11239, 24},
                {13967, 41, 13936, 24},
                {14821, 24, 14709, 20},
                {15224, 24, 15224, 16},
                {10910, 17, 9590, 13},
                {9392, 20, 6990, 12},
                {4794, 0, 13013, 0},
                {9013, 6, 8141, 0},
            },

            {
                {12592, 104, 11239, 55},
                {13973, 93, 13936, 55},
                {14740, 56, 14709, 48},
                {15224, 52, 15224, 37},
                {13563, 38, 9590, 30},
                {8070, 35, 6974, 20},
                {11979, 3, 11425, 0},
                {8154, 15, 8111, 1},
            },

            {
                {12663, 99, 11239, 47},
                {13937, 83, 13936, 47},
                {14709, 53, 14709, 38},
                {15224, 31, 15225, 19},
                {11718, 28, 9590, 16},
                {8404, 7, 6979, 0},
                {13807, 5, 11227, 0},
                {9670, 0, 8103, 0},
            },

            {
                {12268, 95, 11239, 35},
                {13936, 71, 13936, 35},
                {14723, 40, 14709, 26},
                {15224, 17, 15225, 7},
                {13529, 20, 9590, 7},
                {7259, 0, 6972, 0},
                {12772, 0, 11228, 0},
                {10663, 0, 8104, 0},
            },

            {
                {12050, 89, 11239, 23},
                {13945, 55, 13936, 23},
                {14737, 26, 14709, 15},
                {15224, 6, 15225, 0},
                {12934, 7, 9590, 0},
                {6755, 0, 6978, 0},
                {11951, 0, 11228, 0},
                {10685, 0, 8104, 0},
            },

            {
                {11239, 75, 11239, 0},
                {13936, 0, 13936, 0},
                {14710, 0, 14709, 0},
                {15225, 0, 15224, 0},
                {9589, 0, 9590, 0},
                {6954, 0, 6958, 0},
                {11178, 0, 11251, 0},
                {8600, 0, 8146, 0},
            },

            {
                {11239, 74, 11239, 0},
                {13936, 0, 13936, 0},
                {14709, 0, 14709, 0},
                {15225, 0, 15224, 0},
                {9593, 0, 9590, 0},
                {6958, 0, 6957, 0},
                {11174, 0, 11250, 0},
                {8600, 0, 8146, 0},
            },

            {
                {11347, 73, 11239, 7},
                {13947, 11, 13936, 7},
                {14713, 6, 14709, 5},
                {15224, 2, 15224, 1},
                {10090, 2, 9590, 0},
                {7027, 1, 6957, 0},
                {11154, 0, 11248, 0},
                {8612, 0, 8146, 0},
            },

            {
                {11998, 85, 11239, 29},
                {13966, 49, 13936, 29},
                {14741, 30, 14709, 22},
                {15224, 20, 15224, 15},
                {10575, 23, 9590, 12},
                {7069, 16, 6963, 3},
                {10763, 8, 11238, 0},
                {8588, 0, 8146, 0},
            },

            {
                {12493, 96, 11239, 42},
                {13953, 73, 13936, 42},
                {14774, 46, 14709, 32},
                {15224, 31, 15224, 23},
                {10547, 34, 9590, 21},
                {7345, 23, 6965, 6},
                {9223, 9, 11245, 0},
                {8544, 0, 8146, 0},
            },

            {
                {11666, 79, 11239, 7},
                {14055, 25, 13936, 7},
                {14833, 8, 14709, 2},
                {15224, 0, 15224, 0},
                {11821, 0, 9590, 0},
                {5807, 0, 6962, 0},
                {9820, 0, 14300, 0},
                {9706, 0, 8146, 0},
            },

            {
                {11275, 74, 11239, 0},
                {13954, 4, 13936, 0},
                {14837, 1, 14709, 0},
                {15224, 0, 15224, 0},
                {9841, 0, 9590, 0},
                {6810, 0, 6975, 0},
                {3771, 0, 14323, 0},
                {9706, 0, 8146, 0},
            },

            {
                {11239, 74, 11239, 0},
                {13936, 0, 13936, 0},
                {14837, 0, 14709, 0},
                {15224, 0, 15224, 0},
                {9589, 0, 9590, 0},
                {7236, 0, 6978, 0},
                {2985, 0, 14320, 0},
                {9704, 0, 8146, 0},
            },

            {
                {11430, 76, 11239, 12},
                {13954, 23, 13936, 12},
                {14832, 12, 14709, 10},
                {15224, 11, 15224, 7},
                {10146, 8, 9590, 6},
                {9493, 9, 6984, 5},
                {3740, 0, 13660, 0},
                {9361, 2, 8144, 0},
            },

            {
                {11855, 87, 11239, 31},
                {13973, 52, 13936, 31},
                {14813, 31, 14709, 26},
                {15224, 32, 15224, 21},
                {11403, 22, 9590, 17},
                {9124, 26, 6992, 15},
                {5587, 0, 12660, 0},
                {8817, 9, 8138, 0},
            },

            {
                {12188, 93, 11239, 31},
                {13938, 65, 13937, 31},
                {14733, 36, 14709, 21},
                {15224, 13, 15225, 4},
                {13373, 15, 9590, 4},
                {6975, 0, 6975, 0},
                {12196, 0, 11228, 0},
                {10682, 0, 8104, 0},
            },

            {
                {12050, 89, 11239, 23},
                {13945, 55, 13936, 23},
                {14737, 26, 14709, 15},
                {15224, 6, 15225, 0},
                {12934, 7, 9590, 0},
                {6755, 0, 6978, 0},
                {11951, 0, 11228, 0},
                {10685, 0, 8104, 0},
            },

            {
                {11602, 78, 11239, 9},
                {13949, 24, 13936, 9},
                {14714, 8, 14709, 5},
                {15224, 0, 15225, 0},
                {11373, 0, 9590, 0},
                {6905, 0, 6967, 0},
                {12604, 0, 11228, 0},
                {10686, 0, 8104, 0},
            },

            {
                {11261, 75, 11239, 0},
                {13937, 0, 13936, 0},
                {14709, 0, 14709, 0},
                {15224, 0, 15225, 0},
                {9739, 0, 9590, 0},
                {6969, 0, 6958, 0},
                {12778, 0, 11228, 0},
                {10686, 0, 8104, 0},
            },

            {
                {11239, 75, 11239, 0},
                {13936, 0, 13936, 0},
                {14709, 0, 14709, 0},
                {15224, 0, 15225, 0},
                {9593, 0, 9590, 0},
                {6946, 0, 6958, 0},
                {12778, 0, 11227, 0},
                {10686, 0, 8104, 0},
            },

            {
                {12605, 98, 11239, 44},
                {13948, 78, 13936, 44},
                {14787, 49, 14709, 35},
                {15224, 33, 15224, 24},
                {10699, 36, 9590, 22},
                {7584, 23, 6964, 6},
                {8538, 9, 11255, 0},
                {8569, 0, 8146, 0},
            },

            {
                {12816, 102, 11239, 47},
                {13944, 89, 13936, 47},
                {14820, 54, 14709, 36},
                {15224, 35, 15224, 23},
                {10702, 29, 9590, 18},
                {8988, 15, 6959, 4},
                {7688, 8, 11397, 0},
                {9105, 0, 8146, 0},
            },

            {
                {12655, 101, 11239, 42},
                {13984, 77, 13936, 42},
                {14825, 50, 14709, 31},
                {15224, 25, 15224, 16},
                {11425, 23, 9590, 9},
                {8258, 5, 6966, 0},
                {11320, 4, 11603, 0},
                {10005, 0, 8146, 0},
            },

            {
                {12392, 98, 11239, 32},
                {13993, 63, 13936, 32},
                {14807, 36, 14709, 22},
                {15224, 23, 15224, 11},
                {14145, 15, 9590, 4},
                {8045, 1, 6947, 0},
                {12195, 0, 12038, 0},
                {9831, 0, 8146, 0},
            },

            {
                {12324, 93, 11239, 23},
                {14020, 56, 13936, 23},
                {14793, 26, 14709, 14},
                {15224, 15, 15224, 6},
                {14100, 6, 9590, 1},
                {8066, 0, 6945, 0},
                {12840, 0, 13081, 0},
                {9713, 0, 8146, 0},
            },

            {
                {12195, 95, 11239, 43},
                {13981, 72, 13936, 43},
                {14790, 44, 14709, 38},
                {15224, 45, 15224, 31},
                {12417, 32, 9590, 25},
                {8483, 36, 6993, 22},
                {7655, 0, 12041, 0},
                {8466, 14, 8130, 0},
            },

            {
                {12626, 105, 11239, 55},
                {13967, 94, 13936, 55},
                {14731, 57, 14709, 48},
                {15224, 51, 15224, 35},
                {13592, 37, 9590, 29},
                {8153, 31, 6969, 17},
                {12681, 3, 11360, 0},
                {8148, 14, 8107, 1},
            },

            {
                {12722, 101, 11239, 50},
                {13937, 87, 13936, 50},
                {14709, 56, 14709, 41},
                {15224, 35, 15224, 23},
                {11646, 30, 9590, 19},
                {8402, 10, 6978, 0},
                {13627, 6, 11227, 0},
                {9083, 0, 8103, 0},
            },

            {
                {12488, 98, 11239, 43},
                {13936, 78, 13936, 43},
                {14710, 48, 14709, 34},
                {15224, 25, 15225, 14},
                {12839, 25, 9590, 12},
                {8135, 3, 6973, 0},
                {13826, 2, 11227, 0},
                {10362, 0, 8104, 0},
            },

            {
                {12268, 95, 11239, 35},
                {13936, 71, 13936, 35},
                {14723, 40, 14709, 26},
                {15224, 17, 15225, 7},
                {13529, 20, 9590, 7},
                {7259, 0, 6972, 0},
                {12772, 0, 11228, 0},
                {10663, 0, 8104, 0},
            },

            {
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
            },

            {
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
            },

            {
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
            },

            {
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
            },

            {
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
            },
        };
}
