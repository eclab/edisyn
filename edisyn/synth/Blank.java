/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth;

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

public class Blank extends Synth
    {

	////// BELOW ARE DEFAULT IMPLEMENTATION OF COMMON METHODS THAT SYNTH EDITORS IMPLEMENT OR OVERRIDE.
	////// If you do not need to implement or override a method, you should delete that method entirely
	////// unless it is abstract, in which case, keep the default method described here.
	
	

	public Blank()
		{
		// Here you set up your interface.   You can look at other patch editors
		// to see how they were done.  At the very end you typically would say something like:
		
		
		model.set("name", "InitName");  // or whatever, to set the initial name of your patch (assuming you use "name" as the key for the patch name)
		loadDefaults(); 				// this tells Edisyn to load the ".init" sysex file you created.  If you haven't set that up, it won't bother
		}
	
	
	
	
	/////// SOME NOTES ABOUT RELATIONSHIPS BETWEEN CERTAIN METHODS
	

	/// There are a lot of redundant methods here.  You only have to override some of them.

	/// PARSING (LOADING OR RECEIVING)
	/// When a message is received from the synthesizser, Edisyn will do this:
	/// If the message is a Sysex Message, then
	/// 	Call recognize(message data).  If it returns true, then
	///			Call parse(message data, fromFile) [we presume it's a dump or a load from a file]
	///		Else
	///			Call parseParameter(message data) [we presume it's a parameter change, or maybe something else]
	/// Else if the message is a complete CC or NRPN message
	///		Call handleSynthCCOrNRPN(message) [it's some CC or NRPN that your synth is sending us, maybe a parameter change?]
	
	/// SENDING A SINGLE PARAMETER OF KEY key
	/// Call emitAll(key)
	/// 	This calls emit(key)
	///
	/// You could override either of these methods, but probably not both.
	
	/// SENDING TO CURRENT
	/// Call sendAllParameters().  This does:
	///		If getSendsAllParametersInBulk(), this calls:
	///			emitAll(tempModel, toWorkingMemory = true, toFile)
	///				This calls emit(tempModel, toWorkingMemory = true, toFile)
	///		Else for every key it calls:
	/// 		Call emitAll(key)
	/// 			This calls emit(key)
	///
	/// You could override either of the emit...(tempModel...) methods, but probably not both.
	/// You could override either of the emit...(key...) methods, but probably not both.

	/// SENDING TO A PATCH
	/// Call gatherPatchInfo(...,tempModel,...)
	/// If successful
	///		Call changePatch(tempModel)
	/// 	Call sendAllParameters().  This does:
	///			If getSendsAllParametersInBulk(), this calls:
	///				emitAll(tempModel, toWorkingMemory = true, toFile)
	///					This calls emit(tempModel, toWorkingMemory = true, toFile)
	///			Else for every key it calls:
	/// 			Call emitAll(key)
	/// 				This calls emit(key)
	///	
	/// You could override either of the emit...(tempModel...) methods, but probably not both.
	/// You could override either of the emit...(key...) methods, but probably not both.
	
	/// WRITING OR SAVING
	/// Call gatherPatchInfo(...,tempModel,...)
	/// If successful
	/// 	Call emitAll(tempModel, toWorkingMemory = false, toFile)
	///			This calls emit(tempModel, toWorkingMemory = false, toFile)
	///		Call changePatch(tempModel)
	///
	/// You could override either of the emit methods, but probably not both.
	/// Note that saving strips out the non-sysex bytes from emitAll.
	
	/// SAVING
	/// Call emitAll(tempModel, toWorkingMemory, toFile)
	///		This calls emit(tempModel, toWorkingMemory, toFile)
	///
	/// You could override either of the emit methods, but probably not both.
	/// Note that saving strips out the non-sysex bytes from emitAll.
	
	/// REQUESTING A PATCH 
	/// If we're requesting the CURRENT patch
	///		Call performRequestCurrentDump()
	///			this then calls requestCurrentDump()
	/// Else
	/// 	Call gatherPatchInfo(...,tempModel,...)
	///		If successful
	///			Call performRequestDump(tempModel)
	///				This calls changePatch(tempModel)
	///				Then it calls requestDump(tempModel)
	///
	/// You could override performRequestCurrentDump or requestCurrentDump, but probably not both.
	/// Similarly, you could override performRequestDump or requestDump, but probably not both








	////// YOU MUST OVERRIDE ALL OF THE FOLLOWING

    public void changePatch(Model tempModel)
    	{
    	// Here you do stuff that changes patches on the synth.
    	// You probably want to look at tryToSendSysex() and tryToSendMIDI()
    	}

    public boolean gatherPatchInfo(String title, Model changeThis, boolean writing)	
    	{
    	// Here you want to pop up a window which gathers information about a patch,
    	// such as the patch number and the bank, sufficient to load a specific patch
    	// or save a specific patch to/from the synthesizer.  If WRITING is true, you
    	// may assume that you are writing out to the synthesizer (because sometimes
    	// you can read from some banks but can't write to them).  The title of your
    	// dialog window should be TITLE.
    	//
    	// Return TRUE if you successfully gathered information, FALSE if the user cancelled.
    	//
    	// This can be complex to write.  But take a look at implementations in, for example,
    	// Blofeld.java and freely copy that.
    	//
    	return false;
    	}

    public boolean parse(byte[] data, boolean ignorePatch, boolean fromFile)
    	{ 
    	// This bulk patch data will come from a file or transmitted over sysex.
    	// You should parse it into the model and return TRUE if successful, else FALSE.
    	// IGNOREPATCH tells you whether you should ignore any patch access
    	// information (number, bank, etc.) embedded in the data or store it in the
    	// model as well.   FROMFILE indicates that the parse is from a sysex file.
    	return false; 
    	}
    	
    public static boolean recognize(byte[] data)
        {
        // This method should return TRUE, if the data is correct sysex data for a 
        // a bulk dump to your kind of synthesizer, and so you can receive it via
        // parse().
        //
        // Notice that this is a STATIC method -- but you need to implement it
        // anyway.  Edisyn will call the right static version using reflection magic.
        return false;
        }

    public static String getSynthName() 
    	{ 
        // This method should return the name of your synthsizer.
        // Typically these names are of the form BRANDNAME MODELNAME 
        // or BRANDNAME MODELNAME [Multi]		(to indicate a multimode patch)
        //
        // For example:
        //		"Waldorf Microwave II/XT/XTk"
        // or
        //		"Waldorf Microwave II/XT/XTk [Multi]"
        //
        // Notice that this is a STATIC method -- but you need to implement it
        // anyway.  Edisyn will call the right static version using reflection magic.
    	return "Override Me"; 
    	}
    
    public String getDefaultResourceFileName() 
    	{
    	// Ultimately your synth will be initialized by loading a file via parse().  This is usually a
    	// sysex file ending in the extension ".init", such as "WaldorfBlofeld.init",
    	// and is located right next to the class file (that is, "WaldorfBlofeld.class").
    	// 
    	// If you return null here, this initialization step will be bypassed.  But final
    	// production code should not do that.
    	return null; 
    	}
	
    public String getHTMLResourceFileName() 
    	{ 
    	// Ultimately your synth will have an additional tab called "About", which displays an HTML
    	// file.  This is a file ending in the extension ".html", such as "WaldorfBlofeld.html",
    	// and is located right next to the class file (that is, "WaldorfBlofeld.class").
    	// 
    	// If you return null here, this tab will not be created.  But final
    	// production code should not do that.
    	return null; 
    	}

    
    
    
    
    
    
    

	////// YOU PROBABLY WANT TO OVERRIDE ALL OF THE FOLLOWING

    public String getPatchName() 
    	{
    	// Here you'd look up the patch name in the model and return it. 
    	return null; 
    	}
    
    public String revisePatchName(String name)
    	{
    	// Here you tweak the name to make sure it's a valid patch name.
    	// You probably first want to call    name = super.revisePatchName(name)
    	// As this method will remove all trailing whitespace from the name.
    	// At that point you can modify the name as you need to (converting
    	// to all uppercase, say, or removing invalid characters, or truncating
    	// to the proper length, etc.)

        return super.revisePatchName(name);
    	}
    	
    public String reviseID(String id)
    	{
    	// Some synthesizers have an "id" which uniquely identifies them in their
    	// sysex so other synths of the same model ignore that sysex message.  Waldorf's
    	// synths are notable in this respect.  If your synth does this, revise the id
    	// to make sure it's valid, and return a valid String.  If your synth does NOT
    	// do this, then you should always return null.
    	return null;
    	}
    
    public void revise()
        {
        // In this method you need to verify that all the keys in your model have valid values.
        // Some synthesizers send invalid values over sysex or NRPN.  For example, the PreenFM2
        // can send crazy stuff way out of range.
        //
        // The default version of this method bounds all the values to between their stated min and
        // max values.  You might need to do more than this; for example, you might verify that
        // the name is valid.  In this case, call super.revise() and then do further revision
        // as you see fit.  For one way to do this, see the Waldorf Blofeld code.
        
        super.revise();
        }









	////// YOU PROBABLY WANT TO OVERRIDE *ONE* OF THE FOLLOWING

    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        // This does a write of your patch to sysex (to dump to the synth or to store
        // in a file).  TOWORKINGMEMORY indicates whether the dump will go to the synth's
        // working memory, or written to a specific patch store.  TEMPMODEL will hold
        // data regarding the patch store location.  TOFILE indicates that the write will
        // be to a sysex file.
        //
        // IMPORTANT NOTE: if writing to a file, any NON-sysex messages will be
        // stripped out by Edisyn, and the remainder will be concatenated together
        // into one stream.
        //
        // If you need to send more than just a simple sysex message, override this one.
        return super.emitAll(tempModel, toWorkingMemory, toFile);
        }

    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile) 
    	{ 
        // This does a write of your patch to sysex (to dump to the synth or to store
        // in a file).  TOWORKINGMEMORY indicates whether the dump will go to the synth's
        // working memory, or written to a specific patch store.  TEMPMODEL will hold
        // data regarding the patch store location.  TOFILE indicates that the write will
        // be to a sysex file.
        //
        // If you need to send just a simple sysex message, override this one.
    	return new byte[0]; 
    	}
    
    
    
    
    
    
    
    
    
    ////// YOU PROBABLY WANT TO OVERRIDE *ONE* OF THE FOLLOWING

    public Object[] emitAll(String key)
        {
        // This writes a single parameter out to the synth.
        //
        // If you need to send more than just a simple sysex message, override this one.
        return super.emitAll(key);
        }

    public byte[] emit(String key) 
    	{ 
        // This writes a single parameter out to the synth.
        //
        // If you need to send just a simple sysex message, override this one.
    	return new byte[0]; 
    	}









    ////// YOU PROBABLY WANT TO OVERRIDE *ONE* OF THE FOLLOWING
    
    public void performRequestDump(Model tempModel, boolean changePatch)
        {
        // This asks the synth to dump a specific patch (number and bank etc. specified
        // in tempModel).  If CHANGEPATCH is true you should first change the patch.
        //
        // Normally Edisyn implements this method for you, handling the patch-change, 
        // and you can just implement requestDump(...) to send the dump request.
        // If you need to do more than a simple sysex request, reimplement this
        // method.  The default form looks like this:
        
        // if (changePatch)
		//		changePatch(tempModel); 
        // tryToSendSysex(requestDump(tempModel));
        
        super.performRequestDump(tempModel, changePatch);
        }

    public byte[] requestDump(Model tempModel) 
    	{ 
        // This asks the synth to dump a specific patch (number and bank etc. specified
        // in tempModel).  If CHANGEPATCH is true you should first change the patch.
        //
        // If you can let Edisyn call changePatch(), and then you just emit a single
        // sysex command as a patch request, implement this version.
    	return new byte[0]; 
    	}
    
    
    
    
    
    
    
    
    ////// YOU PROBABLY WANT TO OVERRIDE *ONE* OF THE FOLLOWING
        
    public void performRequestCurrentDump()
    	{
        // This asks the synth to dump the currently-playing patch 
        // (number and bank etc. specified in tempModel).
        //
        // Normally Edisyn implements this method for you, and you can just emit
        // a sysex via requestCurrentDump().  But if you need something else,
        // reimplement this method.  The default looks like this:
        
        // tryToSendSysex(requestCurrentDump());

        super.performRequestCurrentDump();
        }

    public byte[] requestCurrentDump()
    	{ 
        // This asks the synth to dump the currently-playing patch 
        // (number and bank etc. specified in tempModel).
        //
        // If you can do this with a single patch request, implement this version.
        
    	return new byte[0];
    	}











    ////// YOU MAY WANT TO IMPLEMENT SOME OF THE FOLLOWING


    public void parseParameter(byte[] data)
    	{
    	// If your synth sent you a sysex message which was not recognized via
    	// the recognize() method, it gets sent here.  Typically this is 
    	// a sysex message for a single parameter update.  If your synth sends 
    	// such things, implement this.  See also handleCCOrNRPNData() below.
    	return; 
    	}
    
    public void handleSynthCCOrNRPN(Midi.CCData data)
    	{
    	// If your synthesizer has sent you CC data or NRPN data, 
    	// it will arrive via this method.  You can use this method to update
    	// a parameter accordingly.   If your synth sends 
    	// such things, implement this.  See also parseParameter() above.
    	}
            
    public boolean requestCloseWindow() 
    	{ 
    	// When the user clicks on the close box of your synth editor,
    	// this method will be called.  If you return true, the window
    	// will be closed, else it will stay open.  You might use
    	// this method to verify with the user that everything is saved,
    	// but in fact none of the current synth editors do this, they
    	// just return true immediately.
    	return true; 
    	}
        
    public int getPauseAfterChangePatch()
    	{
    	// Some synths cannot accept MIDI messages for a while after a patch-change.
    	// For example, the Blofeld has to wait for about 200ms.
    	// Here you can specify that Edisyn must pause at least so many
    	// milliseconds before issuing another MIDI message after you have
    	// changed the patch via changePatch().
    	return 0;
    	}

    public int getPauseBetweenMIDISends() 
    	{
    	// Some synths cannot accept MIDI messages at full speed.  
    	// For example, the Yamaha TX81Z has problems with sysex messages
    	// faster than 50ms.
    	// Here you can specify that Edisyn must pause at least so many
    	// milliseconds before issuing another MIDI message of *any* kind.
    	// This includes note on / note off etc., so don't expect musicality
    	// if you set this to >0.
    	return 0;
    	}
    	
    public int getTestNote()
    	{
    	// It's possible that your synth has different sounds for different
    	// notes, so you need to customize which note is played when the user
    	// asks to send a test note.  Do so here.  The default is Middle C (60).
    	return 60;
    	}

    public int getTestNoteVelocity()
    	{
    	// You might need to customize the velocity of the test note.
    	// Do so here.  The default is full volume (127).
    	return 127;
    	}

    public int getTestNoteChannel()
    	{
    	// It's possible that your synth has a special channel for this patch
    	// (for example, a drum patch).  Override this to provide a custom
    	// channel for the test note to be sent on.  The default is getChannelOut().
    	return getChannelOut();
    	}

    public void windowBecameFront() 
    	{
    	// If your editor's window just became the front window, this method will
    	// be called to inform you.  For example, Waldorf Microwave synthesizers
    	// can change from multimode to single mode (or the other way) as appropriate
    	// when their window comes to the fore.
    	return; 
    	}

    public boolean getSendsAllParametersInBulk() 
    	{
    	// Normally this method returns TRUE meaning that when the user sends
    	// or writes to the synthesizer, emitAll(model,...) will be called to write
    	// a bulk write, typically a sysex message.  But some synthesizers don't 
    	// use sysex.  For example, the PreenFM2 receives each of its parameters
    	// via individual NRPN messages (which are handled via emitAll(key)).  
    	// If your synthesizer is of this type, you should return FALSE.
    	return true; 
    	}

	public JFrame sprout()
		{
		// This is a great big method in Synth.java, and handles building the JFrame and
		// constructing all of the menus.  It's called when the editor is having its GUI
		// constructed.   You may need to do some things here, such as turning off certain
		// menu options that your synthesizer cannot do.  Be sure to call super.sprout();
		// first.
		return super.sprout();
		}

	public boolean getExpectsRawCCFromSynth() 
		{
		// If your synthesizer sends individual parameter data to Edisyn not as sysex,
		// and not as cooked CC messages (such as NRPN), but rather as raw CC messages,
		// then you should override this method to return TRUE.  Generally it's kept FALSE,
		// the default.
		return false;
		}


    }
