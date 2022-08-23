![Edisyn Splash Banner](https://raw.githubusercontent.com/eclab/edisyn/master/pics/Banner.png)

# Edisyn
Synthesizer Patch Editor (Version 31)

By Sean Luke (sean@cs.gmu.edu)

Related projects:  

* [Flow](https://github.com/eclab/flow), a fully-modular, polyphonic, additive software synthesizer.
* [Gizmo](https://cs.gmu.edu/~sean/projects/gizmo/), an Arduino-based MIDI Swiss Army knife.
* [Computational Music Synthesis](https://cs.gmu.edu/~sean/book/synthesis/), an open-content book on building software synthesizers.


## Jump to...

* [Edisyn's Manual](#manual)
* [Supported Synthesizers](#support)
* [Installing on a Mac](#mac)
* [Installing on Windows](#windows)
* [Installing on Linux](#linux)


## Paper on Edisyn's Stochastic Patch Exploration Tools

[Paper Here](https://cs.gmu.edu/~sean/papers/evomusart19.pdf).  Presented at EvoMUSART 2019.

## Contributors

* V. Hoyle collaborated on the Red Sound DarkStar and Microtuning editors, and cooked the deep-learned variational autoencoder for the DX7
* Wim Verheyen wrote the Prophet Rev2 editor
* Special thanks to Derek Cook's [CoreMidi4J](https://github.com/DerekCook/CoreMidi4J), which Edisyn uses to fix critical bugs in MacOS's sysex handling.
* Thanks to the many beta testers and bug report submitters for various editors.  Many of these people are thanked in the individual About panels for different synth editors.

## Donations

Donations are welcome via Paypal to my email address (sean@cs.gmu.edu).

## What's New

Version 31 has various bug fixes and two new editors:

* E-Mu Planet Phatt, Orbit v1 and Orbit v2, Carnaval, Vintage Keys, and Vintage Keys Plus
* E-Mu Proteus 2000 Series 

Version 30 has lots of improvements, but one big one in particular:

* Edisyn now has a Librarian!  
* The Librarian should work with a large number of Edisyn's editors, though has been tested only on about 2/3 of them.  The ones for which it has not been tested will issue a warning.  I need help testing them.  To see the current Librarian status, or to get involved testing, see the [Librarian Page](LIBRARIAN.md).
* Bug fixes to the Roland JV-880 Editor

Version 29 has two changes:

* New Sequential Prophet Rev2 Editor, courtesy of Wim Verheyen
* Bug fixes to the Roland D-110 Editor

Version 28 has many improvements, but the big ones are:

* New Korg Volca Series Editor
* New Waldorf Pulse 2 Editor
* Fixes to M-Audio Venom Editor
* Deep-Learned Variational Autoencoder, which compresses the parameter space for the Yamaha DX7 and thus improves randomization and hill-climbing.  See the manual.

## About Edisyn

Edisyn is a synthesizer patch editor library written in pure Java.   It runs on MacOS, Linux, and Windows.  

Edisyn is particularly good at exploring the space of patches.  It has to my knowledge the most sophisticated set of general-purpose patch-exploration tools of any patch editor available.

<a name="support"/>

Edisyn presently supports:

* Alesis D4 and DM5 
* Casio CZ Series (CZ101, CZ1000, CZ3000, CZ5000, CZ-1, CZ-230S)
* DSI Prophet '08, Tetra, Mopho, Mopho Keyboard, Mopho SE, and Mopho x4 (Single and (for Tetra) Combo modes)
* E-Mu Morpheus and Ultraproteus (Single, Hyperpreset, and MidiMap modes)
* E-Mu Planet Phatt, Orbit and Orbit v2, Carnaval, Vintage Keys, and Vintage Keys Plus
* E-Mu Proteus 1, 1XR, 2, 2XR, 3, 3XR, and 1+Orchestral
* E-Mu Proteus 2000 series (Proteus 1000/2000/2500, Audity 2000, Virtuoso 2000, Xtreme Lead-1, Mo'Phatt, B-3, Planet Earth, Orbit-3, Proteus Custom, XL-1 Turbo, Turbo Phatt, Vintage Pro, XL-7, MP-7, PX-7, PK-6, XK-6, MK-6, Halo, and Vintage Keys Keyboard).
* Kawai K1, Kawai K1m, and Kawai K1r (Single and Multi Modes)
* Kawai K4 and Kawai K4r (Single, Multi, Drum, and Effect Modes)
* Kawai K5 and K5m (Single and Multi Modes, plus single-cycle wave uploading)
* Korg SG Rack (Single and Multi Modes) and Korg SG Pro X
* Korg MicroKorg (Single and Vocoder Modes)
* Korg Microsampler
* Korg Wavestation SR (Performance, Patch, and Wave Sequence Modes)
* Korg Volca Series (Joint editor for Bass, Beats, Drum Single, Drum Split, FM, Keys, Kick, NuBass, Sample/Sample2 Multi, Sample2 Single, /u/pajen firmware)
* M-Audio Venom (Single, Multi, Arpeggiator, and Global Modes)
* Novation Drumstation and D Station
* Novation ReMOTE SL, SL MKII, and SL Compact Series
* Oberheim Matrix 6, 6R, and 1000 (Single and (for 1000) Global Modes) 
* PreenFM2
* Red Sound DarkStar and DarkStar XP2
* Roland D-110 (Tone and Multi Modes)
* Roland JV-80 and JV-880 (Single, Multi, and Drum Modes)
* Sequential Prophet Rev2
* Waldorf Blofeld and Waldorf Blofeld Keyboard (Single and Multi Modes, plus Wavetable uploading)
* Waldorf Kyra (Single and Multi Modes)
* Waldorf Microwave II, XT, and XTk (Single and Multi Modes)
* Waldorf Pulse 2
* Waldorf Rocket
* Yamaha DX7 Family (DX7, TX7, TX802, TX216/TX816, Korg Volca FM, Dexed, DX200, DX9)
* Yamaha 4-Op FM Family (DX21, DX27, DX100, TX81Z, DX11, TQ5, YS100, YS200, B200, V50, etc.) (Single and (for TX81Z and DX11) Multi Modes)
* Yamaha FB01 (Single and Multi Modes)
* Yamaha FS1R (Voice, Performance, and Fseq Modes)
* Yamaha TG33, SY22, and SY35 (Single and (for TG33) Multi Modes)
* General CC, NRPN, and RPN editing
* Microtuning editing

Edisyn has infinite levels of undo, CC and NRPN mapping and learning, offline modes, per-parameter customization, real-time parameter updates, test notes and chords, Pseudo-MPE support, and lots more.  Edisyn also has many specialized tools designed to help you explore new patch possibilities without directly programming them.  These include:

* *Randomization:* Weighted patch mutation
* *Merging:* Weighted recombination of two patches of your choice
* *Mixing:* Bulk recombination of many patches into a single one
* *Blending:* Random recombination of two randomly-chosen patches on your synth
* *Nudging:* Pushing the patch to sound a bit more (or a bit less) like one of four other patches of your choice
* *Morphing:* Real-time interpolation of four patches to form a new patch
* *Hill-Climbing and Constriction:* evolutionary techniques for guided randomized search through the space of parameters, where Edisyn iteratively offers patch possibilities for you to grade, then looks for new ones based on your assessments.
* *Deep-Learned Models:* improvements to Randomization and Hill-Climbing using a deep-learned Variational Autoencoder neural network (DX7 Family only)
* *Mixing:* recombining large numbers of patches to form one blended patch

#### "Could You Develop a Patch Editor for Synth X for Me?"

Sure!  But building a patch editor is a long-term commitment of debugging and maintenance.  It can't easily be done with a remote person (you) handling the debugging.  So with rare exceptions I only build editors for synths I personally own (I might sell them long after the editor is stable).  So if you'd like, say, the Alesis Andromeda to have an editor, all you have to do is **gift me an Alesis Andromeda!**  I'll even pay for shipping.

#### Why These Synths in Particular?

I have a special interest in hard-to-program synths, either because they have very poor interfaces (or no interface at all), or because their synthesis architecture is difficult to program (FM, Additive).  You'll note the high number of 1U rackmounts.  :-)  Certain other synths (Blofeld, Prophet '08, etc.) are there because I own them and like making stuff for them.


<a name="manual"/>

## Manual

Edisyn has an [extensive manual](https://cs.gmu.edu/~eclab/projects/edisyn/Edisyn.pdf) which describes how to run it, and (if you are so inclined) how to make new patch editors.

## Install and Run Edisyn

Edisyn is cross-platform and will run on a variety of platforms (Windows, Linux) but I am personally developing on and for MacOS.  I'd appreciate feedback and screenshots of it running on Windows and Linux so I can tweak things.

<a name="mac"/>

### Installation and Running on MacOS

First install Edisyn from the [Edisyn.app.zip](https://cs.gmu.edu/~eclab/projects/edisyn/Edisyn.app.zip) file.  Sadly, it's a whopping 70MB because it includes the Java VM.  :-(

MacOS has lately locked down the ability to run an application that's not from a commercial, paying Apple Developer.  And I'm not one.  So you will have to instruct MacOS to permit Edisyn to run.  Let's try the easy approach first:

#### Easy Way to Permit Edisyn to Run

On some versions of MacOS, you can CONTROL-Click on Edisyn's App Icon, and while holding Control down, select "Open".  Now instead of telling you that Edisyn cannot be opened because it's from an unidentified developer, you'll be given the option to do so. You probably will only have to do this once.

#### Slightly Harder Way to Permit Edisyn to Run

If this isn't working, try the following.  Let's assume you stuck Edisyn in the /Applications directory as usual.  Now we have to tell Gatekeeper to allow Edisyn to run on your machine:

1. Run the Terminal Program (in /Applications/Utilities/)
2. Type the following command and hit RETURN: `   sudo spctl --add /Applications/Edisyn.app`
4. Enter your password and hit RETURN.
5. Quit the Terminal Program

Now you should be able to run Edisyn.  Let me know if this all works.

If you want to use Edisyn in combination with a DAW, see the manual's section on building a MIDI Loopback.

You can also run Edisyn from its jar file from the command line: see "Running from the command line" at end of these instructions. 

#### Rosetta and the M1

At present Edisyn only runs under Rosetta on the M1, because its package contains an Intel-only Java VM (because I only own an Intel Mac and am not running Big Sur).  Don't expect things to change until I get an M1.  If you have installed Java yourself, you can run Edisyn from the command line (see later below) and it'll probably run natively.

#### If Edisyn bombs in MacOS on selecting a patch editor...

You may have installed the MMJ (MIDI Java) library in the past, which is now obsolete.  This library was required by old applications but no longer works properly and should be removed.  Look in /Library/Java/Extensions/ or in \[Your Home Directory\]/Library/Java/Extensions for the files **mmj.jar** or **libmmj.jnilib**.  If you find them, remove them from the directory and try again.


<a name="windows"/>

### Installation and Running on Windows

I believe that the following should work:

1. Download and install at least [Java 11](http://www.oracle.com/technetwork/java/javase/downloads/index.html).  The JRE should work fine, but if Oracle is no longer offering the JRE, install the JDK (which installs a JRE among other gunk).
2. Download Edisyn's jar file, called [edisyn.jar](https://cs.gmu.edu/~eclab/projects/edisyn/edisyn.jar).
3. Double-click on edisyn.jar to launch Edisyn.

If you want to use Edisyn in combination with a DAW, see the manual's section on building a MIDI Loopback.

#### Casio CZ, Yamaha FB01 and SY22/35/TG33 editors on Windows

These editors require an unusual quirk of MIDI sysex which is not properly supported by Java under Windows.  I have failed to find a workaround for it, and so these editors may not work properly under Windows.  Notably the CZ editor will definitely not work right.  I'm sorry about that.

#### Java Crashing on Windows

I have reports of Java crashing on Windows if you set Edisyn's "Receive From" or "Send To" devices to the same device as the "Controller" or "Controller 2" devices.  So don't do that: there is never a need to do so.  If you don't have a dedicated controller, you should leave the "Controller" and "Controller 2" devices blank.

I have had at least one report that Java 8 on Windows has serious problems with some Edisyn patch editors (probably memory).  Install something newer.

#### Dealing with High-Resolution Displays

Java doesn't handle high-resolution displays properly in Windows, especially Windows 10.  Be sure to have installed at least Java 11 (otherwise you'll have teeny tiny displays).  You'll still have font issues on 4K monitors.  I am told this can help dealing with it:

1. Find the java.exe file you installed.
2. Right-click and select "Properties"
3. Click on the "Compatibility" tab
4. Click on "Change high DPI settings"
5. Here you can try checking "Use this setting to fix scaling problems for this program instead of the one in Settings"
6. If Number 5 above isn't working, you might instead try "Override high DPI scaling behavior.  Scaling performed by: System (Enhanced)"

#### Java Preference Problems

Edisyn makes heavy use of Java preferences to store persistence information: what menu option you chose last time, what should be the default synth editor to pop up, and so on.  However there is a longstanding Java/Windows bug which makes Java preferences not work out of the box in Windows for earlier versions of Java.  I think this is fixed as of Java 11 but you should check and let me know.

#### Incorrect Jar Linkage to Java

When you double-click on a jar file, Windows may not launch Java properly because it has jar files associated with the wrong Java instance.  It's easy to fix this using [JarFix](https://johann.loefflmann.net/en/software/jarfix/index.html)

<a name="linux"/>

### Installation and Running on Linux

I'm told that Edisyn works fine if you have installed at least Java 8.  After this:

1. Download [Edisyn's jar file](https://cs.gmu.edu/~eclab/projects/edisyn/edisyn.jar).
2. You'll need to figure out how to make it so that double-clicking on the jar file launches it in java.  In Ubuntu, here's what you do: right-click on the jar file icon and choose "Properties".  Then select the "Open With" tab, and select your Java VM (for example "Open JDK Java 8 Runtime").  The press "Set as Default".  This makes the Java VM the default application to launch jar files.
3. Thereafter you should be able to just double-click on the file to launch Edisyn.

If you want to use Edisyn in combination with a DAW, see the manual's section on building a MIDI Loopback.

#### Problems with Jack

Edisyn (or more likely Java) seems to have compatibility problems with Jack; disabling Jack will often allow Edisyn to communicate with MIDI devies.  See issues #33 and #52.


### Running from the command line (OS X, Windows, Linux)

1. Make sure Java is installed.
2. Download [Edisyn's jar file](https://cs.gmu.edu/~eclab/projects/edisyn/edisyn.jar).
3. Run Edisyn as:   `java -jar edisyn.jar`


