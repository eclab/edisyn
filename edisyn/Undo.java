/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn;
import java.util.*;

public class Undo
    {
    public static final boolean debug = false;
    
    public ArrayDeque<Model> undo = new ArrayDeque();
    public ArrayDeque<Model> redo = new ArrayDeque();
    public Synth synth;        
    boolean willPush = true;
    boolean willPush2 = true;
    public void clear()
        {
        undo.clear();
        redo.clear();
        }
    
    public boolean shouldShowUndoMenu() { return !undo.isEmpty(); }
    public boolean shouldShowRedoMenu() { return !redo.isEmpty(); }
        
    public Undo(Synth synth) { this.synth = synth; }
        
    public void setWillPush(boolean val) { willPush = val; }
    public boolean getWillPush() { return willPush; }
    
    // The purpose of the second willPush is so that batch downloading can positively
    // disable undo pushes even while underlying machinery tries to restore them.
    void setWillPush2(boolean val) { willPush2 = val; }
    boolean getWillPush2() { return willPush2; }
    
    public void push(Model obj)
        {
        if (!willPush || !willPush2)
            {
            if (debug) System.err.println("Debug (Undo): Tried to push but didn't (!willPush)");
            return;
            }
        undo.push((Model)(obj.clone()));
        redo.clear();
        synth.updateUndoMenus();
        if (debug) System.err.println("Debug (Undo): Pushed " + obj);

        if (debug) printStacks();
        }
        
    public Model top()
        {
        if (undo.isEmpty())
            return null;
        else return undo.peekFirst();
        }
             
    public void printStacks()
        {
        System.err.println("Debug (Undo):\nUNDO");
        Object[] o = undo.toArray();
        for(int i = 0; i < o.length; i++)
            {
            System.err.println("" + i + " " + o[i]);
            }
        System.err.println("\nREDO");
        o = redo.toArray();
        for(int i = 0; i < o.length; i++)
            {
            System.err.println("" + i + " " + o[i]);
            }
                
        }
    
    /// If current is null, 
    public Model undo(Model current)
        {
        if (undo.isEmpty())
            {
            if (debug) System.err.println("Debug (Undo): Empty Undo" + current);
            synth.updateUndoMenus();
            return current;
            }
        if (current != null)
            {
            if (debug) System.err.println("Debug (Undo): Pushing on Redo" + current);
            redo.push((Model)(current.clone()));
            }
        Model model = undo.pop();
        synth.updateUndoMenus();
        
        if (debug) System.err.println("Debug (Undo): Undo " + current + " to " + model + " Left: " + undo.size());
 
        if (debug) printStacks();

        // this last statement fixes a mystery.  When I call Randomize or Reset on
        // a Blofeld or on a Microwave, all of the widgets update simultaneously.
        // But on a Blofeld Multi or Microwave Multi they update one at a time.
        // I've tried a zillion things, even moving all the widgets from the Blofeld Multi
        // into the Blofeld, and it makes no difference!  For some reason the OS X
        // repaint manager is refusing to coallesce their repaint requests.  So I do it here.
        synth.repaint();
        return model;        
        }
                
    public Model redo(Model current)
        {
        if (redo.isEmpty())
            {
            if (debug) System.err.println("Debug (Undo): Empty Redo " + current);
            synth.updateUndoMenus();
            return current;
            }
        if (current != null)
            {
            if (debug) System.err.println("Debug (Undo): Pushing on Undo" + current);
            undo.push((Model)(current.clone()));
            }
        Model model = redo.pop();
        synth.updateUndoMenus();
        if (debug) System.err.println("Debug (Undo): Redo " + current + " to " + model + " Left: " + redo.size());
        if (debug) printStacks();

        // this last statement fixes a mystery.  When I call Randomize or Reset on
        // a Blofeld or on a Microwave, all of the widgets update simultaneously.
        // But on a Blofeld Multi or Microwave Multi they update one at a time.
        // I've tried a zillion things, even moving all the widgets from the Blofeld Multi
        // into the Blofeld, and it makes no difference!  For some reason the OS X
        // repaint manager is refusing to coallesce their repaint requests.  So I do it here.
        synth.repaint();
        return model;
        }
    }
