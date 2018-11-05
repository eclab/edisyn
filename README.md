![Edisyn Splash Banner](https://raw.githubusercontent.com/eclab/edisyn/master/pics/Banner.png)

# Edisyn
Synthesizer Patch Editor (Version 17)
 
By Sean Luke (sean@cs.gmu.edu)

I've been asked where my Patreon page is.  So, sure, here's my <a href="https://www.patreon.com/SeanLuke">Patreon page</a>.

Related projects:  

* [Flow](https://github.com/eclab/flow), a fully-modular, polyphonic, additive software synthesizer.
* [Gizmo](https://cs.gmu.edu/~sean/projects/gizmo/), an Arduino-based MIDI Swiss Army knife



## About

Edisyn is a synthesizer patch editor library written in pure Java.   It runs on OS X, Linux, and Windows.  

Edisyn is particularly good at exploring the space of patches.  It has to my knowledge the most sophisticated set of general-purpose patch-exploration tools of any patch editor available.

Edisyn presently supports:
 
* DSI Prophet '08 
* E-Mu Morpheus and Ultraproteus (Single, Hyperpreset, and MidiMap modes)
* Waldorf Blofeld and Waldorf Blofeld Keyboard (Single and Multi Modes)
* Waldorf Microwave II, XT, and XTk (Single and Multi Modes)
* Oberheim Matrix 1000
* PreenFM2
* Kawai K1, Kawai K1m, and Kawai K1r (Single and Multi Modes)
* Kawai K4 and Kawai K4r (Single, Multi, Drum, and Effect Modes)
* Kawai K5 and K5m
* Yamaha DX7 Family (Single)
* Yamaha TX81Z (Single and Multi Modes)
* Korg SG Rack (Single and Multi Modes) and Korg SG Pro X (Single Mode)
* Korg Microsampler
* Korg MicroKorg
* Korg Wavestation SR (Performance, Patch, and Wave Sequence Modes)

Edisyn has infinite levels of undo, CC and NRPN mapping and learning, offline modes, randomization, merging, nudging, hill-climbing, patch constriction, per-parameter customization, real-time parameter updates, test notes, etc.

## Manual

Edisyn has an [extensive manual](https://github.com/eclab/edisyn/raw/master/docs/manual/Edisyn.pdf) which describes how to run it, and (if you are so inclined) how to make new patch editors.

## Install and Run Edisyn

Edisyn is cross-platform and will run on a variety of platforms (Windows, Linux) but I am personally developing on and for OS X.  I'd appreciate feedback and screenshots of it running on Windows and Linux so I can tweak things.


### Installation and Running on OS X 

First install Edisyn from the [Edisyn.app.zip](https://github.com/eclab/edisyn/raw/master/install/Edisyn.app.zip) file located in the "install" directory.  Sadly, it's a whopping 70MB because it includes the Java VM.  :-(


Sierra has really locked down the ability to run an application that's not from a commercial, paying Apple Developer.  And I'm not one.  So you will have to instruct Sierra to permit Edisyn to run.

Let's assume you stuck Edisyn in the /Applications directory as usual.  Then:

1. Run the Terminal Program (in /Applications/Utilities/)
2. Type the following command and hit RETURN: `   sudo spctl --add /Applications/Edisyn.app`
4. Enter your password and hit RETURN.
5. Quit the Terminal Program

Now you should be able to run Edisyn.  Let me know if this all works.


### Installation and Running on Windows

I believe that the following should work:

1. [Download and install Java 11](http://www.oracle.com/technetwork/java/javase/downloads/index.html).  The JRE should work fine.

2. Download Edisyn's jar file, called [edisyn.jar](https://github.com/eclab/edisyn/raw/master/jar/edisyn.jar), presently located in the "jar" directory.

3. Double-click on edisyn.jar to launch Edisyn.

#### Important Note for Windows User

Java versions earlier than 11 (or so) do not handle high-resolution displays properly, so Edisyn will appear teeny-tiny.  You need to upgrade to 11.

Also Edisyn makes heavy use of Java preferences to store persistence information: what menu option you chose last time, what should be the default synth editor to pop up, and so on.  However there is a longstanding Java/Windows bug which makes Java preferences not work out of the box in Windows for earlier versions of Java.  I think this is fixed as of Java 11 but you should check and let me know.


### Installation and Running on Linux

I'm told that Edisyn works fine if you have installed *Java 8*.  After this:

1. Download [Edisyn's jar file](https://github.com/eclab/edisyn/raw/master/jar/edisyn.jar) located in the "jar" directory.

2. You'll need to figure out how to make it so that double-clicking on the jar file launches it in java.  In Ubuntu, here's what you do: right-click on the jar file icon and choose "Properties".  Then select the "Open With" tab, and select your Java VM (for example "Open JDK Java 8 Runtime").  The press "Set as Default".  This makes the Java VM the default application to launch jar files.

3. Thereafter you should be able to just double-click on the file to launch Edisyn.


### Running from the command line (OS X, Windows, Linux)

1. Make sure Java is installed.

2. Download [Edisyn's jar file](https://github.com/eclab/edisyn/raw/master/jar/edisyn.jar) located in the "jar" directory.

3. Run Edisyn as:   `java -jar edisyn.jar`


