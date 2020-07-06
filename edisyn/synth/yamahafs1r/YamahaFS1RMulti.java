/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.yamahafs1r;

import edisyn.*;
import edisyn.gui.*;
import edisyn.util.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.border.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.sound.midi.*;


/**
   A patch editor for the Yamaha FS1R [Multimode].
   
   <p>Some portions of this code were copied, with permission, from the source code of FS1REditor by K_Take (https://synth-voice.sakura.ne.jp/fs1r_editor_english.html)
   to whom I am very grateful. 
        
   @author Sean Luke
*/

public class YamahaFS1RMulti extends Synth
    {
    /// Various collections of parameter names for pop-up menus

    public static final String[] CATEGORIES = new String[]
    { 
    "[None]", "Piano (Pf)", "Chromatic Percussion (Cp)", "Organ (Or)", "Guitar (Gt)", "Bass (Ba)", 
    "Strings/Orchestral (St)", "Ensemble (En)", "Brass (Br)", "Reed (Rd)", "Pipe (Pi)", 
    "Synth Lead (Ld)", "Synth Pad (Pd)", "Synth Sound Effects (Fx)", "Ethnic (Et)", 
    "Percussive (Pc)", "Sound Effects (Se)", "Drums (Dr)", "Synth Comping (Sc)", "Vocal (Vo)", 
    "Combination (Co)", "Material Wave (Wv)", "Sequence (Sq)" 
    };


	// Default names for all voices in banks A...K.  Internal bank is not listed.
    public static final String[][] VOICES = {
        { "Pf Ballad EP", "Pf Clavmann", "Pf Clavmann 2", "Pf Digi Clav", "Pf DX7Classic", "Pf Mtrial Pno", "Pf MtrialPno2", "Pf MtrialPno3", "Pf Real Rose", "Pf Rose Att", "Pf Rose Sft1", "Pf Rose Sft2", "Pf Suit Rose", "Pf Velvt Rose", "Pf 4 Op Clav", "Cp Da Comp", "Cp Synth Bell", "Cp Tabla", "Or B3JazzComp", "Or B3Perc3rd", "Or DrawOrgn", "Or DrawOrgn2", "Or DrawOrgn3", "Or Fs-Organ", "Or Full Drawb", "Or Ham Organ", "Or OR-Right", "Or Organ Fseq", "Or The Lounge", "Gt Jazz Gtr", "Gt Stratmann", "Ba Acid King", "Ba Ana Bass", "Ba AttackBass", "Ba B-Rave", "Ba Bassline 1", "Ba Bassline 2", "Ba BlegBass", "Ba DidgBass", "Ba Dry Syn", "Ba FM Bass", "Ba FundaBass", "Ba HyperFuzz", "Ba JungleBass", "Ba LoFiAcid", "Ba Matze", "Ba Moon Bass", "Ba Phone Bass", "Ba PlastBass", "Ba PunchBass", "Ba Syn Bass", "Ba Technical", "St FairyStrgs", "St JMichel", "St OB String", "St ResoStrgs", "St Saws", "St SloDu Saws", "St SS String", "St SS String2", "En HitMatrial", "Br ANSweep", "Br FS Brass", "Br Hook", "Br ObiehornL", "Br ObiehornR", "Br Quackz", "Br Stab", "Br Swell", "Pi Kuchibue", "Ld Dual Saws2", "Ld DualSquare", "Ld Earth Lead", "Ld Fetish", "Ld Glass Harp", "Ld Glider", "Ld Lead Saw", "Ld Mitosis", "Ld Retronic", "Ld Score Pad", "Ld Tech Lead", "Ld Trance Csm", "Ld Voc Lead", "Pd Add Pad", "Pd Beauty", "Pd Brassetra", "Pd CineSweep", "Pd Fat Pad", "Pd FormantPad", "Pd FormSweep1", "Pd FormSweep2", "Pd FormSweep3", "Pd FormSweep4", "Pd FS Moby II", "Pd Heimdal", "Pd LFO Pad", "Pd Moving", "Pd Nebulous", "Pd OBx Pad", "Pd OBx Pad2", "Pd Octavian", "Pd Paddy", "Pd Qwerty", "Pd Saws&Hold", "Pd Saws2", "Pd SleepyPad", "Pd Spacy Pad", "Pd Starship", "Pd SuperPad", "Pd SweepersVx", "Pd Tech Lead2", "Pd The Seeker", "Pd The Shadow", "Pd Thermal", "Pd VocPhaseA", "Pd Win Pad", "Pd Wind", "Fx Caravan", "Fx DippeDut", "Fx Furry Bell", "Fx Glacial", "Fx Miracle", "Fx MizuGuitar", "Fx Morph", "Fx Nightmare", "Fx RhythmLoop", "Pd Sho", "Fx Spiral" },
        { "Et BagPipe", "Et BagPipe-dl", "Et Gamelan", "Et Gamelan2", "Et Mukkuri", "Et SuikinStr", "Et Thai Boxin", "Et ThumKalimb", "Pc Big-Gamlan", "Pc Eth-Drum1", "Pc Eth-Drum2", "Se Beep VoX", "Se Dark", "Se ForceField", "Se Ghost", "Se Ghost2", "Se Magic", "Se Night", "Se Open Fseq", "Se RadioNoise", "Se Reso SE", "Se Saucer", "Se Scaling SE", "Se Slow Atk", "Se SpaceBomb", "Se WalkinRobo", "Se Warp1", "Se Warp2", "Dr 09 OpenHat", "Dr 09ClHatBel", "Dr Beat BD", "Dr Beat Cym", "Dr Beat SD", "Dr Beat Zap", "Dr Boom", "Dr Choos", "Dr ClosedHat1", "Dr ClosedHat2", "Dr DanceKick", "Dr FS-Kick1", "Dr FS-Kick2", "Dr FS-Kick3", "Dr Hatty", "Dr Hihat", "Dr Nu Kick 1", "Dr Nu Kick 2", "Dr Nu Kick 3", "Dr Nu Snare 1", "Dr Nu Snare 2", "Dr Nu Snare 3", "Dr Open Hat 1", "Dr Open Hat 2", "Dr PowerKick", "Dr Snare", "Dr Tchak", "Dr Tech BD", "Dr Tech HH", "Dr Tech Rim", "Dr Tech SD", "Dr TR Kick", "Dr TR Snare", "Sc DigiSQ1R", "Sc DigiSQ3", "Sc DogBytes", "Sc Fast&Cheap", "Sc Fmt-Pluck", "Sc FunKey", "Sc Funky Tech", "Sc Fusion", "Sc Metallic", "Sc NoiseDecay", "Sc Raymond", "Sc SawSaw", "Sc Snow Decay", "Sc Snow Pixy", "Sc Spellbound", "Sc Syncorgano", "Sc Thin Mini", "Sc VeloSweep", "Sc Vox Tron", "Sc Zansyo", "Sc Zapper", "Vo Celebratn", "Vo Eh Human", "Vo FairyVoice", "Vo FormSweep", "Vo FS-Choir", "Vo FS-Sweep", "Vo Homy", "Vo Human", "Vo Ih Human", "Vo Man_Eh", "Vo NoisyVce", "Vo Oh Human", "Vo Shaman Woo", "Vo Spacy Aaah", "Vo Spacy FX", "Vo SpacySweep", "Vo SweepyVce", "Vo VocoSweep", "Vo VocPhaseB", "Sq AN Arp 1", "Sq AN Arp 2", "Sq Compu Saw", "Sq DigiSQ1", "Sq DigiSQ2", "Sq Drw-EuroDr", "Sq Hard Pulse", "Sq Harry", "Sq New Key", "Sq Power Key", "Sq RythmLoop2", "Sq Saw Pad", "Sq TekBass", "-- FseqBase01", "-- FseqBase02", "-- FseqBase03", "-- FseqBase04", "-- FseqBase05", "-- FseqBase06", "-- FseqBase07", "-- FseqBase08", "-- FseqBase09", "-- FseqBase10", "-- FseqBase11", "-- FseqBase12", "-- FseqBase13", "-- FseqBase14" },
        { "Pf FortePno 1", "Pf FortePno 2", "Pf MM-Piano 1", "Pf MM-Piano 2", "Pf Pianotone1", "Pf Pianotone2", "Pf Pianotone3", "Pf 5thPiano 1", "Pf 5thPiano 2", "Pf PrprdPiano", "Pf Claviano", "Pf BrightPno1", "Pf BrightPno2", "Pf BrightPno3", "Pf Dark Piano", "Pf Digi Piano", "Pf PianoDrops", "Pf PowerPiano", "Pf CP70 1", "Pf CP70 2", "Pf CP70 3", "Pf El.Grand 1", "Pf El.Grand 2", "Pf El.Grand 3", "Pf El.Grand 4", "Pf MM-ElGnd 1", "Pf MM-ElGnd 2", "Pf E.Piano 1", "Pf E.Piano 2", "Pf E.Piano 3", "Pf E.Piano 4", "Pf E.Piano 5", "Pf E.Piano 6", "Pf E.Piano 7", "Pf E.Piano 8", "Pf E.Piano 9", "Pf E.Piano 10", "Pf E.Piano 11", "Pf E.Piano 12", "Pf E.Piano 13", "Pf E.Piano 14", "Pf E.Piano 15", "Pf E.Piano 16", "Pf E.Piano 17", "Pf Aclectic", "Pf DX-Road 1", "Pf DX-Road 2", "Pf DX-Road 3", "Pf DX-Road 4", "Pf DX-Road 5", "Pf BrightEP 1", "Pf BrightEP 2", "Pf EP 1967", "Pf EP 1970", "Pf EP 1980", "Pf EP 1985", "Pf Soft EP 1", "Pf Soft EP 2", "Pf Soft EP 3", "Pf Hard EP 1", "Pf Hard EP 2", "Pf Hard EP 3", "Pf Hard EP 4", "Pf Clicky EP", "Pf Digitine", "Pf Woody EP", "Pf Metaltine", "Pf Tinesquawk", "Pf FullTine 1", "Pf FullTine 2", "Pf Wurli EP", "Pf Wurli Road", "Pf Dark Wurli", "Pf Big Wurlt", "Pf Andrian", "Pf Blustig", "Pf Woodmetal", "Pf CastePiano", "Pf Chorus EP", "Pf BigJazzyEP", "Pf ClearElPno", "Pf NiteclubEP", "Pf CosaRosa", "Pf DX-Ragtime", "Pf Digi Poly", "Pf Duke EP", "Pf DynoRoad", "Pf Clavarpsi", "Pf Wack EP", "Pf HollowKeys", "Pf HonkyTonk1", "Pf HonkyTonk2", "Pf PotlidKeyz", "Pf Knock EP", "Pf Knock Wack", "Pf Mark III", "Pf XtremeTine", "Pf Mod ElPno", "Pf 3D Road", "Pf PinchedEP", "Pf No Tines", "Pf Old Jazz", "Pf Politti", "Pf Pop Piano", "Pf Prc ElPno", "Pf Prds Piano", "Pf Ratio Dob", "Pf ThinnerEP", "Pf Rezzo EP", "Pf RubbaRoad", "Pf SawBellEP", "Pf QuikPlayEP", "Pf Loud Piano", "Pf Urban", "Pf Vics EP", "Pf DX Classic", "Pf ToyPiano 1", "Pf ToyPiano 2", "Pf ToyPiano 3", "Pf ToyPiano 4", "Pf Plasticky", "Pf Harpsi 1", "Pf Harpsi 2", "Pf Harpsi 3", "Pf Harpsi 4", "Pf Harpsi 5", "Pf Harpsi 6", "Pf Harpsi 7" },
        { "Pf Harpsi 8", "Pf Harpsi 9", "Pf HarpsiWire", "Pf AD 1600s 1", "Pf AD 1600s 2", "Pf AD 1900s", "Pf Caffeine", "Pf RazorWire", "Pf Cembalim", "Pf Cembalo", "Pf ElecHarpsi", "Pf Syn Harpsi", "Pf DX-Clavi 1", "Pf DX-Clavi 2", "Pf DX-Clavi 3", "Pf DX-Clavi 4", "Pf DX-Clavi 5", "Pf DX-Clavi 6", "Pf DX-Clavi 7", "Pf MM-Clavi 1", "Pf MM-Clavi 2", "Pf MM-Clavi 3", "Pf BrightClv1", "Pf BrightClv2", "Pf BasoClavi", "Pf ChorusClav", "Pf Clavecin", "Pf Clavi Comp", "Pf ClaviExcel", "Pf ClaviPluck", "Pf ClaviStaff", "Pf Mute Clavi", "Pf Revinett", "Pf SkeltonClv", "Cp Celesta 1", "Cp Celesta 2", "Cp Celesta 3", "Cp Celesta 4", "Cp MM-Celesta", "Cp Halloween", "Cp Glocken 1", "Cp Glocken 2", "Cp Glocken 3", "Cp Glocken 4", "Cp Glocken 5", "Cp Glocken 6", "Cp HamerGlock", "Cp Magiglokk", "Cp AnvilGlock", "Cp MetalGlock", "Cp Perc Glock", "Cp Glokenring", "Cp SynGlock 1", "Cp SynGlock 2", "Cp MusicBox 1", "Cp MusicBox 2", "Cp MusicBox 3", "Cp MusicBox 4", "Cp MusicBox 5", "Cp MusicBox 6", "Cp MusicBox 7", "Cp MusicBox 8", "Cp MusicBox 9", "Cp MusicBox10", "Cp DX-Vibe 1", "Cp DX-Vibe 2", "Cp DX-Vibe 3", "Cp DX-Vibe 4", "Cp MM-Vibe 1", "Cp MM-Vibe 2", "Cp LFO Vibe", "Cp Vocal Vibe", "Cp Vibetron", "Cp VibraPhone", "Cp DX-Marimb1", "Cp DX-Marimb2", "Cp DX-Marimb3", "Cp DX-Marimb4", "Cp DX-Marimb5", "Cp DX-Marimb6", "Cp DX-Marimb7", "Cp TineMallet", "Cp Thumbpick", "Cp EchoMalet1", "Cp EchoMalet2", "Cp EchoMalet3", "Cp Congorimba", "Cp Bamburimba", "Cp BrightMrmb", "Cp Guitarimba", "Cp MellowMrmb", "Cp Metal Mrmb", "Cp DX-Xylo 1", "Cp DX-Xylo 2", "Cp DX-Xylo 3", "Cp DX-Xylo 4", "Cp DX-Xylo 5", "Cp DX-Xylo 6", "Cp Dual Xylo", "Cp Xylo Log", "Cp Syn Xylo", "Cp Digi Xylo", "Cp DX-Bell 1", "Cp DX-Bell 2", "Cp DX-Bell 3", "Cp DX-Bell 4", "Cp DX-Bell 5", "Cp DX-Bell 6", "Cp DX-Bell 7", "Cp DX-Bell 8", "Cp DX-Bell 9", "Cp DX-Bell 10", "Cp DX-Bell 11", "Cp DX-Bell 12", "Cp SparklBell", "Cp Wire Bell", "Cp DualSparkl", "Cp BellGlassy", "Cp MM-Bell", "Cp Crystal 1", "Cp Crystal 2", "Cp SoftBell 1", "Cp SoftBell 2", "Cp Bell Pluck", "Cp Blow Bell", "Cp Carillon", "Cp BellKeyzis", "Cp Digi Log" },
        { "Cp DumBells", "Cp MellowBell", "Cp Mini Bell", "Cp Child Bell", "Cp PPP Thing", "Cp Stonemetal", "Cp Syn Chime", "Cp Air Bell", "Cp WrapRound", "Cp TempleBel1", "Cp TempleBel2", "Cp TempleBel3", "Cp TempleBel4", "Cp TempleBel5", "Cp DX-Dlcm 1", "Cp DX-Dlcm 2", "Cp DX-Dlcm 3", "Cp Frozentime", "Cp MetalDlcmr", "Cp Silk Road", "Or Full Organ", "Or DrawOrgan1", "Or DrawOrgan2", "Or DrawOrgan3", "Or DrawOrgan4", "Or DrawOrgan5", "Or DrawOrgan6", "Or DrawOrgan7", "Or DrawOrgan8", "Or DrawOrgan9", "Or DrawOrgn10", "Or DrawOrgn11", "Or DrawOrgn12", "Or DrawOrgn13", "Or DrawOrgn14", "Or DrawOrgn15", "Or DrawOrgn16", "Or Organsynth", "Or ChorusOrgn", "Or RotaryOrgn", "Or CirkusOrgn", "Or JazzDrwbr", "Or Keyclick", "Or VibraOrgan", "Or Farf Out", "Or Grinder", "Or JazzOrgan1", "Or JazzOrgan2", "Or PercOrgan1", "Or PercOrgan2", "Or PercOrgan3", "Or PercOrgan4", "Or PercOrgan5", "Or PercOrgan6", "Or PercOrgan7", "Or PercOrgan8", "Or PercOrgan9", "Or PercOrgn10", "Or PercOrgn11", "Or PercOrgn12", "Or PercOrgn13", "Or PercOrgn14", "Or PercOrgn15", "Or PercOrgn16", "Or PercOrgn17", "Or XtraPerc", "Or Road Organ", "Or Fluteorgan", "Or ClickNoise", "Or Novalis", "Or TouchOrgan", "Or RockOrgan1", "Or RockOrgan2", "Or RockOrgan3", "Or RockOrgan4", "Or RockOrgan5", "Or RockOrgan6", "Or RockOrgan7", "Or RockOrgan8", "Or RockOrgan9", "Or RockOrgn10", "Or RockOrgn11", "Or RockOrgn12", "Or RockOrgn13", "Or RockOrgn14", "Or RockOrgn15", "Or Vox Organ", "Or SynOrgan 1", "Or SynOrgan 2", "Or PlasticOrg", "Or PipeOrgan1", "Or PipeOrgan2", "Or PipeOrgan3", "Or PipeOrgan4", "Or PipeOrgan5", "Or PipeOrgan6", "Or PipeOrgan7", "Or PipeOrgan8", "Or TheatreOrg", "Or SmallPipes", "Or ChorusPipe", "Or Wedding", "Or DX-Chrch 1", "Or DX-Chrch 2", "Or BrightOrgn", "Or TamePipe", "Or PuffOrgan1", "Or PuffOrgan2", "Or Late Down", "Or SoftReedOr", "Or SteamOrgan", "Or StreetOrgn", "Or DX-Acrd 1", "Or DX-Acrd 2", "Or DX-Acrd 3", "Or DX-Acrd 4", "Or DX-Acrd 5", "Or DX-Acrd 6", "Or DX-TngAc", "Or DX-Hmnc 1", "Or DX-Hmnc 2", "Or DX-Hmnc 3", "Or DX-Hmnc 4", "Or Chromonica", "Or FM-Hmnc 1", "Or FM-Hmnc 2", "Or Bluesharp", "Or Buzzharp" },
        { "Gt DX-AcstGt1", "Gt DX-AcstGt2", "Gt DX-AcstGt3", "Gt DX-AcstGt4", "Gt DX-AcstGt5", "Gt GuitarBell", "Gt LuteGuitar", "Gt DX-PickGt1", "Gt DX-PickGt2", "Gt DX-PickGt3", "Gt DX-PickGt4", "Gt DX-PickGt5", "Gt DX-PickGt6", "Gt DX-PickGt7", "Gt DX-PickGt8", "Gt Synhalon", "Gt Picksynth", "Gt Compitar", "Gt Stratish", "Gt Banjitar", "Gt Touch Mute", "Gt Firenze", "Gt Folknik", "Gt FunkyPluck", "Gt Guitar Box", "Gt Long Nail", "Gt Pianatar", "Gt RhythmPluk", "Gt SteelyPick", "Gt TiteGuitar", "Gt DX-JazzGt1", "Gt DX-JazzGt2", "Gt DX-JazzGt3", "Gt DX-JazzGt4", "Gt DX-JazzGt5", "Gt Guitorgan", "Gt DX-ClGt 1", "Gt DX-ClGt 2", "Gt DX-ClGt 3", "Gt DX-ClGt 4", "Gt DX-ClGt 5", "Gt DX-ClGt 6", "Gt DX-ClGt 7", "Gt DX-ClGt 8", "Gt DX-ClGt 9", "Gt DX-ClGt 10", "Gt DX-ClGt 11", "Gt DX-ClGt 12", "Gt Buzzstring", "Gt DX-MuteGt1", "Gt DX-MuteGt2", "Gt DX-MuteGt3", "Gt DX-MuteGt4", "Gt Heavy Gage", "Gt DX-OvDrGt", "Gt DX-DistGt1", "Gt DX-DistGt2", "Gt DX-DistGt3", "Gt DX-DistGt4", "Gt DX-DistGt5", "Gt Stortion1", "Gt Pluckoww", "Gt Stortion2", "Gt FuzzGuitar", "Ba DX-WoodBa1", "Ba DX-WoodBa2", "Ba DX-WoodBa3", "Ba DX-WoodBa4", "Ba DX-WoodBa5", "Ba DX-WoodBa6", "Ba DX-WoodBa7", "Ba DarkWodBa1", "Ba DarkWodBa2", "Ba BoogieBass", "Ba BassLegend", "Ba DX-FngrBa1", "Ba DX-FngrBa2", "Ba DX-FngrBa3", "Ba DX-FngrBa4", "Ba Fusit Bass", "Ba FingerPick", "Ba HardFinger", "Ba Harm Bass", "Ba Inorganic", "Ba Nasty Bass", "Ba SkweekBass", "Ba DX-PickBa1", "Ba DX-PickBa2", "Ba DX-PickBa3", "Ba DX-PickBa4", "Ba Bass Magic", "Ba Chiff Bass", "Ba Comped EB", "Ba Metal Bass", "Ba Owl Bass", "Ba Pick Pluck", "Ba Plektrumbs", "Ba Wired Bass", "Ba FretlesBa1", "Ba FretlesBa2", "Ba FretlesBa3", "Ba FretlesBa4", "Ba FretlesBa5", "Ba SlapString", "Ba Lite Slap", "Ba RoundWound", "Ba ImpactBass", "Ba Afresh", "Ba WireString", "Ba Clakwire", "Ba SuperBass1", "Ba SuperBass2", "Ba DigiBass 1", "Ba DigiBass 2", "Ba Digit Bass", "Ba Draft Bass", "Ba Brainacus", "Ba DX-SynBa 1", "Ba DX-SynBa 2", "Ba DX-SynBa 3", "Ba DX-SynBa 4", "Ba DX-SynBa 5", "Ba DX-SynBa 6", "Ba DX-SynBa 7", "Ba DX-SynBa 8", "Ba DX-SynBa 9", "Ba AnalogBass", "Ba Nharmonik" },
        { "Ba BassNovo", "Ba BassResWp", "Ba Cutmandu", "Ba DX-Bass 1", "Ba DX-Bass 2", "Ba DX-Bass 3", "Ba DX-Bass 4", "Ba DX-Bass 5", "Ba DX-Bass 6", "Ba WireBass 1", "Ba WireBass 2", "Ba HardDXBass", "Ba SmakaBass", "Ba AnaBass 1", "Ba AnaBass 2", "Ba AnaBass 3", "Ba 81Z Bass", "Ba DiscBass 1", "Ba DiscBass 2", "Ba Hop Bass 1", "Ba Hop Bass 2", "Ba After 88", "Ba Cable Bass", "Ba Wood Rez", "Ba EazyAction", "Ba ExciteBass", "Ba PrkussBass", "Ba Flapstick", "Ba Jackson", "Ba NipponBass", "Ba Bass Knock", "Ba Ana Stevie", "Ba Munkhen", "Ba Perc Bass", "Ba Remark", "Ba SmoothBass", "Ba Ana Knock", "Ba Jaco Syn", "Ba Werksfunk", "Ba ZedRubba", "St DX-Violin1", "St DX-Violin2", "St DX-Violin3", "St DX-Violin4", "St Violinz", "St DX-Viola 1", "St DX-Viola 2", "St DX-Viola 3", "St DX-Cello 1", "St DX-Cello 2", "St DX-Cello 3", "St DX-Cello 4", "St Rosin", "St DX-Str 1", "St DX-Str 2", "St DX-Str 3", "St DX-Str 4", "St DX-Str 5", "St DX-Str 6", "St DX-Str 7", "St DX-Str 8", "St DX-Str 9", "St DX-Str 10", "St DX-Str 11", "St DX-Str 12", "St DX-Str 13", "St Quick Arco", "St MidString1", "St MidString2", "St LowString1", "St LowString2", "St MM-String", "St DX-AnaSt 1", "St DX-AnaSt 2", "St DX-AnaSt 3", "St DX-SynSt 1", "St DX-SynSt 2", "St DX-SynSt 3", "St DX-SynSt 4", "St DX-SynSt 5", "St DX-SynSt 6", "St DX-SynSt 7", "St WarmStr 1", "St WarmStr 2", "St WarmStr 3", "St WarmStr 4", "St Afternoon", "St Agitate", "St AnnaString", "St Bright Str", "St General", "St GentleMind", "St Gypsy", "St MaxiString", "St Michelle", "St MoterDrive", "St ReverbStrg", "St StrMachine", "St Silk Hall", "St Small Sect", "St Soft Bow", "St Soline", "St Violtron", "St DX-PizzSt", "St PizzString", "St DX-Harp 1", "St DX-Harp 2", "St DX-Harp 3", "St Baroquen", "St Dbl Harp 1", "St Dbl Harp 2", "St Apollon", "St CembaHarp", "St ElectrHarp", "St HarpStrum", "St Lute Harp", "St Metal Harp", "St Orch Harp", "St Syn Harp", "St DX-Timpani", "St Timpanic!", "St Iron Timpa", "En Ensemble", "En HallOrch 1", "En HallOrch 2", "En Orch Brass", "Br DX-Trpt 1", "Br DX-Trpt 2" },
        { "Br DX-Trpt 3", "Br DX-Trpt 4", "Br DX-Trpt 5", "Br DX-Trpt 6", "Br SilverTrpt", "Br Solo Trpt", "Br SynTrumpet", "Br Trumponica", "Br DX-Trb 1", "Br DX-Trb 2", "Br DX-Trb 3", "Br Mute Trb", "Br DX-Tuba 1", "Br DX-Tuba 2", "Br DX-Tuba 3", "Br DX-Horn", "Br Hornz", "Br Alps Horn", "Br BlunchHorn", "Br Horn Ens", "Br MelowHorn1", "Br MelowHorn2", "Br SimpleHorn", "Br Syn Horns", "Br Vibra Horn", "Br DX-Brass 1", "Br DX-Brass 2", "Br Attack Brs", "Br Brasstring", "Br DX-BrsSec1", "Br DX-BrsSec2", "Br MM-Brass 1", "Br MM-Brass 2", "Br MM-Brass 3", "Br 5th Brass", "Br Blow Brass", "Br Brass Sect", "Br Chorus Brs", "Br Fanfare", "Br Hard Brass", "Br Sample Brs", "Br Single Brs", "Br ThickBrass", "Br TightBrs 1", "Br TightBrs 2", "Br DX-SynBr 1", "Br DX-SynBr 2", "Br DX-SynBr 3", "Br DX-SynBr 4", "Br DX-SynBr 5", "Br DX-SynBr 6", "Br DX-SynBr 7", "Br FilterHorn", "Br SharpBrass", "Br Synthorns", "Br CS80-Brs 1", "Br CS80-Brs 2", "Br Ana Poly", "Br AnaFatBrs", "Br AnalogBrs", "Br Faze Brass", "Br Brassy", "Br Court", "Br DX-FatBrs", "Br RezAttack", "Br FunkyRhytm", "Br Chiffhorns", "Br Juice", "Br Kingdom", "Br PowerDrive", "Br Rahool Brs", "Br SyntiBrs", "Br UltraDrive", "Br Warm Brass", "Rd SopranoSax", "Rd DX-ASax 1", "Rd DX-ASax 2", "Rd Alto Sax", "Rd DX-Tsax", "Rd TenorSax", "Rd Tenorsaxes", "Rd Oboe 1", "Rd Oboe 2", "Rd Oboe 3", "Rd Eng.Horn", "Rd Bassoon", "Rd DX-Clari 1", "Rd DX-Clari 2", "Rd Clari Solo", "Rd Slow Clari", "Rd VibratoCla", "Pi Piccolo 1", "Pi Piccolo 2", "Pi DX-Flute 1", "Pi DX-Flute 2", "Pi DX-Flute 3", "Pi DX-Flute 4", "Pi DX-Flute 5", "Pi DX-Flute 6", "Pi DX-Flute 7", "Pi Air Blower", "Pi MetalFlute", "Pi Song Flute", "Pi Recorder 1", "Pi Recorder 2", "Pi Recorder 3", "Pi DX-PnFlute", "Pi Harvest", "Pi Fuhppps!", "Pi DX-Bottle", "Pi Quena", "Pi Whistle 1", "Pi Whistle 2", "Pi Whistle 3", "Pi Sukiyaki", "Pi SambaWhstl", "Pi Cosmowhist", "Pi DX-Ocrn 1", "Pi DX-Ocrn 2", "Pi DX-Ocrn 3", "Et DX-Sitar 1", "Et DX-Sitar 2", "Et Ethre Four", "Et India", "Et Juice Harp", "Et Syntholin", "Et Pilgrim", "Et Tenjiku" },
        { "Et Ukabanjo", "Et Xango", "Et Xanu", "Et Zimbalon", "Et DX-Banjo", "Et Shamisen 1", "Et Shamisen 2", "Et Shamisen 3", "Et DX-Koto", "Et DX-Klmb 1", "Et DX-Klmb 2", "Et DX-Klmb 3", "Et DX-Klmb 4", "Et DX-Klmb 5", "Et DX-Bagpipe", "Et DX-Fiddle", "Et African", "Et Bali", "Et Tibetan", "Et Charango", "Et Gamelan 1", "Et Gamelan 2", "Et Gamelan 3", "Et Kinzoku 1", "Et Kinzoku 2", "Et ScotchTone", "Pc DX-Agogo 1", "Pc DX-Agogo 2", "Pc DX-Bongo", "Pc Bongo", "Pc DX-Clave", "Pc DX-Perc", "Pc Block", "Pc Conga Drum", "Pc Cowbell", "Pc Flexatone", "Pc Glaeser", "Pc Log Drum", "Pc SmlShaker", "Pc Metal", "Pc Percud", "Pc RefrsWhstl", "Pc Seq Pluck", "Pc BigShaker", "Pc Side Stick", "Pc Perkabell", "Pc Spoon", "Pc DX-StelCan", "Pc Steel Can", "Pc DX-StelDr1", "Pc DX-StelDr2", "Pc SteelDrum1", "Pc SteelDrum2", "Pc Steel Band", "Pc Jamaica", "Pc Tambarin", "Pc Triangle 1", "Pc Triangle 2", "Pc BellGliss1", "Pc BellGliss2", "Pc Twincle", "Pc MetalBottl", "Pc NipponDrm1", "Pc NipponDrm2", "Pc Janpany", "Pc Nou", "Pc Sumoh Drum", "Pc HandBell 1", "Pc HandBell 2", "Pc JingleBell", "Pc Light Year", "Pc SlightBell", "Pc TracerBell", "Pc MM-SynDr 1", "Pc MM-SynDr 2", "Pc Click Kick", "Pc Hexagon", "Pc Whapit", "Pc Hi-Hat", "Pc Deep Snare", "Pc DX-MtlSnr", "Pc Snapie", "Pc Snare", "Pc Soft Head", "Pc StreetSD", "Pc Tom Herz", "Pc DX-RevCym1", "Pc DX-RevCym2", "Vo DX-Chorus1", "Vo DX-Chorus2", "Vo DX-Chorus3", "Vo DX-Chorus4", "Vo DX-Chorus5", "Vo DX-Chorus6", "Vo DX-Chorus7", "Vo DX-Chorus8", "Vo DX-Chorus9", "Vo DX-Voice 1", "Vo DX-Voice 2", "Vo MM-Voice 1", "Vo MM-Voice 2", "Vo MM-Voice 3", "Vo MM-Voice 4", "Vo DbVoxFem", "Vo Fem Voice", "Vo Lady Vox", "Vo Space Vox", "Vo Syn Vox", "Co Bell+Pno 1", "Co Bell+Pno 2", "Co Bell+Vibe1", "Co Bell+Str", "Co Bell+Vibe2", "Co Cho+Marimb", "Co Clavi+Bass", "Co DX-Ba+Lead", "Co DX-HpSt", "Co EP+Brass 1", "Co EP+Brass 2", "Co EP+Chime", "Co EP+Clavi", "Co Elec Combi", "Co Glock+Brs", "Co Glock+Pno", "Co Harp+Flute", "Co Koto+Flute", "Co MalletHorn", "Co Mrmb+Gtr" },
        { "Co Orch Chime", "Co Pno+Flute", "Co StringTine", "Co Xylo+Brass", "Ld DX-SynLd 1", "Ld DX-SynLd 2", "Ld DX-SynLd 3", "Ld DX-SynLd 4", "Ld DX-SynLd 5", "Ld DX-SynLd 6", "Ld DX-SynLd 7", "Ld DX-SynLd 8", "Ld DX-SynLd 9", "Ld Pluck Lead", "Ld Perka Lead", "Ld GuitsynLd", "Ld DXSynLd 1", "Ld DXSynLd 2", "Ld DXSynLd 3", "Ld DXSynLd 4", "Ld DXSynLd 5", "Ld DXSynLd 6", "Ld DXSynLd 7", "Ld DXSynLd 8", "Ld SqueezeLd", "Ld Mooganic", "Ld BrassLead1", "Ld BrassLead2", "Ld BrassLead3", "Ld BrassLead4", "Ld Saw Lead", "Ld DX-SawLd 1", "Ld DX-SawLd 2", "Ld DX-Squar", "Ld DX-VoiceLd", "Ld DX-WahLead", "Ld DXAttackLd", "Ld CaliopLd 1", "Ld CaliopLd 2", "Ld CaliopLd 3", "Ld Fifths 1", "Ld Fifths 2", "Ld LdSubHarm", "Ld Buzzer", "Ld Au Campo", "Ld Bass Lead", "Ld Comp Lead", "Ld EadgbeLead", "Ld Flap Synth", "Ld FretlessLd", "Ld Giovanni", "Ld HarmoSynth", "Ld Lead Line", "Ld Lead Phone", "Ld Lyle Lead", "Ld PekingLead", "Ld Puff Pipe", "Ld Reed Lead", "Ld SingleLine", "Ld Super DX", "Ld Sweep Lead", "Ld Vibratoron", "Ld DX-Vocoder", "Ld Winwood", "Pd DrkSweeper", "Pd AnaBrsPad", "Pd 8bitStrPad", "Pd DX-ChoPad1", "Pd DX-ChoPad2", "Pd Bow Pad 1", "Pd Bow Pad 2", "Pd Bow Pad 3", "Pd Glassharp", "Pd Wineglass", "Pd Ice Galaxy", "Pd Ice Heaven", "Pd Hit Pad 1", "Pd Hit Pad 2", "Pd SynBrsPad1", "Pd SynBrsPad2", "Pd SynBrsPad3", "Pd SynBrsPad4", "Pd SynBrsPad5", "Pd SynBrsPad6", "Pd SynBrsPad7", "Pd Vector Pad", "Pd Pada Perka", "Pd DX-MetalPd", "Pd DX-SawPad", "Pd Anna Pad", "Pd Baroque", "Pd BrassyWarm", "Pd Bright Pad", "Pd Clavi Pad", "Pd Digi Pad", "Pd Dispo Pad", "Pd Ethereal", "Pd Film Pad", "Pd Fl.Cloud", "Pd Floating", "Pd Forest99", "Pd Gior Pad", "Pd GreenPeace", "Pd Grunge Pad", "Pd Hyper Sqr", "Pd MM-Pretty", "Pd MonsterPad", "Pd Orwell", "Pd PhaseSweep", "Pd Phasers", "Pd Glass Pad", "Pd Sanctus", "Pd StacHeaven", "Pd Sweep Pad", "Pd Water Log", "Pd Spec-trail", "Pd Whaser Pad", "Pd Whisper", "Pd WhistlePad", "Fx DX-ScFi 1", "Fx DX-ScFi 2", "Fx DX-ScFi 3", "Fx Image 1", "Fx Image 2", "Fx Laser 1", "Fx Laser 2", "Fx Laser 3", "Fx Ri-zer" },
        { "Fx MM-Shock 1", "Fx MM-Shock 2", "Fx Wallop 1", "Fx Wallop 2", "Fx Angel", "Fx BackSuir", "Fx Bird View", "Fx ChorusElms", "Fx DX-Stars", "Fx Electric", "Fx Evolution", "Fx FM-Growth", "Fx Paddawire", "Fx Fantasynt", "Fx Fluv Push", "Fx Fmilters", "Fx Glassy", "Fx Glastine", "Fx Glocker", "Fx IceRevEcho", "Fx InitEnsmbl", "Fx MetalSweep", "Fx SquareModd", "Fx Mpndg Doom", "Fx Mystrian", "Fx RepertRise", "Fx Space Trip", "Fx Syn Rise", "Fx Glider", "Sc Anna DX", "Sc Analog-X", "Sc DX-Atms 1", "Sc DX-Atms 2", "Sc DX-Bright1", "Sc DX-Bright2", "Sc 90 K", "Sc 200 K", "Sc Arrow-X", "Sc Attacker", "Sc Harp Pad", "Sc ChiLight", "Sc Digi Calio", "Sc Digitar", "Sc Distracted", "Sc FinerThing", "Sc Fuji Stabs", "Sc TouchyEdgy", "Sc Metal Box", "Sc MilkyWays", "Sc New Elms", "Sc Pipebells", "Sc Synsitar", "Sc OctiLate", "Sc NoBoKuto", "Sc Syn Bright", "Sc Ting Voice", "Sc Bottlead", "Sc WhapSynth", "Se DX-Flght", "Se Take Off", "Se DX-Helicpt", "Se Helicopter", "Se DX-Ship", "Se DX-Train", "Se Mobile", "Se Motors", "Se MotorCycle", "Se U Boat", "Se Ambulance", "Se Whiz By", "Se Out-Da-Way", "Se Patrol Car", "Se Sirens", "Se DX-TelBusy", "Se DX-TelCall", "Se DX-TelTone", "Se DX-TlRing1", "Se DX-TlRing2", "Se Bugs&Birds", "Se DX-Insect1", "Se DX-Insect2", "Se DX-Piyo", "Se DX-Growl 1", "Se DX-Growl 2", "Se Animals", "Se DX-Wolf", "Se ManEater", "Se Alarm !", "Se Aura", "Se Chi-S&H", "Se Closing", "Se Computer", "Se Crasher", "Se DX-BigBen", "Se DX-Wave", "Se Descent", "Se Doppler", "Se Factory", "Se GhostLine", "Se Heart Beat", "Se Imaging", "Se IronEcho 1", "Se IronEcho 2", "Se MM-Fall", "Se MachineGun", "Se MobbyDick", "Se On the Run", "Se OuterLimit", "Se Perc Shot", "Se Repeater", "Se Jet Cars 1", "Se Scorchers", "Se Sci-Fi Too", "Se Jet Cars 2", "Se Speak-One", "Se Stopper", "Se Super Foot", "Se Talking DX", "Se Transport", "Se Turn Table", "Se UfoTakeOff", "Se Waterfall", "Se Whik Shot", "Se Bubblets", "Se Yes Talk", "Se Help me !", "Se Paranoir", "Se Screamy" }
        };

    public static final String[] FSEQS = { "Internal 1", "Internal 2", "Internal 3", "Internal 4", "Internal 5", "Internal 6", "ShoobyDo", "2BarBeat", "D&B", "D&B Fill", "4BarBeat", "YouCanG", "EBSayHey", "RtmSynth", "VocalRtm", "WooWaPa", "UooLha", "FemRtm", "ByonRole", "WowYeah", "ListenVo", "YAMAHAFS", "Laugh", "Laugh2", "AreYouR", "Oiyai", "Oiaiuo", "UuWaUu", "Wao", "RndArp1", "FiltrArp", "RndArp2", "TechArp", "RndArp3", "Voco-Seq", "PopTech", "1BarBeat", "1BrBeat2", "Undo", "RndArp4", "VoclRtm2", "Reiyowha", "RndArp5", "VocalArp", "CanYouGi", "Pu-Yo", "Yaof", "MyaOh", "ChuckRtm", "ILoveYou", "Jan-On", "Welcome", "One-Two", "Edokko", "Everybdy", "Uwau", "YEEAAH", "4-3-2-1", "Test123", "CheckSnd", "ShavaDo", "R-M-H-R", "HiSchool", "M.Blastr", "L&G MayI", "Hellow", "ChowaUu", "Everybd2", "Dodidowa", "Check123", "BranNewY", "BoomBoom", "Hi=Woo", "FreeForm", "FreqPad", "YouKnow", "OldTech", "B/M", "MiniJngl", "EveryB-S", "IYaan", "Yeah", "ThankYou", "Yes=No", "UnWaEDon", "MouthPop", "Fire", "TBLine", "China", "Aeiou", "YaYeYiYo", "C7Seq", "SoundLib", "IYaan2", "Relax" };
    //public static final String[] FSEQS = { "Internal 1", "Internal 2", "Internal 3", "Internal 4", "Internal 5", "Internal 6", "ShoobyDo: 504", "2BarBeat: 511", "D&B: 511", "D&B Fill: 511", "4BarBeat: 511", "YouCanG: 511", "EBSayHey: 510", "RtmSynth: 431", "VocalRtm: 500", "WooWaPa: 127", "UooLha: 117", "FemRtm: 125", "ByonRole: 77", "WowYeah: 127", "ListenVo: 127", "YAMAHAFS: 127", "Laugh: 121", "Laugh2: 127", "AreYouR: 127", "Oiyai: 127", "Oiaiuo: 127", "UuWaUu: 127", "Wao: 127", "RndArp1: 127", "FiltrArp: 127", "RndArp2: 127", "TechArp: 127", "RndArp3: 120", "Voco-Seq: 127", "PopTech: 113", "1BarBeat: 127", "1BrBeat2: 127", "Undo: 127", "RndArp4: 127", "VoclRtm2: 127", "Reiyowha: 127", "RndArp5: 103", "VocalArp: 127", "CanYouGi: 127", "Pu-Yo: 127", "Yaof: 127", "MyaOh: 127", "ChuckRtm: 127", "ILoveYou: 127", "Jan-On: 127", "Welcome: 127", "One-Two: 127", "Edokko: 127", "Everybdy: 127", "Uwau: 127", "YEEAAH: 127", "4-3-2-1: 127", "Test123: 127", "CheckSnd: 127", "ShavaDo: 127", "R-M-H-R: 127", "HiSchool: 127", "M.Blastr: 127", "L&G MayI: 127", "Hellow: 127", "ChowaUu: 127", "Everybd2: 127", "Dodidowa: 127", "Check123: 127", "BranNewY: 116", "BoomBoom: 127", "Hi=Woo: 127", "FreeForm: 127", "FreqPad: 127", "YouKnow: 127", "OldTech: 127", "B/M: 127", "MiniJngl: 127", "EveryB-S: 127", "IYaan: 127", "Yeah: 127", "ThankYou: 127", "Yes=No: 127", "UnWaEDon: 127", "MouthPop: 127", "Fire: 94", "TBLine: 104", "China: 127", "Aeiou: 127", "YaYeYiYo: 127", "C7Seq: 125", "SoundLib: 127", "IYaan2: 127", "Relax: 127" };
    public static final String[] BANKS = { "Internal", "A", "B", "C" };
    public static final String[] NOTES = { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    public static final String[] FSEQ_CLOCK = { "midi1/4", "midi1/2", "midi", "midi2/1", "midi4/1" };
    public static final String[] FSEQ_LOOP_MODES = { "One Way", "Round" };
    public static final String[] FSEQ_PLAY_MODES = { "Fseq", "Scratch" };
    public static final String[] FSEQ_PITCH_MODES = { "Fseq", "Fixed" };
    public static final String[] FSEQ_TRIGGERS = { "All", "First" };
    public static final String[] OUTS = { "Off", "Pre Insert", "Post Insert" };
    public static final String[] FSEQ_PARTS = { "Off", "1", "2", "3", "4" };
    public static final String[] FSEQ_BANKS = { "Internal", "Preset" };
    public static final String[] PORTAMENTO_MODES = { "Fingered", "Full Time" };
    public static final String[] NOTE_PRIORITIES = { "Last", "Top", "Bottom", "First" };
    public static final String[] VOICE_BANKS = { "Off", "Internal", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K" };
    public static final String[] PRESETS = FSEQ_CLOCK;          // for now
    public static final int[] PRESET_VALS = { 5, 6, 7, 8, 9 };          // for now
    public static final String[] EQ_SHAPES = { "Shelving", "Peaking" };
    public static final String[] PARTS = { "1", "2", "3", "4" };
    public static final int REVERB = 0;
    public static final int VARIATION = 1;
    public static final int INSERTION = 2;
    

    // The following two were taken with permission from FS1REditor by K_Take (https://synth-voice.sakura.ne.jp/fs1r_editor_english.html)
    public static final String[] CONTROL_DESTINATIONS = { "Ins->Rev", "Ins->Var", "Volume", "Pan", "Rev Send", "Var Send", "Flt Freq", "Flt Reso", "Flt EGDepth", "Attack Time", "Decay Time", "Release Time", "PEG InitLvl", "PEGAtakTime", "PEG ReleLvl", "PEGReleTime", "V/N Balance", "Formant", "FM", "Pitch Bias", "Amp EG Bias", "Freq Bias", "V BandWidth", "N BandWidth", "LFO1 Pitch", "LFO1 Amp", "LFO1 Freq", "LFO1 Filter", "LFO1 Speed", "LFO2 Filter", "LFO2 Speed", "Fseq Speed", "FseqScratch" };
    public static final int[] EQ_FREQUENCIES = new int[]
    {
    32,                             //[start low],
    36,
    40,
    45,
    50,
    56,
    63,
    70,
    80,
    90,
    100,                    //[start mid],
    110,
    125,
    140,
    160,
    180,
    200,
    225,
    250,
    280,
    315,
    355,
    400,
    450,
    500,                    //[start high],
    560,
    630,
    700,
    800,
    900,
    1000,
    1100,
    1200,
    1400,
    1600,
    1800,
    2000,                   //[end low],
    2200,
    2500,
    2800,
    3200,
    3600,
    4000,
    4500,
    5000,
    5600,
    6300,
    7000,
    8000,
    9000,
    10000,                  //[end mid],
    11000,
    12000,
    14000,
    16000,                  //[end high],
    };


    public void addYamahaFS1RMenu()
        {
        JMenu menu = new JMenu("FS1R");
        menubar.add(menu);

        JMenuItem swap = new JMenuItem("Swap Parts...");
        menu.add(swap);
        swap.addActionListener(new ActionListener()
            {
            JComboBox part1 = new JComboBox(PARTS);
            JComboBox part2 = new JComboBox(PARTS);
                
            public void actionPerformed(ActionEvent evt)
                {
                boolean result = showMultiOption(YamahaFS1RMulti.this, new String[] { "Swap", "With" }, 
                    new JComponent[] { part1, part2 }, "Swap Parts...", "Enter the parts to swap with one another.");

                if (result)
                    {
                    int p1 = part1.getSelectedIndex() + 1;
                    int p2 = part2.getSelectedIndex() + 1;

                    undo.push(getModel());
                    setSendMIDI(false);
                    boolean currentPush = undo.getWillPush();
                    undo.setWillPush(false);

                    String[] parameters = model.getKeys();
                    for(int i = 0; i < parameters.length; i++)
                        {
                        if (parameters[i].startsWith("part" + p2))
                            {
                            int val2 = model.get(parameters[i]);
                            int val1 = model.get(("part" + p1) + parameters[i].substring(5));
                            model.set(("part" + p1) + parameters[i].substring(5), val2);
                            model.set(parameters[i], val1);
                            }
                        }
                                                                                
                    undo.setWillPush(currentPush);
                    setSendMIDI(true);
                    sendAllParameters();
                    }
                }
            });

        JMenuItem copy = new JMenuItem("Copy Part...");
        menu.add(copy);
        copy.addActionListener(new ActionListener()
            {
            JComboBox part1 = new JComboBox(PARTS);
            JComboBox part2 = new JComboBox(PARTS);
                
            public void actionPerformed(ActionEvent evt)
                {
                boolean result = showMultiOption(YamahaFS1RMulti.this, new String[] { "Copy", "To"}, 
                    new JComponent[] { part1, part2 }, "Copy Part...", "Enter the parts to copy from and to.");

                if (result)
                    {
                    int p1 = part1.getSelectedIndex() + 1;
                    int p2 = part2.getSelectedIndex() + 1;

                    undo.push(getModel());
                    setSendMIDI(false);
                    boolean currentPush = undo.getWillPush();
                    undo.setWillPush(false);

                    String[] parameters = model.getKeys();
                    for(int i = 0; i < parameters.length; i++)
                        {
                        if (parameters[i].startsWith("part" + p1))
                            {
                            int val2 = model.get(parameters[i]);
                            model.set(("part" + p2) + parameters[i].substring(5), val2);
                            }
                        }
                                                                                
                    undo.setWillPush(currentPush);
                    setSendMIDI(true);
                    sendAllParameters();
                    }
                }
            });
        }

    public JFrame sprout()
        {
        JFrame frame = super.sprout();	
        transmitTo.setEnabled(false);		// Though that doesn't matter any more
        addYamahaFS1RMenu();
        return frame;
        }         

    public YamahaFS1RMulti()
        {
        model.set("number", 0);
        model.set("bank", 0);
                
        for(int i = 0; i < allParameters.length; i++)
            {
            allParametersToIndex.put(allParameters[i], Integer.valueOf(i));
            }

        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addGlobal(Style.COLOR_A()));
        vbox.add(hbox);
        vbox.add(addFseq(Style.COLOR_B()));
        
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Common", soundPanel);

        for(int i = 1; i <= 4; i++)
            {
            soundPanel = new SynthPanel(this);
            vbox = new VBox();

            hbox = new HBox();
            hbox.add(addVoice(i, Style.COLOR_A()));
            hbox.addLast(addOutput(i, Style.COLOR_B()));
            vbox.add(hbox);
            vbox.add(addTone(i, Style.COLOR_C()));

            hbox = new HBox();
            hbox.add(addPitch(i, Style.COLOR_A()));
            hbox.addLast(addEnvelopes(i, Style.COLOR_B()));
            vbox.add(hbox);

            vbox.addLast(addPlay(i, Style.COLOR_C()));
                
            soundPanel.add(vbox, BorderLayout.CENTER);
            addTab("Part " + i, soundPanel);
            }

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addReverb(Style.COLOR_A()));
        vbox.add(addVariation(Style.COLOR_B()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Effects 1", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        vbox.add(addInsertion(Style.COLOR_A()));
        vbox.add(addEqualization(Style.COLOR_B()));

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Effects 2", soundPanel);

        soundPanel = new SynthPanel(this);
        vbox = new VBox();
        for(int i = 1; i <= 8; i+=2)
            {
            HBox hbox2 = new HBox();
            if (i % 4 == 1)
                {
                hbox2.add(addController(i, Style.COLOR_A()));
                hbox2.addLast(addController(i + 1, Style.COLOR_B()));
                }
            else
                {
                hbox2.add(addController(i, Style.COLOR_B()));
                hbox2.addLast(addController(i + 1, Style.COLOR_A()));
                }
            vbox.add(hbox2);
            }
        
        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("Voice Control", soundPanel);
                        
        loadDefaults();        
        }
                
                
    public String getDefaultResourceFileName() { return "YamahaFS1RMulti.init"; }
    public String getHTMLResourceFileName() { return "YamahaFS1RMulti.html"; }

    public boolean gatherPatchInfo(String title, Model change, boolean writing)
        {
        JComboBox bank = new JComboBox(BANKS);
        if (writing)
            {
            bank = new JComboBox(new String[] { "Internal" });
            bank.setEnabled(false);
            }
        bank.setEditable(false);
        bank.setMaximumRowCount(32);
        bank.setSelectedIndex(model.get("bank"));
                
        JTextField number = new JTextField("" + (model.get("number") + 1), 3);
                
        while(true)
            {
            boolean result = showMultiOption(this, new String[] { "Bank", "Patch Number"}, 
                new JComponent[] { bank, number }, title, "Enter the Bank and Patch number.");
                
            if (result == false) 
                return false;
                                
            int n;
            try { n = Integer.parseInt(number.getText()); }
            catch (NumberFormatException e)
                {
                showSimpleError(title, "The Patch Number must be an integer 1 ... 128");
                continue;
                }
            if (n < 1 || n > 128)
                {
                showSimpleError(title, "The Patch Number must be an integer 1 ... 128");
                continue;
                }
                                
            change.set("bank", bank.getSelectedIndex());
            change.set("number", n - 1);
                        
            return true;
            }
        }
                                    
    /** Add the global patch category (name, id, number, etc.) */
    public JComponent addNameGlobal(Color color)
        {
        Category globalCategory = new Category(this, getSynthName(), color);
        globalCategory.makeUnresettable();
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
                
        VBox vbox = new VBox();
        HBox hbox2 = new HBox();
        comp = new PatchDisplay(this, 8);
        hbox2.add(comp);
        vbox.add(hbox2);
        
        comp = new StringComponent("Patch Name", this, "name", 10, "Name must be up to 10 ASCII characters.")
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
        vbox.add(comp);  // doesn't work right :-(

        hbox.add(vbox);

        // Not enough space to show the title
        hbox.addLast(Strut.makeHorizontalStrut(120));

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
        }


    public JComponent addGlobal( Color color)
        {
        Category category = new Category(this, "Global", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
                
        params = CATEGORIES;
        comp = new Chooser("Category", this, "category", params);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Volume", this, "performancevolume", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Pan", this, "performancepan", color, 1, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                if (val < 64)
                    return "< " + (64 - val);
                else if (val == 64)
                    return "--";
                else
                    return "" + (val - 64) + " >";
                }
            };
        hbox.add(comp);
        
        comp = new LabelledDial("Note Shift", this, "performancenoteshift", color, 0, 48)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);

        params = OUTS;
        comp = new Chooser("Out", this, "individualout", params);
        vbox.add(comp);
        hbox.add(vbox);


        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    public JComponent addFseq(Color color)
        {
        Category category = new Category(this, "Fseq", color);

        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();

        params = FSEQ_PITCH_MODES;
        final Chooser pm = new Chooser("Pitch Mode", this, "fseqformantpitchmode", params);
        params = FSEQ_LOOP_MODES;
        final Chooser lm = new Chooser("Loop Mode", this, "fseqloopmode", params);
        final LabelledDial ld = new LabelledDial("Delay", this, "fseqformantsequencedelay", color, 0, 99);
        final LabelledDial ls = new LabelledDial("Loop Start", this, "fseqstartstepoflooppoint", color, 0, 511);
        final LabelledDial le = new LabelledDial(" Loop End ", this, "fseqendstepoflooppoint", color, 0, 511);
        final LabelledDial so = new LabelledDial("Start", this, "fseqstartstepoffset", color, 0, 511);


        params = FSEQ_PARTS;
        comp = new Chooser("Part", this, "fseqpart", params);
        vbox.add(comp);

        params = FSEQS;
        comp = new Chooser("Fseq", this, "fseq", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                int val = model.get(key);
                
                // reset to defaults
                model.set("fseqformantpitchmode", FSEQ_PARAMETER_SETTINGS[val][FSEQ_DEFAULT_PITCH_I]);
                model.set("fseqformantsequencedelay", FSEQ_PARAMETER_SETTINGS[val][FSEQ_DEFAULT_START_DELAY_I]);
                model.set("fseqloopmode", FSEQ_PARAMETER_SETTINGS[val][FSEQ_DEFAULT_LOOP_MODE_I]);
                model.set("fseqstartstepoflooppoint", FSEQ_PARAMETER_SETTINGS[val][FSEQ_DEFAULT_LOOP_START_I]);
                model.set("fseqendstepoflooppoint", FSEQ_PARAMETER_SETTINGS[val][FSEQ_DEFAULT_LOOP_END_I]);
                
                // Update upper limits
                int maxLen = FSEQ_PARAMETER_SETTINGS[val][FSEQ_MAX_LENGTH_I];
                
                model.setMax("fseqstartstepoffset", maxLen);
                model.setMetricMax("fseqstartstepoffset", maxLen);
                if (model.get("fseqstartstepoffset") > maxLen)
                    model.set("fseqstartstepoffset", maxLen);
                model.updateListenersForKey("fseqstartstepoffset");

                model.setMax("fseqstartstepoflooppoint", FSEQ_PARAMETER_SETTINGS[val][FSEQ_MAX_LENGTH_I]);
                model.setMetricMax("fseqstartstepoflooppoint", FSEQ_PARAMETER_SETTINGS[val][FSEQ_MAX_LENGTH_I]);
                if (model.get("fseqstartstepoflooppoint") > maxLen)
                    model.set("fseqstartstepoflooppoint", maxLen);
                model.updateListenersForKey("fseqstartstepoflooppoint");

                model.setMax("fseqendstepoflooppoint", FSEQ_PARAMETER_SETTINGS[val][FSEQ_MAX_LENGTH_I]);
                model.setMetricMax("fseqendstepoflooppoint", FSEQ_PARAMETER_SETTINGS[val][FSEQ_MAX_LENGTH_I]);
                if (model.get("fseqendstepoflooppoint") > maxLen)
                    model.set("fseqendstepoflooppoint", maxLen);
                model.updateListenersForKey("fseqendstepoflooppoint");
                }
            };
        vbox.add(comp);
        
        hbox.add(vbox);
        vbox = new VBox();

        vbox.add(lm);
                        
        params = FSEQ_PLAY_MODES;
        comp = new Chooser("Play Mode", this, "fseqplaymode", params);
        vbox.add(comp);

        hbox.add(vbox);
        vbox = new VBox();

        vbox.add(pm);
                
        params = FSEQ_TRIGGERS;
        comp = new Chooser("Key On Trigger", this, "fseqkeyontrigger", params);
        vbox.add(comp);

        hbox.add(vbox);
        vbox = new VBox();

        PushButton button = new PushButton("Speed Preset", PRESETS)
            {
            public void perform(int i)
                {
                getModel().set("fseqspeed", PRESET_VALS[i]);
                }
            };
        vbox.add(button);

        hbox.add(vbox);


        comp = new LabelledDial("Speed", this, "fseqspeed", color, 5, 5000)             // we'll have to convert this at parse/emit time
            {
            public String map(int val)
                {
                if (val < 10) return FSEQ_CLOCK[val - 5];
                else return String.format("%1.1f", (val / 10.0));                          // 10.0 ... 500.0
                } 
            };
        hbox.add(comp);
        
        hbox.add(so);
                
        hbox.add(ld);
                
        hbox.add(ls);
        hbox.add(le);
                
        comp = new LabelledDial("Speed Vel", this, "fseqvelocitysensitivityfortempo", color, 0, 7);
        ((LabelledDial)comp).addAdditionalLabel("Sensitivity");
        hbox.add(comp);

        comp = new LabelledDial("Level Vel", this, "fseqlevelvelocitysensitivity", color, 0, 127, 64)
            {
            public boolean isSymmetric() { return true; }
            };
        ((LabelledDial)comp).addAdditionalLabel("Sensitivity");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
    

	/** Builds destinations for a given controller.  These destinations must change depending
		on the choice of Insertion effect.  */
    String[] buildDestinations()
        {
        String[] destinations = new String[15 + CONTROL_DESTINATIONS.length];
        int val = model.get("insertiontype");
        int version = YamahaFS1RFX.FX_VERSIONS[INSERTION][val] + 1;             // offset for the table

        destinations[0] = "Off";
        for(int i = 1; i < 15; i++)
            {
            destinations[i] = YamahaFS1RFX.ctrlDestNameTable[i-1][version];
            if (destinations[i].equals(""))
                destinations[i] = "Ins " + i + " --- ";
            }
        for(int i = 0; i < CONTROL_DESTINATIONS.length; i++)
            {
            destinations[i + 15] = CONTROL_DESTINATIONS[i];
            }
        return destinations;
        }
        
    public Chooser[] controllerChoosers = new Chooser[8];
    public JComponent addController(int controller, Color color)
        {
        Category category = new Category(this, "Voice Control Set " + controller , color);
        category.makePasteable("controller" + controller);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
               
        VBox vbox = new VBox();
        params = buildDestinations();
        comp = controllerChoosers[controller-1] = new Chooser("Destination", this, "controller" + controller + "destination", params);
        vbox.add(comp);
        
        HBox hbox2 = new HBox();
        comp = new CheckBox("Part 1", this, "controller" + controller + "part1");
        hbox2.add(comp);                
        comp = new CheckBox("Part 2", this, "controller" + controller + "part2");
        hbox2.add(comp);                
                
        vbox.add(hbox2);
        hbox2 = new HBox();
                
        comp = new CheckBox("Part 3", this, "controller" + controller + "part3");
        hbox2.add(comp);
        comp = new CheckBox("Part 4", this, "controller" + controller + "part4");
        hbox2.add(comp);
        vbox.add(hbox2);

        hbox.add(vbox);
        vbox = new VBox();
        
        hbox2 = new HBox();
        comp = new CheckBox("Knob 1", this, "controller" + controller + "kn1");
        ((CheckBox)comp).addToWidth(1);
        hbox2.add(comp);
        comp = new CheckBox("2", this, "controller" + controller + "kn2");
        hbox2.add(comp);
        comp = new CheckBox("3", this, "controller" + controller + "kn3");
        hbox2.add(comp);
        comp = new CheckBox("4", this, "controller" + controller + "kn4");
        hbox2.add(comp);
        vbox.add(hbox2);
        
        hbox2 = new HBox();
        comp = new CheckBox("MIDI 1", this, "controller" + controller + "mc1");
        hbox2.add(comp);
        comp = new CheckBox("2", this, "controller" + controller + "mc2");
        hbox2.add(comp);
        comp = new CheckBox("3", this, "controller" + controller + "mc3");
        hbox2.add(comp);
        comp = new CheckBox("4", this, "controller" + controller + "mc4");
        hbox2.add(comp);
        vbox.add(hbox2);

        hbox2 = new HBox();
        comp = new CheckBox("Foot", this, "controller" + controller + "fc");
        hbox2.add(comp);
        comp = new CheckBox("Breath", this, "controller" + controller + "bc");
        hbox2.add(comp);
        comp = new CheckBox("Bend", this, "controller" + controller + "pb");
        hbox2.add(comp);
        vbox.add(hbox2);
                
        hbox.add(vbox);
        vbox = new VBox();
                
        comp = new CheckBox("Mod Wheel", this, "controller" + controller + "mw");
        vbox.add(comp);
        comp = new CheckBox("Aftertouch", this, "controller" + controller + "cat");
        vbox.add(comp);
        comp = new CheckBox("Poly AT", this, "controller" + controller + "pat");
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Depth", this,  "controller" + controller + "depth", color, 0, 127, 64);
        hbox.add(comp);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }
        
    
    /** Revises the patch name in the Category title for a given Part. */
    void updatePatchName(int part, Category category)
        {
            int bank = model.get("part" + part + "banknumber", -100) - 1;
            int number = model.get("part" + part + "programnumber", -100);
                
            if (bank == -101 || number == -100) return;  // not set up yet
                
            if (bank <= 0)
                {
                category.setName("Voice " + part);
                }
            else 
                {
                category.setName("Voice " + part + ": " + VOICES[bank - 1][number]);
                }
        }
    
    public JComponent addVoice(int part, Color color)
        {
        final Category category = new Category(this, "Voice " + part, color);
        category.makePasteable("part" + part);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        final PushButton pushbutton = new PushButton("Show")
            {
            public void perform()
                {
                if (YamahaFS1RMulti.this.model.get("part" + part + "banknumber") == 0)  // off
                	return;
                	
                final YamahaFS1R synth = new YamahaFS1R();
                if (tuple != null)
                    synth.tuple = tuple.copy(synth.buildInReceiver(), synth.buildKeyReceiver());
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
                                Model tempModel = new Model();
                                synth.setSendMIDI(false);
                                tempModel.set("bank", YamahaFS1RMulti.this.model.get("part" + part + "banknumber") - 1);
                                tempModel.set("number", YamahaFS1RMulti.this.model.get("part" + part + "programnumber"));
                                synth.setSendMIDI(true);
                                synth.performRequestDump(tempModel, false);
                                }
                            });
                    }
                else
                    {
                    showSimpleError("Disconnected", "You can't show a patch when disconnected.");
                    }
                }
            };
        
        VBox vbox = new VBox();
        params = VOICE_BANKS;
        comp = new Chooser("Voice Bank", this, "part" + part + "banknumber", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                updatePatchName(part, category);
                pushbutton.getButton().setEnabled(model.get(key) != 0); // off
                }
            };
        vbox.add(comp);


    	vbox.add(pushbutton);

        hbox.add(vbox);

        comp = new LabelledDial("Voice", this,  "part" + part + "programnumber", color, 0, 127, -1)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                updatePatchName(part, category);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Number");
        hbox.add(comp);

        comp = new LabelledDial("Receive", this,  "part" + part + "rcvchannel", color, 0, 17)
            {
            public String map(int val)
                {
                if (val == 16) return "Perf";
                else if (val == 17) return "Off";
                else return "" + (val + 1);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Channel");
        hbox.add(comp);

        comp = new LabelledDial("Receive", this,  "part" + part + "rcvchannelmax", color, 0, 16)
            {
            public String map(int val)
                {
                if (val == 16) return "Off";
                else return "" + (val + 1);
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Channel Max");
        if (part <= 2)
            {
            hbox.add(comp);
            }
        else
            {
            hbox.add(Strut.makeStrut(comp));
            }

        comp = new LabelledDial("Reserved", this,  "part" + part + "notereserve", color, 0, 32);
        ((LabelledDial)comp).addAdditionalLabel("Num Notes");
        hbox.add(comp);


        category.add(hbox, BorderLayout.CENTER);
        updatePatchName(part, category);
        return category;
        }

    public JComponent addTone(int part, Color color)
        {
        Category category = new Category(this, "Tone " + part, color);
        category.makePasteable("part" + part);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        
        comp = new CheckBox("Filter", this, "part" + part + "filtersw");
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Formant", this,  "part" + part + "formant", color, 0, 127, 64);
        hbox.add(comp);

        comp = new LabelledDial("FM", this,  "part" + part + "fm", color, 0, 127, 64);
        hbox.add(comp);
                
        comp = new LabelledDial("Voiced/Unvoiced", this,  "part" + part + "voicedunvoicedbalance", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Balance");
        hbox.add(comp);
                
        comp = new LabelledDial("LFO1 1", this,  "part" + part + "lfo1rate", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Speed");
        hbox.add(comp);
                
        comp = new LabelledDial("LFO1 1", this,  "part" + part + "lfo1pitchmoddepth", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Pitch Mod");
        hbox.add(comp);
               
        comp = new LabelledDial("LFO1 1", this,  "part" + part + "lfo1delay", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Delay");
        hbox.add(comp);
               
        comp = new LabelledDial("Cutoff", this,  "part" + part + "filtercutofffreq", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Frequency");
        hbox.add(comp);
               
        comp = new LabelledDial("Resonance", this,  "part" + part + "filterresonance", color, 0, 127, 64);
        hbox.add(comp);
               
        comp = new LabelledDial("Filter Env", this,  "part" + part + "filteregdepth", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);
               
        comp = new LabelledDial("LFO1 2", this,  "part" + part + "lfo2rate", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Speed");
        hbox.add(comp);
                
        comp = new LabelledDial("LFO1 2", this,  "part" + part + "lfo2moddepth", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Filter Mod");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



    public JComponent addPitch(int part, Color color)
        {
        Category category = new Category(this, "Pitch " + part, color);
        category.makePasteable("part" + part);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        params = PORTAMENTO_MODES;
        comp = new Chooser("Portamento Mode", this, "part" + part + "portamentomode", params);
        vbox.add(comp);

        comp = new CheckBox("Portamento", this, "part" + part + "portamentoswitch");
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("Portamento", this,  "part" + part + "portamentotime", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Time");
        hbox.add(comp);

        comp = new LabelledDial("Detune", this,  "part" + part + "detune", color, 0, 127, 64);
        hbox.add(comp);

        comp = new LabelledDial("Note Shift", this,  "part" + part + "noteshift", color, 0, 48, 24)
            {
            public boolean isSymmetric() { return true; }
            };
        hbox.add(comp);
                
        comp = new LabelledDial("Pitch Bend", this,  "part" + part + "pitchbendrangehigh", color, 16, 88, 64);
        ((LabelledDial)comp).addAdditionalLabel("Range High");
        hbox.add(comp);
                
        comp = new LabelledDial("Pitch Bend", this,  "part" + part + "pitchbendrangelow", color, 16, 88, 64);
        ((LabelledDial)comp).addAdditionalLabel("Range Low");
        hbox.add(comp);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

                
    public JComponent addOutput(int part, Color color)
        {
        Category category = new Category(this, "Output " + part, color);
        category.makePasteable("part" + part);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        VBox vbox = new VBox();
        comp = new CheckBox("Insertion Send", this, "part" + part + "insertionsw");
        vbox.add(comp);
                
        hbox.add(vbox);

        comp = new LabelledDial("Volume", this, "part" + part + "volume", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Pan", this,  "part" + part + "pan", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                if (val == 0) return "Rnd";
                else if (val < 64) return "< " + (64 - val);
                else if (val > 64) return "" + (val - 64) + " >";
                else return "--";
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Pan Scaling", this,  "part" + part + "panscaling", color, 0, 100);
        hbox.add(comp);

        comp = new LabelledDial("Pan LFO 1", this,  "part" + part + "panlfodepth", color, 0, 99);
        ((LabelledDial)comp).addAdditionalLabel("Depth");
        hbox.add(comp);

        comp = new LabelledDial("Reverb", this, "part" + part + "reverbsend", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Send");
        hbox.add(comp);

        comp = new LabelledDial("Variation", this, "part" + part + "variationsend", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Send");
        hbox.add(comp);

        comp = new LabelledDial("Dry Level", this, "part" + part + "drylevel", color, 0, 127);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


                
    public JComponent addPlay(int part, Color color)
        {
        Category category = new Category(this, "Play " + part, color);
        category.makePasteable("part" + part);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        
        VBox vbox = new VBox();
        params = NOTE_PRIORITIES;
        comp = new Chooser("Note Assign Priority", this, "part" + part + "monopriority", params);
        vbox.add(comp);

        comp = new CheckBox("Polyphonic", this, "part" + part + "monopolymode");
        vbox.add(comp);

        comp = new CheckBox("Sustain", this, "part" + part + "sustainrcvsw");
        vbox.add(comp);
        hbox.add(vbox); 

        comp = new LabelledDial("Low Note", this,  "part" + part + "notelimitlow", color, 0, 127)
            {
            public String map(int val)
                {
                return NOTES[val % 12] + (val / 12 - 2);
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("High Note", this,  "part" + part + "notelimithigh", color, 0, 127)
            {
            public String map(int val)
                {
                return NOTES[val % 12] + (val / 12 - 2);
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Low Velocity", this,  "part" + part + "velocitylimitlow", color, 1, 127);
        hbox.add(comp);

        comp = new LabelledDial("High Velocity", this,  "part" + part + "velocitylimithigh", color, 1, 127);
        hbox.add(comp);

        comp = new LabelledDial("Velocity", this,  "part" + part + "velocitysensedepth", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Sensitivity Depth");
        hbox.add(comp);

        comp = new LabelledDial("Velocity", this,  "part" + part + "velocitysenseoffset", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Sensitivity Offset");
        hbox.add(comp);

        comp = new LabelledDial("Expression", this,  "part" + part + "expressionlowlimit", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Low Limit");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

                
    public JComponent addEnvelopes(int part, Color color)
        {
        Category category = new Category(this, "Envelopes", color);
        category.makePasteable("part" + part);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        comp = new LabelledDial( "Amp/Filter Env ", this,  "part" + part + "egattacktime", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Attack Time");
        hbox.add(comp);

        comp = new LabelledDial("Amp/Filter Env ", this,  "part" + part + "egdecaytime", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Decay Time");
        hbox.add(comp);

        comp = new LabelledDial("Amp/Filter Env ", this,  "part" + part + "egreleasetime", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Release Time");
        hbox.add(comp);

        comp = new LabelledDial("Pitch Env", this,  "part" + part + "pitcheginitiallevel", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Level 0");
        hbox.add(comp);

        comp = new LabelledDial("Pitch Env", this,  "part" + part + "pitchegattacktime", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Attack Time");
        hbox.add(comp);

        comp = new LabelledDial("Pitch Env", this,  "part" + part + "pitchegreleaselevel", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel("Level 4 (Rel)");
        hbox.add(comp);

        comp = new LabelledDial("Pitch Env", this,  "part" + part + "pitchegreleasetime", color, 0, 127, 64);
        ((LabelledDial)comp).addAdditionalLabel(" Time 4 (Rel) ");
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent[] reverbParams = new JComponent[17];
    public JComponent addReverb(Color color)
        {
        Category category = new Category(this, "Reverb", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();
        
        for(int i = 0; i < 17; i++)
            {
            reverbParams[i] = buildReverb(i, color);
            }
                
        final HBox outer = new HBox();
        VBox vbox = new VBox();
        params = YamahaFS1RFX.effectNameTable[REVERB];
        comp = new Chooser("Effect Type", this, "reverbtype", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                int val = model.get(key);
                outer.removeLast();
                outer.addLast(reverbParams[val]);
                outer.revalidate();
                }
            };
        vbox.add(comp);
        hbox.add(vbox);
        
        comp = new LabelledDial("Pan", this,  "reverbpan", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                if (val < 64)
                    return "< " + (64 - val);
                else if (val > 64)
                    return "" + (val - 64) + " >";
                else return "--";
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Return", this,  "reverbreturn", color, 0, 127);
        hbox.add(comp);
        
        hbox.add(Strut.makeHorizontalStrut(30));        
        hbox.add(outer);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }


    public JComponent[] variationParams = new JComponent[29];
    public JComponent addVariation(Color color)
        {
        Category category = new Category(this, "Variation", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        for(int i = 0; i < 29; i++)
            {
            variationParams[i] = buildVariation(i, color);
            }
                
        final HBox outer = new HBox();
        VBox vbox = new VBox();
        params = YamahaFS1RFX.effectNameTable[VARIATION];
        comp = new Chooser("Effect Type", this, "variationtype", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                int val = model.get(key);
                outer.removeLast();
                outer.addLast(variationParams[val]);
                outer.revalidate();
                }
            };
        vbox.add(comp);
        hbox.add(vbox);
        
        comp = new LabelledDial("Pan", this,  "variationpan", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                if (val < 64)
                    return "< " + (64 - val);
                else if (val > 64)
                    return "" + (val - 64) + " >";
                else return "--";
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("Return", this,  "variationreturn", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("Send to", this,  "sendvariationtoreverb", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Reverb");
        hbox.add(comp);

        hbox.add(Strut.makeHorizontalStrut(30));        
        hbox.add(outer);

        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent[] insertionParams = new JComponent[41];
    public JComponent addInsertion(Color color)
        {
        Category category = new Category(this, "Insertion", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        for(int i = 0; i < 41; i++)
            {
            insertionParams[i] = buildInsertion(i, color);
            }
                
        final HBox outer = new HBox();
        VBox vbox = new VBox();
        params = YamahaFS1RFX.effectNameTable[INSERTION];
        comp = new Chooser("Effect Type", this, "insertiontype", params)
            {
            public void update(String key, Model model)
                {
                super.update(key, model);
                int val = model.get(key);
                outer.removeLast();
                outer.addLast(insertionParams[val]);
                outer.revalidate();
                
                // Change controller destinations
                for(int i = 0; i < 8; i++)
                    {
                    if (controllerChoosers[i] != null)              // may not be built yet
                        controllerChoosers[i].replaceElements(buildDestinations());
                    }
                }
            };
        vbox.add(comp);
        hbox.add(vbox);
        
        comp = new LabelledDial("Pan", this,  "insertionpan", color, 0, 127)
            {
            public boolean isSymmetric() { return true; }
            public String map(int val)
                {
                if (val < 64)
                    return "< " + (64 - val);
                else if (val > 64)
                    return "" + (val - 64) + " >";
                else return "--";
                }
            };
        hbox.add(comp);

		// Why this isn't like the other wet/dry settings I have no idea...
        comp = new LabelledDial("Dry/Wet", this,  "insertionlevel", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Level");
        hbox.add(comp);

        comp = new LabelledDial("Send to", this,  "sendinsertiontoreverb", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Reverb");
        hbox.add(comp);

        comp = new LabelledDial("Send to", this,  "sendinsertiontovariation", color, 0, 127);
        ((LabelledDial)comp).addAdditionalLabel("Variation");
        hbox.add(comp);

        hbox.add(Strut.makeHorizontalStrut(30));        
        hbox.add(outer);
                
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }

    public JComponent addEqualization(Color color)
        {
        Category category = new Category(this, "Equalization", color);
                
        JComponent comp;
        String[] params;
        HBox hbox = new HBox();

        VBox vbox = new VBox();
        params = EQ_SHAPES;
        comp = new Chooser("Low Shape", this, "eqlowshape", params);
        vbox.add(comp);
        hbox.add(vbox);
                
        comp = new LabelledDial("Low Gain", this,  "eqlowgain", color, 52, 76, 64);
        hbox.add(comp);

        comp = new LabelledDial("Low Gain", this,  "eqlowfrequency", color, 4, 40)
            {
            public String map(int val)
                {
                return "" + EQ_FREQUENCIES[val - 4];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Frequency");
        hbox.add(comp);

        comp = new LabelledDial("Low Q", this,  "eqlowq", color, 1, 120)
            {
            public String map(int val)
                {
                return String.format("%1.1f", (val * 0.1));
                }
            };
        hbox.add(comp);

        
        comp = new LabelledDial("Mid Gain", this,  "eqmidgain", color, 52, 76, 64);
        hbox.add(comp);

        comp = new LabelledDial("Mid Gain", this,  "eqmidfrequency", color, 14, 54)
            {
            public String map(int val)
                {
                return "" + EQ_FREQUENCIES[val - 4];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Frequency");
        hbox.add(comp);

        comp = new LabelledDial("Mid Q", this,  "eqmidq", color, 1, 120)
            {
            public String map(int val)
                {
                return String.format("%1.1f", (val * 0.1));
                }
            };
        hbox.add(comp);

        comp = new LabelledDial("High Gain", this,  "eqhighgain", color, 52, 76, 64);
        hbox.add(comp);
        comp = new LabelledDial("High Gain", this,  "eqhighfrequency", color, 28, 58)
            {
            public String map(int val)
                {
                return "" + EQ_FREQUENCIES[val - 4];
                }
            };
        ((LabelledDial)comp).addAdditionalLabel("Frequency");
        hbox.add(comp);

        comp = new LabelledDial("High Q", this,  "eqhighq", color, 1, 120)
            {
            public String map(int val)
                {
                return String.format("%1.1f", (val * 0.1));
                }
            };
        hbox.add(comp);
        
        vbox = new VBox();
        params = EQ_SHAPES;
        comp = new Chooser("High Shape", this, "eqhighshape", params);
        vbox.add(comp);
        hbox.add(vbox);
        
        category.add(hbox, BorderLayout.CENTER);
        return category;
        }



	/** Rebuilds, then sets to default values the current reverb parameters for the given reverb type, where val = 0 is "No Effect". 
		This doesn't include the standard parameters (like pan). */
    JComponent buildReverb(int val, Color color)
        {
        VBox vbox = new VBox();
        if (val == 0)   // No Effect
            {
            }
        else
            {
            HBox hbox = new HBox();
            vbox.add(hbox);
            int version = YamahaFS1RFX.FX_VERSIONS[REVERB][val];
            for(int i = 0; i < YamahaFS1RFX.revEffectNameTable.length; i++)
                {
                if (YamahaFS1RFX.revEffectNameTable[i][version].equals("Reverb Pan")) break;              // done
                if (i == 9)
                    {
                    hbox = new HBox();
                    vbox.add(hbox);
                    }
                int type = YamahaFS1RFX.revParaTypeTable[version][i];
                String key = "reverb" + val + "parameter" + (i + 1);
                JComponent widget = YamahaFS1RFX.buildFXVals(YamahaFS1RFX.revEffectNameTable[i][version], type, key, color, this);
                model.set(key, YamahaFS1RFX.revParaInitValueTable[val][i]);

                hbox.add(widget);
                }
            }
        return vbox;
        }    
    
	/** Rebuilds, then sets to default values the current variation parameters for the given variation type, where val = 0 is "No Effect". 
		This doesn't include the standard parameters (like pan).  */
     JComponent buildVariation(int val, Color color)
        {
        VBox vbox = new VBox();
        if (val == 0)   // No Effect
            {
            }
        else
            {
            HBox hbox = new HBox();
            vbox.add(hbox);
            int version = YamahaFS1RFX.FX_VERSIONS[VARIATION][val];
            for(int i = 0; i < YamahaFS1RFX.varEffectNameTable.length; i++)
                {
                if (YamahaFS1RFX.varEffectNameTable[i][version].equals("Var Pan")) break;              // done
                if (i == 9)
                    {
                    hbox = new HBox();
                    vbox.add(hbox);
                    }
                int type = YamahaFS1RFX.varParaTypeTable[version][i];
                String key = "variation" + val + "parameter" + (i + 1);
                JComponent widget = YamahaFS1RFX.buildFXVals(YamahaFS1RFX.varEffectNameTable[i][version], type, key, color, this);
                model.set(key, YamahaFS1RFX.varParaInitValueTable[val][i]);

                hbox.add(widget);
                }
            }
        return vbox;
        }    

	/** Rebuilds, then sets to default values the current insertion parameters for the given insertion type, where val = 0 is "Thru". 
		This doesn't include the standard parameters (like pan).  */
    public JComponent buildInsertion(int val, Color color)
        {    
        VBox vbox = new VBox();
        if (val == 0)   // Thru
            {
            }
        else
            {
            HBox hbox = new HBox();
            vbox.add(hbox);
            int version = YamahaFS1RFX.FX_VERSIONS[INSERTION][val];
            for(int i = 0; i < YamahaFS1RFX.insEffectNameTable.length; i++)
                {
                if (YamahaFS1RFX.insEffectNameTable[i][version].equals("Ins Pan")) break;              // done
                if (i == 7 || i == 14)
                    {
                    hbox = new HBox();
                    vbox.add(hbox);
                    }
                int type = YamahaFS1RFX.insParaTypeTable[version][i];
                String key = "insertion" + val + "parameter" + (i + 1);
                JComponent widget = YamahaFS1RFX.buildFXVals(YamahaFS1RFX.insEffectNameTable[i][version], type, key, color, this);
                //if (i == 5 && val == 30)
                //        System.err.println(YamahaFS1RFX.effectNameTable[INSERTION][val] + " " + val + " " + YamahaFS1RFX.insEffectNameTable[i][version] + " " + version + " " + key + " " + YamahaFS1RFX.insParaInitValueTable[val][i]);
                model.set(key, YamahaFS1RFX.insParaInitValueTable[val][i]);

                hbox.add(widget);
                }
            }
        return vbox;
        }    


                



    /** Map of parameter -> index in the allParameters array. */
    HashMap allParametersToIndex = new HashMap();

    /** List of all Sysex parameters in order.  "-" is a reserved (unused and thus unnamed) parameter. */

    final static String[] allParameters = new String[] 
    {
    
    // COMMON
    
    "-",        // Name
    "-",        // Name
    "-",        // Name
    "-",        // Name
    "-",        // Name
    "-",        // Name
    "-",        // Name
    "-",        // Name
    "-",        // Name
    "-",        // Name
    "-",        // Name
    "-",        // Name
    "-",    // Reserved    
    "-",    // Reserved    
    "category",
    "-",    // Reserved    
    "performancevolume",
    "performancepan",
    "performancenoteshift",
    "-",    // Reserved    
    "individualout",
    "fseqpart",
    "fseqbank",                               // need to join         --      fseq    [note that first 6 are internal]
    "fseqnumber",                            // need to join         --      fseq
    "fseqspeedhi",                              // This has a HOLE IN IT.  It goes 0, 1, 2, 3, 4, ---- , 100, 101, ...   We will to collapse it to 5, 6, 7, 8, 9, 10, ...
    "fseqspeedlo",                              // this is not exactly documented in sysex
    "fseqstartstepoffsethi",            // need to join
    "fseqstartstepoffsetlo",
    "fseqstartstepoflooppointhi",       // need to join
    "fseqstartstepoflooppointlo",
    "fseqendstepoflooppointhi",         // need to join
    "fseqendstepoflooppointlo",                                // missing 3?  See below
    "fseqloopmode",
    "fseqplaymode",                                                     // need to customize.  This is 1..2, not 0..1
    "fseqvelocitysensitivityfortempo",
    "fseqformantpitchmode",
    "fseqkeyontrigger",
    "-",
    "fseqformantsequencedelay",
    "fseqlevelvelocitysensitivity",
    "controller1partswitch",                    // need to join
    "controller2partswitch",
    "controller3partswitch",        
    "controller4partswitch",        
    "controller5partswitch",        
    "controller6partswitch",        
    "controller7partswitch",        
    "controller8partswitch",        
    "controller1sourceswitchhi",                //need to join
    "controller1sourceswitchlo",
    "controller2sourceswitchhi",
    "controller2sourceswitchlo",
    "controller3sourceswitchhi",        
    "controller3sourceswitchlo",        
    "controller4sourceswitchhi",        
    "controller4sourceswitchlo",        
    "controller5sourceswitchhi",        
    "controller5sourceswitchlo",        
    "controller6sourceswitchhi",        
    "controller6sourceswitchlo",        
    "controller7sourceswitchhi",        
    "controller7sourceswitchlo",        
    "controller8sourceswitchhi",        
    "controller8sourceswitchlo",        
    "controller1destination",
    "controller2destination",
    "controller3destination",        
    "controller4destination",        
    "controller5destination",        
    "controller6destination",        
    "controller7destination",        
    "controller8destination",        
    "controller1depth",
    "controller2depth",
    "controller3depth",        
    "controller4depth",        
    "controller5depth",        
    "controller6depth",        
    "controller7depth",        
    "controller8depth",        


    // EFFECT


    "reverbparameter1hi",                       // These will have to be joined of course
    "reverbparameter1lo",        
    "reverbparameter2hi",        
    "reverbparameter2lo",        
    "reverbparameter3hi",        
    "reverbparameter3lo",        
    "reverbparameter4hi",        
    "reverbparameter4lo",        
    "reverbparameter5hi",        
    "reverbparameter5lo",        
    "reverbparameter6hi",        
    "reverbparameter6lo",        
    "reverbparameter7hi",        
    "reverbparameter7lo",        
    "reverbparameter8hi",        
    "reverbparameter8lo",        
    "reverbparameter9",     
    "reverbparameter10",     
    "reverbparameter11",     
    "reverbparameter12",     
    "reverbparameter13",     
    "reverbparameter14",     
    "reverbparameter15",     
    "reverbparameter16",     
    
    
    "variationparameter1hi",                    // These will have to be joined of course
    "variationparameter1lo",        
    "variationparameter2hi",        
    "variationparameter2lo",        
    "variationparameter3hi",        
    "variationparameter3lo",        
    "variationparameter4hi",        
    "variationparameter4lo",        
    "variationparameter5hi",        
    "variationparameter5lo",        
    "variationparameter6hi",        
    "variationparameter6lo",        
    "variationparameter7hi",        
    "variationparameter7lo",        
    "variationparameter8hi",        
    "variationparameter8lo",        
    "variationparameter9hi",        
    "variationparameter9lo",     
    "variationparameter10hi",     
    "variationparameter10lo",     
    "variationparameter11hi",     
    "variationparameter11lo",     
    "variationparameter12hi",     
    "variationparameter12lo",     
    "variationparameter13hi",     
    "variationparameter13lo",     
    "variationparameter14hi",     
    "variationparameter14lo",     
    "variationparameter15hi",     
    "variationparameter15lo",     
    "variationparameter16hi",     
    "variationparameter16lo",     


    "insertionparameter1hi",                    // These will have to be joined of course
    "insertionparameter1lo",        
    "insertionparameter2hi",        
    "insertionparameter2lo",        
    "insertionparameter3hi",        
    "insertionparameter3lo",        
    "insertionparameter4hi",        
    "insertionparameter4lo",        
    "insertionparameter5hi",        
    "insertionparameter5lo",        
    "insertionparameter6hi",        
    "insertionparameter6lo",        
    "insertionparameter7hi",        
    "insertionparameter7lo",        
    "insertionparameter8hi",        
    "insertionparameter8lo",        
    "insertionparameter9hi",        
    "insertionparameter9lo",     
    "insertionparameter10hi",     
    "insertionparameter10lo",     
    "insertionparameter11hi",     
    "insertionparameter11lo",     
    "insertionparameter12hi",     
    "insertionparameter12lo",     
    "insertionparameter13hi",     
    "insertionparameter13lo",     
    "insertionparameter14hi",     
    "insertionparameter14lo",     
    "insertionparameter15hi",     
    "insertionparameter15lo",     
    "insertionparameter16hi",     
    "insertionparameter16lo",     


    "reverbtype",
    "reverbpan",
    "reverbreturn",
    "variationtype",
    "variationpan",
    "variationreturn",
    "sendvariationtoreverb",
    "insertiontype",
    "insertionpan",
    "sendinsertiontoreverb",
    "sendinsertiontovariation",
    "insertionlevel",
    "eqlowgain",
    "eqlowfrequency",
    "eqlowq",
    "eqlowshape",
    "eqmidgain",
    "eqmidfrequency",
    "eqmidq",       
    "eqhighgain",        
    "eqhighfrequency",        
    "eqhighq",        
    "eqhighshape",        
    "-",    // Reserved    


    // PERFORMANCE PART


    "part1notereserve",
    "part1banknumber",
    "part1programnumber",
    "part1rcvchannelmax",                       // we will have to customize this because it has a hole in it
    "part1rcvchannel",                           // we will have to customize this because it has a hole in it
    "part1monopolymode",
    "part1monopriority",
    "part1filtersw",
    "part1noteshift",
    "part1detune",
    "part1voicedunvoicedbalance",
    "part1volume",
    "part1velocitysensedepth",
    "part1velocitysenseoffset",
    "part1pan",
    "part1notelimitlow",
    "part1notelimithigh",
    "part1drylevel",
    "part1variationsend",
    "part1reverbsend",
    "part1insertionsw",
    "part1lfo1rate",
    "part1lfo1pitchmoddepth",
    "part1lfo1delay",
    "part1filtercutofffreq",
    "part1filterresonance",
    "part1egattacktime",
    "part1egdecaytime",
    "part1egreleasetime",
    "part1formant",
    "part1fm",
    "part1filteregdepth",
    "part1pitcheginitiallevel",
    "part1pitchegattacktime",
    "part1pitchegreleaselevel",
    "part1pitchegreleasetime",
    "part1portamentoswitchmode",                        // part1portamentoswitch        part1portamentomode
    "part1portamentotime",
    "part1pitchbendrangehigh",
    "part1pitchbendrangelow",
    "part1panscaling",
    "part1panlfodepth",
    "part1velocitylimitlow",
    "part1velocitylimithigh",
    "part1expressionlowlimit",
    "part1sustainrcvsw",
    "part1lfo2rate",
    "part1lfo2moddepth",
    "-",    // Reserved    
    "-",    // Reserved    
    "-",    // Reserved    
    "-",    // Reserved    

    "part2notereserve",
    "part2banknumber",
    "part2programnumber",
    "part2rcvchannelmax",
    "part2rcvchannel",
    "part2monopolymode",
    "part2monopriority",
    "part2filtersw",
    "part2noteshift",
    "part2detune",
    "part2voicedunvoicedbalance",
    "part2volume",
    "part2velocitysensedepth",
    "part2velocitysenseoffset",
    "part2pan",
    "part2notelimitlow",
    "part2notelimithigh",
    "part2drylevel",
    "part2variationsend",
    "part2reverbsend",
    "part2insertionsw",
    "part2lfo1rate",
    "part2lfo1pitchmoddepth",
    "part2lfo1delay",
    "part2filtercutofffreq",
    "part2filterresonance",
    "part2egattacktime",
    "part2egdecaytime",
    "part2egreleasetime",
    "part2formant",
    "part2fm",
    "part2filteregdepth",
    "part2pitcheginitiallevel",
    "part2pitchegattacktime",
    "part2pitchegreleaselevel",
    "part2pitchegreleasetime",
    "part2portamentoswitchmode",
    "part2portamentotime",
    "part2pitchbendrangehigh",
    "part2pitchbendrangelow",
    "part2panscaling",
    "part2panlfodepth",
    "part2velocitylimitlow",
    "part2velocitylimithigh",
    "part2expressionlowlimit",
    "part2sustainrcvsw",
    "part2lfo2rate",
    "part2lfo2moddepth",
    "-",    // Reserved    
    "-",    // Reserved    
    "-",    // Reserved    
    "-",    // Reserved    

    "part3notereserve",
    "part3banknumber",
    "part3programnumber",
    "part3rcvchannelmax",
    "part3rcvchannel",
    "part3monopolymode",
    "part3monopriority",
    "part3filtersw",
    "part3noteshift",
    "part3detune",
    "part3voicedunvoicedbalance",
    "part3volume",
    "part3velocitysensedepth",
    "part3velocitysenseoffset",
    "part3pan",
    "part3notelimitlow",
    "part3notelimithigh",
    "part3drylevel",
    "part3variationsend",
    "part3reverbsend",
    "part3insertionsw",
    "part3lfo1rate",
    "part3lfo1pitchmoddepth",
    "part3lfo1delay",
    "part3filtercutofffreq",
    "part3filterresonance",
    "part3egattacktime",
    "part3egdecaytime",
    "part3egreleasetime",
    "part3formant",
    "part3fm",
    "part3filteregdepth",
    "part3pitcheginitiallevel",
    "part3pitchegattacktime",
    "part3pitchegreleaselevel",
    "part3pitchegreleasetime",
    "part3portamentoswitchmode",
    "part3portamentotime",
    "part3pitchbendrangehigh",
    "part3pitchbendrangelow",
    "part3panscaling",
    "part3panlfodepth",
    "part3velocitylimitlow",
    "part3velocitylimithigh",
    "part3expressionlowlimit",
    "part3sustainrcvsw",
    "part3lfo2rate",
    "part3lfo2moddepth",
    "-",    // Reserved    
    "-",    // Reserved    
    "-",    // Reserved    
    "-",    // Reserved    

    "part4notereserve",
    "part4banknumber",
    "part4programnumber",
    "part4rcvchannelmax",
    "part4rcvchannel",
    "part4monopolymode",
    "part4monopriority",
    "part4filtersw",
    "part4noteshift",
    "part4detune",
    "part4voicedunvoicedbalance",
    "part4volume",
    "part4velocitysensedepth",
    "part4velocitysenseoffset",
    "part4pan",
    "part4notelimitlow",
    "part4notelimithigh",
    "part4drylevel",
    "part4variationsend",
    "part4reverbsend",
    "part4insertionsw",
    "part4lfo1rate",
    "part4lfo1pitchmoddepth",
    "part4lfo1delay",
    "part4filtercutofffreq",
    "part4filterresonance",
    "part4egattacktime",
    "part4egdecaytime",
    "part4egreleasetime",
    "part4formant",
    "part4fm",
    "part4filteregdepth",
    "part4pitcheginitiallevel",
    "part4pitchegattacktime",
    "part4pitchegreleaselevel",
    "part4pitchegreleasetime",
    "part4portamentoswitchmode",
    "part4portamentotime",
    "part4pitchbendrangehigh",
    "part4pitchbendrangelow",
    "part4panscaling",
    "part4panlfodepth",
    "part4velocitylimitlow",
    "part4velocitylimithigh",
    "part4expressionlowlimit",
    "part4sustainrcvsw",
    "part4lfo2rate",
    "part4lfo2moddepth",
    "-",    // Reserved    
    "-",    // Reserved    
    "-",    // Reserved    
    "-",    // Reserved    
    };
    

        
	/** Returns the part associated with the given key.  For example, "part4pitchbendrangehigh" -> 4 and "eqlowgain" -> 0 */
    // common = 0, part = 1...4
    public int getPart(String key)
        {
        if (key.startsWith("part"))
            {
            return StringUtility.getFirstInt(key);
            }
        else return 0;
        }        

	/** Returns the address associated with the given *sysex key*. */
    public int getAddress(String key)
        {
        int addr = ((Integer)(allParametersToIndex.get(key))).intValue();
        if (addr < 128 + 0x40) return addr;
        // it's in a part
        addr -= (128 + 0x40);
        addr = addr % 52;		// part size
        return addr;
        }

    public Object[] emitAll(String key)
        {
        if (key.equals("number")) return new Object[0];  // this is not emittable
        if (key.equals("bank")) return new Object[0];  // this is not emittable

        if (key.equals("name"))
            {
            Object[] result = new Object[10];
            String name = model.get("name", "INIT VOICE") + "          ";

            for(int i = 0; i < 10; i++)
                {
                int ADDRESS = i;                // we're at the very beginning, so our addresses just happen to be 0...9
                int LSB = (byte)(name.charAt(i));
                byte[] data = new byte[] { (byte)0xF0, (byte)0x43, (byte)(16 + getID() - 1), (byte)0x5E, 
                    (byte)16,                      // HIGH
                    (byte)0,                       // MEDIUM
                    (byte)ADDRESS,                 // LOW
                    (byte)0,
                    (byte)LSB, 
                    (byte)0xF7 };
                result[i] = data;
                }
            return result;
            }
        else
            {
            int address = 0;
            int address2 = 0;
            int val = 0;
            int val2 = 0;
            boolean pair = false;
            int part = getPart(key);
            
            if (key.equals("fseq"))
                {
                int v = model.get(key);
                address = getAddress("fseqnumber");
                val = v < 6 ? v :v - 6;
                address2 = getAddress("fseqbank");
                val2 = v < 6 ? 0 : 1;
                pair = true;
                }
            else if (key.equals("fseqspeed"))
                {
                int v = model.get(key);
                if (v > 4) key += 95;           // open the hole
                address = getAddress("fseqspeedhi");
                val = v; // (v >>> 7) & 127;
                //address2 = getAddress("fseqspeedlo");
                //val2 = (v & 127);               
                //pair = true;
                }
            else if (key.equals("fseqstartstepoffset"))
                {
                int v = model.get(key);
                address = getAddress("fseqstartstepoffsethi");
                val = v; // (v >>> 7) & 127;
                //address2 = getAddress("fseqstartstepoffsetlo");
                //val2 = (v & 127);               
                //pair = true;
                }
            else if (key.equals("fseqstartstepoflooppoint"))
                {
                int v = model.get(key);
                address = getAddress("fseqstartstepoflooppointhi");
                val = v; // (v >>> 7) & 127;
                //address2 = getAddress("fseqstartstepoflooppointlo");
                //val2 = (v & 127);               
                //pair = true;
                }
            else if (key.equals("fseqendstepoflooppoint"))
                {
                int v = model.get(key);
                address = getAddress("fseqendstepoflooppointhi");
                val = v; // (v >>> 7) & 127;
                //address2 = getAddress("fseqendstepoflooppointlo");
                //val2 = (v & 127);               
                //pair = true;
                }
            else if (key.equals("fseqplaymode"))
                {
                int v = model.get(key);
                address = getAddress("fseqplaymode");
                val = v + 1;
                }
            else if (key.startsWith("controller") && (key.endsWith("part1") || key.endsWith("part2") || key.endsWith("part3") || key.endsWith("part4")))
                {
                int cont = StringUtility.getFirstInt(key);
                val = ((model.get("controller" + cont + "part1") << 0) | 
                    (model.get("controller" + cont + "part2") << 1) |
                    (model.get("controller" + cont + "part3") << 2) |
                    (model.get("controller" + cont + "part4") << 3));
                address = getAddress("controller" + cont + "partswitch");
                }
            else if (key.startsWith("controller") && (key.contains("kn") || key.contains("mc") || key.endsWith("fc") || key.endsWith("bc") || key.endsWith("pb") || key.endsWith("mw") || key.endsWith("cat") || key.endsWith("pat")))
                {
                int cont = StringUtility.getFirstInt(key);
                val = ((model.get("controller" + cont + "kn1") << 0) | 
                    (model.get("controller" + cont + "kn2") << 1) |
                    (model.get("controller" + cont + "kn3") << 2) |
                    (model.get("controller" + cont + "kn4") << 3) | 
                    (model.get("controller" + cont + "mc1") << 4) |
                    (model.get("controller" + cont + "mc2") << 5) |
                    (model.get("controller" + cont + "pb") << 6) | 	
                	(model.get("controller" + cont + "cat") << 7) | 
                    (model.get("controller" + cont + "pat") << 8) |
                    (model.get("controller" + cont + "fc") << 9) |
                    (model.get("controller" + cont + "bc") << 10) | 
                    (model.get("controller" + cont + "mc3") << 11) |
                    (model.get("controller" + cont + "mw") << 12) |
                    (model.get("controller" + cont + "mc4") << 13));
                address = getAddress("controller" + cont + "sourceswitchhi");
                }
            else if (key.startsWith("reverb") && !key.equals("reverbtype") && !key.equals("reverbpan") && !key.equals("reverbreturn"))
                {
                int param = StringUtility.getSecondInt(key);
                int effect = model.get("reverbtype");
                
                int version = YamahaFS1RFX.FX_VERSIONS[REVERB][effect];
                int paraType = YamahaFS1RFX.revParaTypeTable[version][param - 1];
                int address_msb = 0x00;
                int address_lsb = YamahaFS1RFX.revEffectParaLSBTable[param - 1][version];
                address = (address_msb << 7) | address_lsb;
                
                val = ((model.get(key) + YamahaFS1RFX.sysexMinTable[paraType]));
                /*
                if (address > 0x60)                     // one byte                                     
                    {
                    val = ((model.get(key) + YamahaFS1RFX.sysexMinTable[paraType]) & 127);
                    }
                else                                            // two bytes
                    {
                    val = ((model.get(key) + YamahaFS1RFX.sysexMinTable[paraType]) >>> 7) & 127;
                    val2 = ((model.get(key) + YamahaFS1RFX.sysexMinTable[paraType]) & 127);
                    address2 = address + 1;
                    pair = true;
                    }
                */
                }
            else if (key.startsWith("variation") && !key.equals("variationtype") && !key.equals("variationpan") && !key.equals("variationreturn"))
                {
                int param = StringUtility.getSecondInt(key);
                int effect = model.get("variationtype");

                int version = YamahaFS1RFX.FX_VERSIONS[VARIATION][effect];
                int paraType = YamahaFS1RFX.varParaTypeTable[version][param - 1];
                int address_msb = YamahaFS1RFX.varEffectParaMSBTable[param - 1][version];
                int address_lsb = YamahaFS1RFX.varEffectParaLSBTable[param - 1][version];
                address = (address_msb << 7) | address_lsb;
                    
                val = ((model.get(key) + YamahaFS1RFX.sysexMinTable[paraType]));
                /*
                val = ((model.get(key) + YamahaFS1RFX.sysexMinTable[paraType]) >>> 7) & 127;
                val2 = ((model.get(key) + YamahaFS1RFX.sysexMinTable[paraType]) & 127);
                address2 = address + 1;
                pair = true;
                */
                }
            else if (key.startsWith("insertion") && !key.equals("insertiontype") && !key.equals("insertionpan") && !key.equals("insertionlevel"))
                {
                int param = StringUtility.getSecondInt(key);
                int effect = model.get("insertiontype");

                int version = YamahaFS1RFX.FX_VERSIONS[INSERTION][effect];
                int paraType = YamahaFS1RFX.insParaTypeTable[version][param - 1];
                int address_msb = 0x01;
                int address_lsb = YamahaFS1RFX.insEffectParaLSBTable[param - 1][version];
                address = (address_msb << 7) | address_lsb;
                    
                val = ((model.get(key) + YamahaFS1RFX.sysexMinTable[paraType]));
                /*
                val = ((model.get(key) + YamahaFS1RFX.sysexMinTable[paraType]) >>> 7) & 127;
                val2 = ((model.get(key) + YamahaFS1RFX.sysexMinTable[paraType]) & 127);
                address2 = address + 1;
                pair = true;
                */
                }
            else if (key.endsWith("rcvchannelmax"))
                {
                address = getAddress(key);
                val = model.get(key);
                if (val == 16)  // Off
                    {
                    val = 0x7F;
                    }
                }
            else if (key.endsWith("rcvchannel"))
                {
                address = getAddress(key);
                val = model.get(key);
                if (val == 17)  // Off, note 17 not 16 as in part1rcvchannelmax
                    {
                    val = 0x7F;
                    }
                }
            else if (key.endsWith("portamentoswitch") || key.endsWith("portamentomode"))
                {
                address = getAddress("part" + part + "portamentoswitchmode");
                val = (model.get("part" + part + "portamentoswitch") << 0) |
                    (model.get("part" + part + "portamentomode") << 1);
                }
            else
                {
                address = getAddress(key);
                val = model.get(key);
                }

            int MSB = (val >>> 7) & 127;
            int LSB = (val & 127);
                        
            byte[] data = new byte[] { (byte)0xF0, (byte)0x43, (byte)(16 + getID() - 1), (byte)0x5E, 
                (byte)(part == 0 ? 0x10 : 0x30 - 1 + part),     // HIGH
                (byte)((address >>> 7) & 127),                   // MEDIUM
                (byte)(address & 127),                          // LOW
                (byte)MSB,
                (byte)LSB, 
                (byte)0xF7 };

            if (pair)
                {
                MSB = (val2 >>> 7) & 127;
                LSB = (val2 & 127);
                                                
                byte[] data2 = new byte[] { (byte)0xF0, (byte)0x43, (byte)(16 + getID() - 1), (byte)0x5E, 
                    (byte)(part == 0 ? 0x10 : 0x30 - 1 + part),     // HIGH
                    (byte)((address2 >>> 7) & 127),                                  // MEDIUM
                    (byte)(address2 & 127),                                  // LOW
                    (byte)MSB,
                    (byte)LSB, 
                    (byte)0xF7 };
                                        
                return new Object[] { data, data2 };
                }
            else
                {
                return new Object[] { data };
                }
            }
        }

    public static final int POS_FSEQBANK = 0x16;
    public static final int POS_FSEQNUMBER = 0x17;
    public static final int POS_FSEQSPEEDHI = 0x18;
    public static final int POS_FSEQSPEEDLO = 0x19;
    public static final int POS_FSEQSTARTSTEPOFFSETHI = 0x1A;
    public static final int POS_FSEQSTARTSTEPOFFSETLO = 0x1B;
    public static final int POS_FSEQSTARTSTEPOFLOOPPOINTHI = 0x1C;
    public static final int POS_FSEQSTARTSTEPOFLOOPPOINTLO = 0x1D;
    public static final int POS_FSEQENDSTEPOFLOOPPOINTHI = 0x1E;
    public static final int POS_FSEQENDSTEPOFLOOPPOINTLO = 0x1F;
    public static final int POS_FSEQPLAYMODE = 0x21;
    public static final int POS_CONTROLLER1PARTSWITCH = 0x28;
    public static final int POS_CONTROLLER1SOURCESWITCHHI = 0x30;
    public static final int POS_REVERBPARAMETER1HI = 0x50;
    public static final int POS_VARIATIONPARAMETER1HI = 0x68;
    public static final int POS_INSERTIONPARAMETER1HI = 136;
    public static final int POS_REVERBTYPE = 168;

    public static final int POS_PART_START = 192;
    public static final int PART_LENGTH = 52;
    public static final int OFFSET_RCVCHANNELMAX = 0x03;
    public static final int OFFSET_RCVCHANNEL = 0x04;
    public static final int OFFSET_PORTAMENTOSWITCHMODE = 0x24;


	// Extracts the key from allParameters, then casts it to a byte.  A syntax sugar method, that's all. 
    public byte getB(int pos)
        {
        String key = allParameters[pos];
        if (key.equals("-")) return (byte)0;
        else return (byte)(model.get(key));
        }

    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        if (tempModel == null)
            tempModel = getModel();

        final int BYTE_COUNT = 400;
                
        byte[] data = new byte[BYTE_COUNT + 11];
        data[0] = (byte)0xF0;
        data[1] = (byte)0x43;
        data[2] = (byte)(getID() - 1);
        data[3] = (byte)0x5E;
        data[4] = (byte)(BYTE_COUNT >>> 7);
        data[5] = (byte)(BYTE_COUNT & 127);
        data[6] = (byte)(toWorkingMemory ? 0x10 : tempModel.get("number"));
        data[7] = (byte)0x0;
        data[8] = (byte)0x0;
        
        String name = model.get("name", "INIT VOICE") + "          ";
        for(int i = 0; i < 12; i++)     
            {
            data[i + 9] = (byte)(name.charAt(i));
            }

        for(int i = 12; i < POS_FSEQBANK; i++)
            {
            data[i + 9] = getB(i);
            }
                
        int val = model.get("fseq");
        data[POS_FSEQBANK + 9] = (byte)(val < 6 ? 0 : 1);
        data[POS_FSEQNUMBER + 9] = (byte)(val < 6 ? val : val - 6);
                
        for(int i = POS_FSEQNUMBER + 1 ; i < POS_FSEQSPEEDHI; i++)
            {
            data[i + 9] = getB(i);
            }

        val = model.get("fseqspeed");
        if (val > 4) val += 95;         // recreate the hole
        data[POS_FSEQSPEEDHI + 9] = (byte)((val >>> 7) & 127);
        data[POS_FSEQSPEEDLO + 9] = (byte)(val & 127);

        val = model.get("fseqstartstepoffset");
        data[POS_FSEQSTARTSTEPOFFSETHI + 9] = (byte)((val >>> 7) & 127);
        data[POS_FSEQSTARTSTEPOFFSETLO + 9] = (byte)(val & 127);

        val = model.get("fseqstartstepoflooppoint");
        data[POS_FSEQSTARTSTEPOFLOOPPOINTHI + 9] = (byte)((val >>> 7) & 127);
        data[POS_FSEQSTARTSTEPOFLOOPPOINTLO + 9] = (byte)(val & 127);

        val = model.get("fseqendstepoflooppoint");
        data[POS_FSEQENDSTEPOFLOOPPOINTHI + 9] = (byte)((val >>> 7) & 127);
        data[POS_FSEQENDSTEPOFLOOPPOINTLO + 9] = (byte)(val & 127);

        for(int i = POS_FSEQENDSTEPOFLOOPPOINTLO + 1; i < POS_FSEQPLAYMODE; i++)
            {
            data[i + 9] = getB(i);
            }

        val = model.get("fseqplaymode");
        val += 1;
        data[POS_FSEQPLAYMODE + 9] = (byte)val;

        for(int i = POS_FSEQPLAYMODE + 1; i < POS_CONTROLLER1PARTSWITCH; i++)
            {
            data[i + 9] = getB(i);
            }

        for(int i = 0; i < 8; i++)
            {
            data[POS_CONTROLLER1PARTSWITCH + i + 9] = (byte)
                (
                (model.get("controller" + (i+1) + "part1") << 0) | 
                (model.get("controller" + (i+1) + "part2") << 1) |
                (model.get("controller" + (i+1) + "part3") << 2) |
                (model.get("controller" + (i+1) + "part4") << 3)
                );
            }

        for(int i = 0; i < 8; i++)
            {
            data[POS_CONTROLLER1SOURCESWITCHHI + i * 2 + 9] = (byte)
                (
                (model.get("controller" + (i+1) +"cat") << 0) | 
                (model.get("controller" + (i+1) +"pat") << 1) |
                (model.get("controller" + (i+1) +"fc") << 2) |
                (model.get("controller" + (i+1) +"bc") << 3) | 
                (model.get("controller" + (i+1) +"mc3") << 4) |
                (model.get("controller" + (i+1) +"mw") << 5) |
                (model.get("controller" + (i+1) +"mc4") << 6)
                );

            data[POS_CONTROLLER1SOURCESWITCHHI + i * 2 + 1 + 9] = (byte)
                (
                (model.get("controller" + (i+1) +"kn1") << 0) | 
                (model.get("controller" + (i+1) +"kn2") << 1) |
                (model.get("controller" + (i+1) +"kn3") << 2) |
                (model.get("controller" + (i+1) +"kn4") << 3) | 
                (model.get("controller" + (i+1) +"mc1") << 4) |
                (model.get("controller" + (i+1) +"mc2") << 5) |
                (model.get("controller" + (i+1) +"pb") << 6)
                );
            }
                
        for(int i = POS_CONTROLLER1SOURCESWITCHHI + 8 * 2; i < POS_REVERBPARAMETER1HI; i++)
            {
            data[i + 9] = getB(i);
            }

        for(int i = POS_REVERBTYPE; i < POS_PART_START; i++)
            {
            data[i + 9] = getB(i);
            }

        int reverbType = model.get("reverbtype");
        int version = YamahaFS1RFX.FX_VERSIONS[REVERB][reverbType];
        if (version != -1)
            for(int i = 0; i < YamahaFS1RFX.revEffectNameTable.length; i++)
                {
                if (YamahaFS1RFX.revEffectNameTable[i][version].equals("Reverb Pan")) break;
                        
                int paraType = YamahaFS1RFX.revParaTypeTable[version][i];
                val = model.get("reverb" + reverbType + "parameter" + (i + 1)) + YamahaFS1RFX.sysexMinTable[paraType];
                int address = (0x00 << 7) | YamahaFS1RFX.revEffectParaLSBTable[i][version];
                if (address < 0x60)     // two bytes
                    {
                    data[address + 9] = (byte)((val >>> 7) & 127);          // hi   
                    data[address + 1 + 9] = (byte)(val & 127);                      // lo
                    }
                else
                    {
                    data[address + 9] = (byte)(val & 127);
                    }
                }

        // let's emit the effects parameters after the type, it's out of order but
        // makes sense for doing the same thing when parsing back in
        int variationType = model.get("variationtype");
        version = YamahaFS1RFX.FX_VERSIONS[VARIATION][variationType];
        if (version != -1)
            for(int i = 0; i < YamahaFS1RFX.varEffectNameTable.length; i++)
                {
                if (YamahaFS1RFX.varEffectNameTable[i][version].equals("Var Pan")) break;
                        
                int paraType = YamahaFS1RFX.varParaTypeTable[version][i];
                val = model.get("variation" + variationType + "parameter" + (i + 1)) + YamahaFS1RFX.sysexMinTable[paraType];
                int address = (YamahaFS1RFX.varEffectParaMSBTable[i][version] << 7) | YamahaFS1RFX.varEffectParaLSBTable[i][version];
                data[address + 9] = (byte)((val >>> 7) & 127);          // hi   
                data[address + 1 + 9] = (byte)(val & 127);                      // lo
                }


        int insertionType = model.get("insertiontype");
        version = YamahaFS1RFX.FX_VERSIONS[INSERTION][insertionType];
        if (version != -1)
            for(int i = 0; i < YamahaFS1RFX.insEffectNameTable.length; i++)
                {
                if (YamahaFS1RFX.insEffectNameTable[i][version].equals("Ins Pan")) break;

                int paraType = YamahaFS1RFX.insParaTypeTable[version][i];
                val = model.get("insertion" + insertionType + "parameter" + (i + 1)) + YamahaFS1RFX.sysexMinTable[paraType];
                int address = (0x01 << 7) | YamahaFS1RFX.insEffectParaLSBTable[i][version];
                data[address + 9] = (byte)((val >>> 7) & 127);          // hi   
                data[address + 1 + 9] = (byte)(val & 127);                      // lo
                }

        for(int part = 0; part < 4; part++)
            {
            int base = POS_PART_START + part * PART_LENGTH;
            for(int i = base; i < base + OFFSET_RCVCHANNELMAX; i++)
                {
                data[i + 9] = getB(i);
                }

            val = model.get("part" + (part + 1) + "rcvchannelmax");
            if (val == 16) val = 0x7F;              // recreate the hole
            data[base + OFFSET_RCVCHANNELMAX + 9] = (byte)val;

            val = model.get("part" + (part + 1) + "rcvchannel");
            if (val == 17) val = 0x7F;              // recreate the hole, notice 17 not 16
            data[base + OFFSET_RCVCHANNEL + 9] = (byte)val;
                
            for(int i = base + OFFSET_RCVCHANNEL + 1; i < base + OFFSET_PORTAMENTOSWITCHMODE; i++)
                {
                data[i + 9] = getB(i);
                }

            data[base + OFFSET_PORTAMENTOSWITCHMODE + 9] = (byte)
                (
                (model.get("part" + (part + 1) + "portamentoswitch") << 0) | 
                (model.get("part" + (part + 1) + "portamentomode") << 1) 
                );
                        
            for(int i = base + OFFSET_PORTAMENTOSWITCHMODE + 1; i < base + PART_LENGTH; i++)
                {
                data[i + 9] = getB(i);
                }
            }

        data[data.length - 2] = produceChecksum(data, 4);
        data[data.length - 1] = (byte)0xF7;
        return data;
        }





    public int parse(byte[] data, boolean fromFile)
        {
        // we're going to presume that incoming data doesn't
        // tell us anything about our bank or number; so we won't
        // change it unless we're fromFile
        if (fromFile)
            {
            model.set("bank", 0);
            model.set("number", 0);
            }
        else
            {
            // No change, we presume we set it when we did the request
            }
                
        char[] name = new char[12];
        for(int i = 0; i < 12; i++)
            {
            name[i] = (char)data[i + 9];
            model.set("name", new String(name));
            }
            
        for(int i = 12; i < POS_FSEQBANK; i++)
            {
            if (!allParameters[i].equals("-")) model.set(allParameters[i], data[i + 9]);
            }
        
        model.set("fseq", data[POS_FSEQBANK + 9] == 0 ? data[POS_FSEQNUMBER + 9] : data[POS_FSEQNUMBER + 9] + 6);
                
        for(int i = POS_FSEQNUMBER + 1; i < POS_FSEQSPEEDHI; i++)
            {
            if (!allParameters[i].equals("-")) model.set(allParameters[i], data[i + 9]);
            }

        int val = ((data[POS_FSEQSPEEDHI + 9] << 7) | data[POS_FSEQSPEEDLO + 9]);
        model.set("fseqspeed", val >= 100 ? val - 95 : val);
        
        val = ((data[POS_FSEQSTARTSTEPOFFSETHI + 9] << 7) | data[POS_FSEQSTARTSTEPOFFSETLO + 9]);
        model.set("fseqstartstepoffset", val);

        val = ((data[POS_FSEQSTARTSTEPOFLOOPPOINTHI + 9] << 7) | data[POS_FSEQSTARTSTEPOFLOOPPOINTLO + 9]);
        model.set("fseqstartstepoflooppoint", val);

        val = ((data[POS_FSEQENDSTEPOFLOOPPOINTHI + 9] << 7) | data[POS_FSEQENDSTEPOFLOOPPOINTLO + 9]);
        model.set("fseqendstepoflooppoint", val);

        for(int i = POS_FSEQENDSTEPOFLOOPPOINTLO + 1; i < POS_FSEQPLAYMODE; i++)
            {
            if (!allParameters[i].equals("-")) model.set(allParameters[i], data[i + 9]);
            }
                
        val = data[POS_FSEQPLAYMODE + 9];
        val -= 1;
        model.set("fseqplaymode", val);

        for(int i = POS_FSEQPLAYMODE + 1; i < POS_CONTROLLER1PARTSWITCH; i++)
            {
            if (!allParameters[i].equals("-")) model.set(allParameters[i], data[i + 9]);
            }

        for(int i = 0; i < 8; i++)
            {
            val = data[POS_CONTROLLER1PARTSWITCH + i + 9];
            model.set("controller" + (i+1) + "part1", (val >>> 0) & 1);
            model.set("controller" + (i+1) + "part2", (val >>> 1) & 1);
            model.set("controller" + (i+1) + "part3", (val >>> 2) & 1);
            model.set("controller" + (i+1) + "part4", (val >>> 3) & 1);
            }

        for(int i = 0; i < 8; i++)
            {
            val = data[POS_CONTROLLER1SOURCESWITCHHI + i * 2 + 9];
            model.set("controller" + (i+1) +"cat", (val >>> 0) & 1);
            model.set("controller" + (i+1) +"pat", (val >>> 1) & 1);
            model.set("controller" + (i+1) +"fc", (val >>> 2) & 1);
            model.set("controller" + (i+1) +"bc", (val >>> 3) & 1);
            model.set("controller" + (i+1) +"mc3", (val >>> 4) & 1);
            model.set("controller" + (i+1) +"mw", (val >>> 5) & 1);
            model.set("controller" + (i+1) +"mc4", (val >>> 6) & 1);

            val = data[POS_CONTROLLER1SOURCESWITCHHI + i * 2 + 1 + 9];
            model.set("controller" + (i+1) +"kn1", (val >>> 0) & 1);
            model.set("controller" + (i+1) +"kn2", (val >>> 1) & 1);
            model.set("controller" + (i+1) +"kn3", (val >>> 2) & 1);
            model.set("controller" + (i+1) +"kn4", (val >>> 3) & 1);
            model.set("controller" + (i+1) +"mc1", (val >>> 4) & 1);
            model.set("controller" + (i+1) +"mc2", (val >>> 5) & 1);
            model.set("controller" + (i+1) +"pb", (val >>> 6) & 1);
            }
                                
        for(int i = POS_CONTROLLER1SOURCESWITCHHI + 8 * 2; i < POS_REVERBPARAMETER1HI; i++)
            {
            if (!allParameters[i].equals("-")) model.set(allParameters[i], data[i + 9]);
            }
                
        for(int i = POS_REVERBTYPE; i < POS_PART_START; i++)
            {
            if (!allParameters[i].equals("-")) model.set(allParameters[i], data[i + 9]);
            }
                
        int reverbType = model.get("reverbtype");               // it's already been set
        int version = YamahaFS1RFX.FX_VERSIONS[REVERB][reverbType];
        if (version != -1)
            for(int i = 0; i < YamahaFS1RFX.revEffectNameTable.length; i++)
                {
                if (YamahaFS1RFX.revEffectNameTable[i][version].equals("Reverb Pan")) break;
                        
                int paraType = YamahaFS1RFX.revParaTypeTable[version][i];
                int address = (0x00 << 7) | YamahaFS1RFX.revEffectParaLSBTable[i][version];
                if (address < 0x60)     // two bytes
                    {
                    val = ((data[address + 9] << 7) |
                        (data[address + 1 + 9]  << 0)) - YamahaFS1RFX.sysexMinTable[paraType];
                    }
                else
                    {
                    val = data[address + 9] - YamahaFS1RFX.sysexMinTable[paraType];
                    }
                model.set("reverb" + reverbType + "parameter" + (i + 1), val);
                }

        // let's emit the effects parameters after the type, it's out of order but
        // makes sense for doing the same thing when parsing back in
        int variationType = model.get("variationtype");
        version = YamahaFS1RFX.FX_VERSIONS[VARIATION][variationType];
        if (version != -1)
            for(int i = 0; i < YamahaFS1RFX.varEffectNameTable.length; i++)
                {
                if (YamahaFS1RFX.varEffectNameTable[i][version].equals("Var Pan")) break;

                int paraType = YamahaFS1RFX.varParaTypeTable[version][i];
                int address = (YamahaFS1RFX.varEffectParaMSBTable[i][version] << 7) | YamahaFS1RFX.varEffectParaLSBTable[i][version];
                        
                val = ((data[address + 9] << 7) |
                    (data[address + 1 + 9]  << 0)) - YamahaFS1RFX.sysexMinTable[paraType];

                model.set("variation" + variationType + "parameter" + (i + 1), val);
                }


        int insertionType = model.get("insertiontype");
        version = YamahaFS1RFX.FX_VERSIONS[INSERTION][insertionType];
        if (version != -1)
            for(int i = 0; i < YamahaFS1RFX.insEffectNameTable.length; i++)
                {
                if (YamahaFS1RFX.insEffectNameTable[i][version].equals("Ins Pan")) break;

                int paraType = YamahaFS1RFX.insParaTypeTable[version][i];
                int address = (0x01 << 7) | YamahaFS1RFX.insEffectParaLSBTable[i][version];
                        
                val = ((data[address + 9] << 7) |
                    (data[address + 1 + 9]  << 0)) - YamahaFS1RFX.sysexMinTable[paraType];

                model.set("insertion" + insertionType + "parameter" + (i + 1), val);
                }


                
        for(int part = 0; part < 4; part++)
            {
            int base = POS_PART_START + part * PART_LENGTH;

            for(int i = 0; i < OFFSET_RCVCHANNELMAX; i++)
                {
                if (!allParameters[base + i].equals("-")) model.set(allParameters[base + i], data[base + i + 9]);
                }
                

            val = data[base + OFFSET_RCVCHANNELMAX + 9];
            model.set("part" + (part + 1) + "rcvchannelmax", val == 0x7F ? 16 : val);
        
            val = data[base + OFFSET_RCVCHANNEL + 9];
            model.set("part" + (part + 1) + "rcvchannel", val == 0x7F ? 17 : val);

                
            for(int i = OFFSET_RCVCHANNEL + 1; i < OFFSET_PORTAMENTOSWITCHMODE; i++)
                {
                if (!allParameters[base + i].equals("-")) model.set(allParameters[base + i], data[base + i + 9]);
                }
                                
            val = data[base + OFFSET_PORTAMENTOSWITCHMODE + 9];
            model.set("part" + (part + 1) + "portamentoswitch", (val >> 0) & 1);
            model.set("part" + (part + 1) + "portamentomode", (val >> 1) & 1);

            for(int i = OFFSET_PORTAMENTOSWITCHMODE + 1; i < PART_LENGTH; i++)
                {
                if (!allParameters[base + i].equals("-")) model.set(allParameters[base + i], data[base + i + 9]);
                }
            }
        
        revise();
        return PARSE_SUCCEEDED_UNTITLED;
        }
    
    
    
    public byte getID() 
        { 
        try 
            { 
            byte b = (byte)(Byte.parseByte(tuple.id));
            if (b >= 1 && b < 16) return b;
            }
        catch (NullPointerException e) { } // expected.  Happens when tuple's not built yet
        catch (NumberFormatException e) { e.printStackTrace(); }
        return 1;
        }
        
    public String reviseID(String id)
        {
        try 
            { 
            byte b =(byte)(Byte.parseByte(id)); 
            if (b >= 1 && b < 16) return "" + b;
            } 
        catch (NumberFormatException e) { }             // expected
        return "" + getID();
        }


    byte produceChecksum(byte[] bytes, int start)
        {
        //	It appears that the FS1R uses the same checksum as the 4-op and DX7 synths
        //
        //      The TX816 owner's manual (p. 54) says the checksum is:
        //                              "the least significant 7 bits of the 2's complement sum of 155 data bytes.
        //                               0eeeeeee must be determined so that the least significant 7 bits of the
        //                              sum of the 155 data bytes and checksum equal zero."
        //
        //              The FS1R manual says "Check-sum is a value that makes "0" (zero) in lower 7 bits of an added value 
        //                                                              of Byte Count, Address, Data, and Check-sum itself"
                
        int checksum = 0;
        for(int i = start; i < bytes.length; i++)
            checksum = (checksum + bytes[i]) & 127;
        return (byte)((128 - checksum) & 127);
        }


    // Will request an internal voice.  At present we can't
    // request voices from presets!!!!  See changePatch() as to why...
    public byte[] requestDump(Model tempModel) 
        {
        // we will have already done a change patch...
        // so all we need to do now is 
        
        return requestCurrentDump();
        }
    
    // Will request the current part
    public byte[] requestCurrentDump()
        {
        return new byte[]
            {
            (byte)0xF0,
            (byte)0x43,
            (byte)(32 + getID() - 1),
            (byte)0x5E,
            (byte)0x10,
            0, 
            0, 
            (byte)0xF7
            };
        }

    public static boolean recognize(byte[] data)
        {
        final int BYTE_COUNT = 400;
        return (data.length == BYTE_COUNT + 11 &&
            data[0] == (byte)0xF0 &&
            data[1] == (byte)0x43 &&
            // don't care about 2, it's the channel
            data[3] == (byte)0x5E);
        }
               
    public static final int MAXIMUM_NAME_LENGTH = 10;
    public String revisePatchName(String name)
        {
        name = super.revisePatchName(name);  // trim first time
        if (name.length() > MAXIMUM_NAME_LENGTH)
            name = name.substring(0, MAXIMUM_NAME_LENGTH);
        
        StringBuffer nameb = new StringBuffer(name);                            
        for(int i = 0 ; i < nameb.length(); i++)
            {
            char c = nameb.charAt(i);
            if (c < 32 || c > 127)
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
        
    public static String getSynthName() { return "Yamaha FS1R [Performance]"; }

    public void changePatch(Model tempModel) 
        {
        // We need to set "Program Change Mode = multi". 

        // Send Bank MSB, which is always 63, go figure...
        tryToSendMIDI(buildCC(getChannelOut(), 0, 63));
        // Send Bank LSB 
        tryToSendMIDI(buildCC(getChannelOut(), 32, 64 + tempModel.get("bank")));
        // Send PC
        tryToSendMIDI(buildPC(getChannelOut(), tempModel.get("number")));
                        
        // we assume that we successfully did it
        if (!isMerging())  // we're actually loading the patch, not merging with it
            {
            model.set("number", tempModel.get("number"));
            model.set("bank", tempModel.get("bank"));
            }
        }
    
    public String getPatchName(Model model) { return model.get("name", "INIT VOICE"); }

    public Model getNextPatchLocation(Model model)
        {
        int bank = model.get("bank");
        int number = model.get("number");
        
        number++;
        if (number >= 128)
            {
            bank++;
            number = 0;
            if (bank >= 4)             // C = 3, Internal = 0
                bank = 0;
            }
                
        Model newModel = buildModel();
        newModel.set("bank", bank);
        newModel.set("number", number);
        return newModel;
        }

    public String getPatchLocationName(Model model)
        {
        // getPatchLocationName() is called from sprout() as a test to see if we should enable
        // batch downloading.  If we haven't yet created an .init file, then parameters won't exist
        // yet and this method will bomb badly.  So we return null in this case.
        if (!model.exists("number")) return null;
        if (!model.exists("bank")) return null;
        
        int number = model.get("number") + 1;
        int bank = model.get("bank");
        return (bank == 0 ? "Int " : BANKS[model.get("bank")]) + 
            (number > 99 ? "" : (number > 9 ? "0" : "00")) + number;
        }
        

    public static final int FSEQ_DEFAULT_PITCH_I = 0;
    public static final int FSEQ_DEFAULT_START_DELAY_I = 1;
    public static final int FSEQ_DEFAULT_LOOP_MODE_I = 2;
    public static final int FSEQ_DEFAULT_LOOP_START_I = 3;
    public static final int FSEQ_DEFAULT_LOOP_END_I = 4;
    public static final int FSEQ_MAX_LENGTH_I = 5;

    // Taken with permission from FS1REditor by K_Take (https://synth-voice.sakura.ne.jp/fs1r_editor_english.html)
    // For each FSEQ, including the first 6 internal ones, provides default settings and the maximum length.
    // I am confused by this because FSEQ ROM Preset #0, from Facebook's patch collection, actually has less than 128 frames (6.3K bytes), but here it's listed as 504? 
    public static final int FSEQ_PARAMETER_SETTINGS[][] = new int[][]                   // [95][6] 
    {
    // INTERNAL
    { 0, 0, 0,   0, 511, 511 },  // Int 01
    { 0, 0, 0,   0, 511, 511 },  // Int 02
    { 0, 0, 0,   0, 511, 511 },  // Int 03
    { 0, 0, 0,   0, 511, 511 },  // Int 04
    { 0, 0, 0,   0, 511, 511 },  // Int 05
    { 0, 0, 0,   0, 511, 511 },  // Int 06
                        
    // PRESETS
    { 0, 0, 0,   0, 498, 504 },  // 01
    { 0, 0, 0,   0, 465, 511 },  // 02
    { 0, 0, 0,   0, 401, 511 },  // 03
    { 0, 0, 0,   0, 455, 511 },  // 04
    { 0, 0, 0,   0, 500, 511 },  // 05
    { 0, 0, 0, 430, 430, 511 },  // 06
    { 0, 0, 0,   0, 507, 510 },  // 07
    { 0, 0, 0,   0, 431, 431 },  // 08
    { 0, 0, 0,   0, 491, 500 },  // 09
    { 1, 0, 0,  62, 125, 127 },  // 10
    { 1, 0, 0, 116, 117, 117 },  // 11
    { 0, 0, 1,  24, 124, 125 },  // 12
    { 0, 0, 1,  30,  42,  77 },  // 13
    { 1, 0, 1, 126, 126, 127 },  // 14
    { 1, 0, 1,  71, 125, 127 },  // 15
    { 0, 0, 0, 127, 127, 127 },  // 16
    { 0, 0, 1,  56, 120, 121 },  // 17
    { 0, 0, 0,   0, 127, 127 },  // 18
    { 0, 0, 0, 127, 127, 127 },  // 19
    { 1, 0, 0,  64, 127, 127 },  // 20
    { 1, 0, 1, 127,   5, 127 },  // 21
    { 1, 0, 0, 106, 106, 127 },  // 22
    { 0, 0, 0,  95,  95, 127 },  // 23
    { 0, 0, 0,   0, 106, 127 },  // 24
    { 1, 0, 0,   0, 100, 127 },  // 25
    { 1, 0, 0,   0, 119, 127 },  // 26
    { 1, 0, 0,   0, 100, 127 },  // 27
    { 0, 0, 0,   0,  99, 120 },  // 28
    { 0, 0, 0,   0, 102, 127 },  // 29
    { 0, 0, 0,   0,  99, 113 },  // 30
    { 0, 0, 0,   0, 125, 127 },  // 31
    { 1, 0, 0,   0, 125, 127 },  // 32
    { 1, 0, 0, 109, 110, 127 },  // 33
    { 1, 0, 0,   0, 124, 127 },  // 34
    { 0, 0, 0,   0, 125, 127 },  // 35
    { 0, 0, 0, 105, 105, 127 },  // 36
    { 0, 0, 0,   0,  99, 103 },  // 37
    { 0, 2, 0,   0, 114, 127 },  // 38
    { 0, 0, 0, 127, 127, 127 },  // 39
    { 0, 0, 0,  50,  50, 127 },  // 40
    { 0, 0, 0,  97,  97, 127 },  // 41
    { 1, 0, 0, 127, 127, 127 },  // 42
    { 0, 0, 0,   0, 115, 127 },  // 43
    { 1, 0, 0, 127, 127, 127 },  // 44
    { 1, 0, 1,  60, 113, 127 },  // 45
    { 0, 0, 0, 127, 127, 127 },  // 46
    { 1, 0, 0,   6, 118, 127 },  // 47
    { 1, 0, 0,   0, 100, 127 },  // 48
    { 0, 0, 0, 127, 127, 127 },  // 49
    { 0, 0, 0, 127, 127, 127 },  // 50
    { 0, 0, 0, 127, 127, 127 },  // 51
    { 0, 0, 0, 127, 127, 127 },  // 52
    { 0, 0, 0, 127, 127, 127 },  // 53
    { 0, 0, 0, 127, 127, 127 },  // 54
    { 0, 0, 0, 127, 127, 127 },  // 55
    { 0, 0, 1,   0, 114, 127 },  // 56
    { 0, 0, 0,   0, 119, 127 },  // 57
    { 0, 0, 0, 127, 127, 127 },  // 58
    { 0, 0, 0, 127, 127, 127 },  // 59
    { 0, 0, 0, 127, 127, 127 },  // 60
    { 1, 0, 0, 120, 121, 127 },  // 61
    { 0, 0, 0, 127, 127, 127 },  // 62
    { 0, 0, 0, 127, 127, 127 },  // 63
    { 0, 0, 0,  39, 127, 127 },  // 64
    { 0, 0, 0, 116, 116, 116 },  // 65
    { 0, 0, 0, 127, 127, 127 },  // 66
    { 0, 0, 0,   0, 121, 127 },  // 67
    { 1, 0, 1,   0, 127, 127 },  // 68
    { 1, 0, 1,  16, 127, 127 },  // 69
    { 0, 0, 0,   0, 127, 127 },  // 70
    { 0, 0, 0,   0, 100, 127 },  // 71
    { 0, 0, 0, 120, 120, 127 },  // 72
    { 1, 0, 0,  23, 123, 127 },  // 73
    { 0, 0, 0,   0, 125, 127 },  // 74
    { 1, 0, 0,  99, 100, 127 },  // 75
    { 0, 0, 0, 127, 127, 127 },  // 76
    { 0, 0, 0, 127, 127, 127 },  // 77
    { 0, 0, 0,   0, 127, 127 },  // 78
    { 1, 0, 1,  50, 126, 127 },  // 79
    { 0, 0, 0,   0, 126, 127 },  // 80
    { 0, 0, 0,  94,  94,  94 },  // 81
    { 0, 0, 0,   0, 100, 104 },  // 82
    { 0, 0, 0,  75, 127, 127 },  // 83
    { 0, 0, 1, 123, 123, 127 },  // 84
    { 0, 0, 1, 120, 126, 127 },  // 85
    { 0, 0, 0,  63, 125, 125 },  // 86
    { 0, 0, 0, 127, 127, 127 },  // 87
    { 1, 0, 0, 119, 119, 127 },  // 88
    { 1, 0, 0,   1, 111, 127 },  // 89
    { 0, 0, 0, 127, 127, 127 },  // 90
    };





    public boolean testVerify(Synth synth2, 
        String key,
        Object obj1, Object obj2) 
        {
        int revtype = model.get("reverbtype");
        if (key.startsWith("reverb") && key.contains("parameter") && StringUtility.getFirstInt(key) != revtype)
            return true; // it's an invalid reverb type parameter
                
        int vartype = model.get("variationtype");
        if (key.startsWith("variation") && key.contains("parameter") && StringUtility.getFirstInt(key) != vartype)
            return true; // it's an invalid variation type parameter

        int instype = model.get("insertiontype");
        if (key.startsWith("insertion") && key.contains("parameter") && StringUtility.getFirstInt(key) != instype)
            return true; // it's an invalid insertion type parameter
                
        return false;
        }

    }
 
 
 
 
 
 
 
 
 
 
 
/// Everything in this class is derived from or directly taken
/// with permission from FS1REditor by K_Take (https://synth-voice.sakura.ne.jp/fs1r_editor_english.html)

class YamahaFS1RFX
    {
    // This lists the effect versions for each effect type, for the reverb, variation, and insertion effects.  
    // includes "NO EFFECT" (-1) and "THRU" (-1)
    public static final int[][] FX_VERSIONS =   // [REVERB/INSERTION/VARIATION][effect]
        {
        { -1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 3, 4, 5 },
        { -1, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 15, 16, 17, 18, 19, 20, 21, 22, 22, 22, 22 },
        { -1, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 12, 13, 13, 14, 14, 15, 16, 17, 18, 19, 20, 21, 21, 22, 23, 22, 23, 24, 25, 26, 27, 28, 29, 29, 30, 30 }
        };
        
    
    // Returns the length of the vals list for the given type, that is, String[][type].
    // This list is transposed, which is why it's complex to do.   The list is over when an element is ""     
    // This is necessary because many of the tables below are oddly transposed.
    static int listLen(String[][] vals, int type)
        {
        int n = vals.length;
        for(int i = 0; i < vals.length; i++)
            {
            if (vals[i][type].equals("")) // done
                {
                n = i;
                break;
                }
            }
        return n;
        }

    // Extracts the list String[][type], up to but not including the first "".
    // This list is transposed, which is why it's complex to do. 
    // This is necessary because many of the tables below are oddly transposed.
    static String[] reduceList(String[][] vals, int type)
        {
        String[] v = new String[listLen(vals, type)];
        int n = vals.length;
        for(int i = 0; i < v.length; i++)
            {
            v[i] = vals[i][type];
            }
        return v;
        }

        
    public static final String[] MONO_STEREO = { "Mono", "Stereo" };
    public static final String[] OFF_ON = { "Off", "On" };
    public static final String[] A_B = { "Type A", "Type B" };
    public static final String[] NORMAL_INV = { "Normal", "Inverse" };
    public static final String[] L_R = { "L", "R", "L & R" };

    /// Builds all the FX widgets.  We are provided with a name, a key, and a widget type (there are 50 some types).
    public static JComponent buildFXVals(String name, final int type, String key, Color color, Synth synth)
        {
        /// These are totally out of order because they're approximately in the order found in Main.cpp of FS1REditor
        
        // First 21 effects: effectParaValueTable[][] 
        if (type == 9 || type == 14 || type == 19 || type == 20)
            {
            VBox vbox = new VBox();
            vbox.add(new Chooser(name, synth, key, reduceList(effectParaValueTable, type)));
            return vbox;
            }
        else if (type < 21)
            {
            return new LabelledDial(name, synth, key, color, 0, listLen(effectParaValueTable, type) - 1)
                {
                public String map(int val)
                    {
                    return effectParaValueTable[val][type];
                    }
                };
            }
        else if (type == 53)
            {
            return buildFXVals(name, 1, key, color, synth);
            }
        else if (type == 54 || type == 55)
            {
            return buildFXVals(name, 4, key, color, synth);
            }
        else if (type == 34)
            {
            return buildFXVals(name, 5, key, color, synth);
            }
        else if (type == 35)
            {
            return buildFXVals(name, 5, key, color, synth);
            }



        else if (type == 21 || type == 41 || type == 43 || type == 44 || type == 46 || type == 47)
            {
            return new LabelledDial(name, synth, key, color, 0, 127);
            }
        else if (type == 48)
            {
            return new LabelledDial(name, synth, key, color, 0, 123)
                {
                public String map(int val)
                    {
                    return "" + (val + 4);                  // is this right?
                    }
                };
            }
        else if (type == 49)
            {
            return new LabelledDial(name, synth, key, color, 0, 124)
                {
                public String map(int val)
                    {
                    return "" + (val + 3);                  // is this right?
                    }
                };
            }
        else if (type == 58)
            {
            return new LabelledDial(name, synth, key, color, 0, 127)
                {
                public String map(int val)
                    {
                    return "" + (val + 1);                  // is this right?
                    }
                };
            }
        else if (type == 22)
            {
            return new LabelledDial(name, synth, key, color, 0, 126)
                {
                public String map(int val)
                    {
                    if (val == 63) return "--";
                    else if (val < 63) return "< " + (63 - val);
                    else return "" + (val - 63) + " >";
                    }
                public boolean isSymmetric() { return true; }
                };
            }
        else if (type == 23)
            {
            return new LabelledDial(name, synth, key, color, 0, 126)
                {
                public String map(int val)
                    {
                    if (val == 63) return "--";
                    else if (val < 63) return "E< " + (63 - val);
                    else return "" + (val - 63) + " >R";
                    }
                public boolean isSymmetric() { return true; }
                };
            }
        else if (type == 25)
            {
            return new LabelledDial(name, synth, key, color, 0, 126, 63);
            }
        else if (type == 38)
            {
            return new LabelledDial(name, synth, key, color, 0, 126)
                {
                public String map(int val)
                    {
                    if (val == 63) return "--";
                    else if (val < 63) return "D< " + (63 - val);
                    else return "" + (val - 63) + " >W";
                    }
                public boolean isSymmetric() { return true; }
                };
            }
        else if (type == 51)
            {
            return new LabelledDial(name, synth, key, color, 0, 126)
                {
                public String map(int val)
                    {
                    if (val == 63) return "--";
                    else if (val < 63) return "L< " + (63 - val);
                    else return "" + (val - 63) + " >H";
                    }
                public boolean isSymmetric() { return true; }
                };
            }
        else if (type == 24)
            {
            return new LabelledDial(name, synth, key, color, 0, 10)
                {
                public String map(int val)
                    {
                    return String.format("%1.1f", (val + 1) * 0.1);
                    }
                };
            }
        else if (type == 27)
            {
            return new LabelledDial(name, synth, key, color, 0, 24, 12)
                {
                public boolean isSymmetric() { return true; }
                };
            }
        else if (type == 29)
            {
            VBox vbox = new VBox();
            vbox.add(new Chooser(name, synth, key, MONO_STEREO));
            return vbox;
            }
        else if (type == 42)
            {
            VBox vbox = new VBox();
            vbox.add(new Chooser(name, synth, key, OFF_ON));
            return vbox;
            }
        else if (type == 45)
            {
            VBox vbox = new VBox();
            vbox.add(new Chooser(name, synth, key, A_B));
            return vbox;
            }
        else if (type == 56)
            {
            VBox vbox = new VBox();
            vbox.add(new Chooser(name, synth, key, NORMAL_INV));
            return vbox;
            }
        else if (type == 57)
            {
            VBox vbox = new VBox();
            vbox.add(new Chooser(name, synth, key, L_R));
            return vbox;
            }
        else if (type == 30)
            {
            return new LabelledDial(name, synth, key, color, 0, 120)
                {
                public String map(int val)
                    {
                    return "" + ((val - 60) * 3);
                    }
                public boolean isSymmetric() { return true; }
                };
            }
        else if (type == 31)
            {
            return new LabelledDial(name, synth, key, color, 0, 6)
                {
                public String map(int val)
                    {
                    return "" + (val + 4);                  // right?
                    }
                };
            }
        else if (type == 32)
            {
            return new LabelledDial(name, synth, key, color, 0, 2)
                {
                public String map(int val)
                    {
                    return "" + (val + 3);                  // right?
                    }
                };
            }
        else if (type == 33)
            {
            return new LabelledDial(name, synth, key, color, 0, 110)
                {
                public String map(int val)
                    {
                    return String.format("%1.1f", (val + 10) * 0.1);         // right?
                    }
                };
            }
        else if (type == 36)
            {
            return new LabelledDial(name, synth, key, color, 0, 42)
                {
                public String map(int val)
                    {
                    return "-" + 30 + (42 - val);                                   // right?
                    }
                };
            }
        else if (type == 37)
            {
            return new LabelledDial(name, synth, key, color, 0, 42)
                {
                public String map(int val)
                    {
                    return "-" + 6 + (42 - val);                                    // right?
                    }
                };
            }
        else if (type == 39)
            {
            return new LabelledDial(name, synth, key, color, 0, 100, 50)
                {
                public boolean isSymmetric() { return true; }
                };
            }
        else if (type == 40)
            {
            return new LabelledDial(name, synth, key, color, 0, 12, 6)
                {
                public boolean isSymmetric() { return true; }
                };
            }
        else if (type == 50)
            {
            return new LabelledDial(name, synth, key, color, 0, 50, 25)
                {
                public boolean isSymmetric() { return true; }
                };
            }
        else if (type == 50)
            {
            return new LabelledDial(name, synth, key, color, 0, 50, 25)
                {
                public boolean isSymmetric() { return true; }
                };
            }
        else if (type == 52)
            {
            return new LabelledDial(name, synth, key, color, 0, 60)
                {
                public String map(int val)
                    {
                    return "" + (val * 3);
                    }
                };
            }
        else if (type == 26)
            {
            return new LabelledDial(name, synth, key, color, 1, 13650)      
                {
                public String map(int val)
                    {
                    return String.format("%1.1f", (val * 0.1));                        // does synth need String.format?
                    }
                };
            }
        else if (type == 28)
            {
            return new LabelledDial(name, synth, key, color, 1, 6820)       
                {
                public String map(int val)
                    {
                    return String.format("%1.1f", (val * 0.1));                        // does synth need String.format?
                    }
                };
            }
        else
            {
            System.err.println("Warning (YamahaFS1RMulti) buildFXVals: no such type " + type + " for key " + key);
            return new VBox();              // Gotta return something
            }
        }
        
        
        
    /// Names to appear in the pop-up lists for each kind of effect (reverb, variation, insertion)
    public static final String[][] effectNameTable =
        {
        { "No Effect", "Hall1", "Hall2", "Room1", "Room2", "Room3", "Stage1", "Stage2", "Plate", "White Room", "Tunnel", "Basement", "Canyon", "Delay LCR", "Delay L,R", "Echo", "Cross Delay" },
        { "No Effect", "Chorus", "Celeste", "Flanger", "Symphonic", "Phaser1", "Phaser2", "Ens Detune", "Rotary SP", "Tremolo", "Auto Pan", "Auto Wah", "Touch Wah", "3-Band EQ", "HM Enhncer", "Noise Gate", "Compressor", "Distortion", "Overdrive", "Amp Sim", "Delay LCR", "Delay L,R", "Echo", "Cross Delay", "Karaoke", "Hall", "Room", "Stage", "Plate", },
        { "Thru", "Chorus", "Celeste", "Flanger", "Symphonic", "Phaser1", "Phaser2", "Pitch Change", "Ens Detune", "Rotary SP", "2WayRotary", "Tremolo", "Auto Pan", "Ambience", "A-Wah +Dist", "A-Wah +Odrv", "T-Wah +Dist", "T-Wah +Odrv", "Wah-DS-Dly", "Wah-OD-Dly", "Lo-Fi", "3-Band EQ", "HM Enhncer", "Noise Gate", "Compressor", "Comp + Dist", "Cmp+DS+Dly", "Cmp+OD+Dly", "Distortion", "Dist+Delay", "Overdrive", "Odrv+Delay", "Amp Sim", "Delay LCR", "Delay L,R", "Echo", "Cross Delay", "ER 1", "ER 2", "Gate Reverb", "Reverse Gate" }
        };


    /// For some reason, some effect values are shifted by a certain amount in sysex.  I don't know why.
    // This table provides the amount to shift by, for each effect type.
    //                                                     0    1    2    3    4    5    6    7    8    9   10   11   12   13   14   15   16   17   18   19   20   21   22   23   24   25   26   27   28   29   30   31   32   33   34   35   36   37   38   39   40   41   42   43   44   45   46   47   48   49   50   51   52   53   54   55   56   57   58
    public static final int sysexMinTable[] = new int[] { 0x00,0x00,0x00,0x22,0x00,0x04,0x1c,0x00,0x00,0x00,0x0e,0x00,0x34,0x00,0x00,0x00,0x00,0x00,0x0a,0x00,0x00,0x00,0x01,0x01,0x01,0x01,0x01,0x34,0x01,0x00,0x04,0x04,0x03,0x0a,0x08,0x1c,0x37,0x4f,0x01,0x0e,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x04,0x03,0x28,0x01,0x00,0x00,0x00,0x00,0x00,0x00,0x01 };


    // Names for widgets by parameter version for reverbs. revEffectNameTable[param][version] 
    public static final String revEffectNameTable[][] = new String[][] {                //      [15][6]
        { "Reverb Time","Reverb Time","Lch Delay" ,"Lch Delay" ,"Lch Delay1","L>R Delay"    },
        { "Diffusion"  ,"Diffusion"  ,"Rch Delay" ,"Rch Delay" ,"Lch FB Lvl","R>L Delay"    },
        { "InitDelay"  ,"InitDelay"  ,"Cch Delay" ,"FB Delay1" ,"Rch Delay1","FB Level"     },
        { "HPF Cutoff" ,"HPF Cutoff" ,"FB Delay"  ,"FB Delay2" ,"Rch FB Lvl","Input Select" },
        { "LPF Cutoff" ,"LPF Cutoff" ,"FB Level"  ,"FB Level"  ,"High Damp" ,"High Damp"    },
        { "Rev Delay"  ,"Width"      ,"Cch Level" ,"High Damp" ,"Lch Delay2","EQ LowFreq"   },
        { "Density"    ,"Height"     ,"High Damp" ,"EQ LowFreq","Rch Delay2","EQ LowGain"   },
        { "ER/Rev"     ,"Depth"      ,"EQ LowFreq","EQ LowGain","Delay2 Lvl","EQ HiFreq"    },
        { "High Damp"  ,"Wall Vary"  ,"EQ LowGain","EQ HiFreq" ,"EQ LowFreq","EQ HiGain"    },
        { "FB Level"   ,"Rev Delay"  ,"EQ HiFreq" ,"EQ HiGain" ,"EQ LowGain","Reverb Pan"   },
        { "Reverb Pan" ,"Density"    ,"EQ HiGain" ,"Reverb Pan","EQ HiFreq" ,""             },
        { ""           ,"ER/Rev"     ,"Reverb Pan",""          ,"EQ HiGain" ,""             },
        { ""           ,"High Damp"  ,""          ,""          ,"Reverb Pan",""             },
        { ""           ,"FB Level"   ,""          ,""          ,""          ,""             },
        { ""           ,"Reverb Pan" ,""          ,""          ,""          ,""             },
        };

// the only high-byte = 1 effect for reverb is pan, but I handle that separately, so effectively
// all the reverb effects have a high byte of 0.

/*
  public static final int revEffectParaMSBTable[][] = new int[][] {           // [15][6]
  { 0,0,0,0,0,0 },
  { 0,0,0,0,0,0 },
  { 0,0,0,0,0,0 },
  { 0,0,0,0,0,0 },
  { 0,0,0,0,0,0 },
  { 0,0,0,0,0,0 },
  { 0,0,0,0,0,0 },
  { 0,0,0,0,0,0 },
  { 0,0,0,0,0,0 },
  { 0,0,0,0,0,1 },
  { 1,0,0,1,0,0 },
  { 0,0,1,0,0,0 },
  { 0,0,0,0,1,0 },
  { 0,0,0,0,0,0 },
  { 0,1,0,0,0,0 },
  };
*/

    // Sysex address locations for parameters by parameter version.  revEffectParaLSBTable[param][version] 
    // There is no MSB: see above.
    public static final int revEffectParaLSBTable[][] = new int[][] {           // [15][6]
        { 0x50,0x50,0x50,0x50,0x50,0x50 },
        { 0x52,0x52,0x52,0x52,0x52,0x52 },
        { 0x54,0x54,0x54,0x54,0x54,0x54 },
        { 0x56,0x56,0x56,0x56,0x56,0x56 },
        { 0x58,0x58,0x58,0x58,0x58,0x58 },
        { 0x62,0x5a,0x5a,0x5a,0x5a,0x64 },
        { 0x63,0x5c,0x5c,0x64,0x5c,0x65 },
        { 0x64,0x5e,0x64,0x65,0x5e,0x66 },
        { 0x65,0x60,0x65,0x66,0x64,0x67 },
        { 0x66,0x62,0x66,0x67,0x65,0x29 },
        { 0x29,0x63,0x67,0x29,0x66,0x00 },
        { 0x00,0x64,0x29,0x00,0x67,0x00 },
        { 0x00,0x65,0x00,0x00,0x29,0x00 },
        { 0x00,0x66,0x00,0x00,0x00,0x00 },
        { 0x00,0x29,0x00,0x00,0x00,0x00 },
        };

    // parameter effect type for each parameter by versions in reverb.  revParaTypeTable[version][param] 
    public static final int revParaTypeTable[][] = new int[][] {                // [6][18]
        // 1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18
        { 0 ,43, 1, 2, 3,53,46,23,24,25,22, 0, 0, 0, 0, 0, 0, 0 }, // Hall1
        { 0 ,43, 1, 2, 3,54,55, 4,47,53,46,23,24,25,22, 0, 0, 0 }, // White Room
        { 26,26,26,26,25,21,24, 5,27, 6,27,22, 0, 0, 0, 0, 0, 0 }, // Delay LCR
        { 26,26,26,26,25,24, 5,27, 6,27,22, 0, 0, 0, 0, 0, 0, 0 }, // Delay L,R
        { 28,25,28,25,24,28,28,21, 5,27, 6,27,22, 0, 0, 0, 0, 0 }, // Echo
        { 28,28,25,57,24, 5,27, 6,27,22, 0, 0, 0, 0, 0, 0, 0, 0 }, // Cross Delay
        };


    // initial values for each parameter effect type for each parameter by effect (not version!).   revParaInitValueTable[effect][param] 
    public static final int revParaInitValueTable[][] = new int[][]     {       // [17][18]
        //   1    2    3    4    5    6    7    8    9  10  11  12  13  14  15  16  17  18
        {   63,   0,   0,   0,   0,   0,   0,   0,   0,  0,  0,  0,  0,  0,  0,  0,  0,  0 }, // No Effect
        {   17,  10,   9,  24,  15,  34,   4,  68,   9, 63, 63,  0,  0,  0,  0,  0,  0,  0 }, // Hall1
        {   27,  10,  28,   6,  12,  28,   3,  99,   9, 63, 63,  0,  0,  0,  0,  0,  0,  0 }, // Hall2
        {   11,   8,  12,  25,  19,  15,   4,  73,   9, 63, 63,  0,  0,  0,  0,  0,  0,  0 }, // Room1
        {    9,  10,   8,  15,  26,  11,   4,  45,   6, 63, 63,  0,  0,  0,  0,  0,  0,  0 }, // Room2
        {    6,  10,   0,  15,  12,   9,   4,  48,   4, 63, 63,  0,  0,  0,  0,  0,  0,  0 }, // Room3
        {   12,  10,  16,   7,  17,  29,   3,  60,   4, 78, 63,  0,  0,  0,  0,  0,  0,  0 }, // Stage1
        {    7,  10,   0,   0,  22,  29,   4,  45,   5, 72, 63,  0,  0,  0,  0,  0,  0,  0 }, // Stage2
        {   15,   5,   7,   6,  20,   2,   3,  63,   6, 83, 63,  0,  0,  0,  0,  0,  0,  0 }, // Plate
        {    1,   5,   0,  11,   4,  16,  73, 104,   6,  8,  4, 63,  3, 68, 63,  0,  0,  0 }, // White Room
        {   20,   6,  10,   0,  10,  33,  52,  70,  16, 20,  4, 53,  9,106, 63,  0,  0,  0 }, // Tunnel
        {    5,   6,   3,   0,   0,  26,   0,  37,  15, 32,  3, 73,  9, 35, 63,  0,  0,  0 }, // Basement
        {   59,   6,  63,   0,  11,  34,  62,  91,  13, 11,  4, 71,  3, 99, 63,  0,  0,  0 }, // Canyon
        { 3333,1667,5000,5000,  73, 100,   2,  22,  12, 18, 12, 63,  0,  0,  0,  0,  0,  0 }, // Delay LCR
        { 2500,3750,3750,3750,  86,   2,  22,  12,  18, 12, 63,  0,  0,  0,  0,  0,  0,  0 }, // Delay L,R
        { 2200,  85,2100,  84,   4,2299,2349,  62,  19,  6, 22, 11, 63,  0,  0,  0,  0,  0 }, // Echo
        { 3650,3650,  87,   1,   4,  21,  12,  22,  10, 63,  0,  0,  0,  0,  0,  0,  0,  0 }, // Cross Delay
//        { 3332,1666,4999,4999,  73, 100,   2,  22,  12, 18, 12, 63,  0,  0,  0,  0,  0,  0 }, // Delay LCR
//        { 2499,3749,3751,3749,  86,   2,  22,  12,  18, 12, 63,  0,  0,  0,  0,  0,  0,  0 }, // Delay L,R
//        { 2199,  85,2099,  84,   4,2299,2349,  62,  19,  6, 22, 11, 63,  0,  0,  0,  0,  0 }, // Echo
//        { 3649,3649,  87,   1,   4,  21,  12,  22,  10, 63,  0,  0,  0,  0,  0,  0,  0,  0 }, // Cross Delay
        };



    // Names for widgets by parameter version for variations. varEffectNameTable[param][version] 
    public static final String varEffectNameTable[][] = new String[][] {                // [14][23]
        { "LFO Freq"    ,"LFO Freq"    ,"LFO Freq"    ,"LFO Freq"   ,"LFO Freq"   ,"Detune"     ,"LFO Freq"   ,"LFO Freq"   ,"LFO Freq"   ,"LFO Freq"   ,"Sensitivty" ,"Low Freq"   ,"HPF Cutoff" ,"Attack"     ,"Attack"     ,"Drive"      ,"Drive"      ,"Lch Delay"  ,"Lch Delay"  ,"Lch Delay1" ,"L>R Delay"  ,"Delay Time" ,"Reverb Time" },
        { "LFO Depth"   ,"LFO Depth"   ,"LFO Depth"   ,"LFO Depth"  ,"LFO Depth"  ,"InitDelayL" ,"LFO Depth"  ,"AM Depth"   ,"L/R Depth"  ,"LFO Depth"  ,"Cutoff Freq","Low Gain"   ,"Drive"      ,"Release"    ,"Release"    ,"EQ Low Freq","Amp Type"   ,"Rch Delay"  ,"Rch Delay"  ,"Lch FB Lvl" ,"R>L Delay"  ,"FB Level"   ,"Diffusion"   },
        { "FB Level"    ,"FB Level"    ,"Delay Offset","Phase Shift","Phase Shift","InitDelayR" ,"EQ LowFreq" ,"PM Depth"   ,"F/R Depth"  ,"Cutoff Freq","Resonance"  ,"Mid Freq"   ,"Mix Level"  ,"Threshold"  ,"Threshold"  ,"EQ Low Gain","LPF Cutoff" ,"Cch Delay"  ,"FB Delay1"  ,"Rch Delay1" ,"FB Level"   ,"HPF Cutoff" ,"InitDelay"   },
        { "Delay Offset","Delay Offset","EQ LowFreq"  ,"FB Level"   ,"FB Level"   ,"EQ LowFreq" ,"EQ LowGain" ,"LFO Phase"  ,"Pan Dir"    ,"Resonance"  ,"EQ LowFreq" ,"Mid Gain"   ,"Var Pan"    ,"OutputLevel","Ratio"      ,"EQ Mid Freq","Edge"       ,"FB Delay"   ,"FB Delay2"  ,"Rch FB Lvl" ,"InputSelect","LPF Cutoff" ,"HPF Cutoff"  },
        { "EQ LowFreq"  ,"EQ LowFreq"  ,"EQ LowGain"  ,"Stage"      ,"Stage"      ,"EQ LowGain" ,"EQ HiFreq"  ,"EQ LowFreq" ,"EQ LowFreq" ,"EQ LowFreq" ,"EQ LowGain" ,"Mid Q"      ,"SendVar-Rev","Var Pan"    ,"OutputLevel","EQ Mid Gain","OutputLevel","FB Level"   ,"FB Level"   ,"High Damp"  ,"High Damp"  ,"Var Pan"    ,"LPF Cutoff"  },
        { "EQ LowGain"  ,"EQ LowGain"  ,"EQ HiFreq"   ,"Diffuse"    ,"LFO Phase"  ,"EQ HiFreq"  ,"EQ HiGain"  ,"EQ LowGain" ,"EQ LowGain" ,"EQ LowGain" ,"EQ HiFreq"  ,"High Freq"  ,""           ,"SendVar-Rev","Var Pan"    ,"EQ Mid Q"   ,"Var Pan"    ,"Cch Level"  ,"High Damp"  ,"Lch Delay2" ,"EQ LowFreq" ,"SendVar-Rev","Density"     },
        { "EQ HiFreq"   ,"EQ HiFreq"   ,"EQ HiGain"   ,"EQ LowFreq" ,"EQ LowFreq" ,"EQ HiGain"  ,"Var Pan"    ,"EQ HiFreq"  ,"EQ HiFreq"  ,"EQ HiFreq"  ,"EQ HiGain"  ,"High Gain"  ,""           ,""           ,"SendVar-Rev","LPF Cutoff" ,"SendVar-Rev","High Damp"  ,"EQ LowFreq" ,"Rch Delay2" ,"EQ LowGain" ,""           ,"ER/Rev"      },
        { "EQ HiGain"   ,"EQ HiGain"   ,"Var Pan"     ,"EQ LowGain" ,"EQ LowGain" ,"Var Pan"    ,"SendVar-Rev","EQ HiGain"  ,"EQ HiGain"  ,"EQ HiGain"  ,"Var Pan"    ,"Mode"       ,""           ,""           ,""           ,"Edge"       ,""           ,"EQ LowFreq" ,"EQ LowGain" ,"Delay2 Lvl" ,"EQ HiFreq"  ,""           ,"High Damp"   },
        { "Mode"        ,"LFO Phase"   ,"SendVar-Rev" ,"EQ HiFreq"  ,"EQ HiFreq"  ,"SendVar-Rev",""           ,"Mode"       ,"Var Pan"    ,"Var Pan"    ,"SendVar-Rev","Var Pan"    ,""           ,""           ,""           ,"OutputLevel",""           ,"EQ LowGain" ,"EQ HiFreq"  ,"EQ LowFreq" ,"EQ HiGain"  ,""           ,"FB Level"    },
        { "Var Pan"     ,"Var Pan"     ,""            ,"EQ HiGain"  ,"EQ HiGain"  ,""           ,""           ,"Var Pan"    ,"SendVar-Rev","SendVar-Rev",""           ,"SendVar-Rev",""           ,""           ,""           ,"Var Pan"    ,""           ,"EQ HiFreq"  ,"EQ HiGain"  ,"EQ LowGain" ,"Var Pan"    ,""           ,"Var Pan"     },
        { "SendVar-Rev" ,"SendVar-Rev" ,""            ,"Var Pan"    ,"Var Pan"    ,""           ,""           ,"SendVar-Rev",""           ,""           ,""           ,""           ,""           ,""           ,""           ,"SendVar-Rev",""           ,"EQ HiGain"  ,"Var Pan"    ,"EQ HiFreq"  ,"SendVar-Rev",""           ,"SendVar-Rev" },
        { ""            ,""            ,""            ,"SendVar-Rev","SendVar-Rev",""           ,""           ,""           ,""           ,""           ,""           ,""           ,""           ,""           ,""           ,""           ,""           ,"Var Pan"    ,"SendVar-Rev","EQ HiGain"  ,""           ,""           ,""            },
        { ""            ,""            ,""            ,""           ,""           ,""           ,""           ,""           ,""           ,""           ,""           ,""           ,""           ,""           ,""           ,""           ,""           ,"SendVar-Rev",""           ,"Var Pan"    ,""           ,""           ,""            },
        { ""            ,""            ,""            ,""           ,""           ,""           ,""           ,""           ,""           ,""           ,""           ,""           ,""           ,""           ,""           ,""           ,""           ,""           ,""           ,"SendVar-Rev",""           ,""           ,""            },
        };

    // MSB of sysex address locations for parameters by parameter version.  varEffectParaMSBTable[param][version] 
    public static final int varEffectParaMSBTable[][] = new int[][] {           // [14][23]
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 },
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 },
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 },
        { 0,0,0,0,0,0,0,1,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0 },
        { 0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,1,0 },
        { 0,0,0,0,1,1,0,0,0,0,0,0,0,1,1,0,1,0,0,0,1,1,0 },
        { 0,0,0,0,0,1,1,0,0,0,0,0,0,0,1,0,1,0,1,0,1,0,1 },
        { 0,0,1,0,0,1,1,0,0,0,1,1,0,0,0,0,0,1,1,0,1,0,1 },
        { 1,1,1,0,0,1,0,1,1,1,1,1,0,0,0,0,0,1,1,1,1,0,1 },
        { 1,1,0,0,0,0,0,1,1,1,0,1,0,0,0,1,0,1,1,1,1,0,1 },
        { 1,1,0,1,1,0,0,1,0,0,0,0,0,0,0,1,0,1,1,1,1,0,1 },
        { 0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0 },
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,1,0,0,0 },
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0 },
        };


    // LSB of sysex address locations for parameters by parameter version.  varEffectParaLSBTable[param][version] 
    public static final int varEffectParaLSBTable[][] = new int[][] {                   // [14][23]
        { 0x68,0x68,0x68,0x68,0x68,0x68,0x68,0x68,0x68,0x68,0x68,0x72,0x68,0x68,0x68,0x68,0x68,0x68,0x68,0x68,0x68,0x68,0x68 },
        { 0x6a,0x6a,0x6a,0x6a,0x6a,0x6a,0x6a,0x6a,0x6a,0x6a,0x6a,0x68,0x6a,0x6a,0x6a,0x6a,0x6a,0x6a,0x6a,0x6a,0x6a,0x6a,0x6a },
        { 0x6c,0x6c,0x6c,0x6c,0x6c,0x6c,0x72,0x6c,0x6c,0x6c,0x6c,0x6a,0x6c,0x6c,0x6c,0x6c,0x6c,0x6c,0x6c,0x6c,0x6c,0x6c,0x6c },
        { 0x6e,0x6e,0x72,0x6e,0x6e,0x7c,0x74,0x02,0x6e,0x6e,0x72,0x6c,0x2c,0x6e,0x6e,0x74,0x7c,0x6e,0x6e,0x6e,0x6e,0x6e,0x6e },
        { 0x72,0x72,0x74,0x7c,0x7c,0x7e,0x76,0x72,0x72,0x72,0x74,0x6e,0x2e,0x2c,0x70,0x76,0x6e,0x70,0x70,0x70,0x70,0x2c,0x70 },
        { 0x74,0x74,0x76,0x7e,0x00,0x00,0x78,0x74,0x74,0x74,0x76,0x74,0x00,0x2e,0x2c,0x78,0x2c,0x72,0x72,0x72,0x00,0x2e,0x7e },
        { 0x76,0x76,0x78,0x72,0x72,0x02,0x2c,0x76,0x76,0x76,0x78,0x70,0x00,0x00,0x2e,0x6e,0x2e,0x74,0x00,0x74,0x02,0x00,0x00 },
        { 0x78,0x78,0x2c,0x74,0x74,0x2c,0x2e,0x78,0x78,0x78,0x2c,0x04,0x00,0x00,0x00,0x7c,0x00,0x00,0x02,0x76,0x04,0x00,0x02 },
        { 0x04,0x02,0x2e,0x76,0x76,0x2e,0x00,0x04,0x2c,0x2c,0x2e,0x2c,0x00,0x00,0x00,0x70,0x00,0x02,0x04,0x00,0x06,0x00,0x04 },
        { 0x2c,0x2c,0x00,0x78,0x78,0x00,0x00,0x2c,0x2e,0x2e,0x00,0x2e,0x00,0x00,0x00,0x2c,0x00,0x04,0x06,0x02,0x2c,0x00,0x2c },
        { 0x2e,0x2e,0x00,0x2c,0x2c,0x00,0x00,0x2e,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x2e,0x00,0x06,0x2c,0x04,0x2e,0x00,0x2e },
        { 0x00,0x00,0x00,0x2e,0x2e,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x2c,0x2e,0x06,0x00,0x00,0x00 },
        { 0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x2e,0x00,0x2c,0x00,0x00,0x00 },
        { 0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x2e,0x00,0x00,0x00 },
        };

    // parameter effect type for each parameter by versions in variation.   varParaTypeTable[version][param] 
    public static final int varParaTypeTable[][] = new int[][] {                                        // [23][18]
        // 1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18
        {  7,21,25, 8, 5,27, 6,27,29,22,21, 0, 0, 0, 0, 0, 0, 0 }, // Chorus
        {  7,21,25, 8, 5,27, 6,27,30,22,21, 0, 0, 0, 0, 0, 0, 0 }, // Flanger
        {  7,21, 8, 5,27, 6,27,22,21, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, // Symphonic
        {  7,21,21,25,31,29, 5,27, 6,27,22,21, 0, 0, 0, 0, 0, 0 }, // Phaser1
        {  7,21,21,25,32,30, 5,27, 6,27,22,21, 0, 0, 0, 0, 0, 0 }, // Phaser2
        { 39, 8, 8, 5,27, 6,27,22,21, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, // Ens Detune
        {  7,21, 5,27, 6,27,22,21, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, // Rotary SP
        {  7,21,21,30, 5,27, 6,27,29,22,21, 0, 0, 0, 0, 0, 0, 0 }, // Tremolo
        {  7,21,21, 9, 5,27, 6,27,22,21, 0, 0, 0, 0, 0, 0, 0, 0 }, // Auto Pan
        {  7,21,21,33, 5,27, 6,27,22,21, 0, 0, 0, 0, 0, 0, 0, 0 }, // Auto Wah
        { 21,21,33, 5,27, 6,27,22,21, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, // Touch Wah
        { 34,27,10,27,33, 6,27,29,22,21, 0, 0, 0, 0, 0, 0, 0, 0 }, // 3-Band EQ
        {  6,21,21,22,21, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, // HM Enhncer
        { 11,12,36,21,22,21, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, // Noise Gate
        { 11,12,37,13,21,22,21, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, // Compressor
        { 21, 5,27,10,27,33, 3,21,21,22,21, 0, 0, 0, 0, 0, 0, 0 }, // Distortion
        { 21,14, 3,21,21,22,21, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, // Amp Sim
        { 26,26,26,26,25,21,24, 5,27, 6,27,22,21, 0, 0, 0, 0, 0 }, // Delay LCR
        { 26,26,26,26,25,24, 5,27, 6,27,22,21, 0, 0, 0, 0, 0, 0 }, // Delay L,R
        { 28,25,28,25,24,28,28,21, 5,27, 6,27,22,21, 0, 0, 0, 0 }, // Echo
        { 28,28,25,57,24, 5,27, 6,27,22,21, 0, 0, 0, 0, 0, 0, 0 }, // Cross Delay
        { 16,25, 2, 3,22,21, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, // Karaoke
        {  0,43, 1, 2, 3,46,23,24,25,22,21, 0, 0, 0, 0, 0, 0, 0 }, // Hall
        };



    // initial values for each parameter effect type for each parameter by effect (not version!).   varParaInitValueTable[effect][param] 
    // Note that I have modified some values, which seem to be off.
    public static final int varParaInitValueTable[][] = new int[][] {           // [28][18]
        //   1    2    3    4    5    6    7    8    9  10  11  12  13  14  15  16  17  18
        {   63,   0,   0,   0,   0,   0,   0,   0,   0,  0,  0,  0,  0,  0,  0,  0,  0,  0 }, // No Effect
        {    5,  46,  91,  10,  16,  12,  22,  12,   1, 63,  0,  0,  0,  0,  0,  0,  0,  0 }, // Chorus
        {   15,  25,  93, 102,  24,  12,  18,  12,   1, 63,  0,  0,  0,  0,  0,  0,  0,  0 }, // Celeste
        {   11,  30, 103,   2,  16,  12,  22,  12,   0, 63,  0,  0,  0,  0,  0,  0,  0,  0 }, // Flanger
        {   10,  40,   0,  18,  12,  22,  12,  63,   0,  0,  0,  0,  0,  0,  0,  0,  0,  0 }, // Symphonic
        {   20, 111,  76, 114,   2,   1,  19,  12,  22, 12, 63,  0,  0,  0,  0,  0,  0,  0 }, // Phaser1
        {    2, 127,  25, 114,   2,   0,  16,  12,  21, 12, 63,  0,  0,  0,  0,  0,  0,  0 }, // Phaser2
        {   20,  10,  30,  18,  12,  20,  12,  63,   0,  0,  0,  0,  0,  0,  0,  0,  0,  0 }, // Ens Detune
        {   51,  76,  18,  12,  20,  12,  63,   0,   0,  0,  0,  0,  0,  0,  0,  0,  0,  0 }, // Rotary SP
        {   84,  60,  20,  60,  19,  12,  12,  12,   0, 63,  0,  0,  0,  0,  0,  0,  0,  0 }, // Tremolo
        {   76, 127,  32,   5,  17,  12,  18,  24,  63,  0,  0,  0,  0,  0,  0,  0,  0,  0 }, // Auto Pan
        {   28,  66,  33,  28,  19,  12,  20,  12,  63,  0,  0,  0,  0,  0,  0,  0,  0,  0 }, // Auto Wah
        {   46,  28,  13,  15,  12,  20,  12,  63,   0,  0,  0,  0,  0,  0,  0,  0,  0,  0 }, // Touch Wah
        {   11,  12,  20,  12,  40,  24,  12,   0,  63,  0,  0,  0,  0,  0,  0,  0,  0,  0 }, // 3-Band EQ
        {   21,  20,  30,  63,   0,   0,   0,   0,   0,  0,  0,  0,  0,  0,  0,  0,  0,  0 }, // HM Enhncer
        {    0,  11,  27,  50,  63,   0,   0,   0,   0,  0,  0,  0,  0,  0,  0,  0,  0,  0 }, // Noise Gate
        {   10,   2,  20,   4,  80,  63,   0,   0,   0,  0,  0,  0,  0,  0,  0,  0,  0,  0 }, // Compressor
        {   60,  15,  20,  21,  22,   0,  19,  80,  48, 63,  0,  0,  0,  0,  0,  0,  0,  0 }, // Distortion
        {   29,  20,  16,  22,  20,   0,  12, 104,  55, 63,  0,  0,  0,  0,  0,  0,  0,  0 }, // Overdrive
        {   76,   3,   8, 102,  55,  63,   0,   0,   0,  0,  0,  0,  0,  0,  0,  0,  0,  0 }, // Amp Sim
        { 3333,1667,5000,5000,  73, 100,   2,  22,  12, 18, 12, 63,  0,  0,  0,  0,  0,  0 }, // Delay LCR
        { 2500,3750,3750,3750,  86,   2,  22,  12,  18, 12, 63,  0,  0,  0,  0,  0,  0,  0 }, // Delay L,R
        { 2200,  85,2100,  84,   4,2299,2349,  62,  19,  6, 22, 11, 63,  0,  0,  0,  0,  0 }, // Echo
        { 3650,3650,  87,   1,   4,  21,  12,  22,  10, 63,  0,  0,  0,  0,  0,  0,  0,  0 }, // Cross Delay
//        { 3332,1666,4999,4999,  73, 100,   2,  22,  12, 18, 12, 63,  0,  0,  0,  0,  0,  0 }, // Delay LCR
//        { 2499,3749,3751,3749,  86,   2,  22,  12,  18, 12, 63,  0,  0,  0,  0,  0,  0,  0 }, // Delay L,R
//        { 2199,  85,2099,  84,   4,2299,2349,  62,  19,  6, 22, 11, 63,  0,  0,  0,  0,  0 }, // Echo
//        { 3649,3649,  87,   1,   4,  21,  12,  22,  10, 63,  0,  0,  0,  0,  0,  0,  0,  0 }, // Cross Delay
        {   63,  96,   0,  14,  63,   0,   0,   0,   0,  0,  0,  0,  0,  0,  0,  0,  0,  0 }, // Karaoke
        {   18,  10,   8,  13,  15,   2,  49,   7,  63, 63,  0,  0,  0,  0,  0,  0,  0,  0 }, // Hall
        {    5,  10,  16,   4,  15,   2,  63,   7,  63, 63,  0,  0,  0,  0,  0,  0,  0,  0 }, // Room
        {   19,  10,  16,   7,  20,   2,  63,   5,  63, 63,  0,  0,  0,  0,  0,  0,  0,  0 }, // Stage
        {   25,  10,   6,   8,  15,   2,  63,   4,  63, 63,  0,  0,  0,  0,  0,  0,  0,  0 }, // Plate
        };




    // Names for widgets by parameter version for insertions. insEffectNameTable[param][version] 
    public static final String insEffectNameTable[][] = new String[][] {                // [18][31]
        { "LFO Freq"    ,"LFO Freq"    ,"LFO Freq"    ,"LFO Freq"     ,"LFO Freq"   ,"Pitch"      ,"Detune"     ,"LFO Freq"   ,"Rotor Speed","LFO Freq"   ,"LFO Freq"   ,"Delay Time" ,"LFO Freq"    ,"Sensitivity"  ,"Sensitivity" ,"Sample Freq","Low Freq"  ,"HPF Cutoff" ,"Attack"     ,"Attack"     ,"Attack"      ,"Attack"      ,"Drive"       ,"Drive"       ,"Drive"       ,"Lch Delay"  ,"Lch Delay"  ,"Lch Delay1" ,"L>R Delay"   ,"Early Type" ,"Gate Type"   },
        { "LFO Depth"   ,"LFO Depth"   ,"LFO Depth"   ,"LFO Depth"    ,"LFO Depth"  ,"InitDelay"  ,"InitDelayL" ,"LFO Depth"  ,"Drive Low"  ,"AM Depth"   ,"L/R Depth"  ,"Phase"      ,"LFO Depth"   ,"Cutoff Freq" ,"Cutoff Freq" ,"Word Length","Low Gain"   ,"Drive"      ,"Release"    ,"Release"    ,"Release"     ,"Release"     ,"EQ LowFreq"  ,"DS LowGain"  ,"Amp Type"    ,"Rch Delay"  ,"Rch Delay"  ,"Lch FB Lvl" ,"R>L Delay"   ,"Room Size"  ,"Room Size"   },
        { "FB Level"    ,"FB Level"    ,"Delay Offset","Phase Shift"  ,"Phase Shift","Fine1"      ,"InitDelayR" ,"EQ LowFreq" ,"Drive High" ,"PM Depth"   ,"F/L Depth"  ,"EQ LowFreq" ,"Cutoff Freq" ,"Resonance"   ,"Resonance"   ,"Output Gain","Mid Freq"   ,"Mix Level"  ,"Threshold"  ,"Threshold"  ,"Threshold"   ,"Threshold"   ,"EQ LowGain"  ,"DS MidGain"  ,"LPF Cutoff"  ,"Cch Delay"  ,"FB Delay1"  ,"Rch Delay1" ,"FB Level"    ,"Diffusion"  ,"Diffusion"   },
        { "Delay Offset","Delay Offset","EQ LowFreq"  ,"FB Level"     ,"FB Level"   ,"Fine2"      ,"EQ LowFreq" ,"EQ LowGain" ,"Low/High"   ,"LFO Phase"  ,"Pan Dir"    ,"EQ LowGain" ,"Resonance"   ,"Release"     ,"Release"     ,"LPF Cutoff" ,"Mid Gain"   ,"Ins Pan"    ,"OutputLevel","Ratio"      ,"Ratio"       ,"Ratio"       ,"EQ MidFreq"  ,"Lch Delay"   ,"Edge"        ,"FB Delay"   ,"FB Delay2"  ,"Rch FB Lvl" ,"Input Select","Init Delay" ,"Init Delay"  },
        { "EQ LowFreq"  ,"EQ LowFreq"  ,"EQ LowGain"  ,"Stage"        ,"Stage"      ,"FB Level"   ,"EQ LowGain" ,"EQ MidFreq" ,"Mic Angle"  ,"EQ LowFreq" ,"EQ LowFreq" ,"EQ HiFreq"  ,"EQ LowFreq"  ,"EQ LowFreq"  ,"Drive"       ,"LPF Reso"   ,"Mid Q"      ,"SendIns-Rev","Ins Pan"    ,"OutputLevel","Drive"       ,"Drive"       ,"EQ MidGain"  ,"Rch Delay"   ,"Output Level","FB Level"   ,"FB Level"   ,"High Damp"  ,"High Damp"   ,"FB Level"   ,"FB Level"    },
        { "EQ LowGain"  ,"EQ LowGain"  ,"EQ MidFreq"  ,"Diffuse"      ,"LFO Phase"  ,"Pan1"       ,"EQ HiFreq"  ,"EQ MidGain" ,"Cross Freq" ,"EQ LowGain" ,"EQ LowGain" ,"EQ HiGain"  ,"EQ LowGain"  ,"EQ LowGain"  ,"Output Level","Filter"     ,"High Freq"  ,"SendIns-Var","SendIns-Rev","Ins Pan"    ,"EQ LowFreq"  ,"Output Level","EQ MidQ"     ,"FB Delay"    ,"Dry/Wet"     ,"Cch Level"  ,"High Damp"  ,"Lch Delay2" ,"EQ LowFreq"  ,"HPF Cutoff" ,"HPF Cutoff"  },
        { "EQ MidFreq"  ,"EQ MidFreq"  ,"EQ MidGain"  ,"EQ LowFreq"   ,"EQ LowFreq" ,"Out Level1" ,"EQ HiGain"  ,"EQ Mid Q"   ,"EQ LowFreq" ,"EQ MidFreq" ,"EQ MidFreq" ,"Dry/Wet"    ,"EQ HiFreq"   ,"EQ HiFreq"   ,"DS LowGain"  ,"BitAssign"  ,"High Gain"  ,"InsDryLevel","SendIns-Var","SendIns-Rev","EQ LowGain"  ,"DS LowGain"  ,"LPF Cutoff"  ,"FB Level"    ,"Ins Pan"     ,"High Damp"  ,"EQ LowFreq" ,"Rch Delay2" ,"EQ LowGain"  ,"LPF Cutoff" ,"LPF Cutoff"  },
        { "EQ MidGain"  ,"EQ MidGain"  ,"EQ Mid Q"    ,"EQ LowGain"   ,"EQ LowGain" ,"Pan2"       ,"Dry/Wet"    ,"EQ HiFreq"  ,"EQ LowGain" ,"EQ MidGain" ,"EQ MidGain" ,"Ins Pan"    ,"EQ HiGain"   ,"EQ HiGain"   ,"DS MidGain"  ,"Emphasis"   ,"Mode"       ,""           ,"InsDryLevel","SendIns-Var","EQ MidFreq"  ,"DS MidGain"  ,"Edge"        ,"Delay Mix"   ,"SendIns-Rev" ,"EQ LowFreq" ,"EQ LowGain" ,"Delay2 Lvl" ,"EQ HiFreq"   ,"Liveness"   ,"Liveness"    },
        { "EQ Mid Q"    ,"EQ Mid Q"    ,"EQ HiFreq"   ,"EQ HiFreq"    ,"EQ HiFreq"  ,"Out Level2" ,"Ins Pan"    ,"EQ HiGain"  ,"EQ HiFreq"  ,"EQ MidQ"    ,"EQ MidQ"    ,"SendIns-Rev","Drive"       ,"Drive"       ,"Delay"       ,"Dry/Wet"    ,"Ins Pan"    ,""           ,""           ,"InsDryLevel","EQ MidGain"  ,"Delay"       ,"Output Level","Output Level","SendIns-Var" ,"EQ LowGain" ,"EQ HiFreq"  ,"EQ LowFreq" ,"EQ HiGain"   ,"Density"    ,"Density"     },
        { "EQ HiFreq"   ,"EQ HiFreq"   ,"EQ HiGain"   ,"EQ HiGain"    ,"EQ HiGain"  ,"Dry/Wet"    ,"SendIns-Rev","Dry/Wet"    ,"EQ HiGain"  ,"EQ HiFreq"  ,"EQ HiFreq"  ,"SendIns-Var","DS LowGain"  ,"DS LowGain"  ,"FB Level"    ,"Ins Pan"    ,"SendIns-Rev",""           ,""           ,""           ,"EQ MidQ"     ,"FB Level"    ,"Dry/Wet"     ,"Dry/Wet"     ,"InsDryLevel" ,"EQ HiFreq"  ,"EQ HiGain"  ,"EQ LowGain" ,"Dry/Wet"     ,"High Damp"  ,"High Damp"   },
        { "EQ HiGain"   ,"EQ HiGain"   ,"Dry/Wet"     ,"Dry/Wet"      ,"Dry/Wet"    ,"Ins Pan"    ,"SendIns-Var","Ins Pan"    ,"Ins Pan"    ,"EQ HiGain"  ,"EQ HiGain"  ,"InsDryLevel","DS MidGain"  ,"DS MidGain"  ,"Delay Mix"   ,"SendIns-Rev","SendIns-Var",""           ,""           ,""           ,"LPF Cutoff"  ,"Delay Mix"   ,"Ins Pan"     ,"Ins Pan"     ,""            ,"EQ HiGain"  ,"Dry/Wet"    ,"EQ HiFreq"  ,"Ins Pan"     ,"Dry/Wet"    ,"Dry/Wet"     },
        { "Mode"        ,"LFO Phase"   ,"Ins Pan"     ,"Ins Pan"      ,"Ins Pan"    ,"SendIns-Rev","InsDryLevel","SendIns-Rev","SendIns-Rev","Mode"       ,"Ins Pan"    ,""           ,"LPF Cutoff"  ,"LPF Cutoff"  ,"Dry/Wet"     ,"SendIns-Var","InsDryLevel",""           ,""           ,""           ,"Edge"        ,"Dry/Wet"     ,"SendIns-Rev" ,"SendIns-Rev" ,""            ,"Dry/Wet"    ,"Ins Pan"    ,"EQ HiGain"  ,"SendIns-Rev" ,"Ins Pan"    ,"Ins Pan"     },
        { "Dry/Wet"     ,"Dry/Wet"     ,"SendIns-Rev" ,"SendIns-Rev"  ,"SendIns-Rev","SendIns-Var",""           ,"SendIns-Var","SendIns-Var","Ins Pan"    ,"SendIns-Rev",""           ,"Output Level","Output Level","Ins Pan"     ,"InsDryLevel",""           ,""           ,""           ,""           ,"Output Level","Ins Pan"     ,"SendIns-Var" ,"SendIns-Var" ,""            ,"Ins Pan"    ,"SendIns-Rev","Dry/Wet"    ,"SendIns-Var" ,"SendIns-Rev","SendIns-Rev" },
        { "Ins Pan"     ,"Ins Pan"     ,"SendIns-Var" ,"SendIns-Var"  ,"SendIns-Var","InsDryLevel",""           ,"InsDryLevel","InsDryLevel","SendIns-Rev","SendIns-Var",""           ,"Dry/Wet"     ,"Dry/Wet"     ,"SendIns-Rev" ,""           ,""           ,""           ,""           ,""           ,"Dry/Wet"     ,"SendIns-Rev" ,"InsDryLevel" ,"InsDryLevel" ,""            ,"SendIns-Rev","SendIns-Var","Ins Pan"    ,"InsDryLevel" ,"SendIns-Var","SendIns-Var" },
        { "SendIns-Rev" ,"SendIns-Rev" ,"InsDryLevel" ,"InsDryLevel"  ,"InsDryLevel",""           ,""           ,""           ,""           ,"SendIns-Var","InsDryLevel",""           ,"Ins Pan"     ,"Ins Pan"     ,"SendIns-Var" ,""           ,""           ,""           ,""           ,""           ,"Ins Pan"     ,"SendIns-Var" ,""            ,""            ,""            ,"SendIns-Var","InsDryLevel","SendIns-Rev",""            ,"InsDryLevel","InsDryLevel" },
        { "SendIns-Var" ,"SendIns-Var" ,""            ,""             ,""           ,""           ,""           ,""           ,""           ,"InsDryLevel",""           ,""           ,"SendIns-Rev" ,"SendIns-Rev" ,"InsDryLevel" ,""           ,""           ,""           ,""           ,""           ,"SendIns-Rev" ,"InsDryLevel" ,""            ,""            ,""            ,"InsDryLevel",""           ,"SendIns-Var",""            ,""           ,""            },
        { "InsDryLevel" ,"InsDryLevel" ,""            ,""             ,""           ,""           ,""           ,""           ,""           ,""           ,""           ,""           ,"SendIns-Var" ,"SendIns-Var" ,""            ,""           ,""           ,""           ,""           ,""           ,"SendIns-Var" ,""            ,""            ,""            ,""            ,""           ,""           ,"InsDryLevel",""            ,""           ,""            },
        { ""            ,""            ,""            ,""             ,""           ,""           ,""           ,""           ,""           ,""           ,""           ,""           ,"InsDryLevel" ,"InsDryLevel" ,""            ,""           ,""           ,""           ,""           ,""           ,"InsDryLevel" ,""            ,""            ,""            ,""            ,""           ,""           ,""           ,""            ,""           ,""            },
        };

    // LSB of sysex address locations for parameters by parameter version.  insEffectParaLSBTable[param][version] 
    // All insertion effects have a high byte (MSB) of 1
    public static final int insEffectParaLSBTable[][] = new int[][] {           // [18][31]
        { 0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x08,0x1c,0x08,0x12,0x08,0x08,0x08,0x1e,0x1c,0x08,0x12,0x08,0x08,0x08,0x08,0x08,0x08,0x08 },
        { 0x0a,0x0a,0x0a,0x0a,0x0a,0x0a,0x0a,0x0a,0x0a,0x0a,0x0a,0x0a,0x0a,0x0a,0x1e,0x0a,0x08,0x0a,0x0a,0x0a,0x20,0x1e,0x0a,0x16,0x0a,0x0a,0x0a,0x0a,0x0a,0x0a,0x0a },
        { 0x0c,0x0c,0x0c,0x0c,0x0c,0x0c,0x0c,0x12,0x0c,0x0c,0x0c,0x12,0x0c,0x0c,0x20,0x0c,0x0a,0x0c,0x0c,0x0c,0x22,0x20,0x0c,0x18,0x0c,0x0c,0x0c,0x0c,0x0c,0x0c,0x0c },
        { 0x0e,0x0e,0x12,0x0e,0x0e,0x0e,0x1c,0x14,0x0e,0x22,0x0e,0x14,0x0e,0x26,0x22,0x0e,0x0c,0x30,0x0e,0x0e,0x24,0x22,0x14,0x08,0x1c,0x0e,0x0e,0x0e,0x0e,0x0e,0x0e },
        { 0x12,0x12,0x14,0x1c,0x1c,0x10,0x1e,0x1c,0x1e,0x12,0x12,0x16,0x12,0x12,0x0e,0x12,0x0e,0x31,0x30,0x10,0x08,0x0e,0x16,0x0a,0x0e,0x10,0x10,0x10,0x10,0x10,0x10 },
        { 0x14,0x14,0x1c,0x1e,0x20,0x1c,0x20,0x1e,0x1c,0x14,0x14,0x18,0x14,0x14,0x10,0x10,0x14,0x32,0x31,0x30,0x0a,0x10,0x18,0x0c,0x1a,0x12,0x12,0x12,0x20,0x12,0x12 },
        { 0x1c,0x1c,0x1e,0x12,0x12,0x1e,0x22,0x20,0x12,0x1c,0x1c,0x1a,0x16,0x16,0x12,0x14,0x10,0x33,0x32,0x31,0x0c,0x12,0x0e,0x0e,0x30,0x14,0x20,0x14,0x22,0x14,0x14 },
        { 0x1e,0x1e,0x20,0x14,0x14,0x20,0x1a,0x16,0x14,0x1e,0x1e,0x30,0x18,0x18,0x14,0x16,0x24,0x00,0x33,0x32,0x14,0x14,0x1c,0x10,0x31,0x20,0x22,0x16,0x24,0x1c,0x1c },
        { 0x20,0x20,0x16,0x16,0x16,0x22,0x30,0x18,0x16,0x20,0x20,0x31,0x1c,0x1c,0x08,0x1a,0x30,0x00,0x00,0x33,0x16,0x08,0x10,0x14,0x32,0x22,0x24,0x20,0x26,0x1e,0x1e },
        { 0x16,0x16,0x18,0x18,0x18,0x1a,0x31,0x1a,0x18,0x16,0x16,0x32,0x1e,0x1e,0x0a,0x30,0x31,0x00,0x00,0x00,0x18,0x0a,0x1a,0x1a,0x33,0x24,0x26,0x22,0x1a,0x20,0x20 },
        { 0x18,0x18,0x1a,0x1a,0x1a,0x30,0x32,0x30,0x30,0x18,0x18,0x33,0x20,0x20,0x0c,0x31,0x32,0x00,0x00,0x00,0x0e,0x0c,0x30,0x30,0x00,0x26,0x1a,0x24,0x30,0x1a,0x1a },
        { 0x24,0x22,0x30,0x30,0x30,0x31,0x33,0x31,0x31,0x24,0x30,0x00,0x22,0x22,0x1a,0x32,0x33,0x00,0x00,0x00,0x1c,0x1a,0x31,0x31,0x00,0x1a,0x30,0x26,0x31,0x30,0x30 },
        { 0x1a,0x1a,0x31,0x31,0x31,0x32,0x00,0x32,0x32,0x30,0x31,0x00,0x24,0x24,0x30,0x33,0x00,0x00,0x00,0x00,0x10,0x30,0x32,0x32,0x00,0x30,0x31,0x1a,0x32,0x31,0x31 },
        { 0x30,0x30,0x32,0x32,0x32,0x33,0x00,0x33,0x33,0x31,0x32,0x00,0x1a,0x1a,0x31,0x00,0x00,0x00,0x00,0x00,0x1a,0x31,0x33,0x33,0x00,0x31,0x32,0x30,0x33,0x32,0x32 },
        { 0x31,0x31,0x33,0x33,0x33,0x00,0x00,0x00,0x00,0x32,0x33,0x00,0x30,0x30,0x32,0x00,0x00,0x00,0x00,0x00,0x30,0x32,0x00,0x00,0x00,0x32,0x33,0x31,0x00,0x33,0x33 },
        { 0x32,0x32,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x33,0x00,0x00,0x31,0x31,0x33,0x00,0x00,0x00,0x00,0x00,0x31,0x33,0x00,0x00,0x00,0x33,0x00,0x32,0x00,0x00,0x00 },
        { 0x33,0x33,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x32,0x32,0x00,0x00,0x00,0x00,0x00,0x00,0x32,0x00,0x00,0x00,0x00,0x00,0x00,0x33,0x00,0x00,0x00 },
        { 0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x33,0x33,0x00,0x00,0x00,0x00,0x00,0x00,0x33,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00 },
        };

    // parameter effect type for each parameter by versions in insertion.   insParaTypeTable[version][param] 
    public static final int insParaTypeTable[][] = new int[][] {                // [31][18]
        // 1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18
        {  7,21,25, 8, 5,27,10,27,33, 6,27,29,38,22,21,21,21, 0 }, // Chorus
        {  7,21,25, 8, 5,27,10,27,33, 6,27,30,38,22,21,21,21, 0 }, // Flanger
        {  7,21, 8, 5,27,10,27,33, 6,27,38,22,21,21,21, 0, 0, 0 }, // Symphonic
        {  7,21,21,25,48,29, 5,27, 6,27,38,22,21,21,21, 0, 0, 0 }, // Phaser1
        {  7,21,21,25,49,30, 5,27, 6,27,38,22,21,21,21, 0, 0, 0 }, // Phaser2
        { 50,16,39,39,25,22,21,22,21,38,22,21,21,21, 0, 0, 0, 0 }, // Pitch Change
        { 39, 8, 8, 5,27, 6,27,38,22,21,21,21, 0, 0, 0, 0, 0, 0 }, // Ens Detune
        {  7,21, 5,27,10,27,33, 6,27,38,22,21,21,21, 0, 0, 0, 0 }, // Rotary SP
        {  7,21,21,51,52,10, 5,27, 6,27,22,21,21,21, 0, 0, 0, 0 }, // 2WayRotary
        {  7,21,21,30, 5,27,10,27,33, 6,27,29,22,21,21,21, 0, 0 }, // Tremolo
        {  7,21,21, 9, 5,27,10,27,33, 6,27,22,21,21,21, 0, 0, 0 }, // Auto Pan
        {  8,56, 5,27, 6,27,38,22,21,21,21, 0, 0, 0, 0, 0, 0, 0 }, // Ambience
        {  7,21,21,33, 5,27, 6,27,21,27,27, 3,21,38,22,21,21,21 }, // A-Wah +Dist
        { 21,21,33,12, 5,27, 6,27,21,27,27, 3,21,38,22,21,21,21 }, // T-Wah +Dist
        { 21,21,33,12,21,21,27,27,26,25,21,38,22,21,21,21, 0, 0 }, // Wah-DS-Dly
        { 17,58,40,18,33,19,41,42,38,22,21,21,21, 0, 0, 0, 0, 0 }, // Lo-Fi
        { 34,27,10,27,33, 6,27,29,22,21,21,21, 0, 0, 0, 0, 0, 0 }, // 3-Band EQ
        {  6,21,21,22,21,21,21, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, // HM Enhncer
        { 11,12,36,21,22,21,21,21, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, // Noise Gate
        { 11,12,37,13,21,22,21,21,21, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, // Compressor
        { 11,12,37,13,21, 5,27,10,27,33, 3,21,21,38,22,21,21,21 }, // Comp + Dist
        { 11,12,37,13,21,21,27,27,26,25,21,38,22,21,21,21, 0, 0 }, // Cmp+DS+Dly
        { 21, 5,27,10,27,33, 3,21,21,38,22,21,21,21, 0, 0, 0, 0 }, // Distortion
        { 21,27,27,26,26,26,25,21,21,38,22,21,21,21, 0, 0, 0, 0 }, // Dist+Delay
        { 21,14, 3,21,21,38,22,21,21,21, 0, 0, 0, 0, 0, 0, 0, 0 }, // Amp Sim
        { 26,26,26,26,25,21,24, 5,27, 6,27,38,22,21,21,21, 0, 0 }, // Delay LCR
        { 26,26,26,26,25,24, 5,27, 6,27,38,22,21,21,21, 0, 0, 0 }, // Delay L,R
        { 28,25,28,25,24,28,28,21, 5,27, 6,27,38,22,21,21,21, 0 }, // Echo
        { 28,28,25,57,24, 5,27, 6,27,38,22,21,21,21, 0, 0, 0, 0 }, // Cross Delay
        { 20,15,43, 1,25, 2, 3,43,44,24,38,22,21,21,21, 0, 0, 0 }, // ER 1
        { 45,15,43, 1,25, 2, 3,43,44,24,38,22,21,21,21, 0, 0, 0 }, // Gate Reverb
        };


    // initial values for each parameter effect type for each parameter by effect (not version!).   insParaInitValueTable[effect][param] 
    // Note that I have modified some values, which seem to be off.
    public static final int insParaInitValueTable[][] = new int[][] {                   // [40][18]
        //   1    2    3    4    5    6    7    8    9  10  11  12  13  14  15  16  17  18
        {   63,   0,   0, 127,   0,   0,   0,   0,   0,  0,  0,  0,  0,  0,  0,  0,  0,  0 }, // Thru
        {    6,  62,  78,  80,  18,  12,  25,  12,   0, 23, 12,  0, 63, 63,  0,  0,127,  0 }, // Chorus
        {   18,  28,  63,  10,  11,  12,  26,  10,   0, 23, 12,  0,126, 63,  0,  0,127,  0 }, // Celeste
        {   11,  30, 103,   2,  16,  12,  25,  12,   0, 22, 12,  0, 95, 63,  0,  0,127,  0 }, // Flanger
        {   10,  40,   0,  18,  12,  32,   9,   0,  22, 12, 95, 63,  0,  0,127,  0,  0,  0 }, // Symphonic
        {   14,  92,  76,  99,   2,   1,  18,  12,  12, 12, 97, 63,  0,  0,127,  0,  0,  0 }, // Phaser1
        {    2, 127,  25, 114,   2,   0,  16,  12,  21, 12, 95, 63,  0,  0,127,  0,  0,  0 }, // Phaser2
        {   24,   1,  65,  34,  63,   0, 125, 126, 127, 60, 63,  0,  0,127,  0,  0,  0,  0 }, // Pitch Change
        {   20,  10,  30,  18,  12,  20,  12,  63,  63,  0,  0,127,  0,  0,  0,  0,  0,  0 }, // Ens Detune
        {   86,  71,  18,   8,  29,   0,  14,  23,   2,126, 63,  0,  0,127,  0,  0,  0,  0 }, // Rotary SP
        {   87,  89,  62,  16,  31,  11,  11,  12,  16, 12, 63,  0,  0,127,  0,  0,  0,  0 }, // 2WayRotary
        {   40, 112,   0,  60,  10,  12,  25,  12,   0, 23, 12,  1, 63,  0,  0,127,  0,  0 }, // Tremolo
        {   41,  80,  32,   5,  13,  12,  25,  12,   0, 22, 12, 63,  0,  0,127,  0,  0,  0 }, // Auto Pan
        {  112,   1,  18,  12,  20,   2,  73,  63,   0,  0,127,  0,  0,  0,  0,  0,  0,  0 }, // Ambience
        {   32,  84,  46,  24,  18,  14,  18,  12,  60, 20, 16, 18, 64,126, 63,  0,  0,127 }, // A-Wah +Dist
        {   25,  64,  32,  13,  18,  14,  18,  12,  16, 16, 20, 11, 68,126, 63,  0,  0,127 }, // A-Wah +Odrv
        {   80,  18,  35,  12,  18,  14,  18,  12,  30, 20, 22, 19, 72,126, 63,  0,  0,127 }, // T-Wah +Dist
        {   61,  30,  31,  12,  18,  14,  18,  12,  15, 16, 20, 15, 72,126, 63,  0,  0,127 }, // T-Wah +Odrv
        {  102,  20,  13,   7,  60,  53,  16,  20,1899, 83, 30,126, 63,  0,  0,127,  0,  0 }, // Wah-DS-Dly
        {   80,  35,  20,  12,  16,  87,  12,  12,1599, 83, 50,126, 63,  0,  0,127,  0,  0 }, // Wah-OD-Dly
        {    2,   0,   3,  50,  19,   4,   1,   0, 126, 63,  0,  0,127,  0,  0,  0,  0,  0 }, // Lo-Fi
        {   11,  12,  20,  12,  40,  24,  12,   0,  63,  0,  0,127,  0,  0,  0,  0,  0,  0 }, // 3-Band EQ
        {   21,  20,  30,  63,   0,   0, 127,   0,   0,  0,  0,  0,  0,  0,  0,  0,  0,  0 }, // HM Enhncer
        {    0,  11,  27,  50,  63,   0,   0, 127,   0,  0,  0,  0,  0,  0,  0,  0,  0,  0 }, // Noise Gate
        {   10,   2,  20,   4,  80,  63,   0,   0, 127,  0,  0,  0,  0,  0,  0,  0,  0,  0 }, // Compressor
        {    6,   2,  21,   5,  60,  18,  17,  32,  20,  0, 19,120, 70,126, 63,  0,  0,127 }, // Comp + Dist
        {    6,  11,  16,   5,  60,  51,  16,  20,1899, 71, 38,126, 63,  0,  0,127,  0,  0 }, // Cmp+DS+Dly
        {    6,   2,  16,   4,  18,  65,  16,  17,1899, 73, 50,126, 63,  0,  0,127,  0,  0 }, // Cmp+OD+Dly
        {   60,  15,  20,  21,  22,   0,  19,  80,  65,126, 63,  0,  0,127,  0,  0,  0,  0 }, // Distortion
        {   60,  20,  22, 499, 999,1999,  83,  80,  42,126, 63,  0,  0,127,  0,  0,  0,  0 }, // Dist+Delay
        {   29,  20,  16,  22,  20,   0,  12, 104,  80,126, 63,  0,  0,127,  0,  0,  0,  0 }, // Overdrive
        {   25,  16,  20, 499, 999,1999,  83,  64,  55,126, 63,  0,  0,127,  0,  0,  0,  0 }, // Odrv+Delay
        {   76,   3,   8, 102,  80, 126,  63,   0,   0,127,  0,  0,  0,  0,  0,  0,  0,  0 }, // Amp Sim
        { 3333,1667,5000,5000,  73, 100,   2,  22,  12, 18, 12, 31, 63,  0,  0,127,  0,  0 }, // Delay LCR
        { 2500,3750,3750,3750,  86,   2,  22,  12,  18, 12, 31, 63,  0,  0,127,  0,  0,  0 }, // Delay L,R
        { 2200,  85,2100,  84,   4,2299,2349,  62,  19,  6, 22, 11, 31, 63,  0,  0,127,  0 }, // Echo
        { 3650,3650,  87,   1,   4,  21,  12,  22,  10, 31, 63,  0,  0,127,  0,  0,  0,  0 }, // Cross Delay
//        { 3332,1666,4999,4999,  73, 100,   2,  22,  12, 18, 12, 31, 63,  0,  0,127,  0,  0 }, // Delay LCR
//        { 2499,3749,3751,3749,  86,   2,  22,  12,  18, 12, 31, 63,  0,  0,127,  0,  0,  0 }, // Delay L,R
//        { 2199,  85,2099,  84,   4,2299,2349,  62,  19,  6, 22, 11, 31, 63,  0,  0,127,  0 }, // Echo
//        { 3649,3649,  87,   1,   4,  21,  12,  22,  10, 31, 63,  0,  0,127,  0,  0,  0,  0 }, // Cross Delay
        {    0,  25,   5,   6,  73,   0,  12,   7,   1,  9, 28, 63,  0,  0,127,  0,  0,  0 }, // ER 1
        {    2,  12,   8,   4,  66,   0,  22,   5,   3,  9, 28, 63,  0,  0,127,  0,  0,  0 }, // ER 2
        {    0,   6,  10,   0,  63,   0,  18,   5,   3,  4, 51, 63,  0,  0,127,  0,  0,  0 }, // Gate Reverb
        {    1,  15,   8,   3,  63,   0,  13,   6,   3,  9,126, 63,  0,  0,127,  0,  0,  0 }, // Reverse Gate
        };




    // Parameter settings for a subset of parameter types (the first 21).  The remainder are handed on a custom and case-by-case basis in buildFXVals().  effectParaValueTable[value][parameter type]
    // Below was K_Take's documentaton.

    // Parameter ID
    //
    // 00:Reverb Time 10:BandEQ MidFreq 20:EarlyType         30:-180Å`+180(3ÇÃî{êî)                    40:-6Å`+36(Lofi OutGain)     50:-24Å`+24(PitchChange Pitch)
    // 01:InitDelay   11:Attack         21:0Å`127            31:4Å`10(Variation Phaser1 stage)         41:0Å`6(Lofi BitAssign)      51:2WayRotary LowHigh  L63>HÅ`L=HÅ`L<H63
    // 02:HPF Cutoff  12:release        22:L63Å`R63          32:3Å`5 (Variation Phaser2 stage)         42:offÅAon                   52:2WayRotary MicAngle 0Å`180Åi3ÇÃî{êîÅj
    // 03:LPF Cutoff  13:ratio          23:E63>RÅ`E=RÅ`E<R63 33:1.0Å`12.0(0.1çèÇ›)                     43:0Å`10                     53:Reverb Delay 0.1Å`99.3(InitDelayÇ∆ìØÇ∂)
    // 04:Depth       14:amp type       24:0.1Å`1.0          34:3BandEQ LowFreqÇÕEQLowÇÃ50à»â∫Ç∆ìØÇ∂   44:0Å`3                      54:Width (DepthÇ∆ìríÜÇ‹Ç≈ìØÇ∂)
    // 05:EQ Low      15:roomsize(ER1)  25:-63Å`+63          35:3BandEQ HighFreqÇÕEQLowÇÃ500à»â∫Ç∆ìØÇ∂ 45:"TypeA" "TypeB"           55:Height(DepthÇ∆ìríÜÇ‹Ç≈ìØÇ∂)
    // 06:EQ HiFreq   16:initdly(pitch) 26:0.1Å`1365.0       36:threshold -72Å`-30ÇÃòAî‘               46:0Å`4 (hall Density)       56:normal,inverse
    // 07:LFO Freq    17:Smpl Freq      27:-12Å`+12          37:compÇÃthreshold -48Å`-6                47:0Å`30(Whiteroom WallVary) 57:L,R,L&R
    // 08:DelayOffset 18:Lofi LPF Cut   28:0.1Å`682.0        38:D63>WÅ`D=WÅ`D<W63                      48:4Å`12(Ins Phaser1 stage)  58:Lofi WordLength(1Å`127)
    // 09:PAN Dir     19:Lofi Filter    29:mono stereo       39:-50Å`+50                               49:3Å`6 (Ins Phaser2 stage)
    //
    //      0      1       2      3       4      5      6       7       8      9       10     11   12    13      14     15     16      17      18      19      20
    public static final String effectParaValueTable[][] = new String[][] {              // [128][21]
        { "0.3" ,"0.1"  ,"thru","1.0k" ,"0.5" ,"32"  ,"500"  ,"0.000","0.0" ,"L<->R","100"  ,"1" ,"10" ,"1.0" ,"off"  ,"0.1" ,"0.1"  ,"48.0k","63"   ,"thru" ,"S-Hall"  }, //0
        { "0.4" ,"1.7"  ,"22"  ,"1.1k" ,"0.8" ,"36"  ,"560"  ,"0.046","0.1" ,"L->R" ,"110"  ,"2" ,"15" ,"1.5" ,"stack","0.3" ,"3.2"  ,"24.0k","70"   ,"pbass","L-Hall"  }, //1
        { "0.5" ,"3.2"  ,"25"  ,"1.2k" ,"1.0" ,"40"  ,"630"  ,"0.091","0.2" ,"L<-R" ,"125"  ,"3" ,"25" ,"2.0" ,"combo","0.4" ,"6.4"  ,"16.0k","80"   ,"radio","random"  }, //2
        { "0.6" ,"4.8"  ,"28"  ,"1.4k" ,"1.3" ,"45"  ,"700"  ,"0.137","0.3" ,"Lturn","140"  ,"4" ,"35" ,"3.0" ,"tube" ,"0.6" ,"9.5"  ,"12.0k","90"   ,"tel"  ,"reverse" }, //3
        { "0.7" ,"6.4"  ,"32"  ,"1.6k" ,"1.5" ,"50"  ,"800"  ,"0.183","0.4" ,"Rturn","160"  ,"5" ,"45" ,"5.0" ,""     ,"0.7" ,"12.7" ,"9.60k","100"  ,"clean","plate"   }, //4
        { "0.8" ,"8.0"  ,"36"  ,"1.8k" ,"1.8" ,"56"  ,"900"  ,"0.229","0.5" ,"L/R"  ,"180"  ,"6" ,"55" ,"7.0" ,""     ,"0.9" ,"15.8" ,"8.00k","110"  ,"low"  ,"spring"  }, //5
        { "0.9" ,"9.5"  ,"40"  ,"2.0k" ,"2.0" ,"63"  ,"1.0k" ,"0.275","0.6" ,""     ,"200"  ,"7" ,"65" ,"10.0",""     ,"1.0" ,"19.0" ,"6.86k","125"  ,""     ,""        }, //6
        { "1.0" ,"11.1" ,"45"  ,"2.2k" ,"2.3" ,"70"  ,"1.1k" ,"0.320","0.7" ,""     ,"225"  ,"8" ,"75" ,"20.0",""     ,"1.2" ,"22.1" ,"6.00k","140"  ,""     ,""        }, //7
        { "1.1" ,"12.7" ,"50"  ,"2.5k" ,"2.6" ,"80"  ,"1.2k" ,"0.366","0.8" ,""     ,"250"  ,"9" ,"85" ,""    ,""     ,"1.4" ,"25.3" ,"5.33k","160"  ,""     ,""        }, //8
        { "1.2" ,"14.3" ,"56"  ,"2.8k" ,"2.8" ,"90"  ,"1.4k" ,"0.412","0.9" ,""     ,"280"  ,"10","100",""    ,""     ,"1.5" ,"28.4" ,"4.80k","180"  ,""     ,""        }, //9
        { "1.3" ,"15.8" ,"63"  ,"3.2k" ,"3.1" ,"100" ,"1.6k" ,"0.458","1.0" ,""     ,"315"  ,"12","115",""    ,""     ,"1.7" ,"31.6" ,"4.36k","200"  ,""     ,""        }, //10
        { "1.4" ,"17.4" ,"70"  ,"3.6k" ,"3.3" ,"110" ,"1.8k" ,"0.504","1.1" ,""     ,"355"  ,"14","140",""    ,""     ,"1.8" ,"34.7" ,"4.00k","225"  ,""     ,""        }, //11
        { "1.5" ,"19.0" ,"80"  ,"4.0k" ,"3.6" ,"125" ,"2.0k" ,"0.549","1.2" ,""     ,"400"  ,"16","170",""    ,""     ,"2.0" ,"37.9" ,"3.69k","250"  ,""     ,""        }, //12
        { "1.6" ,"20.6" ,"90"  ,"4.5k" ,"3.9" ,"140" ,"2.2k" ,"0.595","1.3" ,""     ,"450"  ,"18","230",""    ,""     ,"2.1" ,"41.0" ,"3.43k","280"  ,""     ,""        }, //13
        { "1.7" ,"22.1" ,"100" ,"5.0k" ,"4.1" ,"160" ,"2.5k" ,"0.641","1.4" ,""     ,"500"  ,"20","340",""    ,""     ,"2.3" ,"44.2" ,"3.20k","315"  ,""     ,""        }, //14
        { "1.8" ,"23.7" ,"110" ,"5.6k" ,"4.4" ,"180" ,"2.8k" ,"0.687","1.5" ,""     ,"560"  ,"23","680",""    ,""     ,"2.5" ,"47.3" ,"3.00k","355"  ,""     ,""        }, //15
        { "1.9" ,"25.3" ,"125" ,"6.3k" ,"4.6" ,"200" ,"3.2k" ,"0.732","1.6" ,""     ,"630"  ,"26",""   ,""    ,""     ,"2.6" ,"50.5" ,"2.82k","400"  ,""     ,""        }, //16
        { "2.0" ,"26.9" ,"140" ,"7.0k" ,"4.9" ,"225" ,"3.6k" ,"0.778","1.7" ,""     ,"700"  ,"30",""   ,""    ,""     ,"2.8" ,"53.6" ,"2.67k","450"  ,""     ,""        }, //17
        { "2.1" ,"28.4" ,"160" ,"8.0k" ,"5.2" ,"250" ,"4.0k" ,"0.824","1.8" ,""     ,"800"  ,"35",""   ,""    ,""     ,"2.9" ,"56.8" ,"2.53k","500"  ,""     ,""        }, //18
        { "2.2" ,"30.0" ,"180" ,"9.0k" ,"5.4" ,"280" ,"4.5k" ,"0.870","1.9" ,""     ,"900"  ,"40",""   ,""    ,""     ,"3.1" ,"59.9" ,"2.40k","560"  ,""     ,""        }, //19
        { "2.3" ,"31.6" ,"200" ,"10.0k","5.7" ,"315" ,"5.0k" ,"0.916","2.0" ,""     ,"1.0k" ,""  ,""   ,""    ,""     ,"3.2" ,"63.1" ,"2.29k","630"  ,""     ,""        }, //20
        { "2.4" ,"33.2" ,"225" ,"11.0k","5.9" ,"355" ,"5.6k" ,"0.961","2.1" ,""     ,"1.1k" ,""  ,""   ,""    ,""     ,"3.4" ,"66.2" ,"2.18k","700"  ,""     ,""        }, //21
        { "2.5" ,"34.7" ,"250" ,"12.0k","6.2" ,"400" ,"6.3k" ,"1.007","2.2" ,""     ,"1.2k" ,""  ,""   ,""    ,""     ,"3.5" ,"69.4" ,"2.09k","800"  ,""     ,""        }, //22
        { "2.6" ,"36.3" ,"280" ,"14.0k","6.5" ,"450" ,"7.0k" ,"1.053","2.3" ,""     ,"1.4k" ,""  ,""   ,""    ,""     ,"3.7" ,"72.5" ,"2.00k","900"  ,""     ,""        }, //23
        { "2.7" ,"37.9" ,"315" ,"16.0k","6.7" ,"500" ,"8.0k" ,"1.099","2.4" ,""     ,"1.6k" ,""  ,""   ,""    ,""     ,"3.9" ,"75.7" ,"1.92k","1.0k" ,""     ,""        }, //24
        { "2.8" ,"39.5" ,"355" ,"18.0k","7.0" ,"560" ,"9.0k" ,"1.144","2.5" ,""     ,"1.8k" ,""  ,""   ,""    ,""     ,"4.0" ,"78.8" ,"1.85k","1.1k" ,""     ,""        }, //25
        { "2.9" ,"41.0" ,"400" ,"thru" ,"7.2" ,"630" ,"10.0k","1.190","2.6" ,""     ,"2.0k" ,""  ,""   ,""    ,""     ,"4.2" ,"82.0" ,"1.78k","1.2k" ,""     ,""        }, //26
        { "3.0" ,"42.6" ,"450" ,""     ,"7.5" ,"700" ,"11.0k","1.236","2.7" ,""     ,"2.2k" ,""  ,""   ,""    ,""     ,"4.3" ,"85.1" ,"1.71k","1.4k" ,""     ,""        }, //27
        { "3.1" ,"44.2" ,"500" ,""     ,"7.8" ,"800" ,"12.0k","1.282","2.8" ,""     ,"2.5k" ,""  ,""   ,""    ,""     ,"4.5" ,"88.3" ,"1.66k","1.6k" ,""     ,""        }, //28
        { "3.2" ,"45.7" ,"560" ,""     ,"8.0" ,"900" ,"14.0k","1.328","2.9" ,""     ,"2.8k" ,""  ,""   ,""    ,""     ,"4.6" ,"91.4" ,"1.60k","1.8k" ,""     ,""        }, //29
        { "3.3" ,"47.3" ,"630" ,""     ,"8.3" ,"1.0k","16.0k","1.373","3.0" ,""     ,"3.2k" ,""  ,""   ,""    ,""     ,"4.8" ,"94.6" ,"1.55k","2.0k" ,""     ,""        }, //30
        { "3.4" ,"48.9" ,"700" ,""     ,"8.6" ,"1.1k",""     ,"1.419","3.1" ,""     ,"3.6k" ,""  ,""   ,""    ,""     ,"5.0" ,"97.7" ,"1.50k","2.2k" ,""     ,""        }, //31
        { "3.5" ,"50.5" ,"800" ,""     ,"8.8" ,"1.2k",""     ,"1.465","3.2" ,""     ,"4.0k" ,""  ,""   ,""    ,""     ,"5.1" ,"100.9","1.45k","2.5k" ,""     ,""        }, //32
        { "3.6" ,"52.0" ,"900" ,""     ,"9.1" ,"1.4k",""     ,"1.511","3.3" ,""     ,"4.5k" ,""  ,""   ,""    ,""     ,"5.3" ,"104.0","1.41k","2.8k" ,""     ,""        }, //33
        { "3.7" ,"53.6" ,"1.0k",""     ,"9.4" ,"1.6k",""     ,"1.556","3.4" ,""     ,"5.0k" ,""  ,""   ,""    ,""     ,"5.4" ,"107.2","1.37k","3.2k" ,""     ,""        }, //34
        { "3.8" ,"55.2" ,"1.1k",""     ,"9.6" ,"1.8k",""     ,"1.602","3.5" ,""     ,"5.6k" ,""  ,""   ,""    ,""     ,"5.6" ,"110.3","1.33k","3.6k" ,""     ,""        }, //35
        { "3.9" ,"56.8" ,"1.2k",""     ,"9.9" ,"2.0k",""     ,"1.648","3.6" ,""     ,"6.3k" ,""  ,""   ,""    ,""     ,"5.7" ,"113.5","1.30k","4.0k" ,""     ,""        }, //36
        { "4.0" ,"58.3" ,"1.4k",""     ,"10.2",""    ,""     ,"1.694","3.7" ,""     ,"7.0k" ,""  ,""   ,""    ,""     ,"5.9" ,"116.6","1.26k","4.5k" ,""     ,""        }, //37
        { "4.1" ,"59.9" ,"1.6k",""     ,"10.4",""    ,""     ,"1.598","3.8" ,""     ,"8.0k" ,""  ,""   ,""    ,""     ,"6.1" ,"119.8","1.23k","5.0k" ,""     ,""        }, //38
        { "4.2" ,"61.5" ,"1.8k",""     ,"10.7",""    ,""     ,"1.785","3.9" ,""     ,"9.0k" ,""  ,""   ,""    ,""     ,"6.2" ,"122.9","1.20k","5.6k" ,""     ,""        }, //39
        { "4.3" ,"63.1" ,"2.0k",""     ,"11.0",""    ,""     ,"1.831","4.0" ,""     ,"10.0k",""  ,""   ,""    ,""     ,"6.4" ,"126.1","1.17k","6.3k" ,""     ,""        }, //40
        { "4.4" ,"64.6" ,"2.2k",""     ,"11.2",""    ,""     ,"1.877","4.1" ,""     ,""     ,""  ,""   ,""    ,""     ,"6.5" ,"129.2","1.14k","7.0k" ,""     ,""        }, //41
        { "4.5" ,"66.2" ,"2.5k",""     ,"11.5",""    ,""     ,"1.923","4.2" ,""     ,""     ,""  ,""   ,""    ,""     ,"6.7" ,"132.4","1.12k","8.0k" ,""     ,""        }, //42
        { "4.6" ,"67.8" ,"2.8k",""     ,"11.8",""    ,""     ,"1.968","4.3" ,""     ,""     ,""  ,""   ,""    ,""     ,"6.8" ,"135.5","1.09k","9.0k" ,""     ,""        }, //43
        { "4.7" ,"69.4" ,"3.2k",""     ,"12.1",""    ,""     ,"2.014","4.4" ,""     ,""     ,""  ,""   ,""    ,""     ,"7.0" ,"138.6","1.07k","10.0k",""     ,""        }, //44
        { "4.8" ,"70.9" ,"3.6k",""     ,"12.3",""    ,""     ,"2.060","4.5" ,""     ,""     ,""  ,""   ,""    ,""     ,"7.2" ,"141.8","1.04k","11.0k",""     ,""        }, //45
        { "4.9" ,"72.5" ,"4.0k",""     ,"12.6",""    ,""     ,"2.106","4.6" ,""     ,""     ,""  ,""   ,""    ,""     ,"7.3" ,"144.9","1.02k","12.0k",""     ,""        }, //46
        { "5.0" ,"74.1" ,"4.5k",""     ,"12.9",""    ,""     ,"2.151","4.7" ,""     ,""     ,""  ,""   ,""    ,""     ,"7.5" ,"148.1","1.00k","14.0k",""     ,""        }, //47
        { "5.5" ,"75.7" ,"5.0k",""     ,"13.1",""    ,""     ,"2.197","4.8" ,""     ,""     ,""  ,""   ,""    ,""     ,"7.6" ,"151.2","980"  ,"16.0k",""     ,""        }, //48
        { "6.0" ,"77.2" ,"5.6k",""     ,"13.4",""    ,""     ,"2.243","4.9" ,""     ,""     ,""  ,""   ,""    ,""     ,"7.8" ,"154.4","960"  ,"18.0k",""     ,""        }, //49
        { "6.5" ,"78.8" ,"6.3k",""     ,"13.7",""    ,""     ,"2.289","5.0" ,""     ,""     ,""  ,""   ,""    ,""     ,"7.9" ,"157.5","941"  ,"thru" ,""     ,""        }, //50
        { "7.0" ,"80.4" ,"7.0k",""     ,"14.0",""    ,""     ,"2.335","5.1" ,""     ,""     ,""  ,""   ,""    ,""     ,"8.1" ,"160.7","923"  ,""     ,""     ,""        }, //51
        { "7.5" ,"81.9" ,"8.0k",""     ,"14.2",""    ,""     ,"2.380","5.2" ,""     ,""     ,""  ,""   ,""    ,""     ,"8.2" ,"163.8","906"  ,""     ,""     ,""        }, //52
        { "8.0" ,"83.5" ,""    ,""     ,"14.5",""    ,""     ,"2.426","5.3" ,""     ,""     ,""  ,""   ,""    ,""     ,"8.4" ,"167.0","889"  ,""     ,""     ,""        }, //53
        { "8.5" ,"85.1" ,""    ,""     ,"14.8",""    ,""     ,"2.472","5.4" ,""     ,""     ,""  ,""   ,""    ,""     ,"8.6" ,"170.1","873"  ,""     ,""     ,""        }, //54
        { "9.0" ,"86.7" ,""    ,""     ,"15.1",""    ,""     ,"2.518","5.5" ,""     ,""     ,""  ,""   ,""    ,""     ,"8.7" ,"173.3","857"  ,""     ,""     ,""        }, //55
        { "9.5" ,"88.2" ,""    ,""     ,"15.4",""    ,""     ,"2.563","5.6" ,""     ,""     ,""  ,""   ,""    ,""     ,"8.9" ,"176.4","842"  ,""     ,""     ,""        }, //56
        { "10.0","89.8" ,""    ,""     ,"15.6",""    ,""     ,"2.609","5.7" ,""     ,""     ,""  ,""   ,""    ,""     ,"9.0" ,"179.6","828"  ,""     ,""     ,""        }, //57
        { "11.0","91.4" ,""    ,""     ,"15.9",""    ,""     ,"2.655","5.8" ,""     ,""     ,""  ,""   ,""    ,""     ,"9.2" ,"182.7","814"  ,""     ,""     ,""        }, //58
        { "12.0","93.0" ,""    ,""     ,"16.2",""    ,""     ,"2.701","5.9" ,""     ,""     ,""  ,""   ,""    ,""     ,"9.3" ,"185.9","800"  ,""     ,""     ,""        }, //59
        { "13.0","94.5" ,""    ,""     ,"16.5",""    ,""     ,"2.747","6.0" ,""     ,""     ,""  ,""   ,""    ,""     ,"9.5" ,"189.0","787"  ,""     ,""     ,""        }, //60
        { "14.0","96.1" ,""    ,""     ,"16.8",""    ,""     ,"2.792","6.1" ,""     ,""     ,""  ,""   ,""    ,""     ,"9.7" ,"192.2","774"  ,""     ,""     ,""        }, //61
        { "15.0","97.7" ,""    ,""     ,"17.1",""    ,""     ,"2.838","6.2" ,""     ,""     ,""  ,""   ,""    ,""     ,"9.8" ,"195.3","762"  ,""     ,""     ,""        }, //62
        { "16.0","99.3" ,""    ,""     ,"17.3",""    ,""     ,"2.884","6.3" ,""     ,""     ,""  ,""   ,""    ,""     ,"10.0","198.5","750"  ,""     ,""     ,""        }, //63
        { "17.0","100.8",""    ,""     ,"17.6",""    ,""     ,"2.930","6.4" ,""     ,""     ,""  ,""   ,""    ,""     ,"10.1","201.6","738"  ,""     ,""     ,""        }, //64
        { "18.0","102.4",""    ,""     ,"17.9",""    ,""     ,"3.021","6.5" ,""     ,""     ,""  ,""   ,""    ,""     ,"10.3","204.8","727"  ,""     ,""     ,""        }, //65
        { "19.0","104.0",""    ,""     ,"18.2",""    ,""     ,"3.113","6.6" ,""     ,""     ,""  ,""   ,""    ,""     ,"10.4","207.9","716"  ,""     ,""     ,""        }, //66
        { "20.0","105.6",""    ,""     ,"18.5",""    ,""     ,"3.204","6.7" ,""     ,""     ,""  ,""   ,""    ,""     ,"10.6","211.1","706"  ,""     ,""     ,""        }, //67
        { "25.0","107.1",""    ,""     ,"18.8",""    ,""     ,"3.296","6.8" ,""     ,""     ,""  ,""   ,""    ,""     ,"10.8","214.2","696"  ,""     ,""     ,""        }, //68
        { "30.0","108.7",""    ,""     ,"19.1",""    ,""     ,"3.387","6.9" ,""     ,""     ,""  ,""   ,""    ,""     ,"10.9","217.4","686"  ,""     ,""     ,""        }, //69
        { ""    ,"110.3",""    ,""     ,"19.4",""    ,""     ,"3.479","7.0" ,""     ,""     ,""  ,""   ,""    ,""     ,"11.1","220.5","676"  ,""     ,""     ,""        }, //70
        { ""    ,"111.9",""    ,""     ,"19.7",""    ,""     ,"3.571","7.1" ,""     ,""     ,""  ,""   ,""    ,""     ,"11.2","223.7","667"  ,""     ,""     ,""        }, //71
        { ""    ,"113.4",""    ,""     ,"20.0",""    ,""     ,"3.662","7.2" ,""     ,""     ,""  ,""   ,""    ,""     ,"11.4","226.8","658"  ,""     ,""     ,""        }, //72
        { ""    ,"115.0",""    ,""     ,"20.2",""    ,""     ,"3.754","7.3" ,""     ,""     ,""  ,""   ,""    ,""     ,"11.5","230.0","649"  ,""     ,""     ,""        }, //73
        { ""    ,"116.6",""    ,""     ,"20.5",""    ,""     ,"3.845","7.4" ,""     ,""     ,""  ,""   ,""    ,""     ,"11.7","233.1","640"  ,""     ,""     ,""        }, //74
        { ""    ,"118.2",""    ,""     ,"20.8",""    ,""     ,"3.937","7.5" ,""     ,""     ,""  ,""   ,""    ,""     ,"11.9","236.3","632"  ,""     ,""     ,""        }, //75
        { ""    ,"119.7",""    ,""     ,"21.1",""    ,""     ,"4.028","7.6" ,""     ,""     ,""  ,""   ,""    ,""     ,"12.0","239.4","623"  ,""     ,""     ,""        }, //76
        { ""    ,"121.3",""    ,""     ,"21.4",""    ,""     ,"4.211","7.7" ,""     ,""     ,""  ,""   ,""    ,""     ,"12.2","242.6","615"  ,""     ,""     ,""        }, //77
        { ""    ,"122.9",""    ,""     ,"21.7",""    ,""     ,"4.395","7.8" ,""     ,""     ,""  ,""   ,""    ,""     ,"12.3","245.7","608"  ,""     ,""     ,""        }, //78
        { ""    ,"124.4",""    ,""     ,"22.0",""    ,""     ,"4.578","7.9" ,""     ,""     ,""  ,""   ,""    ,""     ,"12.5","248.9","600"  ,""     ,""     ,""        }, //79
        { ""    ,"126.0",""    ,""     ,"22.4",""    ,""     ,"4.761","8.0" ,""     ,""     ,""  ,""   ,""    ,""     ,"12.6","252.0","593"  ,""     ,""     ,""        }, //80
        { ""    ,"127.6",""    ,""     ,"22.7",""    ,""     ,"4.944","8.1" ,""     ,""     ,""  ,""   ,""    ,""     ,"12.8","255.2","585"  ,""     ,""     ,""        }, //81
        { ""    ,"129.2",""    ,""     ,"23.0",""    ,""     ,"5.127","8.2" ,""     ,""     ,""  ,""   ,""    ,""     ,"12.9","258.3","578"  ,""     ,""     ,""        }, //82
        { ""    ,"130.7",""    ,""     ,"23.3",""    ,""     ,"5.310","8.3" ,""     ,""     ,""  ,""   ,""    ,""     ,"13.1","261.5","571"  ,""     ,""     ,""        }, //83
        { ""    ,"132.3",""    ,""     ,"23.6",""    ,""     ,"5.493","8.4" ,""     ,""     ,""  ,""   ,""    ,""     ,"13.3","264.6","565"  ,""     ,""     ,""        }, //84
        { ""    ,"133.9",""    ,""     ,"23.9",""    ,""     ,"5.676","8.5" ,""     ,""     ,""  ,""   ,""    ,""     ,"13.4","267.7","558"  ,""     ,""     ,""        }, //85
        { ""    ,"135.5",""    ,""     ,"24.2",""    ,""     ,"5.859","8.6" ,""     ,""     ,""  ,""   ,""    ,""     ,"13.6","270.9","552"  ,""     ,""     ,""        }, //86
        { ""    ,"137.0",""    ,""     ,"24.5",""    ,""     ,"6.042","8.7" ,""     ,""     ,""  ,""   ,""    ,""     ,"13.7","274.0","545"  ,""     ,""     ,""        }, //87
        { ""    ,"138.6",""    ,""     ,"24.9",""    ,""     ,"6.226","8.8" ,""     ,""     ,""  ,""   ,""    ,""     ,"13.9","277.2","539"  ,""     ,""     ,""        }, //88
        { ""    ,"140.2",""    ,""     ,"25.2",""    ,""     ,"6.592","8.9" ,""     ,""     ,""  ,""   ,""    ,""     ,"14.0","280.3","533"  ,""     ,""     ,""        }, //89
        { ""    ,"141.8",""    ,""     ,"25.5",""    ,""     ,"6.958","9.0" ,""     ,""     ,""  ,""   ,""    ,""     ,"14.2","283.5","527"  ,""     ,""     ,""        }, //90
        { ""    ,"143.3",""    ,""     ,"25.8",""    ,""     ,"7.324","9.1" ,""     ,""     ,""  ,""   ,""    ,""     ,"14.4","286.6","522"  ,""     ,""     ,""        }, //91
        { ""    ,"144.9",""    ,""     ,"26.1",""    ,""     ,"7.690","9.2" ,""     ,""     ,""  ,""   ,""    ,""     ,"14.5","289.8","516"  ,""     ,""     ,""        }, //92
        { ""    ,"146.5",""    ,""     ,"26.5",""    ,""     ,"8.057","9.3" ,""     ,""     ,""  ,""   ,""    ,""     ,"14.7","292.9","511"  ,""     ,""     ,""        }, //93
        { ""    ,"148.1",""    ,""     ,"26.8",""    ,""     ,"8.423","9.4" ,""     ,""     ,""  ,""   ,""    ,""     ,"14.8","296.1","505"  ,""     ,""     ,""        }, //94
        { ""    ,"149.6",""    ,""     ,"27.1",""    ,""     ,"8.789","9.5" ,""     ,""     ,""  ,""   ,""    ,""     ,"15.0","299.2","500"  ,""     ,""     ,""        }, //95
        { ""    ,"151.2",""    ,""     ,"27.5",""    ,""     ,"9.155","9.6" ,""     ,""     ,""  ,""   ,""    ,""     ,"15.1","302.4","495"  ,""     ,""     ,""        }, //96
        { ""    ,"152.8",""    ,""     ,"27.8",""    ,""     ,"9.522","9.7" ,""     ,""     ,""  ,""   ,""    ,""     ,"15.3","305.5","490"  ,""     ,""     ,""        }, //97
        { ""    ,"154.4",""    ,""     ,"28.1",""    ,""     ,"9.888","9.8" ,""     ,""     ,""  ,""   ,""    ,""     ,"15.5","308.7","485"  ,""     ,""     ,""        }, //98
        { ""    ,"155.9",""    ,""     ,"28.5",""    ,""     ,"10.25","9.9" ,""     ,""     ,""  ,""   ,""    ,""     ,"15.6","311.8","480"  ,""     ,""     ,""        }, //99
        { ""    ,"157.5",""    ,""     ,"28.8",""    ,""     ,"10.62","10.0",""     ,""     ,""  ,""   ,""    ,""     ,"15.8","315.0","475"  ,""     ,""     ,""        }, //100
        { ""    ,"159.1",""    ,""     ,"29.2",""    ,""     ,"10.99","11.1",""     ,""     ,""  ,""   ,""    ,""     ,"15.9","318.1","471"  ,""     ,""     ,""        }, //101
        { ""    ,"160.6",""    ,""     ,"29.5",""    ,""     ,"11.72","12.2",""     ,""     ,""  ,""   ,""    ,""     ,"16.1","321.3","466"  ,""     ,""     ,""        }, //102
        { ""    ,"162.2",""    ,""     ,"29.9",""    ,""     ,"12.45","13.3",""     ,""     ,""  ,""   ,""    ,""     ,"16.2","324.4","462"  ,""     ,""     ,""        }, //103
        { ""    ,"163.8",""    ,""     ,"30.2",""    ,""     ,"13.18","14.4",""     ,""     ,""  ,""   ,""    ,""     ,"16.4","327.6","457"  ,""     ,""     ,""        }, //104
        { ""    ,"165.4",""    ,""     ,""    ,""    ,""     ,"13.92","15.5",""     ,""     ,""  ,""   ,""    ,""     ,"16.6","330.7","453"  ,""     ,""     ,""        }, //105
        { ""    ,"166.9",""    ,""     ,""    ,""    ,""     ,"14.65","17.1",""     ,""     ,""  ,""   ,""    ,""     ,"16.7","333.9","449"  ,""     ,""     ,""        }, //106
        { ""    ,"168.5",""    ,""     ,""    ,""    ,""     ,"15.38","18.6",""     ,""     ,""  ,""   ,""    ,""     ,"16.9","337.0","444"  ,""     ,""     ,""        }, //107
        { ""    ,"170.1",""    ,""     ,""    ,""    ,""     ,"16.11","20.2",""     ,""     ,""  ,""   ,""    ,""     ,"17.0","340.2","440"  ,""     ,""     ,""        }, //108
        { ""    ,"171.7",""    ,""     ,""    ,""    ,""     ,"16.85","21.8",""     ,""     ,""  ,""   ,""    ,""     ,"17.2","343.3","436"  ,""     ,""     ,""        }, //109
        { ""    ,"173.2",""    ,""     ,""    ,""    ,""     ,"17.58","23.3",""     ,""     ,""  ,""   ,""    ,""     ,"17.3","346.5","432"  ,""     ,""     ,""        }, //110
        { ""    ,"174.8",""    ,""     ,""    ,""    ,""     ,"18.31","24.9",""     ,""     ,""  ,""   ,""    ,""     ,"17.5","349.6","429"  ,""     ,""     ,""        }, //111
        { ""    ,"176.4",""    ,""     ,""    ,""    ,""     ,"19.04","26.5",""     ,""     ,""  ,""   ,""    ,""     ,"17.6","352.8","425"  ,""     ,""     ,""        }, //112
        { ""    ,"178.0",""    ,""     ,""    ,""    ,""     ,"19.78","28.0",""     ,""     ,""  ,""   ,""    ,""     ,"17.8","355.9","421"  ,""     ,""     ,""        }, //113
        { ""    ,"179.5",""    ,""     ,""    ,""    ,""     ,"21.24","29.6",""     ,""     ,""  ,""   ,""    ,""     ,"18.0","359.1","417"  ,""     ,""     ,""        }, //114
        { ""    ,"181.1",""    ,""     ,""    ,""    ,""     ,"22.71","31.2",""     ,""     ,""  ,""   ,""    ,""     ,"18.1","362.2","414"  ,""     ,""     ,""        }, //115
        { ""    ,"182.7",""    ,""     ,""    ,""    ,""     ,"24.17","32.8",""     ,""     ,""  ,""   ,""    ,""     ,"18.3","365.4","410"  ,""     ,""     ,""        }, //116
        { ""    ,"184.3",""    ,""     ,""    ,""    ,""     ,"25.63","34.3",""     ,""     ,""  ,""   ,""    ,""     ,"18.4","368.5","407"  ,""     ,""     ,""        }, //117
        { ""    ,"185.8",""    ,""     ,""    ,""    ,""     ,"27.10","35.9",""     ,""     ,""  ,""   ,""    ,""     ,"18.6","371.7","403"  ,""     ,""     ,""        }, //118
        { ""    ,"187.4",""    ,""     ,""    ,""    ,""     ,"28.56","37.5",""     ,""     ,""  ,""   ,""    ,""     ,"18.7","374.8","400"  ,""     ,""     ,""        }, //119
        { ""    ,"189.0",""    ,""     ,""    ,""    ,""     ,"30.03","39.0",""     ,""     ,""  ,""   ,""    ,""     ,"18.9","378.0","397"  ,""     ,""     ,""        }, //120
        { ""    ,"190.6",""    ,""     ,""    ,""    ,""     ,"31.49","40.6",""     ,""     ,""  ,""   ,""    ,""     ,"19.1","381.1","393"  ,""     ,""     ,""        }, //121
        { ""    ,"192.1",""    ,""     ,""    ,""    ,""     ,"32.96","42.2",""     ,""     ,""  ,""   ,""    ,""     ,"19.2","384.3","390"  ,""     ,""     ,""        }, //122
        { ""    ,"193.7",""    ,""     ,""    ,""    ,""     ,"34.42","43.7",""     ,""     ,""  ,""   ,""    ,""     ,"19.4","387.4","387"  ,""     ,""     ,""        }, //123
        { ""    ,"195.3",""    ,""     ,""    ,""    ,""     ,"35.89","45.3",""     ,""     ,""  ,""   ,""    ,""     ,"19.5","390.6","384"  ,""     ,""     ,""        }, //124
        { ""    ,"196.9",""    ,""     ,""    ,""    ,""     ,"37.35","46.9",""     ,""     ,""  ,""   ,""    ,""     ,"19.7","393.7","381"  ,""     ,""     ,""        }, //125
        { ""    ,"198.4",""    ,""     ,""    ,""    ,""     ,"40.28","48.4",""     ,""     ,""  ,""   ,""    ,""     ,"19.8","396.9","378"  ,""     ,""     ,""        }, //126
        { ""    ,"200.0",""    ,""     ,""    ,""    ,""     ,"43.21","50.0",""     ,""     ,""  ,""   ,""    ,""     ,"20.0","400.0","375"  ,""     ,""     ,""        }, //127
        };
    
    // First 14 Controller Destinations, which are all insertion parameters, and change depending on the insertion effect used.  ctrlDestNameTable[dest][version]
    public static final String[][] ctrlDestNameTable = new String[][] {                 // [14][32] 
        //     0             1              2              3              4              5              6              7              8              9              10             11             12            13             14             15             16             17             18             19             20            21             22             23             24             25             26             27             28             29             30             31
        { ""            ,"I:LFO Freq"  ,"I:LFO Freq"  ,"I:LFO Freq"  ,"I:LFO Freq"  ,"I:LFO Freq"  ,""            ,""            ,"I:LFO Freq"  ,"I:Rotor Spd" ,"I:LFO Freq"  ,"I:LFO Freq"  ,""            ,"I:LFO Freq" ,"I:Sens"      ,"I:Sens"      ,"I:Smpl Freq" ,"I:Low Freq"  ,"I:HPF Cutoff","I:Attack"    ,"I:Attack"    ,"I:Attack"   ,"I:Attack"    ,"I:Drive"     ,"I:Drive"     ,"I:Drive"     ,""            ,""            ,""            ,""            ,""            ,""             },
        { ""            ,"I:LFO Depth" ,"I:LFO Depth" ,"I:LFO Depth" ,"I:LFO Depth" ,"I:LFO Depth" ,""            ,""            ,"I:LFO Depth" ,"I:DriveLow"  ,"I:AM Depth"  ,"I:L/R Depth" ,"I:Phase"     ,"I:LFO Depth","I:CutofFreq" ,"I:CutofFreq" ,"I:Word Leng" ,"I:Low Gain"  ,"I:Drive"     ,"I:Release"   ,"I:Release"   ,"I:Release"  ,"I:Release"   ,"I:EQLowFreq" ,"I:DS LowGain",""            ,""            ,""            ,"I:Lch FBLvl" ,""            ,""            ,""             },
        { ""            ,"I:FB Level"  ,"I:FB Level"  ,""            ,""            ,""            ,""            ,""            ,"I:EQLowFreq" ,"I:DriveHigh" ,"I:PM Depth"  ,"I:F/L Depth" ,"I:EQLowFreq" ,"I:CutofFreq","I:Resonance" ,"I:Resonance" ,"I:Out Gain"  ,"I:Mid Freq"  ,"I:Mix Level" ,"I:Threshold" ,"I:Threshold" ,"I:Threshold","I:Threshold" ,"I:EQLowGain" ,"I:DS MidGain","I:LPFCutoff" ,""            ,""            ,""            ,"I:FB Level"  ,""            ,""             },
        { ""            ,""            ,""            ,"I:EQLowFreq" ,"I:FB Level"  ,"I:FB Level"  ,""            ,"I:EQLowFreq" ,"I:EQLowGain" ,"I:Low/High"  ,""            ,""            ,"I:EQLowGain" ,"I:Resonance","I:Release"   ,"I:Release"   ,"I:LPFCutoff" ,"I:Mid Gain"  ,""            ,"I:Out Level" ,"I:Ratio"     ,"I:Ratio"    ,"I:Ratio"     ,"I:EQMidFreq" ,""            ,"I:Edge"      ,""            ,""            ,"I:Rch FBLvl" ,""            ,""            ,""             },
        { ""            ,"I:EQLowFreq" ,"I:EQLowFreq" ,"I:EQLowGain" ,"I:Stage"     ,"I:Stage"     ,"I:FB Level"  ,"I:EQLowGain" ,"I:EQMidFreq" ,""            ,"I:EQLowFreq" ,"I:EQLowFreq" ,"I:EQ HiFreq" ,"I:EQLowFreq","I:EQLowFreq" ,"I:Drive"     ,"I:LPF Reso"  ,"I:Mid Q"     ,""            ,""            ,"I:Out Level" ,"I:Drive"    ,"I:Drive"     ,"I:EQMidGain" ,""            ,"I:Out Level" ,"I:FB Level"  ,"I:FB Level"  ,""            ,""            ,"I:FB Level"  ,"I:FB Level"   },
        { ""            ,"I:EQLowGain" ,"I:EQLowGain" ,"I:EQMidFreq" ,""            ,""            ,"I:Pan1"      ,"I:EQHiFreq"  ,"I:EQMidGain" ,""            ,"I:EQLowGain" ,"I:EQLowGain" ,"I:EQ HiGain" ,"I:EQLowGain","I:EQLowGain" ,"I:Out Level" ,""            ,"I:High Freq" ,""            ,""            ,""            ,"I:EQLowFreq","I:Out Level" ,"I:EQ Mid Q"  ,""            ,"I:Dry/Wet"   ,"I:Cch Level" ,""            ,""            ,"I:EQLowFreq" ,"I:HPF Cutoff","I:HPF Cutoff" },
        { ""            ,"I:EQMidFreq" ,"I:EQMidFreq" ,"I:EQMidGain" ,"I:EQLowFreq" ,"I:EQLowFreq" ,"I:Out Level1","I:EQHiGain"  ,"I:EQ MidQ"   ,"I:EQLowFreq" ,"I:EQMidFreq" ,"I:EQMidFreq" ,"I:Dry/Wet"   ,"I:EQ HiFreq","I:EQ HiFreq" ,"I:DSLowGain" ,""            ,"I:High Gain" ,""            ,""            ,""            ,"I:EQLowGain","I:DSLowGain" ,"I:LPFCutoff" ,"I:FB Level"  ,""            ,""            ,"I:EQLowFreq" ,""            ,"I:EQLowGain" ,"I:LPF Cutoff","I:LPF Cutoff" },
        { ""            ,"I:EQMidGain" ,"I:EQMidGain" ,"I:EQ MidQ"   ,"I:EQLowGain" ,"I:EQLowGain" ,"I:Pan2"      ,"I:Dry/Wet"   ,"I:EQ HiFreq" ,"I:EQLowGain" ,"I:EQMidGain" ,"I:EQMidGain" ,""            ,"I:EQ HiGain","I:EQ HiGain" ,"I:DSMidGain" ,""            ,""            ,""            ,""            ,""            ,"I:EQMidFreq","I:DSMidGain" ,"I:Edge"      ,"I:Delay Mix" ,""            ,"I:EQ LowFreq","I:EQLowGain" ,"I:Delay2Lvl" ,"I:EQ HiFreq" ,""            ,""             },
        { ""            ,"I:EQ MidQ"   ,"I:EQ MidQ"   ,"I:EQ HiFreq" ,"I:EQ HiFreq" ,"I:EQ HiFreq" ,"I:Out Level2",""            ,"I:EQ HiGain" ,"I:EQ HiFreq" ,"I:EQ MidQ"   ,"I:EQ MidQ"   ,""            ,"I:Drive"    ,"I:Drive"     ,""            ,"I:Dry/Wet"   ,""            ,""            ,""            ,""            ,"I:EQMidGain",""            ,"I:Out Level" ,"I:Out Level" ,""            ,"I:EQ LowGain","I:EQ HiFreq" ,"I:EQLowFreq" ,"I:EQ HiGain" ,""            ,""             },
        { ""            ,"I:EQ HiFreq" ,"I:EQ HiFreq" ,"I:EQ HiGain" ,"I:EQ HiGain" ,"I:EQ HiGain" ,"I:Dry/Wet"   ,""            ,"I:Dry/Wet"   ,"I:EQ HiGain" ,"I:EQ HiFreq" ,"I:EQ HiFreq" ,""            ,"I:DSLowGain","I:DS LowGain","I:FB Level"  ,""            ,""            ,""            ,""            ,""            ,"I:EQ Mid Q" ,"I:FB Level"  ,"I:Dry/Wet"   ,"I:Dry/Wet"   ,""            ,"I:EQ HiFreq" ,"I:EQ HiGain" ,"I:EQLowGain" ,"I:Dry/Wet"   ,""            ,""             },
        { ""            ,"I:EQ HiGain" ,"I:EQ HiGain" ,"I:Dry/Wet"   ,"I:Dry/Wet"   ,"I:Dry/Wet"   ,""            ,""            ,""            ,""            ,"I:EQ HiGain" ,"I:EQ HiGain" ,""            ,"I:DSMidGain","I:DS MidGain","I:Delay Mix" ,""            ,""            ,""            ,""            ,""            ,"I:LPFCutoff","I:Delay Mix" ,""            ,""            ,""            ,"I:EQ HiGain" ,"I:Dry/Wet"   ,"I:EQ HiFreq" ,""            ,"I:Dry/Wet"   ,"I:Dry/Wet"    },
        { ""            ,"I:Mode"      ,""            ,""            ,""            ,""            ,""            ,""            ,""            ,""            ,""            ,""            ,""            ,"I:LPFCutoff","I:LPF Cutoff","I:Dry/Wet"   ,""            ,""            ,""            ,""            ,""            ,"I:Edge"     ,"I:Dry/Wet"   ,""            ,""            ,""            ,"I:Dry/Wet"   ,""            ,"I:EQ HiGain" ,""            ,""            ,""             },
        { ""            ,"I:Dry/Wet"   ,"I:Dry/Wet"   ,""            ,""            ,""            ,""            ,""            ,""            ,""            ,""            ,""            ,""            ,"I:Out Level","I:Out Level" ,""            ,""            ,""            ,""            ,""            ,""            ,"I:Out Level",""            ,""            ,""            ,""            ,""            ,""            ,"I:Dry/Wet"   ,""            ,""            ,""             },
        { ""            ,""            ,""            ,""            ,""            ,""            ,""            ,""            ,""            ,""            ,""            ,""            ,""            ,"I:Dry/Wet"  ,"I:Dry/Wet"   ,""            ,""            ,""            ,""            ,""            ,""            ,"I:Dry/Wet"  ,""            ,""            ,""            ,""            ,""            ,""            ,""            ,""            ,""            ,""             },
        };
    }
