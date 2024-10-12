package edisyn.synth.novationastation;

import edisyn.Synth;
import edisyn.gui.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static edisyn.gui.Style.COLOR_GLOBAL;
import static edisyn.synth.novationastation.Mappings.*;

public class UIBuilder {
    private final Synth synth;

    public UIBuilder(Synth synth) {
        this.synth = synth;
    }

    public void build() {
        // General PANEL
        JComponent generalPanel = new SynthPanel(synth);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(COLOR_GLOBAL()));
        hbox.addLast(addGeneral(Style.COLOR_A()));
        vbox.add(hbox);
        vbox.add(addOscillator(1, Style.COLOR_B()));
        vbox.add(addOscillator(2, Style.COLOR_B()));
        vbox.add(addOscillator(3, Style.COLOR_B()));
        vbox.add(addAmpAndPitchModulation(Style.COLOR_C()));
        vbox.add(addMixer(Style.COLOR_A()));
        generalPanel.add(vbox, BorderLayout.CENTER);
        synth.addTab("General", generalPanel);

        // Envelope, LFO, ARP PANEL
        JComponent envelopeLfoFilterPanel = new SynthPanel(synth);
        vbox = new VBox();
        vbox.add(addEnvelope(1, Style.COLOR_A()));
        vbox.add(addEnvelope(2, Style.COLOR_A()));
        vbox.add(addEnvelope(3, Style.COLOR_A()));
        hbox = new HBox();
        hbox.add(addLFO(1, Style.COLOR_B()));
        hbox.addLast(addLFO(2, Style.COLOR_B()));
        vbox.add(hbox);
        vbox.add(addFilter(Style.COLOR_C()));
        //vbox.add(addArp(Style.COLOR_D()));
        envelopeLfoFilterPanel.add(vbox, BorderLayout.CENTER);
        synth.addTab("Envs, LFOs, Filter", envelopeLfoFilterPanel);

        // ARP, EFFECTS PANEL
        JComponent arpEffectsPanel = new SynthPanel(synth);
        vbox = new VBox();
        vbox.add(addARP(Style.COLOR_A()));
        hbox = new HBox();
        hbox.add(addDelay(Style.COLOR_B()));
        hbox.addLast(addReverb(Style.COLOR_C()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addChorus(Style.COLOR_A()));
        hbox.addLast(addDistortion(Style.COLOR_B()));
        vbox.add(hbox);
        hbox = new HBox();
        hbox.add(addEqualizer(Style.COLOR_C()));
        hbox.addLast(addPan(Style.COLOR_A()));
        vbox.add(hbox);
        vbox.add(addVocoder(Style.COLOR_B()));

        arpEffectsPanel.add(vbox, BorderLayout.CENTER);
        synth.addTab("ARP, Effects", arpEffectsPanel);

        // DEVICE-GOODIES panel (non patch related)
        // TODO - nice addition for the future, to be completed though...
        /*
        JComponent devicePanel = new SynthPanel(synth);
        vbox = new VBox();

        vbox.add(addDeviceGoodies(Style.COLOR_A()));
        vbox.add(addDeviceDetails(Style.COLOR_B()));

        devicePanel.add(vbox, BorderLayout.CENTER);

        addTab("Device goodies", devicePanel);
        */

    }

    private JComponent addNameGlobal(Color color) {
        Category globalCategory = new Category(synth, "Novation A Station", color);

        HBox hbox = new HBox();

        VBox vbox = new VBox();
        HBox hbox2 = new HBox();
        hbox2.add(new PatchDisplay(synth, 4));
        vbox.add(hbox2);
        hbox.add(vbox);

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
    }

