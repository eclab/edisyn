/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn;

import edisyn.synth.*;
import edisyn.nn.*;
import edisyn.gui.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

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
    // OPERATIONS
    public static final int OPERATION_SEED_FROM_PATCH = 0;
    public static final int OPERATION_SEED_FROM_MORPH = 1;
    public static final int OPERATION_SEED_FROM_NUDGE = 2;
    public static final int OPERATION_SEED_FROM_FOUR = 3;
    public static final int OPERATION_SEED_FROM_SIX = 4;
    public static final int OPERATION_SEED_FROM_LIBRARIAN = 100;

    public static final int OPERATION_CLIMB = 5;
    public static final int OPERATION_CONSTRICT = 6;
    public static final int OPERATION_CLIMB_NN = 7;

    // HILL CLIMBING AND CONSTRICTION RATES
    // Note that the mutation rates go 0...100 inclusive, ints
    public static final int INITIAL_HILL_CLIMB_RATE = 37;         // roughly 5 when we do weight^3
    public static final int INITIAL_CONSTRICT_RATE = 0;
    // Note that the recombination rates go 0.0...1.0 inclusive, doubles
    public static final double CLIMB_RECOMBINATION_RATE = 0.75;
    public static final double CONSTRICT_RECOMBINATION_RATE = 0.75;

    /// HILL CLIMBING STACK
    class State
        {
        Model[] parents;
        int[] parentIndices;
        boolean[] parentsSelected;
        Model[] children;
        int operation;
        }
    // The stack proper
    ArrayList stack = new ArrayList();
        
        
    /// NUMBER OF CANDIDATE SOLUTIONS
    public static final int NUM_CANDIDATES = 32;
    /// Candidates are divided into STAGES of 16 each.  The mutation/recombionation procedures use the stage to determine how much mutation to do
    public static final int STAGE_SIZE = 16;
    // Size of the archive.  We might make this bigger later.
    public static final int ARCHIVE_SIZE = 6;
    // There are more models than candidates: #17 is the current Model
    public static final int NUM_MODELS = NUM_CANDIDATES + ARCHIVE_SIZE + 1;
    
    
    // models currently being played and displayed
    Model[] currentModels = new Model[NUM_MODELS];

    // the most recent procedure performed (see OPERATIONS above)
    int operation;

    // The ratings buttons, in the form ratings[candidate][rating]
    JRadioButton[][] ratings = new JRadioButton[NUM_MODELS + 1][3];
    // The selection checkboxes, in the form selected[candidate].  Note that the only candidates are the actual candidates, not archives etc.
    JCheckBox[] selected = new JCheckBox[NUM_CANDIDATES];
    // The play buttons, in the form plays[candidate]
    PushButton[] plays = new PushButton[NUM_MODELS];
    
    // An empty synth used to store the hillclimbing and constriction model parameters,
    // since they can't be stored in the regular synth.
    Blank blank;
    
    // "Iteration 123"
    Category iterations;
    
    // Which sound is currently scheduled to be playing in the regular iteration
    int currentPlay = 0;
    // Which sound, if any, is currently playing because the user interrupted the regular iteration by pressing the sound's play button.
    // If no such sound, this is -1
    int temporaryPlay = -1;
    
    // Climb button
    PushButton climb;
    // Constrict button.  Notice that this is different from Climb
    PushButton constrict;
    // Climb mutation weights
    LabelledDial hillClimbRate;
    // Constriction mutation weights.  Notice that this is different from Climb
    LabelledDial constrictRate;

    // Retry button
    PushButton retry;
    // Reset button
    PushButton reset;
    // Back button
    PushButton back;
    // Bigger checkbox
    JCheckBox bigger;
    
    // Box holding the hillcimber button and dial
    VBox hillClimbBox;
    // Box holding the constrictor button and dial
    VBox constrictBox;
    // Box holding either the hillClimbBox or the constrictBox.  Needs to be occasionally revalidated.
    VBox outerBox;
    
    // List of climbing methods
    JComboBox method;
    
    // First 16 candidates
    VBox extraCandidates1;
    // Next 16 candidates
    VBox extraCandidates2;
    // holds either extraCandidates1 or extraCandidates1 + extraCandidates2
    VBox candidates;
    
    
    boolean startSoundsAgain = false;
    public static final int NO_MENU_BUTTON = -1;
    int menuButton = NO_MENU_BUTTON;
    public void setToCurrentPatch()
        {
        if (menuButton != NO_MENU_BUTTON)
            {
            currentModels[menuButton] = synth.getModel().copy();
            currentPlay = menuButton;
            menuButton = NO_MENU_BUTTON;

            if (startSoundsAgain)
                {
                synth.doSendTestNotes();
                startSoundsAgain = false;
                }
            }
        }
        
    State popStack()   
        {
        if (stack.size() == 0) 
            return null;
        else
            return (State)(stack.remove(stack.size() - 1));
        }
    
    void pushStack(int[] parentIndices, Model[] parents, boolean[] parentsSelected, Model[] children)
        {
        State state = new State();
        state.parents = new Model[parents.length];
        state.parentIndices = new int[parents.length];
        state.parentsSelected = new boolean[parentsSelected.length];
        state.operation = operation;
        
        for(int i = 0; i < parents.length; i++)
            {
            state.parents[i] = copy(parents[i]);
            state.parentIndices[i] = parentIndices[i];
            state.parentsSelected[i] = parentsSelected[i];
            }

        for(int i = 0; i < parentsSelected.length; i++)
            {
            state.parentsSelected[i] = parentsSelected[i];
            }
        
        state.children = new Model[children.length];
        for(int i = 0; i < children.length; i++)
            {
            state.children[i] = copy(children[i]);
            }
                
        stack.add(state);
        }
        
    State topStack()
        {
        if (stack.size() == 0) 
            return null;
        else
            return (State)(stack.get(stack.size() - 1));
        }
        
    boolean stackEmpty()
        {
        return (stack.size() == 0);
        }
        
    boolean stackInitial()
        {
        return (stack.size() == 1);
        }
    
    String titleForButton(int _i)
        {
        return "Play " + (_i < 16 ? 
            (char)('a' + _i) :
                (_i < NUM_CANDIDATES ? 
                (char)('A' + (_i - 16)) :
                    (_i < NUM_MODELS - 1 ?
                    (char)('q' + (_i - NUM_CANDIDATES)) :
                    'z')));
        }
     
    VBox buildCandidate(int i)
        {
        final int _i = i;

        VBox vbox = new VBox();
        plays[_i] = new PushButton(titleForButton(i))
            {
            public void perform()
                {
                if (synth.isSendingTestNotes())
                    {
                    temporaryPlay = _i;
                    }
                else
                    {
                    for(int j = 0; j < NUM_MODELS; j++)       
                        {
                        plays[j].getButton().setForeground(new JButton().getForeground());
                        plays[j].getButton().setText(titleForButton(j));
                        }
                    plays[_i].getButton().setForeground(Color.RED);
                    plays[_i].getButton().setText("<HTML><B>" + titleForButton(_i) + "</b></HTML>");

                    // change the model, send all parameters, maybe play a note,
                    // and then restore the model.
                    Model backup = synth.model;
                    synth.model = currentModels[_i];
                    synth.sendAllParameters();
                    synth.doSendTestNote();
                    synth.model = backup;
                    temporaryPlay = _i;
                    }

                }
            };
        plays[i].getButton().setFocusable(false);
        vbox.add(plays[i]);


        HBox hh = new HBox();
        VBox vv = new VBox();

        Box b = new Box(BoxLayout.X_AXIS);
        b.setBackground(Style.BACKGROUND_COLOR());
        b.add(Box.createGlue());
        b.add(ratings[i][0] = new JRadioButton("1"));
        ratings[i][0].setFocusable(false);
        ratings[i][0].setForeground(Style.TEXT_COLOR());
        ratings[i][0].setFont(Style.SMALL_FONT());
        ratings[i][0].putClientProperty("JComponent.sizeVariant", "small");
        ratings[i][0].setOpaque(false);  // for windows
        
        vv.add(b);
                        
        b = new Box(BoxLayout.X_AXIS);
        b.setBackground(Style.BACKGROUND_COLOR());
        b.add(Box.createGlue());
        b.add(ratings[i][1] = new JRadioButton("2"));
        ratings[i][1].setFocusable(false);
        ratings[i][1].setForeground(Style.TEXT_COLOR());
        ratings[i][1].setFont(Style.SMALL_FONT());
        ratings[i][1].putClientProperty("JComponent.sizeVariant", "small");
        ratings[i][1].setOpaque(false);  // for windows
        b.add(Box.createGlue());
        vv.add(b);
                        
        b = new Box(BoxLayout.X_AXIS);
        b.setBackground(Style.BACKGROUND_COLOR());
        b.add(Box.createGlue());
        b.add(ratings[i][2] = new JRadioButton("3"));
        ratings[i][2].setFocusable(false);
        ratings[i][2].setForeground(Style.TEXT_COLOR());
        ratings[i][2].setFont(Style.SMALL_FONT());
        ratings[i][2].putClientProperty("JComponent.sizeVariant", "small");
        ratings[i][2].setOpaque(false);  // for windows
        b.add(Box.createGlue());
        vv.add(b);
                
        hh.add(vv);
                
        vv = new VBox();
        if (i < NUM_CANDIDATES)
            {
            selected[i] = new JCheckBox("");
            selected[i].setFocusable(false);
            selected[i].setForeground(Style.TEXT_COLOR());
            selected[i].setOpaque(false);  // for windows
            selected[i].setFont(Style.SMALL_FONT());
            selected[i].setSelected(true);
            selected[i].putClientProperty("JComponent.sizeVariant", "small");
            vv.add(selected[i]);
            }
        hh.add(vv);
        vbox.add(hh);
        
           
        JMenuItem[] doItems = new JMenuItem[17];
        doItems[0] = new JMenuItem("Keep Patch");
        doItems[0].addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                // Keep for sure?
                //if (synth.showSimpleConfirm("Keep Patch", "Load Patch into Editor?"))
                    {
                    synth.tabs.setSelectedIndex(0);
                    synth.setSendMIDI(false);
                    // push to undo if they're not the same
                    if (!currentModels[_i].keyEquals(synth.getModel()))
                        synth.undo.push(synth.getModel());
                                        
                    // Load into the current model
                    synth.undo.setWillPush(false);
                    currentModels[_i].copyValuesTo(synth.getModel());
                    synth.undo.setWillPush(true);
                    synth.setSendMIDI(true);
                    synth.sendAllParameters();
                    }
                }
            });
        if (_i == NUM_CANDIDATES + ARCHIVE_SIZE)
            doItems[0].setEnabled(false);

        doItems[1] = new JMenuItem("Edit in New Editor");
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
                newSynth.undo.setWillPush(false);
                currentModels[_i].copyValuesTo(newSynth.getModel());
                newSynth.undo.setWillPush(true);
                newSynth.setSendMIDI(true);
                currentModels[_i] = newSynth.getModel();
                newSynth.sendAllParameters();
                }
            });
        if (_i == NUM_CANDIDATES + ARCHIVE_SIZE)
            doItems[1].setEnabled(false);

        doItems[2] = new JMenuItem("Save to File");
        doItems[2].addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                Model backup = synth.model;
                synth.model = currentModels[_i];
                synth.doSaveAs("" + stack.size() + "." + 
                    (_i < NUM_CANDIDATES ? (_i + 1) : ("A" + (_i - NUM_CANDIDATES + 1))) +
                    "." + synth.getPatchName(synth.getModel()) + ".syx");
                synth.model = backup;
                synth.updateTitle();
                }
            });
        if (_i == NUM_CANDIDATES + ARCHIVE_SIZE)
            doItems[2].setEnabled(false);

        doItems[3] = null;

        doItems[4] = new JMenuItem("Request Current Patch");
        doItems[4].addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                if (synth.receiveCurrent.isEnabled())
                    {
                    menuButton = _i;
                    startSoundsAgain = false;
                    // we do this because some synths send data in chunks and in-between those
                    // chunks we may send current patch and play it, messing up the chunk (such as on the JV-880)
                    if (synth.isSendingTestNotes())
                        {
                        synth.doSendTestNotes();
                        startSoundsAgain = true;
                        }
                    synth.doRequestCurrentPatch();
                    // Notice we do NOT do updateAgain(); but we'll do it when the patch comes in
                    }
                else
                    {
                    synth.showSimpleError("Cannot Request Current Patch", "This synthesizer does not support requesting the current patch (sorry).");
                    }
                }
            });
        if (_i == NUM_CANDIDATES + ARCHIVE_SIZE)
            doItems[4].setEnabled(false);
            


        doItems[5] = new JMenuItem("Request Patch...");
        doItems[5].addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                if (synth.receivePatch.isEnabled())
                    {
                    menuButton = _i;
                    startSoundsAgain = false;
                    // we do this because some synths send data in chunks and in-between those
                    // chunks we may send current patch and play it, messing up the chunk (such as on the JV-880)
                    if (synth.isSendingTestNotes())
                        {
                        synth.doSendTestNotes();
                        startSoundsAgain = true;
                        }
                    synth.doRequestPatch();
                    // Notice we do NOT do updateAgain(); but we'll do it when the patch comes in
                    }
                else
                    {
                    synth.showSimpleError("Cannot Request Patch", "This synthesizer does not support requesting a patch (sorry).");
                    }
                }
            });
        if (_i == NUM_CANDIDATES + ARCHIVE_SIZE)
            doItems[5].setEnabled(false);
            



        doItems[6] = new JMenuItem("Load from File...");
        doItems[6].addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                Model backup = synth.model;
                synth.model = currentModels[_i];
                synth.setShowingLimitedBankSysex(true);
                synth.doOpen(false);
                synth.setShowingLimitedBankSysex(false);
                currentModels[_i] = synth.model;
                synth.model = backup;
                synth.updateTitle();
                }
            });
        if (_i == NUM_CANDIDATES + ARCHIVE_SIZE)
            doItems[6].setEnabled(false);
            
        doItems[7] = new JMenuItem("Copy from Morph");
        doItems[7].addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                synth.setSendMIDI(false);
                synth.morph.current.copyValuesTo(currentModels[_i]);
                synth.setSendMIDI(true);
                }
            });
        if (_i == NUM_CANDIDATES + ARCHIVE_SIZE)
            doItems[7].setEnabled(false);
            
        doItems[8] = null;
            
        doItems[9] = new JMenuItem("Nudge Candidates to Me");
        doItems[9].addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                Random random = synth.random;
                String[] keys = synth.getMutationKeys();

                for(int i = 0; i < NUM_CANDIDATES; i++)
                    {
                    if (i == _i) continue;
                    currentModels[i].recombine(random, currentModels[_i], keys, synth.nudgeRecombinationWeight).mutate(random, keys, synth.nudgeMutationWeight);
                    }
                }
            });

        doItems[10] = null;

        doItems[11] = new JMenuItem("Archive to q");
        doItems[11].addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                currentModels[NUM_CANDIDATES + 0] = (Model)(currentModels[_i].clone());
                }
            });

        doItems[12] = new JMenuItem("Archive to r");
        doItems[12].addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                currentModels[NUM_CANDIDATES + 1] = (Model)(currentModels[_i].clone());
                }
            });

        doItems[13] = new JMenuItem("Archive to s");
        doItems[13].addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                currentModels[NUM_CANDIDATES + 2] = (Model)(currentModels[_i].clone());
                }
            });

        doItems[14] = new JMenuItem("Archive to t");
        doItems[14].addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                currentModels[NUM_CANDIDATES + 3] = (Model)(currentModels[_i].clone());
                }
            });
                
        doItems[15] = new JMenuItem("Archive to u");
        doItems[15].addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                currentModels[NUM_CANDIDATES + 4] = (Model)(currentModels[_i].clone());
                }
            });
                
        doItems[16] = new JMenuItem("Archive to v");
        doItems[16].addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                currentModels[NUM_CANDIDATES + 5] = (Model)(currentModels[_i].clone());
                }
            });
            
        PushButton options = new PushButton("Options", doItems);
        options.getButton().setFocusable(false);
        vbox.add(options);
        
        return vbox;
        }


        
    public HillClimb(final Synth synth)
        {
        super(synth);
        
        method = new JComboBox(synth instanceof ProvidesNN ?
            new String[] { "Hill-Climber", "Constrictor", "NN Hill-Climber" } :
            new String[] { "Hill-Climber", "Constrictor" });
    
        blank = new Blank();

        addAncestorListener ( new AncestorListener ()
            {
            public void ancestorAdded ( AncestorEvent event )
                {
                requestFocusInWindow();
                }

            public void ancestorRemoved ( AncestorEvent event )
                {
                // will get removed
                }

            public void ancestorMoved ( AncestorEvent event )
                {
                // don't care
                }
            } );
        setFocusable(true);
    
        addKeyListener(new KeyListener()
            {
            public void keyPressed(KeyEvent e)
                {
                }
            public void keyReleased(KeyEvent e)
                {
                }
            public void keyTyped(KeyEvent e)
                {
                char c = e.getKeyChar();
                        
                if (c >= 'a' && c <= 'p')
                    {
                    int p = (int)(c - 'a');
                    plays[p].perform();
                    }
                else if (c >= 'A' && c <= 'P' && NUM_CANDIDATES == 32)
                    {
                    int p = (int)(c - 'A' + 16);
                    plays[p].perform();
                    }
                else if ((c >= 'q' && c <= 'v'))
                    {
                    int p = (int)(c - 'q' + NUM_CANDIDATES);
                    plays[p].perform();                             
                    }
                else if (c =='z')
                    {
                    int p = (int)(NUM_MODELS - 1);
                    plays[p].perform();                             
                    }
                else if (c == ' ')
                    {
                    climb.perform();
                    }
                else if (c == KeyEvent.VK_BACK_SPACE)
                    {
                    back.perform();
                    }
                else if (c == KeyEvent.VK_ENTER)
                    {
                    retry.perform();
                    }
                else if (c >= '1' && c <= '3')
                    {
                    ratings[lastPlayedSound()][(int)(c - '1')].setSelected(true);
                    }
                }
            });
    
        ButtonGroup one = new ButtonGroup();
        ButtonGroup two = new ButtonGroup();
        ButtonGroup three = new ButtonGroup();
                
                
        VBox top = new VBox();
        HBox toprow = new HBox();
        add(top, BorderLayout.CENTER);

        // add globals

        Category panel = new Category(null, "Iteration 1", Style.COLOR_GLOBAL());
        iterations = panel;


        HBox iterationsBox = new HBox();
        VBox vbox = new VBox();

        // has to be first so others can have their size based on it
        back = new PushButton("Back Up")
            {
            public void perform()
                {
                pop();
                resetCurrentPlay();
                }
            };
        back.getButton().setFocusable(false);
           
        outerBox = new VBox();
        iterationsBox.add(outerBox);
        

        hillClimbBox = new VBox();
        climb = new PushButton("Climb")
            {
            public void perform()
                {
                if (method.getSelectedIndex() == 2)             // NN Climb
                    {
                    climbNN();
                    }
                else
                    {
                    climb();
                    }
                resetCurrentPlay();
                }
            };
        climb.getButton().setPreferredSize(back.getButton().getPreferredSize());
        climb.getButton().setFocusable(false);
                
        hillClimbBox.add(climb);

        String s = synth.getLastX("HillClimbRate", synth.getClass().getName());
        hillClimbRate = new LabelledDial("Mutation Rate", blank, "hillclimbrate", Style.COLOR_GLOBAL(), 0, 100)
            {
            public String map(int val)
                {
                double v = ((val / 100.0) * (val / 100.0) * (val / 100.0)) * 100;
                if (v == 100)
                    return "100.0";
                else if (v >= 10.0)
                    return String.format("%.2f", v);
                else
                    return String.format("%.3f", v);
                }
            
            public void update(String key, Model model)
                {
                super.update(key, model);
                synth.setLastX("" + model.get(key), "HillClimbRate", synth.getClass().getName());
                }
            };
        
        int v = INITIAL_HILL_CLIMB_RATE;
        if (s != null)
            try { v = Integer.parseInt(s); } catch (Exception e) { Synth.handleException(e); }
        if (v < 0 || v > 100) v = INITIAL_HILL_CLIMB_RATE;
        hillClimbRate.setState(v);
        
        blank.getModel().set("hillclimbrate", v);
        hillClimbBox.add(hillClimbRate);
                
        constrictBox = new VBox();
        constrict = new PushButton("Constrict")
            {
            public void perform()
                {
                constrict();
                resetCurrentPlay();
                }
            };
        constrict.getButton().setPreferredSize(back.getButton().getPreferredSize());
        constrict.getButton().setFocusable(false);
        constrictBox.add(constrict);

        s = synth.getLastX("ConstrictRate", synth.getClass().getName());  // we don't do this one anyway

        constrictRate = new LabelledDial("Mutation Rate", blank, "constrictrate", Style.COLOR_GLOBAL(), 0, 100)
            {
            public String map(int val)
                {
                double v = ((val / 100.0) * (val / 100.0) * (val / 100.0)) * 100;
                if (v == 100)
                    return "100.0";
                else if (v >= 10.0)
                    return String.format("%.2f", v);
                else
                    return String.format("%.3f", v);
                }
            
            public void update(String key, Model model)
                {
                super.update(key, model);
                synth.setLastX("" + model.get(key), "ConstrictRate", synth.getClass().getName());
                }
            };

        v = INITIAL_CONSTRICT_RATE;
        if (s != null)
            try { v = Integer.parseInt(s); } catch (Exception e) { Synth.handleException(e); }
        if (v < 0 || v > 100) v = INITIAL_CONSTRICT_RATE;
        constrictRate.setState(v);
        
        blank.getModel().set("constrictrate", v);
        constrictBox.add(constrictRate);
 
 
        vbox = new VBox();
 
        retry = new PushButton("Retry")
            {
            public void perform()
                {
                again();
                resetCurrentPlay();
                }
            };
        retry.getButton().setPreferredSize(back.getButton().getPreferredSize());
        retry.getButton().setFocusable(false);
        vbox.add(retry);

        // add the aforementioned Back up button
        vbox.add(back);
              
        reset = new PushButton("Reset...",
            new String[] { "From Original Patch",
                                   "From Morph",
                                   "From Nudge Targets", 
                                   "From First Four Candidates",
                                   "From First Six Candidates" })
            {
            public void perform(int val)
                {
                if (method.getSelectedIndex() == 2) // NN Climb
                    {
                    Random random = synth.random;
                    String[] keys = synth.getMutationKeys();
                    double weight = blank.getModel().get("hillclimbrate", 0) / 100.0;
                    weight = weight * weight * weight; // make more sensitive at low end
                    Model model = (val == OPERATION_SEED_FROM_PATCH ? synth.getModel() : val == OPERATION_SEED_FROM_MORPH ? synth.morph.current : null);
                    produceNN(random, keys, weight, model);
                    } 
                else
                    {
                    initialize(val == OPERATION_SEED_FROM_PATCH ? synth.getModel() : val == OPERATION_SEED_FROM_MORPH ? synth.morph.current : null, val);
                    }
               
                resetCurrentPlay();
                }
            };
        reset.getButton().setPreferredSize(back.getButton().getPreferredSize());
        reset.getButton().setFocusable(false);
        vbox.add(reset);

        iterationsBox.add(vbox);
        
        panel.add(iterationsBox, BorderLayout.CENTER);

        s = synth.getLastX("HillClimbMethod", synth.getClass().getName());
        method.setFont(Style.SMALL_FONT());
        method.putClientProperty("JComponent.sizeVariant", "small");
        method.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                int m = method.getSelectedIndex();
                synth.setLastX("" + m, "HillClimbMethod", synth.getClass().getName());
                setMethod(m);
                }
            });
        
        v = 0;
        if (s != null)
            try { v = Integer.parseInt(s); } catch (Exception e) { Synth.handleException(e); }
        if (v < 0 || v > 2) v = 0;
        // clear NN
        if (v == 2 && !(synth instanceof ProvidesNN)) v = 1;
        if (v == 0 || v == 2)
            {
            outerBox.add(hillClimbBox);
            }
        else
            {
            outerBox.add(constrictBox);
            }
        method.setSelectedIndex(v);
 
        JLabel methodLabel = new JLabel("Method: ");
        methodLabel.setForeground(Style.TEXT_COLOR());
        methodLabel.setFont(Style.SMALL_FONT());
        methodLabel.putClientProperty("JComponent.sizeVariant", "small");
        methodLabel.setOpaque(false);  // for windows
        
        HBox eb = new HBox();
        eb.add(methodLabel);
        eb.add(method);             // we do addLast rather than add to overcome the stupid OS X "Smoo..." bug.

        bigger = new JCheckBox("Big");
        bigger.setFocusable(false);
        bigger.setOpaque(false);  // for windows
        bigger.setForeground(Style.TEXT_COLOR());
        bigger.setFont(Style.SMALL_FONT());
        bigger.putClientProperty("JComponent.sizeVariant", "small");
        
        s = synth.getLastX("HillClimbBigger", synth.getClass().getName());

        bigger.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                setBigger(bigger.isSelected());
                }
            });
            
        boolean bb = false;
        if (s != null)
            try { bb = (s.equals("true")); } catch (Exception e) { Synth.handleException(e); }

        bigger.setSelected(bb);
        eb.addLast(bigger);

        panel.add(eb, BorderLayout.NORTH);
 
        toprow.add(panel);
        
        panel = new Category(null, "Archive", Style.COLOR_A());
        HBox hbox = new HBox();
        panel.add(hbox);
        for(int i = 0; i < ARCHIVE_SIZE; i++)
            {
            vbox = buildCandidate(NUM_CANDIDATES + i);
            hbox.add(vbox);
            }
        panel.add(hbox);
        toprow.addLast(panel);
        top.add(toprow);
        
        // Add Candidates

        panel =  new Category(null, "Candidates", Style.COLOR_B());

        hbox = new HBox();
                
        candidates = new VBox();
        for(int i = 0; i < NUM_CANDIDATES; i++)
            {
            vbox = buildCandidate(i);
            hbox.add(vbox);

            if (i % 8 == 7)
                {
                VBox vv = new VBox();
                if (i != 7)
                    vv.add(Strut.makeVerticalStrut(20));
                vv.add(hbox);
                hbox = new HBox();
                        
                if (i == 23)
                    extraCandidates1 = vv;
                else if (i == 31)
                    extraCandidates2 = vv;
                else
                    candidates.add(vv);
                }
            }

        panel.add(candidates, BorderLayout.WEST);
        
        HBox hb = new HBox();
        hb.add(panel);

        VBox currentAndNone = new VBox();

        // Add Current 
        panel = new Category(null, "Current", Style.COLOR_C());
        
        vbox = buildCandidate(NUM_MODELS - 1);
        HBox currentHBox = new HBox();
        currentHBox.add(vbox);
        panel.add(currentHBox);
        currentAndNone.add(panel);

        // Add None
        panel = new Category(null, "None", Style.COLOR_C());
        
        vbox = new VBox();
        Box b = new Box(BoxLayout.X_AXIS);
        b.setBackground(Style.BACKGROUND_COLOR());
        b.add(Box.createGlue());
        b.add(ratings[NUM_MODELS][0] = new JRadioButton("1"));
        ratings[NUM_MODELS][0].setFocusable(false);
        ratings[NUM_MODELS][0].setForeground(Style.TEXT_COLOR());
        ratings[NUM_MODELS][0].setFont(Style.SMALL_FONT());
        ratings[NUM_MODELS][0].putClientProperty("JComponent.sizeVariant", "small");
        ratings[NUM_MODELS][0].setOpaque(false);  // for windows
        b.add(Box.createGlue());
        vbox.add(b);
                        
        b = new Box(BoxLayout.X_AXIS);
        b.setBackground(Style.BACKGROUND_COLOR());
        b.add(Box.createGlue());
        b.add(ratings[NUM_MODELS][1] = new JRadioButton("2"));
        ratings[NUM_MODELS][1].setFocusable(false);
        ratings[NUM_MODELS][1].setForeground(Style.TEXT_COLOR());
        ratings[NUM_MODELS][1].setFont(Style.SMALL_FONT());
        ratings[NUM_MODELS][1].putClientProperty("JComponent.sizeVariant", "small");
        ratings[NUM_MODELS][1].setOpaque(false);  // for windows
        b.add(Box.createGlue());
        vbox.add(b);
                        
        b = new Box(BoxLayout.X_AXIS);
        b.setBackground(Style.BACKGROUND_COLOR());
        b.add(Box.createGlue());
        b.add(ratings[NUM_MODELS][2] = new JRadioButton("3"));
        ratings[NUM_MODELS][2].setFocusable(false);
        ratings[NUM_MODELS][2].setForeground(Style.TEXT_COLOR());
        ratings[NUM_MODELS][2].setFont(Style.SMALL_FONT());
        ratings[NUM_MODELS][2].putClientProperty("JComponent.sizeVariant", "small");
        ratings[NUM_MODELS][2].setOpaque(false);  // for windows
        b.add(Box.createGlue());
        vbox.add(b);
        VBox bar = new VBox();
        bar.addBottom(vbox);
        HBox foo = new HBox();
        foo.add(bar);
        foo.add(Strut.makeHorizontalStrut(40));
        panel.add(foo);
        
        currentAndNone.add(panel);
        
        hb.addLast(currentAndNone);

        top.add(hb);
        
        for(int i = 0; i < ratings.length; i++)
            {
            one.add(ratings[i][0]);
            two.add(ratings[i][1]);
            three.add(ratings[i][2]);
            }                
    
        for(int i = NUM_CANDIDATES; i < NUM_CANDIDATES + ARCHIVE_SIZE; i++)
            {
            currentModels[i] = (Model)(synth.getModel().clone());
            }
        currentModels[NUM_CANDIDATES + ARCHIVE_SIZE] = synth.getModel();

        setMethod(method.getSelectedIndex());
        setBigger(bb);
        }
        
   
    public void setBigger(boolean bigger)
        {
        candidates.remove(extraCandidates1);
        candidates.remove(extraCandidates2);
        if (bigger)
            {
            candidates.add(extraCandidates1);
            candidates.add(extraCandidates2);
            }
        candidates.revalidate();
        candidates.repaint();

        synth.setLastX("" + bigger, "HillClimbBigger", synth.getClass().getName());
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
                    {
                    plays[i].getButton().setForeground(new JButton().getForeground());
                    plays[i].getButton().setText(titleForButton(i));
                    }
                if (temporaryPlay >= 0)
                    {
                    plays[temporaryPlay].getButton().setForeground(Color.RED);
                    plays[temporaryPlay].getButton().setText("<HTML><B>" + titleForButton(temporaryPlay) + "</b></HTML>");
                    backup = synth.model;
                    synth.model = currentModels[temporaryPlay];
                    synth.sendAllParameters();
                    temporaryPlay = -1;
                    }
                else
                    {
                    currentPlay++;
                    if (currentPlay >= NUM_CANDIDATES ||
                        currentPlay >= 16 && !bigger.isSelected())
                        currentPlay = 0;
                    plays[currentPlay].getButton().setForeground(Color.RED);
                    plays[currentPlay].getButton().setText("<HTML><B>" + titleForButton(currentPlay) + "</b></HTML>");

                    // change the model, send all parameters, maybe play a note,
                    // and then restore the model.
                    backup = synth.model;
                    synth.model = currentModels[currentPlay];
                    synth.sendAllParameters();
                    }
                }
            }
        }

    void setMethod(int method)
        {
        boolean c = (method == 0 || method == 2);
        climb.getButton().setEnabled(c);
        constrict.getButton().setEnabled(!c);
        for(int i = 0; i < ratings.length; i++)
            for(int j = 0; j < ratings[i].length; j++)
                if (ratings[i][j] != null) ratings[i][j].setEnabled(c);
        for(int i = 0; i < selected.length; i++)
            if (selected[i] != null) selected[i].setEnabled(!c);
        hillClimbRate.setEnabled(c);
        constrictRate.setEnabled(!c);
        this.method.setSelectedIndex(method);
        outerBox.removeAll();
        if (method == 0 || method == 2)
            {
            outerBox.add(hillClimbBox);
            }
        else
            {
            outerBox.add(constrictBox);
            }
        outerBox.revalidate();
        repaint();
        }
      
    int lastPlayedSound()
        {
        if (temporaryPlay >=0)
            return temporaryPlay;
        else return currentPlay;
        } 
                
    public void postUpdateSound()
        {
        repaint();
        if (backup != null)
            synth.model = backup;
        backup = null;
        }

    boolean startedUp = false;
        
    public void startup()
        {
        if (!startedUp)
            {
            resetCurrentPlay();
            if (!synth.isSendingTestNotes() && synth.morphTestNotes)
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

    public void resetCurrentPlay()
        {
        currentPlay = NUM_CANDIDATES - 1;
        temporaryPlay = -1;
        }
                        
    Model copy(Model model)
        {
        if (model != null)
            return model.copy();
        else return null;
        }
        
    void again()
        {
        if (stackEmpty())
            {
            // uh oh...
            System.err.println("Warning (HillClimb): " + "Empty Stack");
            return;
            }
        else if (operation == OPERATION_SEED_FROM_PATCH)
            {
            initialize(synth.getModel(), operation);
            }
        else if (operation == OPERATION_SEED_FROM_NUDGE || operation == OPERATION_SEED_FROM_FOUR || operation == OPERATION_SEED_FROM_SIX)
            {
            initialize(null,  operation);
            }
        else if (operation == OPERATION_CLIMB)
            {
            pop();
            climb();
            }
        else if (operation == OPERATION_CONSTRICT)
            {
            pop();
            constrict();
            }
        else if (operation == OPERATION_CLIMB_NN)
            {
            pop();
            climbNN();
            }
        }
        
    void pop()
        {
        if (stackEmpty())
            {
            // uh oh...
            System.err.println("Warning (HillClimb) 2: " + "Empty Stack");
            return;
            }
        else if (stackInitial())
            {
            // do nothing
            ratings[NUM_MODELS][0].setSelected(true);
            ratings[NUM_MODELS][1].setSelected(true);
            ratings[NUM_MODELS][2].setSelected(true);
            }
        else
            {
            ratings[NUM_MODELS][0].setSelected(true);
            ratings[NUM_MODELS][1].setSelected(true);
            ratings[NUM_MODELS][2].setSelected(true);

            State state = popStack();
            operation = state.operation;
            System.arraycopy(state.children, 0, currentModels, 0, state.children.length);

            for(int j = 0; j < state.parentIndices.length; j++)
                {
                if (state.parentIndices[j] != -1)
                    {
                    //System.err.println("Setting " + state.parentIndices[j] + " to " + j);
                    ratings[state.parentIndices[j]][j].setSelected(true);
                    }
                }

            for(int j = 0; j < state.parentsSelected.length; j++)
                {
                selected[j].setSelected(state.parentsSelected[j]);
                }
                                
            iterations.setName("Iteration " + stack.size());
            repaint();
            }        
        }
    
    public void startHillClimbing()
        {
        for(int i = NUM_CANDIDATES; i < NUM_CANDIDATES + ARCHIVE_SIZE; i++)
            {
            currentModels[i] = (Model)(synth.getModel().clone());
            }       
        currentModels[NUM_CANDIDATES + ARCHIVE_SIZE] = synth.getModel();

        if (method.getSelectedIndex() == 2)
            {
            //System.out.println(method.getSelectedIndex());
            Random random = synth.random;
            String[] keys = synth.getMutationKeys();
            double weight = blank.getModel().get("hillclimbrate", 0) / 100.0;
            weight = weight * weight * weight; // make more sensitive at low end
            produceNN(random, keys, weight, synth.getModel());
            }
        else
            {
            initialize(synth.getModel(), OPERATION_SEED_FROM_PATCH);
            }
        }

    boolean[] getSelectedResults()
        {
        boolean[] sel = new boolean[NUM_CANDIDATES];
        for(int i = 0; i < sel.length; i++)
            {
            sel[i] = selected[i].isSelected();
            }
        return sel;
        }
        

    void initialize(Model seed, int operation)
        {
        // we need a model with NO callbacks
        stack.clear();
        this.operation = operation;
        Random random = synth.random;
        String[] keys = synth.getMutationKeys();
        
        switch(operation)
            {
            case OPERATION_SEED_FROM_PATCH:
                // Fall Thru
            case OPERATION_SEED_FROM_MORPH:
                {
                Model newSeed = seed.copy();                
                double weight = blank.getModel().get("hillclimbrate", 0) / 100.0;
                weight = weight * weight * weight;  // make more sensitive at low end
                int numMutations = 1;
                
                for(int i = 0; i < NUM_CANDIDATES; i++)
                    {
                    currentModels[i] = newSeed.copy();
                    for(int j = 0; j < numMutations; j++)
                        currentModels[i] = currentModels[i].mutate(random, keys, weight);
                    if (i % 4 == 3) numMutations++;
                    }

                for(int i = 0; i < selected.length; i++)
                    selected[i].setSelected(true);
                }
            break;
            case OPERATION_SEED_FROM_NUDGE:
                {
                double weight = blank.getModel().get("constrictrate", 0) / 100.0;
                for(int i = 0; i < 4; i++)
                    currentModels[i] = (Model)(synth.nudge[i].clone());
                int m = 4;
                for(int i = 0; i < 4; i++)
                    for(int j = 0; j < 4; j++)
                        {
                        if (j == i) continue;
                        currentModels[m++] = currentModels[i].copy().crossover(random, currentModels[j], keys, weight);
                        }
                // fill the next 16
                for(int i = 16; i < 32; i++)
                    {
                    // pick two parents, try to make them different from one another
                    int p1 = random.nextInt(16);
                    int p2 = 0;
                    for(int j = 0; j < 100; j++)
                        {
                        p2 = random.nextInt(16);
                        if (p2 != p1) break;
                        }
                    currentModels[i] = currentModels[p1].copy().crossover(random, currentModels[p1], keys, weight);
                    }
                }
            break;
            case OPERATION_SEED_FROM_FOUR:
                {
                double weight = blank.getModel().get("constrictrate", 0) / 100.0;
                int m = 4;
                for(int i = 0; i < 4; i++)
                    for(int j = 0; j < 4; j++)
                        {
                        if (j == i) continue;
                        currentModels[m++] = currentModels[i].copy().crossover(random, currentModels[j], keys, weight);
                        }
                // fill the next 16
                for(int i = 16; i < 32; i++)
                    {
                    // pick two parents, try to make them different from one another
                    int p1 = random.nextInt(16);
                    int p2 = 0;
                    for(int j = 0; j < 100; j++)
                        {
                        p2 = random.nextInt(16);
                        if (p2 != p1) break;
                        }
                    currentModels[i] = currentModels[p1].copy().crossover(random, currentModels[p1], keys, weight);
                    }
                }
            break;
            case OPERATION_SEED_FROM_SIX:
                {
                double weight = blank.getModel().get("constrictrate", 0) / 100.0;
                int m = 6;
                for(int i = 0; i < 6; i++)
                    for(int j = 0; j < 6; j++)
                        {
                        if (j == i) continue;
                        if (m >= 32) break;
                        currentModels[m++] = currentModels[i].copy().crossover(random, currentModels[j], keys, weight);
                        }
                }
            break;
            case OPERATION_SEED_FROM_LIBRARIAN:
                {
                double weight = blank.getModel().get("constrictrate", 0) / 100.0;
                int column = synth.librarian.getCurrentColumn();
                int row = synth.librarian.getCurrentRow();
                int len = synth.librarian.getCurrentLength();
                if (len == 0) return;
                
                for(int i = 0; i < Math.min(len, 32); i++)
                    {
                    currentModels[i] = (Model)(synth.librarian.getLibrary().getModel(column - 1, row + i).clone());
                    }
                    
                int m = len;
                for(int i = 0; i < 6; i++)
                    for(int j = 0; j < 6; j++)
                        {
                        if (j == i) continue;
                        if (m >= 32) break;
                        currentModels[m++] = currentModels[i].copy().crossover(random, currentModels[j], keys, weight);
                        }
                }
            break;
            }
                                
        pushStack(new int[] {-1, -1, -1}, new Model[] { seed, null, null }, getSelectedResults(), currentModels);
        iterations.setName("Iteration " + stack.size());
        ratings[NUM_MODELS][0].setSelected(true);
        ratings[NUM_MODELS][1].setSelected(true);
        ratings[NUM_MODELS][2].setSelected(true);
        repaint();
        }

    void shuffle(Random random, Model[] array, int start, int len)
        {
        for (int i = len - 1; i > 0; i--)
            {
            int index = random.nextInt(i + 1);
            Model temp = array[start + index];
            array[start + index] = array[start + i];
            array[start + i] = temp;
            }
        }

    void produceNN(Random random, String[] keys, double weight, Model a)
        {
        if(a.latentVector == null)
            {
            a.latentVector = ((ProvidesNN)synth).encode(a);
            }
        for(int i = 0; i < NUM_CANDIDATES; i++)
            {
            // A
            currentModels[i] = ((ProvidesNN)synth).decode(Network.shiftVectorGaussian(a.latentVector, random, weight * ProvidesNN.WEIGHT_SCALING));
            }
        shuffle(random, currentModels, 0, NUM_CANDIDATES);
        }

    void produceNN(Random random, String[] keys, double weight, Model a, Model b)
        {
        int numStages = NUM_CANDIDATES / STAGE_SIZE;
        if(a.latentVector == null)
            {
            a.latentVector = ((ProvidesNN)synth).encode(a);
            }
        if(b.latentVector == null)
            {
            b.latentVector = ((ProvidesNN)synth).encode(b);
            }
                
        for(int j = 0; j < numStages; j++)
            {
            for(int i = 0; i < STAGE_SIZE/2; i++)
                {
                // A
                currentModels[j*STAGE_SIZE + i] = ((ProvidesNN)synth).decode(Network.shiftVectorGaussian(a.latentVector, random, weight * ProvidesNN.WEIGHT_SCALING));
                }
            for(int i = STAGE_SIZE/2; i < 3*STAGE_SIZE/4; i++)
                {
                // B
                currentModels[j*STAGE_SIZE + i] = ((ProvidesNN)synth).decode(Network.shiftVectorGaussian(b.latentVector, random, weight * ProvidesNN.WEIGHT_SCALING));
                }
            for(int i = 3*STAGE_SIZE/4; i < STAGE_SIZE; i++)
                {
                // C
                currentModels[j*STAGE_SIZE + i] = ((ProvidesNN)synth).decode(Network.shiftVectorGaussian(Network.vectorMean(a.latentVector, b.latentVector), random, weight * ProvidesNN.WEIGHT_SCALING));
                }
            }
        shuffle(random, currentModels, 0, STAGE_SIZE);
        shuffle(random, currentModels, STAGE_SIZE, STAGE_SIZE);
        

        }

    void produceNN(Random random, String[] keys, double weight, Model a, Model b, Model c)
        {
        int numStages = NUM_CANDIDATES / STAGE_SIZE;
        if(a.latentVector == null)
            {
            a.latentVector = ((ProvidesNN)synth).encode(a);
            }
        if(b.latentVector == null)
            {
            b.latentVector = ((ProvidesNN)synth).encode(b);
            }
        if(c.latentVector == null)
            {
            c.latentVector = ((ProvidesNN)synth).encode(c);
            }
                
        for(int j = 0; j < numStages; j++)
            {
            // A
            currentModels[j*STAGE_SIZE + 0] = ((ProvidesNN)synth).decode(Network.shiftVectorGaussian(a.latentVector, random, weight * ProvidesNN.WEIGHT_SCALING));
            currentModels[j*STAGE_SIZE + 1] = ((ProvidesNN)synth).decode(Network.shiftVectorGaussian(a.latentVector, random, weight * ProvidesNN.WEIGHT_SCALING));
            currentModels[j*STAGE_SIZE + 2] = ((ProvidesNN)synth).decode(Network.shiftVectorGaussian(a.latentVector, random, weight * ProvidesNN.WEIGHT_SCALING));
            currentModels[j*STAGE_SIZE + 3] = ((ProvidesNN)synth).decode(Network.shiftVectorGaussian(a.latentVector, random, weight * ProvidesNN.WEIGHT_SCALING));

            // B
            currentModels[j*STAGE_SIZE + 4] = ((ProvidesNN)synth).decode(Network.shiftVectorGaussian(b.latentVector, random, weight * ProvidesNN.WEIGHT_SCALING));
            currentModels[j*STAGE_SIZE + 5] = ((ProvidesNN)synth).decode(Network.shiftVectorGaussian(b.latentVector, random, weight * ProvidesNN.WEIGHT_SCALING));
            currentModels[j*STAGE_SIZE + 6] = ((ProvidesNN)synth).decode(Network.shiftVectorGaussian(b.latentVector, random, weight * ProvidesNN.WEIGHT_SCALING));

            // C
            currentModels[j*STAGE_SIZE + 7] = ((ProvidesNN)synth).decode(Network.shiftVectorGaussian(c.latentVector, random, weight * ProvidesNN.WEIGHT_SCALING));
            currentModels[j*STAGE_SIZE + 8] = ((ProvidesNN)synth).decode(Network.shiftVectorGaussian(c.latentVector, random, weight * ProvidesNN.WEIGHT_SCALING));

            // Mean(A, B)
            currentModels[j*STAGE_SIZE + 9] = ((ProvidesNN)synth).decode(Network.shiftVectorGaussian(Network.vectorMean(a.latentVector, b.latentVector), random, weight * ProvidesNN.WEIGHT_SCALING));
            currentModels[j*STAGE_SIZE + 10] = ((ProvidesNN)synth).decode(Network.shiftVectorGaussian(Network.vectorMean(a.latentVector, b.latentVector), random, weight * ProvidesNN.WEIGHT_SCALING));

            // Mean(A, C)
            currentModels[j*STAGE_SIZE + 11] = ((ProvidesNN)synth).decode(Network.shiftVectorGaussian(Network.vectorMean(a.latentVector, c.latentVector), random, weight * ProvidesNN.WEIGHT_SCALING));
            currentModels[j*STAGE_SIZE + 12] = ((ProvidesNN)synth).decode(Network.shiftVectorGaussian(Network.vectorMean(a.latentVector, c.latentVector), random, weight * ProvidesNN.WEIGHT_SCALING));

            // Mean(B, C)
            currentModels[j*STAGE_SIZE + 13] = ((ProvidesNN)synth).decode(Network.shiftVectorGaussian(Network.vectorMean(b.latentVector, c.latentVector), random, weight * ProvidesNN.WEIGHT_SCALING));
            currentModels[j*STAGE_SIZE + 14] = ((ProvidesNN)synth).decode(Network.shiftVectorGaussian(Network.vectorMean(b.latentVector, c.latentVector), random, weight * ProvidesNN.WEIGHT_SCALING));

            // Mean(A,B,C)
            currentModels[j*STAGE_SIZE + 15] = ((ProvidesNN)synth).decode(Network.shiftVectorGaussian(Network.vectorMean(a.latentVector, b.latentVector, c.latentVector), random, weight * ProvidesNN.WEIGHT_SCALING));
            }
        shuffle(random, currentModels, 0, STAGE_SIZE);
        shuffle(random, currentModels, STAGE_SIZE, STAGE_SIZE);
        

        }

    public static final double MUTATION_WEIGHT = 1.0;
    
    void produce(Random random, String[] keys, double recombination, double weight, Model a, Model b, Model c, Model oldA)
        {
        int numStages = NUM_CANDIDATES / STAGE_SIZE;
        
        for(int i = 0; i < numStages; i++)
            {
            produce(random, keys, recombination, weight, a, b, c, oldA, i * STAGE_SIZE);
            }

        shuffle(random, currentModels, 0, STAGE_SIZE);
        shuffle(random, currentModels, STAGE_SIZE, STAGE_SIZE);
        }
        
    void produce(Random random, String[] keys, double recombination, double weight, Model a, Model b, Model c, Model oldA, int stage)
        {
        double mutationWeight = (stage/STAGE_SIZE + 1) * MUTATION_WEIGHT * weight;
        
        // A + B
        currentModels[stage + 0] = a.copy().recombine(random, b, keys, recombination).mutate(random, keys, mutationWeight);
        // A + C
        currentModels[stage + 1] = a.copy().recombine(random, c, keys, recombination).mutate(random, keys, mutationWeight);
        // A + (B + C)
        currentModels[stage + 2] = a.copy().recombine(random, b.copy().recombine(random, c, keys, recombination), keys, recombination).mutate(random, keys, mutationWeight);
        // A - B
        currentModels[stage + 3] = a.copy().opposite(random, b, keys, recombination, false).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
        // A - C
        currentModels[stage + 4] = a.copy().opposite(random, c, keys, recombination, false).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
        // A
        currentModels[stage + 5] = a.copy().mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
        // B
        currentModels[stage + 6] = b.copy().mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
        // C
        currentModels[stage + 7] = c.copy().mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);

        if ((stage + 8) < currentModels.length)
            {
            // A - Z
            currentModels[stage + 8] = a.copy().opposite(random, oldA, keys, recombination, false).mutate(random, keys, mutationWeight);
            // B - A
            currentModels[stage + 9] = b.copy().opposite(random, a, keys, recombination, false).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
            // C - A
            currentModels[stage + 10] = c.copy().opposite(random, a, keys, recombination, false).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
            // B - C
            currentModels[stage + 11] = b.copy().opposite(random, c, keys, recombination, false).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
            // C - B
            currentModels[stage + 12] = c.copy().opposite(random, b, keys, recombination, false).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
            // B - Z
            currentModels[stage + 13] = b.copy().opposite(random, oldA, keys, recombination, false).mutate(random, keys, mutationWeight);
            // C - Z
            currentModels[stage + 14] = c.copy().opposite(random, oldA, keys, recombination, false).mutate(random, keys, mutationWeight);
            // B + C
            currentModels[stage + 15] = b.copy().recombine(random, c, keys, recombination).mutate(random, keys, mutationWeight);
            }
        }
        
    void produce(Random random, String[] keys, double recombination, double weight, Model a, Model b, Model oldA)
        {
        int numStages = NUM_CANDIDATES / STAGE_SIZE;
        
        for(int i = 0; i < numStages; i++)
            {
            produce(random, keys, recombination, weight, a, b, oldA, i * STAGE_SIZE);
            }
        
        shuffle(random, currentModels, 0, STAGE_SIZE);
        shuffle(random, currentModels, STAGE_SIZE, STAGE_SIZE);
        }
        
    void produce(Random random, String[] keys, double recombination, double weight, Model a, Model b, Model oldA, int stage)
        {
        double mutationWeight = (stage/STAGE_SIZE + 1) * MUTATION_WEIGHT * weight;
        
        // A + B
        currentModels[stage + 0] = a.copy().recombine(random, b, keys, recombination).mutate(random, keys, mutationWeight);
        currentModels[stage + 1] = a.copy().recombine(random, b, keys, recombination).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
        currentModels[stage + 2] = a.copy().recombine(random, b, keys, recombination).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
        
        // A - B
        currentModels[stage + 3] = a.copy().opposite(random, b, keys, recombination, false).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
        currentModels[stage + 4] = a.copy().opposite(random, b, keys, recombination, false).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
        
        // B - A
        currentModels[stage + 5] = b.copy().opposite(random, a, keys, recombination, false).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
        currentModels[stage + 6] = b.copy().opposite(random, a, keys, recombination, false).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
        
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
        }
                
    void produce(Random random, String[] keys, double recombination, double weight, Model a, Model oldA)
        {
        int numStages = NUM_CANDIDATES / STAGE_SIZE;
        
        for(int i = 0; i < numStages; i++)
            {
            produce(random, keys, recombination, weight, a, oldA, i * STAGE_SIZE);
            }

        shuffle(random, currentModels, 0, STAGE_SIZE);
        shuffle(random, currentModels, STAGE_SIZE, STAGE_SIZE);
        }
        
    void produce(Random random, String[] keys, double recombination, double weight, Model a, Model oldA, int stage)
        {
        double mutationWeight = (stage/STAGE_SIZE + 1) * MUTATION_WEIGHT * weight;
        
        // A
        currentModels[stage + 0] = a.copy().mutate(random, keys, mutationWeight);
        currentModels[stage + 1] = a.copy().mutate(random, keys, mutationWeight);
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
            currentModels[stage + 15] = a.copy().opposite(random, oldA, keys, recombination, false).opposite(random, oldA, keys, recombination, false).mutate(random, keys, mutationWeight).mutate(random, keys, mutationWeight);
            }
        }
    
    
    void constrict()
        {
        int poolSize= (bigger.isSelected() ? NUM_CANDIDATES : STAGE_SIZE);  // that is, 32 vs 16
        Random random = synth.random;
        String[] keys = synth.getMutationKeys();
        double weight = blank.getModel().get("constrictrate", 0) / 100.0;
                
        weight = weight * weight * weight;  // make more sensitive at low end

        // Identify the individuals to replace and the ones to keep
        int numToReplace = 0;
        for(int i = 0; i < selected.length; i++)
            {
            if (!selected[i].isSelected()) numToReplace++;
            }
        int[] replace = new int[numToReplace];
        int[] keep = new int[poolSize - numToReplace];

        if (replace.length == 0 || keep.length == 0) return;

        int k = 0;
        int r = 0;
        for(int i = 0; i < poolSize; i++)
            {
            if (selected[i].isSelected()) 
                keep[k++] = i;
            else
                replace[r++] = i;
            }
                
        pushStack(new int[] { -1, -1, -1 }, new Model[] { currentModels[NUM_CANDIDATES - 1], null, null }, getSelectedResults(), currentModels);
        operation = OPERATION_CONSTRICT;
                
        // Now replace the individuals
        for(int i = 0; i < replace.length; i++)
            {
            // pick two parents, try to make them different from one another
            int p1 = random.nextInt(keep.length);
            int p2 = 0;
            for(int j = 0; j < 100; j++)
                {
                p2 = random.nextInt(keep.length);
                if (p2 != p1) break;
                }
                
            if (method.getSelectedIndex() == 1)
                {
                // our recombination works as follows: 50% of the time we'll do crossover with a 1/2 rate.  Otherwise we'll do it with a 3/4 rate.
                double rate = CONSTRICT_RECOMBINATION_RATE;
                // recombine
                if (random.nextBoolean())
                    rate = 0.5;
                currentModels[replace[i]] = currentModels[keep[p1]].copy().recombine(random, currentModels[keep[p2]], keys, rate).mutate(random, keys, weight);
                }
            }
                
        // Move the new ones to the beginning
        Model[] old = (Model[])(currentModels.clone());
        int count = 0;
        for(int i = 0; i < replace.length; i++)
            {
            currentModels[count++] = old[replace[i]];
            }
        for(int i = 0; i < keep.length; i++)
            {
            currentModels[count++] = old[keep[i]];
            }
        
        iterations.setName("Iteration " + stack.size());
        repaint();
        
        ratings[NUM_MODELS][0].setSelected(true);
        ratings[NUM_MODELS][1].setSelected(true);
        ratings[NUM_MODELS][2].setSelected(true);
        for(int i = 0; i < NUM_CANDIDATES; i++)
            selected[i].setSelected(true);
        }
        
        
    void climb()
        {
        Random random = synth.random;
        String[] keys = synth.getMutationKeys();
        double weight = blank.getModel().get("hillclimbrate", 0) / 100.0;
        
        weight = weight * weight * weight;  // make more sensitive at low end

        currentModels[NUM_MODELS - 1] = synth.getModel();
        
        // What were the best models before?
        int[] bestModels = new int[3];
        
        for(int j = 0; j < 3; j++)
            bestModels[j] = -1;
                        
        // load the best models
        for(int i = 0; i < NUM_MODELS; i++)
            {
            for(int j = 0; j < 3; j++)
                {
                if (ratings[i][j].isSelected())
                    bestModels[j] = i;
                }
            }
        
        // Compact
        if (bestModels[1] == -1)
            {
            bestModels[1] = bestModels[2];
            bestModels[2] = -1;
            }
        if (bestModels[0] == -1)
            {
            bestModels[0] = bestModels[1];
            bestModels[1] = bestModels[2];
            bestModels[2] = -1;
            }        
    
        Model oldA = topStack().parents[0];
        
        if (bestModels[0] == -1)
            {
            again();                                    // nothing was selected as good, so we just do a retry
            }
        else if (bestModels[1] == -1)
            {
            pushStack(bestModels, new Model[] { currentModels[bestModels[0]], null, null }, getSelectedResults(), currentModels);
            produce(random, keys, CLIMB_RECOMBINATION_RATE, weight, currentModels[bestModels[0]], oldA);
            operation = OPERATION_CLIMB;
            }
        else if (bestModels[2] == -1)
            {
            pushStack(bestModels, new Model[] { currentModels[bestModels[0]], currentModels[bestModels[1]], null }, getSelectedResults(), currentModels);
            produce(random, keys, CLIMB_RECOMBINATION_RATE, weight, currentModels[bestModels[0]], currentModels[bestModels[1]], oldA);
            operation = OPERATION_CLIMB;
            }
        else
            {
            pushStack(bestModels, new Model[] { currentModels[bestModels[0]], currentModels[bestModels[1]], currentModels[bestModels[2]] }, getSelectedResults(), currentModels);
            produce(random, keys, CLIMB_RECOMBINATION_RATE, weight, currentModels[bestModels[0]], currentModels[bestModels[1]], currentModels[bestModels[2]], oldA);
            operation = OPERATION_CLIMB;
            }
        
        iterations.setName("Iteration " + stack.size());
        repaint();
        
        ratings[NUM_MODELS][0].setSelected(true);
        ratings[NUM_MODELS][1].setSelected(true);
        ratings[NUM_MODELS][2].setSelected(true);
        for(int i = 0; i < NUM_CANDIDATES; i++)
            selected[i].setSelected(true);
        }

    void climbNN()
        {
        Random random = synth.random;
        String[] keys = synth.getMutationKeys();
        double weight = blank.getModel().get("hillclimbrate", 0) / 100.0;
        
        weight = weight * weight * weight;  // make more sensitive at low end

        currentModels[NUM_MODELS - 1] = synth.getModel();
        
        // What were the best models before?
        int[] bestModels = new int[3];
        
        for(int j = 0; j < 3; j++)
            bestModels[j] = -1;
                        
        // load the best models
        for(int i = 0; i < NUM_MODELS; i++)
            {
            for(int j = 0; j < 3; j++)
                {
                if (ratings[i][j].isSelected())
                    bestModels[j] = i;
                }
            }
        
        // Compact
        if (bestModels[1] == -1)
            {
            bestModels[1] = bestModels[2];
            bestModels[2] = -1;
            }
        if (bestModels[0] == -1)
            {
            bestModels[0] = bestModels[1];
            bestModels[1] = bestModels[2];
            bestModels[2] = -1;
            }        
    
        
        if (bestModels[0] == -1)
            {
            again();                                    // nothing was selected as good, so we just do a retry
            }
        else if (bestModels[1] == -1)
            {
            pushStack(bestModels, new Model[] { currentModels[bestModels[0]], null, null }, getSelectedResults(), currentModels);
            produceNN(random, keys, weight, currentModels[bestModels[0]]);
            operation = OPERATION_CLIMB_NN;
            }
        else if (bestModels[2] == -1)
            {
            pushStack(bestModels, new Model[] { currentModels[bestModels[0]], currentModels[bestModels[1]], null }, getSelectedResults(), currentModels);
            produceNN(random, keys, weight, currentModels[bestModels[0]], currentModels[bestModels[1]]);
            operation = OPERATION_CLIMB_NN;
            }
        else
            {
            pushStack(bestModels, new Model[] { currentModels[bestModels[0]], currentModels[bestModels[1]], currentModels[bestModels[2]] }, getSelectedResults(), currentModels);
            produceNN(random, keys, weight, currentModels[bestModels[0]], currentModels[bestModels[1]], currentModels[bestModels[2]]);
            operation = OPERATION_CLIMB_NN;
            }
        
        iterations.setName("Iteration " + stack.size());
        repaint();
        
        ratings[NUM_MODELS][0].setSelected(true);
        ratings[NUM_MODELS][1].setSelected(true);
        ratings[NUM_MODELS][2].setSelected(true);
        for(int i = 0; i < NUM_CANDIDATES; i++)
            selected[i].setSelected(true);
        }

    }
        
        
