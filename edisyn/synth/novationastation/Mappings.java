package edisyn.synth.novationastation;

import edisyn.Model;

import java.util.Locale;

/**
 * Enumeration containing all available synth parameters and their so-called 'mappings'.
 * <p>
 * Each and every entry is defining (the mapping between)
 * <ul>
 * <li>(edisyn) model key</li>
 * <li>(midi) byte-index in (sysex) program dump</li>
 * <li>(midi) CC</li>
 * <li>(midi) NRPN</li>
 * </ul>
 *
 * <p>
 * Note we have two kind of mappings:
 * <ol>
 *     <li>the 'straight' mappings: one-to-one relationship between (Edisyn) model key and midi parameter</li>
 *     <li>the 'packed' mappings: one-to-many relationship between (Edisyn) model key and midi parameters</li>
 * </ol>
 * </p>
 */
enum Mappings {
    ////
    // program/patch
    ////
    POLYPHONY_MODE(Convertors.Packed.PACKED2, Restrictions.POLYPHONY_MODE),
    PORTAMENTO_MODE(Convertors.Packed.PACKED3, Restrictions.PORTAMENTO_MODE),
    PORTAMENTO_TIME(3, 5, null),
    PREGLIDE_SEMITONES(4, 8, null, Restrictions.CENTRIC_24),
    UNISON_VOICES(Convertors.Packed.PACKED2, Restrictions.UNISON_VOICES),
    UNISON_DETUNE(1, 68, null),
    KEY_SYNC_PHASE(Convertors.Packed.PACKED1, Restrictions.KEYSYNC_PHASE),
    ////
    // Oscillators - global (= applying to all oscillators)
    ////
    OSCS_RANDOM_DETUNE(2, 69, null),
    OSCS_MODWHEEL_PITCH_DEPTH(31, null,5, Restrictions.CENTRIC_127),
    OSCS_AFTERTCH_PITCH_DEPTH(32, null, 6, Restrictions.CENTRIC_127),
    OSCS_BREATH_PITCH_DEPTH(33, null, 7, Restrictions.CENTRIC_127),
    OSCS_MODWHEEL_LFO1_PITCH_DEPTH(34, null, 8, Restrictions.CENTRIC_127),
    OSCS_AFTERTCH_LFO1_PITCH_DEPTH(35, null, 9, Restrictions.CENTRIC_127),
    OSCS_BREATH_LFO1_PITCH_DEPTH(36, null, 10, Restrictions.CENTRIC_127),
    OSCS_MODWHEEL_AMPLITUDE_DEPTH(81, null, 18, Restrictions.CENTRIC_127),
    OSCS_AFTERTCH_AMPLITUDE_DEPTH(82, null, 19, Restrictions.CENTRIC_127),
    OSCS_BREATH_AMPLITUDE_DEPTH(83, null, 20, Restrictions.CENTRIC_127),
    ////
    // Oscillators - individual
    // - 3 basically identical oscillators are available, yet:
    // - osc2 can be synced by osc1
    // - osc3 can be FM'ed by osc2
    ////
    OSC1_OCTAVE(Convertors.Packed.PACKED4, Restrictions.OSC_OCTAVES),
    OSC1_WAVEFORM(Convertors.Packed.PACKED3, Restrictions.OSC_WAVEFORMS),
    OSC1_SEMITONE(7, 40, null, Restrictions.CENTRIC_24),
    OSC1_DETUNE(8, 41, null, Restrictions.CENTRIC_100),
    // TODO - verify restrictions (x3)
    OSC1_BENDWHEEL_AMOUNT(9, 42, null, Restrictions.CENTRIC_127),
    OSC1_LFO1_DEPTH(10, 43, null, Restrictions.CENTRIC_127),
    OSC1_ENV2_DEPTH(11, 44, null, Restrictions.CENTRIC_127),
    OSC1_PULSE_WIDTH(12, 45, null, Restrictions.CENTRIC_127),
    OSC1_LFO2_PULSE_WIDTH_MOD(13, 46, null, Restrictions.CENTRIC_127),
    OSC1_ENV2_PULSE_WIDTH_MOD(14, 47, null, Restrictions.CENTRIC_127),
    OSC2_OCTAVE(Convertors.Packed.PACKED4, Restrictions.OSC_OCTAVES),
    OSC2_WAVEFORM(Convertors.Packed.PACKED3, Restrictions.OSC_WAVEFORMS),
    OSC2_SEMITONE(15, 48, null, Restrictions.CENTRIC_24),
    OSC2_DETUNE(16, 49, null, Restrictions.CENTRIC_100),
    OSC2_BENDWHEEL_AMOUNT(17, 50, null, Restrictions.CENTRIC_127),
    OSC2_LFO1_DEPTH(18, 51, null, Restrictions.CENTRIC_127),
    OSC2_ENV2_DEPTH(19, 52, null, Restrictions.CENTRIC_127),
    OSC2_PULSE_WIDTH(20, 53, null, Restrictions.CENTRIC_127),
    OSC2_LFO2_PULSE_WIDTH_MOD(21, 54, null, Restrictions.CENTRIC_127),
    OSC2_ENV2_PULSE_WIDTH_MOD(22, 55, null, Restrictions.CENTRIC_127),
    OSC2_SYNCED_BY_1(Convertors.Packed.PACKED4, Restrictions.BOOLEAN),
    OSC3_OCTAVE(Convertors.Packed.PACKED4, Restrictions.OSC_OCTAVES),
    OSC3_WAVEFORM(Convertors.Packed.PACKED3, Restrictions.OSC_WAVEFORMS),
    OSC3_SEMITONE(23, 56, null, Restrictions.CENTRIC_24),
    OSC3_DETUNE(24, 57, null, Restrictions.CENTRIC_100),
    OSC3_BENDWHEEL_AMOUNT(25, 58, null, Restrictions.CENTRIC_127),
    OSC3_LFO1_DEPTH(26, 59, null, Restrictions.CENTRIC_127),
    OSC3_ENV2_DEPTH(27, 60, null, Restrictions.CENTRIC_127),
    OSC3_PULSE_WIDTH(28, 61, null, Restrictions.CENTRIC_127),
    OSC3_LFO2_PULSE_WIDTH_MOD(29, 62, null, Restrictions.CENTRIC_127),
    OSC3_ENV2_PULSE_WIDTH_MOD(30, 63, null, Restrictions.CENTRIC_127),
    ////
    // FM (osc2 to osc3)
    ////
    FM_FIXED_LEVEL(56, null, 0),
    FM_ENVELOPE_DEPTH(57, null, 1),
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
    ENVELOPE1_TRIGGER(Convertors.Packed.PACKED1, Restrictions.ENV_TRIGGERS),
    ENVELOPE1_VELOCITY_DEPTH(61, 112, null, Restrictions.CENTRIC_127),
    ENVELOPE2_ATTACK(67, 114, null),
    ENVELOPE2_DECAY(68, 115, null),
    ENVELOPE2_SUSTAIN(69, 116, null),
    ENVELOPE2_RELEASE(70, 117, null),
    ENVELOPE2_TRIGGER(Convertors.Packed.PACKED1, Restrictions.ENV_TRIGGERS),
    ENVELOPE2_VELOCITY_DEPTH(66, 118, null, Restrictions.CENTRIC_127),
    ENVELOPE3_ATTACK(59, null, 3),
    ENVELOPE3_DECAY(60, null, 4),
    ENVELOPE3_TRIGGER(Convertors.Packed.PACKED1, Restrictions.ENV_TRIGGERS),
    ENVELOPE3_VELOCITY_DEPTH(58, 0, 2, Restrictions.CENTRIC_127),
    ////
    // LFOs
    // - 2 identical LFOs are available
    ////
    LFO1_WAVEFORM(Convertors.Packed.PACKED5, Restrictions.LFO_WAVE_FORMS),
    LFO1_SPEED_NON_SYNC(72, 80, null),
    LFO1_SPEED_SYNC(73, 81, null, Restrictions.SYNC_RATES),
    LFO1_DELAY(74, 82, null),
    LFO1_DELAY_MULTI(Convertors.Packed.PACKED5, Restrictions.BOOLEAN),
    LFO1_KEY_SYNC(Convertors.Packed.PACKED6, Restrictions.BOOLEAN),
    LFO1_KEY_SYNC_PHASE_SHIFT(Convertors.Packed.PACKED6, Restrictions.BOOLEAN),
    LFO1_LOCK(Convertors.Packed.PACKED6, Restrictions.BOOLEAN),
    LFO2_WAVEFORM(Convertors.Packed.PACKED5, Restrictions.LFO_WAVE_FORMS),
    LFO2_SPEED_NON_SYNC(75, 83, null),
    LFO2_SPEED_SYNC(76, 84, null, Restrictions.SYNC_RATES),
    LFO2_DELAY(77, 85, null),
    LFO2_DELAY_MULTI(Convertors.Packed.PACKED5, Restrictions.BOOLEAN),
    LFO2_KEY_SYNC(Convertors.Packed.PACKED6, Restrictions.BOOLEAN),
    LFO2_KEY_SYNC_PHASE_SHIFT(Convertors.Packed.PACKED6, Restrictions.BOOLEAN),
    LFO2_LOCK(Convertors.Packed.PACKED6, Restrictions.BOOLEAN),
    ////
    // Filter (low pass)
    ////
    FILTER_TYPE(Convertors.Packed.PACKED2, Restrictions.FILTER_TYPES),
    FILTER_FREQ(46, 105, null),
    FILTER_RESONANCE(44, 106, null),
    FILTER_OVERDRIVE(43, 104, null),
    FILTER_Q_NORMALIZE(45, 103, null),
    FILTER_KEY_TRACK(47, null, 11),
    FILTER_ENV2_DEPTH(52, 107, null, Restrictions.CENTRIC_127),
    FILTER_LFO2_DEPTH(51, 102, null, Restrictions.CENTRIC_127),
    FILTER_MODWHEEL_FREQUENCY_DEPTH(48, null, 12, Restrictions.CENTRIC_127),
    FILTER_AFTERTCH_FREQUENCY_DEPTH(49, null, 13, Restrictions.CENTRIC_127),
    FILTER_BREATH_FREQUENCY_DEPTH(50, null, 14, Restrictions.CENTRIC_127),
    FILTER_MODWHEEL_LFO2_FREQUENCY_DEPTH(53, null, 15, Restrictions.CENTRIC_127),
    FILTER_AFTERTCH_LFO2_FREQUENCY_DEPTH(54, null, 16, Restrictions.CENTRIC_127),
    FILTER_BREATH_LFO2_FREQUENCY_DEPTH(55, null, 17, Restrictions.CENTRIC_127),
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
    ARP_ON_OFF(Convertors.Packed.PACKED7, Restrictions.BOOLEAN),
    ARP_OCTAVES(Convertors.Packed.PACKED7, Restrictions.ARP_OCTAVES),
    ARP_KEY_SYNC(Convertors.Packed.PACKED7, Restrictions.BOOLEAN),
    ARP_LATCH(Convertors.Packed.PACKED7, Restrictions.BOOLEAN),
    ARP_NOTE_DESTINATION(Convertors.Packed.PACKED7, Restrictions.ARP_NOTE_DESTINATION),
    ARP_RATE_NON_SYNC(84, 9, null),
    ARP_RATE_SYNC(85, 87, null, Restrictions.ARP_SYNC_RATES),
    ARP_GATE_TIME(86, 88, null),
    ARP_PATTERN(87, 3, null, Restrictions.ARP_PATTERN),
    // TODO not sure what this is, different from the one in packed7 ?
    // byteindex = null ?
//    ARP_LATCH_ON_MOMENTARY(null, 64, null),
    ////
    // Effects - equalizer
    ////
    EQUALIZER_LEVEL(92, 33, null, Restrictions.EQUALIZER_LEVEL),
    EQUALIZER_FREQUENCY(93, 34, null),
    EQUALIZER_RATE_NON_SYNC(94, 35, null),
    EQUALIZER_RATE_SYNC(95, 36, null, Restrictions.SYNC_RATES),
    EQUALIZER_MOD_DEPTH(96, 37, null),
    EQUALIZER_GLOBAL_SYNC(Convertors.Packed.PACKED9, Restrictions.EQUALIZER_GLOBAL_SYNC),
    ////
    // Effects - delay
    ////
    DELAY_SEND_LEVEL(100, 92, null),
    DELAY_SEND_MODWHEEL(101, 18, null, Restrictions.CENTRIC_127),
    DELAY_TIME_NON_SYNC(102, 19, null),
    DELAY_TIME_SYNC(103, 20, null, Restrictions.SYNC_RATES),
    DELAY_FEEDBACK(104, 21, null),
    DELAY_STEREO_WIDTH(105, 22, null),
    DELAY_RATIO(106, 23, null, Restrictions.DELAY_RATIO),
    ////
    // Effects - chorus/flanger
    ////
    CHORUS_SEND_LEVEL(110, 93, null),
    CHORUS_TYPE(Convertors.Packed.PACKED8, Restrictions.CHORUS_TYPES),
    CHORUS_SEND_MODWHEEL(111, 26, null, Restrictions.CENTRIC_127),
    CHORUS_RATE_NON_SYNC(112, 27, null),
    CHORUS_RATE_SYNC(113, 28, null, Restrictions.SYNC_RATES),
    CHORUS_FEEDBACK(114, 29, null),
    CHORUS_MOD_DEPTH(115, 30, null),
    CHORUS_MOD_CENTRE_POINT(116, 31, null),
    CHORUS_GLOBAL_SYNC(Convertors.Packed.PACKED9, Restrictions.CHORUS_GLOBAL_SYNC),
    ////
    // Effects - reverb
    ////
    REVERB_SEND_LEVEL(107, 91, null),
    REVERB_TYPE(Convertors.Packed.PACKED8, Restrictions.REVERB_TYPES),
    REVERB_SEND_MODWHEEL(108, 24, null, Restrictions.CENTRIC_127),
    REVERB_DECAY(109, 25, null),
    ////
    // Effects - distortion
    ////
    // NOTE - (undocumented) CC 90: realtime control for distortion level !
    DISTORTION_LEVEL(97, 90, null),
    DISTORTION_MODWHEEL(98, 16, null, Restrictions.CENTRIC_127),
    DISTORTION_COMPENSATION(99, 17, null),
    ////
    // Effects - vocoder
    ////
    VOCODER_BALANCE(89, 95, null, Restrictions.CENTRIC_127),
    VOCODER_STEREO_WIDTH(90, 14, null),
    VOCODER_SIBILANCE_TYPE(Convertors.Packed.PACKED10, Restrictions.SIBILANCE_TYPES),
    VOCODER_SIBILANCE_LEVEL(91, 15, null),
    ////
    // Effects - panning
    ////
    PANNING_POSITION(117, 10, null, Restrictions.CENTRIC_127),
    PANNING_MOD_DEPTH(120, 94, null),
    PANNING_RATE_NON_SYNC(118, 12, null),
    PANNING_RATE_SYNC(119, 13, null, Restrictions.SYNC_RATES),
    PANNING_GLOBAL_SYNC(Convertors.Packed.PACKED9, Restrictions.PANNING_GLOBAL_SYNC),
    ////
    // various
    ////
    EXT_AUDIO_TRIGGER(Convertors.Packed.PACKED10),
    EXT_AUDIO_TO_FX(Convertors.Packed.PACKED10),
    // program volume - stored in patch
    PROGRAM_VOLUME(125, 119, null, Restrictions.CENTRIC_24),
    // CC7: Device volume (non-patch related; not stored within patch)
    // for now, let's ignore next, not available in dump, not very useful either
    // DEVICE_VOLUME(null, 7),