    private JComponent addGeneral(Color color) {
        Category categoryGeneral = new Category(synth, "General", color);

        HBox hbox = new HBox();
        VBox vbox = new VBox();
        vbox.add(createChooser("polyphony mode", POLYPHONY_MODE));
        vbox.add(createChooser("keysync phase", KEY_SYNC_PHASE));
        hbox.add(vbox);

        vbox = new VBox();
        vbox.add(createChooser("unison voices", UNISON_VOICES));
        hbox.add(vbox);
        hbox.add(createLabelledDial(List.of("unison", "detune"), UNISON_DETUNE, color));

        hbox.add(createLabelledDial(List.of("random", "detune"), OSCS_RANDOM_DETUNE, color));
        hbox.add(createLabelledDial(List.of("preglide", "semitones"), PREGLIDE_SEMITONES, color));
        hbox.add(createLabelledDial(List.of("program", "volume"), PROGRAM_VOLUME, color));

        categoryGeneral.add(hbox, BorderLayout.CENTER);
        return categoryGeneral;
    }

    private JComponent addAmpAndPitchModulation(Color color) {
        Category categoryGeneral = new Category(synth, "Portamento, Pitch & AMP modulation", color);

        HBox hbox = new HBox();
        VBox vbox = new VBox();
        vbox.add(createChooser("portamento mode", PORTAMENTO_MODE));
        hbox.add(vbox);
        hbox.add(createLabelledDial(List.of("portamento", "time"), PORTAMENTO_TIME, color));

        hbox.add(createLabelledDial(List.of("modwheel", "pitch depth"), OSCS_MODWHEEL_PITCH_DEPTH, color));
        hbox.add(createLabelledDial(List.of("aftertch", "pitch depth"), OSCS_AFTERTCH_PITCH_DEPTH, color));
        hbox.add(createLabelledDial(List.of("breath", "pitch depth"), OSCS_BREATH_PITCH_DEPTH, color));
        hbox.add(createLabelledDial(List.of("modwheel", "lfo1", "pitch depth"), OSCS_MODWHEEL_LFO1_PITCH_DEPTH, color));
        hbox.add(createLabelledDial(List.of("aftertch", "lfo1", "pitch depth"), OSCS_AFTERTCH_LFO1_PITCH_DEPTH, color));
        hbox.add(createLabelledDial(List.of("breath", "lfo1", "pitch depth"), OSCS_BREATH_LFO1_PITCH_DEPTH, color));
        hbox.add(createLabelledDial(List.of("modwheel", "amp depth"), OSCS_MODWHEEL_AMPLITUDE_DEPTH, color));
        hbox.add(createLabelledDial(List.of("aftertch", "amp depth"), OSCS_AFTERTCH_AMPLITUDE_DEPTH, color));
        hbox.add(createLabelledDial(List.of("breath", "amp depth"), OSCS_BREATH_AMPLITUDE_DEPTH, color));

        categoryGeneral.add(hbox, BorderLayout.CENTER);
        return categoryGeneral;
    }

    private JComponent addOscillator(final int osc, Color color) {
        Category category = new Category(synth, "Oscillator " + osc, color);
        category.makePasteable("osc");

        HBox hbox = new HBox();

        VBox vbox = new VBox();
        // NOTE - would probably be nicer to have synth integrated into the semitone dial [-24, +36]
        vbox.add(createChooser("octave", Mappings.find("OSC%d_OCTAVE", osc)));
        vbox.add(createChooser("waveform", Mappings.find("OSC%d_WAVEFORM", osc)));
        if (osc == 2) {
            vbox.add(createCheckBox("1->2 sync", OSC2_SYNCED_BY_1));
        }
        hbox.add(vbox);

        hbox.add(createLabelledDial("semitone", Mappings.find("OSC%d_SEMITONE", osc), color));
        hbox.add(createLabelledDial("detune", Mappings.find("OSC%d_DETUNE", osc), color));
        hbox.add(createLabelledDial(List.of("bendwheel", "depth"), Mappings.find("OSC%d_BENDWHEEL_AMOUNT", osc), color));
        hbox.add(createLabelledDial(List.of("modenv", "depth"), Mappings.find("OSC%d_ENV2_DEPTH", osc), color));
        hbox.add(createLabelledDial(List.of("lfo1", "depth"), Mappings.find("OSC%d_LFO1_DEPTH", osc), color));
        // TODO - add support for 'pwm source' (and related)
        hbox.add(createLabelledDial("PW", Mappings.find("OSC%d_PULSE_WIDTH", osc), color));
        hbox.add(createLabelledDial(List.of("modenv", "PW depth"), Mappings.find("OSC%d_ENV2_PULSE_WIDTH_MOD", osc), color));
        hbox.add(createLabelledDial(List.of("lfo2", "PW depth"), Mappings.find("OSC%d_LFO2_PULSE_WIDTH_MOD", osc), color));

        if (osc == 3) {
            hbox.add(createLabelledDial(List.of("FM", "Level"), FM_FIXED_LEVEL, color));
            hbox.add(createLabelledDial(List.of("FMenv", "depth"), FM_ENVELOPE_DEPTH, color));
        }

        category.add(hbox, BorderLayout.CENTER);
        return category;
    }

