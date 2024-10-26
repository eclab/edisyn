package edisyn.synth.novationastation;

import edisyn.Model;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Enumeration containing all available synth parameters and their so-called 'mappings'.
 * <p>
 * Each and every entry is defining (the mapping between)
 * <ul>
 * <li>(Edisyn) model key</li>
 * <li>(MIDI) byte-index in (sysex) program dump</li>
 * <li>(MIDI) CC</li>
 * <li>(MIDI) NRPN</li>
 * </ul>
 *
 * <p>
 * Note we have two kind of mappings:
 * <ol>
 *     <li>the 'straight' mappings: one-to-one relationship between (Edisyn) model key and MIDI parameter</li>
 *     <li>the 'packed' mappings: one-to-many relationship between (Edisyn) model key and MIDI parameters</li>
 * </ol>
 * </p>
 */
enum Mappings {
    ////
    // program/patch
    ////
    POLYPHONY_MODE(Convertors.Packed.PACKED2, Boundaries.POLYPHONY_MODE),
    PORTAMENTO_MODE(Convertors.Packed.PACKED3, Boundaries.PORTAMENTO_MODE),
    PORTAMENTO_TIME(3, 5, null),
    PREGLIDE_SEMITONES(4, 8, null, Boundaries.CENTRIC_24),
    UNISON_VOICES(Convertors.Packed.PACKED2, Boundaries.UNISON_VOICES),
    UNISON_DETUNE(1, 68, null),
    KEY_SYNC_PHASE(Convertors.Packed.PACKED1, Boundaries.KEYSYNC_PHASE),
    ////
    // Oscillators - global (= applying to all oscillators)
    ////
    OSCS_RANDOM_DETUNE(2, 69, null),
    OSCS_MODWHEEL_PITCH_DEPTH(31, null,5, Boundaries.CENTRIC_127),
    OSCS_AFTERTCH_PITCH_DEPTH(32, null, 6, Boundaries.CENTRIC_127),
    OSCS_BREATH_PITCH_DEPTH(33, null, 7, Boundaries.CENTRIC_127),
    OSCS_MODWHEEL_LFO1_PITCH_DEPTH(34, null, 8, Boundaries.CENTRIC_127),
    OSCS_AFTERTCH_LFO1_PITCH_DEPTH(35, null, 9, Boundaries.CENTRIC_127),
    OSCS_BREATH_LFO1_PITCH_DEPTH(36, null, 10, Boundaries.CENTRIC_127),
    OSCS_MODWHEEL_AMPLITUDE_DEPTH(81, null, 18, Boundaries.CENTRIC_127),
    OSCS_AFTERTCH_AMPLITUDE_DEPTH(82, null, 19, Boundaries.CENTRIC_127),
    OSCS_BREATH_AMPLITUDE_DEPTH(83, null, 20, Boundaries.CENTRIC_127),
    ////
    // Oscillators - individual
    // - 3 basically identical oscillators are available, yet:
    // - osc2 can be synced by osc1
    // - osc3 can be FM'ed by osc2
    ////
    OSC1_OCTAVE(Convertors.Packed.PACKED4, Boundaries.OSC_OCTAVES),
    OSC1_WAVEFORM(Convertors.Packed.PACKED3, Boundaries.OSC_WAVEFORMS),
    OSC1_SEMITONE(7, 40, null, Boundaries.CENTRIC_24),
    OSC1_DETUNE(8, 41, null, Boundaries.CENTRIC_100),
    OSC1_BENDWHEEL_AMOUNT(9, 42, null, Boundaries.CENTRIC_24),
    OSC1_LFO1_DEPTH(10, 43, null, Boundaries.CENTRIC_127),
    OSC1_ENV2_DEPTH(11, 44, null, Boundaries.CENTRIC_127),
    OSC1_PULSE_WIDTH(12, 45, null, Boundaries.CENTRIC_127),
    OSC1_LFO2_PULSE_WIDTH_MOD(13, 46, null, Boundaries.CENTRIC_127),
    OSC1_ENV2_PULSE_WIDTH_MOD(14, 47, null, Boundaries.CENTRIC_127),
    OSC2_OCTAVE(Convertors.Packed.PACKED4, Boundaries.OSC_OCTAVES),
    OSC2_WAVEFORM(Convertors.Packed.PACKED3, Boundaries.OSC_WAVEFORMS),
    OSC2_SEMITONE(15, 48, null, Boundaries.CENTRIC_24),
    OSC2_DETUNE(16, 49, null, Boundaries.CENTRIC_100),
    OSC2_BENDWHEEL_AMOUNT(17, 50, null, Boundaries.CENTRIC_24),
    OSC2_LFO1_DEPTH(18, 51, null, Boundaries.CENTRIC_127),
    OSC2_ENV2_DEPTH(19, 52, null, Boundaries.CENTRIC_127),
    OSC2_PULSE_WIDTH(20, 53, null, Boundaries.CENTRIC_127),
    OSC2_LFO2_PULSE_WIDTH_MOD(21, 54, null, Boundaries.CENTRIC_127),
    OSC2_ENV2_PULSE_WIDTH_MOD(22, 55, null, Boundaries.CENTRIC_127),
    OSC2_SYNCED_BY_1(Convertors.Packed.PACKED4, Boundaries.BOOLEAN),
    OSC3_OCTAVE(Convertors.Packed.PACKED4, Boundaries.OSC_OCTAVES),
    OSC3_WAVEFORM(Convertors.Packed.PACKED3, Boundaries.OSC_WAVEFORMS),
    OSC3_SEMITONE(23, 56, null, Boundaries.CENTRIC_24),
    OSC3_DETUNE(24, 57, null, Boundaries.CENTRIC_100),
    OSC3_BENDWHEEL_AMOUNT(25, 58, null, Boundaries.CENTRIC_24),
    OSC3_LFO1_DEPTH(26, 59, null, Boundaries.CENTRIC_127),
    OSC3_ENV2_DEPTH(27, 60, null, Boundaries.CENTRIC_127),
    OSC3_PULSE_WIDTH(28, 61, null, Boundaries.CENTRIC_127),
    OSC3_LFO2_PULSE_WIDTH_MOD(29, 62, null, Boundaries.CENTRIC_127),
    OSC3_ENV2_PULSE_WIDTH_MOD(30, 63, null, Boundaries.CENTRIC_127),
    ////
    // FM (osc2 to osc3)
    ////
    FM_FIXED_LEVEL(56, null, 0),
    FM_ENVELOPE_DEPTH(57, null, 1, Boundaries.CENTRIC_127),
    ////
    // Envelopes
    // - envelope1=amplitude envelope (ADSR)
    // - envelope2=modulation envelope (ADSR)
    // - envelope3=fm envelope (AD)
    ////
    ENVELOPE1_ATTACK(62, 108, null),
    ENVELOPE1_DECAY(63, 109, null),
    ENVELOPE1_SUSTAIN(64, 110, null),
    ENVELOPE1_RELEASE(65, 111, null),
    ENVELOPE1_TRIGGER(Convertors.Packed.PACKED1, Boundaries.ENV_TRIGGERS),
    ENVELOPE1_VELOCITY_DEPTH(61, 112, null, Boundaries.CENTRIC_127),
    ENVELOPE2_ATTACK(67, 114, null),
    ENVELOPE2_DECAY(68, 115, null),
    ENVELOPE2_SUSTAIN(69, 116, null),
    ENVELOPE2_RELEASE(70, 117, null),
    ENVELOPE2_TRIGGER(Convertors.Packed.PACKED1, Boundaries.ENV_TRIGGERS),
    ENVELOPE2_VELOCITY_DEPTH(66, 118, null, Boundaries.CENTRIC_127),
    ENVELOPE3_ATTACK(59, null, 3),
    ENVELOPE3_DECAY(60, null, 4),
    ENVELOPE3_TRIGGER(Convertors.Packed.PACKED1, Boundaries.ENV_TRIGGERS),
    ENVELOPE3_VELOCITY_DEPTH(58, 0, 2, Boundaries.CENTRIC_127),
    ////
    // LFOs
    // - 2 identical LFOs are available
    ////
    LFO1_WAVEFORM(Convertors.Packed.PACKED5, Boundaries.LFO_WAVE_FORMS),
    LFO1_SPEED_NON_SYNC(72, 80, null),
    LFO1_SPEED_SYNC(73, 81, null, Boundaries.SYNC_RATES),
    LFO1_DELAY(74, 82, null),
    LFO1_DELAY_MULTI(Convertors.Packed.PACKED5, Boundaries.BOOLEAN),
    LFO1_KEY_SYNC(Convertors.Packed.PACKED6, Boundaries.BOOLEAN),
    LFO1_KEY_SYNC_PHASE_SHIFT(Convertors.Packed.PACKED6, Boundaries.BOOLEAN),
    LFO1_LOCK(Convertors.Packed.PACKED6, Boundaries.BOOLEAN),
    LFO2_WAVEFORM(Convertors.Packed.PACKED5, Boundaries.LFO_WAVE_FORMS),
    LFO2_SPEED_NON_SYNC(75, 83, null),
    LFO2_SPEED_SYNC(76, 84, null, Boundaries.SYNC_RATES),
    LFO2_DELAY(77, 85, null),
    LFO2_DELAY_MULTI(Convertors.Packed.PACKED5, Boundaries.BOOLEAN),
    LFO2_KEY_SYNC(Convertors.Packed.PACKED6, Boundaries.BOOLEAN),
    LFO2_KEY_SYNC_PHASE_SHIFT(Convertors.Packed.PACKED6, Boundaries.BOOLEAN),
    LFO2_LOCK(Convertors.Packed.PACKED6, Boundaries.BOOLEAN),
    ////
    // Filter (low pass)
    ////
    FILTER_TYPE(Convertors.Packed.PACKED2, Boundaries.FILTER_TYPES),
    FILTER_FREQ(46, 105, null),
    FILTER_RESONANCE(44, 106, null),
    FILTER_OVERDRIVE(43, 104, null),
    FILTER_Q_NORMALIZE(45, 103, null),
    FILTER_KEY_TRACK(47, null, 11),
    FILTER_ENV2_DEPTH(52, 107, null, Boundaries.CENTRIC_127),
    FILTER_LFO2_DEPTH(51, 102, null, Boundaries.CENTRIC_127),
    FILTER_MODWHEEL_FREQUENCY_DEPTH(48, null, 12, Boundaries.CENTRIC_127),
    FILTER_AFTERTCH_FREQUENCY_DEPTH(49, null, 13, Boundaries.CENTRIC_127),
    FILTER_BREATH_FREQUENCY_DEPTH(50, null, 14, Boundaries.CENTRIC_127),
    FILTER_MODWHEEL_LFO2_FREQUENCY_DEPTH(53, null, 15, Boundaries.CENTRIC_127),
    FILTER_AFTERTCH_LFO2_FREQUENCY_DEPTH(54, null, 16, Boundaries.CENTRIC_127),
    FILTER_BREATH_LFO2_FREQUENCY_DEPTH(55, null, 17, Boundaries.CENTRIC_127),
    ////
    // Mixer
    ////
    MIXER_OSC1(37, 72, null),
    MIXER_OSC2(38, 73, null),
    MIXER_OSC3(39, 74, null),
    MIXER_NOISE(40, 75, null),
    MIXER_RING_MOD(41, 76, null),
    MIXER_EXTERNAL(42, 77, null),
    ////
    // ARP
    ////
    ARP_ON_OFF(Convertors.Packed.PACKED7, Boundaries.BOOLEAN),
    ARP_OCTAVES(Convertors.Packed.PACKED7, Boundaries.ARP_OCTAVES),
    ARP_KEY_SYNC(Convertors.Packed.PACKED7, Boundaries.BOOLEAN),
    ARP_LATCH(Convertors.Packed.PACKED7, Boundaries.BOOLEAN),
    ARP_NOTE_DESTINATION(Convertors.Packed.PACKED7, Boundaries.ARP_NOTE_DESTINATION),
    ARP_RATE_NON_SYNC(84, 9, null, Boundaries.ARP_NON_SYNC_RATES),
    ARP_RATE_SYNC(85, 87, null, Boundaries.ARP_SYNC_RATES),
    ARP_GATE_TIME(86, 88, null),
    ARP_PATTERN(87, 3, null, Boundaries.ARP_PATTERN),
    // TODO not sure what this is, ARP latch by sustain pedal perhaps ?
    // ARP_LATCH_ON_MOMENTARY(null, 64, null),
    ////
    // Effects - equalizer
    ////
    EQUALIZER_LEVEL(92, 33, null, Boundaries.EQUALIZER_LEVEL),
    EQUALIZER_FREQUENCY(93, 34, null),
    EQUALIZER_RATE_NON_SYNC(94, 35, null),
    EQUALIZER_RATE_SYNC(95, 36, null, Boundaries.SYNC_RATES),
    EQUALIZER_MOD_DEPTH(96, 37, null),
    EQUALIZER_GLOBAL_SYNC(Convertors.Packed.PACKED9, Boundaries.EQUALIZER_GLOBAL_SYNC),
    ////
    // Effects - delay
    ////
    DELAY_SEND_LEVEL(100, 92, null),
    DELAY_SEND_MODWHEEL(101, 18, null, Boundaries.CENTRIC_127),
    DELAY_TIME_NON_SYNC(102, 19, null),
    DELAY_TIME_SYNC(103, 20, null, Boundaries.DELAY_SYNC_RATES),
    DELAY_FEEDBACK(104, 21, null),
    DELAY_STEREO_WIDTH(105, 22, null),
    DELAY_RATIO(106, 23, null, Boundaries.DELAY_RATIO),
    ////
    // Effects - chorus/flanger
    ////
    CHORUS_SEND_LEVEL(110, 93, null),
    CHORUS_TYPE(Convertors.Packed.PACKED8, Boundaries.CHORUS_TYPES),
    CHORUS_SEND_MODWHEEL(111, 26, null, Boundaries.CENTRIC_127),
    CHORUS_RATE_NON_SYNC(112, 27, null),
    CHORUS_RATE_SYNC(113, 28, null, Boundaries.SYNC_RATES),
    CHORUS_FEEDBACK(114, 29, null, Boundaries.CENTRIC_127),
    CHORUS_MOD_DEPTH(115, 30, null),
    CHORUS_MOD_CENTRE_POINT(116, 31, null, Boundaries.CENTRIC_127),
    CHORUS_GLOBAL_SYNC(Convertors.Packed.PACKED9, Boundaries.CHORUS_GLOBAL_SYNC),
    ////
    // Effects - reverb
    ////
    REVERB_SEND_LEVEL(107, 91, null),
    REVERB_TYPE(Convertors.Packed.PACKED8, Boundaries.REVERB_TYPES),
    REVERB_SEND_MODWHEEL(108, 24, null, Boundaries.CENTRIC_127),
    REVERB_DECAY(109, 25, null),
    ////
    // Effects - distortion
    ////
    // NOTE - (undocumented) CC 90: realtime control for distortion level !
    DISTORTION_LEVEL(97, 90, null),
    DISTORTION_MODWHEEL(98, 16, null, Boundaries.CENTRIC_127),
    DISTORTION_COMPENSATION(99, 17, null),
    ////
    // Effects - vocoder
    ////
    VOCODER_BALANCE(89, 95, null),
    VOCODER_STEREO_WIDTH(90, 14, null),
    VOCODER_SIBILANCE_TYPE(Convertors.Packed.PACKED10, Boundaries.SIBILANCE_TYPES),
    VOCODER_SIBILANCE_LEVEL(91, 15, null),
    ////
    // Effects - panning
    ////
    PANNING_POSITION(117, 10, null, Boundaries.CENTRIC_127),
    PANNING_MOD_DEPTH(120, 94, null),
    PANNING_RATE_NON_SYNC(118, 12, null),
    PANNING_RATE_SYNC(119, 13, null, Boundaries.SYNC_RATES),
    PANNING_GLOBAL_SYNC(Convertors.Packed.PACKED9, Boundaries.PANNING_GLOBAL_SYNC),
    ////
    // various
    ////
    EXT_AUDIO_TRIGGER(Convertors.Packed.PACKED10, Boundaries.BOOLEAN),
    EXT_AUDIO_TO_FX(Convertors.Packed.PACKED10, Boundaries.BOOLEAN),
    // program volume - stored in patch
    PROGRAM_VOLUME(125, 119, null, Boundaries.CENTRIC_24),
    OSC_SELECT(Convertors.Packed.PACKED11, Boundaries.OSC_SELECT),
    MIXER_SELECT(Convertors.Packed.PACKED11, Boundaries.MIXER_SELECT),
    PWM_SOURCE(Convertors.Packed.PACKED11, Boundaries.PWM_SOURCE),
    LFO_SELECT(Convertors.Packed.PACKED11, Boundaries.LFO_SELECT),

