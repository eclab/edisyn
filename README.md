![Edisyn Splash Banner](https://raw.githubusercontent.com/eclab/edisyn/master/pics/Banner.png)

# Edisyn
Synthesizer Patch Editor (Version 25)

By Sean Luke (sean@cs.gmu.edu)

Related projects:  

* [Flow](https://github.com/eclab/flow), a fully-modular, polyphonic, additive software synthesizer.
* [Gizmo](https://cs.gmu.edu/~sean/projects/gizmo/), an Arduino-based MIDI Swiss Army knife.
* [Computational Music Synthesis](https://cs.gmu.edu/~sean/book/synthesis/), an open-content book on building software synthesizers.

## Paper on Edisyn's Stochastic Patch Exploration Tools

[Paper Here](https://cs.gmu.edu/~sean/papers/evomusart19.pdf).  Presented at EvoMUSART 2019.

## Contributors

* Bryan Hoyle (collaboration on the Red Sound DarkStar and Microtuning editors)

## Donations

Donations are welcome via Paypal.

## About

Edisyn is a synthesizer patch editor library written in pure Java.   It runs on MacOS, Linux, and Windows.  

Edisyn is particularly good at exploring the space of patches.  It has to my knowledge the most sophisticated set of general-purpose patch-exploration tools of any patch editor available.

Edisyn presently supports:

* Alesis D4 and DM5 
* Casio CZ Series (CZ101, CZ1000, CZ3000, CZ5000, CZ-1)
* DSI Prophet '08, Tetra, Mopho, Mopho Keyboard, Mopho SE, and Mopho x4 (Single and (for Tetra) Combo modes)
* E-Mu Morpheus and Ultraproteus (Single, Hyperpreset, and MidiMap modes)
* E-Mu Proteus 1, 1XR, 2, 2XR, 3, 3XR, and 1+Orchestral
* Kawai K1, Kawai K1m, and Kawai K1r (Single and Multi Modes)
* Kawai K4 and Kawai K4r (Single, Multi, Drum, and Effect Modes)
* Kawai K5 and K5m (Single and Multi Modes, plus single-cycle wave uploading)
* Korg SG Rack (Single and Multi Modes) and Korg SG Pro X
* Korg MicroKorg (Single and Vocoder Modes)
* Korg Microsampler
* Korg Wavestation SR (Performance, Patch, and Wave Sequence Modes)
* Novation Drumstation and D-station
* Oberheim Matrix 6, 6R, and 1000 (Single and (for 1000) Global Modes) 
* PreenFM2
* Red Sound DarkStar and DarkStar XP2
* Roland D-110 (Tone and Multi Modes)
* Roland JV-80 and JV-880 (Single and Multi Modes)
* Waldorf Blofeld and Waldorf Blofeld Keyboard (Single and Multi Modes, plus Wavetable uploading)
* Waldorf Microwave II, XT, and XTk (Single and Multi Modes)
* Yamaha DX7 Family (DX7, TX7, TX802, TX216/TX816, Korg Volca FM, Dexed, DX200, DX9)
* Yamaha 4-Op FM Family (DX21, DX27, DX100, TX81Z, DX11, TQ5, YS100, YS200, B200, V50, etc.) (Single and (for TX81Z and DX11) Multi Modes)
* Yamaha FB01 (Single and Multi Modes)
* Yamaha FS1R (Voice, Performance, and Fseq Modes)
* Yamaha TG33, SY22, and SY35 (Single and (for TG33) Multi Modes)
* General CC, NRPN, and RPN editing
* Microtuning editing

Edisyn has infinite levels of undo, CC and NRPN mapping and learning, offline modes, randomization, merging, nudging, hill-climbing, patch constriction, per-parameter customization, real-time parameter updates, test notes and chords, etc.

#### "Could You Develop a Patch Editor for X?"

Sure!  But building a patch editor is a long-term commitment of debugging and maintenance.  So I only build editors for synths I own (I might sell them long after the editor is stable).  So if you'd like, say, the Alesis Andromeda to have an editor, all you have to do is gift me an Alesis Andromeda!

## Manual

Edisyn has an [extensive manual](https://cs.gmu.edu/~eclab/projects/edisyn/Edisyn.pdf) which describes how to run it, and (if you are so inclined) how to make new patch editors.

## Install and Run Edisyn

Edisyn is cross-platform and will run on a variety of platforms (Windows, Linux) but I am personally developing on and for MacOS.  I'd appreciate feedback and screenshots of it running on Windows and Linux so I can tweak things.


### Installation and Running on MacOS

First install Edisyn from the [Edisyn.app.zip](https://cs.gmu.edu/~eclab/projects/edisyn/Edisyn.app.zip) file.  Sadly, it's a whopping 70MB because it includes the Java VM.  :-(

MacOS has lately locked down the ability to run an application that's not from a commercial, paying Apple Developer.  And I'm not one.  So you will have to instruct MacOS to permit Edisyn to run.  Let's assume you stuck Edisyn in the /Applications directory as usual.  Now we have to tell Gatekeeper to allow Edisyn to run on your machine:

1. Run the Terminal Program (in /Applications/Utilities/)
2. Type the following command and hit RETURN: `   sudo spctl --add /Applications/Edisyn.app`
4. Enter your password and hit RETURN.
5. Quit the Terminal Program

Now you should be able to run Edisyn.  Let me know if this all works.

If you want to use Edisyn in combination with a DAW, see the manual's section on building a MIDI Loopback.

You can also run Edisyn from its jar file from the command line: see "Running from the command line" at end of these instructions. 

#### Dealing with Catalina...

Note that Catalina may have added additional requirements to running Edisyn properly: but I don't have a Catalina machine to test on.  Let me know if you run into issues and/or find solutions to them.


#### If Edisyn bombs in MacOS on selecting a patch editor...

You may have installed the MMJ (MIDI Java) library in the past, which is now obsolete.  This library was required by old applications but no longer works properly and should be removed.  Look in /Library/Java/Extensions/ or in \[Your Home Directory\]/Library/Java/Extensions for the files **mmj.jar** or **libmmj.jnilib**.  If you find them, remove them from the directory and try again.


### Installation and Running on Windows

I believe that the following should work:

1. [Download and install at least Java 11](http://www.oracle.com/technetwork/java/javase/downloads/index.html).  The JRE should work fine.

2. Download Edisyn's jar file, called [edisyn.jar](https://cs.gmu.edu/~eclab/projects/edisyn/edisyn.jar), presently located in the "jar" directory.

3. Double-click on edisyn.jar to launch Edisyn.

If you want to use Edisyn in combination with a DAW, see the manual's section on building a MIDI Loopback.

#### Dealing with High-Resolution Displays

Java doesn't handle high-resolution displays properly in Windows, especially Windows 10.  Be sure to have installed at least Java 11 (otherwise you'll have teeny tiny displays).  You'll still have font issues on 4K monitors.  I am told this can help dealing with it:

1. Find java.exe you installed.
2. Right-click and select "Properties"
3. Click on the "Compatibility" tab
4. Click on "Change high DPI settings"
5. Here you can try checking "Use this setting to fix scaling problems for this program instead of the one in Settings"
6. If #5 isn't working, you might instead try "Override high DPI scaling behavior.  Scaling performed by: System (Enhanced)"

#### Java Preference Problems

Edisyn makes heavy use of Java preferences to store persistence information: what menu option you chose last time, what should be the default synth editor to pop up, and so on.  However there is a longstanding Java/Windows bug which makes Java preferences not work out of the box in Windows for earlier versions of Java.  I think this is fixed as of Java 11 but you should check and let me know.

### Incorrect Jar Linkage to Java

When you double-click on a jar file, Windows may not launch Java properly because it has jar files associated with the wrong Java instance.  It's easy to fix this using [JarFix](https://johann.loefflmann.net/en/software/jarfix/index.html)

### Installation and Running on Linux

I'm told that Edisyn works fine if you have installed at least Java 8.  After this:

1. Download [Edisyn's jar file](https://cs.gmu.edu/~eclab/projects/edisyn/edisyn.jar) located in the "jar" directory.

2. You'll need to figure out how to make it so that double-clicking on the jar file launches it in java.  In Ubuntu, here's what you do: right-click on the jar file icon and choose "Properties".  Then select the "Open With" tab, and select your Java VM (for example "Open JDK Java 8 Runtime").  The press "Set as Default".  This makes the Java VM the default application to launch jar files.

3. Thereafter you should be able to just double-click on the file to launch Edisyn.

If you want to use Edisyn in combination with a DAW, see the manual's section on building a MIDI Loopback.



### Running from the command line (OS X, Windows, Linux)

1. Make sure Java is installed.

2. Download [Edisyn's jar file](https://cs.gmu.edu/~eclab/projects/edisyn/edisyn.jar) located in the "jar" directory.

3. Run Edisyn as:   `java -jar edisyn.jar`