    private JComponent addMixer(Color color)
    {
        Category category = new Category(synth, "Mixer", color);

        HBox hbox = new HBox();
        hbox.add(createLabelledDial("osc1", MIXER_OSC1, color));
        hbox.add(createLabelledDial("osc2", MIXER_OSC2, color));
        hbox.add(createLabelledDial("osc3", MIXER_OSC3, color));
        hbox.add(createLabelledDial("noise", MIXER_NOISE, color));
        hbox.add(createLabelledDial(List.of("1*2", "ring"), MIXER_RING_MOD, color));
        hbox.add(createLabelledDial(List.of("external", "input"), MIXER_EXTERNAL, color));

        category.add(hbox, BorderLayout.CENTER);
        return category;
    }

    private JComponent addFilter(Color color) {
        Category category = new Category(synth, "Filter", color);
        HBox mainhbox = new HBox();

        VBox vbox1 = new VBox();
        vbox1.add(createChooser("type", FILTER_TYPE));
        mainhbox.add(vbox1);

        VBox vbox2 = new VBox();
        HBox hbox = new HBox();
        hbox.add(createLabelledDial("frequency", FILTER_FREQ, color));
        hbox.add(createLabelledDial("resonance", FILTER_RESONANCE, color));
        hbox.add(createLabelledDial("overdrive", FILTER_OVERDRIVE, color));
        hbox.add(createLabelledDial("key track", FILTER_KEY_TRACK, color));
        hbox.add(createLabelledDial(List.of("modwheel", "depth"), FILTER_MODWHEEL_FREQUENCY_DEPTH, color));
        hbox.add(createLabelledDial(List.of("modenv", "depth"), FILTER_ENV2_DEPTH, color));
        hbox.add(createLabelledDial(List.of("lfo2", "depth"), FILTER_LFO2_DEPTH, color));
        vbox2.add(hbox);

        hbox = new HBox();
        hbox.add(createLabelledDial("Q normalize", FILTER_Q_NORMALIZE, color));
        hbox.add(createLabelledDial(List.of("aftertch", "depth"), FILTER_AFTERTCH_FREQUENCY_DEPTH, color));
        hbox.add(createLabelledDial(List.of("breath", "depth"), FILTER_BREATH_FREQUENCY_DEPTH, color));
        hbox.add(createLabelledDial(List.of("modwheel", "lfo2 depth"), FILTER_MODWHEEL_LFO2_FREQUENCY_DEPTH, color));
        hbox.add(createLabelledDial(List.of("aftertch", "lfo2 depth"), FILTER_AFTERTCH_LFO2_FREQUENCY_DEPTH, color));
        hbox.add(createLabelledDial(List.of("breath", "lfo2 depth"), FILTER_BREATH_LFO2_FREQUENCY_DEPTH, color));
        vbox2.add(hbox);
        mainhbox.add(vbox2);

        category.add(mainhbox, BorderLayout.CENTER);
        return category;
    }

