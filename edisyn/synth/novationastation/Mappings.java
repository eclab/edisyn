package edisyn.synth.novationastation;

import edisyn.Model;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Enumeration containing all available parameters and their so-called 'mappings'.
 * <p>
 * Each and every entry is defining (mapping between)
 * <ul>
 * <li>(edisyn) model key</li>
 * <li>midi CC</li>
 * <li>byte-index in (sysex) program dump</li>
 * </ul>
 *
 * <p>
 * Note we have two kind of mappings:
 * <ol>
 *     <li>the 'straight' mappings: one-to-one relationship between (edisyn) model key and midi CC / byte-index</li>
 *     <li>the 'packed' mappings: one-to-many relationship between (edisyn) model key and midi CC / byte-index</li>
 * </ol>
 * </p>
 */
public enum Mappings {
    // CC0 -> NA
    // CC1 -> modwheel (receive only)
    // CC2 -> breath control (receive only)
    ARP_PATTERN(                  3,   87, "arppattern"),
    // CC4 -> NA
    PROGRAM_PORTAMENTO(           5,    3, "portamentotime"),
    // CC6 -> NRPN
    // CC7: Device volume (non-patch related; not stored within patch)
    DEVICE_VOLUME(                7, null, "devicevolume"),
    // CC8, 9, -> TODO
    PAN_POSITION(                10,  117, "panposition"),
    // CC11 -> NA
    PANNING_RATE_NON_SYNC(       12,  118, "panningratenonsync"),
    PANNING_RATE_SYNC(           13,  119, "panningratesync"),
    VOCODER_STEREO_WIDTH(        14,   90, "vocoderstereowidth"),
    VOCODER_SIBILANCE_LEVEL(     15,   91, "vocodersibilancelevel"),
    DISTORTION_MODWHEEL(         16,   98, "distortionmodheel"),
    DISTORTION_COMPENSATION(     17,   99, "distortioncompensation"),
    DELAY_SEND_MODWHEEL(         18,  101, "delaysendmodheel"),
    DELAY_TIME_NON_SYNC(         19,  102, "delaytimenonsync"),
    DELAY_TIME_SYNC(             20,  103, "delaytimesync"),
    DELAY_FEEDBACK(              21,  104, "delayfeedback"),
    DELAY_STEREO_WIDTH(          22,  105, "delaystereowidth"),
    DELAY_RATIO(                 23,  106, "delayratio"),
    REVERB_SEND_MODWHEEL(        24,  108, "reverbsendmodheel"),
    REVERB_DECAY(                25,  109, "reverbdecay"),
    CHORUS_SEND_MODWHEEL(        26,  111, "chorussendmodheel"),
    CHORUS_RATE_NON_SYNC(        27,  112, "chorusratenonsync"),
    CHORUS_RATE_SYNC(            28,  113, "chorusratesync"),
    CHORUS_FEEDBACK(             29,  114, "chorusfeedback"),
    CHORUS_MOD_DEPTH(            30,  115, "chorusmoddepth"),
    CHORUS_MOD_CENTRE_POINT(     31,  116, "chorusmodcentrepoint"),
    // CC32 -> TODO bank select
    // CCxx -> TODO equalizer controls
    OSC1_SEMITONE(               40,    7, "osc1semitone"),
    OSC1_DETUNE(                 41,    8, "osc1detune"),
    OSC1_BENDWHEEL_AMOUNT(       42,    9, "osc1bendwheelamount"),
    OSC1_LFO1_DEPTH(             43,   10, "osc1lfo1depth"),
    OSC1_MOD_ENV_DEPTH(          44,   11, "osc1modenvdepth"),
    OSC1_PULSE_WIDTH(            45,   12, "osc1pulsewidth"),
    // TODO - check
    OSC2_SEMITONE(               48,   15, "osc2semitone"),
    OSC2_DETUNE(                 49,   16, "osc2detune"),
    OSC2_BENDWHEEL_AMOUNT(       50,   17, "osc2bendwheelamount"),
    OSC2_LFO1_DEPTH(             51,   18, "osc2lfo1depth"),
    OSC2_MOD_ENV_DEPTH(          52,   19, "osc2modenvdepth"),
    OSC2_PULSE_WIDTH(            53,   20, "osc2pulsewidth"),
    // TODO - check
    OSC3_SEMITONE(               56,   23, "osc3semitone"),
    OSC3_DETUNE(                 57,   24, "osc3detune"),
    OSC3_BENDWHEEL_AMOUNT(       58,   25, "osc3bendwheelamount"),
    OSC3_LFO1_DEPTH(             59,   26, "osc3lfo1depth"),
    OSC3_MOD_ENV_DEPTH(          60,   27, "osc3modenvdepth"),
    OSC3_PULSE_WIDTH(            61,   28, "osc3pulsewidth"),
    // TODO - check
    // TODO
    PACKED1(                     65,   80, Convertors.PACKED1),
    // CC66 -> NA
    PACKED2(                     67,    0, Convertors.PACKED2),
    UNISON_DETUNE(               68,    1, "unisondetune"),
    OSCILLATORS_RANDOM_DETUNE(   69,    2, "oscillatorrandomdetune"),
    PACKED3(                     70,    5, Convertors.PACKED3),
    PACKED4(                     71,    6, Convertors.PACKED4),
    MIXER_OSC1(                  72,   37, "osc1level"),
    MIXER_OSC2(                  73,   38, "osc2level"),
    MIXER_OSC3(                  74,   39, "osc3level"),
    MIXER_NOISE(                 75,   40, "noiselevel"),
    MIXER_RING_MOD(              76,   41, "ringmodulatorlevel"),
    MIXER_EXTERNAL(              77,   42, "externalinputlevel"),
    PACKED5(                     78,   78, Convertors.PACKED5),
    PACKED6(                     79,   79, Convertors.PACKED6),
    LFO1_SPEED_NON_SYNC(         80,   72, "lfo1speednonsync"),
    LFO1_SPEED_SYNC(             81,   73, "lfo1speedsync"),
    LFO1_DELAY(                  82,   74, "lfo1delay"),
    LFO2_SPEED_NON_SYNC(         83,   75, "lfo2speednonsync"),
    LFO2_SPEED_SYNC(             84,   76, "lfo2speedsync"),
    LFO2_DELAY(                  85,   77, "lfo2delay"),
    // CC86 -> NA
    ARP_SYNC(                    87,   85, "arpsync"),
    ARP_GATE_TIME(               88,   86, "arpgatetime"),
    PACKED7(                     89,   88, Convertors.PACKED7),
    // CC90 -> NA
    REVERB_SEND_LEVEL(           91,  107, "reverbsendlevel"),
    DELAY_SEND_LEVEL(            92,  100, "delaysendlevel"),
    CHORUS_SEND_LEVEL(           93,  110, "chorussendlevel"),
    PANNING_MOD_DEPTH(           94,  120, "panningmoddepth"),
    VOCODER_BALANCE(             95,   89, "vocoderbalance"),
    // CC96, 97-> NA
    // CC98 NRPN number
    // CC99, CC100, CC101-> NA
    FILTER_LFO2_DEPTH(          102,   51, "filterlfo2depth"),
    FILTER_Q_NORMALIZE(         103,   45, "filterqnormalize"),
    FILTER_OVERDRIVE(           104,   43, "filteroverdrive"),
    FILTER_FREQ(                105,   46, "filterfrequency"),
    FILTER_RESONANCE(           106,   44, "filterresonance"),
    FILTER_MOD_ENV_DEPTH(       107,   52, "filtermodenvdepth"),
    ENVELOPE_AMP_ATTACK(        108,   62, "amplitudeenvelopeattack"),
    ENVELOPE_AMP_DECAY(         109,   63, "amplitudeenvelopedecay"),
    ENVELOPE_AMP_SUSTAIN(       110,   64, "amplitudeenvelopesustain"),
    ENVELOPE_AMP_RELEASE(       111,   65, "amplitudeenveloperelease"),
    ENVELOPE_AMP_VELOCITY_DEPTH(112,   61, "amplitudeenvelopevelocitydepth"),
    // CC113 -> NA
    ENVELOPE_MOD_ATTACK(        114,   67, "modulationenvelopeattack"),
    ENVELOPE_MOD_DECAY(         115,   68, "modulationenvelopedecay"),
    ENVELOPE_MOD_SUSTAIN(       116,   69, "modulationenvelopesustain"),
    ENVELOPE_MOD_RELEASE(       117,   70, "modulationenveloperelease"),
    ENVELOPE_MOD_VELOCITY_DEPTH(118,   66, "modulationenvelopevelocitydepth"),
    // program volume - stored in patch
    PROGRAM_VOLUME(             119,  125, "programvolume")
    // some standard midi msgs next
    // TODO - handle NRPNS !
    ;

