
/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn;

import edisyn.gui.*;
import edisyn.synth.*;
import edisyn.util.*;
import edisyn.nn.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.border.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.*;
import java.lang.reflect.*;
import javax.sound.midi.*;
import java.io.*;


/**** 
      Abstract super class of synth editor panes.  To implement an editor, you'll need
      to write several methods.  Note that one method you need to write is <i>static</i>,
      but you'll still need to write a static version for your particular subclass:
      <tt> public static boolean recognize(byte[] data)

      <p>This class contains a number of constants which dictate the look and feel of the
      system, as well as the file associated with your synth, the underlying parameter model,
      the MIDI transmitter and receiver, the menu facility, and other stuff you'll need to
      build your editor.

      @author Sean Luke
*/

public abstract class Synth extends JComponent implements Updatable
    {
    // Counter for total number of open windows.  When this drops to zero,
    // the program will quit automatically.
    static int numOpenWindows = 0;
    /** The model proper */
    protected Model model;
    /** Our own private random number generator */
    protected Random random;
    // The file associated with the synth
    File file;
    // Any file the synth may have been loaded from which isn't the primary file (such as a bank or bulk file)
    File auxFile;
    /** Returns the current file associated with this synth. */
    public File getFile() { return file; }
    
    /** The tab pane at the top of the Synth panel. */
    public JTabbedPane tabs = new JTabbedPane();

    /** The largest permitted sysex file. */
    public static final int MAX_FILE_LENGTH = 512 * 1024;        // so we don't go on forever

    /** Used in emitAll(key...) to indicate that emitAll(...) is being used to send one of a stream of all the parameters. */
    public static final int STATUS_SENDING_ALL_PARAMETERS = 0;
    /** Used in emitAll(key...) to indicate that emitAll(...) is being used to send just a single parameter, and is not part of a stream of parameters. */
    public static final int STATUS_UPDATING_ONE_PARAMETER = 1;

    /** The Synth menu bar */
    public JMenuBar menubar;
    /** The "Send to Patch..." menu */
    public JMenuItem transmitTo;
    /** The "Send to Current Patch" menu */
    public JMenuItem transmitCurrent;
    /** The "Write to Patch..." menu */
    public JMenuItem writeTo;
    /** The "Undo" menu */
    public JMenuItem undoMenu;
    /** The "Redo" menu */
    public JMenuItem redoMenu;
    /** The "Request Current Patch" menu */
    public JMenuItem receiveCurrent;
    /** The "Request Patch..." menu */
    public JMenuItem receivePatch;
    /** The "Sends Real Time Changes" menu */
    public JMenuItem receiveNextPatch;
    /** The "Request Next Patch" menu */
    public JCheckBoxMenuItem transmitParameters;
    /** The "Request Merge" menu */
    public JMenu merge;
    /** The "Edit Mutation Parameters" menu */
    public JMenuItem editMutationMenu;
    /** The "Use Parameters for Nudge/Merge" menu, currently disabled */
    public JCheckBoxMenuItem recombinationToggle;
    /** The "Hill-Climb" menu */
    public JMenuItem hillClimbMenu;
    /** The "Morph" menu */
    public JMenuItem morphMenu;
    /** The "Send Test Notes" menu */
    public JCheckBoxMenuItem testNotes;
    /** The "Repeatedly Send Current Patch" menu */
    public JCheckBoxMenuItem repeatCurrentPatch;
    /** The Hill-Climbing tab pane */
    public JComponent hillClimbPane;
    /** The Morphing pane */
    public JComponent morphPane;
    /** The "Batch Download..." pane */
    public JMenuItem getAll;
    /** The "Report Next Controller MIDI" menu */
    public JMenuItem testIncomingController;
    /** The "Report Next Synth MIDI" menu */
    public JMenuItem testIncomingSynth;
    /** The "Send All Sounds Off Before Note On" menu */
    public JCheckBoxMenuItem sendsAllSoundsOffBetweenNotesMenu;
    /** The "Keep Next Popup Open" menu */
    public JCheckBoxMenuItem persistentChooserMenu;
    /** The "Switching Menus Sends All Sounds Off" menu */
    public JCheckBoxMenuItem clearNotesMenu;
    /** The "Launch with Last Editor" menu */
    public JCheckBoxMenuItem launchMenu;
    /** The "Blend" menu */
    public JMenu blend;
    /** NN randomization checkbox, only appears if the Synth providesNN (such as a DX7) */
    public JCheckBoxMenuItem nnRandomize;
        
    // The four nudge models 
    Model[] nudge = new Model[4];
    // The 8 nudge-towards menus
    JMenuItem[] nudgeTowards = new JMenuItem[8];
    // The favorites
    Favorites favorites = new Favorites();
    // The undo
    protected Undo undo = new Undo(this);
    
    public Undo getUndo() { return undo; }
    
    // The current copy preamble    
    String copyPreamble;
    public String getCopyPreamble() { return copyPreamble; }
    public void setCopyPreamble(String preamble) { copyPreamble = preamble; }

    // The current copy type    
    String copyType;
    public String getCopyType() { return copyType; }
    public void setCopyType(String type) { copyType = type; }
    
    // The current copy keys    
    ArrayList copyKeys;
    public ArrayList getCopyKeys() { return copyKeys; }
    public void setCopyKeys(ArrayList keys) { copyKeys = keys; }

    /** Returns the model associated with this editor. */
    public Model getModel() { return model; }
    
    boolean testIncomingControllerMIDI;
    boolean testIncomingSynthMIDI;
    
    static boolean clearNotes;


    boolean parsingForMerge = false;
    /** Indicates that we are a sacrificial synth which is parsing an incoming sysex dump and then will be merged with the main synth. */
    public boolean isParsingForMerge() { return parsingForMerge; }

    // will the next load be a merge?  If 0, we're not merging.  Else it's the merge probability.
    private double merging = 0.0;
    
    /** Returns whether we're not merging. */
    public boolean isMerging() { return merging != 0.0; }

    /** Returns the current merge probability.  If the value is 0.0,
        then merging is not occurring. */
    public double getMergeProbability()
        {
        return merging;
        }

    /** Returns the current merge probability.  If the value is 0.0,
        then merging is not occurring. */
    public void setMergeProbability(double val)
        {
        if (val < 0) val = 0; 
        if (val > 1) val = 1;
        merging = val;
        }

    public JMenuItem copyTab = new JMenuItem("Copy Tab");
    public JMenuItem pasteTab = new JMenuItem("Paste Tab");
    public JMenuItem copyMutableTab = new JMenuItem("Copy Tab (Mutation Parameters Only)");
    public JMenuItem pasteMutableTab = new JMenuItem("Paste Tab (Mutation Parameters Only)");
    public JMenuItem resetTab = new JMenuItem("Reset Tab");
    
    //boolean useMapForRecombination = true;
    boolean showingMutation = false;
    /** Returns true if we're currently trying to merge with another patch.  */
    public boolean isShowingMutation() { return showingMutation; }
    public void setShowingMutation(boolean val) 
        { 
        showingMutation = val; 
        if (val == true) 
            setLearningCC(false); 
        if (isShowingMutation())
            {
            editMutationMenu.setText("Stop Editing Mutation Parameters");
            }
        else
            {
            editMutationMenu.setText("Edit Mutation Parameters");
            }
        updateTitle(); 
        repaint(); 
        }
    public MutationMap mutationMap;
    public String[] getMutationKeys()
        {
        String[] keys = getModel().getKeys();
        ArrayList list = new ArrayList();
        for(int i = 0; i < keys.length; i++)
            {
            if (mutationMap.isFree(keys[i]))
                list.add(keys[i]);
            }
        return (String[])(list.toArray(new String[0]));
        }

    /** Override this to return your own customized Model subclass if you have some need to do so,
        typically in order to override the methods reviseMutatedValue(...)....
        Note that you should not build this with an anonymous class because
        it doesn't have a constructor and can't be build via model.newModel().
        Instead, make a PUBLIC inner class, ideally a static one, and instantiate that. */
    public Model buildModel() { return new Model(); }









    /////// CREATION AND CONSTRUCTION

    public Synth() 
        {
        model = buildModel();
        model.register(Model.ALL_KEYS, this);
        model.setUndoListener(undo);
        ccmap = new CCMap(Prefs.getAppPreferences(getSynthNameLocal(), "CCKey"),
            Prefs.getAppPreferences(getSynthNameLocal(), "CCType"));
        mutationMap = new MutationMap(Prefs.getAppPreferences(getSynthNameLocal(), "Mutation"));

        undo.setWillPush(false);  // instantiate undoes this
        random = new Random(System.currentTimeMillis());
        
        clearNotes = getLastXAsBoolean("SwitchingSendsAllSoundsOff", null, true, false);        
        perChannelCCs = ("" + getLastX("PerChannelCC", getSynthNameLocal(), false)).equalsIgnoreCase("true");                  
        }
        
        
    /** Builds a synth of the given CLASSNAME.
        If THROWAWAY is true, then the window won't be sprouted and MIDI won't be set up.
        If SETUPMIDI is false, then MIDI won't be set up.  The TUPLE provides the default
        MIDI devices. */
        
    public static Synth instantiate(String classname, boolean throwaway, boolean setupMIDI, Midi.Tuple tuple)
        {
        try
            {
            return instantiate(Class.forName(classname), throwaway, setupMIDI, tuple);
            }
        catch (Exception ex)
            {
            JOptionPane.showMessageDialog(null, "An error occurred while creating the synth editor for \n" + classname, "Creation Error", JOptionPane.ERROR_MESSAGE);
            Synth.handleException(ex);
            return null;
            }
        }
    

    /** Builds a synth of the given CLASS.
        If THROWAWAY is true, then the window won't be sprouted and MIDI won't be set up.
        If SETUPMIDI is false, then MIDI won't be set up.  The TUPLE provides the default
        MIDI devices. */
        
    public static Synth instantiate(Class _class, boolean throwaway, boolean setupMIDI, Midi.Tuple tuple)
        {
        try
            {
            Synth synth = (Synth)(_class.newInstance()); // this will setWillPush(false);
            if (!throwaway)
                {
                synth.sprout();
                final JFrame frame = ((JFrame)(SwingUtilities.getRoot(synth)));
                setLastSynth(_class.getName());

                if (Style.isMac())
                    {
                    // When you pack a large frame in OS X, it appears to trigger a bug where if the
                    // Synth's frame is taller than the maximum window bounds, minus some slop, instead of 
                    // setting it to that size, it'll minimize it to zero.  :-(  This workaround seems to do
                    // the job.  The exact minimal slop value is 11: I'm setting it to 20 for good measure.
                                        
                    Rectangle size = frame.getBounds();
                    Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
                    if (size.width > bounds.width)
                        {
                        size.width = bounds.width;
                        frame.setBounds(size);
                        }
                    if (size.height > bounds.height - 20)
                        {
                        size.height = bounds.height - 20;
                        frame.setBounds(size);
                        }
                    }

                frame.setVisible(true);
                if (setupMIDI) 
                    synth.setupMIDI("Choose MIDI devices to send to and receive from.", tuple);

                // we call this here even though it's already been called as a result of frame.setVisible(true)
                // because it's *after* setupMidi(...) and so it gives synths a chance to send
                // a MIDI sysex message in response to the window becoming front.
                if (clearNotes && synth.sendAllSoundsOffWhenWindowChanges())
                    {
                    synth.sendAllSoundsOff(); // not doSendAllSoundsOff(false) because we don't want to turn off the test notes
                    }
                synth.windowBecameFront();                              
                }
            synth.undo.setWillPush(true);
            return synth;
            }
        catch (Exception e2)
            {
            Synth.handleException(e2);
            JOptionPane.showMessageDialog(null, "An error occurred while creating the synth editor for \n" + _class.getSimpleName(), "Creation Error", JOptionPane.ERROR_MESSAGE);
            }
        return null;
        }  
        
        
           





    // CC HANDLING AND MAPPING
        

    // Our CC Map
    CCMap ccmap;
        
    // Are we passing through CCs?        
    boolean passThroughCC;
    boolean passThroughController;
    Object passThroughCCLock = new Object[0];

    // Are we doing per-channel CCs or per-Panel CCs?
    boolean perChannelCCs = false;
    
    // Are we presently in learning mode?
    boolean learning = false;
    // If we're in learning mode, what learning type are we?  One of CCMap.TYPE_ABSOLUTE_CC, 
    // or CCMap.TYPE_RELATIVE_CC_64, or CCMap.TYPE_RELATIVE_CC_0.  
    int learningType;
    
    // MenuItem for Absolute CC, so we can enable/diable it
    JMenuItem learningMenuItem;
    // MenuItem for RelativeCC0, so we can enable/diable it
    //JMenuItem learningMenuItem0;
    // MenuItem for RelativeCC64, so we can enable/diable it
    JMenuItem learningMenuItem64;
    // MenuItem for Passing through CCs, so we can check it
    JCheckBoxMenuItem passThroughCCMenuItem;
    // MenuItem for Passing through CCs, so we can check it
    JCheckBoxMenuItem perChannelCCsMenuItem;
    
    // MenuItem for Passing through Controller Values, so we can check it    
    JCheckBoxMenuItem passThroughControllerMenuItem;
    
    /** Returns whether we are passing through CC */
    public boolean getPassThroughController() { synchronized(passThroughCCLock) { return passThroughController; } }
    public void setPassThroughController(final boolean val)
        {
        synchronized(passThroughCCLock) 
            { 
            passThroughController = val; 
            setLastX("" + val, "PassThroughController", getSynthNameLocal(), false);
            SwingUtilities.invokeLater(new Runnable()
                {
                public void run()
                    {
                    passThroughControllerMenuItem.setState(val);
                    }
                });
            } 
        }

    /** Returns whether we are passing through CC */
    public boolean getPassThroughCC() { synchronized(passThroughCCLock) { return passThroughCC; } }

    /** Sets whether we are pasing through CC */
    public void setPassThroughCC(final boolean val) 
        { 
        synchronized(passThroughCCLock) 
            { 
            passThroughCC = val; 
            setLastX("" + val, "PassThroughCC", getSynthNameLocal(), false);
            SwingUtilities.invokeLater(new Runnable()
                {
                public void run()
                    {
                    passThroughCCMenuItem.setState(val);
                    }
                });
            if (val == true && getLearningCC())
                setLearningCC(false);
            } 
        }


    // Returns the name we should display for a given CC on the Title Bar
    String nameForCC(int cc, int sub)
        {
        if (cc < 256)
            {
            int type = ccmap.getTypeForCCPane(cc, getCurrentTab());
            if (type == CCMap.TYPE_RELATIVE_CC_64)
                //return "CC64(" + sub + ") " + cc;
                return "RCC(" + sub + ") " + cc;
            else if (type == CCMap.TYPE_RELATIVE_CC_0)
                return "CC0(" + sub + ") " + cc;
            else return "CC(" + sub + ") " + cc;
            }
        else return "NRPN(" + sub + ") " + (cc - 256);
        }
        
    /** Returns whether we're presently learning CCs */
    public boolean getLearningCC() { return learning; }
    
    /** Sets whether we're presently learning CCs. */
    public void setLearningCC(boolean val)
        {
        learning = val;
        model.clearLastKey();
        if (learning)
            {
            setShowingMutation(false);
            if (learningMenuItem != null) learningMenuItem.setText("Stop Mapping");
            //if (learningMenuItem0 != null) learningMenuItem0.setEnabled(false);
            if (learningMenuItem64 != null) learningMenuItem64.setEnabled(false);
            }
        else
            {
            if (learningMenuItem != null) learningMenuItem.setText("Map CC / NRPN");
            //if (learningMenuItem0 != null) learningMenuItem0.setEnabled(true);
            if (learningMenuItem64 != null) learningMenuItem64.setEnabled(true);
            }
        updateTitle();
        }
        
    /** Clears all learned CCs, and turns off learning. */
    public void clearLearned()
        {
        ccmap.clear();
        learning = false;
        setLearningCC(false);
        }
        








    //////// SYNTHESIZER EDIT PANES
        
    //// You can get the class names of all the synthesizer editors as:
    ////
    ////        getClassNames()
    ////
    //// There are presently TWO sources of a synthesizer's textual name in Edisyn:
    //// The synth/Synths.txt file
    ////    This is the source of the information found in numSynths(), getClassNames(), and getSynthNames()
    ////
    //// Individual synth classes
    ////    Each synth class "overrides" a STATIC method called getSynthName().
    ////    This method can be easily accessed with one of: getSynthNameForClassName(), getSynthNameForClass(), or getSynthNameLocal()
    ////
    //// The objective here is to speed up Edisyn's load time, which is weighed down by preloading all the
    //// editor classes, some of which (<ahem>FS1R Fseq</ahem>) have an enormous amount of data.  

    static String[] synthClassNames;
    static String[] synthRecognizers;
    static String[] synthNames;
    
    static
        {
        loadSynths();
        }
    
    static Class getSynth(int num)
        {
        try
            {
            return Class.forName(synthClassNames[num]);
            }
        catch (Exception ex)
            {
            Synth.handleException(ex);
            return null;
            }
        }
        
    static void loadSynths()
        {
        ArrayList<String> classes = new ArrayList<String>();
        ArrayList<String> names = new ArrayList<String>();
        Scanner scan = new java.util.Scanner(edisyn.Synth.class.getResourceAsStream("synth/Synths.txt"));
        boolean firstTime = true;
        while(scan.hasNextLine())
            {
            String str = scan.nextLine().trim();
            if (!(str.length() == 0 || str.startsWith("#")))
                {
                int space = str.indexOf(' ');
                int tab = str.indexOf('\t');
                if (space == -1 && tab == -1) 
                    {
                    if (firstTime) System.err.println("synth/Synths.txt has no synth name for certain synths.  Here's what they are, add them:");
                    firstTime = false;
                    try
                        {
                        System.err.print(str + "\t");
                        Class _class = Class.forName(str);
                        Method method = _class.getMethod("getSynthName", new Class[] { });
                        System.err.print((String)(method.invoke(null, new Object[] { } )));
                        }
                    catch (Exception ex) { }
                    System.err.println();
                    continue;
                    }
                // grab the first one
                int result = (tab == -1 ? space : (space == -1 ? tab : (space < tab ? space : tab)));
                String classname = str.substring(0, result).trim();
                String synthname = str.substring(result).trim();
                classes.add(classname);
                names.add(synthname);
                }       
            }
        synthClassNames = (String[])(classes.toArray(new String[0]));
        synthNames = (String[])(names.toArray(new String[0]));
        synthRecognizers = new String[synthClassNames.length];
        for(int i = 0; i < synthRecognizers.length; i++)
            synthRecognizers[i] = synthClassNames[i] + "Rec";
        }

    public static int numSynths()
        {
        return synthNames.length;
        }

    /** All synthesizer classes in Edisyn.  These are loaded from synth/Synths.txt */
    public static String[] getClassNames()
        {
        return synthClassNames;
        }

    /** All synthesizer names in Edisyn, one per class in synths.  These are loaded from synth/Synths.txt */
    public static String[] getSynthNames()
        {
        return synthNames;
        }
                
    /** Return the synth name for a given class name, or null if an error occurred. */
    public static String getSynthNameForClassName(String synth)
        {
        try
            {
            return getSynthNameForClass(Class.forName(synth));
            }
        catch (Exception ex)
            {
            Synth.handleException(ex);
            return null;
            }
        }
                
    /** Return the synth name for a given class, or null if an error occurred.  This is not loaded from synth/Synths.txt, but rather from the getSynthName() */
    public static String getSynthNameForClass(Class synth)
        {
        try
            {
            Method method = synth.getMethod("getSynthName", new Class[] { });
            return (String)(method.invoke(null, new Object[] { } ));
            }
        catch (Exception ex)
            {
            Synth.handleException(ex);
            return null;
            }
        }

    /** Returns the name of this synth, by calling getSynthName(). */
    public final String getSynthNameLocal()
        {
        String val = getSynthNameForClass(getClass());
        if (val == null) val = "Synth with no getSynthName() method, oops";
        return val;
        }
                
                
                
                
                
                
                
                
                
                
    /////// UNDO
                  
    /** Update the Undo and Redo menus to be enabled or disabled. */
    public void updateUndoMenus()
        {
        if (undo == null)               // we could just be a scratch synth, not one with a window
            return;  
                
        if (redoMenu != null)
            redoMenu.setEnabled(
                undo.shouldShowRedoMenu());
        if (undoMenu != null)
            undoMenu.setEnabled(
                undo.shouldShowUndoMenu());
        }









    ////// STUFF YOU MAY HAVE TO OVERRIDE




    /// There are a lot of redundant methods here.  You only have to override some of them.

    /// PARSING (LOADING OR RECEIVING)
    /// When a message is received from the synthesizer, Edisyn will do this:
    /// If the message is a Sysex Message, then
    ///     Call recognize(message data).  If it returns true, then
    ///                                 Call performParse(message data, fromFile) [we presume it's a dump or a load from a file]
    ///                                 Call parse(message data, fromFile) [we presume it's a dump or a load from a file]
    ///             Else
    ///                     Call parseParameter(message data) [we presume it's a parameter change, or maybe something else]
    /// Else if the message is a complete CC or NRPN message
    ///             Call handleSynthCCOrNRPN(message) [it's some CC or NRPN that your synth is sending us, maybe a parameter change?]
        
    /// SENDING A SINGLE PARAMETER OF KEY key
    /// Call emitAll(key, status)
    ///         This calls emitAll(key)
    ///             This calls emit(key)
    ///
    /// You could override either of these methods, but probably not both.
        
    /// SENDING TO CURRENT
    /// Call sendAllParameters().  This does:
    ///             If getSendsAllParametersAsDump(), this calls:
    ///                     emitAll(tempModel, toWorkingMemory = true, toFile)
    ///                             This calls emit(tempModel, toWorkingMemory = true, toFile)
    ///             Else for every key it calls:
    ///                 Call emitAll(key)
    ///                     This calls emit(key)
    ///
    /// You could override either of the emit...(tempModel...) methods, but probably not both.
    /// You could override either of the emit...(key...) methods, but probably not both.

    /// WRITING TO A PATCH
    /// Call gatherPatchInfo(...,tempModel,...)
    /// If successful
    ///     Call emitAll(tempModel, toWorkingMemory = false, toFile)
    ///                     This calls emit(tempModel, toWorkingMemory = false, toFile)
    ///     Call changePatch(tempModel) so we're at the patch we just wrote to
    ///         If getSendsPararametersAfterWrite()
    ///         Call sendAllParameters().  This does:
    ///                     If getSendsAllParametersAsDump(), this calls:
    ///                             emitAll(tempModel, toWorkingMemory = true, toFile)
    ///                                     This calls emit(tempModel, toWorkingMemory = true, toFile)
    ///                     Else for every key it calls:
    ///                         Call emitAll(key)
    ///                             This calls emit(key)
    ///     
    /// You could override either of the emit...(tempModel...) methods, but probably not both.
    /// You could override either of the emit...(key...) methods, but probably not both.
                
    /// SAVING
    /// Call emitAll(tempModel, toWorkingMemory, toFile)
    ///             This calls emit(tempModel, toWorkingMemory, toFile)
    ///
    /// You could override either of the emit methods, but probably not both.
    /// Note that saving strips out the non-sysex bytes from emitAll.
        
    /// REQUESTING A PATCH 
    /// If we're requesting the CURRENT patch
    ///             Call performRequestCurrentDump()
    ///                     this then calls requestCurrentDump()
    /// Else
    ///     Call gatherPatchInfo(...,tempModel,...)
    ///             If successful
    ///                     Call performRequestDump(tempModel)
    ///                             This calls changePatch(tempModel)
    ///                             Then it calls requestDump(tempModel)
    ///
    /// You could override performRequestCurrentDump or requestCurrentDump, but probably not both.
    /// Similarly, you could override performRequestDump or requestDump, but probably not both

    /// ADDITIONAL COMMONLY OVERRIDEN METHODS
    ///
    /// getSynthName()              // you must override this
    /// getPatchName(getModel())              // you ought to override this, it returns null by default
    /// getSendsAllParametersAsDump()       // override this to return FALSE if parameters must be sent one at a time rather than emitted as sysex
    /// getDefaultResourceFileName()        // return the filename of the default init file
    /// getHTMLResourceFileName()           // return the filename of the default HTML file
    /// requestCloseWindow()        // override this to query the user before the window is closed
    /// revisePatchName(name)       // tweak patch name to be valid.  This is a utility method that you commonly override AND call
    /// reviseID(id)                // tweak the id to be valid
    /// revise()                    // tweak all the keys to be within valid ranges.  There's a default form which you might wish to override.
    /// getPauseBetweenMIDISends()  // return the pause (in ms) between MIDI messages if the synth needs them slower
    /// getPauseAfterChangePatch() // return the pause after a PC
    /// sprout()                    // typically you override this (calling super of course) to disable certain menus
    /// windowBecameFront() // override this to be informed that your patch window became the front window (you might switch something on the synth)

    /** Changes the patch and bank to reflect the information in tempModel.
        You may need to call simplePause() if your synth requires a pause after a patch change. */
    public void changePatch(Model tempModel) { }

    
    public static int PARSE_FAILED = 0;
    public static int PARSE_INCOMPLETE = 1;
    public static int PARSE_SUCCEEDED = 2;
    public static int PARSE_SUCCEEDED_UNTITLED = 3;
    public static int PARSE_CANCELLED = 4;
    static int PARSE_ERROR = 5;         // note not public
    
    /** Updates the model to reflect the following sysex patch dump for your synthesizer type.
        FROMFILE indicates that the parse is from a sysex file.
        There are several possible things you can return:
        - PARSE_CANCELLED indicates that the user cancelled the parse and the editor data was not changed.
        - PARSE_FAILED indicates that the parse was not successful and the editor data was not changed.
        - PARSE_INCOMPLETE indicates that the parse was not fully performed -- for example,
        the Yamaha TX81Z needs two separate dumps before it has a full patch, so we return 
        PARSE_INCOMPLETE on the first one, and PARSE_SUCCEEDED only on the last one).
        - PARSE_SUCCEEDED indicates that the parse was completed and the patch is fully modified.
        - PARSE SUCCEEDED_UNTITLED indicates the same, except that assuming the patch was read
        from a file, an alternative version of the patch has been substituted, and so the patch
        filename should be untitled.  For example, the DX7 can alternatively load bank-sysex
        patches and extract a patch from the bank; in this case the patch filename should not
        be the bank sysex filename.*/
    // don't return PARSE_ERROR, that's used internally
    public int parse(byte[] data, boolean fromFile) { return PARSE_FAILED; }
    
    /** Updates the model to reflect the following sysex message from your synthesizer. 
        You are free to IGNORE this message entirely.  Patch dumps will generally not be sent this way; 
        and furthermore it is possible that this is crazy sysex from some other synth so you need to check for it.  */
    public void parseParameter(byte[] data) { return; }
    
    /** Produces a sysex patch dump suitable to send to a remote synthesizer as one
        OR MORE sysex dumps or other MIDI messages.  Each sysex dump is a separate byte array,
        and other midi messages are MIDI message objects.
        If you return a zero-length array, nothing will be sent.  
        If tempModel is non-null, then it should be used to extract meta-parameters
        such as the bank and patch number (stuff that's specified by gatherPatchInfo(...).
        Otherwise the primary model should be used.  The primary model should be used
        for all other parameters.  toWorkingMemory indicates whether the patch should
        be directed to working memory of the synth or to the patch number in tempModel. 
        
        <p>If TOFILE is true, then we are emitting to a file, not to the synthesizer proper. 
        
        <p>It is assumed that the NON byte-array elements may be stripped out if this
        emit is done to a file.
        
        <p>The default version of this method simply calls emit() and returns its
        value as the first subarray.  If you have a synthesizer (like the TX81Z) which
        dumps a single patch as multiple sysex dumps, override this to send the patch
        properly.
    */
    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        byte[] result = emit(tempModel, toWorkingMemory, toFile);
        if (result == null ||
            result.length == 0)
            return new Object[0];
        else
            return new Object[] { result };
        }

    /** Produces a sysex patch dump suitable to send to a remote synthesizer. 
        If you return a zero-length byte array, nothing will be sent.  
        If tempModel is non-null, then it should be used to extract meta-parameters
        such as the bank and patch number (stuff that's specified by gatherPatchInfo(...).
        Otherwise the primary model should be used.  The primary model should be used
        for all other parameters.  toWorkingMemory indicates whether the patch should
        be directed to working memory of the synth or to the patch number in tempModel. 
        
        <p>If TOFILE is true, then we are emitting to a file, not to the synthesizer proper. 

        <p>Note that this method will only be called by emitAll(...).  So if you 
        have overridden emitAll(...) you don't need to implement this method. */
    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile) { return new byte[0]; }
    
    /** Produces one or more sysex parameter change requests for the given parameter as one
        OR MORE sysex dumps or other MIDI messages.  Each sysex dump is a separate byte array,
        and other midi messages are MIDI message objects.  The status integer indicates under
        what condition emitAll(...) is being called, such as STATUS_SENDING_ALL_PARAMETERS
        or STATUS_UPDATING_ONE_PARAMETER.  By default, this calls emitAll(key);
        
        If you return a zero-length byte array, nothing will be sent.  */
    public Object[] emitAll(String key, int status)
        {
        return emitAll(key);
        }

    /** Produces one or more sysex parameter change requests for the given parameter as one
        OR MORE sysex dumps or other MIDI messages.  Each sysex dump is a separate byte array,
        and other midi messages are MIDI message objects.
        
        If you return a zero-length byte array, nothing will be sent.  */
    public Object[] emitAll(String key)
        {
        byte[] result = emit(key);
        if (result == null ||
            result.length == 0)
            return new Object[0];
        else
            return new Object[] { result };
        }

    /** Produces a sysex parameter change request for the given parameter.  
        If you return a zero-length byte array, nothing will be sent.
        
        <p>Note that this method will only be called by emitAll(...).  So if you 
        have overridden emitAll(...) you don't need to implement this method. */        
    public byte[] emit(String key) { return new byte[0]; }
    
    /** Produces a sysex message to send to a synthesizer to request it to initiate
        a patch dump to you.  If you return a zero-length byte array, nothing will be sent. 
        If tempModel is non-null, then it should be used to extract meta-parameters
        such as the bank and patch number or machine ID (stuff that's specified by gatherPatchInfo(...).
        Otherwise the primary model should be used.  The primary model should be used
        for all other parameters.  
    */
    public byte[] requestDump(Model tempModel) { return new byte[0]; }
    
    /** Produces a sysex message to send to a synthesizer to request it to initiate
        a patch dump to you for the CURRENT PATCH.  If you return a zero-length byte array, 
        nothing will be sent.  
    */
    public byte[] requestCurrentDump() { return new byte[0]; }

    /** Performs a request for a dump of the patch indicated in tempModel.
        This method by default does a changePatch() as necessary, then calls
        requestDump(...) and submits it to tryToSendSysex(...), 
        but you can override it to do something more sophisticated. 
        Note that if your synthesizer can load patches without switching to them, you
        should only change patches if changePatch is true.  An example of when
        changePatch will be false: when doing a merge (you'd like to merge an external
        patch into this one but stay where you are).  Another example is when a multi-patch
        wants to pop up a single patch to display it. */    
    public void performRequestDump(Model tempModel, boolean changePatch)
        {
        if (changePatch)
            performChangePatch(tempModel);
            
        tryToSendSysex(requestDump(tempModel));
        }
    
    /** Performs a request for a dump of the patch indicated in the current model.
        This method by default calls requestCurrentDump(...) and submits it to tryToSendSysex(...), 
        but you can override it to do something more sophisticated. */    
    public void performRequestCurrentDump()
        {
        tryToSendSysex(requestCurrentDump());
        }
    
    /** Gathers meta-parameters from the user via a JOptionPane, such as 
        patch number and bank number, which are used to specify where a patch
        should be saved to or loaded from.  These are typically also stored in
        the primary model, but the user may want to change them so as to 
        write out to a different location for example.  The model should not be
        revised to hold the new values; but rather they should be placed into tempModel.
        This method returns TRUE if the user provided the values, and FALSE
        if he cancelled.
        
        <p>If writing is TRUE, then the purpose of this info-gathering is to find
        a place to write or send a patch TO.  Otherwise its purpose is to read a patch FROM
        somewhere.  Some synths allow patches to be read from many locations but written only
        to specific ones (because the others are read-only).
    */
    public boolean gatherPatchInfo(String title, Model tempModel, boolean writing) { return false; }



    /** Override this to handle CC or NRPN messages which arrive from the synthesizer. */
    public void handleSynthCCOrNRPN(Midi.CCData data)
        {
        // do nothing by default
        }
            
    /** Returns the name of the synthesizer.  You should make your own
        static version of this method in your synth panel subclass.  */
    private static String getSynthName() { return "Override Me"; }
    
    /** Returns a Model with the next patch location (bank, number, etc.) beyond the one provided in the given model.
        If the model provided contains the very last patch location, you should wrap around. */
    public Model getNextPatchLocation(Model model)
        {
        return null;
        }
    
    /** Returns the patch location as a simple and short string, such as "B100" for "Bank B Number 100". 
        The default implementation returns null; if this method returns null,
        then bulk downloading will not be available. */
    public String getPatchLocationName(Model model)
        {
        return null;
        }
    
    /** Returns the name of the patch in the given model, or null if there is no such thing. */
    public String getPatchName(Model model) { return null; }
    
    /** Return true if the window can be closed and disposed of. You should do some cleanup
        as necessary (the system will handle cleaning up the receiver and transmitters. 
        By default this just returns true.  */
    public boolean requestCloseWindow() { return true; }
        
    /** Function for tweaking a name to make it valid for display in the editor.
        The default version just does a right-trim of whitespace on the name.  You
        may wish to override this to also restrict the valid characters and the name
        length. */
    public String revisePatchName(String name)
        {
        // right-trim the name
        int i = name.length()-1;
        while (i >= 0 && Character.isWhitespace(name.charAt(i))) i--;
        name = name.substring(0, i+1);
        return name;
        }
        
    /** If the provided id is correct, return it. If the provided id is *null* or incorrect,
        provide the id from getModel().  Return null if there is no such thing as an id for this machine. */
    public String reviseID(String id)
        {
        return null;
        }
    
    public void revise()
        {
        revise(model);
        }
        
    boolean printRevised = true;
    
    /** Only revises / issues warnings on out-of-bounds numerical parameters. 
        You probably want to override this to check more stuff. */
    public void revise(Model model)
        {
        String[] keys = model.getKeys();
        for(int i = 0; i < keys.length; i++)
            {
            String key = keys[i];
            if (!model.isString(key) &&
                model.minExists(key) && 
                model.maxExists(key))
                {
                // verify
                int val = model.get(key);
                if (val < model.getMin(key))
                    { model.set(key, model.getMin(key)); if (printRevised) System.err.println("Warning (Synth): Revised " + key + " from " + val + " to " + model.get(key));}
                if (val > model.getMax(key))
                    { model.set(key, model.getMax(key)); if (printRevised) System.err.println("Warning (Synth): Revised " + key + " from " + val + " to " + model.get(key));}
                }
            }
            
        }

    /** Override this to make sure that at *least* the given time (in Milliseconds) has transpired between MIDI sends. */
    public double getPauseBetweenMIDISends() { return 0; }
    long getNanoPauseBetweenMIDISends() { return (long)(getPauseBetweenMIDISends() * 1000000.0); }

    /** Override this to make sure that the given additional time (in ms) has transpired between MIDI patch changes. */
    public int getPauseAfterChangePatch() { return 0; }
    
    /** Override this to make sure that the given additional time (in ms) has transpired between sending all parameters and anything else (such as playing a note) */
    public int getPauseAfterSendAllParameters() { return 0; }
    
    /** Override this to make sure that the given additional time (in ms) has transpired between sending each parameter via emitAll(key). */
    public int getPauseAfterSendOneParameter() { return 0; }

    /** Override this to make sure that the given additional time (in ms) has transpired after writing a patch but before the change patch following. 
        The default is to be the same as getPauseAfterSendAllParameters(); */
    public int getPauseAfterWritePatch() { return getPauseAfterSendAllParameters(); }

    /** Override this to make sure that the given additional time (in ms) has transpired after receiving a 
        requested patch before we request a second patch (without a change patch command).  
        The default is to be the same as getPauseAfterChangePatch(); */
    public int getPauseAfterReceivePatch() { return getPauseAfterChangePatch(); }

    /** Override this to return TRUE if, after a patch write, we need to change to the patch *again* so as to load it into memory. */
    public boolean getShouldChangePatchAfterWrite() { return false; }
    
    /** Override this to return TRUE if, after recieving a NON-MERGE patch from the synthesizer, we should turn around and sendAllParameters() to it.
        This commonly is needed in some synth multi-mode editors, where program changes have no effect (they don't switch to a new multi-mode synth),
        and so we'll receive the correct patch but the synthesizer won't switch to it.  We can turn around and emit changes to it to get the right
        sound in the edit buffer. */ 
    public boolean getSendsParametersAfterNonMergeParse() { return false; }
    
    /** Override this to return TRUE if you want Edisyn to sendAllParmameters() solely in response to the user directly selecting "Send Current Patch"
        and in no other situation.  By default this returns false. */ 
    public boolean getSendsParametersOnlyOnSendCurrentPatch() { return false; }

    /** Override this to return TRUE if you want Edisyn to sendAllParmameters() immediately after a patch write, because the patch write writes
        to permanent memory but doesn't change working memory.  This isn't very common (the Kyra and Pulse 2 seem to need it). */ 
    public boolean getSendsParametersAfterWrite() { return false; }

    /** Return the filename of your default sysex file (for example "MySynth.init"). Should be located right next to the synth's class file ("MySynth.class") */
    public String getDefaultResourceFileName() { return null; }
        
    /** Return the filename of your HTML About file (for example "MySynth.html"). Should be located right next to the synth's class file ("MySynth.class") */
    public String getHTMLResourceFileName() { return null; }

    /** Override this as you see fit to do something special when your window becomes front. */
    public void windowBecameFront() { }

    /** Returns whether the synth sends its patch dump (TRUE) as one single sysex dump or by
        sending multiple separate parameter change requests (FALSE).  By default this is TRUE. */
    public boolean getSendsAllParametersAsDump() { return true; }

    /** Returns whether the synth sends raw CC or cooked CC (such as for NRPN) to update parameters.  The default is FALSE (cooked or nothing). */
    public boolean getExpectsRawCCFromSynth() { return false; }

    /** Returns whether the synth should send parameters immediately after a successful load or load-merge from disk. */
    public boolean getSendsParametersAfterLoad() { return true; }

    /** Return whether we should be sending an ALL SOUNDS OFF / ALL NOTES OFF whenever we close the window,
        switch to it, open it, etc.  By default this is normally true.  Note that if the user has turned
        off this feature in the menu, then sounds will not be sent regardless of this setting. */
    public boolean sendAllSoundsOffWhenWindowChanges() { return true; }
    
    /** Called when, during Sanity Check testing, another synth (synth2) contains a different value (obj2) for a given
        key than you hold (namely obj1).  Return true if this is acceptable. */
    public boolean testVerify(Synth synth2, String key, Object obj1, Object obj2) { return false; }

    /** Return an extra pause (beyond the pause after sending all parameters) after playing a test sound while hill-climbing or morphing. */
    public int getPauseBetweenHillClimbPlays() { return 0; }
        









    ////////  UTILITY METHODS FOR BUILDING MIDI MESSAGES

    /** Builds a sequence of CCs for an NRPN message. */
    public Object[] buildNRPN(int channel, int parameter, int value)
        {
        try
            {
            int p_msb = (parameter >>> 7);
            int p_lsb = (parameter & 127);
            int v_msb = (value >>> 7);
            int v_lsb = (value & 127);
            if (v_msb > 127) 
                {
                System.err.println("Synth.java NRPN(int, int, int) ERROR.  Problem with value " + value + " at parameter " + parameter);
                }
        
            return new Object[]
                {
                new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 99, (byte)p_msb),
                new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 98, (byte)p_lsb),
                new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 6, (byte)v_msb),
                new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 38, (byte)v_lsb),

                // can't have these right now, it freaks out the PreenFM2
                //new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 101, (byte)127),
                //new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 100, (byte)127),
                };
            }
        catch (InvalidMidiDataException e)
            {
            Synth.handleException(e);
            return new Object[0];
            }
        }

    /** Builds a short (7-bit) CC. */
    public Object[] buildCC(int channel, int parameter, int value)
        {
        try
            {
            return new Object[]
                {
                new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, parameter, (byte)(value & 127))
                };
            }
        catch (InvalidMidiDataException e)
            {
            Synth.handleException(e);
            return new Object[0];
            }
        }


    /** Builds a sequence of CCs for a 14-bit CC message.  The parameter must be 0...31. */
    public Object[] buildLongCC(int channel, int parameter, int value)
        {
        try
            {
            int v_msb = ((value >>> 7) & 127);
            int v_lsb = (value & 127);
            return new Object[]
                {
                new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, parameter, (byte)v_msb),
                new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, parameter + 32, (byte)v_lsb)
                };
            }
        catch (InvalidMidiDataException e)
            {
            Synth.handleException(e);
            return new Object[0];
            }
        }


    /** Builds a Program Change message. */
    public Object[] buildPC(int channel, int program)
        {
        try
            {
            return new Object[]
                {
                new ShortMessage(ShortMessage.PROGRAM_CHANGE, channel, program, 0)
                };
            }
        catch (InvalidMidiDataException e)
            {
            Synth.handleException(e);
            return new Object[0];
            }
        }

    /** Concatenates two object arrays nondestructively.  Useful for making chains of MIDI messages. */
    public Object[] concatenate(Object[] a, Object[] b)
        {
        Object[] result = new Object[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
        }

    /** Concatenates two byte arrays nondestructively.  Useful for making chains of MIDI messages. */
    public byte[] concatenate(byte[] a, byte[] b)
        {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
        }
        
        
        
        
        
        
    /////// PARSING
        
    public int performParse(byte[] data, boolean fromFile)
        {
        boolean previous = model.getUpdateListeners();
        model.setUpdateListeners(false);
        int val = PARSE_ERROR;
        try
            {
            val = parse(data, fromFile);
            }
        finally
            {
            model.setUpdateListeners(previous);
            model.updateAllListeners();
            }
        return val;
        }
        



    ////////  MIDI INTERFACE
        
    /** The Synth's MIDI Tuple */
    public Midi.Tuple tuple;
    
    // The synth's MIDI interface
    Midi midi = new Midi();

    // flag for whether sending MIDI is temporarily turned off or not
    boolean sendMIDI = true;  // we can send MIDI 



    /** Builds a receiver to attach to the current IN transmitter.  The receiver
        can handle merging and patch reception. */
    public Receiver buildInReceiver()
        {
        return new Receiver()
            {
            public void close()
                {
                }
                                
            public void send(final MidiMessage message, final long timeStamp)
                {
                // I'm doing this in the Swing event thread because I figure it's multithreaded
                SwingUtilities.invokeLater(new Runnable()
                    {
                    public void run()
                        {
                        if (amActiveSynth())
                            {
                            if (message instanceof SysexMessage)
                                {
                                final byte[] data = midi.gatherInSysexData((SysexMessage)message);

                                if (data == null)
                                    {
                                    // sysex not completed yet
                                    }
                                else if (recognizeLocal(data))
                                    {
                                    // this last statement fixes a mystery.  When I call Randomize or Reset on
                                    // a Blofeld or on a Microwave, all of the widgets update simultaneously.
                                    // But on a Blofeld Multi or Microwave Multi they update one at a time.
                                    // I've tried a zillion things, even moving all the widgets from the Blofeld Multi
                                    // into the Blofeld, and it makes no difference!  For some reason the OS X
                                    // repaint manager is refusing to coallesce their repaint requests.  So I do it here.
                                    repaint();
                                                                                                
                                    if (merging != 0.0)
                                        {
                                        if (merge(data, merging) == PARSE_INCOMPLETE)
                                            {
                                            //System.err.println("Still Merging");
                                            }
                                        else
                                            {
                                            //System.err.println("Merging Done?");
                                            setMergeProbability(0.0);
                                            }
                                        }
                                    else
                                        {
                                        mergeSynth = null;
                                        // we turn off MIDI because parse() calls revise() which triggers setParameter() with its changes
                                        setSendMIDI(false);
                                        undo.setWillPush(false);
                                        Model backup = (Model)(model.clone());
                                        int result = PARSE_ERROR;
                                        try 
                                            {
                                            result = performParse(data, false);
                                            }
                                        catch (Exception ex)
                                            {
                                            System.err.println("The exception is " + ex);
                                            Synth.handleException(ex);
                                            // result is now PARSE_ERROR
                                            }
                                         
                                        incomingPatch = (result == PARSE_SUCCEEDED || result == PARSE_SUCCEEDED_UNTITLED);
                                        if (result == PARSE_CANCELLED)
                                            {
                                            // nothing
                                            }
                                        else if (result == PARSE_FAILED)
                                            {
                                            showSimpleError("Receive Error", "Could not read the patch.");
                                            }
                                        else if (result == PARSE_ERROR)
                                            {
                                            showSimpleError("Receive Error", "An error occurred on reading the patch.");
                                            }

                                        undo.setWillPush(true);
                                        if (!backup.keyEquals(getModel()))  // it's changed, do an undo push
                                            undo.push(backup);
                                        setSendMIDI(true);
                                        if (getSendsParametersAfterNonMergeParse())
                                            {
                                            sendAllParameters();
                                            }
                                        file = null;
                                        updateBlend();
                                        }

                                    updateTitle();
                                    }
                                else    // Maybe it's a local Parameter change in sysex?
                                    {
                                    // we don't do undo here.  It's not great but PreenFM2 etc. would wreak havoc
                                    boolean willPush = undo.getWillPush();
                                    undo.setWillPush(false);
                                                                                        
                                    sendMIDI = false;  // so we don't send out parameter updates in response to reading/changing parameters
                                    parseParameter(data);
                                    sendMIDI = true;
                                    updateTitle();
                                                                                        
                                    undo.setWillPush(willPush);
                                    }
                                }
                            else if (message instanceof ShortMessage)
                                {
                                midi.resetInSysexData();
                                ShortMessage sm = (ShortMessage)message;
                                if (sm.getCommand() == ShortMessage.CONTROL_CHANGE)
                                    {
                                    boolean willPush = undo.getWillPush();
                                    undo.setWillPush(false);
                                                                                                                
                                    // we don't do undo here.  It's not great but PreenFM2 etc. would wreak havoc
                                    sendMIDI = false;  // so we don't send out parameter updates in response to reading/changing parameters
                                    // let's try parsing it
                                    handleInRawCC(sm);
                                    if (!getReceivesPatchesAsDumps()) 
                                        {
                                        incomingPatch = true;
                                        }
                                    sendMIDI = true;
                                    updateTitle();
                                                                                                                
                                    undo.setWillPush(willPush);
                                    }
                                }
                            }
                        if (testIncomingSynthMIDI) 
                            {
                            showSimpleMessage("Incoming MIDI from Synthesizer", "A MIDI message has arrived from the Synthesizer:\n" + Midi.format(message) + "\nTime: " + timeStamp); 
                            testIncomingSynthMIDI = false; 
                            testIncomingSynth.setText("Report Next Synth MIDI");
                            } 
                        }
                    });
                
                }
            };
        }
        
    byte[] extractData(MidiMessage message)
        {
        byte[] d = message.getMessage();
                
        // weirdly, d.length is permitted to be larger than the actual message length
        int len = message.getLength();
        if (len < d.length)
            {
            byte[] newD = new byte[len];
            System.arraycopy(d, 0, newD, 0, len);
            return newD;
            }
        else return d;                          
        }
                
                

    /** Builds a receiver to attach to the current KEY transmitter.  The receiver
        can resend all incoming requests to the OUT receiver. */
    public Receiver buildKeyReceiver()
        {
        return new Receiver()
            {
            public void close()
                {
                }
                                
            public void send(final MidiMessage message, final long timeStamp)
                {
                // I'm doing this in the Swing event thread because I figure it's multithreaded
                SwingUtilities.invokeLater(new Runnable()
                    {
                    public void run()
                        {
                        if (amActiveSynth())
                            {
                            if (message instanceof ShortMessage)
                                {
                                midi.resetKeySysexData();
                                ShortMessage shortMessage = (ShortMessage)message;
                                try
                                    {
                                    // we intercept a message if:
                                    // 1. It's a CC (maybe NRPN)
                                    // 2. We're not passing through CC
                                    // 3. It's the right channel OR our key channel is OMNI OR we're doing per-channel CCs
                                    if (tuple != null &&
                                        !getPassThroughCC() && 
                                        shortMessage.getCommand() == ShortMessage.CONTROL_CHANGE &&
                                        (shortMessage.getChannel() == (tuple.keyChannel - 1) || tuple.keyChannel == tuple.KEYCHANNEL_OMNI || perChannelCCs))
                                        {
                                        // we intercept this
                                        handleKeyRawCC(shortMessage);
                                        messageFromController(message, true, false);
                                        }
                                        
                                    // We send the message to the synth if:
                                    // 1. We didn't intercept it
                                    // 2. We pass through data to the synth
                                    else if (tuple != null && getPassThroughController())
                                        {
                                        // pass it on!
                                        ShortMessage newMessage = null;
                                                                                
                                        // In order to pass on, we have to make a new one.  But 
                                        // stupidly, ShortMessage has no way of changing its channel, so we have to rebuild
                                        ShortMessage s = (ShortMessage)message;
                                        int status = s.getStatus();
                                        int channel = s.getChannel();
                                        int data1 = s.getData1();
                                        int data2 = s.getData2();
                                        boolean voiceMessage = ( status < 0xF0 );

                                        // should we attempt to reroute to the synth?
                                        if (channel == (tuple.keyChannel - 1) || tuple.keyChannel == tuple.KEYCHANNEL_OMNI)
                                            {
                                            channel = getVoiceMessageRoutedChannel(channel, getChannelOut());
                                            }

                                        if (voiceMessage)
                                            {
                                            newMessage = new ShortMessage(status, channel, data1, data2);
                                            }
                                        else
                                            {
                                            newMessage = new ShortMessage(status, data1, data2);
                                            }
                                                                
                                        tryToSendMIDI(newMessage);
                                        messageFromController(newMessage, false, true);
                                        }
                                    else
                                        {
                                        messageFromController(message, false, false);
                                        }
                                    }
                                catch (InvalidMidiDataException e)
                                    {
                                    Synth.handleException(e);
                                    messageFromController(message, false, false);
                                    }
                                }
                            else if (message instanceof SysexMessage && passThroughController)
                                {
                                tryToSendSysex(extractData(message));
                                messageFromController(message, false, true);
                                }
                            }
                        if (testIncomingControllerMIDI) 
                            { 
                            showSimpleMessage("Incoming MIDI from Controller", "A MIDI message has arrived from the Controller:\n" + Midi.format(message) + "\nTime: " + timeStamp); 
                            testIncomingControllerMIDI = false; 
                            testIncomingController.setText("Report Next Controller MIDI");
                            } 
                        }
                    });
                }
            };
        }
    
    /** Builds a receiver to attach to the current KEY 2 transmitter.  The receiver
        can resend all incoming requests to the OUT receiver. */
    public Receiver buildKey2Receiver()
        {
        return new Receiver()
            {
            public void close()
                {
                }
                                
            public void send(final MidiMessage message, final long timeStamp)
                {
                // I'm doing this in the Swing event thread because I figure it's multithreaded
                SwingUtilities.invokeLater(new Runnable()
                    {
                    public void run()
                        {
                        if (amActiveSynth())
                            {
                            if (message instanceof ShortMessage)
                                {
                                midi.resetKey2SysexData();
                                ShortMessage shortMessage = (ShortMessage)message;
                                try
                                    {
                                    // we intercept a message if:
                                    // 1. It's a CC (maybe NRPN)
                                    // 2. We're not passing through CC
                                    // 3. It's the right channel OR our key 2 channel is OMNI OR we're doing per-channel CCs
                                    if (tuple != null &&
                                        !getPassThroughCC() && 
                                        shortMessage.getCommand() == ShortMessage.CONTROL_CHANGE &&
                                        (shortMessage.getChannel() == (tuple.key2Channel - 1) || tuple.key2Channel == tuple.KEYCHANNEL_OMNI || perChannelCCs))
                                        {
                                        // we intercept this
                                        handleKeyRawCC(shortMessage);
                                        messageFromController(message, true, false);
                                        }
                                        
                                    // We send the message to the synth if:
                                    // 1. We didn't intercept it
                                    // 2. We pass through data to the synth
                                    else if (tuple != null && getPassThroughController())
                                        {
                                        // pass it on!
                                        ShortMessage newMessage = null;
                                                                                
                                        // In order to pass on, we have to make a new one.  But 
                                        // stupidly, ShortMessage has no way of changing its channel, so we have to rebuild
                                        ShortMessage s = (ShortMessage)message;
                                        int status = s.getStatus();
                                        int channel = s.getChannel();
                                        int data1 = s.getData1();
                                        int data2 = s.getData2();
                                        boolean voiceMessage = ( status < 0xF0 );

                                        // should we attempt to reroute to the synth?
                                        if (channel == (tuple.key2Channel - 1) || tuple.key2Channel == tuple.KEYCHANNEL_OMNI)
                                            {
                                            channel = getVoiceMessageRoutedChannel(channel, getChannelOut());
                                            }

                                        if (voiceMessage)
                                            {
                                            newMessage = new ShortMessage(status, channel, data1, data2);
                                            }
                                        else
                                            {
                                            newMessage = new ShortMessage(status, data1, data2);
                                            }
                                                                
                                        tryToSendMIDI(newMessage);
                                        messageFromController(newMessage, false, true);
                                        }
                                    else
                                        {
                                        messageFromController(message, false, false);
                                        }
                                    }
                                catch (InvalidMidiDataException e)
                                    {
                                    Synth.handleException(e);
                                    messageFromController(message, false, false);
                                    }
                                }
                            else if (message instanceof SysexMessage && passThroughController)
                                {
                                tryToSendSysex(extractData(message));
                                messageFromController(message, false, true);
                                }
                            }
                        if (testIncomingControllerMIDI) 
                            { 
                            showSimpleMessage("Incoming MIDI from Controller", "A MIDI message has arrived from the Controller:\n" + Midi.format(message) + "\nTime: " + timeStamp); 
                            testIncomingControllerMIDI = false; 
                            testIncomingController.setText("Report Next Controller MIDI");
                            } 
                        }
                    });
                }
            };
        }    
    
    public void messageFromController(MidiMessage message, boolean interceptedForInternalUse, boolean routedToSynth) { return; }

    public int getVoiceMessageRoutedChannel(int incomingChannel, int synthChannel) { return synthChannel; }

    /** Sets whether sysex parameter changes should be sent in response to changes to the model.
        You can set this to temporarily paralleize your editor when updating parameters. */
    public void setSendMIDI(boolean val) { sendMIDI = val; }

    /** Gets whether sysex parameter changes should be sent in response to changes to the model. */
    public boolean getSendMIDI() { return sendMIDI; }
        
    /** Same as setupMIDI(message, null), with a default "you are disconnected" message. */
    public boolean setupMIDI() { return setupMIDI("You are disconnected. Choose MIDI devices to send to and receive from.", null); }
        
    /** Same as setupMIDI(message, oldTuple), with a default "you are disconnected" message. */
    public boolean setupMIDI(Midi.Tuple oldTuple) { return setupMIDI("You are disconnected. Choose MIDI devices to send to and receive from.", oldTuple); }
        
    /** Lets the user set up the MIDI in/out/key devices.  TheMIDI old devices are provided in oldTuple,
        or you may pass null in if there are no old devices.  Returns TRUE if a new tuple was set up. */
    public boolean setupMIDI(String message, Midi.Tuple oldTuple)
        {
        Midi.Tuple result = Midi.getNewTuple(oldTuple, this, message, buildInReceiver(), buildKeyReceiver(), buildKey2Receiver());
        boolean retval = false;
                
        if (result == Midi.FAILED)
            {
            disableMenuBar();
            JOptionPane.showOptionDialog(this, "An error occurred while trying to connect to the chosen MIDI devices.",  
                "Cannot Connect", JOptionPane.DEFAULT_OPTION, 
                JOptionPane.WARNING_MESSAGE, null,
                new String[] { "Revert" }, "Revert");
            enableMenuBar();
            }
        else if (result == Midi.CANCELLED)
            {
            // nothing
            }
        else
            {
            if (tuple != null)
                tuple.dispose();            
            tuple = result;             // update
            setSendMIDI(true);
            updateTitle();
            retval = true;
            }
                
        return retval;
        }
        


    public void performChangePatch(Model tempModel)
        {
        if (tempModel == null) // uh oh
            {
            System.err.println("Synth.performChangePatch() WARNING: No tempModel provided, so couldn't change patch.  This is likely a bug."); 
            return;
            }
                
        changePatch(tempModel);
        int p = getPauseAfterChangePatch();
        if (p > 0)
            simplePause(p);
        }

    /** Does a basic sleep for the given ms.  You should only call this if you can't
        achieve the same thing by overriding one of the getPause methods, such as
        getPauseAfterChangePatch()... */
    public void simplePause(int ms)
        {
        if (ms == 0) return;
        try { long l = System.currentTimeMillis(); Thread.currentThread().sleep(ms);}
        catch (Exception e) { Synth.handleException(e); }
        }


    long lastMIDISend = 0;
    // this is different from the simple pause in that it only pauses
    // if that much time hasn't already transpired between midi sends
    void midiPause(long expectedPause)
        {
        if (expectedPause <= 0) return;
        
        long pauseSoFar = System.nanoTime() - lastMIDISend;
        if (pauseSoFar >= 0 && pauseSoFar < expectedPause)
            {
            long pause = expectedPause - pauseSoFar;
            // verify that pause is rational
            if (pause < 0L) pause = 0L;
            if (pause > 100000000L) pause = 100000000L;  // 10ms, still within the int range and not so slow as to make the UI impossible
            try { Thread.currentThread().sleep((int)(pause / 1000000), (int)(pause % 1000000)); }
            catch (Exception e) { Synth.handleException(e); }
            }
        }
    
    boolean midiDebug = false;
    
    Object[] midiSendLock = new Object[0];

    /** Attempts to send a NON-Sysex MIDI message. Returns false if (1) the data was empty or null (2)
        synth has turned off the ability to send temporarily (3) the sysex message is not
        valid (4) an error occurred when the receiver tried to send the data.  */
    public boolean tryToSendMIDI(MidiMessage message)
        {
        if (midiDebug)
            {
            System.err.println("MIDI " + (message == null ? "NULL" : Midi.format(message)));
            }
                
        if (message == null) 
            { return false; }
        else if (!amActiveSynth())
            { return false; }
        else if (getSendMIDI())
            {
            if (tuple == null) return false;
            
            Receiver receiver = tuple.out;
            if (receiver == null) return false;
            
            // compute pause
            try { if (!noMIDIPause) midiPause(getNanoPauseBetweenMIDISends()); }
            catch (Exception e)
                {
                Synth.handleException(e);
                }
                                   
            synchronized(midiSendLock) 
                {
                try
                    {
                    receiver.send(message, -1);
                    }
                catch (IllegalStateException e)
                    {
                    // This happens when the device has closed itself and we're still trying to send to it.
                    // For example if the user rips the USB cord for his device out of the laptop.  In this
                    // case we'll also disconnect
                    SwingUtilities.invokeLater(new Runnable()
                        {
                        public void run() { doDisconnectMIDI(); }
                        });
                    return false;
                    }
                }      
            lastMIDISend = System.nanoTime();
            return true;
            }
        else
            return false;
        }
           
    /** If you are sending a sysex message as fragments with pauses in-between them,
        what is the length of the pause?  By default this is 0 (no pause). */
    public int getPauseBetweenSysexFragments() { return 0; }

    /** Indicates that sysex messages are not sent as fragments. */
    public static int NO_SYSEX_FRAGMENT_SIZE = 0;
    
    /** If you are sending a sysex message as fragments with pauses in-between them,
        how large are the fragments? By default, this is NO_SYSEX_FRAGMENT_SIZE. */
    public int getSysexFragmentSize() { return NO_SYSEX_FRAGMENT_SIZE; }    
                        
    /** Attempts to send a single MIDI sysex message. Returns false if (1) the data was empty or null (2)
        synth has turned off the ability to send temporarily (3) the sysex message is not
        valid (4) an error occurred when the receiver tried to send the data.  */
    public boolean tryToSendSysex(byte[] data)
        {
        if (data == null || data.length == 0) 
            return false;
        else if (!amActiveSynth())
            {
            return false;
            }
        
        boolean foundHighByte = false;
        for(int i = 1; i < data.length - 1; i++)
            {
            if (data[i] < 0)  // uh oh, high byte
                {
                if (!foundHighByte)             // print out some stuff
                    {
                    String s = "";
                    int start = 0; // = i - 20;
                    if (start < 0) start = 0;
                    for(int j = start; j <= i; j++)
                        s = s + j + ": " + String.format("%02X", data[j]) + " " + (data[j] < 0 ? data[j] + 256 : data[j]) + " " +
                            (data[j] >= 32 && data[j] < 127 ? (char)data[j] : "") + "\n";
                    Synth.handleException(new RuntimeException("High byte in sysex found.  First example is byte #" + i + "\n" + s));
                    foundHighByte = true;
                    }
                else
                    {
                    System.err.println("Second high byte in sysex found.  Byte is #" + i);
                    }
                                        
                // try to fix things
                if (data[i] != 0xF7)
                    data[i] = (byte)(data[i] & 127);
//                break;
                }
            }

        if (getSendMIDI())
            {
            if (tuple == null) return false;
            Receiver receiver = tuple.out;
            if (receiver == null) return false;

            // compute pause
            midiPause(getNanoPauseBetweenMIDISends());
                                        
            try { 
                SysexMessage message = new SysexMessage(data, data.length);
                synchronized(midiSendLock)
                    {
                    int fragmentSize = getSysexFragmentSize();
                    if (fragmentSize <= NO_SYSEX_FRAGMENT_SIZE || message.getLength() <= fragmentSize)
                        {
                        receiver.send(message, -1); 
                        }
                    else
                        {
                        MidiMessage[] messages = Midi.DividedSysex.divide(message, fragmentSize);
                        for(int i = 0; i < messages.length; i++)
                            {
                            if (i > 0) simplePause(getPauseBetweenSysexFragments());
                            receiver.send(messages[i], -1);
                            }
                        }
                    }      
                lastMIDISend = System.nanoTime();
                return true; 
                }
            catch (InvalidMidiDataException e) { Synth.handleException(e); return false; }
            catch (IllegalStateException e2)
                {
                // This happens when the device has closed itself and we're still trying to send to it.
                // For example if the user rips the USB cord for his device out of the laptop.
                SwingUtilities.invokeLater(new Runnable()
                    {
                    public void run() { doDisconnectMIDI(); }
                    });
                return false;
                }
            }
        else
            return false;
        }
    
    /** If you get a index=0, outOf=0, we're done */
    public void sentMIDI(Object datum, int index, int outOf) { }
        
    /** Attempts to send several MIDI sysex or other kinds of messages.   Data elements can be
        one of four things: (1) null, which is essentially a no-op (2) a byte[], which indicates
        a sysex message (3) a fully constructed and populated MidiMessage (possibly including
        sysex messages), and (4) an Integer, which will be used to indicate a pause for that
        many milliseconds before sending the next message.  
        
        <p>Returns false if 
        (1) the data was empty or null 
        (2) the synth has turned off the ability to send temporarily
        (3) a message was not valid
        (4) an error occurred when the receiver tried to send the data.  
    */
    public boolean tryToSendMIDI(Object[] data)
        {
        if (data == null) return false;
        if (data.length == 0) return false;
        for(int i = 0; i < data.length; i++)
            {
            if (data[i] == null)
                {
                continue;
                }
            else if (data[i] instanceof Integer)
                {
                simplePause(((Integer)data[i]).intValue());
                continue;
                }
            else if (data[i] instanceof byte[])
                {
                byte[] sysex = (byte[])(data[i]);
                if (!tryToSendSysex(sysex))
                    { sentMIDI(null, 0, 0); return false; }
                }
            else if (data[i] instanceof MidiMessage)
                {
                MidiMessage message = (MidiMessage)(data[i]);
                if (!tryToSendMIDI(message))
                    { sentMIDI(null, 0, 0); return false; }
                }
            sentMIDI(data[i], i, data.length);
            }
        sentMIDI(null, 0, 0);
        return true;
        }






    void handleInRawCC(ShortMessage message)
        {
        if (getExpectsRawCCFromSynth())
            {
            handleSynthCCOrNRPN(midi.synthParser.handleRawCC(message.getChannel(), message.getData1(), message.getData2()));
            }
        else
            {
            Midi.CCData ccdata = midi.synthParser.processCC(message, false, false);
            if (ccdata != null)
                {
                handleSynthCCOrNRPN(ccdata);
                }
            }
        }
        
    void handleKeyRawCC(ShortMessage message)
        {
        Midi.CCData ccdata = midi.controlParser.processCC(message, false, false);
        if (ccdata != null)
            {
            if (ccdata.type == Midi.CCDATA_TYPE_NRPN)
                {
                ccdata.number += CCMap.NRPN_OFFSET;
                }
                
            if (learning)
                {
                String key = model.getLastKey();
                if (key != null)
                    {
                    int sub = getCurrentTab();
                    if (perChannelCCs)
                        sub = ccdata.channel;
                    ccmap.setKeyForCCPane(ccdata.number, sub, key);
                    if (ccdata.type == Midi.CCDATA_TYPE_NRPN)
                        ccmap.setTypeForCCPane(ccdata.number, sub, CCMap.TYPE_NRPN);  // though it doesn't really matter
                    else
                        ccmap.setTypeForCCPane(ccdata.number, sub, learningType);
                    setLearningCC(false);
                    }
                }
            else if (tabs.getSelectedComponent() == morphPane)
                {
                morph.handleCC(ccdata);
                }
            else
                {
                int sub = getCurrentTab();
                if (perChannelCCs)
                    sub = ccdata.channel;
                String key = ccmap.getKeyForCCPane(ccdata.number, sub);
                if (key != null)
                    {
                    // handle increment/decrement
                    if (ccdata.increment)
                        {
                        ccdata.value = ccdata.value + model.get(key);
                        }

                    // handle the situation where the range is larger than the CC/NRPN message,
                    // else bump it to min
                    if (model.minExists(key) && model.maxExists(key))
                        {
                        if (ccdata.type == Midi.CCDATA_TYPE_RAW_CC)
                            {
                            int type = ccmap.getTypeForCCPane(ccdata.number, sub);
                            int min = model.getMin(key);
                            int max = model.getMax(key);
                            int val = model.get(key);
                                                        
                            if (type == CCMap.TYPE_ABSOLUTE_CC)
                                {
                                if (max - min + 1 > 127)  // uh oh
                                    {
                                    ccdata.value = (int)(((max - min + 1) / (double) 127) * ccdata.value);
                                    }
                                else
                                    {
                                    ccdata.value = min + ccdata.value;
                                    }
                                }
                            else if (type == CCMap.TYPE_RELATIVE_CC_64)
                                {
                                ccdata.value = val + ccdata.value - 64;
                                }
                            else if (type == CCMap.TYPE_RELATIVE_CC_0)
                                {
                                if (ccdata.value < 64)
                                    ccdata.value = val + ccdata.value;
                                else
                                    ccdata.value = val + ccdata.value - 128;
                                }
                            else
                                {
                                throw new RuntimeException("This Shouldn't Happen");
                                }
                            }
                        else if (ccdata.type == Midi.CCDATA_TYPE_NRPN)
                            {
                            int min = model.getMin(key);
                            int max = model.getMax(key);
                            if (max - min + 1 > 16383)  // uh oh, but very unlikely
                                {
                                ccdata.value = (int)(((max - min + 1) / (double) 16383) * ccdata.value);
                                }
                            else
                                {
                                ccdata.value = min + ccdata.value;
                                }
                            }
                        }

                    model.setBounded(key, ccdata.value);
                    }
                }
            }
        }

    /** Merges in a dumped patch with the existing one and returns TRUE.
        In some rare cases, such as for the TX81Z, merging requires multiple
        sysex dumps to come back.  In this case, if not all the dumps have
        arrived (a merge call will be made for each one), return FALSE, until
        you finally have collected enough data to do a merge, at which point
        you should return super.merge(revisedData, probability). */
    
    Synth mergeSynth = null;
    public int merge(byte[] data, double probability)
        {
        setSendMIDI(false);

        if (mergeSynth == null)
            {
            mergeSynth = instantiate(Synth.this.getClass(), true, false, tuple);
            }

        mergeSynth.setSendMIDI(false);
        mergeSynth.parsingForMerge = true;
        int result = PARSE_ERROR;
        
        try 
            {
            result = mergeSynth.performParse(data, false);
            }
        catch (Exception ex)
            {
            Synth.handleException(ex);
            // result is now PARSE_ERROR
            }
        
        mergeSynth.parsingForMerge = false;
        mergeSynth.setSendMIDI(true);
        
        if (result == PARSE_CANCELLED)
            {
            System.err.println("CANCELLED");
            // nothing
            mergeSynth = null;
            setSendMIDI(false);
            return result;
            }
        else if (result == PARSE_INCOMPLETE)
            {
            System.err.println("INCOMPLETE");
            setSendMIDI(false);
            return result;
            }
        else if (result == PARSE_FAILED)
            {
            showSimpleError("Merge Error", "Could not merge the patch.");
            mergeSynth = null;
            setSendMIDI(false);
            return result;
            }
        else if (result == PARSE_ERROR)
            {
            showSimpleError("Receive Error", "An error occurred on reading the patch.");
            mergeSynth = null;
            setSendMIDI(false);
            return result;
            }
        else
            {
            // this last statement fixes a mystery.  When I call Randomize or Reset on
            // a Blofeld or on a Microwave, all of the widgets update simultaneously.
            // But on a Blofeld Multi or Microwave Multi they update one at a time.
            // I've tried a zillion things, even moving all the widgets from the Blofeld Multi
            // into the Blofeld, and it makes no difference!  For some reason the OS X
            // repaint manager is refusing to coallesce their repaint requests.  So I do it here.
            repaint();

            undo.setWillPush(false);
            Model backup = (Model)(model.clone());
            model.recombine(random, mergeSynth.getModel(), getMutationKeys(), probability);
            revise();  // just in case
            mergeSynth = null;        
                                
            undo.setWillPush(true);
            if (!backup.keyEquals(getModel()))  // it's changed, do an undo push
                undo.push(backup);
            setSendMIDI(true);
            
            // sometimes synths don't have enough time after a random merge, so we pause here
            simplePause(getPauseAfterReceivePatch());
            sendAllParameters();

            return result;
            }
        }

    /** Returns the current channel (0--15, NOT 1--16) with which we are using to 
        communicate with the synth. If there is no MIDI tuple, this returns 0. */
    public int getChannelOut()
        {
        int channel = 0;
        if (tuple != null)
            channel = tuple.outChannel - 1;
        return channel;
        }

    /** Sends all the parameters in a patch to the synth.

        <p>If getSendsParametersOnlyOnSendCurrentPatch() is TRUE then this method
        does nothing.

        <p>If getSendsAllParametersAsDump() returns TRUE, then this is done by sending
        a single patch write to working memory, which may not be supported by all synths.
        
        Otherwise this is done by sending each parameter separately, which isn't as fast.
        The default sends each parameter separately.
    */     
    public final void sendAllParameters()
        {
        if (!getSendsParametersOnlyOnSendCurrentPatch())
            {
            sendAllParametersInternal();
            }
        }
        
    /** Sends all the parameters in a patch to the synth.

        <p>If getSendsAllParametersAsDump() returns TRUE, then this is done by sending
        a single patch write to working memory, which may not be supported by all synths.
        
        Otherwise this is done by sending each parameter separately, which isn't as fast.
        The default sends each parameter separately.
        
        <p>This method returns FALSE if no parameters were sent because MIDI is turned off.
        
        <p>If you override this method, be sure to call super.sendAllParametersInternal();
    */     
    protected boolean sendAllParametersInternal()
        {
        if (!getSendMIDI())
            return false;  // don't bother!  MIDI is off

        if (getSendsAllParametersAsDump())
            {
            boolean sent = tryToSendMIDI(emitAll(getModel(), true, false));
            if (sent)
                simplePause(getPauseAfterSendAllParameters());
            }
        else
            {
            sendDifferentParameters(null);
            }
        return true;
        }

    /** Individually sends all parameters for which the current model differs from the provided model.
        If the provided model is null, then all parameters are sent. */     
    public void sendDifferentParameters(Model other)
        {
        sendDifferentParameters(other, 1.0);
        }
        

    public void sendDifferentParameters(Model other, double probability)
        {
        if (!getSendMIDI())
            return;  // don't bother!  MIDI is off

        boolean sent = false;
        String[] keys = getModel().getKeys();

        if (other == null)
            {
            // Send every single parameter
            for(int i = 0; i < keys.length; i++)
                {
                if (probability != 1.0 && random.nextDouble() >= probability)
                    continue;

                if (sent = tryToSendMIDI(emitAll(keys[i], STATUS_SENDING_ALL_PARAMETERS)) || sent)
                    simplePause(getPauseAfterSendOneParameter());
                }
            }
        else
            {       
            // Send only diffs
            for(int i = 0; i < keys.length; i++)
                {
                if (probability != 1.0 && random.nextDouble() >= probability)
                    continue;
                
                if (getModel().isInteger(keys[i]))              // integers
                    {
                    if (getModel().get(keys[i], 0) != model.get(keys[i], 0))
                        {
                        if (sent = tryToSendMIDI(emitAll(keys[i], STATUS_SENDING_ALL_PARAMETERS)) || sent)
                            simplePause(getPauseAfterSendOneParameter());
                        }
                    }
                else            // strings
                    {
                    if (!(getModel().get(keys[i], "").equals(model.get(keys[i], ""))))
                        {
                        if (sent = tryToSendMIDI(emitAll(keys[i], STATUS_SENDING_ALL_PARAMETERS)) || sent)
                            simplePause(getPauseAfterSendOneParameter());
                        }
                    }
                }
            }
                        
        if (sent)
            simplePause(getPauseAfterSendAllParameters());
        }









    ////////// RECOGNITION



    /** Returns the recognizer for a given class name */
    public static Class getRecognizer(String synthClassName)
        {
        try
            {
            return Class.forName(synthClassName + "Rec");
            }
        catch (ClassNotFoundException ex)
            {
            Synth.handleException(ex);
            return null;
            }
        }
                
                
    // Recognize

    /** Returns whether the given sysex patch dump data is of the type for a synth of the given
        class.  This is done by ultimately calling the CLASS method 
        <tt>public static boolean recognize(data)</tt> that each synthesizer subclass is asked to implement. */
    public static boolean recognize(String synthClassName, byte[] data)
        {
        Class recognizer = getRecognizer(synthClassName);
        if (recognizer == null) 
            { 
            System.err.println("Synth.recognize() WARNING: No recognizer for " + synthClassName); 
            return false;
            }

        try
            {
            Method method = recognizer.getMethod("recognize", new Class[] { byte[].class });
            Object obj = method.invoke(null, data);
            return ((Boolean)obj).booleanValue();
            }
        catch (Exception e)
            {
            System.err.println("Synth.recognize(Class, byte[]) ERROR.  Could not obtain or invoke method for " + synthClassName); 
            Synth.handleException(e);
            return false;
            }
        }
    
    /** Returns whether the given sysex patch dump data is of the type for this particular synth.
        This is done by ultimately calling the CLASS method <tt>public static boolean recognize(data)</tt> 
        that your synthesizer subclass is asked to implement. */
    public final boolean recognizeLocal(byte[] data)
        {
        return recognize(Synth.this.getClass().getName(), data);
        }






    // Recognize Bulk

        
    /** Returns whether the given sysex patch dump data is a bank (multi-patch) dump of the type for a synth of the given
        class.  This is done by ultimately calling the CLASS method 
        <tt>public static boolean recognizeBank(data)</tt> that each synthesizer subclass is asked to implement. */
    public static boolean recognizeBank(String synthClassName, byte[] data)
        {
        Class recognizer = getRecognizer(synthClassName);
        if (recognizer == null) 
            { 
            System.err.println("Synth.recognizeBank() WARNING: No recognizer for " + synthClassName); 
            return false;
            }

        try
            {
            Method method = recognizer.getMethod("recognizeBank", new Class[] { byte[].class });
            Object obj = method.invoke(null, data);
            return ((Boolean)obj).booleanValue();
            }
        catch (Exception e)
            {
            return false;
            }
        }

    /** Returns whether the given sysex patch dump data is a bulk (multi-patch) dump of the type for this particular synth.
        This is done by ultimately calling the CLASS method <tt>public static boolean recognizeBank(data)</tt> 
        that your synthesizer subclass is asked to implement. */
    public final boolean recognizeBankLocal(byte[] data)
        {
        return recognizeBank(Synth.this.getClass().getName(), data);
        }



    // Getting a Sysex Patch Group
        

    /** Returns the position just after the end of the sysex group, starting at start,
        which represents a single patch for the synthesizer of the given class.  */
    public static int getNextSysexPatchGroup(String synthClassName, byte[][] sysex, int start)
        {
        Class recognizer = getRecognizer(synthClassName);
        if (recognizer == null) 
            { 
            System.err.println("Synth.getNextSysexPatchGroup() WARNING: No recognizer for " + synthClassName); 
            return start;
            }

        try
            {
            Method method = recognizer.getMethod("getNextSysexPatchGroup", new Class[] { byte[][].class, Integer.TYPE });
            Object obj = method.invoke(null, sysex, start);
            return ((Integer)obj).intValue();
            }
        catch (Exception e)
            {
            // There is no getNextSysexPatchGroup(...) method for this recognizer, so we assume that it's a single patch
            return start + 1;
            }
        }

    public final int getNextSysexPatchGroupLocal(byte[][] sysex, int start)
        {
        return getNextSysexPatchGroup(Synth.this.getClass().getName(), sysex, start);
        }
        
  
    
    // Bank Name

    /** Returns the name of the bank for a given synth.  This is pretty rare -- it only exists in the FB-01. */
    public static String getBankName(String synthClassName, byte[] data)
        {
        Class recognizer = getRecognizer(synthClassName);
        if (recognizer == null) 
            { 
            System.err.println("Synth.getBankName() WARNING: No recognizer for " + synthClassName); 
            return "--Error--";
            }

        try
            {
            Method method = recognizer.getMethod("getBankName", new Class[] { byte[].class });
            Object obj = method.invoke(null, data);
            return (String)obj;
            }
        catch (Exception e)
            {
            return "";
            }
        }
    
    /** Returns the name of the bank for a given synth.  This is pretty rare -- it only exists in the FB-01. */
    public final String getBankNameLocal(byte[] data)
        {
        return getBankName(Synth.this.getClass().getName(), data);
        }

      
      
      
        
    /// Recognition utilities
        
  
    /** Returns true if any of the given data contain sysex messages
        recognized by this synthesizer. */
    boolean recognizeAnyForLocal(byte[][] data)
        {
        for(int i = 0; i < data.length; i++)
            {
            if (recognizeLocal(data[i]))
                return true;
            }
        return false;
        }

    /** Returns the synthesizer which recognizes this sysex message, or -1 if none. */
    int recognizeSynthForSysex(byte[] data)
        {
        for(int i = 0; i < numSynths(); i++)
            {
            if (recognize(synthClassNames[i], data))
                return i;
            }
        return -1;
        }

    /** Returns all the synthesizers which recognize any message in this data. */
    int[] recognizeAnySynthForSysex(byte[][] data)
        {
        boolean[] recognized = new boolean[numSynths()];

        int lastSynth = 0;
        for(int i = 0; i < data.length; i++)
            {
            // a little caching
            if (recognize(synthClassNames[lastSynth], data[i]))
                {
                recognized[lastSynth] = true;
                continue;
                }
                                
            for(int j = 0; j < numSynths(); j++)
                {
                if (recognize(synthClassNames[j], data[i]))
                    {
                    recognized[j] = true;
                    lastSynth = j;
                    break;
                    }
                }
            }
                
        int count = 0;
        for(int i = 0; i < recognized.length; i++)
            if (recognized[i])
                count++;
                
        int[] result = new int[count];
        count = 0;
        for(int i = 0; i < recognized.length; i++)
            if (recognized[i])
                result[count++] = i;
                
        return result;
        }
                  
    


    ////////// GUI UTILITIES


    public int getNumTabs()
        {
        return tabs.getTabCount();
        }
        
    public int getSelectedTabIndex()
        {
        return tabs.getSelectedIndex();
        }
        
    /** Returns -1 if there is no such tab. */
    public int getIndexOfTabTitle(String title)
        {
        for(int i = 0; i < getNumTabs(); i++)
            {
            if (tabs.getTitleAt(i).equals(title))
                return i;
            }
        return -1;
        }
                        
    /** Returns FALSE if the tab could not be selected */
    public boolean setSelectedTabIndex(int index)
        {
        try
            {
            tabs.setSelectedIndex(index);
            return true;
            }
        catch (Exception e)
            {
            return false;
            }
        }
                
    public JComponent getSelectedTab()
        {
        return (JComponent)(tabs.getSelectedComponent());
        }
                
    public String getSelectedTabTitle()
        {
        return tabs.getTitleAt(tabs.getSelectedIndex());
        }
                
    public JComponent insertTab(String title, JComponent component, int index)
        {
        JScrollPane pane = new JScrollPane(component, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pane.setViewportBorder(null);
        pane.setBorder(null);
        tabs.insertTab(title, null, pane, null, index);
        return pane;
        }

    public JComponent addTab(String title, JComponent component)
        {
        JScrollPane pane = new JScrollPane(component, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pane.setViewportBorder(null);
        pane.setBorder(null);
        tabs.addTab(title, pane);
        return pane;
        }

    public void removeTab(String title)
        {
        int idx = tabs.indexOfTab(title);
        if (idx != -1)
            tabs.remove(idx);
        }


    boolean inSimpleError;

    /** Display a simple error message. */
    public void showSimpleError(String title, String message)
        {
        showSimpleError(this, title, message);
        }

    /** Display a simple error message. */
    public void showSimpleError(JComponent parent, String title, String message)
        {
        // A Bug in OS X (perhaps others?) Java causes multiple copies of the same Menu event to be issued
        // if we're popping up a dialog box in response, and if the Menu event is caused by command-key which includes
        // a modifier such as shift.  To get around it, we're just blocking multiple recursive message dialogs here.
        
        if (inSimpleError) return;
        inSimpleError = true;
        disableMenuBar();
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
        enableMenuBar();
        inSimpleError = false;
        }

    /** Display a simple error message. */
    public void showErrorWithStackTrace(Throwable error, String title, String message)
        {
        if (!ExceptionDump.lastThrowableExists())
            {
            System.err.println("WARNING: error with stack trace requested but there's no Throwable");
            Synth.handleException(error);
            showSimpleError(title, message);
            }
        else
            {
            // A Bug in OS X (perhaps others?) Java causes multiple copies of the same Menu event to be issued
            // if we're popping up a dialog box in response, and if the Menu event is caused by command-key which includes
            // a modifier such as shift.  To get around it, we're just blocking multiple recursive message dialogs here.
                
            if (inSimpleError) return;
            inSimpleError = true;
            disableMenuBar();
            String[] options = new String[] { "Okay", "Save Error" };
            int ret = JOptionPane.showOptionDialog(this, message, title, JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
            enableMenuBar();
            inSimpleError = false;
                
            if (ret == 1)
                {
                ExceptionDump.saveThrowable(this);
                }
            }
        }

    /** Display a simple error message. */
    public void showErrorWithStackTrace(String title, String message)
        {
        showErrorWithStackTrace(null, title, message);
        }

    /** Display a simple error message. */
    public void showSimpleMessage(String title, String message)
        {
        // A Bug in OS X (perhaps others?) Java causes multiple copies of the same Menu event to be issued
        // if we're popping up a dialog box in response, and if the Menu event is caused by command-key which includes
        // a modifier such as shift.  To get around it, we're just blocking multiple recursive message dialogs here.
        
        if (inSimpleError) return;
        inSimpleError = true;
        disableMenuBar();
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
        enableMenuBar();
        inSimpleError = false;
        }

    /** Display a simple (OK / Cancel) confirmation message.  Return the result (ok = true, cancel = false). */
    public boolean showSimpleConfirm(String title, String message)
        {
        disableMenuBar();
        boolean ret = (JOptionPane.showConfirmDialog(Synth.this, message, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null) == JOptionPane.OK_OPTION);
        enableMenuBar();
        return ret;
        }
        
    /** Display a simple (OK-OPTION / Cancel) confirmation message.  Return the result (ok = true, cancel = false). */
    public boolean showSimpleConfirm(String title, String message, String okOption)
        {
        disableMenuBar();
        int ret = JOptionPane.showOptionDialog(Synth.this, message, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
            new String[] { okOption, "Cancel" } , okOption);
        enableMenuBar();
        return (ret == 0);
        }
        
    /** Perform a JOptionPane confirm dialog with MUTLIPLE widgets that the user can select.  The widgets are provided
        in the array WIDGETS, and each has an accompanying label in LABELS.   You specify what BUTTONS appear along the bottom
        as the OPTIONS, which (on the Mac) appear right-to-left. You also get to specify well as the default option -- what
        button is chosen if the user presses RETURN.  On the Mac, classically this is the first (rightmost) option. 
        Returns the option number selected; otherwise returns -1 if the user clicked the close box. */
    public static int showMultiOption(Synth synth, String[] labels, JComponent[] widgets, String[] options, int defaultOption, String title, JComponent message)
        {
        WidgetList list = new WidgetList(labels, widgets);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(new JLabel("    "), BorderLayout.NORTH);
        p.add(message, BorderLayout.CENTER);
        p.add(new JLabel("    "), BorderLayout.SOUTH);
        panel.add(list, BorderLayout.CENTER);
        panel.add(p, BorderLayout.NORTH);

        if (synth != null) synth.disableMenuBar();
        int ret = JOptionPane.showOptionDialog(synth, panel, title, JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[defaultOption]);
        if (synth != null) synth.enableMenuBar();
        return ret;
        }

    /** Perform a JOptionPane confirm dialog with MUTLIPLE widgets that the user can select.  The widgets are provided
        in the array WIDGETS, and each has an accompanying label in LABELS.   You specify what BUTTONS appear along the bottom
        as the OPTIONS, which (on the Mac) appear right-to-left. You also get to specify well as the default option -- what
        button is chosen if the user presses RETURN.  On the Mac, classically this is the first (rightmost) option. 
        Returns the option number selected; otherwise returns -1 if the user clicked the close box. */
    public static int showMultiOption(Synth synth, String[] labels, JComponent[] widgets, String[] options, int defaultOption, String title, String message)
        {
        return showMultiOption(synth, labels, widgets, options, defaultOption, title, new JLabel(message));
        }


    /** Perform a JOptionPane confirm dialog with MUTLIPLE widgets that the user can select.  The widgets are provided
        in the array WIDGETS, and each has an accompanying label in LABELS.   Returns TRUE if the user performed
        the operation, FALSE if cancelled. */
    public static boolean showMultiOption(Synth synth, String[] labels, JComponent[] widgets, String title, String message)
        {
        return showMultiOption(synth, labels, widgets, new String[] { "Okay", "Cancel" }, 0, title, message) == 0;
        }


    /** Perform a JOptionPane confirm dialog with MUTLIPLE widgets that the user can select.  The widgets are provided
        in the array WIDGETS, and each has an accompanying label in LABELS.   Returns TRUE if the user performed
        the operation, FALSE if cancelled. */
    public static boolean showMultiOption(Synth synth, String[] labels, JComponent[] widgets, String title, JComponent message)
        {
        return showMultiOption(synth, labels, widgets, new String[] { "Okay", "Cancel" }, 0, title, message) == 0;
        }
           
    /** Returns the synth name to be used in the title bar. */
    public String getTitleBarSynthName() { return getSynthNameLocal(); }

    /** Updates the JFrame title to reflect the synthesizer type, the patch information, and the filename if any. */
    public void updateTitle()
        {
        JFrame frame = ((JFrame)(SwingUtilities.getRoot(this)));
        if (frame != null) 
            {
            String synthName = getTitleBarSynthName().trim();
            String fileName = (file == null ? 
                    (auxFile == null ? "        Untitled" : 
                    "        (" + auxFile.getName() + ")") :
                "        " + file.getName());
            String disconnectedWarning = ((tuple == null || tuple.in == null) ? "   DISCONNECTED" : "");
            String downloadingWarning = (patchTimer != null ? "   DOWNLOADING" : "");
            String learningWarning = (learning ? "   LEARNING" +
                    (model.getLastKey() != null ? " " + model.getLastKey() + 
                    (model.getRange(model.getLastKey()) > 0 ? "[" + model.getRange(model.getLastKey()) + "]" : "") + 
                        (ccmap.getCCForKey(model.getLastKey()) >= 0 ? "=" + nameForCC(ccmap.getCCForKey(model.getLastKey()),
                            ccmap.getPaneForKey(model.getLastKey())) : "")
                    : "") : "");
            String restrictingWarning = (isShowingMutation() ? "   MUTATION PARAMETERS" : "");
        
            frame.setTitle(synthName + fileName + "        " + disconnectedWarning + downloadingWarning + learningWarning + restrictingWarning);
            }
        }
                
    public int getCurrentTab()
        {
        return tabs.getSelectedIndex();
        }
                
    public void setCurrentTab(int tab)
        {
        //        int len = tabs.getTabCount();
        if (tab >= tabs.getTabCount())
            return;
        if (tab < 0)
            return;
        tabs.setSelectedIndex(tab);
        }
    
        
    
    public int readFully(byte[] array, InputStream input)
        {
        int current = 0;
        try
            {
            while(true)
                {
                int total = input.read(array, current, array.length - current);
                if (total <= 0) break;
                current += total;
                }
            }
        catch (IOException ex) { Synth.handleException(ex); }
        return current;
        }
        
        
        
        
    ////////// DEFAULTS
        
        
    
    // Note that this isn't wrapped in undo, so we can block it at instantiation
    public void loadDefaults()
        {
        String defaultResourceFileName = getDefaultResourceFileName();
        if (defaultResourceFileName == null) return;
        
        InputStream stream = getClass().getResourceAsStream(getDefaultResourceFileName());
        if (stream != null)
            {
            try 
                {
                // this last statement fixes a mystery.  When I call Randomize or Reset on
                // a Blofeld or on a Microwave, all of the widgets update simultaneously.
                // But on a Blofeld Multi or Microwave Multi they update one at a time.
                // I've tried a zillion things, even moving all the widgets from the Blofeld Multi
                // into the Blofeld, and it makes no difference!  For some reason the OS X
                // repaint manager is refusing to coallesce their repaint requests.  So I do it here.
                repaint();

                byte[] buffer = new byte[MAX_FILE_LENGTH];   // better not be longer than this
                int size = readFully(buffer, stream);

                // now shorten
                byte[] data = new byte[size];
                System.arraycopy(buffer, 0, data, 0, size);

                // parse                        
                setSendMIDI(false);
                performParse(data, true);
                setSendMIDI(true);
                model.setUndoListener(undo);    // okay, redundant, but that way the pattern stays the same
                }
            catch (Exception e)
                {
                Synth.handleException(e);
                }
            finally
                {
                try { stream.close(); }
                catch (IOException e) { }
                }
            }
        else
            {
            System.err.println("Warning (Synth): Didn't Parse");
            }
        }  


    /** Given a preferences path X for a given synth, sets X to have the given value.. 
        Also sets the global path X to the value.  Typically this method is called by a
        a cover function (see for example setLastSynth(...) ) */
    static final void setLastX(String value, String x, String synthName)
        {
        setLastX(value, x, synthName, false);
        }

    /** Given a preferences path X for a given synth, sets X to have the given value.. 
        Also sets the global path X to the value.  Typically this method is called by a
        a cover function (see for example setLastSynth(...) ) */
    public static void setLastX(String value, String x, String synthName, boolean onlySetInSynth)
        {
        if (synthName != null)
            {
            java.util.prefs.Preferences app_p = Prefs.getAppPreferences(synthName, "Edisyn");
            app_p.put(x, value);
            Prefs.save(app_p);
            }
        if (!onlySetInSynth)
            {
            setLastX(value, x);
            }
        }
        
    /** Given a preferences path X for a given synth, sets X to have the given value.. 
        Also sets the global path X to the value.  Typically this method is called by a
        a cover function (see for example setLastSynth(...) ) */
    static final void setLastX(String value, String x)
        {
        java.util.prefs.Preferences global_p = Prefs.getGlobalPreferences("Data");
        global_p.put(x, value);
        Prefs.save(global_p);
        }
        
    /** Given a preferences path X for a given synth, returns the value stored in X.
        If there is no such value, then returns the value stored in X in the globals.
        If there again is no such value, returns null.  Typically this method is called by a
        a cover function (see for example getLastSynth(...) ) */
    public static final String getLastX(String x, String synthName)
        {
        return getLastX(x, synthName, false);
        }
    
    /** Given a preferences path X for a given synth, returns the value stored in X.
        If there is no such value, then returns the value stored in X in the globals.
        If there again is no such value, returns null.  Typically this method is called by a
        a cover function (see for example getLastSynth(...) ) */
    public static String getLastX(String x, String synthName, boolean onlyGetFromSynth)
        {
        String lastDir = null;
        if (synthName != null)
            {
            lastDir = Prefs.getAppPreferences(synthName, "Edisyn").get(x, null);
            }
        
        if (!onlyGetFromSynth && lastDir == null)
            {
            lastDir = getLastX(x);
            }
                
        return lastDir;         
        }
        
    private static final String getLastX(String x)
        {
        return Prefs.getGlobalPreferences("Data").get(x, null);
        }
    
    /** Given a preferences path X for a given synth, checks to see if "true" is stored
        there.  If so, returns true.  If not, sets "true" in that location and returns false.
        This can be used to do once-only things like this:
        <p><tt>
        if (!checkAndSet("OnceOnlyExample, getSynthNameLocal()))   <br>
        { showSimpleError("Warning!", "This Warning will only appear once!");  }
    */
    public static boolean checkAndSet(String x, String synthName)
        {
        String val = getLastX(x, synthName, true);
        if (val == null || "false".equalsIgnoreCase(val))
            {
            setLastX("true", x, synthName, true);
            return false;
            }
        return true;
        }

    /** Given a preferences path X for a given synth, returns the value stored in X as an integer.
        If there is no such value, then returns the value stored in X in the globals if !getFromSynthOnly.
        If there again is no such value, returns defaultVal. */
    public static boolean getLastXAsBoolean(String slot, String synth, boolean defaultVal, boolean getFromSynthOnly)
        {
        String val = getLastX(slot, synth, getFromSynthOnly);
        if ("false".equalsIgnoreCase(val))
            {
            return false;
            }
        else if ("true".equalsIgnoreCase(val))
            {
            return true;
            }
        else return defaultVal;
        }

    /** Given a preferences path X for a given synth, returns the value stored in X as an integer.
        If there is no such value, then returns the value stored in X in the globals if !getFromSynthOnly.
        If there again is no such value, returns defaultVal. */
    public static int getLastXAsInt(String slot, String synth, int defaultVal, boolean getFromSynthOnly)
        {
        String tnls = getLastX(slot, synth, getFromSynthOnly);
        try
            {
            return Integer.parseInt(tnls);
            }
        catch (NumberFormatException e)
            {
            return defaultVal;
            }
        catch (NullPointerException e2)
            {
            return defaultVal;
            }
        }

    /** Given a preferences path X for a given synth, returns the value stored in X as a double.
        If there is no such value, then returns the value stored in X in the globals if !getFromSynthOnly.
        If there again is no such value, returns defaultVal. */
    public static double getLastXAsDouble(String slot, String synth, double defaultVal, boolean getFromSynthOnly)
        {
        String tnls = getLastX(slot, synth, getFromSynthOnly);
        try
            {
            return Double.parseDouble(tnls);
            }
        catch (NumberFormatException e)
            {
            return defaultVal;
            }
        catch (NullPointerException e2)
            {
            return defaultVal;
            }
        }
                

   
    // sets the last directory used by load, save, or save as
    public void setLastDirectory(String path) { setLastX(path, "LastDirectory", getSynthNameLocal(), false); }
    // sets the last directory used by load, save, or save as
    public String getLastDirectory() { return getLastX("LastDirectory", getSynthNameLocal(), false); }
    
    // sets the last synthesizer opened to the given classname
    public static void setLastSynth(String synth) { 
        setLastX(synth, "Synth", null, false); 
        }
    // returns the classname of the last synthesizer opened
    public static String getLastSynth() { return getLastX("Synth", null, false); }
        
    public static Color getLastColor(String key, Color defaultColor)
        {
        String val = getLastX(key);
        if (val == null) { return defaultColor; }
        Scanner scan = new Scanner(val);
        if (!scan.hasNextInt()) { return defaultColor; }
        int red = scan.nextInt();
        if (!scan.hasNextInt()) { return defaultColor; }
        int green = scan.nextInt();
        if (!scan.hasNextInt()) { return defaultColor; }
        int blue = scan.nextInt();
        if (red < 0 || green < 0 || blue < 0 || red > 255 || green > 255 || blue > 255) { return defaultColor; }
        return new Color(red, green, blue);
        }
                
    static void setLastColor(String key, Color color)
        {
        if (color == null) return;
        String val = "" + color.getRed() + " " + color.getGreen() + " " + color.getBlue();
        setLastX(val, key);
        }
 
 
 
 











    ///////////    SPROUT, WINDOW, AND MENU HANDLING



    public static Window lastActiveWindow = null;

    /** Temporarily sets me to be the active synth. */
    public void setActiveSynth(boolean val) { activeSynth = val; }
    boolean activeSynth = false;
        
    /** Returns true if my synth panel is the frontmost and is active. */
    public boolean amActiveSynth()
        {
        if (activeSynth) return true;
        
        Window activeWindow = javax.swing.FocusManager.getCurrentManager().getActiveWindow();
        Component synthWindow = SwingUtilities.getRoot(Synth.this);
                                        
        // we want to be either the currently active window, the parent of a dialog box which is the active window, or the last active window if the user is doing something else
        return (synthWindow == activeWindow || (activeWindow != null && synthWindow == activeWindow.getOwner()) ||
            (activeWindow == null && lastActiveWindow == synthWindow));
        }



    public void resetColors()
        {
        setLastColor("background-color", Style.DEFAULT_BACKGROUND_COLOR);
        setLastColor("text-color", Style.DEFAULT_TEXT_COLOR);
        setLastColor("a-color", Style.DEFAULT_COLOR_A);
        setLastColor("b-color", Style.DEFAULT_COLOR_B);
        setLastColor("c-color", Style.DEFAULT_COLOR_C);
        setLastColor("dynamic-color", Style.DEFAULT_DYNAMIC_COLOR);
        setLastColor("unset-color", Style.DEFAULT_UNSET_COLOR);
        Style.updateColors();
        }
                
    public void setupColors()
        {
        Color backgroundColor = getLastColor("background-color", Style.DEFAULT_BACKGROUND_COLOR);
        Color textColor = getLastColor("text-color", Style.DEFAULT_TEXT_COLOR);
        Color aColor = getLastColor("a-color", Style.DEFAULT_COLOR_A);
        Color bColor = getLastColor("b-color", Style.DEFAULT_COLOR_B);
        Color cColor = getLastColor("c-color", Style.DEFAULT_COLOR_C);
        Color dynamicColor = getLastColor("dynamic-color", Style.DEFAULT_DYNAMIC_COLOR);
        Color envelopeColor = getLastColor("envelope-color", Style.DEFAULT_ENVELOPE_COLOR);
        Color dialColor = getLastColor("unset-color", Style.DEFAULT_UNSET_COLOR);
                        
        ColorWell background = new ColorWell(backgroundColor);
        ColorWell text = new ColorWell(textColor);
        ColorWell a = new ColorWell(aColor);
        ColorWell b = new ColorWell(bColor);
        ColorWell c = new ColorWell(cColor);
        ColorWell dynamic = new ColorWell(dynamicColor);
        ColorWell envelope = new ColorWell(dynamicColor);
        ColorWell dial = new ColorWell(dialColor);
        
        disableMenuBar();
        boolean result = Synth.showMultiOption(this, 
            new String[] { "Background  ", "Text  ", "Color A  ", "Color B  ", "Color C  ", "Highlights  ",  "Dials  ", "Envelopes  " },  
            new JComponent[] { background, text, a, b, c, dynamic, dial, envelope }, 
            "Update Colors", 
            "<html><font size='-1'>Note: after changing colors, currently<br>open windows may look scrambled,<br>but new windows will look correct.</font></html>");
        enableMenuBar();
                             
        if (result)
            {
            setLastColor("background-color", background.getColor());
            setLastColor("text-color", text.getColor());
            setLastColor("a-color", a.getColor());
            setLastColor("b-color", b.getColor());
            setLastColor("c-color", c.getColor());
            setLastColor("dynamic-color", dynamic.getColor());
            setLastColor("unset-color", dial.getColor());
            setLastColor("envelope-color", envelope.getColor());
            System.err.println(envelope.getColor());
            Style.updateColors();
            }
        }

    /** Returns the currently displayed SynthPanel. */
    public SynthPanel findPanel()
        {
        Component tab = tabs.getSelectedComponent();
        if (tab instanceof JScrollPane)
            {
            Component inner = ((JScrollPane)tab).getViewport().getView();
            if (inner instanceof SynthPanel)
                {
                return (SynthPanel) inner;
                }
            }
        return null;
        }


    /** Called when a tab has been changed, to update various stuff. */
    public void tabChanged()
        {
        // cancel learning
        setLearningCC(false);
        
        Component tab = tabs.getSelectedComponent();
        if (tab == hillClimbPane)
            {
            morph.shutdown();
            hillClimb.startup();
            }
        else if (tab == morphPane)
            {
            hillClimb.shutdown();
            morph.startup();
            }
        else
            {
            morph.shutdown();
            hillClimb.shutdown();
            }
            
        pasteTab.setEnabled(false);
        pasteMutableTab.setEnabled(false);
        copyTab.setEnabled(false);
        copyMutableTab.setEnabled(false);
        resetTab.setEnabled(false);
        
        SynthPanel panel = findPanel();
        if (panel != null)
            {
            if (panel.isPasteable())
                {
                copyTab.setEnabled(true);
                copyMutableTab.setEnabled(true);
                }
            if (!panel.isUnresettable())
                {
                resetTab.setEnabled(true);
                }
            if (panel.isPasteCompatible(getCopyPreamble()))
                {
                pasteTab.setEnabled(true);
                pasteMutableTab.setEnabled(true);
                }
            }
        }


    ArrayList<JMenuItem> disabledMenus = null;
    int disableCount;
    /** Disables the menu bar.  disableMenuBar() and enableMenuBar() work in tandem to work around
        a goofy bug in OS X: you can't disable the menu bar and reenable it: it won't reenable
        unless the application loses focus and regains it, and even then sometimes it won't work.
        These functions work properly however.  You want to disable and enable the menu bar because
        in OS X the menu bar still functions even when in a modal dialog!  Bad OS X Java errors.
    */
    public void disableMenuBar()
        {
        if (disabledMenus == null)
            {
            disabledMenus = new ArrayList<JMenuItem>();
            disableCount = 0;
            JFrame ancestor = ((JFrame)(SwingUtilities.getWindowAncestor(this)));
            if (ancestor == null) return;
            JMenuBar bar = ancestor.getJMenuBar();
            for(int i = 0; i < bar.getMenuCount(); i++)
                {
                JMenu menu = bar.getMenu(i);
                if (menu != null)
                    {
                    for(int j = 0; j < menu.getItemCount(); j++)
                        {
                        JMenuItem item = menu.getItem(j);
                        if (item != null && item.isEnabled())
                            {
                            disabledMenus.add(item);
                            item.setEnabled(false);
                            }
                        }
                    }
                }
            }
        else
            {
            disableCount++;
            return;
            }
        }       
        
    /** Enables the menu bar.  disableMenuBar() and enableMenuBar() work in tandem to work around
        a goofy bug in OS X: you can't disable the menu bar and reenable it: it won't reenable
        unless the application loses focus and regains it, and even then sometimes it won't work.
        These functions work properly however.  You want to disable and enable the menu bar because
        in OS X the menu bar still functions even when in a modal dialog!  Bad OS X Java errors.
    */
    public void enableMenuBar()
        {
        if (disableCount == 0)
            {
            for(int i = 0; i < disabledMenus.size(); i++)
                {
                disabledMenus.get(i).setEnabled(true);
                }
            disabledMenus = null;
            }
        else
            {
            disableCount--;
            }
        }       


    /** Produces the JFrame for a Synth and displays it.  This generally builds the entire
        menu subsystem as well, so it's pretty involved.  */
    public JFrame sprout()
        {
        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
        tabs.addChangeListener(new ChangeListener()
            {
            public void stateChanged(ChangeEvent e)
                {
                tabChanged();
                }
            });
        hillClimb = new HillClimb(this);
        morph = new Morph(this);

        String html = getHTMLResourceFileName();
        if (html != null)
            {
            try
                {
                tabs.addTab("About", new HTMLBrowser(this.getClass().getResourceAsStream(html)));
                }
            catch (Exception ex)
                {
                System.err.println("Error locating HTML file " + html);
                }
            }

        final JFrame frame = new JFrame();
        menubar = new JMenuBar();
        frame.setJMenuBar(menubar);
        JMenu menu = new JMenu("File");
        menubar.add(menu);

        JMenuItem _new = new JMenuItem("New " + getSynthNameLocal());
        _new.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(_new);
        _new.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                doNew();
                }
            });

        JMenu newSynth = favorites.buildNewSynthMenu(this);
        menu.add(newSynth);

        JMenuItem _copy = new JMenuItem("Duplicate Synth");
        _copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(_copy);
        _copy.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                doDuplicateSynth();
                }
            });
                
        JMenuItem open = new JMenuItem("Load...");
        open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(open);
        open.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setActiveSynth(true);
                if (doOpen(false) && getSendsParametersAfterLoad())
                    sendAllParameters();
                setActiveSynth(false);
                }
            });

        JMenuItem openAndMerge = new JMenuItem("Load and Merge...");
        menu.add(openAndMerge);
        openAndMerge.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setMergeProbability(1.0);
                setActiveSynth(true);
                if (doOpen(true) && getSendsParametersAfterLoad())
                    sendAllParameters();
                setActiveSynth(false);
                setMergeProbability(0.0);
                }
            });
        menu.addSeparator();

        JMenuItem close = new JMenuItem("Close Window");
        close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(close);
        close.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doCloseWindow();
                }
            });
                
        menu.addSeparator();

        JMenuItem save = new JMenuItem("Save");
        save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(save);
        save.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doSave();
                }
            });
                
        JMenuItem saveAs = new JMenuItem("Save As...");
        menu.add(saveAs);
        saveAs.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doSaveAs();
                }
            });

        menu.addSeparator();

        getAll = new JMenuItem("Batch Download...");
        menu.add(getAll);
        getAll.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doGetAllPatches();
                }
            });

        if (getPatchLocationName(getModel()) == null)
            {
            // not implemented. :-(
            getAll.setEnabled(false);
            }

        JMenuItem saveToText = new JMenuItem("Export to Text...");
        menu.add(saveToText);
        saveToText.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doSaveText();
                }
            });
            
        if (!Style.isMac())
            {
            JMenuItem exit = new JMenuItem("Exit");
            menu.addSeparator();
            menu.add(exit);
            exit.addActionListener(new ActionListener()
                {
                public void actionPerformed( ActionEvent e)
                    {
                    doQuit();
                    }
                });
            }

        menu = new JMenu("Edit");
        menubar.add(menu);
                
        undoMenu = new JMenuItem("Undo");
        undoMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(undoMenu);
        undoMenu.setEnabled(false);
        undoMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doUndo();
                }
            });
            
        redoMenu = new JMenuItem("Redo");
        redoMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | InputEvent.SHIFT_MASK));
        menu.add(redoMenu);
        redoMenu.setEnabled(false);
        redoMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doRedo();
                }
            });            
            
        
        menu.addSeparator();
        
        JMenuItem reset = new JMenuItem("Reset");
        menu.add(reset);
        reset.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doReset();
                }
            });

        JMenu randomize = new JMenu("Randomize");
        menu.add(randomize);
        
        if (this instanceof ProvidesNN)
        	{
        	nnRandomize = new JCheckBoxMenuItem("Randomizes using NN");
        	nnRandomize.setSelected(getLastXAsBoolean("RandomizeNN", getSynthName(), false, true));        
        	nnRandomize.addActionListener(new ActionListener()
            	{
            	public void actionPerformed( ActionEvent e)
               		{
               		setLastX("" + nnRandomize.isSelected(), "RandomizeNN", getSynthName(), true);
                	}
	            });
        	randomize.add(nnRandomize);
        	randomize.addSeparator();
        	}
        
        JMenuItem randomize1 = new JMenuItem("Randomize by 1%");
        randomize.add(randomize1);
        JMenuItem randomize2 = new JMenuItem("Randomize by 2%");
        randomize.add(randomize2);
        JMenuItem randomize5 = new JMenuItem("Randomize by 5%");
        randomize.add(randomize5);
        JMenuItem randomize10 = new JMenuItem("Randomize by 10%");
        randomize.add(randomize10);
        JMenuItem randomize25 = new JMenuItem("Randomize by 25%");
        randomize.add(randomize25);
        JMenuItem randomize50 = new JMenuItem("Randomize by 50%");
        randomize.add(randomize50);
        JMenuItem randomize100 = new JMenuItem("Randomize by 100%");
        randomize.add(randomize100);

        randomize.addSeparator();

        JMenuItem undoAndRandomize = new JMenuItem("Undo and Randomize Again");
        randomize.add(undoAndRandomize);

        randomize1.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doMutate(0.01);
                }
            });

        randomize2.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doMutate(0.02);
                }
            });

        randomize5.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doMutate(0.05);
                }
            });

        randomize10.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doMutate(0.1);
                }
            });

        randomize25.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doMutate(0.25);
                }
            });

        randomize50.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doMutate(0.5);
                }
            });

        randomize100.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doMutate(1.0);
                }
            });

        undoAndRandomize.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                if (undo.shouldShowUndoMenu())
                    {
                    if (lastMutate > 0.0)
                        {
                        doUndo(false);          // no reason to send it
                        doMutate(lastMutate);
                        }
                    else
                        {
                        showSimpleError("Undo", "Can't Undo and Randomize Again: no previous randomize!");
                        }
                    }
                else
                    {
                    showSimpleError("Undo", "Can't Undo and Randomize Again: no previous randomize!");
                    }
                }
            });
        undoAndRandomize.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));


        blend = new JMenu("Blend");
        JMenuItem blendOnce = new JMenuItem("Once");
        blend.add(blendOnce);
        blendOnce.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doBlendFull();
                }
            });
        JMenuItem blendOnceInRange = new JMenuItem("Once in Range...");
        blend.add(blendOnceInRange);
        blendOnceInRange.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doBlendInRange();
                }
            });
        JMenuItem undoAndBlend = new JMenuItem("Undo and Blend Again");
        blend.add(undoAndBlend);
        undoAndBlend.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doUndoAndBlendAgain();
                }
            });
        undoAndBlend.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(blend);

        JMenu nudgeMenu = new JMenu("Nudge");
        menu.add(nudgeMenu);
        
        nudgeTowards[0] = new JMenuItem("Towards 1");
        nudgeMenu.add(nudgeTowards[0]);
        nudgeTowards[0].addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doNudge(0);
                }
            });
        nudgeTowards[0].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | InputEvent.ALT_MASK));
                
        nudgeTowards[1] = new JMenuItem("Towards 2");
        nudgeMenu.add(nudgeTowards[1]);
        nudgeTowards[1].addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doNudge(1);
                }
            });
        nudgeTowards[1].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | InputEvent.ALT_MASK));
                
        nudgeTowards[2] = new JMenuItem("Towards 3");
        nudgeMenu.add(nudgeTowards[2]);
        nudgeTowards[2].addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doNudge(2);
                }
            });
        nudgeTowards[2].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | InputEvent.ALT_MASK));
                
        nudgeTowards[3] = new JMenuItem("Towards 4");
        nudgeMenu.add(nudgeTowards[3]);
        nudgeTowards[3].addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doNudge(3);
                }
            });
        nudgeTowards[3].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | InputEvent.ALT_MASK));
        
        nudgeMenu.addSeparator();

        nudgeTowards[4] = new JMenuItem("Away from 1");
        nudgeMenu.add(nudgeTowards[4]);
        nudgeTowards[4].addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doNudge(4);
                }
            });
        nudgeTowards[4].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | InputEvent.SHIFT_MASK | InputEvent.ALT_MASK));
                
        nudgeTowards[5] = new JMenuItem("Away from 2");
        nudgeMenu.add(nudgeTowards[5]);
        nudgeTowards[5].addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doNudge(5);
                }
            });
        nudgeTowards[5].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_6, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | InputEvent.SHIFT_MASK | InputEvent.ALT_MASK));
                
        nudgeTowards[6] = new JMenuItem("Away from 3");
        nudgeMenu.add(nudgeTowards[6]);
        nudgeTowards[6].addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doNudge(6);
                }
            });
        nudgeTowards[6].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_7, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | InputEvent.SHIFT_MASK | InputEvent.ALT_MASK));
                
        nudgeTowards[7] = new JMenuItem("Away from 4");
        nudgeMenu.add(nudgeTowards[7]);
        nudgeTowards[7].addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doNudge(7);
                }
            });
        nudgeTowards[7].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_8, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | InputEvent.SHIFT_MASK | InputEvent.ALT_MASK));

        nudgeMenu.addSeparator();

        JMenuItem undoAndNudge = new JMenuItem("Undo and Nudge Again");
        nudgeMenu.add(undoAndNudge);
        undoAndNudge.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                if (undo.shouldShowUndoMenu())
                    {
                    if (lastNudge > -1)
                        {
                        doUndo();
                        doNudge(lastNudge);
                        }
                    else
                        {
                        showSimpleError("Undo", "Can't Undo and Nudge Again: no previous nudge!");
                        }
                    }
                else
                    {
                    showSimpleError("Undo", "Can't Undo and Nudge Again: no previous nudge!");
                    }
                }
            });
        undoAndNudge.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | InputEvent.SHIFT_MASK));

        nudgeMenu.addSeparator();
        
        JMenuItem nudgeSet1 = new JMenuItem("Set 1");
        nudgeMenu.add(nudgeSet1);
        nudgeSet1.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doSetNudge(0);
                }
            });
                
        JMenuItem nudgeSet2 = new JMenuItem("Set 2");
        nudgeMenu.add(nudgeSet2);
        nudgeSet2.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doSetNudge(1);
                }
            });
                
        JMenuItem nudgeSet3 = new JMenuItem("Set 3");
        nudgeMenu.add(nudgeSet3);
        nudgeSet3.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doSetNudge(2);
                }
            });
                
        JMenuItem nudgeSet4 = new JMenuItem("Set 4");
        nudgeMenu.add(nudgeSet4);
        nudgeSet4.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doSetNudge(3);
                }
            });
        
        // reset the nudges now
        for(int i = 0; i < nudge.length; i++)
            doSetNudge(i);


                
        nudgeMenu.addSeparator();

        ButtonGroup nudgeMutationButtonGroup = new ButtonGroup();
        JMenu nudgeMutation = new JMenu("Set Nudge Mutation");
        nudgeMenu.add(nudgeMutation);
        JRadioButtonMenuItem nudgeMutation0 = new JRadioButtonMenuItem("0%");
        nudgeMutation.add(nudgeMutation0);
        nudgeMutationButtonGroup.add(nudgeMutation0);
        JRadioButtonMenuItem nudgeMutation1 = new JRadioButtonMenuItem("1%");
        nudgeMutation.add(nudgeMutation1);
        nudgeMutationButtonGroup.add(nudgeMutation1);
        JRadioButtonMenuItem nudgeMutation2 = new JRadioButtonMenuItem("2%");
        nudgeMutation.add(nudgeMutation2);
        nudgeMutationButtonGroup.add(nudgeMutation2);
        JRadioButtonMenuItem nudgeMutation5 = new JRadioButtonMenuItem("5%");
        nudgeMutation.add(nudgeMutation5);
        nudgeMutationButtonGroup.add(nudgeMutation5);
        JRadioButtonMenuItem nudgeMutation10 = new JRadioButtonMenuItem("10%");
        nudgeMutation.add(nudgeMutation10);
        nudgeMutationButtonGroup.add(nudgeMutation10);

        nudgeMutation0.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                nudgeMutationWeight = 0.0;
                setLastX("" + nudgeMutationWeight, "NudgeMutationWeight", getSynthNameLocal(), false); 
                }
            });

        nudgeMutation1.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                nudgeMutationWeight = 0.01;
                setLastX("" + nudgeMutationWeight, "NudgeMutationWeight", getSynthNameLocal(), false); 
                }
            });

        nudgeMutation2.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                nudgeMutationWeight = 0.02;
                setLastX("" + nudgeMutationWeight, "NudgeMutationWeight", getSynthNameLocal(), false); 
                }
            });

        nudgeMutation5.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                nudgeMutationWeight = 0.05;
                setLastX("" + nudgeMutationWeight, "NudgeMutationWeight", getSynthNameLocal(), false); 
                }
            });

        nudgeMutation10.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                nudgeMutationWeight = 0.10;
                setLastX("" + nudgeMutationWeight, "NudgeMutationWeight", getSynthNameLocal(), false); 
                }
            });

        double nudgeVal = getLastXAsDouble("NudgeMutationWeight", getSynthNameLocal(), 0.0, false);
        if (nudgeVal < 0.01) { nudgeMutationWeight = 0.0; nudgeMutation0.setSelected(true); }
        else if (nudgeVal < 0.02) { nudgeMutationWeight = 0.01; nudgeMutation0.setSelected(true); }
        else if (nudgeVal < 0.05) { nudgeMutationWeight = 0.02; nudgeMutation2.setSelected(true); }
        else if (nudgeVal < 0.10) { nudgeMutationWeight = 0.05; nudgeMutation5.setSelected(true); }
        else { nudgeMutationWeight = 0.10; nudgeMutation10.setSelected(true); }


        ButtonGroup nudgeRecombinationButtonGroup = new ButtonGroup();
        JMenu nudgeRecombination = new JMenu("Set Nudge Recombination");
        nudgeMenu.add(nudgeRecombination);
        JRadioButtonMenuItem nudgeRecombination2 = new JRadioButtonMenuItem("2%");
        nudgeRecombination.add(nudgeRecombination2);
        nudgeRecombinationButtonGroup.add(nudgeRecombination2);
        JRadioButtonMenuItem nudgeRecombination5 = new JRadioButtonMenuItem("5%");
        nudgeRecombination.add(nudgeRecombination5);
        nudgeRecombinationButtonGroup.add(nudgeRecombination5);
        JRadioButtonMenuItem nudgeRecombination10 = new JRadioButtonMenuItem("10%");
        nudgeRecombination.add(nudgeRecombination10);
        nudgeRecombinationButtonGroup.add(nudgeRecombination10);
        JRadioButtonMenuItem nudgeRecombination25 = new JRadioButtonMenuItem("25%");
        nudgeRecombination.add(nudgeRecombination25);
        nudgeRecombinationButtonGroup.add(nudgeRecombination25);
        JRadioButtonMenuItem nudgeRecombination50 = new JRadioButtonMenuItem("50%");
        nudgeRecombination.add(nudgeRecombination50);
        nudgeRecombinationButtonGroup.add(nudgeRecombination50);
        JRadioButtonMenuItem nudgeRecombination100 = new JRadioButtonMenuItem("100%");
        nudgeRecombination.add(nudgeRecombination100);
        nudgeRecombinationButtonGroup.add(nudgeRecombination100);

        nudgeRecombination2.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                nudgeRecombinationWeight = 0.02;
                setLastX("" + nudgeRecombinationWeight, "NudgeRecombinationWeight", getSynthNameLocal(), false); 
                }
            });

        nudgeRecombination5.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                nudgeRecombinationWeight = 0.05;
                setLastX("" + nudgeRecombinationWeight, "NudgeRecombinationWeight", getSynthNameLocal(), false); 
                }
            });

        nudgeRecombination10.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                nudgeRecombinationWeight = 0.10;
                setLastX("" + nudgeRecombinationWeight, "NudgeRecombinationWeight", getSynthNameLocal(), false); 
                }
            });

        nudgeRecombination25.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                nudgeRecombinationWeight = 0.25;
                setLastX("" + nudgeRecombinationWeight, "NudgeRecombinationWeight", getSynthNameLocal(), false); 
                }
            });

        nudgeRecombination50.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                nudgeRecombinationWeight = 0.50;
                setLastX("" + nudgeRecombinationWeight, "NudgeRecombinationWeight", getSynthNameLocal(), false); 
                }
            });

        nudgeRecombination100.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                nudgeRecombinationWeight = 1.0;
                setLastX("" + nudgeRecombinationWeight, "NudgeRecombinationWeight", getSynthNameLocal(), false); 
                }
            });

        nudgeVal = getLastXAsDouble("NudgeRecombinationWeight", getSynthNameLocal(), 0.25, false);
        if (nudgeVal < 0.05) { nudgeRecombinationWeight = 0.02; nudgeRecombination2.setSelected(true); }
        else if (nudgeVal < 0.10) { nudgeRecombinationWeight = 0.05; nudgeRecombination5.setSelected(true); }
        else if (nudgeVal < 0.25) { nudgeRecombinationWeight = 0.10; nudgeRecombination10.setSelected(true); }
        else if (nudgeVal < 0.50) { nudgeRecombinationWeight = 0.25; nudgeRecombination25.setSelected(true); }
        else if (nudgeVal < 1.00) { nudgeRecombinationWeight = 0.50; nudgeRecombination50.setSelected(true); }
        else { nudgeRecombinationWeight = 1.00; nudgeRecombination100.setSelected(true); }



        hillClimbMenu = new JMenuItem("Hill-Climb");
        menu.add(hillClimbMenu);
        hillClimbMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doHillClimb();
                }
            });

        morphMenu = new JMenuItem("Morph");
        menu.add(morphMenu);
        morphMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doMorph();
                }
            });

        menu.addSeparator();
        
        menu.add(copyTab);
        copyTab.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                SynthPanel p = findPanel();
                if (p != null) p.copyPanel(true);
                }
            });
        menu.add(pasteTab);
        pasteTab.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                SynthPanel p = findPanel();
                if (p != null) 
                    {
                    getUndo().push(getModel());
                    getUndo().setWillPush(false);
                    setSendMIDI(false);
                    p.pastePanel(true);
                    setSendMIDI(true);
                    // We do this TWICE because for some synthesizers, updating a parameter
                    // will reveal other parameters which also must be updated but aren't yet
                    // in the mapping.
                    p.pastePanel(true);
                    getUndo().setWillPush(true);
                    }
                }
            });
        menu.add(copyMutableTab);
        copyMutableTab.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                SynthPanel p = findPanel();
                if (p != null) p.copyPanel(false);
                }
            });
        menu.add(pasteMutableTab);
        pasteMutableTab.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                SynthPanel p = findPanel();
                if (p != null) 
                    {
                    getUndo().push(getModel());
                    getUndo().setWillPush(false);
                    setSendMIDI(false);
                    p.pastePanel(false);
                    setSendMIDI(true);
                    // We do this TWICE because for some synthesizers, updating a parameter
                    // will reveal other parameters which also must be updated but aren't yet
                    // in the mapping.
                    p.pastePanel(false);
                    getUndo().setWillPush(true);
                    }
                }
            });
        resetTab.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                SynthPanel p = findPanel();
                if (p != null) 
                    {
                    getUndo().push(getModel());
                    getUndo().setWillPush(false);
                    p.resetPanel();
                    getUndo().setWillPush(true);
                    }
                }
            });
        menu.add(resetTab);


        menu.addSeparator();
        
        editMutationMenu = new JMenuItem("Edit Mutation Parameters");
        menu.add(editMutationMenu);
        editMutationMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doToggleMutationMapEdit();
                }
            });

        JMenuItem clearAllMutationRestrictions = new JMenuItem("Clear All Mutation Parameters");
        menu.add(clearAllMutationRestrictions);
        clearAllMutationRestrictions.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doSetAllMutationMap(false);
                }
            });
                
        JMenuItem setAllMutationRestrictions = new JMenuItem("Set All Mutation Parameters");
        menu.add(setAllMutationRestrictions);
        setAllMutationRestrictions.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doSetAllMutationMap(true);
                }
            });

        menu = new JMenu("MIDI");
        menubar.add(menu);
                
        
        receiveCurrent = new JMenuItem("Request Current Patch");
        receiveCurrent.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(receiveCurrent);
        receiveCurrent.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doRequestCurrentPatch();
                }
            });

        receivePatch = new JMenuItem("Request Patch...");
        receivePatch.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | InputEvent.SHIFT_MASK));
        menu.add(receivePatch);
        receivePatch.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doRequestPatch();
                }
            });
                
        receiveNextPatch = new JMenuItem("Request Next Patch");
        receiveNextPatch.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(receiveNextPatch);
        receiveNextPatch.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doRequestNextPatch();
                }
            });
                
        merge = new JMenu("Request Merge");
        menu.add(merge);
        JMenuItem merge25 = new JMenuItem("Merge in 25%");
        merge.add(merge25);
        JMenuItem merge50 = new JMenuItem("Merge in 50%");
        merge.add(merge50);
        JMenuItem merge75 = new JMenuItem("Merge in 75%");
        merge.add(merge75);
        JMenuItem merge100 = new JMenuItem("Merge in 100%");
        merge.add(merge100);
        
        merge25.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doRequestMerge(0.25);
                }
            });

        merge50.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doRequestMerge(0.50);
                }
            });

        merge75.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doRequestMerge(0.75);
                }
            });

        merge100.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doRequestMerge(1.0);
                }
            });
                
        menu.addSeparator();


        transmitCurrent = new JMenuItem("Send to Current Patch");
        transmitCurrent.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(transmitCurrent);
        transmitCurrent.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doSendToCurrentPatch();
                }
            });


        writeTo = new JMenuItem("Write to Patch...");
        writeTo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()  |  InputEvent.SHIFT_MASK));
        menu.add(writeTo);
        writeTo.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doWriteToPatch();
                }
            });

        transmitParameters = new JCheckBoxMenuItem("Sends Real Time Changes");
        menu.add(transmitParameters);
        transmitParameters.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doAllowParameterTransmit();
                }
            });

        String sendInRealTime = getLastX("AllowTransmitParameters", getSynthNameLocal(), false);
        if (sendInRealTime == null) sendInRealTime = "true";
        allowsTransmitsParameters = Boolean.parseBoolean(sendInRealTime);
        transmitParameters.setSelected(allowsTransmitsParameters);

                
        repeatCurrentPatch = new JCheckBoxMenuItem("Repeatedly Send to Current Patch");
        //repeatCurrentPatch.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()  |  InputEvent.SHIFT_MASK));
        menu.add(repeatCurrentPatch);
        repeatCurrentPatch.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doRepeatCurrentPatch();
                }
            });

        // we don't use this any more
        transmitTo = new JMenuItem("Send to Patch...");        

        menu.addSeparator();

        JMenuItem change = new JMenuItem("Change MIDI");
        menu.add(change);
        change.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doChangeMIDI();
                }
            });
            
        JMenuItem disconnect = new JMenuItem("Disconnect MIDI");
        menu.add(disconnect);
        disconnect.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doDisconnectMIDI();
                }
            });
            
        menu.addSeparator();

        JMenuItem sendSysex = new JMenuItem("Send Sysex...");
        menu.add(sendSysex);
        sendSysex.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                String hex = JOptionPane.showInputDialog(Synth.this, "Enter a sysex hex string", "");
                if (hex != null)
                    {
                    java.util.Scanner scanner = new java.util.Scanner(hex);
                    ArrayList list = new ArrayList();
                    while(scanner.hasNextInt(16))
                        {
                        list.add(new Integer(scanner.nextInt(16)));
                        }
                    byte[] data = new byte[list.size()];
                    for(int i = 0; i < data.length; i++)
                        {
                        data[i] = (byte)(((Integer)(list.get(i))).intValue());
                        }
                    tryToSendSysex(data);
                    }
                }
            });

        testIncomingSynth = new JMenuItem("Report Next Synth MIDI");
        menu.add(testIncomingSynth);
        testIncomingSynth.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                testIncomingSynthMIDI = !testIncomingSynthMIDI;
                if (testIncomingSynthMIDI)
                    testIncomingSynth.setText("Stop Reporting Synth MIDI");
                else
                    testIncomingSynth.setText("Report Next Synth MIDI");                    
                }
            });

        testIncomingController = new JMenuItem("Report Next Controller MIDI");
        menu.add(testIncomingController);
        testIncomingController.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                testIncomingControllerMIDI = !testIncomingControllerMIDI;
                if (testIncomingControllerMIDI)
                    testIncomingController.setText("Stop Reporting Controller MIDI");
                else
                    testIncomingController.setText("Report Next Controller MIDI");
                }
            });

        menu.addSeparator();

        JMenuItem allSoundsOff = new JMenuItem("Send All Sounds Off");
        allSoundsOff.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(allSoundsOff);
        allSoundsOff.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doSendAllSoundsOff(false);
                }
            });
                
        JMenuItem testNote = new JMenuItem("Send Test Note");
        testNote.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(testNote);
        testNote.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doSendTestNote();
                }
            });

        testNotes = new JCheckBoxMenuItem("Send Test Notes");
        testNotes.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() |  InputEvent.SHIFT_MASK));
        menu.add(testNotes);
        testNotes.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doSendTestNotes();
                }
            });

        menu.addSeparator();

        sendTestNotesTimer = new javax.swing.Timer(1000, new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                if (repeatingCurrentPatch)
                    {
                    if (!hillClimbing)
                        {
                        sendAllParameters();
                        }
                    }
                        
                if (sendingTestNotes)
                    {
                    if (hillClimbing)
                        hillClimb.updateSound();
                    if (morphing)
                        morph.updateSound();
                    doSendTestNote();
                    if (hillClimbing)
                        hillClimb.postUpdateSound();
                    if (morphing)
                        morph.postUpdateSound();
                    }
                }
            });
        sendTestNotesTimer.setRepeats(true);
        sendingTestNotes = false;
        repeatingCurrentPatch = false;
        sendTestNotesTimer.start();

        ButtonGroup testNoteGroup = new ButtonGroup();
        
        JRadioButtonMenuItem tns[] = new JRadioButtonMenuItem[8];
                
        JMenu testNoteLength = new JMenu("Test Note Length");
        menu.add(testNoteLength);
        JRadioButtonMenuItem tn = tns[0] = new JRadioButtonMenuItem("1/8 Second");
        testNoteLength.add(tn);
        tn.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setTestNoteLength(125);
                setLastX("" + getTestNoteLength(), "TestNoteLength", getSynthNameLocal(), false); 
                }
            });
        testNoteGroup.add(tn);
        tn = tns[1] = new JRadioButtonMenuItem("1/4 Second");
        testNoteLength.add(tn);
        tn.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setTestNoteLength(250);
                setLastX("" + getTestNoteLength(), "TestNoteLength", getSynthNameLocal(), false); 
                }
            });
        testNoteGroup.add(tn);
        tn = tns[2] = new JRadioButtonMenuItem("1/2 Second");
        testNoteLength.add(tn);
        tn.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setTestNoteLength(500);
                setLastX("" + getTestNoteLength(), "TestNoteLength", getSynthNameLocal(), false); 
                }
            });
        testNoteGroup.add(tn);
        tn = tns[3] = new JRadioButtonMenuItem("1 Second");
        testNoteLength.add(tn);
        tn.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setTestNoteLength(1000);
                setLastX("" + getTestNoteLength(), "TestNoteLength", getSynthNameLocal(), false); 
                }
            });
        testNoteGroup.add(tn);
        tn = tns[4] = new JRadioButtonMenuItem("2 Seconds");
        testNoteLength.add(tn);
        tn.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setTestNoteLength(2000);
                setLastX("" + getTestNoteLength(), "TestNoteLength", getSynthNameLocal(), false); 
                }
            });
        testNoteGroup.add(tn);
        tn = tns[5] = new JRadioButtonMenuItem("4 Seconds");
        testNoteLength.add(tn);
        tn.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setTestNoteLength(4000);
                setLastX("" + getTestNoteLength(), "TestNoteLength", getSynthNameLocal(), false); 
                }
            });
        testNoteGroup.add(tn);
        tn = tns[6] = new JRadioButtonMenuItem("8 Seconds");
        testNoteLength.add(tn);
        tn.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setTestNoteLength(8000);
                setLastX("" + getTestNoteLength(), "TestNoteLength", getSynthNameLocal(), false); 
                }
            });
        testNoteGroup.add(tn);
        tn = tns[7] = new JRadioButtonMenuItem("16 Seconds");
        testNoteLength.add(tn);
        tn.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setTestNoteLength(16000);
                setLastX("" + getTestNoteLength(), "TestNoteLength", getSynthNameLocal(), false); 
                }
            });
        testNoteGroup.add(tn);

        int v = getLastXAsInt("TestNoteLength", getSynthNameLocal(), 500, false);
        switch(v)
            {
            case 125:
                tns[0].setSelected(true); setTestNoteLength(v); break;
            case 250:
                tns[1].setSelected(true); setTestNoteLength(v); break;
            case 500:
                tns[2].setSelected(true); setTestNoteLength(v); break;
            case 1000:
                tns[3].setSelected(true); setTestNoteLength(v); break;
            case 2000:
                tns[4].setSelected(true); setTestNoteLength(v); break;
            case 4000:
                tns[5].setSelected(true); setTestNoteLength(v); break;
            case 8000:
                tns[6].setSelected(true); setTestNoteLength(v); break;
            case 16000:
                tns[7].setSelected(true); setTestNoteLength(v); break;
            default:
                tns[2].setSelected(true); setTestNoteLength(500); break;
            }        



        testNoteGroup = new ButtonGroup();
        tns = new JRadioButtonMenuItem[9];

        JMenu TestNotePause = new JMenu("Pause Between Test Notes");
        menu.add(TestNotePause);
        tn = tns[0] = new JRadioButtonMenuItem("Default");
        TestNotePause.add(tn);
        tn.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setTestNotePause(TEST_NOTE_PAUSE_DEFAULT);
                setLastX("" + getTestNotePause(), "TestNotePause", getSynthNameLocal(), false); 
                }
            });
        testNoteGroup.add(tn);
        tn = tns[1] = new JRadioButtonMenuItem("0 Seconds");
        TestNotePause.add(tn);
        tn.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setTestNotePause(0);
                setLastX("" + getTestNotePause(), "TestNotePause", getSynthNameLocal(), false); 
                }
            });
        testNoteGroup.add(tn);
        tn = tns[2] = new JRadioButtonMenuItem("1/8 Second");
        TestNotePause.add(tn);
        tn.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setTestNotePause(125);
                setLastX("" + getTestNotePause(), "TestNotePause", getSynthNameLocal(), false); 
                }
            });
        testNoteGroup.add(tn);
        tn = tns[3] = new JRadioButtonMenuItem("1/4 Second");
        TestNotePause.add(tn);
        tn.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setTestNotePause(250);
                setLastX("" + getTestNotePause(), "TestNotePause", getSynthNameLocal(), false); 
                }
            });
        testNoteGroup.add(tn);
        tn = tns[4] = new JRadioButtonMenuItem("1/2 Second");
        TestNotePause.add(tn);
        tn.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setTestNotePause(500);
                setLastX("" + getTestNotePause(), "TestNotePause", getSynthNameLocal(), false); 
                }
            });
        testNoteGroup.add(tn);
        tn = tns[5] = new JRadioButtonMenuItem("1 Second");
        TestNotePause.add(tn);
        tn.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setTestNotePause(1000);
                setLastX("" + getTestNotePause(), "TestNotePause", getSynthNameLocal(), false); 
                }
            });
        testNoteGroup.add(tn);
        tn = tns[6] = new JRadioButtonMenuItem("2 Seconds");
        TestNotePause.add(tn);
        tn.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setTestNotePause(2000);
                setLastX("" + getTestNotePause(), "TestNotePause", getSynthNameLocal(), false); 
                }
            });
        testNoteGroup.add(tn);

        tn = tns[7] = new JRadioButtonMenuItem("4 Seconds");
        TestNotePause.add(tn);
        tn.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setTestNotePause(4000);
                setLastX("" + getTestNotePause(), "TestNotePause", getSynthNameLocal(), false); 
                }
            });
        testNoteGroup.add(tn);
        tn = tns[8] = new JRadioButtonMenuItem("8 Seconds");
        TestNotePause.add(tn);
        tn.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setTestNotePause(8000);
                setLastX("" + getTestNotePause(), "TestNotePause", getSynthNameLocal(), false); 
                }
            });
        testNoteGroup.add(tn);


        v = getLastXAsInt("TestNotePause", getSynthNameLocal(), -1, false);
        switch(v)
            {
            case TEST_NOTE_PAUSE_DEFAULT:
                tns[0].setSelected(true); setTestNotePause(v); break;
            case 0:
                tns[1].setSelected(true); setTestNotePause(v); break;
            case 125:
                tns[2].setSelected(true); setTestNotePause(v); break;
            case 250:
                tns[3].setSelected(true); setTestNotePause(v); break;
            case 500:
                tns[4].setSelected(true); setTestNotePause(v); break;
            case 1000:
                tns[5].setSelected(true); setTestNotePause(v); break;
            case 2000:
                tns[6].setSelected(true); setTestNotePause(v); break;
            case 4000:
                tns[7].setSelected(true); setTestNotePause(v); break;
            case 8000:
                tns[8].setSelected(true); setTestNotePause(v); break;
            default:
                tns[0].setSelected(true); setTestNotePause(TEST_NOTE_PAUSE_DEFAULT); break;
            }        





        JMenu testNoteChord = new JMenu("Test Note Chord");
        menu.add(testNoteChord);
        
        tns = new JRadioButtonMenuItem[CHORDS.length];
        testNoteGroup = new ButtonGroup();
        
        for(int i = 0; i < CHORDS.length; i++)
            {
            final int _i = i;
            tns[i] = new JRadioButtonMenuItem(CHORD_NAMES[i]);
            testNoteChord.add(tns[i]);
            tns[i].addActionListener(new ActionListener()
                {
                public void actionPerformed( ActionEvent e)
                    {
                    setTestNoteChord(_i);
                    setLastX("" + _i, "TestNoteChord", getSynthNameLocal(), false); 
                    }
                });
            testNoteGroup.add(tns[i]);
            }
            
        v = getLastXAsInt("TestNoteChord", getSynthNameLocal(), 0, false);
        if (v < 0 || v >= CHORDS.length) v = 0;
        tns[v].setSelected(true); 
        setTestNoteChord(v);





        JMenu testNotePitch = new JMenu("Test Note Pitch");
        menu.add(testNotePitch);
        
        tns = new JRadioButtonMenuItem[7];
        
        testNoteGroup = new ButtonGroup();
        tn = tns[0] = new JRadioButtonMenuItem("3 Octaves Up");
        testNotePitch.add(tn);
        tn.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setTestNotePitch(96);
                setLastX("" + 96, "TestNotePitch", getSynthNameLocal(), false); 
                }
            });
        testNoteGroup.add(tn);
        tn = tns[1] =new JRadioButtonMenuItem("2 Octaves Up");
        testNotePitch.add(tn);
        tn.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setTestNotePitch(84);
                setLastX("" + getTestNotePitch(), "TestNotePitch", getSynthNameLocal(), false); 
                }
            });
        testNoteGroup.add(tn);
        tn = tns[2] =new JRadioButtonMenuItem("1 Octave Up");
        testNotePitch.add(tn);
        tn.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setTestNotePitch(72);
                setLastX("" + getTestNotePitch(), "TestNotePitch", getSynthNameLocal(), false); 
                }
            });
        testNoteGroup.add(tn);
        tn = tns[3] =new JRadioButtonMenuItem("Middle C");
        tn.setSelected(true);
        testNotePitch.add(tn);
        tn.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setTestNotePitch(60);
                setLastX("" + getTestNotePitch(), "TestNotePitch", getSynthNameLocal(), false); 
                }
            });
        testNoteGroup.add(tn);
        tn = tns[4] =new JRadioButtonMenuItem("1 Octave Down");
        testNotePitch.add(tn);
        tn.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setTestNotePitch(48);
                setLastX("" + getTestNotePitch(), "TestNotePitch", getSynthNameLocal(), false); 
                }
            });
        testNoteGroup.add(tn);
        tn = tns[5] =new JRadioButtonMenuItem("2 Octaves Down");
        testNotePitch.add(tn);
        tn.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setTestNotePitch(36);
                setLastX("" + getTestNotePitch(), "TestNotePitch", getSynthNameLocal(), false); 
                }
            });
        testNoteGroup.add(tn);
        tn = tns[6] =new JRadioButtonMenuItem("3 Octaves Down");
        testNotePitch.add(tn);
        tn.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setTestNotePitch(24);
                setLastX("" + getTestNotePitch(), "TestNotePitch", getSynthNameLocal(), false); 
                }
            });
        testNoteGroup.add(tn);
        
            
        v = getLastXAsInt("TestNotePitch", getSynthNameLocal(), 60, false);
        switch(v)
            {
            case 96:
                tns[0].setSelected(true); setTestNotePitch(v); break;
            case 84:
                tns[1].setSelected(true); setTestNotePitch(v); break;
            case 72:
                tns[2].setSelected(true); setTestNotePitch(v); break;
            case 60:
                tns[3].setSelected(true); setTestNotePitch(v); break;
            case 48:
                tns[4].setSelected(true); setTestNotePitch(v); break;
            case 36:
                tns[5].setSelected(true); setTestNotePitch(v); break;
            case 24:
                tns[6].setSelected(true); setTestNotePitch(v); break;
            default:
                //tns[3].setSelected(true); setTestNotePitch(60); break;
                break;
            }        


        JMenu testNoteVolume = new JMenu("Test Note Volume");
        menu.add(testNoteVolume);
        
        tns = new JRadioButtonMenuItem[5];

        testNoteGroup = new ButtonGroup();
        tn = tns[0] = new JRadioButtonMenuItem("Full Volume");
        tn.setSelected(true);
        testNoteVolume.add(tn);
        tn.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setTestNoteVelocity(127);
                setLastX("" + getTestNoteVelocity(), "TestNoteVelocity", getSynthNameLocal(), false); 
                }
            });
        testNoteGroup.add(tn);
        tn = tns[1] = new JRadioButtonMenuItem("1/2 Volume");
        testNoteVolume.add(tn);
        tn.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setTestNoteVelocity(64);
                setLastX("" + getTestNoteVelocity(), "TestNoteVelocity", getSynthNameLocal(), false); 
                }
            });
        testNoteGroup.add(tn);
        tn = tns[2] = new JRadioButtonMenuItem("1/4 Volume");
        testNoteVolume.add(tn);
        tn.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setTestNoteVelocity(32);
                setLastX("" + getTestNoteVelocity(), "TestNoteVelocity", getSynthNameLocal(), false); 
                }
            });
        testNoteGroup.add(tn);
        tn = tns[3] = new JRadioButtonMenuItem("1/8 Volume");
        testNoteVolume.add(tn);
        tn.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setTestNoteVelocity(16);
                setLastX("" + getTestNoteVelocity(), "TestNoteVelocity", getSynthNameLocal(), false); 
                }
            });
        testNoteGroup.add(tn);
        tn = tns[4] = new JRadioButtonMenuItem("1/16 Volume");
        testNoteVolume.add(tn);
        tn.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setTestNoteVelocity(8);
                setLastX("" + getTestNoteVelocity(), "TestNoteVelocity", getSynthNameLocal(), false); 
                }
            });
        testNoteGroup.add(tn);


        v = getLastXAsInt("TestNoteVelocity", getSynthNameLocal(), 127, false);
        switch(v)
            {
            case 127:
                tns[0].setSelected(true); setTestNoteVelocity(v); break;
            case 64:
                tns[1].setSelected(true); setTestNoteVelocity(v); break;
            case 32:
                tns[2].setSelected(true); setTestNoteVelocity(v); break;
            case 16:
                tns[3].setSelected(true); setTestNoteVelocity(v); break;
            case 8:
                tns[4].setSelected(true); setTestNoteVelocity(v); break;
            default:
                tns[0].setSelected(true); setTestNoteVelocity(127); break;
            }        

        sendsAllSoundsOffBetweenNotesMenu = new JCheckBoxMenuItem("Send All Sounds Off Before Note On");
        sendsAllSoundsOffBetweenNotesMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doSendsAllSoundsOffBetweenNotes();
                }
            });
        
        menu.add(sendsAllSoundsOffBetweenNotesMenu);

        String str1 = getLastX("SendAllSoundsOffBetweenNotes", getSynthNameLocal(), false);
        if (str1 == null) str1 = "false";
        sendsAllSoundsOffBetweenNotes = Boolean.parseBoolean(str1);
        sendsAllSoundsOffBetweenNotesMenu.setSelected(sendsAllSoundsOffBetweenNotes);



        menu = new JMenu("Map");
        menubar.add(menu);

        learningMenuItem = new JMenuItem("Map Absolute CC / NRPN");
        learningMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(learningMenuItem);
        learningMenuItem.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doMapCC(CCMap.TYPE_ABSOLUTE_CC);
                }
            });
                
        //        learningMenuItem64 = new JMenuItem("Map Relative CC [64]");
        learningMenuItem64 = new JMenuItem("Map Relative CC");
        learningMenuItem64.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(learningMenuItem64);
        learningMenuItem64.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doMapCC(CCMap.TYPE_RELATIVE_CC_64);
                }
            });
        menu.addSeparator();
        
        JMenuItem clearAllCC = new JMenuItem("Clear all Mapped CCs");
        menu.add(clearAllCC);
        clearAllCC.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doClearAllCC();
                }
            });

        perChannelCCsMenuItem = new JCheckBoxMenuItem("Do Per-Channel CCs");
        menu.add(perChannelCCsMenuItem);
        perChannelCCsMenuItem.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doPerChannelCCs(perChannelCCsMenuItem.getState());
                }
            });
        perChannelCCsMenuItem.setSelected(perChannelCCs);

        menu.addSeparator(); 

        passThroughCCMenuItem = new JCheckBoxMenuItem("Pass Through All CCs");
        menu.add(passThroughCCMenuItem);
        passThroughCCMenuItem.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doPassThroughCC(passThroughCCMenuItem.getState());
                }
            });
        String val = getLastX("PassThroughCC", getSynthNameLocal(), false);
        setPassThroughCC(val != null && val.equalsIgnoreCase("true"));
            
            
        passThroughControllerMenuItem = new JCheckBoxMenuItem("Pass Through Controller MIDI");
        menu.add(passThroughControllerMenuItem);
        passThroughControllerMenuItem.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doPassThroughController(passThroughControllerMenuItem.getState());
                }
            });
        val = getLastX("PassThroughController", getSynthNameLocal(), true);
        setPassThroughController(val == null || val.equalsIgnoreCase("true"));
            
            
        menu = new JMenu("Options");
        menubar.add(menu);
                        
        persistentChooserMenu  = new JCheckBoxMenuItem("Keep Next Popup Open");
        persistentChooserMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(persistentChooserMenu);
        
        launchMenu = new JCheckBoxMenuItem("Launch with Last Editor");
        menu.add(launchMenu);
        launchMenu.setSelected(getLastXAsBoolean("ShowSynth", null, true, false));
        launchMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setLastX("" + launchMenu.isSelected(), "ShowSynth", null);
                }
            });

        clearNotesMenu = new JCheckBoxMenuItem("Send All Sounds Off when Changing Windows");
        menu.add(clearNotesMenu);
        clearNotesMenu.setSelected(clearNotes);
        clearNotesMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                clearNotes = clearNotesMenu.isSelected();
                setLastX("" + clearNotes, "SwitchingSendsAllSoundsOff", null);
                }
            });

        menu.addSeparator();

        JMenuItem colorMenu = new JMenuItem("Change Color Scheme...");
        menu.add(colorMenu);
        colorMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                setupColors();
                }
            });

        JMenuItem resetColorMenu = new JMenuItem("Reset Color Scheme");
        menu.add(resetColorMenu);
        resetColorMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                if (showSimpleConfirm("Reset Colors",
                        "<html>Reset Color Scheme to Defaults?<br><br><font size='-1'>Note: after resetting colors, currently<br>open windows may look scrambled,<br>but new windows will look correct.</font></html>"))
                    resetColors();
                }
            });


        menu = new JMenu("Tabs");
        menubar.add(menu);
                
                
        JMenuItem prev = new JMenuItem("Previous Tab");
        prev.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(prev);
        prev.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doPreviousTab();
                }
            });


        JMenuItem next = new JMenuItem("Next Tab");
        next.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(next);
        next.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doNextTab();
                }
            });


        menu.addSeparator();

        JMenuItem taba = new JMenuItem("Tab 1");
        taba.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(taba);
        taba.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doTab(0);
                }
            });

        taba = new JMenuItem("Tab 2");
        taba.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(taba);
        taba.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doTab(1);
                }
            });

        taba = new JMenuItem("Tab 3");
        taba.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(taba);
        taba.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doTab(2);
                }
            });

        taba = new JMenuItem("Tab 4");
        taba.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(taba);
        taba.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doTab(3);
                }
            });

        taba = new JMenuItem("Tab 5");
        taba.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(taba);
        taba.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doTab(4);
                }
            });

        taba = new JMenuItem("Tab 6");
        taba.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_6, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(taba);
        taba.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doTab(5);
                }
            });

        taba = new JMenuItem("Tab 7");
        taba.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_7, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(taba);
        taba.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doTab(6);
                }
            });

        taba = new JMenuItem("Tab 8");
        taba.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_8, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(taba);
        taba.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doTab(7);
                }
            });

        taba = new JMenuItem("Tab 9");
        taba.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_9, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(taba);
        taba.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doTab(8);
                }
            });

        taba = new JMenuItem("Tab 10");
        taba.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(taba);
        taba.addActionListener(new ActionListener()
            {
            public void actionPerformed( ActionEvent e)
                {
                doTab(9);
                }
            });
                        
        // Set up Mac.  See Mac.java.
        if (Style.isMac())
            Mac.setup(this);
        
        // Handle About menu for non-Macs
        if (Style.isWindows() || Style.isUnix())
            {
            // right now the only thing under "Help" is
            // the About menu, so it doesn't exist on the Mac,
            // where the About menu is elsewhere.
            JMenu helpMenu = new JMenu("Help");
            JMenuItem aboutMenuItem = new JMenuItem("About Edisyn");
            aboutMenuItem.addActionListener(new ActionListener()
                {
                public void actionPerformed( ActionEvent e)
                    {
                    doAbout();
                    }
                });
            helpMenu.add(aboutMenuItem);
            menubar.add(helpMenu);
            }

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(this, BorderLayout.CENTER);

        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); 
        frame.addWindowListener(new java.awt.event.WindowAdapter() 
            {
            public void windowClosing(java.awt.event.WindowEvent windowEvent) 
                {
                doCloseWindow();
                }

            public void windowActivated(WindowEvent e)
                {
                if (clearNotes && sendAllSoundsOffWhenWindowChanges())
                    {
                    sendAllSoundsOff(); // not doSendAllSoundsOff(false) because we don't want to turn off the test notes
                    }
                updateMenu();
                windowBecameFront();
                lastActiveWindow = frame;
                }

            });
    
        updateTitle();
        numOpenWindows++;  
 
        tabChanged();   // so we reset the copy tab etc. menus
       
        frame.pack();
        return frame;
        }


    void updateMenu()
        {
        launchMenu.setSelected(getLastXAsBoolean("ShowSynth", null, true, false));
        clearNotesMenu.setSelected(clearNotes);
        }
            
    void doPerChannelCCs(boolean val)
        {
        if (showSimpleConfirm("Change Per-Channel CC Settings?", "This clears all CCs.  Change your per-channel CC settings?"))
            {
            clearLearned();
            perChannelCCs = val;
            setLastX("" + perChannelCCs, "PerChannelCC", getSynthNameLocal(), false);              
            }
        else
            {
            // reset
            perChannelCCsMenuItem.setState(!perChannelCCsMenuItem.getState());
            }
        }


    void doRequestCurrentPatch()
        {
        if (tuple == null || tuple.out == null)
            {
            if (!setupMIDI())
                return;
            }
                
        setMergeProbability(0.0);
        performRequestCurrentDump();
        }  
    
    /** Milliseconds in which we pause before sending a patch request.  The reason for this is that 
        some synths respond so fast to a patch request that we don't have time to take down the gatherPatchInfo(...)
        window.  As a result when the response comes in, */
    public static final int PAUSE_BEFORE_PATCH_REQUEST = 50;
    
    void doRequestPatch()
        {
        if (tuple == null || tuple.out == null)
            {
            if (!setupMIDI())
                return;
            }
                
        Model tempModel = buildModel();
        if (gatherPatchInfo("Request Patch", tempModel, false))
            {
            resetBlend();
            setMergeProbability(0.0);
            performRequestDump(tempModel, true);
            }
        } 

    void doRequestNextPatch()
        {
        if (tuple == null || tuple.out == null)
            {
            if (!setupMIDI())
                return;
            }
                
        Model tempModel = getNextPatchLocation(getModel());
        resetBlend();
        setMergeProbability(0.0);
        performRequestDump(tempModel, true);
        } 
        
    public void doRequestMerge(double percentage)
        {
        if (tuple == null || tuple.out == null)
            {
            if (!setupMIDI())
                return;
            }
                
        Model tempModel = buildModel();
        if (gatherPatchInfo("Request Merge", tempModel, false))
            {
            resetBlend();
            setMergeProbability(percentage);
            performRequestDump(tempModel, false);
            }
        }
                
    void doSendPatch()
        {
        if (tuple == null || tuple.out == null)
            {
            if (!setupMIDI())
                return;
            }
                
        sendAllParameters();
        }
        
    void doSendToPatch()
        {
        if (tuple == null || tuple.out == null)
            {
            if (!setupMIDI())
                return;
            }
                
        if (gatherPatchInfo("Send Patch To...", getModel(), true))
            {
            performChangePatch(getModel());     // do it first here, as opposed to doWritetoPatch, which does it at the end
            sendAllParameters();
            }
        }       
                
                
    void doSendToCurrentPatch()
        {
        if (tuple == null || tuple.out == null)
            {
            if (!setupMIDI())
                return;
            }
        sendAllParametersInternal();            // this ALWAYS sends to parameters
        }
                
    void doReset()
        {
        // this last statement fixes a mystery.  When I call Randomize or Reset on
        // a Blofeld or on a Microwave, all of the widgets update simultaneously.
        // But on a Blofeld Multi or Microwave Multi they update one at a time.
        // I've tried a zillion things, even moving all the widgets from the Blofeld Multi
        // into the Blofeld, and it makes no difference!  For some reason the OS X
        // repaint manager is refusing to coallesce their repaint requests.  So I do it here.
        repaint();

        //if (!showSimpleConfirm("Reset", "Reset the parameters to initial values?"))
        //    return;
                
        setSendMIDI(false);
        // because loadDefaults isn't wrapped in an undo, we have to
        // wrap it manually here
        undo.setWillPush(false);
        Model backup = (Model)(model.clone());
        loadDefaults();
        undo.setWillPush(true);
        if (!backup.keyEquals(getModel()))  // it's changed, do an undo push
            undo.push(backup);
        setSendMIDI(true);
        sendAllParameters();
        }
                
    void doWriteToPatch()
        {
        if (tuple == null || tuple.out == null)
            {
            if (!setupMIDI())
                return;
            }
                
        if (gatherPatchInfo("Write Patch To...", getModel(), true))
            {
            writeAllParameters(getModel());
            }
        }
        
    public void writeAllParameters(Model model)
        {
        // performChangePatch(model);     // we need to be at the start for the Oberheim Matrix 1000
        tryToSendMIDI(emitAll(model, false, false));
        simplePause(getPauseAfterWritePatch());
        performChangePatch(model);     // do it at the end AND start here, as opposed to doSendtoPatch, which does it first.  We need to be at the end for the Kawai K4.
        if (getSendsParametersAfterWrite())
            sendAllParameters();
        }
                
    void doChangeMIDI()
        {
        if (!setupMIDI("Choose new MIDI devices to send to and receive from.", tuple))
            return;
        }
    
    boolean noMIDIPause = false;
    boolean sendingAllSoundsOff = false;
    void doSendAllSoundsOff(boolean fromDoSendTestNotes)  // used to break infinite loop fights with doSendTestNotes()
        {
        if (!fromDoSendTestNotes && sendingTestNotes)
            {
            sendingAllSoundsOff = true;
            doSendTestNotes(); // turn off
            sendingAllSoundsOff = false;
            }
        
        if (!sendingAllSoundsOff)
            {
            sendAllSoundsOff();
            }
        }
        
        
    void sendAllSoundsOff()
        {
        noMIDIPause = true;
        try
            {
            // do an all sounds off (some synths don't properly respond to all notes off)
            for(int i = 0; i < 16; i++)
                tryToSendMIDI(new ShortMessage(ShortMessage.CONTROL_CHANGE, i, 120, 0));
            // do an all notes off (some synths don't properly respond to all sounds off)
            for(int i = 0; i < 16; i++)
                tryToSendMIDI(new ShortMessage(ShortMessage.CONTROL_CHANGE, i, 123, 0));
            // Plus, for some synths that respond to neither <ahem Korg Wavestation>, maybe we can turn off the current note,
            // assuming the user hasn't changed it.   
            clearChord();
            }
        catch (InvalidMidiDataException e2)
            {
            Synth.handleException(e2);
            }
        noMIDIPause = false;
        }


    int testNoteLength = 500;
    void setTestNoteLength(int val)
        {
        testNoteLength = val;
        setTestNotePause(getTestNotePause());  // update in case it's default.  This also changes the timer length.
        }

    int getTestNoteLength()
        {
        return testNoteLength;
        }

    static final int TEST_NOTE_PAUSE_DEFAULT = -1;
    int testNotePause = TEST_NOTE_PAUSE_DEFAULT;
    void setTestNotePause(int val)
        {
        testNotePause = val;
        sendTestNotesTimer.setDelay(getTestNoteTotalLength());
        }

    int getTestNoteTotalLength()
        {
        if (getTestNotePause() == TEST_NOTE_PAUSE_DEFAULT)
            {
            int len = getTestNoteLength();
            int delay = (len <= 500 ? len * 2 : len + 500);
            return delay;
            }
        else
            {
            return getTestNotePause() + getTestNoteLength();
            }
        }

    int getTestNotePause()
        {
        return testNotePause;
        }


    boolean sendingTestNotes = false;   
    javax.swing.Timer sendTestNotesTimer;
                    
    public void doSendTestNotes()
        {
        if (sendingTestNotes)
            {
            //sendTestNotesTimer.stop();
            doSendAllSoundsOff(true);
            sendingTestNotes = false;
            testNotes.setSelected(false);
            }       
        else
            {
            //sendTestNotesTimer.start();
            sendingTestNotes = true;
            testNotes.setSelected(true);
            }       
        }
    
    public void doRepeatCurrentPatch()
        {
        if (repeatingCurrentPatch)
            {
            repeatingCurrentPatch = false;
            repeatCurrentPatch.setSelected(false);
            }       
        else if (showSimpleConfirm("Repeat Current Patch", "This will constantly send the current patch to your synthesizer.\nThe sends will be at the rate that test notes are sent.\nThis is for certain synths with limitations: see the manual.\nAre you sure you want to do this?"))
            {
            repeatingCurrentPatch = true;
            repeatCurrentPatch.setSelected(true);
            }
        else
            {
            repeatingCurrentPatch = false;
            repeatCurrentPatch.setSelected(false);
            }
        }
    
    boolean repeatingCurrentPatch = false;
    public boolean isRepeatingCurrentPatch()
        {
        return repeatingCurrentPatch;
        }
        
    public boolean isSendingTestNotes()
        {
        return sendingTestNotes;
        }

    boolean allowsTransmitsParameters;
    
    public boolean getAllowsTransmitsParameters()
        {
        return allowsTransmitsParameters;
        }

    void doAllowParameterTransmit()
        {
        allowsTransmitsParameters = transmitParameters.isSelected();
        setLastX("" + allowsTransmitsParameters, "AllowTransmitParameters", getSynthNameLocal(), false);
        }
        
    boolean sendsAllSoundsOffBetweenNotes;
    
    public boolean getSendsAllSoundsOffBetweenNotes()
        {
        return sendsAllSoundsOffBetweenNotes;
        }
    
    void doSendsAllSoundsOffBetweenNotes()
        {
        sendsAllSoundsOffBetweenNotes = sendsAllSoundsOffBetweenNotesMenu.isSelected();
        setLastX("" + sendsAllSoundsOffBetweenNotes, "SendAllSoundsOffBetweenNotes", getSynthNameLocal(), false);
        }



    static final int C = 0;
    static final int Db = 1;
    static final int D = 2;
    static final int Eb = 3;
    static final int E = 4;
    static final int F = 5;
    static final int Gb = 6;
    static final int G = 7;
    static final int Ab = 8;
    static final int A = 9;
    static final int Bb = 10;
    static final int B = 11;
    
    public static final int[][] CHORDS = new int[][]
    {{ C }, { C, Eb }, { C, E }, { C, F }, { C, G }, { C, Ab }, { C, A }, { C, C + 12},
    { C, E, G }, { C, Eb, Ab }, { C, F, A }, { C, Eb, G }, { C, E, A }, { C, F, Ab }, { C, G, C + 12} };
    public static final String[] CHORD_NAMES = new String[]
    { "C", "C Eb (min3)", "C E (Maj3)", "C F (4th)", "C G (5th)", "C Ab (min6)", "C A (Maj6)", "C C2 (Oct)",
      "C E G (Maj)", "C Eb Ab (Maj Inv 1)", "C F A (Maj Inv 2)", "C Eb G (min)", "C E A (min Inv 1)", "C F Ab (min Inv 2)", "C G C2 (Oct + 5)" };
    int[] lastChord = new int[0];
    int[] testNoteChord = new int[0];
    void setTestNoteChord(int chord) { testNoteChord = CHORDS[chord]; }
    
    public static final int[] TEST_NOTE_PITCHES = new int[] { 96, 84, 72, 60, 48, 36, 24 };
    int testNote = 60;
    void setTestNotePitch(int note) { testNote = note; }
    public int getTestNotePitch() { return testNote; }
    void playChord()
        {
        if (testNoteChord != null)
            {
            int[] chord = new int[testNoteChord.length];
            try
                {
                for(int i = 0; i < testNoteChord.length; i++)
                    {
                    chord[i] = testNoteChord[i] + getTestNotePitch();
                    tryToSendMIDI(new ShortMessage(ShortMessage.NOTE_ON, getTestNoteChannel(), chord[i], getTestNoteVelocity()));
                    }
                }
            catch (Exception ex)
                {
                Synth.handleException(ex);
                }
            lastChord = chord;
            }
        }       

    public void clearChord()
        {
        if (lastChord != null)
            {
            try
                {
                for(int i = 0; i < lastChord.length; i++)
                    {
                    tryToSendMIDI(new ShortMessage(ShortMessage.NOTE_OFF, getTestNoteChannel(), lastChord[i], 0));
                    }
                }
            catch (Exception ex)
                {
                Synth.handleException(ex);
                }
            }
        lastChord = new int[0];
        }       
        
    /** Override this to customize the MIDI channel of the test note. */
    public int getTestNoteChannel() { return getChannelOut(); }

    int testNoteVelocity = 127;    
    void setTestNoteVelocity(int velocity) { testNoteVelocity = velocity; }
    public int getTestNoteVelocity() { return testNoteVelocity; }
    
    javax.swing.Timer noteTimer = null;
    public void doSendTestNote()
        {
        doSendTestNote(getTestNotePitch(), true);
        }

    public void doSendTestNote(int testNote, final boolean clearOldNotes)
        {
        // possibly clear all notes
        if (clearOldNotes)
            {
            if (getSendsAllSoundsOffBetweenNotes())
                {
                sendAllSoundsOff();
                }
                
            clearChord();
            }
            
        playChord();
                                                         
        // schedule a note off
        if (noteTimer != null) noteTimer.stop();
        noteTimer = new javax.swing.Timer(testNoteLength, new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                if (clearOldNotes)
                    {
                    clearChord();
                    }
                if (noteTimer != null)
                    {
                    noteTimer.stop();
                    noteTimer = null;
                    }
                }
            });
        noteTimer.setRepeats(false);
        noteTimer.start();
        
        // the purpose of the code below is that when we're hill-climbing we often take longer than the full
        // second of the test notes timer to just get the data out and play.  So here we submit our timer,
        // then we tell the test notes timer to reset itself to exactly the same initial delay as our timer.
        // This SHOULD put the test notes timer back in the queue AFTER our note-off timer so we have enough
        // time to turn off the note before the test notes timer fires another note.
        
        if (sendingTestNotes) // sendTestNotesTimer.isRunning())
            {
            sendTestNotesTimer.setInitialDelay(getTestNoteTotalLength() + (tabs.getSelectedComponent() == hillClimbPane ? getPauseBetweenHillClimbPlays() : 0));
            sendTestNotesTimer.restart();
            }
        }

    void doMapCC(int type)
        {
        // has to be done first because doPassThroughCC(false) may turn it off
        setLearningCC(!getLearningCC());
        learningType = type;
        doPassThroughCC(false);
        }
                
    void doClearAllCC()
        {
        if (showSimpleConfirm("Clear CCs", "Are you sure you want to clear all CCs?"))
            clearLearned();
        }
                
    void doPreviousTab()
        {
        setCurrentTab(getCurrentTab() - 1);
        }
                
    void doNextTab()
        {
        setCurrentTab(getCurrentTab() + 1);
        }
                
    void doTab(int tab)
        {
        setCurrentTab(tab);
        }
                
    void doNew()
        {
        instantiate(Synth.this.getClass(), false, true, tuple);
        }
           
    /** This is public to allow Favorites to access it only. */     
    public void doNewSynth(int synth)
        {
        String[] synthNames = getSynthNames();
        instantiate(getSynth(synth), false, true, tuple);
        }
                
    Synth doDuplicateSynth()
        {
        Synth newSynth = instantiate(Synth.this.getClass(), false, true, tuple);
        newSynth.setSendMIDI(false);
        boolean currentPush = newSynth.undo.getWillPush();
        newSynth.undo.setWillPush(false);
        model.copyValuesTo(newSynth.model);
        newSynth.undo.setWillPush(currentPush);
        newSynth.setSendMIDI(true);
        return newSynth;
        }
                
    void doUndo() { doUndo(true); }
    void doUndo(boolean send)
        {
        setSendMIDI(false);
        if (model.equals(undo.top()))
            model = undo.undo(null);  // don't push into the redo stack
        else 
            model = undo.undo(model);
        boolean currentPush = undo.getWillPush();
        undo.setWillPush(false);
        model.updateAllListeners();
        undo.setWillPush(currentPush);
        setSendMIDI(true);
        if (send) sendAllParameters();
        }
                
    void doRedo()
        {
        setSendMIDI(false);
        model = (Model)(undo.redo(getModel()));
        boolean currentPush = undo.getWillPush();
        undo.setWillPush(false);
        model.updateAllListeners();
        undo.setWillPush(currentPush);
        setSendMIDI(true);
        sendAllParameters();
        }
        
    void doQuit()
        {
        if (clearNotes && sendAllSoundsOffWhenWindowChanges())
            {
            sendAllSoundsOff(); // not doSendAllSoundsOff(false) because we don't want to turn off the test notes
            }
        simplePause(50);        // maybe enough time to flush out the all sounds off notes?  dunno
        System.exit(0);
        }
    
    /** Removes the in/out/key devices. */
    void doDisconnectMIDI()
        {
        if (tuple != null)
            tuple.dispose();

        tuple = null;
        setSendMIDI(true);
        updateTitle();
        }

    void doSetNudge(int i, Model model, String name)
        {
        nudge[i] = model.copy(); // (Model)(model.clone());             /// don't need listeners
        nudgeTowards[i].setText("Towards " + (i + 1) + ": " + name);
        nudgeTowards[i + 4].setText("Away from " + (i + 1) + ": " + name);
        }

    void doSetNudge(int i)
        {
        doSetNudge(i, getModel(), getPatchName(getModel()));
        }

    Model getNudge(int i)
        {
        return nudge[i];
        }

    int lastNudge = -1;
        
    double nudgeRecombinationWeight = 0.25;
    double nudgeMutationWeight = 0.10;
    
    void doNudge(int towards)
        {
        if (towards == -1) return;
                
        // this last statement fixes a mystery.  When I call Randomize or Reset on
        // a Blofeld or on a Microwave, all of the widgets update simultaneously.
        // But on a Blofeld Multi or Microwave Multi they update one at a time.
        // I've tried a zillion things, even moving all the widgets from the Blofeld Multi
        // into the Blofeld, and it makes no difference!  For some reason the OS X
        // repaint manager is refusing to coallesce their repaint requests.  So I do it here.
        repaint();
        
        setSendMIDI(false);
        undo.push(model);
        if (towards < 4)
            {
            if (nudgeRecombinationWeight > 0.0) model.recombine(random, nudge[towards], getMutationKeys(),  //useMapForRecombination ? getMutationKeys() : model.getKeys(), 
                nudgeRecombinationWeight);
            if (nudgeMutationWeight > 0.0) model.mutate(random, getMutationKeys(), nudgeMutationWeight);
            }
        else
            {
            if (nudgeRecombinationWeight > 0.0) model.opposite(random, nudge[towards - 4], getMutationKeys(), //useMapForRecombination ? getMutationKeys() : model.getKeys(), 
                nudgeRecombinationWeight, true);
            if (nudgeMutationWeight > 0.0) model.mutate(random, getMutationKeys(), nudgeMutationWeight);
            }
        revise();  // just in case

        setSendMIDI(true);
        sendAllParameters();

        lastNudge = towards;
        }

    double lastMutate = 0.0;

    /** This method is public to allow the test classes to access it. */
    public void doMutate(double probability)
        {
        // this last statement fixes a mystery.  When I call Randomize or Reset on
        // a Blofeld or on a Microwave, all of the widgets update simultaneously.
        // But on a Blofeld Multi or Microwave Multi they update one at a time.
        // I've tried a zillion things, even moving all the widgets from the Blofeld Multi
        // into the Blofeld, and it makes no difference!  For some reason the OS X
        // repaint manager is refusing to coallesce their repaint requests.  So I do it here.
        repaint();

        if (probability == 0.0) 
            return;
                
        setSendMIDI(false);
        undo.setWillPush(false);
        Model backup = (Model)(model.clone());
        
        if (nnRandomize != null && nnRandomize.isSelected())
        	{
        	((ProvidesNN)this).randomizeNNModel(probability).copyValuesTo(model, getMutationKeys());
        	}
        else
        	{
	        model.mutate(random, getMutationKeys(), probability);
	        }
        revise();  // just in case
                
        undo.setWillPush(true);
        if (!backup.keyEquals(getModel()))  // it's changed, do an undo push
            undo.push(backup);
        setSendMIDI(true);
        
        sendAllParameters();
        lastMutate = probability;
        }

    void doPassThroughCC(boolean val)
        {
        setPassThroughCC(val);
        }

    void doPassThroughController(boolean val)
        {
        setPassThroughController(val);
        }

    /** Goes through the process of saving to a new sysex file and associating it with
        the editor. */
    void doSaveAs()
        {
        doSaveAs(null);
        }
        
    /** Goes through the process of saving to a new sysex file and associating it with
        the editor. */
    void doSaveAs(String filename)
        {
        FileDialog fd = new FileDialog((Frame)(SwingUtilities.getRoot(this)), "Save Patch to Sysex File...", FileDialog.SAVE);
                
        if (filename != null)
            {
            fd.setFile(StringUtility.reviseFileName(filename));
            String path = getLastDirectory();
            if (path != null)
                fd.setDirectory(path);
            }
        else if (file != null)
            {
            fd.setFile(StringUtility.reviseFileName(file.getName()));
            fd.setDirectory(file.getParentFile().getPath());
            }
        else
            {
            if (getPatchName(getModel()) != null)
                fd.setFile(StringUtility.reviseFileName(getPatchName(getModel()).trim() + ".syx"));
            else
                fd.setFile(StringUtility.reviseFileName("Untitled.syx"));
            String path = getLastDirectory();
            if (path != null)
                fd.setDirectory(path);
            }            
            
        disableMenuBar();
        fd.setVisible(true);
        enableMenuBar();
        File f = null; // make compiler happy
        FileOutputStream os = null;
        if (fd.getFile() != null)
            try
                {
                f = new File(fd.getDirectory(), StringUtility.ensureFileEndsWith(fd.getFile(), ".syx"));
                os = new FileOutputStream(f);
                os.write(flatten(emitAll((Model)null, false, true)));
                os.close();
                file = f;
                auxFile = null;
                setLastDirectory(fd.getDirectory());
                } 
            catch (IOException e) // fail
                {
                showErrorWithStackTrace(e, "File Error", "An error occurred while saving to the file " + (f == null ? " " : f.getName()));
                Synth.handleException(e);
                }
            finally
                {
                if (os != null)
                    try { os.close(); }
                    catch (IOException e) { }
                }

        updateTitle();
        }


    /** Goes through the process of saving to an existing sysex file associated with
        the editor, else it calls doSaveAs(). */
    void doSave()
        {
        if (file == null)
            {
            doSaveAs();
            } 
        else
            {
            FileOutputStream os = null;
            try
                {
                os = new FileOutputStream(file);
                os.write(flatten(emitAll((Model)null, false, true)));
                os.close();
                }
            catch (Exception e) // fail
                {
                showErrorWithStackTrace(e, "File Error", "An error occurred while saving to the file " + file);
                Synth.handleException(e);
                }
            finally
                {
                if (os != null)
                    try { os.close(); }
                    catch (IOException e) { }
                }
            }

        updateTitle();
        }


    void doCloseWindow()
        {
        JFrame frame = (JFrame)(SwingUtilities.getRoot(this));
        if (frame == null || !frame.isDisplayable()) return;  // we clicked multiple times on the close button
        
        else if (requestCloseWindow())
            {
            if (clearNotes && sendAllSoundsOffWhenWindowChanges())
                {
                sendAllSoundsOff(); // not doSendAllSoundsOff(false) because we don't want to turn off the test notes
                }
                
            // Kill the timers
            if (sendTestNotesTimer != null)
                {
                sendTestNotesTimer.stop();  // notice we don't set it to null
                }
            if (patchTimer != null)
                {
                patchTimer.stop();
                saveBatchPatches();
                patchFileOrDirectory = null;
                patchTimer = null;
                }
            if (noteTimer != null)
                {
                noteTimer.stop();
                noteTimer = null;
                }
                                
            // get rid of MIDI connection
            if (tuple != null)
                tuple.dispose();
            tuple = null;
            
            frame.setVisible(false);
            frame.dispose();
                                
            numOpenWindows--;
            if (numOpenWindows <= 0)
                {                    
                Synth result = doNewSynthPanel();
                if (result == null)
                    {
                    System.exit(0);
                    }
                }
            }
        }
        
    /** Writes out all parameters to a text file. */
    void doSaveText()
        {
        FileDialog fd = new FileDialog((Frame)(SwingUtilities.getRoot(this)), "Write Patch to Text File...", FileDialog.SAVE);
                
        if (getPatchName(getModel()) != null)
            fd.setFile(StringUtility.reviseFileName(getPatchName(getModel()).trim() + ".txt"));
        else
            fd.setFile(StringUtility.reviseFileName("Untitled.txt"));
        String path = getLastDirectory();
        if (path != null)
            fd.setDirectory(path);
                
        disableMenuBar();
        fd.setVisible(true);
        enableMenuBar();
        File f = null; // make compiler happy
        PrintWriter os = null;
        if (fd.getFile() != null)
            try
                {
                f = new File(fd.getDirectory(), StringUtility.ensureFileEndsWith(fd.getFile(), ".txt"));
                os = new PrintWriter(new FileOutputStream(f));
                getModel().print(os);
                os.close();
                setLastDirectory(fd.getDirectory());
                } 
            catch (IOException e) // fail
                {
                showErrorWithStackTrace(e, "File Error", "An error occurred while saving to the file " + (f == null ? " " : f.getName()));
                Synth.handleException(e);
                }
            finally
                {
                if (os != null)
                    os.close();
                }
        }
        
        
        
        
        
        
        
        
    ///// SYSEX UTILITIES
        
    /** Returns all sysex messages in the given MIDI file (not sysex file). */
    public byte[][] extractSysexFromMidFile(File f)
        {
        try
            {
            Sequence seq = MidiSystem.getSequence(f);
            int count = 0;
                
            Track[] t  = seq.getTracks();
            for(int i = 0; i < t.length; i++)
                for(int j = 0; j < t[i].size(); j++)
                    if (t[i].get(j).getMessage() instanceof SysexMessage)
                        count++;
                                                
            byte[][] sysex = new byte[count][];
                        
            count = 0;
            for(int i = 0; i < t.length; i++)
                for(int j = 0; j < t[i].size(); j++)
                    if (t[i].get(j).getMessage() instanceof SysexMessage)
                        {
                        // irritatingly, the sysex data doesn't include the 0xF0
                        byte[] data = ((SysexMessage)(t[i].get(j).getMessage())).getData();
                        sysex[count] = new byte[data.length + 1];
                        sysex[count][0] = (byte)0xF0;
                        System.arraycopy(data, 0, sysex[count], 1, data.length);
                        count++;
                        }

            return sysex;
            }
        catch (Exception ex)
            {
            Synth.handleException(ex);
            return null;
            }
        }


    /** Returns all short, non-sysex messages in the given MIDI file (not sysex file). */
    public ShortMessage[] extractShortMessagesFromFile(File f)
        {
        try
            {
            Sequence seq = MidiSystem.getSequence(f);
            int count = 0;
                
            Track[] t  = seq.getTracks();
            for(int i = 0; i < t.length; i++)
                for(int j = 0; j < t[i].size(); j++)
                    {
                    if (t[i].get(j).getMessage() instanceof ShortMessage)
                        count++;
                    }
                                                
            ShortMessage[] messages = new ShortMessage[count];
                        
            count = 0;
            for(int i = 0; i < t.length; i++)
                for(int j = 0; j < t[i].size(); j++)
                    {
                    if (t[i].get(j).getMessage() instanceof ShortMessage)
                        {
                        messages[count] = (ShortMessage)(t[i].get(j).getMessage());
                        count++;
                        }
                    }

            return messages;
            }
        catch (Exception ex)
            {
            Synth.handleException(ex);
            return null;
            }
        }




    /** Flattens a two-dimensional array to a one-dimensional array, stripping out the non-sysex elements. 
        The array may contain (1) byte[] sysex data (2) Sysex Messages of the form javax.sound.midi.SysexMessage
        (3) other data, notably Integers. #3 is stripped out, and 1 and 2 are concatenated into a stream of
        bytes suitable to save as a file or send to a synthesizer as a collection of sysex messages.
    */
    public byte[] flatten(Object[] data)
        {
        if (data == null)
            return null;
        if (data.length == 0)
            return new byte[0];
        if (data.length == 1 && data[0] instanceof byte[])
            return (byte[])(data[0]);
                
        // otherwise flatten
                
        int len = 0;
        for(int i = 0; i < data.length; i++)
            {
            if (data[i] instanceof byte[])
                {
                len += ((byte[])data[i]).length;
                }
            else if (data[i] instanceof javax.sound.midi.SysexMessage)
                {
                len += ((javax.sound.midi.SysexMessage)data[i]).getLength();
                }
            }
                        
        byte[] result = new byte[len];
        int start = 0;
        for(int i = 0; i < data.length; i++)
            {
            if (data[i] instanceof byte[])
                {
                byte[] b = (byte[])(data[i]);
                System.arraycopy(b, 0, result, start, b.length);
                start += b.length;
                }
            else if (data[i] instanceof javax.sound.midi.SysexMessage)
                {
                // For some reason, getData() doesn't include the 0xF0, even
                // though getLength() considers the 0xF0.  I don't know why.
                result[start++] = (byte)0xF0;
                
                byte[] b = ((javax.sound.midi.SysexMessage)data[i]).getData();
                System.arraycopy(b, 0, result, start, b.length);
                start += b.length;
                }
            }
        return result;
        }


    /** Breaks data into multiple sysex messages, discarding apparent non-sysex data. */
    public static byte[][] cutUpSysex(byte[] data)
        {
        ArrayList sysex = new ArrayList();
        for(int start = 0; start < data.length; start++)
            {
            // Look for next 0xF0
            if (data[start] != (byte)0xF0) continue;
            
            int end;
            for(end = start + 1; end < data.length; end++)
                {
                if (data[end] == (byte)0xF7)
                    {
                    byte[] d = new byte[end - start + 1];
                    System.arraycopy(data, start, d, 0, end - start + 1);
                    sysex.add(d);
                    break;
                    }
                }
            start = end;  // we'll get a start++ in a second at the next for-iteration
            }
        byte[][] b = (byte[][])sysex.toArray(new byte[sysex.size()][]);
        return b;
        } 



    
    /** Collects the sysex messages stored in data into
        Patches, discarding those which do not seem to match anything Edisyn knows about.
    */
    public Patch[] gatherPatchSysex(byte[][] sysex)
        {
        int pos = 0;
        ArrayList patches = new ArrayList();
                
        while(pos < sysex.length)
            {
            // First, do we have a synth for this thing?
            int rec = recognizeSynthForSysex(sysex[pos]);
            if (rec >= 0)
                {
                // How many sysex messages per patch?
                int next = getNextSysexPatchGroup(getClassNames()[rec], sysex, pos);
                if (next > pos)
                    {
                    byte[][] groups = new byte[next - pos][];
                    for(int i = 0; i < groups.length; i++)
                        {
                        groups[i] = sysex[i + pos];
                        }
                    patches.add(new Patch(rec, groups, recognizeBank(getClassNames()[rec], sysex[pos])));
                    pos = next;
                    }
                else
                    {
                    pos++;          // I *think* this should work?
                    }
                }
            else
                {
                pos++;  // skip this sysex message
                }
            }

        Patch[] p = (Patch[])(patches.toArray(new Patch[0]));

        // build names and locations for each patch
        Synth temp = null;
        HashMap map = new HashMap();
        for(int i = 0; i < p.length; i++)
            {
            if (p[i].bank)
                {
                String classname = getClassNames()[p[i].synth];
                String bankname = getBankName(classname, flatten(p[i].sysex));
                if (map.containsKey(classname))
                    {
                    int in = ((Integer)(map.get(classname))).intValue();
                    p[i].name = "Bank Sysex (" + in + ") " + bankname;
                    map.put(classname, Integer.valueOf(in + 1));
                    }
                else
                    {
                    p[i].name = "Bank Sysex " + bankname;
                    map.put(classname, Integer.valueOf(2));
                    }
                }
            else
                {
                // This is easily the slowest part.  We have to 
                // build the synth to parse, then parse it and extract the name
                try
                    {
                    if (temp == null || temp.getClass() != getSynth(p[i].synth))
                        temp = (Synth)(instantiate(getClassNames()[p[i].synth], true, false, null));
                    temp.printRevised = false;
                    temp.setSendMIDI(false);
                    temp.undo.setWillPush(false);
                    temp.getModel().clearListeners();         // otherwise we GC horribly.....
                    temp.performParse(flatten(p[i].sysex), true);
                    p[i].name = temp.getPatchName(temp.model);
                    p[i].location = temp.getPatchLocationName(temp.model);
                    }
                catch (Exception ex)
                    {
                    Synth.handleException(ex);
                    }
                }
            }
                
        return p;
        }


    /** Reduces a group of patches to just those whose synthesizer matches THIS synthesizer.  */
    Patch[] reducePatchesToLocal(Patch[] patches)
        {
        String thisClassName = getClass().getName();
        int count = 0;
        for(int i = 0; i < patches.length; i++)
            {
            if (thisClassName.equals(getClassNames()[patches[i].synth]))
                count++;
            }
        Patch[] reduced = new Patch[count];
        count = 0;
        for(int i = 0; i < patches.length; i++)
            {
            if (thisClassName.equals(getClassNames()[patches[i].synth]))
                reduced[count++] = patches[i];
            }
        return reduced;
        }


    /** Extracts indices into a patch[] array in the form int[synthtype][patchnum], so we can 
        grab out of patch[] all the patches for a given synthesizer. */
    int[][] extractPatchIndices(Patch[] patches)
        {
        // compute how many different kinds of synths we have
        int count = 0;
        boolean[] set = new boolean[synthClassNames.length];
        for(int i = 0; i < patches.length; i++)
            {
            if (!set[patches[i].synth])
                {
                count++;
                set[patches[i].synth] = true;
                }
            }
                                
        // next compute the different synths and the counts for each
        int[] synths = new int[count];
        count = 0;
        set = new boolean[synthClassNames.length];
        for(int i = 0; i < patches.length; i++)
            {
            if (!set[patches[i].synth])
                {
                synths[count] = patches[i].synth;
                count++;
                set[patches[i].synth] = true;
                }
            }

        // next count the patches for each synth
        int[] counts = new int[count];
        for(int i = 0; i < patches.length; i++)
            {
            for(int j = 0; j < synths.length; j++)
                {
                if (patches[i].synth == synths[j])  // found it
                    {
                    counts[j]++;
                    break;
                    }
                }
            }

        // next prepare the ragged patch slots for each synth
        int[][] indices = new int[synths.length][];
        for(int i = 0; i < indices.length; i++)
            {
            indices[i] = new int[counts[i]];
            }
                        
        // next load the indices
        counts = new int[count];
        for(int i = 0; i < patches.length; i++)
            {
            for(int j = 0; j < synths.length; j++)
                {
                if (patches[i].synth == synths[j])  // found it
                    {
                    indices[j][counts[j]] = i;
                    counts[j]++;
                    break;
                    }
                }
            }
                
        return indices;
        }

    /** Extracts the class names for synthesizers from the given patches, in the order provided by the outer array of indices. 
        Indices were generated by extractPatchIndices(). 
    */
    String[] extractSynthClassNames(Patch[] patches, int[][] indices)
        {
        String[] names = new String[indices.length];
        for(int i = 0; i < indices.length; i++)
            {
            names[i] = getClassNames()[patches[indices[i][0]].synth];
            }
        return names;
        }

    /** Extracts the synthesizer names for synthesizers from the given patches, in the order provided by the outer array of indices.  
        Indices were generated by extractPatchIndices().
    */
    String[] extractSynthNames(Patch[] patches, int[][] indices)
        {
        String[] names = new String[indices.length];
        for(int i = 0; i < indices.length; i++)
            {
            names[i] = getSynthNames()[patches[indices[i][0]].synth];
            }
        return names;
        }

    /** Extracts the patch names for synthesizers from the given patches, in the same structure as provided by indices.
        Indices were generated by extractPatchIndices().
    */
    String[][] extractPatchNames(Patch[] patches, int[][] indices)
        {
        String[][] names = new String[indices.length][];
        for(int i = 0; i < indices.length; i++)
            {
            names[i] = new String[indices[i].length];
            for(int j = 0; j < indices[i].length; j++)
                {
                String location = patches[indices[i][j]].location;
                String name = patches[indices[i][j]].name;
                if (location != null)
                    names[i][j] = location + "   " + name;
                else
                    names[i][j] = name;
                }
            }
        return names;
        }
        
    String[] flattenNames(String[][] names)
        {
        ArrayList<String> n = new ArrayList<String>();
        for(int i = 0; i < names.length; i++)
            for(int j = 0; j < names[i].length; j++)
                n.add(names[i][j]);
        return (String[])(n.toArray(new String[0]));
        }

    /** Reorganizes the patches in the order provided by indices. 
        Indices were generated by extractPatchIndices().
    */
    Patch[][] organizePatches(Patch[] patches, int[][] indices)
        {
        Patch[][] p = new Patch[indices.length][];
        for(int i = 0; i < indices.length; i++)
            {
            p[i] = new Patch[indices[i].length];
            for(int j = 0; j < indices[i].length; j++)
                {
                p[i][j] = patches[indices[i][j]];
                }
            }
        return p;
        }
        
    Patch[] flattenPatches(Patch[][] patches)
        {
        int len = 0;
        for(int i = 0; i < patches.length; i++)
            len += patches[i].length;
        Patch[] p = new Patch[len];
        int count = 0;
        for(int i = 0; i < patches.length; i++)
            for(int j = 0 ; j < patches[i].length; j++)                     
                {
                p[count++] = patches[i][j];
                }
        return p;
        }

    byte[][][] extractSysex(Patch[] patches)
        {
        byte[][][] data = new byte[patches.length][][];
        for(int i = 0; i < patches.length; i++)
            data[i] = patches[i].sysex;
        return data;
        }

    byte[][] flattenSysex(byte[][][] data)
        {
        ArrayList<byte[]> messages = new ArrayList<byte[]>();
        for(int i = 0; i < data.length; i++)
            for(int j = 0; j < data[i].length; j++)
                messages.add(data[i][j]);
        return (byte[][])(messages.toArray(new byte[0][0]));
        }


    // Returns 0 if cancelled, 1 if corrupt, fix, 2 if not corrupt
    int checkCorruptSysex(byte[] data)
        {
        for(int i = 1; i < data.length - 1; i++)        // not first or last
            {
            if (data[i] < 0) // uh oh
                {
                if (showSimpleConfirm("Corrupt Sysex Data", "This data appears to have some corruption.\nTry to fix and upload?", "Try to Fix and Upload"))
                    {
                    return 1;
                    }
                else
                    {
                    return 0;
                    }
                }
            }
        return 2;
        }

    // Returns 0 if cancelled, 1 if corrupt, fix, 2 if not corrupt
    int checkCorruptSysex(byte[][] data)
        {
        for(int i = 0; i < data.length; i++)
            {
            int ret = checkCorruptSysex(data[i]);
            if (ret == 0) // cancelled
                return 0;
            else if (ret == 1)      //fix
                return 1;
            }
        return 2;
        }

    /** Returns TRUE if fixed or not corrupt, FALSE if cancelled */
    public boolean fixCorruptSysex(byte[] data)
        {
        int ret = checkCorruptSysex(data);
        if (ret == 0) return false;
        if (ret == 2) return true;
        for(int i = 1; i < data.length - 1; i++)        // not first or last
            {
            // fix
            if (data[i] < 0) data[i] = (byte)(data[i] & 127);
            }
        return true;
        }

    /** Returns TRUE if fixed or not corrupt, FALSE if cancelled */
    public boolean fixCorruptSysex(byte[][] data)
        {
        int ret = checkCorruptSysex(data);
        if (ret == 0) return false;
        if (ret == 2) return true;
        for(int j = 0; j < data.length; j++)
            {
            for(int i = 1; i < data[j].length - 1; i++)     // not first or last
                {
                // fix
                if (data[j][i] < 0) data[j][i] = (byte)(data[j][i] & 127);
                }
            }
        return true;
        }
    












    /////// FILE OPENING AND BULK SYSEX HANDLING

    
    class UString 
        {
        String string;
        public UString(String str) { string = str; }
        public String toString() { return string; }
        }
        
    UString[] makeUniqueStrings(String[] strings)
        {
        UString[] strs = new UString[strings.length];
        for(int i = 0; i < strings.length; i++)
            strs[i] = new UString(strings[i]);
        return strs;
        }
        
    UString[][] makeUniqueStrings(String[][] strings)
        {
        UString[][] strs = new UString[strings.length][];
        for(int i = 0; i < strings.length; i++)
            {
            strs[i] = new UString[strings[i].length];
            for(int j = 0; j < strings[i].length; j++)
                {                       
                strs[i][j] = new UString(strings[i][j]);
                }
            }
        return strs;
        }

    public static final int BULK_DIALOG_RESULT_CANCEL = 0;
    public static final int BULK_DIALOG_RESULT_LOCAL = 1;
    public static final int BULK_DIALOG_RESULT_NEW = 2;
    public static final int BULK_DIALOG_RESULT_WRITE = 3;
    public static final int BULK_DIALOG_RESULT_BREAK_OUT = 4;
    public static final int BULK_DIALOG_RESULT_INDIVIDUAL = 5;
        
    /** Displays a bulk dialog panel for patches, returning data in the form { result, primary, secondary }.
        primary and secondary are only meaningful if result is BULK_DIALOG_RESULT_LOCAL or BULK_DIALOG_RESULT_NEW. */
    public int[] displayLoadBulkDialog(Patch[] patches, int[][] patchIndices, String title, String localLoad, 
        String newLoad, String writeAll, String breakOut, String individual, String cancel, boolean reduced )
        {
        String[][] patchnames = extractPatchNames(patches, patchIndices);
        String[] classnames = extractSynthClassNames(patches, patchIndices);
        String[] synthnames = extractSynthNames(patches, patchIndices);
                
        // build the two-level menu
        final JButton localButton = new JButton(localLoad);
        localButton.setEnabled(Synth.this.getClass().getName().equals(classnames[0]));

        TwoLevelMenu menu = new TwoLevelMenu(synthnames, makeUniqueStrings(patchnames), "Synth", "Patch", 0, 0)
            {
            public void selection(int primary, int secondary)
                {
                localButton.setEnabled(Synth.this.getClass().getName().equals(classnames[primary]));
                }
            };
                                                        
        Color color = new JPanel().getBackground();
        HBox hbox = new HBox();
        hbox.setBackground(color);
        VBox vbox = new VBox();
        vbox.setBackground(color);
        vbox.add(new JLabel("   "));
        vbox.add(new JLabel("Choose a patch."));
        vbox.add(new JLabel("   "));
        hbox.addLast(vbox);
        vbox = new VBox();
        vbox.setBackground(color);
        vbox.add(hbox);
        vbox.add(menu);

        JComboBox actions = null;
        if (!reduced)
            {
            actions = new JComboBox(new String[] { localLoad, newLoad, writeAll, breakOut, individual });
            vbox.add(new JSeparator());
            vbox.add(new JLabel("   "));

            JPanel actionsPanel = new JPanel();
            actionsPanel.setLayout(new BorderLayout());
            actionsPanel.add(new JLabel("Action  "), BorderLayout.WEST);
            actionsPanel.add(actions, BorderLayout.CENTER);
            vbox.add(actionsPanel);
            }

        final JOptionPane pane = new JOptionPane(vbox,  JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, 
            reduced ?
            new Object[] { localButton, "Cancel" } :
            new Object[] {  "Do Action", "Cancel" }, 
            reduced ? 
            localButton : "Do Action");
                                                                
        localButton.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                pane.setValue(localButton);
                }
            });
                                                                        
        JDialog dialog = pane.createDialog(this, title);
        disableMenuBar();
        dialog.setVisible(true);
        enableMenuBar();
        Object result = pane.getValue();
        if (result == null) return new int[] { BULK_DIALOG_RESULT_CANCEL, menu.getPrimary(), menu.getSecondary() };
        else if (result.equals(localButton)) return new int[] { BULK_DIALOG_RESULT_LOCAL, menu.getPrimary(), menu.getSecondary() };
        else if (result.equals("Do Action")) 
            {
            if (actions.getSelectedIndex() == 0)  // local load
                return new int[] { BULK_DIALOG_RESULT_LOCAL, menu.getPrimary(), menu.getSecondary() };
            else if (actions.getSelectedIndex() == 1)  // new load
                return new int[] { BULK_DIALOG_RESULT_NEW, menu.getPrimary(), menu.getSecondary() };
            else if (actions.getSelectedIndex() == 2)  // write
                return new int[] { BULK_DIALOG_RESULT_WRITE, menu.getPrimary(), menu.getSecondary() };
            else if (actions.getSelectedIndex() == 3)  // save by type
                return new int[] { BULK_DIALOG_RESULT_BREAK_OUT, menu.getPrimary(), menu.getSecondary() };
            else // if (actions.getSelectedIndex() == 3)  // save individually
                return new int[] { BULK_DIALOG_RESULT_INDIVIDUAL, menu.getPrimary(), menu.getSecondary() };
            }
        // cancelled in some way.  An apparent bug in JOptionPane is such that if you press ESC, then -1 is returned by getValue().  It should be returning null.
        else return new int[] { BULK_DIALOG_RESULT_CANCEL, menu.getPrimary(), menu.getSecondary() };
        }
                

    boolean doOpen(boolean merge)
        {
        parsingForMerge = merge;
        mergeSynth = null;

        boolean succeeded = false;
        String[] synthNames =  getSynthNames();
        int numDumpsPerPatch = 1;
        
        
        //// FIRST we have the user choose a file
        
        FileDialog fd = new FileDialog((Frame)(SwingUtilities.getRoot(this)), "Load Sysex Patch File...", FileDialog.LOAD);
        fd.setFilenameFilter(new FilenameFilter()
            {
            public boolean accept(File dir, String name)
                {
                return StringUtility.ensureFileEndsWith(name, ".syx").equals(name) || StringUtility.ensureFileEndsWith(name, ".SYX").equals(name) || StringUtility.ensureFileEndsWith(name, ".sysex").equals(name) || StringUtility.ensureFileEndsWith(name, ".mid").equals(name) || StringUtility.ensureFileEndsWith(name, ".MID").equals(name) || StringUtility.ensureFileEndsWith(name, ".midi").equals(name) || StringUtility.ensureFileEndsWith(name, ".MIDI").equals(name);
                }
            });

        if (file != null)
            {
            fd.setFile(file.getName());
            fd.setDirectory(file.getParentFile().getPath());
            }
        else
            {
            String path = getLastDirectory();
            if (path != null)
                fd.setDirectory(path);
            }
                
        disableMenuBar();
        fd.setVisible(true);
        enableMenuBar();
        File f = null; // make compiler happy
        FileInputStream is = null;
        if (fd.getFile() != null)
            {
            try
                {
                f = new File(fd.getDirectory(), fd.getFile());
                
                //// SECOND if the file is really big we check to see if that's okay

                is = new FileInputStream(f);
                boolean okay = true;
                if (f.length() > MAX_FILE_LENGTH)
                    {
                    okay = showSimpleConfirm("File too Large", "This file is very large.  Loading it could hang Edisyn.\nLoad anyway?");
                    }
                
                if (okay)
                    {
                    //// THIRD we extract all sysex from the file

                    byte[][] data;
                    String filename = f.getName();
                    if (filename.endsWith(".mid") || filename.endsWith(".MID") || filename.endsWith(".midi") || filename.endsWith(".MIDI"))
                        {
                        data = extractSysexFromMidFile(f);
                        is.close();
                        }
                    else        // sysex file
                        {
                        byte[] d = new byte[(int)f.length()];
                        readFully(d, is);
                        is.close();
                        data = cutUpSysex(d);
                        }
                        
                    if (data == null || data.length == 0) // wasn't sysex, or we couldn't cut it up right.  Maybe someone still recognizes it.
                        {
                        showSimpleError("File Error", "File does not appear to contain sysex data.");
                        succeeded = false;
                        }
                    else 
                        {
                        //// FOURTH we break the sysex into patches we can work with

                        Patch[] patches = gatherPatchSysex(data);
                        if (merge) patches = reducePatchesToLocal(patches);                     // this will reduce us to just our own kind of patches
                                                
                        //// FIFTH if we can't find any usable patches, inform the user

                        if (patches.length == 0)                // no patches anyone recognizes
                            {
                            if (isShowingLimitedBankSysex() || merge)
                                {
                                showSimpleError("Load Error", "File does not contain a patch that Edisyn can load.");
                                succeeded = false;                                                              
                                }
                            else
                                {
                                // offer to upload anyway
                                succeeded = unknownSysexFileError(data);
                                }
                            }

                        //// SIXTH if there's just one patch, load it

                        else if (patches.length == 1)   // just a single patch, so we load it
                            {
                            succeeded = loadOne(flatten(patches[0].sysex), patches[0].synth, recognizeAnyForLocal(data), merge, f, fd, false);
                            }

                        //// SEVENTH if there are multiple patches, offer the user several options

                        else                                                    // multiple patches, let's display them
                            {
                            int[][] patchIndices = extractPatchIndices(patches);
                            String[] classNames = extractSynthClassNames(patches, patchIndices);
                            Patch[][] pat = organizePatches(patches, patchIndices);

                            int[] results = displayLoadBulkDialog(
                                patches, 
                                patchIndices,
                                "Choose Patch from File",
                                merge ? "Merge" : (isShowingLimitedBankSysex() ? "Load" : "Load In This Editor"), 
                                "Load in New Editor", 
                                "Write All Patches to Synth...", 
                                "Save All Patches as Bulk...", 
                                "Save All Patches Individually...", 
                                "Cancel", 
                                (isShowingLimitedBankSysex() || merge) );
                            int result = results[0];
                            int primary = results[1];
                            int secondary = results[2];
                                                        
                            if (result == BULK_DIALOG_RESULT_CANCEL)
                                {
                                succeeded = false;
                                }
                            else if (result == BULK_DIALOG_RESULT_LOCAL)
                                {
                                succeeded = loadOne(flatten(pat[primary][secondary].sysex), pat[primary][secondary].synth, 
                                    this.getClass().equals(getSynth(pat[primary][secondary].synth)), merge, null, fd, false);
                                }
                            else if (result == BULK_DIALOG_RESULT_NEW)
                                {
                                succeeded = loadOneExternal(flatten(pat[primary][secondary].sysex), getSynth(pat[primary][secondary].synth), null, fd);
                                }
                            else if (result == BULK_DIALOG_RESULT_WRITE)
                                {
                                String[][] names = extractPatchNames(patches, patchIndices);
                                if (pat.length == 1)
                                    {
                                    if (showSimpleConfirm("Write All Patches", "Write all patches to the synth?"))
                                        {
                                        succeeded = true;
                                        byte[][][] d = extractSysex(pat[0]);            // will modify the sysex in the patches themselves, but that's okay, we're not reusing them
                                        for(int i = 0; i < classNames.length; i++)
                                            {
                                            Synth temp = (Synth)(instantiate(classNames[i], true, false, null));
                                            if (!temp.adjustBulkSysexForWrite(this, d))
                                                { succeeded = false; break; }
                                            }
                                        if (succeeded)
                                            {
                                            succeeded = writeBulk((Synth)(instantiate(classNames[0], true, false, null)), /*names[0]*/ null, flattenSysex(d));
                                            }
                                        }
                                    else succeeded = false;
                                    }
                                else
                                    {
                                    succeeded = true;
                                    int res = showMultiOption(this, new String[0], new JComponent[0], 
                                        new String[] { "Just This Synth", "All Synths", "Cancel" },
                                        0, "Write All Patches", 
                                        new JLabel("Write patches for all synth types in file, or just for " + getSynthNames()[pat[primary][0].synth] + "?"));
                                    if (res < 0 || res == 2)
                                        succeeded = false;
                                    else if (res == 0)
                                        {
                                        succeeded = true;
                                        byte[][][] d = extractSysex(pat[primary]);              // will modify the sysex in the patches themselves, but that's okay, we're not reusing them
                                        for(int i = 0; i < classNames.length; i++)
                                            {
                                            Synth temp = (Synth)(instantiate(classNames[i], true, false, null));
                                            if (!temp.adjustBulkSysexForWrite(this, d))
                                                { succeeded = false; break; }
                                            }
                                        if (succeeded)
                                            {
                                            succeeded = writeBulk((Synth)(instantiate(classNames[primary], true, false, null)), /*names[primary]*/ null, flattenSysex(d));
                                            }
                                        }
                                    else
                                        {
                                        for(int j = 0; j < pat.length; j++)
                                            {
                                            byte[][][] d = extractSysex(pat[j]);            // will modify the sysex in the patches themselves, but that's okay, we're not reusing them
                                            for(int i = 0; i < classNames.length; i++)
                                                {
                                                Synth temp = (Synth)(instantiate(classNames[i], true, false, null));
                                                if (!temp.adjustBulkSysexForWrite(this, d))
                                                    { succeeded = false; break; }
                                                }
                                            if (!succeeded) break;
                                            }
                                        if (succeeded)
                                            {
                                            byte[][][] d = extractSysex(flattenPatches(pat));
                                            succeeded = writeBulk((Synth)(instantiate(classNames[0], true, false, null)), /*flattenNames(names)*/ null, flattenSysex(d));
                                            }
                                        }
                                    }
                                }
                            else if (result == BULK_DIALOG_RESULT_BREAK_OUT)
                                {
                                if (pat.length == 1)
                                    {
                                    //if (showSimpleConfirm("Save Bulk File...", "Save a bulk file for " + getSynthNames()[pat[primary][0].synth] + "?"))
                                        {
                                        succeeded = saveAllPatches(pat, 0, true);
                                        }
                                    //  else succeeded = false;
                                    }
                                else
                                    {
                                    int res = showMultiOption(this, new String[0], new JComponent[0], 
                                        new String[] { "Just This Synth", "All Synths", "Cancel" },
                                        0, "Save Bulk Files...", 
                                        new JLabel("Save bulk files for all synth types in this file, or just for " + getSynthNames()[pat[primary][0].synth] + "?"));
                                    if (res < 0 || res == 2)
                                        succeeded = false;
                                    else succeeded = saveAllPatches(pat, (res == 0 ? primary : -1), true);
                                    }
                                }
                            else if (result == BULK_DIALOG_RESULT_INDIVIDUAL)
                                {
                                if (pat.length == 1)
                                    {
                                    // if (showSimpleConfirm("Save Individual Files...", "Save a individual files for " + getSynthNames()[pat[primary][0].synth] + "?"))
                                        {
                                        succeeded = saveAllPatches(pat, 0, false);
                                        }
                                    //  else succeeded = false;
                                    }
                                else
                                    {
                                    int res = showMultiOption(this, new String[0], new JComponent[0], 
                                        new String[] { "Just This Synth", "All Synths", "Cancel" },
                                        0, "Save Individual Files...", 
                                        new JLabel("Save all Synths in File, or just for " + getSynthNames()[pat[primary][0].synth] + "?"));
                                    if (res < 0 || res == 2)
                                        succeeded = false;
                                    else succeeded = saveAllPatches(pat, (res == 0 ? primary : -1), false);
                                    }
                                }
                            else            /// uh....`
                                {
                                succeeded = false;
                                }
                            }
                        }
                    }
                else            // "not okay"
                    {
                    succeeded = false;
                    }
                }        
            catch (Throwable e) // fail  -- could be an Error or an Exception
                {
                showErrorWithStackTrace(e, "File Error", "An error occurred while loading from the file.");
                Synth.handleException(e);
                }
            finally
                {
                if (is != null)
                    try { is.close(); }
                    catch (IOException e) { }
                }
            }
                
        updateTitle();
        parsingForMerge = false;
        return succeeded;
        }
    
    
    /** 
        Returns a directory selected by the user, either for saving or for loading.
        This method uses Mac-specific features to display a better file chooser
        than the JFileChooser.
    */ 
    public File selectDirectory(String title, File initialParentDirectory, boolean save)
        {
        Frame parent = (Frame)(SwingUtilities.getRoot(this));
        
        if (Style.isMac())
            {
            FileDialog fd = new FileDialog(parent, title, save ? FileDialog.SAVE : FileDialog.LOAD);
            if (initialParentDirectory != null)
                fd.setDirectory(initialParentDirectory.getAbsolutePath());
            disableMenuBar();
            System.setProperty("apple.awt.fileDialogForDirectories", "true");
            fd.setVisible(true);
            System.setProperty("apple.awt.fileDialogForDirectories", "false");
            enableMenuBar();
            if (fd.getFile() == null)
                {
                return null;
                }
            else 
                {
                return new File(fd.getFile());
                }
            }
        else
            {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle(title);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            if (initialParentDirectory != null)
                chooser.setCurrentDirectory(initialParentDirectory);
            disableMenuBar();         
            if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION)
                {
                enableMenuBar();
                return chooser.getSelectedFile();
                }
            else
                {
                return null;
                }
            }
        } 
    
    
    // This saves out a collection of patches to a directory in one of two ways.
    // If groupByType is TRUE, then the patches are grouped by synth type, and written out
    // as bulk files with the name of the synth type.
    // If groupByType is FALSE, then the patches are all written individually.
    // In this case, if more than one synth type exists, separate subdirectories are
    // made for each synth type.  Normally patchType is -1, meaning all patch types;
    // otherwise it indicates saving only a specific type.
        
    boolean saveAllPatches(Patch[][] patches, int patchType, boolean groupByType)
        {
        File dir = selectDirectory(groupByType ? "Select Directory to Save Patch Groups" : "Select Directory to Save Patches",
            file != null ? new File(file.getParentFile().getPath()) : (getLastDirectory() == null ? new File(getLastDirectory()) : null), 
            true);
        if (dir != null) setLastDirectory(dir.getParent());
        
        /*
          JFileChooser chooser = new JFileChooser();
          chooser.setDialogTitle(groupByType ? "Select Directory to Save Patch Groups" : "Select Directory to Save Patches");
          chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          chooser.setAcceptAllFileFilterUsed(false);
        
          if (file != null)
          {
          chooser.setCurrentDirectory(new File(file.getParentFile().getPath()));
          }
          else
          {
          String path = getLastDirectory();
          if (path != null)
          chooser.setCurrentDirectory(new File(path));
          }
          disableMenuBar();         
          if (chooser.showOpenDialog((Frame)(SwingUtilities.getRoot(this))) != JFileChooser.APPROVE_OPTION)
          {
          enableMenuBar();
          return false;                   // cancelled
          }
          else
          {
          enableMenuBar();
        */

        if (dir == null)
            {
            return false;
            }
        else
            {
            //File dir = chooser.getSelectedFile();          
            if (groupByType)
                {
                int start = 0;
                int end = patches.length;
                if (patchType != -1)
                    {
                    start = patchType;
                    end = patchType + 1;
                    }
                                        
                for(int i = start; i < end; i++)
                    {
                    FileOutputStream os = null;
                    String name = getSynthNames()[patches[i][0].synth];
                    File f = new File(dir, name + ".syx");
                    try
                        {
                        os = new FileOutputStream(f);
                        for(int j = 0; j < patches[i].length; j++)
                            {
                            os.write(flatten(patches[i][j].sysex));
                            }
                        os.close();
                        }
                    catch (IOException e) // fail
                        {
                        showErrorWithStackTrace(e, "Patch Save Error.", "An error occurred while saving to the file " + (f == null ? " " : f.getName()));
                        Synth.handleException(e);
                        return false;
                        }
                    finally
                        {
                        if (os != null)
                            try { os.close(); }
                            catch (IOException e) { }
                        }
                    }
                }
            else
                {
                int start = 0;
                int end = patches.length;
                if (patchType != -1)
                    {
                    start = patchType;
                    end = patchType + 1;
                    }

                File subdir = dir;
                for(int i = start; i < end; i++)
                    {
                    int count = 1;
                    if (end - start > 1)            // more than one being saved out
                        {
                        String name = getSynthNames()[patches[i][0].synth];
                        subdir = new File(dir, name);
                        if (!subdir.mkdir())
                            {
                            showSimpleError("Patch Save Error", "Could not create the subdirectory" + subdir + "\nIs there possibly a file by that name already?");
                            return false;
                            }
                        }
                                                
                    FileOutputStream os = null;
                    for(int j = 0; j < patches[i].length; j++)
                        {
                        String name = ("" + (count++) + (patches[i][j].name == null ? "" : "." + patches[i][j].name));
                        File f = new File(subdir, name + ".syx");
                        try
                            {
                            os = new FileOutputStream(f);
                            os.write(flatten(patches[i][j].sysex));
                            os.close();
                            }
                        catch (IOException e) // fail
                            {
                            showErrorWithStackTrace(e, "Patch Save Error.", "An error occurred while saving to the file " + (f == null ? " " : f.getName()));
                            Synth.handleException(e);
                            return false;
                            }
                        finally
                            {
                            if (os != null)
                                try { os.close(); }
                                catch (IOException e) { }
                            }
                        }                                               
                    }
                }
            }
        return true;
        }
        

    // This writes out a bulk sysex, using a JProgressBar and a cancel button.  It's a complex thing to
    // do because it's using a JOptionPane, which is modal and takes control over everything.  To make 
    // things more complex, this method is called from the Swing event thread.  So I have to use a Timer
    // to write everything out one by one.
    //
    // You pass in all the sysex to write out, plus a synth which ought to be typical of the
    // data being written (it'll be queried with getPauseAfterWritePatch() to determine how much time
    // to wait in-between writes). 
    // 
    // if synth == null, then the pause between writes will be DEFAULT_BULK_WRITE_PAUSE (150ms)
        
    public static final int DEFAULT_BULK_WRITE_PAUSE = 150;
        
    boolean writeBulk(Synth synth, final String[] names, byte[][] data)
        {
        if (tuple == null || tuple.out == null)
            {
            if (!setupMIDI())
                return false;
            }
            
        if (!fixCorruptSysex(data))
            return false;
                    
        final boolean[] writeBulkCancelled = new boolean[] { false };
        final JProgressBar bar = new JProgressBar(0, data.length - 1);
        final JLabel patchName = new JLabel("");
        SwingUtilities.invokeLater(new Runnable()
            {
            public void run()
                {
                showMultiOption(Synth.this, (names == null ? new String[] { "Progress " } : new String[] { "Progress ", "Original Location/Name " }), 
                    (names == null ? new JComponent[] { bar } : new JComponent[] { bar, patchName }), 
                    new String[] { "Cancel" }, 0, "Bulk Write", "Writing patches to synthesizer...");
                writeBulkCancelled[0] = true;
                }
            });
                
        final byte[][] dat = data;
        final int[] index = new int[] { 0 };
        final boolean[] invalid = new boolean[] { false };
        final javax.swing.Timer[] timer = new javax.swing.Timer[1];
        final int pause = (synth == null ? DEFAULT_BULK_WRITE_PAUSE : synth.getPauseAfterWritePatch() + 1);
                
        final long time = System.currentTimeMillis();
        timer[0] = new javax.swing.Timer(pause , new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                if (index[0] >= data.length || writeBulkCancelled[0])   // finished
                    {
                    Window win = SwingUtilities.getWindowAncestor(bar);
                    if (win != null) win.dispose();                 // force close, though it may already be closed
                    if (timer[0] != null) timer[0].stop();
                                        
                    long time2 = System.currentTimeMillis();
                    if (time2 > time && time2 - time < 1000)
                        simplePause((int)(1000L - (time2 - time)));             // this is a decorative pause to give the user time to spot the window in case it appears and disappears rapidly
                                        
                    if (invalid[0])
                        showSimpleError("Write Error", "Some patches could not be written.");
                    }
                else
                    {
                    if (!tryToSendSysex(dat[index[0]])) invalid[0] = true;  // we ignore the return value because we'll try 
                    simplePause(pause);
                    index[0]++;
                    bar.setValue(index[0] + 1);
                    if (names != null && index[0] < names.length)
                        patchName.setText(names[index[0]]);
                    }
                }
            });
        timer[0].start();

        return true;
        }
                        
                        
                        
    // Private function used by doOpen(...) to issue an error when Edisyn doesn't know how to parse
    // the provided sysex data.
    boolean unknownSysexFileError(byte[][] data)
        {
        String val = Midi.getManufacturerForSysex(data[0]);
               
        String message = "File might contain sysex data but has an invalid manufacturer ID.";  
                                                                                                       
        if (val != null)
            message = "<html>File does not contain sysex data that Edisyn knows how to load.<br>" +
                "This appears to be sysex data from the following manufacturer:<br>" + val + "</html>";
                
        Object[] options = { "Write to MIDI Anyway", "Cancel" };
        int result = JOptionPane.showOptionDialog(Synth.this, message, "File Error", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
        if (result == 0)
            {
            // double check
            if (showSimpleConfirm("Write All Patches", "Write all patches to MIDI?"))
                {
                writeBulk(null, null, data);
                return true;
                }
            else return false;
            }
        else return false;
        }


    // Private function used by doOpen(...) to load either locally or externally.  We provide
    // enough information for both situations, but the LOCAL boolean determines what to do.
    boolean loadOne(byte[] data, int synth, boolean local, boolean merge, File f, FileDialog fd, boolean dontVerifyExternal)
        {
        String[] synthNames = getSynthNames();

        if (local)
            return loadOneLocal(data, merge, f, fd);
        else 
            {
            if (dontVerifyExternal || showSimpleConfirm("Load Other Synth Patch Editor",
                    "File doesn't contain sysex data for the " + getSynthNameLocal() + 
                    ".\nIt appears to contain data for the " + synthNames[synth] + 
                    ".\nLoad for the " + synthNames[synth] + " instead?"))
                {
                return loadOneExternal(data, getSynth(synth), f, fd);
                }
            else
                {
                return false;
                }                                               
            }
        }
        
    // Private function used by doOpen(...) to load locally
    boolean loadOneLocal(byte[] data, boolean merge, File f, FileDialog fd)
        {
        boolean succeeded;
        
        // this last statement fixes a mystery.  When I call Randomize or Reset on
        // a Blofeld or on a Microwave, all of the widgets update simultaneously.
        // But on a Blofeld Multi or Microwave Multi they update one at a time.
        // I've tried a zillion things, even moving all the widgets from the Blofeld Multi
        // into the Blofeld, and it makes no difference!  For some reason the OS X
        // repaint manager is refusing to coallesce their repaint requests.  So I do it here.
        repaint();  

        setSendMIDI(false);
        undo.setWillPush(false);
        Model backup = (Model)(model.clone());
        if (merge)
            {
            int res = merge(data, getMergeProbability());
            succeeded = (res == PARSE_SUCCEEDED || res == PARSE_SUCCEEDED_UNTITLED);
            if (!succeeded)
                {
                showSimpleError("File Error", "Could not read the patch.");
                }
            else
                {
                undo.setWillPush(true);
                if (!backup.keyEquals(getModel()))  // it's changed, do an undo push
                    undo.push(backup);
                }
            }
        else
            {
            int result = PARSE_ERROR;
            
            try
                {
                result = performParse(data, true);
                }
            catch (Exception ex)
                {
                Synth.handleException(ex);
                // The default result will be PARSE_ERROR
                }
                                
            if (result == PARSE_FAILED)
                {
                showSimpleError("File Error", "Could not read the patch.");
                succeeded = false;
                }
            else if (result == PARSE_ERROR)
                {
                showSimpleError("File Error", "An error occurred on reading the patch.");
                succeeded = false;
                }
            else                // including PARSE_CANCELLED
                {
                undo.setWillPush(true);
                if (!backup.keyEquals(getModel()))  // it's changed, do an undo push
                    undo.push(backup);
                if (result == PARSE_SUCCEEDED)
                    {
                    file = f;
                    auxFile = null;
                    }
                else if (result == PARSE_SUCCEEDED_UNTITLED)
                    {
                    file = null;
                    auxFile = f;
                    }
                setLastDirectory(fd.getDirectory());

                if (result == PARSE_SUCCEEDED || result == PARSE_SUCCEEDED_UNTITLED)
                    {
                    succeeded = true;
                    }
                else
                    succeeded = false;
                }
            }

        updateTitle();  // so I show the right filename -- this may not be necessary

        setSendMIDI(true);
                                                                
        return succeeded;       
        }
    
    // Private function used by doOpen(...) to load externally
    boolean loadOneExternal(byte[] data, Class synthClass, File f, FileDialog fd)
        {
        Synth otherSynth = instantiate(synthClass, false, true, null);
        otherSynth.setSendMIDI(false);
        otherSynth.undo.setWillPush(false);

        // this last statement fixes a mystery.  When I call Randomize or Reset on
        // a Blofeld or on a Microwave, all of the widgets update simultaneously.
        // But on a Blofeld Multi or Microwave Multi they update one at a time.
        // I've tried a zillion things, even moving all the widgets from the Blofeld Multi
        // into the Blofeld, and it makes no difference!  For some reason the OS X
        // repaint manager is refusing to coallesce their repaint requests.  So I do it here.
        otherSynth.repaint();
        
        int result = PARSE_ERROR;
        try
            {
            result = otherSynth.performParse(data, true);
            }
        catch (Exception ex)
            {
            Synth.handleException(ex);
            // result is now PARSE_ERROR
            }

        if (result == PARSE_FAILED)
            {
            otherSynth.showSimpleError("File Error", "Could not read the patch.");
            otherSynth.setSendMIDI(true);
            return false;
            }
        else if (result == PARSE_ERROR)
            {
            otherSynth.showSimpleError("File Error", "An error occurred on reading the patch.");
            otherSynth.setSendMIDI(true);
            return false;
            }
        
        if (result == PARSE_SUCCEEDED)  // not PARSE_SUCEEDED_UNTITLED
            {
            otherSynth.file = f;
            otherSynth.auxFile = null;
            }
        else
            {
            otherSynth.file = null;
            otherSynth.auxFile = f;
            }
        
        otherSynth.setLastDirectory(fd.getDirectory());

        otherSynth.undo.setWillPush(true);
        otherSynth.setSendMIDI(true);

        if (otherSynth.getSendsParametersAfterLoad()) // we'll need to do this
            otherSynth.sendAllParameters();

        otherSynth.updateTitle();       // so it shows the right filename
                
        // we don't want this to look like it's succeeded
        return false;  // (result == PARSE_SUCCEEDED || result == PARSE_SUCCEEDED_UNTITLED);
        }
                

    /** Pops up at the start of the program to ask the user what synth he wants. */
    static Synth doNewSynthPanel()
        {
        while(true)
            {
            try
                {
                return Favorites.doNewSynthDialog();
                }
            catch (Exception ex)
                {
                Synth.handleException(ex);
                JOptionPane.showMessageDialog(null, "Sorry, I couldn't open that patch editor.\nTry another one.", 
                    "Can't Open Editor", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

    void doPrefs()
        {
        // do nothing
        }
    
    void doAbout()
        {
        ImageIcon icon = new ImageIcon(Synth.class.getResource("gui/About.jpg"));
        //      JLabel picture = new JLabel(icon);
        JFrame frame = new JFrame("About Edisyn");
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().setBackground(Color.BLACK);
        frame.getContentPane().add(new JLabel(icon), BorderLayout.CENTER);

        JPanel pane = new JPanel()
            {
            public Insets getInsets() { return new Insets(10, 10, 10, 10); }
            };
        pane.setBackground(Color.GRAY);
        pane.setLayout(new BorderLayout());

        JLabel edisyn = new JLabel("Edisyn");
        edisyn.setForeground(Color.BLACK);
        edisyn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        pane.add(edisyn, BorderLayout.WEST);

        JLabel about = new JLabel("Version " + Edisyn.VERSION + "      by Sean Luke      http://github.com/eclab/edisyn/");
        about.setForeground(Color.BLACK);
        about.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        pane.add(about, BorderLayout.EAST);

        frame.add(pane, BorderLayout.SOUTH);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        if (Style.isMac())
            {
            frame.setJMenuBar(makeDisabledMenuBar());
            }
        frame.setVisible(true);
        }
        
    void doToggleMutationMapEdit()
        {
        setShowingMutation(!isShowingMutation());
        }
        
    void doSetAllMutationMap(boolean val)
        {
        String title = "Clear All Mutation Parameters";
        String message = "Are you sure you want to make all parameters immutable?";
                        
        if (val)
            {
            title = "Set All Mutation Parameters";
            message = "Are you sure you want to make all parameters mutable?";
            }
                
        if (showSimpleConfirm(title, message))
            {
            String[] keys = getModel().getKeys();
            for(int i = 0; i < keys.length; i++)
                {
                mutationMap.setFree(keys[i], val);
                }
            }
                        
        repaint();
        }     
    
    
    
    
    
    
    
    
    ////// HILL-CLIMBING
    
    HillClimb hillClimb;
    boolean hillClimbing = false;

    void doHillClimb()
        {
        if (hillClimbing)
            {
            Component selected = tabs.getSelectedComponent();
            hillClimb.shutdown();
            tabs.remove(hillClimbPane);
            hillClimbMenu.setText("Hill-Climb");
            if (selected == hillClimbPane)  // we were in the hill-climb pane when this menu was selected
                tabs.setSelectedIndex(0);
            hillClimbing = false;
            }
        else
            {
            morph.shutdown();
            hillClimb.startHillClimbing();
            hillClimb.startup();
            hillClimbPane = addTab("Hill-Climb", hillClimb);
            tabs.setSelectedComponent(hillClimbPane);
            hillClimbMenu.setText("Stop Hill-Climbing");
            hillClimbing = true;
            }
        }  
        
        
        
        
        
        
        
    ////// MORPHING
    
    Morph morph;
    boolean morphing = false;
        
    void doMorph()
        {
        if (morphing)
            {
            Component selected = tabs.getSelectedComponent();
            morph.shutdown();
            tabs.remove(morphPane);
            morphMenu.setText("Morph");
            if (selected == morphPane)  // we were in the morph pane when this menu was selected
                tabs.setSelectedIndex(0);
            morphing = false;
            }
        else
            {
            hillClimb.shutdown();
            morph.initialize();
            morph.startup();
            morphPane = addTab("Morph", morph);
            tabs.setSelectedComponent(morphPane);
            morphMenu.setText("Stop Morphing");
            morphing = true;
            }
        }  
        
        
        
        
        
    //////// BATCH DOWNLOADING
    
    boolean incomingPatch = false;
    int patchCounter = 0;
    Model currentPatch = null;
    Model finalPatch = null;
    File patchFileOrDirectory = null;
    FileOutputStream batchPatches = null;
    javax.swing.Timer patchTimer = null;
        
    public int getBatchDownloadWaitTime() { return 500; }
    
    public boolean isBatchDownloading() { return patchTimer != null; }
    
    void saveBatchPatches()
        {
        if (batchPatches != null)
            {
            try { batchPatches.close(); }
            catch (IOException ex) { }
            }
        batchPatches = null;
        }
        
    void loadBatchPatch(byte[] sysex)
        {
        if (batchPatches != null)
            {
            try
                {
                batchPatches.write(sysex);
                batchPatches.flush();  // in case the user quits Edisyn
                }
            catch (IOException e) // fail
                {
                showErrorWithStackTrace(e, "File Error", "An error occurred while saving to the bulk file " + (patchFileOrDirectory == null ? " " : patchFileOrDirectory.getName()));
                Synth.handleException(e);
                if (batchPatches != null)
                    try { batchPatches.close(); }
                    catch (IOException ex) { }
                batchPatches = null;
                }
            }
        }

    /** This method normally queries the user for start and end patch numbers to
        use for batch downloading.  In rare cases you may need to customize this,
        such as to hard-code the start and end patch.  Otherwise, don't override it.
    */
    public boolean setupBatchStartingAndEndingPatches(Model startPatch, Model endPatch)
        {
        if (!gatherPatchInfo("Starting Patch", currentPatch, false))
            return false;
                        
        if (!gatherPatchInfo("Ending Patch", endPatch, false))
            return false;
                
        return true;
        }
    
    void doGetAllPatches()
        {
        if (patchTimer != null)
            {
            patchTimer.stop();
            patchTimer = null;
            saveBatchPatches();
            patchFileOrDirectory = null;
            getAll.setText("Download Batch...");
            showSimpleMessage("Batch Download", "Batch download stopped." );
            }
        else
            {
            // turn off hill-climbing
            if (hillClimbing)
                doHillClimb();
            // turn off morphing
            if (morphing)
                doMorph();
                
                
            int result = showMultiOption(this, new String[0], new JComponent[0], new String[] { "Save as Individual Files", "Save to Bulk File", "Cancel"}, 0,
                "Batch Download", "Save the Patches as individual files or as a single bulk file?");
            System.err.println(result);
            if (result == -1 || result == 2) return;
            if (result == 1)
                {
                FileDialog fd = new FileDialog((Frame)(SwingUtilities.getRoot(this)), "Save to Bulk Sysex File...", FileDialog.SAVE);

                fd.setFile(StringUtility.reviseFileName(getSynthNameLocal() + ".bulk.syx"));
                String path = getLastDirectory();
                if (path != null)
                    fd.setDirectory(path);
                                                                        
                disableMenuBar();
                fd.setVisible(true);
                enableMenuBar();
        
                File f = null; // make compiler happy
                FileOutputStream os = null;
                if (fd.getFile() != null)
                    {
                    setLastDirectory(fd.getDirectory());
                    patchFileOrDirectory = new File(fd.getDirectory(), StringUtility.ensureFileEndsWith(fd.getFile(), ".syx"));
                    batchPatches = null;
                    try 
                        {
                        batchPatches = new FileOutputStream(patchFileOrDirectory);
                        } 
                    catch (IOException e) // fail
                        {
                        showErrorWithStackTrace(e, "File Error", "An error occurred while saving to the bulk file " + (patchFileOrDirectory == null ? " " : patchFileOrDirectory.getName()));
                        Synth.handleException(e);
                        if (batchPatches != null)
                            try { batchPatches.close(); }
                            catch (IOException ex) { }
                        batchPatches = null;
                        }
                    }
                else return;
                }
            else
                {       
                File dir = selectDirectory("Select Directory for Patches",
                    file == null ? null : new File(file.getParentFile().getPath()), 
                    true);
                if (dir != null) setLastDirectory(dir.getParent());
                
                if (dir == null)
                    {
                    currentPatch = null;
                    return;
                    }
                else
                    {
                    patchFileOrDirectory = dir;
                    batchPatches = null;
                    }
                }

            /*
              JFileChooser chooser = new JFileChooser();
              chooser.setDialogTitle("Select Directory for Patches");
              chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
              chooser.setAcceptAllFileFilterUsed(false);

              if (file != null)
              {
              chooser.setCurrentDirectory(new File(file.getParentFile().getPath()));
              }
              else
              {
              String path = getLastDirectory();
              if (path != null)
              chooser.setCurrentDirectory(new File(path));
              }
              disableMenuBar();         
              if (chooser.showOpenDialog((Frame)(SwingUtilities.getRoot(this))) != JFileChooser.APPROVE_OPTION)
              {
              enableMenuBar();
              currentPatch = null;
              return;
              }
              enableMenuBar();
              patchFileOrDirectory = chooser.getSelectedFile();
              batchPatches = null;
              }
            */
            
            currentPatch = buildModel();
            finalPatch = buildModel();
            if (!setupBatchStartingAndEndingPatches(currentPatch, finalPatch))
                {
                currentPatch = null;
                finalPatch = null;
                return;
                }
                
            // request patch

            getAll.setText("Stop Downloading Batch");
            resetBlend();
            setMergeProbability(0.0);
            performRequestDump(currentPatch, true);
            incomingPatch = false;
            
            batchDownloadFailureCountdown = getBatchDownloadFailureCountdown();
            // set timer to request further patches         
            patchTimer = new javax.swing.Timer(getBatchDownloadWaitTime(),
                new ActionListener()
                    {
                    public void actionPerformed(ActionEvent e)
                        {
                        if (incomingPatch)
                            {
                            if (patchLocationEquals(getModel(), currentPatch))
                                {
                                batchDownloadFailureCountdown = getBatchDownloadFailureCountdown();
                                processCurrentPatch();
                                requestNextPatch();
                                }
                            else
                                {
                                System.err.println("Warning (Synth): Download of " + getPatchLocationName(currentPatch) + " failed.  Received unexpected patch " + getPatchLocationName(getModel()));
                                }
                            }
                        else 
                            {
                            if ((batchDownloadFailureCountdown--) == 0)
                                {
                                batchDownloadFailureCountdown = getBatchDownloadFailureCountdown();
                                System.err.println("Warning (Synth): Download of " + getPatchLocationName(currentPatch) + " failed.  Requesting again.");
                                resetBlend();
                                setMergeProbability(0.0);
                                performRequestDump(currentPatch, true);
                                }
                            else
                                {
                                System.err.println("Warning (Synth): Download of " + getPatchLocationName(currentPatch) + " failed.  Waiting: " + (batchDownloadFailureCountdown + 1));
                                }
                            }
                        }
                    });
            patchTimer.start();
            }
        }

    public int getBatchDownloadFailureCountdown() { return 0; }
    int batchDownloadFailureCountdown;

    void requestNextPatch()
        {
        if (patchLocationEquals(currentPatch, finalPatch))     // we're done
            {
            patchTimer.stop();
            patchTimer = null;
            saveBatchPatches();
            patchFileOrDirectory = null;
            getAll.setText("Download Batch...");
            showSimpleMessage("Batch Download", "Batch download finished." );
            }
        else
            {
            currentPatch = getNextPatchLocation(currentPatch);
            resetBlend();
            setMergeProbability(0.0);
            performRequestDump(currentPatch, true);
            incomingPatch = false;
            }
        }
        
    /** This tells Edisyn whether your synthesizer sends patches to Edisyn via a sysex patch dump
        (as opposed to individual CC or NRPN messages as is done in synths such as the PreenFM2).
        The default is TRUE, which is nearly always the case. */
    public boolean getReceivesPatchesAsDumps() { return true; }
                
    void processCurrentPatch()
        {                       
        // process current patch
        byte[] data = flatten(emitAll((Model)null, false, true));
        if (data != null && data.length > 0)
            {
            if (patchFileOrDirectory == null) { Synth.handleException(new RuntimeException("Nonexistent directory or file for handling dump patch loads")); return; } // this shouldn't happen
            
            if (batchPatches != null)
                {
                loadBatchPatch(data);
                }
            else        // saving to directory
                {
                String filename = getPatchLocationName(getModel());
                if (filename == null) filename = "";
                if (filename.length() > 0) filename = filename + ".";
                String patchname = getPatchName(getModel());
                if (patchname != null && patchname.length() > 0)
                    filename = filename + getPatchName(getModel());
                filename = filename.trim();
                if (filename.length() == 0)
                    filename = "Patch" + patchCounter + ".syx";
                else
                    filename = filename + ".syx";
                        
                filename = StringUtility.makeValidFilename(filename);
                // substitute separators. Might as well
                // filename = filename.replace('/', '-').replace('\\', '-');
                        
                FileOutputStream os = null;
                File f = null;
                try
                    {
                    os = new FileOutputStream(f = new File(patchFileOrDirectory, filename));
                    os.write(data);
                    }
                catch (IOException e) // fail
                    {
                    patchTimer.stop();
                    patchTimer = null;
                    saveBatchPatches();
                    patchFileOrDirectory = null;
                    getAll.setText("Download Batch...");
                    showErrorWithStackTrace(e, "Batch Download Failed.", "An error occurred while saving to the file " + (f == null ? " " : f.getName()));
                    Synth.handleException(e);
                    }
                finally
                    {
                    if (os != null)
                        try { os.close(); }
                        catch (IOException e) { }
                    }
                }
            }
        }
        
        
    /** By default this method says two patches are the same if they have the same
        "bank" and "number": if both are missing the "bank" (or the "number") then the
        "bank" (or "number") is assumed to be the same.  You should not use 
        Integer.MIN_VALUE as either a bank or a number.
        Override this if you need further customization. */
    public boolean patchLocationEquals(Model patch1, Model patch2)
        {
        int bank1 = patch1.get("bank", Integer.MIN_VALUE);
        int number1 = patch1.get("number", Integer.MIN_VALUE);
        int bank2 = patch2.get("bank", Integer.MIN_VALUE);
        int number2 = patch2.get("number", Integer.MIN_VALUE);
        return (bank1 == bank2 && number1 == number2);
        }
       
        
        
        
        


    ///////// RANDOM PATCH MERGING


    /** Returns a "first" patch location (commonly bank 0 patch 0), or null 
        if there is no such location. If your synth has an unusual (non-zero) 
        starting number or bank, you may need to override this.  Otherwise you 
        can probably avoid implementing this method, assuming that 
        getNextPatchLocation(...) returns either null or a model with valid 
        numbers and/or banks.  */
    public Model getFirstPatchLocation()
        {
        // test to see if this is legitimate
        Model nextModel = getNextPatchLocation(model);
        if (nextModel == null)                                          // uh oh
            {
            return null;
            }
        // test to see if numbers are implemented
        if (nextModel.get("number", -1000) == -1000)            // uh oh
            {
            return null;
            }
        // test to see if banks are implemented
        if (nextModel.get("bank", -1000) == -1000)              // no banks, revise
            {
            Model newModel = buildModel();
            newModel.set("number", 0);
            return newModel;
            }
        else
            {
            Model newModel = buildModel();
            newModel.set("number", 0);
            newModel.set("bank", 0);
            return newModel;
            }
        }
    
    PatchLocation[] allPatchLocations = null;           // a little cacheing
    /** Returns all the readable patch locations available in the Synth. */
    public PatchLocation[] gatherAllPatchLocations()
        {
        if (allPatchLocations != null) return allPatchLocations;
        
        Model startPatch = getFirstPatchLocation();
        if (startPatch != null)
            {
            Model endPatch = getNextPatchLocation(startPatch);
            if (endPatch != null)
                {
                return gatherPatchLocations(endPatch, startPatch);              // notice they're backward!
                }
            else            // uh....
                {
                Synth.handleException(new RuntimeException("Synth.gatherAllPatchLocations had no valid next location."));
                return new PatchLocation[0];
                }
            }       
        else return new PatchLocation[0];
        }
        
    /** Creates an array of patch locations starting at startPatch and ending at endPatch. */
    public PatchLocation[] gatherPatchLocations(Model startPatch, Model endPatch)
        {
        PatchLocation start = new PatchLocation(startPatch);
        PatchLocation end = new PatchLocation(endPatch);
        if (start.number == PatchLocation.NO_NUMBER) 
            {
            Synth.handleException(new RuntimeException("Synth.gatherPatchLocations received a start patch with no valid number."));
            return new PatchLocation[0];
            }
        if (end.number == PatchLocation.NO_NUMBER) 
            {
            Synth.handleException(new RuntimeException("Synth.gatherPatchLocations received a start patch with no valid number."));
            return new PatchLocation[0];
            }
                                
        ArrayList patches = new ArrayList();
        while(true)
            {
            patches.add(start);
            if (start.equals(end))  // all done
                break;
                        
            startPatch = getNextPatchLocation(startPatch);
            start = new PatchLocation(startPatch);
            }
                
        return (PatchLocation[])(patches.toArray(new PatchLocation[0]));
        }
        

    Model secondModel = null;
    PatchLocation[] blendLocations = null;
        
    public void doBlendFull()
        {
        if (tuple == null || tuple.out == null)
            {
            if (!setupMIDI())
                return;
            }
                
        PatchLocation[] locs = gatherAllPatchLocations();
        if (locs.length == 0 || locs.length == 1) // uh oh
            {
            showSimpleError("Blend Error", "Cannot blend patches for this synthesizer.");
            }
        else
            {
            blendLocations = locs;
            performBlend();
            }
        }

    public void doBlendInRange()
        {
        if (tuple == null || tuple.out == null)
            {
            if (!setupMIDI())
                return;
            }
                
        PatchLocation[] locs = gatherAllPatchLocations();
        if (locs.length == 0 || locs.length == 1) // uh oh
            {
            showSimpleError("Blend Error", "Cannot blend patches for this synthesizer.");
            }
        else
            {
            Model startPatch = buildModel();
            if (!gatherPatchInfo("Blend Starting Patch", startPatch, false))
                { startPatch = null; return; }
                
            Model endPatch = buildModel();
            if (!gatherPatchInfo("Blend Ending Patch", endPatch, false))
                { endPatch = null; return; }
                        
            blendLocations = gatherPatchLocations(startPatch, endPatch);
            performBlend();
            }
        }
        
    void performBlend()
        {               
        PatchLocation first = blendLocations[random.nextInt(blendLocations.length)];
        PatchLocation second = blendLocations[random.nextInt(blendLocations.length)];
        
        while(second.equals(first))
            {
            second = blendLocations[random.nextInt(blendLocations.length)];
            }
        
        Model firstModel = first.assignLocations(buildModel());
        secondModel = second.assignLocations(buildModel());
        
        setMergeProbability(0.0);
        performRequestDump(firstModel, false);  // I don't *think* we need to change the patch
        }
                
    public void doUndoAndBlendAgain()
        {
        if (undo.undo.size() >= 2)      // gotta have at least two, because we're undoing twice
            {
            if (tuple == null || tuple.out == null)
                {
                if (!setupMIDI())
                    return;
                }
                
            if (blendLocations != null)
                {
                doUndo(false);          // no reason to send it
                doUndo(false);          // no reason to send it         // yes, twice
                performBlend();
                }
            else
                {
                showSimpleError("Undo", "Can't Undo and Blend Again: no previous blending!");
                }
            }
        else
            {
            showSimpleError("Undo", "Can't Undo and Blend Again: no previous blending!");
            }
        }
                
    void updateBlend()
        {
        if (secondModel != null)
            {
            setMergeProbability(random.nextDouble() * 0.5 + 0.5);   // from 0.5 to 1.0
            simplePause(getPauseAfterReceivePatch());
            performRequestDump(secondModel, false);
            }
        secondModel = null;
        }

    void resetBlend()
        {
        secondModel = null;
        }

                




        
    ///////// CALLBACKS


    /** Called by the model to update the synth whenever a parameter is changed. 
        You would probably never call this method. */
    public void update(String key, Model model)
        {
        if (learning)
            updateTitle();
            
        sendOneParameter(key);
        }

        
    /** Sends a single parameter if the synthesizer is capable of doing this. */
    public void sendOneParameter(String key)
        {
        if (allowsTransmitsParameters && getSendMIDI())
            {
            if (tryToSendMIDI(emitAll(key, STATUS_UPDATING_ONE_PARAMETER)))
                simplePause(getPauseAfterSendOneParameter());
            }
        }

        





    //// BANK SYSEX SUPPORT
        
        
    // This returns one of:
    // 1. 0 ... names.length - 1    [choice of name]
    // 2. BANK_CANCELLED    [cancelled]
    // 3. BANK_SAVED        [saved]
    // 4. BANK_UPLOADED     [uploaded to synth]
        
    // If the value is #1, then you have to edit or merge the patch, and return whatever is appropriate.
    // If the value is BANK_CANCELLED, BANK_SAVED, or BANK_UPLOADED (all < 0), then you should return PARSE_FAILED
        
    public static final int BANK_CANCELLED = -1;
    public static final int BANK_SAVED = -2;
    public static final int BANK_UPLOADED = - 3;
    
    /** Displays a panel asking what to do with a bank sysex with the given data[], and which
        contains patches with the given names[].  If the user cancels the panel, then
        this method simply returns BANK_CANCELLED.  If the user presses "Save Bank", then the
        method saves the bank to a file specified by the user, then returns BANK_SAVED.  If the
        user presses "Write Bank", then the method writes the bank to the synthesizer and returns
        BANK_UPLOADED.  If the user selects a patch and then presses "Edit Patch" (the default),
        then this method returns the patch number (0....) in the bank.
    */
    
    boolean showingLimitedBankSysex = false;
    boolean isShowingLimitedBankSysex() { return showingLimitedBankSysex; }
    void setShowingLimitedBankSysex(boolean val) { showingLimitedBankSysex = val; }
    

    public int showBankSysexOptions(byte[] data, String[] names)
        {
        while(true)
            {
            Color color = new JPanel().getBackground();
            HBox hbox = new HBox();
            hbox.setBackground(color);
            VBox vbox = new VBox();
            vbox.setBackground(color);
            vbox.add(new JLabel("   "));
            if (isParsingForMerge() || isShowingLimitedBankSysex())
                {
                vbox.add(new JLabel("A Bank Sysex has been received."));
                }
            else
                {
                vbox.add(new JLabel("A Bank Sysex has been received. Choose an action to perfom."));
                }
            vbox.add(new JLabel("   "));
            hbox.addLast(vbox);
            vbox = new VBox();
            vbox.setBackground(color);
            vbox.add(hbox);
            JLabel boxLabel = new JLabel("Patch");
            JComboBox box = new JComboBox(makeUniqueStrings(names));
            box.setMaximumRowCount(25);
            JPanel boxPanel = new JPanel();
            boxPanel.setLayout(new BorderLayout());
            boxPanel.add(boxLabel, BorderLayout.WEST);
            boxPanel.add(box, BorderLayout.CENTER);
            vbox.add(boxPanel);
            JComponent opt = getAdditionalBankSysexOptionsComponents(data, names);
            if (opt != null) vbox.add(opt);
            
            JComboBox actions = null;
                  
            int result = 0;
            if (isShowingLimitedBankSysex())
                {
                disableMenuBar();
                result = JOptionPane.showOptionDialog(this, vbox, "Bank Sysex Received", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[] {  "Load Patch", "Cancel" }, "Load Patch");
                enableMenuBar();
                }
            else if (isParsingForMerge())
                {
                disableMenuBar();
                result = JOptionPane.showOptionDialog(this, vbox, "Bank Sysex Received", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[] {  "Merge Patch", "Cancel" }, "Merge Patch");
                enableMenuBar();
                }
            else 
                {
                actions = new JComboBox(new String[] {  "Edit Patch in Bank", "Save Bank...", "Write Bank to Synthesizer" });
                vbox.add(new JLabel("   "));
                vbox.add(new JSeparator());
//                vbox.add(Strut.makeVerticalStrut(16));
                JPanel actionsPanel = new JPanel();
                actionsPanel.setLayout(new BorderLayout());
                actionsPanel.add(new JLabel("Action "), BorderLayout.WEST);
                actionsPanel.add(actions, BorderLayout.CENTER);
                vbox.add(actionsPanel);
                
                disableMenuBar();
                result = JOptionPane.showOptionDialog(this, vbox, "Bank Sysex Received", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[] { "Do Action", "Cancel" }, "Do Action");
                enableMenuBar();
                }
                
            if (result != 0)  // cancel.  ESC and Close Box both return < 0
                {
                return BANK_CANCELLED;
                }
            else if (actions != null)
                {
                if (actions.getSelectedIndex() == 2)    // write bank
                    {
                    boolean choice = false;
                    String[] banks = getBanksForBankSysex(data, getModel());
                    int bankChoice = 0;
                                
                    if (banks == null || banks.length == 0)
                        {
                        choice = showSimpleConfirm("Write Bank", "Write the whole bank to the synth?");
                        }
                    else
                        {
                        JComboBox combo = new JComboBox(banks);
                        combo.setSelectedIndex(getDefaultBankForBankSysex(data, getModel()));
                        choice = (showMultiOption(this, new String[] { "Write to Bank" }, new JComponent[] { combo },
                                new String[] { "Write", "Cancel" }, 0, "Write Bank", "Write the whole bank to the synth?") == 0);
                        bankChoice = combo.getSelectedIndex();
                        }
                                                
                    if (choice)
                        {
                        if (tuple == null || tuple.out == null)
                            {
                            if (!setupMIDI())
                                continue;
                            }

                        if (!fixCorruptSysex(data))
                            return BANK_CANCELLED;

                        Object obj = adjustBankSysexForEmit(data, getModel(), bankChoice);
                        boolean send = getSendMIDI();
                        setSendMIDI(true);
                        if (obj instanceof byte[]) 
                            {
                            tryToSendSysex((byte[])obj);
                            }
                        else
                            {
                            tryToSendMIDI((Object[])obj);
                            }
                        setSendMIDI(send);      
                        return BANK_UPLOADED;
                        }
                    }
                else if (actions.getSelectedIndex() == 1)
                    {
                    FileDialog fd = new FileDialog((Frame)(SwingUtilities.getRoot(this)), "Save Bank to Sysex File...", FileDialog.SAVE);

                    fd.setFile(StringUtility.reviseFileName(getSynthNameLocal() + ".bank" + getBankNameLocal(data) + ".syx"));
                    String path = getLastDirectory();
                    if (path != null)
                        fd.setDirectory(path);
                                        
                    disableMenuBar();
                    fd.setVisible(true);
                    enableMenuBar();
        
                    File f = null; // make compiler happy
                    FileOutputStream os = null;
                    if (fd.getFile() != null)
                        try
                            {
                            f = new File(fd.getDirectory(), StringUtility.ensureFileEndsWith(fd.getFile(), ".syx"));
                            os = new FileOutputStream(f);
                            os.write(data);
                            os.close();
                            setLastDirectory(fd.getDirectory());
                            if (os != null)
                                try { os.close(); }
                                catch (IOException ex) { }
                            return BANK_SAVED;
                            } 
                        catch (IOException e) // fail
                            {
                            showErrorWithStackTrace(e, "File Error", "An error occurred while saving to the file " + (f == null ? " " : f.getName()));
                            Synth.handleException(e);
                            if (os != null)
                                try { os.close(); }
                                catch (IOException ex) { }
                            continue;  // try again
                            }
                    }
                else // if actions.getSelectedIndex() == 0
                    {
                    return box.getSelectedIndex();
                    }
                }
            else            // Load or Merge Patch
                {
                return box.getSelectedIndex();
                }
            }
        }
               
    /** Override this method to provide banks (if any) for the user to choose from when modifying a bank sysex. 
        The default returns null.  */
    public String[] getBanksForBankSysex(byte[] data, Model model) { return null; }

    /** Override this method to the default bank from getBanksForBankSysex() to display to the user, if any. 
        The default is 0, which is likely meaningless. */
    public int getDefaultBankForBankSysex(byte[] data, Model model) { return 0; }

    /** Override this method to modify the given bank sysex data so it can be emitted properly to the synthesizer
        specified by the provided model: for example, you might modify the outgoing channel or synthesizer id. 
        Many synthesizers (Kawai, Korg, Yamaha notably) just need set <tt>data[2] = (byte)getChannelOut();</tt>
        You can then either return the revised sysex as a byte[], or you can return an Object[]
        containing a variety of messages essentially like what is returned by emitAll().  The model provided
        is the current model of the synthesizer; and the bank provided is the bank chosen by the user -- if any -- 
        when asked what bank to upload this sysex to.  Many synths don't have specific banks, that is, the
        "bank" is the whole RAM, so this value will have no meaning.
    */
    public Object adjustBankSysexForEmit(byte[] data, Model model, int bank) { return data; }
    
    /** Override this method to modify the given bulk sysex data, in the form
        data[patch][sysexmessage][bytes].  All the data will be for your kind of
        synthesizer.  If you return false, then the write is canceled.  The provided
        synthesizer is *not* the synthesizer for the data (that's you).  Instead, it
        allows you to properly pop up a confirm dialog centered at the given window.
        That's all it should be used for.
    */
    public boolean adjustBulkSysexForWrite(Synth window, byte[][][] data)
        {
        return true;
        }

    /** Override this method to add an additional JComponent to the Bank Sysex Options dialog; you can then
        query this JComponent later to revise the bank dump prior to writing it to disk or to the synth via
        adjustBankSysexForEmit(...).  This is a very rare need. By default this method returns null.  */
    public JComponent getAdditionalBankSysexOptionsComponents(byte[] data, String[] names) { return null; }
    
    
    
    
    public static final int DEFAULT_PASTES = 3;
    /** Override this method to force Edisyn to paste multiple times to the same category or tab.
        The reason you might want to do this is because Edisyn uses the *receiving* category to 
        determine the parameters to paste to, and if this category contains componets which dynamically
        appear or disappear, it might require multiple pastes to cause them to appear and eventually
        receive parameter changes.  The default returns DEFAULT_PASTES (3).  */
    public int getNumberOfPastes() { return DEFAULT_PASTES; }
    
    

    
    
    
    
    
    
    
    
    
    ////////// UTILITIES

            
    /** Call this to log exceptions.  By default this method just prints the exception to the
        command line, but it can be modified to open a file and log the exception that way
        so a user can mail the developer a debug file. */
    public static void handleException(Throwable ex) { ex.printStackTrace(); }    
    
    
    public static void printSysex(byte[] data)
        {
        System.err.print(Midi.getManufacturerForSysex(data) + " " + data.length + ":  ");
        for(int i = 0; i < data.length; i++)
            System.err.print(" " + StringUtility.toHex(data[i]));
        System.err.println();
        }   
        
        
    public JMenuBar makeDisabledMenuBar()
        {
        JMenuBar mb = new JMenuBar();
        for(int i = 0; i < menubar.getMenuCount(); i++)
            {
            JMenu old = menubar.getMenu(i);
            JMenu menu = new JMenu(old.getText());
            for(int j = 0; j < old.getMenuComponentCount(); j++)
                {
                Component comp = old.getMenuComponent(j);
                if (comp == null)
                    {
                    handleException(new RuntimeException("Unknown JMenu Component " + comp));
                    }
                else if (comp instanceof JMenu)
                    {
                    JMenu item = new JMenu(((JMenu)comp).getText());
                    KeyStroke ks = ((JMenu)comp).getAccelerator();
                    if (ks != null) item.setAccelerator(ks);
                    item.setEnabled(false);
                    menu.add(item);
                    }
                else if (comp instanceof JMenuItem)
                    {
                    JMenuItem item = new JMenuItem(((JMenuItem)comp).getText());
                    KeyStroke ks = ((JMenuItem)comp).getAccelerator();
                    if (ks != null) item.setAccelerator(ks);
                    item.setEnabled(false);
                    menu.add(item);
                    }
                else if (comp instanceof JSeparator)
                    {
                    menu.add(new JSeparator());
                    }
                else
                    {
                    handleException(new RuntimeException("Unknown JMenu IteComponentm " + comp));
                    }               
                }
            mb.add(menu);
            }
        return mb;
        }
    }
