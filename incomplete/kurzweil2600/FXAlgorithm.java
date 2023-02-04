package edisyn.synth.kurzweilk2600;
import edisyn.*;


class FxAlgorithm
    {
    static final String[] PAR_FX_STR = new String[] {
        "None", "Mix Lvl", "Mix Bal", "Aux Lvl", "Aux Bal",
        "EQ1 Bass G", "EQ1 Bass F", "EQ2 Treb G", "EQ2 Treb F", 
        "Send1Lvl", "Send1Pan", "Send1Width", "Send2Lvl", "Send2Pan", "Send2Width",
        "Absorption", "Bass Gain", "BassShlf F", "BassShlf G", "Build Env", "Build Time", "Decay Time",
        "Density", "Diff Scale", "DiffAmtScl", "DiffCross", "DiffExtent", "DiffLenScl", "Diffusion", 
        "Dly Lenght", "Dly Lvl", "E Build", "E DfDlyScl", "E DfLenScl", "E DiffAmt", "E DiffDlyL", 
        "E DiffDlyLX", "E DiffDlyR", "E DiffDlyRX", "E Dly L", "E Dly LX", "E Dly R", "E Dly RX", 
        "E Dly Scl", "E Fdbk Amt", "E HF Damp", "E PreDlyL", "E PreDlyR", "E X Blend", "Early Bass",
        "Early Damp", "EarRef Lvl", "Expanse", "Fdbk Lvl", "Gate Atk", "Gate Duck",  "Gate Rel", 
        "Gate Thres", "Gate Time", "GateSigDly", "HF Damping", "InfinDecay", "Inj Build", "Inj LP", 
        "Inj Skew", "Inj Spread", "L Density", "L Diff Scl", "L Dry Pan", "L HF Damp", "L Out Gain", 
        "L Pre Dly", "L Pre DlyL", "L Pre DlyR", "L Room Type", "L Rvb Time", "L Size Scl", "L Wet Bal", 
        "L Wet/Dry", "Late Bass", "Late Damp", "Late Lvl", "LateRvbTim", "LF Damping", "LF Split", 
        "LF Time", "LFO Depth", "LFO Rate", "Lopass", "Lowpass", "Mid Bass", "Mid Damp", "Out Gain", 
        "Pre Dly", "R Density", "R Diff Scl", "R Dry Pan", "R HF Damp", "R Out Gain", "R Pre Dly", 
        "R Pre DlyL", "R Pre DlyR", "R Room Type", "R Rvb Time", "R Size Scl", "R Wet Bal", "R Wet/Dry", 
        "Room Size", "Room Type", "Rvb Env", "Rvb Length", "Rvrb Time", "Size Scale", "TrebShlf F", 
        "TrebShlf G", "Wet/Dry", 
        "Bass Freq", "Comp Atk", "Comp Ratio", "Comp Rel", "Comp Thres", "CompSmooth", "DecayRateA",
        "DecayRateB", "DecayRateC", "DecayRateD", "Delay Scale", "Diff Amt", "Diff Delay", "Dist Drive",
        "DistWarmth", "Dly Len A", "Dly Len B", "Dly Len C", "Dly Len D", "DlySelect1", "DlySelect2",
        "DlySelect3", "DlySelect4", "Dry Bal", "Dry Gain", "Dry In/Out", "FB2/FB1->FB", "Fdbk Image",
        "Fdbk Kill", "Fdbk Level", "Feedback", "Gain A", "Gain B", "Gain C", "Gain D", "Hold", 
        "L Diff Dly", "L Fdbk1 Dly", "L Fdbk2 Dly", "L Pan", "L Tap1 Dly", "L Tap1 Lvl", "L Tap2 Dly",
        "L Tap2 Lvl", "L Tap3 Dly", "L Tap3 Lvl", "LFO Period", "Loop Crs", "Loop Fine", "Loop Gain",
        "Loop Length", "Loop Lvl", "LpLFODepth", "LpLFOPhase", "Max Fdbk", "Mid1 Freq", "Mid1 Gain",
        "Mid1 Width", "Mid2 Freq", "Mid2 Gain", "Mid2 Width", "Pan", "R Diff Dly", "R Fdbk1 Dly",
        "R Fdbk2 Dly", "R Tap1 Dly", "R Tap1 Lvl", "R Tap2 Dly", "R Tap3 Dly", "R Tap2 Lvl", 
        "R Tap3 Lvl", "Send Gain", "T1LFODepth", "T1LFOPhase", "T2LFODepth", "T2LFOPhase", "Tap1 Bal",
        "Tap1 Crs", "Tap1 Delay", "Tap1 Fine", "Tap1 Level", "Tap1 Pan", "Tap1 Shapr", "Tap1/-5Bal",
        "Tap2 Bal", "Tap2 Crs", "Tap2 Delay", "Tap2 Fine", "Tap2 Level", "Tap2 Pan", "Tap2 Pitch", 
        "Tap2 PtAmt", "Tap2 Shapr", "Tap2/-6Bal", "Tap3 Bal", "Tap3 Crs", "Tap3 Delay", "Tap3 Fine",
        "Tap3 Level", "Tap3 Pitch", "Tap3 PtAmt", "Tap3 Shapr", "Tap3/-7Bal", "Tap4 Bal", "Tap4 Crs",
        "Tap4 Delay", "Tap4 Fine", "Tap4 Level", "Tap4 Pitch", "Tap4 PtAmt", "Tap4 Shapr", "Tap4/-8Bal",
        "Tap5 Bal", "Tap5 Crs", "Tap5 Delay", "Tap5 Fine", "Tap5 Level", "Tap5 Pitch", "Tap5 PtAmt",
        "Tap5 Shapr", "Tap6 Bal", "Tap6 Crs", "Tap6 Delay", "Tap6 Fine", "Tap6 Level", "Tap6 Pitch",
        "Tap6 PtAmt", "Tap6 Shapr", "Tap7 Bal", "Tap7 Crs", "Tap7 Delay", "Tap7 Fine", "Tap7 Level",
        "Tap8 Bal", "Tap8 Crs", "Tap8 Delay", "Tap8 Fine", "Tap8 Level", "Tempo", "Treb Freq",
        "Treb Gain", "Xcouple", "R Pan", "Delay", "LFO Mode", 
        "EQ1 Treb G", "EQ1 Treb F", "EQ1 LP-1 F", "EQ1 LP-2 F", "EQ1 HP-1 F", "EQ1 HP-2 F", "EQ1 Mid G", 
        "EQ1 Mid F", "EQ2 Bass G", "EQ2 Bass F", "EQ2 LP-1 F", "EQ2 LP-2 F", "EQ2 HP-1 F", "Send1Bal",
        "Send2Bal",
        "Bandwidth", "CenterFreq", "Dly1 Crs", "Dly1 Fin", "Dly2 Crs", "Dly2 Fin", "Dly3 Crs", 
        "Dly3 Fin", "Dly4 Crs", "Dly4 Fin", "FB APNotch", "FLFO Depth", "FLFO LRPhs", "FLFO Rate", 
        "In Width",  "L Ctr Freq", "L Fdbk Lvl", "L Feedback", "L LFO Rate", "L LFO1Depth", "L LFO1Rate",
        "L LFO2Depth", "L LFO2Rate", "L LFO3Depth", "L LFO3Rate", "L LFODepth", "L PitchEnv", "L Tap Dly",
        "L Tap Lvl", "L Tap Pan", "L Tap1 Pan", "L Tap2 Pan", "L Tap3 Pan", "L/R Phase", "LCenterFrq",
        "LFO Fdbk", "LFO1 Depth", "LFO1 Level", "LFO1 LRPhs", "LFO1 Phase", "LFO1 Rate", "LFO2 Depth",
        "LFO2 Level", "LFO2 LRPhs", "LFO2 Phase", "LFO2 Rate", "LFO3 Depth", "LFO3 Level", "LFO3 LRPhs",
        "LFO3 Phase", "LFO3 Rate", "LFO4 Level", "LFO4 Phase", "N/F Phase", "NLFO Depth", "NLFO LRPhs",
        "NLFO Rate", "Noise Gain", "Noise LP", "Notch/BP", "Notch/Dry", "NotchDepth", "OutAPNotch",
        "Pitch Env", "R Ctr Freq", "R Fdbk Lvl", "R Feedback", "R LFO Rate", "R LFO1Depth", "R LFO1Rate",
        "R LFO2Depth", "R LFO2Rate", "R LFO3Depth", "R LFO3Rate", "R LFODepth", "R PitchEnv", "R Tap Dly",
        "R Tap Lvl", "R Tap Pan", "R Tap1 Pan", "R Tap2 Pan", "R Tap3 Pan", "RCenterFrq", "Stat Fdbk",
        "StatDlyCrs", "StatDlyFin", "StatDlyLvl", "Tap Dly", "Tap Lvl", "Tap1 Dly", "Tap1 Lvl",
        "Tap2 Dly", "Tap2 Lvl", "Tap3 Dly", "Tap3 Lvl", "Xcurs1 Crs", "Xcurs1 Fin", "Xcurs2 Crs",
        "Xcurs2 Fin", "Xcurs3 Crs", "Xcurs3 Fin", "Xcurs4 Crs", "Xcurs4 Fin", "LFO Halt", "LFO Tempo",
        "*/Dry->Dly", "*/Dry->Rvb", "4T FB Imag", "4T FB Lvl", "4T FB Xcpl", "4T HF Damp", "4T LF Damp",
        "4T LoopLen", "4T Tempo", "A->B cfg", "A/Dry->B", "Ch Delay 1", "Ch Delay 2", "Ch Delay L",
        "Ch Delay R", "Ch Depth 1", "Ch Depth 2", "Ch Depth L", "Ch Depth R", "Ch Fdbk", "Ch Fdbk L",
        "Ch Fdbk R", "Ch HF Damp", "Ch LFO cfg", "Ch LRPhase", "Ch PtchEnv", "Ch Rate 1", "Ch Rate 2",
        "Ch Rate L", "Ch Rate R", "Ch Xcouple", "Ch/Dly->*", "Ch/Dry>4T", "Ch/Dry>Dly", "ChPtchEnvL",
        "ChPtchEnvR", "CompIn/Out", "CompMakeUp", "CompSigDly", "dc Offset", "Dly FBImag", "Dly Fdbk L",
        "Dly Fdbk R", "Dly HFDamp", "Dly Image", "Dly LF Damp", "Dly LFDamp", "Dly Tempo", "Dly Time L",
        "Dly Time R", "Dly Xcple", "DynamRange", "FdbkComprs", "Fl  R Phase", "Fl Delay 1", "Fl Delay 2",
        "Fl Delay C", "Fl Delay F", "Fl Delay L", "Fl Delay R", "Fl Fdbk", "Fl Fdbk 1", "Fl Fdbk 2",
        "Fl Fdbk L", "Fl Fdbk R", "Fl HF Damp", "Fl L Phase", "Fl LFO cfg", "Fl LFO Lvl", "Fl LRPhase",
        "Fl Period", "Fl Phase 1", "Fl Phase 2", "Fl Phase L", "Fl Phase R", "Fl Rate", "Fl Rate 1",
        "Fl Rate 2", "Fl StatDly", "Fl StatDlyC", "Fl StatDlyF", "Fl StatFB", "Fl StatLvl", "Fl Tempo",
        "Fl Xcouple", "Fl Xcurs 1", "Fl Xcurs 2", "Fl Xcurs C", "Fl Xcurs F", "Fl Xcurs L", "Fl Xcurs R",
        "Fl/Dly->*", "Fl/Dry>4T", "Fl/Dry>Dly", "Flange W/D", "Headroom", "In/Out", "L Delay",
        "L LFO Dpth", "L LFO Mode", "L Mix Chor", "L Mix Dly", "L Mix Flan", "L Mix Ptch", "L Mix Rvb",
        "L1 Delay", "L1 Fdbk", "L1 HF Damp", "L1 LFODpth", "L1 LFOMode", "L1 LFORate", "L1 Mix",
        "L1 Pan", "L1/Dry->L2", "L2 Delay", "L2 Fdbk", "L2 HF Damp", "L2 LFODpth", "L2 LFOMode",
        "L2 LFORate", "L2 Mix", "L2 Pan", "LsrCntourL", "LsrCntourR", "LsrSpace L", "LsrSpace R",
        "Mix 4 Tap", "Mix Chorus", "Mix Delay", "Mix Flange", "Mix Pitchr", "Mix Reverb", "Mix Shaper",
        "Pt 1/2 Wts", "Pt 1/4 Wts", "Pt Inp Bal", "Pt Odd Wts", "Pt Offset", "Pt Out Pan", "Pt PairWts",
        "Pt Pitch", "Pt PkShape", "Pt PkSplit", "Pt/Ch->*", "Pt/Dry->Ch", "Pt/Dry->Fl", "Pt/Fl->*",
        "Quant W/D", "R Delay", "R LFO Dpth", "R LFO Mode", "R Mix Chor","R Mix Dly", "R Mix Flan",
        "R Mix Ptch", "R Mix Rvb", "R1 Delay", "R1 Delay", "R1 Fdbk", "R1 HF Damp", "R1 LFODpth",
        "R1 LFOMode", "R1 LFORate", "R1 Mix", "R1 Pan", "R1/Dry->R2", "R2 Delay", "R2 Fdbk",
        "R2 HF Damp", "R2 LFODpth", "R2 LFOMode", "R2 LFORate", "R2 Mix", "R2 Pan", "Reverb W/D",
        "ReverbGain", "Rv Density", "Rv DiffScl", "Rv HF Damp", "Rv PreDlyL", "Rv PreDlyR", "Rv SizeScl",
        "Rv Time", "Rv Type", "SCBassFreq", "SCBassGain", "SCEQIn/Out", "SCMidFreq", "SCMidGain",
        "SCMidWidth", "SCTrebFreq", "SCTrebGain", "Shp Amount", "Shp Inp LP", "Shp Out LP", "Shp OutPad",
        "Bass Tone", "Cab Bypass", "Cab In/Out", "Cab Pan", "Cab Preset", "Cabinet HP", "Cabinet LP",
        "Ch Fdbk  L", "Ch Fdbk  R", "Ch Out Bal", "Ch Rate  L", "Ch Rate  R", "Ch Wet/Dry", "Curve 1",
        "Curve 2", "Curve 3", "Curve 4", "Curve 5", "Curve 6", "Curve 7", "Curve 8",
        "Fl Fdbk  L", "Fl Fdbk  R", "Fl Out Bal", "Fl Wet/Dry", "Highpass", "Input Bal", "LP0 Freq",
        "LP1 Freq", "LP2 Freq", "LP3 Freq", "LP4 Freq", "LP5 Freq", "LP6 Freq", "LP7 Freq",
        "LP8 Freq", "MD Delay", "MD Fdbk", "MD Insert", "MD LFODpth", "MD LFOMode", "MD LFORate",
        "MD Wet/Dry", "Mid Tone", "Poly Drive", "Treb Tone", "Tube Drive", "Warmth",
        "Alias W/D", "Beam Width", "Curvature", "Dist Curve", "Dist Gain", "Dist LP A", "Dist LP B",
        "DistLPFreq", "EvenOrders", "Gain", "Hi Beam W", "Hi Gain", "Hi LP", "Hi Rate",
        "Hi Res Dly", "Hi Size", "Hi Trem", "Hi Xover", "HiMic Lvls", "HiMicA Lvl", "HiMicA Pan",
        "HiMicA Pos", "HiMicB Lvl", "HiMicB Pan", "HiMicB Pos", "HiResonate", "HiResXcurs", "Lo Beam W",
        "Lo Gain", "Lo HP", "Lo Rate", "Lo Res Dly", "Lo Size", "Lo Trem", "Lo Xover",
        "LoMic Lvls", "LoMicA Lvl", "LoMicA Pan", "LoMicA Pos", "LoMicB Lvl", "LoMicB Pan", "LoMicB Pos",
        "LoResonate", "LoResXcurs", "Mic A Lvl", "Mic A Pan", "Mic A Pos", "Mic Angle", "Mic B Lvl",
        "Mic B Pan", "Mic B Pos", "Pch/Dry>Rv", "Pitch Crs", "Pitch Fine", "Pt Offst", "Rate",
        "Res Dly", "Res HiPhs", "Res Phs", "Res Xcurs", "ResH/LPhs", "Resonate", "Roto InOut",
        "Size", "Tremolo", "Vib/Chor", "VibChInOut", "Xover",
        "Ch Delay", "Ch Depth", "Ch Fdbk", "Ch Rate", "Ch Xcouple", "CmpSCBassF", "CmpSCBassG",
        "CmpSCMidF", "CmpSCMidG", "CmpSCMidW", "CmpSCTrebF", "CmpSCTrebG", "Comp SC EQ", "CompSCInp",
        "Fl Delay", "Fl Xcurs", "Gate Chan", "GateIn/Out", "GateSCInp", "L/RDpthDif", "RDlyDiff",
        "A Freq  1", "A Freq  2", "A Freq  3", "A Freq  4", "A Gain  1", "A Gain  2", "A Gain  3",
        "A Gain  4", "A Width 1", "A Width 2", "A Width 3", "A Width 4", "A/B Mix", "AFreqScale",
        "Amount", "Atk Rate", "Atk Time", "B Freq  1", "B Freq  2", "B Freq  3", "B Freq  4", 
        "B Gain  1", "B Gain  2", "B Gain  3", "B Gain  4", "B Width 1", "B Width 2", "B Width 3", 
        "B Width 4", "Band FreqC", "Band FreqF", "Band Width", "BFreqScale", "ComprsChan", "Contour", 
        "CrossOver1", "CrossOver2", "Delay Crs", "Delay Fine", "Dly Coarse", "Dly Fine", "Dly FreqC", 
        "Dly FreqF", "Drive", "Drive Cut", "DwnOffsLvl", "DwnOffsPan", "Env Rate", "FB Invert", 
        "FilterType", "Freq Sweep", "Frequency", "GateSCSrc", "Half Wts", "Hi Amt", "Hi Enable", 
        "Hi Mix", "In Gain L", "In Gain R", "In Lowpass", "InLowpassL", "InLowpassR", "L FiltType", 
        "L Freq", "L Phase", "L*R Gain", "L*R Pan", "LFO PlsWid", "LFO Shape", "LFO Smooth", 
        "Lo Amt", "Lo Enable", "Lo Mix", "LResonance", "MakeUpGain", "Max Freq", "Mid Amt", 
        "Mid Enable", "Mid Mix", "Min Freq", "Mod Mode", "Morph A>B", "Odd Wts", "Offs Scale", 
        "OffsetFreq", "Osc1 Freq", "Osc1 Lvl", "Osc1 Shape", "Osc1PlsWid", "Osc1Smooth", "Out Pan", 
        "Out Width", "Pair Wts", "Pan Wet", "PchOffs AL", "PchOffs AR", "PchOffs BL", "PchOffs BR", 
        "Pitch", "Pitch A", "Pitch B", "Ptch Offst", "Quartr Wts", "R FiltType", "R Freq", 
        "R Phase", "Ratio", "Rel Rate", "Rel Thres", "Rel Time", "Rel Time A", "Rel Time B", 
        "Resonance", "Retrigger", "RResonance", "Rvrs W/D", "SC Input", "Shift Crs", "Shift Fine",
        "Signal Dly", "Sine2 Freq", "Sine2 Lvl", "Sine3 Freq", "Sine3 Lvl", "Sine4 Freq", "Sine4 Lvl", 
        "Sine5 Freq", "Sine5 Lvl", "SmoothTime", "Smth Rate", "Spacing", "Threshold", "Trigger", 
        "UpOffsLvl", "UpOffsPan", "Wet Gain",
        "   31Hz G", "   62Hz G", "  125Hz G", "  250Hz G", "  500Hz G", " 1000Hz G", " 2000Hz G",
        " 4000Hz G", " 8000Hz G", "16000Hz G", "50% Weight", "Atk High", "Atk Low", "Atk Mid",
        "Center", "CenterGain", "CentrAtten", "CmpExpChan", "Comp1Ratio", "Comp1Thres", "Comp2Ratio",
        "Comp2Thres", "CrossOver", "Delay High", "Delay Low", "Delay Mid", "Depth", "Diff Gain", 
        "DiffBassF", "DiffBassG", "Ducking", "Env Time", "Exp Atk", "Exp Ratio", "Exp Rel",
        "Exp Thres", "ExpandChan", "Fund FreqC", "Fund FreqF", "Harmonics", "Hi Delay", "Hi Drive",
        "Hi Shelf F", "Hi Shelf G", "Hi Xfer", "Hi Xfer1", "Hi Xfer2", "ImageWidth", "In Select",
        "L   31Hz G", "L   62Hz G", "L  125Hz G", "L  250Hz G", "L  500Hz G", "L 1000Hz G", "R 1000Hz G",
        "L 2000Hz G", "L 4000Hz G", "L 8000Hz G", "L CentrAtt", "L In Gain", "L In/Out", "L LFOShape",
        "L Origin", "L PanWidth", "L PlseWdth", "L RateScal", "L/R Delay", "L16000Hz G", "LFO Phase", 
        "Lo Delay", "Lo Drive", "Lo Xfer", "MakeUp Low", "MakeUp Mid", "MakeUpHigh", "Mid  Freq",
        "Mid  Gain", "Mid  Wid", "Mid Delay", "Mid Drive", "Mid Freq", "Mid Gain", "Mid Xfer1",
        "Mid Xfer2", "Mid3 Freq", "Mid3 Gain", "Mid3 Width", "Origin", "Pan High", "Pan Low",
        "Pan Mid", "PanWidth", "PulseWidth", "R   31Hz G", "R   62Hz G", "R  125Hz G", "R  250Hz G",
        "R  500Hz G", "R 2000Hz G", "R 4000Hz G", "R 8000Hz G", "R CentrAtt", "R In Gain", "R In/Out",
        "R LFOShape", "R Origin", "R PanWidth", "R PlseWdth", "R RateScal", "R16000Hz G", "Rate Scale",
        "Ratio High", "Ratio Low", "Ratio Mid", "Rel High", "Rel Low", "Rel Mid", "RMS Settle",
        "Smth High", "Smth Low", "Smth Mid", "Space", "Stim Gain", "Thres High", "Thres Low",
        "Thres Mid", "Bipole 1", "Bipole 2", "Bipole 3", "Bipole 4", "L Invert", "L Out Mode",
        "Monopole 5", "Monopole 6", "Monopole 7", "Monopole 8", "R Invert", "R Out Mode"
        };
        
    static final int[] PAR_FX_TYPE = new int[]{ 
        // TODO : dummy filled, 
        // a value from PAR_FX_IDX indexes the algorithm type in this table
        // make this an enum
        0, 1, 5, 1, 5, // "Aux Bal"
        3, 4, 3, 4, // "EQ2 Treb F"
        1, 5, 5, 1, 5, 5, // "Send2Width"
        99, 99, 99, 99, 99, 99, 99, // "Decay Time"
        99, 99, 99, 99, 99, 99, 99, // "Diffusion"
        99, 99, 99, 99, 99, 99, 99, // "E DiffDlyL"
        99, 99, 99, 99, 99, 99, 99, // "E Dly RX"
        99, 99, 99, 99, 99, 99, 99, // "Early Bass"
        99, 99, 99, 99, 99, 99, 99, // "Gate Rel"
        99, 99, 99, 99, 99, 99, 99, // "Inj LP"
        99, 99, 99, 99, 99, 99, 99, // "L Out Gain"
        99, 99, 99, 99, 99, 99, 99, // "L Wet Bal"
        99, 99, 99, 99, 99, 99, 99, // "LF Split"
        99, 99, 99, 99, 99, 99, 99, 1, // "Out Gain"
        99, 99, 99, 99, 99, 99, 99, // "R Pre Dly"
        99, 99, 99, 99, 99, 99, 99, // "R Wet/Dry"
        99, 99, 99, 99, 99, 99, 99, // "TrebShlf F"
        99, 6, // "Wet/Dry"
        99, 99, 99, 99, 99, 99, 99, // "DecayRateA"
        99, 99, 99, 99, 99, 99, 99, // "Dist Drive"
        99, 99, 99, 99, 99, 99, 99, // "DlySelect2"
        99, 99, 99, 99, 99, 99, 99, // "Fdbk Image"
        99, 99, 99, 99, 99, 99, 99, 99, // "Hold
        99, 99, 99, 99, 99, 99, 99, // "L Tap2 Dly"
        99, 99, 99, 99, 99, 99, 99, // "Loop Gain"
        99, 99, 99, 99, 99, 99, 99, // "Mid1 Gain"
        99, 99, 99, 99, 99, 99, 99, // "R Fdbk1 Dly"
        99, 99, 99, 99, 99, 99, // "R Tap2 Lvl"
        99, 99, 99, 99, 99, 99, 99, // "Tap1 Bal"
        99, 99, 99, 99, 99, 99, 99, // "Tap1/-5Bal"
        99, 99, 29, 99, 99, 99, 99, // "Tap2 Pitch"
        99, 99, 99, 99, 99, 99, 99, // "Tap3 Fine"
        99, 99, 99, 99, 99, 99, 99, // "Tap4 Crs"
        99, 99, 99, 99, 99, 99, 99, // "Tap4/-8Bal"
        99, 99, 99, 99, 99, 99, 99, // "Tap5 PtAmt"
        99, 99, 99, 99, 99, 99, 99, // "Tap6 Pitch"
        99, 99, 99, 99, 99, 99, 99, // "Tap7 Level"
        99, 99, 99, 99, 99, 99, 99, // "Treb Freq"
        99, 99, 99, 99, 99, // "LFO Mode"
        3,  4,  4,  4,  4,  4,  3, // "EQ1 Mid G"
        4,  3,  4,  4,  4,  4,  5, // "Send1Bal"
        5, // "Send2Bal"
        99, 99, 99, 99, 99, 99, 99, // "Dly3 Crs"
        99, 99, 99, 99, 99, 99, 99, // "FLFO Rate"
        99, 99, 99, 99, 99, 99, 99, // "L LFO1Rate"
        99, 99, 99, 99, 99, 99, 99, // "L Tap Dly"
        99, 99, 99, 99, 99, 99, 99, // "LCenterFrq"
        99, 99, 99, 99, 99, 99, 99, // "LFO2 Depth"
        99, 99, 99, 99, 99, 99, 99, // "LFO3 LRPhs"
        99, 99, 99, 99, 99, 99, 99, // "NLFO LRPhs"
        99, 99, 99, 99, 99, 99, 99, // "OutAPNotch"
        99, 99, 99, 99, 99, 99, 99, // "R LFO1Rate"
        99, 99, 99, 99, 99, 99, 99, // "R Tap Dly"
        99, 99, 99, 99, 99, 99, 99, // "Stat Fdbk"
        99, 99, 99, 99, 99, 99, 99, // "Tap1 Lvl"
        99, 99, 99, 99, 99, 99, 99, // "Xcurs2 Crs"
        99, 99, 99, 99, 99, 99, 99, // "LFO Tempo"
        99, 99, 99, 99, 99, 99, 99, // "4T LF Damp"
        99, 99, 99, 99, 99, 99, 99, // "Ch Delay L"
        99, 99, 99, 99, 99, 99, 99, // "Ch Fdbk L"
        99, 99, 99, 99, 99, 99, 99, // "Ch Rate 2"
        99, 99, 99, 99, 99, 99, 99, // "ChPtchEnvL"
        99, 99, 99, 99, 99, 99, 99, // "Dly Fdbk L"
        99, 99, 99, 99, 99, 99, 99, // "Dly Time L"
        99, 99, 99, 99, 99, 99, 99, // "Fl Delay 2"
        99, 99, 99, 99, 99, 99, 99, // "Fl Fdbk 2"
        99, 99, 99, 99, 99, 99, 99, // "Fl LRPhase"
        99, 99, 99, 99, 99, 99, 99, // "Fl Rate 1"
        99, 99, 99, 99, 99, 99, 99, // "Fl Tempo"
        99, 99, 99, 99, 99, 99, 99, // "Fl Xcurs R"
        99, 99, 99, 99, 99, 99, 99, // "L Delay"
        99, 99, 99, 99, 99, 99, 99, // "L Mix Rvb"
        99, 99, 99, 99, 99, 99, 99, // "L1 Mix"
        99, 99, 99, 99, 99, 99, 99, // "L2 LFOMode"
        99, 99, 99, 99, 99, 99, 99, // "LsrSpace R"
        99, 99, 99, 99, 99, 99, 99, // "Mix Shaper"
        99, 99, 99, 99, 99, 99, 99, // "Pt PairWts"
        99, 99, 99, 99, 99, 99, 99, // "Pt/Fl->*"
        99, 99, 99, 99, 99, 99, 99, // "R Mix Flan"
        99, 99, 99, 99, 99, 99, 99, // "R1 LFODpth"
        99, 99, 99, 99, 99, 99, 99, // "R2 Fdbk"
        99, 99, 99, 99, 99, 99, 99, // "Reverb W/D"
        99, 99, 99, 99, 99, 99, 99, // "Rv SizeScl"
        99, 99, 99, 99, 99, 99, 99, // "SCMidGain"
        99, 99, 99, 99, 99, 99, 99, // "Shp OutPad"
        99, 99, 99, 99, 99, 99, 99, // "Cabinet LP"
        99, 99, 99, 99, 99, 99, 99, // "Curve 1"
        99, 99, 99, 99, 99, 99, 99, // "Curve 8"
        99, 99, 99, 99, 99, 99, 99, // "LP0 Freq"
        99, 99, 99, 99, 99, 99, 99, // "LP7 Freq"
        99, 99, 99, 99, 99, 99, 99, // "MD LFORate"
        99, 99, 99, 99, 99, 99, // "Warmth"
        99, 99, 99, 99, 99, 99, 99, // "Dist LP B"
        99, 99, 99, 99, 99, 99, 99, // "Hi Rate"
        99, 99, 99, 99, 99, 99, 99, // "HiMicA Pan"
        99, 99, 99, 99, 99, 99, 99, // "Lo Beam W"
        99, 99, 99, 99, 99, 99, 99, // "Lo Xover"
        99, 99, 99, 99, 99, 99, 99, // "LoMicB Pos"
        99, 99, 99, 99, 99, 99, 99, // "Mic B Lvl"
        99, 99, 99, 99, 99, 99, 99, // "Rate"
        99, 99, 99, 99, 99, 99, 99, // "Roto InOut"
        99, 99, 99, 99, 99, // "Xover"
        99, 99, 99, 99, 99, 99, 99, // "CmpSCBassG"
        99, 99, 99, 99, 99, 99, 99, // "CompSCInp"
        99, 99, 99, 99, 99, 99, 99, // "RDlyDiff"
        99, 99, 99, 99, 99, 99, 99, // "A Gain  3"
        99, 99, 99, 99, 99, 99, 99, // "AFreqScale"
        99, 99, 99, 99, 99, 99, 99, // "B Freq  4"
        99, 99, 99, 99, 99, 99, 99, // "B Width 3"
        99, 99, 99, 99, 99, 99, 99, // "Contour"
        99, 99, 99, 99, 99, 99, 99, // "Dly FreqC"
        99, 99, 99, 99, 99, 99, 99, // "FB Invert"
        99, 99, 99, 99, 99, 99, 99, // "Hi Enable"
        99, 99, 99, 99, 99, 99, 99, // "L FiltType"
        99, 99, 99, 99, 99, 99, 99, // "LFO Smooth"
        99, 99, 99, 99, 99, 99, 99, // "Mid Amt"
        99, 99, 99, 99, 99, 99, 99, // "Offs Scale"
        99, 99, 99, 99, 99, 99, 99, // "Out Pan"
        99, 99, 99, 99, 99, 99, 99, // "PchOffs BR"
        99, 99, 99, 99, 99, 99, 99, // "R Freq"
        99, 99, 99, 99, 99, 99, 99, // "Rel Time B"
        99, 99, 99, 99, 99, 99, 99, // "Shift Fine"
        99, 99, 99, 99, 99, 99, 99, // "Sine4 Lvl"
        99, 99, 99, 99, 99, 99, 99, // "Trigger"
        99, 99, 99, // "Wet Gain"
        99, 99, 99, 99, 99, 99, 99, // " 2000Hz G"
        99, 99, 99, 99, 99, 99, 99, // "Atk Mid"
        99, 99, 99, 99, 99, 99, 99, // "Comp2Ratio"
        99, 99, 99, 99, 99, 99, 99, // "Diff Gain"
        99, 99, 99, 99, 99, 99, 99, // "Exp Rel"
        99, 99, 99, 99, 99, 99, 99, // "Hi Drive"
        99, 99, 99, 99, 99, 99, 99, // "In Select"
        99, 99, 99, 99, 99, 99, 99, // "R 1000Hz G"
        99, 99, 99, 99, 99, 99, 99, // "L LFOShape"
        99, 99, 99, 99, 99, 99, 99, // "LFO Phase"
        99, 99, 99, 99, 99, 99, 99, // "Mid  Freq"
        99, 99, 99, 99, 99, 99, 99, // "Mid Xfer1"
        99, 99, 99, 99, 99, 99, 99, // "Pan Low"
        99, 99, 99, 99, 99, 99, 99, // "R  250Hz G"
        99, 99, 99, 99, 99, 99, 99, // "R In/Out"
        99, 99, 99, 99, 99, 99, 99, // "Rate Scale"
        99, 99, 99, 99, 99, 99, 99, // "RMS Settle"
        99, 99, 99, 99, 99, 99, 99, // "Thres Low"
        99, 99, 99, 99, 99, 99, 99, // "L Out Mode"
        99, 99, 99, 99, 99, 99 // "R Out Mode"
        };
        
    static final int[][] PAR_FX_IDX = new int[][] { // index in PAR_FX_STR 
        {0},
        // Reverb 1 -> 1
        {0, 1, 2, 3, 4, 115, 111, 71, 92, 60, 99, 108, 23, 112, 22}, // 
        {0, 1, 2, 3, 4, 78, 70, 77, 68, 106, 98, 105, 96, 74, 75, 67, 76, 72, 66, 69, 73, 102, 103, 95, 104, 100, 94, 97, 101},
        {0, 1, 2, 3, 4, 115, 111, 71, 92, 60, 99, 108, 23, 112, 22, 57, 55, 58, 54, 56, 59},
        {0, 1, 2, 3, 4, 115, 15, 60, 71, 92, 51, 81, 99, 108, 112, 113, 114, 24, 27, 87, 86, 84, 85, 32, 34, 39, 40, 35, 36, 48, 41, 42, 37, 38},
        {0, 1, 2, 3, 4, 115, 111, 60, 71, 92, 51, 81, 99, 108, 112, 61, 113, 114, 24, 27, 87, 86, 84, 85, 32, 34, 39, 40, 35, 36, 48, 41, 42, 37, 38},
        {0, 1, 2, 3, 4, 115, 15,  60, 71, 92, 51, 81, 99, 108, 112, 84, 85, 113, 114, 24, 27, 87, 86, 62, 65, 34, 33, 43, 46, 63, 31, 44, 45, 47},
        {0, 1, 2, 3, 4, 115, 111, 60, 71, 92, 51, 81, 99, 108, 112, 61, 84, 85, 113, 114, 24, 27, 87, 86, 62, 65, 34, 33, 43, 46, 63, 31, 44, 45, 47},
        {0, 1, 2, 3, 4, 115, 15, 60, 71, 92, 88, 99, 108, 112, 84, 85, 26, 25, 24, 27, 87, 86},
        {0, 1, 2, 3, 4, 115, 82, 60, 71, 92, 89, 99, 108, 112, 61, 84, 85, 26, 25, 24, 27, 87, 86},
        {0, 1, 2, 3, 4, 115, 15, 60, 71, 92, 89, 99, 108, 112, 84, 85, 52, 24, 27, 87, 86, 62, 65, 64, 113, 114, 99, 18},
        {0, 1, 2, 3, 4, 115, 111, 60, 71, 92, 89, 99, 108, 112, 61, 84, 85, 52, 24, 27, 87, 86, 62, 65, 64, 113, 114, 17, 18},
        {0, 1, 2, 3, 4, 115, 107, 93, 60, 92, 21, 16, 20, 19},
        {0, 1, 2, 3, 4, 115, 107, 93, 60, 92, 28, 21, 16, 89, 87, 86, 20, 19},
        {0, 1, 2, 3, 4, 115, 107, 93, 60, 92, 21, 83, 89, 16},
        {0, 1, 2, 3, 4, 115, 53, 60, 92, 30, 29, 109, 110, 49, 90, 79, 50, 91, 80},
        // Delay 130 -> 16
        {0, 1, 2, 3, 4, 115, 146, 142, 60, 92, 152, 178, 127,153, 154, 156, 158, 160, 179, 180, 181, 183, 184, 157, 159, 161, 182, 185, 186, 306, 307, 308},
        {0, 1, 2, 3, 4, 115, 145, 60, 92, 139, 151, 163, 164, 193, 195, 201, 203, 126, 211, 213, 220, 222, 196, 204, 214, 223, 192, 200, 210, 219},
        {0, 1, 2, 3, 4, 115, 145, 60, 92, 254, 139, 151, 166, 194, 202, 212, 221, 196, 204, 214, 223, 192, 200, 210, 219},
        {0, 1, 2, 3, 4, 115, 145, 257, 60, 92, 139, 151, 163, 164, 193, 195, 201, 203, 126, 211, 213, 220, 211, 219, 231, 237, 239, 245, 247, 250, 252, 196, 204, 214, 223, 199, 209, 232, 240, 248, 253, 218, 227},
        {0, 1, 2, 3, 4, 115, 145, 257, 60, 92, 254, 139, 151, 166, 194, 202, 212, 221, 230, 238, 246, 251, 196, 204, 214, 223, 232, 240, 248, 253, 192, 200, 210, 219, 228, 236, 244, 249},
        {0, 1, 2, 3, 4, 115, 145, 60, 83, 92, 254, 128, 127, 166, 142, 194, 198, 196, 192, 202, 208, 206, 207, 204, 200, 212, 217, 215, 216, 214, 210, 221, 226, 224, 225, 223, 219},
        {0, 1, 2, 3, 4, 115, 145, 60, 83, 92, 254, 128, 127, 166, 143, 194, 198, 196, 192, 202, 208, 207, 204, 200, 212, 217, 215, 216, 214, 210, 221, 226, 224, 225, 223, 219, 230, 235, 233, 234, 232, 228, 238, 243, 241, 242, 240, 236},
        {0, 1, 2, 3, 4, 115, 165, 167, 60, 92, 254, 281, 83, 166, 162, 16, 116, 256, 255, 172, 171, 173, 175, 174, 176, 168, 169, 188, 189, 190, 191, 194, 196, 197, 202, 204, 205, 117, 119, 121, 118, 120, 129, 130},
        {0, 1, 2, 3, 4, 141, 140, 144, 170, 92, 254, 177, 60, 135, 136, 137, 138, 131, 122, 147, 132, 123, 148, 133, 124, 149, 134, 125, 150},
        {0, 1, 2, 3, 4, 115, 155, 92, 258, 259, 260, 87, 86, 146, 60},
        // Chorus / Flange / Phase 150 -> 26
        {0, 1, 2, 3, 4, 115, 145, 257, 60, 92, 339, 364, 363, 87, 86, 309},
        {0, 1, 2, 3, 4, 115, 145, 257, 60, 92, 339, 366, 368, 370, 365, 367, 369, 316, 321, 326, 312, 317, 322, 314, 319, 324},
        {0, 1, 2, 3, 4, 78, 70, 292, 257, 106, 98, 341, 304, 305, 294, 301, 303, 69, 353, 354, 343, 350, 352, 97, 302, 351 },
        {0, 1, 2, 3, 4, 78, 70, 292, 257, 106, 98, 341, 157, 159, 161, 306, 307, 308, 182, 185, 186, 355, 356, 357, 296, 298, 300, 295, 297, 299, 345, 347, 349, 344, 346, 348, 156, 158, 160, 69, 302, 181, 183, 184, 97, 351},
        {0, 1, 2, 3, 4, 115, 145, 257, 60, 92, 380, 162, 379, 362, 313, 318, 309, 315, 320, 360, 361, 371, 372, 373, 374, 278, 279, 280, 281},
        {0, 1, 2, 3, 4, 115, 311, 257, 60, 92, 359, 380, 162, 333, 362, 313, 318, 323, 327, 334, 309, 315, 320, 325, 328, 360, 361, 371, 372, 373, 374, 379, 375, 376, 377, 378, 278, 279, 280, 281, 282, 283, 284, 285},
        {0, 1, 2, 3, 4, 115, 145, 92, 277, 287, 289, 288, 337, 330, 332, 331, 329},
        {0, 1, 2, 3, 4, 5, 336, 277, 86, 87, 309},
        {0, 1, 2, 3, 4, 335, 293, 291, 92, 342, 340},
        {0, 1, 2, 3, 4, 115, 145, 257, 92, 277, 86, 87, 276, 309, 290},
        {0, 1, 2, 3, 4, 115, 145, 92, 87, 277, 287, 288, 329, 337, 330, 331},
        {0, 1, 2, 3, 4, 115, 146, 310, 286, 92, 257, 358, 338},
        // Combination 700 -> 38
        {0, 1, 2, 3, 4, 115, 508, 509, 92, 406, 409, 398, 394, 400, 411, 405, 410, 399, 395, 403, 414, 429, 422, 424, 428, 430, 423, 425}, 
        {0, 1, 2, 3, 4, 115, 508, 507, 92, 406, 409, 398, 394, 400, 405, 410, 399, 395, 413, 389, 388, 384, 194, 196, 192, 202, 204, 200, 212, 214, 210, 221, 223},
        {0, 1, 2, 3, 4, 115, 508, 507, 390, 92, 406, 389, 391, 404, 407, 396, 392, 401, 411, 405, 408, 397, 393, 402, 403, 388, 384, 383, 385, 386, 387, 194, 196, 192, 202, 204, 200, 212, 214, 210, 221, 223, 219},
        {0, 1, 2, 3, 4, 115, 481, 482, 485, 92, 532, 533, 536, 406, 409, 398, 394, 401, 411, 405, 408, 397, 393, 402, 414, 429, 422, 428, 430, 423, 412, 382, 558, 562, 560, 564, 563, 557, 559, 561},
        {0, 1, 2, 3, 4, 115, 508, 512, 390, 92, 406, 391, 404, 407, 396, 392, 401, 411, 405, 408, 397, 393, 402, 403, 564, 563, 558, 562, 560, 557, 559, 561},
        {0, 1, 2, 3, 4, 115, 508, 509, 390, 92, 406, 428, 391, 404, 407, 396, 392, 401, 411, 405, 408, 397, 393, 402, 403, 429, 422, 424, 426, 503, 505, 430, 423, 421, 431, 504, 506},
        {0, 1, 2, 3, 4, 115, 510, 509, 92, 464, 456, 470, 439, 444, 454, 471, 440, 445, 455, 446, 465, 459, 463, 474, 429, 422, 424, 428, 430, 423, 425},
        {0, 1, 2, 3, 4, 115, 510, 507, 92, 464, 456, 470, 439, 444, 454, 471, 440, 445, 455, 473, 389, 388, 384, 194, 196, 192, 202, 204, 200, 212, 214, 210, 221, 223, 219},
        {0, 1, 2, 3, 4, 115, 510, 507, 390, 92, 464, 389, 391, 448, 457, 466, 435, 442, 452, 450, 458, 467, 436, 443, 453, 446, 459, 462, 463, 449, 388, 384, 383, 385, 386, 387, 194, 196, 192, 202, 204, 200, 212, 214, 210, 221, 223, 219},
        {0, 1, 2, 3, 4, 115, 483, 482, 485, 92, 534, 533, 536, 464, 456, 470, 439, 444, 454, 446, 471, 440, 445, 455, 474, 429, 422, 428, 430, 423, 425, 472, 382, 558, 562, 560, 564, 563, 557, 559, 561},
        {0, 1, 2, 3, 4, 115, 510, 512, 390, 92, 464, 391, 448, 457, 466, 435, 442, 452, 450, 458, 467, 436, 443, 453, 459, 462, 463, 449, 564, 563, 558, 562, 560, 557, 559, 561},
        {0, 1, 2, 3, 4, 115, 510, 509, 390, 92, 464, 428, 391, 448, 457, 466, 435, 442, 452, 450, 458, 467, 436, 443, 453, 446, 459, 462, 463, 449, 429, 422, 424, 426, 503, 505, 430, 423, 421, 431, 504, 506},
        {0, 1, 2, 3, 4, 115, 510, 511, 390, 92, 464, 391, 448, 457, 466, 435, 442, 452, 450, 458, 467, 436, 443, 453, 446, 465, 459, 462, 463, 449, 521, 518, 517, 520, 515, 514},
        {0, 1, 2, 3, 4, 115, 510, 513, 390, 92, 464, 391, 448, 457, 466, 435, 442, 452, 450, 458, 467, 436, 443, 453, 446, 465, 459, 462, 463, 449, 574, 573, 575, 576},
        {0, 1, 2, 3, 4, 477, 528, 475, 92, 432, 420, 476, 464, 451, 447, 463, 441, 434, 449, 460, 461, 468, 469, 437, 438},
        {0, 1, 2, 3, 4, 78, 70, 155, 106, 98, 258, 478, 480, 294, 479, 293, 69, 529, 531, 343, 530, 342, 97},
        {0, 1, 2, 3, 4, 78, 70, 492, 501, 106, 98, 544, 553, 493, 502, 68, 494, 545, 554, 96, 546, 486, 490, 491, 489, 487, 488, 495, 499, 500, 498, 496, 497, 537, 542, 543, 541, 539, 540, 547, 551, 552, 550, 548, 549},
        {0, 1, 2, 3, 4, 115, 509, 512, 390, 92, 428, 391, 429, 422, 424, 426, 503, 505, 430, 423, 421, 504, 506, 564, 563, 558, 562, 560, 557, 559, 561},
        {0, 1, 2, 3, 4, 115, 513, 512, 390, 92, 391, 574, 573, 575, 576, 564, 563, 558, 562, 560, 557, 559, 561},
        {0, 1, 2, 3, 4, 477, 555, 560, 559, 556, 563, 561, 417, 390, 564, 558, 562, 557, 117, 119, 121, 419, 118, 120, 418, 433, 566, 565, 569, 568, 570, 572, 571, 567},
        {0, 1, 2, 3, 4, 115, 511, 508, 525, 92, 516, 521, 523, 519, 518, 522, 415, 409, 398, 394, 401, 411, 416, 410, 399, 395, 402, 403},
        {0, 1, 2, 3, 4, 115, 511, 510, 526, 92, 464, 516, 521, 523, 519, 518, 522, 448, 457, 466, 435, 452, 441, 450, 458, 467, 436, 453, 446},
        {0, 1, 2, 3, 4, 115, 484, 481, 482, 92, 535, 532, 533, 521, 518, 517, 520, 515, 514, 525, 409, 398, 394, 401, 411, 406, 410, 399, 395, 402, 403, 524, 381, 429, 422, 428, 430, 423, 425},
        {0, 1, 2, 3, 4, 115, 484, 483, 482, 92, 535, 534, 533, 521, 518, 517, 520, 515, 514, 526, 446, 465, 459, 463, 456, 470, 439, 444, 454, 464, 471, 440, 445, 455, 527, 381, 429, 422, 428, 430, 423, 425},
        // Distortion 724 -> 62
        {0, 1, 2, 3, 4, 115, 129, 624, 602, 92},
        {0, 1, 2, 3, 4, 477, 129, 624, 92, 177, 578, 581},
        {0, 1, 2, 3, 4, 115, 129, 624, 582, 92, 420, 583, 16, 116, 172, 171, 173, 256, 255, 175, 174, 176},
        {0, 1, 2, 3, 4, 115, 129, 92, 590, 591, 592, 593, 594, 595, 596, 597, 604, 605, 606, 607, 608, 609, 610, 611, 612, 16, 116, 172, 171, 173, 256, 255, 175, 174, 176},
        {0, 1, 2, 3, 4, 115, 129, 624, 582, 92, 583, 16, 116, 172, 171, 173, 256, 255},
        {0, 1, 2, 3, 4, 477, 603, 92, 577, 620, 622, 623, 624, 579, 581, 580, 615, 619, 613, 617, 618, 616, 614, 587, 398, 394, 584, 406, 589, 588, 399, 395, 585, 586},
        {0, 1, 2, 3, 4, 477, 603, 92, 577, 620, 622, 623, 624, 579, 581, 580, 615, 619, 613, 617, 618, 616, 614, 456, 470, 439, 598, 454, 601, 464, 471, 440, 599, 455, 600},
        {0, 1, 2, 3, 4, 477, 603, 92, 577, 620, 622, 621, 624, 579, 581, 580, 615, 619, 613, 617, 618, 616, 614, 587, 398, 394, 584, 406, 589, 588, 399, 395, 585, 586},
        {0, 1, 2, 3, 4, 477, 603, 92, 577, 620, 622, 621, 624, 579, 581, 580, 615, 619, 613, 617, 618, 616, 614, 456, 470, 439, 598, 454, 601, 464, 471, 440, 599, 455, 600},
        // Tone Wheel Organ 733 -> 71
        {0, 1, 2, 3, 4, 477, 691, 690, 687, 92, 583, 692, 653, 655, 657, 658, 652, 636, 638, 640, 641, 635, 663, 661, 662, 646, 644, 645, 666, 664, 665, 649, 647, 648, 667, 656, 668, 685, 650, 639, 651},
        {0, 1, 2, 3, 4, 477, 582, 583, 92, 129, 130, 692, 653, 655, 657, 658, 672, 636, 638, 640, 641, 667, 656, 668, 685, 650, 639, 651},
        {0, 1, 2, 3, 4, 477, 691, 690, 92, 129, 624, 687, 653, 659, 654, 636, 642, 637},
        {0, 1, 2, 3, 4, 477, 92, 653, 655, 657, 658, 652, 636, 638, 640, 641, 635, 663, 661, 662, 646, 644, 645, 666, 664, 665, 649, 647, 648, 667, 656, 668, 685, 650, 639, 651},
        {0, 1, 2, 3, 4, 477, 691, 690, 687, 92, 129, 130, 583, 692, 653, 655, 657, 658, 652, 636, 638, 640, 641, 635, 663, 661, 662, 646, 644, 645, 666, 664, 665, 649, 647, 648, 667, 656, 668, 685, 650, 639, 651},
        {0, 1, 2, 3, 4, 477, 691, 690, 687, 92, 129, 130, 583, 634, 680, 688, 689, 626, 671, 669, 670, 675, 673, 674, 686, 681, 684, 683},
        {0, 1, 2, 3, 4, 477, 691, 690, 687, 92, 129, 628, 632, 692, 655, 657, 658, 638, 640, 641, 635, 660, 663, 643, 646, 666, 649, 650, 639, 651, 682},
        {0, 1, 2, 3, 4, 477, 691, 690, 687, 92, 623, 583, 692, 653, 655, 657, 658, 636, 638, 640, 641, 635, 663, 661, 662, 646, 644, 645, 666, 664, 665, 649, 647, 648, 667, 656, 668, 685, 650, 639, 651},
        {0, 1, 2, 3, 4, 477, 92, 583, 634, 680, 688, 689, 626, 671, 669, 670, 675, 673, 674, 686, 681, 684, 683},
        {0, 1, 2, 3, 4, 477, 691, 690, 687, 92, 129, 130, 692, 655, 657, 658, 638, 640, 641, 660, 663, 643, 646, 666, 649, 650, 639, 651, 682},
        {0, 1, 2, 3, 4, 477, 141, 627, 633, 92, 629, 630, 631},
        {0, 1, 2, 3, 4, 477, 528, 625, 89, 92, 432, 420, 476, 677, 678, 86},
        {0, 1, 2, 3, 4, 115, 511, 512, 92, 521, 679, 517, 520, 515, 514, 676, 564, 563, 558, 562, 560, 557, 559, 561},
        {0, 1, 2, 3, 4, 477, 555, 560, 559, 556, 563, 561, 417, 564, 558, 562, 557, 117, 119, 121, 419, 118, 120, 418, 433},
        // New 781 -> 85
        {0, 1, 2, 3, 4, 115, 508, 509, 92, 406, 695, 693, 692, 694, 696, 309, 711, 712, 403, 414, 429, 422, 424, 428, 430, 423, 425},
        {0, 1, 2, 3, 4, 115, 510, 509, 92, 464, 456, 708, 707, 441, 454, 455, 446, 465, 459, 463, 474, 429, 422, 424, 428, 430, 423, 425},
        {0, 1, 2, 3, 4, 710, 711, 92, 417, 706, 433, 57, 55, 58, 54, 56, 59, 117, 119, 121, 419, 118, 120, 418, 699, 698, 701, 700, 702, 704, 703, 705, 555, 564, 563, 558, 562, 560, 557, 559, 561},
        {0, 1, 2, 3, 4, 477, 710, 711, 709, 92, 57, 55, 58, 54, 56, 59, 577, 620, 622, 623, 624, 581},
        // Special FX 900 -> 89
        {0, 1, 2, 3, 4, 115, 763, 92, 793, 764, 826, 845, 729, 821, 843}, 
        {0, 1, 2, 3, 4, 115, 763, 92, 793, 789, 826, 846, 827, 761, 821, 843},
        {0, 1, 2, 3, 4, 115, 380, 162, 379, 92, 782, 781, 783, 763, 826, 778, 793, 789, 819},
        {0, 1, 2, 3, 4, 115, 763, 765, 826, 92},
        {0, 1, 2, 3, 4, 78 , 70, 106, 98, 776, 777, 787, 817, 818, 828},
        {0, 1, 2, 3, 4, 477, 795, 727, 92, 804, 805, 746, 714, 722, 718, 715, 723, 719, 731, 739, 735, 732, 740, 736, 716, 724, 720, 717, 725, 721, 733, 741, 737, 734, 742, 738},
        {0, 1, 2, 3, 4, 477, 795, 727, 92, 804, 746, 714, 722, 718, 715, 723, 719, 731, 739, 735, 732, 740, 736, 716, 724, 720, 717, 725, 721, 733, 741, 737, 734, 742, 738},
        {0, 1, 2, 3, 4, 115, 794, 92, 779, 780, 800, 801, 802, 803, 799, 835, 837, 839, 841, 834, 836, 838, 840},
        {0, 1, 2, 3, 4, 115, 812, 796, 806, 92, 815, 816, 767},
        {0, 1, 2, 3, 4, 115, 728, 92, 115, 728},
        {0, 1, 2, 3, 4, 115, 749, 750, 92, 785, 784, 786, 791, 790, 792, 769, 768, 770},
        {0, 1, 2, 3, 4, 115, 53, 60, 92, 807, 753, 754, 844, 748},
        {0, 1, 2, 3, 4, 115, 53, 257, 60, 92, 753, 754, 844, 748},
        {0, 1, 2, 3, 4, 115, 53, 257, 60, 92, 753, 754, 844, 748},
        {0, 1, 2, 3, 4, 115, 829, 92, 177, 753, 754, 844, 748},
        {0, 1, 2, 3, 4, 115, 53, 257, 60, 92, 710, 711, 766, 751, 752, 844, 748, 57, 55, 58, 54, 56, 59},
        {0, 1, 2, 3, 4, 115, 796, 806, 92, 816, 767, 726, 813, 808, 814, 810, 809, 811},
        {0, 1, 2, 3, 4, 115, 773, 92, 798, 797, 759, 760, 847, 848},
        {0, 1, 2, 3, 4, 115, 771, 772, 774, 92, 849, 775, 759, 760, 847, 848},
        {0, 1, 2, 3, 4, 115, 146, 87, 89, 92, 602, 831, 832},
        {0, 1, 2, 3, 4, 477, 757, 758, 624, 92 , 762, 755, 756, 602, 16, 116, 172, 171, 173, 89, 256, 255, 175, 174, 176},
        {0, 1, 2, 3, 4, 477, 743, 744, 745, 92, 433, 830, 747, 730, 823, 842, 833, 820, 845, 788},
        {0, 1, 2, 3, 4, 477, 433, 833, 92, 830, 747, 730, 824, 825, 842, 820, 120, 822, 788},
        // Studio / Mixdown FX 950 -> 112
        {0, 1, 2, 3, 4, 477, 433, 92, 830, 747, 730, 823, 842, 833, 820, 845, 788},
        {0, 1, 2, 3, 4, 477, 433, 92, 830, 747, 730, 823, 842, 833, 820, 845, 788,},
        {0, 1, 2, 3, 4, 477, 92, 830, 886, 730, 823, 842, 833, 820, 845, 788},
        {0, 1, 2, 3, 4, 477, 433, 92, 830, 747, 730, 823, 842, 833, 820, 845, 788, 566, 565, 569, 568, 570, 572, 571, 567},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 477, 889, 887, 888, 92, 830, 886, 730, 823, 842, 833, 820, 845, 788},
        // Tools
        {0, 1, 2, 3, 4, 977, 978, 979, 980, 983, 984, 985, 986},
        {0, 1, 2, 3, 4, 910, 981, 982, 917, 953, 987, 988, 968},
        // FX_ALG_MIX
        {0, 1, 2},
        // FX_ALG_FX_EMPTY
        {0, 1, 2, 3, 4},
        // FX_ALG_AUX_EMPTY
        {0, 1, 2},
        // FX_ALG_IN (not used,because variable lenght)
        {0}, {0}, {0}, {0}, {0}, {0}, {0}, {0},
        };
            
    static final int[][] PAR_MAP = new int[][] { // remapping fxparam
        {0},
        // Reverb (1-15)
        {0, 1, 2, 3, 4, 5, 6, 7, 9, 10, 11, 14, 20, 21, 22}, // 1 -> 1
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 16, 17, 18, 22, 23, 24, 25, 26, 28, 29, 30, 34, 35, 36},
        {0, 1, 2, 3, 4, 5, 6, 7, 9, 10, 11, 14, 20, 21, 22, 25, 26, 31, 32, 33, 34},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 33, 34, 35, 36},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 33, 34, 35, 36},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 33, 34, 35, 36},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 33, 34, 35, 36},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 13, 14, 17, 18, 19, 20, 21, 22, 23, 24},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 13, 14, 15, 17, 18, 19, 20, 21, 22, 23, 24},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 13, 14, 17, 18, 19, 21, 22, 23, 24, 28, 29, 30, 33, 34, 35, 36},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 13, 14, 15, 17, 18, 19, 21, 22, 23, 24, 28, 29, 30, 33, 34, 35, 36},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 15, 21, 22},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 15, 16, 17, 18, 21, 22},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 15, 21},
        {0, 1, 2, 3, 4, 5, 6, 8, 9, 15, 16, 21, 22, 26, 27, 28, 33, 34, 35},
        // Delay 130 -> 16
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 16, 17, 18, 19, 20, 22, 23, 24, 28, 29, 30, 34, 35, 36, 40, 41, 42},
        {0, 1, 2, 3, 4, 5, 6, 8, 9, 11, 12, 13, 14, 15, 16, 17, 18, 19, 21, 22, 23, 24, 27, 28, 29, 30, 33, 34, 35, 36},
        {0, 1, 2, 3, 4, 5, 6, 8, 9, 10, 11, 12, 14, 15, 16, 17, 18, 27, 28, 29, 30, 33, 34, 35, 36},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 13, 14, 15, 16, 17, 18, 19, 21, 22, 23, 24, 27, 28, 29, 30, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 14, 15, 16, 17, 18, 21, 22, 23, 24, 27, 28, 29, 30, 33, 34, 35, 36, 39, 40, 41, 42, 45, 46, 47, 48},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 43, 44, 45, 46},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36},
        {0, 1, 2, 3, 4, 5, 7, 9, 11, 13, 14, 15, 16, 17, 18},
        // Chorus / Flange / Phase 150 -> 26
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 12, 15, 16, 21, 22, 23},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 12, 15, 16, 17, 21, 22, 23, 25, 26, 27, 28, 29, 30, 31, 32,33},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 27, 28,},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 41, 42, 43, 44, 45, 47, 48},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 15, 16, 19, 21, 22, 25, 26, 27, 28, 29, 30, 33, 34, 35, 36},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 33, 34, 35, 36, 39, 40, 41, 42, 45, 46, 47, 48},
        {0, 1, 2, 3, 4, 5, 6, 9, 14, 15, 16, 17, 20, 21, 22, 23, 24},
        {0, 1, 2, 3, 4, 5, 7, 8, 11, 12},
        {0, 1, 2, 3, 4, 5, 6, 7, 9, 10, 11},
        {0, 1, 2, 3, 4, 5, 6, 7, 9, 14, 15, 16, 20, 21, 23},
        {0, 1, 2, 3, 4, 5, 6, 9, 14, 16, 17, 18, 20, 22, 23, 24},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12},
        // Combination 700 -> 38
        {0, 1, 2, 3, 4, 5, 6, 7, 9, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 24, 25, 27, 28, 29, 31, 33, 34, 35}, 
        {0, 1, 2, 3, 4, 5, 6, 7, 9, 13, 14, 15, 16, 17, 19, 20, 21, 22, 31, 32, 34, 35, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 25, 27, 28, 31, 33, 34, 37, 38, 40, 41, 42, 43, 44, 46, 47, 48},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 28, 29, 30, 34, 35, 36},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36},
        {0, 1, 2, 3, 4, 5, 6, 7, 9, 13, 14, 15, 16, 17, 18, 21, 22, 23, 24, 25, 26, 28, 29, 37, 39, 40, 41, 43, 45, 46, 47},
        {0, 1, 2, 3, 4, 5, 6, 7, 9, 13, 14, 15, 16, 17, 18, 21, 22, 23, 24, 31, 32, 34, 35, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 21, 22, 23, 24, 25, 27, 28, 31, 33, 34, 35, 37, 38, 40, 41, 42, 43, 44, 46, 47, 48},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 27, 28, 29, 30, 37, 38, 40, 41, 42, 46, 47, 48},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 27, 28, 29, 30, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 32, 33, 34, 35},
        {0, 1, 2, 3, 4, 5, 6, 7, 9, 10, 11, 12, 14, 15, 16, 18, 20, 22, 24, 27, 28, 33, 34, 35, 36},
        {0, 1, 2, 3, 4, 5, 6, 7, 9, 10, 11, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 17, 19, 20, 21, 23, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 23, 24, 25, 26, 28, 29, 30, 34, 35, 36},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 12, 14, 15, 16, 17, 25, 26, 28, 29, 30, 34, 35, 36},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 15, 21, 22, 23, 25, 26, 27, 28, 31, 32, 33, 34, 37, 38, 40, 41, 42, 43, 44, 45},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 15, 16, 17, 21, 22, 23, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 15, 16, 17, 21, 22, 23, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 14, 15, 17, 18, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 40, 41, 43, 46, 47, 48},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 23, 24, 25, 27, 28, 29, 30, 31, 33, 34, 35, 36, 37, 38, 40, 41, 43, 46, 47, 48},
        // Distortion 724 -> 62
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9},
        {0, 1, 2, 3, 4, 5, 6, 7, 9, 10, 11, 12},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 13, 14, 16, 17, 18, 19, 20, 22, 23, 24},
        {0, 1, 2, 3, 4, 5, 6, 9, 15, 16, 17, 18, 21, 22, 23, 24, 25, 27, 28, 29, 30, 33, 34, 35, 36, 37, 38, 40, 41, 42, 43, 44, 46, 47, 48},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 12, 13, 14, 16, 17, 18, 19, 20},
        {0, 1, 2, 3, 4, 5, 6, 9, 15, 16, 17, 19, 20, 22, 23, 24, 26, 27, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 48},
        {0, 1, 2, 3, 4, 5, 6, 9, 15, 16, 17, 19, 20, 22, 23, 24, 26, 27, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48},
        {0, 1, 2, 3, 4, 5, 6, 9, 15, 16, 17, 19, 20, 22, 23, 24, 26, 27, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 48},
        {0, 1, 2, 3, 4, 5, 6, 9, 15, 16, 17, 19, 20, 22, 23, 24, 26, 27, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48},
        // Tone Wheel Organ 733 -> 71
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 12, 13, 14, 15, 16, 17, 18, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 38, 39, 40, 42, 44, 45, 46},
        {0, 1, 2, 3, 4, 5, 6, 7, 9, 10, 11, 13, 14, 15, 16, 17, 19, 20, 21, 22, 23, 27, 28, 29, 31, 33, 34, 35},
        {0, 1, 2, 3, 4, 5, 6, 7, 9, 10, 11, 13, 15, 16, 17, 21, 22, 23},
        {0, 1, 2, 3, 4, 5, 9, 14, 15, 16, 17, 18, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 38, 39, 40, 42, 44, 45, 46},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 38, 39, 40, 42, 44, 45, 46},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 14, 15, 16, 17, 18, 25, 26, 27, 31, 32, 33, 38, 39, 40, 42},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 15, 16, 17, 21, 22, 23, 24, 25, 26, 28, 29, 32, 35, 44, 45, 46, 48},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 13, 14, 15, 16, 17, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 38, 39, 40, 42, 44, 45, 46},
        {0, 1, 2, 3, 4, 5, 9, 12, 14, 15, 16, 17, 18, 25, 26, 27, 31, 32, 33, 38, 39, 40, 42},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 15, 16, 17, 21, 22, 23, 25, 26, 28, 29, 32, 35, 44, 45, 46, 48},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 14, 15, 16},
        {0, 1, 2, 3, 4, 5, 6, 7, 9, 13, 14, 15, 16, 17, 18, 25, 26, 27, 28, 29, 30, 34, 35, 36},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 15, 21, 22, 23, 25, 26, 27, 28, 31, 32, 33, 34},
        // New 781 -> 85
        {0, 1, 2, 3, 4, 5, 6, 7, 9, 13, 14, 15, 16, 17, 18, 20, 21, 22, 24, 25, 27, 28, 29, 31, 33, 34, 35},
        {0, 1, 2, 3, 4, 5, 6, 7, 9, 13, 14, 15, 16, 17, 18, 24, 25, 26, 28, 29, 37, 39, 40, 41, 43, 45, 46, 47},
        {0, 1, 2, 3, 4, 5, 6, 9, 10, 11, 12, 13, 14, 19, 20, 21, 22, 25, 26, 27, 28, 31, 32, 33, 37, 38, 40, 41, 42, 43, 44, 46, 49, 50, 51, 52, 53, 54, 58, 59, 60},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 13, 14, 19, 20, 21, 22, 27, 28, 29, 33, 34, 35},
        // Special FX 900 -> 89
        {0, 1, 2, 3, 4, 5, 6, 9, 10, 11, 12, 13, 19, 20, 21},
        {0, 1, 2, 3, 4, 5, 6, 9, 10, 11, 12, 13, 14, 19, 20, 21},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 19, 20, 21},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9},
        {0, 1, 2, 3, 4, 5, 6, 9, 10, 15, 17, 18, 21, 23, 24},
        {0, 1, 2, 3, 4, 5, 6, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36},
        {0, 1, 2, 3, 4, 5, 6, 8, 9, 10, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36},
        {0, 1, 2, 3, 4, 5, 7, 9, 11, 12, 14, 16, 17, 18, 20, 27, 28, 29, 30, 33, 34, 35, 36},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12},
        {0, 1, 2, 3, 4, 5, 6, 9, 10, 11},
        {0, 1, 2, 3, 4, 5, 7, 8, 9, 13, 14, 15, 16, 17, 18, 19, 20, 21},
        {0, 1, 2, 3, 4, 5, 6, 8, 9, 10, 15, 16, 17, 21},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 15, 16, 17, 21},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 15, 16, 17, 21},
        {0, 1, 2, 3, 4, 5, 6, 9, 10, 15, 16, 17, 21},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 15, 16, 17, 21, 25, 26, 31, 32, 33, 34},
        {0, 1, 2, 3, 4, 5, 7, 8, 9, 11, 12, 13, 15, 16, 17, 18, 22, 24},
        {0, 1, 2, 3, 4, 5, 8, 9, 13, 14, 16, 17, 22, 23},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 16, 17, 22, 23},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 19, 20, 21},
        {0, 1, 2, 3, 4, 5, 7, 8, 9, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22},
        // Studio / Mixdown FX 950 -> 112
        {0, 1, 2, 3, 4, 5, 7, 9, 11, 12, 13, 14, 15, 16, 19, 20, 21},
        {0, 1, 2, 3, 4, 5, 7, 9, 11, 12, 13, 14, 15, 16, 19, 20, 21},
        {0, 1, 2, 3, 4, 5, 9, 11, 12, 13, 14, 15, 16, 19, 20, 21},
        {0, 1, 2, 3, 4, 5, 7, 9, 11, 12, 13, 14, 15, 16, 19, 20, 21, 25, 26, 28, 29, 30, 31, 32, 34},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 13, 14, 15, 16, 19, 20, 21},
        // Tools
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12},
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12},
        // FX_ALG_MIX
        {0, 1, 2},
        // FX_ALG_FX_EMPTY
        {0, 1, 2, 3, 4},
        // FX_ALG_AUX_EMPTY
        {0, 1, 2},
        // FX_ALG_IN (not used,because variable lenght)
        {0}, {0}, {0}, {0}, {0}, {0}, {0}, {0},
        };
            
    KurzweilK2600 synth;
        
    public FxAlgorithm(KurzweilK2600 synth) { this.synth = synth; }
        
    public int getType(int typidx)
        {
        return PAR_FX_TYPE[typidx];
        }
        
    public int[] getParfxidx(int algno, int bus)
        {
        int i = 0;
        if (bus == FxStudio.FX_BUS_AUX)
            { // skip Aux Lvl and Aux Bal
            if ( (algno > 0) && (algno != FxStudio.FX_ALG_AUX_EMPTY) )
                {
                int intsel[] = new int[PAR_FX_IDX[algno].length - 2];
                for (i = 0; i < 3; i++)
                    {
                    intsel[i] = PAR_FX_IDX[algno][i];
                    }
                for (i = 5; i < PAR_FX_IDX[algno].length; i++)
                    {
                    intsel[i - 2] = PAR_FX_IDX[algno][i];
                    }
                return intsel;
                }
            else
                {
                return PAR_FX_IDX[FxStudio.FX_ALG_AUX_EMPTY];
                }
            }
        else
            {
            if (algno >= FxStudio.FX_ALG_IN) return (synth.fxStudio.getParfxidxin(algno - FxStudio.FX_ALG_IN));
            else return PAR_FX_IDX[algno];
            }
        }
        
    public int[] getParmap(int algno, int bus)
        {
        int i = 0;
        if (bus == FxStudio.FX_BUS_AUX)
            { // skip Aux Lvl and Aux Bal
            if ( (algno > 0) && (algno != FxStudio.FX_ALG_AUX_EMPTY) )
                {
                int intmap[] = new int[PAR_MAP[algno].length - 2];
                for (i = 0; i < 3; i++)
                    {
                    intmap[i] = PAR_MAP[algno][i];
                    }
                for (i = 5; i < PAR_MAP[algno].length; i++)
                    {
                    intmap[i - 2] = PAR_MAP[algno][i];
                    }
                return intmap;
                }
            else
                {
                return PAR_MAP[FxStudio.FX_ALG_AUX_EMPTY];
                }
            }
        else
            {
            if (algno >= FxStudio.FX_ALG_IN) return (synth.fxStudio.getParmapin(algno - FxStudio.FX_ALG_IN));
            else return PAR_MAP[algno];
            }
        }
        
    public String[] getParfxstr()
        {
        return PAR_FX_STR;
        }
    }
    