    private static final Integer NA = null;
    private static final Map<Integer, Mappings> BY_CC = buildCCMap();
    private static final Map<String, Mappings> BY_KEY = buildKeyMap();
    private static final Map<Integer, Mappings> BY_INDEX = buildIndexMap();

    private static Map<Integer, Mappings> buildCCMap() {
        return Stream.of(Mappings.values()).collect(Collectors.toUnmodifiableMap(Mappings::getCC, Function.identity()));
    }

    private static Map<String, Mappings> buildKeyMap() {
        Map<String, Mappings> result = new HashMap<>();
        Stream.of(Mappings.values()).forEach(mapping -> {
            mapping.keys.forEach(k -> result.put(k, mapping));
        });
        return Collections.unmodifiableMap(result);
    }

    private static Map<Integer, Mappings> buildIndexMap() {
        return Stream.of(Mappings.values())
                .filter(m -> m.getDumpIndex() != null)
                .collect(Collectors.toUnmodifiableMap(Mappings::getDumpIndex, Function.identity()));
    }

    // (MIDI) CC number
    private final int cc;
    private final Integer dumpIndex;
    // key(s) as used in the Synth Model, typically containing single element, will contain multiple elements for packed parameters
    private final Set<String> keys;
    // Convertor
    private final PackedConvertor packedConvertor;

