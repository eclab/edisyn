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
    public static final int NUM_MODELS = 16;

    ArrayList oldA = new ArrayList();
    ArrayList oldB = new ArrayList();
    ArrayList oldC = new ArrayList();
    Model[] currentModels = new Model[NUM_MODELS];
    Model[] bestModels = new Model[3];
    JRadioButton[][] ratings = new JRadioButton[16][3];
    PushButton[] plays = new PushButton[16];
    public static final int INITIAL_MUTATION_RATE = 20;
    public static final int INITIAL_RECOMBINATION_RATE = 75;
    JSlider mutationRate = new JSlider(JSlider.HORIZONTAL, 0, 100, INITIAL_MUTATION_RATE);
    JSlider recombinationRate = new JSlider(JSlider.HORIZONTAL, 0, 100, INITIAL_RECOMBINATION_RATE);
    Category iterations;
        
    Synth synth;
    int currentPlay = 0;
        
    public HillClimb(final Synth synth)
        {
        this.synth = synth;
        
        VBox top = new VBox();
        add(top, BorderLayout.CENTER);

        // add globals

        Category panel = new Category(null, "Iteration 1", Style.COLOR_A);
        iterations = panel;
                
        PushButton backup = new PushButton("Backup")
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
                
        JLabel recombinationRateLabel = new JLabel("Recombination Rate", SwingConstants.RIGHT);
        recombinationRateLabel.setFont(Style.SMALL_FONT);
        recombinationRateLabel.setForeground(Style.TEXT_COLOR);
        recombinationRateLabel.setOpaque(false);
        final JLabel recombinationRateVal = new JLabel("" + INITIAL_RECOMBINATION_RATE + "%");
        recombinationRateVal.setFont(Style.SMALL_FONT);
        recombinationRateVal.setForeground(Style.TEXT_COLOR);
        recombinationRateVal.setOpaque(false);
        recombinationRate.setOpaque(false);
        recombinationRate.addChangeListener(new ChangeListener()
            {
            public void stateChanged(ChangeEvent e) { recombinationRateVal.setText("" + recombinationRate.getValue() + "%"); }
            });
        HBox ratebox = new HBox();
        ratebox.add(climb);
        ratebox.add(retry);
        ratebox.add(Strut.makeHorizontalStrut(30));
        ratebox.add(recombinationRateLabel);
        ratebox.add(recombinationRate);
        ratebox.add(recombinationRateVal);
        vbox.add(ratebox);


        JLabel mutationRateLabel = new JLabel("Mutation Rate", SwingConstants.RIGHT);
        mutationRateLabel.setPreferredSize(recombinationRateLabel.getPreferredSize());
        mutationRateLabel.setFont(Style.SMALL_FONT);
        mutationRateLabel.setForeground(Style.TEXT_COLOR);
        mutationRateLabel.setOpaque(false);
        final JLabel mutationRateVal = new JLabel("" + INITIAL_MUTATION_RATE + "%");
        mutationRateVal.setFont(Style.SMALL_FONT);
        mutationRateVal.setForeground(Style.TEXT_COLOR);
        mutationRateVal.setOpaque(false);
        mutationRate.setOpaque(false);
        mutationRate.addChangeListener(new ChangeListener()
            {
            public void stateChanged(ChangeEvent e) { mutationRateVal.setText("" + mutationRate.getValue() + "%"); }
            });
        ratebox = new HBox();
        ratebox.add(backup);
        ratebox.add(reset);
        ratebox.add(Strut.makeHorizontalStrut(30));
        ratebox.add(mutationRateLabel);
        ratebox.add(mutationRate);
        ratebox.add(mutationRateVal);
        vbox.add(ratebox);
                                
        panel.add(vbox, BorderLayout.WEST);
        top.add(panel);
        
        
        // Add Candidates

        panel =  new Category(null, "Candidates", Style.COLOR_B);

        HBox hbox = new HBox();
                
        ButtonGroup one = new ButtonGroup();
        ButtonGroup two = new ButtonGroup();
        ButtonGroup three = new ButtonGroup();
                
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

            Box b = new Box(BoxLayout.X_AXIS);
            b.setBackground(Style.BACKGROUND_COLOR);
            b.add(Box.createGlue());
            b.add(ratings[i][0] = new JRadioButton("1"));
            ratings[i][0].setForeground(Style.TEXT_COLOR);
            ratings[i][0].setFont(Style.SMALL_FONT);
            ratings[i][0].putClientProperty("JComponent.sizeVariant", "small");
            b.add(Box.createGlue());
            vbox.add(b);
                        
            b = new Box(BoxLayout.X_AXIS);
            b.setBackground(Style.BACKGROUND_COLOR);
            b.add(Box.createGlue());
            b.add(ratings[i][1] = new JRadioButton("2"));
            ratings[i][1].setForeground(Style.TEXT_COLOR);
            ratings[i][1].setFont(Style.SMALL_FONT);
            ratings[i][1].putClientProperty("JComponent.sizeVariant", "small");
            b.add(Box.createGlue());
            vbox.add(b);
                        
            b = new Box(BoxLayout.X_AXIS);
            b.setBackground(Style.BACKGROUND_COLOR);
            b.add(Box.createGlue());
            b.add(ratings[i][2] = new JRadioButton("3"));
            ratings[i][2].setForeground(Style.TEXT_COLOR);
            ratings[i][2].setFont(Style.SMALL_FONT);
            ratings[i][2].putClientProperty("JComponent.sizeVariant", "small");
            b.add(Box.createGlue());
            vbox.add(b);

            one.add(ratings[i][0]);
            two.add(ratings[i][1]);
            three.add(ratings[i][2]);
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
                
        //              initialize(synth.getModel(), true);
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
                for(int i = 0; i < 16; i++)                             
                    plays[i].getButton().setForeground(new JButton().getForeground());
                currentPlay++;
                if (currentPlay >= 16)
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
        double weight = mutationRate.getValue() / 100.0;
                
        for(int i = 0; i < 4; i++)
            {
            currentModels[i] = ((Model)(newSeed.clone())).mutate(random, keys, weight / 2.0);
            }

        for(int i = 0; i < 4; i++)
            {
            currentModels[i + 4] = ((Model)(currentModels[i].clone())).mutate(random, keys, weight / 2.0);
            }

        for(int i = 0; i < 4; i++)
            {
            currentModels[i + 8] = ((Model)(currentModels[i + 4].clone())).mutate(random, keys, weight / 2.0);
            }

        for(int i = 0; i < 4; i++)
            {
            currentModels[i + 12] = ((Model)(currentModels[i + 8].clone())).mutate(random, keys, weight / 2.0);
            }

        oldA.add(newSeed);
        oldB.add(newSeed);
        oldC.add(newSeed);
        iterations.setName("Iteration " + oldA.size());
        repaint();

        ratings[0][0].setSelected(true);
        ratings[1][1].setSelected(true);
        ratings[2][2].setSelected(true);
        }

        
    public void climb(boolean determineBest)
        {
        Random random = synth.random;
        String[] keys = synth.getMutationKeys();
        double recombination = recombinationRate.getValue() / 100.0;
        double weight = mutationRate.getValue() / 100.0;
                
        if (determineBest)
            {
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
                        
                
        // Standard Mutations and Recombinations
        
        /**
        	The original sound is Z.
        	The user chooses A, B, and C.
        	The mutations are:
        	
        	Noisy A + B
        	Noisy A + C
        	Noisy B + C
        	Noisy A + (B + C)
        	Noisy Beyond A
        	Noisy Beyond B
        	Noisy Beyond C
			Noisy Beyond Beyond A
        	Noisy A
        	Noisy Noisy A
        	Noisy Noisy Noisy A
        	Noisy B
        	Noisy Beyond B from A
        	Noisy C
        	Noisy Beyond C from A
        	Current Patch
        	
           The Mutations are
           A
           A + B
           A + C
           A + (B + C)
           B + C
           Beyond A from Z
           Even Further Beyond A from Z
           Noisy(A)
           Noisy(A + B)
           Noisy(A + C)
           Noisy(A + (B + C))
           Noisy(Noisy(A))
           Noisy(Noisy(A + B))
           Noisy(Noisy(A + C))
           Noisy(Noisy(A + (B + C)))
           Current Patch
        */
        
        // Noisy A + B
        currentModels[0] = ((Model)(bestModels[0].clone())).recombine(random, bestModels[1], keys, recombination).mutate(random, keys, weight / 2.0);
        // Noisy A + C
        currentModels[1] = ((Model)(bestModels[0].clone())).recombine(random, bestModels[2], keys, recombination).mutate(random, keys, weight / 2.0);
        // Noisy B + C
        currentModels[2] = ((Model)(bestModels[1].clone())).recombine(random, bestModels[2], keys, recombination).mutate(random, keys, weight / 2.0);
        // Noisy A + (B + C)
        currentModels[3] = ((Model)(bestModels[0].clone())).recombine(random, ((Model)(bestModels[1].clone())).recombine(random, bestModels[2], keys, recombination), keys, recombination).mutate(random, keys, weight / 2.0);
        // Noisy Beyond A from Z
        currentModels[4] = ((Model)(bestModels[0].clone())).opposite(random, (Model)(oldA.get(oldA.size() - 1)), keys, recombination, false).mutate(random, keys, weight / 2.0);
        // Noisy Beyond B from Z
        currentModels[5] = ((Model)(bestModels[1].clone())).opposite(random, (Model)(oldA.get(oldA.size() - 1)), keys, recombination, false).mutate(random, keys, weight / 2.0);
        // Noisy Beyond C from Z
        currentModels[6] = ((Model)(bestModels[1].clone())).opposite(random, (Model)(oldA.get(oldA.size() - 1)), keys, recombination, false).mutate(random, keys, weight / 2.0);
        // Noisy Even further Beyond A from Z
        currentModels[7] = ((Model)(bestModels[0].clone())).opposite(random, (Model)(oldA.get(oldA.size() - 1)), keys, 2.0 * recombination, false).mutate(random, keys, weight / 2.0);
        // Noisy A
        currentModels[8] = ((Model)(bestModels[0].clone())).mutate(random, keys, weight / 2.0);
        // Noisy Noisy A
        currentModels[9] = ((Model)(bestModels[0].clone())).mutate(random, keys, weight / 2.0).mutate(random, keys, weight / 2.0);
        // Noisy Noisy Noisy A
        currentModels[10] = ((Model)(bestModels[0].clone())).mutate(random, keys, weight / 2.0).mutate(random, keys, weight / 2.0).mutate(random, keys, weight / 2.0);
        // Noisy B
        currentModels[11] = ((Model)(bestModels[1].clone())).mutate(random, keys, weight / 2.0).mutate(random, keys, weight / 2.0);
        // Noisy Noisy B
        currentModels[12] = ((Model)(bestModels[1].clone())).mutate(random, keys, weight / 2.0).mutate(random, keys, weight / 2.0).mutate(random, keys, weight / 2.0);
        // Noisy C
        currentModels[13] = ((Model)(bestModels[2].clone())).mutate(random, keys, weight / 2.0).mutate(random, keys, weight / 2.0);
        // Noisy Noisy C
        currentModels[14] = ((Model)(bestModels[2].clone())).mutate(random, keys, weight / 2.0).mutate(random, keys, weight / 2.0).mutate(random, keys, weight / 2.0);
        // Current patch
        currentModels[15] = ((Model)(synth.getModel().clone()));
        
        /*
        // A
        currentModels[0] = ((Model)(bestModels[0].clone()));
        // A + B
        currentModels[1] = ((Model)(bestModels[0].clone())).recombine(random, bestModels[1], keys, recombination);
        // A + C
        currentModels[2] = ((Model)(bestModels[0].clone())).recombine(random, bestModels[2], keys, recombination);
        // A + (B + C)
        currentModels[3] = ((Model)(bestModels[0].clone())).recombine(random, ((Model)(bestModels[1].clone())).recombine(random, bestModels[2], keys, recombination), keys, recombination);

        // Noisy versions of A, A + B, A + C, A + (B + C)
        for(int i = 0; i < 4; i ++)
            {
            currentModels[i + 4] = ((Model)(currentModels[i].clone())).mutate(random, keys, weight / 2.0);
            }
                
        // Really noisy versions of A, A + B, A + C, A + (B + C)
        for(int i = 0; i < 4; i ++)
            {
            currentModels[i + 8] = ((Model)(currentModels[i + 4].clone())).mutate(random, keys, weight / 2.0);
            }

        // B + C
        currentModels[12] = ((Model)(bestModels[1].clone())).recombine(random, bestModels[2], keys, recombination);
        // Beyond A from Z
        currentModels[13] = ((Model)(bestModels[0].clone())).opposite(random, (Model)(oldA.get(oldA.size() - 1)), keys, recombination, false);
        // Even further Beyond A from Z
        currentModels[14] = ((Model)(bestModels[0].clone())).opposite(random, (Model)(oldA.get(oldA.size() - 1)), keys, 2.0 * recombination, false);
        // Current patch
        currentModels[15] = ((Model)(synth.getModel().clone()));
        */


        oldA.add(bestModels[0]);
        oldB.add(bestModels[1]);
        oldC.add(bestModels[2]);
        iterations.setName("Iteration " + oldA.size());
        repaint();

        ratings[0][0].setSelected(true);
        ratings[1][1].setSelected(true);
        ratings[2][2].setSelected(true);
        }
    }
        
        
