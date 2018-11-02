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
    // OPERATIONS
    public static final int OPERATION_SEED_FROM_PATCH = 0;
    public static final int OPERATION_SEED_FROM_NUDGE = 1;
    public static final int OPERATION_SEED_FROM_FOUR = 2;
    public static final int OPERATION_SEED_FROM_SIX = 3;
    public static final int OPERATION_CLIMB = 4;
    public static final int OPERATION_CONSTRICT = 5;

    /// HILL CLIMBING STACK
    
    class State
        {
        Model[] parents;
        int[] parentIndices;
        boolean[] parentsSelected;
        Model[] children;
        int operation;
        }
    ArrayList stack = new ArrayList();
        
        
    /// NUMBER OF CANDIDATE SOLUTIONS
        
    public static final int NUM_CANDIDATES = 32;
    public static final int STAGE_SIZE = 16;
    public static final int ARCHIVE_SIZE = 6;
    // There are more models than candidates: #17 is the current Model
    public static final int NUM_MODELS = NUM_CANDIDATES + ARCHIVE_SIZE + 1;
    
    // models currently being played and displayed
    Model[] currentModels = new Model[NUM_MODELS];

    public int operation;

    JRadioButton[][] ratings = new JRadioButton[NUM_MODELS + 1][3];
    JCheckBox[] selected = new JCheckBox[NUM_CANDIDATES];
    PushButton[] plays = new PushButton[NUM_MODELS];
    public static final int INITIAL_MUTATION_RATE = 37;         // roughly 5 when we do weight^3
    public static final int STANDARD_RECOMBINATION_RATE = 75;
    Blank blank;
    Category iterations;
    int currentPlay = 0;
    int temporaryPlay = -1;
    HBox nudgeBox;
    PushButton retry;
    PushButton climb;
    PushButton reset;
    PushButton back;
    PushButton constrict;
    JCheckBox bigger;
    LabelledDial mutationRate;
    LabelledDial recombinationRate;
    
    JComboBox method = new JComboBox(new String[] { "Hill-Climber", "Constrictor", "Smooth Constrictor" });
    
    VBox candidates;
    VBox extraCandidates1;
    VBox extraCandidates2;
        
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
    
    /*
      void seed(int type)
      {
      Random random = synth.random;
      String[] keys = synth.getMutationKeys();
      double weight = blank.getModel().get("recombinationrate", 0) / 100.0;
                
      switch(val)
      {
      case 0:
      {
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
      operation = OPERATION_SEED_FROM_NUDGE;
      }
      break;
      case 1:
      {
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
      operation = OPERATION_SEED_FROM_FOUR;
      }
      break;
      case 2:
      {
      int m = 6;
      for(int i = 0; i < 6; i++)
      for(int j = 0; j < 6; j++)
      {
      if (j == i) continue;
      if (m >= 32) break;
      currentModels[m++] = currentModels[i].copy().crossover(random, currentModels[j], keys, weight);
      }
      operation = OPERATION_SEED_FROM_SIX;
      }
      break;
      }
      }
    */
    
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
                    temporaryPlay = i;
                    }
                else
                    {
                    for(int j = 0; j < NUM_MODELS; j++)       
                        {
                        plays[j].getButton().setForeground(new JButton().getForeground());
                        plays[j].getButton().setText(titleForButton(j));
                        }
                    plays[_i].getButton().setForeground(Color.RED);
                    plays[_i].getButton().setText("<HTML><B>" + titleForButton(i) + "</b></HTML>");

                    // change the model, send all parameters, maybe play a note,
                    // and then restore the model.
                    Model backup = synth.model;
                    synth.model = currentModels[_i];
                    synth.sendAllParameters();
                    synth.doSendTestNote(false);
                    synth.model = backup;
                    temporaryPlay = i;
                    }

                }
            };
        plays[i].getButton().setFocusable(false);
        vbox.add(plays[i]);


