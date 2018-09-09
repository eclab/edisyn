/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.korgwavestation;

import edisyn.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public abstract class KorgWavestationAbstract extends Synth
    {
    public static final String[] BANKS = new String[] { "RAM 1", "RAM 2", "RAM 3", "ROM 4", "ROM 5", "ROM 6", "ROM 7", "ROM 8", "ROM 9", "ROM10", "ROM11", "Card" };
    public static final String[] KEYS = new String[] { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    public static final String[] SOURCES = new String[] { "Linear Keyboard", "Centered Keyboard", "Linear Velocity", "Exponential Velocity", "LFO1", "LFO2", "Envelope 1", "Aftertouch", "AT + Mod Wheel", "Mod Wheel", "MIDI 1", "MIDI 2", "MIDI Mod Pedal" };
    public static final String[] FX_SOURCES = new String[] { "None", "Mod Wheel", "Aftertouch", "Last Key Velocity", "High Key Velocity", "All Amp Envelopes", "Key Down Gate", "Effects Switch", "Effects Toggle", "MIDI Mod Pedal", "MIDI 1", "MIDI 2", "AT + Mod Wheel", "Joystick AC", "Joystick BD" };
    public static final int[] MIDI_BANKS = new int[] { 0, 0, 2, 2, 3, 3, 4, 4, 5, 5, 1, 1 };
    public static final int[] MIDI_PROG_CHANGE_OFFSETS = new int[] { 0, 50, 0, 50, 0, 50, 0, 50, 0, 50, 0, 50 };
        
    public static final String[] FX = new String[] { "Off", "Small Hall", "Medium Hall", "Large Hall", "Small Room", "Large Room", "Live Stage", "Wet Plate", "Dry Plate", "Spring Reverb", 
                                                     "Early Reflections 1", "Early Reflections 2", "Early Reflections 3", "Gated Reverb", "Reverse Gate", "Stereo Delay", "Ping-Pong Delay", "Dual Mono Delay", 
                                                     "Multi-Tap Delay 1", "Multi-Tap Delay 2", "Multi-Tap Delay 3",
                                                     "Stereo Chorus", "Quadrature Chorus", "Crossover Chorus", "Harmonic Chorus", "Stereo Flanger 1", "Stereo Flanger 2", "Crossover Flanger", 
                                                     "Enhancer/Exciter", "Distortion-Filter", "Overdrive-Filter", "Stereo Phaser 1", "Stereo Phaser 2",
                                                     "Rotary Speaker", "Stereo Mod-Pan", "Quadrature Mod-Pan",
                                                     "Stereo Parametric EQ", "Chorus-Stereo Delay", "Flanger-Stereo Delay", "Dual Mono Delay/Hall Reverb", "Dual Mono Delay/Room Reverb", "Dual Mono Delay/Chorus",
                                                     "Dual Mono Delay/Flanger", "Dual Mono Delay/Distortion",  "Dual Mono Delay/Overdrive", "Dual Mono Delay/Phaser", "Dual Mono Delay/Rotary Speaker",
                                                     "Stereo Pitch Shifter", "Modulatable Pitch Shifter-Delay", "Stereo Compressor-Limiter/Gate", "Small Vocoder 1", "Small Vocoder 2",
                                                     "Small Vocoder 3", "Small Vocoder 4", "Stereo Vocoder 1", "Stereo Vocoder 2" };

    public static final String[][] PERFORMANCES = new String[][]
    {
    { "Ski Jam", "Entropy", "Pinger", "Reswacker", "Lead Rock Guitar", "Softwaves", "Cascade Falls", "Blow the Bottle", "Magic Guitar", "Will I Dream?", "Fire Dance", "Analog Love Thang", "Panned Waves", "Super Res", "Ballerina Bells", "Soft Analog", "Mod Wheel Air", "Bowed Strings", "Pluckrimba", "Vector Guitar", "Midnight Run", "African Sunset", "Harmonic Motion", "Air Chorus & Bell", "SunGlasses Kid", "Stabby Horns", "Soft EP w/Tine", "Artificial Strg", "The Pied Piper", "Vox Concrete", "Snake Charmer", "Rock Tine Piano", "Pressure Glass", "Vox Arpeggios", "Struck Bell", "Upright & Oboe", "Refinery", "Kick up da Bass", "Syn Vox", "Kingdom Come", "Cat’s Eye", "Jazz Mutes", "VS Bell Pad", "Spectra", "New Sparkle", "Vektor Organ", "Alien Dreams", "End of Voltaire", "Kilimanjaro", "Debussy On Wheels" },
    { "Pharoah’s Jig", "City of Tomorrow", "Spectrumize", "Fuzzy Pop Clav", "ZZ Lead", "Wack Flute", "Glitter Vox", "Bee Hive", "Waves On Wheels", "21st Century", "Sustain Pedal Jam", "Nasty Harmonics", "Glider", "Mr. Wave Table", "Alpine Bells", "The Big Brass", "Gentle Winds", "String & Woodwind", "Rain Chiffs", "Guitar Rez", "ScrittiFunk", "Sonar Bell String", "New Zealand Vice", "Sunday Morning", "Split on Sunset", "Brass Orchestra", "Digipno & Breath", "Lassie Come Home", "Mellow Square Pad", "Antarctica", "Echo Hunters", "Organomics", "Trans Atlantic", "The Wave Guitar", "Bell Tree", "Palo Alto Pad", "Thick Pick", "Skip’s Boom Bass", "Folk Guitar", "Ivesian Split", "Saturn Rings", "Rotary Organ", "Star Bell Sweep", "Pop Box", "Xnaos Split", "Rock Steady", "20Sec. Invasion", "Chronos", "Mambo Marimba!", "The Big Pad" },
    { "ResoPad 1", "Unit Brass 1", "Wow Clavi", "Fuzzy Strings", "Wild Thing", "Plucky Heaven", "Digi Power", "Great Sweep", "Hammering", "Pulsitar", "FairyTale", "Unit Brass 2", "Touch Organ", "ResoStrings", "Unisolo", "Digitarian", "PulSeq Pad", "Gremlins 1", "Mixed Bass", "Nebulus", "Chromanium", "UnitBrass 3", "ResClav", "Strings 2", "Fripatronix", "Singing", "Dynamic Saw", "Resonator", "Gizmo", "SpaceBound", "ResoSynBell", "WS Brass 4", "Portative", "SawStrings", "VS Waves", "Movin' Choir", "AfterTouch Reso", "Gremlins 2", "ResoPad 2", "Abyss", "SynBell 1", "Syn Brass", "RazorKlav", "String/Brass", "BanJoe", "Ooo's", "Env Sweep", "Elec. Violin", "XtraRez Bass", "PluckyAirString" },
    { "Mr.Terminator", "Ancient Light", "Belle Star1", "16' Grand Piano", "MonoLeadSynth", "MaxiBassSynth", "DaysOfBreth", "Rez/Sync Comp", "Acoustic Guitar", "Astral Pad", "RockHaus", "Funky Metally", "Bach's Organ", "Funkanette", "Attack Bell", "TromboneTrumets", "Buzzzwacker", "Cello Ensemble", "PerkoPanFlute", "Elec. Guit1", "SambaSamba Wh^", "Day Dreams", "HarpsiLand", "Syn Soprano", "AbilityToSplit", "Brass Stabs", "New Tines", "Light Strings", "2 Flutes", "Marscape", "Drum Kit", "WS E.P.2", "Wide Open Synth", "CorelliClassics", "Belletts", "Alto Sax", "WaterPhone", "Bass2 InYo'Face", "Ghosts", "BiggerThanLife", "The Total Kit", "Crystal Roads", "Vintage Digital", "Vibes Split", "Angelina", "Legend2 Organ", "Tropical Forest", "Tine Bass", "African Kalimba", "Southern Pole" },
    { "HousePianoSplt", "BowedSteelPad", "GodChimes", "The Piano Pad", "Spank-a-lead", "PWM Bass", "Dee Five Oh", "Velo Filter Wah", "Nylon Guitar", "Dream's Essence", "BamBam Drum Jam", "Snap Synth", "Legend1 Organ", "Steel Clav", "Angel's Bell", "Solo Trumpbone", "Voicicato", "WH.Filt.Strings", "Angelica", "Elec. Guit2", "Vocodadrum", "Windmere Harp", "DigiClavOrgan", "Four Singers", "Time 2 JAM It", "Brass Section", "16' EP Hybrid", "Analog Strings", "SynFlute AT", "Slow Glass", "Techno Acid Kit", "Elektrik Roads", "Big Pulse Pad", "Phase/FlangeStr", "AngelsWithBells", "Tenor Sax", "OctoThang", "Dyno Slap Bass", "Analog God Pad", "DigiPan Synth", "Kit 1 Down", "Roads&Strings", "Synth Stabs", "OrchestralSplit", "FluteBells1", "Cool Jazz Organ", "The Creature", "SynthBassSplit", "Soft Kalimba", "DreamInMotion" },
    { "Funky Planet", "Dark Village", "Bell Horn", "Piano&Voices", "OD3MiniLead", "NastyNasty Bass", "Swirling Eddies", "Finger Res", "12-StringGuitar", "Magic Waters", "The Wave Police", "SynKeys", "60’s Organ", "Clavland", "DaydreamBells", "Lonely Trumpet", "Air Pizzoish", "Dream Strings", "Perc Vox", "Elec. Guit3", "Afro Salsa", "Faerie's Harp", "Metal Clav/Pad", "Vox PressurePad", "Mountain Duet", "Breath Horns", "E.Piano WS", "Jet Strings", "Velocity Flute", "Jupiter Dream", "Rap It Up !!!", "Fenda' Roadz", "Pulsanaloggin'", "Harp&Strings", "Shimmer Bell", "Group Sax", "JB Whistle", "Analog Bass", "Dreaming of...", "Sting Rays", "Massive Noises", "The East River", "SynthaKey", "Analog Split", "Harp&Bell 1", "Organ Donor", "Rain Forest", "SynthTwang Bass", "Marimba", "Lost Generation" },
    { "Toto'ly 6/8", "Dream of Java", "Cosmic Dawn", "8' Grand Piano", "Cutting Lead", "Zzap Syn Bass", "VS SynthPad", "DigiResoBass", "Jazz Guitar", "A “Formal” Pad", "Joystick Jam #2", "Chamber Music", "PercussiveOrgan", "Jammin' Clavi", "LA Bell", "Fr.Horn&Strings", "Synthetic Pizzi", "VelCelloFellow", "Whisper Tines", "JazGitBassSplit", "Dr.Wave", "Heavenly Harp", "MetalOrgan2", "Slow Choir", "Funk Split", "Nuclear Brass", "Vibe Roads", "SynString 1", "FlootStringLead", "Chromes", "Gated Kit", "Drawbars", "Analogness", "VoiceAirString", "Ethnic Bell", "Saxmosphere", "Sicilian Theme", "Sweet Fretless", "BeyondTheClouds", "Mr.Vibe E.Piano", "PitchSlapBack", "DigiPiano III", "Magic Synth", "Jazz-Man", "Slightly Belly", "P5 Organ", "Far From Earth", "Punch Bass", "Island Marimba", "Fat Sine Pad" },
    { "Layers Of Funk", "Leaving the Pod", "Vocal Highlands", "MIDI Piano", "OD Mini 2", "Saw Bass", "Bliss Pad", "AFT Vocal Pad", "Guitaro", "Night Winds", "MIDI Clock Song", "SynthKlavier", "B Assault", "Phasey Clavvy", "StarBellPiano", "Big Band Ensmbl", "Pizzicato", "Distant Hills", "Breathathon Pad", "Finger Bass", "Tangerine D.", "SpaceHarp", "Spike Comp", "WHEEL Vox", "Synth/Bass Splt", "Big Band", "X Roads", "Sanctuary", "CuCu Flute", "Paulina", "Space Gate Kit", "E Piano'n", "WiseOldProphet", "Slow Pad", "Harp&Bell 2", "Clarinet", "Orchestra Blitz", "Mondo Bass", "Gray Encounter", "PluckAnaPad", "MidiEchoDrums", "Mr.Fat Stack", "Flash Pad", "Doors To Kiev", "Midland Bells", "Real Organ", "Timeworm", "BeefCakeBass", "HollowMarimba", "And there was 1" },
    { "Life Goes On...", "Cin-a-max 90", "Crystal Pad", "Piano Layers", "Bender Mini", "Swingin'Bass", "Digizip Twists", "Resonant Waves", "Peace Guitars", "Cinema God Pad", "MC-SR Rap", "Dyno-Synth", "Holy Organs", "HarpsiEP", "Mazatlan", "Syn Brass 2", "PizziTineString", "Slow String Pad", "Farelite Voice", "Sub Stick", "Amazon Life", "Valley Harp", "Love Trash", "7 Sirens", "LA Stylez Split", "Big Big Brass", "UpFront EP", "Noble Strings", "Pan Flute", "Heaven's Gate", "InsaneDrumLayer", "Markus Roadz", "Ice Cubes", "JX Soundtrack", "Wonder Bells", "Accordion 1", "Res Sweep Pad", "Synclav Bass", "Solar Rings", "Sting Voxers", "Flange My Kit!!", "Dyno Vibes", "MathematicalSky", "BriteSynthSplit", "FluteBells2", "MetalPerc Organ", "Theatre Ecstasy", "Droid Bass", "Ice Vibes", "Delicate Pad" },
    { "Mizik Split", "Crystal Theme", "Belle Star2", "8'Piano&Strings", "Purple Haze", "ElecroFunk Bass", "Arianne", "Mr.”Q”", "Stratosphere", "Magic Winds", "Bangagong Jam", "Mr. Perky", "MetalOrgan1", "Ultra Clav", "Bell Chorus", "SmokeyBacon", "NylonMallet", "String Swell", "WoodyPercVox", "Bass1 InYo'Face", "Phase Dance", "Solo Harp", "Tight Synth", "Glass Voices", "Catalina Island", "Sweet'n'Sour", "Crystal EP", "SynString 2", "Pan Man", "Space Sailing", "Mondo Combi Kit", "PunchyDigiPiano", "Oh Bee Poly", "1 Finger L.S.O.", "Bell Piano", "Accordion 2", "China Orch", "Octave Divider", "Eternal", "Fire & Ice", "Electik Kit", "Trampin'", "Plux Nirvana", "Fretless&Piano", "WS SpaceBell", "Stadium Organ", "ModWheel Surf", "MetallicBass", "Fuzz Mallet", "Magic Glass" },
    { "The Wave Song", "Deep Atmosphere", "Sting Waves", "Metropolitan", "Mini Lead", "Tack Horns", "Guardians", "Digital ResWave", "Sandman", "Time Traveler", "Song Bells", "Analog Punch", "Cosmic Zone", "Super Clav", "Toy Box", "Analog Brass", "Modernesque", "Octave Strings", "Glass Tambu", "Vektar", "Whisper Voices", "Vulcan Harp", "Quarks", "Vocalise", "Gig Split", "Touch Brass", "Tine Piano", "Warm Strings", "Chiffy Kalimba", "Northern Lights", "Bottle Air", "Rock Stack", "Excalibur", "Wave Tables", "Bells", "Prophet Horn", "Mahogany", "Round Wound", "Digi Harp", "Motion Mix", "Stereo Waves", "Screamer", "Paradise", "Digital Touch", "Voice & Bell", "Resonant Synth", "Rhythm of the Wave", "Introspective", "Wave Mallet", "Station Platform" },
// this last one is for CARD
    { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49" }
    };


    public static final String[][] PATCHES = new String[][]
    {
    { "Wave Dance", "Wave Rhythm", "Slow Waves", "Tap Drops", "ChromeTrans", "Box Waves", "Changin'Tines", "Analog Sequence", "Sea Glass", "Arpeggio", "Env Sweep", "Will I Dream?", "Dist. Guitar", "Boom Bass", "Time Slice", "Entropy", "Glider", "Debussy VS", "Pressure Play", "Impressionist", "AlteronInvasion", "Cascade Falls", "Vectorism", "African Sunset", "Vektor Organ", "Organ 1", "Organ 2", "Organ Perc", "Bowing", "Slow Strings", "Doublepad", "Pitch Pad", "Sine Vox", "Vox Breath", "Choir" },
    { "Soft Marimba", "Tubular Bells", "Wack 1", "Tambourine", "Bell Chimes", "New Metal Hit", "Crystals", "Soft EP", "Digi Piano", "VS Clav", "Electric Guitar", "Acoustic Guitar", "Mute Guitar", "Pluck 1", "Pluck 2", "Marbles", "Upright Bass", "Shock Bass", "Kick Bass", "TriSquare", "Hard Sync", "Nasty Waves", "Pop Waves", "Analog Brass", "Brass Mix", "Fanfare", "Bassoon/Oboe", "Mr.Alto", "Flute", "Spitty", "Neon Air", "Sonar", "Spectrum 1", "Spectrum 2", "Spectrum 3" },
    { "Strings1", "Strings2", "Saw Pad", "Brass 1", "Brass 2", "TouchBrass", "WS Brass4", "Touch Organ", "Portative", "Singing", "VocalTract", "Pluck Seq", "Pluck 1", "Pluck 2", "DigiPluck", "Clavi 1", "SonarPluck", "Nu Bass1", "Nu Bass2", "Bass 03", "ResoBass", "Reso001", "Reso002", "ResoSweep", "Env Sweep", "Reso Pad 1", "Reso Pad 2", "SynBell 1", "Pulseq Pad", "SlowSaw", "Vs Seq", "Elec.Violin", "OverDrive", "DigiPower", "Quaxla" },
    { "Piano Low", "Piano Hi", "DigiPiano", "CelloEnsmble", "String Pad", "Fretless", "Slap Bass", "Dyno Bass", "SynBass 3", "Deep Mini Bass", "Marimba", "Kalimba", "Harp", "Nylon Guitar", "Pan Flute", "Wood Flute", "A.Sax 2", "French Horn", "SynBrass2", "Accordion", "VoiceSynthPad2", "MagicWind", "Wave Bell", "AttackBell", "VS155s", "PsychoWave", "VS126", "PWMs", "BugsAtSea", "Acoustic Guitar", "Amazon", "WavePolice", "DanceNow", "Snare&16th", "Bad Dance" },
    { "Analog Bass", "RudeBass", "FingerBass", "WaveBass", "Jazz Bass", "Jazz Guitar", "D.P.E. Piano", "OB-Res", "DigitalPerc", "The Classics", "SawStringsWHEEL", "LA Bell", "P-saws", "Sine Pad", "AeroSynth", "AngelBell", "BellPiano", "Elec. Guit2", "Organ 3", "Slapper", "Pulsey", "Analog Pad", "Mini 2", "Vibes", "WaterPhone", "Filter Vox", "Harmonic", "Super Sync", "Jupiter Dream", "Voicey", "HouseBass", "Busy Bass", "Tenor Sax", "TromboneTrumpet", "Chug" },
    { "FunkHouse", "Stdy 4/4", "Funk", "Rhythm 1", "Mizik", "Visitor", "Scriti", "HoUsE", "PercMiniBass", "Stick", "MIDI Grand", "DynoWhirly", "Cross Bee", "KorgyClavi", "BritePipes", "E.Piano2", "MagicSynth", "WS Punch", "Brasser", "Analogness", "Analog Seq. 2", "TrasHybrid", "E.Guitar", "Ethnic Bell", "M Heaven", "VS Ago", "Swirling", "Harp Wind", "Highlands", "Four Singers", "JetString", "SynFlute", "2 Flutes", "Mr.Perky", "MultiMarimba" },
    { "Organic", "AngelBell2", "Air Choir", "BriteAnaBrass", "Log Bass", "VocalPad", "WS Harp", "WS SpaceBell", "Tubular2", "WS EPiano", "Dreaming", "Solar Rings", "Synbass#1", "Vox Strings", "Wind Magic", "Twang Bass", "Roadz", "Punch Bass", "Digi Bass", "L.A.Piano", "PWM", "East Side", "Cheesy Farfeesy", "PwmPad", "Pole Wind", "JB Harpsi", "PCM Synth", "Inharmonic", "String KD", "BigPad", "AirHarp", "Spike", "Bender Mini", "Alien", "Ta Vox" },
    { "VelCello", "Synth Pizzi", "DelaySwellStrng", "Mid Bells", "VoxKeys", "Glass EPiano", "Metal Tines", "VS ElPiano", "SynKeys 1", "Vibes ala Pr VS", "Glass Pad", "Funky Metally", "Hollow Marimba", "Breath Pad", "Breathaton Pad", "Syn Soprano", "Organ Donor", "Holy Organs", "Ourghan", "Snazzo Organ", "Bach's Organ", "Organ Frills", "Pulsanaloggin'", "Velocity Flute", "Perko Pan Flute", "AnaPlukPad", "Digizip Twists", "God Chime", "Mini1Bass", "Saw Key 'n Bass", "Bassnap", "MC-WS Rap", "Scratch 2", "Rap Fill", "Midi Echo" },
    { "Tines 2", "Slow String Pad", "Plux Nirvana", "TimeWorm", "FairVoice", "Suspense Pad", "Dream's Essence", "Tubular", "Chromes", "OrganPercussion", "Ancient Light", "Orchestra Swat", "E.P.1", "Inharma Star", "Res Sweep Pad", "Sicilian", "Heaven's Gate", "Arianne", "Res2Sweep", "Drum Seq 1", "Angelica", "Bell Chorus", "Ice Cubes", "Elka Rhapsody", "Inspires", "SyncBass", "Syn Brass For U", "Poly Filler", "VS Wave 3", "New Steel Git.1", "Rippin", "GuiPad", "Destructo", "Vector Hell....", "Descending" },
    { "Galaxis", "Space Sailing", "Wheel Vox", "Surf", "7/8 Seq", "Nick’s Pad", "Slow Glass", "One", "Clarinet", "Forest", "Whistle", "Accordeon", "Horn Knee", "Rappa 1", "Drum Kit", "Kit1 DWN", "Ride Cymbal", "WS Crash", "RapHatOpn", "RapHat", "RapSnare", "RapBDsus", "Kit1 +", "Kit1 UP", "RapCowbell", "RapKick", "KinGuit", "Tambo 1", "E.Bass Pt 1", "Percus 1", "Afro Pt", "Samba(Rio)", "PhaseDrums", "The Dream", "Orbits" },
    { "Touch Tone", "Deep Waves", "Quarks", "DigitalResWave", "WS Strings", "Motion", "WS Metal", "WS S&H", "WS Table", "Vocalise", "Voices", "Air Vox", "Glass Vox", "Glass Bottle", "Softwaves", "SynOrch", "PWM Strings", "SynString", "Ravel By Number", "EP Body 1", "EP Tine", "E.Piano", "Mini", "Super Clav", "Pluck 3", "Digi Harp", "Tambotack", "Syn Bass 1", "Vox Bass", "VS Bell", "Sweet Bells", "Soft Horn", "Syn Brass", "Wave Song", "Industrial" },
// this last one is for CARD
    { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34" }
    };
        
    public static final String[] WAVES = new String[] 
    {
    // first 32 are actually wave sequences
    /*"WSTouch", "DeepWav", "Quarks", "ResXwav", "Strings", "Unison", "WSMetal", "WS S&H", "WSTable", "WSVoice", 
      "ResMove", "WSNoise", "LobWave", "FolowMe", "P5 Res", "Complex", "WS Fade", "VelHarm", "Mini", "SoftWav", 
      "Spectra", "WSGrowl", "SynWav1", "EnSweep", "GateRez", "Marbles", "Ostinat", "Drops", "SloWave", "WavRytm", 
      "Ski Jam", "WavSong", */
    "Soft EP", "Hard EP", "EP Tine", "EP Body1", "EP Body2", "EP Body3", "Digi EP", 
    "E_PIAN03", "CLAV_DSM", "Organ 1", "Organ 2", "Organ 3", "PipeOrg1", "PipeOrg2", "Pluck 1", "Pluck 2", 
    "Pluck 3", "A. Guitar", "E. Guitar", "Dist. Gtr", "EGuitChEme", "MuteGtr1", "MuteGtr2", "MuteGtr3", "Koto", 
    "harmonic", "Stick", "E. Bass", "Synbass1", "Synbass2", "BassHarm", "Vibes", "Hi Bell", "Jar", 
    "TinCup", "Agogo", "Gendar", "Tubular", "New Pole", "Soft Mrmba", "Thai Mrmba", "Glass Hit", "Crystal", 
    "Flute", "FluteTrans", "Overblown", "Bottle", "BassnOboe", "Clarinet", "BariSax", "TenorSax", "AltoSax", 
    "BrassEns", "TromTrp", "Tuba&Flu", "Bowing", "Synorch", "PWM String", "SynString", "Airvox", "Voices", 
    "Choir", "Glass Vox", "\"OO\" Vox", "\"AH\" Vox", "MV Wave", "FV Wave", "DW Voice", "SynthPad", "Birdland", 
    "ChromRes", "ProSync", "SuperSaw", "Ping Wave", "Digital1", "Digital2", "Digital3", "Bellwave", "PercWave", 
    "ShellDrum", "BD head", "Tambourine", "Cabasa", "Woodblock", "HH Loop", "WhiteNoi", "Spectrm1", "Spectrm2", 
    "Spectrm3", "Spectrm4", "Sonar", "Metal 1TR", "Metal 2TR", "KalimbaTR", "GamelanTR", "MarimbaTR", "Potnoise", 
    "Ticker", "VibeHit", "Whack 1", "Whack 2", "HDulciTR", "HoseHit1", "HoseHit2", "SynbassTR", "A.BassTR", 
    "\"ch\"", "\"hhh\"", "\"kkk\"", "\"puh\"", "\"sss\"", "\"tnn\"", "Inharm1", "Inharm2", "Inharm3", "Inharm4", 
    "Inharm5", "Inharm6", "Inharm7", "Inharm8", "Inharm9", "Inharm10", "Formant1", "Formant2", "Formant3", 
    "Formant4", "Formant5", "Formant6", "Formant7", "Sine", "Triangle", "VS 35", "VS 36", "VS 37", "VS 38", 
    "VS 39", "VS 40", "VS 41", "VS 42", "VS 43", "VS 44", "VS 45", "VS 46", "VS 47", "VS 48", "VS 49", "VS 50", 
    "VS 51", "VS 52", "VS 53", "VS 54", "VS 55", "VS 56", "VS 57", "VS 58", "VS 59", "VS 60", "VS 61", "VS 62", 
    "VS 63", "VS 64", "VS 65", "VS 66", "VS 67", "VS 68", "VS 69", "VS 70", "VS 71", "VS 72", "VS 73", "VS 74", 
    "VS 75", "VS 76", "VS 77", "VS 78", "VS 79", "VS 80", "VS 81", "VS 82", "VS 83", "VS 84", "VS 85", "VS 86", 
    "VS 87", "VS 88", "VS 89", "VS 90", "VS 91", "VS 92", "VS 93", "VS 94", "VS 95", "VS 96", "VS 97", "VS 98", 
    "VS 99", "VS 100", "VS 101", "VS 102", "VS 103", "VS 104", "VS 105", "VS 106", "VS 107", "VS 108", "VS 109", 
    "VS 110", "VS 111", "VS 112", "VS 113", "VS 114", "VS 115", "VS 116", "VS 117", "VS 118", "VS 119", "VS 120", 
    "VS 121", "VS 122", "VS 123", "VS 124", "VS 125", "saw", "OBPUL1", "OBPUL3", "OBPUL4", "OBPUL5", "OBPUL6", 
    "OBPUL7", "OBRES1", "OBRES2", "OBRES3", "OBSAW3", "OBTRESB", "OBTRESD", "OBTRESF", "OBTRESH", "OBTRESJ", 
    "DBTRESL", "OBTRESN", "PPUL2", "PPUL3", "PPUL4", "PPUL5", "PPUL6", "PSAW2", "13 - 01", "13 - 03", "13 - 05", 
    "13 - 07", "13 - 09", "13 - 11", "13 - 13", "13 - 15", "13 - 17", "13 - 19", "13 - 21", "13 - 23", "13 - 25", 
    "13 - 27", "13 - 29", "13 - 31", "13 - 33", "13 - 35", "13 - 37", "13 - 39", "13 - 41", "13 - 43", "13 - 45", 
    "13 - 47", "13 - 49", "13 - 51", "13 - 53", "13 - 55", "13 - 57", "13 - 59", "13 - 61", "13 - 63", "resx001", 
    "resx002", "resx003", "resx004", "resx005", "resx006", "resx007", "resx008", "resx009", "resx010", "resx011", 
    "resx012", "resx013", "resx014", "resx015", "resx016", "resx017", "resx018", "resx019", "resx020", "resx021", 
    "resx022", "resx023", "resx024", "resx025", "resx026", "resx027", "resx028", "resx029", "resx030", "resx031", 
    "resx032", "Min1 - 01a", "Min1 - 02a", "Min1 - 04a", "Min1 - 05a", "Min1 - 06a", "Min1 - 07a", "Min1 - 08a", 
    "Min1 - 09a", "Min1 - 12a", "Min1 - 13a", "Pres321", "Pres335", "Pres349", "Pres363", "Pres377", "Pres384", 
    "Pres391", "Pres398", "Pres110", "Pres3112", "Pres3119", "Pres3126", "Sax .1sec", "Sax 1 sec", "Sax 1.3sec", 
    "Sax 1.5sec", "Sax 1.7sec", "Sax 2 sec", "Sax 2.2sec", "Sax 2.4sec", "Sax 2.7sec", "Sax 2.9sec", "Sax 3 sec", 
    "Sax 3.4sec", "Sax 3.6sec", "Sax 4.3sec", "Sax 4.7sec", "Sax 5 sec", "Square", "Pulse02", "Pulse04", "Pulse06", 
    "Pulse08", "Pulse10", "Pulse12", "Pulse14", "Pulse16", "Pulse18", "Pulse20", "Pulse22", "Pulse24", "Pulse26", 
    "Pulse28", "Pulse30", "Pulse31", "MagicOrgan", "Magic 1a", "Crickets", "Noise 2", "GrandPiano", "DigiPiano", 
    "SynthPd2", "SynPad2a", "AirSynth", "VoiceSyn", "VoiSyn1a", "BellWind", "PWM", "AnaStrings", "Square Res", 
    "Res Wave", "TrashWave", "TrshWv1a", "PsychoWave", "SynBass3", "SynBas3a", "DynoBass", "DynoBs1a", "DeepBass", 
    "DeepBs1a", "MiniBass", "MiniBs1a", "Slap Bass", "Fretless", "Fretles1a", "Cello", "Cello 1a", "AltoSax2", 
    "Horn Sectn", "FrenchHorn", "PanFlute", "PanFl 1a", "Hard Flute", "Wood Flute", "Harmonium", "Hrmnium1a", 
    "Guitar 1", "Guitar 2", "Harp", "Harp 1a", "Shamisen", "Shamsn1a", "Marimba", "Marim 1a", "Marim Loop", 
    "HrdKalimba", "SofKalimba", "SftKalim1a", "Vibes 2", "PercBell", "M.Heaven", "BrightBell", "BrBel 1a", 
    "Drum Kit", "Kick", "AmbiKick", "Crack Snar", "Snare", "Sidestick", "Tom", "HiHat Clos", "HiHat Open", 
    "Conga", "Conga Loop", "Claves", "Tenny Hit", "Thonk", "Tick Hit", "Pot Hit", "Hammer", "PianoHit", 
    "NoiseVibe", "\"Tuunn\"", "\"Pehh\"", "\"Thuum\"", "\"Kaahh\"", "\"Tchh\"", "\"Pan\"", "\"Ti\"", "\"Cap\"", 
    "\"Chhi\"", "\"Tinn\"", "\"Haaa\"", "Glottal", "VS 126", "VS 127", "VS 128", "VS 129", "VS 130", "VS 131", 
    "VS 132", "VS 133", "VS 134", "VS 135", "VS 136", "VS 137", "VS 138", "VS 139", "VS 140", "VS 141", "VS 142", 
    "VS 143", "VS 144", "VS 145", "VS 146", "VS 147", "VS 148", "VS 149", "VS 150", "VS 151", "VS 152", "VS 153", 
    "VS 154", "VS 155", "Input1 [A/D]", "Input2 [A/D]"
    };

    public static final String[][] WAVE_SEQUENCES = new String[][]
    {
    { "Partial", "Invashn", "PulsMod", "OB Res1", "OB Res2", "Wave 13", "Sparks", "SonaNoi", "Whisper", "WhSweep", "WavTabl", "Chrome", "DreamSq", "BellSwp", "XWind 1", "TineVel", "Str Oct", "Breath", "OB Sax", "VelTran", "SynWav2", "PlukRez", "TineRez", "RezStep", "AtSweep", "Haitian", "Rez Seq", "3/4 Jam", "SpecJam", "RaspRap", "MIDISki", "MIDsong" },
    { "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "" },
    { "String", "Wave01", "Wave02", "EnvSwee", "DigiPlk", "Vox Seq", "Wave 06", "Wave 07", "Wave 08", "StrgTrm", "SlowRes", "FastRes", "ResoSeq", "Breath", "Beat", "PulSeq", "ResoPd1", "OB-13", "ResoPd2", "Sonar", "VS Wave", "DigiPow", "Reswing", "SftRing", "FormaWv", "VoxWv", "Resbell", "Brswing", "Vswing1", "Vswing2", "SftRise", "RptRise" },
    { "Snare 1", "16 Rthm", "Kick", "DSdrms", "Afrika", "DSbass", "Helicop", "BizyVox", "MagiWnd", "Gtr+Pno", "Orch WS", "NoizBug", "Rain D1", "RedRain", "W echoL", "Drum R", "Drum L", "W echoR", "Mr.Funk", "Kinko", "1/4 Kik", "HHts 1", "TikTok", "Snare 2", "Jungle1", "Crazy’X", "Kik+Snr", "JoVox", "LoopDrm", "FunW/16", "Indstrl", "Jungle2" },
    { "Chug", "Chug5th", "Brite 1", "Harpsi1", "ZooLoo1", "PlukOB2", "Res&Pul", "AeroPad", "Bas&Pad", "VibrSaw", "WaterPh", "GlasLoo", "TubelGl", "TinAgo", "Bass 3", "ChifTyn", "HdChif1", "Mini 4", "Mini 2", "Mini 6", "BrasTrn", "WveStas", "E.Bass1", "Ch+Kkk", "PGDance", "AngelBl", "AnglBl2", "Mystery", "RezDown", "RezBass", "Pulses3", "Resomin" },
    { "Life1", "Life 2", "StdyKik", "RunHats", "Heebie", "Jeebies", "Noizz1", "Trmnatr", "PanMal1", "PanMal2", "AvengrB", "Mr. 4/4", "IbidBox", "Iso Box", "Swirls", "TrasHyb", "HarpPad", "Cross B", "Groove1", "Bounce1", "BasiKik", "Hats 3", "Conga 1", "FunSnar", "Bounce2", "Hatties", "SynWav2", "KikPat1", "Kuntry1", "Kuntry2", "8_6/8KS", "Haties2" },
    { "WS Harp", "WS Bell", "Inharmo", "HouseBs", "HipHop", "OB)PWM", "OB(PWM", "Owwaah!", "Inharmc", "Harpsi", "HarpGl", "Ping!", "BentMin", "SDB2", "String", "PWM", "Atmos", "Haunted", "Alien1", "Freddie", "AnglBl3", "Bell1", "Subtle", "Digits", "B-Stng", "Orient", "Tin Stg", "Swerl", "Evolver", "OB Wow", "Tribes", "Wildlie" }, 
    { "Rap Hat", "RapFill", "Rap K&S", "RapVar2", "Rap Var", "EPnoPad", "VelEPno", "PanBell", "SaxBrss", "HrnyBrs", "RapKick", "ZapKick", "ZapSnar", "HarpRun", "SpdMtl", "TingStr", "BowdPad", "BowdStr", "BowdSyn", "BowCelo", "Nyloner", "AckGits", "SonrVox", "NylnFlt", "TickSyn", "AirPerc", "BrthPad", "THOKbas", "KUHLbas", "HarmBas", "KlikBas", "MidiEko" },
    { "Unknown", "Strince", "Taurust", "Celsia2", "SlowRes", "Ariane", "HellBel", "Organic", "Quicky2", "Drums!", "Chromes", "Spectrm", "WSNois2", "Tubular", "BendUp", "Inharma", "WetDrem", "VSWave3", "BusyBas", "Heaven1", "Heaven2", "Waiting", "SlapSeq", "PolySeq", "HowHats", "HowKik+", "HowSnar", "HowDity", "KotoPad", "FeedBck", "DarkSid", "Tremol" },
    { "WhlVox", "KikS", "Quar", "Stri", "Hats", "S. Pole", "Galaxis", "SpSail", "RappaWv", "SlowGls", "2->3 Pt", "Samba 1", "Samba 2", "Tambou", "Forest", "Caba", "Galax2", "Chime", "Agogo", "WoodBrc", "Tremolo", "PoppaBs", "Morgan", "Bassing", "Bassier", "Afro 1", "E.Bass", "Brass 1", "BassHar", "Orbits", "SloWav2", "TaDream" },
    { "WSTouch", "DeepWav", "Quarks", "ResXwav", "Strings", "Unison", "WSMetal", "WS S&H", "WSTable", "WSVoice", "ResMove", "WSNoise", "LobWave", "FolowMe", "P5 Res", "Complex", "WS Fade", "VelHarm", "Mini", "SoftWav", "Spectra", "WSGrowl", "SynWav1", "EnSweep", "GateRez", "Marbles", "Ostinat", "Drops", "SloWave", "WavRytm", "Ski Jam", "WavSong" },
// this last one is for CARD
    { "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "" }
    };
        
        
    public static final int  CURRENT_BANK =  0 ;
    public static final int  CARD_NAME =  1 ;
    public static final int  CURRENT_PROG =  2 ;
    public static final int  PROG_NAME =  3 ;
    public static final int  MIDI_MODE =  4 ;
    public static final int  MIDI_BASE_CHAN =  5 ;
    public static final int  NUM_MONO_CHANS =  6 ;
    public static final int  KEY_NUM_OFFSET =  7 ;
    public static final int  MIDI_PARAM_ENABLE =  8 ;
    public static final int  CONTROLLER_1 =  9 ;
    public static final int  CONTROLLER_2 =  10 ;
    public static final int  XMIT_MODE =  11 ;
    public static final int  LOCAL_KBD =  12 ;
    public static final int  XMIT_PROG_CHANGE =  13 ;
    public static final int  XMIT_AFTERTOUCH =  14 ;
    public static final int  XMIT_PITCH_BEND =  15 ;
    public static final int  XMIT_CONTROLLERS =  16 ;
    public static final int  REC_PROG_CHANGE =  17 ;
    public static final int  REC_AFTERTOUCH =  18 ;
    public static final int  REC_PITCH_BEND =  19 ;
    public static final int  REC_CONTROLLERS =  20 ;
    public static final int  REC_NOTE_ON_OFF =  21 ;
    public static final int  REC_ALL_NOTES_OFF =  22 ;
    public static final int  PROGMAP_ENABLE =  23 ;
    public static final int  PROGMAP_CHANGE_NUM =  24 ;
    public static final int  PROGMAP_PROG_BANK =  25 ;
    public static final int  PROGMAP_PROG_NUM =  26 ;
    public static final int  PROGMAP_PROG_NAME =  27 ;
    public static final int  CURRENT_MULTISET =  28 ;
    public static final int  MULTISET_FX_CONTROL_CHAN =  29 ;
    public static final int  MULTISET_CHAN =  30 ;
    public static final int  MULTISET_CHAN_ENABLE =  31 ;
    public static final int  MULTISET_LEVEL =  32 ;
    public static final int  MULTISET_PROG_BANK =  33 ;
    public static final int  MULTISET_PROG_NUM =  34 ;
    public static final int  MULTISET_PROG_NAME =  35 ;
    public static final int  SYSEX_PATCH_BANK =  36 ;
    public static final int  SYSEX_PATCH_NUM =  37 ;
    public static final int  SYSEX_ALL_BANK =  38 ;
    public static final int  SYSEX_WAVESEQ_BANK =  39 ;
    public static final int  SYSEX_PROG_BANK =  40 ;
    public static final int  SYSEX_PROG_NUM =  41 ;
    public static final int  MASTER_TUNE =  42 ;
    public static final int  EFFECTS_ENABLE =  43 ;
    public static final int  MEM_PROTECT_INTERNAL =  44 ;
    public static final int  MEM_PROTECT_CARD =  45 ;
    public static final int  PITCH_BEND_RANGE =  46 ;
    public static final int  VELOCITY_RESPONSE =  47 ;
    public static final int  SAVE_DATA_TYPE =  48 ;
    public static final int  SAVE_SOURCE_BANK =  49 ;
    public static final int  SAVE_SOURCE_NUM =  50 ;
    public static final int  SAVE_SOURCE_NAME =  51 ;
    public static final int  SAVE_DEST_BANK =  52 ;
    public static final int  SAVE_DEST_NUM =  53 ;
    public static final int  SAVE_DEST_NAME =  54 ;
    public static final int  SAVE_PLAY =  55 ;
    public static final int  CURRENT_PART =  56 ;
    public static final int  PART_PATCH_BANK =  57 ;
    public static final int  PART_PATCH_NUM =  58 ;
    public static final int  PART_PATCH_NAME =  59 ;
    public static final int  PART_MODE =  60 ;
    public static final int  PART_VOLUME =  61 ;
    public static final int  PART_OUTPUT =  62 ;
    public static final int  PART_KEY_LIMIT_LOW =  63 ;
    public static final int  PART_KEY_LIMIT_HIGH =  64 ;
    public static final int  PART_VEL_LIMIT_LOW =  65 ;
    public static final int  PART_VEL_LIMIT_HIGH =  66 ;
    public static final int  PART_TRANSPOSE =  67 ;
    public static final int  PART_DETUNE =  68 ;
    public static final int  PART_SUS_ENABLE =  69 ;
    public static final int  PART_DELAY =  70 ;
    public static final int  PART_UNI_NOTE_PRIORITY =  71 ;
    public static final int  PART_MTUNE_TAB =  72 ;
    public static final int  PART_MTUNE_KEY =  73 ;
    public static final int  PART_MIDI_XMIT_CHAN =  74 ;
    public static final int  PART_PLAY_MODE =  75 ;
    public static final int  PART_PROG_CHANGE_XMIT =  76 ;
    public static final int  PATCH_STRUCTURE =  77 ;
    public static final int  PATCH_HARD_SYNC =  78 ;
    public static final int  CURRENT_WAVE =  79 ;
    public static final int  PATCH_PITCH_MACRO =  80 ;
    public static final int  PATCH_FILTER_MACRO =  81 ;
    public static final int  PATCH_AMP_MACRO =  82 ;
    public static final int  PATCH_PAN_MACRO =  83 ;
    public static final int  PATCH_ENV_MACRO =  84 ;
    public static final int  PATCH_PITCH_BEND_RANGE =  85 ;
    public static final int  PATCH_PITCH_RAMP_AMT =  86 ;
    public static final int  PATCH_PITCH_RAMP_RATE =  87 ;
    public static final int  PATCH_PITCH_VEL_AMT =  88 ;
    public static final int  PITCH_SOURCE_1 =  89 ;
    public static final int  PITCH_SOURCE_1_AMOUNT =  90 ;
    public static final int  PITCH_SOURCE_2 =  91 ;
    public static final int  PITCH_SOURCE_2_AMOUNT =  92 ;
    public static final int  FILTER_MOD_CUTOFF =  93 ;
    public static final int  FILTER_MOD_TRACKING =  94 ;
    public static final int  FILTER_EXCITER_AMOUNT =  95 ;
    public static final int  FILTER_MOD_SOURCE1 =  96 ;
    public static final int  FILTER_MOD_SOURCE1_AMT =  97 ;
    public static final int  FILTER_MOD_SOURCE2 =  98 ;
    public static final int  FILTER_MOD_SOURCE2_AMT =  99 ;
    public static final int  GP_ENV_LEVEL_0 =  100 ;
    public static final int  GP_ENV_LEVEL_1 =  101 ;
    public static final int  GP_ENV_LEVEL_2 =  102 ;
    public static final int  GP_ENV_LEVEL_3 =  103 ;
    public static final int  GP_ENV_LEVEL_4 =  104 ;
    public static final int  GP_ENV_RATE_1 =  105 ;
    public static final int  GP_ENV_RATE_2 =  106 ;
    public static final int  GP_ENV_RATE_3 =  107 ;
    public static final int  GP_ENV_RATE_4 =  108 ;
    public static final int  GP_VEL_ENV_AMT =  109 ;
    public static final int  AMP_ENV_LEVEL_0 =  110 ;
    public static final int  AMP_ENV_LEVEL_1 =  111 ;
    public static final int  AMP_ENV_LEVEL_2 =  112 ;
    public static final int  AMP_ENV_LEVEL_3 =  113 ;
    public static final int  AMP_ENV_RATE_1 =  114 ;
    public static final int  AMP_ENV_RATE_2 =  115 ;
    public static final int  AMP_ENV_RATE_3 =  116 ;
    public static final int  AMP_ENV_RATE_4 =  117 ;
    public static final int  AMP_MOD_VEL_ENV_AMOUNT =  118 ;
    public static final int  AMP_MOD_SOURCE_1 =  119 ;
    public static final int  AMP_MOD_SOURCE_1_AMOUNT =  120 ;
    public static final int  AMP_MOD_SOURCE_2 =  121 ;
    public static final int  AMP_MOD_SOURCE_2_AMOUNT =  122 ;
    public static final int  AMP_MOD_VEL_ATTACK_RATE =  123 ;
    public static final int  AMP_MOD_KBD_DECAY_RATE =  124 ;
    public static final int  LFO1_RATE =  125 ;
    public static final int  LFO1_INITIAL_AMOUNT =  126 ;
    public static final int  LFO1_SHAPE =  127 ;
    public static final int  LFO1_SYNC =  128 ;
    public static final int  LFO1_DELAY =  129 ;
    public static final int  LFO1_FADE_IN =  130 ;
    public static final int  LFO1_DEPTH_MOD_SOURCE =  131 ;
    public static final int  LFO1_DEPTH_MOD_SRC_AMT =  132 ;
    public static final int  LFO1_RATE_MOD_SOURCE =  133 ;
    public static final int  LFO1_RATE_MOD_SRC_AMT =  134 ;
    public static final int  LFO2_RATE =  135 ;
    public static final int  LFO2_INITIAL_AMOUNT =  136 ;
    public static final int  LFO2_SHAPE =  137 ;
    public static final int  LFO2_SYNC =  138 ;
    public static final int  LFO2_DELAY =  139 ;
    public static final int  LFO2_FADE_IN =  140 ;
    public static final int  LFO2_DEPTH_MOD_SOURCE =  141 ;
    public static final int  LFO2_DEPTH_MOD_SRC_AMT =  142 ;
    public static final int  LFO2_RATE_MOD_SOURCE =  143 ;
    public static final int  LFO2_RATE_MOD_SRC_AMT =  144 ;
    public static final int  PAN_VELOCITY_AMOUNT =  145 ;
    public static final int  PAN_KEYBOARD_AMOUNT =  146 ;
    public static final int  WAVEA_BANK =  147 ;
    public static final int  WAVEA_NUM =  148 ;
    public static final int  WAVEA_NAME =  149 ;
    public static final int  WAVEA_LEVEL =  150 ;
    public static final int  WAVEA_TUNE_COARSE =  151 ;
    public static final int  WAVEA_TUNE_FINE =  152 ;
    public static final int  WAVEA_TUNE_SLOPE =  153 ;
    public static final int  WAVEB_BANK =  154 ;
    public static final int  WAVEB_NUM =  155 ;
    public static final int  WAVEB_NAME =  156 ;
    public static final int  WAVEB_LEVEL =  157 ;
    public static final int  WAVEB_TUNE_COARSE =  158 ;
    public static final int  WAVEB_TUNE_FINE =  159 ;
    public static final int  WAVEB_TUNE_SLOPE =  160 ;
    public static final int  WAVEC_BANK =  161 ;
    public static final int  WAVEC_NUM =  162 ;
    public static final int  WAVEC_NAME =  163 ;
    public static final int  WAVEC_LEVEL =  164 ;
    public static final int  WAVEC_TUNE_COARSE =  165 ;
    public static final int  WAVEC_TUNE_FINE =  166 ;
    public static final int  WAVEC_TUNE_SLOPE =  167 ;
    public static final int  WAVED_BANK =  168 ;
    public static final int  WAVED_NUM =  169 ;
    public static final int  WAVED_NAME =  170 ;
    public static final int  WAVED_LEVEL =  171 ;
    public static final int  WAVED_TUNE_COARSE =  172 ;
    public static final int  WAVED_TUNE_FINE =  173 ;
    public static final int  WAVED_TUNE_SLOPE =  174 ;
    public static final int  WAVE_SEQ_NUM =  175 ;
    public static final int  WAVE_SEQ_BANK =  176 ;
    public static final int  WAVE_SEQ_NAME =  177 ;
    public static final int  WAVE_SEQ_STEP =  178 ;
    public static final int  WAVE_SEQ_WAVE_BANK =  179 ;
    public static final int  WAVE_SEQ_WAVE_NUM =  180 ;
    public static final int  WAVE_SEQ_WAVE_NAME =  181 ;
    public static final int  WAVE_SEQ_COARSE =  182 ;
    public static final int  WAVE_SEQ_FINE =  183 ;
    public static final int  WAVE_SEQ_LEVEL =  184 ;
    public static final int  WAVE_SEQ_DURATION =  185 ;
    public static final int  WAVE_SEQ_XFADE =  186 ;
    public static final int  WAVE_SEQ_LOOP_START =  187 ;
    public static final int  WAVE_SEQ_LOOP_END =  188 ;
    public static final int  WAVE_SEQ_REPEATS =  189 ;
    public static final int  WAVE_SEQ_START_STEP =  190 ;
    public static final int  WAVE_SEQ_MOD_SRC =  191 ;
    public static final int  WAVE_SEQ_MOD_AMT =  192 ;
    public static final int  MIX_ENV_POINT =  193 ;
    public static final int  MIX_ENV_RATE =  194 ;
    public static final int  MIX_ENV_X =  195 ;
    public static final int  MIX_ENV_Y =  196 ;
    public static final int  MIX_PERCENT_A =  197 ;
    public static final int  MIX_PERCENT_B =  198 ;
    public static final int  MIX_PERCENT_C =  199 ;
    public static final int  MIX_PERCENT_D =  200 ;
    public static final int  MIX_ENV_LOOP =  201 ;
    public static final int  MIX_ENV_REPEATS =  202 ;
    public static final int  MIX_MOD_X_SOURCE1 =  203 ;
    public static final int  MIX_MOD_X_SRC1_AMT =  204 ;
    public static final int  MIX_MOD_X_SOURCE2 =  205 ;
    public static final int  MIX_MOD_X_SRC2_AMT =  206 ;
    public static final int  MIX_MOD_Y_SOURCE1 =  207 ;
    public static final int  MIX_MOD_Y_SRC1_AMT =  208 ;
    public static final int  MIX_MOD_Y_SOURCE2 =  209 ;
    public static final int  MIX_MOD_Y_SRC2_AMT =  210 ;
    public static final int  COPY_MACRO_MODULE =  211 ;
    public static final int  COPY_MACRO_SOURCE_WAVE =  212 ;
    public static final int  COPY_MACRO_SOURCE_BANK =  213 ;
    public static final int  COPY_MACRO_SOURCE_NUM =  214 ;
    public static final int  COPY_MACRO_SOURCE_NAME =  215 ;
    public static final int  COPY_MACRO_DEST_MODULE =  216 ;
    public static final int  COPY_MACRO_DEST_WAVE =  217 ;
    public static final int  COPY_MACRO_DEST_BANK =  218 ;
    public static final int  COPY_MACRO_DEST_NUM =  219 ;
    public static final int  COPY_MACRO_DEST_NAME =  220 ;
    public static final int  COPY_DEST_PART =  221 ;
    public static final int  COPY_DEST_PART_PATCH_BLANK =  222 ;
    public static final int  COPY_DEST_PART_PATCH_NUM =  223 ;
    public static final int  COPY_DEST_PART_PATCH_NAME =  224 ;
    public static final int  COPY_WS_SOURCE_FROM_STEP =  225 ;
    public static final int  COPY_WS_SOURCE_FROM_BANK =  226 ;
    public static final int  COPY_WS_SOURCE_FROM_NUM =  227 ;
    public static final int  COPY_WS_SOURCE_FROM_NAME =  228 ;
    public static final int  COPY_WS_SOURCE_TO_STEP =  229 ;
    public static final int  COPY_WS_SOURCE_TO_BANK =  230 ;
    public static final int  COPY_WS_SOURCE_TO_NUM =  231 ;
    public static final int  COPY_WS_SOURCE_TO_NAME =  232 ;
    public static final int  COPY_WS_DEST_BANK =  233 ;
    public static final int  COPY_WS_DEST_NUM =  234 ;
    public static final int  COPY_WS_DEST_NAME =  235 ;
    public static final int  COPY_WS_DEST_AFTER_STEP =  236 ;
    public static final int  COPY_WS_DEST_AFTER_BANK =  237 ;
    public static final int  COPY_WS_DEST_AFTER_NUM =  238 ;
    public static final int  COPY_WS_DEST_AFTER_NAME =  239 ;
    public static final int  COPY_WS_DEST_BEFORE_STEP =  240 ;
    public static final int  COPY_WS_DEST_BEFORE_BANK =  241 ;
    public static final int  COPY_WS_DEST_BEFORE_NUM =  242 ;
    public static final int  COPY_WS_DEST_BEFORE_NAME =  243 ;
    public static final int  MTUNE_C =  244 ;
    public static final int  MTUNE_CS =  245 ;
    public static final int  MTUNE_D =  246 ;
    public static final int  MTUNE_DS =  247 ;
    public static final int  MTUNE_E =  248 ;
    public static final int  MTUNE_F =  249 ;
    public static final int  MTUNE_FS =  250 ;
    public static final int  MTUNE_G =  251 ;
    public static final int  MTUNE_GS =  252 ;
    public static final int  MTUNE_A =  253 ;
    public static final int  MTUNE_AS =  254 ;
    public static final int  MTUNE_B =  255 ;
    public static final int  CURRENT_MTUNE =  256 ;
    public static final int  FX_PLACEMENT =  257 ;
    public static final int  FX1_PROG =  258 ;
    public static final int  FX2_PROG =  259 ;
    public static final int  FX_MIX_3 =  260 ;
    public static final int  FX_MIX_4 =  261 ;
    public static final int  FX_MOD_3 =  262 ;
    public static final int  FX_MOD_4 =  263 ;
    public static final int  FX_MOD_AMT_3 =  264 ;
    public static final int  FX_MOD_AMT_4 =  265 ;
    public static final int  CURRENT_FX =  266 ;
    public static final int  FX_PROG =  267 ;
    public static final int  FX_FOOTSWITCH_ENABLE1 =  268 ;
    public static final int  FX_FOOTSWITCH_ENABLE6 =  269 ;
    public static final int  FX_LFO_SHAPE =  270 ;
    public static final int  FX_MOD1 =  271 ;
    public static final int  FX_MOD2 =  272 ;
    public static final int  FX_MOD3 =  273 ;
    public static final int  FX_MOD4 =  274 ;
    public static final int  FX_MOD5 =  275 ;
    public static final int  FX_MOD6 =  276 ;
    public static final int  FX_MOD7 =  277 ;
    public static final int  FX_MOD8 =  278 ;
    public static final int  FX_MOD10 =  279 ;
    public static final int  FX_LFO_RATE1 =  280 ;
    public static final int  FX_LFO_RATE3 =  281 ;
    public static final int  FX_LFO_RATE4 =  282 ;
    public static final int  FX_LFO_RATE5 =  283 ;
    public static final int  FX_LFO_RATE6 =  284 ;
    public static final int  FX_LFO_RATE7 =  285 ;
    public static final int  FX_SPLIT_POINT2 =  286 ;
    public static final int  FX_SPLIT_POINT3 =  287 ;
    public static final int  FX_SPLIT_POINT10 =  288 ;
    public static final int  FX_DELAY_FACTOR7 =  289 ;
    public static final int  FX_TOP_DELAY3 =  290 ;
    public static final int  FX_WG_JUCT_MIX10 =  291 ;
    public static final int  FX_EQ_FREQ_LOW0 =  292 ;
    public static final int  FX_EQ_FREQ_MID2 =  293 ;
    public static final int  FX_EQ_FREQ_HIGH7 =  294 ;
    public static final int  FX_EQ_WIDTH6 =  295 ;
    public static final int  FX_100_WET_DRY0 =  296 ;
    public static final int  FX_100_WET_DRY3 =  297 ;
    public static final int  FX_100_WET_DRY4 =  298 ;
    public static final int  FX_10_WET_DRY0 =  299 ;
    public static final int  FX_10_WET_DRY3 =  300 ;
    public static final int  FX_10_WET_DRY4 =  301 ;
    public static final int  FX_UPARAM0 =  302 ;
    public static final int  FX_UPARAM1 =  303 ;
    public static final int  FX_UPARAM2 =  304 ;
    public static final int  FX_UPARAM3 =  305 ;
    public static final int  FX_UPARAM4 =  306 ;
    public static final int  FX_UPARAM5 =  307 ;
    public static final int  FX_UPARAM6 =  308 ;
    public static final int  FX_UPARAM7 =  309 ;
    public static final int  FX_UPARAM8 =  310 ;
    public static final int  FX_UPARAM9 =  311 ;
    public static final int  FX_UPARAM10 =  312 ;
    public static final int  FX_UPARAM11 =  313 ;
    public static final int  FX_UPARAM12 =  314 ;
    public static final int  FX_UPARAM13 =  315 ;
    public static final int  FX_PARAM0 =  316 ;
    public static final int  FX_PARAM1 =  317 ;
    public static final int  FX_PARAM2 =  318 ;
    public static final int  FX_PARAM3 =  319 ;
    public static final int  FX_PARAM4 =  320 ;
    public static final int  FX_PARAM5 =  321 ;
    public static final int  FX_PARAM6 =  322 ;
    public static final int  FX_PARAM7 =  323 ;
    public static final int  FX_PARAM8 =  324 ;
    public static final int  FX_PARAM9 =  325 ;
    public static final int  FX_PARAM10 =  326 ;
    public static final int  FX_PARAM11 =  327 ;
    public static final int  FX_PARAM12 =  328 ;
    public static final int  FX_PARAM13 =  329 ;
    public static final int  FX_DEST_TYPE =  330 ;
    public static final int  FX_DEST_PROG =  331 ;
    public static final int  FX_DEST_FX_NUM =  332 ;
    public static final int  FX_DEST_PLACEMENT =  333 ;
    public static final int  FX_DEST_FX1 =  334 ;
    public static final int  FX_DEST_FX2 =  335 ;
    public static final int  WAVE_MUTE =  336 ;
    public static final int  WAVESEQ_WAVE =  337 ;
    public static final int  WAVE_SEQ_LOOP_DIR =  338 ;
    public static final int  WAVESEQ_COMPAND_SCALE =  339 ;
    public static final int  FOOT_DAMPER_FUNCTION =  340 ;
    public static final int  FOOT_DAMPER_POLARITY =  341 ;
    public static final int  FOOT_ASSIGN_1_FUNCTION =  342 ;
    public static final int  FOOT_ASSIGN_1_POLARITY =  343 ;
    public static final int  FOOT_ASSIGN_2_FUNCTION =  344 ;
    public static final int  FOOT_ASSIGN_2_POLARITY =  345 ;
    public static final int  BANK_COPY_TYPE =  346 ;
    public static final int  ENV1_MOD_VEL_RATE =  347 ;
    public static final int  ENV1_MOD_KBD_RATE =  348 ;
    public static final int  WS_MIDI_CLOCK =  349 ;
    public static final int  VIEW_BANK =  350 ;
    public static final int  VIEW_PERF_NUM =  351 ;
    public static final int  VIEW_PERF_NAME =  352 ;
    public static final int  COPY_FX_SOURCE_BANK =  353 ;
    public static final int  COPY_FX_SOURCE_NUM =  354 ;
    public static final int  COPY_FX_SOURCE_NAME =  355 ;
    public static final int  FX_11_WET_DRY0 =  356 ;
    public static final int  FX_11_WET_DRY3 =  357 ;
    public static final int  FX_11_WET_DRY4 =  358 ;
    public static final int  FX_RAMP5 =  359 ;
    public static final int  SOURCE_CARD_NAME =  360 ;
    public static final int  DEST_CARD_NAME =  361 ;
    public static final int  WAVEA_BUS_A =  362 ;
    public static final int  WAVEA_BUS_B =  363 ;
    public static final int  WAVEA_BUS_C =  364 ;
    public static final int  WAVEA_BUS_D =  365 ;
    public static final int  WAVEB_BUS_A =  366 ;
    public static final int  WAVEB_BUS_B =  367 ;
    public static final int  WAVEB_BUS_C =  368 ;
    public static final int  WAVEB_BUS_D =  369 ;
    public static final int  WAVEC_BUS_A =  370 ;
    public static final int  WAVEC_BUS_B =  371 ;
    public static final int  WAVEC_BUS_C =  372 ;
    public static final int  WAVEC_BUS_D =  373 ;
    public static final int  WAVED_BUS_A =  374 ;
    public static final int  WAVED_BUS_B =  375 ;
    public static final int  WAVED_BUS_C =  376 ;
    public static final int  WAVED_BUS_D =  377 ;
    public static final int  COPY_PART_SOURCE_BANK =  378 ;
    public static final int  GLOBAL_UTIL_DEST_BANK =  379 ;
    public static final int  REMAP_TO_JOY_X =  380 ;
    public static final int  REMAP_TO_JOY_Y =  381 ;
    public static final int  REMAP_TO_FX_SWITCH =  382 ;
    public static final int  PROG_TO_MULTI_FX =  383 ;
    public static final int  CHANGE_MULTI_WITH =  384 ;
    public static final int  ANALOG_LEV_1 =  385 ;
    public static final int  ANALOG_LEV_2 =  386 ;
    public static final int  ANALOG_CHAN_1 =  387 ;
    public static final int  ANALOG_CHAN_2 =  388 ;
    public static final int  ANALOG_1_BUS_A =  389 ;
    public static final int  ANALOG_1_BUS_B =  390 ;
    public static final int  ANALOG_1_BUS_C =  391 ;
    public static final int  ANALOG_1_BUS_D =  392 ;
    public static final int  ANALOG_2_BUS_A =  393 ;
    public static final int  ANALOG_2_BUS_B =  394 ;
    public static final int  ANALOG_2_BUS_C =  395 ;
    public static final int  ANALOG_2_BUS_D =  396 ;
    public static final int  FX_BUS0 =  397 ;
    public static final int  FX_BUS2 =  398 ;
    public static final int  ANALOG_BUS_MACRO =  399 ;
    public static final int  ANALOG_1_FILTER =  400 ;
    public static final int  ANALOG_2_FILTER =  401 ;
    public static final int  ANALOG_1_EXCITER =  402 ;
    public static final int  ANALOG_2_EXCITER =  403 ;
    public static final int  ANALOG_INPUT_DISABLE =  404 ;
    public static final int  COMP_CONTROL0 =  405 ;
    public static final int  LOCAL_XPOSE =  406 ;
    public static final int  SYSEX_XMIT_TYPE =  407 ;
    public static final int  SYSEX_XMIT_BANK =  408 ;
    public static final int  SYSEX_XMIT_NUM =  409 ;
    public static final int  WAVE_BANK =  410 ;
    public static final int  WAVE_NUM =  411 ;
    public static final int  WAVE_NAME =  412 ;
    public static final int  WAVE_LEVEL =  413 ;
    public static final int  WAVE_TUNE_COARSE =  414 ;
    public static final int  WAVE_TUNE_FINE =  415 ;
    public static final int  WAVE_TUNE_SLOPE =  416 ;
    public static final int  WAVE_BUS_A =  417 ;
    public static final int  WAVE_BUS_B =  418 ;
    public static final int  WAVE_BUS_C =  419 ;
    public static final int  WAVE_BUS_D =  420 ;
    public static final int  MIX_ENV_RATE_1 =  421 ;
    public static final int  MIX_ENV_RATE_2 =  422 ;
    public static final int  MIX_ENV_RATE_3 =  423 ;
    public static final int  MIX_ENV_RATE_4 =  424 ;
    public static final int  PART_SOLO_STATUS =  425 ;
    public static final int  WS_STEP_SOLO_STATUS =  426 ;
    public static final int  PERF_MIDI_MODE =  427 ;
    public static final int  MULTI_MIDI_MODE =  428 ;
    public static final int  CURRENT_DEMO =  429 ;
    public static final int  CURRENT_DEMO_NAME =  430 ;
    public static final int  WAVE_WAVE =  431 ;
    public static final int  MULTISET_NAME =  432 ;
    public static final int  MULTISET_OUTPUT =  433 ;
    public static final int  NUM_MULTI_CHANS =  434 ;
    public static final int  RESET_ACT_SENS_CONTROL =  435 ;
    public static final int  REC_BANK_CHANGE =  436 ;
    public static final int  KSD_ENABLE =  437 ;
    public static final int  VERSION_NUM =  438 ;
    public static final int  VERSION_DATE =  439 ;
    public static final int  ERROR_NUMBER =  440 ;
    public static final int  TASK_NUMBER =  441 ;
    public static final int  STATUS_REGISTER =  442 ;
    public static final int  CRASH_LEVEL =  443 ;
    public static final int  PROGRAM_COUNTER =  444 ;
    public static final int  C_PROGRAM_COUNTER =  445 ;
    public static final int  STACK_POINTER =  446 ;
    public static final int  USER_STACK_POINTER =  447 ;
    public static final int  POOL_LEVEL =  448 ;
    public static final int  POOL_LEVEL_MAX =  449 ;
    public static final int  BOGUS_PARAM =  450 ;
    public static final int  EXECUTE_PLAY_DEMO =  451 ;
    public static final int  EXECUTE_SOLO_PART =  452 ;
    public static final int  EXECUTE_INIT_PART =  453 ;
    public static final int  EXECUTE_COPY_PART =  454 ;
    public static final int  EXECUTE_COPY_MODULES =  455 ;
    public static final int  EXECUTE_COPY_FX_ALL =  456 ;
    public static final int  EXECUTE_COPY_FX_PARAMS =  457 ;
    public static final int  EXECUTE_COPY_FX_MIX =  458 ;
    public static final int  EXECUTE_INSERT_WS_STEP =  459 ;
    public static final int  EXECUTE_DELETE_WS_STEP =  460 ;
    public static final int  EXECUTE_ZONE_KEY_LAYER =  461 ;
    public static final int  EXECUTE_ZONE_KEY_SPLIT =  462 ;
    public static final int  EXECUTE_ZONE_VEL_SWITCH =  463 ;
    public static final int  EXECUTE_ZONE_VEL_LAYER =  464 ;
    public static final int  EXECUTE_SOLO_WS_STEP =  465 ;
    public static final int  EXECUTE_WRITE =  466 ;
    public static final int  EXECUTE_COMPARE =  467 ;
    public static final int  EXECUTE_INIT_PATCH =  468 ;
    public static final int  EXECUTE_CENTER_POINT =  469 ;
    public static final int  EXECUTE_SYSEX_XMIT =  470 ;
    public static final int  EXECUTE_SYSEX_MULTI_DUMP =  471 ;
    public static final int  EXECUTE_BANK_MOVE =  472 ;
    public static final int  EXECUTE_FORMAT_CARD =  473 ;
    public static final int  EXECUTE_WAVESEQ_COMPAND =  474 ;
    public static final int  EXECUTE_WAVESEQ_INIT =  475 ;
    public static final int  EXECUTE_WAVESEQ_COPY =  476 ;
    public static final int  EXECUTE_WAVESEQ_STEP_COPY =  477 ;
    public static final int  EXECUTE_ENABLE_SHOW =  478 ;
    public static final int  EXECUTE_INIT_RAM =  479 ;
    public static final int  EXECUTE_LOAD_DIAG =  480 ;
    public static final int  EXECUTE_SAVE_DIAG =  481 ;
    public static final int  EXECUTE_EXIT_LEVEL =  482 ;
    public static final int  EXECUTE_INIT_PERF =  483 ;
    public static final int  PARAM_END =  484 ;


    // According to the SR Overview, the wavestation bank numbers are not
    // in the expected order.  So when you want to change a bank, you have to
    // send the right WS Bank request.  However while the SR Overview gives
    // the bank ordering when you send out parameters, it appears from the SR Sysex
    // document that when you set the appropriate bits in Bank_Exp for patch or
    // performance download, there is a *different* ordering still -- namely RAM3,
    // RAM4, and RAM5 have different ordering.
    //
    // The four arrays following this table are intended to convert between these banks
    // as if they were functions, for example: edisynToWSBank[5] -> 7
    // 
    //      Bank Name    Edisyn Bank        WS Bank (Params)        WS Bank (Bank_Exp)
    //      RAM1         0                          0                       0
    //      RAM2         1                          1                       1
    //      RAM3         2                          4                       6
    //      ROM4         3                          5                       4
    //      ROM5         4                          6                       5
    //      ROM6         5                          7                       7
    //      ROM7         6                          8                       8
    //      ROM8         7                          9                       9
    //      ROM9         8                          10                      10
    //      ROM10        9                          11                      11
    //      ROM11        10                         2                       2
    //      CARD         11                         3                       3

    public static final int[] edisynToWSBank = new int[] { 0, 1, 4, 5, 6, 7, 8, 9, 10, 11, 2, 3 };
    public static final int[] wsToEdisynBank = new int[] { 0, 1, 10, 11, 2, 3, 4, 5, 6, 7, 8, 9 };
    public static final int[] wsBankExpToEdisynBank = new int[] { 0, 1, 10, 11, 3, 4, 2, 5, 6, 7, 8, 9 };
    public static final int[] edisynToWSBankExpBank = new int[] {0, 1, 6, 4, 5, 7, 8, 9, 10, 11, 2, 3 };


    public static final int DEFAULT_PART = 1;
    public static final int DEFAULT_WAVE = 1;
    public static final int DEFAULT_PATCH_NUM = 0;
    public static final int DEFAULT_PATCH_BANK = 0;
    public static final int PERFORMANCE_MIDI_MODE = 0;
    public static final int MULTISET_MIDI_MODE = 1;


    public byte[] paramBytes(int param)
        {
        return paramBytes(param, new char[0]);
        }

    public byte[] paramBytes(int param, int val)
        {
        if (val > 0)
            return paramBytes(param, ("+" + val).toCharArray());
        else
            return paramBytes(param, ("" + val).toCharArray());
        }

    public byte[] paramBytes(int param, String str)
        {
        return paramBytes(param, str.toCharArray());
        }

    public byte[] paramBytes(int param, char[] chars)
        {
        byte[] mesg = new byte[chars.length + 9];
        mesg[0] = (byte)0xF0;
        mesg[1] = (byte)0x42;
        mesg[2] = (byte)(0x30 + getChannelOut());
        mesg[3] = (byte)0x28;
        mesg[4] = (param >= 407 ? (byte)0x43 : (param >= 380 ? (byte)0x42 : (byte)0x41));
        mesg[5] = (byte)(param & 127);
        mesg[6] = (byte)((param >>> 7) & 127);
        mesg[mesg.length - 2] = (byte)0x0;
        mesg[mesg.length - 1] = (byte)0xF7;
        for(int i = 0; i < chars.length; i++)
            {
            mesg[i + 7] = (chars[i] == 32 ? (byte)0x7F : (byte)chars[i]);
            }
        return mesg;
        }
                
    int range(int a)
        {
        while (a > 255) a -= 256;
        while (a < 0) a += 256;
        return a;
        }
               
    // only extracts WHOLE NUMBERS, ignores - and + and .
    int[] extractNumbers(String str)
        {
        String[] s = str.split("\\D");
        int count = 0;
        for(int i = 0; i < s.length; i++)
            if (s[i] != null && s[i].length() > 0)
                count++;
        int[] vals = new int[count];
        count=0;
        for(int i = 0; i < s.length; i++)
            if (s[i] != null && s[i].length() > 0)
                vals[count++] = Integer.parseInt(s[i]);
        return vals;
        } 
        
    public int writeByte(int val, byte[] data, int pos)
        {
        data[pos] = (byte) val;
        return pos + 1;
        }
    public int writeUByte(int val, byte[] data, int pos)
        {
        return writeByte(val, data, pos);
        }
    public int writeWord(int val, byte[] data, int pos)
        {
        data[pos] = (byte)((val >>> 8) & 0xFF);
        data[pos + 1] = (byte) (val & 0xFF);
        return pos + 2;
        }
    public int writeLong(int val, byte[] data, int pos)
        {
        data[pos] = (byte)((val >>> 24) & 0xFF);
        data[pos + 1] = (byte) ((val >>> 16) & 0xFF);
        data[pos + 2] = (byte) ((val >>> 8) & 0xFF);
        data[pos + 3] = (byte) (val & 0xFF);
        return pos + 4;
        }
    public int writeULong(long val, byte[] data, int pos)
        {
        data[pos] = (byte)((val >>> 24) & 0xFF);
        data[pos + 1] = (byte) ((val >>> 16) & 0xFF);
        data[pos + 2] = (byte) ((val >>> 8) & 0xFF);
        data[pos + 3] = (byte) (val & 0xFF);
        return pos + 4;
        }
    public int writeUWord(int val, byte[] data, int pos)
        {
        return writeWord(val, data, pos);
        }
    public int readByte(byte[] data, int pos)
        {
        return data[pos];
        }
    public int readUByte(byte[] data, int pos)
        {
        int b = data[pos];
        if (b < 0) b += 256;
        return b;
        }
    public int readWord(byte[] data, int pos)
        {
        // This is nuanced.  We have to add a signed extension, and we do that
        // by casting to a short first, then when it casts back to an int, 
        // the signed extension is attached. 
        // If we don't do & 0xFF, then if the byte is negative it gets sign-extended
        // which we definitely don't want when OR-ing
        return (short)(((data[pos] & 0xFF) << 8) | (data[pos + 1] & 0xFF));
        }
    public int readUWord(byte[] data, int pos)
        {
        int b = readWord(data, pos);
        if (b < 0) b += 65536;
        return b;
        }
    public int readLong(byte[] data, int pos)
        {
        // If we don't do & 0xFF, then if the byte is negative it gets sign-extended
        // which we definitely don't want when OR-ing
        return (((data[pos] & 0xFF) << 24) | ((data[pos + 1] & 0xFF) << 16) | ((data[pos + 2] & 0xFF) << 8) | (data[pos + 3] & 0xFF));
        }
    public long readULong(byte[] data, int pos)
        {
        // If we don't do & 0xFF, then if the byte is negative it gets sign-extended
        // which we definitely don't want when OR-ing
        return (((data[pos] & (long)0xFF) << 24) | ((data[pos + 1] & (long)0xFF) << 16) | ((data[pos + 2] & (long)0xFF) << 8) | (data[pos + 3] & (long)0xFF));
        }
        
        
    // nybblizes all the data
    public byte[] nybblize(byte[] data)
        {
        byte[] nybbles = new byte[data.length * 2];
        for(int i = 0; i < data.length; i++)
            {
            nybbles[i * 2] = (byte)(data[i] & 15);
            nybbles[i * 2 + 1] = (byte)((data[i] >>> 4) & 15);
            }
        return nybbles;
        }
                
    // denybblizes all the data starting at OFFSET and going up to, but not including, the final two bytes (checksum and F7)
    public byte[] denybblize(byte[] nybbles, int offset)
        {
        return denybblize(nybbles, offset, (nybbles.length - offset - 2));
        }

    // denybblizes all the data starting at OFFSET and reading LENGTH bytes
    public byte[] denybblize(byte[] nybbles, int offset, int length)
        {
        byte[] data = new byte[length / 2];
        for(int i = 0; i < data.length; i++)
            {
            data[i] = (byte)(nybbles[i * 2 + offset] | (nybbles[i * 2 + offset + 1] << 4));
            }
        return data;
        }

    boolean sendWavestationParametersInBulk = true;
        
    public void addWavestationMenu()
        {
        JMenu menu = new JMenu("Wavestation");
        menubar.add(menu);
        addWavestationMenu(menu);
        }
                
    public void addWavestationMenu(JMenu menu)
        {
        /*
        // classic patch names
                
        JMenu sendParameters = new JMenu("Send Parameters");
        menu.add(sendParameters);
                
        String str = getLastX("SendParameters", getSynthName(), true);
        if (str == null)
        sendWavestationParametersInBulk = true;
        else if (str.equalsIgnoreCase("BULK"))
        sendWavestationParametersInBulk = true;
        else if (str.equalsIgnoreCase("INDIVIDUALLY"))
        sendWavestationParametersInBulk = false;
        else sendWavestationParametersInBulk = true;

        ButtonGroup bg = new ButtonGroup();

        JRadioButtonMenuItem separately = new JRadioButtonMenuItem("Individually");
        separately.addActionListener(new ActionListener()
        {
        public void actionPerformed(ActionEvent evt)
        {
        sendWavestationParametersInBulk = false;
        setLastX("INDIVIDUALLY", "SendParameters", getSynthName(), true);
        }
        });
        sendParameters.add(separately);
        bg.add(separately);
        if (sendWavestationParametersInBulk == false) 
        separately.setSelected(true);

        JRadioButtonMenuItem bulk = new JRadioButtonMenuItem("In Bulk");
        bulk.addActionListener(new ActionListener()
        {
        public void actionPerformed(ActionEvent evt)
        {
        sendWavestationParametersInBulk = true;
        setLastX("BULK", "SendParameters", getSynthName(), true);
        }
        });
        sendParameters.add(bulk);
        bg.add(bulk);
        if (sendWavestationParametersInBulk == true) 
        bulk.setSelected(true);
        */       
        }
        
    public String waveName(int bank, int num)
        {
        if (num < 32)
            {
            return  "WS" + num + " " + KorgWavestationSequence.WAVE_SEQUENCES[bank][num];
            }
        else
            {
            if (bank != BANK_CARD)
                return KorgWavestationSequence.WAVES[num - 32];
            else
                return "Card Wave " + num;
            }
        }

    public static final int BANK_CARD = 11;
    public String[] buildWaves(int bank)
        {
        String[] waves = new String[517];
        
        for(int i = 0; i < 517; i++)
            {
            waves[i] = waveName(bank, i);
            }
        return waves;
        }
        


    }
