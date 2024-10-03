package edisyn.synth.novationastation;

import edisyn.Model;

import java.util.List;
import java.util.Locale;

/**
 * Enumeration containing all available synth parameters and their so-called 'mappings'.
 * <p>
 * Each and every entry is defining (the mapping between)
 * <ul>
 * <li>(edisyn) model key</li>
 * <li>(midi) CC</li>
 * <li>(midi) byte-index in (sysex) program dump</li>
 * </ul>
 *
 * <p>
 * Note we have two kind of mappings:
 * <ol>
 *     <li>the 'straight' mappings: one-to-one relationship between (Edisyn) model key and midi CC / byte-index</li>
 *     <li>the 'packed' mappings: one-to-many relationship between (Edisyn) model key and midi CC / byte-index</li>
 * </ol>
 * </p>
 */
enum Mappings {
    ////
    // program/patch
    ////
    POLYPHONY_MODE(Convertors.Packed.PACKED2),
    PORTAMENTO_MODE(Convertors.Packed.PACKED3 /*, Restrictions.values("exponential", "linear"*/),
    PORTAMENTO_TIME(5, 3),
    PREGLIDE_SEMITONES(8, 4),
    UNISON_VOICES(Convertors.Packed.PACKED2),
    UNISON_DETUNE(68, 1),
    KEY_SYNC_PHASE(Convertors.Packed.PACKED1),
    ////
    // Oscillators - global (= applying to all oscillators)
    ////
    OSCS_RANDOM_DETUNE(69, 2),
    OSCS_MODWHEEL_PITCH_DEPTH(null, 31),
    OSCS_AFTERTCH_PITCH_DEPTH(null, 32),
    OSCS_BREATH_PITCH_DEPTH(null, 33),
    OSCS_MODWHEEL_LFO1_PITCH_DEPTH(null, 34),
    OSCS_AFTERTCH_LFO1_PITCH_DEPTH(null, 35),
    OSCS_BREATH_LFO1_PITCH_DEPTH(null, 36),
    ////
    // Oscillators - individual
    // - 3 basically identical oscillators are available, yet:
    // - osc2 can be synced by osc1
    // - osc3 can be FM'ed by osc2
    ////
    OSC1_OCTAVE(Convertors.Packed.PACKED4),
    OSC1_WAVEFORM(Convertors.Packed.PACKED3),
    OSC1_SEMITONE(40, 7),
    OSC1_DETUNE(41, 8),
    OSC1_BENDWHEEL_AMOUNT(42, 9),
    OSC1_LFO1_DEPTH(43, 10),
    OSC1_ENV2_DEPTH(44, 11),
    OSC1_PULSE_WIDTH(45, 12),
    OSC1_LFO2_PULSE_WIDTH_MOD(46, 13),
    OSC1_ENV2_PULSE_WIDTH_MOD(47, 14),
    OSC2_OCTAVE(Convertors.Packed.PACKED4),
    OSC2_WAVEFORM(Convertors.Packed.PACKED3),
    OSC2_SEMITONE(48, 15),
    OSC2_DETUNE(49, 16),
    OSC2_BENDWHEEL_AMOUNT(50, 17),
    OSC2_LFO1_DEPTH(51, 18),
    OSC2_ENV2_DEPTH(52, 19),
    OSC2_PULSE_WIDTH(53, 20),
    OSC2_LFO2_PULSE_WIDTH_MOD(54, 21),
    OSC2_ENV2_PULSE_WIDTH_MOD(55, 22),
    OSC2_SYNCED_BY_1(Convertors.Packed.PACKED4),
    OSC3_OCTAVE(Convertors.Packed.PACKED4),
    OSC3_WAVEFORM(Convertors.Packed.PACKED3),
    OSC3_SEMITONE(56, 23),
    OSC3_DETUNE(57, 24),
    OSC3_BENDWHEEL_AMOUNT(58, 25),
    OSC3_LFO1_DEPTH(59, 26),
    OSC3_ENV2_DEPTH(60, 27),
    OSC3_PULSE_WIDTH(61, 28),
    OSC3_LFO2_PULSE_WIDTH_MOD(62, 29),
    OSC3_ENV2_PULSE_WIDTH_MOD(63, 30),
    ////
    // FM (osc2 to osc3)
    ////
    FM_FIXED_LEVEL(null, 56),
    FM_ENVELOPE_DEPTH(null, 57),
    FM_VELOCITY_DEPTH(null, 58),
    ////
    // Envelopes
    // - envelope1=amplitude envelope (ADSR)
    // - envelope2=modulation envelope (ADSR)
    // - envelope3=fm envelope (AD)
    ////
    ENVELOPE1_ATTACK(108, 62),
    ENVELOPE1_DECAY(109, 63),
    ENVELOPE1_SUSTAIN(110, 64),
    ENVELOPE1_RELEASE(111, 65),
    ENVELOPE1_TRIGGER(Convertors.Packed.PACKED1),
    ENVELOPE1_VELOCITY_DEPTH(112, 61),
    ENVELOPE2_ATTACK(114, 67),
    ENVELOPE2_DECAY(115, 68),
    ENVELOPE2_SUSTAIN(116, 69),
    ENVELOPE2_RELEASE(117, 70),
    ENVELOPE2_TRIGGER(Convertors.Packed.PACKED1),
    ENVELOPE2_VELOCITY_DEPTH(118, 66),
    ENVELOPE_FM_ATTACK(Convertors.Packed.PACKED1),
    ENVELOPE_FM_DECAY(Convertors.Packed.PACKED1),
    ENVELOPE_FM_TRIGGER(Convertors.Packed.PACKED1),
    ////
    // LFOs
    // - 2 identical LFOs are available
    ////
    LFO1_WAVEFORM(Convertors.Packed.PACKED5),
    LFO1_SPEED_NON_SYNC(80, 72),
    LFO1_SPEED_SYNC(81, 73),
    LFO1_DELAY(82, 74),
    LFO1_DELAY_MULTI(Convertors.Packed.PACKED5),
    LFO1_KEY_SYNC(Convertors.Packed.PACKED6),
    LFO1_KEY_SYNC_PHASE_SHIFT(Convertors.Packed.PACKED6),
    LFO1_LOCK(Convertors.Packed.PACKED6),
    LFO2_WAVEFORM(Convertors.Packed.PACKED5),
    LFO2_SPEED_NON_SYNC(83, 75),
    LFO2_SPEED_SYNC(84, 76),
    LFO2_DELAY(85, 77),
    LFO2_DELAY_MULTI(Convertors.Packed.PACKED5),
    LFO2_KEY_SYNC(Convertors.Packed.PACKED6),
    LFO2_KEY_SYNC_PHASE_SHIFT(Convertors.Packed.PACKED6),
    LFO2_LOCK(Convertors.Packed.PACKED6),
    ////
    // Filter (low pass)
    ////
    FILTER_TYPE(Convertors.Packed.PACKED2),
    FILTER_FREQ(105, 46),
    FILTER_RESONANCE(106, 44),
    FILTER_OVERDRIVE(104, 43),
    // CC6 -> filtertracking (on latest firmware - whilst NRPN according to latest (yet older) spec I could find)
    FILTER_KEY_TRACK(6, 47),
    FILTER_ENV2_DEPTH(107, 52),
    FILTER_LFO2_DEPTH(102, 51),
    FILTER_Q_NORMALIZE(103, 45),
    // TODO - add extra mappings from sysex dump
    ////
    // Mixer
    ////
    MIXER_OSC1(72, 37),
    MIXER_OSC2(73, 38),
    MIXER_OSC3(74, 39),
    MIXER_NOISE(75, 40),
    MIXER_RING_MOD(76, 41),
    MIXER_EXTERNAL(77, 42),
    ////
    // ARP
    ////
    ARP_ON_OFF(Convertors.Packed.PACKED7),
    ARP_OCTAVES(Convertors.Packed.PACKED7),
    ARP_KEY_SYNC(Convertors.Packed.PACKED7),
    ARP_LATCH(Convertors.Packed.PACKED7),
    ARP_NOTE_DESTINATION(Convertors.Packed.PACKED7),
    ARP_SYNC(87, 85),
    ARP_GATE_TIME(88, 86),
    ARP_PATTERN(3, 87),
    ARP_RATE(9, 84),
    // not sure what this is, different from the one in packed7 ?
    ARP_LATCH_ON_MOMENTARY(64, null),
    ////
    // Effects - equalizer
    ////
    EQUALIZER_LEVEL(33, 92),
    EQUALIZER_FREQUENCY(34, 93),
    EQUALIZER_RATE_NON_SYNC(35, 94),
    EQUALIZER_RATE_SYNC(36, 95),
    EQUALIZER_MOD_DEPTH(37, 96),
    EQUALIZER_GLOBAL_SYNC(Convertors.Packed.PACKED9),
    ////
    // Effects - delay
    ////
    DELAY_SEND_LEVEL(92, 100),
    DELAY_SEND_MODWHEEL(18, 101),
    DELAY_TIME_NON_SYNC(19, 102),
    DELAY_TIME_SYNC(20, 103),
    DELAY_FEEDBACK(21, 104),
    DELAY_STEREO_WIDTH(22, 105),
    DELAY_RATIO(23, 106),
    ////
    // Effects - chorus/flanger
    ////
    CHORUS_SEND_LEVEL(93, 110),
    CHORUS_PHASER(Convertors.Packed.PACKED8),
    CHORUS_SEND_MODWHEEL(26, 111),
    CHORUS_RATE_NON_SYNC(27, 112),
    CHORUS_RATE_SYNC(28, 113),
    CHORUS_FEEDBACK(29, 114),
    CHORUS_MOD_DEPTH(30, 115),
    CHORUS_MOD_CENTRE_POINT(31, 116),
    CHORUS_GLOBAL_SYNC(Convertors.Packed.PACKED9),
    ////
    // Effects - reverb
    ////
    REVERB_SEND_LEVEL(91, 107),
    REVERB_TYPE(Convertors.Packed.PACKED8),
    REVERB_SEND_MODWHEEL(24, 108),
    REVERB_DECAY(25, 109),
    ////
    // Effects - distortion
    ////
    DISTORTION_MODWHEEL(16, 98),
    DISTORTION_COMPENSATION(17, 99),
    ////
    // Effects - vocoder
    ////
    VOCODER_BALANCE(95, 89),
    VOCODER_STEREO_WIDTH(14, 90),
    VOCODER_SIBILANCE_TYPE(Convertors.Packed.PACKED10),
    VOCODER_SIBILANCE_LEVEL(15, 91),
    ////
    // Effects - panning
    ////
    PANNING_POSITION(10, 117),
    PANNING_MOD_DEPTH(94, 120),
    PANNING_RATE_NON_SYNC(12, 118),
    PANNING_RATE_SYNC(13, 119),
    PANNING_GLOBAL_SYNC(Convertors.Packed.PACKED9),
    ////
    // various
    ////
    EXT_AUDIO_TRIGGER(Convertors.Packed.PACKED10),
    EXT_AUDIO_TO_FX(Convertors.Packed.PACKED10),
    // program volume - stored in patch
    PROGRAM_VOLUME(119, 125),
    // CC7: Device volume (non-patch related; not stored within patch)
    DEVICE_VOLUME(7, null),
    ;

