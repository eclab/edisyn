package edisyn.synth.novationastation;

import edisyn.Model;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static edisyn.synth.novationastation.Mappings.*;

/**
 * Registry of all available Convertors.
 * This registry is (statically) build from the <code>Mappings</code> enum
 *
 * As a side effect of this, it additionally validates the <code>Mappings</code> to ensure there are no double
 * usages defined for each and every(edisyn) model key and (MIDI) CC, NRPN, (sysex) byte index
 *
 * @see Mappings
 *
 * TODO room for code improvement/reorg here (refactor code, introducing base class, generalize implementation...)
 */
class Convertors {
    // get convertor for a given (Edisyn model) key, Optional.empty if none
    static Optional<Convertor> getByKey(String key) {
        return BY_KEY.containsKey(key) ? Optional.of(BY_KEY.get(key)) : Optional.empty();
    }

    // get convertor for a given (MIDI SysEx dataDump) byteIndex, Optional.empty if none
    static Optional<Convertor> getByIndex(int cc) {
        return BY_INDEX.containsKey(cc) ? Optional.of(BY_INDEX.get(cc)) : Optional.empty();
    }

    // get convertor for a given (MIDI) CC, Optional.empty if none
    static Optional<Convertor> getByCC(int cc) {
        return BY_CC.containsKey(cc) ? Optional.of(BY_CC.get(cc)) : Optional.empty();
    }

    // get convertor for a given (MIDI) NRPN, Optional.empty if none
    static Optional<Convertor> getByNRPN(int nrpn) {
        return BY_NRPN.containsKey(nrpn) ? Optional.of(BY_NRPN.get(nrpn)) : Optional.empty();
    }

    private static final Map<String, Convertor> BY_KEY = buildKeyMap();
    private static final Map<Integer, Convertor> BY_INDEX = buildIndexMap();
    private static final Map<Integer, Convertor> BY_CC = buildCCMap();
    private static final Map<Integer, Convertor> BY_NRPN = buildNRPNMap();

    private static Map<String, Convertor> buildKeyMap() {
        return Stream.of(Mappings.values())
                .collect(Collectors.toUnmodifiableMap(
                        Mappings::getKey,
                        Mappings::getConvertor,
                        (convertor1, convertor2) -> {
                            if (convertor1 != convertor2) {
                                throw new IllegalStateException("Cannot assign different convertors to the same key.");
                            }
                            return convertor1;
                        }
                ));
    }

    private static Map<Integer, Convertor> buildIndexMap() {
        return Stream.of(Mappings.values())
                .map(Mappings::getConvertor)
                .filter(convertor -> convertor.getByteIndex().isPresent())
                .collect(Collectors.toUnmodifiableMap(
                        convertor -> convertor.getByteIndex().getAsInt(),
                        Function.identity(),
                        (convertor1, convertor2) -> {
                            if (convertor1 != convertor2) {
                                throw new IllegalStateException("Cannot assign different convertors to the same byteIndex: " + convertor1.getByteIndex());
                            }
                            return convertor1;
                        }
                ));
    }

    private static Map<Integer, Convertor> buildCCMap() {
        return Stream.of(Mappings.values())
                .map(Mappings::getConvertor)
                .filter(convertor -> convertor.getCC().isPresent())
                .collect(Collectors.toUnmodifiableMap(
                        convertor -> convertor.getCC().getAsInt(),
                        Function.identity(),
                        (convertor1, convertor2) -> {
                            if (convertor1 != convertor2) {
                                throw new IllegalStateException("Cannot assign different convertors to the same CC: " + convertor1.getCC());
                            }
                            return convertor1;
                        }
                ));
    }

    private static Map<Integer, Convertor> buildNRPNMap() {
        return Stream.of(Mappings.values())
                .map(Mappings::getConvertor)
                .filter(convertor -> convertor.getNRPN().isPresent())
                .collect(Collectors.toUnmodifiableMap(
                        convertor -> convertor.getNRPN().getAsInt(),
                        Function.identity(),
                        (convertor1, convertor2) -> {
                            if (convertor1 != convertor2) {
                                throw new IllegalStateException("Cannot assign different convertors to the same NRPN: " + convertor1.getNRPN());
                            }
                            return convertor1;
                        }
                ));
    }