    // "straight": cc-to-key is one-to-one
    Mappings(int cc, Integer dumpIndex, String key) {
        this.cc = cc;
        this.keys = Set.of(key);
        this.packedConvertor = null;
        this.dumpIndex = dumpIndex;
    };

    // "packed": cc-to-key is one-to-many
    Mappings(int cc, Integer dumpIndex, PackedConvertor packedConvertor) {
        this.cc = cc;
        this.packedConvertor = packedConvertor;
        this.keys = packedConvertor.getKeys();
        this.dumpIndex = dumpIndex;
    };

    public int getCC() {
        return cc;
    }

    public Integer getDumpIndex() {
        return dumpIndex;
    }

    // update model, for this mapping entry, with a given value
    void toModel(Model model, int value) {
        if (packedConvertor == null) {
            // straight conversion, single key assumed
            String key = keys.iterator().next();
            model.set(key, value);
        } else {
            // packed conversion, delegate to appropriate convertor
            packedConvertor.toModel(model, value);
        }
    }

    // extract, from the model, the value to be sent to the actual synth/device.
    public int toSynth(Model model) {
        if (packedConvertor == null) {
            // straight conversion, single key assumed
            String key = keys.iterator().next();
            return model.get(key);
        } else {
            return packedConvertor.toSynth(model);
        }
    }

    // get mapping by (model) key, Optional.empty if none
    static Optional<Mappings> getByKey(String key) {
        return BY_KEY.containsKey(key) ? Optional.of(BY_KEY.get(key)) : Optional.empty();
    }

