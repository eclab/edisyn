/***
    Copyright 2020 by Sean Luke
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
import edisyn.util.*;

public class Morph extends SynthPanel
    {
    Joystick joystick;
    Blank blank;
    PushButton[] buttons;
    Model[] sources;
    double[] lastWeights = new double[] { 1.0, 0.0, 0.0, 0.0 };
    Model current;
    JPanel top;
    JPanel bottom;
    JPanel topCenter;
    JPanel bottomCenter;
    VBox margin;
    HBox outerMargin;
    JPanel outer;
 
    public static final int TIMER_DELAY = 2;		// too large?  too small?  3ms would be half-time if the sysex messages were all 10 bytes long
    public static final String EDISYN_MORPH_PREFERENCES_KEY = "EdisynMorph";
//    public static final double[] SEND_PROBABILITY = new double[] { 0, 0, 1, 0.5, 0.25, 0.125, 0.0625, 0.03125 };
    public static final String[] SEND_TO_SYNTH = new String[] { "When Playing Test Notes", "When Changing", "Trickle", "Deluge" };
    public static final String[] CATEGORICAL_STRATEGIES = new String[] { "Morph", "Use Closest", "Use Current Patch", "Use Top Left", "Use Top Right", "Use Bottom Left", "Use Bottom Right" };
    public static final String[] POSITIONS = new String[] { "Top Left", "Top Right", "Bottom Left", "Bottom Right" };
	
	public static final int SEND_TYPE_NOTE = 0;
	public static final int SEND_TYPE_CHANGING = 1;
	public static final int SEND_TYPE_TRICKLE = 2;
	public static final int SEND_TYPE_DELUGE = 3;

 	javax.swing.Timer timer;
 	int timerDelay = TIMER_DELAY;
 	int timerCount = 0;
    String[] shuffledKeys;
           
    public Morph(final Synth synth)
        {
        super(synth);

        blank = new Blank();

		// load preferences first 
 		int xcc = Synth.getLastXAsInt("XCC", EDISYN_MORPH_PREFERENCES_KEY, -1, true); 
		if (xcc < -1 || xcc > 127) xcc = -1;
		blank.getModel().set("xcc", xcc);
		int ycc = Synth.getLastXAsInt("YCC", EDISYN_MORPH_PREFERENCES_KEY, -1, true); 
		if (ycc < -1 || ycc > 127) ycc = -1;
		blank.getModel().set("ycc", ycc);
        
       	sources = new Model[4];
        current = new Model();

        setLayout(new BorderLayout());

        outer = new JPanel();
        outer.setLayout(new BorderLayout());
        outer.setBackground(getBackground());
        add(outer, BorderLayout.CENTER);
        margin = new VBox();
        outerMargin = new HBox();
        outerMargin.add(margin);
        outerMargin.add(Strut.makeHorizontalStrut(8));
        add(outerMargin, BorderLayout.WEST);
                
                
        margin.add(Strut.makeStrut(new PushButton("Throwaway")));
                
        String[] params = CATEGORICAL_STRATEGIES;
        Chooser nonmetric = new Chooser("Non-Metric Parameters", blank, "nonmetricparams", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                updateAgain();
                }
            };
        margin.add(nonmetric);


        params = SEND_TO_SYNTH;
        Chooser sendtosynth = new Chooser("Send to Synth", blank, "sendonchange", params)
        	{
        	public void update(String key, Model model)
        		{
        		super.update(key, model);
        		if (timer != null)
        			{
        			if (model.get("sendonchange", SEND_TYPE_NOTE) >= SEND_TYPE_TRICKLE)	// trickle or deluge
        				{
        				timer.start();
        				}
        			else
        				{
        				timer.stop();
        				}
        			}
        		}
        	};
        margin.add(sendtosynth);
        		       
        margin.add(Strut.makeVerticalStrut(16));
                
        margin.add(new PushButton("Export...", new String[]
            {
            "Keep Patch",
            "Edit Patch",
            "Save to File"
            })
            {
            public void perform(int val)
                {
                save(val);
                }
            });

        margin.add(Strut.makeVerticalStrut(16));

		JPanel pan = new JPanel();
        pan.setLayout(new BorderLayout());
        pan.setBackground(getBackground());
		margin.add(pan);

		HBox hbox = new HBox();
		pan.add(hbox, BorderLayout.CENTER);
        LabelledDial xCC = new LabelledDial("X CC", blank, "xcc", Style.COLOR_A(), -1, 127)
        	{
            public void update(String key, Model model)
                {
                super.update(key, model);
                //System.err.println(model.get(key, -1));
				Synth.setLastX("" + model.get(key, -1), "XCC", EDISYN_MORPH_PREFERENCES_KEY); 
                }

        	public String map(int value)
        		{
        		if (value == -1) return "Off";
        		else return "" + value;
        		}
        	};
		hbox.add(xCC); 

        LabelledDial yCC = new LabelledDial("Y CC", blank, "ycc", Style.COLOR_B(), -1, 127)
        	{
            public void update(String key, Model model)
                {
                super.update(key, model);
                //System.err.println(model.get(key, -1));
				Synth.setLastX("" + model.get(key, -1), "YCC", EDISYN_MORPH_PREFERENCES_KEY); 
                }

        	public String map(int value)
        		{
        		if (value == -1) return "Off";
        		else return "" + value;
        		}
        	};
		hbox.add(yCC); 

        buttons = new PushButton[4];
        for(int i = 0; i < 4; i++)
            {
            int s = 0;
            final int[] swap = new int[3];
            for(int j = 0; j < 4; j++)
                {
                if (j != i)
                    {
                    swap[s] = j;
                    s++;
                    }
                }
                                
            final int _i = i;
            buttons[i] = new PushButton("[Empty]", new String[] 
                { 
                "Set to Current Patch",
                "Set to Joystick Position",
                "Load from File...",
                "Clear",
                null,
                "Swap with " + POSITIONS[swap[0]],
                "Swap with " + POSITIONS[swap[1]],
                "Swap with " + POSITIONS[swap[2]],
                null,
                "Take from Nudge 1",
                "Take from Nudge 2",
                "Take from Nudge 3",
                "Take from Nudge 4",
                null,
                "Take from Hill-Climb Archive q",
                "Take from Hill-Climb Archive r",
                "Take from Hill-Climb Archive s",
                "Take from Hill-Climb Archive t",
                "Take from Hill-Climb Archive u",
                "Take from Hill-Climb Archive v",
                })
                {
                public void perform(int val)
                    {
                    if (val < 4)
                        resetButton(_i, val);
                    else if (val < 8)
                        swap(_i, swap[val - 5]);
                    else if (val < 13)
                        takeFromNudge(_i, val - 9);
                    else
                        takeFromArchive(_i, val - 14);
                    updateAgain();               
                    }
                };
            }

                
        joystick = new Joystick(synth)
            {
            public void updatePosition()
                {
                super.updatePosition();
                Morph.this.update(xPos, yPos);
                }
            };
        joystick.setUnsetColor(Color.BLUE);
        outer.add(joystick,BorderLayout.CENTER);
        top = new JPanel();
        top.setBackground(getBackground());
        top.setLayout(new BorderLayout());
        bottom = new JPanel();
        bottom.setBackground(getBackground());
        bottom.setLayout(new BorderLayout());
        outer.add(top, BorderLayout.NORTH);
        outer.add(bottom, BorderLayout.SOUTH);
        topCenter = new JPanel();
        topCenter.setBackground(getBackground());
        topCenter.setLayout(new BorderLayout());
        bottomCenter = new JPanel();
        bottomCenter.setBackground(getBackground());
        bottomCenter.setLayout(new BorderLayout());
        top.add(topCenter, BorderLayout.CENTER);
        bottom.add(bottomCenter, BorderLayout.CENTER);
        top.add(buttons[0], BorderLayout.WEST);
        top.add(buttons[1], BorderLayout.EAST);
        bottom.add(buttons[2], BorderLayout.WEST);
        bottom.add(buttons[3], BorderLayout.EAST);
        }
    
    public void handleCC(Midi.CCData cc)
    	{
    	// the channel is right, we already know that, so just check for type
    	if (cc.type == Midi.CCDATA_TYPE_RAW_CC)
    		{
    		boolean updated = false;
    		if (cc.number == blank.getModel().get("xcc", -1))
    			{
    			joystick.xPos = ((cc.value / 127.0) * 2.0) - 1.0;
    			updated = true;
    			}
    			
    		if (cc.number == blank.getModel().get("ycc", -1))	// note no "else"
    			{
    			joystick.yPos = ((cc.value / 127.0) * 2.0) - 1.0;
    			updated = true;
    			}

			if (updated)
				{
    			joystick.updatePosition();
                update(joystick.xPos, joystick.yPos);
                }
            joystick.repaint();
    		}
    	}
    
    public void updateSound()
        {
        if (blank.getModel().get("sendonchange", SEND_TYPE_NOTE) == SEND_TYPE_NOTE)               // send on note instead
            {
            Model backup = synth.getModel();
            synth.model = current;
            synth.sendAllParameters();
            synth.model = backup;                   
            }
        }
        
    public void postUpdateSound()
        {
        }
        
    void takeFromNudge(int val, int nudge)
        {
        Model n = synth.nudge[nudge];
        if (n == null)
            {
            sources[val] = null;
            buttons[val].getButton().setText("[Empty]");
            }
        else
            {
            sources[val] = n.copy();
            buttons[val].getButton().setText("Nudge " + (nudge + 1));
            }
        }
    
    void takeFromArchive(int val, int archive)
        {
        HillClimb climb = synth.hillClimb;
        if (climb == null)
            {
            sources[val] = null;
            buttons[val].getButton().setText("[Empty]");
            }
        else
            {
            Model n = climb.currentModels[HillClimb.NUM_CANDIDATES + val];  // archive starts after candidates
            if (n == null)
                {
                sources[val] = null;
                buttons[val].getButton().setText("[Empty]");
                }
            else
                {
                sources[val] = n.copy();
                buttons[val].getButton().setText("Archive " + (char)('q' + archive));
                }
            }
        }
    
    void swap(int a, int b)
        {
        Model temp = sources[a];
        sources[a] = sources[b];
        sources[b] = temp;
        String tempString = buttons[a].getButton().getText();
        buttons[a].getButton().setText(buttons[b].getButton().getText());
        buttons[b].getButton().setText(tempString);
        repaint();
        }
        
    void save(int operation)
        {
        if (operation == 0)     // Keep
            {
            // Keep for sure?
            if (synth.showSimpleConfirm("Keep Patch", "Load Patch into Editor?"))
                {
                synth.tabs.setSelectedIndex(0);
                synth.setSendMIDI(false);
                // push to undo if they're not the same
                if (!current.keyEquals(synth.getModel()))
                    synth.undo.push(synth.getModel());
                                                                        
                // Load into the current model
                current.copyValuesTo(synth.getModel());
                synth.setSendMIDI(true);
                synth.sendAllParameters();
                }
            }
        else if (operation == 1)        // Edit
            {
            Synth newSynth = synth.doDuplicateSynth();
            // Copy the parameters forward into the synth, then
            // link the synth's model back to currentModels[_i].
            // We do this because the new synth's widgets are registered
            // with its model, so we can't just replace the model.
            // But we can certainly replace currentModels[_i]!
            newSynth.setSendMIDI(false);
            current.copyValuesTo(newSynth.getModel());
            newSynth.setSendMIDI(true);
            newSynth.sendAllParameters();
            }
        else    // Save to File  -- FIXME, should we copy to the synth.model?
            {
            Model backup = synth.model;
            synth.model = current;
            synth.doSaveAs("morphed." + synth.getPatchName(synth.getModel()) + ".syx");
            synth.model = backup;
            synth.updateTitle();
            }
        }
    
    
    int joy = 0;
    void resetButton(int button, int reset)
        {
        if (reset == 0)
            {
            sources[button] = synth.getModel().copy();
       		String currentPatchName = synth.getPatchName(synth.getModel());
       		buttons[button].getButton().setText(currentPatchName == null ? "Current Patch" : "Current Patch : " + currentPatchName);
            }
        else if (reset == 1)
            {
            sources[button] = current.copy();
            buttons[button].getButton().setText("Joystick " + (++joy));
            }
        else if (reset == 2)
            {
            Model cancel = sources[button];         //  the original model, to be restored if we failed
            Model backup = synth.model;
            sources[button] = backup.copy();
            synth.model = sources[button];
            synth.setShowingLimitedBankSysex(true);
            boolean result = synth.doOpen(false);
            synth.setShowingLimitedBankSysex(false);
            synth.model = backup;           // restore
            synth.updateTitle();
            if (result)
                {
                String name = sources[button].get("name", "");
                if (name.equals(""))
                    {
                    File filename = synth.getFile();
                    if (filename != null)
                        {
                        buttons[button].getButton().setText(filename.getName());
                        }
                    else
                        {
                        buttons[button].getButton().setText(synth.getTitleBarSynthName().trim() + " " + (++joy));
                        }
                    }
                else
                    {
                    buttons[button].getButton().setText(name);
                    }
                }
            else
                {
                sources[button] = cancel;
                }
            }
        else if (reset == 3)
            {
            sources[button] = null;
            buttons[button].getButton().setText("[Empty]");
            }
        }
    
    void updateAgain() { update(lastx, lasty); }
    
    double lastx = 0;
    double lasty = 0;
    void update(double x, double y)
        {
        lastx = x;
        lasty = y;
        
        x += 1;
        x *= 0.5;
        y += 1;
        y *= 0.5;
        // now x and y are 0...1
        
        // determine how many models are non-null
        int count = 0;
        for(int i = 0; i < 4; i++)
            if (sources[i] != null)
                count++;
                        
        if (count == 0)         // uh ... stupid user
            return;
                
        Model[] models = new Model[count];
        double[] weights = new double[4];
        double[] lw = new double[count];
        double[] w = new double[count];
        
        // Figure out our strategy
        int strategy = blank.getModel().get("nonmetricparams", 0) + Model.CATEGORICAL_STRATEGY_MORPH;           // so it goes -3, -2, ..., 3
        if (strategy >= 0 && sources[strategy] == null)         // fix null models right off the bat
            strategy = Model.CATEGORICAL_STRATEGY_MORPH;
        
        // Fill the models, weights, and last weights
        count = 0;
        for(int i = 0; i < 4; i++)
            if (sources[i] != null)
                {
                weights[i] = computeWeight(i, x, y);
                w[count] = weights[i];
                lw[count] = lastWeights[i];
                models[count] = sources[i];
                if (strategy == i)              // we were locking to this one
                    strategy = count;       // change the index since we're removing null models
                count++;
                }
        
        // perform morph
        synth.getUndo().setWillPush(false);
        synth.setSendMIDI(false);
        current = current.morph(synth.random, models, synth.getModel(), synth.getMutationKeys(), w, lw, strategy);
        synth.getUndo().setWillPush(true);
        synth.setSendMIDI(true);
        lastWeights = weights;
                                
        // emit
        if (blank.getModel().get("sendonchange", SEND_TYPE_NOTE) == SEND_TYPE_CHANGING)               // send only on change
            {
            Model backup = synth.getModel();
            synth.model = current;
            synth.sendAllParameters();
            synth.model = backup;
            }
        }
        
    double computeWeight(int index, double x, double y)
        {
        if (index == 0) { x = 1 - x; y = 1 - y; }       // top left
        else if (index == 1) { y = 1 - y; }             // top right
        else if (index == 2) { x = 1 - x; }             // bottom left
        else if (index == 3) {  }                       // bottom right
        return (x < y ? x : y); 
        }
   
    boolean isShowingPane()
        {
        return (synth.morphPane != null && synth.tabs.getSelectedComponent() == synth.morphPane);
        }
    
    boolean startedUp = false;
    
    
    int untitled = 0;
    public void initialize()
        {
        current = synth.getModel().copy(); // load it up initially so it's not blank
        String currentPatchName = synth.getPatchName(synth.getModel());
        buttons[0].getButton().setText(currentPatchName == null ? ("Untitled " + (untitled++)) : currentPatchName);
        buttons[1].getButton().setText("[Empty]");
        buttons[2].getButton().setText("[Empty]");
        buttons[3].getButton().setText("[Empty]");
        sources[0] = synth.getModel().copy();
        sources[1] = null;
        sources[2] = null;
        sources[3] = null;
        
        shuffledKeys = synth.getModel().getKeys();
        StringUtility.shuffle(shuffledKeys, synth.random);
        
 		if (timer == null) 
 			timer = new javax.swing.Timer(timerDelay, new ActionListener()
 				{
 				public void actionPerformed(ActionEvent e)
{
 				if (blank.getModel().get("sendonchange", SEND_TYPE_NOTE) == SEND_TYPE_TRICKLE)	// trickle
 					{
					timerCount++;
					if (timerCount >= shuffledKeys.length)
						{
						StringUtility.shuffle(shuffledKeys, synth.random);
						timerCount = 0;
						}
    		        Model backup = synth.getModel();
            		synth.model = current;
					synth.sendOneParameter(shuffledKeys[timerCount]);
	         	   synth.model = backup;                   
					}
				else if (blank.getModel().get("sendonchange", SEND_TYPE_NOTE) == SEND_TYPE_DELUGE)	// deluge
					{
					Model backup = synth.getModel();
					synth.model = current;
					synth.sendAllParameters();
					synth.model = backup;                   
					}
				else
					{
					timer.stop();
					}
				}
 				});
         }
        
    public void startup()
        {
        if (!startedUp)
            {
            if (!synth.isSendingTestNotes())
                {
                synth.doSendTestNotes();
                }
            }
        startedUp = true;
        if (timer != null)
        	timer.start();
        }
                
    public void shutdown()
        {
        if (timer != null) 
        	timer.stop();
        if (startedUp)
            {
            synth.doSendAllSoundsOff(false);
            if (synth.isSendingTestNotes())
                {
                synth.doSendTestNotes();
                }
            if (synth.isRepeatingCurrentPatch())
                {
                synth.doRepeatCurrentPatch();
                }
            // restore patch
            synth.sendAllParameters();
            }
        startedUp = false;
        }
                        
    }
        
        
