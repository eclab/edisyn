/***
    Copyright 2021 by Wim Verheyen
    Licensed under the Apache License version 2.0
    
    Set K device ID 0
    Set K program change method Extended
    TODO : Doc in about : Request current patch returns program 202, since there is no way to get the current edit buffer
    TODO : Doc in about : Send / write patch : patch must not be in edit mode on K
    TODO : Doc in about : startup time is 10 seconds
    TODO : Doc in about : keymap and studio names not displayed until after load from K
    TODO : make pastable
    TODO : Identify kb3 program
    TODO : sanity check 
    TODO : Borderlayout
    TODO : apparently triple is format 5
    TODO : load program 1 and check keymap names
*/


/**
   A patch editor for the Kurzweil K2600
   @author Wim Verheyen
*/
// Compile with javac edisyn/*.java edisyn/*/*.java edisyn/*/*/*.java
// Run with java -cp libraries/coremidi4j-1.6.jar:. edisyn.Edisyn
// java edisyn.test.SanityCheck

package edisyn.synth.kurzweilk2600;

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
import java.time.*;

public class KurzweilK2600 extends Synth
    {
    static final ImageIcon[] ALGORITHM_ICONS = 
        {
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),  // alg 0 not used
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg2.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg3.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg4.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg5.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg6.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg7.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg8.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg9.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg10.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg11.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg12.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg13.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg14.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg15.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg16.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg17.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg18.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg19.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg20.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg21.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg22.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg23.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg24.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg25.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg26.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg27.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg28.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg29.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg30.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg31.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg31.png")), // alg 32 not used
        new ImageIcon(KurzweilK2600.class.getResource("alg33.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg34.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg35.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg36.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg37.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg38.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg39.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg40.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")), // TODO
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")), // TODO
        new ImageIcon(KurzweilK2600.class.getResource("alg81.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")), // TODO
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")), // TODO
        new ImageIcon(KurzweilK2600.class.getResource("alg101.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")), // TODO
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")),
        new ImageIcon(KurzweilK2600.class.getResource("alg1.png")), // TODO
        };
    static final String[] BANKS = new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
    
    static final String[] CONTROL_LIST = new String[] {
        "Off", "ModWheel", "Breath", "Midi03", "Foot", "PortTim", "Data", "Volume", "Balance", "Midi09", 
        "Pan", "Express", "Midi12", "Midi13", "Midi14", "Midi15", "Ctl A", "Ctl B", "Ctl C", "Ctl D", 
        "Midi20", "Midi21", "Midi22", "Midi23", "Midi24", "Midi25", "Midi26", "Midi27", "Midi28", "Midi29", 
        "Midi30", "Midi31", "Chan St", "MPress", "BMPres", "PWHeel", "Bi-Mwl", "AbsPwl", "ASR2", "FUN2", 
        "LFO2", "LFO2ph", "FUN4", "Vol  Ctl", "Bal Ctl", "Pan Ctl", "ChanCnt", "A Clk4", "~AClk4", "B Clk4", 
        "~BClk4", "A Clk2", "~AClk2", "B Clk2", "~BClk2", "Tempo", "AClock", "~A Clk", "BClock", "~B Clk", 
        "GPhas1", "GPhas2", "GRand1", "GRand2", "Sustain", "PortSw", "SostPd", "SoftPd", "LegatoSW", "FrezPd", 
        "Midi70", "Midi71", "Midi72", "Midi73", "Midi74", "Midi75", "Midi76", "Midi77", "Midi78", "Midi79", 
        "Ctl E", "Ctl F", "Ctl G", "Ctl H", "Midi84", "Midi85", "Midi86", "Midi87", "Midi88", "Midi89", 
        "Midi90", "FX Depth", "Midi92", "Midi93", "Midi94", "Midi95", "Note St","Key St", "KeyNum", "BKeyNum", 
        "AttVel", "InvAVel", "PPress", "BPPress", "RelVel", "Bi-AVel", "Vtrig1", "Vtrig2", "RandV1", "RandV2", 
        "ASR1", "ASR2", "FUN1", "FUN2", "LFO1", "LFO1ph", "LFO2", "LFO2ph", "FUN3", "FUN4", 
        "AMPENV", "ENV2", "ENV3", "Loop St", "PB Rate", "Atk Stat", "Rel Stat", "On", "-On", "GKeyNum", 
        "GAttVel", "GLowKey", "GHiKey", 
        "-0.99", "-0.98", "-0.97", "-0.96", "-0.95", "-0.94", "-0.93", "-0.92", "-0.91", "-0.90", "-0.88", 
        "-0.86", "-0.84", "-0.82", "-0.80", "-0.78", "-0.76", "-0.74", "-0.72", "-0.70", "-0.68", "-0.66", 
        "-0.64", "-0.62", "-0.60", "-0.58", "-0.56", "-0.54", "-0.52", "-0.50", "-0.48", "-0.46", "-0.44", 
        "-0.42", "-0.40", "-0.38", "-0.36", "-0.34", "-0.32", "-0.30", "-0.28", "-0.26", "-0.24", "-0.22", 
        "-0.20", "-0.18", "-0.16", "-0.14", "-0.12", "-0.10", "-0.09", "-0.08", "-0.07", "-0.06", "-0.05", 
        "-0.04", "-0.03", "-0.02", "-0.01", 
        "Off", "0.01", "0.02", "0.03", "0.04", "0.05", "0.06", "0.07", "0.08", "0.09", "0.10", "0.12", "0.14", 
        "0.16", "0.18", "0.20", "0.22", "0.24", "0.26", "0.28", "0.30", "0.32", "0.34", "0.36", "0.38", 
        "0.40", "0.42", "0.44", "0.46", "0.48", "0.50", "0.52", "0.54", "0.56", "0.58", "0.60", "0.62", 
        "0.64", "0.66", "0.68", "0.70", "0.72", "0.74", "0.76", "0.78", "0.80", "0.82", "0.84", "0.86", 
        "0.88", "0.90", "0.91", "0.92", "0.93", "0.94", "0.95", "0.96", "0.97", "0.98","0.99"};
    // all except G sources (only for KDFX)
    static final int[] CONTROL_MAP = new int[] {
        0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 
        10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
        20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 
        30, 31, 32, 33, 34, 35, 36, 37, 
        43, 44, 45, 46, 47, 48, 49, 
        50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 
        60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 
        70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 
        80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 
        90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 
        100,101,102,103,104,105,106,107,108,109,
        110,111,112,113,114,115,116,117,118,119, 
        120,121,122,123,124,125,126,127,    129, 
        130,131,132};
    static final String[] CONTROL_LIST_STR  = new String[CONTROL_MAP.length];
    // CONTROL_MAP except unused ones for Globals and Enable
    static final int[] CONTROL_MAP_GE = new int[] {
        0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 
        10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
        20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 
        30, 31, 32, 33, 34, 35, 36, 37, 
        43, 44, 45, 46, 47, 48, 49, 
        50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 
        60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 
        70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 
        80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 
        90, 91, 92, 93, 94, 95, 
        111,    113,        116,117,    119, 
        127,    129,
        130,131,132};
    static final String[] CONTROL_LIST_STR_GE  = new String[CONTROL_MAP_GE.length];
    // all except G sources plus FUN constants
    static final int[] CONTROL_MAP_FUN = new int[] {
        0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 
        10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
        20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 
        30, 31, 32, 33, 34, 35, 36, 37, 
        43, 44, 45, 46, 47, 48, 49, 
        50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 
        60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 
        70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 
        80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 
        90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 
        100,101,102,103,104,105,106,107,108,109,
        110,111,112,113,114,115,116,117,118,119, 
        120,121,122,123,124,125,126,127,128,129, 
        130, 131, 132, 
        133, 134, 135, 136, 137, 138, 139, 140, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 
        153, 154, 155, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 
        173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189, 190, 191, 192, 
        193, 194, 195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 
        213, 214, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 227, 228, 229, 230, 231, 232, 
        233, 234, 235, 236, 237, 238, 239, 240, 241, 242, 243, 244, 245, 246, 247, 248, 249, 250, 251};
    static final String[] CONTROL_LIST_FUN_STR  = new String[CONTROL_MAP_FUN.length];
    // CONTROL_MAP except unused ones for Globals and Enable plus FUN constants
    static final int[] CONTROL_MAP_FUN_GE = new int[] {
        0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 
        10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
        20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 
        30, 31, 32, 33, 34, 35, 36, 37, 
        43, 44, 45, 46, 47, 48, 49, 
        50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 
        60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 
        70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 
        80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 
        90, 91, 92, 93, 94, 95, 
        111,    113,        116,117,    119, 
        127,128,129, 
        130, 131, 132, 
        133, 134, 135, 136, 137, 138, 139, 140, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 
        153, 154, 155, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 
        173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189, 190, 191, 192, 
        193, 194, 195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 
        213, 214, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 227, 228, 229, 230, 231, 232, 
        233, 234, 235, 236, 237, 238, 239, 240, 241, 242, 243, 244, 245, 246, 247, 248, 249, 250, 251};
    static final String[] CONTROL_LIST_FUN_GE_STR  = new String[CONTROL_MAP_FUN_GE.length];
    // all including G sources for KDFX
    static final int[] CONTROL_MAP_FX = new int[] {
        0,  1,  2,  3,  4,  5,  6,  7,  8,  9,
        10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
        20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 
        30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 
        40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 
        50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 
        60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 
        70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 
        80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 
        90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 
        100,101,102,103,104,105,106,107,108,109, 
        110,111,112,113,114,115,116,117,118,119,
        126,127,    129, 
        130,131,132};
    static final String[] CONTROL_LIST_FX_STR  = new String[CONTROL_MAP_FX.length];
    // all including G sources for KDFX plus FUN constants
    static final int[] CONTROL_MAP_FUN_FX = new int[] {
        0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 
        10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
        20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 
        30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 
        40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 
        50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 
        60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 
        70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 
        80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 
        90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 
        100,101,102,103,104,105,106,107,108,109,
        110,111,112,113,114,115,116,117,118,119, 
        120,121,122,123,124,125,126,127,128,129, 
        130,131,132, 
        133, 134, 135, 136, 137, 138, 139, 140, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 
        153, 154, 155, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 
        173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189, 190, 191, 192, 
        193, 194, 195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 
        213, 214, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 227, 228, 229, 230, 231, 232, 
        233, 234, 235, 236, 237, 238, 239, 240, 241, 242, 243, 244, 245, 246, 247, 248, 249, 250, 251};
    static final String[] CONTROL_LIST_FUN_FX_STR  = new String[CONTROL_MAP_FUN_FX.length];
        
    static final String[] NOTES = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    
    static final String[] CENT_VALUES = new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", 
                                                      "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "22", "24", "26", "28", "30", "32", "34", 
                                                      "36", "38", "40", "42", "44", "46", "48", "50", "55", "60", "65", "70", "75", "80", "85", "90", "95", 
                                                      "100", "110", "120", "130", "140", "150", "160", "170", "180", "190", "200", "220", "240", "260", 
                                                      "280", "300", "320", "340", "360", "380", "400", "450", "500", "550", "600", "650", "700", "750", 
                                                      "800", "850", "900", "950", "1000", "1100", "1200", "1300", "1400", "1500", "1600", "1700", "1800", 
                                                      "1900", "2000", "2100", "2200", "2300", "2400", "2500", "2600", "2700", "2800", "2900", "3000", 
                                                      "3100", "3200", "3300", "3400", "3500", "3600", "3700", "3800", "3900", "4000", "4100", "4200", 
                                                      "4300", "4400", "4500", "4600", "4700", "4800", "4900", "5000", "5300", "5500", "6000", "6500",
                                                      "6700", "7200"};
    
    static final String[] PORTA_VALUES = new String[] {"1.0", "1.2", "1.4", "1.6", "1.8", "2.0", "2.2", "2.4",
                                                       "2.6", "2.8", "3.0", "3.2", "3.4", "3.6", "3.8", "4.0", "4.2", "4.4", "4.6", "4.8", "5.0", "5.3", 
                                                       "5.6", "6.0", "6.5", "7.0", "7.5", "8.0", "8.5", "9.0", "9.5", "10", "11", "12", "13", 
                                                       "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", 
                                                       "26", "28", "30", "32", "34", "36", "38", "40", "42", "44", "46", "48", 
                                                       "50", "53", "56", "60", "65", "70", "75", "80", "85", "90", "95", "100", 
                                                       "110", "120", "130", "140", "150", "160", "170", "180", "190", "200", "210", 
                                                       "220", "230", "240", "250", "260", "280", "300", "320", "340", "360", "380", 
                                                       "400", "420", "440", "460", "480", "500", "530", "560", "600", "650", "700", 
                                                       "750", "800", "850", "900", "950", "1000", "1100", "1200", "1300", "1400", 
                                                       "1500", "1600", "1700", "1800", "1900", "2000", "2100", "2200", "2300", "2400", 
                                                       "2500", "2600", "2800", "3000"};
    
    // LFO shape list without holes
    static final String[] LFO_SHP = new String[] {"None", "Sine", "+Sine", "Square", "+Square", "Triangle", 
                                                  "+Triangle", "Rise Saw", "+Rise Saw", "Fall Saw", "+Fall Saw", "3 Step", "+3 Step", "4 Step", 
                                                  "+4 Step", "5 Step", "+5 Step", "6 Step", "+6 Step", "7 Step", "+7 Step", "8 Step", "+8 Step", 
                                                  "10 Step", "+10 Step", "12 Step", "+12 Step",};
    static final int[] LFO_SHP_MAP = new int[] {
        0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 
        10, 
        20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 
        30, 31,         34, 35,         38, 39};
    static final String[] LFO_PHS = new String[] {"0 Degrees", "90 Degrees", "180 Degrees", "270 Degrees"};
    static final String[] LFO_RATES = new String[] {"0.00", "0.01", "0.02", "0.03", "0.04", "0.05", "0.06", 
                                                    "0.07", "0.08", "0.09", "0.10", "0.11", "0.12", "0.13", "0.14", "0.15", "0.16", "0.17", "0.18", 
                                                    "0.19", "0.20", "0.25", "0.30", "0.35", "0.40", "0.45", "0.50", "0.55", "0.60", "0.65", "0.70", 
                                                    "0.75", "0.80", "0.85", "0.90", "0.95", "1.0", "1.1", "1.2", "1.3", "1.4", "1.5", "1.6", 
                                                    "1.7", "1.8", "1.9", "2.0", "2.1", "2.2", "2.3", "2.4", "2.5", "2.6", "2.7", "2.8", 
                                                    "2.9", "3.0", "3.1", "3.2", "3.3", "3.4", "3.5", "3.6", "3.7", "3.8", "3.9", "4.0", 
                                                    "4.1", "4.2", "4.3", "4.4", "4.5", "4.6", "4.7", "4.8", "4.9", "5.0", "5.1", "5.2", 
                                                    "5.3", "5.4", "5.5", "5.6", "5.7", "5.8", "5.9", "6.0", "6.1", "6.2", "6.3", "6.4", 
                                                    "6.5", "6.6", "6.7", "6.8", "6.9", "7.0", "7.1", "7.2", "7.3", "7.4", "7.5", "7.6", 
                                                    "7.7", "7.8", "7.9", "8.0", "8.1", "8.2", "8.3", "8.4", "8.5", "8.6", "8.7", "8.8", 
                                                    "8.9", "9.0", "9.1", "9.2", "9.3", "9.4", "9.0", "9.6", "9.7", "9.8", "9.9", "10.0", 
                                                    "10.2", "10.4", "10.6", "10.8", "11.0", "11.2", "11.4", "11.6", "11.8", "12.0", "12.2", 
                                                    "12.4", "12.6", "12.8", "13.0", "13.2", "13.4", "13.6", "13.8", "14.0", "14.2", "14.4", 
                                                    "14.6", "14.8", "15.0", "15.2", "15.4", "15.6", "15.8", "16.0", "16.2", "16.4", "16.6", 
                                                    "16.8", "17.0", "17.2", "17.4", "17.6", "17.8", "18.0", "18.2", "18.4", "18.6", "18.8", 
                                                    "19.0", "19.2", "19.4", "19.6", "19.8", "20.0", "20.5", "21.0", "21.5", "22.0", "22.5", 
                                                    "23.0", "23.5", "24.0"};
    
    // FUN list without holes
    static final String[] FUN_FUN = new String[] {"None", "a+b", "a-b", "(a+b)/2", "a/2+b", "a/4+b/2", 
                                                  "(a+2b)/3", "a*b", "-a*b", "a*10^b", "|a+b|", "|a-b|", "min(a,b)", "max(a,b)", "Quantize B to A", 
                                                  "lowpass (f = a,b)", "hipass (f = a,b)", "b/(1-a)", "a(b-y)", "(a+b)^2", "sin(a+b)", "cos(a+b)", 
                                                  "tri(a+b)", "warp1(a,b)", "warp2(a,b)", "warp3(a,b)", "warp4(a,b)", "warp8(a,b)", "a AND b", 
                                                  "a OR b", "b > a", "ramp(f=a+b)", "ramp(f=a-b)", "ramp(f=(a+b)/2)", "ramp(f=a*b)", "ramp(f=-a * b)", 
                                                  "ramp(f=a*10^b)", "ramp(f=(a+b)/4)", "a(y+b)", "ay+b", "(a+1)y +b", "y+a(y+b)", "a|y|+b", 
                                                  "Sample B On A", "Sample A On ~A", "Track B While A", "diode (a-b)", "diode (a-b+.5)", 
                                                  "diode (a-b-.5)", "diode (a-b+.25)", "diode (a-b-.25)", "Track B While ~A"};
    static final int[] FUN_FUN_MAP = new int[] {
        0,  1,  2,  3,  4,  5,  6,          9, 
        10,         13,         16, 17, 18, 19, 
        20,     22, 23,     25,     27,     29,
        31, 32, 33,     35, 36, 37, 38, 39, 
        41, 42, 43,             47, 48, 49, 
        50, 51, 52, 53, 54, 55, 56, 57, 58, 
        61, 62, 63, 64, 65, 66, 67, 68, 69};
    
    static final String[] ASR_MOD = new String[] {"Normal", "Hold", "Repeat"};
    
    static final String[] ENV_TIMES = new String[] {"0", ".002", ".005", "0.01", "0.02", "0.04", "0.06", 
                                                    "0.08", "0.10", "0.12", "0.14", "0.16", "0.18", "0.20", "0.22", "0.24", "0.26", "0.28", "0.30", 
                                                    "0.32", "0.34", "0.36", "0.38", "0.40", "0.42", "0.44", "0.46", "0.48", "0.50", "0.52", "0.54", 
                                                    "0.56", "0.58", "0.60", "0.62", "0.64", "0.66", "0.68", "0.70", "0.72", "0.74", "0.76", "0.78", 
                                                    "0.80", "0.82", "0.84", "0.86", "0.88", "0.90", "0.92", "0.94", "0.96", "0.98", "1.00", "1.02", 
                                                    "1.04", "1.06", "1.08", "1.10", "1.12", "1.14", "1.16", "1.18", "1.20", "1.22", "1.24", "1.26", 
                                                    "1.28", "1.30", "1.32", "1.34", "1.36", "1.38", "1.40", "1.42", "1.44", "1.46", "1.48", "1.50", 
                                                    "1.52", "1.54", "1.56", "1.58", "1.60", "1.62", "1.64", "1.66", "1.68", "1.70", "1.72", "1.74", 
                                                    "1.76", "1.78", "1.80", "1.82", "1.84", "1.86", "1.88", "1.90", "1.92", "1.94", "1.96", "1.98", 
                                                    "2.00", "2.04", "2.08", "2.12", "2.16", "2.20", "2.24", "2.28", "2.32", "2.36", "2.40", "2.44", 
                                                    "2.48", "2.52", "2.56", "2.60", "2.64", "2.68", "2.72", "2.76", "2.80", "2.84", "2.88", "2.92", 
                                                    "2.96", "3.00", "3.04", "3.08", "3.12", "3.16", "3.20", "3.24", "3.28", "3.32", "3.36", "3.40", 
                                                    "3.44", "3.48", "3.52", "3.56", "3.60", "3.64", "3.68", "3.72", "3.76", "3.80", "3.84", "3.88", 
                                                    "3.92", "3.96", "4.00", "4.04", "4.08", "4.12", "4.16", "4.20", "4.24", "4.28", "4.32", "4.36", 
                                                    "4.40", "4.44", "4.48", "4.52", "4.56", "4.60", "4.64", "4.68", "4.72", "4.76", "4.80", "4.84", 
                                                    "4.88", "4.92", "4.96", "5.00", "5.10", "5.20", "5.30", "5.40", "5.50", "5.60", "5.70", "5.80", 
                                                    "5.90", "6.00", "6.10", "6.20", "6.30", "6.40", "6.50", "6.60", "6.70", "6.80", "6.90", "7.00", 
                                                    "7.10", "7.20", "7.30", "7.40", "7.50", "7.60", "7.70", "7.80", "7.90", "8.00", "8.10", "8.20", 
                                                    "8.30", "8.40", "8.50", "8.60", "8.70", "8.80", "8.90", "9.00", "9.10", "9.20", "9.30", "9.40", 
                                                    "9.50", "9.60", "9.70", "9.80", "9.90", "10.0s", "10.50", "11.00", "11.50", "12.00", "12.50", 
                                                    "13.0", "13.5", "14.0", "14.5", "15.0", "16.0", "17.0", "18.0", "19.0", "20.0", "21.0", 
                                                    "22.0", "23.0", "24.0", "25.0", "30.0", "35.0", "40.0", "45.0", "50.0", "55.0", "60.0"};
    
    static final String[] LAYER_VELOCITY = new String[] {"ppp", "pp", "p", "mp", "mf", "f", "ff", "fff"};
    static final String[] LAYER_PBMODE = new String[] {"All", "Off", "Key"};
    static final String[] LAYER_TRIGGER = new String[] {"Normal", "Reversed"};
    static final String[] LAYER_DTIMES = new String[] {"0", "0.002", "0.004", "0.006", "0.008", "0.010", 
                                                       "0.012", "0.014", "0.016", "0.018", "0.020", "0.022", "0.024", "0.026", "0.028", "0.030", "0.032", 
                                                       "0.034", "0.036", "0.038", "0.040", "0.042", "0.044", "0.046", "0.048", "0.050", "0.055", "0.060", 
                                                       "0.065", "0.070", "0.075", "0.080", "0.085", "0.090", "0.095", "0.100", "0.110", "0.120", "0.130", 
                                                       "0.140", "0.150", "0.160", "0.170", "0.180", "0.190", "0.200", "0.210", "0.220", "0.230", "0.240", 
                                                       "0.250", "0.260", "0.270", "0.280", "0.290", "0.300", "0.320", "0.340", "0.360", "0.380", "0.400", 
                                                       "0.420", "0.440", "0.460", "0.480", "0.500", "0.520", "0.540", "0.560", "0.580", "0.600", "0.620", 
                                                       "0.640", "0.660", "0.680", "0.700", "0.720", "0.740", "0.760", "0.780", "0.800", "0.820", "0.840", 
                                                       "0.860", "0.880", "0.900", "0.920", "0.940", "0.390", "0.980", "1.00", "1.05", "1.10", "1.15", 
                                                       "1.20", "1.25", "1.30", "1.35", "1.40", "1.45", "1.50", "1.55", "1.60", "1.65", "1.70",  
                                                       "1.75", "1.80", "1.85", "1.90", "1.95", "2.00", "2.05", "2.10", "2.15", "2.20", "2.25", 
                                                       "2.30", "2.35", "2.40", "2.45", "2.50", "2.55", "2.60", "2.65", "2.70", "2.75", "2.80", 
                                                       "2.85", "2.90", "2.95", "3.00", "3.05", "3.10", "3.15", "3.20", "3.25", "3.30", "3.35", 
                                                       "3.40", "3.45", "3.50", "3.55", "3.60", "3.65", "3.70", "3.75", "3.80", "3.85", "3.90", 
                                                       "3.95", "4.00", "4.05", "4.10", "4.15", "4.20", "4.25", "4.30", "4.35", "4.40", "4.45", 
                                                       "4.50", "4.55", "4.60", "4.65", "4.70", "4.75", "4.80", "4.85", "4.90", "4.95", "5.00", 
                                                       "5.10", "5.20", "5.30", "5.40", "5.50", "5.60", "5.70", "5.80", "5.90", "6.00", "6.10", 
                                                       "6.20", "6.30", "6.40", "6.50", "6.60", "6.70", "6.80", "6.90", "7.00", "7.10", "7.20", 
                                                       "7.30", "7.40", "7.50", "7.60", "7.70", "7.80", "7.90", "8.00", "8.10", "8.20", "8.30", 
                                                       "8.40", "8.50", "8.60", "8.70", "8.80", "8.90", "9.00", "9.10", "9.20", "9.30", "9.40", 
                                                       "9.50", "9.60", "9.70", "9.80", "9.90", "10.0", "10.2", "10.4", "10.6", "10.8", 
                                                       "11.0", "11.2", "11.4", "11.6", "11.8", "12.0", "12.5", "13.0", "13.5", "14.0", 
                                                       "14.5", "15.0", "15.5", "16.0", "16.5", "17.0", "17.5", "18.0", "18.5", "19.0", 
                                                       "19.5", "20.0", "20.5", "21.0", "21.5", "22.0", "22.5", "23.0", "23.5", "24.0", 
                                                       "25.0"};
    static final String[] LAYER_SUSPDL = new String[] {"On", "Off", "On2"};
    static final int[] LAYER_SUSPDL_MAP = new int[] {0, 2, 64};
    
    static final String[] KEY_TRACK = new String[] {"0", "5", "10", "15", "20", "25", "30", "35", "40", "42", 
                                                    "44", "46", "48", "50", "52", "54", "56", "58", "60", "62", "64", "66", "68", "70", "72", "74", "76", 
                                                    "78", "80", "82", "84", "86", "88", "90", "91", "92", "93", "94", "95", "96", "97", "98", "99", 
                                                    "100", "101", "102", "103", "104", "105", "106", "107", "108", "109", "110", "112", "114", "116", 
                                                    "118", "120", "122", "124", "126", "128", "130", "132", "134", "136", "138", "140", "142", "144", 
                                                    "146", "148", "150", "152", "154", "156", "158", "160", "165", "170", "175", "180", "185", "190", 
                                                    "195", "200", "220", "240", "260", "280", "300", "320", "340", "360", "380", "400", "450", "500", 
                                                    "550", "600", "650", "700", "750", "800", "850", "900", "950", "1000", "1050", "1100", "1150", 
                                                    "1200", "1300", "1300", "1500", "1600", "1800", "2000", "2200", "2400"};
    
    static final String[] COARSE_HZ = new String[] {"8", "8", "9", "9","10", "11", "11", "12", "13", "13", "14", "15",
                                                    "16", "17", "18", "19", "21", "22", "23", "24", "26", 
                                                    "28", "29", "31", "33", "35", "37", "39", "41", "44", "46", "49", "52", "55", "58", "62", "65", "69", 
                                                    "73", "78", "82", "87", "92", "98", "104", "110", "117", "123", "131", "139", "147", "156", "165", 
                                                    "175", "185", "196", "208", "220", "233", "247", "262", "277", "294", "311", "330", "349", "370", 
                                                    "392", "415", "440", "466", "494", "523", "554", "587", "622", "659", "698", "740", "784", "831", 
                                                    "880", "932", "988", "1047", "1109", "1175", "1245", "1319", "1397", "1480", "1568", "1661", "1760",
                                                    "1865", "1976", "2093", "2217", "2349", "2489", "2637", "2794", "2960", "3136", "3322", "3520", 
                                                    "3729", "3951", "4186", "4435", "4699", "4978", "5274", "5588", "5920", "6272", "6645", "7040", 
                                                    "7459", "7902", "8372", "8870", "9397", "9956", "10548", "11175", "11840", "12455", "13290", "14080", 
                                                    "14917", "15804", "16744", "17740", "18795", "19912", "21096", "22351", "23680", "25088"};
    
    static final String[] VEL_TRACK = new String[] {"0", "2", "4", "6", "8", "10", "12", "14", "16", "18", 
                                                    "20", "22", "24", "27", "30", "35", "40", "45", "50", "55", "60", "70", "80", "90", "100", "120", 
                                                    "150", "200", "250", "300", "350", "400", "450", "500", "600", "700", "800", "900", "1000", "1100", 
                                                    "1200", "1300", "1400", "1500", "1600", "1700", "1800", "1900", "2000", "2100", "2200", "2300", 
                                                    "2400", "2500", "2600", "2700", "2800", "2900", "3000", "3100", "3200", "3300", "3400", "3500", 
                                                    "3600", "3700", "3800", "3900", "4000", "4100", "4200", "4300", "4400", "4500", "4600", "4700", 
                                                    "4800", "4900", "5000", "5100", "5200", "5300", "5400", "5500", "5600", "5700", "5800", "5900", 
                                                    "6000", "6100", "6200", "6300", "6400", "6500", "6600", "6700", "6800", "6900", "7000", "7100", 
                                                    "7200", "7300", "7400", "7500", "7600", "7700", "7800", "7900", "8000", "8100", "8200", "8300", 
                                                    "8400", "8500", "8600", "8700", "8800", "8900", "9000", "9100", "9200", "9300", "9400", "9500", 
                                                    "9600", "10000", "10400", "10800"};
    
    static final String[] PAD = new String[] {"0 dB", "6 dB", "12 dB", "18 dB"};
    
    static final String[] KEYMAP_SAMSKIP = new String[] {"Off", "Auto", "On"};
    static final int[] KEYMAP_SAMSKIP_MAP = new int[] {0, 64, 128};
    static final String[] KEYMAP_PLAYMODE = new String[] {"Normal", "Reverse", "Bidirectional", "Noise", 
                                                          "ADAT in 1", "ADAT in 2","ADAT in 3","ADAT in 4","ADAT in 5","ADAT in 6","ADAT in 7","ADAT in 8"};
    static final int[] KEYMAP_PLAYMODE_MAP = new int[] {0, 2, 4, 6, 7, 8, 9, 10, 11, 12, 13, 14};
    static final String[] KEYMAP_ALT_METHOD = new String[] {"Switched", "Continuous"};
    
    static final String[] ENV_MODE = new String[] {"User", "Natural"};
    static final String[] ENV_LOOP_TYPE = new String[] {"Off", "seg 1F", "seg 2F", "seg 3F", "seg 1B", "seg 2B", "seg 3B"};
    static final int[] ENV_LOOP_TYPE_MAP = new int[] {0, 1, 2, 3, 5, 6, 7};
    static final String[] ENV_LOOP_TIMES = new String[] {"Infinite", "1 X", "2 X", "3 X", "4 X", "5 X", 
                                                         "6 X", "7 X", "8 X", "9 X", "10 X", "11 X", "12 X", "13 X", "14 X", "15 X", "16 X", "17 X", "18 X", 
                                                         "19 X", "20 X", "21 X", "22 X", "23 X", "24 X", "25 X", "26 X", "27 X", "28 X", "29 X", "30 X", "31 X"};
    
    static final String[] ENV_CTL = new String[] {"0.018", "0.020", "0.022", "0.025", "0.027", "0.030", 
                                                  "0.033", "0.036", "0.040", "0.043", "0.047", "0.050", "0.055", "0.061", "0.067", "0.073", "0.080", 
                                                  "0.090", "0.10", "0.11", "0.12", "0.13", "0.14", "0.15", "0.16", "0.18", "0.20", "0.22", 
                                                  "0.25", "0.27", "0.30", "0.33", "0.36", "0.40", "0.43", "0.47", "0.50", "0.55", "0.61", 
                                                  "0.67", "0.73", "0.80", "0.90", "1.0", "1.1", "1.2", "1.3", "1.4", "1.5", "1.6",
                                                  "1.8", "2.0", "2.2", "2.5", "2.7", "3.0", "3.3", "3.6", "4.0", "4.3", "4.7", 
                                                  "5.0", "5.5", "6.1", "6.7", "7.3", "8.0" , "9.0", "10.0", "11.0", "12.0", 
                                                  "13.0", "14.0", "15.0", "16.0", "18.0", "20.0", "22.0", "25.0", "27.0", "30.0", 
                                                  "33.0", "36.0", "40.0", "43.0", "47.0", "50.0"};
    
    static final String[] WIDTH_AMT = new String[] {"0.010", "0.010", "0.011", "0.013", "0.014", "0.015", 
                                                    "0.015", "0.016", "0.018", "0.019", "0.020", "0.021", "0.024", "0.025", "0.028", "0.030", "0.031", 
                                                    "0.034", "0.035", "0.038", "0.040", "0.041", "0.044", "0.045", "0.048", "0.050", "0.055", "0.060", 
                                                    "0.065", "0.070", "0.075", "0.080", "0.085", "0.090", "0.095", "0.10", "0.11", "0.12", "0.13", 
                                                    "0.14", "0.15", "0.16", "0.17", "0.18", "0.19", "0.20", "0.22", "0.24", "0.26", "0.28", 
                                                    "0.30", "0.32", "0.34", "0.36", "0.38", "0.40", "0.42", "0.44", "0.46", "0.48", "0.50", 
                                                    "0.55", "0.60", "0.65", "0.70", "0.75", "0.80", "0.85", "0.90", "0.95", "1.00", "1.05", 
                                                    "1.10", "1.15", "1.20", "1.25", "1.30", "1.35", "1.40", "1.45", "1.50", "1.55", "1.60", 
                                                    "1.65", "1.70", "1.75", "1.80", "1.85", "1.90", "1.95", "2.00", "2.05", "2.10", "2.15", 
                                                    "2.20", "2.25", "2.30", "2.35", "2.40", "2.45", "2.50", "2.55", "2.60", "2.65", "2.70", 
                                                    "2.75", "2.80", "2.85", "2.90", "2.95", "3.00", "3.05", "3.10", "3.15", "3.20", "3.25", 
                                                    "3.30", "3.35", "3.40", "3.45", "3.50", "3.55", "3.60", "3.65", "3.70", "3.75", "3.80", 
                                                    "3.85", "3.90", "3.95", "4.00", "4.05", "4.10", "4.15", "4.20", "4.25", "4.30", "4.35", 
                                                    "4.40", "4.45", "4.50", "4.55", "4.60", "4.65", "4.70", "4.75", "4.80", "4.85", "4.90", 
                                                    "4.95", "5.00",};
    
    static final String[] WIDTH_VEL = new String[] {"0.00", "0.01", "0.02", "0.03", "0.04", "0.05", "0.06", 
                                                    "0.07", "0.08", "0.09", "0.10", "0.12", "0.14", "0.16", "0.18", "0.20", "0.22", "0.24", "0.26", 
                                                    "0.28", "0.30", "0.32", "0.34", "0.36", "0.38", "0.40", "0.42", "0.44", "0.46", "0.48", "0.50", 
                                                    "0.52", "0.54", "0.56", "0.58", "0.60", "0.62", "0.64", "0.66", "0.68", "0.70", "0.72", "0.74", 
                                                    "0.76", "0.78", "0.80", "0.82", "0.84", "0.86", "0.88", "0.90", "0.92", "0.94", "0.96", "0.98", 
                                                    "1.00", "1.05", "1.10", "1.15", "1.20", "1.25", "1.30", "1.35", "1.40", "1.45", "1.50", "1.55", 
                                                    "1.60", "1.65", "1.70", "1.75", "1.80", "1.85", "1.90", "1.95", "2.00", "2.10", "2.20", "2.30", 
                                                    "2.40", "2.50", "2.60", "2.70", "2.80", "2.90", "3.00", "3.10", "3.20", "3.30", "3.40", "3.50", 
                                                    "3.60", "3.70", "3.80", "3.90", "4.00", "4.10", "4.20", "4.30", "4.40", "4.50", "4.60", "4.70", 
                                                    "4.80", "4.90", "5.00"};
    
    static final String[] COARSE_LF = new String[] {"0.1", "1", "10", "100", "1000"};
    
    static final String[] FINE_LF = new String[] {"1.00", "1.01", "1.02", "1.03", "1.04", "1.05", "1.06", 
                                                  "1.07", "1.08", "1.09", "1.10", "1.11", "1.12", "1.13", "1.14", "1.15", "1.16", "1.17", "1.18", 
                                                  "1.19", "1.20", "1.21", "1.22", "1.23", "1.24", "1.25", "1.26", "1.27", "1.28", "1.29", "1.30", 
                                                  "1.31", "1.32", "1.33", "1.34", "1.35", "1.36", "1.37", "1.38", "1.39", "1.40", "1.41", "1.42", 
                                                  "1.43", "1.44", "1.45", "1.46", "1.47", "1.48", "1.49", "1.50", "1.52", "1.54", "1.56", "1.58", 
                                                  "1.60", "1.62", "1.64", "1.66", "1.68", "1.70", "1.72", "1.74", "1.76", "1.78", "1.80", "1.82", 
                                                  "1.84", "1.86", "1.88", "1.90", "1.92", "1.94", "1.96", "1.98", "2.00", "2.02", "2.04", "2.06", 
                                                  "2.08", "2.10", "2.12", "2.14", "2.16", "2.18", "2.20", "2.22", "2.24", "2.26", "2.28", "2.30", 
                                                  "2.32", "2.34", "2.36", "2.38", "2.40", "2.42", "2.44", "2.46", "2.48", "2.50", "2.52", "2.54", 
                                                  "2.56", "2.58", "2.60", "2.62", "2.64", "2.66", "2.68", "2.70", "2.72", "2.74", "2.76", "2.78", 
                                                  "2.80", "2.82", "2.84", "2.86", "2.88", "2.90", "2.92", "2.94", "2.96", "2.98", "3.00", "3.05", 
                                                  "3.10", "3.15", "3.20", "3.25", "3.30", "3.35", "3.40", "3.45", "3.50", "3.55", "3.60", "3.65", 
                                                  "3.70", "3.75", "3.80", "3.85", "3.90", "3.95", "4.00", "4.05", "4.10", "4.15", "4.20", "4.25", 
                                                  "4.30", "4.35", "4.40", "4.45", "4.50", "4.55", "4.60", "4.65", "4.70", "4.75", "4.80", "4.85", 
                                                  "4.90", "4.95", "5.00", "5.05", "5.10", "5.15", "5.20", "5.25", "5.30", "5.35", "5.40", "5.45", 
                                                  "5.50", "5.55", "5.60", "5.65", "5.70", "5.75", "5.80", "5.85", "5.90", "5.95", "6.00", "6.10", 
                                                  "6.20", "6.30", "6.40", "6.50", "6.60", "6.70", "6.80", "6.90", "7.00", "7.10", "7.20", "7.30", 
                                                  "7.40", "7.50", "7.60", "7.70", "7.80", "7.90", "8.00", "8.10", "8.20", "8.30", "8.40", "8.50", 
                                                  "8.60", "8.70", "8.80", "8.90", "9.00", "9.10", "9.20", "9.30", "9.40", "9.50", "9.60", "9.70", 
                                                  "9.80", "9.90", "10.00", "10.25", "10.50", "10.75", "11.00", "11.25", "11.50", "11.75", "12.00", 
                                                  "12.25", "12.50", "12.75", "13.00", "13.25", "13.50", "13.75", "14.00", "14.25", "14.50", "14.75", 
                                                  "15.00", "15.50", "16.00", "16.50", "17.00", "17.50", "18.00", "18.50", "19.00", "19.50", "20.00"};
    
    static final String[] KEY_TRACK_LF = new String[] {"0.10", "0.11", "0.12", "0.13", "0.14", "0.15", 
                                                       "0.16", "0.17", "0.18", "0.19", "0.20", "0.21", "0.22", "0.23", "0.24", "0.25", "0.26", "0.27", 
                                                       "0.28", "0.29", "0.30", "0.31", "0.32", "0.33", "0.34", "0.35", "0.36", "0.37", "0.38", "0.39",
                                                       "0.40", "0.41", "0.42", "0.43", "0.44", "0.45", "0.46", "0.47", "0.48", "0.49", "0.50", "0.51", 
                                                       "0.52", "0.53", "0.54", "0.55", "0.56", "0.57", "0.58", "0.59", "0.60", "0.61", "0.62", "0.63", 
                                                       "0.64", "0.65", "0.66", "0.67", "0.68", "0.69", "0.70", "0.71", "0.72", "0.73", "0.74", "0.75", 
                                                       "0.76", "0.77", "0.78", "0.79", "0.80", "0.81", "0.82", "0.83", "0.84", "0.85", "0.86", "0.87",
                                                       "0.88", "0.89", "0.90", "0.91", "0.92", "0.93", "0.94", "0.95", "0.96", "0.97", "0.98", "0.99",
                                                       "1.00", "1.01", "1.02", "1.03", "1.04", "1.05", "1.06", "1.07", "1.08", "1.09", "1.10", "1.11", 
                                                       "1.12", "1.13", "1.14", "1.15", "1.16", "1.17", "1.18", "1.19", "1.20", "1.30", "1.40", "1.50",
                                                       "1.60", "1.70", "1.80", "1.90", "2.00", "2.10", "2.20", "2.30", "2.40", "2.50", "2.60", "2.70", 
                                                       "2.80", "2.90", "3.00", "3.10", "3.20", "3.30", "3.40", "3.50", "3.60", "3.70", "3.80", "3.90",
                                                       "4.00", "4.10", "4.20", "4.30", "4.40", "4.50", "4.60", "4.70", "4.80", "4.90", "5.00", "5.10", 
                                                       "5.20", "5.30", "5.40", "5.50", "5.60", "5.70", "5.80", "5.90", "6.00", "6.10", "6.20", "6.30", 
                                                       "6.40", "6.60", "6.80", "7.00", "7.20", "7.40", "7.60", "7.80", "8.00", "8.20", "8.40", "8.60", 
                                                       "8.80", "9.00", "9.20", "9.40", "9.60", "9.80", "10.00",};
    
    static final String[] VEL_TRACK_LF = new String[] {"0.010", "0.011", "0.012", "0.013", "0.014", "0.015",
                                                       "0.016", "0.017", "0.018", "0.019", "0.020", "0.021", "0.022", "0.023", "0.024", "0.025", "0.026", 
                                                       "0.027", "0.028", "0.029", "0.030", "0.032", "0.034", "0.036", "0.038", "0.040", "0.042", "0.044", 
                                                       "0.046", "0.048", "0.050", "0.052", "0.054", "0.056", "0.058", "0.060", "0.062", "0.064", "0.066", 
                                                       "0.068", "0.070", "0.072", "0.074", "0.076", "0.078", "0.080", "0.082", "0.084", "0.086", "0.088",
                                                       "0.090", "0.092", "0.094", "0.096", "0.098", "0.10", "0.11", "0.12", "0.13", "0.14", "0.15",
                                                       "0.16", "0.17", "0.18", "0.19", "0.20", "0.21", "0.22", "0.23", "0.24", "0.25", "0.26", 
                                                       "0.27", "0.28", "0.29", "0.30", "0.32", "0.34", "0.36", "0.38", "0.40", "0.42", "0.44", 
                                                       "0.46", "0.48", "0.50", "0.52", "0.54", "0.56", "0.58", "0.60", "0.62", "0.64", "0.66", 
                                                       "0.68", "0.70", "0.72", "0.74", "0.76", "0.78", "0.80", "0.82", "0.84", "0.86", "0.88",
                                                       "0.90", "0.92", "0.94", "0.96", "0.98", "1.00", "1.02", "1.04", "1.06", "1.08", "1.10", 
                                                       "1.12", "1.14", "1.16", "1.18", "1.20", "1.30", "1.40", "1.50", "1.60", "1.70", "1.80",
                                                       "1.90", "2.00", "2.10", "2.20", "2.30", "2.40", "2.50", "2.60", "2.70", "2.80", "2.90", 
                                                       "3.00", "3.10", "3.20", "3.30", "3.40", "3.50", "3.60", "3.80", "4.00", "4.20", "4.40", 
                                                       "4.60", "4.80", "5.00", "5.20", "5.40", "5.60", "5.80", "6.00", "6.20", "6.40", "6.60", 
                                                       "6.80", "7.00", "7.20", "7.40", "7.60", "7.80", "8.00", "8.20", "8.40", "8.60", "8.80", 
                                                       "9.00", "9.20", "9.40", "9.60", "9.80", "10.0", "10.5", "11.0", "11.5", "12.0", 
                                                       "12.5", "13.0", "13.5", "14.0", "14.5", "15.0", "15.5", "16.0", "16.5", "17.0", 
                                                       "17.5", "18.0", "18.5", "19.0", "19.5", "20.0", "20.5", "21.0", "21.5", "22.0", 
                                                       "22.5", "23.0", "23.5", "24.0", "24.5", "25.0", "25.5", "26.0", "26.5", "27.0", 
                                                       "27.5", "28.0", "28.5", "29.0", "29.5", "30.0", "30.5", "31.0", "31.5", "32.0"}; 

    static final String[] OUTPUT_PAIR = new String[] {"KDFX-A", "KDFX-B", "KDFX-C", "KDFX-D"};
    static final String[] OUTPUT_PAIR_PROG = new String[] {"KDFX-A", "KDFX-B", "KDFX-C", "KDFX-D", "Prog"};
    static final String[] OUTPUT_MODE = new String[] {"Fixed", "+MIDI", "Auto", "Reverse"};
    static final String[] OUTPUT_GAIN = new String[] {"-12 dB", "-6 dB", "0 dB", "6 dB", "12 dB", "18 dB", "24 dB", "30 dB"};
    
    static final String FONT_SIZE_3 = new String ("<html><font size=3>");
    static final String FONT = new String ("</font></html>");
    
    // initial single layer format 4 and 5
    static final byte[] LAYER_SINGLE = new byte[] {
        9 ,0, 0, 0, 12, 108, 0, 127, 0, 4, 0, 0, 0, 0, 0, 0,
        16, 0, 0, 0, 0, 0, 0, 0, 
        17, 0, 0, 0, 0, 0, 0, 0,
        24, 0, 0, 0, 
        25, 0, 0, 0,
        20, 0, 0, 0, 0, 0, 0, 0, 
        21, 0, 0, 0, 0, 0, 0, 0,
        26, 0, 0, 0, 
        27, 0, 0, 0,
        32, 0, 1, 0, 0, 0, 73, 0, 0, 0, 72, 0, 0, 0,71, 0,
        39, 0, 0, 0, 0, 0, 70, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        33, 0, 100, 0, 0, 0, 0, 0, 100, 0, 0, 0, 0, 0, 0, 0,
        34, 0, 100, 0, 0, 0, 0, 0, 100, 0, 0, 0, 0, 0, 0, 0,
        35, 0, 100, 0, 0, 0, 0, 0, 100, 0, 0, 0, 0, 0, 0, 0,
        64, 127, 0, 0, 43, 0, 0, 0, 0, 0, 0, 64, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0,
        80, 62, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        81, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        82, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0,
        83, 1, 6, 0, 0, 35, 0, 0, 0, 0, 0, 0, 0, 0, 2, 4
        };
    
    // initial triple layer format 4 and 5
    static final byte[][] LAYER_TRIPLE = new byte[][] {
            {
            9 ,0, 0, 0, 12, 108, 64, 127, 0, 4, 0, 0, 0, 0, 0, 0, 
            16, 0, 0, 0, 0, 0, 0, 0, 
            17, 0, 0, 0, 0, 0, 0, 0,
            24, 0, 0, 0, 
            25, 0, 0, 0,
            20, 0, 0, 36, 0, 0, 1, 0, 
            21, 0, 0, 36, 0, 0, 1, 0,
            26, 0, 0, 0, 
            27, 0, 0, 0,
            32, 0, 1, 0, 0, 0, 73, 0, 0, 0, 72, 0, 0, 0,71, 0,
            39, 0, 0, 0, 0, 0, 70, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            33, 0, 100, 0, 0, 0, 0, 0, 100, 0, 0, 0, 0, 0, 0, 0,
            34, 0, 100, 0, 0, 0, 0, 0, 100, 0, 0, 0, 0, 0, 0, 0,
            35, 0, 100, 0, 0, 0, 0, 0, 100, 0, 0, 0, 0, 0, 0, 0,
            64, 127, 0, 0, 43, 0, 0, 0, 0, 0, 0, 64, 0, -104, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 33, 0, 
            80, 61, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            81, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            82, 61, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0,
            83, 15, 0, 0, 0, 35, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0
            },
            {
            9 ,0, 0, 0, 12, 108, -128, 127, 0, 4, 0, 0, 0, 0, 0, 0, 
            16, 0, 0, 0, 0, 0, 0, 0, 
            17, 0, 0, 0, 0, 0, 0, 0,
            24, 0, 0, 0, 
            25, 0, 0, 0,
            20, 0, 0, 36, 0, 0, 1, 0, 
            21, 0, 0, 36, 0, 0, 1, 0,
            26, 0, 0, 0, 
            27, 0, 0, 0,
            32, 0, 1, 0, 0, 0, 73, 0, 0, 0, 72, 0, 0, 0,71, 0,
            39, 0, 0, 0, 0, 0, 70, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            33, 0, 100, 0, 0, 0, 0, 0, 100, 0, 0, 0, 0, 0, 0, 0,
            34, 0, 100, 0, 0, 0, 0, 0, 100, 0, 0, 0, 0, 0, 0, 0,
            35, 0, 100, 0, 0, 0, 0, 0, 100, 0, 0, 0, 0, 0, 0, 0,
            64, 127, 0, 0, 43, 0, 0, 0, 0, 0, 0, 64, 0, -57, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 81, 0, 
            80, 62, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            81, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            82, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0,
            83, 18, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 4
            },
            {
            9 ,0, 0, 0, 12, 108, -36, 127, 0, 4, 0, 0, 0, 0, 0, 0, 
            16, 0, 0, 0, 0, 0, 0, 0, 
            17, 0, 0, 0, 0, 0, 0, 0,
            24, 0, 0, 0, 
            25, 0, 0, 0,
            20, 0, 0, 36, 0, 0, 1, 0, 
            21, 0, 0, 36, 0, 0, 1, 0,
            26, 0, 0, 0, 
            27, 0, 0, 0,
            32, 0, 1, 0, 0, 0, 73, 0, 0, 0, 72, 0, 0, 0,71, 0,
            39, 0, 0, 0, 0, 0, 70, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            33, 0, 100, 0, 0, 0, 0, 0, 0, -78, 0, 28, 0, 0, 0, 0,
            34, 0, 100, 0, 0, 0, 0, 0, 100, 0, 0, 0, 0, 0, 0, 0,
            35, 0, 100, 0, 0, 0, 0, 0, 100, 0, 0, 0, 0, 0, 0, 0,
            64, 127, 0, 0, 43, 0, 0, 0, 0, 0, 0, 64, 0, -104, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 101, 0, 
            80, 61, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            81, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0,
            82, 60, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0,
            83, 1, 0, 0, 0, 25, 0, 0, 0, 0, 0, 0, 0, 0, 3, 4
            } };
    
    static final int MAXIMUM_NAME_LENGTH = 16;
    static final int START_OF_NAME = 13;
    
    static final byte SOX = (byte)0xF0;
    static final byte EOX = (byte)0xF7;
    static final byte KURZWEIL_ID = 0x07;
    static final byte K2600_ID = 0x78; // K2600, K2500. K2000
    static final byte FORM_NIBBLE = 0;
    static final byte DIR_MSG = 0x04;
    static final byte INFO_MSG = 0x05;
    static final byte WRITE_MSG = 0x09;
    static final byte READ_MSG = 0x0A;
    static final byte PANEL_MSG = 0x14;
    static final byte PARAM_VALUE_MSG = 0x16;
    static final byte SCREENREPLY_MSG = 0x19;
    static final byte FORMAT_2 = 0x02; // K2000 or K2500 without impact and without KDFX
    static final byte FORMAT_3 = 0x03; // K2500 with impact
    static final byte FORMAT_4 = 0x04; // K2500 with KDFX, K2600
    static final byte FORMAT_5 = 0x05; // K2600 with triple layer pogram
    static final byte PLUS_BUTTON = 0x16;
    static final byte MINUS_BUTTON = 0x17;
    static final int PROGRAM_TYPE = 132;
    static final int KEYMAP_TYPE = 133;
    static final int STUDIO_TYPE = 140;
    static final int FX_PRESET_TYPE = 141;
    
    static final int MAX_NUM_LAYERS = 32;
    static final int KB3_FIRST_DATA = 248;
    static final int LAYER_DATA_SIZE_FMT_2 = 224;
    static final int LAYER_DATA_SIZE_FMT_3_4 = 240;
    static final int LAYER_FIRST_DATA_FMT_2_3 = 48;
    static final int LAYER_FIRST_DATA_FMT_4 = 344;
    
    static final int ASR_DP_SIZE = 8;
    static final int LFO_DP_SIZE = 8;
    static final int FUN_DP_SIZE = 4;
    static final int FXMOD_DATA_SIZE = 8;
    static final int ENVCTL_DP_SIZE = 16;
    static final int ENV_DATA_SIZE = 16;
     
    static final int NUM_ALGS_1_LYR = 31;
    static final int ALG_LYR_T1 = 33;
    static final int ALG_LYR_T2 = 63;
    static final int ALG_LYR_T3 = 101;
    static final int MAX_NUM_ALGS = 126;
    
    static final int NUM_FXMODS = 18;
    static final int NUM_FX_LENGTHS = 48; // all possible lengths of fxparameter list
    
    // DSP Function block Types
    static enum FB_ENUM 
        {
        OFF(0), AMP(1), FRQ(2), RES(3), AMT(4), DRV(5), WID(6), PCH(7), WRP(8), POS(9), 
        XFD(10), EVN(11), ODD(12), SEP(13), DEP (14), WPW(15), PLF(16), NON(17);
        int value;
        FB_ENUM(int value) 
            {
            this.value = value;
            }
        public int getValue() 
            {
            return value;
            }
        };
    
    static int selLayer = 0;
    
    static byte[] programdata = null;
    static boolean resumeparse = false;
    static int prevstudio = 0;
    static int pddi = 0;
    
    static JComponent[] layerPanel = new JComponent[MAX_NUM_LAYERS];
    static JComponent[] layerDspPanel = new JComponent[MAX_NUM_LAYERS];
    static JComponent[] layerEnvPanel = new JComponent[MAX_NUM_LAYERS];
    static JComponent[] layerModPanel = new JComponent[MAX_NUM_LAYERS];
    
    static LabelledDial calalg[] = new LabelledDial[MAX_NUM_LAYERS];
    static Chooser funcdummy[][] = new Chooser[4][MAX_NUM_LAYERS];
    
    static Algorithm alg[] = new Algorithm[MAX_NUM_ALGS];
    
    static Function fEmpty;
    static Function fPitch;
    static Function fPan;
    static Function fAmp;
    static Function fAmpF2;
    static Function fGain;
    static Function fSyncm;
    static Function fSyncs;
    static Function fLopas2;
    static Function fShapemodosc;
    static Function fA1;
    static Function fA2;
    static Function fA3F1;
    static Function fA3F3;
    static Function fA4;
    static Function fA5;
    static Function fA6F2;
    static Function fA6F4;
    static Function fA7;
    static Function fA8F1;
    static Function fA8F2;
    static Function fA9;
    static Function fA10F1;
    static Function fA10F2;
    static Function fA16;
    static Function fA17;
    static Function fA18;
    static Function fA20;
    static Function fA33F1;
    static Function fA33F2;
    static Function fA33F3;
    static Function fA34F1;
    static Function fA34F2;
    static Function fA34F3;
    static Function fA34F4;
    static Function fA37F1;
    static Function fA37F2;
    static Function fA40F1;
    static Function fA40F2;
    static Function fA40F3;
    static Function fA40F4;
    static Function fA64F1;
    static Function fA64F2;
    static Function fA64F3;
    static Function fA66F2;
    static Function fA66F3;
    static Function fA66F4;
    static Function fA80;
    static Function fA81;
    static Function fA82F1;
    static Function fA82F2;
    static Function fA82F3;
    static Function fA82F4;
    static Function fA104F1;
    static Function fA104F3;
    static Function fA112;
    static Function fA123;
    
    FunctionBlock functionBlock = new FunctionBlock(this);
    OutCom outCom = new OutCom(this);
    FxAlgorithm fxAlgorithm = new FxAlgorithm(this);
    FxStudio fxStudio = new FxStudio(this);
    KeymapName keymapName = new KeymapName();
    
    public KurzweilK2600()
        {
        System.out.println("KurzweilK2600");
        model.setUpdateListeners(false);
        
        if (parametersToIndex == null)
            {
            parametersToIndex = new HashMap();
            for(int i = 0; i < parameters.length; i++)
                {
                parametersToIndex.put(parameters[i], Integer.valueOf(i));
                }
            }
        
        Instant previous, current;
        previous = Instant.now();
        
        selLayer = 0;
        model.set("numlayers", 1);
        model.setMin("numlayers", 1);
        model.setMax("numlayers", MAX_NUM_LAYERS);
        
        makeStringArray(CONTROL_LIST, CONTROL_LIST_STR, CONTROL_MAP);
        makeStringArray(CONTROL_LIST, CONTROL_LIST_STR_GE, CONTROL_MAP_GE);
        makeStringArray(CONTROL_LIST, CONTROL_LIST_FUN_STR, CONTROL_MAP_FUN);
        makeStringArray(CONTROL_LIST, CONTROL_LIST_FUN_GE_STR, CONTROL_MAP_FUN_GE);
        makeStringArray(CONTROL_LIST, CONTROL_LIST_FX_STR, CONTROL_MAP_FX);
        for (int i = 110; i < 120; i++) CONTROL_LIST_FX_STR[i] = "FX" + CONTROL_LIST_FX_STR[i];
        makeStringArray(CONTROL_LIST, CONTROL_LIST_FUN_FX_STR, CONTROL_MAP_FUN_FX);
        for (int i = 110; i < 120; i++) CONTROL_LIST_FUN_FX_STR[i] = "FX" + CONTROL_LIST_FUN_FX_STR[i];
        
        // DSP function selection lists
        final int dspselEmpty[] = new int[] {115};
        final int dspselPitch[] = new int[] {0};
        final int dspselPan[] = new int[] {40};
        final int dspselAmp[] = new int[] {1};
        final int dspselAmp2[] = new int[] {104};
        final int dspselGain[] = new int[] {18};
        final int dspselSyncm[] = new int[] {33};
        final int dspselSyncs[] = new int[] {34};
        final int dspselLopas2[] = new int[] {37};
        final int dspselShapemodosc[] = new int[] {68};
        final int dspselA1[] = new int[] {12, 13, 14, 50, 54, 55, 56, 62};
        final int dspselA2[] = new int[] {64, 2, 3, 4, 5, 8, 9, 51, 61};
        final int dspselA3F1[] = new int[] {64, 2, 3, 4, 5, 61};
        final int dspselA3F3[] = new int[] {38, 39};
        final int dspselA4[] = new int[] {70, 71, 76, 15, 16, 17, 18, 19, 20, 25, 26, 53, 60};
        final int dspselA5[] = new int[] {73, 74, 77, 78, 35, 36, 37, 52, 57, 60};
        final int dspselA6F2[] = new int[] {105, 106, 107};
        final int dspselA6F4[] = new int[] {48, 49, 75};
        final int dspselA7[] = new int[] {70, 71, 76, 15, 16, 17, 18, 19, 20, 23, 24, 25, 26, 53, 60};
        final int dspselA8[] = new int[] {15, 16, 17, 18, 19, 20, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 60};
        final int dspselA9[] = new int[] {15, 16, 17, 18, 19, 20, 25, 26, 31, 60};
        final int dspselA10[] = new int[] {15, 16, 17, 18, 19, 20, 23, 24, 25, 26, 27, 28, 29, 30, 31, 60};
        final int dspselA16[] = new int[] {8, 9, 61};
        final int dspselA17[] = new int[] {68, 72, 61};
        final int dspselA18[] = new int[] {66, 67, 61};
        final int dspselA20[] = new int[] {41, 42, 43, 44, 60};
        final int dspselA33[] = new int[] {61, 64, 79, 2, 3, 4, 5, 8, 9, 51};
        final int dspselA34[] = new int[] {60, 102, 15, 16, 17, 18, 19, 20, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31};
        final int dspselA37[] = new int[] {62, 81, 82};
        final int dspselA40[] = new int[] {60, 41, 42, 43, 44};
        final int dspselA64[] = new int[] {61, 83, 84, 85, 86, 87, 88, 89, 51};
        final int dspselA66[] = new int[] {63, 41, 42, 80};
        final int dspselA80[] = new int[] {61, 10, 11};
        final int dspselA81[] = new int[] {62, 95, 96, 97, 98, 99, 100, 101};
        final int dspselA82[] = new int[] {60, 102, 15, 16, 17, 18, 19, 20, 23, 24, 25, 26, 53, 70, 71, 76};
        final int dspselA104F1[] = new int[] {60, 108, 109, 110, 111, 112, 113, 114};
        final int dspselA104F3[] = new int[] {60, 35, 36, 37, 52, 57, 73, 74};
        final int dspselA112[] = new int[] {61, 83, 87, 88, 89, 103};
        final int dspselA123[] = new int[] {60, 102, 15, 16, 17, 18, 19, 20, 22, 23, 24, 25, 26, 53, 70, 71, 76};
        
        // DSP functions per Function : selection list, no. of blocks, alg (0 = don't care), page
        fEmpty = new Function(dspselEmpty, 1, 128, 0);
        fPitch = new Function(dspselPitch, 1, 129, 0);
        fPan = new Function(dspselPan, 1, 130, 3);
        fAmp = new Function(dspselAmp, 1, 131, 4);
        fAmpF2 = new Function(dspselAmp2, 1, 132, 2);
        fGain = new Function(dspselGain, 1, 133, 4); 
        fSyncm = new Function(dspselSyncm, 1, 134, 1);
        fSyncs = new Function(dspselSyncs, 1, 135, 2);
        fLopas2 = new Function(dspselLopas2, 1, 136, 1);
        fShapemodosc = new Function(dspselShapemodosc, 2, 137, 2);
        fA1 = new Function(dspselA1, 3, 1, 1);
        fA2 = new Function(dspselA2, 2, 2, 1);
        fA3F1 = new Function(dspselA3F1, 2, 3, 1);
        fA3F3 = new Function(dspselA3F3, 2, 3, 3);
        fA4 = new Function(dspselA4, 1, 4, 3);
        fA5 = new Function(dspselA5, 1, 5, 3);
        fA6F2 = new Function(dspselA6F2, 1, 6, 2);
        fA6F4 = new Function(dspselA6F4, 1, 6, 4);
        fA7 = new Function(dspselA7, 1, 7, 3);
        fA8F1 = new Function(dspselA8, 1, 8, 1);
        fA8F2 = new Function(dspselA8, 1, 8, 2);
        fA9 = new Function(dspselA9, 1, 9, 2);
        fA10F1 = new Function(dspselA10, 1, 10, 1);
        fA10F2 = new Function(dspselA10, 1, 10, 2);
        fA16 = new Function(dspselA16, 2, 16, 2);
        fA17 = new Function(dspselA17, 2, 17, 2);
        fA18 = new Function(dspselA18, 2, 18, 2);
        fA20 = new Function(dspselA20, 1, 20, 2);
        fA33F1 = new Function(dspselA33, 2, 33, 1);
        fA33F2 = new Function(dspselA33, 2, 33, 2);
        fA33F3 = new Function(dspselA33, 2, 33, 3);
        fA34F1 = new Function(dspselA34, 1, 34, 1);
        fA34F2 = new Function(dspselA34, 1, 34, 2);
        fA34F3 = new Function(dspselA34, 1, 34, 3);
        fA34F4 = new Function(dspselA34, 1, 34, 4);
        fA37F1 = new Function(dspselA37, 3, 37, 1);
        fA37F2 = new Function(dspselA37, 3, 37, 2);
        fA40F1 = new Function(dspselA40, 1, 40, 1);
        fA40F2 = new Function(dspselA40, 1, 40, 2);
        fA40F3 = new Function(dspselA40, 1, 40, 3);
        fA40F4 = new Function(dspselA40, 1, 40, 4);
        fA64F1 = new Function(dspselA64, 2, 64, 1);
        fA64F2 = new Function(dspselA64, 2, 64, 2);
        fA64F3 = new Function(dspselA64, 2, 64, 3);
        fA66F2 = new Function(dspselA66, 1, 66, 2);
        fA66F3 = new Function(dspselA66, 1, 66, 3);
        fA66F4 = new Function(dspselA66, 1, 66, 4);
        fA80 = new Function(dspselA80, 2, 80, 2);
        fA81 = new Function(dspselA81, 3, 81, 1);
        fA82F1 = new Function(dspselA82, 1, 82, 1);
        fA82F2 = new Function(dspselA82, 1, 82, 2);
        fA82F3 = new Function(dspselA82, 1, 82, 3);
        fA82F4 = new Function(dspselA82, 1, 82, 4);
        fA104F1 = new Function(dspselA104F1, 1, 104, 1);
        fA104F3 = new Function(dspselA104F3, 1, 104, 3);
        fA112 = new Function(dspselA112, 2, 112, 2);
        fA123 = new Function(dspselA123, 1, 123, 3);
        
        // index is calalg - 1, so alg[0] is Algorithm 1
        alg[0] = new Algorithm();
        alg[0].addFunction(fPitch);
        alg[0].addFunction(fA1);
        alg[0].addFunction(fAmp);
        
        alg[1] = new Algorithm();
        alg[1].addFunction(fPitch);
        alg[1].addFunction(fA2);
        alg[1].addFunction(fPan);
        alg[1].addFunction(fAmp);
        
        alg[2] = new Algorithm();
        alg[2].addFunction(fPitch);
        alg[2].addFunction(fA3F1);
        alg[2].addFunction(fA3F3);
        
        alg[3] = new Algorithm();
        alg[3].addFunction(fPitch);
        alg[3].addFunction(fA2);
        alg[3].addFunction(fA4);
        alg[3].addFunction(fAmp);
        
        alg[4] = new Algorithm();
        alg[4].addFunction(fPitch);
        alg[4].addFunction(fA2);
        alg[4].addFunction(fA5);
        alg[4].addFunction(fAmp);
        
        alg[5] = new Algorithm();
        alg[5].addFunction(fPitch);
        alg[5].addFunction(fA3F1);
        alg[5].addFunction(fA4);
        alg[5].addFunction(fA6F4);
        
        alg[6] = new Algorithm();
        alg[6].addFunction(fPitch);
        alg[6].addFunction(fA3F1);
        alg[6].addFunction(fA7);
        alg[6].addFunction(fA6F4);
        
        alg[7] = new Algorithm();
        alg[7].addFunction(fPitch);
        alg[7].addFunction(fA8F1);
        alg[7].addFunction(fA9);
        alg[7].addFunction(fA4);
        alg[7].addFunction(fAmp);
        
        alg[8] = new Algorithm();
        alg[8].addFunction(fPitch);
        alg[8].addFunction(fA8F1);
        alg[8].addFunction(fA9);
        alg[8].addFunction(fA5);
        alg[8].addFunction(fAmp);
        
        alg[9] = new Algorithm();
        alg[9].addFunction(fPitch);
        alg[9].addFunction(fA8F1);
        alg[9].addFunction(fA10F2);
        alg[9].addFunction(fA4);
        alg[9].addFunction(fA6F4);
        
        alg[10] = new Algorithm();
        alg[10].addFunction(fPitch);
        alg[10].addFunction(fA8F1);
        alg[10].addFunction(fA10F2);
        alg[10].addFunction(fA7);
        alg[10].addFunction(fA6F4);
        
        alg[11] = new Algorithm();
        alg[11].addFunction(fPitch);
        alg[11].addFunction(fA8F1);
        alg[11].addFunction(fA8F2);
        alg[11].addFunction(fA4);
        alg[11].addFunction(fA6F4);
        
        alg[12] = new Algorithm();
        alg[12].addFunction(fPitch);
        alg[12].addFunction(fA8F1);
        alg[12].addFunction(fA9);
        alg[12].addFunction(fPan);
        alg[12].addFunction(fAmp);
        
        alg[13] = new Algorithm();
        alg[13].addFunction(fPitch);
        alg[13].addFunction(fA10F1);
        alg[13].addFunction(fA10F2);
        alg[13].addFunction(fA3F3);
        
        alg[14] = new Algorithm();
        alg[14].addFunction(fPitch);
        alg[14].addFunction(fA8F1);
        alg[14].addFunction(fA10F2);
        alg[14].addFunction(fA3F3);
        
        alg[15] = new Algorithm();
        alg[15].addFunction(fPitch);
        alg[15].addFunction(fA10F1);
        alg[15].addFunction(fA16);
        alg[15].addFunction(fAmp);
        
        alg[16] = new Algorithm();
        alg[16].addFunction(fPitch);
        alg[16].addFunction(fA8F1);
        alg[16].addFunction(fA17);
        alg[16].addFunction(fAmp);
        
        alg[17] = new Algorithm();
        alg[17].addFunction(fPitch);
        alg[17].addFunction(fA10F1);
        alg[17].addFunction(fA18);
        alg[17].addFunction(fAmp);
        
        alg[18] = new Algorithm();
        alg[18].addFunction(fPitch);
        alg[18].addFunction(fLopas2);
        alg[18].addFunction(fShapemodosc);
        alg[18].addFunction(fAmp);
        
        alg[19] = new Algorithm();
        alg[19].addFunction(fPitch);
        alg[19].addFunction(fA10F1);
        alg[19].addFunction(fA20);
        alg[19].addFunction(fA4);
        alg[19].addFunction(fAmp);
        
        alg[20] = new Algorithm();
        alg[20].addFunction(fPitch);
        alg[20].addFunction(fA10F1);
        alg[20].addFunction(fA20);
        alg[20].addFunction(fA5);
        alg[20].addFunction(fAmp);
        
        alg[21] = new Algorithm();
        alg[21].addFunction(fPitch);
        alg[21].addFunction(fA10F1);
        alg[21].addFunction(fA20);
        alg[21].addFunction(fA7);
        alg[21].addFunction(fA6F4);
        
        alg[22] = new Algorithm();
        alg[22].addFunction(fPitch);
        alg[22].addFunction(fA10F1);
        alg[22].addFunction(fA20);
        alg[22].addFunction(fA7);
        alg[22].addFunction(fA6F4);
        
        alg[23] = new Algorithm();
        alg[23].addFunction(fPitch);
        alg[23].addFunction(fA10F1);
        alg[23].addFunction(fA20);
        alg[23].addFunction(fPan);
        alg[23].addFunction(fAmp);
        
        alg[24] = new Algorithm();
        alg[24].addFunction(fPitch);
        alg[24].addFunction(fA10F1);
        alg[24].addFunction(fA20);
        alg[24].addFunction(fA3F3);
        
        alg[25] = new Algorithm();
        alg[25].addFunction(fEmpty);
        alg[25].addFunction(fSyncm);
        alg[25].addFunction(fSyncs);
        alg[25].addFunction(fPan);
        alg[25].addFunction(fAmp);
        
        alg[26] = new Algorithm();
        alg[26].addFunction(fEmpty);
        alg[26].addFunction(fSyncm);
        alg[26].addFunction(fSyncs);
        alg[26].addFunction(fA7);
        alg[26].addFunction(fAmp);
        
        alg[27] = new Algorithm();
        alg[27].addFunction(fEmpty);
        alg[27].addFunction(fSyncm);
        alg[27].addFunction(fSyncs);
        alg[27].addFunction(fA5);
        alg[27].addFunction(fAmp);
        
        alg[28] = new Algorithm();
        alg[28].addFunction(fEmpty);
        alg[28].addFunction(fSyncm);
        alg[28].addFunction(fSyncs);
        alg[28].addFunction(fA7);
        alg[28].addFunction(fA6F4);
        
        alg[29] = new Algorithm();
        alg[29].addFunction(fEmpty);
        alg[29].addFunction(fSyncm);
        alg[29].addFunction(fSyncs);
        alg[29].addFunction(fA7);
        alg[29].addFunction(fA6F4);
        
        alg[30] = new Algorithm();
        alg[30].addFunction(fEmpty);
        alg[30].addFunction(fSyncm);
        alg[30].addFunction(fSyncs);
        alg[30].addFunction(fA3F3);
        
        alg[31] = new Algorithm(); // not used
        alg[31].addFunction(fEmpty);
        
        // triple algorithms
        alg[32] = new Algorithm();
        alg[32].addFunction(fPitch);
        alg[32].addFunction(fA33F1);
        alg[32].addFunction(fA33F3);
        
        alg[33] = new Algorithm();
        alg[33].addFunction(fPitch);
        alg[33].addFunction(fA34F1);
        alg[33].addFunction(fA33F2);
        alg[33].addFunction(fA34F4);
        
        alg[34] = new Algorithm();
        alg[34].addFunction(fPitch);
        alg[34].addFunction(fA33F1);
        alg[34].addFunction(fA34F3);
        alg[34].addFunction(fA34F4);
        
        alg[35] = new Algorithm();
        alg[35].addFunction(fPitch);
        alg[35].addFunction(fA34F1);
        alg[35].addFunction(fA34F2);
        alg[35].addFunction(fA33F3);
        
        alg[36] = new Algorithm();
        alg[36].addFunction(fPitch);
        alg[36].addFunction(fA37F1);
        alg[36].addFunction(fA34F4);
        
        alg[37] = new Algorithm();
        alg[37].addFunction(fPitch);
        alg[37].addFunction(fA34F1);
        alg[37].addFunction(fA37F2);
        
        alg[38] = new Algorithm();
        alg[38].addFunction(fPitch);
        alg[38].addFunction(fA34F1);
        alg[38].addFunction(fA34F2);
        alg[38].addFunction(fA34F3);
        alg[38].addFunction(fA34F4);
        
        alg[39] = new Algorithm();
        alg[39].addFunction(fPitch);
        alg[39].addFunction(fA34F1);
        alg[39].addFunction(fA34F2);
        alg[39].addFunction(fA34F3);
        alg[39].addFunction(fA40F4);
        
        alg[40] = new Algorithm();
        alg[40].addFunction(fPitch);
        alg[40].addFunction(fA34F1);
        alg[40].addFunction(fA34F2);
        alg[40].addFunction(fA34F3);
        alg[40].addFunction(fA40F4);
        
        alg[41] = new Algorithm();
        alg[41].addFunction(fPitch);
        alg[41].addFunction(fA34F1);
        alg[41].addFunction(fA34F2);
        alg[41].addFunction(fA34F3);
        alg[41].addFunction(fA40F4);
        
        alg[42] = new Algorithm();
        alg[42].addFunction(fPitch);
        alg[42].addFunction(fA34F1);
        alg[42].addFunction(fA34F2);
        alg[42].addFunction(fA40F3);
        alg[42].addFunction(fA34F4);
        
        alg[43] = new Algorithm();
        alg[43].addFunction(fPitch);
        alg[43].addFunction(fA34F1);
        alg[43].addFunction(fA34F2);
        alg[43].addFunction(fA40F3);
        alg[43].addFunction(fA34F4);
        
        alg[44] = new Algorithm();
        alg[44].addFunction(fPitch);
        alg[44].addFunction(fA34F1);
        alg[44].addFunction(fA40F2);
        alg[44].addFunction(fA34F3);
        alg[44].addFunction(fA34F4);
        
        alg[45] = new Algorithm();
        alg[45].addFunction(fPitch);
        alg[45].addFunction(fA34F1);
        alg[45].addFunction(fA40F2);
        alg[45].addFunction(fA33F3);
        
        alg[46] = new Algorithm();
        alg[46].addFunction(fPitch);
        alg[46].addFunction(fA33F1);
        alg[46].addFunction(fA34F3);
        alg[46].addFunction(fA40F4);
        
        alg[47] = new Algorithm();
        alg[47].addFunction(fPitch);
        alg[47].addFunction(fA34F1);
        alg[47].addFunction(fA33F2);
        alg[47].addFunction(fA40F4);
        
        alg[48] = new Algorithm();
        alg[48].addFunction(fPitch);
        alg[48].addFunction(fA33F1);
        alg[48].addFunction(fA34F3);
        alg[48].addFunction(fA40F4);
        
        alg[49] = new Algorithm();
        alg[49].addFunction(fPitch);
        alg[49].addFunction(fA34F1);
        alg[49].addFunction(fA33F2);
        alg[49].addFunction(fA40F4);
        
        alg[50] = new Algorithm();
        alg[50].addFunction(fPitch);
        alg[50].addFunction(fA37F1);
        alg[50].addFunction(fA40F4);
        
        alg[51] = new Algorithm();
        alg[51].addFunction(fPitch);
        alg[51].addFunction(fA33F1);
        alg[51].addFunction(fA40F3);
        alg[51].addFunction(fA34F4);
        
        alg[52] = new Algorithm();
        alg[52].addFunction(fPitch);
        alg[52].addFunction(fA34F1);
        alg[52].addFunction(fA40F2);
        alg[52].addFunction(fA34F3);
        alg[52].addFunction(fA40F4);
        
        alg[53] = new Algorithm();
        alg[53].addFunction(fPitch);
        alg[53].addFunction(fA34F1);
        alg[53].addFunction(fA40F2);
        alg[53].addFunction(fA34F3);
        alg[53].addFunction(fA40F4);
        
        alg[54] = new Algorithm();
        alg[54].addFunction(fPitch);
        alg[54].addFunction(fA34F1);
        alg[54].addFunction(fA40F2);
        alg[54].addFunction(fA40F3);
        alg[54].addFunction(fA34F4);
        
        alg[55] = new Algorithm();
        alg[55].addFunction(fPitch);
        alg[55].addFunction(fA34F1);
        alg[55].addFunction(fA34F2);
        alg[55].addFunction(fA40F3);
        alg[55].addFunction(fA40F4);
        
        alg[56] = new Algorithm();
        alg[56].addFunction(fPitch);
        alg[56].addFunction(fA34F1);
        alg[56].addFunction(fA34F2);
        alg[56].addFunction(fA40F3);
        alg[56].addFunction(fA40F4);
        
        alg[57] = new Algorithm();
        alg[57].addFunction(fPitch);
        alg[57].addFunction(fA33F1);
        alg[57].addFunction(fA40F3);
        alg[57].addFunction(fA40F4);
        
        alg[58] = new Algorithm();
        alg[58].addFunction(fPitch);
        alg[58].addFunction(fA34F1);
        alg[58].addFunction(fA40F2);
        alg[58].addFunction(fA40F3);
        alg[58].addFunction(fA40F4);
        
        alg[59] = new Algorithm();
        alg[59].addFunction(fEmpty);
        alg[59].addFunction(fSyncm);
        alg[59].addFunction(fSyncs);
        alg[59].addFunction(fA34F3);
        alg[59].addFunction(fA40F4);
        
        alg[60] = new Algorithm();
        alg[60].addFunction(fEmpty);
        alg[60].addFunction(fSyncm);
        alg[60].addFunction(fSyncs);
        alg[60].addFunction(fA40F3);
        alg[60].addFunction(fA34F4);
        
        alg[61] = new Algorithm();
        alg[61].addFunction(fEmpty);
        alg[61].addFunction(fSyncm);
        alg[61].addFunction(fSyncs);
        alg[61].addFunction(fA40F3);
        alg[61].addFunction(fA40F4);
        
        alg[62] = new Algorithm();
        alg[62].addFunction(fPitch);
        alg[62].addFunction(fA40F1);
        alg[62].addFunction(fA34F2);
        alg[62].addFunction(fA82F3);
        alg[62].addFunction(fA82F4);
        
        alg[63] = new Algorithm();
        alg[63].addFunction(fPitch);
        alg[63].addFunction(fA40F1);
        alg[63].addFunction(fA34F2);
        alg[63].addFunction(fA64F3);
        
        alg[64] = new Algorithm();
        alg[64].addFunction(fPitch);
        alg[64].addFunction(fA34F1);
        alg[64].addFunction(fA40F2);
        alg[64].addFunction(fA82F3);
        alg[64].addFunction(fA82F4);
        
        alg[65] = new Algorithm();
        alg[65].addFunction(fPitch);
        alg[65].addFunction(fA34F1);
        alg[65].addFunction(fA40F2);
        alg[65].addFunction(fA82F3);
        alg[65].addFunction(fA66F4);
        
        alg[66] = new Algorithm();
        alg[66].addFunction(fPitch);
        alg[66].addFunction(fA34F1);
        alg[66].addFunction(fA40F2);
        alg[66].addFunction(fA64F3);
        
        alg[67] = new Algorithm();
        alg[67].addFunction(fPitch);
        alg[67].addFunction(fA34F1);
        alg[67].addFunction(fA40F2);
        alg[67].addFunction(fA82F3);
        alg[67].addFunction(fA82F4);
        
        alg[68] = new Algorithm();
        alg[68].addFunction(fPitch);
        alg[68].addFunction(fA34F1);
        alg[68].addFunction(fA40F2);
        alg[68].addFunction(fA64F3);
        
        alg[69] = new Algorithm();
        alg[69].addFunction(fPitch);
        alg[69].addFunction(fA34F1);
        alg[69].addFunction(fA34F2);
        alg[69].addFunction(fA82F3);
        alg[69].addFunction(fA66F4);
        
        alg[70] = new Algorithm();
        alg[70].addFunction(fPitch);
        alg[70].addFunction(fA34F1);
        alg[70].addFunction(fA34F2);
        alg[70].addFunction(fA82F3);
        alg[70].addFunction(fA66F4);
        
        alg[71] = new Algorithm();
        alg[71].addFunction(fPitch);
        alg[71].addFunction(fA40F1);
        alg[71].addFunction(fA34F2);
        alg[71].addFunction(fA82F3);
        alg[71].addFunction(fA66F4);
        
        alg[72] = new Algorithm();
        alg[72].addFunction(fPitch);
        alg[72].addFunction(fA40F1);
        alg[72].addFunction(fA34F2);
        alg[72].addFunction(fA82F3);
        alg[72].addFunction(fA66F4);
        
        alg[73] = new Algorithm();
        alg[73].addFunction(fPitch);
        alg[73].addFunction(fA40F1);
        alg[73].addFunction(fA34F2);
        alg[73].addFunction(fA82F3);
        alg[73].addFunction(fA66F4);
        
        alg[74] = new Algorithm();
        alg[74].addFunction(fPitch);
        alg[74].addFunction(fA40F1);
        alg[74].addFunction(fA34F2);
        alg[74].addFunction(fA82F3);
        alg[74].addFunction(fA66F4);
        
        alg[75] = new Algorithm();
        alg[75].addFunction(fPitch);
        alg[75].addFunction(fA40F1);
        alg[75].addFunction(fA34F2);
        alg[75].addFunction(fA82F3);
        alg[75].addFunction(fA66F4);
        
        alg[76] = new Algorithm();
        alg[76].addFunction(fPitch);
        alg[76].addFunction(fA40F1);
        alg[76].addFunction(fA34F2);
        alg[76].addFunction(fA82F3);
        alg[76].addFunction(fA66F4);
        
        alg[77] = new Algorithm();
        alg[77].addFunction(fPitch);
        alg[77].addFunction(fA40F1);
        alg[77].addFunction(fA34F2);
        alg[77].addFunction(fA82F3);
        alg[77].addFunction(fA66F4);
        
        alg[78] = new Algorithm();
        alg[78].addFunction(fPitch);
        alg[78].addFunction(fA40F1);
        alg[78].addFunction(fA34F2);
        alg[78].addFunction(fA82F3);
        alg[78].addFunction(fA66F4);
        
        alg[79] = new Algorithm();
        alg[79].addFunction(fPitch);
        alg[79].addFunction(fA40F1);
        alg[79].addFunction(fA80);
        alg[79].addFunction(fGain);
        
        alg[80] = new Algorithm();
        alg[80].addFunction(fEmpty);
        alg[80].addFunction(fA81);
        alg[80].addFunction(fGain);
        
        alg[81] = new Algorithm();
        alg[81].addFunction(fEmpty);
        alg[81].addFunction(fA33F1);
        alg[81].addFunction(fA82F3);
        alg[81].addFunction(fA82F4);
        
        alg[82] = new Algorithm();
        alg[82].addFunction(fEmpty);
        alg[82].addFunction(fA33F1);
        alg[82].addFunction(fA64F3);
        
        alg[83] = new Algorithm();
        alg[83].addFunction(fEmpty);
        alg[83].addFunction(fA33F1);
        alg[83].addFunction(fA82F3);
        alg[83].addFunction(fA66F4);
        
        alg[84] = new Algorithm();
        alg[84].addFunction(fEmpty);
        alg[84].addFunction(fA33F1);
        alg[84].addFunction(fA82F3);
        alg[84].addFunction(fA66F4);
        
        alg[85] = new Algorithm();
        alg[85].addFunction(fEmpty);
        alg[85].addFunction(fA34F1);
        alg[85].addFunction(fA34F2);
        alg[85].addFunction(fA82F3);
        alg[85].addFunction(fA82F4);
        
        alg[86] = new Algorithm();
        alg[86].addFunction(fEmpty);
        alg[86].addFunction(fA34F1);
        alg[86].addFunction(fA34F2);
        alg[86].addFunction(fA64F3);
        
        alg[87] = new Algorithm();
        alg[87].addFunction(fEmpty);
        alg[87].addFunction(fA34F1);
        alg[87].addFunction(fA34F2);
        alg[87].addFunction(fA82F3);
        alg[87].addFunction(fA66F4);
        
        alg[88] = new Algorithm();
        alg[88].addFunction(fEmpty);
        alg[88].addFunction(fA34F1);
        alg[88].addFunction(fA34F2);
        alg[88].addFunction(fA82F3);
        alg[88].addFunction(fA66F4);
        
        alg[89] = new Algorithm();
        alg[89].addFunction(fEmpty);
        alg[89].addFunction(fA34F1);
        alg[89].addFunction(fA34F2);
        alg[89].addFunction(fA82F3);
        alg[89].addFunction(fA66F4);
        
        alg[90] = new Algorithm();
        alg[90].addFunction(fEmpty);
        alg[90].addFunction(fA34F1);
        alg[90].addFunction(fA80);
        alg[90].addFunction(fGain);
        
        alg[91] = new Algorithm();
        alg[91].addFunction(fEmpty);
        alg[91].addFunction(fA34F1);
        alg[91].addFunction(fA17);
        alg[91].addFunction(fGain);
        
        alg[92] = new Algorithm();
        alg[92].addFunction(fEmpty);
        alg[92].addFunction(fA34F1);
        alg[92].addFunction(fA18);
        alg[92].addFunction(fGain);
        
        alg[93] = new Algorithm();
        alg[93].addFunction(fEmpty);
        alg[93].addFunction(fLopas2);
        alg[93].addFunction(fShapemodosc);
        alg[93].addFunction(fGain);
        
        alg[94] = new Algorithm();
        alg[94].addFunction(fEmpty);
        alg[94].addFunction(fA34F1);
        alg[94].addFunction(fA40F2);
        alg[94].addFunction(fA82F3);
        alg[94].addFunction(fA82F4);
        
        alg[95] = new Algorithm();
        alg[95].addFunction(fEmpty);
        alg[95].addFunction(fA34F1);
        alg[95].addFunction(fA40F2);
        alg[95].addFunction(fA64F3);
        
        alg[96] = new Algorithm();
        alg[96].addFunction(fEmpty);
        alg[96].addFunction(fA34F1);
        alg[96].addFunction(fA40F2);
        alg[96].addFunction(fA82F3);
        alg[96].addFunction(fA66F4);
        
        alg[97] = new Algorithm();
        alg[97].addFunction(fEmpty);
        alg[97].addFunction(fA34F1);
        alg[97].addFunction(fA40F2);
        alg[97].addFunction(fA82F3);
        alg[97].addFunction(fA66F4);
        
        alg[98] = new Algorithm();
        alg[98].addFunction(fEmpty);
        alg[98].addFunction(fA34F1);
        alg[98].addFunction(fA34F2);
        alg[98].addFunction(fA34F3);
        alg[98].addFunction(fA40F4);
        
        alg[99] = new Algorithm();
        alg[99].addFunction(fEmpty);
        alg[99].addFunction(fA33F1);
        alg[99].addFunction(fA82F3);
        alg[99].addFunction(fA66F4);
        
        alg[100] = new Algorithm();
        alg[100].addFunction(fEmpty);
        alg[100].addFunction(fA64F1);
        alg[100].addFunction(fA82F3);
        alg[100].addFunction(fAmp);
        
        alg[101] = new Algorithm();
        alg[101].addFunction(fEmpty);
        alg[101].addFunction(fA82F1);
        alg[101].addFunction(fA82F2);
        alg[101].addFunction(fA82F3);
        alg[101].addFunction(fAmp);
        
        alg[102] = new Algorithm();
        alg[102].addFunction(fEmpty);
        alg[102].addFunction(fA82F1);
        alg[102].addFunction(fA64F2);
        alg[102].addFunction(fAmp);
        
        alg[103] = new Algorithm();
        alg[103].addFunction(fEmpty);
        alg[103].addFunction(fA82F1);
        alg[103].addFunction(fA82F2);
        alg[103].addFunction(fA104F3);
        alg[103].addFunction(fAmp);
        
        alg[104] = new Algorithm();
        alg[104].addFunction(fEmpty);
        alg[104].addFunction(fA64F1);
        alg[104].addFunction(fA104F3);
        alg[104].addFunction(fAmp);
        
        alg[105] = new Algorithm();
        alg[105].addFunction(fEmpty);
        alg[105].addFunction(fA82F1);
        alg[105].addFunction(fA82F2);
        alg[105].addFunction(fPan);
        alg[105].addFunction(fAmp);
        
        alg[106] = new Algorithm();
        alg[106].addFunction(fEmpty);
        alg[106].addFunction(fA64F1);
        alg[106].addFunction(fPan);
        alg[106].addFunction(fAmp);
        
        alg[107] = new Algorithm();
        alg[107].addFunction(fEmpty);
        alg[107].addFunction(fA82F1);
        alg[107].addFunction(fA82F2);
        alg[107].addFunction(fA3F3);
        
        alg[108] = new Algorithm();
        alg[108].addFunction(fEmpty);
        alg[108].addFunction(fA64F1);
        alg[108].addFunction(fA82F3);
        alg[108].addFunction(fA6F4);
        
        alg[109] = new Algorithm();
        alg[109].addFunction(fEmpty);
        alg[109].addFunction(fA64F1);
        alg[109].addFunction(fA82F3);
        alg[109].addFunction(fA6F4);
        
        alg[110] = new Algorithm();
        alg[110].addFunction(fEmpty);
        alg[110].addFunction(fA82F1);
        alg[110].addFunction(fA64F2);
        alg[110].addFunction(fA6F4);
        
        alg[111] = new Algorithm();
        alg[111].addFunction(fEmpty);
        alg[111].addFunction(fA82F1);
        alg[111].addFunction(fA112);
        alg[111].addFunction(fA6F4);
        
        alg[112] = new Algorithm();
        alg[112].addFunction(fEmpty);
        alg[112].addFunction(fA82F1);
        alg[112].addFunction(fA82F2);
        alg[112].addFunction(fA82F3);
        alg[112].addFunction(fA6F4);
        
        alg[113] = new Algorithm();
        alg[113].addFunction(fEmpty);
        alg[113].addFunction(fA82F1);
        alg[113].addFunction(fA82F2);
        alg[113].addFunction(fA82F3);
        alg[113].addFunction(fA6F4);
        
        alg[114] = new Algorithm();
        alg[114].addFunction(fEmpty);
        alg[114].addFunction(fA82F1);
        alg[114].addFunction(fA82F2);
        alg[114].addFunction(fA82F3);
        alg[114].addFunction(fA6F4);
        
        alg[115] = new Algorithm();
        alg[115].addFunction(fEmpty);
        alg[115].addFunction(fA82F1);
        alg[115].addFunction(fA66F2);
        alg[115].addFunction(fA82F3);
        alg[115].addFunction(fAmp);
        
        alg[116] = new Algorithm();
        alg[116].addFunction(fEmpty);
        alg[116].addFunction(fA82F1);
        alg[116].addFunction(fA66F2);
        alg[116].addFunction(fA104F3);
        alg[116].addFunction(fAmp);
        
        alg[117] = new Algorithm();
        alg[117].addFunction(fEmpty);
        alg[117].addFunction(fA82F1);
        alg[117].addFunction(fA82F2);
        alg[117].addFunction(fA66F3);
        alg[117].addFunction(fA6F4);
        
        alg[118] = new Algorithm();
        alg[118].addFunction(fEmpty);
        alg[118].addFunction(fA34F1);
        alg[118].addFunction(fA66F2);
        alg[118].addFunction(fA82F3);
        alg[118].addFunction(fA6F4);
        
        alg[119] = new Algorithm();
        alg[119].addFunction(fEmpty);
        alg[119].addFunction(fA82F1);
        alg[119].addFunction(fA66F2);
        alg[119].addFunction(fA82F3);
        alg[119].addFunction(fA6F4);
        
        alg[120] = new Algorithm();
        alg[120].addFunction(fEmpty);
        alg[120].addFunction(fA82F1);
        alg[120].addFunction(fA66F2);
        alg[120].addFunction(fA66F3);
        alg[120].addFunction(fA6F4);
        
        alg[121] = new Algorithm();
        alg[121].addFunction(fEmpty);
        alg[121].addFunction(fA82F1);
        alg[121].addFunction(fA66F2);
        alg[121].addFunction(fPan);
        alg[121].addFunction(fAmp);
        
        alg[122] = new Algorithm();
        alg[122].addFunction(fEmpty);
        alg[122].addFunction(fA82F1);
        alg[122].addFunction(fA6F2);
        alg[122].addFunction(fA123);
        alg[122].addFunction(fA6F4);
        
        alg[123] = new Algorithm();
        alg[123].addFunction(fEmpty);
        alg[123].addFunction(fA82F1);
        alg[123].addFunction(fA6F2);
        alg[123].addFunction(fA104F3);
        alg[123].addFunction(fAmp);
        
        alg[124] = new Algorithm();
        alg[124].addFunction(fEmpty);
        alg[124].addFunction(fA82F1);
        alg[124].addFunction(fAmpF2);
        alg[124].addFunction(fA82F3);
        alg[124].addFunction(fAmp);
        
        alg[125] = new Algorithm();
        alg[125].addFunction(fEmpty);
        alg[125].addFunction(fA104F1);
        alg[125].addFunction(fAmpF2);
        alg[125].addFunction(fA104F3);
        alg[125].addFunction(fAmp);
        
        SynthPanel programPanel = new SynthPanel(this);
        
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addProgram(Style.COLOR_A()));
        vbox.add(hbox);
        
        programPanel.add(vbox, BorderLayout.CENTER);
        programPanel.makePasteable("");
        programPanel.setSendsAllParameters(false);
        addTab("Program", programPanel);

        for (int layer = 0; layer < MAX_NUM_LAYERS; layer++)
            {
            layerPanel[layer] = new SynthPanel(this);
            vbox = new VBox();
            hbox = new HBox();
            
            hbox.addLast(addLayer(layer, Style.COLOR_A()));
            vbox.add(hbox);
            
            hbox = new HBox();
            hbox.addLast(addKeymap(layer, Style.COLOR_B()));
            vbox.add(hbox);
            
            hbox = new HBox();
            hbox.addLast(addOutput(layer, Style.COLOR_C()));
            vbox.add(hbox);
            
            layerPanel[layer].add(vbox, BorderLayout.CENTER);
            
            current = Instant.now();
            // System.out.println("layer : " + layer + " : " + Duration.between(previous, current));
            previous = Instant.now();
            
            layerDspPanel[layer] = new SynthPanel(this);
            vbox = new VBox();
            hbox = new HBox();
            
            hbox.addLast(addAlg(layer, Style.COLOR_A()));
            vbox.add(hbox);
            
            hbox = new HBox();
            hbox.addLast(addPitch(layer, Style.COLOR_B()));
            vbox.add(hbox);
            
            for (int page = 0; page < 4; page ++)
                {
                hbox = new HBox();
                hbox.addLast(addFpage(page, layer, Style.COLOR_C()));
                vbox.add(hbox);
                }
                    
            layerDspPanel[layer].add(vbox, BorderLayout.CENTER);
            
            current = Instant.now();
            //System.out.println("layerdsp : " + layer + " : " + Duration.between(previous, current));
            previous = Instant.now();
            
            layerEnvPanel[layer] = new SynthPanel(this);
            vbox = new VBox();
            hbox = new HBox();
            
            hbox.addLast(addAmpenv(layer, Style.COLOR_A()));
            vbox.add(hbox);
            
            hbox = new HBox();
            hbox.addLast(addEnv23(layer, 2, Style.COLOR_A()));
            vbox.add(hbox);
            
            hbox = new HBox();
            hbox.addLast(addEnv23(layer, 3, Style.COLOR_A()));
            vbox.add(hbox);
            
            layerEnvPanel[layer].add(vbox, BorderLayout.CENTER);
            
            layerModPanel[layer] = new SynthPanel(this);
            vbox = new VBox();
            hbox = new HBox();
            
            hbox.add(addEnvctl("Attack", layer, Style.COLOR_A()));
            hbox.addLast(addEnvctl("Decay", layer, Style.COLOR_A()));
            vbox.add(hbox);
            
            hbox = new HBox();
            hbox.add(addEnvctl("Release", layer, Style.COLOR_A()));
            hbox.addLast(addEnvctl("Impact", layer, Style.COLOR_A()));
            vbox.add(hbox);
            
            hbox = new HBox();
            hbox.add(addLfo("", layer, 1, Style.COLOR_B()));
            hbox.addLast(addLfo("", layer, 2, Style.COLOR_B()));
            vbox.add(hbox);
            
            hbox = new HBox();
            hbox.add(addAsr("", layer, 1, Style.COLOR_C()));
            hbox.addLast(addAsr("", layer, 2, Style.COLOR_C()));
            vbox.add(hbox);
            
            hbox = new HBox();
            hbox.add(addFcn("", layer, 0, Style.COLOR_A()));
            hbox.add(addFcn("", layer, 1, Style.COLOR_A()));
            hbox.add(addFcn("", layer, 2, Style.COLOR_A()));
            hbox.addLast(addFcn("", layer, 3, Style.COLOR_A()));
            vbox.add(hbox);
            
            hbox = new HBox();
            hbox.add(addVtrig(0, layer, Style.COLOR_B()));
            hbox.addLast(addVtrig(1, layer, Style.COLOR_B()));
            vbox.add(hbox);
            
            layerModPanel[layer].add(vbox, BorderLayout.CENTER);
            
            current = Instant.now();
            //System.out.println("layermod : " + layer + " : " + Duration.between(previous, current));
            previous = Instant.now();
            }
        
        // layerPanel[layer].makePasteable("layer");
        addTab("Layer:1/" + model.get("numlayers"), layerPanel[0]);
        addTab("Layer Dsp:1/" + model.get("numlayers"), layerDspPanel[0]);
        addTab("Layer Env:1/" + model.get("numlayers"), layerEnvPanel[0]);
        addTab("Layer Mod:1/" + model.get("numlayers"), layerModPanel[0]);
        
        SynthPanel kdfxmodPanel = new SynthPanel(this);
        
        vbox = new VBox();
        hbox = new HBox();
        
        hbox.addLast(addKdfx(Style.COLOR_A()));
        vbox.add(hbox);
        
        for(int fxmod = 0; fxmod < NUM_FXMODS; fxmod += 6)
            { // TODO : gets too wide 
            hbox = new HBox();
            hbox.add(addFxmod(fxmod, Style.COLOR_B()));
            hbox.add(addFxmod(fxmod + 1, Style.COLOR_B()));
            hbox.add(addFxmod(fxmod + 2, Style.COLOR_B()));
            hbox.add(addFxmod(fxmod + 3, Style.COLOR_B()));
            hbox.add(addFxmod(fxmod + 4, Style.COLOR_B()));
            hbox.addLast(addFxmod(fxmod + 5, Style.COLOR_B()));
            vbox.add(hbox);
            }
        
        kdfxmodPanel.add(vbox, BorderLayout.CENTER);
        kdfxmodPanel.makePasteable("");
        addTab("KDFX FXmod", kdfxmodPanel);
        
        SynthPanel kdmodPanel = new SynthPanel(this);
        
        vbox = new VBox();
        hbox = new HBox();
        
        hbox.add(addLfo("F", 0, 1, Style.COLOR_A()));
        hbox.addLast(addLfo("F", 0, 2, Style.COLOR_A()));
        vbox.add(hbox);
        
        hbox = new HBox();
        hbox.add(addAsr("F", 0, 1, Style.COLOR_B()));
        hbox.addLast(addAsr("F", 0, 2, Style.COLOR_B()));
        vbox.add(hbox);
        
        hbox = new HBox();
        hbox.add(addFcn("F", 0, 0, Style.COLOR_C()));
        hbox.add(addFcn("F", 0, 1, Style.COLOR_C()));
        hbox.add(addFcn("F", 0, 2, Style.COLOR_C()));
        hbox.addLast(addFcn("F", 0, 3, Style.COLOR_C()));
        vbox.add(hbox);
        
        kdmodPanel.add(vbox, BorderLayout.CENTER);
        kdmodPanel.makePasteable("");
        addTab("KDFX MOD", kdmodPanel);
        
        SynthPanel kb3Panel = new SynthPanel(this);
        vbox = new VBox();
        hbox = new HBox();
        // TODO
        kb3Panel.add(vbox, BorderLayout.CENTER);
        kb3Panel.makePasteable("");
        addTab("KB3", kb3Panel);
        
        model.setUpdateListeners(true);
        
        model.set("name", "Untitled");
        model.set("bank", 0);
        model.set("number", 0);
        loadDefaults();
        }
    
    JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, getSynthName(), color);
                
        JComponent comp;
        String[] params;
        
        HBox hbox = new HBox();                  
        VBox vbox = new VBox();
        HBox inner = new HBox();                  
        
        comp = new PatchDisplay(this, 4);
        inner.add(comp);
        vbox.add(inner);
        
        comp = new StringComponent("Program Name", this, "name", MAXIMUM_NAME_LENGTH, "Name must be up to 16 characters.")
            { public String replace(String val) { return revisePatchName(val); }
                                
            public void update(String key, Model model)
                {
                super.update(key, model);
                updateTitle();
                }
            };
        vbox.add(comp);
        
        comp = new ReadOnlyString("", this, "strnumlayers", MAXIMUM_NAME_LENGTH);
        vbox.add(comp);
        
        hbox.add(vbox);
        
        /* for testing parameter inc/dec
           vbox = new VBox();

           comp = new PushButton("Inc")
           {
           public void perform()
           {
           incdecRead((byte)PLUS_BUTTON);
           }
           };
           vbox.add(comp);
        
           comp = new PushButton("Dec")
           {
           public void perform()
           {
           incdecRead((byte)MINUS_BUTTON);
           }
           };
           vbox.add(comp)
        */
        
        hbox.add(vbox);
        
        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }
    
    JComponent addProgram(Color color)
        {
        Category category = new Category(this, "Common", color);
        category.makePasteable("");
        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        VBox monoswitch = new VBox();
        VBox monodial = new VBox();
        
        comp = new CheckBox("Globals", this, "globalflag");
        vbox.add(comp);
        
        final CheckBox checklegato = new CheckBox("Legato", this, "legato");
        monoswitch.add(checklegato);
        
        final CheckBox checkporta = new CheckBox("Portamento", this, "portaflag");
        monoswitch.add(checkporta);
        
        final CheckBox checkattporta = new CheckBox("Attack Porta", this, "attportaflag");
        monoswitch.add(checkattporta);
        
        final LabelledDial porta = new LabelledDial("Portamento", this, "portslope", color, 0, 127)
            { public String map(int val) { return (FONT_SIZE_3 + PORTA_VALUES[val] + "/s" + FONT); } };
        monodial.add(porta);
        
        comp = new CheckBox("Monophonic", this, "monoflag")
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (model.get(key) == 0) 
                    {
                    monoswitch.removeAll();
                    monodial.removeAll();
                    }
                else 
                    {
                    monoswitch.add(checklegato);
                    monoswitch.add(checkporta);
                    monoswitch.add(checkattporta);
                    monodial.add(porta);
                    }
                monoswitch.revalidate();
                }
            };
        ((CheckBox)comp).addToWidth(1);
        vbox.add(comp);
        
        comp = new Chooser("Output Pair", this, "outcom", OUTPUT_PAIR_PROG)
            {
            public void update (String key, Model model)
                {
                super.update(key, model);
                if (outCom.isuCfrommodel() == true) outCom.uCnotfrommodel();
                else if (outCom.isuCfrommodel() == false) outCom.updateLayers(model.get(key));
                }
            };
        
        vbox.add(comp);
        
        hbox.add(vbox);
        hbox.add(monoswitch);
        hbox.add(monodial);
        
        comp = new BipolarMappedDial("Pitch Bend", "bendrange", color, 123, CENT_VALUES, "ct");
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
    
    JComponent addAlg(int layer, Color color)
        {
        Category category = new Category(this, "Algorithm", color);
        category.makePasteable("");
        
        JComponent comp;
        String prefix = "layer" + layer;
        HBox funcselhbox = new HBox();
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        fEmpty.getChooser()[layer] = new Chooser("", this, "", fEmpty.getStrsel());
        fEmpty.getChooser()[layer].setPreferredSize(new Dimension(120, 30));
        fPitch.getChooser()[layer] = new Chooser("", this, "", fPitch.getStrsel());
        fPitch.getChooser()[layer].setPreferredSize(new Dimension(120, 30));
        
        fAmpF2.getChooser()[layer] = new DspfChooser(fAmpF2, layer);
        fAmp.getChooser()[layer] = new DspfChooser(fAmp, layer);
        fPan.getChooser()[layer] = new DspfChooser(fPan, layer);
        fSyncm.getChooser()[layer] = new DspfChooser(fSyncm, layer);
        fSyncs.getChooser()[layer] = new DspfChooser(fSyncs, layer);
        fGain.getChooser()[layer] = new DspfChooser(fGain, layer);
        fLopas2.getChooser()[layer] = new DspfChooser(fLopas2, layer);
        fShapemodosc.getChooser()[layer] = new DspfChooser(fShapemodosc, layer);
        
        fA1.getChooser()[layer] = new DspfChooser(fA1, layer);
        fA2.getChooser()[layer] = new DspfChooser(fA2, layer);
        fA3F1.getChooser()[layer] = new DspfChooser(fA3F1, layer);
        fA3F3.getChooser()[layer] = new DspfChooser(fA3F3, layer);
        fA4.getChooser()[layer] = new DspfChooser(fA4, layer);
        fA5.getChooser()[layer] = new DspfChooser(fA5, layer);
        fA6F2.getChooser()[layer] = new DspfChooser(fA6F2, layer);
        fA6F4.getChooser()[layer] = new DspfChooser(fA6F4, layer);
        fA7.getChooser()[layer] = new DspfChooser(fA7, layer);
        fA8F1.getChooser()[layer] = new DspfChooser(fA8F1, layer);
        fA8F2.getChooser()[layer] = new DspfChooser(fA8F2, layer);
        fA9.getChooser()[layer] = new DspfChooser(fA9, layer);
        fA10F1.getChooser()[layer] = new DspfChooser(fA10F1, layer);
        fA10F2.getChooser()[layer] = new DspfChooser(fA10F2, layer);
        fA16.getChooser()[layer] = new DspfChooser(fA16, layer);
        fA17.getChooser()[layer] = new DspfChooser(fA17, layer);
        fA18.getChooser()[layer] = new DspfChooser(fA18, layer);
        fA20.getChooser()[layer] = new DspfChooser(fA20, layer);
        fA33F1.getChooser()[layer] = new DspfChooser(fA33F1, layer);
        fA33F2.getChooser()[layer] = new DspfChooser(fA33F2, layer);
        fA33F3.getChooser()[layer] = new DspfChooser(fA33F3, layer);
        fA34F1.getChooser()[layer] = new DspfChooser(fA34F1, layer);
        fA34F2.getChooser()[layer] = new DspfChooser(fA34F2, layer);
        fA34F3.getChooser()[layer] = new DspfChooser(fA34F3, layer);
        fA34F4.getChooser()[layer] = new DspfChooser(fA34F4, layer);
        fA37F1.getChooser()[layer] = new DspfChooser(fA37F1, layer);
        fA37F2.getChooser()[layer] = new DspfChooser(fA37F2, layer);
        fA40F1.getChooser()[layer] = new DspfChooser(fA40F1, layer);
        fA40F2.getChooser()[layer] = new DspfChooser(fA40F2, layer);
        fA40F3.getChooser()[layer] = new DspfChooser(fA40F3, layer);
        fA40F4.getChooser()[layer] = new DspfChooser(fA40F4, layer);
        fA64F1.getChooser()[layer] = new DspfChooser(fA64F1, layer);
        fA64F2.getChooser()[layer] = new DspfChooser(fA64F2, layer);
        fA64F3.getChooser()[layer] = new DspfChooser(fA64F3, layer);
        fA66F2.getChooser()[layer] = new DspfChooser(fA66F2, layer);
        fA66F3.getChooser()[layer] = new DspfChooser(fA66F3, layer);
        fA66F4.getChooser()[layer] = new DspfChooser(fA66F4, layer);
        fA80.getChooser()[layer] = new DspfChooser(fA80, layer);
        fA81.getChooser()[layer] = new DspfChooser(fA81, layer);
        fA82F1.getChooser()[layer] = new DspfChooser(fA82F1, layer);
        fA82F2.getChooser()[layer] = new DspfChooser(fA82F2, layer);
        fA82F3.getChooser()[layer] = new DspfChooser(fA82F3, layer);
        fA82F4.getChooser()[layer] = new DspfChooser(fA82F4, layer);
        fA104F1.getChooser()[layer] = new DspfChooser(fA104F1, layer);
        fA104F3.getChooser()[layer] = new DspfChooser(fA104F3, layer);
        fA112.getChooser()[layer] = new DspfChooser(fA112, layer);
        fA123.getChooser()[layer] = new DspfChooser(fA123, layer);
        
        calalg[layer] = new LabelledDial("Algorithm", this, prefix + "calalg", color, 1, MAX_NUM_ALGS)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                funcselhbox.removeAll();
                funcselhbox.add(Strut.makeHorizontalStrut(10));
                
                int al = model.get(key) - 1;
                
                for (int fpage = 0; fpage <= 4; fpage++)
                    {
                    if (alg[al].getUsepage(fpage)) 
                        {
                        funcselhbox.add(alg[al].getFunction(fpage).getChooser()[layer]);
                        if (fpage > 0) updateFpage(layer, fpage); // not for pitch
                        }
                    }
                funcselhbox.revalidate();
                funcselhbox.repaint();
                }
            };
        hbox.add(calalg[layer]);
        
        comp = new IconDisplay(null, ALGORITHM_ICONS, this, prefix + "calalg", 623, 72);
        vbox.add(comp);
        vbox.add(funcselhbox);
        
        hbox.add(vbox);
        
        category.add(hbox, BorderLayout.WEST);
        return category;
        }
    
    JComponent addLayer(int layer, Color color)
        {
        Category category = new Category(this, "Layer", color);
        category.makePasteable("");
        
        JComponent comp;
        String prefix = "layer" + layer;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        comp = new LabelledDial("Low Key", this, prefix + "lokey", color, 0, 127)
            { public String map(int val) { return (FONT_SIZE_3 + NOTES[val % 12] + " " + ((val / 12) - 1) + FONT); } };
        hbox.add(comp);
        
        comp = new LabelledDial("High Key", this, prefix + "hikey", color, 0, 127)
            { public String map(int val) { return (FONT_SIZE_3 + NOTES[val % 12] + " " + ((val / 12) - 1) + FONT); } };
        hbox.add(comp);
        
        params = LAYER_VELOCITY;
        comp = new Chooser("Low Velocity", this, prefix + "lovel", params);
        vbox.add(comp);
        
        params = LAYER_VELOCITY;
        comp = new Chooser("High Velocity", this, prefix + "hivel", params);
        vbox.add(comp);
        
        hbox.add(vbox);
        
        vbox = new VBox();
        
        params = LAYER_PBMODE;
        comp = new Chooser("Pitch Bend Mode", this, prefix + "pbmode", params);
        vbox.add(comp);
        
        params = LAYER_TRIGGER;
        comp = new Chooser("Trigger", this, prefix + "trig", params);
        vbox.add(comp);
        
        hbox.add(vbox);
        
        vbox = new VBox();
        
        params = CONTROL_LIST_STR;
        comp = new Chooser("Delay Control", this, prefix + "dlyctl", params);
        vbox.add(comp);
        
        params = CONTROL_LIST_STR_GE;
        comp = new Chooser("Enable", this, prefix + "eswitch", params);
        vbox.add(comp);
        
        hbox.add(vbox);
        
        comp = new LabelledDial("Min Delay", this, prefix + "dlymin", color, 0, 255)
            { public String map(int val) {return (FONT_SIZE_3 + LAYER_DTIMES[val] + "s" + FONT); } };
        hbox.add(comp);
        
        comp = new LabelledDial("Max Delay", this, prefix + "dlymax", color, 0, 255)
            { public String map(int val) { return (FONT_SIZE_3 + LAYER_DTIMES[val] + "s" + FONT); } };
        hbox.add(comp);
        
        vbox = new VBox();
        
        params = LAYER_TRIGGER;
        comp = new Chooser("Enable Sense", this, prefix + "ensense", params);
        vbox.add(comp);
        
        params = LAYER_SUSPDL;
        comp = new Chooser("Sustain Pedal", this, prefix + "suspdl", params);
        vbox.add(comp);
        
        hbox.add(vbox);
        
        comp = new LabelledDial("Enable Min", this, prefix + "loenable", color, -128, 127)
            { public String map(int val) {return (FONT_SIZE_3 + val + FONT); } };
        hbox.add(comp);
        
        comp = new LabelledDial("Enable Max", this, prefix + "hienable", color, -128, 127)
            { public String map(int val) {return (FONT_SIZE_3 + val + FONT); } };
        hbox.add(comp);
        
        vbox = new VBox();
        
        comp = new CheckBox("Opaque", this, prefix + "opaque");
        vbox.add(comp);
        
        comp = new CheckBox("Sostenuto Pedal", this, prefix + "sospdl");
        vbox.add(comp);
        
        comp = new CheckBox("Freeze Pedal", this, prefix + "frzpdl");
        vbox.add(comp);
        
        hbox.add(vbox);
        
        vbox = new VBox();
        
        comp = new CheckBox("Ignore Release", this, prefix + "ignrel");
        vbox.add(comp);
        
        comp = new CheckBox("Hold Through Attack", this, prefix + "thratt");
        ((CheckBox)comp).addToWidth(1);
        vbox.add(comp);
        
        comp = new CheckBox("Hold Until Sustain", this, prefix + "tildec");
        vbox.add(comp);
        
        hbox.add(vbox);
        
        comp = new Chooser("", this, prefix + "calalg", new String[] {""})
            {
            public void update(String key, Model model)
                { // no layer block for T2 and T3 in triple
                category.removeAll();
                if (model.get(key) >= ALG_LYR_T2) category.add(Strut.makeVerticalStrut(hbox.getPreferredSize().height), BorderLayout.WEST);
                else category.add(hbox, BorderLayout.WEST);
                }
            };
        return category;
        }
    
    JComponent addKeymap(int layer, Color color)
        {
        Category category = new Category(this, "Keymap", color);
        category.makePasteable("");
        
        JComponent comp;
        String prefix = "layer" + layer;
        String[] params;
        HBox hbox = new HBox();
        HBox stereodial = new HBox();
        VBox vbox = new VBox();
        VBox name2 = new VBox();
        VBox stereobox = new VBox();
        
        comp = new LabelledDial("Keymap", this, prefix + "keymap", color, 0, 999)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (layer == selLayer) getObjectname(KEYMAP_TYPE, model.get(key)); // just for the layer that is displayed
                }
            public String map(int val) {return (FONT_SIZE_3 + val + FONT); } 
            };
        hbox.add(comp);
        
        LabelledDial keymap2 = new LabelledDial("Keymap 2", this, prefix + "keymap2", color, 0, 999)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (layer == selLayer) getObjectname(KEYMAP_TYPE, model.get(key)); // just for the layer that is displayed
                }
            public String map(int val) {return (FONT_SIZE_3 + val + FONT); } 
            };
        stereodial.add(keymap2);
        
        hbox.add(stereodial);
        
        comp = new ReadOnlyString("Keymap", this, prefix + "keymapname", MAXIMUM_NAME_LENGTH);
        model.setStatus(prefix + "keymapname", Model.STATUS_IMMUTABLE);
        vbox.add(comp);
        
        ReadOnlyString keymapname2 = new ReadOnlyString("Keymap2", this, prefix + "keymapname2", MAXIMUM_NAME_LENGTH);
        model.setStatus(prefix + "keymapname2", Model.STATUS_IMMUTABLE);
        name2.add(keymapname2);
        vbox.add(name2);
        
        hbox.add(vbox);
        
        comp = new LabelledDial("Transpose", this, prefix + "transpose", color, -128, 127)
            { public String map(int val) {return (FONT_SIZE_3 + val + "ST" + FONT); } };
        hbox.add(comp);
        
        comp = new LabelledDial("Timbre Shift", this, prefix + "caltshift", color, -60, 60)
            { public String map(int val) {return (FONT_SIZE_3 + val + "ST" + FONT); } };
        hbox.add(comp);
        
        comp = new BipolarMappedDial("Key Track", prefix + "caltkscale", color, 120, KEY_TRACK, "ct");
        hbox.add(comp);
        
        comp = new BipolarMappedDial("Vel Track", prefix + "caltvscale", color, 123, CENT_VALUES, "ct");
        hbox.add(comp);
        
        vbox = new VBox();
        
        params = KEYMAP_SAMSKIP;
        comp = new Chooser("Sample Skipping", this, prefix + "sampleskipping", params);
        vbox.add(comp);
        
        params = KEYMAP_PLAYMODE;
        comp = new Chooser("Playback Mode", this, prefix + "playbackmode", params);
        vbox.add(comp);
        
        hbox.add(vbox);
        
        vbox = new VBox();
        
        params = CONTROL_LIST_STR;
        comp = new Chooser("Alt Control", this, prefix + "altctrl", params);
        vbox.add(comp);
        
        params = KEYMAP_ALT_METHOD;
        comp = new Chooser("Alt Method", this, prefix + "altmethod", params);
        vbox.add(comp);
        
        hbox.add(vbox);
        
        CheckBox stereoflag = new CheckBox("Stereo", this, prefix + "stereoflag")
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                stereodial.removeAll();
                name2.removeAll();
                if (model.get(key) == 0)
                    {
                    stereodial.add(Strut.makeHorizontalStrut(keymap2.getPreferredSize().width));
                    name2.add(Strut.makeVerticalStrut(keymapname2.getPreferredSize().height));
                    }
                else 
                    {
                    stereodial.add(keymap2);
                    name2.add(keymapname2);
                    }
                stereodial.revalidate();
                stereodial.repaint();
                }
            };
        
        hbox.add(stereobox);
        
        comp = new Chooser("", this, prefix + "calalg", new String[] {""})
            {
            public void update(String key, Model model)
                {
                stereobox.removeAll();
                if (model.get(key) <= NUM_ALGS_1_LYR) stereobox.add(stereoflag);
                }
            };
        
        category.add(hbox, BorderLayout.WEST);
        return category;
        }
    
    JComponent addPitch(int layer, Color color)
        {
        Category category = new Category(this, "Pitch", color);
        category.makePasteable("");
        
        JComponent comp;
        String prefix = "layer" + layer;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        comp = new LabelledDial("Coarse", this, prefix + "calcpitch", color, -128 , 127)
            { public String map(int val) {return (FONT_SIZE_3 + val + "ST" + FONT); } };
        hbox.add(comp);
        
        comp = new LabelledDial("Fine", this, prefix + "calfpitch", color, -100, 100)
            { public String map(int val) {return (FONT_SIZE_3 + val + "ct" + FONT); } };
        hbox.add(comp);
        
        comp = new LabelledDial("Fine Hz", this, prefix + "calfinehz", color, -60, 60)
            { public String map(int val) { return (FONT_SIZE_3 + String.valueOf((float)val / 10) + FONT); } };
        hbox.add(comp);
        
        comp = new BipolarMappedDial("Key Track", prefix + "calckscale", color, 120, KEY_TRACK, "ct");
        hbox.add(comp);
        
        comp = new BipolarMappedDial("Vel Track", prefix + "calcvscale", color, 123, CENT_VALUES, "ct");
        hbox.add(comp);
        
        hbox.add(vbox);
        
        vbox = new VBox();
        
        params = CONTROL_LIST_STR;
        comp = new Chooser("Source 1", this, prefix + "calpcontrol", params);
        vbox.add(comp);
        
        hbox.add(vbox);
        
        comp = new BipolarMappedDial("Depth", prefix + "calprange", color, 123, CENT_VALUES, "ct");
        hbox.add(comp);
        
        vbox = new VBox();
        
        params = CONTROL_LIST_STR;
        comp = new Chooser("Source 2", this, prefix + "calpsource", params);
        vbox.add(comp);
        
        params = CONTROL_LIST_STR;
        comp = new Chooser("Depth Control", this, prefix + "calpdepth", params);
        vbox.add(comp);
        
        hbox.add(vbox);
        
        comp = new BipolarMappedDial("Min Depth", prefix + "calpmin", color, 123, CENT_VALUES, "ct");
        hbox.add(comp);
        
        comp = new BipolarMappedDial("Max Depth", prefix + "calpmax", color, 123, CENT_VALUES, "ct");
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.WEST);
        return category;
        }
    
    JComponent addFpage(int page, int layer, Color color)
        {
        Category category = new Category(this, "F" + (page + 1), color);
        category.makePasteable("");
        
        JComponent comp;
        String prefix = "layer" + layer + "f" + (page + 1);
        HBox hbox = new HBox();
        HBox col1 = new HBox();
        HBox col2 = new HBox();
        HBox col3 = new HBox();
        HBox col4 = new HBox();
        VBox vbox = new VBox();
        
        final LabelledDial coarse = new LabelledDial("Coarse", this, prefix + "coarse", color, -48 , 79)
            { public String map(int val) { return (FONT_SIZE_3 + NOTES[(val + 48) % 12] + " " + ((val + 48) / 12) + FONT); } };
            
        final LabelledDial adjust = new LabelledDial("Adjust", this, prefix + "coarseadj", color, -96 , 48)
            { public String map(int val) {return (FONT_SIZE_3 + val + "dB" + FONT); } };
        
        final LabelledDial adjustres = new LabelledDial("Adjust", this, prefix + "coarseres", color, -24 , 96)
            { public String map(int val) { return (FONT_SIZE_3 + ((float)val / 2) + "dB" + FONT); } };
        
        final LabelledDial adjustamt = new LabelledDial("Adjust", this, prefix + "coarseamt", color, 4 , 160)
            { public String map(int val) { return (FONT_SIZE_3 + String.format("%.03f", (float)val / 40) + "x" + FONT); } };
        
        final LabelledDial adjustwid = new LabelledDial("Adjust", this, prefix + "coarsewid", color, 0 , 150)
            { public String map(int val) { return (FONT_SIZE_3 + WIDTH_AMT[val] + "oct" + FONT); } };
        
        final LabelledDial adjustpct = new LabelledDial("Adjust", this, prefix + "coarsepct", color, -100, 100)
            { public String map(int val) {return (FONT_SIZE_3 + val + "%" + FONT); } };
        
        final LabelledDial adjustpwm = new LabelledDial("Adjust", this, prefix + "coarsepwm", color, 0, 100)
            { public String map(int val) {return (FONT_SIZE_3 + val + "%" + FONT); } };
        
        final LabelledDial adjustwrap = new LabelledDial("Adjust", this, prefix + "coarsewrap", color, -128 , 48)
            { public String map(int val) { return (FONT_SIZE_3 + ((float)val / 4) + "dB" + FONT); } };
        
        final LabelledDial coarsehz = new LabelledDial("Coarse Hz", this, prefix + "coarse", color, -48 , 79)
            { public String map(int val) { return (FONT_SIZE_3 + COARSE_HZ[val + 60] + FONT); } };
        
        final LabelledDial coarsect = new LabelledDial("Coarse", this, prefix + "coarsect", color, -108 , 108)
            { public String map(int val) { return (FONT_SIZE_3 + (val * 100) + "ct" + FONT); } };
        
        final LabelledDial coarsepch = new LabelledDial("Coarse", this, prefix + "coarsepch", color, -120 , 60)
            { public String map(int val) {return (FONT_SIZE_3 + val + "ST" + FONT); } };
        
        final LabelledDial coarsepchlf = new LabelledDial("Coarse", this, prefix + "coarsepchlf", color, 0 , 4)
            { public String map(int val) { return (FONT_SIZE_3 + COARSE_LF[val] + "Hz" + FONT); } };
        
        final LabelledDial fine = new LabelledDial("Fine", this, prefix + "fine", color, -100, 100)
            { public String map(int val) {return (FONT_SIZE_3 + val + "ct" + FONT); } };
        
        final LabelledDial finehz = new LabelledDial("Fine Hz", this, prefix + "outputlsbfh", color, -60, 60)
            { public String map(int val) { return (FONT_SIZE_3 + String.valueOf((float)val / 10) + FONT); } };
        
        final LabelledDial finepchlf = new LabelledDial("Fine", this, prefix + "finepchlf", color, 0, 255)
            { public String map(int val) { return (FONT_SIZE_3 + FINE_LF[val] + "x" + FONT); } };
        
        final LabelledDial kstart = new LabelledDial("", this, prefix + "outputlsbks", color, -120, 120)
            {
            public String map(int val)
                {
                if (val < 0)
                    {
                    setLabel("Bipolar");
                    return (FONT_SIZE_3 + NOTES[(120 + val) % 12] + " " + ((108 + val) / 12) + FONT);
                    }
                else 
                    {
                    setLabel("Unipolar");
                    return (FONT_SIZE_3 + NOTES[(val) % 12] + " " + ((val / 12) - 1) + FONT);
                    }
                }
            };
        kstart.addAdditionalLabel("Key Start");
        
        final LabelledDial keytrackct = new LabelledDial("Key Track", this, prefix + "kscalect", color, -125, 125)
            { public String map(int val) { return (FONT_SIZE_3 + (val * 2) + "ct" + FONT); } };
        
        final LabelledDial keytrackdb = new LabelledDial("Key Track", this, prefix + "kscaledb", color, -100, 100)
            { public String map(int val) { return (FONT_SIZE_3 + ((float)val / 50) + "dB" + FONT); } };
        
        final LabelledDial keytrackamt = new LabelledDial("Key Track", this, prefix + "kscaleamt", color, -100, 100)
            { public String map(int val) { return (FONT_SIZE_3 + ((float)val / 500) + "x" + FONT); } };
        
        final LabelledDial keytrackwid = new LabelledDial("Key Track", this, prefix + "kscalewid", color, -100, 100)
            { public String map(int val) { return (FONT_SIZE_3 + ((float)val / 500) + "oct" + FONT);  } };
        
        final LabelledDial keytrackpct = new LabelledDial("Key Track", this, prefix + "kscalepct", color, -80, 80)
            { public String map(int val) { return (FONT_SIZE_3 + ((float)val / 5) + "%" + FONT); } };
        
        final BipolarMappedDial keytrackpch = new BipolarMappedDial("Key Track", prefix + "kscalepch", color, 120, KEY_TRACK, "ct");
        
        final LabelledDial keytrackpwm = new LabelledDial("Key Track", this, prefix + "kscalepwm", color, -80, 80)
            { public String map(int val) { return (FONT_SIZE_3 + ((float)val / 10) + "%" + FONT); } };
        
        final LabelledDial keytrackpchlf = new LabelledDial("Key Track", this, prefix + "kscalepchlf", color, 0 , 180)
            { public String map(int val) { return (FONT_SIZE_3 + KEY_TRACK_LF[val] + "x" + FONT); } };
        
        final BipolarMappedDial veltrackct = new BipolarMappedDial("Vel Track", prefix + "vscalect", color, 127, VEL_TRACK, "ct");
        
        final BipolarMappedDial veltrackpch = new BipolarMappedDial("Vel Track", prefix + "vscalepch", color, 123, CENT_VALUES, "ct");
        
        final LabelledDial veltrackdb = new LabelledDial("Vel Track", this, prefix + "vscaledb", color, -96, 96)
            { public String map(int val) { return (FONT_SIZE_3 + val + "dB" + FONT); } };
        
        final LabelledDial veltrackdbres = new LabelledDial("Vel Track", this, prefix + "vscaleres", color, -60, 120)
            { public String map(int val) { return (FONT_SIZE_3 + ((float)val / 2) + "dB" + FONT); } };
        veltrackdbres.addAdditionalLabel("dB");
        
        final LabelledDial veltrackamt = new LabelledDial("Vel Track", this, prefix + "vscaleamt", color, -80, 80)
            { public String map(int val) { return (FONT_SIZE_3 + String.format("%.02f", (float)val / 20) + "x" + FONT); } };
        
        final BipolarMappedDial veltrackwid = new BipolarMappedDial("Vel Track", prefix + "vscalewid", color, 105, WIDTH_VEL, "oct");
        
        final LabelledDial veltrackpct = new LabelledDial("Vel Track", this, prefix + "vscalepct", color, -100, 100)
            { public String map(int val) { return (FONT_SIZE_3 + (val * 2) + "%" + FONT); } };
        
        final LabelledDial veltrackpwm = new LabelledDial("Vel Track", this, prefix + "vscalepwm", color, -100, 100)
            { public String map(int val) { return (FONT_SIZE_3 + val + "%" + FONT); } };
        
        final LabelledDial veltrackwrap = new LabelledDial("Vel Track", this, prefix + "vscalewrap", color, -96, 96)
            { public String map(int val) { return (FONT_SIZE_3 + ((float)val / 2) + "dB" + FONT); } };
        
        final LabelledDial veltrackpchlf = new LabelledDial("Vel Track", this, prefix + "vscalepchlf", color, 0, 220)
            { public String map(int val) { return (FONT_SIZE_3 + VEL_TRACK_LF[val] + "x" + FONT); }};
        
        final BipolarMappedDial depthct = new BipolarMappedDial("Depth", prefix + "rangect", color, 127 , VEL_TRACK, "ct");
        
        final LabelledDial depthdb = new LabelledDial("Depth", this, prefix + "rangedb", color, -96 , 96)
            { public String map(int val) { return (FONT_SIZE_3 + val + "dB" + FONT); } };
        
        final LabelledDial depthdbres = new LabelledDial("Depth", this, prefix + "rangeres", color, -60 , 120)
            { public String map(int val) { return (FONT_SIZE_3 + ((float)val / 2) + "dB" + FONT); } };
        
        final LabelledDial depthamt = new LabelledDial("Depth", this, prefix + "rangeamt", color, -80 , 80)
            { public String map(int val) { return (FONT_SIZE_3 + String.format("%.02f", (float)val / 20) + "x" + FONT); } };
        
        final BipolarMappedDial depthwid = new BipolarMappedDial("Depth", prefix + "rangewid", color, 105, WIDTH_VEL, "oct");
        
        final LabelledDial depthpct = new LabelledDial("Depth", this, prefix + "rangepct", color, -100, 100)
            { public String map(int val) { return (FONT_SIZE_3 + (val * 2) + "%" + FONT); } };
        
        final BipolarMappedDial depthpch = new BipolarMappedDial("Depth", prefix + "rangepch", color, 123, CENT_VALUES, "ct");
        
        final LabelledDial depthpwm = new LabelledDial("Depth", this, prefix + "rangepwm", color, -100, 100)
            { public String map(int val) { return (FONT_SIZE_3 + val + "%" + FONT); } };
        
        final LabelledDial depthwrap = new LabelledDial("Depth", this, prefix + "rangewrap", color, -96 , 96)
            { public String map(int val) { return (FONT_SIZE_3 + ((float)val / 2) + "dB" + FONT); } };
        
        final LabelledDial depthpchlf = new LabelledDial("Depth", this, prefix + "rangepchlf", color, 0 , 220)
            { public String map(int val) { return (FONT_SIZE_3 + VEL_TRACK_LF[val] + "x" + FONT); } };
        
        final BipolarMappedDial mindepthct = new BipolarMappedDial("Min Depth", prefix + "mindepthct", color, 127 , VEL_TRACK, "ct");
        
        final LabelledDial mindepthdb = new LabelledDial("Min Depth", this, prefix + "mindepthdb", color, -96 , 96)
            { public String map(int val) { return (FONT_SIZE_3 + val + "dB" + FONT); } };
        
        final LabelledDial mindepthdbres = new LabelledDial("Min Depth", this, prefix + "mindepthres", color, -60 , 120)
            { public String map(int val) { return (FONT_SIZE_3 + ((float)val / 2) + "dB" + FONT); } };
        
        final LabelledDial mindepthamt = new LabelledDial("Min Depth", this, prefix + "mindepthamt", color, -80 , 80)
            { public String map(int val) { return (FONT_SIZE_3 + String.format("%.02f", (float)val / 20) + "x" + FONT); } };
        
        final BipolarMappedDial mindepthwid = new BipolarMappedDial("Min Depth", prefix + "mindepthwid", color, 105, WIDTH_VEL, "oct");
        
        final LabelledDial mindepthpct = new LabelledDial("Min Depth", this, prefix + "mindepthpct", color, -100, 100)
            { public String map(int val) { return (FONT_SIZE_3 + (val * 2) + "%" + FONT); } };
        
        final BipolarMappedDial mindepthpch = new BipolarMappedDial("Min Depth", prefix + "mindepthpch", color, 123, CENT_VALUES, "ct");
        
        final LabelledDial mindepthpwm = new LabelledDial("Min Depth", this, prefix + "mindepthpwm", color, -100, 100)
            { public String map(int val) { return (FONT_SIZE_3 + val + "%" + FONT); } };
        
        final LabelledDial mindepthwrap = new LabelledDial("Min Depth", this, prefix + "mindepthwrap", color, -96 , 96)
            { public String map(int val) { return (FONT_SIZE_3 + ((float)val / 2) + "dB" + FONT); } };
        
        final LabelledDial mindepthpchlf = new LabelledDial("Min Depth", this, prefix + "mindepthpchlf", color, 0 , 220)
            { public String map(int val) { return (FONT_SIZE_3 + VEL_TRACK_LF[val] + "x" + FONT); } };
        
        final Chooser cpad = new Chooser("Pad", this, prefix + "tsrcmsb", PAD);
        
        final BipolarMappedDial maxdepthct = new BipolarMappedDial("Max Depth", prefix + "maxdepthct", color, 127 , VEL_TRACK, "ct");
        
        final LabelledDial maxdepthdb = new LabelledDial("Max Depth", this, prefix + "maxdepthdb", color, -96 , 96)
            { public String map(int val) { return (FONT_SIZE_3 + val + "dB" + FONT); } };
        
        final LabelledDial maxdepthdbres = new LabelledDial("Max Depth", this, prefix + "maxdepthres", color, -60 , 120)
            { public String map(int val) { return (FONT_SIZE_3 + ((float)val / 2) + "dB" + FONT); } };
        
        final LabelledDial maxdepthamt = new LabelledDial("Max Depth", this, prefix + "maxdepthamt", color, -80 , 80)
            { public String map(int val) { return (FONT_SIZE_3 + String.format("%.02f", (float)val / 20) + "x" + FONT); } };
        
        final BipolarMappedDial maxdepthwid = new BipolarMappedDial("Max Depth", prefix + "maxdepthwid", color, 105, WIDTH_VEL, "oct");
        
        final LabelledDial maxdepthpct = new LabelledDial("Max Depth", this, prefix + "maxdepthpct", color, -100, 100)
            { public String map(int val) { return (FONT_SIZE_3 + (val * 2) + "%" + FONT); } };
        
        final BipolarMappedDial maxdepthpch = new BipolarMappedDial("Max Depth", prefix + "maxdepthpch", color, 123, CENT_VALUES, "ct");
        
        final LabelledDial maxdepthwrap = new LabelledDial("Max Depth", this, prefix + "maxdepthwrap", color, -96 , 96)
            { public String map(int val) { return (FONT_SIZE_3 + ((float)val / 2) + "dB" + FONT); } };
        
        final LabelledDial maxdepthpchlf = new LabelledDial("Min Depth", this, prefix + "maxdepthpchlf", color, 0 , 220)
            { public String map(int val) { return (FONT_SIZE_3 + VEL_TRACK_LF[val] + "x" + FONT); } };
        
        final LabelledDial maxdepthpwm = new LabelledDial("Max Depth", this, prefix + "maxdepthpwm", color, -100, 100)
            { public String map(int val) { return (FONT_SIZE_3 + val + "%" + FONT); } };
        
        funcdummy[page][layer] = new Chooser("", this, "", new String[] {""})
            { // Precondition : function block type set
            public void update(String key, Model model)
                {
                FB_ENUM type = functionBlock.getType(page);
                boolean pad = functionBlock.getPad(page, layer);
                
                col1.removeAll();
                col2.removeAll();
                col3.removeAll();
                col4.removeAll();
                
                if (pad == true) col2.add(cpad);
                
                category.setName("F" + (page + 1) + " " + functionBlock.getDesc(type.getValue()));
                
                switch (type)
                    {
                    case OFF:
                    case NON:
                    default:
                        col1.add(Strut.makeStrut(adjust.getPreferredSize().width * 5, adjust.getPreferredSize().height));
                        col3.add(Strut.makeStrut(adjust.getPreferredSize().width, adjust.getPreferredSize().height));
                        break;
                    case AMP:
                    case DRV:
                    case EVN:
                    case ODD:
                    case DEP:
                        col1.add(adjust);
                        col1.add(Strut.makeHorizontalStrut(coarse.getPreferredSize().width));
                        if (type == FB_ENUM.AMP) col1.add(Strut.makeHorizontalStrut(coarse.getPreferredSize().width));
                        else col1.add(kstart);
                        col1.add(keytrackdb);
                        col1.add(veltrackdb);
                        col3.add(depthdb);
                        col4.add(mindepthdb);
                        col4.add(maxdepthdb);
                        break;
                    case FRQ:
                        col1.add(coarse);
                        col1.add(coarsehz);
                        col1.add(fine);
                        col1.add(keytrackct);
                        col1.add(veltrackct);
                        col3.add(depthct);
                        col4.add(mindepthct);
                        col4.add(maxdepthct);
                        break;
                    case RES:
                        col1.add(adjustres);
                        col1.add(Strut.makeHorizontalStrut(coarse.getPreferredSize().width));
                        col1.add(Strut.makeHorizontalStrut(coarse.getPreferredSize().width));
                        col1.add(keytrackdb);
                        col1.add(veltrackdbres);
                        col3.add(depthdbres);
                        col4.add(mindepthdbres);
                        col4.add(maxdepthdbres);
                        break;
                    case AMT:
                        col1.add(adjustamt);
                        col1.add(Strut.makeHorizontalStrut(coarse.getPreferredSize().width));
                        col1.add(kstart);
                        col1.add(keytrackamt);
                        col1.add(veltrackamt);
                        col3.add(depthamt);
                        col4.add(mindepthamt);
                        col4.add(maxdepthamt);
                        break;
                    case WID:
                        col1.add(adjustwid);
                        col1.add(Strut.makeHorizontalStrut(coarse.getPreferredSize().width));
                        col1.add(Strut.makeHorizontalStrut(coarse.getPreferredSize().width));
                        col1.add(keytrackwid);
                        col1.add(veltrackwid);
                        col3.add(depthwid);
                        col4.add(mindepthwid);
                        col4.add(maxdepthwid);
                        break;
                    case PCH:
                        col1.add(coarsepch);
                        col1.add(fine);
                        col1.add(finehz);
                        col1.add(keytrackpch);
                        col1.add(veltrackpch);
                        col3.add(depthpch);
                        col4.add(mindepthpch);
                        col4.add(maxdepthpch);
                        break;
                    case WRP:
                        col1.add(adjustwrap);
                        col1.add(Strut.makeHorizontalStrut(coarse.getPreferredSize().width));
                        col1.add(kstart);
                        col1.add(keytrackdb);
                        col1.add(veltrackwrap);
                        col3.add(depthwrap);
                        col4.add(mindepthwrap);
                        col4.add(maxdepthwrap);
                        break;
                    case POS:
                    case XFD:
                        col1.add(adjustpct);
                        col1.add(Strut.makeHorizontalStrut(coarse.getPreferredSize().width));
                        col1.add(Strut.makeHorizontalStrut(coarse.getPreferredSize().width));
                        col1.add(keytrackpct);
                        col1.add(veltrackpct);
                        col3.add(depthpct);
                        col4.add(mindepthpct);
                        col4.add(maxdepthpct);
                        break;
                    case SEP:
                        col1.add(coarsect);
                        col1.add(Strut.makeHorizontalStrut(coarse.getPreferredSize().width));
                        col1.add(fine);
                        col1.add(keytrackct);
                        col1.add(veltrackct);
                        col3.add(depthct);
                        col4.add(mindepthct);
                        col4.add(maxdepthct);
                        break;
                    case WPW :
                        col1.add(adjustpwm);
                        col1.add(Strut.makeHorizontalStrut(coarse.getPreferredSize().width));
                        col1.add(Strut.makeHorizontalStrut(coarse.getPreferredSize().width));
                        col1.add(keytrackpwm);
                        col1.add(veltrackpwm);
                        col3.add(depthpwm);
                        col4.add(mindepthpwm);
                        col4.add(maxdepthpwm);
                        break;
                    case PLF:
                        col1.add(coarsepchlf);
                        col1.add(finepchlf);
                        col1.add(Strut.makeHorizontalStrut(coarse.getPreferredSize().width));
                        col1.add(keytrackpchlf);
                        col1.add(veltrackpchlf);
                        col3.add(depthpchlf);
                        col4.add(mindepthpchlf);
                        col4.add(maxdepthpchlf);
                        break;
                    }
                col1.revalidate();
                col2.revalidate();
                col3.revalidate();
                col4.revalidate();
                }
            };
        
        hbox.add(col1);
        
        comp = new Chooser("Source 1", this, prefix + "control", CONTROL_LIST_STR);
        vbox.add(comp);
        vbox.add(col2);
        
        hbox.add(vbox);
        
        hbox.add(col3);
        
        vbox = new VBox();
        
        comp = new Chooser("Source 2", this, prefix + "source", CONTROL_LIST_STR);
        vbox.add(comp);
        
        comp = new Chooser("Depth Control", this, prefix + "depth", CONTROL_LIST_STR);
        vbox.add(comp);
        
        hbox.add(vbox);
        
        hbox.add(col4);
        
        category.add(hbox, BorderLayout.WEST);
        return category;
        }
      
    JComponent addOutput(int layer, Color color)
        {
        Category category = new Category(this, "Output", color);
        category.makePasteable("");
        
        JComponent comp;
        String[] params;
        String prefix = "layer" + layer;
        HBox hbox = new HBox();
        HBox hbox2 = new HBox();
        HBox hboxl = new HBox();
        VBox vbox = new VBox();
        VBox vboxl = new VBox();
        VBox colpu = new VBox();
        VBox colpl = new VBox();
        VBox vbox2 = new VBox();
        
        final Chooser pair = new Chooser("Pair", this, prefix + "pairu", OUTPUT_PAIR)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                outCom.updateCommon();
                }
            };
        vbox2.add(pair);
        
        final Chooser mode = new Chooser("Mode", this, prefix + "modeu", OUTPUT_MODE);
        vbox2.add(mode);
        
        final Chooser gain = new Chooser("Gain", this, prefix + "gainu", OUTPUT_GAIN);
        vbox2.add(gain);
        
        hbox2.add(vbox2);
        
        final LabelledDial pan1 = new LabelledDial("Pan", this, prefix + "panu", color, -7 , +7)
            { public String map(int val) { return (FONT_SIZE_3 + val + FONT); } };
        hbox2.add(pan1);
        
        final LabelledDial pan2 = new LabelledDial("Pan2", this, prefix + "pan2u", color, -7 , +7)
            { public String map(int val) { return (FONT_SIZE_3 + val + FONT); } };// TODO : param
        
        comp = new CheckBox("", this, prefix + "stereoflag")
            {
            public void update(String key, Model model)
                {
                colpu.removeAll();
                if (model.get(key) == 0) colpu.add(Strut.makeHorizontalStrut(pan2.getPreferredSize().width));
                else colpu.add(pan2);
                }
            };
        hbox2.add(colpu);
        
        vbox2 = new VBox();
        
        comp = new Chooser("Pair L", this, prefix + "pairl", OUTPUT_PAIR)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                outCom.updateCommon();
                }
            };
        vbox2.add(comp);
        
        comp = new Chooser("Mode L", this, prefix + "model", OUTPUT_MODE);
        vbox2.add(comp);
        
        comp = new Chooser("GAIN L", this, prefix + "gainl", OUTPUT_GAIN);
        vbox2.add(comp);
        
        hboxl.add(vbox2);
        
        comp = new LabelledDial("Pan L ", this, prefix + "panl", color, -7 , +7)
            { public String map(int val) { return (FONT_SIZE_3 + val + FONT); } };
        hboxl.add(comp);
        
        final LabelledDial pan2l = new LabelledDial("Pan2 L", this, prefix + "pan2l", color, -7 , +7)
            { public String map(int val) { return (FONT_SIZE_3 + val + FONT); } };// TODO : param
        
        comp = new CheckBox("", this, prefix + "stereoflag")
            {
            public void update(String key, Model model)
                {
                colpl.removeAll();
                if (model.get(key) == 0) colpl.add(Strut.makeHorizontalStrut(pan2.getPreferredSize().width));
                else colpl.add(pan2l);
                }
            };
        hboxl.add(colpl);
        
        comp = new Chooser("", this, prefix + "calalg", new String[] {""})
            {
            public void update(String key, Model model)
                {
                vboxl.removeAll();
                pan1.setLabel("Pan");
                pan2.setLabel("Pan2");
                pair.setLabel("Pair");
                mode.setLabel("Mode");
                gain.setLabel("Gain");
                int al = model.get(key) - 1;
                for (int fpage = 1; fpage <= 4; fpage++)
                    {
                    if (alg[al].getUsepage(fpage))
                        { // check for double output algorithm, panner, bal amp and algorithms 123-126
                        if ( (alg[al].getFunction(fpage) == fPan) || (alg[al].getFunction(fpage) == fA3F3) || (al > 121) ) 
                            {
                            vboxl.add(hboxl);
                            pan1.setLabel("Pan U");
                            pan2.setLabel("Pan2 U");
                            pair.setLabel("Pair U");
                            mode.setLabel("Mode U");
                            gain.setLabel("Gain U");
                            }
                        }
                    }
                outCom.updateCommon();
                }
            };
        
        hbox2.add(vboxl);
        
        vbox.add(hbox2);
        
        hbox2 = new HBox();
        
        comp = new Chooser("Crossfade", this, prefix + "xfade", CONTROL_LIST_STR);
        hbox2.add(comp);
        
        comp = new Chooser("XFade Sense", this, prefix + "xfsense", LAYER_TRIGGER);
        hbox2.add(comp);
        
        vbox.add(hbox2);
        
        hbox.add(vbox);
        
        comp = new Chooser("", this, prefix + "calalg", new String[] {""})
            {
            public void update(String key, Model model)
                { // no output block for T1 and T2 in triple
                category.removeAll();
                if ( (model.get(key) >= ALG_LYR_T1) && (model.get(key) < ALG_LYR_T3) )
                    category.add(Strut.makeVerticalStrut(hbox.getPreferredSize().height), BorderLayout.WEST);
                else category.add(hbox, BorderLayout.WEST);
                }
            };
        
        category.add(hbox, BorderLayout.WEST);
        return category;
        }
    
    JComponent addAmpenv(int layer, Color color)
        {
        Category category = new Category(this, "Amplitude Envelope", color);
        category.makePasteable("");
        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        VBox vdial = new VBox();
        VBox vswitch = new VBox();
        VBox vdisp = new VBox();
        HBox times = new HBox();
        HBox levels = new HBox();
        
        String prefix = "layer" + layer + "env1";
        
        LabelledDial atttime = new LabelledDial("Attack 1", this, prefix + "att1time", color, 0 , 255)
            { public String map(int val) {return (FONT_SIZE_3 + ENV_TIMES[val] + "s" + FONT);} };
        atttime.addAdditionalLabel("Time");
        times.add(atttime);
        
        comp = new EnvTimesDial("Attack 2", prefix + "att2time", color, 255);
        times.add(comp);
        
        comp = new EnvTimesDial("Attack 3", prefix + "att3time", color, 255);
        times.add(comp);
        
        comp = new EnvTimesDial("Decay", prefix + "dec1time", color, 255);
        times.add(comp);
        
        comp = new EnvTimesDial("Release 1", prefix + "rel1time", color, 255);
        times.add(comp);
        
        comp = new EnvTimesDial("Release 2", prefix + "rel2time", color, 255);
        times.add(comp);
        
        comp = new EnvTimesDial("Release 3", prefix + "rel3time", color, 255);
        times.add(comp);
        
        comp = new EnvLevelsDial("Attack 1", prefix + "att1", color, 0, 100);
        levels.add(comp);
        
        comp = new EnvLevelsDial("Attack 2", prefix + "att2", color, 0, 100);
        levels.add(comp);
        
        comp = new EnvLevelsDial("Attack 3", prefix + "att3", color, 0, 100);
        levels.add(comp);
        
        comp = new EnvLevelsDial("Decay", prefix + "dec1", color, 0, 100);
        levels.add(comp);
        
        comp = new EnvLevelsDial("Release 1", prefix + "rel1", color, 0, 150);
        levels.add(comp);
        
        comp = new EnvLevelsDial("Release 2", prefix + "rel2", color, 0, 150);
        levels.add(comp);
        
        params = ENV_LOOP_TYPE;
        Chooser looptype = new Chooser("Loop Type", this, prefix + "looptype", params)
            { 
            public void update(String key, Model model)
                {
                super.update(key, model);
                int step = model.get(key);
                if (step > 3) step -= 3;
                if (step == 0) step = 5;
                model.set(prefix + "loopback", step - 1); model.setStatus(prefix + "loopback", Model.STATUS_IMMUTABLE);
                } 
            };
        
        params = ENV_LOOP_TIMES;
        Chooser numloops = new Chooser("# Loops", this, prefix + "numloops", params);
        
        EnvelopeDisplay envdisp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, prefix + "att1time", prefix + "att2time", prefix + "att3time",  
                           prefix + "dec1time", null,
                           prefix + "rel1time", prefix + "rel2time", prefix + "rel3time" },
            new String[] { null, prefix + "att1", prefix + "att2", prefix + "att3", 
                           prefix + "dec1", prefix + "dec1",
                           prefix + "rel1", prefix + "rel2", null },
            new double[] { 0.0, 0.133/252.0,  0.133/252.0,  0.133/252.0,  
                           0.133/252.0, 0.069,
                           0.133/252.0, 0.133/252.0, 0.133/252.0},
            new double[] { 0.0, 1.0/100.0, 1.0/100.0, 1.0/100.0, 
                           1.0/100.0, 1.0/100.0,
                           1.0/100.0, 1.0/100.0, 0.0});
        model.set(prefix + "susstakey", 4); model.setStatus(prefix + "susstakey", Model.STATUS_IMMUTABLE);
        model.set(prefix + "finstakey", 5); model.setStatus(prefix + "finstakey", Model.STATUS_IMMUTABLE);
        envdisp.setSustainStageKey(prefix + "susstakey");
        envdisp.setFinalStageKey(prefix + "finstakey");
        envdisp.setLoopKeys(0, prefix + "loopback", prefix + "susstakey");
        envdisp.setPreferredHeight(atttime.getPreferredSize().height * 2);
            
        params = ENV_MODE;
        comp = new Chooser("Mode", this, "layer" + layer + "encflags", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                vdial.removeAll();
                vswitch.removeAll();
                vdisp.removeAll();
                if (model.get(key) == 0)
                    {
                    vdial.add(times);
                    vdial.add(levels);
                    vswitch.add(looptype);
                    vswitch.add(numloops);
                    vdisp.add(envdisp);
                    }
                else
                    {
                    vdial.add(Strut.makeStrut(times));
                    vdial.add(Strut.makeStrut(levels));
                    vswitch.add(Strut.makeStrut(looptype));
                    vswitch.add(Strut.makeStrut(numloops));
                    }
                vdial.revalidate();
                vdial.repaint();
                vswitch.revalidate();
                vswitch.repaint();
                vdisp.revalidate();
                vdisp.repaint();
                }
            };
        vbox.add(comp);
        vbox.add(vswitch);
        
        hbox.add(vbox);
        hbox.add(vdial);
        
        hbox.addLast(vdisp);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
    
    JComponent addEnv23(int layer, int env, Color color)
        {
        Category category = new Category(this, "Envelope " + env, color);
        category.makePasteable("");
        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        HBox times = new HBox();
        HBox levels = new HBox();
        
        String prefix = "layer" + layer + "env" + env;
        
        comp = new EnvTimesDial("Attack 1", prefix + "att1time", color, 255);
        times.add(comp);
        
        comp = new EnvTimesDial("Attack 2", prefix + "att2time", color, 255);
        times.add(comp);
        
        comp = new EnvTimesDial("Attack 3", prefix + "att3time", color, 255);
        times.add(comp);
        
        comp = new EnvTimesDial("Decay", prefix + "dec1time", color, 255);
        times.add(comp);
        
        comp = new EnvTimesDial("Release 1", prefix + "rel1time", color, 255);
        times.add(comp);
        
        comp = new EnvTimesDial("Release 2", prefix + "rel2time", color, 255);
        times.add(comp);
        
        comp = new EnvTimesDial("Release 3", prefix + "rel3time", color, 255); 
        times.add(comp);
        
        comp = new EnvLevelsDial("Attack 1", prefix + "att1", color, -100, 100);
        levels.add(comp);
        
        comp = new EnvLevelsDial("Attack 2", prefix + "att2", color, -100, 100);
        levels.add(comp);
        
        comp = new EnvLevelsDial("Attack 3", prefix + "att3", color, -100, 100);
        levels.add(comp);
        
        comp = new EnvLevelsDial("Decay", prefix + "dec1", color, -100, 100);
        levels.add(comp);
        
        comp = new EnvLevelsDial("Release 1", prefix + "rel1", color, -100, 100);
        levels.add(comp);
        
        comp = new EnvLevelsDial("Release 2", prefix + "rel2", color, -100, 100);
        levels.add(comp);
        
        comp = new EnvLevelsDial("Release 3", prefix + "rel3", color, -100, 100);
        levels.add(comp);
        
        params = ENV_LOOP_TYPE;
        comp = new Chooser("Loop Type", this, prefix + "looptype", params)
            { 
            public void update(String key, Model model)
                {
                super.update(key, model);
                int step = model.get(key);
                if (step > 3) step -= 3;
                if (step == 0) step = 5;
                model.set(prefix + "loopback", step - 1); model.setStatus(prefix + "loopback", Model.STATUS_IMMUTABLE);
                } 
            };
        vbox.add(comp);
        
        params = ENV_LOOP_TIMES;
        comp = new Chooser("# Loops", this, prefix + "numloops", params);
        vbox.add(comp);
        
        hbox.add(vbox);
        
        vbox = new VBox();
        vbox.add(times);
        vbox.add(levels);
        hbox.add(vbox);
        
        EnvelopeDisplay envdisp = new EnvelopeDisplay(this, Style.ENVELOPE_COLOR(), 
            new String[] { null, prefix + "att1time", prefix + "att2time", prefix + "att3time",  
                           prefix + "dec1time", null,
                           prefix + "rel1time", prefix + "rel2time", prefix + "rel3time" },
            new String[] { null, prefix + "att1", prefix + "att2", prefix + "att3", 
                           prefix + "dec1", prefix + "dec1",
                           prefix + "rel1", prefix + "rel2", prefix + "rel3" },
            new double[] { 0.0, 0.133/252.0,  0.133/252.0,  0.133/252.0,  
                           0.133/252.0, 0.069,
                           0.133/252.0, 0.133/252.0, 0.133/252.0},
            new double[] { 0.0, 1.0/100.0, 1.0/100.0, 1.0/100.0, 
                           1.0/100.0, 1.0/100.0,
                           1.0/100.0, 1.0/100.0, 1.0/100.0});
        envdisp.setAxis(0.5);
        envdisp.setSigned(true);
        model.set(prefix + "susstakey", 4); model.setStatus(prefix + "susstakey", Model.STATUS_IMMUTABLE);
        model.set(prefix + "finstakey", 5); model.setStatus(prefix + "finstakey", Model.STATUS_IMMUTABLE);
        envdisp.setSustainStageKey(prefix + "susstakey");
        envdisp.setFinalStageKey(prefix + "finstakey");
        envdisp.setLoopKeys(0, prefix + "loopback", prefix + "susstakey");
        hbox.addLast(envdisp);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
    
    JComponent addEnvctl(String cat, int layer, Color color)
        {
        Category category = new Category(this, "Envelope Control " + cat, color);
        category.makePasteable("");
        
        JComponent comp;
        String[] params;
        String prefix;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        if (cat == "Attack") prefix = "layer" + layer + "at";
        else if (cat == "Decay") prefix = "layer" + layer + "dt";
        else if (cat == "Release") prefix = "layer" + layer + "rt";
        else prefix = "layer" + layer + "atkat";
        
        JComponent adjust;
        
        if (cat == "Impact")
            {
            adjust = new LabelledDial("Adjust", this, prefix + "time", color, -120, 120)
                { public String map(int val) 
                    { return (FONT_SIZE_3 + String.valueOf((float)val / 5) + "dB" + FONT); } };
            }
        else
            {
            adjust = new LabelledDial("Adjust", this, prefix + "time", color, -43, 43)
                { public String map(int val) { return (FONT_SIZE_3 + ENV_CTL[val + 43] + "x" + FONT); } };
            }
        hbox.add(adjust);
        
        if (cat == "Impact")
            {
            comp = new LabelledDial("Key Track", this, prefix + "kscale", color, -100, 100)
                { public String map(int val) 
                    { return (FONT_SIZE_3 + String.valueOf((float)val / 50) + "dB" + FONT); } };
            }
        else
            {
            comp = new LabelledDial("Key Track", this, prefix + "kscale", color, -43, 43)
                { public String map(int val) { return (FONT_SIZE_3 + ENV_CTL[val + 43] + "x" + FONT); } };
            }
        hbox.add(comp);
        
        if (cat == "Impact")
            {
            comp = new LabelledDial("Vel Track", this, prefix + "vscale", color, -120, 120)
                { public String map(int val) 
                    { return (FONT_SIZE_3 + String.valueOf((float)val / 5) + "dB" + FONT); } };
            hbox.add(comp);
            }
        else if (cat == "Attack")
            {
            comp = new LabelledDial("Vel Track", this, prefix + "vscale", color, -43, 43)
                { public String map(int val) { return (FONT_SIZE_3 + ENV_CTL[val + 43] + "x" + FONT); } };
            hbox.add(comp);
            }
        else
            {
            hbox.add(Strut.makeStrut(adjust));
            }
        
        if (cat == "Impact")
            {
            comp = new LabelledDial("Depth", this, prefix + "range", color, -120, 120)
                { public String map(int val) 
                    { return (FONT_SIZE_3 + String.valueOf((float)val / 5) + "dB" + FONT); } };
            }
        else
            {
            comp = new LabelledDial("Depth", this, prefix + "range", color, -43, 43)
                { public String map(int val) { return (FONT_SIZE_3 + ENV_CTL[val + 43] + "x" + FONT); } };
            }
        hbox.add(comp);
        
        vbox = new VBox();
        
        params = CONTROL_LIST_STR;
        comp = new Chooser("Source", this, prefix + "ctl", params);
        vbox.add(comp);
        
        hbox.add(vbox);
        
        category.add(hbox, BorderLayout.WEST);
        return category;
        }
    
    JComponent addVtrig(int num, int layer, Color color)
        {
        Category category = new Category(this, "Vtrig " + (num + 1), color);
        category.makePasteable("");
        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        params = LAYER_VELOCITY;
        comp = new Chooser("Level", this, "layer" + layer + "n" + num + "vtriglevel", params);
        hbox.add(comp);
        
        params = LAYER_TRIGGER;
        comp = new Chooser("Sense", this, "layer" + layer + "n" + num + "vtrigsense", params);
        hbox.add(comp);
        
        hbox.add(vbox);
        
        category.add(hbox, BorderLayout.WEST);
        return category;
        }
        
    JComponent addKdfx(Color color)
        {
        Category category = new Category(this, "KDFX", color);
        category.makePasteable("");
        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        VBox outer = new VBox();
        
        comp = new LabelledDial("Studio", this, "studio", color, 0, 999)
            {
            public void update(String key, Model model)
                {
                //super.update(key, model);
                if (model.get(key) != prevstudio) // apparently gets updated all the time ???
                    {
                    prevstudio = model.get(key);
                    fxStudio.getStudio();
                    }
                }
            };
        vbox.add(comp);
        
        hbox.add(vbox);
        
        comp = new ReadOnlyString("Studio", this, "studioname", MAXIMUM_NAME_LENGTH);
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.WEST);
        return category;
        }
    
    JComponent addFxmod(int fxmod, Color color)
        {
        Category category = new Category(this, "FXmod " + (fxmod + 1), color);
        category.makePasteable("");
        
        JComponent comp;
        HBox hbox = new HBox();
        HBox parabox = new HBox();
        HBox busbox = new HBox();
        VBox vbox = new VBox();
        VBox adjdepbox = new VBox();
        FxparamChooser paramchooser[][] = new FxparamChooser[NUM_FX_LENGTHS + 1][NUM_FXMODS]; // length 0 is not used
        FxBusChooser buschooser[][] = new FxBusChooser[5][NUM_FXMODS];
        
        for (int len = 1; len <= NUM_FX_LENGTHS; len++)
            {
            paramchooser[len][fxmod] = new FxparamChooser(len, fxmod);
            }
        
        for (int len = 0; len < 5; len++)
            {
            buschooser[len][fxmod] = new FxBusChooser(len, fxmod);
            }
        
        final Chooser source = new Chooser("Source", this, "source" + fxmod, CONTROL_LIST_FX_STR);
        
        final LabelledDial adjusteqg = new LabelledDial("Adjust", this, "adjustg" + fxmod, color, -127, 48)
            { 
            public String map(int val) 
                {
                if (val < -96) return (FONT_SIZE_3 + (float)(val + 48) + "dB" + FONT);
                else return "" + (FONT_SIZE_3 + (float)val / 2 + "dB" + FONT); 
                } 
            };
        
        final LabelledDial adjusteqf = new LabelledDial("Adjust", this, "adjustf" + fxmod, color, 0, 127)
            { public String map(int val) { return (FONT_SIZE_3 + COARSE_HZ[val + 12] + "Hz" + FONT); } };
            
        final LabelledDial adjusteqfd = new LabelledDial("Adjust", this, "adjustfd" + fxmod, color, -12, 127)
            { public String map(int val) { return (FONT_SIZE_3 + COARSE_HZ[val + 12] + "Hz" + FONT); } };
        
        final LabelledDial adjustlvl = new LabelledDial("Adjust", this, "adjustl" + fxmod, color, -128, 48)
            { 
            public String map(int val) 
                {
                if (val == -128) return "Off";
                if (val < -96) return (FONT_SIZE_3 + (float)(val + 48) + "dB" + FONT);
                else return (FONT_SIZE_3 + ((float)val / 2) + "dB" + FONT); 
                } 
            };
        
        final LabelledDial adjustpan = new LabelledDial("Adjust", this, "adjustp" + fxmod, color, -100, 100)
            { public String map(int val) { return (FONT_SIZE_3 + val + "%" + FONT); } };
            
        final LabelledDial adjustwd = new LabelledDial("Adjust", this, "adjustwd" + fxmod, color, 0, 100)
            { public String map(int val) { return (FONT_SIZE_3 + val + "%" + FONT); } };
        
        final LabelledDial deptheqg = new LabelledDial("Depth", this, "depthg" + fxmod, color, -104, 104)
            { public String map(int val) { return (FONT_SIZE_3 + val + "dB" + FONT); } };
        
        final BipolarMappedDial deptheqf = new BipolarMappedDial("Depth", "depthf" + fxmod, color, 127, VEL_TRACK, "ct");
        
        final LabelledDial depthlvl = new LabelledDial("Depth", this, "depthl" + fxmod, color, -104, 104)
            { public String map(int val) { return (FONT_SIZE_3 + val + "dB" + FONT); } };
        
        final LabelledDial depthpan = new LabelledDial("Depth", this, "depthp" + fxmod, color, -100, 100)
            { public String map(int val) { return (FONT_SIZE_3 + (val * 2) + "%" + FONT); } };
            
        final LabelledDial depthwd = new LabelledDial("Depth", this, "depthwd" + fxmod, color, -100, 100)
            { public String map(int val) { return (FONT_SIZE_3 + val + "%" + FONT); } };
        
        comp = new Chooser("", this, "studiogot", new String[] {"0", "1"})
            {
            public void update(String key, Model model)
                {
                if (model.get(key) == 1)
                    {
                    busbox.removeAll();
                    int len = fxStudio.getBusmapint().length - 11; // length starting at 0 is index in buschooser array
                    //System.out.println("studiogot fxmod " + fxmod + " len " + len);
                    buschooser[len][fxmod].setPreferredSize(source.getPreferredSize());
                    buschooser[len][fxmod].setFxelements();
                    model.set("strip" + fxmod + "a" + len, model.get("strip" + fxmod + "a" + len));
                    busbox.add(buschooser[len][fxmod]);
                    busbox.revalidate();
                    }
                }
            };
        
        comp = new Chooser("", this, "fxbuschosen" + fxmod, new String[15])
            {
            public void update(String key, Model model)
                {
                parabox.removeAll();
                int bus = fxStudio.getBusmapint()[model.get(key)];
                int algidx = fxStudio.getAlgidx()[bus];
                int len = fxAlgorithm.getParfxidx(algidx, bus).length; // length is index in paramchooser array
                //System.out.println("fxbuschosen fxmod " + fxmod + " key " + model.get(key) + " bus " + bus + " algidx " + algidx +
                //    " parfxidxlen " + fxAlgorithm.getParfxidx(algidx, bus).length);
                paramchooser[len][fxmod].setPreferredSize(source.getPreferredSize());
                paramchooser[len][fxmod].setFxelements(algidx, bus);
                model.set("fxparam" + fxmod + "a" + len, model.get("fxparam" + fxmod + "a" + len));
                parabox.add(paramchooser[len][fxmod]);
                parabox.revalidate();
                }
            };
            
        comp = new Chooser("", this, "fxparchosen" + fxmod, new String[NUM_FX_LENGTHS]) // dummy list long enough so it gets not revised
            { 
            public void update(String key, Model model)
                { // TODO : also when alg is updated
                int len = fxStudio.getBusmapint().length - 11;
                int bus = fxStudio.getBusmapint()[model.get("strip" + fxmod + "a" + len)];
                int algidx = fxStudio.getAlgidx()[bus];
                int paramval = model.get(key);
                if (paramval >= fxAlgorithm.getParfxidx(algidx, bus).length) paramval = fxAlgorithm.getParfxidx(algidx, bus).length - 1;
                // System.out.println("fxparchosen fxmod " + fxmod + " len " + len + " bus " + bus + " algidx " + algidx + " key " + model.get(key));
                int paridx = fxAlgorithm.getParfxidx(algidx, bus)[paramval];
                // System.out.println("paridx " + paridx + " type " + fxAlgorithm.getType(paridx));
                
                adjdepbox.removeAll();
                switch (fxAlgorithm.getType(paridx))
                    { // TODO : more cases
                    case 1:
                        adjdepbox.add(adjustlvl);
                        adjdepbox.add(depthlvl);
                        break;
                    case 2:
                        adjdepbox.add(adjusteqfd);
                        adjdepbox.add(deptheqf);
                        break;
                    case 3:
                        adjdepbox.add(adjusteqg);
                        adjdepbox.add(deptheqg);
                        break;
                    case 4:
                        adjdepbox.add(adjusteqf);
                        adjdepbox.add(deptheqf);
                        break;
                    case 5:
                        adjdepbox.add(adjustpan);
                        adjdepbox.add(depthpan);
                        break;
                    case 6:
                        adjdepbox.add(adjustwd);
                        adjdepbox.add(depthwd);
                        break;
                    default:
                        adjdepbox.add(Strut.makeStrut(adjusteqg.getPreferredSize().width, adjusteqg.getPreferredSize().height));
                        adjdepbox.add(Strut.makeStrut(adjusteqg.getPreferredSize().width, adjusteqg.getPreferredSize().height));
                        break;
                    }
                adjdepbox.revalidate();
                adjdepbox.repaint();
                }
            };
        
        vbox.add(busbox);
        vbox.add(parabox);
        vbox.add(source);
            
        hbox.add(vbox);
        hbox.add(adjdepbox);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
    
    JComponent addAsr(String cat, int layer, int num, Color color)
        {
        Category category = new Category(this, cat + "ASR " + num, color);
        category.makePasteable("");
        
        JComponent comp;
        String[] params;
        String prefix;
        HBox hbox = new HBox();
        VBox vswitch = new VBox();
        HBox hswitch = new HBox();
        
        if (cat == "F")
            {
            prefix = "k";
            params = CONTROL_LIST_FX_STR;
            }
        else 
            {
            prefix = "layer" + layer;
            params = CONTROL_LIST_STR;
            }
        
        final Chooser localtrigger = new Chooser("Trigger", this, prefix + "asr" + num + "trigger", params);
        vswitch.add(localtrigger);
        
        params = ASR_MOD;
        final Chooser localmode = new Chooser("Mode", this, prefix + "asr" + num + "flags", params);
        vswitch.add(localmode);
        
        final EnvTimesDial localdelay = new EnvTimesDial("Delay", prefix + "asr" + num + "dtime", color, 249); 
        hswitch.add(localdelay);
        
        final EnvTimesDial localattack = new EnvTimesDial("Attack", prefix + "asr" + num + "atime", color, 249); 
        hswitch.add(localattack);
        
        final EnvTimesDial localrelease = new EnvTimesDial("Release", prefix + "asr" + num + "rtime", color, 249); 
        hswitch.add(localrelease);
        
        if ((prefix != "k") && (num == 2))
            {
            params = CONTROL_LIST_STR_GE;
            final Chooser globaltrigger = new Chooser("Trigger", this, "gasr2trigger", params);
            
            params = ASR_MOD;
            final Chooser globalmode = new Chooser("Mode", this, "gasr2flags", params);
            
            final EnvTimesDial globaldelay = new EnvTimesDial("Delay", "gasr2dtime", color, 249);
            
            final EnvTimesDial globalattack = new EnvTimesDial("Attack", "gasr2atime", color, 249); 
            
            final EnvTimesDial globalrelease = new EnvTimesDial("Release", "gasr2rtime", color, 249); 
            
            comp = new CheckBox("", this, "globalflag")
                {
                public void update(String key, Model model)
                    {
                    vswitch.removeAll();
                    hswitch.removeAll();
                    if (model.get(key) == 0)
                        {
                        vswitch.add(localtrigger);
                        vswitch.add(localmode);
                        hswitch.add(localdelay);
                        hswitch.add(localattack);
                        hswitch.add(localrelease);
                        category.setName("ASR 2");
                        }
                    else
                        {
                        vswitch.add(globaltrigger);
                        vswitch.add(globalmode);
                        hswitch.add(globaldelay);
                        hswitch.add(globalattack);
                        hswitch.add(globalrelease);
                        category.setName("GASR 2");
                        }
                    vswitch.revalidate();
                    hswitch.revalidate();
                    }
                };
            }
        
        hbox.add(vswitch);
        hbox.add(hswitch);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
    
    JComponent addLfo(String cat, int layer, int num, Color color)
        {
        Category category = new Category(this, cat + "LFO " + num, color);
        category.makePasteable("");
        
        JComponent comp;
        String[] params;
        String prefix;
        HBox hbox = new HBox();
        VBox vswitch = new VBox();
        HBox hswitch = new HBox();
        
        if (cat == "F")
            {
            prefix = "k";
            params = CONTROL_LIST_FX_STR;
            }
        else 
            {
            prefix = "layer" + layer;
            params = CONTROL_LIST_STR;
            }
        
        final Chooser localratectl = new Chooser("Rate Control", this, prefix + "lfo" + num + "ratectl", params);
        vswitch.add(localratectl);
        
        params = LFO_SHP;
        final Chooser localshape = new Chooser("Shape", this, prefix + "lfo" + num + "shape", params);
        vswitch.add(localshape);
        
        params = LFO_PHS;
        final Chooser localphase = new Chooser("Phase", this, prefix + "lfo" + num + "phase", params);
        vswitch.add(localphase);
        
        final LabelledDial localminrate = new LabelledDial("Min Rate", this, prefix + "lfo" + num + "minrate", color, 0, 184)
            { public String map(int val) { return (FONT_SIZE_3 + LFO_RATES[val] + "Hz" + FONT); } };
        hswitch.add(localminrate);
        
        final LabelledDial localmaxrate = new LabelledDial("Max Rate", this, prefix + "lfo" + num + "maxrate", color, 0, 184)
            { public String map(int val) { return (FONT_SIZE_3 + LFO_RATES[val] + "Hz" + FONT); } };
        hswitch.add(localmaxrate);
        
        if ((prefix != "k") && (num == 2))
            {
            params = CONTROL_LIST_STR_GE;
            final Chooser globalratectl = new Chooser("Rate Control", this, "glfo2ratectl", params);
            
            params = LFO_SHP;
            final Chooser globalshape = new Chooser("Shape", this, "glfo2shape", params);
            
            params = LFO_PHS;
            final Chooser globalphase = new Chooser("Phase", this, "glfo2phase", params);
            
            final LabelledDial globalminrate = new LabelledDial("Min Rate", this, "glfo2minrate", color, 0, 184)
                { public String map(int val) { return (FONT_SIZE_3 + LFO_RATES[val] + "Hz" + FONT); } };
            
            final LabelledDial globalmaxrate = new LabelledDial("Max Rate", this, "glfo2maxrate", color, 0, 184)
                { public String map(int val) { return (FONT_SIZE_3 + LFO_RATES[val] + "Hz" + FONT); } };
            
            comp = new CheckBox("", this, "globalflag")
                {
                public void update(String key, Model model)
                    {
                    vswitch.removeAll();
                    hswitch.removeAll();
                    if (model.get(key) == 0)
                        {
                        vswitch.add(localratectl);
                        vswitch.add(localshape);
                        vswitch.add(localphase);
                        hswitch.add(localminrate);
                        hswitch.add(localmaxrate);
                        category.setName("LFO 2");
                        }
                    else
                        {
                        vswitch.add(globalratectl);
                        vswitch.add(globalshape);
                        vswitch.add(globalphase);
                        hswitch.add(globalminrate);
                        hswitch.add(globalmaxrate);
                        category.setName("GLFO 2");
                        }
                    vswitch.revalidate();
                    hswitch.revalidate();
                    }
                };
            }
        
        hbox.add(vswitch);
        hbox.add(hswitch);
                        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
    
    JComponent addFcn(String cat, int layer, int num, Color color)
        {
        Category category = new Category(this, cat + "FUN " + (num + 1), color);
        category.makePasteable("");
        
        JComponent comp;
        String[] params;
        String prefix;
        HBox hbox = new HBox();
        VBox vswitch = new VBox();
        
        if (cat == "F")
            {
            prefix = "k"; 
            params = CONTROL_LIST_FUN_FX_STR;
            }
        else 
            {
            prefix = "layer" + layer;
            params = CONTROL_LIST_FUN_STR;
            }
        
        final Chooser localinputa = new Chooser("Input a", this, prefix + "fcn" + (num + 1) + "arg1", params);
        vswitch.add(localinputa);
        
        final Chooser localinputb = new Chooser("Input b", this, prefix + "fcn" + (num + 1) + "arg2", params);
        vswitch.add(localinputb);
        
        params = FUN_FUN;
        final Chooser localfunction = new Chooser("Function", this, prefix + "fcn" + (num + 1) + "op", params);
        vswitch.add(localfunction);
        
        if ((prefix != "k") && ((num == 1) || (num == 3)))
            {
            params = CONTROL_LIST_FUN_GE_STR;
            final Chooser globalinputa = new Chooser("Input a", this, "gfcn" + (num + 1) + "arg1", params);
            
            final Chooser globalinputb = new Chooser("Input b", this, "gfcn" + (num + 1) + "arg2", params);
            
            params = FUN_FUN;
            final Chooser globalfunction = new Chooser("Function", this, "gfcn" + (num + 1) + "op", params);
            
            comp = new CheckBox("", this, "globalflag")
                {
                public void update(String key, Model model)
                    {
                    vswitch.removeAll();
                    if (model.get(key) == 0)
                        {
                        vswitch.add(localinputa);
                        vswitch.add(localinputb);
                        vswitch.add(localfunction);
                        category.setName("FUN " + (num + 1));
                        }
                    else
                        {
                        vswitch.add(globalinputa);
                        vswitch.add(globalinputb);
                        vswitch.add(globalfunction);
                        category.setName("GFUN " + (num + 1));
                        }
                    vswitch.revalidate();
                    }
                };
            }
        
        hbox.add(vswitch);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
    
    public void changePatch(Model tempModel)
        {
        changePatch(tempModel.get("bank"), tempModel.get("number"));
        }
    
    public void changePatch(int bank, int number)
        {
        Object[] message = new Object[2];
        
        // bank select
        message[0] = buildCC(getChannelOut(), 32, bank)[0];
        // PC
        message[1] = buildPC(getChannelOut(), number)[0];
            
        tryToSendMIDI(message);
        }
    public boolean gatherPatchInfo(String title, Model changeThis, boolean writing)     
        {
        String[] banks = BANKS;
        
        JComboBox bank = new JComboBox(banks);
        int num = model.get("number");
        JTextField number = new SelectedTextField("" + (num < 10 ? "0" :  "") + num, 3);
        
        while(true)
            {
            boolean result = showMultiOption(this, new String[] { "Bank", "Patch Number"}, 
                new JComponent[] { bank, number }, title, "Enter the Bank and Patch number");
            
            if (result == false) 
                return false;
                            
            int n;
            try { n = Integer.parseInt(number.getText()); }
            catch (NumberFormatException e)
                {
                showSimpleError(title, "The Patch Number must be an integer 0...99");
                continue;
                }
            if (n < 0 || n > 99)
                {
                showSimpleError(title, "The Patch Number must be an integer 0...99");
                continue;
                }
                            
            int i = bank.getSelectedIndex();
                    
            changeThis.set("bank", i);
            changeThis.set("number", n);
                    
            return true;
            }
        }
    
    public int parse(byte[] data, boolean fromFile)
        { 
        // TODO : if a "not found" object is requested, the Kurzweil does not reply at all
        int i = 0;
        int sod = 0;                                       // start of data
        byte[] data8 = null;                               // converted data array
        byte[] name = new byte[MAXIMUM_NAME_LENGTH];
        
        System.out.println("parse");
        
        i = 5;                                             // type
        int type = data[i] * 128 + data[i + 1];
        i = 7;                                             // ID
        int id = data[i] * 128 + data[i + 1];
        i = 9;                                             // number of effective bytes after conversion
        int size = (data[i] << 14) + (data[i + 1] << 7) + data[i + 2];
        i = START_OF_NAME;
        do
            {
            i += 1;
            } while (data[i] != 0);
        System.arraycopy(data, START_OF_NAME, name, 0, i - START_OF_NAME);
        sod = i + 2;                                       // 1 byte past form : actual data
        
        if (data[sod - 1] == FORM_NIBBLE) data8 = convert4To8Bit(data, sod);
        else data8 = convert7To8Bit(data, sod);
        
        if (type == PROGRAM_TYPE) 
            {
            programdata = data8; // preserve data for resumeParseprogram
            resumeparse = true; 
            prevstudio = 0;      // allow update of studio
            }
        if ( (tuple != null) && (model.get("fmt") >= FORMAT_4) )
            { // MIDI and KDFX present
            if (type == PROGRAM_TYPE) return(parseProgram(programdata, id, name));
            else if (type == STUDIO_TYPE) return(parseStudio(data8, name));
            else if (type == FX_PRESET_TYPE) return(parsePreset(data8, id));
            }
        else
            {
            parseProgram(programdata, id, name);
            model.set("studioname", "Not connected");
            // System.out.println(data8[0] + " " + pddi);
            return(resumeParseprogram(programdata, pddi));
            }
        return PARSE_FAILED;
        }
        
    int parseProgram(byte[] d, int pid, byte[] name)
        {
        int i = 0;
        int di = 0; // index in data array
        
        System.out.println("parseProgram");
        model.set("bank", pid / 100);
        model.set("number", pid % 100);
        
        try
            {
            model.set("name", new String(name, "US-ASCII"));
            }
        catch (UnsupportedEncodingException e)
            {
            Synth.handleException(e); 
            }
        
        model.set("pgmSegTag", d[di++]); model.setStatus("pgmSegTag", Model.STATUS_IMMUTABLE);
        model.set("fmt", d[di++]); model.setStatus("fmt", Model.STATUS_IMMUTABLE);
        model.set("numlayers", d[di++]);
        int numlayers = model.get("numlayers");
        model.set("strnumlayers", numlayers + " layer" + ((numlayers > 1) ? "s" : ""));
        model.set("monoflag", (d[di] & 0x01) == 0 ? 0 : 1);
        model.set("portaflag", (d[di] & 0x02) == 0 ? 0 : 1);
        model.set("globalflag", (d[di] & 0x04) == 0 ? 0 : 1);
        model.set("attportaflag", (d[di] & 0x08) == 0 ? 1 : 0);
        model.set("legato", (d[di++] & 0x10) == 0 ? 1 : 0);
        model.set("bendrange", d[di++]);
        model.set("portslope", d[di++]);
        di += 10; // rfus
        
        setasrparams("g", 2, CONTROL_MAP_GE, d, di); di += ASR_DP_SIZE;
        setfunparams("g", 2, CONTROL_MAP_FUN_GE, d, di); di += FUN_DP_SIZE;
        setlfoparams("g", 2, CONTROL_MAP_GE, d, di); di += LFO_DP_SIZE;
        setfunparams("g", 4, CONTROL_MAP_FUN_GE, d, di); di += FUN_DP_SIZE;
        
        model.set("efxSegTag", d[di++]); model.setStatus("efxSegTag", Model.STATUS_IMMUTABLE);
        model.set("efxchan", d[di++]); model.setStatus("efxchan", Model.STATUS_IMMUTABLE);
        model.set("efxprog", d[di++]); model.setStatus("efxprog", Model.STATUS_IMMUTABLE);
        model.set("efxmix", d[di++]); model.setStatus("efxmix", Model.STATUS_IMMUTABLE);
        model.set("efxctl1", d[di++]);model.setStatus("efxctl1", Model.STATUS_IMMUTABLE);
        model.set("efxout1", d[di++]); model.setStatus("efxout1", Model.STATUS_IMMUTABLE);
        model.set("efxctl2", d[di++]); model.setStatus("efxctl2", Model.STATUS_IMMUTABLE);
        model.set("efxout2", d[di++]);model.setStatus("efxout2", Model.STATUS_IMMUTABLE); 
        
        if (model.get("fmt") >= FORMAT_4)
            { // KDFX present
            model.set("fxrtSegTag", d[di++]); model.setStatus("fxrtSegTag", Model.STATUS_IMMUTABLE);
            model.set("vers", d[di++]); model.setStatus("vers", Model.STATUS_IMMUTABLE);
            model.set("studio", d[di++] * 256 + (d[di] < 0 ? 256 + d[di] : d[di])); di++;
            di += 4; // rfu
            }
        pddi = di;
        
        return PARSE_INCOMPLETE; 
        }
    
    int resumeParseprogram(byte[] d, int di)
        {
        System.out.println("resumeParseprogram");
        
        int layerfirstdata = LAYER_FIRST_DATA_FMT_4;
        int layerdatasize = LAYER_DATA_SIZE_FMT_3_4;
        if (model.get("fmt") < FORMAT_4)
            {
            layerfirstdata = LAYER_FIRST_DATA_FMT_2_3;
            }
        if (model.get("fmt") == FORMAT_2)
            {
            layerdatasize = LAYER_DATA_SIZE_FMT_2;
            }
        
        int numlayers = model.get("numlayers");
        
        if (model.get("fmt") >= FORMAT_4)
            { // KB3 (and KDFX) present
            for (int fxmod = 0; fxmod < NUM_FXMODS; fxmod++)
                {
                setfxparams(fxmod, CONTROL_MAP_FX, d, di);
                di += FXMOD_DATA_SIZE;
                }
            
            setasrparams("k", 1, CONTROL_MAP_FX, d, di); di += ASR_DP_SIZE;
            setasrparams("k", 2, CONTROL_MAP_FX, d, di); di += ASR_DP_SIZE;
            setfunparams("k", 1, CONTROL_MAP_FUN_FX, d, di); di += FUN_DP_SIZE;
            setfunparams("k", 2, CONTROL_MAP_FUN_FX, d, di); di += FUN_DP_SIZE;
            setlfoparams("k", 1, CONTROL_MAP_FX, d, di);  di += LFO_DP_SIZE;
            setlfoparams("k", 2, CONTROL_MAP_FX, d, di);  di += LFO_DP_SIZE;
            setfunparams("k", 3, CONTROL_MAP_FUN_FX, d, di); di += FUN_DP_SIZE;
            setfunparams("k", 4, CONTROL_MAP_FUN_FX, d, di); di += FUN_DP_SIZE;
            
            int kb3_first_param = (Integer)(parametersToIndex.get("hammSegTag1"));
            for (int kb3par = 0; kb3par < ((Integer)(parametersToIndex.get("lyrSegTag")) - kb3_first_param); kb3par++)
                {
                if (!parameters[kb3_first_param + kb3par].equals("---"))
                    {
                    model.set(parameters[kb3_first_param + kb3par], d[KB3_FIRST_DATA + kb3par]);
                    model.setStatus(parameters[kb3_first_param + kb3par], Model.STATUS_IMMUTABLE); // TODO : remove when kb3 implemented
                    }
                }
            }
        
        for (int layer = 0; layer < numlayers; layer++)
            {
            setlayerparams(layer, d, layerfirstdata + layer * layerdatasize);
            }
        
        /*for (int i = 0; i < (layerfirstdata + layerdatasize); i++)
          {
          System.out.println(i + " " + (int)(d[i] & 0xFF)); 
          }*/
        //System.out.println("strip0 " + (int)(d[58] & 0xFF));
        // System.out.println("fxparam0a0 " + (int)(d[59] & 0xFF));
        //System.out.println("adjustf0 " + (int)(d[60] & 0xFF));
        
        selLayer = 0;
        setTabs();
        
        revise();
        
        outCom.uCfrommodel(); // explicitly tell that update common comes from the model, not from manual chooser action
        
        resumeparse = false;
        pddi = 0;
        return PARSE_SUCCEEDED; 
        }
        
    int parseStudio(byte[] d, byte[] name)
        {
        System.out.println("parseStudio");
        
        try
            {
            model.set("studioname", new String(name, "US-ASCII"));
            }
        catch (UnsupportedEncodingException e)
            {
            Synth.handleException(e); 
            }
        
        fxStudio.getPresets(d);
        return PARSE_INCOMPLETE;
        }
    
    int parsePreset(byte[] d, int id)
        {
        System.out.println("parsePreset");
        boolean allalgsreceived = false;
        
        allalgsreceived = fxStudio.getAlgsfrompreset(d, id);
        if (!allalgsreceived) return PARSE_INCOMPLETE;
        else
            {
            if (resumeparse) return(resumeParseprogram(programdata, pddi));
            else return PARSE_SUCCEEDED; 
            }
        }
    
    public void parseParameter(byte[] data) 
        { // unrecognised response, so not a patch, must be a name
        System.out.println("parseParameter");
        byte[] bname = new byte[MAXIMUM_NAME_LENGTH];
        String key = new String();
        int i = 0;
        int start_of_name = 0;
        int size = 1;
        
        i = 4; // reply
        int msg = data[i];
        if (msg == SCREENREPLY_MSG)
            {
            start_of_name = 5; // string
            }
        else
            {
            i = 5; // type
            int type = data[i] * 128 + data[i + 1];
            
            i = 7; // ID
            int id = data[i] * 128 + data[i + 1];
            if (type == KEYMAP_TYPE)
                {
                keymapName.parseRequest();
                // TODO : only works for current layer and not same ID for both keymaps
                if (id == model.get("layer" + selLayer + "keymap")) key = "layer" + selLayer + "keymapname";
                if (id == model.get("layer" + selLayer + "keymap2")) key = "layer" + selLayer + "keymapname2";
                }
            
            i = 9; // size
            size = (data[i] << 14) + (data[i + 1] << 7) + data[i + 2];
            
            start_of_name = START_OF_NAME;
            }
        i = start_of_name;
        do
            {
            i += 1;
            } while ( (i < data.length) && (data[i] != 0) );
        System.arraycopy(data, start_of_name, bname, 0, i - start_of_name);
        try
            {
            String sname = new String(bname, "US-ASCII");
            if (size == 0) sname = "Not found";
            if (msg == SCREENREPLY_MSG) System.out.println(sname);
            else model.set(key, sname);
            }
        catch (UnsupportedEncodingException e)
            {
            Synth.handleException(e); 
            }
        }
    
    public static String getSynthName() 
        { 
        return "Kurzweil K2600"; 
        }
    
    public String getDefaultResourceFileName() 
        {
        return "KurzweilK2600.init"; 
        }
        
    public String getHTMLResourceFileName() 
        { 
        return "KurzweilK2600.html"; 
        }
    
    public String getPatchLocationName(Model model)
        {
        if (!model.exists("number")) return null;
        if (!model.exists("bank")) return null;
        
        int number = (model.get("number"));
        return ((BANKS[model.get("bank")]) + "-" + (number > 99 ? "" : (number > 9 ? "" : "0")) + number);
        }
    
    public Model getNextPatchLocation(Model model)
        {
        int bank = model.get("bank");
        int number = model.get("number");
        
        int numBanks = BANKS.length;
        
        number++;
        if (number >= 100)
            {
            bank++;
            number = 0;
            if (bank >= 10)
                bank = 0;
            }
                
        Model newModel = buildModel();
        newModel.set("bank", bank);
        newModel.set("number", number);
        return newModel;
        }
    
    public String getPatchName(Model model) 
        {
        return model.get("name", "Untitled");
        }
    
    public String revisePatchName(String name)
        {
        name = super.revisePatchName(name);  // trim first time
        if (name.length() > MAXIMUM_NAME_LENGTH)
            name = name.substring(0, MAXIMUM_NAME_LENGTH);
        
        StringBuffer nameb = new StringBuffer(name);
        for(int i = 0 ; i < nameb.length(); i++)
            {
            char c = nameb.charAt(i);
            if (c < 32 || c > 127)
                nameb.setCharAt(i, ' ');
            }
        name = nameb.toString();
        return super.revisePatchName(name);  // trim again
        }
    
    public void revise()
        {
        super.revise();
        
        String nm = model.get("name", "Untitled");
        String newnm = revisePatchName(nm);
        if (!nm.equals(newnm))
            model.set("name", newnm);
        }
    
    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile) 
        { 
        System.out.println("emit");
        if (tempModel == null)
            tempModel = getModel();
        
        byte[] data = null;
        int numlayers = model.get("numlayers");
        int layerfirstdata = LAYER_FIRST_DATA_FMT_4;
        int layerdatasize = LAYER_DATA_SIZE_FMT_3_4;
        if (model.get("fmt") < FORMAT_4)
            {
            layerfirstdata = LAYER_FIRST_DATA_FMT_2_3;
            }
        if (model.get("fmt") == FORMAT_2)
            {
            layerdatasize = LAYER_DATA_SIZE_FMT_2;
            }
        int size = layerfirstdata + (numlayers * layerdatasize) + 2; 
        byte[] d = new byte[size];
        int di = 0; // index in data array
        
        d[di++] = (byte)model.get("pgmSegTag");
        d[di++] = (byte)model.get("fmt");
        d[di++] = (byte)model.get("numlayers");
        d[di++] = (byte)(model.get("monoflag") + 
            ((model.get("portaflag") == 0) ? 0 : 0x02) + 
            ((model.get("globalflag") == 0) ? 0 : 0x04) +
            ((model.get("attportaflag") == 0) ? 0x08 : 0) +
            ((model.get("legato") == 0) ? 0x10 : 0));
        d[di++] = (byte)(model.get("bendrange"));
        d[di++] = (byte)(model.get("portslope"));
        di += 10; // rfus
        
        getasrparams("g", 2, CONTROL_MAP_GE, d, di); di += ASR_DP_SIZE;
        getfunparams("g", 2, CONTROL_MAP_FUN_GE, d, di); di += FUN_DP_SIZE;
        getlfoparams("g", 2, CONTROL_MAP_GE, d, di); di += LFO_DP_SIZE;
        getfunparams("g", 4, CONTROL_MAP_FUN_GE, d, di); di += FUN_DP_SIZE;
        
        d[di++] = (byte)model.get("efxSegTag");
        d[di++] = (byte)model.get("efxchan");
        d[di++] = (byte)model.get("efxprog");
        d[di++] = (byte)model.get("efxmix");
        d[di++] = (byte)model.get("efxctl1");
        d[di++] = (byte)model.get("efxout1");
        d[di++] = (byte)model.get("efxctl2");
        d[di++] = (byte)model.get("efxout2");
        
        if (model.get("fmt") >= FORMAT_4)
            { // KB3 (and KDFX) present
            d[di++] = (byte)model.get("fxrtSegTag");
            d[di++] = (byte)model.get("vers");
            int studio = model.get("studio");
            d[di++] = (byte)(studio / 256);
            d[di++] = (byte)(studio % 256);
            di += 4; // rfus
            
            for (int fxmod = 0; fxmod < NUM_FXMODS; fxmod++) 
                {
                getfxparams(fxmod, CONTROL_MAP_FX, d, di);
                di += FXMOD_DATA_SIZE;
                }
             
            getasrparams("k", 1, CONTROL_MAP_FX, d, di); di += ASR_DP_SIZE;
            getasrparams("k", 2, CONTROL_MAP_FX, d, di); di += ASR_DP_SIZE;
            getfunparams("k", 1, CONTROL_MAP_FUN_FX, d, di); di += FUN_DP_SIZE;
            getfunparams("k", 2, CONTROL_MAP_FUN_FX, d, di); di += FUN_DP_SIZE;
            getlfoparams("k", 1, CONTROL_MAP_FX, d, di); di += LFO_DP_SIZE;
            getlfoparams("k", 2, CONTROL_MAP_FX, d, di); di += LFO_DP_SIZE;
            getfunparams("k", 3, CONTROL_MAP_FUN_FX, d, di); di += FUN_DP_SIZE;
            getfunparams("k", 4, CONTROL_MAP_FUN_FX, d, di); di += FUN_DP_SIZE;
            
            int kb3_first_param = (Integer)(parametersToIndex.get("hammSegTag1"));
            for (int kb3par = 0; kb3par < ((Integer)(parametersToIndex.get("lyrSegTag")) - kb3_first_param); kb3par++)
                {
                if (!parameters[kb3_first_param + kb3par].equals("---"))
                    {
                    d[KB3_FIRST_DATA + kb3par] = (byte)model.get(parameters[kb3_first_param + kb3par]);
                    }
                }
            }
        
        for (int layer = 0; layer < numlayers; layer++)
            {
            getlayerparams(layer, d, layerfirstdata + layer * layerdatasize);
            }
        
        /*
          for (int i = 0; i < (layerfirstdata + layerdatasize); i++)
          {
          System.out.println(i + " " + (int)(d[i] & 0xFF)); 
          }
        */
        
        data = convert8To7Bit(d); 
        
        int pid = tempModel.get("bank") * 100 + tempModel.get("number");
        // number of effective bytes after conversion
        int size1 = (size >> 14);
        int rest = size - (size1 << 14);
        int size2 = (rest >> 7);
        int size3 = rest - (size2 << 7);
        
        int nulterm = 0;
        char[] name = (model.get("name", "Untitled")).toCharArray();
        if (name[name.length - 1] != 0) nulterm = 1;
        
        int sod = START_OF_NAME + name.length + nulterm + 1;    // start of data
        byte[] emit = new byte[sod + data.length + 1 + 1]; // header, name + null, form, data, xsum, eox
        emit[0] = SOX;
        emit[1] = KURZWEIL_ID;
        emit[2] = (byte) 0x00;         // device ID
        emit[3] = K2600_ID;
        emit[4] = WRITE_MSG;
        emit[5] = (byte)(PROGRAM_TYPE / 128);
        emit[6] = (byte)(PROGRAM_TYPE % 128);
        emit[7] = (byte)(pid / 128);  // idno MSB
        emit[8] = (byte)(pid % 128);  // idno LSB
        emit[9] = (byte) size1;
        emit[10] = (byte) size2;
        emit[11] = (byte) size3;
        emit[12] = (byte) 0x00;        // mode
        for(int i = 0; i < name.length; i++)
            emit[START_OF_NAME + i] = (byte)(name[i] & 127);
        emit[START_OF_NAME + name.length + nulterm] = (byte) 0x01;        // form
        
        System.arraycopy(data, 0, emit, sod, data.length);     // data
        
        int xsum = 0;
        for (int i =0; i < (data.length - 2); i++) xsum += data[i];
        xsum &= 0x7F;
        emit[emit.length - 2] = (byte)xsum;
        
        emit[emit.length - 1] = EOX;
        
        return emit;
        }
    
    public byte[] requestDump(Model tempModel) 
        { 
        if (tempModel == null)
            tempModel = getModel();
        
        byte msb = (byte)((tempModel.get("bank", 0) * 100 + tempModel.get("number", 0)) / 128);
        byte lsb = (byte)((tempModel.get("bank", 0) * 100 + tempModel.get("number", 0)) % 128);
        byte[] data = new byte[11];
        data[0] = SOX;
        data[1] = KURZWEIL_ID;
        data[2] = (byte)0x00;   // device ID
        data[3] = K2600_ID;
        data[4] = READ_MSG;
        data[5] = (byte)(PROGRAM_TYPE / 128);
        data[6] = (byte)(PROGRAM_TYPE % 128);
        data[7] = msb;
        data[8] = lsb;
        data[9] = (byte)0x01;   // form
        data[10] = EOX;
        return data;
        }
    
    public byte[] requestCurrentDump()
        { 
        // Returns patch 202
        byte[] data = new byte[11];
        data[0] = SOX;
        data[1] = KURZWEIL_ID;
        data[2] = (byte)0x00;   // device ID
        data[3] = K2600_ID;
        data[4] = READ_MSG;
        data[5] = (byte)(PROGRAM_TYPE / 128);
        data[6] = (byte)(PROGRAM_TYPE % 128);
        data[7] = (byte)(202 / 128);
        data[8] = (byte)(202 % 128);
        data[9] = (byte)0x01;   // form
        data[10] = EOX;
        return data;
        }
    
    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        addKurzweilMenu();
        
        return frame;
        }
    
    public void addKurzweilMenu()
        {
        JMenu menu = new JMenu("Kurzweil K2600");
        menubar.add(menu);
        
        JMenuItem layernxt = new JMenuItem("Next Layer");
        layernxt.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(layernxt);
        layernxt.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                if (model.get("numlayers") > 1)
                    {
                    if (selLayer < (model.get("numlayers") - 1)) selLayer += 1;
                    else selLayer = 0;
                    
                    setTabs();
                    }
                }
            });
        
        JMenuItem layerprv = new JMenuItem("Prev Layer");
        layerprv.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() |  InputEvent.SHIFT_MASK));
        menu.add(layerprv);
        layerprv.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                if (model.get("numlayers") > 1)
                    {
                    if (selLayer > 0) selLayer -= 1;
                    else selLayer = model.get("numlayers") - 1;
                    
                    setTabs();
                    }
                }
            });
        
        menu.addSeparator();
        
        JMenuItem newlyr = new JMenuItem("New layer");
        menu.add(newlyr);
        newlyr.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                int numlayers = model.get("numlayers");
                if (numlayers < MAX_NUM_LAYERS)
                    {
                    undo.push(getModel());
                    setSendMIDI(false);
                    boolean currentPush = undo.getWillPush();
                    undo.setWillPush(false);
                    
                    selLayer = numlayers;
                    setlayerparams(selLayer, LAYER_SINGLE, 0);
                    numlayers += 1;
                    model.set("numlayers", numlayers);
                    model.set("strnumlayers", numlayers + " layer" + ((numlayers > 1) ? "s" : ""));
                    outCom.updateCommon();
                    setTabs();
                    
                    undo.setWillPush(currentPush);
                    setSendMIDI(true);
                    }
                else
                    {
                    JOptionPane.showMessageDialog(null, "Maximum 32 layers allowed.", "Cannot create layer", JOptionPane.WARNING_MESSAGE);
                    }
                }
            });
        
        
        JMenuItem newtriplyr = new JMenuItem("New triple layer");
        menu.add(newtriplyr);
        newtriplyr.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                int numlayers = model.get("numlayers");
                if (numlayers < (MAX_NUM_LAYERS - 2))
                    {
                    undo.push(getModel());
                    setSendMIDI(false);
                    boolean currentPush = undo.getWillPush();
                    undo.setWillPush(false);
                    
                    selLayer = numlayers;
                    for (int layer = 0; layer < 3; layer++)
                        {
                        setlayerparams(layer + selLayer, LAYER_TRIPLE[layer], 0);
                        }
                    numlayers += 3;
                    model.set("numlayers", numlayers);
                    model.set("strnumlayers", numlayers + " layer" + ((numlayers > 1) ? "s" : ""));
                    outCom.updateCommon();
                    setTabs();
                    
                    undo.setWillPush(currentPush);
                    setSendMIDI(true);
                    }
                else
                    {
                    JOptionPane.showMessageDialog(null, "Maximum 32 layers allowed.", "Cannot create layer", JOptionPane.WARNING_MESSAGE);
                    }
                }
            });
        
        JMenuItem duplyr = new JMenuItem("Duplicate layer");
        menu.add(duplyr);
        duplyr.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                int numlayers = model.get("numlayers");
                int calalg = model.get("layer" + selLayer + "calalg");
                if (calalg < ALG_LYR_T1)
                    {
                    if (numlayers < MAX_NUM_LAYERS)
                        {
                        int layerfirstparam = (Integer)(parametersToIndex.get("lyrSegTag"));
                        int layer_params = (Integer)(parametersToIndex.get("f4outputlsbfh")) - (Integer)(parametersToIndex.get("lyrSegTag")) + 1;
                        if (model.get("fmt") < FORMAT_4)
                            {
                            layerfirstparam = (Integer)(parametersToIndex.get("fxrtSegTag"));
                            }
                        if (model.get("fmt") == FORMAT_2)
                            {
                            layer_params -= 16;
                            }
                        int layerlastparam = layerfirstparam + layer_params;
                        for(int i = layerfirstparam; i < layerlastparam; i++)
                            {
                            model.set("layer" + numlayers + parameters[i], model.get("layer" + selLayer + parameters[i]));
                            }
                        setAlgminmax(numlayers);
                        selLayer = numlayers;
                        numlayers += 1;
                        model.set("numlayers", numlayers);
                        model.set("strnumlayers", numlayers + " layer" + ((numlayers > 1) ? "s" : ""));
                        outCom.updateCommon();
                        setTabs();
                        }
                    else
                        {
                        JOptionPane.showMessageDialog(null, "Maximum 32 layers allowed.", "Cannot create layer", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                else
                    {
                    if (numlayers < (MAX_NUM_LAYERS - 2))
                        {
                        if (calalg >= ALG_LYR_T2) selLayer -= 1;
                        if (calalg >= ALG_LYR_T3) selLayer -= 1;
                        
                        for (int layer = 0; layer < 3; layer++)
                            { 
                            for(int i = (Integer)(parametersToIndex.get("lyrSegTag")); i < ((Integer)(parametersToIndex.get("f4outputlsbfh")) + 1); i++)
                                {
                                model.set("layer" + (layer + numlayers) + parameters[i], model.get("layer" + (layer + selLayer) + parameters[i]));
                                }
                            setAlgminmax(layer + numlayers);
                            }
                        selLayer = numlayers;
                        numlayers += 3;
                        model.set("numlayers", numlayers);
                        model.set("strnumlayers", numlayers + " layer" + ((numlayers > 1) ? "s" : ""));
                        outCom.updateCommon();
                        setTabs();
                        }
                    else
                        {
                        JOptionPane.showMessageDialog(null, "Maximum 32 layers allowed.", "Cannot create layer", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                }
            });
        
        JMenuItem dellyr = new JMenuItem("Delete layer");
        menu.add(dellyr);
        dellyr.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent evt)
                {
                int numlayers = model.get("numlayers");
                // TODO
                }
            });
        }
    
    public void tabChanged()
        {
        // This method is called whenever the tabs are changed in case you need to do something
        // like update a menu item in response to it etc.  Be sure to call super.tabChanged();
        super.tabChanged();
        }
    
    public boolean testVerify(Synth synth2, String key, Object obj1, Object obj2)
        {
        // The edisyn.test.SanityCheck class performs sanity-checks on synthesizer classes
        // by randomizing a synth instance, then writing it out, then reading it back in in a new synth, 
        // and comparing the two.  When parameters are different, this could be because of an emit bug 
        // or a parse bug, OR it could be entirely legitimate (perhaps you don't emit a certain 
        // parameter, or use it for a special purpose, etc.)  Before it issues an error in this case,
        // it calls this method to see if the difference is legitimate.  It calls testVerify(...)
        // on the first synth, passing in the second one.  The parameter in question is provided as
        // a key, as are the two values (as Strings or Integers) in question.  Return TRUE if the
        // difference is legitimate, else false.  By default, all differences are considered illegitimate.
        
        //if (key.contains("loopback")) return true;
        //else if (key.contains("susstakey")) return true;
        //else if (key.contains("finstakey")) return true;
        //else 
        return false;
        
        /* TODO : correct
           edisyn.synth.kurzweilk2600.KurzweilK2600
           WARNING (Model.mutate): metric max is above max.  That can't be right:  layer0calalg
           java.lang.IllegalArgumentException: bound must be positive
           at java.base/java.util.Random.nextInt(Random.java:322)
           at edisyn.Model.mutate(Model.java:1061)
           at edisyn.Synth.doMutate(Synth.java:5777)
           at edisyn.test.SanityCheck.test(SanityCheck.java:200)
           at edisyn.test.SanityCheck.main(SanityCheck.java:181)
        */
        }
    
    public int getPauseAfterReceivePatch() { return 1000; }
    
    public String[] getPatchNumberNames() { return buildIntegerNames(100, 0); }
    public String[] getBankNames() { return BANKS; }
    public boolean[] getWriteableBanks() { return new boolean[] { true, true, true, true, true, true, true, true, true, true }; }
    public boolean getSupportsPatchWrites() { return true; }
    public int getPatchNameLength() { return MAXIMUM_NAME_LENGTH; }
    
    /** Map of parameter -> index in the allParameters array. */
    static HashMap parametersToIndex = null;
    
    final static String[] parameters = new String[]
    {
    // pgm             index in sysex data
    "pgmSegTag",    // 0    8
    "fmt",          // 1    fmt 4 : Kurzweil + KB3 (+ KDFX)
    "numlayers",    // 2    # layers in program
    // "modeflags"  // 3    COMMON  bitmap MONO/PORT/GLOBALS/ATT/PORT LEGATO (see bitmaps)
    "monoflag",
    "portaflag",
    "globalflag",
    "attportaflag",
    "legato",
    "bendrange",    // 4    COMMON pitch bend range
    "portslope",    // 5    COMMON portamento rate
    "---",          // 6    effmixcontrol EFFECT Wet/Dry SRC (ctl-list) NA to K2600 / KDFX     NA
    "---",          // 7    effmixrange EFFECT Wet/Dry Depth                                   NA
    "---",          // 8    effcoarse1 EFFECT variable 1 adjust                                NA
    "---",          // 9    effcontrol1 EFFECT variable 1 src                                  NA
    "---",          // 10   effrange1 EFFECT variable 1 depth                                  NA
    "---",          // 11   effdest1 EFFECT variable 1                                         NA
    "---",          // 12   effcoarse2 EFFECT variable 2 adjust                                NA
    "---",          // 13   effcontrol2 EFFECT variable 2 src                                  NA
    "---",          // 14   effrange2 EFFECT variable 2 depth                                  NA
    "---",          // 15   effdest2 EFFECT variable 2                                         NA
    
    // asr GASR2
    "gasr2SegTag",  // 16   asrSegTag + 1 : 17
    "---",          // 17   rfu1
    "gasr2trigger", // 18
    "gasr2flags",   // 19   mode
    "gasr2dtime",   // 20   dly
    "gasr2atime",   // 21   atk
    "---",          // 22   rfu2
    "gasr2rtime",   // 23   rls
    
    // fcn1 GFUN2
    "gfcn2SegTag",  // 24   fcnSegTag + 1 : 25
    "gfcn2op",      // 25   Global ON 0000 0010
    "gfcn2arg1",    // 26   input A
    "gfcn2arg2",    // 27   input B
    
    // lfo GLFO2
    "glfo2SegTag",  // 28   lfoSegTag + 1 : 21
    "---",          // 29   rfu1
    "glfo2ratectl", // 30   LFO glfo2 rate ctl
    "glfo2minrate", // 31   LFO glfo2 min rate
    "glfo2maxrate", // 32   LFO glfo2 max rate
    "glfo2phase",   // 33   LFO glfo2 phase
    "glfo2shape",   // 34   LFO glfo2 shape
    "---",          // 35   rfu2
    
    // fcn2 GFUN4
    "gfcn4SegTag",  // 36   fcnSegTag + 3 : 27
    "gfcn4op",      // 37   Global ON 0000 0100
    "gfcn4arg1",    // 38   input A
    "gfcn4arg2",    // 39   input B
    
    // efx not with KDFX 
    "efxSegTag",    // 40   15
    "efxchan",      // 41   chan, 0 means internal      0                                          NA
    "efxprog",      // 42   EFFECT preset # (in memory order)                                  NA 1 kb3 : 105
    "efxmix",       // 43   EFFECT Wet/Dry Mix Adjust %                                        NA
    "efxctl1",      // 44   input 1                                                            NA
    "efxout1",      // 45   output MIDI ctl                                                    NA
    "efxctl2",      // 46   input 2                                                            NA
    "efxout2",      // 47   output MIDI ctl                                                    NA
    
    // fxrt KDFX
    "fxrtSegTag",   // 48   (layer format 2 & 3 starts here)
    "vers",         // 49       which slot      vers                                                   NA
    // "studiomsb", // 50
    // "studiolsb", // 51
    "studio",
    "---",          // 52   rfu[0]
    "---",          // 53   rfu[1]
    "---",          // 54   rfu[2]
    "---",          // 55   rfu[3]
    
    // patch[0] FXMOD1
    "fxptSegTag0",  // 56   fxptSegTag 105
    "num0",         // 57   which slot #, 1 if in use
    "strip0a0",     // 58   bus
    "fxparam0a3",   // 59   which param
    "fxparam0a1",
    "fxparam0a2",
    "fxparam0a4",
    "fxparam0a5",
    "fxparam0a6",
    "fxparam0a7",
    "fxparam0a8",
    "fxparam0a9",
    "fxparam0a10",
    "fxparam0a11",
    "fxparam0a12",
    "fxparam0a13",
    "fxparam0a14",
    "fxparam0a15",
    "fxparam0a16",
    "fxparam0a17",
    "fxparam0a18",
    "fxparam0a19",
    "fxparam0a20",
    "fxparam0a21",
    "fxparam0a22",
    "fxparam0a23",
    "fxparam0a24",
    "fxparam0a25",
    "fxparam0a26",
    "fxparam0a27",
    "fxparam0a28",
    "fxparam0a29",
    "fxparam0a30",
    "fxparam0a31",
    "fxparam0a32",
    "fxparam0a33",
    "fxparam0a34",
    "fxparam0a35",
    "fxparam0a36",
    "fxparam0a37",
    "fxparam0a38",
    "fxparam0a39",
    "fxparam0a40",
    "fxparam0a41",
    "fxparam0a42",
    "fxparam0a43",
    "fxparam0a44",
    "fxparam0a45",
    "fxparam0a46",
    "fxparam0a47",
    "fxparam0a48",
    "adjustg0",     // 60 
    "adjustfd0",
    "adjustf0",
    "adjustl0",
    "adjustp0",
    "source0",      // 61
    "depthg0",      // 62
    "depthf0",
    "depthl0",
    "depthp0",
    "---",          // 63   rfu
    
    // patch[1] FXMOD2
    "fxptSegTag1",  // 64   fxptSegTag 105
    "num1",         // 65   which slot #, 2 if in use
    "strip1a0",     // 66
    "fxparam1a3",   // 67
    "fxparam1a1",
    "fxparam1a2",
    "fxparam1a4",
    "fxparam1a5",
    "fxparam1a6",
    "fxparam1a7",
    "fxparam1a8",
    "fxparam1a9",
    "fxparam1a10",
    "fxparam1a11",
    "fxparam1a12",
    "fxparam1a13",
    "fxparam1a14",
    "fxparam1a15",
    "fxparam1a16",
    "fxparam1a17",
    "fxparam1a18",
    "fxparam1a19",
    "fxparam1a20",
    "fxparam1a21",
    "fxparam1a22",
    "fxparam1a23",
    "fxparam1a24",
    "fxparam1a25",
    "fxparam1a26",
    "fxparam1a27",
    "fxparam1a28",
    "fxparam1a29",
    "fxparam1a30",
    "fxparam1a31",
    "fxparam1a32",
    "fxparam1a33",
    "fxparam1a34",
    "fxparam1a35",
    "fxparam1a36",
    "fxparam1a37",
    "fxparam1a38",
    "fxparam1a39",
    "fxparam1a40",
    "fxparam1a41",
    "fxparam1a42",
    "fxparam1a43",
    "fxparam1a44",
    "fxparam1a45",
    "fxparam1a46",
    "fxparam1a47",
    "fxparam1a48",
    "adjustg1",     // 68
    "adjustfd1",
    "adjustf1",
    "adjustl1",
    "adjustp1",
    "source1",      // 69
    "depthg1",      // 70
    "depthf1",
    "depthl1",
    "depthp1",
    "---",          // 71   rfu
    
    // patch[2] FXMOD3
    "fxptSegTag2",  // 72   fxptSegTag 105
    "num2",         // 73
    "strip2a0",     // 74
    "fxparam2a3",   // 75
    "fxparam2a1",
    "fxparam2a2",
    "fxparam2a4",
    "fxparam2a5",
    "fxparam2a6",
    "fxparam2a7",
    "fxparam2a8",
    "fxparam2a9",
    "fxparam2a10",
    "fxparam2a11",
    "fxparam2a12",
    "fxparam2a13",
    "fxparam2a14",
    "fxparam2a15",
    "fxparam2a16",
    "fxparam2a17",
    "fxparam2a18",
    "fxparam2a19",
    "fxparam2a20",
    "fxparam2a21",
    "fxparam2a22",
    "fxparam2a23",
    "fxparam2a24",
    "fxparam2a25",
    "fxparam2a26",
    "fxparam2a27",
    "fxparam2a28",
    "fxparam2a29",
    "fxparam2a30",
    "fxparam2a31",
    "fxparam2a32",
    "fxparam2a33",
    "fxparam2a34",
    "fxparam2a35",
    "fxparam2a36",
    "fxparam2a37",
    "fxparam2a38",
    "fxparam2a39",
    "fxparam2a40",
    "fxparam2a41",
    "fxparam2a42",
    "fxparam2a43",
    "fxparam2a44",
    "fxparam2a45",
    "fxparam2a46",
    "fxparam2a47",
    "fxparam2a48",
    "adjustg2",     // 76
    "adjustfd2",
    "adjustf2",
    "adjustl2",
    "adjustp2",
    "source2",      // 77
    "depthg2",      // 78
    "depthf2",
    "depthl2",
    "depthp2",
    "---",          // 79
    
    // patch[3] FXMOD4
    "fxptSegTag3",  // 80   fxptSegTag 105
    "num3",         // 81
    "strip3a0",     // 82
    "fxparam3a3",   // 83
    "fxparam3a1",
    "fxparam3a2",
    "fxparam3a4",
    "fxparam3a5",
    "fxparam3a6",
    "fxparam3a7",
    "fxparam3a8",
    "fxparam3a9",
    "fxparam3a10",
    "fxparam3a11",
    "fxparam3a12",
    "fxparam3a13",
    "fxparam3a14",
    "fxparam3a15",
    "fxparam3a16",
    "fxparam3a17",
    "fxparam3a18",
    "fxparam3a19",
    "fxparam3a20",
    "fxparam3a21",
    "fxparam3a22",
    "fxparam3a23",
    "fxparam3a24",
    "fxparam3a25",
    "fxparam3a26",
    "fxparam3a27",
    "fxparam3a28",
    "fxparam3a29",
    "fxparam3a30",
    "fxparam3a31",
    "fxparam3a32",
    "fxparam3a33",
    "fxparam3a34",
    "fxparam3a35",
    "fxparam3a36",
    "fxparam3a37",
    "fxparam3a38",
    "fxparam3a39",
    "fxparam3a40",
    "fxparam3a41",
    "fxparam3a42",
    "fxparam3a43",
    "fxparam3a44",
    "fxparam3a45",
    "fxparam3a46",
    "fxparam3a47",
    "fxparam3a48",
    "adjustg3",     // 84
    "adjustfd3",
    "adjustf3",
    "adjustl3",
    "adjustp3",
    "source3",      // 85
    "depthg3",      // 86
    "depthf3",
    "depthl3",
    "depthp3",
    "---",          // 87
    
    // patch[4] FXMOD5
    "fxptSegTag4",  // 88   fxptSegTag 105
    "num4",         // 89
    "strip4a0",     // 90
    "fxparam4a3",   // 91
    "fxparam4a1",
    "fxparam4a2",
    "fxparam4a4",
    "fxparam4a5",
    "fxparam4a6",
    "fxparam4a7",
    "fxparam4a8",
    "fxparam4a9",
    "fxparam4a10",
    "fxparam4a11",
    "fxparam4a12",
    "fxparam4a13",
    "fxparam4a14",
    "fxparam4a15",
    "fxparam4a16",
    "fxparam4a17",
    "fxparam4a18",
    "fxparam4a19",
    "fxparam4a20",
    "fxparam4a21",
    "fxparam4a22",
    "fxparam4a23",
    "fxparam4a24",
    "fxparam4a25",
    "fxparam4a26",
    "fxparam4a27",
    "fxparam4a28",
    "fxparam4a29",
    "fxparam4a30",
    "fxparam4a31",
    "fxparam4a32",
    "fxparam4a33",
    "fxparam4a34",
    "fxparam4a35",
    "fxparam4a36",
    "fxparam4a37",
    "fxparam4a38",
    "fxparam4a39",
    "fxparam4a40",
    "fxparam4a41",
    "fxparam4a42",
    "fxparam4a43",
    "fxparam4a44",
    "fxparam4a45",
    "fxparam4a46",
    "fxparam4a47",
    "fxparam4a48",
    "adjustg4",     // 92
    "adjustfd4",
    "adjustf4",
    "adjustl4",
    "adjustp4",
    "source4",      // 93
    "depthg4",      // 94
    "depthf4",
    "depthl4",
    "depthp4",
    "---",          // 95
    
    // patch[5] FXMOD6
    "fxptSegTag5",  // 96   fxptSegTag 105
    "num5",         // 97
    "strip5a0",     // 98
    "fxparam5a3",   // 99
    "fxparam5a1",
    "fxparam5a2",
    "fxparam5a4",
    "fxparam5a5",
    "fxparam5a6",
    "fxparam5a7",
    "fxparam5a8",
    "fxparam5a9",
    "fxparam5a10",
    "fxparam5a11",
    "fxparam5a12",
    "fxparam5a13",
    "fxparam5a14",
    "fxparam5a15",
    "fxparam5a16",
    "fxparam5a17",
    "fxparam5a18",
    "fxparam5a19",
    "fxparam5a20",
    "fxparam5a21",
    "fxparam5a22",
    "fxparam5a23",
    "fxparam5a24",
    "fxparam5a25",
    "fxparam5a26",
    "fxparam5a27",
    "fxparam5a28",
    "fxparam5a29",
    "fxparam5a30",
    "fxparam5a31",
    "fxparam5a32",
    "fxparam5a33",
    "fxparam5a34",
    "fxparam5a35",
    "fxparam5a36",
    "fxparam5a37",
    "fxparam5a38",
    "fxparam5a39",
    "fxparam5a40",
    "fxparam5a41",
    "fxparam5a42",
    "fxparam5a43",
    "fxparam5a44",
    "fxparam5a45",
    "fxparam5a46",
    "fxparam5a47",
    "fxparam5a48",
    "adjustg5",     // 100
    "adjustfd5",
    "adjustf5",
    "adjustl5",
    "adjustp5",
    "source5",      // 101
    "depthg5",      // 102
    "depthf5",
    "depthl5",
    "depthp5",
    "---",          // 103
    
    // patch[6] FXMOD7
    "fxptSegTag6",  // 104  fxptSegTag 105
    "num6",         // 105
    "strip6a0",     // 106
    "fxparam6a3",   // 107
    "fxparam6a1",
    "fxparam6a2",
    "fxparam6a4",
    "fxparam6a5",
    "fxparam6a6",
    "fxparam6a7",
    "fxparam6a8",
    "fxparam6a9",
    "fxparam6a10",
    "fxparam6a11",
    "fxparam6a12",
    "fxparam6a13",
    "fxparam6a14",
    "fxparam6a15",
    "fxparam6a16",
    "fxparam6a17",
    "fxparam6a18",
    "fxparam6a19",
    "fxparam6a20",
    "fxparam6a21",
    "fxparam6a22",
    "fxparam6a23",
    "fxparam6a24",
    "fxparam6a25",
    "fxparam6a26",
    "fxparam6a27",
    "fxparam6a28",
    "fxparam6a29",
    "fxparam6a30",
    "fxparam6a31",
    "fxparam6a32",
    "fxparam6a33",
    "fxparam6a34",
    "fxparam6a35",
    "fxparam6a36",
    "fxparam6a37",
    "fxparam6a38",
    "fxparam6a39",
    "fxparam6a40",
    "fxparam6a41",
    "fxparam6a42",
    "fxparam6a43",
    "fxparam6a44",
    "fxparam6a45",
    "fxparam6a46",
    "fxparam6a47",
    "fxparam6a48",
    "adjustg6",     // 108
    "adjustfd6",
    "adjustf6",
    "adjustl6",
    "adjustp6",
    "source6",      // 109
    "depthg6",      // 110
    "depthf6",
    "depthl6",
    "depthp6",
    "---",          // 111
    
    // patch[7] FXMOD8
    "fxptSegTag7",  // 112  fxptSegTag 105
    "num7",         // 113
    "strip7a0",     // 114
    "fxparam7a3",   // 115
    "fxparam7a1",
    "fxparam7a2",
    "fxparam7a4",
    "fxparam7a5",
    "fxparam7a6",
    "fxparam7a7",
    "fxparam7a8",
    "fxparam7a9",
    "fxparam7a10",
    "fxparam7a11",
    "fxparam7a12",
    "fxparam7a13",
    "fxparam7a14",
    "fxparam7a15",
    "fxparam7a16",
    "fxparam7a17",
    "fxparam7a18",
    "fxparam7a19",
    "fxparam7a20",
    "fxparam7a21",
    "fxparam7a22",
    "fxparam7a23",
    "fxparam7a24",
    "fxparam7a25",
    "fxparam7a26",
    "fxparam7a27",
    "fxparam7a28",
    "fxparam7a29",
    "fxparam7a30",
    "fxparam7a31",
    "fxparam7a32",
    "fxparam7a33",
    "fxparam7a34",
    "fxparam7a35",
    "fxparam7a36",
    "fxparam7a37",
    "fxparam7a38",
    "fxparam7a39",
    "fxparam7a40",
    "fxparam7a41",
    "fxparam7a42",
    "fxparam7a43",
    "fxparam7a44",
    "fxparam7a45",
    "fxparam7a46",
    "fxparam7a47",
    "fxparam7a48",
    "adjustg7",     // 116
    "adjustfd7",
    "adjustf7",
    "adjustl7",
    "adjustp7",
    "source7",      // 117
    "depthg7",      // 118
    "depthf7",
    "depthl7",
    "depthp7",
    "---",          // 119
    
    // patch[8] FXMOD9
    "fxptSegTag8",  // 120  105
    "num8",         // 121
    "strip8a0",     // 122
    "fxparam8a3",   // 123
    "fxparam8a1",
    "fxparam8a2",
    "fxparam8a4",
    "fxparam8a5",
    "fxparam8a6",
    "fxparam8a7",
    "fxparam8a8",
    "fxparam8a9",
    "fxparam8a10",
    "fxparam8a11",
    "fxparam8a12",
    "fxparam8a13",
    "fxparam8a14",
    "fxparam8a15",
    "fxparam8a16",
    "fxparam8a17",
    "fxparam8a18",
    "fxparam8a19",
    "fxparam8a20",
    "fxparam8a21",
    "fxparam8a22",
    "fxparam8a23",
    "fxparam8a24",
    "fxparam8a25",
    "fxparam8a26",
    "fxparam8a27",
    "fxparam8a28",
    "fxparam8a29",
    "fxparam8a30",
    "fxparam8a31",
    "fxparam8a32",
    "fxparam8a33",
    "fxparam8a34",
    "fxparam8a35",
    "fxparam8a36",
    "fxparam8a37",
    "fxparam8a38",
    "fxparam8a39",
    "fxparam8a40",
    "fxparam8a41",
    "fxparam8a42",
    "fxparam8a43",
    "fxparam8a44",
    "fxparam8a45",
    "fxparam8a46",
    "fxparam8a47",
    "fxparam8a48",
    "adjustg8",     // 124
    "adjustfd8",
    "adjustf8",
    "adjustl8",
    "adjustp8",
    "source8",      // 125
    "depthg8",      // 126
    "depthf8",
    "depthl8",
    "depthp8",
    "---",          // 127
    
    // patch[9] FXMOD10
    "fxptSegTag9",  // 128  fxptSegTag 105
    "num9",         // 129
    "strip9a0",     // 130
    "fxparam9a3",   // 131
    "fxparam9a1",
    "fxparam9a2",
    "fxparam9a4",
    "fxparam9a5",
    "fxparam9a6",
    "fxparam9a7",
    "fxparam9a8",
    "fxparam9a9",
    "fxparam9a10",
    "fxparam9a11",
    "fxparam9a12",
    "fxparam9a13",
    "fxparam9a14",
    "fxparam9a15",
    "fxparam9a16",
    "fxparam9a17",
    "fxparam9a18",
    "fxparam9a19",
    "fxparam9a20",
    "fxparam9a21",
    "fxparam9a22",
    "fxparam9a23",
    "fxparam9a24",
    "fxparam9a25",
    "fxparam9a26",
    "fxparam9a27",
    "fxparam9a28",
    "fxparam9a29",
    "fxparam9a30",
    "fxparam9a31",
    "fxparam9a32",
    "fxparam9a33",
    "fxparam9a34",
    "fxparam9a35",
    "fxparam9a36",
    "fxparam9a37",
    "fxparam9a38",
    "fxparam9a39",
    "fxparam9a40",
    "fxparam9a41",
    "fxparam9a42",
    "fxparam9a43",
    "fxparam9a44",
    "fxparam9a45",
    "fxparam9a46",
    "fxparam9a47",
    "fxparam9a48",
    "adjustg9",     // 132
    "adjustfd9",
    "adjustf9",
    "adjustl9",
    "adjustp9",
    "source9",      // 133
    "depthg9",      // 134
    "depthf9",
    "depthl9",
    "depthp9",
    "---",          // 135
    
    // patch[10] FXMOD11
    "fxptSegTag10", // 136  fxptSegTag 105
    "num10",        // 137
    "strip10a0",    // 138
    "fxparam10a3",  // 139
    "fxparam10a1",
    "fxparam10a2",
    "fxparam10a4",
    "fxparam10a5",
    "fxparam10a6",
    "fxparam10a7",
    "fxparam10a8",
    "fxparam10a9",
    "fxparam10a10",
    "fxparam10a11",
    "fxparam10a12",
    "fxparam10a13",
    "fxparam10a14",
    "fxparam10a15",
    "fxparam10a16",
    "fxparam10a17",
    "fxparam10a18",
    "fxparam10a19",
    "fxparam10a20",
    "fxparam10a21",
    "fxparam10a22",
    "fxparam10a23",
    "fxparam10a24",
    "fxparam10a25",
    "fxparam10a26",
    "fxparam10a27",
    "fxparam10a28",
    "fxparam10a29",
    "fxparam10a30",
    "fxparam10a31",
    "fxparam10a32",
    "fxparam10a33",
    "fxparam10a34",
    "fxparam10a35",
    "fxparam10a36",
    "fxparam10a37",
    "fxparam10a38",
    "fxparam10a39",
    "fxparam10a40",
    "fxparam10a41",
    "fxparam10a42",
    "fxparam10a43",
    "fxparam10a44",
    "fxparam10a45",
    "fxparam10a46",
    "fxparam10a47",
    "fxparam10a48",
    "adjustg10",    // 140
    "adjustfd10",
    "adjustf10",
    "adjustl10",
    "adjustp10",
    "source10",     // 141
    "depthg10",     // 142
    "depthf10",
    "depthl10",
    "depthp10",
    "---",          // 143
    
    // patch[11] FXMOD12
    "fxptSegTag11", // 144  fxptSegTag 105
    "num11",        // 145
    "strip11a0",    // 146
    "fxparam11a3",  // 147
    "fxparam11a1",
    "fxparam11a2",
    "fxparam11a4",
    "fxparam11a5",
    "fxparam11a6",
    "fxparam11a7",
    "fxparam11a8",
    "fxparam11a9",
    "fxparam11a10",
    "fxparam11a11",
    "fxparam11a12",
    "fxparam11a13",
    "fxparam11a14",
    "fxparam11a15",
    "fxparam11a16",
    "fxparam11a17",
    "fxparam11a18",
    "fxparam11a19",
    "fxparam11a20",
    "fxparam11a21",
    "fxparam11a22",
    "fxparam11a23",
    "fxparam11a24",
    "fxparam11a25",
    "fxparam11a26",
    "fxparam11a27",
    "fxparam11a28",
    "fxparam11a29",
    "fxparam11a30",
    "fxparam11a31",
    "fxparam11a32",
    "fxparam11a33",
    "fxparam11a34",
    "fxparam11a35",
    "fxparam11a36",
    "fxparam11a37",
    "fxparam11a38",
    "fxparam11a39",
    "fxparam11a40",
    "fxparam11a41",
    "fxparam11a42",
    "fxparam11a43",
    "fxparam11a44",
    "fxparam11a45",
    "fxparam11a46",
    "fxparam11a47",
    "fxparam11a48",
    "adjustg11",    // 148
    "adjustfd11",
    "adjustf11",
    "adjustl11",
    "adjustp11",
    "source11",     // 149
    "depthg11",     // 150
    "depthf11",
    "depthl11",
    "depthp11",
    "---",          // 151
    
    // patch[12] FXMOD13
    "fxptSegTag12", // 152  fxptSegTag 105
    "num12",        // 153
    "strip12a0",    // 154
    "fxparam12a3",  // 155
    "fxparam12a1",
    "fxparam12a2",
    "fxparam12a4",
    "fxparam12a5",
    "fxparam12a6",
    "fxparam12a7",
    "fxparam12a8",
    "fxparam12a9",
    "fxparam12a10",
    "fxparam12a11",
    "fxparam12a12",
    "fxparam12a13",
    "fxparam12a14",
    "fxparam12a15",
    "fxparam12a16",
    "fxparam12a17",
    "fxparam12a18",
    "fxparam12a19",
    "fxparam12a20",
    "fxparam12a21",
    "fxparam12a22",
    "fxparam12a23",
    "fxparam12a24",
    "fxparam12a25",
    "fxparam12a26",
    "fxparam12a27",
    "fxparam12a28",
    "fxparam12a29",
    "fxparam12a30",
    "fxparam12a31",
    "fxparam12a32",
    "fxparam12a33",
    "fxparam12a34",
    "fxparam12a35",
    "fxparam12a36",
    "fxparam12a37",
    "fxparam12a38",
    "fxparam12a39",
    "fxparam12a40",
    "fxparam12a41",
    "fxparam12a42",
    "fxparam12a43",
    "fxparam12a44",
    "fxparam12a45",
    "fxparam12a46",
    "fxparam12a47",
    "fxparam12a48",
    "adjustg12",    // 156
    "adjustfd12",
    "adjustf12",
    "adjustl12",
    "adjustp12",
    "source12",     // 157
    "depthg12",     // 158
    "depthf12",
    "depthl12",
    "depthp12",
    "---",          // 159
    
    // patch[13] FXMOD14
    "fxptSegTag13", // 160  fxptSegTag 105
    "num13",        // 161
    "strip13a0",    // 162
    "fxparam13a3",  // 163
    "fxparam13a1",
    "fxparam13a2",
    "fxparam13a4",
    "fxparam13a5",
    "fxparam13a6",
    "fxparam13a7",
    "fxparam13a8",
    "fxparam13a9",
    "fxparam13a10",
    "fxparam13a11",
    "fxparam13a12",
    "fxparam13a13",
    "fxparam13a14",
    "fxparam13a15",
    "fxparam13a16",
    "fxparam13a17",
    "fxparam13a18",
    "fxparam13a19",
    "fxparam13a20",
    "fxparam13a21",
    "fxparam13a22",
    "fxparam13a23",
    "fxparam13a24",
    "fxparam13a25",
    "fxparam13a26",
    "fxparam13a27",
    "fxparam13a28",
    "fxparam13a29",
    "fxparam13a30",
    "fxparam13a31",
    "fxparam13a32",
    "fxparam13a33",
    "fxparam13a34",
    "fxparam13a35",
    "fxparam13a36",
    "fxparam13a37",
    "fxparam13a38",
    "fxparam13a39",
    "fxparam13a40",
    "fxparam13a41",
    "fxparam13a42",
    "fxparam13a43",
    "fxparam13a44",
    "fxparam13a45",
    "fxparam13a46",
    "fxparam13a47",
    "fxparam13a48",
    "adjustg13",    // 164
    "adjustfd13",
    "adjustf13",
    "adjustl13",
    "adjustp13",
    "source13",         // 165
    "depthg13",     // 166
    "depthf13",
    "depthl13",
    "depthp13",
    "---",          // 167
    
    // patch[14] FXMOD15
    "fxptSegTag14", // 168  fxptSegTag 105
    "num14",        // 169
    "strip14a0",    // 170
    "fxparam14a3",  // 171
    "fxparam14a1",
    "fxparam14a2",
    "fxparam14a4",
    "fxparam14a5",
    "fxparam14a6",
    "fxparam14a7",
    "fxparam14a8",
    "fxparam14a9",
    "fxparam14a10",
    "fxparam14a11",
    "fxparam14a12",
    "fxparam14a13",
    "fxparam14a14",
    "fxparam14a15",
    "fxparam14a16",
    "fxparam14a17",
    "fxparam14a18",
    "fxparam14a19",
    "fxparam14a20",
    "fxparam14a21",
    "fxparam14a22",
    "fxparam14a23",
    "fxparam14a24",
    "fxparam14a25",
    "fxparam14a26",
    "fxparam14a27",
    "fxparam14a28",
    "fxparam14a29",
    "fxparam14a30",
    "fxparam14a31",
    "fxparam14a32",
    "fxparam14a33",
    "fxparam14a34",
    "fxparam14a35",
    "fxparam14a36",
    "fxparam14a37",
    "fxparam14a38",
    "fxparam14a39",
    "fxparam14a40",
    "fxparam14a41",
    "fxparam14a42",
    "fxparam14a43",
    "fxparam14a44",
    "fxparam14a45",
    "fxparam14a46",
    "fxparam14a47",
    "fxparam14a48",
    "adjustg14",    // 172
    "adjustfd14",
    "adjustf14",
    "adjustl14",
    "adjustp14",
    "source14",     // 173
    "depthg14",     // 174
    "depthf14",
    "depthl14",
    "depthp14",
    "---",          // 175
    
    // patch[15] FXMOD16
    "fxptSegTag15", // 176  fxptSegTag 105
    "num15",        // 177
    "strip15a0",    // 178
    "fxparam15a3",  // 179
    "fxparam15a1",
    "fxparam15a2",
    "fxparam15a4",
    "fxparam15a5",
    "fxparam15a6",
    "fxparam15a7",
    "fxparam15a8",
    "fxparam15a9",
    "fxparam15a10",
    "fxparam15a11",
    "fxparam15a12",
    "fxparam15a13",
    "fxparam15a14",
    "fxparam15a15",
    "fxparam15a16",
    "fxparam15a17",
    "fxparam15a18",
    "fxparam15a19",
    "fxparam15a20",
    "fxparam15a21",
    "fxparam15a22",
    "fxparam15a23",
    "fxparam15a24",
    "fxparam15a25",
    "fxparam15a26",
    "fxparam15a27",
    "fxparam15a28",
    "fxparam15a29",
    "fxparam15a30",
    "fxparam15a31",
    "fxparam15a32",
    "fxparam15a33",
    "fxparam15a34",
    "fxparam15a35",
    "fxparam15a36",
    "fxparam15a37",
    "fxparam15a38",
    "fxparam15a39",
    "fxparam15a40",
    "fxparam15a41",
    "fxparam15a42",
    "fxparam15a43",
    "fxparam15a44",
    "fxparam15a45",
    "fxparam15a46",
    "fxparam15a47",
    "fxparam15a48",
    "adjustg15",    // 180
    "adjustfd15",
    "adjustf15",
    "adjustl15",
    "adjustp15",
    "source15",     // 181
    "depthg15",     // 182
    "depthf15",
    "depthl15",
    "depthp15",
    "---",          // 183
    
    // patch[16] FXMOD17
    "fxptSegTag16", // 184  fxptSegTag 105
    "num16",        // 185
    "strip16a0",    // 186
    "fxparam16a3",  // 187
    "fxparam16a1",
    "fxparam16a2",
    "fxparam16a4",
    "fxparam16a5",
    "fxparam16a6",
    "fxparam16a7",
    "fxparam16a8",
    "fxparam16a9",
    "fxparam16a10",
    "fxparam16a11",
    "fxparam16a12",
    "fxparam16a13",
    "fxparam16a14",
    "fxparam16a15",
    "fxparam16a16",
    "fxparam16a17",
    "fxparam16a18",
    "fxparam16a19",
    "fxparam16a20",
    "fxparam16a21",
    "fxparam16a22",
    "fxparam16a23",
    "fxparam16a24",
    "fxparam16a25",
    "fxparam16a26",
    "fxparam16a27",
    "fxparam16a28",
    "fxparam16a29",
    "fxparam16a30",
    "fxparam16a31",
    "fxparam16a32",
    "fxparam16a33",
    "fxparam16a34",
    "fxparam16a35",
    "fxparam16a36",
    "fxparam16a37",
    "fxparam16a38",
    "fxparam16a39",
    "fxparam16a40",
    "fxparam16a41",
    "fxparam16a42",
    "fxparam16a43",
    "fxparam16a44",
    "fxparam16a45",
    "fxparam16a46",
    "fxparam16a47",
    "fxparam16a48",
    "adjustg16",    // 188
    "adjustfd16",
    "adjustf16",
    "adjustl16",
    "adjustp16",
    "source16",     // 189
    "depthg16",     // 190
    "depthf16",
    "depthl16",
    "depthp16",
    "---",          // 191
    
    // patch[17] FXMOD18
    "fxptSegTag17", // 192  fxptSegTag 105
    "num17",        // 193
    "strip17a0",    // 194
    "fxparam17a3",  // 195
    "fxparam17a1",
    "fxparam17a2",
    "fxparam17a4",
    "fxparam17a5",
    "fxparam17a6",
    "fxparam17a7",
    "fxparam17a8",
    "fxparam17a9",
    "fxparam17a10",
    "fxparam17a11",
    "fxparam17a12",
    "fxparam17a13",
    "fxparam17a14",
    "fxparam17a15",
    "fxparam17a16",
    "fxparam17a17",
    "fxparam17a18",
    "fxparam17a19",
    "fxparam17a20",
    "fxparam17a21",
    "fxparam17a22",
    "fxparam17a23",
    "fxparam17a24",
    "fxparam17a25",
    "fxparam17a26",
    "fxparam17a27",
    "fxparam17a28",
    "fxparam17a29",
    "fxparam17a30",
    "fxparam17a31",
    "fxparam17a32",
    "fxparam17a33",
    "fxparam17a34",
    "fxparam17a35",
    "fxparam17a36",
    "fxparam17a37",
    "fxparam17a38",
    "fxparam17a39",
    "fxparam17a40",
    "fxparam17a41",
    "fxparam17a42",
    "fxparam17a43",
    "fxparam17a44",
    "fxparam17a45",
    "fxparam17a46",
    "fxparam17a47",
    "fxparam17a48",
    "adjustg17",    // 196
    "adjustfd17",
    "adjustf17",
    "adjustl17",
    "adjustp17",
    "source17",     // 197
    "depthg17",     // 198
    "depthf17",
    "depthl17",
    "depthp17",
    "---",          // 199
    
    // kdfx asr1 FASR1
    "kasr1SegTag",  // 200  asrSegTag : 16
    "---",          // 201  rfu1
    "kasr1trigger", // 202  trig
    "kasr1flags",   // 203  mode
    "kasr1dtime",   // 204  dly
    "kasr1atime",   // 205  atk
    "---",          // 206  rfu2
    "kasr1rtime",   // 207  rls
    
    // kdfx asr2 FASR2
    "kasr2SegTag",  // 208  asrSegTag + 1 : 17
    "---",          // 209
    "kasr2trigger", // 210
    "kasr2flags",   // 211
    "kasr2dtime",   // 212
    "kasr2atime",   // 213
    "---",          // 214
    "kasr2rtime",   // 215
    
    // kdfx fcn1 FFUN1
    "kfcn1SegTag",  // 216  fcnSegTag : 24
    "kfcn1op",      // 217
    "kfcn1arg1",    // 218
    "kfcn1arg2",    // 219
    
    // kdfx fcn2 FFUN2
    "kfcn2SegTag",  // 220  fcnSegTag + 1 : 25
    "kfcn2op",      // 221
    "kfcn2arg1",    // 222
    "kfcn2arg2",    // 223
    
    // kdfx lfo1 FLFO1
    "klfo1SegTag",  // 224  lfoSegTag : 20
    "---",          // 225  rfu1
    "klfo1ratectl", // 226  rate ctl
    "klfo1minrate", // 227  min rate
    "klfo1maxrate", // 228  max rate
    "klfo1phase",   // 229  phase
    "klfo1shape",   // 230  shape
    "---",          // 231  rfu2
    
    // kdfx lfo2 FLFO2
    "klfo2SegTag",  // 232  lfoSegTag : 20
    "---",          // 233
    "klfo2ratectl", // 234
    "klfo2minrate", // 235
    "klfo2maxrate", // 236
    "klfo2phase",   // 237
    "klfo2shape",   // 238
    "---",          // 239
    
    // kdfx fcn3 FFUN3
    "kfcn3SegTag",  // 240  fcnSegTag + 2 : 26
    "kfcn3op",      // 241
    "kfcn3arg1",    // 242
    "kfcn3arg2",    // 243
    
    // kdfx fcn4 FFUN4
    "kfcn4SegTag",  // 244  fcnSegTag + 3 : 27
    "kfcn4op",      // 245
    "kfcn4arg1",    // 246
    "kfcn4arg2",    // 247
    
    // hammseg KB3
    "hammSegTag1",  // 248  120 (KB3_FIRST_DATA) (kb3_first_param)
    "hammversion",  // 249  version
    "basekey",      // 250  key # of base tone wheel
    "wheels",       // 251  # tone wheels
    "kb3flags",     // 252                                                                TODO : check
    "toneleak",     // 253  tone wheel leakage level
    "resmap",       // 254  keyboard resistor map #
    "bartones0",    // 255  draw bar tuning (semis) 
    "bartones1",    // 256
    "bartones2",    // 257
    "bartones3",    // 258
    "bartones4",    // 259
    "bartones5",    // 260
    "bartones6",    // 261
    "bartones7",    // 262
    "bartones8",    // 263
    "volmap",       // 264  tone wheel volume map #
    "volume",       // 265  tone wheel volume adjust
    "balance",      // 266  tone wheel hi/low balance
    "emphg",        // 267  preemphasis
    "emphf",        // 268  preemphasis
    "bassg",        // 269  bass shelf
    "bassf",        // 270  bass shelf
    "par1g",        // 271  parametric
    "par1f",        // 272  parametric
    "par1q",        // 273  parametric
    "par2g",        // 274  parametric
    "par2f",        // 275  parametric
    "par2q",        // 276  parametric
    "trebg",        // 277  treble shelf
    "trebf",        // 278  treble shelf
    "noizvol",      // 279  noise volume adjust
    
    "hammSegTag2",  // 280  121
    "noizpitch",    // 281  noise pitch
    "noizlevel",    // 282  noise level (dB/10)
    "noizdecay",    // 283  noise decay (enum)
    "percflags",    // 284
    "percbar",      // 285
    "noizvel",      // 286
    "percvel",      // 287
    "perclevel0",   // 288  percussion levels (dB/10)
    "perclevel1",   // 289  percussion levels (dB/10)
    "perclevel2",   // 290  percussion levels (dB/10)
    "perclevel3",   // 291  percussion levels (dB/10)
    "percdecay0",   // 292  percussion decays (enum)
    "percdecay1",   // 293  percussion decays (enum)
    "percdecay2",   // 294  percussion decays (enum)
    "percdecay3",   // 295  percussion decays (enum)
    "perclcomp0",   // 296  loud/soft compensation (db/10)
    "perclcomp1",   // 297  loud/soft compensation (db/10)
    "perclcomp2",   // 298  loud/soft compensation (db/10)
    "perclcomp3",   // 299  loud/soft compensation (db/10)
    "---",          // 300  rfu2[0]
    "---",          // 301  rfu2[1]
    "---",          // 302  rfu2[2]
    "---",          // 303  rfu2[2]
    "bassq",        // 304
    "trebq",        // 305
    "noiztrig",     // 306  noise re-trigger threshold
    "leakmode",     // 307  leakage mode
    "lesliectl",    // 308
    "chorusctl",    // 309
    "chorusselect", // 310
    "---",          // 311  rfu3
    
    "hammSegTag3",  // 312  122
    "drawbars0",        // 313
    "drawbars1",        // 314
    "drawbars2",        // 315
    "drawbars3",        // 316
    "drawbars4",        // 317
    "drawbars5",        // 318
    "drawbars6",        // 319
    "drawbars7",        // 320
    "drawbars8",        // 321
    "---",          // 322  rfu6
    "---",          // 323  rfu7
    "keyclick",     // 324
    "relclick",     // 325
    "noizran",      // 326
    "---",          // 327  rfu4                                                         TODO : check
    "lotune",       // 328  lowest pitch ?
    "rcvmap",       // 329  0 = Kurzweil, 1 = Voce
    "---",          // 330  rfu5[0]
    "---",          // 331  rfu5[1]
    "---",          // 332  rfu5[2]
    "---",          // 333  rfu5[3]
    "---",          // 334  rfu5[4]
    "---",          // 335  rfu5[5]
    "---",          // 336  rfu5[6]
    "---",          // 337  rfu5[7]
    "---",          // 338  rfu5[8]
    "---",          // 339  rfu5[9]
    "---",          // 340  rfu5[10]
    "---",          // 341  rfu5[11]
    "---",          // 342  rfu5[12]
    "---",          // 343  rfu5[13]
    
    // layer lyr    // TODO : if not used param, set immutable
    "lyrSegTag",    // 344  9 (layer format 4 starts here)
    "loenable",     // 345
    "lyrtrans",     // 346                                                               TODO : check
    "tune",         // 347                                                               TODO : check
    "lokey",        // 348
    "hikey",        // 349
    // "vrange"     // 350      lo vel/hi vel
    "lovel",
    "hivel",
    "eswitch",      // 351  LAYER enable (Ctl-list)
    // "lyrflags",  // 352  LAYER sust/sost/freez/ign/rls/til/dec/thr attk (see bitmaps)
    "suspdl",
    "sospdl",
    "frzpdl",
    "ignrel",
    "thratt",
    "tildec",
    // "moreflags", // 353  LAYER pbend mode/EnableS/opaque/KEYMAP stereo/OUTPUT/xfade sense
    "pbmode",
    "trig",
    "ensense",
    "opaque",
    "stereoflag",
    "xfsense",
    // "vtrig",     // 354
    "n0vtriglevel",
    "n0vtrigsense",
    "n1vtriglevel",
    "n1vtrigsense",
    "hienable",     // 355 
    "dlyctl",       // 356  delay ctl
    "dlymin",       // 357  min delay
    "dlymax",       // 358  max delay
    "xfade",        // 359  OUTPUT xfade (Ctl-list)
    
    // asr1 ASR1
    "asr1SegTag",   // 360  asrSegTag : 16
    "---",          // 361  rfu1
    "asr1trigger",  // 362  trig
    "asr1flags",    // 363  mode
    "asr1dtime",    // 364  dly
    "asr1atime",    // 365  atk
    "---",          // 366  rfu2
    "asr1rtime",    // 367  rls
    
    // asr2 ASR2
    "asr2SegTag",   // 368  asrSegTag + 1 : 17
    "---",          // 369
    "asr2trigger",  // 370
    "asr2flags",    // 371
    "asr2dtime",    // 372
    "asr2atime",    // 373
    "---",          // 374
    "asr2rtime",    // 375
    
    // lfcn1 FUN1
    "fcn1SegTag",   // 376  555  fcnSegTag : 24
    "fcn1op",       // 377
    "fcn1arg1",     // 378  input A
    "fcn1arg2",     // 379  input B
    
    // fcn2 FUN2
    "fcn2SegTag",   // 380  559  fcnSegTag + 1 : 25
    "fcn2op",       // 381
    "fcn2arg1",     // 382
    "fcn2arg2",     // 383
    
    // lfo1 LFO1
    "lfo1SegTag",   // 384  563  lfoSegTag : 20
    "---",          // 385  rfu1
    "lfo1ratectl",  // 386  rate ctl
    "lfo1minrate",  // 387  min rate
    "lfo1maxrate",  // 388  max rate
    "lfo1phase",    // 389  phase
    "lfo1shape",    // 390  shape
    "---",          // 391  rfu2
    
    // lfo2 LFO2
    "lfo2SegTag",   // 392  571  lfoSegTag + 1: 21
    "---",          // 393
    "lfo2ratectl",  // 394
    "lfo2minrate",  // 395
    "lfo2maxrate",  // 396
    "lfo2phase",    // 397
    "lfo2shape",    // 398
    "---",          // 399
    
    // fcn3 FUN3
    "fcn3SegTag",   // 400  579 fcnSegTag + 2 : 26
    "fcn3op",       // 401
    "fcn3arg1",     // 402
    "fcn3arg2",     // 403
    
    // fcn3 FUN4
    "fcn4SegTag",   // 404  583  fcnSegTag + 3 : 27
    "fcn4op",       // 405
    "fcn4arg1",     // 406
    "fcn4arg2",     // 407
    
    // enc enc
    "encSegTag",    // 408  587 encSegTag : 32
    "---",          // 409  rfu1
    "encflags",     // 410  natural / user
    "attime",       // 411
    "atkscale",     // 412
    "atvscale",     // 413
    "atctl",        // 414
    "atrange",      // 415
    "dttime",       // 416
    "dtkscale",     // 417
    "dtctl",        // 418
    "dtrange",      // 419
    "rttime",       // 420
    "rtkscale",     // 421
    "rtctl",        // 422
    "rtrange",      // 423
    
    // enc atk (impact) not on K2000
    "atkencSegTag", // 424  603 encSegTag : 39
    "---",          // 425  rfu1
    "atkencflags",  // 426
    "atkattime",    // 427
    "atkatkscale",  // 428
    "atkatvscale",  // 429
    "atkatctl",     // 430
    "atkatrange",   // 431
    "atkdttime",    // 432                                                                     NA
    "atkdtkscale",  // 433                                                                     NA
    "atkdtctl",     // 434                                                                     NA
    "atkdtrange",   // 435                                                                     NA
    "atkrttime",    // 436                                                                     NA
    "atkrtkscale",  // 437                                                                     NA
    "atkrtctl",     // 438                                                                     NA
    "atkrtrange",   // 439                                                                     NA
    
    // env AMPENV
    "env1SegTag",   // 440  envSegTag : 33
    // "env1flags", // 441  LOOP seg-X (2 parms)
    "env1looptype", //
    "env1numloops", //
    "env1att1",     // 442
    "env1att1time", // 443
    "env1att2",     // 444
    "env1att2time", // 445
    "env1att3",     // 446
    "env1att3time", // 447
    "env1dec1",     // 448
    "env1dec1time", // 449
    "env1rel1",     // 450
    "env1rel1time", // 451
    "env1rel2",     // 452
    "env1rel2time", // 453
    "env1rel3",     // 454
    "env1rel3time", // 455
    
    // env ENV2
    "env2SegTag",   // 456  envSegTag + 1 : 34
    // "env2flags", // 457  LOOP seg-X (2 parms)
    "env2looptype", //
    "env2numloops", //
    "env2att1",     // 458
    "env2att1time", // 459
    "env2att2",     // 460
    "env2att2time", // 461
    "env2att3",     // 462
    "env2att3time", // 462
    "env2dec1",     // 464
    "env2dec1time", // 465
    "env2rel1",     // 466
    "env2rel1time", // 467
    "env2rel2",     // 468
    "env2rel2time", // 469
    "env2rel3",     // 470
    "env2rel3time", // 471
    
    // env ENV3
    "env3SegTag",   // 472  envSegTag + 2 : 35
    // "env3flags", // 473  LOOP seg-X (2 parms)
    "env3looptype", //
    "env3numloops", //
    "env3att1",     // 474
    "env3att1time", // 475
    "env3att2",     // 476
    "env3att2time", // 477
    "env3att3",     // 478
    "env3att3time", // 479
    "env3dec1",     // 480
    "env3dec1time", // 481
    "env3rel1",     // 482
    "env3rel1time", // 483
    "env3rel2",     // 484
    "env3rel2time", // 485
    "env3rel3",     // 486
    "env3rel3time", // 487
    
    // cseg
    "calSegTag",    // 488  calSegTag : 64
    "calsubSegTag",     // 489  subTag                                                       TODO : check
    // "caltrans",  // 490  KEYMAP Transpose/Timbre shift
    "transpose",    //
    "caldtune",     // 491                                                               TODO : check
    "caltkscale",   // 492  KEYPMAP key track
    "caltvscale",   // 493  KEYPMAP vel track
    "caltcontrol",  // 494                                                               TODO : check
    "caltrange",    // 495                                                               TODO : check
    // "calskeymapmsb",// 496   the other keymap for stereo operation
    // "calskeymaplsb",// 497
    "keymap2",      //
    "calsroot",     // 498                                                               TODO : check
    // "calslegato",// 499
    "sampleskipping",//
    "altmethod",    //
    "playbackmode", //
    // "calkeymapmsb",// 500
    // "calkeymaplsb",// 501
    "keymap",       //
    "calroot",      // 502                                                               TODO : check
    "callegato",    // 503  KEYPMAP AltSwitch Ctl (Ctl-list) (legato ?)
    "caltshift",    // 504  KEYPMAP timbre shift
    "---",          // 505  rfu2
    "calcpitch",    // 506  PITCH coarse
    "calfpitch",    // 507  PITCH fine
    "calckscale",   // 508  PITCH key track
    "calcvscale",   // 509  PITCH vel track
    "calpcontrol",  // 510  PITCH src (Ctl-list)
    "calprange",    // 511  PITCH depth
    "calpdepth",    // 512  PITCH depth ctl (Ctl-list)
    "calpmin",      // 513  PITCH min depth
    "calpmax",      // 514  PITCH max depth
    "calpsource",   // 515  PITCH src2 (Ctl-list)
    "calccrmsb",    // 516
    "calccrlsb",    // 517  KEYMAP playback mode  (0=normal  2=reverse  4=bidirect 6=noise)
    "calalg",       // 518  Algorithm #
    "calfinehz",    // 519  PITCH fine hz
    
    // hseg [0] F1
    "f1hobSegTag",  // 520  hobSegTag : 80
    // "f1dspfunc", // 521  subTag
    "f1a134dspfunc",//
    "f1a136dspfunc",
    "f1a1dspfunc",
    "f1a2dspfunc",
    "f1a3dspfunc",
    "f1a8dspfunc",
    "f1a33dspfunc",
    "f1a34dspfunc",
    "f1a37dspfunc",
    "f1a40dspfunc",
    "f1a64dspfunc",
    "f1a81dspfunc",
    "f1a82dspfunc",
    "f1a104dspfunc",
    "f1coarse",     // 522
    "f1coarseadj",
    "f1coarseres",
    "f1coarseamt",
    "f1coarsewid",
    "f1coarsepct",
    "f1coarsepwm",
    "f1coarsewrap",
    "f1coarsect",
    "f1coarsepch",
    "f1coarsepchlf",
    "f1fine",       // 523
    "f1finepchlf", 
    "f1kscalect",   // 524
    "f1kscaledbt",
    "f1kscaleamt",
    "f1kscalewid",
    "f1kscalepct",
    "f1kscalepch",
    "f1kscalepwm",
    "f1kscalepchlf",
    "f1vscalect",   // 525
    "f1vscalepch",
    "f1vscaledb",
    "f1vscaleres",
    "f1vscaleamt",
    "f1vscalewid",
    "f1vscalepct",
    "f1vscalepwm",
    "f1vscalewrap",
    "f1vscalepchlf",
    "f1control",    // 526
    "f1rangect",    // 527
    "f1rangedb",
    "f1rangeres",
    "f1rangeamt",
    "f1rangewid",
    "f1rangepct",
    "f1rangepch",
    "f1rangepwm",
    "f1rangewrap",
    "f1rangepchlf",
    "f1depth",      // 528
    "f1mindepthct", // 529
    "f1mindepthdb",
    "f1mindepthres",
    "f1mindepthamt",
    "f1mindepthwid",
    "f1mindepthpct",
    "f1mindepthpch",
    "f1mindepthpwm",
    "f1mindepthwrap",
    "f1mindepthpchlf",
    "f1maxdepthct", // 530
    "f1maxdepthdb",
    "f1maxdepthres",
    "f1maxdepthamt",
    "f1maxdepthwid",
    "f1macdepthpct",
    "f1maxdepthpch",
    "f1maxdepthpwm",
    "f1maxdepthwrap",
    "f1maxdepthpchlf",
    "f1source",     // 531
    "f1tsrcmsb",    // 532  pad
    "f1tsrclsb",    // 533
    "f1outputmsb",  // 534
    "f1outputlsbks",// 535  fineHz Kstart
    "f1outputlsbfh",
    
    // hseg [1] F2
    "f2hobSegTag",  // 536  hobSegTag + 1 : 81
    // "f2dspfunc", //
    "f2a132dspfunc",
    "f2a135dspfunc",
    "f2a137dspfunc",
    "f2a6dspfunc",
    "f2a8dspfunc",
    "f2a9dspfunc",
    "f2a10dspfunc",
    "f2a16dspfunc",
    "f2a17dspfunc",
    "f2a18dspfunc",
    "f2a20dspfunc",
    "f2a33dspfunc",
    "f2a34dspfunc",
    "f2a40dspfunc",
    "f2a64dspfunc",
    "f2a66dspfunc",
    "f2a80dspfunc",
    "f2a82dspfunc",
    "f2a112dspfunc",
    "f2coarse",     // 538
    "f2coarseadj",
    "f2coarseres",
    "f2coarseamt",
    "f2coarsewid",
    "f2coarsepct",
    "f2coarsepwm",
    "f2coarsewrap",
    "f2coarsect",
    "f2coarsepch",
    "f2coarsepchlf",
    "f2fine",       // 539
    "f2finepchlf",
    "f2kscalect",   // 540
    "f2kscaledb",
    "f2kscaleamt",
    "f2kscalewid",
    "f2kscalepct",
    "f2kscalepch",
    "f2kscalepwm",
    "f2kscalepchlf",
    "f2vscalect",   // 541
    "f2vscalepch",
    "f2vscaledb",
    "f2vscaleres",
    "f2vscaleamt",
    "f2vscalewid",
    "f2vscalepctv",
    "f2vscalepwm",
    "f2vscalewrap",
    "f2vscalepchlf",
    "f2control",    // 542
    "f2rangect",    // 543
    "f2rangedb",
    "f2rangeres",
    "f2rangeamt",
    "f2rangewid",
    "f2rangepct",
    "f2rangepch",
    "f2rangepwm",
    "f2rangewrap",
    "f2rangepchlf",
    "f2depth",      // 544
    "f2mindepthct", // 545
    "f2mindepthdb",
    "f2mindepthres",
    "f2mindepthamt",
    "f2mindepthwid",
    "f2mindepthpct",
    "f2mindepthpch",
    "f2mindepthpwm",
    "f2mindepthwrap",
    "f2mindepthpchlf",
    "f2maxdepthct", // 546
    "f2maxdepthdb",
    "f2maxdepthres",
    "f2maxdepthamt",
    "f2maxdepthwid",
    "f2macdepthpct",
    "f2maxdepthpch",
    "f2maxdepthpwm",
    "f2maxdepthwrap",
    "f2maxdepthpchlf",
    "f2source",     // 547
    "f2tsrcmsb",    // 548
    "f2tsrclsb",    // 549
    "f2outputmsb",  // 550
    "f2outputlsbks",// 551
    "f2outputlsbfh",
    
    // hseg [2] F3
    "f3hobSegTag",  // 552  hobSegTag + 2 : 82
    // "f3dspfunc", // 553
    "f3a130dspfunc",
    "f3a3dspfunc",
    "f3a4dspfunc",
    "f3a5dspfunc",
    "f3a7dspfunc",
    "f3a33dspfunc",
    "f3a34dspfunc",
    "f3a40dspfunc",
    "f3a64dspfunc",
    "f3a66dspfunc",
    "f3a82dspfunc",
    "f3a104dspfunc",
    "f3a123spfunc",
    "f3coarse",     // 554
    "f3coarseadj",
    "f3coarseres",
    "f3coarseamt",
    "f3coarsewid",
    "f3coarsepct",
    "f3coarsepwm",
    "f3coarsewrap",
    "f3coarsect",
    "f3coarsepch",
    "f3coarsepchlf",
    "f3fine",       // 555
    "f3finepchlf",
    "pan2l",
    "f3kscalect",   // 556
    "f3kscaledb",
    "f3kscaleamt",
    "f3kscalewid",
    "f3kscalepct",
    "f3kscalepch",
    "f3kscalepwm",
    "f3kscalepchlf",
    "f3vscalect",   // 557
    "f3vscalepch",
    "f3vscaledb",
    "f3vscaleres",
    "f3vscaleamt",
    "f3vscalewid",
    "f3vscalepct",
    "f3vscalepwm",
    "f3vscalewrap",
    "f3vscalepchlf",
    "f3control",    // 558
    "f3rangect",    // 559
    "f3rangedb",
    "f3rangeres",
    "f3rangeamt",
    "f3rangewid",
    "f3rangepct",
    "f3rangepch",
    "f3rangepwm",
    "f3rangewrap",
    "f3rangepchlf",
    "f3depth",      // 560
    "f3mindepthct", // 561
    "f3mindepthdb",
    "f3mindepthres",
    "f3mindepthamt",
    "f3mindepthwid",
    "f3mindepthpct",
    "f3mindepthpch",
    "f3mindepthpwm",
    "f3mindepthwrap",
    "f3mindepthpchlf",
    "f3maxdepthct", // 562
    "f3maxdepthdb",
    "f3maxdepthres",
    "f3maxdepthamt",
    "f3maxdepthwid",
    "f3macdepthpct",
    "f3maxdepthpch",
    "f3maxdepthpwm",
    "f3maxdepthwrap",
    "f3maxdepthpchlf",
    "f3source",     // 563
    "f3tsrcmsb",    // 564
    "f3tsrclsb",    // 565
    //"f3outputmsb",// 566
    "pairl",
    "gainl",
    //"f3outputlsbks",// 567
    "panl",
    "model",
    "f3outputlsbfh",
   
    // hseg [3] F4
    "f4hobSegTag",  // 568  hobSegTag + 3 : 83
    // "f4dspfunc", // 569
    "f4a131dspfunc",
    "f4a133dspfunc",
    "f4a6dspfunc",
    "f4a34dspfunc",
    "f4a40dspfunc",
    "f4a66dspfunc",
    "f4a82dspfunc",
    "f4coarse",     // 570
    "f4coarseadj",
    "f4coarseres",
    "f4coarseamt",
    "f4coarsewid",
    "f4coarsepct",
    "f4coarsepwm",
    "f4coarsewrap",
    "f4coarsect",
    "f4coarsepch",
    "f4coarsepchlf",
    "f4fine",       // 571  
    "f4finepchlf",
    "pan2u",
    "f4kscalect",   // 572
    "f4kscaledb",
    "f4kscaleamt",
    "f4kscalewid",
    "f4kscalepct",
    "f4kscalepch",
    "f4kscalepwm",
    "f4kscalepchlf",
    "f4vscalect",   // 573
    "f4vscalepch",
    "f4vscaledb",
    "f4vscaleres",
    "f4vscaleamt",
    "f4vscalewid",
    "f4vscalepct",
    "f4vscalepwm",
    "f4vscalewrap",
    "f4vscalepchlf",
    "f4control",    // 574
    "f4rangect",    // 575
    "f4rangedb",
    "f4rangeres",
    "f4rangeamt",
    "f4rangewid",
    "f4rangepct",
    "f4rangepch",
    "f4rangepwm",
    "f4rangewrap",
    "f4rangepchlf",
    "f4depth",      // 576
    "f4mindepthct", // 577
    "f4mindepthdb",
    "f4mindepthres",
    "f4mindepthamt",
    "f4mindepthwid",
    "f4mindepthpct",
    "f4mindepthpch",
    "f4mindepthpwm",
    "f4mindepthwrap",
    "f4mindepthpchlf",
    "f4maxdepthct", // 578
    "f4maxdepthdb",
    "f4maxdepthres",
    "f4maxdepthamt",
    "f4maxdepthwid",
    "f4macdepthpct",
    "f4maxdepthpch",
    "f4maxdepthpwm",
    "f4maxdepthwrap",
    "f4maxdepthpchlf",
    "f4source",     // 579
    "f4tsrcmsb",    // 580
    "f4tsrclsb",    // 581
    //"f4outputmsb",// 582  output MSB Pair + Gain
    "pairu",
    "gainu",
    //"f4outputlsbks",// 583  output LSB Pan + Mode
    "panu",
    "modeu",
    "f4outputlsbfh",
    // 584 (LAYER_LAST_PARAM_FMT_4) (LAYER_LAST_PARAM_FMT_2)
    };
    // not parsed parameters
    // strnumlayers
    // studiogot
    // fxparchosen
    // fxbuschosen
    // studioname
    // keymapname
    // keymapname2
    // susstakey
    // finstakey
    
    class BipolarMappedDial extends LabelledDial
        {
        String[] map;
        String unit;
        
        public BipolarMappedDial(String _label, String key, Color staticColor, int max, String[] inmap, String inunit)
            {
            super(_label, KurzweilK2600.this, key, staticColor, -max, max);
            map = inmap;
            unit = inunit;
            }
        
        public String map (int val)
            {
            if (val < 0) return (FONT_SIZE_3 + "-" + map[-val] + unit + FONT);
            else return (FONT_SIZE_3 + map[val] + unit + FONT);
            }
        }
    
    class EnvTimesDial extends LabelledDial
        {
        public EnvTimesDial(String _label, String key, Color staticColor, int max)
            {
            super(_label, KurzweilK2600.this, key, staticColor, 0, max);
            this.addAdditionalLabel("Time");
            }
        
        public String map (int val)
            {
            if (val < 4) val = 0;
            return (FONT_SIZE_3 + ENV_TIMES[val] + "s" + FONT);
            }
        }
    
    class EnvLevelsDial extends LabelledDial
        {
        public EnvLevelsDial(String _label, String key, Color staticColor, int min, int max)
            {
            super(_label, KurzweilK2600.this, key, staticColor, min, max);
            this.addAdditionalLabel("Level");
            }
        
        public String map (int val)
            {
            return (FONT_SIZE_3 + val + "%" + FONT);
            }
        }
    
    class DspfChooser extends Chooser
        {
        int layer;
        int fpage;
        
        public DspfChooser(Function f, int lyr)
            {
            super("", KurzweilK2600.this, "layer" + lyr + f.getDfparameter(), f.getStrsel());
            this.setPreferredSize(new Dimension(120 * f.getLen(), 30));
            layer = lyr;
            fpage = f.getFpage();
            }
        
        public void update(String key, Model model)
            {
            super.update(key, model);
            updateFpage(layer, fpage);
            }
        }
    
    class Algorithm
        { // an algorithm is a variable lenght array of functions
        ArrayList<Function> functions;
        boolean[] usepage = {false, false, false, false, false};
        
        public Algorithm()
            {
            functions = new ArrayList<Function>();
            }
        
        public void addFunction(Function f)
            {
            functions.add(f);
            usepage[f.getFpage()] = true;
            }
        
        public Function getFunction(int fpage)
            {
            int found = 0;
            for (int i = 1; i < functions.size(); i++)
                {
                if (functions.get(i).getFpage() == fpage) found = i;
                }
            return functions.get(found);
            }
        
        public boolean getUsepage(int fpage)
            {
            return usepage[fpage];
            }
        }

    


    
    class FxparamChooser extends Chooser
        { // 1 chooser per lenght of parameter list
        int fxmod;
        int len;
        int sellen = 0;
        int[] intsel;
        String[] strsel;
        
        public FxparamChooser(int ln, int fxm)
            {
            super("", KurzweilK2600.this, "fxparam" + fxm + "a" + ln, new String[] {""});
            strsel = new String[ln];
            super.setElements("Param", strsel); // list of the proper length of empty strings 
            fxmod = fxm;
            len = ln;
            }
        
        public void update(String key, Model model)
            {
            super.update(key, model);
            if (len == sellen)
                {
                model.set("fxparchosen" + fxmod, model.get(key));
                }
            // System.out.println(key + " param update " + model.get(key));
            }
        
        public void setFxelements(int algno, int bus)
            {
            intsel = fxAlgorithm.getParfxidx(algno, bus);
            sellen = intsel.length;
            strsel = new String[sellen];
            for (int i =0; i < sellen; i++) strsel[i] = fxAlgorithm.getParfxstr()[intsel[i]];
            super.replaceElements(strsel);
            }
        }
    
    class FxBusChooser extends Chooser
        { // 1 chooser per lenght of parameter list
        int fxmod;
        int len;
        
        public FxBusChooser(int ln, int fxm)
            {
            super("", KurzweilK2600.this, "strip" + fxm + "a" + ln, new String[] {""});
            super.setElements("Bus", new String[ln + 11]); // list of the proper length of empty strings 
            fxmod = fxm;
            len = ln;
            }
        
        public void update(String key, Model model)
            {
            super.update(key, model);
            if (len == (fxStudio.getBusmapint().length - 11))
                {
                model.set("fxbuschosen" + fxmod, model.get(key));
                model.setStatus("fxbuschosen" + fxmod, Model.STATUS_IMMUTABLE);
                }
            }
        
        public void setFxelements()
            {
            super.replaceElements(fxStudio.getBusmapstr());
            }
        }
        
    
    class KeymapName
        {
        int requests = 0;
        
        public KeymapName() 
            {
            requests = 0;
            }
        
        public void getName(int type, int id)
            {
            requests++;
            getObjectname(type, id);
            }
        
        public int parseRequest()
            {
            requests--;
            return requests;
            }
        }
    
    void updateFpage(int layer, int fpage)
        {
        int genfunc = 0;
        boolean next = false;
        int al = model.get("layer" + layer + "calalg") - 1;
        int page = fpage - 1;
        
        if (alg[al].getUsepage(fpage))
            { 
            int specfunc = model.get("layer" + layer + alg[al].getFunction(fpage).getDfparameter());
            int[] funcs = alg[al].getFunction(fpage).getIntsel();
            genfunc = funcs[specfunc]; 
            // first function in block
            next = functionBlock.setType(layer, page, genfunc, 0);
            // function block type needed for funcdummy
            funcdummy[page][layer].update("", model);
            if (next == true)
                {
                next = functionBlock.setType(layer, page + 1, genfunc, 1);
                funcdummy[page + 1][layer].update("", model);
                if (next == true)
                    {
                    next = functionBlock.setType(layer, page + 2, genfunc, 2);
                    funcdummy[page + 2][layer].update("", model);
                    }
                }
            }
        }
    
    void makeStringArray(String[] src, String[] dest, int[] inds)
        {
        for (int i =0; i < inds.length; i++) dest[i] = src[inds[i]];
        }
    
    void setlayerparams(int layer, byte[] data, int di)
        {
        String prefix = "layer" + layer;
        
        model.set(prefix + "lyrSegTag", data[di++]); model.setStatus(prefix + "lyrSegTag", Model.STATUS_IMMUTABLE);
        
        if (data[di] > 63) model.set(prefix + "loenable", data[di] - 192);
        else model.set(prefix + "loenable", data[di] + 64);
        di++;
        
        model.set(prefix + "lyrtrans", data[di++]);
        model.set(prefix + "tune", data[di++]);
        model.set(prefix + "lokey", data[di++]);
        model.set(prefix + "hikey", data[di++]);
        
        if (data[di] >= 64) data[di] -= 64; // triple T1 add 64 to this value
        if (data[di] < 0) data[di] = 0; // triple T2 and T3 have fixed negative values
        model.set(prefix + "hivel", 7 - (data[di] % 8));
        model.set(prefix + "lovel", data[di] / 8); 
        di++; 
        
        model.set(prefix + "eswitch", getmapindex((int)data[di++] & 0xFF, CONTROL_MAP_GE));
        
        model.set(prefix + "suspdl", getmapindex((data[di] & 0x42), LAYER_SUSPDL_MAP));
        model.set(prefix + "sospdl", (data[di] & 0x04) == 0 ? 1 : 0);
        model.set(prefix + "frzpdl", (data[di] & 0x08) == 0 ? 1 : 0);
        model.set(prefix + "ignrel", (data[di] & 0x01) == 0 ? 0 : 1);
        model.set(prefix + "thratt", (data[di] & 0x10) == 0 ? 0 : 1);
        model.set(prefix + "tildec", (data[di] & 0x20) == 0 ? 0 : 1);
        di++; 
        
        model.set(prefix + "pbmode", data[di] & 0x03);
        model.set(prefix + "trig", (data[di] & 0x80) == 0 ? 0 : 1);
        model.set(prefix + "ensense", (data[di] & 0x04) == 0 ? 1 : 0);
        model.set(prefix + "opaque", (data[di] & 0x08) == 0 ? 0 : 1);
        model.set(prefix + "stereoflag", (data[di] & 0x20) == 0 ? 0 : 1);
        model.set(prefix + "xfsense", (data[di] & 0x10) == 0 ? 0 : 1);
        di++; 
        
        int vtrigflags = (int)(data[di] & 0xFF);
        model.set(prefix + "n0vtriglevel", vtrigflags & 0x07);
        model.set(prefix + "n0vtrigsense", (vtrigflags >> 3) & 0x01);
        model.set(prefix + "n1vtriglevel", (vtrigflags >> 4) & 0x07);
        model.set(prefix + "n1vtrigsense", (vtrigflags >> 7) & 0x01);
        di++;
        
        if (data[di] > 0) model.set(prefix + "hienable", data[di] - 129);
        else model.set("layer" + layer + "hienable", data[di] + 127);
        di++;
        
        model.set(prefix + "dlyctl", getmapindex((int)(data[di++] & 0xFF), CONTROL_MAP));
        
        model.set(prefix + "dlymin", (data[di] < 0) ? data[di] + 256 : data[di]); di++;
        model.set(prefix + "dlymax", (data[di] < 0) ? data[di] + 256 : data[di]); di++;
        
        model.set(prefix + "xfade", getmapindex((int)(data[di++] & 0xFF), CONTROL_MAP));
        
        setasrparams(prefix, 1, CONTROL_MAP, data, di); di += ASR_DP_SIZE;
        setasrparams(prefix, 2, CONTROL_MAP, data, di); di += ASR_DP_SIZE;
        setfunparams(prefix, 1, CONTROL_MAP_FUN, data, di); di += FUN_DP_SIZE;
        setfunparams(prefix, 2, CONTROL_MAP_FUN, data, di); di += FUN_DP_SIZE;
        setlfoparams(prefix, 1, CONTROL_MAP, data, di); di += LFO_DP_SIZE;
        setlfoparams(prefix, 2, CONTROL_MAP, data, di); di += LFO_DP_SIZE;
        setfunparams(prefix, 3, CONTROL_MAP_FUN, data, di); di += FUN_DP_SIZE;
        setfunparams(prefix, 4, CONTROL_MAP_FUN, data, di); di += FUN_DP_SIZE;
        
        setenvctlparams(prefix, data, di); di += ENVCTL_DP_SIZE;
        if (model.get("fmt") != FORMAT_2)
            { // special attack controls
            setenvctlparams(prefix + "atk", data, di); di += ENVCTL_DP_SIZE;
            }
        
        for (int env = 1; env < 4; env++)
            {
            setenvparams(prefix + "env" + env, data, di); di += ENV_DATA_SIZE;
            }
        
        model.set(prefix + "calSegTag", data[di++]); model.setStatus(prefix + "calSegTag", Model.STATUS_IMMUTABLE);
        model.set(prefix + "calsubSegTag", data[di++]);
        model.set(prefix + "transpose", data[di] + data[di + 14]); di++; // di + 14 : caltshift
        model.set(prefix + "caldtune", data[di++]);
        model.set(prefix + "caltkscale", data[di++]);
        model.set(prefix + "caltvscale", data[di++]);
        model.set(prefix + "caltcontrol", data[di++]);
        model.set(prefix + "caltrange", data[di++]);
        
        int keymap2lsb = data[di + 1];
        int keymap2 = data[di] * 256 + (keymap2lsb < 0 ? 256 + keymap2lsb : keymap2lsb);
        model.set(prefix + "keymap2", keymap2);
        di += 2;
        
        model.set(prefix + "calsroot", data[di++]);
        
        model.set(prefix + "sampleskipping", getmapindex((data[di] & 0xC0), KEYMAP_SAMSKIP_MAP));
        model.set(prefix + "altmethod", (data[di] & 0x20) == 0 ? 0 : 1);
        model.set(prefix + "playbackmode", getmapindex(
                (data[di + 18] & 0x06) + // di + 18 : calccrlsb
            (((data[di] & 0x0F) == 0) ? 0 : 6) +
            (data[di] & 0x0F), KEYMAP_PLAYMODE_MAP));
        di++;
        
        int keymaplsb = data[di + 1];
        int keymap = data[di] * 256 + (keymaplsb < 0 ? 256 + keymaplsb : keymaplsb);
        model.set(prefix + "keymap", keymap);
        di += 2;
        
        model.set(prefix + "calroot", data[di++]);
        model.set(prefix + "altctrl", getmapindex((int)data[di++] & 0xFF, CONTROL_MAP));
        model.set(prefix + "caltshift", data[di++]);
        di++; // rfu
        
        model.set(prefix + "calcpitch", data[di] - data[di - 2]); di++; // di - 2 : caltshift
        model.set(prefix + "calfpitch", data[di++]);
        model.set(prefix + "calckscale", data[di++]);
        model.set(prefix + "calcvscale", data[di++]);
        model.set(prefix + "calpcontrol", getmapindex((int)data[di++] & 0xFF, CONTROL_MAP));
        model.set(prefix + "calprange", data[di++]);
        model.set(prefix + "calpdepth", getmapindex((int)data[di++] & 0xFF, CONTROL_MAP));
        model.set(prefix + "calpmin", data[di++]);
        model.set(prefix + "calpmax", data[di++]);
        model.set(prefix + "calpsource", getmapindex((int)data[di++] & 0xFF, CONTROL_MAP));
        model.set(prefix + "calccrmsb", data[di++]);
        model.set(prefix + "calccrlsb", data[di++]);
        model.set(prefix + "calalg", data[di++]);
        model.set(prefix + "calfinehz", data[di++]);
        
        for (int page = 0; page < 4; page++)
            {
            boolean next = false;
            int genfunc = 0;
            int specfunc = 0;
            int al = model.get("layer" + layer + "calalg") - 1;
            int fpage = page + 1;
            
            prefix = "layer" + layer + "f" + fpage;
            model.set(prefix + "hobSegTag", data[di++]); model.setStatus(prefix + "hobSegTag", Model.STATUS_IMMUTABLE);
            genfunc = data[di++]; 
            if (alg[al].getUsepage(fpage))
                { // only if at start of function : set all function block types for this generic function
                next = functionBlock.setType(layer, page, genfunc, 0);
                if (next == true)
                    {
                    next = functionBlock.setType(layer, page + 1, genfunc, 1);
                    if (next == true)
                        {
                        next = functionBlock.setType(layer, page + 2, genfunc, 2);
                        }
                    }
                specfunc = getmapindex(genfunc ,alg[al].getFunction(fpage).getIntsel());
                model.set("layer" + layer + alg[al].getFunction(fpage).getDfparameter(), specfunc);
                }
            FB_ENUM type = functionBlock.getType(page);
            switch (type)
                {
                case AMP:
                case DRV:
                case EVN:
                case ODD:
                case DEP:
                    model.set(prefix + "coarseadj", data[di]);
                    model.set(prefix + "kscaledb", data[di + 2]);
                    model.set(prefix + "vscaledb", data[di + 3]);
                    model.set(prefix + "rangedb", data[di + 5]);
                    model.set(prefix + "mindepthdb", data[di + 7]);
                    model.set(prefix + "maxdepthdb", data[di + 8]);
                    if (type != FB_ENUM.AMP) model.set(prefix + "outputlsbks", data[di + 13]); // F4 uses this for output pan & mode
                    break;
                case FRQ:
                    model.set(prefix + "coarse", data[di]);
                    model.set(prefix + "fine", data[di + 1]);
                    model.set(prefix + "kscalect", data[di + 2]);
                    model.set(prefix + "vscalect", data[di + 3]);
                    model.set(prefix + "rangect", data[di + 5]);
                    model.set(prefix + "mindepthct", data[di + 7]);
                    model.set(prefix + "maxdepthct", data[di + 8]);
                    break;
                case RES:
                    model.set(prefix + "coarseres", data[di]);
                    model.set(prefix + "kscaledb", data[di + 2]);
                    model.set(prefix + "vscaleres", data[di + 3]);
                    model.set(prefix + "rangeres", data[di + 5]);
                    model.set(prefix + "mindepthres", data[di + 7]);
                    model.set(prefix + "maxdepthres", data[di + 8]);
                    break;
                case AMT:
                    model.set(prefix + "coarseamt", (int)data[di] & 0xFF);
                    model.set(prefix + "kscaleamt", data[di + 2]);
                    model.set(prefix + "vscaleamt", data[di + 3]); 
                    model.set(prefix + "rangeamt", data[di + 5]);
                    model.set(prefix + "mindepthamt", data[di + 7]);
                    model.set(prefix + "maxdepthamt", data[di + 8]);
                    model.set(prefix + "outputlsbks", data[di + 13]);
                    break;
                case WID:
                    model.set(prefix + "coarsewid", (int)data[di] & 0xFF);
                    model.set(prefix + "kscalewid", data[di + 2]);
                    model.set(prefix + "vscalewid", data[di + 3]);
                    model.set(prefix + "rangewid", data[di + 5]);
                    model.set(prefix + "mindepthwid", data[di + 7]);
                    model.set(prefix + "maxdepthwid", data[di + 8]);
                    break;
                case PCH:
                    model.set(prefix + "coarsepch", (int)data[di]);
                    model.set(prefix + "fine", data[di + 1]);
                    model.set(prefix + "kscalepch", data[di + 2]);
                    model.set(prefix + "vscalepch", data[di + 3]);
                    model.set(prefix + "rangepch", data[di + 5]);
                    model.set(prefix + "mindepthpch", data[di + 7]);
                    model.set(prefix + "maxdepthpch", data[di + 8]);
                    model.set(prefix + "outputlsbfh", data[di + 13]);
                    break;
                case WRP:
                    model.set(prefix + "coarsewrap", data[di]);
                    model.set(prefix + "kscaledb", data[di + 2]);
                    model.set(prefix + "vscalewrap", data[di + 3]);
                    model.set(prefix + "rangewrap", data[di + 5]);
                    model.set(prefix + "mindepthwrap", data[di + 7]);
                    model.set(prefix + "maxdepthwrap", data[di + 8]);
                    model.set(prefix + "outputlsbks", data[di + 13]);
                    break;
                case POS:
                case XFD:
                    model.set(prefix + "coarsepct", data[di]);
                    model.set(prefix + "kscalepct", data[di + 2]);
                    model.set(prefix + "vscalepct", data[di + 3]);
                    model.set(prefix + "rangepct", data[di + 5]);
                    model.set(prefix + "mindepthpct", data[di + 7]);
                    model.set(prefix + "maxdepthpct", data[di + 8]);
                    break;
                case SEP:
                    model.set(prefix + "coarsect", data[di]);
                    model.set(prefix + "fine", data[di + 1]);
                    model.set(prefix + "kscalect", data[di + 2]);
                    model.set(prefix + "vscalect", data[di + 3]);
                    model.set(prefix + "rangect", data[di + 5]);
                    model.set(prefix + "mindepthct", data[di + 7]);
                    model.set(prefix + "maxdepthct", data[di + 8]);
                    break;
                case WPW :
                    model.set(prefix + "coarsepwm", data[di]);
                    model.set(prefix + "kscalepwm", data[di + 2]);
                    model.set(prefix + "vscalepwm", data[di + 3]);
                    model.set(prefix + "rangepwm", data[di + 5]);
                    model.set(prefix + "mindepthpwm", data[di + 7]);
                    model.set(prefix + "maxdepthpwm", data[di + 8]);
                    break;
                case PLF:
                    int pchlf = 0;
                    model.set(prefix + "coarsepchlf", data[di]);
                    model.set(prefix + "finepchlf", (int)data[di + 1] & 0xFF);
                    pchlf = (int)data[di + 2] & 0xFF;
                    if (pchlf < 91) pchlf += 90;
                    else if (pchlf > 165) pchlf -= 166;
                    model.set(prefix + "kscalepchlf", pchlf); 
                    pchlf = (int)data[di + 3] & 0xFF;
                    if (pchlf < 111) pchlf += 110;
                    else if (pchlf > 145) pchlf -= 146;
                    model.set(prefix + "vscalepchlf", pchlf);
                    pchlf = (int)data[di + 5] & 0xFF;
                    if (pchlf < 111) pchlf += 110;
                    else if (pchlf > 145) pchlf -= 146;
                    model.set(prefix + "rangepchlf", pchlf);
                    pchlf = (int)data[di + 7] & 0xFF;
                    if (pchlf < 111) pchlf += 110;
                    else if (pchlf > 145) pchlf -= 146;
                    model.set(prefix + "mindepthpchlf", pchlf);
                    pchlf = (int)data[di + 8] & 0xFF;
                    if (pchlf < 111) pchlf += 110;
                    else if (pchlf > 145) pchlf -= 146;
                    model.set(prefix + "maxdepthpchlf", pchlf);
                    break;
                default:
                    break; 
                }
            
            model.set(prefix + "control", getmapindex((int)data[di + 4] & 0xFF, CONTROL_MAP)); 
            model.set(prefix + "depth", getmapindex((int)data[di + 6] & 0xFF, CONTROL_MAP)); 
            model.set(prefix + "source", getmapindex((int)data[di + 9] & 0xFF, CONTROL_MAP)); 
            model.set(prefix + "tsrcmsb", data[di + 10]);
            model.set(prefix + "tsrclsb", data[di + 11]);
            
            int pan = 0;
            if (page == 2)
                {
                model.set("layer" + layer + "pairl", data[di + 12] / 8);
                model.set("layer" + layer + "gainl", 7 - data[di + 12] % 8);
                if ( (type == FB_ENUM.POS) || (type == FB_ENUM.AMP) )
                    { // double output, F3 uses this for fine and kstart otherwise
                    pan = (data[di + 1]) / 16;
                    model.set("layer" + layer + "pan2l", pan); 
                    if (data[di + 13] < 0) pan = (data[di + 13] - 13) / 16;
                    else pan = (data[di + 13]) / 16;
                    model.set("layer" + layer + "panl", pan);
                    model.set("layer" + layer + "model", (data[di + 13] - pan * 16) / 4); 
                    }
                }
            else if (page == 3)
                {
                pan = (data[di + 1]) / 16;
                model.set("layer" + layer + "pan2u", pan);
                model.set("layer" + layer + "pairu", data[di + 12] / 8);
                model.set("layer" + layer + "gainu", 7 - data[di + 12] % 8);
                if (data[di + 13] < 0) pan = (data[di + 13] - 13) / 16;
                else pan = (data[di + 13]) / 16;
                model.set("layer" + layer + "panu", pan);
                model.set("layer" + layer + "modeu", (data[di + 13] - pan * 16) / 4);
                }
            else 
                {
                model.set(prefix + "outputmsb", data[di + 12]);
                }
            di += 14;
            }
        
        //System.out.println("setlayerparams " + di + " <di> ");
        
        setAlgminmax(layer);
        }
    
    void getlayerparams(int layer, byte[] data, int di)
        {
        String prefix = "layer" + layer;
        int src = 0;
        
        data[di++]= (byte)model.get(prefix + "lyrSegTag");
        
        src = model.get(prefix + "loenable");
        data[di++] = (byte)((src < -64) ? src + 192 : src - 64);
        
        data[di++] = (byte)model.get(prefix + "lyrtrans");
        data[di++] = (byte)model.get(prefix + "tune");
        data[di++] = (byte)model.get(prefix + "lokey");
        data[di++] = (byte)model.get(prefix + "hikey");
        data[di++] = (byte)(7 - model.get(prefix + "hivel") + model.get(prefix + "lovel") * 8); // TODO : different for T1, T2, T3
        data[di++] = (byte)CONTROL_MAP_GE[model.get(prefix + "eswitch")];
        
        src = LAYER_SUSPDL_MAP[model.get("layer" + layer + "suspdl")] +
            ((model.get(prefix + "sospdl") == 0) ? 0x4 : 0) +
            ((model.get(prefix + "frzpdl") == 0) ? 0x8 : 0) +
            ((model.get(prefix + "ignrel") == 0) ? 0 : 1) +
            ((model.get(prefix + "thratt") == 0) ? 0 : 0x10) +
            ((model.get(prefix + "tildec") == 0) ? 0 : 0x20);
        data[di++] = (byte)src;
        
        src = model.get(prefix + "pbmode") +
            ((model.get(prefix + "trig") == 0) ? 0 : 0x80) +
            ((model.get(prefix + "ensense") == 0) ? 0x04 : 0) +
            ((model.get(prefix + "opaque") == 0) ? 0 : 0x08) +
            ((model.get(prefix + "stereoflag") == 0) ? 0 : 0x20) +
            ((model.get(prefix + "xfsense") == 0) ? 0 : 0x10);
        data[di++] = (byte)src;
        
        src = model.get(prefix + "n0vtriglevel") + 
            (model.get(prefix + "n0vtrigsense") << 3) + 
            (model.get(prefix + "n1vtriglevel") << 4) +
            (model.get(prefix + "n1vtrigsense") << 7);
        data[di++] = (byte)src;
        
        src = model.get(prefix + "hienable");
        data[di++] = (byte)((src < 1) ? src + 129 : src - 127);
        
        data[di++] = (byte)CONTROL_MAP[model.get("layer" + layer + "dlyctl")];
        data[di++] = (byte)model.get(prefix + "dlymin");
        data[di++] = (byte)model.get(prefix + "dlymax");
        data[di++] = (byte)CONTROL_MAP[model.get(prefix + "xfade")];
        
        getasrparams(prefix, 1, CONTROL_MAP, data, di); di += ASR_DP_SIZE;
        getasrparams(prefix, 2, CONTROL_MAP, data, di); di += ASR_DP_SIZE;
        getfunparams(prefix, 1, CONTROL_MAP_FUN, data, di); di += FUN_DP_SIZE;
        getfunparams(prefix, 2, CONTROL_MAP_FUN, data, di); di += FUN_DP_SIZE;
        getlfoparams(prefix, 1, CONTROL_MAP, data, di); di += LFO_DP_SIZE;
        getlfoparams(prefix, 2, CONTROL_MAP, data, di); di += LFO_DP_SIZE;
        getfunparams(prefix, 3, CONTROL_MAP_FUN, data, di); di += FUN_DP_SIZE;
        getfunparams(prefix, 4, CONTROL_MAP_FUN, data, di); di += FUN_DP_SIZE;
        
        getenvctlparams(prefix, data, di); di += ENVCTL_DP_SIZE;
        if (model.get("fmt") != FORMAT_2)
            { // impact parameter 
            getenvctlparams(prefix + "atk", data, di); di += ENVCTL_DP_SIZE;
            }
        
        for (int env = 1; env < 4; env++)
            {
            getenvparams(prefix + "env" + env, data, di); di += ENV_DATA_SIZE;
            }
        
        data[di++] = (byte)model.get(prefix + "calSegTag");
        data[di++] = (byte)model.get(prefix + "calsubSegTag");
        data[di++] = (byte)(model.get(prefix + "transpose") - model.get(prefix + "caltshift"));
        data[di++] = (byte)model.get(prefix + "caldtune");
        data[di++] = (byte)model.get(prefix + "caltkscale");
        data[di++] = (byte)model.get(prefix + "caltvscale");
        data[di++] = (byte)model.get(prefix + "caltcontrol");
        data[di++] = (byte)model.get(prefix + "caltrange");
        
        src = model.get("layer" + layer + "keymap2");
        data[di++] = (byte)(src / 256);
        data[di++] = (byte)(src % 256);
        
        data[di++] = (byte)model.get(prefix + "calsroot");
        
        int playmode = KEYMAP_PLAYMODE_MAP[model.get(prefix + "playbackmode")];
        src = KEYMAP_SAMSKIP_MAP[model.get(prefix + "sampleskipping")] +
            ((model.get(prefix + "altmethod") == 0) ? 0 : 0x20) +
            ((playmode > 6) ? (playmode - 6) : 0);
        data[di++] = (byte)src;
        
        src = model.get(prefix + "keymap");
        data[di++] = (byte)(src / 256);
        data[di++] = (byte)(src % 256);
        
        data[di++] = (byte)model.get(prefix + "calroot");
        data[di++] = (byte)CONTROL_MAP[model.get(prefix + "altctrl")];
        data[di++] = (byte)model.get(prefix + "caltshift");
        di++;
        data[di++] = (byte)(model.get(prefix + "calcpitch") + model.get(prefix + "caltshift"));
        data[di++] = (byte)model.get(prefix + "calfpitch");
        data[di++] = (byte)model.get(prefix + "calckscale");
        data[di++] = (byte)model.get(prefix + "calcvscale");
        data[di++] = (byte)CONTROL_MAP[model.get(prefix + "calpcontrol")];
        data[di++] = (byte)model.get(prefix + "calprange");
        data[di++] = (byte)CONTROL_MAP[model.get(prefix + "calpdepth")];
        data[di++] = (byte)model.get(prefix + "calpmin");
        data[di++] = (byte)model.get(prefix + "calpmax");
        data[di++] = (byte)CONTROL_MAP[model.get(prefix + "calpsource")];
        data[di++] = (byte)model.get(prefix + "calccrmsb");
        data[di++] = (byte)((playmode <= 6) ? playmode : 0);
        data[di++] = (byte)model.get(prefix + "calalg");
        data[di++] = (byte)model.get(prefix + "calfinehz");
        
        for (int page = 0; page < 4; page++)
            {
            int fpage = page + 1;
            int genfunc = 0; 
            boolean next = false;
            prefix = "layer" + layer + "f" + fpage;
            
            data[di++] = (byte)model.get(prefix + "hobSegTag");
            
            int al = model.get("layer" + layer + "calalg") - 1;
            if (alg[al].getUsepage(fpage))
                {
                int specfunc = model.get("layer" + layer + alg[al].getFunction(fpage).getDfparameter());
                int[] funcs = alg[al].getFunction(fpage).getIntsel();
                genfunc = funcs[specfunc]; 
                data[di] = (byte)genfunc;
                // first function in block
                next = functionBlock.setType(layer, page, genfunc, 0);
                // function block type needed for funcdummy
                funcdummy[page][layer].update("", model);
                if (next == true)
                    {
                    next = functionBlock.setType(layer, page + 1, genfunc, 1);
                    funcdummy[page + 1][layer].update("", model);
                    if (next == true)
                        {
                        next = functionBlock.setType(layer, page + 2, genfunc, 2);
                        funcdummy[page + 2][layer].update("", model);
                        }
                    }
                }
            di++;
            
            FB_ENUM type = functionBlock.getType(page);
            switch (type)
                {
                case AMP:
                case DRV:
                case EVN:
                case ODD:
                case DEP:
                    data[di] = (byte)model.get(prefix + "coarseadj");
                    data[di + 2] = (byte)model.get(prefix + "kscaledb");
                    data[di + 3] = (byte)model.get(prefix + "vscaledb");
                    data[di + 5] = (byte)model.get(prefix + "rangedb");
                    data[di + 7] = (byte)model.get(prefix + "mindepthdb");
                    data[di + 8] = (byte)model.get(prefix + "maxdepthdb");
                    if (type != FB_ENUM.AMP) data[di + 13] = (byte)model.get(prefix + "outputlsbks"); // F4 uses this for output pan & mode
                    break;
                case FRQ:
                    data[di] = (byte)model.get(prefix + "coarse");
                    data[di + 1] = (byte)model.get(prefix + "fine");
                    data[di + 2] = (byte)model.get(prefix + "kscalect");
                    data[di + 3] = (byte)model.get(prefix + "vscalect");
                    data[di + 5] = (byte)model.get(prefix + "rangect");
                    data[di + 7] = (byte)model.get(prefix + "mindepthct");
                    data[di + 8] = (byte)model.get(prefix + "maxdepthct");
                    break;
                case RES:
                    data[di] = (byte)model.get(prefix + "coarseres");
                    data[di + 2] = (byte)model.get(prefix + "kscaledb");
                    data[di + 3] = (byte)model.get(prefix + "vscaleres");
                    data[di + 5] = (byte)model.get(prefix + "rangeres");
                    data[di + 7] = (byte)model.get(prefix + "mindepthres");
                    data[di + 8] = (byte)model.get(prefix + "maxdepthres");
                    break;
                case AMT:
                    data[di] = (byte)model.get(prefix + "coarseamt");
                    data[di + 2] = (byte)model.get(prefix + "kscaleamt");
                    data[di + 3] = (byte)model.get(prefix + "vscaleamt");
                    data[di + 5] = (byte)model.get(prefix + "rangeamt");
                    data[di + 7] = (byte)model.get(prefix + "mindepthamt");
                    data[di + 8] = (byte)model.get(prefix + "maxdepthamt");
                    data[di + 13] = (byte)model.get(prefix + "outputlsbks");
                    break;
                case WID:
                    data[di] = (byte)model.get(prefix + "coarsewid");
                    data[di + 2] = (byte)model.get(prefix + "kscalewid");
                    data[di + 3] = (byte)model.get(prefix + "vscalewid");
                    data[di + 5] = (byte)model.get(prefix + "rangewid");
                    data[di + 7] = (byte)model.get(prefix + "mindepthwid");
                    data[di + 8] = (byte)model.get(prefix + "maxdepthwid");
                    break;
                case PCH:
                    data[di] = (byte)model.get(prefix + "coarsepch");
                    data[di + 1] = (byte)model.get(prefix + "fine");
                    data[di + 2] = (byte)model.get(prefix + "kscalepch");
                    data[di + 3] = (byte)model.get(prefix + "vscalepch");
                    data[di + 5] = (byte)model.get(prefix + "rangepch");
                    data[di + 7] = (byte)model.get(prefix + "mindepthpch");
                    data[di + 8] = (byte)model.get(prefix + "maxdepthpch");
                    data[di + 13] = (byte)model.get(prefix + "outputlsbfh");
                    break;
                case WRP:
                    data[di] = (byte)model.get(prefix + "coarsewrap");
                    data[di + 2] = (byte)model.get(prefix + "kscaledb");
                    data[di + 3] = (byte)model.get(prefix + "vscalewrap");
                    data[di + 5] = (byte)model.get(prefix + "rangewrap");
                    data[di + 7] = (byte)model.get(prefix + "mindepthwrap");
                    data[di + 8] = (byte)model.get(prefix + "maxdepthwrap");
                    data[di + 13] = (byte)model.get(prefix + "outputlsbks");
                    break;
                case POS:
                case XFD:
                    data[di] = (byte)model.get(prefix + "coarsepct");
                    data[di + 2] = (byte)model.get(prefix + "kscalepct");
                    data[di + 3] = (byte)model.get(prefix + "vscalepct");
                    data[di + 5] = (byte)model.get(prefix + "rangepct");
                    data[di + 7] = (byte)model.get(prefix + "mindepthpct");
                    data[di + 8] = (byte)model.get(prefix + "maxdepthpct");
                    break;
                case SEP:
                    data[di] = (byte)model.get(prefix + "coarsect");
                    data[di + 1] = (byte)model.get(prefix + "fine");
                    data[di + 2] = (byte)model.get(prefix + "kscalect");
                    data[di + 3] = (byte)model.get(prefix + "vscalect");
                    data[di + 5] = (byte)model.get(prefix + "rangect");
                    data[di + 7] = (byte)model.get(prefix + "mindepthct");
                    data[di + 8] = (byte)model.get(prefix + "maxdepthct");
                    break;
                case WPW :
                    data[di] = (byte)model.get(prefix + "coarsepwm");
                    data[di + 2] = (byte)model.get(prefix + "kscalepwm");
                    data[di + 3] = (byte)model.get(prefix + "vscalepwm");
                    data[di + 5] = (byte)model.get(prefix + "rangepwm");
                    data[di + 7] = (byte)model.get(prefix + "mindepthpwm");
                    data[di + 8] = (byte)model.get(prefix + "maxdepthpwm");
                    break;
                case PLF:
                    int pchlf =0;
                    data[di] = (byte)model.get(prefix + "coarsepchlf");
                    data[di + 1] = (byte)model.get(prefix + "finepchlf");
                    pchlf = model.get(prefix + "kscalepchlf");
                    if (pchlf < 90) pchlf += 166;
                    else pchlf -= 90;
                    data[di + 2] = (byte)pchlf;
                    pchlf = model.get(prefix + "vscalepchlf");
                    if (pchlf < 110) pchlf += 146;
                    else pchlf -= 110;
                    data[di + 3] = (byte)pchlf;
                    pchlf = model.get(prefix + "rangepchlf");
                    if (pchlf < 110) pchlf += 146;
                    else pchlf -= 110;
                    data[di + 5] = (byte)pchlf;
                    pchlf = model.get(prefix + "mindepthpchlf");
                    if (pchlf < 110) pchlf += 146;
                    else pchlf -= 110;
                    data[di + 7] = (byte)pchlf;
                    pchlf = model.get(prefix + "maxdepthpchlf");
                    if (pchlf < 110) pchlf += 146;
                    else pchlf -= 110;
                    data[di + 8] = (byte)pchlf;
                    break;
                default:
                    break; 
                }
            data[di + 4] = (byte)CONTROL_MAP[model.get(prefix + "control")]; 
            data[di + 6] = (byte)CONTROL_MAP[model.get(prefix + "depth")]; 
            data[di + 9] = (byte)CONTROL_MAP[model.get(prefix + "source")]; 
            data[di + 10] = (byte)model.get(prefix + "tsrcmsb");
            data[di + 11] = (byte)model.get(prefix + "tsrclsb");
            if (page == 2)
                {
                data[di + 12] = (byte)(model.get("layer" + layer + "pairl") * 8 + 7 - model.get("layer" + layer + "gainl"));
                if ( (type == FB_ENUM.POS) || (type == FB_ENUM.AMP) )
                    { // F3 uses this for fine and kstart otherwise
                    data[di + 1] = (byte)(model.get("layer" + layer + "pan2l") * 16); 
                    data[di + 13] = (byte)(model.get("layer" + layer + "panl") * 16 + model.get("layer" + layer + "model") * 4); 
                    }
                }
            else if (page == 3)
                {
                data[di + 1] = (byte)(model.get("layer" + layer + "pan2u") * 16);
                data[di + 12] = (byte)(model.get("layer" + layer + "pairu") * 8 + 7 - model.get("layer" + layer + "gainu"));
                data[di + 13] = (byte)(model.get("layer" + layer + "panu") * 16 + model.get("layer" + layer + "modeu") * 4);
                }
            else 
                {
                data[di + 12] = (byte)model.get(prefix + "outputmsb");
                }
            di += 14;
            }
        // System.out.println("getlayerparams " + di + " <di> ");
        }
    
    void setlfoparams(String prefix, int num, int[] map, byte[] data, int di)
        {
        model.set(prefix + "lfo" + num + "SegTag", data[di++]); model.setStatus(prefix + "lfo" + num + "SegTag", Model.STATUS_IMMUTABLE);
        di++ ; // rfu
        model.set(prefix + "lfo" + num + "ratectl", getmapindex((int)data[di++], map));
        model.set(prefix + "lfo" + num + "minrate", (data[di] < 0) ? data[di] + 256 : data[di]); di++;
        model.set(prefix + "lfo" + num + "maxrate", (data[di] < 0) ? data[di] + 256 : data[di]); di++;
        model.set(prefix + "lfo" + num + "phase", data[di++]);
        model.set(prefix + "lfo" + num + "shape", getmapindex((int)data[di], LFO_SHP_MAP));
        }
    
    void getlfoparams(String prefix, int num, int[] map, byte[] data, int di)
        {
        data[di++]= (byte)model.get(prefix + "lfo" + num + "SegTag");
        di++;// rfu
        data[di++]= (byte)map[model.get(prefix + "lfo" + num + "ratectl")];
        data[di++]= (byte)model.get(prefix + "lfo" + num + "minrate");
        data[di++]= (byte)model.get(prefix + "lfo" + num + "maxrate");
        data[di++]= (byte)model.get(prefix + "lfo" + num + "phase");
        data[di++]= (byte)LFO_SHP_MAP[model.get(prefix + "lfo" + num + "shape")];
        }
    
    void setasrparams(String prefix, int num, int[] map, byte[] data, int di)
        {
        model.set(prefix + "asr" + num + "SegTag", data[di++]); model.setStatus(prefix + "asr" + num + "SegTag", Model.STATUS_IMMUTABLE);
        di++; // rfu
        model.set(prefix + "asr" + num + "trigger", getmapindex((int)data[di++] & 0xFF, map));
        model.set(prefix + "asr" + num + "flags", data[di++]);
        model.set(prefix + "asr" + num + "dtime", (data[di] < 0) ? data[di] + 256 : data[di]); di++;
        model.set(prefix + "asr" + num + "atime", (data[di] < 0) ? data[di] + 256 : data[di]); di++;
        di++; // rfu
        model.set(prefix + "asr" + num + "rtime", (data[di] < 0) ? data[di] + 256 : data[di]);
        }
    
    void getasrparams(String prefix, int num, int[] map, byte[] data, int di)
        {
        byte time = 0;
        data[di++]= (byte)model.get(prefix + "asr" + num + "SegTag");
        di++; // rfu
        data[di++] = (byte)map[model.get(prefix + "asr" + num + "trigger")];
        data[di++] = (byte)model.get(prefix + "asr" + num + "flags");
        time = (byte)model.get(prefix + "asr" + num + "dtime");
        data[di++] = (time < 4) ? 0 : time;
        time = (byte)model.get(prefix + "asr" + num + "atime");
        data[di++] = (time < 4) ? 0 : time;
        di++; // rfu
        time = (byte)model.get(prefix + "asr" + num + "rtime");
        data[di++] = (time < 4) ? 0 : time;
        }
    
    void setfunparams(String prefix, int num, int[] map,  byte[] data, int di)
        {
        model.set(prefix + "fcn" + num + "SegTag", data[di++]); model.setStatus(prefix + "fcn" + num + "SegTag", Model.STATUS_IMMUTABLE);
        model.set(prefix + "fcn" + num + "op", getmapindex((int)data[di++], FUN_FUN_MAP));
        model.set(prefix + "fcn" + num + "arg1", getmapindex((int)data[di++] & 0xFF, map));
        model.set(prefix + "fcn" + num + "arg2", getmapindex((int)data[di] & 0xFF, map));
        }
    
    void getfunparams(String prefix, int num, int[] map, byte[] data, int di)
        {
        data[di++]= (byte)model.get(prefix + "fcn" + num + "SegTag");
        data[di++]= (byte)FUN_FUN_MAP[model.get(prefix + "fcn" + num + "op")];
        data[di++]= (byte)map[model.get(prefix + "fcn" + num + "arg1")];
        data[di++]= (byte)map[model.get(prefix + "fcn" + num + "arg2")];
        }
    
    void setfxparams(int fxmod, int[] map, byte[] data, int di)
        {
        model.set("fxptSegTag" + fxmod, data[di++]); model.setStatus("fxptSegTag" + fxmod, Model.STATUS_IMMUTABLE);
        model.set("num" + fxmod, data[di++]); model.setStatus("num" + fxmod, Model.STATUS_IMMUTABLE);
        
        int len = 0;
        int bus = data[di++];
        int cbus = getmapindex(bus, fxStudio.getBusmapint());
        
        len = fxStudio.getBusmapint().length - 11;
        model.set("strip" + fxmod + "a" + len, cbus);
        
        int algidx = fxStudio.getAlgidx()[bus];
        len = fxAlgorithm.getParfxidx(algidx, bus).length;
        int mappedparam = getmapindex((int)data[di], fxAlgorithm.getParmap(algidx, bus));
        model.set("fxparam" + fxmod + "a" + len, mappedparam);
        
        int paridx = fxAlgorithm.getParfxidx(algidx, bus)[mappedparam];
        switch(fxAlgorithm.getType(paridx))
            {   // TODO : more cases
            case 1:
                model.set("adjustl" + fxmod, data[di + 1]);
                model.set("depthl" + fxmod, data[di + 3]);
                break;
            case 2:
                model.set("adjustfd" + fxmod, data[di + 1]);
                model.set("depthf" + fxmod, data[di + 3]);
                break;
            case 3:
                model.set("adjustg" + fxmod, data[di + 1]);
                model.set("depthg" + fxmod, data[di + 3]);
                break;
            case 4:
                model.set("adjustf" + fxmod, data[di + 1]);
                model.set("depthf" + fxmod, data[di + 3]);
                break;
            case 5:
                model.set("adjustp" + fxmod, data[di + 1]);
                model.set("depthp" + fxmod, data[di + 3]);
                break;
            case 6:
                model.set("adjustwd" + fxmod, data[di + 1]);
                model.set("depthwd" + fxmod, data[di + 3]);
                break;
            default:
                break;
            }
        
        model.set("source" + fxmod, getmapindex((int)data[di + 2], map)); 
        }
    
    void getfxparams(int fxmod, int[] map, byte[] data, int di)
        {
        data[di++]= (byte)model.get("fxptSegTag" + fxmod);
        data[di++]= (byte)model.get("num" + fxmod);
        int len = 0;
        int algidx = 0;
        
        len = fxStudio.getBusmapint().length - 11;
        int bus = fxStudio.getBusmapint()[model.get("strip" + fxmod + "a" + len)]; 
        data[di++]= (byte)bus;
        // TODO : check ! choose correct param see setfxparams
        algidx = fxStudio.getAlgidx()[bus];
        len = fxAlgorithm.getParfxidx(algidx, bus).length;
        data[di] = (byte)fxAlgorithm.getParmap(algidx, bus)[model.get("fxparam" + fxmod + "a" + len)];
        
        switch (model.get("fxparam" + fxmod + "a" + len))
            {
            case 2:
                data[di + 1] = (byte)model.get("adjustfd" + fxmod);
                data[di + 3] = (byte)model.get("depthf" + fxmod);
                break;
            case 3:
                data[di + 1] = (byte)model.get("adjustg" + fxmod);
                data[di + 3] = (byte)model.get("depthg" + fxmod);
                break;
            case 4:
                data[di + 1] = (byte)model.get("adjustf" + fxmod);
                data[di + 3] = (byte)model.get("depthf" + fxmod);
                break;
            case 1:
                data[di + 1] = (byte)model.get("adjustl" + fxmod);
                data[di + 3] = (byte)model.get("depthl" + fxmod);
                break;
            case 5:
                data[di + 1] = (byte)model.get("adjustp" + fxmod);
                data[di + 3] = (byte)model.get("depthp" + fxmod);
                break;
            default:
                break;
            }
        
        data[di + 2] = (byte)map[model.get("source" + fxmod)];
        }
    
    void setenvctlparams(String prefix, byte[] data, int di)
        {
        model.set(prefix + "encSegTag", data[di++]); model.setStatus(prefix + "encSegTag", Model.STATUS_IMMUTABLE);
        di++; // rfu
        model.set(prefix + "encflags", data[di++]);
        model.set(prefix + "attime", data[di++]);
        model.set(prefix + "atkscale", data[di++]);
        model.set(prefix + "atvscale", data[di++]);
        model.set(prefix + "atctl", getmapindex((int)(data[di++] & 0xFF), CONTROL_MAP));
        model.set(prefix + "atrange", data[di++]);
        model.set(prefix + "dttime", data[di++]);
        model.set(prefix + "dtkscale", data[di++]);
        model.set(prefix + "dtctl", getmapindex((int)(data[di++] & 0xFF), CONTROL_MAP));
        model.set(prefix + "dtrange", data[di++]);
        model.set(prefix + "rttime", data[di++]);
        model.set(prefix + "rtkscale", data[di++]);
        model.set(prefix + "rtctl", getmapindex((int)(data[di++] & 0xFF), CONTROL_MAP));
        model.set(prefix + "rtrange", data[di]);
        if (prefix.contains("atk"))
            { // unused parameters
            model.setStatus(prefix + "encflags", Model.STATUS_IMMUTABLE);
            model.setStatus(prefix + "dttime", Model.STATUS_IMMUTABLE);
            model.setStatus(prefix + "dtkscale", Model.STATUS_IMMUTABLE);
            model.setStatus(prefix + "dtctl", Model.STATUS_IMMUTABLE);
            model.setStatus(prefix + "dtrange", Model.STATUS_IMMUTABLE);
            model.setStatus(prefix + "rttime", Model.STATUS_IMMUTABLE);
            model.setStatus(prefix + "rtkscale", Model.STATUS_IMMUTABLE);
            model.setStatus(prefix + "rtctl", Model.STATUS_IMMUTABLE);
            model.setStatus(prefix + "rtrange", Model.STATUS_IMMUTABLE);
            }
        }
    
    void getenvctlparams(String prefix, byte[] data, int di)
        {
        data[di++]= (byte)model.get(prefix + "encSegTag");
        di++; // rfu
        data[di++]= (byte)model.get(prefix + "encflags");
        data[di++]= (byte)model.get(prefix + "attime");
        data[di++]= (byte)model.get(prefix + "atkscale");
        data[di++]= (byte)model.get(prefix + "atvscale");
        data[di++]= (byte)CONTROL_MAP[model.get(prefix + "atctl")];
        data[di++]= (byte)model.get(prefix + "atrange");
        if (prefix.contains("atk"))
            { // unused parameters
            data[di++]= 0;
            data[di++]= 0;
            data[di++]= 0;
            data[di++]= 0;
            data[di++]= 0;
            data[di++]= 0;
            data[di++]= 0;
            data[di++]= 0;
            }
        else
            {
            data[di++]= (byte)model.get(prefix + "dttime");
            data[di++]= (byte)model.get(prefix + "dtkscale");
            data[di++]= (byte)CONTROL_MAP[model.get(prefix + "dtctl")];
            data[di++]= (byte)model.get(prefix + "dtrange");
            data[di++]= (byte)model.get(prefix + "rttime");
            data[di++]= (byte)model.get(prefix + "rtkscale");
            data[di++]= (byte)CONTROL_MAP[model.get(prefix + "rtctl")];
            data[di++]= (byte)model.get(prefix + "rtrange");
            }
        }
    
    void setenvparams(String prefix, byte[] data, int di)
        {
        model.set(prefix + "SegTag", data[di++]); model.setStatus(prefix + "SegTag", Model.STATUS_IMMUTABLE);
        model.set(prefix + "looptype", getmapindex((int)(data[di] & 0xFF) & 0x07, ENV_LOOP_TYPE_MAP));
        model.set(prefix + "numloops", (int)(data[di++] & 0xFF) >> 3);
        model.set(prefix + "att1", data[di++]);
        model.set(prefix + "att1time", (data[di] < 0) ? data[di] + 256 : data[di]); di++;
        model.set(prefix + "att2", data[di++]);
        model.set(prefix + "att2time", (data[di] < 0) ? data[di] + 256 : data[di]); di++;
        model.set(prefix + "att3", data[di++]);
        model.set(prefix + "att3time", (data[di] < 0) ? data[di] + 256 : data[di]); di++;
        model.set(prefix + "dec1", data[di++]);
        model.set(prefix + "dec1time", (data[di] < 0) ? data[di] + 256 : data[di]); di++;
        model.set(prefix + "rel1", data[di++]);
        model.set(prefix + "rel1time", (data[di] < 0) ? data[di] + 256 : data[di]); di++;
        model.set(prefix + "rel2", data[di++]);
        model.set(prefix + "rel2time", (data[di] < 0) ? data[di] + 256 : data[di]); di++;
        model.set(prefix + "rel3", data[di++]);
        model.set(prefix + "rel3time", (data[di] < 0) ? data[di] + 256 : data[di]);
        }
    
    void getenvparams(String prefix, byte[] data, int di)
        {
        byte time = 0;
        data[di++] = (byte)model.get(prefix + "SegTag");
        data[di++] = (byte)(ENV_LOOP_TYPE_MAP[model.get(prefix + "looptype")] + (model.get(prefix + "numloops") << 3));
        data[di++] = (byte)model.get(prefix + "att1");
        data[di++] = (byte)model.get(prefix + "att1time");
        data[di++] = (byte)model.get(prefix + "att2");
        time = (byte)model.get(prefix + "att2time");
        data[di++] = (time < 4) ? 0 : time;
        data[di++] = (byte)model.get(prefix + "att3");
        time = (byte)model.get(prefix + "att3time");
        data[di++] = (time < 4) ? 0 : time;
        data[di++] = (byte)model.get(prefix + "dec1");
        time = (byte)model.get(prefix + "dec1time");
        data[di++] = (time < 4) ? 0 : time;
        data[di++] = (byte)model.get(prefix + "rel1");
        time = (byte)model.get(prefix + "rel1time");
        data[di++] = (time < 4) ? 0 : time;
        data[di++] = (byte)model.get(prefix + "rel2");
        time = (byte)model.get(prefix + "rel2time");
        data[di++] = (time < 4) ? 0 : time;
        data[di++] = (byte)model.get(prefix + "rel3");
        time = (byte)model.get(prefix + "rel3time");
        data[di++] = (time < 4) ? 0 : time;
        }
    
    int getmapindex(int src, int[] map)
        {
        int index = 0;
        for (int i = 0; i < map.length; i++)
            {
            if (src == map[i]) index = i;
            }
        return index;
        }
    
    void setTabs()
        {
        int seltab = tabs.getSelectedIndex();
        String triple = "";
        
        tabs.remove(1);
        tabs.remove(1);
        tabs.remove(1);
        tabs.remove(1);
        if (model.get("layer" + selLayer + "calalg") >= ALG_LYR_T3) triple = "T3-";
        else if (model.get("layer" + selLayer + "calalg") >= ALG_LYR_T2) triple = "T2-";
        else if (model.get("layer" + selLayer + "calalg") >= ALG_LYR_T1) triple = "T1-";
        else triple = "";
        insertTab("Layer:" + triple + (selLayer + 1) + "/" + model.get("numlayers"), layerPanel[selLayer], 1);
        insertTab("Layer Dsp:" + triple + (selLayer + 1) + "/" + model.get("numlayers"), layerDspPanel[selLayer], 2);
        insertTab("Layer Env:" + triple + (selLayer + 1) + "/" + model.get("numlayers"), layerEnvPanel[selLayer], 3);
        insertTab("Layer Mod:" + triple + (selLayer + 1) + "/" + model.get("numlayers"), layerModPanel[selLayer], 4);
        tabs.setSelectedIndex(seltab);
        getObjectname(KEYMAP_TYPE, model.get("layer" + selLayer + "keymap")); 
        getObjectname(KEYMAP_TYPE, model.get("layer" + selLayer + "keymap2"));
        }
    
    // converts all but last byte (F7)
    byte[] convert4To8Bit(byte[] data, int offset)
        {
        // How big?
        int size = (data.length - offset - 1) / 2; // - 1 is for the EOX
        byte[] newd = new byte[size];
        
        for(int i = offset; i < (data.length - 2); i += 2)
            {
            newd[(i - offset) / 2] = (byte)((data[i] << 4) + data[i + 1]);
            }
        
        return newd;
        }
    
    // converts all but last byte (F7)
    byte[] convert7To8Bit(byte[] data, int offset)
        {
        int size = (data.length - offset - 1) / 8 * 7;
        if ((data.length - offset - 1) % 8 > 0)
            size += ((data.length - offset - 1) % 8 - 1);
        byte[] newd = new byte[size];
        
        int j = 0;
        for(int i = offset; i < data.length; i += 8)
            {
            for(int x = 0; x < 7; x++)
                {
                if (j + x < newd.length)
                    newd[j + x] = (byte)( ((data[i + x]) << (x + 1)) + ((data[i + x + 1]) >> (6 - x)) );
                }
            j += 7;
            }
        return newd;
        }
    
    // converts all bytes
    byte[] convert8To7Bit(byte[] data)
        {
        // How big?
        int size = (data.length) / 7 * 8;
        if (data.length % 7 > 0)
            size += (1 + data.length % 7);
        byte[] newd = new byte[size];
        
        int j = 0;
        for(int i = 0; i < data.length; i+=7)
            {
            newd[j] = (byte)((data[i] & 0xFF) >>> 1);
            if ((i+1) < data.length) newd[j+1] = (byte)( ((data[i+0] << 6) & 0x7F) | ((data[i+1] & 0xFF) >>> 2) );
            if ((i+2) < data.length) newd[j+2] = (byte)( ((data[i+1] << 5) & 0x7F) | ((data[i+2] & 0xFF) >>> 3) );
            if ((i+3) < data.length) newd[j+3] = (byte)( ((data[i+2] << 4) & 0x7F) | ((data[i+3] & 0xFF) >>> 4) );
            if ((i+4) < data.length) newd[j+4] = (byte)( ((data[i+3] << 3) & 0x7F) | ((data[i+4] & 0xFF) >>> 5) );
            if ((i+5) < data.length) newd[j+5] = (byte)( ((data[i+4] << 2) & 0x7F) | ((data[i+5] & 0xFF) >>> 6) );
            if ((i+6) < data.length) newd[j+6] = (byte)( ((data[i+5] << 1) & 0x7F) | ((data[i+6] & 0xFF) >>> 7) );
            if ((i+6) < data.length) newd[j+7] = (byte)(data[i+6] & 0x7F);
            j += 8;
            }
        return newd;
        }
    
    void setAlgminmax(int layer)
        {
        int alg = model.get("layer" + layer + "calalg");
        if (alg >= ALG_LYR_T3) {calalg[layer].setMin(ALG_LYR_T3); calalg[layer].setMax(MAX_NUM_ALGS);}
        else if (alg >= ALG_LYR_T2) {calalg[layer].setMin(ALG_LYR_T2); calalg[layer].setMax(ALG_LYR_T3 - 1);}
        else if (alg >= ALG_LYR_T1) {calalg[layer].setMin(ALG_LYR_T1); calalg[layer].setMax(ALG_LYR_T2 - 1);}
        else {calalg[layer].setMin(1); calalg[layer].setMax(NUM_ALGS_1_LYR);}
        }
    
    void getKdfxObject(int type, int id)
        {
        byte[] data = new byte[11];
        data[0] = SOX;
        data[1] = KURZWEIL_ID;
        data[2] = (byte)0x00;   // device ID
        data[3] = K2600_ID;
        data[4] = READ_MSG;
        data[5] = (byte)(type / 128);
        data[6] = (byte)(type % 128);
        data[7] = (byte)(id / 128);
        data[8] = (byte)(id % 128);
        data[9] = (byte)0x01;   // form
        data[10] = EOX;
        boolean val = getSendMIDI();
        setSendMIDI(true);
        tryToSendSysex(data);
        setSendMIDI(val);
        }
    
    void getObjectname(int type, int id)
        {
        byte[] data = new byte[10];
        data[0] = SOX;
        data[1] = KURZWEIL_ID;
        data[2] = (byte)0x00;   // device ID
        data[3] = K2600_ID;
        data[4] = DIR_MSG;
        data[5] = (byte)(type / 128);
        data[6] = (byte)(type % 128);
        data[7] = (byte)(id / 128);
        data[8] = (byte)(id % 128);
        data[9] = EOX;
        boolean val = getSendMIDI();
        setSendMIDI(true);
        tryToSendSysex(data);
        setSendMIDI(val);
        }
    
    void incdecRead(byte ic)
        {
        byte[] data = new byte[9];
        data[0] = SOX;
        data[1] = KURZWEIL_ID;
        data[2] = (byte)0x00;
        data[3] = K2600_ID;
        data[4] = PANEL_MSG;
        data[5] = (byte)0x09;
        data[6] = (byte)ic;;
        data[7] = (byte)0x00;
        data[8] = EOX;
        
        boolean val = getSendMIDI();
        setSendMIDI(true);
        tryToSendSysex(data);
        
        byte[] data2 = new byte[6];
        data2[0] = SOX;
        data2[1] = KURZWEIL_ID;
        data2[2] = (byte)0x00;
        data2[3] = K2600_ID;
        data2[4] = PARAM_VALUE_MSG;
        data2[5] = EOX;
        
        tryToSendSysex(data2);
        setSendMIDI(val);
        }
    
    }