    // get mapping by (midi) cc, Optional.empty if none
    static Optional<Mappings> getByCC(int cc) {
        return BY_CC.containsKey(cc) ? Optional.of(BY_CC.get(cc)) : Optional.empty();
    }

    // get mapping by (sysex datadump) index, Optional.empty if none
    static Optional<Mappings> getByIndex(int cc) {
        return BY_INDEX.containsKey(cc) ? Optional.of(BY_INDEX.get(cc)) : Optional.empty();
    }

    /**
     * Definition of available Convertors;
     * Responsible for converting (single/multiple) values from the model from/to single Midi CC
     */
    private enum Convertors implements PackedConvertor {
        PACKED1 {
            @Override
            public Set<String> getKeys() {
                return Set.of("amplitudeenvelopetrigger", "modulationenvelopetrigger", "fmenvelopetrigger", "keysyncphase");
            }
            @Override
            public void toModel(Model model, int value) {
                model.set("amplitudeenvelopetrigger", 0x1 & value);
                model.set("modulationenvelopetrigger", 0x1 & (value >> 1));
                model.set("fmenvelopetrigger", 0x1 & (value >> 2));
                model.set("keysyncphase", 0xF & (value >> 3));
            }
            @Override
            public int toSynth(Model model) {
                return (0x1 & model.get("amplitudeenvelopetrigger"))
                        | ((0x1 & model.get("modulationenvelopetrigger")) << 1)
                        | ((0x1 & model.get("fmenvelopetrigger")) << 2)
                        | ((0xF & model.get("keysyncphase")) << 3);
            }
        },
        PACKED2 {
            @Override
            public Set<String> getKeys() {
                return Set.of("unisonvoices", "unisonpolyphonymode", "filtertype");
            }
            @Override
            public void toModel(Model model, int value) {
                model.set("unisonvoices", 0x7 & value);
                model.set("polyphonymode", 0x3 & (value >> 3));
                model.set("filtertype", 0x1 & (value >> 5));
            }
            @Override
            public int toSynth(Model model) {
                return (0x1 & model.get("amplitudeenvelopetrigger"))
                        | ((0x1 & model.get("modulationenvelopetrigger")) << 1)
                        | ((0x1 & model.get("fmenvelopetrigger")) << 2)
                        | ((0xF & model.get("keysyncphase")) << 3);
            }
        },

