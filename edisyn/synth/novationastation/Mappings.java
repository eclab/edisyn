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
    ////
    // Oscillators - global (= applying to all oscillators)
    ////
    PORTAMENTO_TIME("portamentotime",                                5,   3),
    PREGLIDE_SEMITONES("preglidesemitones",                          8,   4),
    UNISON_DETUNE("unisondetune",                                   68,   1),
    OSCS_RANDOM_DETUNE("oscsrandomdetune",                          69,   2),
    OSCS_MODWHEEL_PITCH_DEPTH("oscsmodwheelpitchdepth",           null,  31),
    OSCS_AFTERTCH_PITCH_DEPTH("oscsaftertchpitchdepth",           null,  32),
    OSCS_BREATH_PITCH_DEPTH("oscsbreathpitchdepth",               null,  33),
    OSCS_MODWHEEL_LFO1_PITCH_DEPTH("oscsmodwheellfo1pitchdepth",  null,  34),
    OSCS_AFTERTCH_LFO1_PITCH_DEPTH("oscsaftertchlfo1pitchdepth",  null,  35),
    OSCS_BREATH_LFO1_PITCH_DEPTH("oscsbreathlfo1pitchdepth",      null,  36
    ),
    ////
    // Oscillators - individual
    ////
    OSC1_SEMITONE("osc1semitone",                                   40,   7),
    OSC1_DETUNE("osc1detune",                                       41,   8),
    OSC1_BENDWHEEL_AMOUNT("osc1bendwheelamount",                    42,   9),
    OSC1_LFO1_DEPTH("osc1lfo1depth",                                43,  10),
    OSC1_MOD_ENV_DEPTH("osc1modenvdepth",                           44,  11),
    OSC1_PULSE_WIDTH("osc1pulsewidth",                              45,  12),
    OSC1_LFO2_PULSE_WIDTH_MOD("osc1lfo2pulsewidthmod",              46,  13),
    OSC1_MODENV_PULSE_WIDTH_MOD("osc1modenvpulsewidthmod",          47,  14),
    OSC2_SEMITONE("osc2semitone",                                   48,  15),
    OSC2_DETUNE("osc2detune",                                       49,  16),
    OSC2_BENDWHEEL_AMOUNT("osc2bendwheelamount",                    50,  17),
    OSC2_LFO1_DEPTH("osc2lfo1depth",                                51,  18),
    OSC2_MOD_ENV_DEPTH("osc2modenvdepth",                           52,  19),
    OSC2_PULSE_WIDTH("osc2pulsewidth",                              53,  20),
    OSC2_LFO2_PULSE_WIDTH_MOD("osc2lfo2pulsewidthmod",              54,  21),
    OSC2_MODENV_PULSE_WIDTH_MOD("osc2modenvpulsewidthmod",          55,  22),
    OSC3_SEMITONE("osc3semitone",                                   56,  23),
    OSC3_DETUNE("osc3detune",                                       57,  24),
    OSC3_BENDWHEEL_AMOUNT("osc3bendwheelamount",                    58,  25),
    OSC3_LFO1_DEPTH("osc3lfo1depth",                                59,  26),
    OSC3_MOD_ENV_DEPTH("osc3modenvdepth",                           60,  27),
    OSC3_PULSE_WIDTH("osc3pulsewidth",                              61,  28),
    OSC3_LFO2_PULSE_WIDTH_MOD("osc3lfo2pulsewidthmod",              62,  29),
    OSC3_MODENV_PULSE_WIDTH_MOD("osc3modenvpulsewidthmod",          63,  30),
    ////
    // Envelopes
    ////
    ENVELOPE_AMP_ATTACK("amplitudeenvelopeattack",                 108,  62),
    ENVELOPE_AMP_DECAY("amplitudeenvelopedecay",                   109,  63),
    ENVELOPE_AMP_SUSTAIN("amplitudeenvelopesustain",               110,  64),
    ENVELOPE_AMP_RELEASE("amplitudeenveloperelease",               111,  65),
    ENVELOPE_AMP_VELOCITY_DEPTH("amplitudeenvelopevelocitydepth",  112,  61),
    ENVELOPE_MOD_ATTACK("modulationenvelopeattack",                114,  67),
    ENVELOPE_MOD_DECAY("modulationenvelopedecay",                  115,  68),
    ENVELOPE_MOD_SUSTAIN("modulationenvelopesustain",              116,  69),
    ENVELOPE_MOD_RELEASE("modulationenveloperelease",              117,  70),
    ENVELOPE_MOD_VELOCITY_DEPTH("modulationenvelopevelocitydepth", 118,  66),
    ////
    // LFOs
    ////
    LFO1_SPEED_NON_SYNC("lfo1speednonsync",                         80,  72),
    LFO1_SPEED_SYNC("lfo1speedsync",                                81,  73),
    LFO1_DELAY("lfo1delay",                                         82,  74),
    LFO2_SPEED_NON_SYNC("lfo2speednonsync",                         83,  75),
    LFO2_SPEED_SYNC("lfo2speedsync",                                84,  76),
    LFO2_DELAY("lfo2delay",                                         85,  77),
    ////
    // Filter
    ////
    FILTER_FREQ("filterfrequency",                                 105,  46),
    FILTER_RESONANCE("filterresonance",                            106,  44),
    FILTER_OVERDRIVE("filteroverdrive",                            104,  43),
    // CC6 -> filtertracking (on latest firmware - whilst NRPN according to latest (yet older) spec I could find)
    FILTER_KEY_TRACK("filterkeytrack",                               6,  47),
    FILTER_MOD_ENV_DEPTH("filtermodenvdepth",                      107,  52),
    FILTER_LFO2_DEPTH("filterlfo2depth",                           102,  51),
    FILTER_Q_NORMALIZE("filterqnormalize",                         103,  45),
    ////
    // Mixer
    ////
    MIXER_OSC1("osc1level",                                         72,  37),
    MIXER_OSC2("osc2level",                                         73,  38),
    MIXER_OSC3("osc3level",                                         74,  39),
    MIXER_NOISE("noiselevel",                                       75,  40),
    MIXER_RING_MOD("ringmodulatorlevel",                            76,  41),
    MIXER_EXTERNAL("externalinputlevel",                            77,  42),
    ////
    // ARP
    ////
    ARP_SYNC("arpsync",                                             87,  85),
    ARP_GATE_TIME("arpgatetime",                                    88,  86),
    ARP_PATTERN("arppattern",                                        3,  87),
    ARP_RATE("arprate",                                              9,  84),
    // not sure what this is, different from the one in packed7 ?
    ARP_LATCH_ON_MOMENTARY("arplatchonmomentary",                   64,null),
    ////
    // Effects - equalizer
    ////
    EQUALIZER_LEVEL("equalizerlevel",                               33,  92),
    EQUALIZER_FREQUENCY("equalizerfrequency",                       34,  93),
    EQUALIZER_RATE_NON_SYNC("equalizerratenonsync",                 35,  94),
    EQUALIZER_RATE_SYNC("equalizerratesync",                        36,  95),
    EQUALIZER_MOD_DEPTH("equalizermoddepth",                        37,  96),
    ////
    // Effects - delay
    ////
    DELAY_SEND_LEVEL("delaysendlevel",                              92, 100),
    DELAY_SEND_MODWHEEL("delaysendmodheel",                         18, 101),
    DELAY_TIME_NON_SYNC("delaytimenonsync",                         19, 102),
    DELAY_TIME_SYNC("delaytimesync",                                20, 103),
    DELAY_FEEDBACK("delayfeedback",                                 21, 104),
    DELAY_STEREO_WIDTH("delaystereowidth",                          22, 105),
    DELAY_RATIO("delayratio",                                       23, 106),
    ////
    // Effects - chorus/flanger
    ////
    CHORUS_SEND_LEVEL("chorussendlevel",                            93, 110),
    CHORUS_SEND_MODWHEEL("chorussendmodheel",                       26, 111),
    CHORUS_RATE_NON_SYNC("chorusratenonsync",                       27, 112),
    CHORUS_RATE_SYNC("chorusratesync",                              28, 113),
    CHORUS_FEEDBACK("chorusfeedback",                               29, 114),
    CHORUS_MOD_DEPTH("chorusmoddepth",                              30, 115),
    CHORUS_MOD_CENTRE_POINT("chorusmodcentrepoint",                 31, 116),
    ////
    // Effects - reverb
    ////
    REVERB_SEND_LEVEL("reverbsendlevel",                            91, 107),
    REVERB_SEND_MODWHEEL("reverbsendmodheel",                       24, 108),
    REVERB_DECAY("reverbdecay",                                     25, 109),
    ////
    // Effects - distortion
    ////
    DISTORTION_MODWHEEL("distortionmodheel",                        16,  98),
    DISTORTION_COMPENSATION("distortioncompensation",               17,  99),
    ////
    // Effects - vocoder
    ////
    VOCODER_BALANCE("vocoderbalance",                               95,  89),
    VOCODER_STEREO_WIDTH("vocoderstereowidth",                      14,  90),
    VOCODER_SIBILANCE_LEVEL("vocodersibilancelevel",                15,  91),
    ////
    // Effects - panning
    ////
    PAN_POSITION("panposition",                                     10, 117),
    PANNING_MOD_DEPTH("panningmoddepth",                            94, 120),
    PANNING_RATE_NON_SYNC("panningratenonsync",                     12, 118),
    PANNING_RATE_SYNC("panningratesync",                            13, 119),
    // CC7: Device volume (non-patch related; not stored within patch)
    DEVICE_VOLUME("devicevolume",                                    7,null),
    // program volume - stored in patch
    PROGRAM_VOLUME("programvolume",                                119, 125),

    ////
    // The packed ones (TODO - rework and integrate into functional sections ?)
    ////
    PACKED1(Convertors.PACKED1,                                          65,  80),
    PACKED2(Convertors.PACKED2,                                          67,   0),
    PACKED3(Convertors.PACKED3,                                          70,   5),
    PACKED4(Convertors.PACKED4,                                          71,   6),
    PACKED5(Convertors.PACKED5,                                          78,  78),
    PACKED6(Convertors.PACKED6,                                          79,  79),
    PACKED7(Convertors.PACKED7,                                          89,  88),

    // TODO - handle NRPNS !?

    ////
    // FYI - CC void
    ////
    // CC0 -> NA
    // CC1 -> modwheel (receive only)
    // CC2 -> breath control (receive only)
    // CC4 -> NA
    // CC11 -> NA
    // CC32 -> bank select
    // CC38, 39 -> NA
    // CC66 -> NA
    // CC86 -> NA
    // CC90 -> NA
    // CC96, 97 -> NA
    // CC98 NRPN number
    // CC99, CC100, CC101-> NA
    // CC113 -> NA


    ;

    private static final Map<Integer, Mappings> BY_CC = buildCCMap();
    private static final Map<String, Mappings> BY_KEY = buildKeyMap();
    private static final Map<Integer, Mappings> BY_INDEX = buildIndexMap();

    private static Map<Integer, Mappings> buildCCMap() {
        return Stream.of(Mappings.values())
                .filter(m -> m.getCC() != null)
                .collect(Collectors.toUnmodifiableMap(Mappings::getCC, Function.identity()));
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
    private final Integer cc;
    private final Integer dumpIndex;
    // key(s) as used in the Synth Model, typically containing single element, will contain multiple elements for packed parameters
    private final List<String> keys;
    // Convertor
    private final PackedConvertor packedConvertor;

    // "straight": cc-to-key is one-to-one
    Mappings(String key, Integer cc, Integer dumpIndex) {
        this.keys = List.of(key);
        this.packedConvertor = null;
        this.cc = cc;
        this.dumpIndex = dumpIndex;
    };

    // "packed": cc-to-key is one-to-many
    Mappings(PackedConvertor packedConvertor, Integer cc, Integer dumpIndex) {
        this.packedConvertor = packedConvertor;
        this.keys = packedConvertor.getKeys();
        this.cc = cc;
        this.dumpIndex = dumpIndex;
    };

    public Integer getCC() {
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
            return model.get(getKey());
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

    public String getKey() {
        if (keys.size() != 1) {
            throw new IllegalStateException("Should not get here");
        }
        return keys.getFirst();
    }

    /**
     * Definition of available Convertors;
     * Responsible for converting (single/multiple) values from the model from/to single Midi CC
     */
    private enum Convertors implements PackedConvertor {
        PACKED1 {
            @Override
            public List<String> getKeys() {
                return List.of("amplitudeenvelopetrigger", "modulationenvelopetrigger", "fmenvelopetrigger", "keysyncphase");
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
            public List<String> getKeys() {
                return List.of("unisonvoices", "unisonpolyphonymode", "filtertype");
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
            public List<String> getKeys() {
                return List.of("osc1waveform", "osc2waveform", "osc3waveform", "portamentomode");
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
            public List<String> getKeys() {
                return List.of("osc1octave", "osc2octave", "osc3octave", "osc1to2sync");
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
            public List<String> getKeys() {
                return List.of("lfo1delaymulti", "lfo2delaymulti", "lfo1waveform", "lfo2waveform");
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
            public List<String> getKeys() {
                return List.of("lfo1keysyncphaseshift", "lfo1keysync", "lfo1lock",
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
            public List<String> getKeys() {
                return List.of("arpoctaves", "arponoff", "arpkeysync", "arplatch", "arpnotedestination");
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
            public List<String> getKeys() {
                return List.of("reverbtype", "chorusphaser");
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
        List<String> getKeys();

        // update model with a given (CC-param) value
        void toModel(Model model, int value);

        // extract value from model, to be sent to the actual synth/device.
        int toSynth(Model model);
    }
}
