/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.korgwavestation;

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

   0. Mutation - does not change length
   1. Write        - includes length change, resets undo, warning
   2. Send         - does not include length change
   3. Set length   - resets undo, warning
        
   @author Sean Luke
*/

public class KorgWavestationSequence extends KorgWavestationAbstract
    {
    /// Various collections of parameter names for pop-up menus
    
    public static final int NAME_LENGTH = 8;
    public static final int NUM_STEPS = 255;
    
    NumberButton lengthbutton;
    JCheckBoxMenuItem blockSending;

//    public JCheckBoxMenuItem includeLengthInBulkSend = new JCheckBoxMenuItem("Include Length when Sending");

    public static final String[] SEMITONE_TUNINGS = new String[]
    {
    "-24", "-23", "-22", "-21", "-20", "-19", "-18", "-17", "-16", "-15", "-14", "-13", "-12", "-11", "-10", "-9", "-8", "-7", "-6", "-5", "-4", "-3", "-2", "-1", "0",
    "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24"
    };


    VBox stepAndDisplay = new VBox();
    VBox outer = new VBox();
        
    public KorgWavestationSequence()
        {
        JComponent soundPanel = new SynthPanel(this);
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addSequenceGlobal(Style.COLOR_A()));
        outer.add(hbox);


        stepHolder.add(addSteps(Style.COLOR_B()));
        stepHolder.addLast(steps[0]);
        stepAndDisplay.add(stepHolder);
        stepAndDisplay.add(addEnvelope(Style.COLOR_B()));

        outer.addBottom(stepAndDisplay);
        
        
        soundPanel.add(outer, BorderLayout.CENTER);
        addTab("Wave Sequence", soundPanel);
        
        model.set("name", "Init");
        
        model.set("number", 0);
        model.set("bank", 0);

        loadDefaults();
        }
                
    
    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        transmitTo.setEnabled(false);  // there's no such thing as "sending to" another patch -- you should always write to it
        receiveCurrent.setEnabled(false);  // we can't request the "current" sequence
        addWavestationMenu();
        return frame;
        }

    public String getDefaultResourceFileName() { return "KorgWavestationSequence.init"; }
    public String getHTMLResourceFileName() { return "KorgWavestationSequence.html"; }
                
    
    public static final int MINIMUM_SECONDS_FOR_LENGTH_CHANGE = 10;
    public boolean verifyLengthChange(int val)
        {
        if (val == 0) return true;
        else if (getAllowsTransmitsParameters())
            {
            int seconds = ((val * (val - 1)) / 2 * MS_PER_STEP_BY_INDEX + MS_PER_STEP * val + MS_PER_STEP_DATA * val * 8 + MS_PER_INITIALIZATION) / 1000;
            return (seconds < MINIMUM_SECONDS_FOR_LENGTH_CHANGE || showSimpleConfirm("Verify Number of Steps",
                    "This process will take over " + seconds + " seconds.\nContinue?"));
            }
        else
            return true;  // it'll be instantaneous
        }
                
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

        comp = new ReadOnlyString("Sequence Name", this, "name", NAME_LENGTH)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                updateTitle();
                }
            };
        hbox2.add(comp);

        vbox.add(hbox2);
        
        VBox vbox2 = new VBox();
        hbox2 = new HBox();

        comp = lengthbutton = new NumberButton("Num Steps", this, "length", 0, NUM_STEPS, "Value must be 0..." + NUM_STEPS + ".")
            {
            public void submitValue(int val)
                {
                if (blockSending.isSelected() || verifyLengthChange(val))
                    {
                    super.submitValue(val);
                        
                    // When we update the number of steps, we can't undo this
                    KorgWavestationSequence.this.getUndo().clear();
                    KorgWavestationSequence.this.updateUndoMenus();
                        
                    currentParameter = 0;
                    totalParameters = 0;
                    offsetParameters = 0;
                    sendingLength = false;
                    sequenceGlobalCategory.setName("Sequence");
                    }
                }
                
            public void update(String key, Model model)
                {
                super.update(key, model);
                revise();
                outer.removeLast();
                if (model.get(key, 0) != 0)
                    {
                    outer.addBottom(stepAndDisplay);
                    }
                outer.revalidate();
                outer.repaint();
                }
            };
        model.setStatus("length", Model.STATUS_IMMUTABLE);
        hbox2.add(comp);
        vbox.addBottom(hbox2);
        hbox.add(vbox);
        
        
        hbox.addLast(Strut.makeHorizontalStrut(100));

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


    public Category sequenceGlobalCategory;
        
    public JComponent addSequenceGlobal(Color color)
        {
        Category category  = sequenceGlobalCategory = new Category(this, "Sequence", color);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        VBox main = new VBox();
        
        params = SOURCES;
        comp = new Chooser("Modulation Source", this, "modsource", params);
        vbox.add(comp);
                
        comp = new CheckBox("Loop Back and Forth", this, "loopbackandforth", false);
        vbox.add(comp);
        hbox.add(vbox);
                
        comp = new LabelledDial("Start", this, "start", color, 1, NUM_STEPS);
        hbox.add(comp);

        comp = new LabelledDial("Loop Start", this, "loopstart", color, 1, NUM_STEPS)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                // revise loopend to be over loopstart
                int loopstart = model.get("loopstart", 1);
                int loopend = model.get("loopend", 1);
                if (loopend < loopstart)
                    model.set("loopend", loopstart);
                }
            };
        hbox.add(comp);

        comp = new LabelledDial(" Loop End ", this, "loopend", color, 1, NUM_STEPS)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                // revise loopstart to be under loopend
                int loopend = model.get("loopend", 1);
                int loopstart = model.get("loopstart", 1);
                if (loopend < loopstart)
                    model.set("loopstart", loopend);
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Loop Repeats", this, "looprepeats", color, 0, 127)
            {
            public String map(int val)
                {
                if (val == 0) return "Off";
                if (val == 127) return "Infinity";
                return "" + val;
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Modulation", this, "modulationamount", color, -127, 127);
        ((LabelledDial)comp).addAdditionalLabel("Amount");
        hbox.add(comp);

        category.add(hbox, BorderLayout.WEST);
        return category;
        }
    
    
    public static final int BANK_CARD = 11;
    public String[] buildWaves(int bank)
        {
        if (bank != BANK_CARD)
            {
            return KorgWavestationSequence.WAVES;
            }
        else
            {
            String[] waves = new String[517 - 32];
            for(int i = 0; i < 517 - 32; i++)
                {
                waves[i] = "Wave " + i;
                }
            return waves;
            }
        }
        
        
        
    public Category addStep(int num, Color color)
        {
        Category category = new Category(this, "" + num, color);
        category.makePasteable("step" + num);
        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        params = buildWaves(model.get("bank", 0));
        final Chooser waves = new Chooser("Wave", this, "step" + num + "number", params);
        vbox.add(waves);

        final CheckBox card = new CheckBox("Card", this, "step" + num + "bank", false)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                
                // maybe should turn off MIDI transmission here so the chooser doesn't send a bunch of gunk
                boolean oldSendMIDI = getSendMIDI();
                setSendMIDI(false);
                String currentKey = getKey().replace("bank", "number");
                int currentWave = model.get(currentKey, 0);
                waves.setElements("Wave", buildWaves(model.get(key, 0) == 1 ? BANK_CARD : 0));
                model.set(currentKey, currentWave);
                setSendMIDI(oldSendMIDI);
                }
            };
        vbox.add(card);
        hbox.add(vbox);
                
        final LabelledDial level = new LabelledDial("Level", this, "step" + num + "level", color, 0, 99);
        hbox.add(level);

        final LabelledDial duration = new LabelledDial("Duration", this, "step" + num + "duration", color, 1, 500)
            {
            public String map(int val)
                {
                if (val == 500) return "Gate";
                else return "" + val;
                }
            };
        hbox.add(duration);
        
        final LabelledDial crossfade = new LabelledDial("Crossfade", this, "step" + num + "crossfade", color, 0, 998);
        hbox.add(crossfade);

        final LabelledDial semitone = new LabelledDial("Semitone", this, "step" + num + "semitone", color, -24, 24);
        model.set("step" + num + "semitone", 0);
        hbox.add(semitone);

        final LabelledDial finetune = new LabelledDial("Fine Tune", this, "step" + num + "fine", color, -99, 99);
        model.set("step" + num + "fine", 0);
        hbox.add(finetune);
        
        category.add(hbox, BorderLayout.WEST);
        
        return category;
        }
                
    Category[] steps = new Category[NUM_STEPS];
    HBox stepHolder = new HBox();
                
    public JComponent addSteps(Color color)
        {
        Category category = new Category(this, "Step", color);
                        
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        for(int i = 0; i < NUM_STEPS; i++)
            {
            steps[i] = addStep(i + 1, color);
            }
                        
        comp = new LabelledDial(" Number ", this, "step", color, 1, NUM_STEPS)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                        
                int step = model.get(key, 1);
                if (step == 0) step = 1;                // this will draw the wrong step but it's okay since we're not being drawn at all
                stepHolder.removeLast();
                stepHolder.addLast(steps[step - 1]);
                //stepHolder.revalidate();
                stepHolder.repaint();
                }
            };
        model.setStatus("step", Model.STATUS_IMMUTABLE);

        model.set("length", model.get("length", 1));
                
        hbox.add(comp);

        vbox = new VBox();      
        comp = new PushButton("Up")
            {
            public void perform()
                {
                int wave = model.get("step", 1);
                int max = model.getMax("step");
                if (wave < max)
                    model.set("step", wave + 1);
                }
            };
        vbox.add(comp);
        comp = new PushButton("Down")
            {
            public void perform()
                {
                int wave = model.get("step", 1);
                if (wave > 1)
                    model.set("step", wave - 1);
                }
            };
        vbox.add(comp);
        hbox.add(vbox);                 
        
        vbox = new VBox();  
        
        /*
          vbox = new VBox();
          comp = new PushButton("Insert Before")
          {
          public void perform()
          {
          insertStep(true);
          }
          };
          vbox.add(comp);
        */

        comp = new PushButton("Add Step")
            {
            public void perform()
                {
                insertStep(false);
                }
            };
        vbox.add(comp);

        comp = new PushButton("Delete Step")
            {
            public void perform()
                {
                deleteStep();
                }
            };
        vbox.add(comp);

        hbox.add(vbox);
        hbox.add(Strut.makeHorizontalStrut(50));

        category.add(hbox, BorderLayout.WEST);
        return category;
        }    

    public JComponent addEnvelope(Color color)
        {
        Category category = new Category(this, "Display", color);
                        
        JComponent comp;
        String[] params;
        
        double[] lengths = new double[NUM_STEPS];
        for(int i = 0; i < lengths.length; i++)
            lengths[i] = 1.0 / 11.0 / 500;            // 11 = 3 + (3 + 1) * 2
                
        String[] lengthKeys = new String[NUM_STEPS];
        for(int i = 0; i < lengthKeys.length; i++)
            lengthKeys[i] = "step" + (i + 1) + "duration";

        double[] levels = new double[NUM_STEPS];
        for(int i = 0; i < levels.length; i++)
            levels[i] = 1.0 / 99;
        
        String[] levelKeys = new String[NUM_STEPS];
        for(int i = 0; i < levelKeys.length; i++)
            levelKeys[i] = "step" + (i + 1) + "level";

        double[] fades = new double[NUM_STEPS];
        for(int i = 0; i < fades.length; i++)
            fades[i] = 1.0 / 11.0 / 499;              // 11 = 3 + (3 + 1) * 2
                
        String[] fadeKeys = new String[NUM_STEPS];
        for(int i = 0; i < fadeKeys.length; i++)
            fadeKeys[i] = "step" + (i + 1) + "crossfade";
            
        HBox hbox = new HBox();
        
        comp = new LabelledDial("Magnify", this, "magnify", color, 1, 100);
        hbox.add(comp);

        CrossfadeEnvelopeDisplay display = new CrossfadeEnvelopeDisplay(this, Style.DYNAMIC_COLOR(), color, 
            "step", "length", lengths, levels, fades, lengthKeys, levelKeys, fadeKeys, "magnify");

        hbox.addLast(display);
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }    

    class Seq
        {
        int link;
        int sLink;
        int loopStart;
        int loopEnd;
        int loopCount;
        int startStep;
        int modSrc;
        int modAmt;
        int dynoMod;
        int startTime;
        int time;
        char[] name = new char[8];

        public void setName(String val)
            {
            val = val + "        ";
            System.arraycopy(val.toCharArray(), 0, name, 0, name.length); 
            }
        
        int read(byte[] data, int pos)
            {
            link = readUWord(data, pos);
            pos += 2;
            sLink = readUWord(data, pos);
            pos += 2;
            loopStart = readUByte(data, pos);
            pos += 1;
            loopEnd = readUByte(data, pos);
            pos += 1;
            loopCount = readUByte(data, pos);
            pos += 1;
            startStep = readUByte(data, pos);
            pos += 1;
            modSrc = readUByte(data, pos);
            pos += 1;
            modAmt = readByte(data, pos);
            pos += 1;
            dynoMod = readWord(data, pos);
            pos += 2;
            startTime = readUWord(data, pos);
            pos += 2;
            time = readUWord(data, pos);
            pos += 2;
            return pos;
            }
            
        int readName(byte[] data, int pos)
            {
            for(int i = 0; i < 8; i++)
                { int d = data[i + pos]; if (d < 0) d += 256; name[i] = (char)d; }
            return pos + 8;
            }
        }
    
    class Step implements Cloneable
        {
        int fLink;
        int bLink;
        int lLink;
        int waveNum;
        int coarse;
        int fine;
        int xFade;
        int duration;
        int level;
        int modIndex;
        
        Step copy()  // faster than clone?  dunno
            {
            Step newstep = new Step();
            newstep.fLink = fLink;
            newstep.bLink = bLink;
            newstep.lLink = lLink;
            newstep.waveNum = waveNum;
            newstep.coarse = coarse;
            newstep.fine = fine;
            newstep.xFade = xFade;
            newstep.duration = duration;
            newstep.level = level;
            newstep.modIndex = modIndex;
            return newstep;
            }
        
        int read(byte[] data, int pos)
            {
            fLink = readUWord(data, pos);
            pos += 2;
            bLink = readUWord(data, pos);
            pos += 2;
            lLink = readUWord(data, pos);
            pos += 2;
            
            ///// NOTE: The wave numbers are not properly documented.  
            ///// The first byte indicates whether 256 should be added to the
            ///// second byte.  Specifically, if the first byte is -127 (that is, 129),
            ///// then the second byte should have 256 added to form the wave number.
            ///// If the first byte is -128 (that is, 128), then the second byte stays as-is.
            ///// I have also seen 0 in the first byte for blank wave sequence slots.
            /////
            ///// Finally, if coarse is >= 48, then its true value is 72 less, and
            ///// we also add 365 to the wave number to push us into expanded PCM.
            
            // read the high byte...
            int high = readByte(data, pos);
            pos += 1;
            waveNum = readUByte(data, pos);
            pos += 1;
            
            if (high == -127)
                waveNum += 256;
            else if (high == -128)
                {
                // nothin'
                }
            else if (high == 0)
                {
                // nothin'
                }
            else
                {
                // unusual high byte.
                System.err.println("Warning (KorgWavestationSequence): Unusual high byte " + high);
                }

            coarse = readByte(data, pos);
            if (coarse >= 48)
                {
                coarse -= 72;
                waveNum += 365;  // yes, 365
                }
            pos += 1;

            fine = readByte(data, pos);
            pos += 1;

            xFade = readUWord(data, pos);
            pos += 2;

            duration = readUWord(data, pos);
            pos += 2;

            level = readUByte(data, pos);
            pos += 1;

            modIndex = readUByte(data, pos);
            pos += 1;
            return pos;
            }
        }
        
    class Block
        {
        Seq[] seqs = new Seq[32];
        Step[] steps = new Step[501];
        //int length;

        public Block()
            {
            for(int i = 0; i < seqs.length; i++)
                {
                seqs[i] = new Seq();
                }
                        
            for(int i = 0; i < steps.length; i++)
                {
                steps[i] = new Step();
                }
            }

        int read(byte[] data, int pos)
            {
            // load wave sequences
            for(int i = 0; i < 32; i++)
                {
                seqs[i] = new Seq();
                pos = seqs[i].read(data, pos);
                }
            // load steps
            for(int i = 0; i < 501; i++)
                {
                steps[i] = new Step();
                pos = steps[i].read(data, pos);
                }
            // load names
            for(int i = 0; i < 32; i++)
                {
                pos = seqs[i].readName(data, pos);
                }
            //length = straighten();
            return pos;
            }
            
        /*
          int write(byte[] data, int pos)
          {
          // save wave sequences
          for(int i = 0; i < 32; i++)
          {
          pos = steps[i].write(data, pos);
          }
          // load steps
          for(int i = 0; i < 501; i++)
          {
          pos = steps[i].write(data, pos);
          }
          // load names
          for(int i = 0; i < 32; i++)
          {
          pos = seqs[i].writeName(data, pos);
          }
          return pos;
          }
                
          int getLength()
          {
          return length;
          }
                
          Step allocate()
          {
          if (length == 501)  // all done
          return null;
          else
          return steps[length++];
          }
                
          void clear(int sequence)
          {
          seqs[sequence].sLink =  seqs[sequence].link;
          seqs[sequence].loopStart = 0;
          seqs[sequence].loopEnd = 0;
          seqs[sequence].loopCount = 0;
          seqs[sequence].startStep = 0;
          steps[seqs[sequence].link].fLink = 0;
          steps[seqs[sequence].link].bLink = 0;
          steps[seqs[sequence].link].lLink = seqs[sequence].link;  // self-loop?
          straighten();
          }

          int straighten()
          {
          int[] osteps = new int[steps.length];   // maps old->new
          boolean[] used = new boolean[steps.length];     // initially all false
          osteps[0] = 0;
          used[0] = true;

          // determine mapping
          int pos = 0;            // we'll start at 1
          for(int seq = 0; seq < seqs.length; seq++)
          {
          pos++;
          int next = seqs[seq].link;
          osteps[next] = pos;
          used[next] = true;
                        
          while(next != 0)
          {
          pos++;
          next = steps[next].fLink;
          osteps[next] = pos;
          used[next] = true;
          }
          }

          pos++;  // pos now holds the length
                
          // remap sequences
          for(int seq = 0; seq < seqs.length; seq++)
          {
          seqs[seq].link = osteps[seqs[seq].link];
          seqs[seq].sLink = osteps[seqs[seq].sLink];
//                              seqs[seq].loopstart = osteps[seqs[seq].loopstart];
//                              seqs[seq].loopend = osteps[seqs[seq].loopend];
//                              seqs[seq].startstep = osteps[seqs[seq].startstep];
}

// remap steps (except step 0)
for(int step = 1; step < steps.length; step++)
{
steps[step].fLink = osteps[steps[step].fLink];
steps[step].bLink = osteps[steps[step].bLink];
steps[step].lLink = osteps[steps[step].lLink];
}
                        
// move the steps to new locations and garbage collect the unused ones
Step[] newsteps = new Step[steps.length];
for(int step = 0; step < steps.length; step++)
{
if (used[step])
newsteps[osteps[step]] = steps[step];
else
newsteps[osteps[step]] = steps[0].copy();  // zero out
}
                        
// update
steps = newsteps;
return pos;
}
        */
        }

    public int parse(byte[] data, boolean fromFile)
        {
        setSendMIDI(false);        
        // Are we loading from our special data format?
        if (data[2] == 'E' && data[3] == 'D' && data[4] == 'I')  // that's enough
            {
            int pos = 22;
                
            model.set("bank", data[pos++]);
            model.set("number", data[pos++]); 
                
            for(int i = 0; i < MAIN_KEYS.length; i++)
                {
                if (MAIN_KEYS[i].equals("name"))
                    {
                    char[] name = new char[NAME_LENGTH];
                    for(int j = 0; j < NAME_LENGTH; j++)
                        { int d = data[pos++]; if (d < 0) d += 256; name[j] = (char)d; }
                    model.set(MAIN_KEYS[i], new String(name));
                    }
                else
                    {
                    model.set(MAIN_KEYS[i], (data[pos++] << 7) | (data[pos++] & 127));
                    }
                }
            for(int j = 0; j < NUM_STEPS; j++)
                {
                for(int i = 0; i < STEP_KEYS.length; i++)
                    {
                    model.set("step" + (j + 1) + STEP_KEYS[i], (data[pos++] << 7) | (data[pos++] & 127));
                    }
                }                
            }
        else
            {
            Block block = null;
            int pos = 0;
            int mySeq = -1;
            
            // something came in, either from a load or a spontaneous send.  We specify the bank here.  The number will be requested.
            if (!requestingPatch && !isParsingForMerge() )
                {
                block = new Block();
                pos = block.read(denybblize(data, 6), 0);

                String[] n = new String[32];
                for(int i = 0; i < 32; i++)
                    {
                    n[i] = "" + i + "   " + new String(block.seqs[i].name);
                    }

                mySeq = showBankSysexOptions(data, n);
                if (mySeq < 0)
                    {
                    setSendMIDI(true);        
                    return PARSE_CANCELLED;
                    }
                }
            else    // something came in due to a request.  We've already set the bank and number
                {
                model.set("bank", wsToEdisynBank[data[5]]);
                requestingPatch = false;
                }
        
            // if we didn't choose a sequence, we need to read the block in, extract the number, etc.
            if (block == null)
                {
                block = new Block();
                pos = block.read(denybblize(data, 6), 0);
                mySeq = model.get("number", 0);
                }
                
            int step = block.seqs[mySeq].link;
            int len = 0;
            model.set("name", new String(block.seqs[mySeq].name));
        
            // clear others
            for(int i = len; i < 255; i++)
                {
                model.set("step" + (len + 1) + "semitone", 0);
                model.set("step" + (len + 1) + "fine", 0);
                model.set("step" + (len + 1) + "level", 0);
                model.set("step" + (len + 1) + "duration", 1);
                model.set("step" + (len + 1) + "crossfade", 0);
                model.set("step" + (len + 1) + "number", 0);
                model.set("step" + (len + 1) + "bank", 0);                           ///// ROM?  CARD?   Dunno.
                } 
                                
            /// First things, first, let's load the steps
            while(step != 0)  // not the stop sequence
                {
                int semi = block.steps[step].coarse;
                if (semi >= 48) semi -= 72;
                model.set("step" + (len + 1) + "semitone", semi);
                model.set("step" + (len + 1) + "fine", block.steps[step].fine);
                model.set("step" + (len + 1) + "level", block.steps[step].level);
                model.set("step" + (len + 1) + "duration", block.steps[step].duration);
                model.set("step" + (len + 1) + "crossfade", block.steps[step].xFade);
                int wave = block.steps[step].waveNum;
                if (block.steps[step].coarse >= 48) wave += 365;
                model.set("step" + (len + 1) + "number", wave - 32);  // because the wave number stored in the struct actually starts beyond the wave sequence values
                model.set("step" + (len + 1) + "bank", 0);                           ///// ROM?  CARD?   Dunno.
                step = block.steps[step].fLink;
                len++;
                }               
                
            model.set("length", len);
                
            // Set remaining variables
            model.set("loopstart", block.seqs[mySeq].loopStart + 1);            // It appears that loopStart = 1 is 0
            model.set("loopend", block.seqs[mySeq].loopEnd + 1);                        // It appears that loopEnd = 1 is 0
            model.set("modulationamount", block.seqs[mySeq].modAmt);
            model.set("modsource", block.seqs[mySeq].modSrc & 127);                     // looks like modSrc can have its high bit set for some reason?
            model.set("looprepeats", block.seqs[mySeq].loopCount & 127);
            model.set("loopbackandforth", (block.seqs[mySeq].loopCount >>> 7) & 0x1);
            }

        revise();       
        setSendMIDI(true);        
        return PARSE_SUCCEEDED;     
        }
    
    
    
    public Object[] emitAll(String key, int status)
        {
        if (!writingParameters && blockSending.isSelected()) return new Object[0];  // we don't send anything
        if (key.equals("bank")) return new Object[0];  // this is not emittable
        if (key.equals("number")) return new Object[0];  // this is not emittable
        if (key.equals("magnify")) return new Object[0]; // this is not emittable
        
        int index = 0;
        int value = 0;
        sendingLength = false;
        
        byte[] bankmesg = null;
        if (status == STATUS_UPDATING_ONE_PARAMETER)
            {
            bankmesg = paramBytes(WAVE_SEQ_BANK, edisynToWSBank[model.get("bank")]);
            }
        
        byte[] nummesg = null;
        if (status == STATUS_UPDATING_ONE_PARAMETER)
            {
            nummesg = paramBytes(WAVE_SEQ_NUM, model.get("number", 0));
            }
                
        if (key.equals("step"))         // we'll use this to just change the current step, not that it matters because the screen doesn't change unless the user presses a button...
            {
            byte[] mesg = paramBytes(WAVE_SEQ_STEP, model.get(key));
            return new byte[][] { bankmesg, nummesg, mesg };
            }
        else if (key.equals("name"))
            {
            // bug in SR always puts a \0 at the beginning, ruining the name  So we don't do it.
            //byte[] mesg = paramBytes(SAVE_SOURCE_NAME, model.get(key, "").toCharArray());
            //return new byte[][] { mesg }; //bankmesg, nummesg, mesg };
            return new Object[0];
            }
        else if (key.startsWith("step"))
            {
            // emit when it's just one parameter, or when we're doing a bulk send but
            // we are NOT including length, since it'll emit us anyway
//            if (status == STATUS_UPDATING_ONE_PARAMETER || !includeLengthInBulkSend.isSelected())
                {
                int step = extractNumbers(key)[0];
                if (step <= model.get("length"))  // otherwise do not bother
                    {
                    int val = model.get(key, 0);
                                                
                    if (key.endsWith("semitone"))
                        index = WAVE_SEQ_COARSE;
                    else if (key.endsWith("fine"))
                        index = WAVE_SEQ_FINE;
                    else if (key.endsWith("level"))
                        index = WAVE_SEQ_LEVEL;
                    else if (key.endsWith("duration"))
                        index = WAVE_SEQ_DURATION;
                    else if (key.endsWith("crossfade"))
                        index = WAVE_SEQ_XFADE;
                    else if (key.endsWith("number"))
                        {
                        index = WAVE_SEQ_WAVE_NUM;
                        val += 32;
                        }
                    else if (key.endsWith("bank"))
                        {
                        index = WAVE_SEQ_WAVE_BANK;
                        val = edisynToWSBank[val];
                        }
                                                
                    byte[] step_mesg = paramBytes(WAVE_SEQ_STEP, step);
                    byte[] mesg = paramBytes(index, val);
                    return new byte[][] { bankmesg, nummesg, step_mesg, mesg };
                    }
                else
                    return new Object[0];
                }
            }
        else if (key.equals("length"))
            {
            // emit only when it's just one parameter
            if (status == STATUS_UPDATING_ONE_PARAMETER || status == STATUS_KORG_WS_SEQUENCE_WRITING)
                {
                //// IMPORTANT NOTE -- the order here matters.  If it's changed, you also have
                //// to change the code in sentMIDI(...) below.
                        
                int length = model.get("length", 0);
                
                byte[][] obj = new byte[4][];
                if (length == 0)
                    {
                    obj[0] = bankmesg;
                    obj[1] = nummesg; 
                    obj[2] = paramBytes(EXECUTE_WAVESEQ_INIT, 1);
                    // now we're at 1 step.  So we delete one.  This is a rare need, but...
                    obj[3] = paramBytes(EXECUTE_DELETE_WS_STEP, 1);
                    }
                else
                    {
                    obj = new byte[1 + (length) * 2 + length * 8 + 2][];
                    obj[0] = bankmesg;
                    obj[1] = nummesg; 
                                
                    int pos = 2;
                                
                    // first clear the sequence
                    obj[pos++] = paramBytes(EXECUTE_WAVESEQ_INIT, 1);
                                
                    // Now we have ONE wave step.  We need to insert length-1 additional ones
                                                
                    // Now insert steps.  Each time we go back to step 0 because it seems to be a bit faster
                    for(int i = 0; i < length - 1; i++)
                        {
                        obj[pos++] = paramBytes(WAVE_SEQ_STEP, 0);
                        obj[pos++] = paramBytes(EXECUTE_INSERT_WS_STEP, 1);
                        }

                    stepPos = pos;
                                                                
                    if (status != STATUS_KORG_WS_SEQUENCE_WRITING)
                        {
                        // Now we set the step data for ALL wave steps
                        for(int i = 0; i < length; i++)
                            {
                            obj[pos++] = paramBytes(WAVE_SEQ_STEP, (i + 1));
                            obj[pos++] = paramBytes(WAVE_SEQ_COARSE, model.get("semitone", 0));
                            obj[pos++] = paramBytes(WAVE_SEQ_FINE, model.get("fine", 0));
                            obj[pos++] = paramBytes(WAVE_SEQ_LEVEL, model.get("level", 0));
                            obj[pos++] = paramBytes(WAVE_SEQ_DURATION, model.get("duration", 0));
                            obj[pos++] = paramBytes(WAVE_SEQ_XFADE, model.get("crossfade", 0));
                            obj[pos++] = paramBytes(WAVE_SEQ_WAVE_BANK, edisynToWSBank[model.get("bank", 0)]);
                            obj[pos++] = paramBytes(WAVE_SEQ_WAVE_NUM, model.get("number", 0));
                            }
                        }
                    }
                        
                sendingLength = true;  // this is set anyway, but whatever...
                currentParameter = 0;
                offsetParameters = obj.length;
                totalParameters = offsetParameters;
                        
                // totalParameters and offsetParameters and sendingLength will get reset in the button-press code
                        
                return obj;
                }
            else return new Object[0];
            }
        else if (key.equals("start"))
            {
            byte[] mesg = paramBytes(WAVE_SEQ_START_STEP, model.get(key, 0));
            return new byte[][] { bankmesg, nummesg, mesg };
            }

        else if (key.equals("loopbackandforth"))
            {
            byte[] mesg = paramBytes(WAVE_SEQ_LOOP_DIR, model.get(key, 0));
            return new byte[][] { bankmesg, nummesg, mesg };
            }
        else if (key.equals("looprepeats"))
            {
            byte[] mesg = paramBytes(WAVE_SEQ_REPEATS, model.get(key, 0));
            return new byte[][] { bankmesg, nummesg, mesg };
            }
        else if (key.equals("loopstart"))
            {
            byte[] mesg = paramBytes(WAVE_SEQ_LOOP_START, model.get(key, 0));
            return new byte[][] { bankmesg, nummesg, mesg };
            }
        else if (key.equals("loopend"))
            {
            byte[] mesg = paramBytes(WAVE_SEQ_LOOP_END, model.get(key, 0));
            return new byte[][] { bankmesg, nummesg, mesg };
            }
        else if (key.equals("modulationamount"))
            {
            byte[] mesg = paramBytes(WAVE_SEQ_MOD_AMT, model.get(key, 0));
            return new byte[][] { bankmesg, nummesg, mesg };
            }
        else if (key.equals("modsource"))
            {
            byte[] mesg = paramBytes(WAVE_SEQ_MOD_SRC, model.get(key, 0));
            return new byte[][] { bankmesg, nummesg, mesg };
            }
        else
            {
            System.err.println("Warning (KorgWavestationSequence): Unknown Key " + key);
            return new Object[0];
            }
        }



    // To create a sequence we:
    // 1. Initialize
    // 2. If the length is zero, we just delete the step created after initialization and we're done
    // 3. Else for N-1 steps
    // 3.1   For N - 1 steps
    // 3.1.1    Go to step zero
    // 3.1.2    Insert a step
    // 3.2   Go to step zero
    // 3.3   For N steps
    // 3.3.1     Change all step parameters
    // 3.3.2     Go to next step
    //
    // To update a sequence we start at line 3.2
    //
    // Also if we update a single parameter, we just do the appropriate item inside 3.3.1
    //
    // After initialization (1), insertion (3.1.2), and parameter changes (each change in 3.3.1) we do a pause.
    // the pauses are defined as:
    //
    // After initialization
    //      MS_PER_INITIALIZATION
    // After step insertion (but not step move) [steps are one-based here]
    //      MS_PER_STEP_BY_INDEX * step * 2 + MS_PER_STEP
    // After each parameter
    //      MS_PER_STEP_DATA
    //
    // Note that step insertion has O(n) pauses, so it's O(n^2) all together.  :-(
    // Also note that MS_PER_STEP_DATA is not the same as MS_PER_STEP, which is just
    // a constant on top of the O(n^2).
        

    public static final int MS_PER_STEP_BY_INDEX = 7;
    public static final int MS_PER_STEP = 250;
    public static final int MS_PER_INITIALIZATION = 1000;
    public static final int MS_PER_STEP_DATA = 80;
    int stepPos = 0;
    boolean sendingLength = false;
    public static final int MINIMUM_SENT_ELEMENTS_FOR_DISPLAY_CHANGE = 20;
    
    // This complicated function is meant to add some additional pauses at select locations when we do
    // bulk downloads of patches.  There's a bit of pause we have to do after initialization,
    // as well as some O(n^2) pauses we have to do after insertion (MS_PER_STEP_BY_INDEX and MS_PER_STEP),
    // plus some pauses we have to do to fill in the data (MS_PER_STEP_DATA).
    // Note that when we do a single parameter send we emit bank and number information, so we only
    // do a pause after ALL the parameter send data. 
    
    public void sentMIDI(Object datum, int index, int outOf)
        { 
        if (outOf == 0) // end of a MIDI sequence
            return;
        
        if (!sendingAllParameters)  // we're not doing a bulk send, it's just one parameter
            {
            // only pause at the end
            if (index == outOf - 1)
                {
                if (getSendMIDI()) simplePause(MS_PER_STEP_DATA);  // for typical parameters
                }
            }
        else            // bulk
            {
            if (totalParameters >= MINIMUM_SENT_ELEMENTS_FOR_DISPLAY_CHANGE)
                {
                sequenceGlobalCategory.setName("Sequence Sent: " + currentParameter + " / " + totalParameters);
                paintImmediately(sequenceGlobalCategory.getParent().getBounds());
                }               

            //// IMPORTANT NOTE -- the order here is based on code in emitAll(String[]) above.
                                
            if (datum == null)
                sendingLength = false;
            else if (!sendingLength)
                {
                if (getSendMIDI()) simplePause(MS_PER_STEP_DATA);  // for typical parameters
                }
            else if (sendingLength)
                {
                // My initial tests suggest that these values will work but I don't know if they'll
                // work in general for any memory configuration.
                                
                if (index == 0)                         // this is erasure
                    { 
                    if (getSendMIDI()) simplePause(MS_PER_INITIALIZATION);             // the minimum appears to be about 850ms
                    }
                else if (index >= stepPos)      // these are step data, we don't pause here
                    { 
                    if (getSendMIDI()) simplePause(MS_PER_STEP_DATA); 
                    }
                else if (index % 2 == 0)                // step insertion is at 2, 4, ...
                    { 
                    if (getSendMIDI()) simplePause(index * MS_PER_STEP_BY_INDEX + MS_PER_STEP); 
                    }
                }
            currentParameter++;
            }
        }
        
    public static String[] MAIN_KEYS = new String[]
    {
    "name", "length", "looprepeats", "loopbackandforth", "start", "loopstart", "loopend", "modulationamount", "modsource"
    };
        
    public static String[] STEP_KEYS = new String[]
    {
    "semitone", "fine", "level", "duration", "crossfade", "number", "bank"
    };
        
    public boolean getSendsAllParametersInBulk() { return false; }
    
    public static final int STATUS_KORG_WS_SEQUENCE_WRITING = -10000;
    
    int offsetParameters = 0;
    int totalParameters = 0;
    int currentParameter = 0;
    boolean sendingAllParameters = false;
    public void sendAllParameters()
        {
        if (!getSendMIDI())
            return;

        if (!writingParameters && blockSending.isSelected()) return;  // we don't send anything
                
        sendingAllParameters = true;
        totalParameters = offsetParameters + 14 * getModel().get("length") + 7;
        currentParameter = offsetParameters;
        
        // we have a hack here to send patch information first so we write it to the right place.
        tryToSendMIDI(new Object[] { paramBytes(WAVE_SEQ_BANK, edisynToWSBank[model.get("bank")]) });
        tryToSendMIDI(new Object[] { paramBytes(WAVE_SEQ_NUM, model.get("number", 0)) });
        super.sendAllParameters();      
        
        currentParameter = 0;
        offsetParameters = 0;
        totalParameters = 0;

        sequenceGlobalCategory.setName("Sequence");
        paintImmediately(sequenceGlobalCategory.getParent().getBounds());
        sendingAllParameters = false;
        }
    
    boolean writingParameters = false;
    
    public void writeAllParameters(Model model)
        {
        if (!getSendMIDI())
            return;        
        
        writingParameters = true; 

        if (verifyLengthChange(model.get("length")))
            {
            // we have a hack here to send patch information first so we write it to the right place.
            tryToSendMIDI(new Object[] { paramBytes(WAVE_SEQ_BANK, edisynToWSBank[model.get("bank")]) });
            tryToSendMIDI(new Object[] { paramBytes(WAVE_SEQ_NUM, model.get("number", 0)) });

            // send length first.  Note it doesn't send the wave parameters, those will get
            // sent in the next step automatically.
            sendingLength = true;  // this is set anyway, but whatever...
            Object[] obj = emitAll("length", STATUS_KORG_WS_SEQUENCE_WRITING);
            offsetParameters = obj.length;
            totalParameters = offsetParameters + 7 * model.get("length") + 7;
            tryToSendMIDI(obj);
            sendingLength = false;
                        
            // now send the other parameters, including wave parameters
            sendAllParameters();
            }
            
        writingParameters = false;
        }
                
    // We don't send in bulk, so this is for writing to files only
    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (!toFile) return new byte[0];
        
        byte[] sysex = new byte[STEP_KEYS.length * NUM_STEPS * 2 + (MAIN_KEYS.length - 1) * 2 + NAME_LENGTH + 23 + 2];  // 2 for bank and number

        sysex[0] = (byte)0xF0;
        sysex[1] = (byte)0x7D;
        sysex[2] = (byte)'E';
        sysex[3] = (byte)'D';
        sysex[4] = (byte)'I';
        sysex[5] = (byte)'S';
        sysex[6] = (byte)'Y';
        sysex[7] = (byte)'N';
        sysex[8] = (byte)' ';
        sysex[9] = (byte)'K';
        sysex[10] = (byte)'O';
        sysex[11] = (byte)'R';
        sysex[12] = (byte)'G';
        sysex[13] = (byte)'W';
        sysex[14] = (byte)'S';
        sysex[15] = (byte)'S';
        sysex[16] = (byte)'R';
        sysex[17] = (byte)' ';
        sysex[18] = (byte)'S';
        sysex[19] = (byte)'E';
        sysex[20] = (byte)'Q';
        sysex[21] = (byte)0;            // sysex version
        
        int pos = 22;
        
        sysex[pos++] = (byte)model.get("bank");
        sysex[pos++] = (byte)model.get("number");
        
        for(int i = 0; i < MAIN_KEYS.length; i++)
            {
            if (MAIN_KEYS[i].equals("name"))
                {
                char[] name = (model.get(MAIN_KEYS[i], "") + "                ").toCharArray();
                for(int j = 0; j < NAME_LENGTH; j++)
                    {
                    sysex[pos++] = (byte)name[j];
                    }
                }
            else
                {
                int val = model.get(MAIN_KEYS[i], 0);
                sysex[pos++] = (byte)((val >>> 7) & 127);
                sysex[pos++] = (byte)(val & 127);
                }
            }
        for(int j = 0; j < NUM_STEPS; j++)
            {
            for(int i = 0; i < STEP_KEYS.length; i++)
                {
                int val = model.get("step" + (j + 1) + STEP_KEYS[i], 0);
                sysex[pos++] = (byte)((val >>> 7) & 127);
                sysex[pos++] = (byte)(val & 127);
                }
            }
        
        sysex[sysex.length - 1] = (byte)0xF7;
        return sysex;
        }

    public int getPauseAfterChangePatch() { return 300; }  // looks like 300 is about the minimum for a standard PC (see Performance.java); may be too much here.
    
    public void changePatch(Model tempModel)
        {
        // Not sure if we need to do this.  See Developer FAQ about writing performances
        byte[] midi_mesg = paramBytes(MIDI_MODE, MULTISET_MIDI_MODE);
        tryToSendSysex(midi_mesg);
        
        byte[] midi_mesg_2 = paramBytes(MIDI_MODE, PERFORMANCE_MIDI_MODE);
        tryToSendSysex(midi_mesg_2);
        
        byte[] wave_bank_mesg = paramBytes(WAVE_BANK, edisynToWSBank[tempModel.get("bank", 0)]);
        tryToSendSysex(wave_bank_mesg);

        byte[] wave_num_mesg = paramBytes(WAVE_NUM, tempModel.get("number", 0));                
        tryToSendSysex(wave_num_mesg);
        }

    public byte[] requestDump(Model tempModel)
        {
        model.set("bank", tempModel.get("bank"));
        model.set("number", tempModel.get("number"));
        byte BB = (byte)edisynToWSBank[tempModel.get("bank")];
        return new byte[] { (byte)0xF0, (byte)0x42, (byte)(48 + getChannelOut()), 0x28, 0x0C, BB, (byte)0xF7 };
        }
                
    public static final int EXPECTED_SYSEX_LENGTH = 17576;
    public static boolean recognize(byte[] data)
        {
        if (data.length > 22 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x7D &&
            data[2] == (byte)'E' &&
            data[3] == (byte)'D' &&
            data[4] == (byte)'I' &&
            data[5] == (byte)'S' &&
            data[6] == (byte)'Y' &&
            data[7] == (byte)'N' &&
            data[8] == (byte)' ' &&
            data[9] == (byte)'K' &&
            data[10] == (byte)'O' &&
            data[11] == (byte)'R' &&
            data[12] == (byte)'G' &&
            data[13] == (byte)'W' &&
            data[14] == (byte)'S' &&
            data[15] == (byte)'S' &&
            data[16] == (byte)'R' &&
            data[17] == (byte)' ' &&
            data[18] == (byte)'S' &&
            data[19] == (byte)'E' &&
            data[20] == (byte)'Q' &&
            data[21] == (byte)0)
            return true;
        else return recognizeBulk(data);
        }
        
    public static boolean recognizeBulk(byte[] data)
        {
        boolean b = (data.length == EXPECTED_SYSEX_LENGTH &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x42 &&
            data[3] == (byte)0x28 &&
            data[4] == (byte)0x54);   
        return b;              
        }
    
    
    
    
    /////// OTHER ABSTRACT METHODS
    
    public static final int NO_BANK = -1;
    boolean requestingPatch = false;
    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        return (requestingPatch = gatherPatchInfo2(title, change, writing, NO_BANK));
        }
    
    public boolean gatherPatchInfo2(String title, Model change, boolean writing, int bnk)
        {
        JComboBox bank = new JComboBox(BANKS);
        bank.setEditable(false);
        bank.setMaximumRowCount(32);
        bank.setSelectedIndex(model.get("bank"));
                
        JTextField number = new JTextField("" + model.get("number"), 3);
                
        while(true)
            {
            boolean result;
            
            if (bnk == NO_BANK)
                {
                result = showMultiOption(this, new String[] { "Bank", "Patch Number"}, 
                    new JComponent[] { bank, number }, title, "Enter the Bank and Patch number.");
                }
            else
                {
                result = showMultiOption(this, new String[] { "Patch Number"}, 
                    new JComponent[] { number }, title, "Bank " + BANKS[bnk] + " is loaded.  Enter patch number.");
                }
                
            if (result == false) 
                return false;
                
            int n;
            try { n = Integer.parseInt(number.getText()); }
            catch (NumberFormatException e)
                {
                showSimpleError(title, "The Patch Number must be an integer 0 ... 31");
                continue;
                }
            if (n < 0 || n > 31)
                {
                showSimpleError(title, "The Patch Number must be an integer 0 ... 31");
                continue;
                }
                                
            change.set("bank", bnk == NO_BANK ? bank.getSelectedIndex() : bnk);
            change.set("number", n);
                        
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
            
        // revise start to under length
        int length = model.get("length", 1);
        int start = model.get("start", 1);
        if (length == 0)
            {
            model.set("start", 1);
            model.setMax("start", 1);
            }
        else if (length < start)
            {
            model.set("start", length);
            model.setMax("start", length);
            }
        else
            {
            model.setMax("start", length);
            }
                                
        // revise loopend to under length
        int loopend = model.get("loopend", 1);
        if (length == 0)
            {
            model.set("loopend", 1);
            model.setMax("loopend", 1);
            }
        else if (length < loopend)
            {
            model.set("loopend", length);
            model.setMax("loopend", length);
            }
        else
            {
            model.setMax("loopend", length);
            }
                                
        // revise loopstart to under loopend
        int loopstart = model.get("loopstart", 1);
        if (length == 0)
            {
            model.set("loopstart", 1);
            model.setMax("loopstart", 1);
            }
        else if (loopend < loopstart)
            {
            model.set("loopstart", loopend);
            model.setMax("loopstart", length);
            }
        else
            {
            model.setMax("loopstart", length);
            }
                                
        // bound the step index
        int step = model.get("step", 1);
        if (length == 0)
            {
            model.set("step", 1);
            model.setMax("step", 1);
            }
        else if (length < step)
            {
            model.setMax("step", length);
            model.set("step", length);
            }
        else
            {
            model.setMax("step", length);
            }
        }
        
    public static String getSynthName() { return "Korg Wavestation SR [Sequence]"; }
    
    public String getPatchName(Model model) { return model.get("name", "Init"); }    
    
    public void deleteStep()
        {
        int len = model.get("length");
        if (len <= 0)
            showSimpleMessage("Sequence Empty", "Cannot delete any more steps.");
        else
            {                       
            int current = model.get("step", 1);
            if (!blockSending.isSelected())
                {         
                tryToSendSysex(paramBytes(WAVE_SEQ_STEP, model.get("step", 1)));
                tryToSendSysex(paramBytes(EXECUTE_DELETE_WS_STEP, 1));
                }
            setSendMIDI(false);
            for(int i = current; i < len; i++)
                {
                model.set("step" + i + "semitone", model.get("step" + (i + 1) + "semitone", 0));
                model.set("step" + i + "fine", model.get("step" + (i + 1) + "fine", 0));
                model.set("step" + i + "level", model.get("step" + (i + 1) + "level", 0));
                model.set("step" + i + "duration", model.get("step" + (i + 1) + "duration", 1));
                model.set("step" + i + "crossfade", model.get("step" + (i + 1) + "crossfade", 0));
                model.set("step" + i + "number", model.get("step" + (i + 1) + "number", 0));
                model.set("step" + i + "bank", model.get("step" + (i + 1) + "bank", 0));        ///// ROM?  CARD?   Dunno.
                }
            len--;
            model.set("length", len);
            if (current > 1)
                current--;  // The WS backs up after a delete.  Weird. 
            setSendMIDI(true);
            model.set("step", current);
            }
        }

    public void insertStep(boolean before)
        {
        int len = model.get("length") + 1;
        int current = model.get("step", 1);

        if (len > 255)
            showSimpleMessage("Sequence Full", "Cannot add any more steps.");
        else if (len == 1)
            {
            // If the length is 0 -> 1, then we just do a simple insert at position 1.  We set the new length and the step to 1,
            // then we update step 1's values to defaults
                        
            if (!blockSending.isSelected())
                {         
                tryToSendSysex(paramBytes(WAVE_SEQ_STEP, 1));
                tryToSendSysex(paramBytes(EXECUTE_INSERT_WS_STEP, 1));
                }
            setSendMIDI(false);
            model.set("length", len);
            setSendMIDI(true);
            model.set("step", 1);

            model.set("step" + 1 + "semitone", 0);
            model.set("step" + 1 + "fine", 0);
            model.set("step" + 1 + "level", 0);
            model.set("step" + 1 + "duration", 1);
            model.set("step" + 1 + "crossfade", 0);
            model.set("step" + 1 + "number", 0);
            model.set("step" + 1 + "bank", 0);      ///// ROM?  CARD?   Dunno.
            }
        else
            {
            // since unlike the WS's insert mechanism we make a full copy, it doesn't *really*
            // matter if we're inserting before or after.  The only difference of consequence is where
            // we put the new step number!
               
            if (!blockSending.isSelected())
                {         
                tryToSendSysex(paramBytes(WAVE_SEQ_STEP, current));
                tryToSendSysex(paramBytes(EXECUTE_INSERT_WS_STEP, 1));
                }
            setSendMIDI(false);
            for(int i = len; i >= current; i--)             // note >=
                {
                model.set("step" + i + "semitone", model.get("step" + (i - 1) + "semitone", 0));
                model.set("step" + i + "fine", model.get("step" + (i - 1) + "fine", 0));
                model.set("step" + i + "level", model.get("step" + (i - 1) + "level", 0));
                model.set("step" + i + "duration", model.get("step" + (i - 1) + "duration", 1));
                model.set("step" + i + "crossfade", model.get("step" + (i - 1) + "crossfade", 0));
                model.set("step" + i + "number", model.get("step" + (i - 1) + "number", 0));
                model.set("step" + i + "bank", model.get("step" + (i - 1) + "bank", 0));        ///// ROM?  CARD?   Dunno.
                }
            model.set("length", len);
            setSendMIDI(true);
            model.set("step", before ? current : (current + 1));

            // The WS inserts "before" by default.  So the new step is at the "current" step now.  We need to revise it.
            // We'll make it a copy of the "old" current step.
            model.set("step" + current + "semitone", model.get("step" + (current + 1) + "semitone", 0));
            model.set("step" + current + "fine", model.get("step" + (current + 1) + "fine", 0));
            model.set("step" + current + "level", model.get("step" + (current + 1) + "level", 0));
            model.set("step" + current + "duration", model.get("step" + (current + 1) + "duration", 1));
            model.set("step" + current + "crossfade", model.get("step" + (current + 1) + "crossfade", 0));
            model.set("step" + current + "number", model.get("step" + (current + 1) + "number", 0));
            model.set("step" + current + "bank", model.get("step" + (current + 1) + "bank", 0));    ///// ROM?  CARD?   Dunno.
                        
                        
                        
            }
        }

    public Model getNextPatchLocation(Model model)
        {
        int bank = model.get("bank");
        int number = model.get("number");
        
        number++;
        if (number >= 32)
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

    public String getPatchLocationName(Model model)
        {
        // getPatchLocationName() is called from sprout() as a test to see if we should enable
        // batch downloading.  If we haven't yet created an .init file, then parameters won't exist
        // yet and this method will bomb badly.  So we return null in this case.
        if (!model.exists("number")) return null;
        if (!model.exists("bank")) return null;
        
        int number = model.get("number");
        return BANKS[model.get("bank")] + " " + (number > 9 ? "" : "0")  + number;
        }

    public void sendTestPerformance()
        {
        if (tuple == null)
            if (!setupMIDI(tuple))
                return;

        if (tuple != null)
            {
            final KorgWavestationPerformance synth = new KorgWavestationPerformance();
            synth.tuple = tuple.copy(synth.buildInReceiver(), synth.buildKeyReceiver());
            if (synth.tuple != null)
                {
                synth.loadDefaults();
                synth.getModel().set("part1bank", DEFAULT_PATCH_BANK);
                synth.getModel().set("part1number", DEFAULT_PATCH_NUM);
                synth.performChangePatch(synth.getModel());
                synth.tryToSendMIDI(synth.emitAll(synth.getModel(), true, false));
                }
            }
                        
        if (tuple != null)
            {
            final KorgWavestationPatch synth2 = new KorgWavestationPatch();
            synth2.tuple = tuple.copy(synth2.buildInReceiver(), synth2.buildKeyReceiver());
            if (synth2.tuple != null)
                {
                synth2.loadDefaults();
                synth2.getModel().set("osc1wavebank", model.get("bank"));
                synth2.getModel().set("osc1wave", model.get("number"));
                synth2.performChangePatch(synth2.getModel());
                synth2.tryToSendMIDI(synth2.emitAll(synth2.getModel(), true, false));
                }
            }
        }
    
    public void stepSolo()
        {
        tryToSendSysex(paramBytes(WAVE_SEQ_BANK, edisynToWSBank[model.get("bank")]));
        tryToSendSysex(paramBytes(WAVE_SEQ_NUM, model.get("number", 0)));
        tryToSendSysex(paramBytes(EXECUTE_SOLO_WS_STEP, 1));
        }
        
    public void addWavestationMenu(JMenu menu)
        {
        JMenuItem sendTestPerformanceMenu = new JMenuItem("Set up Test Performance/Patch in RAM 1 Slot 0 Wave A");
        sendTestPerformanceMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                sendTestPerformance();
                }
            });
        menu.add(sendTestPerformanceMenu);

        JMenuItem soloMenu = new JMenuItem("Toggle Solo-Step [First Press Perf, then Edit]");
        soloMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                stepSolo();
                }
            });
        menu.add(soloMenu);

        blockSending = new JCheckBoxMenuItem("Block Sending Any Parameters");
        blockSending.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                setLastX(blockSending.isSelected() ? "YES" : "NO", "BlockSendingParameters", getSynthName(), true);
                }
            });
        menu.add(blockSending);

        String str = getLastX("BlockSendingParameters", getSynthName(), true);
        
        if (str == null)
            blockSending.setSelected(false);
        else if (str.equalsIgnoreCase("YES"))
            blockSending.setSelected(true);
        else 
            blockSending.setSelected(false);
        }

    public boolean getSendsParametersAfterLoad()
        {
        return false;
        }

    }
    
