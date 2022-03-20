package edisyn.synth.kurzweilk2600;
import edisyn.*;
import static edisyn.synth.kurzweilk2600.KurzweilK2600.*;

import java.util.*;

class FxStudio
    {
    static final int[] ALG_MAP = new int [] { 
        0, 
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,             // Reverb
        130, 131, 132, 133, 134, 135, 136, 138, 139, 140,              // Delay
        150, 151, 152, 153, 154, 155, 156, 157, 158, 159, 160, 161,    // Chorus / Flange / Phase
        700, 701, 702, 703, 704, 705, 706, 707, 708, 709,              // Combination
        710, 711, 712, 713, 714, 715, 716, 717, 718, 719,
        720, 721, 722, 723, 
        724, 725, 726, 727, 728, 729, 730, 731, 732,                   // Distortion
        733, 734, 735, 736, 737, 738, 739, 740, 741, 742, 743, 744, 745, 746,         // Tone Wheel Organ
        781, 784, 790, 792,                                            // New
        900, 901, 902, 903, 904, 905, 906, 907, 908, 909,              // Special FX
        910, 911, 912, 913, 914, 915, 916, 917, 918, 919,
        920, 948, 949,
        950, 951, 952, 953, 954, 955, 956, 957, 958, 959,              // Studio / Mixdown FX
        960, 961, 962, 963, 964, 965, 966, 967, 968, 969,
        970, 971, 972, 975, 
        998, 999                                                       // Tools
        };
        
    static final int FX_BUS_NONE = 0;
    static final int FX_BUS_AIN = 1;
    static final int FX_BUS_AINR = 2;
    static final int FX_BUS_BIN = 3;
    static final int FX_BUS_BINR = 4;
    static final int FX_BUS_CIN = 5;
    static final int FX_BUS_CINR = 6;
    static final int FX_BUS_DIN = 7;
    static final int FX_BUS_DINR = 8;
    static final int FX_BUS_FX1 = 9;
    static final int FX_BUS_FX2 = 10;
    static final int FX_BUS_FX3 = 11;
    static final int FX_BUS_FX4 = 12;
    static final int FX_BUS_AUX = 13;
    static final int FX_BUS_MIX = 14;
        
    static final int NUM_IN_BUSES = 8;
    static final int NUM_FX_BUSES = 5; // including aux
        
    static final int FX_ALG_NONE = 0;
    static final int FX_ALG_MIX = 138;
    static final int FX_ALG_FX_EMPTY = 139;
    static final int FX_ALG_AUX_EMPTY = 140;
    static final int FX_ALG_IN = 141;
    static final int MAX_NUM_FX_ALGS = 149;
        
    static final String[] FX_BUS = new String[] { "None", "InA", "InAR", "InB", "InBR", 
                                                  "InC", "InCR", "InD", "InDR", "FX1", "FX2", "FX3", "FX4", "Aux", "Mix"};
    static int nopresreq = 0; // no of presets requested
    static int nopresgot = 0; // no of presets received
    static int presetno = 0;
    int presetid[] = new int[] {0, 0, 0, 0, 0};
    int algid[] = new int[] {FX_ALG_NONE, 
                             FX_ALG_IN + 0, FX_ALG_IN + 1, FX_ALG_IN + 2, FX_ALG_IN + 3, 
                             FX_ALG_IN + 4, FX_ALG_IN + 5, FX_ALG_IN + 6, FX_ALG_IN + 7,
                             FX_ALG_FX_EMPTY, FX_ALG_FX_EMPTY, FX_ALG_FX_EMPTY, FX_ALG_FX_EMPTY, 
                             FX_ALG_AUX_EMPTY, FX_ALG_MIX};
    ArrayList<Integer> parfxidxin[]; // variable lenght for inbus params, depend on studio
    ArrayList<Integer> parmapin[];
    ArrayList<Integer> busmap;
    boolean busmapaddR[] = new boolean[] {false, false, false, false}; // In right channel to be added
        
    KurzweilK2600 synth;
    public FxStudio(KurzweilK2600 synth) 
        {
        this.synth = synth;
        parfxidxin = new ArrayList[NUM_IN_BUSES];
        parmapin = new ArrayList[NUM_IN_BUSES];
        for (int inbus = 0; inbus < NUM_IN_BUSES; inbus++)
            {
            parfxidxin[inbus] = new ArrayList<Integer>();
            parfxidxin[inbus].add(0);
            parmapin[inbus] = new ArrayList<Integer>();
            parmapin[inbus].add(0);
            }
        busmap = new ArrayList<Integer>();
        busmap.add(FX_BUS_NONE);
        busmap.add(FX_BUS_AIN);
        busmap.add(FX_BUS_BIN);
        busmap.add(FX_BUS_CIN);
        busmap.add(FX_BUS_DIN);
        busmap.add(FX_BUS_FX1);
        busmap.add(FX_BUS_FX2);
        busmap.add(FX_BUS_FX3);
        busmap.add(FX_BUS_FX4);
        busmap.add(FX_BUS_AUX);
        busmap.add(FX_BUS_MIX);
        }
        
    public void getStudio()
        {
        System.out.println("getStudio");
        if ( (synth.tuple != null) && (synth.getModel().get("fmt") >= FORMAT_4) )
            {
            synth.getModel().set("studiogot", 0);
            synth.getKdfxObject(STUDIO_TYPE, synth.getModel().get("studio"));
            }
        else synth.getModel().set("studiogot", 1);
        }
        
    public void getPresets(byte[] d)
        { // studio received, get info from studio, get presets
        System.out.println("getPresets");
        nopresreq = 0;
        nopresgot = 0;
        presetno = 0;
        for (int r = 0; r < 4; r++) {busmapaddR[r] = false;}
            
        for (int inbus = 0; inbus < NUM_IN_BUSES; inbus++)
            {
            parfxidxin[inbus].clear();
            parfxidxin[inbus].add(0);
            parmapin[inbus].clear();
            parmapin[inbus].add(0);
            switch(d[41 + inbus * 32]) // eq 1 type
                {
                case 1 : // LoShelf
                    parfxidxin[inbus].add(5); // EQ1 Bass G
                    parfxidxin[inbus].add(6); // EQ1 Bass F
                    parmapin[inbus].add(1);
                    parmapin[inbus].add(2);
                    break;
                case 2 : // HiShelf
                    parfxidxin[inbus].add(261); // EQ1 Treb G
                    parfxidxin[inbus].add(262); // EQ1 Treb F
                    parmapin[inbus].add(1);
                    parmapin[inbus].add(2);
                    break;
                case 3 : // LoPass1
                    parfxidxin[inbus].add(263); // EQ1 LP-1 F
                    parmapin[inbus].add(1);
                    break;
                case 4 : // LoPass2
                    parfxidxin[inbus].add(264); // EQ1 LP-2 F
                    parmapin[inbus].add(1);
                    break;
                case 5 : // Lopas1
                    parfxidxin[inbus].add(265); // EQ1 HP-1 F
                    parmapin[inbus].add(1);
                    break;
                case 6 : // Lopas2
                    parfxidxin[inbus].add(266); // EQ1 HP-2 F
                    parmapin[inbus].add(1);
                    break;
                case 7 : // ParaMid
                    parfxidxin[inbus].add(267); // EQ1 Treb G
                    parfxidxin[inbus].add(268); // EQ1 Treb F
                    parmapin[inbus].add(1);
                    parmapin[inbus].add(2);
                    break;
                default:
                    break;
                }
            switch(d[46 + inbus * 32]) // eq 2 type
                {
                case 1 : // LoShelf
                    parfxidxin[inbus].add(269); // EQ2 Bass G
                    parfxidxin[inbus].add(270); // EQ2 Bass F
                    parmapin[inbus].add(4);
                    parmapin[inbus].add(5);
                    break;
                case 2 : // HiShelf
                    parfxidxin[inbus].add(7); // EQ2 Treb G
                    parfxidxin[inbus].add(8); // EQ2 Treb F
                    parmapin[inbus].add(4);
                    parmapin[inbus].add(5);
                    break;
                case 3 : // LoPass1
                    parfxidxin[inbus].add(271); // EQ2 LP-1 F
                    parmapin[inbus].add(4);
                    break;
                case 4 : // LoPass2
                    parfxidxin[inbus].add(272); // EQ2 LP-2 F
                    parmapin[inbus].add(4);
                    break;
                case 5 : // Lopas1
                    parfxidxin[inbus].add(273); // EQ2 HP-1 F
                    parmapin[inbus].add(4);
                    break;
                default:
                    break;
                }
            if (d[52 + inbus * 32] >= 0) // send 1 assign
                {
                parfxidxin[inbus].add(9); // Send1Lvl
                parmapin[inbus].add(7);
                if (d[38 + (inbus / 2) * 64] == 2) // Mono
                    {
                    parfxidxin[inbus].add(10); // Send1Pan
                    parmapin[inbus].add(8);
                    busmapaddR[inbus / 2] = true;
                    }
                else
                    {
                    if (d[38 + (inbus / 2) * 64] == 0) // Stereo Pan
                        {
                        parfxidxin[inbus].add(10); // Send1Pan
                        parmapin[inbus].add(8);
                        }
                    else // Stereo Bal
                        {
                        parfxidxin[inbus].add(274); // Send1Bal
                        parmapin[inbus].add(8);
                        }
                    parfxidxin[inbus].add(11); // Send1Width
                    parmapin[inbus].add(9);
                    }
                }
            if (d[58 + inbus * 32] >= 0) // send 2 assign
                {
                parfxidxin[inbus].add(12); // Send2Lvl
                parmapin[inbus].add(10);
                if (d[38 + (inbus / 2) * 64] == 2) // Mono
                    {
                    parfxidxin[inbus].add(13); // Send2Pan
                    parmapin[inbus].add(11);
                    }
                else
                    {
                    if (d[38 + (inbus / 2) * 64] == 0) // Stereo Pan
                        {
                        parfxidxin[inbus].add(13); // Send2Pan
                        parmapin[inbus].add(11);
                        }
                    else // Stereo Bal
                        {
                        parfxidxin[inbus].add(275); // Send2Bal
                        parmapin[inbus].add(11);
                        }
                    parfxidxin[inbus].add(14); // Send2Width
                    parmapin[inbus].add(12);
                    }
                }
            }
            
        busmap.clear();
        busmap.add(FX_BUS_NONE);
        busmap.add(FX_BUS_AIN); // AIn(L)
        if (busmapaddR[0]) busmap.add(FX_BUS_AINR);
        busmap.add(FX_BUS_BIN); // BIn(L)
        if (busmapaddR[1]) busmap.add(4);
        busmap.add(FX_BUS_CIN); // CIn(L)
        if (busmapaddR[2]) busmap.add(FX_BUS_CINR);
        busmap.add(FX_BUS_DIN); // DIn(L)
        if (busmapaddR[3]) busmap.add(FX_BUS_DINR);
        busmap.add(FX_BUS_FX1);
        busmap.add(FX_BUS_FX2);
        busmap.add(FX_BUS_FX3);
        busmap.add(FX_BUS_FX4);
        busmap.add(FX_BUS_AUX);
        busmap.add(FX_BUS_MIX);
            
        for (int fxp = 0; fxp < 4; fxp++)
            {
            algid[fxp + FX_BUS_FX1] = FX_ALG_FX_EMPTY;
            presetid[fxp] = 0;
                
            if (d[296 + 20 * fxp] != 0) // allocated ?
                {
                presetid[fxp] = (int)(d[294 + 20 * fxp] & 0xFF) * 256 + (int)(d[295 + 20 * fxp] & 0xFF);
                if (presetid[fxp] != 0)
                    {
                    synth.getKdfxObject(FX_PRESET_TYPE, presetid[fxp]);
                    nopresreq++;
                    }
                // System.out.println("nopresreq " + nopresreq + " presetid "+ presetid[fxp]);
                }
            }
        algid[FX_BUS_AUX] = FX_ALG_AUX_EMPTY;
        presetid[4] = (int)(d[2] & 0xFF) * 256 + (int)(d[3] & 0xFF);
        if (presetid[4] != 0)
            {
            synth.getKdfxObject(FX_PRESET_TYPE, presetid[4]);
            nopresreq++;
            }
        }
        
    public boolean getAlgsfrompreset(byte[] d, int id)
        { // preset received, get algorithm
        System.err.println("getAlgsfrompreset");
        int fxalgindex = 0;
        int fxp = 0;
        boolean allalgsreceived = false;
            
        while (presetid[presetno] == 0) presetno++; // skip unused or empty presets
        fxalgindex = presetno;
        presetno++;
        algid[fxalgindex + FX_BUS_FX1] = synth.getmapindex((int)(d[2] & 0xFF) * 256 + (int)(d[3] & 0xFF), ALG_MAP);
        if (algid[fxalgindex + FX_BUS_FX1] == FX_ALG_NONE) algid[fxalgindex + FX_BUS_FX1] = FX_ALG_FX_EMPTY;
        if (algid[FX_BUS_AUX] == FX_ALG_NONE) algid[FX_BUS_AUX] = FX_ALG_AUX_EMPTY;
        nopresgot++;
        // System.out.println("nopresgot " + nopresgot + " fgalgindex " + fxalgindex + " fx algorithm " + algid[fxalgindex + 9]);
        if (nopresreq == nopresgot) 
            {
            System.out.println("all algs received");
            allalgsreceived = true;
            synth.getModel().set("studiogot", 1);
            }
        return allalgsreceived;
        }
        
    public int[] getAlgidx()
        { // array of 15 algorithm numbers for NONE, InA() - InDR,  FX1 - FX4 and aux and mix
        return algid;
        }
        
    public int[] getParfxidxin(int inbus)
        {
        // System.out.println("inbus " + inbus + " parfx " + parfxidxin[inbus] + " parmap " + parmapin[inbus]);
        int size = parfxidxin[inbus].size();
        int[] pi = new int[size];
        for (int i = 0; i < size; i++)
            {
            pi[i] = parfxidxin[inbus].get(i);
            }
        return pi;
        }
        
    public int[] getParmapin(int inbus)
        {
        int size = parmapin[inbus].size();
        int[] pm = new int[size];
        for (int i = 0; i < size; i++)
            {
            pm[i] = parmapin[inbus].get(i);
            }
        return pm;
        }
        
    public int[] getBusmapint()
        {
        int[] bm = new int[busmap.size()];
        for (int i = 0; i < busmap.size(); i++) { bm[i] = busmap.get(i); }
        return bm;
        }
        
    public String[] getBusmapstr()
        {
        //System.out.println(busmap);
        String[] bm = new String[busmap.size()];
        for (int i = 0; i < busmap.size(); i++)
            {
            bm[i] = FX_BUS[busmap.get(i)]; 
            if ( (busmap.get(i) == FX_BUS_AIN) && (busmapaddR[0]) ) bm[i] = "InAL";
            if ( (busmap.get(i) == FX_BUS_BIN) && (busmapaddR[1]) ) bm[i] = "InBL";
            if ( (busmap.get(i) == FX_BUS_CIN) && (busmapaddR[2]) ) bm[i] = "InCL";
            if ( (busmap.get(i) == FX_BUS_DIN) && (busmapaddR[3]) ) bm[i] = "InDL";
            }
        return bm;
        }
    }
