![Edisyn Splash Banner](https://raw.githubusercontent.com/eclab/edisyn/master/pics/Banner.png)

# Edisyn
Synthesizer Patch Editor (Version 9)

By Sean Luke (sean@cs.gmu.edu)


## About

Edisyn is a synthesizer patch editor library written in pure Java.   It runs on OS X, Linux, and (probably) Windows.  

Edisyn presently supports:

* Waldorf Blofeld and Waldorf Blofeld Keyboard (Single and Multi Modes)
* Waldorf Microwave II, XT, and XTk (Single and Multi Modes)

Edisyn has infinite levels of undo, CC mapping and learning, offline modes, different amounts of randomization and patch merging/recombination, and other goodies.

Ultimately Edisyn will have some more written for it as well as I get time; and of course the patch editors I can write are restricted to the synths I own and can test on!  Edisyn has no graphical interface editor system like Ctrlr, but it's designed to make the GUI pretty easy to write (well, for me anyway).

## Pictures!

* Blofeld Single: [Oscillators and Filters](https://raw.githubusercontent.com/eclab/edisyn/master/pics/BlofeldSingle1.png), 
[LFOs and Envelopes](https://raw.githubusercontent.com/eclab/edisyn/master/pics/BlofeldSingle2.png), 
[Modulation and Effects](https://raw.githubusercontent.com/eclab/edisyn/master/pics/BlofeldSingle3.png), 
[Arpeggiator](https://raw.githubusercontent.com/eclab/edisyn/master/pics/BlofeldSingle4.png)

* Blofeld Multi: [Multi and Parts 1-4](https://raw.githubusercontent.com/eclab/edisyn/master/pics/BlofeldMulti1.png), 
[Parts 5-10](https://raw.githubusercontent.com/eclab/edisyn/master/pics/BlofeldMulti2.png), 
[Parts 11-16](https://raw.githubusercontent.com/eclab/edisyn/master/pics/BlofeldMulti3.png)

* Microwave II/XT/XTk Single: [Oscillators and Filters](https://raw.githubusercontent.com/eclab/edisyn/master/pics/XTSingle1.png), 
[Envelopes](https://raw.githubusercontent.com/eclab/edisyn/master/pics/XTSingle2.png), 
[Modulation](https://raw.githubusercontent.com/eclab/edisyn/master/pics/XTSingle3.png), 
[Other](https://raw.githubusercontent.com/eclab/edisyn/master/pics/XTSingle4.png)

* Microwave II/XT/XTk Multi: [Multi and Instruments 1-2](https://raw.githubusercontent.com/eclab/edisyn/master/pics/XTMulti1.png), 
[Instruments 3-5](https://raw.githubusercontent.com/eclab/edisyn/master/pics/XTMulti2.png), 
[Instruments 6-8](https://raw.githubusercontent.com/eclab/edisyn/master/pics/XTMulti3.png)


## Install and Run Edisyn

Edisyn is cross-platform and will run on a variety of platforms (Windows, Linux) but I am personally developing on and for OS X.  I'd appreciate feedback and screenshots of it running on Windows and Linux so I can tweak things.


### Installation and Running on OS X 

First install Edisyn from the [Edisyn.dmg](https://github.com/eclab/edisyn/raw/master/install/Edisyn.dmg) file located in the "install" directory.  Sadly, it's a whopping 70MB because it includes the Java VM.  :-(


Sierra has really locked down the ability to run an application that's not from a paid-up Apple Developer.  And I'm not one.  So you will have to instruct Sierra to permit Edisyn to run.

Let's assume you stuck Edisyn in the /Application directory as usual.  Then:

1. Run the Terminal Program (in /Applications/Utilities/)
2. Type the following command and hit RETURN: `   sudo spctl --add /Applications/Edisyn.app`
4. Enter your password and hit RETURN.
5. Quit the Terminal Program

Now you should be able to run Edisyn.  Let me know if this all works.


### Installation and Running on Windows

I don't know.  But you may be able to download the [jar file](https://github.com/eclab/edisyn/raw/master/jar/edisyn.jar) located in the "jar" directory and double-click on it.  Tell me if this works.


### Installation and Running on Linux

I'm told that this works on Ubuntu:

1. Install Java if you don't have it yet (openjdk probably).

2. Download [Edisyn's jar file](https://github.com/eclab/edisyn/raw/master/jar/edisyn.jar) located in the "jar" directory.

3. Right-click on the jar file icon and choose "Properties".  Then select the "Open With" tab, and select your Java VM (for example "Open JDK Java 8 Runtime").  
The press "Set as Default".  This makes the Java VM the default application to launch jar files.

4. Thereafter you should be able to just double-click on the file to launch Edisyn.


### Running from the command line (OS X, Windows, Linux)

1. Make sure Java is installed.

2. Download [Edisyn's jar file](https://github.com/eclab/edisyn/raw/master/jar/edisyn.jar) located in the "jar" directory.

3. Run Edisyn as:   `java -jar edisyn.jar`



## Using Edisyn

### The Editor Pane

The editor pane should be self-explanatory.  There are multiple tabs which together cover all of the parameters of 
the synthesizer.   The first tab also contains an area of the name of the Synthesizer ("Waldorf Blofeld", say).
This area holds the name of the patch, the patch number etc., and the device ID if any.

Edisyn tries hard to stay *in sync* with the synthesizer: that is, what you are editing in Edisyn 
reflects what's getting edited in the synthesizer.  If Edisyn believes it is not in sync, the
patch number and the device ID will turn RED and the window will display "UNSYNCED".

Various operations will cause Edisyn to drop out of sync; and if you're out of sync, Edisyn may ask you
to specify a patch number etc. before continuing certain operations so it can get back in sync.

You can have multiple editor panes, but note that only the frontmost editor pane will receive MIDI.  So if you for some reason set up both editor panes with the same interface, then request MIDI from one pane, then quickly switch to the other pane, you could in theory get the MIDI sent to the other pane.  So don't do that.


### The File Menu

* *New [synth name]* creates a new editor pane of the same kind as your current synth editor pane, set to the default (the synth's Init patch setting).

* *New Synth* creates an editor pane for the synthesizer of your choice, set to its init patch setting.

* *Duplicate Synth* creates a new editor pane of the same kind as your current synth editor pane, with the same parameter settings.

* *Load...* loads a sysex file into the existing editor pane.  It's called Load instead of Open because it 
doesn't open a new window.

* *Close Window* closes, well, you know, it closes, um, the window.

* *Save* and *Save As...* save to a sysex file.


### The Edit Menu

* *Undo* Undoes the most recent change to a parameter, patch randomization, patch reset, patch loading, etc..  Note that if you made multipe changes to the *same* parameter in a row, Undo will undo all of them.  If you undo a change, Edisyn will assume that the model is now unsynced. There are infinite levels of Undo.

* *Redo* Undoes the undo.  There are infinite levels of Redo.


### The MIDI Menu

Note that you can *send* a patch to the synthesizer and you can *write* a patch to the synthesizer.  The former
just temporarily updates the synth's current patch memory so you can play it.  The latter actually writes the 
patch to an address in the synth, replacing whatever is there.

* *Request Current Patch* asks the synthesizer to load the current patch memory into the editor.  On some machines,
the patch information sent to Edisyn isn't enough for it to determine the patch number.  This will cause Edisyn
to drop out of sync.

* *Request Patch...* asks the synthesizer to load a specific patch into the editor.  If the synthesizer complies,
once the patch is loaded, Edisyn will then send the patch to the synthesizer.

* *Request Merge* asks the synthesizer to load a specific patch into the editor.  If the synthesizer complies, then the patch is *merged* with the existing patch.  Then Edisyn will then send the patch to the synthesizer if not out of sync.
Merging works like this: if a parameter is *metric* (numeric), then Edisyn sets it to some random value in-between the two patches.  If the parameter is non-metric (for example, a set of wavetables), then Edisyn will randomly set it to one or the other of the patch values.  The percentage indicates roughly the percentage of parameters which will get merged in (versus staying as they are).

* *Randomize* randomizes the editor's current patch by various amounts, then sends it to the synthesizer if not out of sync.  Randomization works as follows.  Every parameter gets a chance to be randomized.  If a parameter is *metric* (numeric), then Edisyn will mutate it -- larger percentages mean more extreme mutations.  If a parameter is non-metric (for example, a set of wavetables), then Edisyn will, with the given percentage probability, pick an entirely random new value for it.

* *Reset* resets the editor's current patch to its initialized state, then sends it to the synthesizer if
not out of sync.

* *Send Patch* sends the patch to the synthesizer at its patch location and moves to that location (if not already there).  

* *Send Patch To...* sends the patch to *some other patch location* on the synthesizer and moves to that location.

* *Send Current Patch* sends the patch to the synth's current patch setting, whatever that is.

* *Write Patch...* writes the patch to a given location in the synthesizer.

* *Change MIDI* sets or updates the MIDI interface.

* *Disconnect MIDI* disconnects the MIDI interface.

* *Send All Sounds Off* sends the All Sounds Off message to all channels.

* *Send Test Note* sends a 1/2 second test note to the primary channel.

* *Map CC* (or *End Map CC*) toggles CC Mapping mode, which enables you to map a CC message from your controller keyboard to almost any Edisyn
editor widget.  See "CC Mapping" below.

* *Clear CCs* clears all mapped CCs.  See "CC Mapping" below.

### The Tabs Menu

The Tabs menu lets you quickly move between tabs.

* *Previous Tab* moves one tab to the left.

* *Next Tab* moves one tab to the right.

* *Tab 1* through *Tab 8* select the first through the eighth tab respectively.

### The MIDI Interface

Edisyn makes up to three MIDI device connections.  The *Receiving Device* is the MIDI device from which we will accept
patches.  This is usually your synthesizer.  The *Sending Device* is the MIDI device to which we will send 
patches and parameter changes.  We'll also need a *Channel* for the Sending Device so we can send test notes.

If you opt not to connect to a receiving or sending device (you cancel), the editor pane title bar will display "DISCONNECTED".

Optionally you can route your controller keyboard through Edisyn to play the sounds directly if you wish.  To do this,
the *Controller* is the MIDI Device of your controller keyboard.  You'll also specify an incoming *Controller Channel*
channel of course.  This can be set to "Any" for any channel (Omni).

Additionally, if you're sick of using your mouse, you can map *CC MIDI Commands* from your Controller Keyboard to 
directly manipulate dials and other widgets in Edisyn.  See "CC Mapping" below.


### Sending and Recieving Parameters

If you change a widget in the editor, Edisyn will send the appropriate sysex command to the synthesizer to change it on
the synth as well.  Additionally, if you change a parameter on the synthesizer and it forwards a *sysex* command to Edisyn,
then Edisyn will update the appropriate widget in the editor.  At present Edisyn does't support CC commands from the
synthesizer (maybe later).  So (for example) on the Blofeld you'll need to change the machine to *send sysex* -- not CC only -- when changing parameters on the synth.

### CC Mapping

If you select *Map CC*, you can map a CC controller to a widget.  This is done as follows:

1. Make sure you told Edisyn the correct MIDI device and channel for your controller.
2. Choose *Map CC*
3. Edisyn will change the window to say "LEARNING"
4. Tweak the widget to change its value.  This informs Edisyn that it's the widget you want to map.
5. Edisyn will change the window to say "LEARNING parameter[range]=cc" (*range* and *cc* might not show up)
where *parameter* is the name of the parameter the widget is controlling, *range* is the range of the parameter (if any),
and *cc* is the current CC assigned to that parameter (if any).
6. Send the CC message from your controller (turn a knob or press a button).
7. It'd now be a good idea to set your controller to only send CC values within the provided range.  For example, if *range* was 7, you might set your controller
to only send values from 0..6.  If *range* was 2, you'd set it to send values 0..1.  And so on.

Every *Tab* on every *Synthesizer Panel type* has its own unique set of CCs.
Mapped CCs for a given Synthesizer panel type are permanent until you clear all CCs for the synth by choosing *Clear CCs*, which clears them for all tabs for that Synthesizer.

### Per-Synth Specific Notes and Bugs

Gotchas and important things to know are contained in the <b>About</b> Tab in each synth editor window.  You should read it before using the editor for that synth.

## Caveats

To work around some bugs in OS X Midi and CoreMIDI4J, Edisyn's architecture at present does not let you
plug in new devices (or remove them) after Edisyn has been launched.  If you need to do so, restart Edisyn
(for now). 

Randomize isn't very useful right now.  I'm working on it.

Everything has to be sent via sysex for the moment: I don't have code written to make it easy to send CC or NRPN
if you wanted to build a patch which did that.

Popping up a new panel is slow.  Profiling suggests that the primary reason for this is that JComboBox construction
is slow.  So I can't get around it.

