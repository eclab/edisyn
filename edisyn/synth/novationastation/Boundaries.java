package edisyn.synth.novationastation;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public enum Boundaries implements Boundary {
    // non param related
    BANKS(IntStream.rangeClosed(1, 4).boxed().map(String::valueOf).collect(Collectors.toList())),
    PATCH_NUMBERS(IntStream.rangeClosed(0, 99).boxed().map(String::valueOf).collect(Collectors.toList())),
    // param-related: some are generic
    NONE(0, 127),
    CENTRIC_24(52, 76, 64),
    CENTRIC_100(14, 114, 64),
    CENTRIC_127(0, 127, 64),
    BOOLEAN(0, 1),
    // param-related: some are specific
    POLYPHONY_MODE("Mono", "Mono Autoglide", "Poly", "Poly Voice Stealing"),
    PORTAMENTO_MODE("Exponential", "Linear"),
    UNISON_VOICES("Off", "2", "3", "4", "5", "6", "7", "8"),
    OSC_SELECT("1", "2", "3"),
    OSC_OCTAVES("-1", "0", "1", "2"),
    OSC_WAVEFORMS("Sine", "Triangle", "Saw", "Square/Pulse"),
    PWM_SOURCE("Manual", "LFO2", "Mod Env"), // 3 values from the docs / in practice only first 2 get evented
    ENV_TRIGGERS("Single", "Multi"),
    MIXER_SELECT("Noise", "1*2", "Ext"),
    FILTER_TYPES("12dB/Octave", "24dB/Octave"),
    LFO_SELECT("1", "2"),
    LFO_WAVE_FORMS("Triangle", "Saw", "Square", "S&H"),
    SIBILANCE_TYPES("High Pass", "Noise"),
    REVERB_TYPES("Echo Chamber", "Small Room", "Small Hall", "Large Room", "Large Hall", "Grand Hall"),
    CHORUS_TYPES("Chorus", "Phaser"),
    KEYSYNC_PHASE("Free Running", "0", "24", "48", "72", "96", "120", "144", "168", "192", "216", "240", "264", "288", "312", "336"),
    ARP_OCTAVES("1", "2", "3", "4"),
    ARP_NOTE_DESTINATION("Internal", "External", "Internal+External", "External+Played"),
    ARP_PATTERN("Up", "Down", "UD1", "UD2", "Order", "Random"),
    EQUALIZER_GLOBAL_SYNC("Off", "Low", "Mid", "High"),
    CHORUS_GLOBAL_SYNC("Off", "Left", "Center", "Right"),
    PANNING_GLOBAL_SYNC(CHORUS_GLOBAL_SYNC),
    SYNC_RATES("NA", "32t", "32", "16t", "16", "8t", "16D", "8", "4t", "8d", "4", "2t", "4d", "2", "1t", "2d",
                       "1b", "2t", "1d", "2b", "4t", "3b", "5t", "4b", "3d", "7t", "5b", "8t", "6b", "7b", "5d", "8b", "9b", "7d", "12"),
    DELAY_SYNC_RATES(Arrays.asList(SYNC_RATES.values).subList(0, 20)),
    ARP_SYNC_RATES(Arrays.asList(SYNC_RATES.values).subList(1, 17)),
    ARP_NON_SYNC_RATES(0, 127, -64), // 64 -> 191 BPM
    DELAY_RATIO("1-1", "4-3", "3-4", "3-2", "2-3", "2-1", "1-2", "3-1", "1-3", "4-1", "1-4", "1-0", "0-1"),
    EQUALIZER_LEVEL(IntStream.rangeClosed(0, 127).boxed().map(i -> {
        if (i < 64) {
            return "LP_" + i;
        } else if (i > 64) {
            return "HP_" + i;
        }
        return "FLAT";
    }).collect(Collectors.toList()));

    private final int min;
    private final int max;
    private final int offset;
    private final String[] values;

    Boundaries(List<String> values) {
        this(values.toArray(new String[0]));
    }

    Boundaries(String... values) {
        this(0, values.length - 1, 0, values);
    }

    Boundaries(int min, int max) {
        this(min, max, 0);
    }

    Boundaries(int min, int max, int offset) {
        this(min, max, offset, (String[]) null);
    }

    Boundaries(int min, int max, int offset, String... values) {
        this.min = min;
        this.max = max;
        this.offset = offset;
        this.values = values;
    }

    Boundaries(Boundaries other) {
        this(other.min, other.max, other.offset, other.values);
    }

    @Override
    public int getMin() {
        return min;
    }

    @Override
    public int getMax() {
        return max;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public String[] getValues() {
        return values;
    }
}
