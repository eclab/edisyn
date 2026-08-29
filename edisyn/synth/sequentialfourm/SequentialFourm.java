/***
    Copyright 2026 by Raphaël Jungers
    Licensed under the Apache License version 2.0
*/

/**
   Patch editor and librarian for the Sequential Fourm synthesizer.

   MIDI Implementation reference: Fourm-MIDI-Implementation-Document_V1.1.pdf

   SysEx format:
     Manufacturer ID: 0x01 (Sequential)
     Device ID:       0x3B (Fourm)

   Program data is 4103 raw bytes (verified: 4103 -> 4690 packed MIDI bytes).
   Sequential "packed MS bit" format: 8-byte packets (1 MS-bit byte + 7 data bytes).

   NRPN# = byte_offset + 1. Name offset 88 (bytes 88-107) verified with hardware.
   Multi-byte params: osc1freq/osc2freq (0-1400, bytes 2+3 and 4+5),
   filtercutoff (0-1023, bytes 26+27). Big-endian (first byte is high byte) --
   verified against a real hardware bulk dump (Sequential-Fourm.bulk.syx);
   the little-endian reading put every real patch's value out of range.
*/

package edisyn.synth.sequentialfourm;

import edisyn.*;
import edisyn.gui.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.sound.midi.*;
import javax.swing.*;

