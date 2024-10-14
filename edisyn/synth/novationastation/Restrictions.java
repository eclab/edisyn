package edisyn.synth.novationastation;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// TODO - rename to boundaries (..) ?
public enum Restrictions implements Restriction {
    // non param related
    BANKS(IntStream.rangeClosed(1, 4).boxed().map(String::valueOf).toList()),
    PATCH_NUMBERS(IntStream.rangeClosed(0, 99).boxed().map(String::valueOf).toList()),
    // param-related: some are generic
    NONE(0, 127, 0),
    CENTRIC_24(52, 76, 64),
    CENTRIC_100(14, 114, 64),
    CENTRIC_127(0, 127, 64),
    BOOLEAN(0, 1, 0),
    // param-related: some are specific
    POLYPHONY_MODE("mono", "mono autoglide", "poly", "poly voice stealing"),
    PORTAMENTO_MODE("exponential", "linear"),
    UNISON_VOICES("off", "2","3","4","5","6","7", "8"),
    OSC_OCTAVES("-1", "0", "1", "2"),
    OSC_WAVEFORMS("sine", "triangle", "saw", "square/pulse"),
    ENV_TRIGGERS("single", "multi"),
    FILTER_TYPES("12dB/octave", "24dB/octave"),
    LFO_WAVE_FORMS("triangle", "saw", "square", "S&H"),
    SIBILANCE_TYPES("high pass", "noise"),
    REVERB_TYPES("echo chamber", "small room", "small hall", "large room", "large hall", "grand hall"),
    CHORUS_TYPES("chorus", "phaser"),
    KEYSYNC_PHASE("free-running", "0", "24", "48", "72", "96", "120", "144", "168", "192", "216", "240", "264", "288", "312", "336"),
    ARP_OCTAVES("1", "2", "3", "4"),
    ARP_NOTE_DESTINATION("internal", "external", "internal+external", "external+played"),
    ARP_PATTERN("up", "down", "ud1", "ud2", "order", "random"),
    EQUALIZER_GLOBAL_SYNC("off", "low", "mid", "high"),
    CHORUS_GLOBAL_SYNC("off", "left", "center", "right"),
    PANNING_GLOBAL_SYNC(CHORUS_GLOBAL_SYNC),
    SYNC_RATES("NA", "32T", "32", "16T", "16", "8T", "16D", "8", "4T", "8D", "4", "2T", "4D", "2", "1T", "2D",
                       "1", "2T", "1D", "2", "4T", "3", "5T", "4", "4D", "7T", "5", "8T", "6", "7", "7D", "8", "9", "10D", "12"),
    ARP_SYNC_RATES(List.of(SYNC_RATES.values).subList(0, 17).toArray(new String[0])),
    DELAY_RATIO("1-1", "4-3", "3-4", "3-2", "2-3", "2-1", "1-2", "3-1", "1-3", "4-1", "1-4", "1-0", "0-1"),
    EQUALIZER_LEVEL(IntStream.rangeClosed(0, 127).boxed().map(i -> {
        if (i < 64) {
            return "LP_" + i;
        } else if (i > 64) {
            return "HP_" + i;
        }
        return "FLAT";
    }).toList());

    private final int min;
    private final int max;
    // note: hmm, not really belonging here(not really model related, more UI related)
    private final int offset;
    private final String[] values;

    Restrictions(List<String> values) {
        this(values.toArray(new String[0]));
    }

    Restrictions(String... values) {
        this(0, values.length - 1, 0, values);
    }

    Restrictions(int min, int max, int offset) {
        this(min, max, offset, (String[]) null);
    }

    Restrictions(int min, int max, int offset, String... values) {
        this.min = min;
        this.max = max;
        this.offset = offset;
        this.values = values;
    }

    Restrictions(Restrictions other) {
        this(other.min, other.max, other.offset, other.values);
    }

    @Override
    public Integer getMin() {
        return min;
    }

    @Override
    public Integer getMax() {
        return max;
    }

    @Override
    public Integer getOffset() {
        return offset;
    }

    @Override
    public String[] getValues() {
        return values;
    }
}