    OSC_SELECT(Convertors.Packed.PACKED11),
    MIXER_SELECT(Convertors.Packed.PACKED11),
    PWM_SOURCE(Convertors.Packed.PACKED11),
    LFO_SELECT(Convertors.Packed.PACKED11)
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
    // CC90 -> NA (according to doc, however used for distortion level in practice)
    // CC96, 97 -> NA
    // CC98 NRPN number
    // CC99, CC100, CC101-> NA
    // CC113 -> NA

    // NOTE: looks like (non-documented) Packed11convertor is coming in via NRPN 26

    // key(s) as used in the Synth Model, typically containing single element, will contain multiple elements for packed parameters
    private final String key;
    // Convertor
    private final Convertor convertor;
    private final Restrictions restrictions;

    Mappings(int byteIndex, Integer cc, Integer nrpn) {
        this(byteIndex, cc, nrpn, Restrictions.NONE);
    }

    Mappings(int byteIndex, Integer cc, Integer nrpn, Restrictions restrictions) {
        this.key = extractKey(this);
        this.convertor = createStraight(key, byteIndex, cc, nrpn);
        this.restrictions = restrictions;
    }

    Mappings(Convertor convertor) {
        this(convertor, convertor.getRestrictions());
    }

    Mappings(Convertor convertor, Restrictions restrictions) {
        this.key = extractKey(this);
        this.convertor = convertor;
        this.restrictions = restrictions;
    }