public class SequentialFourm extends Synth
    {
    static final byte FOURM_ID = 0x3B;
    static final int NAME_OFFSET = 88;   // NRPN 89-108 -> bytes 88-107; verified with hardware
    static final int NAME_LENGTH = 20;
    static final int DATA_LENGTH = 4103;
    static final int NRPN_COUNT = 726;

    static final String[] ALL_BANKS = { "U1", "U2", "F1", "F2" };
    static final String[] WRITEABLE_BANKS = { "U1", "U2" };

    // ---- Chooser option strings ----

    static final String[] GLIDE_MODES = { "Fixed Rate", "Fixed Rate A", "Fixed Time", "Fixed Time A" };
    static final String[] KEY_MODES = { "Poly", "Mono", "Unison" };
    static final String[] NOISE_TYPES = { "White", "Pink", "Digital", "Red" };
    static final String[] LFO_SH_TYPES = { "S/H", "Random", "Pink", "White", "Violet", "DC" };
    static final String[] MOD_SRC_ROUTES = { "Off", "Pos", "Full" };
    static final String[] MOD_DST_SRCS = { "Off", "Filter Env", "Osc B", "LFO" };
    static final String[] AT_DESTS = { "Off", "Filter", "Amp", "LFO" };
    static final String[] ARP_MODES = { "Up", "Down", "Up+Down1", "Up+Down2", "Random", "Assign", "Seq Note", "Seq Mod" };
    static final String[] ARP_RANGES = { "1 Oct", "2 Oct", "3 Oct" };
    static final String[] ARP_REPEATS = { "Off", "1", "2", "3" };
    static final String[] SEQ_PLAY_MODES = { "Retrig", "Continue", "One Shot", "Step" };
    static final String[] SEQ_MOD_DESTS = { "Osc Freq A", "Osc Freq B", "Filter Cutoff", "MOD 1 Amt",
        "MOD 2 Amt", "MOD 3 Amt", "LFO Freq", "Pulse Width A", "Pulse Width B", "Pulse Width All", "Feedback" };
    static final String[] CLOCK_DIVS = { "32nd", "16th", "8th", "8th Trip", "Quarter", "Qtr Trip",
        "Half", "Half Trip", "Whole", "Dot 8th", "Dot Qtr" };
    static final String[] CATEGORIES = { "Misc", "Pad", "Lead", "Bass", "Poly", "Keys", "String",
        "Pluck", "Bell", "Arp", "Brass", "Voice", "Organ", "Percussion", "Tuned Percussion", "SFX" };
    static final String[] NOTES = { "C", "C#", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B" };

    // ---- Parameter table: index = NRPN# - 1 = byte offset in unpacked dump ----
    // A few entries deviate from the MIDI doc's NRPN order or pack the value into
    // partial bits; see FOURM.md in this directory for the full list.

    static final String[] PARAMETERS;
    static final HashMap<String, Integer> PARAMETERS_MAP = new HashMap<>();
    static
        {
        PARAMETERS = new String[NRPN_COUNT];
        Arrays.fill(PARAMETERS, "---");

        PARAMETERS[0]  = "osc1octave";
        PARAMETERS[1]  = "osc2octave";
        PARAMETERS[2]  = "osc1freq";        // 0-1400, 2-byte big-endian: low bits in PARAMETERS[3]
        PARAMETERS[4]  = "osc2freq";        // 0-1400, 2-byte big-endian: low bits in PARAMETERS[5]
        PARAMETERS[6]  = "osc1sync";
        PARAMETERS[7]  = "osc2tri";
        PARAMETERS[8]  = "osc1saw";
        PARAMETERS[9]  = "osc2saw";
        PARAMETERS[10] = "osc1pulse";
        PARAMETERS[11] = "osc2pulse";
        PARAMETERS[12] = "osc1pw";
        PARAMETERS[13] = "osc2pw";
        PARAMETERS[14] = "osc1level";
        PARAMETERS[15] = "osc2level";
        PARAMETERS[16] = "feedbacklevel";
        PARAMETERS[17] = "feedbackon";
        PARAMETERS[18] = "noiselevel";
        PARAMETERS[19] = "noiseon";
        PARAMETERS[20] = "noisetype";
        PARAMETERS[22] = "glideon";
        PARAMETERS[23] = "glidemode";
        PARAMETERS[24] = "gliderate";
        PARAMETERS[25] = "pitchbendrange";
        PARAMETERS[26] = "filtercutoff";    // 0-1023, 2-byte big-endian: low bits in PARAMETERS[27]
        PARAMETERS[28] = "filterres";       // 0-255
        PARAMETERS[29] = "filterkeyamt";
        PARAMETERS[30] = "filterkeytrack";
        PARAMETERS[32] = "lfosync";
        PARAMETERS[33] = "lfofreq";         // 0-255
        PARAMETERS[34] = "lfofreqsync";     // 0-15
        PARAMETERS[35] = "lfoshape";        // 0-15
        PARAMETERS[36] = "lforevsaw";
        PARAMETERS[37] = "lfoshtype";       // 0-5
        PARAMETERS[39] = "filtenvattack";   // 0-255
        PARAMETERS[40] = "ampenvattack";    // 0-255
        PARAMETERS[41] = "filtenvdecay";    // 0-255
        PARAMETERS[42] = "ampenvdecay";     // 0-255
        PARAMETERS[43] = "filtenvrelease";  // 0-255
        PARAMETERS[44] = "ampenvsustain";   // 0-127
        PARAMETERS[45] = "filtenvsustain";  // 0-127
        PARAMETERS[46] = "ampenvrelease";   // 0-255
        PARAMETERS[47] = "filtenvamt";
        PARAMETERS[48] = "ampenvvelon";
        PARAMETERS[49] = "envretrig";
        PARAMETERS[50] = "ampenvvelamt";
        PARAMETERS[51] = "filtenvvelamt";
        PARAMETERS[52] = "filtenvvelon";
        PARAMETERS[53] = "ampenvamt";       // 0-255
        PARAMETERS[55] = "voicevolume";
        PARAMETERS[56] = "vintage";
        PARAMETERS[57] = "unisondetune";
        PARAMETERS[58] = "unisonon";
        PARAMETERS[59] = "unisonvoices";
        PARAMETERS[60] = "unisonnote1";
        PARAMETERS[61] = "unisonnote2";
        PARAMETERS[62] = "unisonnote3";
        PARAMETERS[63] = "unisonnote4";
        PARAMETERS[64] = "modsrcfiltenvamt";
        PARAMETERS[65] = "modsrcoscbroute";
        PARAMETERS[66] = "modsrclforoute";
        PARAMETERS[67] = "modsrcfiltenvroute";
        PARAMETERS[68] = "modsrcoscbamt";     // 0-254
        PARAMETERS[69] = "modsrclfoamt";      // 0-254
        PARAMETERS[70] = "modsrcatamt";       // 0-254
        PARAMETERS[71] = "moddstfreqasrc";
        PARAMETERS[72] = "moddstfreqbsrc";
        PARAMETERS[73] = "moddstpwasrc";
        PARAMETERS[74] = "moddstpwbsrc";
        PARAMETERS[75] = "moddstcutoffsrc";
        PARAMETERS[76] = "moddstampsrc";
        PARAMETERS[77] = "moddstlfofrqsrc";
        PARAMETERS[78] = "moddstlfoamtsrc";
        PARAMETERS[79] = "moddstatdest1";
        PARAMETERS[80] = "moddstatdest2";
        PARAMETERS[81] = "moddstatdest3";
        PARAMETERS[82] = "keymode";
        PARAMETERS[83] = "scale";           // 0-65
        PARAMETERS[84] = "transpose";       // 0-4
        PARAMETERS[85] = "category";        // 0-15
        PARAMETERS[86] = "clockdiv";        // 0-10
        PARAMETERS[87] = "clockbpm";        // 30-250
        // [88-107]: name bytes, handled separately
        // [108]: editor byte, skip
        PARAMETERS[109] = "arpon";
        PARAMETERS[110] = "arprange";
        PARAMETERS[111] = "arpmode";
        PARAMETERS[112] = "arprepeat";
        PARAMETERS[113] = "arprelatch";
        PARAMETERS[114] = "arpbeatsync";
        PARAMETERS[115] = "seqreset";
        PARAMETERS[116] = "seqplaymode";
        PARAMETERS[117] = "seqmoddest";

        for (int t = 0; t < 4; t++)
            for (int s = 0; s < 8; s++)
                PARAMETERS[118 + t * 8 + s] = "seq" + (t + 1) + "rest" + (s + 1);
        for (int t = 0; t < 4; t++)
            for (int s = 0; s < 8; s++)
                PARAMETERS[150 + t * 8 + s] = "seq" + (t + 1) + "tie" + (s + 1);
        for (int t = 0; t < 4; t++)
            for (int s = 0; s < 64; s++)
                PARAMETERS[182 + t * 64 + s] = "seq" + (t + 1) + "note" + (s + 1);
        for (int t = 0; t < 4; t++)
            for (int s = 0; s < 64; s++)
                PARAMETERS[438 + t * 64 + s] = "seq" + (t + 1) + "vel" + (s + 1);
        for (int t = 0; t < 4; t++)
            for (int s = 0; s < 8; s++)
                PARAMETERS[694 + t * 8 + s] = "seq" + (t + 1) + "glide" + (s + 1);

        for (int i = 0; i < PARAMETERS.length; i++)
            if (!PARAMETERS[i].equals("---"))
                PARAMETERS_MAP.put(PARAMETERS[i], i);
        }

    // ---- Parameters that only occupy the low bits of their byte (see FOURM.md) ----
    // The high bits hold an unidentified value; preserved round-trip in a shadow
    // "<key>hibits" model key rather than discarded.
    static final HashMap<String, Integer> MASKED_PARAMS = new HashMap<>();
    static
        {
        MASKED_PARAMS.put("osc1sync", 0x01);
        MASKED_PARAMS.put("arpon", 0x01);
        MASKED_PARAMS.put("lforevsaw", 0x01);
        MASKED_PARAMS.put("noisetype", 0x03);
        MASKED_PARAMS.put("moddstfreqasrc", 0x03);
        MASKED_PARAMS.put("seqplaymode", 0x03);
        MASKED_PARAMS.put("pitchbendrange", 0x0F);
        MASKED_PARAMS.put("lfofreqsync", 0x0F);
        }

    // ---- Constructor ----

    public SequentialFourm()
        {
        SynthPanel globalPanel = new SynthPanel(this);
        globalPanel.makePasteable("global");
        VBox vbox = new VBox();
        vbox.add(buildGlobalPanel(Style.COLOR_GLOBAL()));
        globalPanel.add(vbox, BorderLayout.CENTER);
        addTab("Global", globalPanel);

        SynthPanel oscPanel = new SynthPanel(this);
        oscPanel.makePasteable("osc");
        vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(buildOsc1Panel(Style.COLOR_A()));
        hbox.addLast(buildOsc2Panel(Style.COLOR_A()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(buildMixPanel(Style.COLOR_A()));
        hbox.addLast(buildGlidePanel(Style.COLOR_A()));
        vbox.add(hbox);
        oscPanel.add(vbox, BorderLayout.CENTER);
        addTab("Oscillators", oscPanel);

        SynthPanel filtPanel = new SynthPanel(this);
        filtPanel.makePasteable("filt");
        vbox = new VBox();
        hbox = new HBox();
        hbox.add(buildFilterPanel(Style.COLOR_B()));
        hbox.addLast(buildFilterEnvPanel(Style.COLOR_B()));
        vbox.add(hbox);
        filtPanel.add(vbox, BorderLayout.CENTER);
        addTab("Filter", filtPanel);

        SynthPanel envPanel = new SynthPanel(this);
        envPanel.makePasteable("env");
        vbox = new VBox();
        hbox = new HBox();
        hbox.add(buildAmpEnvPanel(Style.COLOR_C()));
        hbox.addLast(buildLFOPanel(Style.COLOR_C()));
        vbox.add(hbox);
        envPanel.add(vbox, BorderLayout.CENTER);
        addTab("Envelope + LFO", envPanel);

        SynthPanel modPanel = new SynthPanel(this);
        modPanel.makePasteable("mod");
        vbox = new VBox();
        vbox.add(buildModPanel(Style.COLOR_C()));
        modPanel.add(vbox, BorderLayout.CENTER);
        addTab("Modulation", modPanel);

        SynthPanel arpPanel = new SynthPanel(this);
        arpPanel.makePasteable("arp");
        vbox = new VBox();
        hbox = new HBox();
        hbox.add(buildArpPanel(Style.COLOR_A()));
        hbox.addLast(buildSeqControlPanel(Style.COLOR_A()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.addLast(buildUnisonPanel(Style.COLOR_B()));
        vbox.add(hbox);
        arpPanel.add(vbox, BorderLayout.CENTER);
        addTab("Arp / Voice", arpPanel);

        for (int t = 1; t <= 4; t++)
            {
            SynthPanel seqPanel = new SynthPanel(this);
            seqPanel.makePasteable("seq" + t);
            VBox vbox2 = new VBox();
            vbox2.addLast(buildSeqTrackPanel(t));
            seqPanel.add(vbox2, BorderLayout.CENTER);
            addTab("Seq " + t, seqPanel);
            }

        model.set("name", "Init");
        model.set("bank", 0);
        model.set("number", 0);
        loadDefaults();
        }

    // ---- Panel builders ----

    JComponent buildGlobalPanel(Color color)
        {
        Category category = new Category(this, "Sequential Fourm", color);
        category.makePasteable("global");
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        JComponent comp = new PatchDisplay(this, 4);
        hbox.add(comp);

        comp = new StringComponent("Patch Name", this, "name", NAME_LENGTH, "Name must be up to 20 characters.")
            {
            public String replace(String val) { return revisePatchName(val); }
            public void update(String key, Model model) { super.update(key, model); updateTitle(); }
            };
        vbox.add(comp);
        comp = new Chooser("Category", this, "category", CATEGORIES);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Volume", this, "voicevolume", color, 0, 127);
        hbox.add(comp);
        comp = new LabelledDial("Scale", this, "scale", color, 0, 65);
        hbox.add(comp);
        comp = new LabelledDial("Transpose", this, "transpose", color, 0, 4)
            {
            public String map(int val) { return String.valueOf(val - 2); }
            };
        hbox.add(comp);
        comp = new LabelledDial("Pitch Bend", this, "pitchbendrange", color, 0, 12);
        hbox.add(comp);
        comp = new LabelledDial("Clock BPM", this, "clockbpm", color, 30, 250);
        hbox.add(comp);
        comp = new Chooser("Clock Div", this, "clockdiv", CLOCK_DIVS);
        hbox.add(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }

    JComponent buildOsc1Panel(Color color)
        {
        Category category = new Category(this, "Oscillator 1", color);
        category.makePasteable("osc1");
        HBox hbox = new HBox();

        JComponent comp = new LabelledDial("Octave", this, "osc1octave", color, 0, 4);
        hbox.add(comp);
        comp = new LabelledDial("Freq", this, "osc1freq", color, 0, 1400);
        hbox.add(comp);
        comp = new LabelledDial("PW", this, "osc1pw", color, 0, 127);
        hbox.add(comp);
        comp = new LabelledDial("Level", this, "osc1level", color, 0, 127);
        hbox.add(comp);

        VBox vbox = new VBox();
        comp = new CheckBox("Sync", this, "osc1sync");
        vbox.add(comp);
        comp = new CheckBox("Saw", this, "osc1saw");
        vbox.add(comp);
        comp = new CheckBox("Pulse", this, "osc1pulse");
        vbox.add(comp);
        hbox.addLast(vbox);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }

    JComponent buildOsc2Panel(Color color)
        {
        Category category = new Category(this, "Oscillator 2", color);
        category.makePasteable("osc2");
        HBox hbox = new HBox();

        JComponent comp = new LabelledDial("Octave", this, "osc2octave", color, 0, 6);
        hbox.add(comp);
        comp = new LabelledDial("Freq", this, "osc2freq", color, 0, 1400);
        hbox.add(comp);
        comp = new LabelledDial("PW", this, "osc2pw", color, 0, 127);
        hbox.add(comp);
        comp = new LabelledDial("Level", this, "osc2level", color, 0, 127);
        hbox.add(comp);

        VBox vbox = new VBox();
        comp = new CheckBox("Tri", this, "osc2tri");
        vbox.add(comp);
        comp = new CheckBox("Saw", this, "osc2saw");
        vbox.add(comp);
        comp = new CheckBox("Pulse", this, "osc2pulse");
        vbox.add(comp);
        hbox.addLast(vbox);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }

    JComponent buildMixPanel(Color color)
        {
        Category category = new Category(this, "Mix", color);
        category.makePasteable("mix");
        HBox hbox = new HBox();

        VBox vbox = new VBox();
        JComponent comp = new CheckBox("Feedback On", this, "feedbackon");
        vbox.add(comp);
        hbox.add(vbox);
        comp = new LabelledDial("Feedback", this, "feedbacklevel", color, 0, 127);
        hbox.add(comp);

        vbox = new VBox();
        comp = new CheckBox("Noise On", this, "noiseon");
        vbox.add(comp);
        hbox.add(vbox);
        comp = new LabelledDial("Noise Level", this, "noiselevel", color, 0, 127);
        hbox.add(comp);
        comp = new Chooser("Noise Type", this, "noisetype", NOISE_TYPES);
        hbox.add(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }

    JComponent buildGlidePanel(Color color)
        {
        Category category = new Category(this, "Glide", color);
        category.makePasteable("glide");
        HBox hbox = new HBox();

        VBox vbox = new VBox();
        JComponent comp = new CheckBox("Glide On", this, "glideon");
        vbox.add(comp);
        hbox.add(vbox);
        comp = new Chooser("Glide Mode", this, "glidemode", GLIDE_MODES);
        hbox.add(comp);
        comp = new LabelledDial("Glide Rate", this, "gliderate", color, 0, 127);
        hbox.add(comp);
        comp = new LabelledDial("Vintage", this, "vintage", color, 0, 127);
        hbox.addLast(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }

    JComponent buildFilterPanel(Color color)
        {
        Category category = new Category(this, "Filter", color);
        category.makePasteable("filt");
        HBox hbox = new HBox();

        JComponent comp = new LabelledDial("Cutoff", this, "filtercutoff", color, 0, 1023);
        hbox.add(comp);
        comp = new LabelledDial("Resonance", this, "filterres", color, 0, 255);
        hbox.add(comp);
        comp = new LabelledDial("Key Amt", this, "filterkeyamt", color, 0, 255);
        hbox.add(comp);

        VBox vbox = new VBox();
        comp = new CheckBox("Key Track", this, "filterkeytrack");
        vbox.add(comp);
        hbox.addLast(vbox);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }

    JComponent buildFilterEnvPanel(Color color)
        {
        Category category = new Category(this, "Filter Envelope", color);
        category.makePasteable("filtenv");
        HBox hbox = new HBox();

        JComponent comp = new LabelledDial("Attack", this, "filtenvattack", color, 0, 255);
        hbox.add(comp);
        comp = new LabelledDial("Decay", this, "filtenvdecay", color, 0, 255);
        hbox.add(comp);
        comp = new LabelledDial("Sustain", this, "filtenvsustain", color, 0, 127);
        hbox.add(comp);
        comp = new LabelledDial("Release", this, "filtenvrelease", color, 0, 255);
        hbox.add(comp);
        comp = new LabelledDial("Amount", this, "filtenvamt", color, 0, 255);
        hbox.add(comp);
        comp = new LabelledDial("Vel Amt", this, "filtenvvelamt", color, 0, 127);
        hbox.add(comp);

        VBox vbox = new VBox();
        comp = new CheckBox("Vel On", this, "filtenvvelon");
        vbox.add(comp);
        comp = new CheckBox("Retrig", this, "envretrig");
        vbox.add(comp);
        hbox.addLast(vbox);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }

    JComponent buildAmpEnvPanel(Color color)
        {
        Category category = new Category(this, "Amp Envelope", color);
        category.makePasteable("ampenv");
        HBox hbox = new HBox();

        JComponent comp = new LabelledDial("Attack", this, "ampenvattack", color, 0, 255);
        hbox.add(comp);
        comp = new LabelledDial("Decay", this, "ampenvdecay", color, 0, 255);
        hbox.add(comp);
        comp = new LabelledDial("Sustain", this, "ampenvsustain", color, 0, 127);
        hbox.add(comp);
        comp = new LabelledDial("Release", this, "ampenvrelease", color, 0, 255);
        hbox.add(comp);
        comp = new LabelledDial("Amount", this, "ampenvamt", color, 0, 255);
        hbox.add(comp);
        comp = new LabelledDial("Vel Amt", this, "ampenvvelamt", color, 0, 127);
        hbox.add(comp);

        VBox vbox = new VBox();
        comp = new CheckBox("Vel On", this, "ampenvvelon");
        vbox.add(comp);
        hbox.addLast(vbox);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }

    JComponent buildLFOPanel(Color color)
        {
        Category category = new Category(this, "LFO", color);
        category.makePasteable("lfo");
        HBox hbox = new HBox();

        JComponent comp = new LabelledDial("Freq", this, "lfofreq", color, 0, 255);
        hbox.add(comp);
        comp = new LabelledDial("Freq Sync", this, "lfofreqsync", color, 0, 15);
        hbox.add(comp);
        comp = new LabelledDial("Shape", this, "lfoshape", color, 0, 15);
        hbox.add(comp);
        comp = new Chooser("S/H Type", this, "lfoshtype", LFO_SH_TYPES);
        hbox.add(comp);

        VBox vbox = new VBox();
        comp = new CheckBox("Sync", this, "lfosync");
        vbox.add(comp);
        comp = new CheckBox("Rev Saw", this, "lforevsaw");
        vbox.add(comp);
        hbox.addLast(vbox);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }

    JComponent buildModPanel(Color color)
        {
        Category category = new Category(this, "Modulation", color);
        category.makePasteable("mod");
        HBox hbox = new HBox();

        // Mod sources
        VBox vbox = new VBox();
        JComponent comp = new Chooser("Filt Env Route", this, "modsrcfiltenvroute", MOD_SRC_ROUTES);
        vbox.add(comp);
        comp = new LabelledDial("Filt Env Amt", this, "modsrcfiltenvamt", color, 0, 254);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        comp = new Chooser("Osc B Route", this, "modsrcoscbroute", MOD_SRC_ROUTES);
        vbox.add(comp);
        comp = new LabelledDial("Osc B Amt", this, "modsrcoscbamt", color, 0, 254);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        comp = new Chooser("LFO Route", this, "modsrclforoute", MOD_SRC_ROUTES);
        vbox.add(comp);
        comp = new LabelledDial("LFO Amt", this, "modsrclfoamt", color, 0, 254);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        comp = new LabelledDial("AT Amt", this, "modsrcatamt", color, 0, 254);
        vbox.add(comp);
        hbox.add(vbox);

        // Separator
        hbox.add(new JSeparator(JSeparator.VERTICAL));

        // Mod destinations
        vbox = new VBox();
        comp = new Chooser("Freq A Src", this, "moddstfreqasrc", MOD_DST_SRCS);
        vbox.add(comp);
        comp = new Chooser("Freq B Src", this, "moddstfreqbsrc", MOD_DST_SRCS);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        comp = new Chooser("PW A Src", this, "moddstpwasrc", MOD_DST_SRCS);
        vbox.add(comp);
        comp = new Chooser("PW B Src", this, "moddstpwbsrc", MOD_DST_SRCS);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        comp = new Chooser("Cutoff Src", this, "moddstcutoffsrc", MOD_DST_SRCS);
        vbox.add(comp);
        comp = new Chooser("Amp Src", this, "moddstampsrc", MOD_DST_SRCS);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        comp = new Chooser("LFO Freq Src", this, "moddstlfofrqsrc", MOD_DST_SRCS);
        vbox.add(comp);
        comp = new Chooser("LFO Amt Src", this, "moddstlfoamtsrc", MOD_DST_SRCS);
        vbox.add(comp);
        hbox.add(vbox);

        vbox = new VBox();
        comp = new Chooser("AT Dest 1", this, "moddstatdest1", AT_DESTS);
        vbox.add(comp);
        comp = new Chooser("AT Dest 2", this, "moddstatdest2", AT_DESTS);
        vbox.add(comp);
        comp = new Chooser("AT Dest 3", this, "moddstatdest3", AT_DESTS);
        vbox.add(comp);
        hbox.addLast(vbox);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }

    JComponent buildArpPanel(Color color)
        {
        Category category = new Category(this, "Arpeggiator", color);
        category.makePasteable("arp");
        HBox hbox = new HBox();

        VBox vbox = new VBox();
        JComponent comp = new CheckBox("Arp On", this, "arpon");
        vbox.add(comp);
        comp = new CheckBox("Relatch", this, "arprelatch");
        vbox.add(comp);
        comp = new CheckBox("Beat Sync", this, "arpbeatsync");
        vbox.add(comp);
        hbox.add(vbox);

        comp = new Chooser("Mode", this, "arpmode", ARP_MODES);
        hbox.add(comp);
        comp = new Chooser("Range", this, "arprange", ARP_RANGES);
        hbox.add(comp);
        comp = new Chooser("Repeat", this, "arprepeat", ARP_REPEATS);
        hbox.addLast(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }

    JComponent buildSeqControlPanel(Color color)
        {
        Category category = new Category(this, "Sequencer", color);
        category.makePasteable("seqctrl");
        HBox hbox = new HBox();

        VBox vbox = new VBox();
        JComponent comp = new CheckBox("Reset", this, "seqreset");
        vbox.add(comp);
        hbox.add(vbox);

        comp = new Chooser("Play Mode", this, "seqplaymode", SEQ_PLAY_MODES);
        hbox.add(comp);
        comp = new Chooser("Mod Dest", this, "seqmoddest", SEQ_MOD_DESTS);
        hbox.addLast(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }

    JComponent buildUnisonPanel(Color color)
        {
        Category category = new Category(this, "Voice", color);
        category.makePasteable("voice");
        HBox hbox = new HBox();

        VBox vbox = new VBox();
        JComponent comp = new CheckBox("Unison On", this, "unisonon");
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Voices", this, "unisonvoices", color, 0, 4);
        hbox.add(comp);
        comp = new LabelledDial("Detune", this, "unisondetune", color, 0, 127);
        hbox.add(comp);
        comp = new LabelledDial("Note 1", this, "unisonnote1", color, 0, 127) { public String map(int val) { return val == 127 ? "Off" : "" + val; } };
        hbox.add(comp);
        comp = new LabelledDial("Note 2", this, "unisonnote2", color, 0, 127) { public String map(int val) { return val == 127 ? "Off" : "" + val; } };
        hbox.add(comp);
        comp = new LabelledDial("Note 3", this, "unisonnote3", color, 0, 127) { public String map(int val) { return val == 127 ? "Off" : "" + val; } };
        hbox.add(comp);
        comp = new LabelledDial("Note 4", this, "unisonnote4", color, 0, 127) { public String map(int val) { return val == 127 ? "Off" : "" + val; } };
        hbox.add(comp);
        comp = new Chooser("Key Mode", this, "keymode", KEY_MODES);
        hbox.addLast(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }

    // ---- Sequencer step editor ----

    JComponent buildSeqTrackPanel(final int track)
        {
        final JComponent typical = buildSeqStep(track, 1, Style.COLOR_A());
        final int stepH = typical.getPreferredSize().height;

        VBox steps = new VBox()
            {
            public Dimension getPreferredScrollableViewportSize()
                {
                Dimension size = getPreferredSize();
                size.height = stepH * 4;
                return size;
                }
            };

        for (int s = 1; s <= 64; s++)
            steps.add(buildSeqStep(track, s, (s % 2 == 1 ? Style.COLOR_A() : Style.COLOR_B())));

        JScrollPane pane = new JScrollPane(steps,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pane.getViewport().setBackground(Style.BACKGROUND_COLOR());
        pane.setBorder(null);
        return pane;
        }

    JComponent buildSeqStep(int track, int step, Color color)
        {
        Category category = new Category(this, "Step " + step, color);
        category.makePasteable("seq" + track);
        category.makeDistributable("seq" + track);
        HBox hbox = new HBox();

        JComponent comp = new LabelledDial("Note", this, "seq" + track + "note" + step, color, 0, 128)
            {
            public String map(int val)
                {
                if (val == 128) return "Off";
                return NOTES[val % 12] + " " + ((val / 12) - 2);
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Velocity", this, "seq" + track + "vel" + step, color, 0, 128)
            {
            public String map(int val)
                {
                return val == 128 ? "Off" : "" + val;
                }
            };
        hbox.add(comp);

        VBox vbox = new VBox();
        comp = new CheckBox("Rest", this, "seq" + track + "stepr" + step);
        vbox.add(comp);
        comp = new CheckBox("Tie", this, "seq" + track + "stept" + step);
        vbox.add(comp);
        comp = new CheckBox("Glide", this, "seq" + track + "stepg" + step);
        vbox.add(comp);
        hbox.addLast(vbox);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }

    // ---- Patch location ----

    public String getPatchLocationName(Model model)
        {
        if (!model.exists("bank") || !model.exists("number")) return null;
        int bank = model.get("bank", 0);
        int num = model.get("number", 0) + 1;
        String numStr = (num < 10 ? "00" : num < 100 ? "0" : "") + num;
        return ALL_BANKS[bank] + "-" + numStr;
        }

    public Model getNextPatchLocation(Model model)
        {
        int bank = model.get("bank", 0);
        int num  = model.get("number", 0);
        num++;
        if (num > 127) { num = 0; bank = (bank + 1) % ALL_BANKS.length; }
        Model next = buildModel();
        next.set("bank", bank);
        next.set("number", num);
        return next;
        }

    public boolean patchLocationEquals(Model patch1, Model patch2)
        {
        return patch1.get("bank", 0) == patch2.get("bank", 0) &&
               patch1.get("number", 0) == patch2.get("number", 0);
        }

    // ---- Patch name ----

    public String getPatchName(Model model) { return model.get("name", "Init"); }

    public String revisePatchName(String name)
        {
        name = super.revisePatchName(name);
        if (name.length() > NAME_LENGTH) name = name.substring(0, NAME_LENGTH);
        StringBuilder sb = new StringBuilder(NAME_LENGTH);
        for (int i = 0; i < name.length(); i++)
            {
            char c = name.charAt(i);
            sb.append((c >= 32 && c < 127) ? c : ' ');
            }
        while (sb.length() < NAME_LENGTH) sb.append(' ');
        return sb.toString();
        }

    // ---- Patch I/O ----

    public boolean gatherPatchInfo(String title, Model changeThis, boolean writing)
        {
        String[] banks = writing ? WRITEABLE_BANKS : ALL_BANKS;
        JComboBox<String> bank = new JComboBox<>(banks);
        bank.setSelectedIndex(writing
            ? Math.min(changeThis.get("bank", 0), WRITEABLE_BANKS.length - 1)
            : changeThis.get("bank", 0));
        int num = changeThis.get("number", 0) + 1;
        JTextField number = new SelectedTextField(
            (num < 10 ? "00" : num < 100 ? "0" : "") + num, 3);

        while (true)
            {
            boolean result = showMultiOption(this,
                new String[]{ "Bank", "Patch Number" },
                new JComponent[]{ bank, number },
                title, "Enter the Bank and Patch Number");
            if (!result) return false;
            int n;
            try { n = Integer.parseInt(number.getText()); }
            catch (NumberFormatException e)
                {
                showSimpleError(title, "The Patch Number must be an integer 1...128");
                continue;
                }
            if (n < 1 || n > 128)
                {
                showSimpleError(title, "The Patch Number must be an integer 1...128");
                continue;
                }
            changeThis.set("bank", bank.getSelectedIndex());
            changeThis.set("number", n - 1);
            return true;
            }
        }

    public void changePatch(Model tempModel)
        {
        int bank = tempModel.get("bank", 0);
        int num  = tempModel.get("number", 0);
        tryToSendMIDI(new Object[]{
            buildCC(getChannelOut(), 0, bank)[0],
            buildCC(getChannelOut(), 32, bank)[0],
            buildPC(getChannelOut(), num)[0]
            });
        }

    public int getPauseAfterChangePatch() { return 200; }

    public byte[] requestDump(Model tempModel)
        {
        return new byte[]{
            (byte)0xF0, (byte)0x01, FOURM_ID, (byte)0x05,
            (byte)tempModel.get("bank", 0),
            (byte)tempModel.get("number", 0),
            (byte)0xF7
            };
        }

    public byte[] requestCurrentDump()
        {
        return new byte[]{ (byte)0xF0, (byte)0x01, FOURM_ID, (byte)0x06, (byte)0xF7 };
        }

    public int parse(byte[] data, boolean fromFile)
        {
        if (data[3] == 0x02)
            {
            model.set("bank", data[4] & 0x03);
            model.set("number", data[5] & 0x7F);
            }

        int offset = (data[3] == 0x02) ? 6 : 4;
        byte[] raw = unpackFrom7Bit(data, offset);

        // Single-byte parameters
        for (int i = 0; i < Math.min(NRPN_COUNT, raw.length); i++)
            {
            String key = PARAMETERS[i];
            if (key.equals("---") || key.equals("osc1freq") || key.equals("osc2freq") || key.equals("filtercutoff") ||
                MASKED_PARAMS.get(key) != null)
                continue;
            model.set(key, raw[i] & 0xFF);
            }

        // Multi-byte parameters (big-endian 16-bit: verified against a hardware bulk dump,
        // where the little-endian reading put nearly every real patch's value out of range)
        if (raw.length > 3)
            model.set("osc1freq", Math.min(((raw[2] & 0xFF) << 8) | (raw[3] & 0xFF), 1400));
        if (raw.length > 5)
            model.set("osc2freq", Math.min(((raw[4] & 0xFF) << 8) | (raw[5] & 0xFF), 1400));
        if (raw.length > 27)
            model.set("filtercutoff", Math.min(((raw[26] & 0xFF) << 8) | (raw[27] & 0xFF), 1023));

        // Parameters that only occupy the low bits of their byte (see MASKED_PARAMS)
        for (String key : MASKED_PARAMS.keySet())
            {
            int idx = PARAMETERS_MAP.get(key);
            if (idx < raw.length)
                {
                int mask = MASKED_PARAMS.get(key);
                int rawByte = raw[idx] & 0xFF;
                model.set(key, rawByte & mask);
                model.set(key + "hibits", rawByte & ~mask & 0xFF);
                }
            }

        // Name
        if (raw.length >= NAME_OFFSET + NAME_LENGTH)
            {
            byte[] nameBytes = new byte[NAME_LENGTH];
            System.arraycopy(raw, NAME_OFFSET, nameBytes, 0, NAME_LENGTH);
            try { model.set("name", new String(nameBytes, "US-ASCII").trim()); }
            catch (UnsupportedEncodingException e) { Synth.handleException(e); }
            }

        // Remaining bytes beyond the NRPN table
        for (int i = NRPN_COUNT; i < raw.length; i++)
            model.set("b" + i, raw[i] & 0xFF);

        // Expand rest/tie/glide byte keys into per-step boolean keys for the UI
        for (int t = 1; t <= 4; t++)
            for (int n = 1; n <= 8; n++)
                {
                int r  = model.get("seq" + t + "rest"  + n, 0);
                int ti = model.get("seq" + t + "tie"   + n, 0);
                int g  = model.get("seq" + t + "glide" + n, 0);
                for (int b = 0; b < 8; b++)
                    {
                    int s = (n - 1) * 8 + b + 1;
                    model.set("seq" + t + "stepr" + s, (r  >> b) & 1);
                    model.set("seq" + t + "stept" + s, (ti >> b) & 1);
                    model.set("seq" + t + "stepg" + s, (g  >> b) & 1);
                    }
                }

        revise();
        return PARSE_SUCCEEDED;
        }

    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null) tempModel = getModel();

        byte[] raw = new byte[DATA_LENGTH];

        // Single-byte parameters
        for (int i = 0; i < NRPN_COUNT; i++)
            {
            String key = PARAMETERS[i];
            if (key.equals("---") || key.equals("osc1freq") || key.equals("osc2freq") || key.equals("filtercutoff") ||
                MASKED_PARAMS.get(key) != null)
                continue;
            raw[i] = (byte)(model.get(key, 0) & 0xFF);
            }

        // Parameters that only occupy the low bits of their byte (see MASKED_PARAMS / parse())
        for (String key : MASKED_PARAMS.keySet())
            {
            int idx = PARAMETERS_MAP.get(key);
            int mask = MASKED_PARAMS.get(key);
            raw[idx] = (byte)((model.get(key, 0) & mask) | (model.get(key + "hibits", 0) & ~mask & 0xFF));
            }

        // Multi-byte parameters (big-endian 16-bit, see parse())
        int v = model.get("osc1freq", 0);
        raw[2] = (byte)((v >> 8) & 0xFF); raw[3] = (byte)(v & 0xFF);
        v = model.get("osc2freq", 0);
        raw[4] = (byte)((v >> 8) & 0xFF); raw[5] = (byte)(v & 0xFF);
        v = model.get("filtercutoff", 0);
        raw[26] = (byte)((v >> 8) & 0xFF); raw[27] = (byte)(v & 0xFF);

        // Name
        String name = (model.get("name", "Init") + "                    ").substring(0, NAME_LENGTH);
        for (int i = 0; i < NAME_LENGTH; i++)
            raw[NAME_OFFSET + i] = (byte)(name.charAt(i) & 0x7F);

        // Remaining bytes (not in NRPN table)
        for (int i = NRPN_COUNT; i < DATA_LENGTH; i++)
            raw[i] = (byte)(model.get("b" + i, 0) & 0xFF);

        byte[] packed = packTo7Bit(raw);

        if (toWorkingMemory)
            {
            byte[] msg = new byte[4 + packed.length + 1];
            msg[0] = (byte)0xF0; msg[1] = (byte)0x01; msg[2] = FOURM_ID; msg[3] = (byte)0x03;
            System.arraycopy(packed, 0, msg, 4, packed.length);
            msg[msg.length - 1] = (byte)0xF7;
            return msg;
            }
        else
            {
            byte[] msg = new byte[6 + packed.length + 1];
            msg[0] = (byte)0xF0; msg[1] = (byte)0x01; msg[2] = FOURM_ID; msg[3] = (byte)0x02;
            msg[4] = (byte)tempModel.get("bank", 0);
            msg[5] = (byte)tempModel.get("number", 0);
            System.arraycopy(packed, 0, msg, 6, packed.length);
            msg[msg.length - 1] = (byte)0xF7;
            return msg;
            }
        }

    public Object[] emitAll(String key)
        {
        if (key.equals("bank") || key.equals("number")) return new Object[0];
        if (key.startsWith("b") && PARAMETERS_MAP.get(key) == null) return new Object[0];

        if (key.equals("name"))
            {
            Object[] ret = new Object[4 * NAME_LENGTH];
            String name = (model.get("name", "Init") + "                    ").substring(0, NAME_LENGTH);
            for (int i = 0; i < NAME_LENGTH; i++)
                {
                Object[] nrpn = buildNRPN(getChannelOut(), 89 + i, (int)name.charAt(i));
                System.arraycopy(nrpn, 0, ret, i * 4, 4);
                }
            return ret;
            }

        // Step-level rest/tie/glide flags: pack into the byte key and emit that NRPN
        if (key.startsWith("seq"))
            {
            String flagType = null, stepType = null;
            if (key.indexOf("stepr") >= 0)      { flagType = "rest";  stepType = "stepr"; }
            else if (key.indexOf("stept") >= 0) { flagType = "tie";   stepType = "stept"; }
            else if (key.indexOf("stepg") >= 0) { flagType = "glide"; stepType = "stepg"; }
            if (flagType != null)
                {
                int t = key.charAt(3) - '0';
                int s = Integer.parseInt(key.substring(4 + stepType.length()));
                int n = (s - 1) / 8 + 1;
                int byteVal = 0;
                for (int b = 0; b < 8; b++)
                    byteVal |= (model.get("seq" + t + stepType + ((n - 1) * 8 + b + 1), 0) & 1) << b;
                String byteKey = "seq" + t + flagType + n;
                boolean midi = getSendMIDI();
                setSendMIDI(false);
                model.set(byteKey, byteVal);
                setSendMIDI(midi);
                Integer bidx = PARAMETERS_MAP.get(byteKey);
                return bidx != null ? buildNRPN(getChannelOut(), bidx + 1, byteVal) : new Object[0];
                }
            }

        Integer idx = PARAMETERS_MAP.get(key);
        if (idx == null) return new Object[0];

        Integer mask = MASKED_PARAMS.get(key);
        if (mask != null)
            {
            int byteVal = (model.get(key, 0) & mask) | (model.get(key + "hibits", 0) & ~mask & 0xFF);
            return buildNRPN(getChannelOut(), idx + 1, byteVal);
            }

        return buildNRPN(getChannelOut(), idx + 1, model.get(key, 0));
        }

    public void handleSynthCCOrNRPN(Midi.CCData data)
        {
        if (data.type != Midi.CCDATA_TYPE_NRPN) return;
        int nrpn = data.number;

        if (nrpn >= 89 && nrpn <= 108)
            {
            char[] name = (model.get("name", "Init") + "                    ").toCharArray();
            name[nrpn - 89] = (char)(data.value & 0x7F);
            model.set("name", new String(name, 0, NAME_LENGTH).trim());
            return;
            }

        if (nrpn >= 1 && nrpn <= NRPN_COUNT)
            {
            String key = PARAMETERS[nrpn - 1];
            if (!key.equals("---"))
                {
                Integer mask = MASKED_PARAMS.get(key);
                if (mask != null)
                    {
                    model.set(key, data.value & mask);
                    model.set(key + "hibits", data.value & ~mask & 0xFF);
                    return;
                    }
                model.set(key, data.value);
                // Unpack rest/tie/glide bytes to per-step keys
                String flagType = null, stepType = null;
                if (key.indexOf("rest") > 0)        { flagType = "rest";  stepType = "stepr"; }
                else if (key.indexOf("tie") > 0)    { flagType = "tie";   stepType = "stept"; }
                else if (key.indexOf("glide") > 0)  { flagType = "glide"; stepType = "stepg"; }
                if (flagType != null)
                    {
                    int t = key.charAt(3) - '0';
                    int n = Integer.parseInt(key.substring(4 + flagType.length()));
                    boolean midi = getSendMIDI();
                    setSendMIDI(false);
                    for (int b = 0; b < 8; b++)
                        model.set("seq" + t + stepType + ((n - 1) * 8 + b + 1), (data.value >> b) & 1);
                    setSendMIDI(midi);
                    }
                }
            }
        }

    // ---- Librarian support ----

    public String[] getPatchNumberNames() { return buildIntegerNames(128, 1); }
    public String[] getBankNames() { return ALL_BANKS; }
    public boolean[] getWriteableBanks() { return new boolean[]{ true, true, false, false }; }
    public boolean getSupportsPatchWrites() { return true; }
    public int getPatchNameLength() { return NAME_LENGTH; }
    public boolean librarianTested() { return true; }

    public static String getSynthName() { return "Sequential Fourm"; }
    public String getDefaultResourceFileName() { return "SequentialFourm.init"; }
    public String getHTMLResourceFileName() { return "SequentialFourm.html"; }

    // ---- Pack/unpack (Sequential "packed MS bit" format) ----

    byte[] unpackFrom7Bit(byte[] data, int offset)
        {
        int packedLen = data.length - offset - 1;
        int size = packedLen / 8 * 7;
        if (packedLen % 8 > 0) size += packedLen % 8 - 1;
        byte[] out = new byte[size];
        int j = 0;
        for (int i = offset; i < data.length - 1; i += 8)
            {
            for (int x = 0; x < 7; x++)
                if (j + x < out.length)
                    out[j + x] = (byte)(data[i + x + 1] | (((data[i] >>> x) & 1) << 7));
            j += 7;
            }
        return out;
        }

    byte[] packTo7Bit(byte[] data)
        {
        int size = data.length / 7 * 8;
        if (data.length % 7 > 0) size += 1 + data.length % 7;
        byte[] out = new byte[size];
        int j = 0;
        for (int i = 0; i < data.length; i += 7)
            {
            for (int x = 0; x < 7; x++)
                if (i + x < data.length && j + x + 1 < out.length)
                    {
                    out[j + x + 1] = (byte)(data[i + x] & 0x7F);
                    out[j] = (byte)(out[j] | (((data[i + x] >>> 7) & 1) << x));
                    }
            j += 8;
            }
        return out;
        }
    }
