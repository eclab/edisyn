# Testing Edisyn

So you want to test an Edisyn patch editor?  There's lots of pieces to Edisyn: but you can just try out some of them and let me know how they go.

## Reporting Possible Bugs

After you've done some testing, send me email (sean@cs.gmu.edu) with this information:

* A precise step-by-step description of what you did
* A precise description of what you had expected would happen
* A precise description of what happened instead
* Is it repeatable?
* What is your operating system?
* What version of Edisyn are you using?
* What version of your synthesizer firmware are you using? Which synthesier is it exactly?
* What is your MIDI interface?  Or otherwise, how are you connecting to your synthesizer?
* Did you read the ABOUT panel for the editor?
* What did you set in Edisyn and in your synthesizer to connect the two?

 

## Warnings

I get a lot of false negative tests ("It didn't work", when in it works great).  The number one reason for these is a bad MIDI interface which does not properly support sysex.  There are many such interfaces out there, especially the ones that are little blobs with wires attached at both ends, mostly bad Chinese copies of the M-Audio Uno.  If you can't connect to Edisyn or you can send notes but not patches, it'd help a lot if you were able to double-check with another interface.

Also note that a known bug in Windows prevents Edisyn from properly working with certain machines, notably the FB-01 and Casio CZ-series.

In most cases you should keep "Send Real-Time Changes" and "Auto Sends Patches" TURNED ON unless otherwise instructed in the ABOUT panel.

## Things Edisyn Does With Your Synthesizer

Edisyn communicates with your synthesizer via MIDI, and especially sysex.  Edisyn's communication can be broken into several operations, not all of which are available on all synthesizers (unfortunately).  So sometimes I have to do workarounds.  Here are the big ones:

* Asking the synthesizer to change its patch
* Asking the synthesizer to provide the patch currently in working memory
* Asking the synthesizer to provide a patch stored in RAM
* Sending a patch to the synthesizer's current working memory
* Writing a patch a slot in the synthesizer's RAM
* Updating an individual parameter in the synthesizer's current working memory
* Sending test notes
* Asking the synthesizer to provide an entire bank, or all patches

Most of Edisyn's features rely on one or more of these items, especially sending to current working memory.

## Things you Could Test

### Basic Operations
* Does the editor fire up?
* Do all the widgets work?  Are their ranges and options correct? Do they match the ranges and options on the synthesizer?  Is anything missing?  Do any of them seem to be accidentally linked?
* Can you save a patch to disk?  Does it load from disk properly?
* Can you connect to the synthesizer?  Can you send test notes to it?
* Can you disconnect and reconnect without issue?

### Basic Sending
* If the synthesizer supports it, can you update each of its parameters independently? When you do so, do each of the parameters update in the synth in the same way? Does it sound right? (Many synths won't update their screens unless you force it in some way -- see the ABOUT panel).
* Can you send a patch to the synth's current working memory? ("MIDI->Send to Current Patch")
* Can undo and redo, and if you do so, does it update the synthesizer?
* Can you randomize the patch to some degree ("Edit->Randomize")?  Does it update the synthesizer?

### Stress Testing Sending
* Fire up the Hill-climber.  Learn how to use it.  Can you hill-climb without causing problems in the synthesizer?
* Fire up the Morpher.  Learn how to use it.  Can you morph with without causing problems?  Can you run in autopilot for a while without issues?
* Does Blend work?

### Writing and Requesting
* Can you request a patch from current working memory (if the synth supports it)? ("MIDI->Request Current Patch")
* Can you request a patch from RAM ("MIDI->Request Patch...")
* If you request the last patch from RAM, can you then request the FIRST patch from RAM via "MIDI->Request Next Patch"?  That is, does it wrap around?  How about bank boundaries (last of one bank to first of the next)?
* Can you request a merge successfully? ("MIDI->Request Merge")  (if supported)
* Can you write a patch to the synthesizer? ("MIDI->Write Patch")  Can you then request the same patch in a new editor and compare the two?  Are they the same?
* If the synth supports it, can you request the current patch? Can you send a patch to current memory, then request the patch, and compare the two?  Did anything change?

### Batch Downloads

* Try doing a Batch Download from the synthesizer.  Can you save it to disk as separate files?  As a bulk file?  Can you send it to the Librarian?

### Librarian
* If Supported, fire up the Librarian.  Do the banks look right?  Numbering?
* Download patches into the Librarian: try downloading all patches; patches in each bank separately; and individual patch ranges.  Does it work right?
* Write patches from the Librarian: try writing all patches; patches in each bank separately; and individual patch ranges.  Does it work right?
* Can you save patches individually?  Can you save a group of patches as separate files?  As a single bulk file?
* Can you change the names of patches? (Press RETURN)  Can you move, copy, and swap patches?  Can you do so between two different librarian windows?  Does double-clicking on a patch slot properly open a copy of it in the editor?
* If Supported, can you "Request All Patches from Synth"?  (or "Request Bank from Synth"?)  (These are special fast-download options).


### Uninvited Information from the Synthesizer
* If your synthesizer supports it, dump an entire bank or all patches to Edisyn.  For synths that send entire banks as a single patch (like the DX-7), does Edisyn recognize this and give you options?  For synths that send banks as individua patches, does Edisyn update itself each time in response?
* Try doing the same thing when the Librarian is open.  Does the Librarian accept the patches and put them in the right places?
* If your synthesizer supports it, send individual parameters to Edisyn by tweaking knobs etc.  Does Edisyn recognize this?