    // TODO - introduce NRPNS !?

    ////
    // FYI - CC voids/ignores
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

    // key(s) as used in the Synth Model, typically containing single element, will contain multiple elements for packed parameters
    private final String key;
    // Convertor
    private final Convertor convertor;
//    private final Restrictions restrictions;

    Mappings(Integer cc, Integer dumpIndex) {
        this.key = extractKey(this);
        this.convertor = createStraight(key, cc, dumpIndex);
    }

    Mappings(Convertor convertor) {
        this.key = extractKey(this);
        this.convertor = convertor;
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

    // deduce (Edisym model) key from enum name, yet adding some conversion to adhere to the Edisyn naming conventions
    private String extractKey(Mappings mappings) {
        return mappings.name().replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
    }

    // create straight convertor
    private static Convertor createStraight(String key, Integer cc, Integer dumpIndex) {
        return new Straight(key, cc, dumpIndex);
    }

    private static final class Straight implements Convertor {
        private final String key;
        private final Integer cc;
        private final Integer byteIndex;

        private Straight(String key, Integer cc, Integer byteIndex) {
            this.key = key;
            this.cc = cc;
            this.byteIndex = byteIndex;
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
        public Integer getCC() {
            return cc;
        }

        @Override
        public Integer getByteIndex() {
            return byteIndex;
        }
    }

//    private static class Restrictions {
//        private List<String> values;
//
//        Restrictions values(List<String> values){
//            this.values = values;
//            return this;
//        }
//
//        List<String> getValues() {
//            return values;
//        }
//    }
}