    ////
    // known, yet non-used: listed here for completenes & to avoid noisy logs
    ////
    // CC7: Device volume (non-patch related; not stored within patch, only CC-evented)
    DEVICE_VOLUME(null, 7, null),
    BANK_SELECT(null, 32, null);
    ;

    ////
    // FYI - CC voids/ignores
    ////
    // CC0 -> NA
    // CC1 -> modwheel (receive only)
    // CC2 -> breath control (receive only)
    // CC4 -> NA
    // CC6 -> used for NRPN value
    // CC11 -> NA
    // CC32 -> bank select
    // CC38, 39 -> NA
    // CC66 -> NA
    // CC86 -> NA
    // CC90 -> NA according to doc, however used for distortion level in practice
    // CC96, 97 -> NA
    // CC98 NRPN number
    // CC99, CC100, CC101-> NA
    // CC113 -> NA

    // NOTE: looks like (non-documented) Packed11convertor is coming in via NRPN 26

    // key as used in the Edisyn Synth Model, deduced from enum name, made compliant with Edisyn naming conventions
    private final String key;
    // Convertor
    private final Convertor convertor;
    // Restrictions: defining boundaries on the data in terms of min or max value, or listing possible values (and string representation)
    private final Boundaries boundaries;

    Mappings(Integer byteIndex, Integer cc, Integer nrpn) {
        this(byteIndex, cc, nrpn, Boundaries.NONE);
    }

