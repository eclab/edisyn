/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.generic;

import edisyn.*;
import edisyn.gui.*;
import edisyn.util.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.border.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.sound.midi.*;

public class Generic extends Synth
    {

    public static final String[] CC_NAMES = {
        "Bank Select",
        "Modulation",
        "Breath",
        "",
        "Foot Controller",
        "Portamento Time",
        "Data Entry MSB",
        "Volume",
        "Balance",
        "",
        "Pan",
        "Expression",
        "Effect 1",
        "Effect 2",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        " Data Entry LSB",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "Damper/Sustain",
        "Portamento",
        "Sostenuto",
        "Soft Pedal",
        "Legato",
        "Hold 2",
        "Sound 1",
        "Sound 2",
        "Sound 3",
        "Sound 4",
        "Sound 5",
        "Sound 6",
        "Sound 7",
        "Sound 8",
        "Sound 9",
        "Sound 10",
        "Portamento Amt",
        "",
        "Effect 1 Depth",
        "Effect 2 Depth",
        "Effect 3 Depth",
        "Effect 4 Depth",
        "Effect 5 Depth",
        "Data Increment",
        "Data Decrement",
        "NRPN LSB",
        "NRPN MSB",
        "RPN LSB",
        "RPN MSB",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "All Sound Off",
        "Reset Cntrlrs",
        "Local On/Off",
        "All Notes Off",
        "Omni Off",
        "Omni On",
        "Mono Mode",
        "Poly Mode"
        };


    ////// BELOW ARE DEFAULT IMPLEMENTATION OF COMMON METHODS THAT SYNTH EDITORS IMPLEMENT OR OVERRIDE.
    ////// If you do not need to implement or override a method, you should delete that method entirely
    ////// unless it is abstract, in which case, keep the default method described here.
        
        

    public Generic()
        {
        // Here you set up your interface.   You can look at other patch editors
        // to see how they were done.  At the very end you typically would say something like:
            
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        //hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        //vbox.add(hbox);
        vbox.add(addRawController(0, Style.COLOR_A()));
        soundPanel.add(vbox, BorderLayout.CENTER);
        tabs.addTab("All CC", soundPanel);

        vbox = new VBox();
        vbox.add(addRawController(64, Style.COLOR_A()));
        soundPanel = new SynthPanel(this);
        soundPanel.add(vbox, BorderLayout.CENTER);
        tabs.addTab("All CC", soundPanel);
        
        vbox = new VBox();
        vbox.add(addCustomController(Style.COLOR_B()));
        soundPanel = new SynthPanel(this);
        soundPanel.add(vbox, BorderLayout.CENTER);
        tabs.addTab("Custom CC", soundPanel);
        
        vbox = new VBox();
        hbox.add(new Joystick(this));
        vbox.add(hbox);
        vbox.add(addBlah(0, Style.COLOR_A()));
        soundPanel = new SynthPanel(this);
        soundPanel.add(vbox, BorderLayout.CENTER);
        tabs.addTab("Joystick", soundPanel);
        
        model.set("name", "Generic");  // or whatever, to set the initial name of your patch (assuming you use "name" as the key for the patch name)
        //loadDefaults();                                 // this tells Edisyn to load the ".init" sysex file you created.  If you haven't set that up, it won't bother
        }

    public JComponent addBlah(final int offset, Color color)
        {    
        Category globalCategory = new Category(this, "Foo", color);
                
        JComponent comp;
        String[] params;
        
        HBox hbox = new HBox();
        
        for(int j = 0; j < 16; j++)
            {
            hbox.add(new LabelledDial(null, this, "blah", color, 0, 127));
            }
        globalCategory.add(hbox);
        return globalCategory;
        }
    
    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, "Generic", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        hbox.add(Strut.makeHorizontalStrut(100));
        
        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }

    public JComponent addCustomController(Color color)
        {
        Category category = new Category(this, "Custom CC", color);
                
        JComponent comp;
        String[] params;
        
        VBox main = new VBox();
        for(int j = 1; j < 18; j += 3)
            {
            HBox hbox = new HBox();
            for(int i = j; i < j + 3 ; i++)
                {
                final int _i = i;
                VBox vbox = new VBox();
                                
                LabelledDial msblsb = new LabelledDial("MSB + LSB", this, "cust-cc-msb-lsb-" + i, color, 0, 16383);
                                
                comp = new LabelledDial("Parameter", this, "cust-cc-param-" + i, color, 0, 127)
                    {
                    public void update(String key, Model model)     
                        {
                        super.update(key, model);
                        msblsb.setEnabled(model.get(key) < 32);
                        }
                    };
                                        
                vbox.add(comp);
                comp = new StringComponent(null, this, "cust-cc-name-" + i, 5, "Name can be as long as you like")
                    {
                    public String getCommand() { return "Enter CC Label"; }
                    };
                model.set("cust-cc-name-" + i, "Name");
                vbox.add(comp);
                hbox.add(vbox);
                                
                vbox = new VBox();
                comp = msblsb;
                vbox.add(comp);
                comp = new CheckBox("On", this, "cust-cc-msb-" + i)
                    {
                    public void update(String key, Model model) { getCheckBox().setSelected(getState() > 64); }
                    };
                ((CheckBox)comp).setMax(127);
                ((CheckBox)comp).setState(((CheckBox)comp).getState());
                vbox.add(comp);
                hbox.add(vbox);

                vbox = new VBox();
                comp = new LabelledDial("MSB", this, "cust-cc-msb-" + i, color, 0, 127);
                vbox.add(comp);
                comp = new PushButton("Send") 
                    { 
                    public void perform() 
                        { 
                        getModel().set("cust-cc-msb-" + _i, getModel().get("cust-cc-msb-" + _i)); 
                        } 
                    };
                vbox.add(comp);
                hbox.add(vbox);

                vbox = new VBox();
                comp = new LabelledDial("MSB Alt", this, "cust-cc-msb-alt-" + i, color, 0, 127);
                vbox.add(comp);
                comp = new PushButton("Alt") 
                    { 
                    public void perform() 
                        { 
                        getModel().set("cust-cc-msb-alt-" + _i, getModel().get("cust-cc-msb-alt-" + _i)); 
                        } 
                    };
                vbox.add(comp);
                hbox.add(vbox);
                hbox.add(Strut.makeHorizontalStrut(40));
                }
            main.add(hbox);
            main.add(Strut.makeVerticalStrut(5));
            }
        
        category.add(main);
        return category;
        }

    public JComponent addRawController(final int offset, Color color)
        {
        Category category = new Category(this, "Raw CC", color);
                
        JComponent comp;
        String[] params;
        
        VBox main = new VBox();
        HBox hbox = new HBox();
        
        for(int j = 0; j < 64; j+=8)
            {
            hbox = new HBox();
            for(int i = j; i < j+8; i++)
                {
                final int _i = i;
                VBox vbox = new VBox();
                        
                VBox v = new VBox();
                HBox h = new HBox();
                comp = new CheckBox("On", this, "cc" + (i + offset))
                    {
                    public void update(String key, Model model) { getCheckBox().setSelected(getState() > 64); }
                    };
                ((CheckBox)comp).setMax(127);
                ((CheckBox)comp).setState(((CheckBox)comp).getState());
                v.add(comp);
                comp = new PushButton("Send") 
                    { 
                    public void perform() 
                        { 
                        getModel().set("cc" + _i, getModel().get("cc" +  (_i + offset))); 
                        } 
                    };
                v.add(comp);
                h.add(v);
                vbox.add(h);
                comp = new LabelledDial(null, this, "cc" + (i + offset), color, 0, 127);
                h.add(comp);


                comp = new StringComponent(null, this, "cc-name" + (i + offset), 6, "Name can be as long as you like")
                    {
                    public String getTitle() { return "CC " +  (_i + offset); }
                    public String getCommand() { return "Enter CC Label"; }
                    };
                vbox.add(comp);



                hbox.add(vbox);
                if (CC_NAMES[i + offset].equals(""))
                    model.set("cc-name" + (i + offset), "CC " + (i + offset));
                else
                    model.set("cc-name" + (i + offset), "CC " +  (i + offset) + " " + CC_NAMES[i + offset]);
                }
            main.add(hbox);
            }
        
        category.add(main);
        return category;
        }
    
    
        
        
    ////// YOU MUST OVERRIDE ALL OF THE FOLLOWING

    public void changePatch(Model tempModel)
        {
        // Here you do stuff that changes patches on the synth.
        // You probably want to look at tryToSendSysex() and tryToSendMIDI()
        //
        // This method is used primariily to switch to a new patcgh prior to loading it
        // from the synthesizer or emitting it to the synthesizer.  Some synthesizers do 
        // not report their patch location information when emitting a dump to Edisyn.  
        // If this is the case, you might want add some code at the end of this method which
        // assumes that the patch change and subsequent parse were successful, so you can
        // just change the patch information here in this method.  You should NOT do this when
        // changing a patch for the purpose of merging.  So in this case (and ONLY in this case)
        // you should end this method with something along the lines of:
        //
        //     // My synth doesn't report patch info in its parsed data, so here assume that we successfully did it
        //     if (!isMerging())
        //         {
        //         setSendMIDI(false);
        //         model.set("number", number);
        //         model.set("bank", bank);
        //         setSendMIDI(true);
        //         }
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

    public int parse(byte[] data, boolean fromFile)
        { 
        // This bulk patch data will come from a file or transmitted over sysex.
        // You should parse it into the model and return PARSE_SUCCEEDED if successful,
        // PARSE_FAILED if the parse failed, and PARSE_INCOMPLETE if the parse was
        // successful but not complete enough to assume that we have a full patch.
        // For example, the Yamaha TX81Z needs two separate parses of dumps before a patch
        // is complete -- you should only return PARSE_SUCCEEDED when the second one has come in.
        // IGNOREPATCH tells you whether you should ignore any patch access
        // information (number, bank, etc.) embedded in the data or store it in the
        // model as well.   FROMFILE indicates that the parse is from a sysex file.
        //
        // If parse resulted in a successful and *complete* 
        return PARSE_FAILED; 
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
        // or BRANDNAME MODELNAME [Multi]               (to indicate a multimode patch)
        //
        // For example:
        //              "Waldorf Microwave II/XT/XTk"
        // or
        //              "Waldorf Microwave II/XT/XTk [Multi]"
        //
        // Notice that this is a STATIC method -- but you need to implement it
        // anyway.  Edisyn will call the right static version using reflection magic.
        return "Generic"; 
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

    public String getPatchLocationName(Model model)
        {
        // Given the patch information (such as number of bank) stored in the provided model,
        // return a simple and short string which describes the
        // patch location.  For example, if your patch is number 72 of bank B, you might say
        // "B72" or "B072" or whatnot.
        //
        // The name should ideally be free of spaces and punctuation (no
        // parentheses, hyphens, backslashes, periods, commas, etc.)   This is because it'll be
        // used in filenames, such as "B072.syx"  But it's not a hard-and-fast rule.
        //
        // The default implementation of this method returns null.  This tells Edisyn that
        // you have not implemented this method, nor getNextPatchLocation(...), nor
        // patchLocationEquals(...), and thus Edisyn should disable the Batch Downloads menu.
        //
        // This method is used for doing batch downloads.
        //
        // IMPORTANT NOTE.  sprout() calls this method to determine if we should set up batch 
        // downloading.  But if you have not yet set up an .init file, then you can't access
        // any parameters at this point because the widgets haven't been created yet.  So you
        // need to test for this and return null if so.  For example, you might start with
        // a line like:
        //
        // if (!model.exists("number")) return null;
        //
        // ... or some key your function would ordinarily need to extract from the model
        
        return null;
        }
    
    public Model getNextPatchLocation(Model model)
        {
        // Given the provided model containing a patch location (bank, number, etc.), this should 
        // return a model containing the NEXT patch location.  For example, if the
        // model's location is B072, then perhaps the next model might be B073.  And if the model was
        // B128, maybe the next model should be C001.  This should wrap around as well.  Let's say
        // that the model is D128, the final location in the synthesizer.  Then the next model should
        // be something like A001.
        //
        // And yes, if you only have one patch on your synthesizer, then it's always the next patch.
        // Just return it.
        //
        // This method is used for doing batch downloads.
        return null;
        }

    public boolean patchLocationEquals(Model patch1, Model patch2)
        {
        // This should return true if the patch locations stored in the given two patches are the same.
        // For example, they're both Bank B, Number 72.
        //
        // This method is used for doing batch downloads.
        return false;
        }
    
    
    
 
 
    ////// YOU PROBABLY WANT TO OVERRIDE ALL OF THE FOLLOWING

    public String getPatchName(Model model) 
        {
        // Here you'd look up the patch name in the model and return it. 
        // If there is no patch name, you can just return null.
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
        if (key.startsWith("cc-"))
            {
            int cc = StringUtility.getInt(key);
            return buildCC(getChannelOut(), cc, model.get(key));
            }
        else if (key.startsWith("cust-cc-msb-lsb-"))
            {
            int param = StringUtility.getInt(key);
            return buildLongCC(getChannelOut(), model.get("cust-cc-param-" + param), model.get(key));
            }
        else if (key.startsWith("cust-cc-msb-alt-") || key.startsWith("cust-cc-msb-"))
            {
            int param = StringUtility.getInt(key);
            return buildCC(getChannelOut(), model.get("cust-cc-param-" + param), model.get(key));
            }
        else return new Object[0];
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
        //              changePatch(tempModel); 
        // tryToSendSysex(requestDump(tempModel));
        
        // It is possible that it's impossible to request a dump without changing
        // the patch regardless.  In this case you can ignore changePatch and just
        // do a changePatch always.  You'd need to implement this.
        
        super.performRequestDump(tempModel, changePatch);
        }

    public byte[] requestDump(Model tempModel) 
        { 
        // This asks the synth to dump a specific patch (number and bank etc. specified
        // in tempModel).  If CHANGEPATCH is true you should first change the patch.
        //
        // If you can let Edisyn call changePatch(), and then you just emit a single
        // sysex command as a patch request, implement this version.
        //
        // It is possible that requestDump and requestCurrentDump are identical.  This
        // might happen if you always have to change the patch no matter what (see the
        // description of performRequestDump above) in which case you could just have this
        // method call requestCurrentDump().
        
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

    public double getPauseBetweenMIDISends() 
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
        
    public int getPauseAfterSendAllParameters() 
        {
        // Some synths need extra time after a parameter dump before
        // they can do anything else, notably play notes properly.  
        // For example, the Kawai K4 needs about 100ms after a parameter
        // dump or else it'll play notes in a strange truncated way.
        // Here you can specify that Edisyn must pause at least so many
        // milliseconds before issuing another MIDI message after it has
        // called sendAllParmeters().
        return 0;
        }
        
    public int getBulkDownloadWaitTime()
        {
        // Edisyn does bulk downloads by iteratively requesting a patch, then
        // waiting for it to load, then saving it.  Edisyn will wait for up to
        // getBulkDownloadWaitTime() milliseconds before it checks to see if the
        // patch has arrived and try to save it; else it will issue another request.
        //
        // The default value is 1000 (one second).  If your synth takes more (or less!)
        // time to respond and dump a patch to Edisyn, you may wish to change this value.
        // You'd like it as short as possible without missing dumps. 
        return 1000; 
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

    public boolean getReceivesPatchesInBulk()
        {
        // Most synthesizers send patch dumps to Edisyn via a single sysex message which
        // is handled using the parse(...) method.  But some synthesizers, such as the
        // PreenFM2, send patch dumps as multiple separate NRPN or CC messages.  If this
        // is the case, you should override this method to return FALSE so Edisyn can
        // detect this during its batch patch-download process.
        return true;
        }
        
    public boolean getSendsParametersAfterNonMergeParse()
        {
        // Some synthesizers cannot change patches via program change when in multi-mode.
        // So when you issue a patch request, they just give Edisyn the patch, but don't
        // switch to playing it.  So this command issues a sendAllParameters() on receiving
        // a (non-merge) parse from the synthesizer to keep it up to date.  Example synths
        // with this issue: Waldorf Blofeld and Microwave.
        return false;
        }

    public int getVoiceMessageRoutedChannel(int incomingChannel, int synthChannel)
        {
        // Some synthesizers need to reroute voiced messages (messages with channels) from
        // the controller to the synthesizer along some other channel.  For example, the KawaiK4
        // needs to route drum notes to a special channel different from the standard K4
        // input channel.  If you need to customize the channel that the Controller routes
        // to, override this to return some other channel.
        return synthChannel;
        }

    }
