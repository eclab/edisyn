/***
    Copyright 2021 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.rolandu220;

import edisyn.*;
import edisyn.gui.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.border.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.sound.midi.*;
import edisyn.util.*;

/**
   A patch editor for the Roland U-220 (Drums).
   @author Sean Luke
*/

public class RolandU220Drum extends Synth
    {
    public static final String[] PCM_NAMES = 
        {
        "Internal",
        "01 Pipe Organ & Harpsichord",
        "02 Latin & FX Percussion",
        "03 Ethnic",
        "04 Electric Grand and Clavi",
        "05 Orchestral Strings",
        "06 Orchestral Winds",
        "07 Electric Guitar",
        "08 Synthesizer",
        "09 Guitar & Keyboards",
        "10 Rock Drums",
        "11 Sound Effects",
        "12 Sax & Trombone",
        "13 Super Strings",
        "14 Super Ac Guitar",
        "15 Super Brass"
        };

    public static final String[][] PCM =
        {
            {
            // U-220 INTERNAL
            "A. Piano 1", "A. Piano 2", "A. Piano 3", "A. Piano 4", "A. Piano 5", 
            "A. Piano 6", "A. Piano 7", "A. Piano 8", "A. Piano 9", "A. Piano 10", 
            "E. Piano 1", "E. Piano 2", "E. Piano 3", "E. Piano 4", "E. Piano 5", 
            "Bright EP 1", "Bright EP 2", "Vib 1", "Vib 2", "Vib 3", "Bell 1", "Bell 2", "Marimba", 
            "A. Guitar 1", "A. Guitar 2", "A. Guitar 3", "A. Guitar 4", "A. Guitar 5", 
            "E. Guitar 1", "E. Guitar 2", "E. Guitar 3", "E. Guitar 4", "Heavy EG 1", "Heavy EG 2", 
            "Slap 1", "Slap 2", "Slap 3", "Slap 4", "Slap 5", "Slap 6", 
            "Slap 7", "Slap 8", "Slap 9", "Slap 10", "Slap 11", "Slap 12", 
            "Fingered 1", "Fingered 2", "Picked 1", "Picked 2", "Fretless 1", "Fretless 2", "AC. Bass", 
            "Syn. Bass 1", "Syn. Bass 2", "Syn. Bass 3", "Syn. Bass 4", 
            "Syn. Bass 5", "Syn. Bass 6", "Syn. Bass 7", "Syn. Bass 8", 
            "Choir 1", "Choir 2", "Choir 3", "Choir 4", "Strings 1", "Strings 2", "Strings 3", "Strings 4", 
            "E. Organ 1", "E. Organ 2", "E. Organ 3", "E. Organ 4", "E. Organ 5", 
            "E. Organ 6", "E. Organ 7", "E. Organ 8", "E. Organ 9", "R. Organ 1", "R. Organ 2", 
            "Soft TP 1", "Soft TP 2", "TP / TRB 1", "TP / TRB 2", "TP / TRB 3", 
            "Sax 1", "Sax 2", "Sax 3", "Sax 4", "Sax 5", "Brass 1", "Flute 1", 
            "Shaku 1", "Shaku 2", "Fantasia", "Bell Pad", "Syn Choir", 
            "Breath Vox", "Syn. Vox 1", "Syn. Vox 2", "L. Calliope", "Calliope", 
            "Metal Hit", "Rich Brass", "JP. Brass 1", "JP. Brass 2", "Brass Strings", 
            "String Pad 1", "String Pad 2", "JP. Strings", "Pizzagogo", "Fanta Bell", 
            "Spect Bell", "Bell Drum", "Synth Harp", "Pulse Wave 1", "Pulse Wave 2", "Pulse Wave 3", 
            "Saw Wave 1", "Saw Wave 2", "Pizz", "Metal", "Breath", "Nails", "Spectrum 1", "Spectrum 2", "N. Dance", "Drums [SETUP]"
            },
            { 
            // SN-U110-01 - Pipe Organ and Harpsichord
            "Harpsichord 1", "Harpsichord 2", "Harpsichord 3", "Harpsichord 4", "Harpsichord 5", "Harpsichord 6",
            "Positive 1", "Positive 2", "Positive 3", "Positive 4", "Positive 5", "Positive 6", 
            "Church 1", "Church 2", "Church 3", "Church 4", "Church 5", "Church 6", "Church 7", "Church 8", "Church Reverb", 
            },
            { 
            // SN-U110-02 - Latin and FX Percussion
            "Latin 1 [SETUP]", "Latin 2 [SETUP]", "Latin 3 [SETUP]", "FX 1 [SETUP]", "FX 2 [SETUP]", "FX 3 [SETUP]", "FX 4 [SETUP]", 
            "Conga 1", "Conga 2", "Conga 3", "Bongo", "Claves", "Timbale", 
            "Tambourine", "Wood Block", "Whistle", "Triangle", "Belltree", 
            "Jingle Bell", "Vibraslap", "Castanet", "Maracas", "Agogo 1", "Agogo 2", 
            "Cuica 1", "Cuica 2", "Guiro 1", "Guiro 2", "Guiro 3", "Berimbau", 
            "Shekele", "Steel Drum", "Log Drum", "Orch Hit", "Siren", 
            "Type 1", "Type 2", "Clock", "Pinball", "Telephone", "Smsh Glass", 
            "Rezno", "Eerie", "Ambia Jr", "Templ Blk", "Zing!", "Boing!", 
            "Mod Zap", "Interface", "Scratch", "Stake", "Zappu"
            },
            {
            // SN-U110-03 - Ethnic
            "Tabla", "Tabla-Ga", "Tabla-Te", "Tabla-Na", "Tabla-Trkt", "Tabla-Tun", 
            "Tsuzumi 1", "Tsuzumi 2", "Tsuzumi 3", "Hyosigi", "Gender 1", "Gender 2", 
            "Sanza 1", "Sanza 2", "Barafon 1", "Barafon 2", "Barafon 3", "Barafon 4", 
            "Sitar 1", "Sitar 2", "Sitar 3", "Santur 1", "Santur 2", "Santur 3", 
            "Koto 1", "Koto 2", "Koto 3", "Koto 4", "Koto 5", "Koto 6", "Koto 7", "Koto 8", "Koto Tremo", 
            "Sicu 1", "Sicu 2", "Shanai 1", "Shanai 2", "Shanai 3"
            },
            {
            // SN-U110-04 - Electric Grand and Clavi
            "Electric Grand 1", "Electric Grand 2", "Electric Grand 3", "Electric Grand 4", 
            "Electric Grand 5", "Electric Grand 6", "Electric Grand 7", "Electric Grand 8", 
            "Clavichord 1", "Clavichord 2", "Clavichord 3", "Clavichord 4", 
            },
            {
            // SN-U110-05 - Orchestral Strings
            "Violin 1", "Violin 2", "Violin 3",
            "Cello 1", "Cello 2", "Cello 3", "Cello 4", "Cello / Violin", "Contrabass / Cello", "Pizzicato", 
            "Harp 1", "Harp 2", 
            },
            {
            // SN-U110-06 - Orchestral Winds
            "Oboe 1", "Oboe 2", "Oboe 3", "Oboe 4", "Oboe 5", "Oboe 6", 
            "Bassoon 1", "Bassoon 2", "Bassoon 3", "Bassoon 4", "Bassoon 5", 
            "Clarinet 1", "Clarinet 2", "Clarinet 3", "Clarinet 4", "Clarinet 5", "Clarinet 6", 
            "Bass Clarinet 1", "Bass Clarinet 2", "Bass Clarinet 3", "Bass Clarinet 4", "Bass Clarinet 5", 
            "French Horn 1", "French Horn 2", "French Horn 3", "French Horn 4", "French Horn 5", "French Horn 6", 
            "Tuba 1", "Tuba 2", "Tuba 3", "Tuba 4", "Tuba 5", 
            "Timpani 1", "Timpani 2"
            },
            {
            // SN-U110-07 - Electric Guitar
            "Jazz Guitar SW 1", "Jazz Guitar SW 2", "Jazz Guitar SW 3", "Jazz Guitar P", "Jazz Guitar F", 
            "Jazz Guitar DT P", "Jazz Guitar DT F", "Jazz Guitar OCT P1", "Jazz Guitar OCT P2", "Jazz Guitar OCT F1", 
            "Jazz Guitar OCT F2", "Jazz Guitar SW S/F", "Jazz Guitar COMP 1", "Jazz Guitar COMP 1", "Jazz Guitar COMP 1", 
            "Overdrive Guitar SW 1", "Overdrive Guitar SW 2", "Overdrive Guitar SW 3", "Overdrive Guitar SW 4", 
            "Overdrive Guitar SW 5", "Overdrive Guitar SW HM", "Overdrive Guitar P", "Overdrive Guitar F", 
            "Overdrive Guitar DT P", "Overdrive Guitar DT F", "Overdrive Guitar OCT P1", "Overdrive Guitar OCT P2", 
            "Overdrive Guitar OCT F1", "Overdrive Guitar OCT F2", "Overdrive Guitar SW S/F", "Overdrive Guitar FB 1", 
            "Overdrive Guitar FB 2", "Overdrive Guitar FB 3", "Overdrive Guitar FB 4", "Overdrive Guitar FB 5", 
            "Overdrive Guitar FB 6", "Overdrive Guitar FB 7", "Overdrive Guitar FB 8", "Overdrive Guitar FB 9", 
            "Overdrive Guitar FB 10", "Overdrive Guitar FB 11", "Overdrive Guitar FB 12", 
            "Distortion Guitar SW 1", "Distortion Guitar SW 2", "Distortion Guitar SW 3", "Distortion Guitar SW 4", 
            "Distortion Guitar SW 5", "Distortion Guitar SW HM", "Distortion Guitar P", "Distortion Guitar F", 
            "Distortion Guitar DT", "Distortion Guitar +4TH 1", "Distortion Guitar +4TH 2", "Distortion Guitar -5TH 1", 
            "Distortion Guitar -5TH 2", "Distortion Guitar OCT 1", "Distortion Guitar OCT 2", "Distortion Guitar SW S/F", 
            "Distortion Guitar FB 1", "Distortion Guitar FB 2", "Distortion Guitar FB 3", "Distortion Guitar FB 4", 
            "Distortion Guitar FB 5", "Distortion Guitar FB 6", "Distortion Guitar FB 7", "Distortion Guitar FB 8", 
            "Distortion Guitar FB 9", "Distortion Guitar FB 10", "Distortion Guitar FB 11", "Distortion Guitar FB 12", 
            "Picking Harmonics"
            },
            {
            // SN-U110-08 - Synthesizer
            // NOTE -- already available on the U-220 internal
            "Fantasia", "Bell Pad", "Syn Choir", "Breath Vox", "L. Calliope", "Calliope", 
            "Metal Hit", "Rich Brass", "Brass Strings", "String Pad 1", "String Pad 2", 
            "Pizzagogo", "Fanta Bell", "Spect Bell", "Bell Drum", "Synth Harp", 
            "Pulse Wave 1", "Pulse Wave 2", "Pulse Wave 3", "Saw Wave 1", "Saw Wave 2", 
            "Pizz", "Metal", "Breath", "Nails", "Spectrum 1", "Spectrum 2", "N. Dance"
            },
            {
            // SN-U110-09 - Guitar & Keyboards
            // NOTE -- already available on the U-220 internal
            "Bright EP 1", "Bright EP 2", "Syn. Vox 1", "Syn. Vox 2", 
            "Syn. Bass 4", "Syn. Bass 5", "Syn. Bass 6", "Syn. Bass 7", "Syn. Bass 8", 
            "Heavy EG 1", "Heavy EG 2", "JP. Strings", "JP. Brass 1", "JP. Brass 2", 
            "R. Organ 1", "R. Organ 2"
            },
            {
            // SN-U110-10 - Rock Drums
            "Rock Drums [SETUP]",
            "Electronic Drums [SETUP]"
            },
            {
            // SN-U110-11 - Sound Effects
            "Creaking", "Door", "Footsteps", "Waterphone", "S-Strings", 
            "Screaming", "Laughing", "Dog", "Wave", "Stream", "Bird", 
            "Drop", "Rain", "Thunder", "Car Door", "Car Stop", "Car Crash", 
            "Train", "Pistol", "Machine Gun", "Missile", "Explosion", 
            "Big Foot", "Godzilla", "Telephone Call", "Chime", "Applause", 
            "From Radio", "Bubble 1", "Bubble 2", "Toy", "Fantasy Hit", 
            "S-Set", "C-Set",
            },
            {
            // SN-U110-12 - Sax and Trombone
            // NOTE -- The tones labelled Trumpet/Trombone were originally labelled "TP/TRB",
            // which I *assume* is Trumpet and Trombone
            "Saxophone SW 1", "Saxophone SW 2", "Saxophone SW 3", "Saxophone SW 4", "Saxophone P 1", 
            "Saxophone P 2", "Saxophone P 3", "Saxophone MF 1", "Saxophone MF 2", "Saxophone FF", 
            "Trombone SW 1", "Trombone SW 2", "Trombone P", "Trombone MF", "Trombone FF", 
            "Trumpet/Trombone SW 1", "Trumpet/Trombone SW 2", "Trumpet/Trombone P", "Trumpet/Trombone MF", "Trumpet/Trombone FF",
            },
            {
            // SN-U110-13 - Super Strings (From JV-80)
            "Super Strings 1", "Super Strings 1L", "Super Strings 1R", 
            "Super Strings 2", "Super Strings 2L", "Super Strings 2R", 
            "Super Strings 3", "Super Strings 3L", "Super Strings 3R", 
            "Super Strings 4", "Super Strings 4L", "Super Strings 4R",
            },
            {
            // SN-U110-14 - Super Acoustic Guitar (From JV-80)
            "Steel Guitar 1", "Steel Soft", "Steel Hard", "Steel Guitar 2", "Steel (L)", "Steel (R)", 
            "Nylon Guitar 1", "Nylon Soft", "Nylon Hard", "Nylon Guitar 2", "Nylon (L)", "Nylon (R)", 
            "12-String Guitar 1", "12-String Guitar 2", "12-String Guitar 3", "12-String Guitar 4", "12-String Guitar 5", 
            "Harmonics", "Squeak",
            },
            {
            // SN-U110-15 - Super Brass (From JV-80)
            "High Brass 1", "High Brass 2", "High Brass SF", 
            "Low Brass 1", "Low Brass 2", "Low Brass SF", 
            "Brass Combo 1", "Brass Combo 1L", "Brass Combo 1R", 
            "Brass Combo 2", "Brass Combo 2L", "Brass Combo 2R", 
            "Brass Combo SF",
            },
        };

    public static final String[] INTERNAL_DRUMS = 
        {
        "Bass Drum 1", "Bass Drum 2", "Rim Shot", "Snare Drum 1", "Hand Clap", 
        "Snare Drum 2", "Low Tom Tom 1", "Closed High Hat 1", "Low Tom Tom 2", 
        "Open High Hat 2", "Mid Tom Tom 1", "Open High Hat 1", "Mid Tom Tom 2", 
        "Hi Tom Tom 1", "Crash Cymbal", "Hi Tom Tom 2", "Ride Cymbal", "China Cymbal", 
        "Cup (Mute)", "Tambourine [L]", "Splash Cymbal", "Cowbell", "Crash Cymbal", 
        "Snare Drum 3", "Ride Cymbal", "Bongo High [L]", "Bongo Low [L]", "Conga (Mute) [L]", 
        "Conga High [L]", "Conga Low [L]", "Timable High [L]", "Timable Low [L]", "Agaogo High [L]", 
        "Agaogo Low [L]", "Cabasa", "Maracas [L]", "Short Whistle [L]", "Long Whistle [L]", 
        "Vibra - Slap [L]", "Bell Tree [L]", "Claves [L]", "Guiro 2 [L]", "Guiro 1 [L]", "Castanets [L]", 
        "Hi Pitch Tom 2", "Triangle [L]", "Hi Pitch Tom 1", "Wood Block [L]", "Jingle Bell [L]", 
        "Bass Drum 3", "Bass Drum 4", "Snare Drum 4", "Snare Drum 5", "Snare Drum 6", 
        "Low Tom Tom 3", "Closed High Hat 2", "Mid Tom Tom 3", "China Cymbal", 
        "High Tom Tom 3", "Ride Cymbal", "Native Drum 1 [L]", "Native Drum 2 [L]", "Native Drum 3 [L]", 
        "--"
        };
        
    public static final String[][] ROCK_DRUMS =
        {
        // SN-U110-10 - Rock Drums
            {
            // Rock Drums
            "Bass Drum 1", "Bass Drum 2", "Rim Shot", "Snare Drum 1", "Stick", 
            "Snare Drum 2", "Low Tom Tom 1", "Closed High Hat 1", "Low Tom Tom 2", 
            "Half Open HH 1", "Middle Tom Tom 1", "Open High Hat 1", "Middle Tom Tom 2", 
            "High Tom Tom 1", "Crash Cymbal", "High Tom Tom 2", "Cup", "Gong", 
            "--", "--", "--", "--", "Crash Cymbal", "Snare Drum 3", 
            "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", 
            "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", 
            "High Pitch TT 2", "--", "High Pitch TT 1", "--", "--", 
            "Bass Drum 3", "Bass Drum 4", "Snare Drum 4", "Snare Drum 5", "Snare Drum 6", 
            "Low Tom Tom 3", "Closed High Hat 1", "Middle Tom Tom 3", "Crash Cymbal", 
            "High Tom Tom 3", "Cup", "--",
            // These three are not part of the card but I need them to round out to 98
            "--", "--", "--",
            },
            {
            // Electronic Drums
            "Bass Drum 5", "Bass Drum 6", "Rim Shot", "Snare Drum 7", 
            "Chimes", "Snare Drum 8", "LowTom Tom 4", "Closed High Hat 2", 
            "Low Tom Tom 5", "Closed High Hat 2", "Middle Tom Tom 4", 
            "Open High Hat 2", "Middle Tom Tom 5", "High Tom Tom 4", 
            "Crash Cymbal", "High Tom Tom 5", "Ride Cymbal", 
            "--", "--", "--", "--", "--", "--", "--", "--", "--", 
            "--", "--", "--", "--", "--", "--", "--", "--", "--", 
            "--", "--", "--", "--", "--", "--", "--", "--", "--", 
            "--", "--", "--", "--", "--", "--", "--", "--", "--", 
            "--", "--", "--", "--", "--", "--", "--", "--",
            // These three are not part of the card but I need them to round out to 98
            "--", "--", "--",
            }
        };

    public static final String[][] LATIN_DRUMS =
        {
        // SN-U110-02 - Latin and FX Percussions
            {
            // Latin 1
            "Timbale-LMT1", "Timbale-LMT2", "Conga-MT/H", "Timbale-H/L1", "Bell Tree/L", 
            "Timbale-1-1/L2", "Timbale-L/L1", "Maracas/H", "Timbale-H/L5", "Tambourine/H", 
            "Timbale-L/L2", "Tambourine/L1", "Timbale-H/L4", "Timbale-L/L3", "Whistle/L", 
            "Timbale-H/L3", "Triangle/L", "Agogo-L/H", "Agogo-H/L", "Tambourine", 
            "Vibra-slap/H", "Claves/H", "Vibra-slap/L", "Timbale-H/H", "Triangle/H", 
            "Bongo-H", "Bongo-L", "Conga (mute)", "Conga-H", "Conga-L", 
            "Timbale-H/M", "Timbale-L/M", "Agogo-H/M", "Agogo-L/M", "Maracas/H", 
            "Maracas", "Short Whistle", "Long Whistle", "Vibra-slap", "Bell Tree", 
            "Claves", "Cuica-H/H", "Cuica-H/L", "Castanets", "Bongo-H/L", "Triangle", 
            "Bongo-L/L", "Wood Block", "Jingle Bell", "Timbale-LMT3", "Timbale-LMT4", 
            "Timbale-L/H1", "Timbale-L/H2", "Timbale-L/H3", "Congo-L/L3", "Maracas/H2", 
            "Conga-L/L2", "Tambourine/L2", "Conga-L/L1", "Jingle Bell/H", "Native Drum-1", 
            "Native Drum-2", "Native Drum-3", 
            // This one is not part of the card but I need it to round out to 98
            "--",
            },
            {
            // Latin 2
            "--", "--", "--", "--", "--", "--", "--", 
            "--", "--", "--", "--", "--", "--", "--", 
            "--", "--", "--", "--", "--", "Tambourine", 
            "--", "--", "--", "--", "--", 
            "Bongo-H", "Bongo-L", "Conga (mute)", "Conga-H", 
            "Conga-L", "Timbale-H", "Timbale-L", "Agogo-H", 
            "Agogo-L", "--", "Maracas", "Short Whistle", 
            "Long Whistle", "Vibra-slap", "Bell Tree", "Claves",
            "-- ", "--", "Castanets", "--", "Triangle", 
            "--", "Wood Block", "Jingle Bell", "--", "--",
            "--", "--", "--", "-- ", "--", "--", "-- ", 
            "--", "--", "Native Drum-1", "Native Drum-2", "Native Drum-3",
            // This one is not part of the card but I need it to round out to 98
            "--",
            },
            {
            // Latin 3
			"--", "--", "--", "--", "--", 
			"--", "--", "--", "--", "--", 
			"--", "--", "--", "--", "--", 
			"--", "--", "--", "Berimbau", 
			"--", "5th Agogo", "--", "Guiro Short", 
			"Shekele", "Guiro Long", "--", 
			"--", "--", "--", "--", "--", 
			"--", "--", "--", "--", "--", 
			"--", "--", "--", "--", "--", 
			"Cuica-H", "Cuica-L", "--", 
			"Slit Drum-H", "--", "Slit Drum-L", 
			"--", "--", "Steel Drum-H", 
			"Steel Drum-L", "--", "--", 
			"--", "--", "--", "--", "--", 
			"--", "--", "--", "--", "--",
			"--"            
            },
            {
            // FX 1
            "Stake-L", "Stake-L", "Stake-L", "Stake-L", "Stake-L", "Eerie", 
            "Stake-L", "Stake-L", "Eerie", "Boing1-L", "Zap Up-L2", "Boing1-M", 
            "Zap Up L1", "Zap Up-M", "Boing1-H", "Mod Zap-L2", "Rezno-M", 
            "Mod Zap-L1", "Mod Zap-M", "Rezno-H", "Ambiance-M", "Pinball-L", 
            "Ambiance-H1", "Pinball-M", "Ambiance-H2", "Orchestra Hit", 
            "Orchestra Hit", "Orchestra Hit", "Orchestra Hit", "Orchestra Hit", 
            "Orchestra Hit", "Orchestra Hit", "Orchestra Hit", "Orchestra Hit", 
            "Orchestra Hit", "Orchestra Hit", "Orchestra Hit", "Orchestra Hit", 
            "Temple Block", "Interface-L", "Interface-M", "Interlace-H", "Scratch-M", 
            "Zing!-L", "Scratch-H", "--", "Telephone-M", "Zing!-H", "Telephone-H", 
            "Typewriter 1-L2", "Typewriter 2-M", "Typewriter 1-L1", "Typewriter 2-H", 
            "Typewriter 1-M", "Siren-L", "Clock-L", "Siren-M", "Clock-M", "Siren-H", 
            "Clock-H", "Smash Glass-L", "Smash Glass-M", "Smash Glass-H", 
            // This one is not part of the card but I need it to round out to 98
            "--",
            },
            {
            // FX 2
            "--", "--", "--", "--", "--", "--", 
            "--", "--", "--", "--", "--", "--", 
            "--", "--", "--", "--", "--", "--", "--", 
            "Siren", "--", "--", "--", "--", "--", 
            "Pinball", "Temple Block", "Ambiance Jr", 
            "Zap Up", "Mod Zap", "Orchestra Hit", 
            "Stake", "Boing!", "Typewriter 1", "Rezno", 
            "Typewriter 2", "Scratch", "Interface", 
            "Boing!-L", "Siren-H", "Smash Glass", "Eerie", 
            "Clock-H", "Clock-M", "--", "Clock-L", "--", 
            "Zing!", "Telephone", "--", "--", "--", 
            "--", "-- ", "--", "--", "--", "--", 
            "--", "--", "--", "--", "--", 
            // This one is not part of the card but I need it to round out to 98
            "--",
            },
            {
            // FX 3
            "--", "--", "--", "--", "--", "--", 
            "--", "--", "--", "--", "--", "--", 
            "--", "--", "--", "--", "--", "--", "--", 
            "Siren", "--", "--", "--", "--", "--", 
            "Pinball-M", "Pinball-L", "Stake-H", "Stake-M", "Orchestra Hit-L", 
            "Orchestra Hit-M", "Stake-L", "Typewriter 1-H", "Typewriter 1-M", 
            "Typewriter 1-L", "Typewriter 2", "Scratch-H", "Scratch-L", "Boing!", 
            "Temple Block", "Smash Glass-H", "Smash Glass-L", "Clock-H", 
            "Clock-M", "--", "Clock-L", "--", "Telephone-H", "Telephone-M", 
            "--", "--", "--", "--", "-- ", "--", 
            "--", "--", "--", "--", "--", "--", "--", "--", 
            // This one is not part of the card but I need it to round out to 98
            "--",
            },              
            {
            // FX 4
            "--", "--", "--", "--", "--", "--", 
            "--", "--", "--", "--", "--", "--", 
            "--", "--", "--", "--", "--", "--", "--", 
            "Siren-M", "--", "--", "--", "--", "--", 
            "Siren-L", "Temple Block", "Ambiance Jr-M", "Zap Up-M", 
            "Mod Zap-M", "Zap Up-L", "Ambiance Jr-L", "Boing!-H", 
            "Interface-H", "Rezno-H", "Rezno-M", "Interface-M", 
            "Interface-L", "Boing!-M", "Siren-H", "Boing!-L", 
            "Eerie-H", "Eerie-M", "Mod Zap-H", "--", "Zing!-H", 
            "--", "Zing!-M", "Eerie-L", "--", "--", "--", "--", 
            "-- ", "--", "--", "--", "--", "--", "--", "--", 
            "--", "--", 
            // This one is not part of the card but I need it to round out to 98
            "--",
            }                       
        };
        
    public static final int[] LOWER_BEND_RANGES = { -36, -24, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0 };         
    public static final int[] SENSITIVITY_RANGES = { -36, -24, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };         
    public static final String[] NOTES = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    public String[] OUTPUT_ASSIGNS = { "Dry", "Reverb", "Chorus", "Dir 1" };
    
    public RolandU220Drum()
        {
        for(int i = 0; i < allCommonParameters.length; i++)
            {
            allCommonParametersToIndex.put(allCommonParameters[i], Integer.valueOf(i));
            }

        for(int i = 0; i < allDrumParameters.length; i++)
            {
            allDrumParametersToIndex.put(allDrumParameters[i], Integer.valueOf(i));
            }

        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addCommon(Style.COLOR_C()));
        vbox.add(hbox);
        vbox.add(addKeys(Style.COLOR_B()));

/// We're using the keyboard, instead of (below) a scrolling region of all the key categories

/*
  final JComponent typical = addDrum(0, Style.COLOR_B());
  final int h = typical.getPreferredSize().height;
  final int w = typical.getPreferredSize().width;

  VBox svbox = new VBox()
  {
  public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
  {
  if (orientation == SwingConstants.VERTICAL)
  return w;
  else
  return h;
  }

  public Dimension getPreferredScrollableViewportSize()
  {
  Dimension size = getPreferredSize();
  size.height = h * 3;
  return size;
  }
  };
                
  JScrollPane pane = new JScrollPane(svbox, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
  pane.getViewport().setBackground(Style.BACKGROUND_COLOR());
  pane.setBorder(null);

  for(int drum = 0; drum < 64; drum++)
  {
  svbox.add(addDrum(drum, (drum % 2 == 1 ? Style.COLOR_B() : Style.COLOR_A())));
  }

  Category drums = new Category(this, "Drums", Style.COLOR_B());
  drums.add(pane);
        
  vbox.addLast(drums);
*/

        vbox.add(drumDisplay);
        soundPanel.add(vbox);

        addTab("Drum", soundPanel);
        
        loadDefaults();
        }
        


    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, getSynthName(), color);
        //globalCategory.makeUnresettable();
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                
        VBox vbox = new VBox();
        comp = new PatchDisplay(this, 8);
        vbox.add(comp);

        comp = new StringComponent("Patch Name", this, "name", MAXIMUM_NAME_LENGTH, "Name must be up to 12 ASCII characters.")
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
        hbox.add(vbox);

        // Not enough space to show the title
        hbox.addLast(Strut.makeHorizontalStrut(100));

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }


    public JComponent addCommon(Color color)
        {
        Category category = new Category(this, "Common", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        // FIXME: docs say the range is 0...15 but there are only 15 elements
        comp = new LabelledDial("Bend Range", this, "bendrangelower", color, 0, 14)
            {
            public double getStartAngle() { return 240; }
            public String map(int val)
                {
                return "" + LOWER_BEND_RANGES[val];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Lower");
        hbox.add(comp);
        
        comp = new LabelledDial("Bend Range", this, "bendrangeupper", color, 0, 12);
        ((LabelledDial)comp).addAdditionalLabel("Upper");
        hbox.add(comp);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

        
    public VBox buildKey(final int key, Color color)
        {
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        
        vbox.add(addDrum(key, Style.COLOR_A()));
                
        return vbox;
        }


    VBox[] keys = new VBox[64];
    VBox drumDisplay = new VBox();


    public JComponent addKeys(Color color)
        {
        final Category category = new Category(this, "Drum Key", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        for(int i = 0; i < keys.length; i++)
            {
            keys[i] = buildKey(i, color);
            }
                
        comp = new LabelledDial("Note", this, "note", color, 35, 98)
            {
            public String map(int val)
                {
                return NOTES[val % 12] + ((val / 12) - 1);  // note integer division
                }
            };
        hbox.add(comp);
        model.setStatus("note", Model.STATUS_RESTRICTED);
        
        comp = new KeyDisplay("Note", this, "note", color, 35, 98, 0)
            {
            public void userPressed(int key)
                {
                doSendTestNote(key, false);
                }
            };
        ((KeyDisplay)comp).setDynamicUpdate(true);
        ((KeyDisplay)comp).setOctavesBelowZero(KeyDisplay.OCTAVES_BELOW_ZERO_SPN);
        hbox.add(comp);
                                
        model.register("note", new Updatable()
            {
            public void update(String key, Model model)
                {
                drumDisplay.removeAll();
                drumDisplay.add(keys[model.get(key, 35) - 35]);
                drumDisplay.revalidate();
                drumDisplay.repaint();
                }
            });

        // set it once                  
        model.set("note", model.get("note", 35));
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addDrum(int note, Color color)
        {
        Category category = new Category(this, NOTES[(note + 35) % 12] + ((note + 35) / 12 - 1), color);
        category.makePasteable("drum");

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        
        final JLabel[] rhythmKey = new JLabel[1];
        final LabelledDial keyDial = new LabelledDial("            Source            ", this, "drum" + note + "sourcekey", color, 0, 127)
            {
            public int getDefaultValue() 
            	{
				int media = model.get("drum" + note + "media");
				int n = model.get("drum" + note + "number", 0);

            	// For Benjamin Wild (dj@benjaminwild.com): double-clicking the source should jump to the lowest drum note
            	if (media == 0) // internal
            		{
            		if (n == 127)	// Drum set
            			return 35;
            		else return 0;
            		}
            	else if (media == 2)	// Latin and FX
            		{
            		if (n == 0)	// Latin 1
            			return 35;
            		else if (n == 1) // Latin 2
            			return 54;
            		else if (n == 2) // Latin 3
            			return 53;
            		else if (n == 3) // FX 1
            			return 35;
            		else if (n == 4) // FX 2
            			return 54;
            		else if (n == 5) // FX 3
            			return 54;
            		else if (n == 6) // FX 4
            			return 54;
            		else return 0;
            		}
            	else if (media == 10)	// Rock Drums
            		{
            		return 35;
            		}
            	else
            		{
            		return 0;
            		}
            	}

            public String map(int val)
            	{
				return "<html><center>" + val + "<br><font size=-2>" + NOTES[val % 12] + ((val / 12) - 1) + "</font></center></html>";
            	}
            	
            public void update(String key, Model model)
                {
                super.update(key, model);
                if (rhythmKey[0] != null) 
                    {
                    int media = model.get("drum" + note + "media");
                    int n = model.get("drum" + note + "number", 0);
                   	if (media == 0) // internal 
                        {
                        int k = model.get(key, 0);
                        if (k >= 35 && k <= 98 && n == 127)
                            rhythmKey[0].setText(INTERNAL_DRUMS[k - 35]);
                        else 
                            rhythmKey[0].setText("--");
                        }
                    else if (media == 2)    // Latin and FX
                        {
                        int k = model.get(key, 0);
						// FIXME: here we have a problem similar to the proteus 2000 -- I think the drum
						// note number can be a value outside of 0 or 1 because the media has changed
						// but the number has not yet. This happens sometimes when randomizing.
						// I have a hack here but we need to revisit if this will be a problem for mutation 
                        if (k >= 35 && k <= 98 && (n >= 0 && n <= 6))
                            {
                            rhythmKey[0].setText(LATIN_DRUMS[n][k - 35]);
                            }
                        else 
                            rhythmKey[0].setText("--");
                        }
                    else if (media == 10)   // Rock Drums 
                        {
                        int k = model.get(key, 0);
						// FIXME: here we have a problem similar to the proteus 2000 -- I think the drum
						// note number can be a value outside of 0 or 1 because the media has changed
						// but the number has not yet. This happens sometimes when randomizing.
						// I have a hack here but we need to revisit if this will be a problem for mutation 
                        if (k >= 35 && k <= 98 & (n >= 0 && n <= 1))
                            {
                            rhythmKey[0].setText(ROCK_DRUMS[n][k - 35]);
                            }
                        else 
                            rhythmKey[0].setText("--");
                        }
                    else
                        rhythmKey[0].setText("--");
                    }
                }
            };
        rhythmKey[0] = ((LabelledDial)keyDial).addAdditionalLabel("--");


        params = PCM[0];
        Chooser pcm = new Chooser("Tone", this, "drum" + note + "number", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                keyDial.update("drum" + note + "sourcekey", model);
                model.setLastKey(key);		// we set it again so that it's the last thing updated, so we can distribute it
                }
            };
        
        params = PCM_NAMES;
        comp = new Chooser("Tone Medium", this, "drum" + note + "media", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                int val = model.get(key, 0);
                int cur = model.get("drum" + note + "number", 0);
                if (val >= 0 && val < PCM.length)               // just in case
                    pcm.setElements(PCM[val]);
                if (cur >= PCM[val].length)
                    model.set("drum" + note + "number", 0);
                keyDial.update("drum" + note + "sourcekey", model);
                model.setLastKey(key);		// we set it again so that it's the last thing updated, so we can distribute it
                }
            };
        vbox.add(comp);        
        vbox.add(pcm);

        params = OUTPUT_ASSIGNS;
        comp = new Chooser("Output Assign", this, "drum" + note + "outputassign", params);
        vbox.add(comp);

        comp = new CheckBox("Envelope Sustain", this, "drum" + note + "envmode", true); // flipped
        vbox.add(comp);

        hbox.add(vbox);

        VBox outer = new VBox();
        HBox inner = new HBox();

        inner.add(keyDial);
                
        comp = new LabelledDial("Mute", this, "drum" + note + "muteinst", color, 34, 98)
            {
            public String map(int key)
                {
                if (key == 34) return "Off";
                else return (NOTES[key % 12] + (key / 12 - 1));
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Note");
        inner.add(comp);

        comp = new LabelledDial("Level", this, "drum" + note + "instlevel", color, 0, 31)
			{
            public int getDefaultValue() { return 31; }
			};
        inner.add(comp);

        // FIXME: This is backwards!  0...6 are RIGHT and 8...14 are LEFT, and 15 is RND
        comp = new LabelledDial("Pan", this, "drum" + note + "pan", color, 0, 15)
            {
            public boolean isSymmetric() { return true; }
            public int getDefaultValue() { return 7; }
            public String map(int value)
                {
                if (value < 6) return (7 - value) + " >";
                else if (value == 7) return "--";
                else if (value < 15) return "< " + (value - 7);
                else return "Rnd";
                }
            };
        inner.add(comp);
        
        comp = new LabelledDial("Velocity", this, "drum" + note + "velocitysens", color, 1, 15)
            {
            public boolean isSymmetric() { return true; }
            public String map(int value)
                {
                return "" + (value - 8);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Sensitivity");
        inner.add(comp);

        // FIXME: docs say the range is 0...27 but there are only 27 elements
        comp = new LabelledDial("Pitch Shift", this, "drum" + note + "pitchshiftcoarse", color, 0, 26)
            {
            public int getDefaultValue() { return 14; }
            public double getStartAngle() { return 240; }
            public String map(int val)
                {
                return "" + SENSITIVITY_RANGES[val];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Coarse");
        inner.add(comp);

        comp = new LabelledDial("Pitch Shift", this, "drum" + note + "pitchshiftfine", color, 14, 114)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                return "" + (val - 64);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Fine");
        inner.add(comp);
        
        comp = new LabelledDial("Detune", this, "drum" + note + "detunedepth", color, 0, 15);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        inner.add(comp);

        outer.add(inner);
        inner = new HBox();

        comp = new LabelledDial("Env Attack", this, "drum" + note + "envattackrate", color, 1, 15)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                return "" + (val - 8);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        inner.add(comp);

        comp = new LabelledDial("Env Decay", this, "drum" + note + "envdecayrate", color, 1, 15)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                return "" + (val - 8);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        inner.add(comp);

        comp = new LabelledDial("Env Release", this, "drum" + note + "envreleaserate", color, 1, 15)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                return "" + (val - 8);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        inner.add(comp);

        // FIXME: docs say the range is 0...27 but there are only 27 elements
        comp = new LabelledDial("Pitch Channel", this, "drum" + note + "pitchpressuresens", color, 0, 26)
            {
            public int getDefaultValue() { return 14; }
            public double getStartAngle() { return 240; }
            public String map(int val)
                {
                return "" + SENSITIVITY_RANGES[val];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Pressure Mod");
        inner.add(comp);

        // FIXME: docs say the range is 0...27 but there are only 27 elements
        comp = new LabelledDial("Pitch Poly", this, "drum" + note + "pitchpolysens", color, 0, 26)
            {
            public int getDefaultValue() { return 14; }
            public double getStartAngle() { return 240; }
            public String map(int val)
                {
                return "" + SENSITIVITY_RANGES[val];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Pressure Mod");
        inner.add(comp);

        comp = new LabelledDial("Pitch", this, "drum" + note + "random", color, 0, 15);
        ((LabelledDial)comp).addAdditionalLabel("Randomize");
        inner.add(comp);
        
        // FIXME: docs say the range is 0...27 but there are only 27 elements
        comp = new LabelledDial("Auto Bend", this, "drum" + note + "autobenddepth", color, 0, 26)
            {
            public int getDefaultValue() { return 14; }
            public double getStartAngle() { return 240; }               // 240?
            public String map(int val)
                {
                return "" + SENSITIVITY_RANGES[val];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        inner.add(comp);

        comp = new LabelledDial("Auto Bend", this, "drum" + note + "autobendrate", color, 0, 15);
        ((LabelledDial)comp).addAdditionalLabel("Rate");
        inner.add(comp);
        
        outer.add(inner);
        hbox.addLast(outer);
                        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

                
    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        int original = model.get("number");
                
        JTextField number = new SelectedTextField("" + (original + 1), 3);

        while(true)
            {
            boolean result = showMultiOption(this, new String[] { "Patch Number"}, 
                new JComponent[] { number }, title, "Enter Patch number");
                
            if (result == false)
                return false;
                                
            int n;
            try { n = Integer.parseInt(number.getText()); }
            catch (NumberFormatException e)
                {
                showSimpleError(title, "The Patch Number must be an integer 1...4");
                continue;
                }
            if (n < 1 || n > 4)
                {
                showSimpleError(title, "The Patch Number must be an integer 1...4");
                continue;
                }
                        
            change.set("number", n - 1);
            return true;
            }
        }
        
    byte[] parseData = new byte[1568];

    public int parse(byte[] data, boolean fromFile)
        {
        if (numSysexMessages(data) > 1)
            {
            int result = PARSE_FAILED;
            byte[][] d = cutUpSysex(data);
            for(int i = 0; i < d.length; i++)
                {
                result = parse(d[i], fromFile);
                if (result != PARSE_INCOMPLETE) break;
                }
            return result;
            }
                
        // What is the tone patch number?
        int AA = data[5];
        int BB = data[6];
        int CC = data[7];
        
        int parseDataPosition = (AA == 0x00 ? ((BB - 0x20) * 128 + CC) % 1568 : (BB * 128 + CC) % 1568);
        
        if (parseDataPosition == 0)
            {
            for(int x = 0; x < parseData.length; x++)
                parseData[x] = 0;
            }
                
        System.arraycopy(data, 8, parseData, parseDataPosition, Math.min(parseData.length - parseDataPosition, 128));
        if (parseDataPosition + 128 >= 1568)    // last position
            {
            /*
              if (data.length == 138)         // uh oh, it's a dense bank patch, we can't handle those
              {
              return PARSE_FAILED;
              }
            */

            if (AA == 0x05)         // Write to Drum banks
                {
                model.set("number", (BB * 128 + CC) / 1568);
                }
                
            // The U-220 is entirely byte-packed :-(  So we have to do this by hand.

            int pos = 0;
            String name = "";
            for(int i = 0; i < 12; i++)
                {
                int lsb = parseData[pos++];
                int msb = parseData[pos++];
                name = name + (char)(lsb | (msb << 4));
                }
            model.set("name", name);

            int lsb1 = parseData[pos++];
            int msb1 = parseData[pos++];
            int lsb2 = parseData[pos++];
            int msb2 = parseData[pos++];
            int val = lsb1 | (msb1 << 4) | (lsb2 << 8) | (msb2 << 12);
            model.set("bendrangeupper", (val >>> 0x5) & 15);                // 5 6 7 8
            model.set("bendrangelower", (val >>> 0x0) & 31);                // 0 1 2 3 4

            for(int i = 0; i < 64; i++)
                {
                lsb1 = parseData[pos++];
                msb1 = parseData[pos++];
                lsb2 = parseData[pos++];
                msb2 = parseData[pos++];
                val = lsb1 | (msb1 << 4) | (lsb2 << 8) | (msb2 << 12);
                model.set("drum" + i + "detunedepth", (val >>> 0xC) & 15);      // C D E F
                model.set("drum" + i + "media", (val >>> 0x7) & 31);                    // 7 8 9 A B
                //System.err.println("Updated Media " + i + " to " + model.get("drum" + i + "media"));
                // we need to make sure we load the number AFTER the medium
                model.set("drum" + i + "number", (val >>> 0x0) & 127);          // 0 1 2 3 4 5 6
                //System.err.println("Updated Number " + i + " to " + model.get("drum" + i + "number"));

                lsb1 = parseData[pos++];
                msb1 = parseData[pos++];
                lsb2 = parseData[pos++];
                msb2 = parseData[pos++];
                val = lsb1 | (msb1 << 4) | (lsb2 << 8) | (msb2 << 12);
                model.set("drum" + i + "autobendrate", (val >>> 0xC) & 15);                     // C D E F
                model.set("drum" + i + "pitchshiftcoarse", (val >>> 0x7) & 31);         // 8 9 A B
                model.set("drum" + i + "pitchshiftfine", (val >>> 0x0) & 127);          // 0 1 2 3

                lsb1 = parseData[pos++];
                msb1 = parseData[pos++];
                lsb2 = parseData[pos++];
                msb2 = parseData[pos++];
                val = lsb1 | (msb1 << 4) | (lsb2 << 8) | (msb2 << 12);
                model.set("drum" + i + "envmode", (val >>> 0xF) & 1);                           // F
                model.set("drum" + i + "pitchpressuresens", (val >>> 0xA) & 31);                // A B C D E
                model.set("drum" + i + "pitchpolysens", (val >>> 0x5) & 31);                    // 5 6 7 8 9
                model.set("drum" + i + "autobenddepth", (val >>> 0x0) & 31);                    // 0 1 2 3 4

                lsb1 = parseData[pos++];
                msb1 = parseData[pos++];
                lsb2 = parseData[pos++];
                msb2 = parseData[pos++];
                val = lsb1 | (msb1 << 4) | (lsb2 << 8) | (msb2 << 12);
                model.set("drum" + i + "envreleaserate", (val >>> 0xC) & 15);           // C D E F
                model.set("drum" + i + "envdecayrate", (val >>> 0x8) & 15);                     // 8 9 A B
                model.set("drum" + i + "envattackrate", (val >>> 0x4) & 15);            // 4 5 6 7
                model.set("drum" + i + "velocitysens", (val >>> 0x0) & 15);                     // 0 1 2 3

                lsb1 = parseData[pos++];
                msb1 = parseData[pos++];
                lsb2 = parseData[pos++];
                msb2 = parseData[pos++];
                val = lsb1 | (msb1 << 4) | (lsb2 << 8) | (msb2 << 12);
                model.set("drum" + i + "muteinst", (val >>> 0x8) & 127);                                // 8 9 A B C D E
                model.set("drum" + i + "sourcekey", (val >>> 0x0) & 127);                       // 0 1 2 3 4 5 6

                lsb1 = parseData[pos++];
                msb1 = parseData[pos++];
                lsb2 = parseData[pos++];
                msb2 = parseData[pos++];
                val = lsb1 | (msb1 << 4) | (lsb2 << 8) | (msb2 << 12);
                model.set("drum" + i + "random", (val >>> 0xB) & 15);                           // B C D E
                model.set("drum" + i + "outputassign", (val >>> 0x9) & 3);                      // 9 A
                model.set("drum" + i + "instlevel", (val >>> 0x4) & 31);                                // 4 5 6 7 8
                model.set("drum" + i + "pan", (val >>> 0x0) & 15);                                      // 0 1 2 3
                }
            revise();
            return PARSE_SUCCEEDED;
            }
        else return PARSE_INCOMPLETE;
        }

    HashMap allCommonParametersToIndex = new HashMap();
        
    final static String[] allCommonParameters = new String[]
    {
    // Name is 12 bytes but they're broken into two nibbles each
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "-",
    "bendrangelower",
    "bendrangeupper",
    };


    HashMap allDrumParametersToIndex = new HashMap();
        
    final static String[] allDrumParameters = new String[]
    {
    "media",
    "number",
    "sourcekey",
    "muteinst",
    "instlevel",
    "velocitysens",
    "envmode",
    "envattackrate",
    "envdecayrate",
    "envreleaserate",
    "pitchshiftcoarse",
    "pitchshiftfine",
    "pitchpressuresens",
    "pitchpolysens",
    "random",
    "autobenddepth",
    "autobendrate",
    "detunedepth",
    "outputassign",
    "pan",
    };



    public static String getSynthName() { return "Roland U-20 / 220 [Drum]"; }
    
    String defaultResourceFileName = null;
    public String getDefaultResourceFileName() 
        {
        // See the Menu (preset options)
        if (defaultResourceFileName != null)
            return defaultResourceFileName;
        else
            return "RolandU220Drum.init"; 
        }
        
    public String getHTMLResourceFileName() 
        { 
        return "RolandU220Drum.html";
        }


    public byte getID() 
        { 
        try 
            { 
            byte b = (byte)(Byte.parseByte(tuple.id));
            if (b >= 17) return (byte)(b - 1);
            }
        catch (NullPointerException e) { } // expected.  Happens when tuple's not built yet
        catch (NumberFormatException e) { Synth.handleException(e); }
        return (byte)16;                // IDs start at 17
        }
        
    public byte[] emit(String key)
        {
        if (key.equals("number")) return new byte[0];  // this is not emittable
        if (key.equals("bank")) return new byte[0];  // this is not emittable
        
        if (key.equals("name"))
            {
            byte[] data = new byte[10 + 24];
            data[0] = (byte) 0xF0;
            data[1] = (byte) 0x41;
            data[2] = (byte) getID();
            data[3] = (byte) 0x2B;
            data[4] = (byte) 0x12;
            data[5] = (byte) 0x11;
            data[6] = (byte) 0x00;
            data[7] = (byte) 0x00;
                
            char[] name = (model.get("name", "") + "            ").toCharArray();
            for(int i = 0; i < 12; i++)
                {
                data[i * 2 + 8]= (byte)(name[i] & 15);
                data[i * 2 + 8 + 1] = (byte)((name[i] >>> 4) & 15);
                }
            data[data.length - 2] = produceChecksum(data, 5, data.length - 2);
            data[data.length - 1] = (byte)0xF7;
            return data;
            }
        else if (key.startsWith("drum"))
            {
            int drum = StringUtility.getFirstInt(key);
            String rest = (drum < 10 ? key.substring(5) : key.substring(6));
            byte AA = (byte) 0x11;
            byte BB = (byte) (0x23 + drum);         // starts with drum0
            byte CC = (byte) ((Integer)(allDrumParametersToIndex.get(rest))).intValue();
            byte val = (byte)(model.get(key));
                
            byte checksum = produceChecksum(new byte[] { AA, BB, CC, val });
            return new byte[] { (byte)0xF0, 0x41, getID(), 0x2B, 0x12, AA, BB, CC, val, checksum, (byte)0xF7 };
            }
        else if (key.startsWith("note"))                // changing the note, that's all
            {
            return new byte[0];
            }
        else    // More common
            {
            byte AA = (byte) 0x11;
            byte BB = (byte) 0x00;
            byte CC = (byte)((Integer)(allCommonParametersToIndex.get(key))).intValue();
            byte val = (byte)(model.get(key));
                
            byte checksum = produceChecksum(new byte[] { AA, BB, CC, val });
            return new byte[] { (byte)0xF0, 0x41, getID(), 0x2B, 0x12, AA, BB, CC, val, checksum, (byte)0xF7 };
            }
        }



    /** If the user is editing the patch on the synth, the U-220 won't change patches!
    	So just in case we send this. */
    public boolean getSendsParametersAfterNonMergeParse() { return true; }

    public byte produceChecksum(byte[] data)
        {
        return produceChecksum(data, 0, data.length);
        }
                
    /** The checksum is computed on all the ADDRESS and DATA data.
        Just add up the data, mod 128, and subtract the result from 128. Return that, unless it is 128, in which case return 0. */
    public byte produceChecksum(byte[] data, int start, int end)
        {
        // The checksum works as follows:
        // 1. Add all the data
        // 2. mod by 128 (that is, & 127)
        // 3. Subtract from 128
        // 4. If the result is 128, return 0
        // 5. Else return the result
                
        int check = 0;
        for(int i = start; i < end; i++)
            {
            check += data[i];
            }
        check = check & 0x7F;
        check = 0x80 - check;
        if (check == 0x80) check = 0;
        return (byte) check;
        }
        
    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {             
        if (tempModel == null)
            tempModel = getModel();

        int start = tempModel.get("number");
        int AA = (toWorkingMemory ? 0x00 : 0x05);
        int BB = (toWorkingMemory ? 0x20 : (start * 1568) / 128 );
        int CC = (toWorkingMemory ? 0x00 : (start * 1568) % 128 );
                
        // The U-220 is entirely byte-packed :-(  So we have to do this by hand.
                
        byte[] buf = new byte[1568];
        int pos = 0;
                
        String name = model.get("name", "Untitled") + "            ";
        for(int i = 0; i < 12; i++)
            {
            char c = name.charAt(i);
            buf[pos++] = (byte)(c & 15);
            buf[pos++] = (byte)((c >>> 4) & 15);
            }
        
        int d = 
            (model.get("bendrangeupper") << 0x5) |
            (model.get("bendrangelower") << 0x0);
        buf[pos++] = (byte)(d & 15);
        buf[pos++] = (byte)((d >>> 4) & 15);
        buf[pos++] = (byte)((d >>> 8) & 15);
        buf[pos++] = (byte)((d >>> 12) & 15);
        
        for(int i = 0; i < 64; i++)
            {
            d = 
                (model.get("drum" + i + "detunedepth") << 0xC) |
                (model.get("drum" + i + "media") << 0x7) |
                (model.get("drum" + i + "number") << 0x0);
            buf[pos++] = (byte)(d & 15);
            buf[pos++] = (byte)((d >>> 4) & 15);
            buf[pos++] = (byte)((d >>> 8) & 15);
            buf[pos++] = (byte)((d >>> 12) & 15);
                
            d = 
                (model.get("drum" + i + "autobendrate") << 0xC) |
                (model.get("drum" + i + "pitchshiftcoarse") << 0x7) |
                (model.get("drum" + i + "pitchshiftfine") << 0x0);
            buf[pos++] = (byte)(d & 15);
            buf[pos++] = (byte)((d >>> 4) & 15);
            buf[pos++] = (byte)((d >>> 8) & 15);
            buf[pos++] = (byte)((d >>> 12) & 15);

            d = 
                (model.get("drum" + i + "envmode") << 0xF) |
                (model.get("drum" + i + "pitchpressuresens") << 0xA) |
                (model.get("drum" + i + "pitchpolysens") << 0x5) |
                (model.get("drum" + i + "autobenddepth") << 0x0);
            buf[pos++] = (byte)(d & 15);
            buf[pos++] = (byte)((d >>> 4) & 15);
            buf[pos++] = (byte)((d >>> 8) & 15);
            buf[pos++] = (byte)((d >>> 12) & 15);

            d = 
                (model.get("drum" + i + "envreleaserate") << 0xC) |
                (model.get("drum" + i + "envdecayrate") << 0x8) |
                (model.get("drum" + i + "envattackrate") << 0x4) |
                (model.get("drum" + i + "velocitysens") << 0x0);
            buf[pos++] = (byte)(d & 15);
            buf[pos++] = (byte)((d >>> 4) & 15);
            buf[pos++] = (byte)((d >>> 8) & 15);
            buf[pos++] = (byte)((d >>> 12) & 15);

            d = 
                (model.get("drum" + i + "muteinst") << 0x8) |
                (model.get("drum" + i + "sourcekey") << 0x0);
            buf[pos++] = (byte)(d & 15);
            buf[pos++] = (byte)((d >>> 4) & 15);
            buf[pos++] = (byte)((d >>> 8) & 15);
            buf[pos++] = (byte)((d >>> 12) & 15);
                        
            d = 
                (model.get("drum" + i + "random") << 0xB) |
                (model.get("drum" + i + "outputassign") << 0x9) |
                (model.get("drum" + i + "instlevel") << 0x4) |
                (model.get("drum" + i + "pan") << 0x0);
            buf[pos++] = (byte)(d & 15);
            buf[pos++] = (byte)((d >>> 4) & 15);
            buf[pos++] = (byte)((d >>> 8) & 15);
            buf[pos++] = (byte)((d >>> 12) & 15);
            }

        Object[] result = new Object[26];
        for(int i = 0; i < 13; i++)
            {
            byte[] dat1 = new byte[Math.min(128, 1568 - 128 * i) + 10];
            dat1[0] = (byte)0xF0;
            dat1[1] = (byte)0x41;
            dat1[2] = (byte)getID();
            dat1[3] = (byte)0x2B;
            dat1[4] = (byte)0x12;
            dat1[5] = (byte) AA;
            dat1[6] = (byte) (BB + i);
            dat1[7] = (byte) CC;
            System.arraycopy(buf, 128 * i, dat1, 8, dat1.length - 10);
            dat1[dat1.length - 2] = produceChecksum(dat1, 5, dat1.length - 2);
            dat1[dat1.length - 1] = (byte)0xF7;
            result[i * 2] = dat1;
            result[i * 2 + 1] = Integer.valueOf(160);               // or so
            }
        return result;
        }


    public byte[] requestCurrentDump()
        {
        byte AA = (byte)(0x00);
        byte BB = (byte)(0x20);
        byte CC = (byte)(0x00);
        byte LSB = (byte)0x0C;
        byte MSB = (byte)0x20;          // 1568 bytes
        
        byte checksum = produceChecksum(new byte[] { AA, BB, CC, (byte)0x00, LSB, MSB });
        byte[] b = new byte[] { (byte)0xF0, (byte)0x41, getID(), (byte)0x2B, (byte)0x11, 
            AA, BB, CC, (byte)0x00, MSB, LSB, checksum, (byte)0xF7 }; 
        return b;
        }

    // Requests a Drum patch from a specific RAM slot (1...4)
    public byte[] requestDump(Model tempModel)
        {
        if (tempModel == null)
            tempModel = getModel();

        int number = tempModel.get("number");
        byte AA = (byte)(0x05);
        // size is 1568 I believe 
        byte BB = (byte)((1568 * number) / 128);
        byte CC = (byte)((1568 * number) % 128);
        byte LSB = (byte)(1568 % 128);
        byte MSB = (byte)(1568 / 128);
        
        byte checksum = produceChecksum(new byte[] { AA, BB, CC, (byte)0x00, LSB, MSB });
        byte[] b = new byte[] { (byte)0xF0, (byte)0x41, getID(), (byte)0x2B, (byte)0x11, 
            AA, BB, CC, (byte)0x00, MSB, LSB, checksum, (byte)0xF7 }; 
        return b;
        }
    
    
    

    public int getTestNotePitch() { return model.get("note"); }

    
    public JFrame sprout()
        {
        JFrame frame = super.sprout();
        addU220DrumMenu();
        return frame;
        }


    public void addU220DrumMenu()
        {
        JMenu menu = new JMenu("U-220");
        menubar.add(menu);
        JMenuItem showCurrentMultiPatchMenu = new JMenuItem("Show Current Multi Patch");
        showCurrentMultiPatchMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                final RolandU220Multi synth = new RolandU220Multi();
                if (tuple != null)
                    synth.tuple = new Midi.Tuple(tuple, synth.buildInReceiver(), synth.buildKeyReceiver(), synth.buildKey2Receiver());
                if (synth.tuple != null)
                    {       
                    // This is a little tricky.  When the dump comes in from the synth,
                    // Edisyn will only send it to the topmost panel.  So we first sprout
                    // the panel and show it, and THEN send the dump request.  But this isn't
                    // enough, because what setVisible(...) does is post an event on the
                    // Swing Event Queue to build the window at a later time.  This later time
                    // happens to be after the dump comes in, so it's ignored.  So what we
                    // ALSO do is post the dump request to occur at the end of the Event Queue,
                    // so by the time the dump request has been made, the window is shown and
                    // frontmost.
                                                
                    synth.sprout();
                    JFrame frame = ((JFrame)(SwingUtilities.getRoot(synth)));
                    frame.setVisible(true);

                    SwingUtilities.invokeLater(
                        new Runnable()
                            {
                            public void run() 
                                { 
                                Model tempModel = buildModel();
                                synth.performRequestCurrentDump();
                                }
                            });
                    }
                else
                    {
                    showSimpleError("Disconnected", "You can't show a patch when disconnected.");
                    }
                }
            });
        menu.add(showCurrentMultiPatchMenu);
        JMenuItem setupTestPatchMenu = new JMenuItem("Set up Test Patch for Drum Only");
        setupTestPatchMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                setupTestPatch(true);
                }
            });
        menu.add(setupTestPatchMenu);
        JMenuItem setupTestPatchMenu2 = new JMenuItem("Set up Test Patch for All Parts");
        setupTestPatchMenu2.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                setupTestPatch(false);
                }
            });
        menu.add(setupTestPatchMenu2);
        JMenuItem distributeMenu = new JMenuItem("Distribute Last Parameter to All Drums");
        distributeMenu.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                distributeLastParameter();
                }
            });
        menu.add(distributeMenu);
        }
    
    public void distributeLastParameter()
    	{
    	String key = getModel().getLastKey();
    	if (key == null) 
    		{
            showSimpleError("No Parameter", "No parameter has been set yet.");
    		}
    	else if (allCommonParametersToIndex.containsKey(key))
    		{
            showSimpleError("No Parameter", "To distribute, the last set parameter must be for a drum note.");
    		}
    	else
    		{
    		int val = getModel().get(key);
			String stripped = StringUtility.removePreambleAndFirstDigits(key, "drum");
			if (stripped.equals(key)) // uh oh
				{
				new RuntimeException("Couldn't remove premable 'drum' from " + key).printStackTrace();
				}
   			else
   				{
   				for(int i = 0; i < 63; i++)
    				{
    				getModel().set("drum" + i + stripped, val);
    				}
    			}
    		}
    	}  
              
          
    // Prepare a Patch whose slot N has the current MIDI channel, and has all the partials in reserve.
    // N is defined as the current emit location.  All other slots have zero partials and MIDI channel OFF.
        
    public void setupTestPatch(boolean drumOnly)
        {
        if (tuple == null)
            if (!setupMIDI())
                return;

        if (tuple != null)
            {
            final RolandU220Multi synth = new RolandU220Multi();
            synth.tuple = new Midi.Tuple(tuple, synth.buildInReceiver(), synth.buildKeyReceiver(), synth.buildKey2Receiver());
            if (synth.tuple != null)
                {
                synth.loadDefaults();
                
                if (drumOnly)
                    {
                    for(int i = 1; i <= 6; i++)
                        {
                        synth.getModel().set("p" + i + "outputlevel", 100);
                                        
                        // turn off everybody
                        synth.getModel().set("p" + i + "receivechannel", RolandU220Multi.MIDI_CHANNEL_OFF);
                        synth.getModel().set("p" + i + "voicereserve", 0);
                        }
                                
                    // turn on rhythm
                    synth.getModel().set("rhythmmidichannel", 9);           // channel 10
                    synth.getModel().set("rhythmoutputlevel", 30);
                    }
                else
                    {
                    for(int i = 1; i <= 6; i++)
                        {
                        synth.getModel().set("p" + i + "outputlevel", 100);
                                        
                        // turn on everybody, sharing equally
                        synth.getModel().set("p" + i + "receivechannel", i - 1);
                        synth.getModel().set("p" + i + "voicereserve", 4);
                        }
                                                
                    // turn on rhythm                                       
                    synth.getModel().set("rhythmmidichannel", 9);           // channel 10
                    synth.getModel().set("rhythmoutputlevel", 6);       // remaining voices 
                    }

                synth.sendAllParameters();
                sendAllParameters();
                }
            }
        }


    public static final int MAXIMUM_NAME_LENGTH = 12;
    public String revisePatchName(String name)
        {
        name = super.revisePatchName(name);  // trim first time
        if (name.length() > MAXIMUM_NAME_LENGTH)
            name = name.substring(0, MAXIMUM_NAME_LENGTH);
        
        StringBuffer nameb = new StringBuffer(name);                            
        for(int i = 0 ; i < nameb.length(); i++)
            {
            char c = nameb.charAt(i);
            if (c >= 32 && c <= 127)
                continue;
            else
                nameb.setCharAt(i, ' ');
            }
        name = nameb.toString();
        return super.revisePatchName(name);  // trim again
        }        

        
    /** Verify that all the parameters are within valid values, and tweak them if not. */
    public void revise()
        {
        // check the easy stuff -- out of range parameters
        super.revise();

        String nm = model.get("name", "Init");
        String newnm = revisePatchName(nm);
        if (!nm.equals(newnm))
            model.set("name", newnm);
        }

 
    public void changePatch(Model tempModel) 
        {
        if (tempModel == null)
            tempModel = getModel();

        // We do a PC to the Rhythm's Rx Channel (patch/r.part/rx ch),
        // which we assume to be the same as the existing channel
                
        tryToSendMIDI(buildPC(getChannelOut(), tempModel.get("number")));
        }
    
    public String getPatchName(Model model) { return model.get("name", "Untitled  "); }
        
    public Model getNextPatchLocation(Model model)
        {
        int number = model.get("number");
        
        number++;
        if (number >= 4)
            number = 0;
                
        Model newModel = buildModel();
        newModel.set("number", number);
        return newModel;
        }

    /** Roland only allows IDs from 17...32.  Don't ask. */
    public String reviseID(String id)
        {
        try
            {
            int val = Integer.parseInt(id);
            if (val < 17) val = 17;
            if (val > 32) val = 32;
            return "" + val;
            }
        catch (NumberFormatException ex)
            {
            return "" + (getID() + 1);
            }
        }
        
    public String getPatchLocationName(Model model)
        {
        // getPatchLocationName() is called from sprout() as a test to see if we should enable
        // batch downloading.  If we haven't yet created an .init file, then parameters won't exist
        // yet and this method will bomb badly.  So we return null in this case.
        if (!model.exists("number")) return null;
        
        return "R-" + (model.get("number") + 1 < 100 ? (model.get("number") + 1 < 10 ? "00" : "0") : "") + ((model.get("number") + 1));
        }
        
    public int getBatchDownloadWaitTime() { return 2200; }

    /** Return a list of all patch number names, such as "1", "2", "3", etc.
        Default is null, which indicates that the patch editor does not support librarians.  */
    public String[] getPatchNumberNames() 
        {
        return new String[] { "1", "2", "3", "4" };
        }

    /** Return whether individual patches can be written.  Default is FALSE. */
    public boolean getSupportsPatchWrites() { return true; }

    /** Return the maximum number of characters a patch name may hold. The default returns 16. */
    public int getPatchNameLength() { return MAXIMUM_NAME_LENGTH; }

    /** Return true if individual (non-bank) patches on the synthesizer contain location information (bank, number). */
    public boolean getPatchContainsLocation() { return true; }

    public boolean librarianTested() { return true; }
    }
