package edisyn.synth.novationastation;

// TODO - rename to boundaries (..) ?
public enum Restrictions implements Restriction {
    // some are generic
    NONE(),
    CENTRIC_24(52, 76, 64),
    CENTRIC_100(14, 114, 64),
    CENTRIC_127(0, 127, 64),
    // some are specific
    POLYPHONY_MODE("mono", "mono, autoglide", "poly", "poly, voice stealing"),
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
    KEYSYNC_PHASE("free-running", "0", "24", "48", "72", "96", "120", "144", "168", "192", "216", "240", "264", "288", "312", "336");

    private final int min;
    private final int max;
    // note: hmm, not really belonging here(not really model related, more UI related)
    private final int offset;
    private final String[] values;

    Restrictions() {
        this((String[]) null);
    }

    Restrictions(String... values) {
        this(0, 127, 0, values);
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
