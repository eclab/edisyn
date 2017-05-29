# edisyn
Synthesizer Patch Editor

By Sean Luke (sean@cs.gmu.edu)


## About

Edisyn is a synthesizer patch editor library written in pure Java.  It comes with one completed patch editor
for the *Waldorf Blofeld*, but will ultimately have some more written for it as well as I get time.  Edisyn
has no graphical interface editor system like Ctrlr, but it's designed to make the GUI pretty easy to write.

At present Edisyn *only runs on OS X* (well, I've only tried it on OS X, that's its target).  If you try running
it elsewhere, you're on your own.

This is Edisyn version 1, a very early prerelease which doesn't feel very MacOS like yet, and which has lots of
wires sticking out.  But it works!  Try it out on your Blofeld.


## Installation

For the time being, to install Edisyn you need to do the following three things.

1. Install the Java JDK.
2. Place the file `libraries/coremidi4j-1.0.jar` in the directory `/Library/Java/Extensions`
3. Put file `jar/edisyn.1.jar` where you like (it's Edisyn's executable).


## Running

Double click on the file `edisyn.1.jar`

Edisyn should launch and either present you with a window asking what MIDI interface you want to use, or tell
you that there are no avaialble MIDI interfaces, and that you'll need to work offline.


### The Editor Pane

The editor pane should be self-explanatory.  There are four tabs which together cover all of the parameters of 
the synthesizer.  The first tab, *Oscillators and Filters*, contains an are called *Waldorf Blofeld* which
lets you set the patch name and category, bank, number, and Blofeld ID.   The bank, number, and ID are mostly
for saving out to sysex files: whenever you upload or download patch to/from the Blofeld, you'll be prompted
to revise those if necessary.

### The File Menu

* *New* creates a new editor pane.  Note that only the frontmost editor pane will receive MIDI.  So if you for
some reason set up both editor panes with the same interface, then request MIDI from one pane, then quickly
switch to the other pane, you could in theory get the MIDI sent to the other pane.  So don't do that.

* *Load* loads a sysex file into the existing editor pane.  It's called Load instead of Open because it 
doesn't open a new window.

* *Save* and *Save As...* save to a sysex file.


### The MIDI Menu

Note that you can *send* a patch to the synthesizer and you can *burn* a patch to the synthesizer.  The former
just temporarily updates the synth's current patch memory so you can play it.  The latter actually writes the 
patch to an address in the synth, replacing whatever is there.

* *Request Patch and Send* asks the synthesizer to load a specific patch into the editor.  If the synthesizer complies,
once the patch is loaded, Edisyn will then send the patch to the synthesizer.

* *Request Merge and Send* asks the synthesizer to load a specific patch into the editor.  If the synthesizer complies,
then the patch is *merged* with the existing patch, meaning that some *percentage* of parameters in the existing
patch are replaced with the old patch.  Then Edisyn will then send the patch to the synthesizer.

* *Send Patch* sends the current patch to the synthesizer.

* *Randomize and Send* randomizes the editor's current patch, then sends it to the synthesizer.

* *Burn Patch* burns the patch to a given location in the synthesizer.

* *Change MIDI* sets or updates the MIDI interface.

* *Disconnect MIDI* disconnects the MIDI interface.

* *Send All Sounds Off* sends the All Sounds Off message to all channels.

* *Send Test Note* sends a 1/2 second test note to the primary channel.


### The MIDI Interface

Edisyn makes up to three MIDI device connections.  The *Receiving Device* is the MIDI device from which we will accept
patches.  This is usually your synthesizer.  The *Sending Device* is the MIDI device to which we will send 
patches and parameter changes.  We'll also need a channel for the Sending Device so we can send test notes.

Optionally you can route your controller keyboard through Edisyn to play the sounds directly if you wish.  To do this,
the *Keyboard Device* is the MIDI Device of your controller keyboard.  You'll also specify an incoming keyboard
channel of course.  This can be set to "Any" for any channel (Omni).


## Caveats and Bugs

CoreMIDI sometimes doesn't launch properly.  If Edisyn seems to be hanging on startup, just kill it and try again.

Randomize and Send isn't very useful right now.  I'm working on it.

For now, there's no interface for sending test notes via the screen.

Everything has to be sent via sysex for the moment: I don't have code written to make it easy to send CC or NRPN
if you wanted to build a patch which did that.

Making a new panel is slow.  Profiling suggests that the primary reason for this is that JComboBox construction
is slow.  So I can't get around it.

