package edisyn.synth.novationastation;

import edisyn.Model;

import java.util.OptionalInt;

/**
 * interface defining conversion of data between
 * <ul>
 *     <li>(edisyn) model</li>
 *     <li>(MIDI) CC</li>
 *     <li>(MIDI) NRPN</li>
 *     <li>(MIDI-SysEx) byteIndex</li>
 * </ul>
 */
interface Convertor {
    // update model for a given value (coming from CC, NRPN or byteIndex)
    void toModel(Model model, int value);

    // extract value from model (to be used in CC, NRPN or byteIndex).
    int toSynth(Model model);

    // get (MIDI-SysEx) byteIndex
    OptionalInt getByteIndex();

    // get (MIDI) CC number
    OptionalInt getCC();

    // get (MIDI) NRPN number
    OptionalInt getNRPN();

    // get restrictions
    Boundaries getBoundaries();
}
