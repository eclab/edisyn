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
 */
interface Convertor {
    // update model for a given value (coming from CC or byteIndex)
    void toModel(Model model, int value);

    // extract value from model (to be used in CC or byteIndex).
    int toSynth(Model model);

    // get (Midi) CC
    Integer getCC();

    // get (Midi-SysEx) byteIndex
    Integer getByteIndex();
}
