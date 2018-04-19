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
    public void push(Model obj)
        {
        if (!willPush)
            {
            if (debug) System.err.println("Did not push " + obj);
            return;
            }
        undo.push((Model)(obj.clone()));
        redo.clear();
        synth.updateUndoMenus();
        if (debug) System.err.println("Pushed " + obj);
        }
        
    public Model top()
        {
        if (undo.isEmpty())
            return null;
        else return undo.peekFirst();
        }
                
    public Model undo(Model current)
        {
        if (undo.isEmpty())
            {
            if (debug) System.err.println("Empty Undo" + current);
            synth.updateUndoMenus();
            return current;
            }
        if (current != null)
            {
            if (debug) System.err.println("Pushing on Redo" + current);
            redo.push((Model)(current.clone()));
            }
        Model model = undo.pop();
        synth.updateUndoMenus();
        if (debug) System.err.println("Undo " + current + " to " + model + " Left: " + undo.size());
        return model;        
        }
                
    public Model redo(Model current)
        {
        if (redo.isEmpty())
            {
            if (debug) System.err.println("Empty Redo " + current);
            synth.updateUndoMenus();
            return current;
            }
        if (current != null)
            {
            if (debug) System.err.println("Pushing on Undo" + current);
            undo.push((Model)(current.clone()));
            }
        Model model = redo.pop();
        synth.updateUndoMenus();
        if (debug) System.err.println("Redo " + current + " to " + model + " Left: " + redo.size());
        return model;
        }
    }
