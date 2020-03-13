/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.tuning;

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

public class Tuning extends Synth
    {
    public static final String DEFAULT_NAME = "Tuning";
	public static final String[] NOTES = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    
    public Tuning()
        {
        // Here you set up your interface.   You can look at other patch editors
        // to see how they were done.  At the very end you typically would say something like:
            
        VBox vbox = new VBox();
        vbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        vbox.add(addTuning(1, Style.COLOR_A()));
        SynthPanel soundPanel = new SynthPanel(this);
        soundPanel.add(vbox, BorderLayout.CENTER);
        tabs.addTab("Global, 1-64", soundPanel);

        vbox = new VBox();
        vbox.add(addTuning(2, Style.COLOR_A()));
        soundPanel = new SynthPanel(this);
        soundPanel.add(vbox, BorderLayout.CENTER);
        tabs.addTab("65-128", soundPanel);
        
        model.set("name", DEFAULT_NAME);                // or whatever, to set the initial name of your patch (assuming you use "name" as the key for the patch name)
        model.set("number", 0);
        //loadDefaults();                   // this tells Edisyn to load the ".init" sysex file you created.  If you haven't set that up, it won't bother
        }

    public JFrame sprout()     
        {
        JFrame frame = super.sprout();
        addTuningMenu();
        return frame;
        }

    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, "MIDI Tuning Standard", color);
        globalCategory.makeUnresettable();
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        comp = new PatchDisplay(this, 3);
        vbox.add(comp);
        
        comp = new StringComponent("Patch Name", this, "name", 16, "Name must be up to 16 ASCII characters.")
            {
            public String replace(String val)
                {
                return revisePatchName(val);
                }
                                
            public void update(String key, Model model)
                {
                super.update(key, model);
                updateTitle();
                }
            };
        vbox.add(comp);
                                
        globalCategory.add(vbox, BorderLayout.WEST);
        return globalCategory;
        }
        
    public JComponent addTuning(int num, Color color)
        {
        Category category = new Category(this, "Tuning " + (num == 0 ? "1-64" : "65-128"), color);
                
        JComponent comp;
        String[] params;
        
        int pos = (num == 1 ? 1 : 65);
        
        VBox main = new VBox();
        for(int j = pos; j < pos + 64; j += 8)
            {
            HBox hbox = new HBox();
            for(int i = j; i < j + 8 ; i++)
                {
                if (i != j) hbox.add(Strut.makeHorizontalStrut(10));

                comp  = new LabelledDial("" + i + " Base", this, "base-" + i, color, 0, 127)
                	{
                	public String map(int val)
                		{
                		return (NOTES[val % 12] + " " + (val / 12)); 
                		}
                	};
                hbox.add(comp);

                comp  = new LabelledDial("" + i + " Detune", this, "detune-" + i, color, 0, 16383)
                	{
                	public String map(int val)
                		{
                		return String.format("%3.2f", (val / 16384.0 * 100.0));   // Note 16384, no 16383
                		}
                	};
                hbox.add(comp);
                }
            main.add(hbox);
            }
        
        category.add(main);
        return category;
        }


	public void setTunings(int[] base, int[] detune)
		{
        getUndo().push(model);
        getUndo().setWillPush(false);
		setSendMIDI(false);
		for(int i = 0; i < 128; i++)
			{
			model.set("base-" + (i + 1), base[i]);
			model.set("detune-" + (i + 1), base[i]);
			}
		repaint();		// see discussion in Blofeld patch editor
		setSendMIDI(true);
		getUndo().setWillPush(true);
		sendAllParameters();
		}

	public void addTuningMenu()
		{
        JMenu menu = new JMenu("Tuning");
        menubar.add(menu);
        JMenuItem seanTuningMenu = new JMenuItem("Sean Tuning");
        seanTuningMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                // for demonstration, we set the tunings to all 0
                setTunings(new int[128], new int[128]);
                }
            });
        menu.add(seanTuningMenu);
		}
        
        
    //// may not need these

    public byte getID() 
        { 
        try 
            { 
            byte b = (byte)(Byte.parseByte(tuple.id));
            if (b >= 0) return b;
            }
        catch (NullPointerException e) { } // expected.  Happens when tuple's not built yet
        catch (NumberFormatException e) { e.printStackTrace(); }
        return 0;
        }
        
    public String reviseID(String id)
        {
        try 
            { 
            byte b =(byte)(Byte.parseByte(id)); 
            if (b >= 0) return "" + b;
            } 
        catch (NumberFormatException e) { }             // expected
        return "" + getID();
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
        return "Tuning"; 
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
        if (!model.exists("number")) return null;
        return ("" + model.get("number"));
        }
    
    public Model getNextPatchLocation(Model model)
        {
        int current = model.get("number", 0) + 1;
        if (current > 127) 
            current = 0;
                
        Model newModel = buildModel();
        newModel.set("number", current);
        return newModel;
        }

    public boolean patchLocationEquals(Model patch1, Model patch2)
        {
        return patch1.get("number", 0) == patch2.get("number", 0);
        }
    
 
    public String getPatchName(Model model) 
        {
        return model.get("name", DEFAULT_NAME);
        }

    public String revisePatchName(String name)
        {
        name = (name + "                ").substring(0, 16);
        
        char[] c = name.toCharArray();
        for(int i = 0; i < c.length; i++)
            if (c[i] > 127) c[i] = 32;

        return new String(c);       
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






    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile) 
        { 
        return new Object[0]; 
        }
    
    
    public byte[] emit(String key) 
        { 
        return new byte[0]; 
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



    }
