The sysex files (.syx) in this directory are the dumps from four synthesizers:

	- E-Mu Planet Phatt
	- E-Mu Orbit V2
	- E-Mu Carnaval
	- E-Mu Vintage Keys Plus

... in response to the command

	F0 18 0A dd 0E F7	[dd is the device ID]

... which asks the synth to provide a mapping of its instruments to their
ROM offsets, which must be used in sysex patch dumps and parameter changes.
These dumps ultimately provide three pieces of information for each instrument:

	(1) The instrument number on the front panel
	(2) The internal ROM offset of the instrument
	(2) The instrument name

The accompanying .txt files are conversions of the .syx data into human-
readable format, and have one column for each of these three items.  These
were generated using the DumpInstruments.java program located in
edisyn/synths/emuplanetphatt/