    private JComponent addEnvelope(final int envelope, Color color)
    {
        String categoryName = envelope == 1 ? "Amp" : (envelope == 2 ? "Mod" : "FM");
        Category category = new Category(synth, categoryName + " Envelope", color);
        // TODO - to be verified
        // category.makePasteable("env");

        HBox hbox = new HBox();
        VBox vbox = new VBox();
        vbox.add(createChooser("env trigger", Mappings.find("ENVELOPE%d_TRIGGER", envelope)));
        hbox.add(vbox);

        Mappings velocityDepth = Mappings.find("ENVELOPE%d_VELOCITY_DEPTH", envelope);
        hbox.add(createLabelledDial("velocity depth", velocityDepth, color));

        Mappings mappingsAttack = Mappings.find("ENVELOPE%d_ATTACK", envelope);
        hbox.add(createLabelledDial("attack", mappingsAttack, color));

        Mappings mappingsDecay = Mappings.find("ENVELOPE%d_DECAY", envelope);
        hbox.add(createLabelledDial("decay", mappingsDecay, color));

        if (envelope == 3) {
            // AD: for FM envelope - that's it
            JComponent comp = new EnvelopeDisplay(synth, Color.red,
                    new String[] { null, mappingsAttack.getKey(), mappingsDecay.getKey(), null, null },
                    new String[] { null, null, null, null, null },
                    new double[] { 0, 0.25/127.0, 0.25 / 127.0,  0.25, 0.25},
                    new double[] { 0, 1.0, 1.0 / 127.0, 0, 0 });
            hbox.addLast(comp);
        } else {
            // ADSR: for AMP & MOD env - add sustain/release
            Mappings mappingsSustain = Mappings.find("ENVELOPE%d_SUSTAIN", envelope);
            hbox.add(createLabelledDial("sustain", mappingsSustain, color));

            Mappings mappingsRelease = Mappings.find("ENVELOPE%d_RELEASE", envelope);
            hbox.add(createLabelledDial("release", mappingsRelease, color));

            // ADSR
            JComponent comp = new EnvelopeDisplay(synth, Color.red,
                    new String[] { null, mappingsAttack.getKey(), mappingsDecay.getKey(), null, mappingsRelease.getKey() },
                    new String[] { null, null, mappingsSustain.getKey(), mappingsSustain.getKey(), null },
                    new double[] { 0, 0.25/127.0, 0.25 / 127.0,  0.25, 0.25/127.0},
                    new double[] { 0, 1.0, 1.0 / 127.0, 1.0/127.0, 0 });
            hbox.addLast(comp);
        }

        category.add(hbox, BorderLayout.CENTER);
        return category;
    }

    private JComponent addLFO(final int lfo, Color color) {
        Category category = new Category(synth, "LFO " + lfo, color);
        // TODO - to be verified
        //category.makePasteable("lfo");

        HBox hbox = new HBox();
        VBox vbox = new VBox();
        vbox.add(createChooser("waveform", Mappings.find("LFO%d_WAVEFORM", lfo)));
        HBox hboxInner = new HBox();
        hboxInner.add(createCheckBox("lock", Mappings.find("LFO%d_LOCK", lfo)));
        hboxInner.add(createCheckBox("delay multi", Mappings.find("LFO%d_DELAY_MULTI", lfo)));
        vbox.add(hboxInner);
        vbox.add(createCheckBox("key sync", Mappings.find("LFO%d_KEY_SYNC", lfo)));
        vbox.add(createCheckBox("key sync - phase shift", Mappings.find("LFO%d_KEY_SYNC_PHASE_SHIFT", lfo)));
        hbox.add(vbox);

        // TODO - single/dynamic dial for both sync and non-sync ?
        hbox.add(createLabelledDial(List.of("speed", "(non-sync)"), Mappings.find("LFO%d_SPEED_NON_SYNC", lfo), color));
        hbox.add(createLabelledDial(List.of("speed", "(sync)"), Mappings.find("LFO%d_SPEED_SYNC", lfo), color));
        hbox.add(createLabelledDial("delay", Mappings.find("LFO%d_DELAY", lfo), color));

        category.add(hbox, BorderLayout.CENTER);
        return category;
    }