    Mappings(Integer byteIndex, Integer cc, Integer nrpn, Boundaries boundaries) {
        this.key = extractKey(this);
        this.convertor = createStraight(key, byteIndex, cc, nrpn);
        this.boundaries = Objects.requireNonNull(boundaries);
    }

    Mappings(Convertor convertor, Boundaries boundaries) {
        this.key = extractKey(this);
        this.convertor = Objects.requireNonNull(convertor);
        this.boundaries = Objects.requireNonNull(boundaries);
    }

    /**
     * Find a mapping object by name & index
     * @param name, referring to name of enum, optionally containing a placeholder for an integer when we have multiple
     *              Mapping objects only differentiated by some sort of index. In that case, this name parameter should
     *              contain an integer placeholder (ref: the format used in <code>String.format</code>)
     * @param index used as argument in the (formatted string) name parameter
     * @return Mapping object
     *
     * @throws IllegalArgumentException when none is found
     *
     * @see java.lang.String#format(String, Object...)
     */
    static Mappings find(String name, int index) {
        return Mappings.valueOf(String.format(name, index));
    }

    /**
     * get (Edisyn) model key
     * @return key, non null
     */
    public String getKey() {
        return key;
    }

    /**
     * get convertor object (Edisyn data VS MIDI data)
     * @return convertor, non null
     */
    public Convertor getConvertor() {
        return convertor;
    }

