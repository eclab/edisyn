/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.korgsg;

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

/**
   A patch editor for the Korg SG RACK [Multi].  Note NOT for the SG Pro X.
        
   @author Sean Luke
*/

public class KorgSGMulti extends Synth
    {
    /// Various collections of parameter names for pop-up menus
    
    
    // It seems to me that the right ordering of these would be COLUMN order, not ROW order
    /*
      Concert, Studio, DancePiano, Dyna-Stage, Wurly EP, FM EP 1, Piano & EP, Funkamatic, SGX Organ, R&B Organ, Clav, Vibraphone, TheStrings, WhisperVox, SynthFlute, Acoustic,
      Bright, Rock Piano, Chorused, Classic EP, Dyno Bell, Wave EP 1, PF&Strings, FM&Analog, Velo \"B\", CX-3 Organ, PhaserClav, Bellphonic, Symphonic, Slow Waves, SynthBrass, Fretless,
      Classic, Jazz Piano, Mix Piano, Stage Bell, FM EP 2, Motion EP, MIDI Grant, EP&Strings, Perc Organ, Gospel Org, Mutronics, Crystaline, PadStrings, BreathyVox, Synth Air, FingerBass,
      Dynamic, Ballad, StagePiano, Stage Time, FM EP 3, Wave EP 2, Power Keys, EP Magic, Full Organ, Pipe Organ, Clavitar, BellString, StringsL&R, Voices, Synth Horn, Synth Bass
    */
    
    /*
      PianoLayer  B03     C07
      PF&Strings  B08     B07
      A.Bass/PF   A16     A02
      EP Layer    A06     D08
      EP&Strings  B08     A04
      BS/FM&Pad   B08     C16
      PowerLayer  B02     C07
      Crystal EP  C12     B06
      OrganSplit  B10     B09
      BrassLayer  C07     B15
      FlangeFunk  A08     B11
      ABass/Vibe  A12     A16
      SGXStrings  D13     B13
      Fifth Wave  B14     B14
      Ensemble    B13     D15
      PIANO-SNGL  A01     OFF
    
      FMEP&Piano  A06     A01
      PFHornPad   B07     D15
      BS/PF&Pad   B07     B16
      Ballad EP   A06     D05
      EP&BellPad  A06     D05
      BS/StageEP  B05     C16
      Piano & EP  A06     D07
      Modern EP   B06     B04
      BS/Organ    C10     C16
      PowerBrass  D15     B15
      StereoClav  B11     A11
      BellChines  B12     C15
      Symphony    D13     C13
      Modern Pad  A14     B14
      Bows&Brass  B13     B15
      PL/LD-SPLT  B14     D15
    
      PowerWaves  C06     D07
      PFBrassPad  B07     B15
      BS/PFLayer  A07     B16
      Whisper EP  A06     A14
      EPHornPad   B08     D15
      EP/SynHorn  D15     A04
      LayerGrand  C07     C06
      Flange EP   A08     D06
      SynthOrgan  B10     D15
      Air Horns   A15     D15
      Phat Clav   D16     A11
      BellString  B12     D12
      Divisi      B13     D13
      Phaser Pad  B14     C14
      StringPizz  B13     A16
      SFLUTE-LYR  A15     C15
    
      PF Air Pad  C07     C15
      PF&Voices   B07     D14
      P&O-SPLIT   D03     A01
      Metalic EP  A06     B12
      EP&Analog   B08     A06
      BS/EP7Pad   C08     B16
      MondoLayer  B07     C15
      Wurly EFX   B14     A05
      FullPipes   D10     D09
      Air Brass   B15     C15
      BellGuitar  B12     D11
      Air Bells   B12     B14
      AirStrings  D13     C15
      VoxVoices   D14     C14
      Orchestral  B13     A15
      BASS-OCTAV  D16     D16
    */
    
    static final String[] PROGRAMS = new String[] { "Concert", "Studio", "DancePiano", "Dyna-Stage", "Wurly EP", "FM EP 1", "Piano & EP", "Funkamatic", "SGX Organ", "R&B Organ", "Clav", "Vibraphone", "TheStrings", "WhisperVox", "SynthFlute", "Acoustic",
                                                    "Bright", "Rock Piano", "Chorused", "Classic EP", "Dyno Bell", "Wave EP 1", "PF&Strings", "FM&Analog", "Velo \"B\"", "CX-3 Organ", "PhaserClav", "Bellphonic", "Symphonic", "Slow Waves", "SynthBrass", "Fretless",
                                                    "Classic", "Jazz Piano", "Mix Piano", "Stage Bell", "FM EP 2", "Motion EP", "MIDI Grant", "EP&Strings", "Perc Organ", "Gospel Org", "Mutronics", "Crystaline", "PadStrings", "BreathyVox", "Synth Air", "FingerBass",
                                                    "Dynamic", "Ballad", "StagePiano", "Stage Time", "FM EP 3", "Wave EP 2", "Power Keys", "EP Magic", "Full Organ", "Pipe Organ", "Clavitar", "BellString", "StringsL&R", "Voices", "Synth Horn", "Synth Bass" };
    static final String[] BANKS = new String[] { "A", "B", "C", "D" };
    static final String[] NOTES = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
        
    static final String[] PROGRAMS_REARRANGED;
    static
        {
        int x = 0;
        String[] pg = new String[PROGRAMS.length];
        for(int i = 0; i < PROGRAMS.length / 4; i++)
            {
            for(int j = 0; j < 4; j++)
                {
                pg[x++] = BANKS[j] + (i < 9 ? "0" + i : i)  + ": " + PROGRAMS[j * PROGRAMS.length/4 + i];
                }
            }
        PROGRAMS_REARRANGED = pg;
        }
            


    public KorgSGMulti()
        {
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addMain(Style.COLOR_A()));
        vbox.add(hbox);
        vbox.add(addTimbre(1, Style.COLOR_B()));
        vbox.add(addTimbre(2, Style.COLOR_B()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Performance", soundPanel);
                
        model.set("name", "Init");
        
        model.set("number", 0);
        model.set("bank", 0);

        loadDefaults();
        }
                
    
    public String getDefaultResourceFileName() { return "KorgSGMulti.init"; }
    public String getHTMLResourceFileName() { return "KorgSGMulti.html"; }
                
                
              
    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, getSynthName(), color);
        globalCategory.makeUnresettable();
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                
        VBox vbox = new VBox();
        HBox hbox2 = new HBox();
        comp = new PatchDisplay(this, 9);
        hbox2.add(comp);
        vbox.add(hbox2);
        
        comp = new StringComponent("Patch Name", this, "name", 10, "Name must be up to 10 ASCII characters.")
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
        vbox.addBottom(comp);  // doesn't work right :-(
        hbox.add(vbox);
        
        hbox.add(Strut.makeHorizontalStrut(60));

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }
        
    public String revisePatchName(String name)
        {
        name = super.revisePatchName(name);
        if (name == null) name = "";
        char[] chars = name.toCharArray();
        for(int i = 0; i < chars.length; i++)
            {
            if (chars[i] < 32 || chars[i] > 127)
                chars[i] = ' ';
            }
        return new String(chars);
        }

              
    public JComponent addMain(Color color)
        {
        Category category  = new Category(this, "Main", color);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        // it is not clear what "use" vs "pass" is here -- need to check
        comp = new CheckBox("Timbre B Goes Through Effect 1", this, "timbfxrout");
        ((CheckBox)comp).addToWidth(2);  // requires two, not one!  A first I think.
        vbox.add(comp);
        
        comp = new CheckBox("Version 8", this, "filtdatatype");
        model.setStatus("filtdatatype", Model.STATUS_IMMUTABLE);
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Aftertouch", this, "afttouchcurve", color, 0, 8)
            {
            public int getDefaultValue() { return 1; }
            public String map(int val)
                {
                if (val < 8)
                    return "" + (val + 1);
                else 
                    return "Global";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Curve");
        getModel().setMetricMax("afttouchcurve", 7);
        hbox.add(comp);

        comp = new LabelledDial("Velocity", this, "velocityfigure", color, 0, 4)
            {
            public int getDefaultValue() { return 1; }
            public String map(int val)
                {
                if (val < 4)
                    return "" + (val + 1);
                else 
                    return "Global";
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Curve");
        getModel().setMetricMax("velocityfigure", 3);
        hbox.add(comp);

        comp = new LabelledDial("Velocity (p)", this, "velpointp", color, 1, 127);
        ((LabelledDial)comp).addAdditionalLabel("Point (Soft)");
        hbox.add(comp);

        comp = new LabelledDial("Velocity (f)", this, "velpointf", color, 1, 150);
        ((LabelledDial)comp).addAdditionalLabel(" Point (Loud) ");
        hbox.add(comp);
        
        category.add(hbox);
        return category;
        }
        
    public JComponent addTimbre(int val, Color color)
        {
        Category category  = new Category(this, "Timbre " + (val == 1 ? "A" : "B"), color);
        category.makePasteable("timbre" + val);
                        
        JComponent comp;
        String[] params;
        VBox main = new VBox();
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        params = PROGRAMS_REARRANGED;
        comp = new Chooser("Program", this, "timbre" + val + "program", params);
        vbox.add(comp);

        // 0 is on, 1 is off.  That makes perfect sense.
        comp = new CheckBox("On", this, "timbre" + val + "switch", true);
        vbox.add(comp);

        hbox.add(vbox);

        comp = new LabelledDial("Level", this, "timbre" + val + "outputlevel", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Transpose", this, "timbre" + val + "transpose", color, -12, 12)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);

        comp = new LabelledDial("Tune", this, "timbre" + val + "tune", color, -50, 50)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);

        comp = new LabelledDial("Pan", this, "timbre" + val + "lrpanpot", color, 0, 128)
            {
            // as it turns out, this is exactly symmetric, even with the "Pgrm".  Not expected.
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                if (val == 0)
                    return "L";
                else if (val < 64)
                    return "L" + val;
                else if (val == 64)
                    return "--";
                else if (val < 127)
                    return "R" + val;
                else if (val == 127)
                    return "R";
                else
                    return "Prgm";
                }
            };
        getModel().setMetricMax("timbre" + val + "lrpanpot", 127);
        hbox.add(comp);
        
        comp = new LabelledDial("Key Zone", this, "timbre" + val + "keyzonetop", color, 0, 127)
            {
            public String map(int val)
                {
                String note = NOTES[val % 12];
                int octave = (val / 12) - 1;
                return note + octave;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Top");
        hbox.add(comp);

        comp = new LabelledDial("Key Zone", this, "timbre" + val + "keyzonebottom", color, 0, 127)
            {
            public String map(int val)
                {
                String note = NOTES[val % 12];
                int octave = (val / 12) - 1;
                return note + octave;
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Botttom");
        hbox.add(comp);

        comp = new LabelledDial("Vel Zone", this, "timbre" + val + "velzonetop", color, 1, 127);
        ((LabelledDial)comp).addAdditionalLabel("Top");
        hbox.add(comp);

        comp = new LabelledDial("Vel Zone", this, "timbre" + val + "velzonebottom", color, 1, 127);
        ((LabelledDial)comp).addAdditionalLabel("Top");
        hbox.add(comp);

        main.add(hbox);

        hbox = new HBox();
        vbox = new VBox();
                
        comp = new CheckBox("Damper/Sostenuto", this, "timbre" + val + "dampsostfilt");
        ((CheckBox)comp).addToWidth(1);
        vbox.add(comp);

        comp = new CheckBox("Controller [v1-7]", this, "timbre" + val + "controllerfilt");
        vbox.add(comp);

        hbox.add(vbox);
        vbox = new VBox();
                
        comp = new CheckBox("Tone Character [v8]", this, "timbre" + val + "tonecharafilt");
        vbox.add(comp);

        comp = new CheckBox("Pitch Bend [v8]", this, "timbre" + val + "pitchbendfilt");
        vbox.add(comp);

        hbox.add(vbox);
        vbox = new VBox();
                
        comp = new CheckBox("Mod Wheel [v8]", this, "timbre" + val + "modwheelfilt");
        vbox.add(comp);

        comp = new CheckBox("Volume [v8]", this, "timbre" + val + "volumefilt");
        vbox.add(comp);

        hbox.add(vbox);
        vbox = new VBox();
                
        comp = new CheckBox("Expression [v8]", this, "timbre" + val + "expressionfilt");
        ((CheckBox)comp).addToWidth(2);
        vbox.add(comp);

        comp = new CheckBox("Aftertouch [v8]", this, "timbre" + val + "aftertouchfilt");
        ((CheckBox)comp).addToWidth(1);
        vbox.add(comp);

        hbox.add(vbox);
        vbox = new VBox();

        comp = new CheckBox("Pan [v8]", this, "timbre" + val + "panpotfilt");
        vbox.add(comp);

        hbox.add(vbox);
                
        main.add(hbox);         
        category.add(main);



        return category;
        }    



    int range(int a)
        {
        while (a > 255) a -= 256;
        while (a < 0) a += 256;
        return a;
        }
        
    // converts all but last byte (F7)
    byte[] convertTo8Bit(byte[] data, int offset)
        {
        // How big?
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
                    newd[j + x] = (byte)(data[i + x + 1] | (byte)(((data[i] >>> x) & 0x1) << 7));
                }
            j += 7;
            }
        return newd;
        }
        
    // converts all bytes
    byte[] convertTo7Bit(byte[] data)
        {
        // How big?
        int size = (data.length) / 7 * 8;
        if (data.length % 7 > 0)
            size += (1 + data.length % 7);
        byte[] newd = new byte[size];
        
        int j = 0;
        for(int i = 0; i < data.length; i+=7)
            {
            for(int x = 0; x < 7; x++)
                {
                if (j + x + 1 < newd.length)
                    {
                    newd[j + x + 1] = (byte)(data[i + x] & 127);
                    // Note that I have do to & 1 because data[i + x] is promoted to an int
                    // first, and then shifted, and that makes a BIG NUMBER which requires
                    // me to mask out the 1.  I hope this isn't the case for other stuff (which
                    // is typically 7-bit).
                    newd[j] = (byte)(newd[j] | (((data[i + x] >>> 7) & 1) << x));
                    }
                }
            j += 8;
            }
        return newd;
        }
        
        
    public int parse(byte[] data, boolean fromFile)
        {
        // The data is F0, 42, 3[CHANNEL], 4A, 40, ... DATA ..., F7
        
        data = convertTo8Bit(data, 5);
        
        char[] namec = new char[10];
        String name;
        for(int i = 0; i < 10; i++)
            {
            namec[i] = (char)data[i];
            }
        name = new String(namec);
        model.set("name", name);
        
        // this will have to be set entirely custom.  :-(  Stupid Korg.  Really bad sysex.
        
        model.set("afttouchcurve", data[10]);
        model.set("velocityfigure", data[11]);
        model.set("velpointp", data[12]);
        model.set("velpointf", range(data[13]));
        model.set("timbfxrout", data[14]);
        model.set("filtdatatype", data[15] & 1);
        
        // skip 16...95
        
        // timbres a and b
        for(int i = 1; i < 3; i++)
            {
            int offset = (i == 1 ? 96 : 106);
            model.set("timbre" + i + "program", data[0 + offset] & 63);
            model.set("timbre" + i + "switch", (data[0 + offset] >>> 7) & 1);
            model.set("timbre" + i + "outputlevel", data[1 + offset]);
            model.set("timbre" + i + "transpose", data[2 + offset]);
            model.set("timbre" + i + "tune", data[3 + offset]);
            model.set("timbre" + i + "lrpanpot", range(data[4 + offset]));

            model.set("timbre" + i + "dampsostfilt", data[5 + offset] & 1);
            if (model.get("filtdatatype", 0) == 0)  // old
                {
                model.set("timbre" + i + "controllerfilt", (data[5 + offset] >>> 1) & 1);
                }
            else // new
                {
                model.set("timbre" + i + "tonecharafilt", (data[5 + offset] >>> 1) & 1);
                model.set("timbre" + i + "pitchbendfilt", (data[5 + offset] >>> 2) & 1);
                model.set("timbre" + i + "modwheelfilt", (data[5 + offset] >>> 3) & 1);
                model.set("timbre" + i + "volumefilt", (data[5 + offset] >>> 4) & 1);
                model.set("timbre" + i + "expressionfilt", (data[5 + offset] >>> 5) & 1);
                model.set("timbre" + i + "aftertouchfilt", (data[5 + offset] >>> 6) & 1);
                model.set("timbre" + i + "panpotfilt", (data[5 + offset] >>> 7) & 1);
                }
                        
            model.set("timbre" + i + "keyzonetop", data[6 + offset]);
            model.set("timbre" + i + "keyzonebottom", data[7 + offset]);
            model.set("timbre" + i + "velzonetop", data[8 + offset]);
            model.set("timbre" + i + "velzonebottom", data[9 + offset]);
            }
        
        // skip 116...243
        
        revise();       
        return PARSE_SUCCEEDED;     
        }
    

    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();
            
        // The SG cannot write to a patch.  We have to emit to current memory, then save
        // to a patch, so we'll tack some extra sysex on in that situation
            
        byte BB = (byte) tempModel.get("bank");
        byte NN = (byte) tempModel.get("number");
        
        Object[] d = new Object[1];
        if (!toWorkingMemory && !toFile)
            {
            d = new Object[2];
            }
        
        byte[] data = new byte[244];
        
        // LOAD DATA HERE
        
        String name = model.get("name", "Untitled");
        char[] namec = new char[10];
        char[] b = name.toCharArray();
        System.arraycopy(b, 0, namec, 0, b.length);
        for(int i = 0; i < 10; i++)
            {
            data[i] = (byte)(namec[i] & 127);
            }

        data[10] = (byte)model.get("afttouchcurve", 0);
        data[11] = (byte)model.get("velocityfigure", 0);
        data[12] = (byte)model.get("velpointp", 0);
        data[13] = (byte)model.get("velpointf", 0);
        data[14] = (byte)model.get("timbfxrout", 0);
        data[15] = (byte)(model.get("filtdatatype", 0) & 1);
                
        // skip 16...95
                
        // timbres a and b
        for(int i = 1; i < 3; i++)
            {
            int offset = (i == 1 ? 96 : 106);
            data[0 + offset] = (byte)((model.get("timbre" + i + "program", 0) & 63) | ((model.get("timbre" + i + "switch", 0) & 1) << 7));
            data[1 + offset] = (byte)model.get("timbre" + i + "outputlevel", 0);
            data[2 + offset] = (byte)model.get("timbre" + i + "transpose", 0);
            data[3 + offset] = (byte)model.get("timbre" + i + "tune", 0);
            data[4 + offset] = (byte)model.get("timbre" + i + "lrpanpot", 0);
                        
            data[5 + offset] = (byte)(model.get("timbre" + i + "dampsostfilt", 0) & 1);
            if (model.get("filtdatatype", 0) == 0)  // old
                {
                data[5 + offset] = (byte)(data[5 + offset] | ((model.get("timbre" + i + "controllerfilt", 0) & 1) << 1));
                }
            else
                {
                data[5 + offset] = (byte)(data[5 + offset] | ((model.get("timbre" + i + "tonecharafilt", 0) & 1) << 1));
                data[5 + offset] = (byte)(data[5 + offset] | ((model.get("timbre" + i + "pitchbendfilt", 0) & 1) << 2));
                data[5 + offset] = (byte)(data[5 + offset] | ((model.get("timbre" + i + "modwheelfilt", 0) & 1) << 3));
                data[5 + offset] = (byte)(data[5 + offset] | ((model.get("timbre" + i + "volumefilt", 0) & 1) << 4));
                data[5 + offset] = (byte)(data[5 + offset] | ((model.get("timbre" + i + "expressionfilt", 0) & 1) << 5));
                data[5 + offset] = (byte)(data[5 + offset] | ((model.get("timbre" + i + "aftertouchfilt", 0) & 1) << 6));
                data[5 + offset] = (byte)(data[5 + offset] | ((model.get("timbre" + i + "panpotfilt", 0) & 1) << 7));
                }
                        
            data[6 + offset] = (byte)model.get("timbre" + i + "keyzonetop", 0);
            data[7 + offset] = (byte)model.get("timbre" + i + "keyzonebottom", 0);
            data[8 + offset] = (byte)model.get("timbre" + i + "velzonetop", 0);
            data[9 + offset] = (byte)model.get("timbre" + i + "velzonebottom", 0);
            }
                        
        // skip 116 .. 243
        
        // Convert 
        
        byte[] data2 = convertTo7Bit(data);
        data = new byte[6 + data2.length];  // resetting data to new value
        data[0] = (byte)0xF0;
        data[1] = (byte)0x42;
        data[2] = (byte)(48 + getChannelOut());
        data[3] = (byte)0x4A;
        data[4] = (byte)0x49;
        data[data.length - 1] = (byte)0xF7;
        System.arraycopy(data2, 0, data, 5, data2.length);
        d[0] = data;
        
        if (!toWorkingMemory && !toFile)
            {
            data = new byte[] { (byte)0xF0, (byte)0x42, (byte)(48 + getChannelOut()), (byte)0x4A, (byte)0x1A, (byte)0,
                (byte)(BB * 16 + NN), (byte)0xF7 };
            d[1] = data;
            }
        return d;
        }

        
    public int getPauseAfterChangePatch() { return 200; }

    public void changePatch(Model tempModel)
        {
        // enter performance mode, which goes back out
        tryToSendSysex(new byte[] { (byte)0xF0, 0x42, (byte)(48 + getChannelOut()), 0x4A, 0x4E, 0x00, 0x0, (byte)0xF7 });

        byte BB = (byte)tempModel.get("bank");
        byte NN = (byte)tempModel.get("number");
        try {
            // Number change is PC
            tryToSendMIDI(new ShortMessage(ShortMessage.PROGRAM_CHANGE, getChannelOut(), BB * 16 + NN, 0));
            }
        catch (Exception e) { e.printStackTrace(); }

        // we assume that we successfully did it
        if (!isMerging())  // we're actually loading the patch, not merging with it
            {
            setSendMIDI(false);
            model.set("number", tempModel.get("number"));
            model.set("bank", tempModel.get("bank"));
            setSendMIDI(true);
            }
        }

    public void performRequestDump(Model tempModel, boolean changePatch)
        {
        // we always change the patch no matter what
        changePatch(tempModel);

        // enter performance edit mode, which loads the patch into edit buffer memory
        tryToSendSysex(new byte[] { (byte)0xF0, 0x42, (byte)(48 + getChannelOut()), 0x4A, 0x4E, 0x01, 0x0, (byte)0xF7 });
        tryToSendSysex(requestCurrentDump());
        }
            
    public void performRequestCurrentDump()
        {
        // enter performance mode, which goes back out
        tryToSendSysex(new byte[] { (byte)0xF0, 0x42, (byte)(48 + getChannelOut()), 0x4A, 0x4E, 0x0, 0x0, (byte)0xF7 });

        // enter program edit mode, which loads the patch into edit buffer memory
        tryToSendSysex(new byte[] { (byte)0xF0, 0x42, (byte)(48 + getChannelOut()), 0x4A, 0x4E, 0x01, 0x0, (byte)0xF7 });
        tryToSendSysex(requestCurrentDump());
        }
            
    public byte[] requestCurrentDump()
        {
        return new byte[] { (byte)0xF0, 0x42, (byte)(48 + getChannelOut()), 0x4A, 0x19, (byte)0xF7 };
        }
    
    
    public static final int EXPECTED_SYSEX_LENGTH = 285;
    public static boolean recognize(byte[] data)
        {
        boolean v = (
            data.length == EXPECTED_SYSEX_LENGTH &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x42 &&
            // don't care
            //data[2] == (byte)(48 + getChannelOut()) &&
            data[3] == (byte)0x4A &&
            data[4] == (byte)0x49);
        return v;
        }
    
    
    
    
    /////// OTHER ABSTRACT METHODS
    
    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(BANKS);
        bank.setEditable(false);
        bank.setMaximumRowCount(32);
        bank.setSelectedIndex(model.get("bank"));
                
        JTextField number = new JTextField("" + (model.get("number") + 1), 3);
                
        while(true)
            {
            boolean result = showMultiOption(this, new String[] { "Bank", "Patch Number"}, 
                new JComponent[] { bank, number }, title, "Enter the Bank and Patch number.");
                
            if (result == false) 
                return false;
                                
            int n;
            try { n = Integer.parseInt(number.getText()); }
            catch (NumberFormatException e)
                {
                showSimpleError(title, "The Patch Number must be an integer 1 ... 16");
                continue;
                }
            if (n < 1 || n > 16)
                {
                showSimpleError(title, "The Patch Number must be an integer 1 ... 16");
                continue;
                }
                                
            change.set("bank", bank.getSelectedIndex());
            change.set("number", n - 1);
                        
            return true;
            }
        }
        

    public void revise()
        {
        // check the easy stuff -- out of range parameters
        super.revise();

        String nm = model.get("name", "Init");
        String newnm = revisePatchName(nm);
        if (!nm.equals(newnm))
            model.set("name", newnm);
        }
        
    public static String getSynthName() { return "Korg SG Rack [Multi]"; }
    
    public String getPatchName(Model model) { return model.get("name", "Init"); }
    

    public Model getNextPatchLocation(Model model)
        {
        int bank = model.get("bank");
        int number = model.get("number");
        
        number++;
        if (number >= 16)
            {
            bank++;
            number = 0;
            if (bank >= 4)
                bank = 0;
            }
                
        Model newModel = buildModel();
        newModel.set("bank", bank);
        newModel.set("number", number);
        return newModel;
        }

    public String getPatchLocationName(Model model)
        {
        // getPatchLocationName() is called from sprout() as a test to see if we should enable
        // batch downloading.  If we haven't yet created an .init file, then parameters won't exist
        // yet and this method will bomb badly.  So we return null in this case.
        if (!model.exists("number")) return null;
        if (!model.exists("bank")) return null;
        
        int number = model.get("number") + 1;
        return BANKS[model.get("bank")] + (number > 9 ? "" : "0") + number;
        }
        
    }
    
