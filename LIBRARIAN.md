# Librarian Support

NOTE: The Librarian is not present in the current build.  It'll show up in the coming build.

Edisyn's librarian is in its very early stages, and I can only test it on certain machines here and there.  If you'd like to assist in testing, I would be greatful -- just report errors on github or mail me directly (sean@cs.gmu.edu).  For instructions on how to test, see HOW TO TEST below.

Below are the various stages of implementation and testing of the librarian system for various editors.

## Patch Editor Testing Status

I'll update this list as I or others successfully test librarians and/or fix them.

### Moderately Tested

* DSI Prophet '08 / Mopho / Tetra
* Kawai K4
* Kawai K4 Effect
* Kawai K4 Multi
* Korg SG
* Korg SG Multi
* Korg Wavestation SR
* Korg Wavestation SR Performance
* M-Audio Venom
* M-Audio Venom Multi
* M-Audio Venom Arp
* Novation SL
* Oberheim Matrix 6/6R/1000
* Roland D110
* Roland D110 Multi
* Roland JV880
* Roland JV880 Multi
* Waldorf Blofeld
* Waldorf Blofeld Multi
* Waldorf Pulse 2
* Yamaha 4 Op
* Yamaha 4 Op Multi
* Yamaha FS1R
* Yamaha FS1R Multi
* Yamaha FS1R Fseq


### Untested

* Alesis D4/DM5
* Casio CZ
* DSI Tetra Combo
* Emu Morpheus
* Emu Morpheus Hyper
* Emu Morpheus Map
* Emu Proteus
* Kawai K5
* Korg MicroKorg
* Korg MicroKorg Vocoder
* Red Sound DarkStar
* Sequential Prophet Rev2
* Tuning
* Waldorf Kyra
* Waldorf Kyra Multi
* Waldorf Microwave II/XT/XTk
* Waldorf Microwave II/XT/XTk Multi
* Yamaha DX7
* Yamaha FB01
* Yamaha FB01 Multi

### Not Fully Coded

* Kawai K1
* Kawai K1 Multi

### Cannot Support Librarians 

* Generic
* Kawai K4 Drum
* Korg Microsampler
* Korg Volca
* Korg Wavestation SR Sequence
* M-Audio Venom Global
* Oberheim Matrix 1000 Global
* Preen FM2
* Roland JV880 Drum
* Waldorf Rocket

### Will Not Implement

* Yamaha TG33.
  * Patch sysex format is too nonstandard.

* Yamaha TG33 Multi.
  * Patch sysex format is too nonstandard.

* Novation Drumstation / D Station
  * The Drumstation does have an (undocumented) bulk format, but reading and writing to it is problematic as it requires the user to make manual changes.  I'm hesitant to support the librarian on it.


## Testing

A simple test regimen would be as follows:

* Connect to the synth with Edisyn

* Fire up the Editor

* Fire up the Librarian
   * Does it launch properly?

* Download all patches.
   * Does this take a weird amount of time?  Does it get stuck permanently?  If you have access to a terminal window, you might launch Edisyn as `java -jar edisyn.jar` and repeat.  Does anything get printed to the terminal?
   * Are the names right?
   * Are there any missing patches?

* Save all the patches
   * Does it save properly?  Does it hang? Does it take a strangely long time?

* Reload the saved patches in a new Librarian
   * Does that seem to work?
   * Does anything bad happen when you close the new librarian patch editor window to get rid of it?

* Double click on a few patch slots.
   * Does the data look correct?

* Go back to the Librarian.  Select a small range of patches.  Copy them to a new location, overwriting the originals.

* Select the patches you just overwrote.   Choose Write Selected Patches to write the new ones to the synthesizer.
   * A few synths (Yamaha DX7, Yamaha 4-op series, etc.) can only write entire banks.  You can do that instead.
   * Does it write to the synthesizer successfully?
   * Does it take an unusually long amount of time (in some cases it might but usually not)

* Delete the patches you had written to the synthesizer (select them and press BACKSPACE).

* Download the same patches you had just deleted.  
   * Do the new ones fill in the deleted slots?

* Some synths have a fast bank or all-patches download option.
   * Does this work?
   * Is it faster?