        PACKED3 {
            @Override
            public Set<String> getKeys() {
                return Set.of("osc1waveform", "osc2waveform", "osc3waveform", "portamentomode");
            }
            @Override
            public void toModel(Model model, int value) {
                model.set("osc1waveform", 0x3 & value);
                model.set("osc2waveform", 0x3 & (value >> 2));
                model.set("osc3waveform", 0x3 & (value >> 4));
                model.set("portamentomode", 0x1 & (value >> 6));
            }
            @Override
            public int toSynth(Model model) {
                return (0x3 & model.get("osc1waveform"))
                        | ((0x3 & model.get("osc2waveform")) << 2)
                        | ((0x3 & model.get("osc3waveform")) << 4)
                        | ((0x1 & model.get("portamentomode")) << 6);
            }
        },
        PACKED4 {
            @Override
            public Set<String> getKeys() {
                return Set.of("osc1octave", "osc2octave", "osc3octave", "osc1to2sync");
            }
            @Override
            public void toModel(Model model, int value) {
                model.set("osc1octave", 0x3 & value);
                model.set("osc2octave", 0x3 & (value >> 2));
                model.set("osc3octave", 0x3 & (value >> 4));
                model.set("osc1to2sync", 0x1 & (value >> 6));
            }
            @Override
            public int toSynth(Model model) {
                return (0x3 & model.get("osc1octave"))
                        | ((0x3 & model.get("osc2octave")) << 2)
                        | ((0x3 & model.get("osc3octave")) << 4)
                        | ((0x1 & model.get("osc1to2sync")) << 6);
            }
        },
        PACKED5 {
            @Override
            public Set<String> getKeys() {
                return Set.of("lfo1delaymulti", "lfo2delaymulti", "lfo1waveform", "lfo2waveform");
            }
            @Override
            public void toModel(Model model, int value) {
                model.set("lfo1delaymulti", 0x1 & value);
                model.set("lfo2delaymulti", 0x1 & (value >> 1));
                model.set("lfo1waveform", 0x3 & (value >> 2));
                model.set("lfo2waveform", 0x3 & (value >> 4));
            }
            @Override
            public int toSynth(Model model) {
                return (0x1 & model.get("lfo1delaymulti"))
                        | ((0x1 & model.get("lfo2delaymulti")) << 1)
                        | ((0x3 & model.get("lfo1waveform")) << 2)
                        | ((0x3 & model.get("lfo2waveform")) << 4);
            }
        },
        PACKED6 {
            @Override
            public Set<String> getKeys() {
                return Set.of("lfo1keysyncphaseshift", "lfo1keysync", "lfo1lock",
                        "lfo2keysyncphaseshift", "lfo2keysync", "lfo2lock");
            }
            @Override
            public void toModel(Model model, int value) {
                model.set("lfo1keysyncphaseshift", 0x1 & value);
                model.set("lfo1keysync", 0x1 & (value >> 1));
                model.set("lfo1lock", 0x1 & (value >> 2));
                model.set("lfo2keysyncphaseshift", 0x1 & (value >> 3));
                model.set("lfo2keysync", 0x1 & (value >> 4));
                model.set("lfo2lock", 0x1 & (value >> 5));
            }
            @Override
            public int toSynth(Model model) {
                return (0x1 & model.get("lfo1keysyncphaseshift"))
                        | ((0x1 & model.get("lfo1keysync")) << 1)
                        | ((0x1 & model.get("lfo1lock")) << 2)
                        | ((0x1 & model.get("lfo2keysyncphaseshift")) << 3)
                        | ((0x1 & model.get("lfo2keysync")) << 4)
                        | ((0x1 & model.get("lfo1lock")) << 5);
            }
        },
        PACKED7 {
            @Override
            public Set<String> getKeys() {
                return Set.of("arpoctaves", "arponoff", "arpkeysync", "arplatch", "arpnotedestination");
            }
            @Override
            public void toModel(Model model, int value) {
                model.set("arpoctaves", 0x3 & value);
                model.set("arponoff", 0x1 & (value >> 2));
                model.set("arpkeysync", 0x1 & (value >> 3));
                model.set("arplatch", 0x1 & (value >> 4));
                model.set("arpnotedestination", 0x3 & (value >> 5));
            }
            @Override
            public int toSynth(Model model) {
                return (0x3 & model.get("arpoctaves"))
                        | ((0x1 & model.get("arponoff")) << 2)
                        | ((0x1 & model.get("arpkeysync")) << 3)
                        | ((0x1 & model.get("arplatch")) << 4)
                        | ((0x3 & model.get("arpnotedestination")) << 5);
            }
        },
        PACKED8 {
            @Override
            public Set<String> getKeys() {
                return Set.of("reverbtype", "chorusphaser");
            }
            @Override
            public void toModel(Model model, int value) {
                model.set("reverbtype", 0x7 & value);
                model.set("chorusphaser", 0x1 & (value >> 3));
            }
            @Override
            public int toSynth(Model model) {
                return (0x7 & model.get("reverbtype"))
                        | ((0x1 & model.get("chorusphaser")) << 3);
            }
        };
    }

    private interface PackedConvertor {
        // get the keys this instance is handling
        Set<String> getKeys();

        // update model with a given (CC-param) value
        void toModel(Model model, int value);

        // extract value from model, to be sent to the actual synth/device.
        int toSynth(Model model);
    }
}