    /**
     * get restrictions (boundaries imposed on the data)
     * @return restrictions, non null
     */
    public Boundaries getRestrictions() {
        return boundaries;
    }

    // deduce (Edisym model) key from enum name, yet adding some conversion to adhere to the Edisyn naming conventions
    private String extractKey(Mappings mappings) {
        return mappings.name().replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
    }

    // create straight convertor
    private static Convertor createStraight(String key, Integer byteIndex, Integer cc, Integer nrpn) {
        return new Straight(key, byteIndex, cc, nrpn);
    }

    /**
     * Most straightforward implementation of a <code>Convertor</code>, based on a simple one-to-one conversion
     */
    private static final class Straight implements Convertor {
        private final String key;
        private final OptionalInt byteIndex;
        private final OptionalInt cc;
        private final OptionalInt nrpn;

        private Straight(String key, Integer byteIndex, Integer cc, Integer nrpn) {
            this.key = key;
            this.byteIndex = byteIndex == null ? OptionalInt.empty() : OptionalInt.of(byteIndex);
            this.cc = cc == null ? OptionalInt.empty() : OptionalInt.of(cc);
            this.nrpn = nrpn == null ? OptionalInt.empty() : OptionalInt.of(nrpn);
        }

        @Override
        public void toModel(Model model, int value) {
            model.set(key, value);
        }

        @Override
        public int toSynth(Model model) {
            return model.get(key);
        }

        @Override
        public OptionalInt getByteIndex() {
            return byteIndex;
        }

        @Override
        public OptionalInt getCC() {
            return cc;
        }

        @Override
        public OptionalInt getNRPN() {
            return nrpn;
        }

        @Override
        public Boundaries getBoundaries() {
            return Boundaries.NONE;
        }
    }
}