    // TODO
    private JComponent addARP(Color color) {
        Category category = new Category(synth, "ARP", color);

        HBox hbox = new HBox();

        VBox vbox = new VBox();
        vbox.add(createCheckBox("enable", ARP_ON_OFF));
        vbox.add(createCheckBox("key sync", ARP_KEY_SYNC));
        vbox.add(createCheckBox("latch", ARP_LATCH));
        hbox.add(vbox);

        vbox = new VBox();
        vbox.add(createChooser("octaves", ARP_OCTAVES));
        vbox.add(createChooser("note destination", ARP_NOTE_DESTINATION));
        hbox.add(vbox);

        hbox.add(createLabelledDial("sync", ARP_SYNC, color));
        hbox.add(createLabelledDial("rate", ARP_RATE, color));
        hbox.add(createLabelledDial("pattern", ARP_PATTERN, color));
        hbox.add(createLabelledDial("gate time", ARP_GATE_TIME, color));

        category.add(hbox);
        return category;
    }

    private JComponent addDelay(Color color) {
        Category category = new Category(synth, "delay", color);
        HBox hbox = new HBox();

        hbox.add(createLabelledDial(List.of("send", "level"), DELAY_SEND_LEVEL, color));
        hbox.add(createLabelledDial("modwheel", DELAY_SEND_MODWHEEL, color));

        // TODO - single/dynamic dial for both sync and non-sync ?
        hbox.add(createLabelledDial(List.of("time", "(sync)"), DELAY_TIME_SYNC, color));
        hbox.add(createLabelledDial(List.of("time", "(non-sync)"), DELAY_TIME_NON_SYNC, color));

        hbox.add(createLabelledDial("feedback", DELAY_FEEDBACK, color));

        // TODO - dropdown here ? or different dial ?
        hbox.add(createLabelledDial("ratio", DELAY_RATIO, color));
        hbox.add(createLabelledDial(List.of("stereo", "width"), DELAY_STEREO_WIDTH, color));

        category.add(hbox);
        return category;
    }

    private JComponent addReverb(Color color) {
        Category category = new Category(synth, "reverb", color);

        HBox hbox = new HBox();
        VBox vbox = new VBox();
        vbox.add(createChooser("type", REVERB_TYPE));
        hbox.add(vbox);
        hbox.add(createLabelledDial(List.of("send", "level"), REVERB_SEND_LEVEL, color));
        hbox.add(createLabelledDial("modwheel", REVERB_SEND_MODWHEEL, color));
        hbox.add(createLabelledDial("decay", REVERB_DECAY, color));
        category.add(hbox);

        return category;
    }

    private JComponent addChorus(Color color) {
        Category category = new Category(synth, "chorus", color);
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        vbox.add(createChooser("type", CHORUS_TYPE));
        vbox.add(createChooser("global sync", CHORUS_GLOBAL_SYNC));
        hbox.add(vbox);
        hbox.add(createLabelledDial(List.of("send", "level"), CHORUS_SEND_LEVEL, color));
        hbox.add(createLabelledDial("modwheel", CHORUS_SEND_MODWHEEL, color));

        // TODO - dropdown here ? or different dial ?
        hbox.add(createLabelledDial(List.of("rate", "(sync)"), CHORUS_RATE_SYNC, color));
        // TODO - single/dynamic dial for both sync and non-sync ?
        hbox.add(createLabelledDial(List.of("rate", "(non-sync)"), CHORUS_RATE_NON_SYNC, color));

        hbox.add(createLabelledDial("feedback", CHORUS_FEEDBACK, color));
        hbox.add(createLabelledDial("depth", CHORUS_MOD_DEPTH, color));
        hbox.add(createLabelledDial(List.of("centre", "point"), CHORUS_MOD_CENTRE_POINT, color));

        category.add(hbox);
        return category;
    }

    private JComponent addDistortion(Color color) {
        Category category = new Category(synth, "distortion", color);
        HBox hbox = new HBox();

        hbox.add(createLabelledDial("modwheel", DISTORTION_MODWHEEL, color));
        hbox.add(createLabelledDial("compensation", DISTORTION_COMPENSATION, color));

        category.add(hbox);
        return category;
    }

