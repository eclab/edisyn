package edisyn.synth.kurzweilk2600;
import edisyn.*;
import edisyn.gui.*;

class Function
    {
    int[] intsel;       // list of selection numbers
    int length;         // no. of function blocks
    Chooser[] chooser = new Chooser[KurzweilK2600.MAX_NUM_LAYERS];
    int dfpalg;         // alg no. used in dspfunc parameter
    int fpage;          // page no. used in dspfunc parameter
    String[] strsel;    // list of selection names
        
    // DSP functions with holes
    static final String[] DF_STR = new String[] {"PITCH", "AMP", "2POLE LOWPASS", "BANDPASS FILT",
        "NOTCH FILTER", "2POLE ALLPASS", "", "", 
        "PARA BASS", "PARA TREBLE", "PARA BASS", "PARA TREBLE", "HIFREQ STIMULATOR", "PARAMETRIC EQ",
        "STEEP RESONANT BASS",
        "LOPASS", "HIPASS", "ALPASS", "GAIN", "SHAPER", "DIST", "", 
        "PWM", "SINE", "LF SIN", "SW+SHP", "SAW+", "SAW", "LF SAW", "SQUARE", "LF SQR", "WRAP", "",
        "SYNC M", "SYNC S", "BAND2", "NOTCH2", "LOPAS2", "AMP U                          AMP L",
        "BAL                              AMP",
        "PANNER", "x GAIN", "+ GAIN", "XFADE", "AMPMOD", "", "", "",
        "x AMP", "+ AMP", "4POLE LOPASS W/SEP", "PARA MID", "HIPAS2", "SW+DIST", 
        "4POLE HIPASS W/SEP ", "TWIN PEAKS BANDPASS", "DOUBLE NOTCH W/SEP", "LPGATE", "", "",
        "NONE", "NONE", "NONE", "NONE", "2PARAM SHAPER", "",
        "x SHAPEMOD OSC", "+ SHAPEMOD OSC", "SHAPE MOD OSC", "",
        "LPCLIP", "SINE+", "AMP MOD OSC", "LP2RES", "SHAPE2", "! AMP", "NOISE+", "MASTER", "SLAVE",
        // Triples
        "LOPAS2                           GAIN", "!GAIN", "PARAMETRIC EQ", "STEEP RESONANT BASS",
        "LOPAS2                           GAIN", "HIPAS2                            GAIN",
        "BAND2                            GAIN", "NOTCH2                         GAIN",
        "LP2RES                            GAIN", "SHAPE2                           GAIN",
        "LPGATE                           GAIN",
        "", "", "", "", "",
        "HIFREQ STIMULATOR", "PARAMETRIC EQ", "STEEP RESONANT BASS", "4POLE LOPASS W/SEP", "4POLE HIPASS W/SEP ",
        "TWIN PEAKS BANDPASS", "DOUBLE NOTCH W/SEP", "AMP", "NOISE+                           GAIN",
        "AMP", "x AMP", "+ AMP", "! AMP",
        "LP2RES", "SHAPE2", "BAND2", "NOTCH2", "LOPAS2", "HIPAS2", "LPGATE",
        "" /* this last one is empty for the empty chooser */};
        
    public Function(int[] is, int len, int d, int f)
        {
        intsel = is;
        length = len;
        dfpalg = d;
        fpage = f;
        strsel = new String[intsel.length];
        for (int i =0; i < intsel.length; i++) strsel[i] = DF_STR[intsel[i]];
        }
        
    public int getLen()
        {
        return length;
        }
        
    public int[] getIntsel()
        {
        return intsel;
        }
        
    public Chooser[] getChooser()
        {
        return chooser;
        }
        
    public int getFpage()
        {
        return fpage;
        }
        
    public String[] getStrsel()
        {
        return strsel;
        }
        
    public String getDfparameter()
        {
        return ("f" + fpage + "a" + dfpalg + "dspfunc");
        }
    }
