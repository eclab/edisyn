/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn;

import java.util.*;
import java.io.*;
import edisyn.gui.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import edisyn.synth.*;

/***

    Procedure:

    Menu -> Start Hill-Climb [or Menu -> Reset Hill-Climb if already started]
    Creates Hill-Climb Panel and Resets it to current patch
    Menu -> Stop Hill-Climb
    Deletes Hill-Climb Panel

    Each sound has:
    1. Done (or Keep?) -> Deletes Hill-Climb Panel and sets current patch to this sound [probably requires double-check dialog panel]
    2. Try -> Sends sound to current patch
    3. Best / Second Best / Third Best

    The Panel also has:
    1. Iterate
    2. Rate
    3. Backup
    
    
    Hill-Climbing variations
    
    

***/


public class HillClimb extends SynthPanel
    {
    public static final int NUM_CANDIDATES = 16;
    public static final int NUM_MODELS = NUM_CANDIDATES + 1;

    ArrayList oldA = new ArrayList();
    ArrayList oldB = new ArrayList();
    ArrayList oldC = new ArrayList();
    Model[] currentModels = new Model[NUM_MODELS];
    Model[] bestModels = new Model[3];
    JRadioButton[][] ratings = new JRadioButton[NUM_MODELS + 1][3];
    PushButton[] plays = new PushButton[NUM_MODELS];
    public static final int INITIAL_MUTATION_RATE = 10;
    public static final int INITIAL_RECOMBINATION_RATE = 75;
    Blank blank;
    Category iterations;
        
    int currentPlay = 0;
        
    public HillClimb(Synth synth)
        {
        super(synth);
        
        ButtonGroup one = new ButtonGroup();
        ButtonGroup two = new ButtonGroup();
        ButtonGroup three = new ButtonGroup();
                
        VBox top = new VBox();
        HBox toprow = new HBox();
        add(top, BorderLayout.CENTER);

        // add globals

        Category panel = new Category(null, "Iteration 1", Style.COLOR_A());
        iterations = panel;
                
        PushButton backup = new PushButton("Back Up")
            {
            public void perform()
                {
                pop();
                resetCurrentPlay();
                }
            };
            
        PushButton climb = new PushButton("Climb")
            {
            public void perform()
                {
                climb(true);
                resetCurrentPlay();
                }
            };
        climb.getButton().setPreferredSize(backup.getButton().getPreferredSize());
                
        PushButton reset = new PushButton("Reset")
            {
            public void perform()
                {
                initialize((Model)(synth.getModel().clone()), true);
                resetCurrentPlay();
                }
            };
        reset.getButton().setPreferredSize(backup.getButton().getPreferredSize());
                
        PushButton retry = new PushButton("Retry")
            {
            public void perform()
                {
                again();
                resetCurrentPlay();
                }
            };
        retry.getButton().setPreferredSize(backup.getButton().getPreferredSize());
                
        VBox vbox = new VBox();
        
        
        VBox buttonVBox = new VBox();
        HBox buttonBox = new HBox();
        buttonBox.add(climb);
        buttonBox.add(retry);
        buttonVBox.add(buttonBox);
        buttonBox = new HBox();
        buttonBox.add(backup);
        buttonBox.add(reset);
        buttonVBox.add(buttonBox);
                

        blank = new Blank();
        HBox ratebox = new HBox();
        ratebox.add(buttonVBox);
        LabelledDial recombinationRate = new LabelledDial("Recombination", blank, "recombinationrate", Style.COLOR_A(), 0, 100);
        blank.getModel().set("recombinationrate", INITIAL_RECOMBINATION_RATE);
        recombinationRate.addAdditionalLabel("Rate");
        ratebox.add(recombinationRate);

        LabelledDial mutationRate = new LabelledDial("Mutation", blank, "mutationrate", Style.COLOR_A(), 0, 100);
        mutationRate.addAdditionalLabel("Rate");
        blank.getModel().set("mutationrate", INITIAL_MUTATION_RATE);
        ratebox.add(mutationRate);
        vbox.add(ratebox);

                                
        panel.add(vbox, BorderLayout.WEST);
        toprow.add(panel);
        
        // Add None
        panel = new Category(null, "None", Style.COLOR_B());
        
        vbox = new VBox();
        Box b = new Box(BoxLayout.X_AXIS);
        b.setBackground(Style.BACKGROUND_COLOR());
        b.add(Box.createGlue());
        b.add(ratings[NUM_MODELS][0] = new JRadioButton("1"));
        ratings[NUM_MODELS][0].setForeground(Style.TEXT_COLOR());
        ratings[NUM_MODELS][0].setFont(Style.SMALL_FONT());
        ratings[NUM_MODELS][0].putClientProperty("JComponent.sizeVariant", "small");
        b.add(Box.createGlue());
        vbox.add(b);
                        
        b = new Box(BoxLayout.X_AXIS);
        b.setBackground(Style.BACKGROUND_COLOR());
        b.add(Box.createGlue());
        b.add(ratings[NUM_MODELS][1] = new JRadioButton("2"));
        ratings[NUM_MODELS][1].setForeground(Style.TEXT_COLOR());
        ratings[NUM_MODELS][1].setFont(Style.SMALL_FONT());
        ratings[NUM_MODELS][1].putClientProperty("JComponent.sizeVariant", "small");
        b.add(Box.createGlue());
        vbox.add(b);
                        
        b = new Box(BoxLayout.X_AXIS);
        b.setBackground(Style.BACKGROUND_COLOR());
        b.add(Box.createGlue());
        b.add(ratings[NUM_MODELS][2] = new JRadioButton("3"));
        ratings[NUM_MODELS][2].setForeground(Style.TEXT_COLOR());
        ratings[NUM_MODELS][2].setFont(Style.SMALL_FONT());
        ratings[NUM_MODELS][2].putClientProperty("JComponent.sizeVariant", "small");
        b.add(Box.createGlue());
        vbox.add(b);
        VBox bar = new VBox();
        bar.addBottom(vbox);
        HBox foo = new HBox();
        foo.add(bar);
        foo.add(Strut.makeHorizontalStrut(40));
        panel.add(foo);
        toprow.add(panel);
                
        // Add Current 
        panel = new Category(null, "Current", Style.COLOR_B());
        
        vbox = new VBox();
 
        vbox = new VBox();
        plays[NUM_MODELS - 1] = new PushButton("Play")
            {
            public void perform()
                {
                if (synth.isSendingTestNotes())
                    {
                    currentPlay = NUM_MODELS - 1;
                    }
                else
                    {
                    // change the model, send all parameters, maybe play a note,
                    // and then restore the model.
                    Model backup = synth.model;
                    synth.model = currentModels[NUM_MODELS - 1];
                    synth.sendAllParameters();
                    synth.doSendTestNote(false);
                    synth.model = backup;
                    }

                }
            };
        vbox.add(plays[NUM_MODELS - 1]);

        b = new Box(BoxLayout.X_AXIS);
        b.setBackground(Style.BACKGROUND_COLOR());
        b.add(Box.createGlue());
        b.add(ratings[NUM_MODELS - 1][0] = new JRadioButton("1"));
        ratings[NUM_MODELS - 1][0].setForeground(Style.TEXT_COLOR());
        ratings[NUM_MODELS - 1][0].setFont(Style.SMALL_FONT());
        ratings[NUM_MODELS - 1][0].putClientProperty("JComponent.sizeVariant", "small");
        b.add(Box.createGlue());
        vbox.add(b);
                        
        b = new Box(BoxLayout.X_AXIS);
        b.setBackground(Style.BACKGROUND_COLOR());
        b.add(Box.createGlue());
        b.add(ratings[NUM_MODELS - 1][1] = new JRadioButton("2"));
        ratings[NUM_MODELS - 1][1].setForeground(Style.TEXT_COLOR());
        ratings[NUM_MODELS - 1][1].setFont(Style.SMALL_FONT());
        ratings[NUM_MODELS - 1][1].putClientProperty("JComponent.sizeVariant", "small");
        b.add(Box.createGlue());
        vbox.add(b);
                        
        b = new Box(BoxLayout.X_AXIS);
        b.setBackground(Style.BACKGROUND_COLOR());
        b.add(Box.createGlue());
        b.add(ratings[NUM_MODELS - 1][2] = new JRadioButton("3"));
        ratings[NUM_MODELS - 1][2].setForeground(Style.TEXT_COLOR());
        ratings[NUM_MODELS - 1][2].setFont(Style.SMALL_FONT());
        ratings[NUM_MODELS - 1][2].putClientProperty("JComponent.sizeVariant", "small");
        b.add(Box.createGlue());
        vbox.add(b);
        foo = new HBox();
        foo.add(vbox);
        panel.add(foo);
                
        toprow.addLast(panel);
        top.add(toprow);

        // Add Candidates

        panel =  new Category(null, "Candidates", Style.COLOR_B());

        HBox hbox = new HBox();
                
        VBox vr = new VBox();
        for(int i = 0; i < 16; i++)
            {
            final int _i = i;

            vbox = new VBox();
            plays[i] = new PushButton("Play")
                {
                public void perform()
                    {
                    if (synth.isSendingTestNotes())
                        {
                        currentPlay = _i - 1;
                        }
                    else
                        {
                        // change the model, send all parameters, maybe play a note,
                        // and then restore the model.
                        Model backup = synth.model;
                        synth.model = currentModels[_i];
                        synth.sendAllParameters();
                        synth.doSendTestNote(false);
                        synth.model = backup;
                        }

                    }
                };
            vbox.add(plays[i]);

            b = new Box(BoxLayout.X_AXIS);
            b.setBackground(Style.BACKGROUND_COLOR());
            b.add(Box.createGlue());
            b.add(ratings[i][0] = new JRadioButton("1"));
            ratings[i][0].setForeground(Style.TEXT_COLOR());
            ratings[i][0].setFont(Style.SMALL_FONT());
            ratings[i][0].putClientProperty("JComponent.sizeVariant", "small");
            b.add(Box.createGlue());
            vbox.add(b);
                        
            b = new Box(BoxLayout.X_AXIS);
            b.setBackground(Style.BACKGROUND_COLOR());
            b.add(Box.createGlue());
            b.add(ratings[i][1] = new JRadioButton("2"));
            ratings[i][1].setForeground(Style.TEXT_COLOR());
            ratings[i][1].setFont(Style.SMALL_FONT());
            ratings[i][1].putClientProperty("JComponent.sizeVariant", "small");
            b.add(Box.createGlue());
            vbox.add(b);
                        
            b = new Box(BoxLayout.X_AXIS);
            b.setBackground(Style.BACKGROUND_COLOR());
            b.add(Box.createGlue());
            b.add(ratings[i][2] = new JRadioButton("3"));
            ratings[i][2].setForeground(Style.TEXT_COLOR());
            ratings[i][2].setFont(Style.SMALL_FONT());
            ratings[i][2].putClientProperty("JComponent.sizeVariant", "small");
            b.add(Box.createGlue());
            vbox.add(b);

            vbox.add(new PushButton("Keep")
                {
                public void perform()
                    {
                    // Keep for sure?
                    if (synth.showSimpleConfirm("Keep Patch", "Load Patch into Editor?"))
                        {
                        synth.tabs.setSelectedIndex(0);
                        synth.setSendMIDI(false);
                        // push to undo if they're not the same
                        if (!currentModels[_i].keyEquals(synth.getModel()))
                            synth.undo.push(synth.getModel());
                                        
                        // Load into the current model
                        currentModels[_i].copyValuesTo(synth.getModel());
                        synth.setSendMIDI(true);
                        synth.sendAllParameters();
                        }
                    }
                });
            hbox.add(vbox);
            if (i == 7)
                {
                vr.add(hbox);
                vr.add(Strut.makeVerticalStrut(20));
                hbox = new HBox();
                }
            }
        vr.add(hbox);

        panel.add(vr, BorderLayout.WEST);
        top.add(panel);
        
        for(int i = 0; i < ratings.length; i++)
            {
            one.add(ratings[i][0]);
            two.add(ratings[i][1]);
            three.add(ratings[i][2]);
            }                
        }       
                
    public static final int UPDATE_SOUND_RATE = 1;
    int updateSoundTick = 0;
    Model backup = null;
        
    boolean isShowingPane()
        {
        return (synth.hillClimbPane != null && synth.tabs.getSelectedComponent() == synth.hillClimbPane);
        }
                
    public void updateSound()
        {
        updateSoundTick++;
        if (updateSoundTick >= UPDATE_SOUND_RATE)
            updateSoundTick = 0;
                        
        if (updateSoundTick == 0)
            {
            if (isShowingPane())
                {
                for(int i = 0; i < NUM_CANDIDATES; i++)                             
                    plays[i].getButton().setForeground(new JButton().getForeground());
                currentPlay++;
                if (currentPlay >= NUM_CANDIDATES)
                    currentPlay = 0;
                plays[currentPlay].getButton().setForeground(Color.RED);

                // change the model, send all parameters, maybe play a note,
                // and then restore the model.
                backup = synth.model;
                synth.model = currentModels[currentPlay];
                synth.sendAllParameters();
                }
            }
        }
                
    public void postUpdateSound()
        {
        if (backup!= null)
            synth.model = backup;
        backup = null;
        }

    boolean startedUp = false;
        
    public void startup()
        {
        if (!startedUp)
            {
            resetCurrentPlay();
            if (!synth.isSendingTestNotes())
                {
                synth.doSendTestNotes();
                }
            }
        startedUp = true;
        }
                
    public void shutdown()
        {
        if (startedUp)
            {
            synth.doSendAllSoundsOff();
            if (synth.isSendingTestNotes())
                {
                synth.doSendTestNotes();
                }
            // restore patch
            synth.sendAllParameters();
            }
        startedUp = false;
        }

    public void resetCurrentPlay()
        {
        currentPlay = 15;
        }
                        
    public void again()
        {
        if (oldA.size() > 1)
            {
            // rebuild
            bestModels[0] = (Model)(oldA.remove(oldA.size() - 1));
            bestModels[1] = (Model)(oldB.remove(oldB.size() - 1));
            bestModels[2] = (Model)(oldC.remove(oldC.size() - 1));
            climb(false);
            }
        else
            {
            // Just rebuild
            Model seed = (Model)(oldA.remove(oldA.size() - 1));
            oldB.remove(oldB.size() - 1);
            oldC.remove(oldC.size() - 1);
            initialize(seed, true);
            }

        ratings[NUM_MODELS][0].setSelected(true);
        ratings[NUM_MODELS][1].setSelected(true);
        ratings[NUM_MODELS][2].setSelected(true);
        }
        
    public void pop()
        {
        if (oldA.size() > 2)
            {
            // remove the current old stuff
            oldA.remove(oldA.size() - 1);
            oldB.remove(oldB.size() - 1);
            oldC.remove(oldC.size() - 1);
                        
            // back up to the previous old stuff and rebuild
            bestModels[0] = (Model)(oldA.remove(oldA.size() - 1));
            bestModels[1] = (Model)(oldB.remove(oldB.size() - 1));
            bestModels[2] = (Model)(oldC.remove(oldC.size() - 1));
            climb(false);
            }
        else if (oldA.size() > 1)
            {
            // remove the current old stuff
            oldA.remove(oldA.size() - 1);
            oldB.remove(oldB.size() - 1);
            oldC.remove(oldC.size() - 1);
                        
            // back up to the previous old stuff and rebuild
            Model seed = (Model)(oldA.remove(oldA.size() - 1));
            oldB.remove(oldB.size() - 1);
            oldC.remove(oldC.size() - 1);
            initialize(seed, true);
            }
        else
            {
            // Just rebuild
            Model seed = (Model)(oldA.remove(oldA.size() - 1));
            oldB.remove(oldB.size() - 1);
            oldC.remove(oldC.size() - 1);
            initialize(seed, true);
            }
                
        iterations.setName("Iteration " + oldA.size());
        repaint();

        ratings[NUM_MODELS][0].setSelected(true);
        ratings[NUM_MODELS][1].setSelected(true);
        ratings[NUM_MODELS][2].setSelected(true);
        }
                
    public void initialize(Model seed, boolean clear)
        {
        // we need a model with NO callbacks
        Model newSeed = (Model)(seed.clone());
        newSeed.clearListeners();
                
        if (clear)
            {
            oldA.clear();
            oldB.clear();
            oldC.clear();
            }
                
        Random random = synth.random;
        String[] keys = synth.getMutationKeys();
        double weight = blank.getModel().get("mutationrate", 0) / 100.0; //mutationRate.getValue() / 100.0;
                
        for(int i = 0; i < 4; i++)
            {
            currentModels[i] = ((Model)(newSeed.clone())).mutate(random, keys, weight * MUTATION_WEIGHT);
            }

        for(int i = 0; i < 4; i++)
            {
            currentModels[i + 4] = ((Model)(currentModels[i].clone())).mutate(random, keys, weight * MUTATION_WEIGHT);
            }

        for(int i = 0; i < 4; i++)
            {
            currentModels[i + 8] = ((Model)(currentModels[i + 4].clone())).mutate(random, keys, weight * MUTATION_WEIGHT);
            }

        for(int i = 0; i < 4; i++)
            {
            currentModels[i + 12] = ((Model)(currentModels[i + 8].clone())).mutate(random, keys, weight * MUTATION_WEIGHT);
            }

        oldA.add(newSeed);
        oldB.add(newSeed);
        oldC.add(newSeed);
        iterations.setName("Iteration " + oldA.size());
        repaint();

        ratings[NUM_MODELS][0].setSelected(true);
        ratings[NUM_MODELS][1].setSelected(true);
        ratings[NUM_MODELS][2].setSelected(true);
        }

    void shuffle(Random random, Model[] array, int len)
        {
        for (int i = len - 1; i > 0; i--)
            {
            int index = random.nextInt(i + 1);
            Model temp = array[index];
            array[index] = array[i];
            array[i] = temp;
            }
        }

	public static double MUTATION_WEIGHT = 0.5;
	
    public void produce(Random random, String[] keys, double recombination, double weight, Model a, Model b, Model c, Model oldA)
        {
        // A + B
        currentModels[0] = ((Model)(a.clone())).recombine(random, b, keys, recombination).mutate(random, keys, weight * MUTATION_WEIGHT);
        // A + C
        currentModels[1] = ((Model)(a.clone())).recombine(random, c, keys, recombination).mutate(random, keys, weight * MUTATION_WEIGHT);
        // B + C
        currentModels[2] = ((Model)(b.clone())).recombine(random, c, keys, recombination).mutate(random, keys, weight * MUTATION_WEIGHT);
        // A + (B + C)
        currentModels[3] = ((Model)(a.clone())).recombine(random, ((Model)(b.clone())).recombine(random, c, keys, recombination), keys, recombination).mutate(random, keys, weight * MUTATION_WEIGHT);
        // A - B
        currentModels[4] = ((Model)(a.clone())).opposite(random, b, keys, recombination, false).mutate(random, keys, weight * MUTATION_WEIGHT);
		// B - A
        currentModels[5] = ((Model)(b.clone())).opposite(random, a, keys, recombination, false).mutate(random, keys, weight * MUTATION_WEIGHT);
        // A - C
        currentModels[6] = ((Model)(a.clone())).opposite(random, c, keys, recombination, false).mutate(random, keys, weight * MUTATION_WEIGHT);
        // C - A
        currentModels[7] = ((Model)(c.clone())).opposite(random, a, keys, recombination, false).mutate(random, keys, weight * MUTATION_WEIGHT);
        // B - C
        currentModels[8] = ((Model)(b.clone())).opposite(random, c, keys, recombination, false).mutate(random, keys, weight * MUTATION_WEIGHT);
        // C - B
        currentModels[9] = ((Model)(c.clone())).opposite(random, b, keys, recombination, false).mutate(random, keys, weight * MUTATION_WEIGHT);
        // A - Z
        currentModels[10] = ((Model)(a.clone())).opposite(random, oldA, keys, recombination, false).mutate(random, keys, weight * MUTATION_WEIGHT);
        // B - Z
        currentModels[11] = ((Model)(b.clone())).opposite(random, oldA, keys, recombination, false).mutate(random, keys, weight * MUTATION_WEIGHT);
        // C - Z
        currentModels[12] = ((Model)(c.clone())).opposite(random, oldA, keys, recombination, false).mutate(random, keys, weight * MUTATION_WEIGHT);
        // A
        currentModels[13] = ((Model)(a.clone())).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT);
        // B
        currentModels[14] = ((Model)(b.clone())).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT);
        // C
        currentModels[15] = ((Model)(c.clone())).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT);
        
        shuffle(random, currentModels, NUM_MODELS - 1);
        }
        
    public void produce(Random random, String[] keys, double recombination, double weight, Model a, Model b, Model oldA)
        {
        // A + B
        currentModels[0] = ((Model)(a.clone())).recombine(random, b, keys, recombination).mutate(random, keys, weight * MUTATION_WEIGHT);
        currentModels[1] = ((Model)(a.clone())).recombine(random, b, keys, recombination).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT);
        currentModels[2] = ((Model)(a.clone())).recombine(random, b, keys, recombination).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT);
        
        // A - B
        currentModels[3] = ((Model)(a.clone())).opposite(random, b, keys, recombination, false).mutate(random, keys, weight * MUTATION_WEIGHT);
        currentModels[4] = ((Model)(a.clone())).opposite(random, b, keys, recombination, false).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT);
        
        // B - A
        currentModels[5] = ((Model)(b.clone())).opposite(random, a, keys, recombination, false).mutate(random, keys, weight * MUTATION_WEIGHT);
        currentModels[6] = ((Model)(b.clone())).opposite(random, a, keys, recombination, false).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT);
        
        // A - Z
        currentModels[7] = ((Model)(a.clone())).opposite(random, oldA, keys, recombination, false).mutate(random, keys, weight * MUTATION_WEIGHT);
        currentModels[8] = ((Model)(a.clone())).opposite(random, oldA, keys, recombination, false).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT);
        
        // B - Z
        currentModels[9] = ((Model)(b.clone())).opposite(random, oldA, keys, recombination, false).mutate(random, keys, weight * MUTATION_WEIGHT);
        currentModels[10] = ((Model)(b.clone())).opposite(random, oldA, keys, recombination, false).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT);

		// (A - Z) + (B - Z)
        currentModels[11] = ((Model)(a.clone())).opposite(random, oldA, keys, recombination, false).recombine(random, 
        		((Model)(b.clone())).opposite(random, oldA, keys, recombination, false), keys, recombination).mutate(random, keys, weight * MUTATION_WEIGHT);

		// A
        currentModels[12] = ((Model)(a.clone())).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT);
        currentModels[13] = ((Model)(a.clone())).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT);
        
        // B
        currentModels[14] = ((Model)(b.clone())).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT);
        currentModels[15] = ((Model)(b.clone())).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT);
        
        shuffle(random, currentModels, NUM_MODELS - 1);
        }
                
    public void produce(Random random, String[] keys, double recombination, double weight, Model a, Model oldA)
        {
        // A
        currentModels[0] = ((Model)(a.clone())).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT);
        currentModels[1] = ((Model)(a.clone())).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT);
        currentModels[2] = ((Model)(a.clone())).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT);
        currentModels[3] = ((Model)(a.clone())).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT);
        currentModels[4] = ((Model)(a.clone())).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT);
        currentModels[5] = ((Model)(a.clone())).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT);
        currentModels[6] = ((Model)(a.clone())).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT);
        currentModels[7] = ((Model)(a.clone())).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT);
        currentModels[8] = ((Model)(a.clone())).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT);
        currentModels[9] = ((Model)(a.clone())).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT);
        currentModels[10] = ((Model)(a.clone())).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT);
        currentModels[11] = ((Model)(a.clone())).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT);
        currentModels[12] = ((Model)(a.clone())).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT);
        
        // A - Z
        currentModels[13] = ((Model)(a.clone())).opposite(random, oldA, keys, recombination, false).mutate(random, keys, weight * MUTATION_WEIGHT);
        currentModels[14] = ((Model)(a.clone())).opposite(random, oldA, keys, recombination, false).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT);
        currentModels[15] = ((Model)(a.clone())).opposite(random, oldA, keys, 2.0 * recombination, false).mutate(random, keys, weight * MUTATION_WEIGHT).mutate(random, keys, weight * MUTATION_WEIGHT);
        shuffle(random, currentModels, NUM_MODELS - 1);
        }
                
        
    public void climb(boolean determineBest)
        {
        Random random = synth.random;
        String[] keys = synth.getMutationKeys();
        double recombination = blank.getModel().get("recombinationrate", 0) / 100.0; //recombinationRate.getValue() / 100.0;
        double weight = blank.getModel().get("mutationrate", 0) / 100.0; //mutationRate.getValue() / 100.0;
                
        if (determineBest)
            {
            for(int j = 0; j < 3; j++)
                bestModels[j] = null;
                
            // load the best models
            for(int i = 0; i < 16; i++)
                {
                for(int j = 0; j < 3; j++)
                    {
                    if (ratings[i][j].isSelected())
                        bestModels[j] = currentModels[i];
                    }
                }
            }
        
        if (bestModels[0] == null)
            {
            bestModels[0] = bestModels[1];
            bestModels[1] = bestModels[2];
            bestModels[2] = null;
            }
        if (bestModels[0] == null)
            {
            bestModels[0] = bestModels[1];
            bestModels[1] = bestModels[2];
            bestModels[2] = null;
            }
        if (bestModels[1] == null)
            {
            bestModels[1] = bestModels[2];
            bestModels[2] = null;
            }
        
        boolean zeroModels = false;     
        if (bestModels[0] == null)
            {
            again();
            zeroModels = true;
            }
        else if (bestModels[1] == null)
            {
            produce(random, keys, recombination, weight, bestModels[0], (Model)(oldA.get(0)));
            }
        else if (bestModels[2] == null)
            {
            produce(random, keys, recombination, weight, bestModels[0], bestModels[1], (Model)(oldA.get(0)));
            }
        else
            {
            produce(random, keys, recombination, weight, bestModels[0], bestModels[1], bestModels[2], (Model)(oldA.get(0)));
            }
        
        if (!zeroModels)
            {
            oldA.add(bestModels[0]);
            oldB.add(bestModels[1]);
            oldC.add(bestModels[2]);
            iterations.setName("Iteration " + oldA.size());
            repaint();
        
            ratings[NUM_MODELS][0].setSelected(true);
            ratings[NUM_MODELS][1].setSelected(true);
            ratings[NUM_MODELS][2].setSelected(true);
            }
        }
    }
        
        
