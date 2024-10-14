package edisyn.synth.novationastation;

import edisyn.Model;

import java.util.List;

/**
 * interface defining conversion of data between
 * <ul>
 *     <li>(edisyn) model</li>
 *     <li>(Midi) CC</li>
 *     <li>(Midi-SysEx) byteIndex</li>
 * </ul>
 *
 * dev note: useful to add validation on values in different implementations ? (ref Restrictions set in the model)
 */
interface Convertor {
    // update model for a given value (coming from CC or byteIndex)
    void toModel(Model model, int value);

    // extract value from model (to be used in CC or byteIndex).
    int toSynth(Model model);

    // get (Midi-SysEx) byteIndex
    int getByteIndex();

    // get (Midi) CC number
    Integer getCC();

    // get (Midi) NRPN number
    Integer getNRPN();

    // get restrictions
    Restrictions getRestrictions();
}