    private JComponent addEqualizer(Color color) {
        Category category = new Category(synth, "equalizer", color);

        HBox hbox = new HBox();

        VBox vbox = new VBox();
        vbox.add(createChooser("global sync", EQUALIZER_GLOBAL_SYNC));
        hbox.add(vbox);

        hbox.add(createLabelledDial("level", EQUALIZER_LEVEL, color));
        hbox.add(createLabelledDial("level", EQUALIZER_LEVEL, color));
        hbox.add(createLabelledDial("frequency", EQUALIZER_LEVEL, color));
        // TODO - dropdown here ? or different dial ?
        hbox.add(createLabelledDial(List.of("rate", "(sync)"), EQUALIZER_RATE_SYNC, color));
        // TODO - single/dynamic dial for both sync and non-sync ?
        hbox.add(createLabelledDial(List.of("rate", "(non-sync)"), EQUALIZER_RATE_NON_SYNC, color));
        hbox.add(createLabelledDial("mod depth", EQUALIZER_MOD_DEPTH, color));

        category.add(hbox);
        return category;
    }


    private JComponent addPan(Color color) {
        Category category = new Category(synth, "panning", color);

        HBox hbox = new HBox();

        VBox vbox = new VBox();
        vbox.add(createChooser("global sync", PANNING_GLOBAL_SYNC));
        hbox.add(vbox);

        hbox.add(createLabelledDial("position", PANNING_POSITION, color));

        // TODO - dropdown here ? or different dial ?
        hbox.add(createLabelledDial(List.of("rate", "(sync)"), PANNING_RATE_SYNC, color));

        // TODO - single/dynamic dial for both sync and non-sync ?
        hbox.add(createLabelledDial(List.of("rate", "(non-sync)"), PANNING_RATE_NON_SYNC, color));
        hbox.add(createLabelledDial("depth", PANNING_MOD_DEPTH, color));

        category.add(hbox);
        return category;
    }

    private JComponent addVocoder(Color color) {
        Category category = new Category(synth, "vocoder", color);

        HBox hbox = new HBox();
        VBox vbox = new VBox();
        vbox.add(createChooser("sibilance type", VOCODER_SIBILANCE_TYPE));
        hbox.add(vbox);
        hbox.add(createLabelledDial("balance", VOCODER_BALANCE, color));
        hbox.add(createLabelledDial(List.of("stereo", "width"), VOCODER_STEREO_WIDTH, color));
        hbox.add(createLabelledDial(List.of("sibilance", "level"), VOCODER_SIBILANCE_LEVEL, color));

        category.add(hbox);
        return category;
    }

    private JComponent addDeviceGoodies(Color color) {
        Category category = new Category(synth, "Goodies", color);

        JComponent comp;

        HBox hbox = new HBox();
        comp = new LabelledDial("device volume", synth, "devicevolume", color, 0, 127);
        hbox.add(comp);

        category.add(hbox);
        return category;
    }

    private JComponent addDeviceDetails(Color color) {
        Category category = new Category(synth, "Details", color);

        JComponent comp;

        HBox hbox = new HBox();
        // TODO - revisit how to present synth. Plain key/value (same for the other params)
        comp = new ReadOnlyString("sw version", synth, "swversionstring", 1);
        hbox.add(comp);

        category.add(hbox);
        return category;
    }

    private Chooser createChooser(String label, Mappings mappings) {
        Restrictions restrictions = mappings.getRestrictions();
        // sanity
        if (restrictions.getValues() == null) {
            throw new IllegalStateException("expecting values for a chooser component !");
        }
        return new Chooser(label, synth,
                mappings.getKey(),
                mappings.getRestrictions().getValues()) {
//            @Override
//            // a matter of taste, I guess..
//            public boolean isLabelToLeft() { return true; }
        };
    }

    private LabelledDial createLabelledDial(String label, Mappings mappings, Color color) {
        return createLabelledDial(List.of(label), mappings, color);
    }

    private LabelledDial createLabelledDial(List<String> labels, Mappings mappings, Color color) {
        Restrictions restrictions = mappings.getRestrictions();
        LabelledDial result = new LabelledDial(labels.get(0), synth,
                mappings.getKey(),
                color, restrictions.getMin(), restrictions.getMax(), restrictions.getOffset());
        for (int index = 1; index < labels.size(); ++index) {
            result.addAdditionalLabel(labels.get(index));
        }
        return result;
    }

    private JComponent createCheckBox(String label, Mappings mappings) {
        return new CheckBox(label, synth, mappings.getKey());
    }
}