/*
  Box b = new Box(BoxLayout.X_AXIS);
  b.setBackground(Style.BACKGROUND_COLOR());
  b.add(Box.createGlue());
  b.add(ratings[i][0] = new JRadioButton("1"));
  ratings[i][0].setFocusable(false);
  ratings[i][0].setForeground(Style.TEXT_COLOR());
  ratings[i][0].setFont(Style.SMALL_FONT());
  ratings[i][0].putClientProperty("JComponent.sizeVariant", "small");
  ratings[i][0].setHorizontalTextPosition(SwingConstants.CENTER);
  ratings[i][0].setVerticalTextPosition(JRadioButton.TOP);

  b.add(ratings[i][1] = new JRadioButton("2"));
  ratings[i][1].setFocusable(false);
  ratings[i][1].setForeground(Style.TEXT_COLOR());
  ratings[i][1].setFont(Style.SMALL_FONT());
  ratings[i][1].putClientProperty("JComponent.sizeVariant", "small");
  ratings[i][1].setHorizontalTextPosition(SwingConstants.CENTER);
  ratings[i][1].setVerticalTextPosition(JRadioButton.TOP);

  b.add(ratings[i][2] = new JRadioButton("3"));
  ratings[i][2].setFocusable(false);
  ratings[i][2].setForeground(Style.TEXT_COLOR());
  ratings[i][2].setFont(Style.SMALL_FONT());
  ratings[i][2].putClientProperty("JComponent.sizeVariant", "small");
  ratings[i][2].setHorizontalTextPosition(SwingConstants.CENTER);
  ratings[i][2].setVerticalTextPosition(JRadioButton.TOP);
  b.add(Box.createGlue());
  vbox.add(b);
*/            

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
        
           
        JMenuItem[] doItems = new JMenuItem[13];
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
        if (_i == NUM_CANDIDATES + ARCHIVE_SIZE)
            doItems[0].setEnabled(false);

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
        if (_i == NUM_CANDIDATES + ARCHIVE_SIZE)
            doItems[3].setEnabled(false);
            
        doItems[4] = null;
            
        doItems[5] = new JMenuItem("Nudge Candidates to Me");
        doItems[5].addActionListener(new ActionListener()
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

        doItems[6] = null;

        doItems[7] = new JMenuItem("Archive to q");
        doItems[7].addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                currentModels[NUM_CANDIDATES + 0] = (Model)(currentModels[_i].clone());
                }
            });

        doItems[8] = new JMenuItem("Archive to r");
        doItems[8].addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                currentModels[NUM_CANDIDATES + 1] = (Model)(currentModels[_i].clone());
                }
            });

        doItems[9] = new JMenuItem("Archive to s");
        doItems[9].addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                currentModels[NUM_CANDIDATES + 2] = (Model)(currentModels[_i].clone());
                }
            });

        doItems[10] = new JMenuItem("Archive to t");
        doItems[10].addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                currentModels[NUM_CANDIDATES + 3] = (Model)(currentModels[_i].clone());
                }
            });
                
        doItems[11] = new JMenuItem("Archive to u");
        doItems[11].addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                currentModels[NUM_CANDIDATES + 4] = (Model)(currentModels[_i].clone());
                }
            });
                
        doItems[12] = new JMenuItem("Archive to v");
        doItems[12].addActionListener(new ActionListener()
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
        
        
    public HillClimb(Synth synth)
        {
        super(synth);
        
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
           

        climb = new PushButton("Climb")
            {
            public void perform()
                {
                climb(true);
                resetCurrentPlay();
                }
            };
        climb.getButton().setPreferredSize(back.getButton().getPreferredSize());
        climb.getButton().setFocusable(false);
                
        vbox.add(climb);

        String s = synth.getLastX("HillClimbMutationRate", null);
        mutationRate = new LabelledDial("Rate", blank, "mutationrate", Style.COLOR_GLOBAL(), 0, 100)
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
                synth.setLastX("" + model.get(key), "HillClimbMutationRate", null);
                }
            };
        
        int v = INITIAL_MUTATION_RATE;
        if (s != null)
            try { v = Integer.parseInt(s); } catch (Exception e) { e.printStackTrace(); }
        if (v < 0 || v > 100) v = INITIAL_MUTATION_RATE;
        mutationRate.setState(v);
        
        blank.getModel().set("mutationrate", v);
        vbox.add(mutationRate);
                
        iterationsBox.add(vbox);
        
        vbox = new VBox();
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
        vbox = new VBox();
        vbox.add(constrict);

        s = synth.getLastX("HillClimbRecombinationRate", null);

        recombinationRate = new LabelledDial("Rate", blank, "recombinationrate", Style.COLOR_GLOBAL(), 0, 100)
            {
            public String map(int val)
                {
                if (val == 100) 
                    return "100.0";
                else if (val >= 10.0)
                    return String.format("%.2f", (double)val);
                else
                    return String.format("%.3f", (double)val);
                }
            
            public void update(String key, Model model)
                {
                super.update(key, model);
                synth.setLastX("" + model.get(key), "HillClimbRecombinationRate", null);
                }
            };

        v = STANDARD_RECOMBINATION_RATE;
        if (s != null)
            try { v = Integer.parseInt(s); } catch (Exception e) { e.printStackTrace(); }
        if (v < 0 || v > 100) v = STANDARD_RECOMBINATION_RATE;
        recombinationRate.setState(v);
        
        blank.getModel().set("recombinationrate", v);
        vbox.add(recombinationRate);
 
        iterationsBox.add(vbox);


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
              
        /*
          reset = new PushButton("Reset")
          {
          public void perform()
          {
          if (synth.showSimpleConfirm("Reset", "Are you sure you want to reset the Hill-Climber?"))
          {
          initialize((Model)(synth.getModel().clone()), OPERATION_SEED_FROM_PATCH);
          resetCurrentPlay();
          }
          }
          };
          reset.getButton().setPreferredSize(back.getButton().getPreferredSize());
          reset.getButton().setFocusable(false);
          vbox.add(reset);
        */

        reset = new PushButton("Reset...",
            new String[] { "From Original Patch",
                           "From Nudge Targets", 
                           "From First Four Candidates",
                           "From First Six Candidates" })
            {
            public void perform(int val)
                {
                initialize(val == OPERATION_SEED_FROM_PATCH ? synth.getModel() : null, val);
                resetCurrentPlay();
                }
            };
        reset.getButton().setPreferredSize(back.getButton().getPreferredSize());
        reset.getButton().setFocusable(false);
        vbox.add(reset);

        iterationsBox.add(vbox);
        
        panel.add(iterationsBox, BorderLayout.CENTER);

        s = synth.getLastX("HillClimbMethod", null);
        method.setFont(Style.SMALL_FONT());
        method.putClientProperty("JComponent.sizeVariant", "small");
        method.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                int m = method.getSelectedIndex();
                synth.setLastX("" + m, "HillClimbMethod", null);
                setMethod(m);
                }
            });
        
        v = 0;
        if (s != null)
            try { v = Integer.parseInt(s); } catch (Exception e) { e.printStackTrace(); }
        if (v < 0 || v > 2) v = 0;
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
        
        s = synth.getLastX("HillClimbBigger", null);

        bigger.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                candidates.remove(extraCandidates1);
                candidates.remove(extraCandidates2);
                if (bigger.isSelected())
                    {
                    candidates.add(extraCandidates1);
                    candidates.add(extraCandidates2);
                    }
                candidates.revalidate();
                candidates.repaint();

                synth.setLastX("" + bigger.isSelected(), "HillClimbBigger", null);
                }
            });
            
        boolean bb = false;
        if (s != null)
            try { bb = (s.equals("true")); } catch (Exception e) { e.printStackTrace(); }

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
        boolean c = (method == 0);
        climb.getButton().setEnabled(c);
        constrict.getButton().setEnabled(!c);
        for(int i = 0; i < ratings.length; i++)
            for(int j = 0; j < ratings[i].length; j++)
                if (ratings[i][j] != null) ratings[i][j].setEnabled(c);
        for(int i = 0; i < selected.length; i++)
            if (selected[i] != null) selected[i].setEnabled(!c);
        mutationRate.setEnabled(c);
        recombinationRate.setEnabled(!c);
        this.method.setSelectedIndex(method);
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
            synth.doSendAllSoundsOff(false);
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
            State state = popStack();
            System.arraycopy(state.children, 0, currentModels, 0, state.children.length);

            ratings[NUM_MODELS][0].setSelected(true);
            ratings[NUM_MODELS][1].setSelected(true);
            ratings[NUM_MODELS][2].setSelected(true);

            for(int j = 0; j < state.parentIndices.length; j++)
                {
                if (state.parentIndices[j] != -1)
                    ratings[state.parentIndices[j]][j].setSelected(true);
                }
                
            for(int j = 0; j < state.parentsSelected.length; j++)
                {
                selected[j].setSelected(state.parentsSelected[j]);
                }
        
            climb(false);
            }
        else if (operation == OPERATION_CONSTRICT)
            {
            State state = popStack();
            System.arraycopy(state.children, 0, currentModels, 0, state.children.length);

            ratings[NUM_MODELS][0].setSelected(true);
            ratings[NUM_MODELS][1].setSelected(true);
            ratings[NUM_MODELS][2].setSelected(true);
            constrict();
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
            }
        else
            {
            State state = popStack();
            operation = state.operation;
            System.arraycopy(state.children, 0, currentModels, 0, state.children.length);

            for(int j = 0; j < state.parentIndices.length; j++)
                {
                if (state.parentIndices[j] != -1)
                    ratings[state.parentIndices[j]][j].setSelected(true);
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
        
        initialize(synth.getModel(),  OPERATION_SEED_FROM_PATCH);
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
                {
                Model newSeed = seed.copy();                
                double weight = blank.getModel().get("mutationrate", 0) / 100.0;
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
                double weight = blank.getModel().get("recombinationrate", 0) / 100.0;
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
                double weight = blank.getModel().get("recombinationrate", 0) / 100.0;
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
                double weight = blank.getModel().get("recombinationrate", 0) / 100.0;
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
        double weight = blank.getModel().get("recombinationrate", 0) / 100.0;
                
                        
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
                
            if (method.getSelectedIndex() == 2)
                {
                // recombine
                currentModels[replace[i]] = currentModels[keep[p1]].copy().recombine(random, currentModels[keep[p2]], keys, weight);
                }
            else
                {
                // cross over
                currentModels[replace[i]] = currentModels[keep[p1]].copy().crossover(random, currentModels[keep[p2]], keys, weight);
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
        
        
    void climb(boolean determineBest)
        {
        Random random = synth.random;
        String[] keys = synth.getMutationKeys();
        double weight = blank.getModel().get("mutationrate", 0) / 100.0;
        
        weight = weight * weight * weight;  // make more sensitive at low end

        int[] bestModels = new int[3];
        
        currentModels[NUM_MODELS - 1] = synth.getModel();
        
        if (determineBest)
            {
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
            }
        
        if (bestModels[0] == -1)
            {
            bestModels[0] = bestModels[1];
            bestModels[1] = bestModels[2];
            bestModels[2] = -1;
            }
        if (bestModels[0] == -1)
            {
            bestModels[0] = bestModels[1];
            bestModels[1] = bestModels[2];
            bestModels[2] = -1;
            }
        if (bestModels[1] == -1)
            {
            bestModels[1] = bestModels[2];
            bestModels[2] = -1;
            }
        
        boolean zeroModels = false;     
        Model oldA = topStack().parents[0];
        
        if (bestModels[0] == -1)
            {
            again();
            zeroModels = true;
            }
        else if (bestModels[1] == -1)
            {
            pushStack(bestModels, new Model[] { currentModels[bestModels[0]], null, null }, getSelectedResults(), currentModels);
            produce(random, keys, STANDARD_RECOMBINATION_RATE / 100.0, weight, currentModels[bestModels[0]], oldA);
            operation = OPERATION_CLIMB;
            }
        else if (bestModels[2] == -1)
            {
            pushStack(bestModels, new Model[] { currentModels[bestModels[0]], currentModels[bestModels[1]], null }, getSelectedResults(), currentModels);
            produce(random, keys, STANDARD_RECOMBINATION_RATE / 100.0, weight, currentModels[bestModels[0]], currentModels[bestModels[1]], oldA);
            operation = OPERATION_CLIMB;
            }
        else
            {
            pushStack(bestModels, new Model[] { currentModels[bestModels[0]], currentModels[bestModels[1]], currentModels[bestModels[2]] }, getSelectedResults(), currentModels);
            produce(random, keys, STANDARD_RECOMBINATION_RATE / 100.0, weight, currentModels[bestModels[0]], currentModels[bestModels[1]], currentModels[bestModels[2]], oldA);
            operation = OPERATION_CLIMB;
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
        
        
