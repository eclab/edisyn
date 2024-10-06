package edisyn.synth.novationastation;

import edisyn.Model;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static edisyn.synth.novationastation.Mappings.*;

/**
 * Registry of all available Convertors.
 * This registry is build from the <code>Mappings</code> enum
 */
class Convertors {
    // get convertor for a given (Edisyn model) key, Optional.empty if none
    static Optional<Convertor> getByKey(String key) {
        return BY_KEY.containsKey(key) ? Optional.of(BY_KEY.get(key)) : Optional.empty();
    }

    // get convertor for a given (Midi) cc, Optional.empty if none
    static Optional<Convertor> getByCC(int cc) {
        return BY_CC.containsKey(cc) ? Optional.of(BY_CC.get(cc)) : Optional.empty();
    }

    // get convertor for a given (Midi SysEx dataDump) byteIndex, Optional.empty if none
    static Optional<Convertor> getByIndex(int cc) {
        return BY_INDEX.containsKey(cc) ? Optional.of(BY_INDEX.get(cc)) : Optional.empty();
    }

    private static final Map<String, Convertor> BY_KEY = buildKeyMap();
    private static final Map<Integer, Convertor> BY_CC = buildCCMap();
    private static final Map<Integer, Convertor> BY_INDEX = buildIndexMap();

    private static Map<String, Convertor> buildKeyMap() {
        return Stream.of(Mappings.values())
                .collect(Collectors.toUnmodifiableMap(
                        Mappings::getKey,
                        Mappings::getConvertor,
                        (convertor1, convertor2) -> {
                            if (convertor1 != convertor2) {
                                throw new IllegalStateException("Cannot assign different convertors to the same key");
                            }
                            return convertor1;
                        }
                ));
    }

    private static Map<Integer, Convertor> buildCCMap() {
        return Stream.of(Mappings.values())
                .map(Mappings::getConvertor)
                .filter(convertor -> convertor.getCC() != null)
                .collect(Collectors.toUnmodifiableMap(
                        Convertor::getCC,
                        Function.identity(),
                        (convertor1, convertor2) -> {
                            if (convertor1 != convertor2) {
                                throw new IllegalStateException("Cannot assign different convertors to the same key");
                            }
                            return convertor1;
                        }
                ));
    }


    private static Map<Integer, Convertor> buildIndexMap() {
        return Stream.of(Mappings.values())
                .map(Mappings::getConvertor)
                .filter(convertor -> convertor.getByteIndex() != null)
                .collect(Collectors.toUnmodifiableMap(
                        Convertor::getByteIndex,
                        Function.identity(),
                        (convertor1, convertor2) -> {
                            if (convertor1 != convertor2) {
                                throw new IllegalStateException("Cannot assign different convertors to the same key");
                            }
                            return convertor1;
                        }
                ));
    }

    /**
     * enum defining some predefined (so-called packed) convertors
     * These convertors are handling data where different parameters are combined (as in: 'packed') in a single value
     * The definition of these convertors are solely driven by the A-Station specification
     *
     * TODO room for improvement here (refactor code, base class, ...)
     */
    enum Packed implements Convertor {
        PACKED1 {
            @Override
            public Integer getCC() {
                return 65;
            }
            @Override
            public Integer getByteIndex() {
                return 80;
            }
            @Override
            public void toModel(Model model, int value) {
                model.set(ENVELOPE1_TRIGGER.getKey(), 0x1 & value);
                model.set(ENVELOPE2_TRIGGER.getKey(), 0x1 & (value >> 1));
                model.set(ENVELOPE_FM_TRIGGER.getKey(), 0x1 & (value >> 2));
                model.set(KEY_SYNC_PHASE.getKey(), 0xF & (value >> 3));
            }
            @Override
            public int toSynth(Model model) {
                return (0x1 & model.get(ENVELOPE1_TRIGGER.getKey()))
                        | ((0x1 & model.get(ENVELOPE2_TRIGGER.getKey())) << 1)
                        | ((0x1 & model.get(ENVELOPE_FM_TRIGGER.getKey())) << 2)
                        | ((0xF & model.get(KEY_SYNC_PHASE.getKey())) << 3);
            }
        },
        PACKED2 {
            @Override
            public Integer getCC() {
                return 67;
            }
            @Override
            public Integer getByteIndex() {
                return 0;
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
        },
        PACKED3 {
            @Override
            public Integer getCC() {
                return 70;
            }
            @Override
            public Integer getByteIndex() {
                return 5;
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
        },
        PACKED4 {
            @Override
            public Integer getCC() {
                return 71;
            }
            @Override
            public Integer getByteIndex() {
                return 6;
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
        },
        PACKED5 {
            @Override
            public Integer getCC() {
                return 78;
            }
            @Override
            public Integer getByteIndex() {
                return 78;
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
        },
        PACKED6 {
            @Override
            public Integer getCC() {
                return 79;
            }
            @Override
            public Integer getByteIndex() {
                return 79;
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
        },
        PACKED7 {
            @Override
            public Integer getCC() {
                return 89;
            }
            @Override
            public Integer getByteIndex() {
                return 88;
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
        },
        PACKED8 {
            @Override
            public Integer getCC() {
                return null;
            }
            @Override
            public Integer getByteIndex() {
                return 121;
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
        },
        PACKED9 {
            @Override
            public Integer getCC() {
                return null;
            }
            @Override
            public Integer getByteIndex() {
                return 122;
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
        },
        PACKED10 {
            @Override
            public Integer getCC() {
                return null;
            }
            @Override
            public Integer getByteIndex() {
                return 123;
            }
            @Override
            public void toModel(Model model, int value) {
                model.set(VOCODER_SIBILANCE_TYPE.getKey(), 0x4 & value >> 2);
                model.set(EXT_AUDIO_TRIGGER.getKey(), 0x10 & (value >> 4));
                model.set(EXT_AUDIO_TO_FX.getKey(), 0x20 & (value >> 5));
            }
            @Override
            public int toSynth(Model model) {
                return (0x4 & model.get(VOCODER_SIBILANCE_TYPE.getKey()) << 2)
                        | ((0x10 & model.get(EXT_AUDIO_TRIGGER.getKey())) << 4)
                        | ((0x20 & model.get(EXT_AUDIO_TRIGGER.getKey())) << 5);
            }
        };
    }
}
