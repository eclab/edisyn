package edisyn.synth.kurzweilk2600;
import edisyn.*;
import static edisyn.synth.kurzweilk2600.KurzweilK2600.*;

class FunctionBlock
    { // determine function block type from generic function
    static final String[] FB_STR = new String[] {"Off", "Amp", "Frequency", "Resonance",
        "Amount", "Drive", "Width", "Pitch", "Wrap", "Position", "Crossfade", "Even", "Odd",
        "Separation", "Depth", "Width (PWM)", "Pitch (LF)", "Off"};
        
    // 3 possible blocks per DSP function, triple function has all 3 != OFF, double function has last one OFF, etc.
    static final KurzweilK2600.FB_ENUM[][] FB_INT = new KurzweilK2600.FB_ENUM[][] 
    {
    {FB_ENUM.OFF, FB_ENUM.OFF, FB_ENUM.OFF}, // 0
    {FB_ENUM.AMP, FB_ENUM.OFF, FB_ENUM.OFF}, // AMP
    {FB_ENUM.FRQ, FB_ENUM.RES, FB_ENUM.OFF}, // 2POLE LOWPASS
    {FB_ENUM.FRQ, FB_ENUM.WID, FB_ENUM.OFF}, // BANDPASS FILT
    {FB_ENUM.FRQ, FB_ENUM.WID, FB_ENUM.OFF}, // NOTCH FILTER 
    {FB_ENUM.FRQ, FB_ENUM.WID, FB_ENUM.OFF}, // 2POLE ALLPASS
    {FB_ENUM.OFF, FB_ENUM.OFF, FB_ENUM.OFF}, // 6
    {FB_ENUM.OFF, FB_ENUM.OFF, FB_ENUM.OFF}, // 7
    {FB_ENUM.FRQ, FB_ENUM.AMP, FB_ENUM.OFF}, // PARA BASS
    {FB_ENUM.FRQ, FB_ENUM.AMP, FB_ENUM.OFF}, // PARA TREBLE
    {FB_ENUM.FRQ, FB_ENUM.AMP, FB_ENUM.OFF}, // PARA BASS
    {FB_ENUM.FRQ, FB_ENUM.AMP, FB_ENUM.OFF}, // PARA TREBLE
    {FB_ENUM.FRQ, FB_ENUM.DRV, FB_ENUM.AMP}, // HIFREQ STIMULATOR
    {FB_ENUM.FRQ, FB_ENUM.WID, FB_ENUM.AMP}, // PARAMETRIC EQ 
    {FB_ENUM.FRQ, FB_ENUM.RES, FB_ENUM.AMP}, // STEEP RESONANT BASS
    {FB_ENUM.FRQ, FB_ENUM.OFF, FB_ENUM.OFF}, // LOPASS
    {FB_ENUM.FRQ, FB_ENUM.OFF, FB_ENUM.OFF}, // HIPASS
    {FB_ENUM.FRQ, FB_ENUM.OFF, FB_ENUM.OFF}, // ALPASS
    {FB_ENUM.AMP, FB_ENUM.OFF, FB_ENUM.OFF}, // GAIN
    {FB_ENUM.AMT, FB_ENUM.OFF, FB_ENUM.OFF}, // SHAPER
    {FB_ENUM.DRV, FB_ENUM.OFF, FB_ENUM.OFF}, // DIST
    {FB_ENUM.OFF, FB_ENUM.OFF, FB_ENUM.OFF}, // 21
    {FB_ENUM.WPW, FB_ENUM.OFF, FB_ENUM.OFF}, // PWM
    {FB_ENUM.PCH, FB_ENUM.OFF, FB_ENUM.OFF}, // SINE
    {FB_ENUM.PLF, FB_ENUM.OFF, FB_ENUM.OFF}, // LF SIN
    {FB_ENUM.PCH, FB_ENUM.OFF, FB_ENUM.OFF}, // SW+SHP
    {FB_ENUM.PCH, FB_ENUM.OFF, FB_ENUM.OFF}, // SAW+
    {FB_ENUM.PCH, FB_ENUM.OFF, FB_ENUM.OFF}, // SAW
    {FB_ENUM.PLF, FB_ENUM.OFF, FB_ENUM.OFF}, // LF SAW
    {FB_ENUM.PCH, FB_ENUM.OFF, FB_ENUM.OFF}, // SQUARE
    {FB_ENUM.PLF, FB_ENUM.OFF, FB_ENUM.OFF}, // LF SQR
    {FB_ENUM.WRP, FB_ENUM.OFF, FB_ENUM.OFF}, // WRAP
    {FB_ENUM.OFF, FB_ENUM.OFF, FB_ENUM.OFF}, // 32
    {FB_ENUM.PCH, FB_ENUM.OFF, FB_ENUM.OFF}, // SYNC M
    {FB_ENUM.PCH, FB_ENUM.OFF, FB_ENUM.OFF}, // SYNC S
    {FB_ENUM.FRQ, FB_ENUM.OFF, FB_ENUM.OFF}, // BAND2
    {FB_ENUM.FRQ, FB_ENUM.OFF, FB_ENUM.OFF}, // NOTCH2
    {FB_ENUM.FRQ, FB_ENUM.OFF, FB_ENUM.OFF}, // LOPAS2
    {FB_ENUM.AMP, FB_ENUM.AMP, FB_ENUM.OFF}, // AMP U AMP L
    {FB_ENUM.POS, FB_ENUM.AMP, FB_ENUM.OFF}, // BAL AMP
    {FB_ENUM.POS, FB_ENUM.OFF, FB_ENUM.OFF}, // PANNER
    {FB_ENUM.AMP, FB_ENUM.OFF, FB_ENUM.OFF}, // x GAIN
    {FB_ENUM.AMP, FB_ENUM.OFF, FB_ENUM.OFF}, // + GAIN
    {FB_ENUM.XFD, FB_ENUM.OFF, FB_ENUM.OFF}, // XFADE
    {FB_ENUM.AMP, FB_ENUM.OFF, FB_ENUM.OFF}, // AMPMOD
    {FB_ENUM.OFF, FB_ENUM.OFF, FB_ENUM.OFF}, // 45
    {FB_ENUM.OFF, FB_ENUM.OFF, FB_ENUM.OFF}, // 46
    {FB_ENUM.OFF, FB_ENUM.OFF, FB_ENUM.OFF}, // 47
    {FB_ENUM.AMP, FB_ENUM.OFF, FB_ENUM.OFF}, // x AMP
    {FB_ENUM.AMP, FB_ENUM.OFF, FB_ENUM.OFF}, // + AMP
    {FB_ENUM.FRQ, FB_ENUM.RES, FB_ENUM.SEP}, // 4POLE LOPASS W/SEP 
    {FB_ENUM.FRQ, FB_ENUM.AMP, FB_ENUM.OFF}, // PARA MID
    {FB_ENUM.FRQ, FB_ENUM.OFF, FB_ENUM.OFF}, // HIPAS2
    {FB_ENUM.PCH, FB_ENUM.OFF, FB_ENUM.OFF}, // SW+DIST
    {FB_ENUM.FRQ, FB_ENUM.RES, FB_ENUM.SEP}, // 4POLE HIPASS W/SEP
    {FB_ENUM.FRQ, FB_ENUM.WID, FB_ENUM.SEP}, // TWIN PEAKS BANDPASS 
    {FB_ENUM.FRQ, FB_ENUM.WID, FB_ENUM.SEP}, // DOUBLE NOTCH W/SEP
    {FB_ENUM.FRQ, FB_ENUM.OFF, FB_ENUM.OFF}, // LPGATE
    {FB_ENUM.OFF, FB_ENUM.OFF, FB_ENUM.OFF}, // 58
    {FB_ENUM.OFF, FB_ENUM.OFF, FB_ENUM.OFF}, // 59
    {FB_ENUM.NON, FB_ENUM.OFF, FB_ENUM.OFF}, // NONE 1 function
    {FB_ENUM.NON, FB_ENUM.NON, FB_ENUM.OFF}, // NONE 2 function
    {FB_ENUM.NON, FB_ENUM.NON, FB_ENUM.NON}, // NONE 3 function
    {FB_ENUM.NON, FB_ENUM.OFF, FB_ENUM.OFF}, // NONE 1 function
    {FB_ENUM.EVN, FB_ENUM.ODD, FB_ENUM.OFF}, // 2PARAM SHAPER
    {FB_ENUM.OFF, FB_ENUM.OFF, FB_ENUM.OFF}, // 65
    {FB_ENUM.PCH, FB_ENUM.DEP, FB_ENUM.OFF}, // x SHAPEMOD OSC
    {FB_ENUM.PCH, FB_ENUM.DEP, FB_ENUM.OFF}, // + SHAPEMOD OSC
    {FB_ENUM.PCH, FB_ENUM.DEP, FB_ENUM.OFF}, // SHAPE MOD OSC
    {FB_ENUM.OFF, FB_ENUM.OFF, FB_ENUM.OFF}, // 69
    {FB_ENUM.FRQ, FB_ENUM.OFF, FB_ENUM.OFF}, // LPCLIP
    {FB_ENUM.PCH, FB_ENUM.OFF, FB_ENUM.OFF}, // SINE+
    {FB_ENUM.PCH, FB_ENUM.DEP, FB_ENUM.OFF}, // AMP MOD OSC
    {FB_ENUM.FRQ, FB_ENUM.OFF, FB_ENUM.OFF}, // LP2RES
    {FB_ENUM.AMT, FB_ENUM.OFF, FB_ENUM.OFF}, // SHAPE2
    {FB_ENUM.AMP, FB_ENUM.OFF, FB_ENUM.OFF}, // ! AMP
    {FB_ENUM.AMP, FB_ENUM.OFF, FB_ENUM.OFF}, // NOISE+
    {FB_ENUM.FRQ, FB_ENUM.OFF, FB_ENUM.OFF}, // MASTER
    {FB_ENUM.FRQ, FB_ENUM.OFF, FB_ENUM.OFF}, // SLAVE
    {FB_ENUM.FRQ, FB_ENUM.AMP, FB_ENUM.OFF}, // LOPAS2 GAIN
    {FB_ENUM.AMP, FB_ENUM.OFF, FB_ENUM.OFF}, // 80
    {FB_ENUM.FRQ, FB_ENUM.WID, FB_ENUM.AMP}, // PARAMETRIC EQ
    {FB_ENUM.FRQ, FB_ENUM.RES, FB_ENUM.AMP}, // STEEP RESONANT BASS
    {FB_ENUM.FRQ, FB_ENUM.AMP, FB_ENUM.OFF}, // LOPAS2 GAIN
    {FB_ENUM.FRQ, FB_ENUM.AMP, FB_ENUM.OFF}, // HIPAS2 GAIN
    {FB_ENUM.FRQ, FB_ENUM.AMP, FB_ENUM.OFF}, // BAND2 GAIN
    {FB_ENUM.FRQ, FB_ENUM.AMP, FB_ENUM.OFF}, // NOTCH2 GAIN
    {FB_ENUM.FRQ, FB_ENUM.AMP, FB_ENUM.OFF}, // LP2RES GAIN
    {FB_ENUM.AMT, FB_ENUM.AMP, FB_ENUM.OFF}, // SHAPE2 GAIN
    {FB_ENUM.FRQ, FB_ENUM.AMP, FB_ENUM.OFF}, // LPGATE GAIN
    {FB_ENUM.OFF, FB_ENUM.OFF, FB_ENUM.OFF}, // 90
    {FB_ENUM.OFF, FB_ENUM.OFF, FB_ENUM.OFF}, // 91
    {FB_ENUM.OFF, FB_ENUM.OFF, FB_ENUM.OFF}, // 92
    {FB_ENUM.OFF, FB_ENUM.OFF, FB_ENUM.OFF}, // 93
    {FB_ENUM.OFF, FB_ENUM.OFF, FB_ENUM.OFF}, // 94
    {FB_ENUM.FRQ, FB_ENUM.DRV, FB_ENUM.AMP}, // HIFREQ STIMULATOR
    {FB_ENUM.FRQ, FB_ENUM.WID, FB_ENUM.AMP}, // PARAMETRIC EQ
    {FB_ENUM.FRQ, FB_ENUM.RES, FB_ENUM.AMP}, // STEEP RESONANT BASS
    {FB_ENUM.FRQ, FB_ENUM.RES, FB_ENUM.SEP}, // 4POLE LOPASS W/SEP
    {FB_ENUM.FRQ, FB_ENUM.RES, FB_ENUM.SEP}, // 4POLE HIPASS W/SEP
    {FB_ENUM.FRQ, FB_ENUM.WID, FB_ENUM.SEP}, // TWIN PEAKS BANDPASS 
    {FB_ENUM.FRQ, FB_ENUM.WID, FB_ENUM.SEP}, // DOUBLE NOTCH W/SEP
    {FB_ENUM.AMP, FB_ENUM.OFF, FB_ENUM.OFF}, // AMP
    {FB_ENUM.AMP, FB_ENUM.AMP, FB_ENUM.OFF}, // NOISE+ GAIN
    {FB_ENUM.AMP, FB_ENUM.OFF, FB_ENUM.OFF}, // AMP
    {FB_ENUM.AMP, FB_ENUM.OFF, FB_ENUM.OFF}, // xAMP
    {FB_ENUM.AMP, FB_ENUM.OFF, FB_ENUM.OFF}, // +AMP
    {FB_ENUM.AMP, FB_ENUM.OFF, FB_ENUM.OFF}, // !AMP
    {FB_ENUM.FRQ, FB_ENUM.OFF, FB_ENUM.OFF}, // LP2RES
    {FB_ENUM.AMT, FB_ENUM.OFF, FB_ENUM.OFF}, // SHAPE2
    {FB_ENUM.FRQ, FB_ENUM.OFF, FB_ENUM.OFF}, // BAND20
    {FB_ENUM.FRQ, FB_ENUM.OFF, FB_ENUM.OFF}, // NOTCH2
    {FB_ENUM.FRQ, FB_ENUM.OFF, FB_ENUM.OFF}, // LOPAS2
    {FB_ENUM.FRQ, FB_ENUM.OFF, FB_ENUM.OFF}, // HIPAS2
    {FB_ENUM.FRQ, FB_ENUM.OFF, FB_ENUM.OFF}  // LPGATE
    };
            
    static KurzweilK2600.FB_ENUM[] type = new KurzweilK2600.FB_ENUM[] {FB_ENUM.OFF, FB_ENUM.OFF, FB_ENUM.OFF, FB_ENUM.OFF};
    static boolean[] pad = {false, false, false, false};
        
    KurzweilK2600 synth;
    public FunctionBlock(KurzweilK2600 synth) { this.synth = synth;}
        
    public boolean setType(int layer, int page, int func, int block)
        { // set type and pad in page, from generic function (precondition) and block number
        type[page] = FB_INT[func][block];
        pad[page] = (block == 0) ? true : false; // first block has a pad
        if (block < 2)
            { // next block available ?
            return (FB_INT[func][block + 1] != FB_ENUM.OFF) ? true : false; 
            }
        else
            { // last block reached
            return false;
            }
        }
        
    public KurzweilK2600.FB_ENUM getType(int page)
        {
        return type[page];
        }
        
    public boolean getPad(int fpage, int layer)
        {
        int al = synth.getModel().get("layer" + layer + "calalg");
        if (alg[al - 1].getFunction(1) == fSyncm) 
            { // no pad on sync functions 
            if (al < ALG_LYR_T1) pad[0] = false; // but syncm has a pad in triples
            pad[1] = false;
            } 
        if ( (fpage == 3) && (type[2] == FB_ENUM.POS) ) pad[fpage] = false; // panner in F3 has no pad in F4
        return pad[fpage];
        }
        
    public String getDesc(int type)
        {
        return FB_STR[type];
        }
    } 
