package edisyn.synth.kurzweilk2600;
import edisyn.*;
import static edisyn.synth.kurzweilk2600.KurzweilK2600.*;


class OutCom
    {
    static boolean frommodel = false;
        
    KurzweilK2600 synth;
    public OutCom(KurzweilK2600 synth) { this.synth = synth; }
        
    public void updateCommon()
        { // updated from any change in pairu / pairl
        int page = 0;
        boolean prog = false;
            
        frommodel = true;
            
        int pair = synth.getModel().get("layer0pairu");
        for (int layer = 0; layer < synth.getModel().get("numlayers"); layer++)
            {
            int al = synth.getModel().get("layer" + layer + "calalg");
            if ( (al < ALG_LYR_T1) || (al >= ALG_LYR_T3) )
                { // only check single layer and last layer of triple
                al -= 1;
                for (int fpage = 1; fpage <= 4; fpage++)
                    {
                    if (alg[al].getUsepage(fpage))
                        { // check for double output algorithm, panner, bal amp and algorithms 123-126
                        if ( (alg[al].getFunction(fpage) == fPan) || (alg[al].getFunction(fpage) == fA3F3) || (al > 121) )
                            {
                            if (pair != synth.getModel().get("layer" + layer + "pairl")) prog = true;
                            }
                        }
                    }
                if (pair != synth.getModel().get("layer" + layer + "pairu")) prog = true;
                }
            }
        if (prog == true) pair = 4;
        synth.getModel().set("outcom", pair);
        }
        
    public void updateLayers(int pair)
        {
        int page = 0;
            
        frommodel = false;
            
        for (int layer = 0; layer < synth.getModel().get("numlayers"); layer++)
            {
            int al = synth.getModel().get("layer" + layer + "calalg");
            if ( (al < ALG_LYR_T1) || (al >= ALG_LYR_T3) )
                { // only check single layer and last layer of triple
                al -= 1;
                for (int fpage = 1; fpage <= 4; fpage++)
                    {
                    if (alg[al].getUsepage(fpage))
                        { // check for double output algorithm, panner, bal amp and algorithms 123-126
                        if ( (alg[al].getFunction(fpage) == fPan) || (alg[al].getFunction(fpage) == fA3F3) || (al > 121) )
                            {
                            synth.getModel().set("layer" + layer + "pairl", pair);
                            }
                        }
                    }
                synth.getModel().set("layer" + layer + "pairu", pair);
                }
            }
        }
        
    public boolean isuCfrommodel()
        {
        return frommodel;
        }
        
    public void uCnotfrommodel()
        {
        frommodel = false;
        }
        
    public void uCfrommodel()
        {
        frommodel = true;
        }
    }
