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
	class State
		{
		Model[] parents;
		Model[] children;
		}
	public ArrayList stack = new ArrayList();
	
    public static final int NUM_CANDIDATES = 16;
    public static final int NUM_MODELS = NUM_CANDIDATES + 1;
    
    // When the nudge buttons are being REQUESTED to play, then currentNudgeButton
    // is set to the nudge button values.  When a nudge button is PRESENTLY playing,
    // then currentNudgeButton is set to this value + NUDGE_PLAYING_DELTA.  When 
    // no nudge button is playing, then currentNudgeButton is set to -1.  This allows
    // us to eventualliy turn off the nudge button when it's time to play something else
    // but still properly highlight it when it's playing.
    public static final int NUDGE_PLAYING_DELTA = 100;

	// models currently being played and displayed
    Model[] currentModels = new Model[NUM_MODELS];

    JRadioButton[][] ratings = new JRadioButton[NUM_MODELS + 1][3];
    ButtonGroup nudgeGroup = new ButtonGroup();
    JRadioButton[] nudge = new JRadioButton[5];
    PushButton[] plays = new PushButton[NUM_MODELS];
    public static final int INITIAL_MUTATION_RATE = 5;
    public static final int INITIAL_RECOMBINATION_RATE = 75;
    Blank blank;
    Category iterations;
    int currentNudgeButton = -1;
    int currentPlay = 0;
    HBox nudgeBox;
    
    
    public State popStack()	
    	{
    	if (stack.size() == 0) 
    		return null;
    	else
    		return (State)(stack.remove(stack.size() - 1));
    	}
    	
    public void pushStack(Model[] parents, Model[] children)
    	{
    	State state = new State();
    	state.parents = new Model[parents.length];
    	for(int i = 0; i < parents.length; i++)
    		{
    		state.parents[i] = copy(parents[i]);
    		}
    	
    	state.children = new Model[children.length];
    	for(int i = 0; i < children.length; i++)
    		{
    		state.children[i] = copy(children[i]);
    		}
    		
    	stack.add(state);
    	}
    	
    public State topStack()
    	{
    	if (stack.size() == 0) 
    		return null;
    	else
    		return (State)(stack.get(stack.size() - 1));
    	}
    	
    public boolean stackEmpty()
    	{
    	return (stack.size() == 0);
    	}
    	
    public boolean stackInitial()
    	{
    	return (stack.size() == 1);
    	}
    	
    	
        
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
                if (synth.showSimpleConfirm("Reset", "Are you sure you want to reset the Hill-Climber?"))
                	{
	                initialize((Model)(synth.getModel().clone()), true);
	                resetCurrentPlay();
	                }
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
           
           
     	nudgeBox = new HBox();

        PushButton nudgeButton = new PushButton("Nudge To:")
            {
            public void perform()
                {
                int nudged = 0;
                for(int i = 1; i < 4; i++)
                	{ if (nudge[i].isSelected()) { nudged = i; break; } }
                nudge(nudged);
                resetCurrentPlay();
                }
            };
        nudgeButton.getButton().setPreferredSize(backup.getButton().getPreferredSize());
        nudgeBox.add(nudgeButton);

        for(int i = 0; i < 5; i++)
        	{
        	if (i == 4)
        		{
        		nudge[i] = new JRadioButton("Current Patch");
        		nudge[i].setSelected(true);
        		}
        	else
        		nudge[i] = new JRadioButton("" + (i + 1));
            nudge[i].setForeground(Style.TEXT_COLOR());
            nudge[i].setFont(Style.SMALL_FONT());
            nudge[i].putClientProperty("JComponent.sizeVariant", "small");
            nudgeGroup.add(nudge[i]);
			}
		
		for(int i = 0; i < 5; i++)
			{
			final int _i = i;
	        nudge[i].addActionListener(new ActionListener()
				{
				public void actionPerformed(ActionEvent e)
					{
					if (synth.isSendingTestNotes())
						{
						currentNudgeButton = _i;
						}
					else
						{
						synth.sendAllParameters();
						synth.doSendTestNote(false);
						}
					}
				});

			 Border border = new LineBorder(Style.BACKGROUND_COLOR(), 1)
				{
				public void paintBorder(final Component c, final Graphics g, final int x, final int y, final int width, final int height) 
					{
					if (currentNudgeButton == _i || currentNudgeButton == _i + NUDGE_PLAYING_DELTA)
						super.lineColor = Style.DYNAMIC_COLOR();
					else
						super.lineColor = Style.BACKGROUND_COLOR();
					super.paintBorder(c, g, x, y, width, height);
					}
				};

			JPanel pan = new JPanel();
			pan.setLayout(new BorderLayout());
			pan.add(nudge[i], BorderLayout.CENTER);
			pan.setBorder(border);
        	pan.setBackground(Style.BACKGROUND_COLOR());
            nudgeBox.add(pan);
			}
		                
        VBox vbox = new VBox();
        
        
        VBox buttonVBox = new VBox();
        HBox buttonBox = new HBox();
        buttonBox.add(climb);
        buttonBox.add(retry);
       // buttonVBox.add(buttonBox);
        //buttonBox = new HBox();
        buttonBox.add(backup);
        buttonBox.add(reset);
        buttonVBox.add(buttonBox);
    	buttonVBox.add(nudgeBox);

        PushButton pushToButton = new PushButton("Go To:")
            {
            public void perform()
                {
                int nudged = 0;
                for(int i = 1; i < 4; i++)
                	{ if (nudge[i].isSelected()) { nudged = i; break; } }
				Model model = (nudged == 4 ? synth.getModel() : synth.getNudge(nudged));
                initialize((Model)(model.clone()), false);
                resetCurrentPlay();
                }
            };
        pushToButton.getButton().setPreferredSize(backup.getButton().getPreferredSize());

        HBox gotoHBox = new HBox();
        gotoHBox.add(pushToButton);
        gotoHBox.addLast(Stretch.makeHorizontalStretch());
        buttonVBox.add(gotoHBox);

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
                    currentPlay = NUM_MODELS - 1;  // so it'll be NUM_MODELS when we update, and trigger playing it specially
                    }
                else
                    {
                    synth.sendAllParameters();
                    synth.doSendTestNote(false);
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
        for(int i = 0; i < NUM_CANDIDATES; i++)
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
            
            
            JMenuItem[] doItems = new JMenuItem[14];
            doItems[0] = new JMenuItem("Keep Patch");
            doItems[0].addActionListener(new ActionListener()
            	{
            	public void actionPerformed(ActionEvent e)
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

            doItems[1] = new JMenuItem("Edit Patch");
            doItems[1].addActionListener(new ActionListener()
            	{
            	public void actionPerformed(ActionEvent e)
            		{
            		Synth newSynth = synth.doDuplicateSynth();
            		// Copy the parameters forward into the synth, then
            		// link the synth's model back to currentModels[_i].
            		// We do this because the new synth's widgets are registered
            		// with its model, so we can't just replace the model.
            		// But we can certainly replace currentModels[_i]!
                    newSynth.setSendMIDI(false);
                    currentModels[_i].copyValuesTo(newSynth.getModel());
                    newSynth.setSendMIDI(true);
            		currentModels[_i] = newSynth.getModel();
            		newSynth.sendAllParameters();
            		}
            	});

            doItems[2] = new JMenuItem("Save to File");
            doItems[2].addActionListener(new ActionListener()
            	{
            	public void actionPerformed(ActionEvent e)
            		{
            		Model backup = synth.model;
	                synth.model = currentModels[_i];
	                synth.doSaveAs();
	                synth.model = backup;
	                synth.updateTitle();
	                }
            	});

            doItems[3] = new JMenuItem("Load from File");
            doItems[3].addActionListener(new ActionListener()
            	{
            	public void actionPerformed(ActionEvent e)
            		{
            		Model backup = synth.model;
	                synth.model = currentModels[_i];
	                synth.doOpen(false);
	                currentModels[_i] = synth.model;
	                synth.model = backup;
	                synth.updateTitle();
            		}
            	});
            
            doItems[4] = null;
            
            doItems[5] = new JMenuItem("Set 1");
            doItems[5].addActionListener(new ActionListener()
            	{
            	public void actionPerformed(ActionEvent e)
            		{
            		synth.doSetNudge(0, currentModels[_i], "Hill-Climb " + _i);
            		}
            	});

            doItems[6] = new JMenuItem("Set 2");
            doItems[6].addActionListener(new ActionListener()
            	{
            	public void actionPerformed(ActionEvent e)
            		{
            		synth.doSetNudge(1, currentModels[_i], "Hill-Climb " + _i);
            		}
            	});

            doItems[7] = new JMenuItem("Set 3");
            doItems[7].addActionListener(new ActionListener()
            	{
            	public void actionPerformed(ActionEvent e)
            		{
            		synth.doSetNudge(2, currentModels[_i], "Hill-Climb " + _i);
            		}
            	});

            doItems[8] = new JMenuItem("Set 4");
            doItems[8].addActionListener(new ActionListener()
            	{
            	public void actionPerformed(ActionEvent e)
            		{
            		synth.doSetNudge(3, currentModels[_i], "Hill-Climb " + _i);
            		}
            	});
            	
            doItems[9] = null;
            
            doItems[10] = new JMenuItem("Load 1");
            doItems[10].addActionListener(new ActionListener()
            	{
            	public void actionPerformed(ActionEvent e)
            		{
            		currentModels[_i] = (Model)(synth.getNudge(0).clone());
            		}
            	});

            doItems[11] = new JMenuItem("Load 2");
            doItems[11].addActionListener(new ActionListener()
            	{
            	public void actionPerformed(ActionEvent e)
            		{
            		currentModels[_i] = (Model)(synth.getNudge(1).clone());
            		}
            	});

            doItems[12] = new JMenuItem("Load 3");
            doItems[12].addActionListener(new ActionListener()
            	{
            	public void actionPerformed(ActionEvent e)
            		{
            		currentModels[_i] = (Model)(synth.getNudge(2).clone());
            		}
            	});

            doItems[13] = new JMenuItem("Load 4");
            doItems[13].addActionListener(new ActionListener()
            	{
            	public void actionPerformed(ActionEvent e)
            		{
            		currentModels[_i] = (Model)(synth.getNudge(3).clone());
            		}
            	});
            	

            vbox.add(new PushButton("Options", doItems));
            hbox.add(vbox);
            
            if (i % 8 == 7)
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
                for(int i = 0; i < NUM_MODELS; i++)                             
                    plays[i].getButton().setForeground(new JButton().getForeground());
                if (currentNudgeButton >= 0 && currentNudgeButton < NUDGE_PLAYING_DELTA)
                	{
					backup = synth.model;
					if (currentNudgeButton != 4)
						synth.model = synth.getNudge(currentNudgeButton);
    				nudge[currentNudgeButton].repaint();
					synth.sendAllParameters();
					currentNudgeButton += NUDGE_PLAYING_DELTA;
                	}
                else
                	{
					currentPlay++;
					currentNudgeButton = -1;
					if (currentPlay == NUM_MODELS || currentPlay == NUM_MODELS + 5)  // user asked to play the current patch
						{
						plays[NUM_MODELS - 1].getButton().setForeground(Color.RED);
	
						backup = synth.model;
						synth.sendAllParameters();
						}
					else
						{
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
            }
        }
                
    public void postUpdateSound()
        {
    	repaint();
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
                        
    Model copy(Model model)
    	{
    	if (model != null)
    		return model.copy();
    	else return null;
    	}
    	
    public void again()
        {
        if (stackEmpty())
        	{
        	// uh oh...
        	System.err.println("EMPTY STACK!");
        	return;
        	}
        else if (stackInitial())
        	{
        	initialize(topStack().parents[0], true);
        	}
        else
        	{
        	popStack();
        	climb(false);
        	}

        ratings[NUM_MODELS][0].setSelected(true);
        ratings[NUM_MODELS][1].setSelected(true);
        ratings[NUM_MODELS][2].setSelected(true);
        }
        
    public void pop()
        {
        if (stackEmpty())
        	{
        	// uh oh...
        	System.err.println("EMPTY STACK!");
        	return;
        	}
        else if (stackInitial())
        	{
        	// do nothing
        	}
        else
        	{
        	State state = popStack();
        	System.arraycopy(state.children, 0, currentModels, 0, state.children.length);
        	}
                
        iterations.setName("Iteration " + stack.size());
        repaint();

        ratings[NUM_MODELS][0].setSelected(true);
        ratings[NUM_MODELS][1].setSelected(true);
        ratings[NUM_MODELS][2].setSelected(true);
        }
    
    public void initialize(Model seed, boolean clear)
        {
        // we need a model with NO callbacks
        Model newSeed = seed.copy();
                
        if (clear)
            {
            stack.clear();
            }
                
        Random random = synth.random;
        String[] keys = synth.getMutationKeys();
        double weight = blank.getModel().get("mutationrate", 0) / 100.0;
        
        double mutationWeight = weight * MUTATION_WEIGHT;
        
        for(int i = 0; i < 4; i++)
            {
            currentModels[i] = newSeed.copy().mutate(random, keys, mutationWeight);
            }

        for(int j = 4; j < NUM_CANDIDATES; j+= 4)
        	{
			for(int i = 0; i < 4; i++)
				{
				currentModels[j + i] = currentModels[j + i - 4].copy().mutate(random, keys, mutationWeight);
				}
            }

		pushStack(new Model[] { newSeed, newSeed, newSeed}, currentModels);
        iterations.setName("Iteration " + stack.size());
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

    public static double MUTATION_WEIGHT = 1.0;
    
    public void produce(Random random, String[] keys, double recombination, double weight, Model a, Model b, Model c, Model oldA)
    	{
    	int numStages = NUM_CANDIDATES / 16;
    	
    	for(int i = 0; i < numStages; i++)
    		{
    		produce(random, keys, recombination, weight, a, b, c, oldA, i);
    		}
    	}
        
    public void produce(Random random, String[] keys, double recombination, double weight, Model a, Model b, Model c, Model oldA, int stage)
        {
        double mutationWeight = (stage + 1) * MUTATION_WEIGHT * weight;
        
        // A + B
        currentModels[stage + 0] = a.copy().recombine(random, b, keys, recombination).mutate(random, keys, mutationWeight);
        // A + C
        currentModels[stage + 1] = a.copy().recombine(random, c, keys, recombination).mutate(random, keys, mutationWeight);
        // B + C
        currentModels[stage + 2] = b.copy().recombine(random, c, keys, recombination).mutate(random, keys, mutationWeight);
        // A + (B + C)
        currentModels[stage + 3] = a.copy().recombine(random, b.copy().recombine(random, c, keys, recombination), keys, recombination).mutate(random, keys, mutationWeight);
        // A - B
        currentModels[stage + 4] = a.copy().opposite(random, b, keys, recombination, false).mutate(random, keys, mutationWeight);
        // B - A
        currentModels[stage + 5] = b.copy().opposite(random, a, keys, recombination, false).mutate(random, keys, mutationWeight);
        // A - C
        currentModels[stage + 6] = a.copy().opposite(random, c, keys, recombination, false).mutate(random, keys, mutationWeight);
        // C - A
        currentModels[stage + 7] = c.copy().opposite(random, a, keys, recombination, false).mutate(random, keys, mutationWeight);
        
        if ((stage + 8) < currentModels.length)
        {
        // B - C
        currentModels[stage + 8] = b.copy().opposite(random, c, keys, recombination, false).mutate(random, keys, mutationWeight);
        // C - B
        currentModels[stage + 9] = c.copy().opposite(random, b, keys, recombination, false).mutate(random, keys, mutationWeight);
        // A - Z
        currentModels[stage + 10] = a.copy().opposite(random, oldA, keys, recombination, false).mutate(random, keys, mutationWeight);
        // B - Z
        currentModels[stage + 11] = b.copy().opposite(random, oldA, keys, recombination, false).mutate(random, keys, mutationWeight);
        // C - Z
        currentModels[stage + 12] = c.copy().opposite(random, oldA, keys, recombination, false).mutate(random, keys, mutationWeight);
        // A
        currentModels[stage + 13] = a.copy().mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
        // B
        currentModels[stage + 14] = b.copy().mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
        // C
        currentModels[stage + 15] = c.copy().mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
        }
        
        shuffle(random, currentModels, NUM_MODELS - 1);
        }
        
    public void produce(Random random, String[] keys, double recombination, double weight, Model a, Model b, Model oldA)
    	{
    	int numStages = NUM_CANDIDATES / 16;
    	
    	for(int i = 0; i < numStages; i++)
    		{
    		produce(random, keys, recombination, weight, a, b, oldA, i);
    		}
    	}
        
    public void produce(Random random, String[] keys, double recombination, double weight, Model a, Model b, Model oldA, int stage)
        {
        double mutationWeight = (stage + 1) * MUTATION_WEIGHT * weight;
        
        // A + B
        currentModels[stage + 0] = a.copy().recombine(random, b, keys, recombination).mutate(random, keys, mutationWeight);
        currentModels[stage + 1] = a.copy().recombine(random, b, keys, recombination).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
        currentModels[stage + 2] = a.copy().recombine(random, b, keys, recombination).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
        
        // A - B
        currentModels[stage + 3] = a.copy().opposite(random, b, keys, recombination, false).mutate(random, keys, mutationWeight);
        currentModels[stage + 4] = a.copy().opposite(random, b, keys, recombination, false).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
        
        // B - A
        currentModels[stage + 5] = b.copy().opposite(random, a, keys, recombination, false).mutate(random, keys, mutationWeight);
        currentModels[stage + 6] = b.copy().opposite(random, a, keys, recombination, false).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
        
        // A - Z
        currentModels[stage + 7] = a.copy().opposite(random, oldA, keys, recombination, false).mutate(random, keys, mutationWeight);

        if ((stage + 8) < currentModels.length)
        {
        currentModels[stage + 8] = a.copy().opposite(random, oldA, keys, recombination, false).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
        
        // B - Z
        currentModels[stage + 9] = b.copy().opposite(random, oldA, keys, recombination, false).mutate(random, keys, mutationWeight);
        currentModels[stage + 10] = b.copy().opposite(random, oldA, keys, recombination, false).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);

        // (A - Z) + (B - Z)
        currentModels[stage + 11] = a.copy().opposite(random, oldA, keys, recombination, false).recombine(random, 
            b.copy().opposite(random, oldA, keys, recombination, false), keys, recombination).mutate(random, keys, mutationWeight);

        // A
        currentModels[stage + 12] = a.copy().mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
        currentModels[stage + 13] = a.copy().mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
        
        // B
        currentModels[stage + 14] = b.copy().mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
        currentModels[stage + 15] = b.copy().mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
        }
        
        shuffle(random, currentModels, NUM_MODELS - 1);
        }
                
    public void produce(Random random, String[] keys, double recombination, double weight, Model a, Model oldA)
    	{
    	int numStages = NUM_CANDIDATES / 16;
    	
    	for(int i = 0; i < numStages; i++)
    		{
    		produce(random, keys, recombination, weight, a, oldA, i);
    		}
    	}
        
    public void produce(Random random, String[] keys, double recombination, double weight, Model a, Model oldA, int stage)
        {
        double mutationWeight = (stage + 1) * MUTATION_WEIGHT * weight;
        
        // A
        currentModels[stage + 0] = a.copy().mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
        currentModels[stage + 1] = a.copy().mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
        currentModels[stage + 2] = a.copy().mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
        currentModels[stage + 3] = a.copy().mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
        currentModels[stage + 4] = a.copy().mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
        currentModels[stage + 5] = a.copy().mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
        currentModels[stage + 6] = a.copy().mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
        currentModels[stage + 7] = a.copy().mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
        
                if ((stage + 8) < currentModels.length)
        {
		currentModels[stage + 8] = a.copy().mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
        currentModels[stage + 9] = a.copy().mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
        currentModels[stage + 10] = a.copy().mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
        currentModels[stage + 11] = a.copy().mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
        currentModels[stage + 12] = a.copy().mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
        
        // A - Z
        currentModels[stage + 13] = a.copy().opposite(random, oldA, keys, recombination, false).mutate(random, keys, mutationWeight);
        currentModels[stage + 14] = a.copy().opposite(random, oldA, keys, recombination, false).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
        currentModels[stage + 15] = a.copy().opposite(random, oldA, keys, 2.0 * recombination, false).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
        }
        
        shuffle(random, currentModels, NUM_MODELS - 1);
        }
             
	public void nudge(int towards)
		{
        Random random = synth.random;
        String[] keys = synth.getMutationKeys();
		for(int i = 0; i < NUM_CANDIDATES; i++)
			{
			currentModels[i] =  currentModels[i].copy().recombine(
							random, 
							towards == 4 ? synth.getModel() : synth.getNudge(towards), 
							synth.getMutationKeys(),
									synth.nudgeRecombinationWeight);
            if (synth.nudgeMutationWeight > 0.0) currentModels[i].mutate(random, synth.getMutationKeys(), 
            						synth.nudgeMutationWeight);
            }
		}   
        
    public void climb(boolean determineBest)
        {
        Random random = synth.random;
        String[] keys = synth.getMutationKeys();
        double recombination = blank.getModel().get("recombinationrate", 0) / 100.0;
        double weight = blank.getModel().get("mutationrate", 0) / 100.0;
        
        Model[] bestModels = new Model[3];
        
        currentModels[NUM_MODELS - 1] = synth.getModel();
        
        if (determineBest)
            {
            for(int j = 0; j < 3; j++)
                bestModels[j] = null;
                
            // load the best models
            for(int i = 0; i < NUM_CANDIDATES; i++)
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
        Model oldA = topStack().parents[0];
        
        if (bestModels[0] == null)
            {
            again();
            zeroModels = true;
            }
        else if (bestModels[1] == null)
            {
            pushStack(bestModels, currentModels);
            produce(random, keys, recombination, weight, bestModels[0], oldA);
            }
        else if (bestModels[2] == null)
            {
            pushStack(bestModels, currentModels);
            produce(random, keys, recombination, weight, bestModels[0], bestModels[1], oldA);
            }
        else
            {
            pushStack(bestModels, currentModels);
            produce(random, keys, recombination, weight, bestModels[0], bestModels[1], bestModels[2], oldA);
            }
        
        if (!zeroModels)
            {
            iterations.setName("Iteration " + stack.size());
            repaint();
        
            ratings[NUM_MODELS][0].setSelected(true);
            ratings[NUM_MODELS][1].setSelected(true);
            ratings[NUM_MODELS][2].setSelected(true);
            }
        }
    }
        
        