    static Mappings find(String string, int index) {
        return Mappings.valueOf(String.format(string, index));
    }

    public String getKey() {
        return key;
    }

    public Convertor getConvertor() {
        return convertor;
    }

    public Restrictions getRestrictions() {
        return restrictions;
    }

    // deduce (Edisym model) key from enum name, yet adding some conversion to adhere to the Edisyn naming conventions
    private String extractKey(Mappings mappings) {
        return mappings.name().replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
    }

    // create straight convertor
    private static Convertor createStraight(String key, int dumpIndex, Integer cc, Integer nrpn) {
        return new Straight(key, dumpIndex, cc, nrpn);
    }

    private static final class Straight implements Convertor {
        private final String key;
        private final int byteIndex;
        private final Integer cc;
        private final Integer nrpn;

        private Straight(String key, int byteIndex, Integer cc, Integer nrpn) {
            this.key = key;
            this.cc = cc;
            this.byteIndex = byteIndex;
            this.nrpn = nrpn;
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
        public int getByteIndex() {
            return byteIndex;
        }

        @Override
        public Integer getCC() {
            return cc;
        }

        @Override
        public Integer getNRPN() {
            return nrpn;
        }

        @Override
        public Restrictions getRestrictions() {
            return Restrictions.NONE;
        }
    }
}
