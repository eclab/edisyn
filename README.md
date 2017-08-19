![Edisyn Splash Banner](https://raw.githubusercontent.com/eclab/edisyn/master/pics/Banner.png)

# Edisyn
Synthesizer Patch Editor (Version 10)

By Sean Luke (sean@cs.gmu.edu)


## About

Edisyn is a synthesizer patch editor library written in pure Java.   It runs on OS X, Linux, and (probably) Windows.  

Edisyn presently supports:

* Waldorf Blofeld and Waldorf Blofeld Keyboard (Single and Multi Modes)
* Waldorf Microwave II, XT, and XTk (Single and Multi Modes)
* Oberheim Matrix 1000
* PreenFM2
* Kawai K4 and Kawai K4r (Single, Multi, Drum, and Effect Modes)

Soon Edisyn will support:

* Yamaha TX81Z (Single and Multi Modes)

Edisyn has infinite levels of undo, CC and NRPN mapping and learning, offline modes, randomization/merging/nudging with per-parameter customization, real-time parameter updates, test notes, etc.  Edisyn is particularly strong in sound exploration tools.

## Manual

Edisyn has an [extensive manual](https://github.com/eclab/edisyn/raw/master/docs/manual/Edisyn.pdf) which describes how to run it, and (if you are so inclined) how to make new patch editors.

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