    /**
     * enum defining some predefined (so-called packed) convertors
     * These convertors are handling data where different parameters are combined (as in: 'packed') in a single value
     * The definition of these convertors are solely driven by the A-Station specification
     */
    enum Packed implements Convertor {
        PACKED1 {
            @Override
            public OptionalInt getByteIndex() {
                return OptionalInt.of(80);
            }
            @Override
            public OptionalInt getCC() {
                return OptionalInt.of(65);
            }
            @Override
            public OptionalInt getNRPN() {
                return OptionalInt.empty();
            }
            @Override
            public void toModel(Model model, int value) {
                model.set(ENVELOPE1_TRIGGER.getKey(), 0x1 & value);
                model.set(ENVELOPE2_TRIGGER.getKey(), 0x1 & (value >> 1));
                model.set(ENVELOPE3_TRIGGER.getKey(), 0x1 & (value >> 2));
                model.set(KEY_SYNC_PHASE.getKey(), 0xF & (value >> 3));
            }
            @Override
            public int toSynth(Model model) {
                return (0x1 & model.get(ENVELOPE1_TRIGGER.getKey()))
                        | ((0x1 & model.get(ENVELOPE2_TRIGGER.getKey())) << 1)
                        | ((0x1 & model.get(ENVELOPE3_TRIGGER.getKey())) << 2)
                        | ((0xF & model.get(KEY_SYNC_PHASE.getKey())) << 3);
            }
            @Override
            public Boundaries getBoundaries() {
                return Boundaries.NONE;
            }
        },
        PACKED2 {
            @Override
            public OptionalInt getByteIndex() {
                return OptionalInt.of(0);
            }
            @Override
            public OptionalInt getCC() {
                return OptionalInt.of(67);
            }
            @Override
            public OptionalInt getNRPN() {
                return OptionalInt.empty();
            }
            @Override
            public void toModel(Model model, int value) {
                model.set(UNISON_VOICES.getKey(), 0x7 & value);
                model.set(POLYPHONY_MODE.getKey(), 0x3 & (value >> 3));
                model.set(FILTER_TYPE.getKey(), 0x1 & (value >> 5));
            }
            @Override
            public int toSynth(Model model) {
                return (0x7 & model.get(UNISON_VOICES.getKey()))
                        | ((0x3 & model.get(POLYPHONY_MODE.getKey())) << 3)
                        | ((0x1 & model.get(FILTER_TYPE.getKey())) << 5);
            }
            @Override
            public Boundaries getBoundaries() {
                return Boundaries.NONE;
            }
        },
        PACKED3 {
            @Override
            public OptionalInt getByteIndex() {
                return OptionalInt.of(5);
            }
            @Override
            public OptionalInt getCC() {
                return OptionalInt.of(70);
            }
            @Override
            public OptionalInt getNRPN() {
                return OptionalInt.empty();
            }
            @Override
            public void toModel(Model model, int value) {
                model.set(OSC1_WAVEFORM.getKey(), 0x3 & value);
                model.set(OSC2_WAVEFORM.getKey(), 0x3 & (value >> 2));
                model.set(OSC3_WAVEFORM.getKey(), 0x3 & (value >> 4));
                model.set(PORTAMENTO_MODE.getKey(), 0x1 & (value >> 6));
            }
            @Override
            public int toSynth(Model model) {
                return (0x3 & model.get(OSC1_WAVEFORM.getKey()))
                        | ((0x3 & model.get(OSC2_WAVEFORM.getKey())) << 2)
                        | ((0x3 & model.get(OSC3_WAVEFORM.getKey())) << 4)
                        | ((0x1 & model.get(PORTAMENTO_MODE.getKey())) << 6);
            }
            @Override
            public Boundaries getBoundaries() {
                return Boundaries.NONE;
            }
        },
        PACKED4 {
            @Override
            public OptionalInt getByteIndex() {
                return OptionalInt.of(6);
            }
            @Override
            public OptionalInt getCC() {
                return OptionalInt.of(71);
            }
            @Override
            public OptionalInt getNRPN() {
                return OptionalInt.empty();
            }
            @Override
            public void toModel(Model model, int value) {
                model.set(OSC1_OCTAVE.getKey(), 0x3 & value);
                model.set(OSC2_OCTAVE.getKey(), 0x3 & (value >> 2));
                model.set(OSC3_OCTAVE.getKey(), 0x3 & (value >> 4));
                model.set(OSC2_SYNCED_BY_1.getKey(), 0x1 & (value >> 6));
            }
            @Override
            public int toSynth(Model model) {
                return (0x3 & model.get(OSC1_OCTAVE.getKey()))
                        | ((0x3 & model.get(OSC2_OCTAVE.getKey())) << 2)
                        | ((0x3 & model.get(OSC3_OCTAVE.getKey())) << 4)
                        | ((0x1 & model.get(OSC2_SYNCED_BY_1.getKey())) << 6);
            }
            @Override
            public Boundaries getBoundaries() {
                return Boundaries.NONE;
            }
        },
        PACKED5 {
            @Override
            public OptionalInt getByteIndex() {
                return OptionalInt.of(78);
            }
            @Override
            public OptionalInt getCC() {
                return OptionalInt.of(78);
            }
            @Override
            public OptionalInt getNRPN() {
                return OptionalInt.empty();
            }
            @Override
            public void toModel(Model model, int value) {
                model.set(LFO1_DELAY_MULTI.getKey(), 0x1 & value);
                model.set(LFO2_DELAY_MULTI.getKey(), 0x1 & (value >> 1));
                model.set(LFO1_WAVEFORM.getKey(), 0x3 & (value >> 2));
                model.set(LFO2_WAVEFORM.getKey(), 0x3 & (value >> 4));
            }
            @Override
            public int toSynth(Model model) {
                return (0x1 & model.get(LFO1_DELAY_MULTI.getKey()))
                        | ((0x1 & model.get(LFO2_DELAY_MULTI.getKey())) << 1)
                        | ((0x3 & model.get(LFO1_WAVEFORM.getKey())) << 2)
                        | ((0x3 & model.get(LFO2_WAVEFORM.getKey())) << 4);
            }
            @Override
            public Boundaries getBoundaries() {
                return Boundaries.NONE;
            }
        },
        PACKED6 {
            @Override
            public OptionalInt getByteIndex() {
                return OptionalInt.of(79);
            }
            @Override
            public OptionalInt getCC() {
                return OptionalInt.of(79);
            }
            @Override
            public OptionalInt getNRPN() {
                return OptionalInt.empty();
            }
            @Override
            public void toModel(Model model, int value) {
                model.set(LFO1_KEY_SYNC_PHASE_SHIFT.getKey(), 0x1 & value);
                model.set(LFO1_KEY_SYNC.getKey(), 0x1 & (value >> 1));
                model.set(LFO1_LOCK.getKey(), 0x1 & (value >> 2));
                model.set(LFO2_KEY_SYNC_PHASE_SHIFT.getKey(), 0x1 & (value >> 3));
                model.set(LFO2_KEY_SYNC.getKey(), 0x1 & (value >> 4));
                model.set(LFO2_LOCK.getKey(), 0x1 & (value >> 5));
            }
            @Override
            public int toSynth(Model model) {
                return (0x1 & model.get(LFO1_KEY_SYNC_PHASE_SHIFT.getKey()))
                        | ((0x1 & model.get(LFO1_KEY_SYNC.getKey())) << 1)
                        | ((0x1 & model.get(LFO1_LOCK.getKey())) << 2)
                        | ((0x1 & model.get(LFO2_KEY_SYNC_PHASE_SHIFT.getKey())) << 3)
                        | ((0x1 & model.get(LFO2_KEY_SYNC.getKey())) << 4)
                        | ((0x1 & model.get(LFO2_LOCK.getKey())) << 5);
            }
            @Override
            public Boundaries getBoundaries() {
                return Boundaries.NONE;
            }
        },
        PACKED7 {
            @Override
            public OptionalInt getByteIndex() {
                return OptionalInt.of(88);
            }
            @Override
            public OptionalInt getCC() {
                return OptionalInt.of(89);
            }
            @Override
            public OptionalInt getNRPN() {
                return OptionalInt.empty();
            }
            @Override
            public void toModel(Model model, int value) {
                model.set(ARP_OCTAVES.getKey(), 0x3 & value);
                model.set(ARP_ON_OFF.getKey(), 0x1 & (value >> 2));
                model.set(ARP_KEY_SYNC.getKey(), 0x1 & (value >> 3));
                model.set(ARP_LATCH.getKey(), 0x1 & (value >> 4));
                model.set(ARP_NOTE_DESTINATION.getKey(), 0x3 & (value >> 5));
            }
            @Override
            public int toSynth(Model model) {
                return (0x3 & model.get(ARP_OCTAVES.getKey()))
                        | ((0x1 & model.get(ARP_ON_OFF.getKey())) << 2)
                        | ((0x1 & model.get(ARP_KEY_SYNC.getKey())) << 3)
                        | ((0x1 & model.get(ARP_LATCH.getKey())) << 4)
                        | ((0x3 & model.get(ARP_NOTE_DESTINATION.getKey())) << 5);
            }
            @Override
            public Boundaries getBoundaries() {
                return Boundaries.NONE;
            }
        },
        PACKED8 {
            @Override
            public OptionalInt getByteIndex() {
                return OptionalInt.of(121);
            }
            @Override
            public OptionalInt getCC() {
                return OptionalInt.empty();
            }
            @Override
            public OptionalInt getNRPN() {
                return OptionalInt.of(21);
            }
            @Override
            public void toModel(Model model, int value) {
                model.set(REVERB_TYPE.getKey(), 0x7 & value);
                model.set(CHORUS_TYPE.getKey(), 0x1 & (value >> 3));
            }
            @Override
            public int toSynth(Model model) {
                return (0x7 & model.get(REVERB_TYPE.getKey()))
                        | ((0x1 & model.get(CHORUS_TYPE.getKey())) << 3);
            }
            @Override
            public Boundaries getBoundaries() {
                return Boundaries.NONE;
            }
        },
        PACKED9 {
            @Override
            public OptionalInt getByteIndex() {
                return OptionalInt.of(122);
            }
            @Override
            public OptionalInt getCC() {
                return OptionalInt.empty();
            }
            @Override
            public OptionalInt getNRPN() {
                return OptionalInt.of(22);
            }
            @Override
            public void toModel(Model model, int value) {
                model.set(CHORUS_GLOBAL_SYNC.getKey(), 0x3 & value);
                model.set(PANNING_GLOBAL_SYNC.getKey(), 0x3 & (value >> 2));
                model.set(EQUALIZER_GLOBAL_SYNC.getKey(), 0x3 & (value >> 4));
            }
            @Override
            public int toSynth(Model model) {
                return (0x3 & model.get(CHORUS_GLOBAL_SYNC.getKey()))
                        | ((0x3 & model.get(PANNING_GLOBAL_SYNC.getKey())) << 2)
                        | ((0x3 & model.get(EQUALIZER_GLOBAL_SYNC.getKey())) << 4);
            }
            @Override
            public Boundaries getBoundaries() {
                return Boundaries.NONE;
            }
        },
        PACKED10 {
            @Override
            public OptionalInt getByteIndex() {
                return OptionalInt.of(123);
            }
            @Override
            public OptionalInt getCC() {
                return OptionalInt.empty();
            }
            @Override
            public OptionalInt getNRPN() {
                return OptionalInt.of(23);
            }
            @Override
            public void toModel(Model model, int value) {
                // NOTE: unlike documented: bit 4 (iso 3)
                model.set(VOCODER_SIBILANCE_TYPE.getKey(), 0x1 & value >> 3);
                // NOTE: unlike documented: bit 6 (iso 5)
                model.set(EXT_AUDIO_TRIGGER.getKey(), 0x1 & (value >> 5));
                // NOTE: unlike documented: bit 7 (iso 6)
                model.set(EXT_AUDIO_TO_FX.getKey(), 0x1 & (value >> 6));
            }
            @Override
            public int toSynth(Model model) {
                // NOTE: unlike documented: bit 4 (iso 3)
                return ((0x1 & model.get(VOCODER_SIBILANCE_TYPE.getKey())) << 3)
                        // NOTE: unlike documented: bit 6 (iso 5)
                        | ((0x1 & model.get(EXT_AUDIO_TRIGGER.getKey())) << 5)
                        // NOTE: unlike documented: bit 7 (iso 6)
                        | ((0x1 & model.get(EXT_AUDIO_TRIGGER.getKey())) << 6);
            }
            @Override
            public Boundaries getBoundaries() {
                return Boundaries.NONE;
            }
        }, PACKED11 {
            @Override
            public OptionalInt getByteIndex() {
                return OptionalInt.of(127);
            }
            @Override
            public OptionalInt getCC() {
                return OptionalInt.empty();
            }
            @Override
            // NOTE - (undocumented) NRPN 26 used forPACKED 11 !
            public OptionalInt getNRPN() {
                return OptionalInt.of(26);
            }
            @Override
            public void toModel(Model model, int value) {
                model.set(OSC_SELECT.getKey(), 0x3 & value);
                model.set(MIXER_SELECT.getKey(), 0x3 & (value >> 2));
                model.set(PWM_SOURCE.getKey(), 0x3 & (value >> 4));
                model.set(LFO_SELECT.getKey(), 0x1 & (value >> 6));
            }
            @Override
            public int toSynth(Model model) {
                return (0x3 & model.get(OSC_SELECT.getKey()))
                        | ((0x3 & model.get(MIXER_SELECT.getKey())) << 2)
                        | ((0x3 & model.get(PWM_SOURCE.getKey())) << 4)
                        | ((0x1 & model.get(LFO_SELECT.getKey())) << 6);
            }
            @Override
            public Boundaries getBoundaries() {
                return Boundaries.NONE;
            }
        };
    }
}
