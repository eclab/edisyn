package edisyn.synth.novationastation;

import edisyn.Synth;
import edisyn.gui.*;

import javax.swing.*;
import java.awt.*;

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
        vbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        vbox.add(addGeneral(Style.COLOR_B()));
        vbox.add(addAdvanced(Style.COLOR_C()));
        generalPanel.add(vbox, BorderLayout.CENTER);
        synth.addTab("General", generalPanel);

        // OSCx, Mixer, Filter PANEL
        JComponent OscMixerFilterPanel = new SynthPanel(synth);
        vbox = new VBox();
        vbox.add(addOscillator(1, Style.COLOR_A()));
        vbox.add(addOscillator(2, Style.COLOR_A()));
        vbox.add(addOscillator(3, Style.COLOR_A()));
        vbox.add(addMixer(Style.COLOR_B()));
        vbox.add(addFilter(Style.COLOR_C()));
        OscMixerFilterPanel.add(vbox, BorderLayout.CENTER);
        synth.addTab("Oscs, Mix, Filter", OscMixerFilterPanel);

        // Envelope, LFO, ARP PANEL
        JComponent envelopeLfoArpPanel = new SynthPanel(synth);
        vbox = new VBox();
        vbox.add(addEnvelope(1, Style.COLOR_B()));
        vbox.add(addEnvelope(2, Style.COLOR_B()));
        vbox.add(addLFO(1, Style.COLOR_C()));
        vbox.add(addLFO(2, Style.COLOR_C()));
        //vbox.add(addArp(Style.COLOR_D()));
        envelopeLfoArpPanel.add(vbox, BorderLayout.CENTER);
        synth.addTab("Envs, LFOs, ARP", envelopeLfoArpPanel);

        // EFFECTS PANEL
        JComponent effectsPanel = new SynthPanel(synth);
        vbox = new VBox();
        vbox.add(addDelay(Style.COLOR_A()));
        vbox.add(addReverb(Style.COLOR_B()));
        vbox.add(addChorus(Style.COLOR_C()));
        vbox.add(addDistortion(Style.COLOR_A()));
        vbox.add(addPan(Style.COLOR_B()));
        vbox.add(addVocoder(Style.COLOR_C()));

        // TODO add equalizer controls
        effectsPanel.add(vbox, BorderLayout.CENTER);
        synth.addTab("FXs", effectsPanel);

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

        VBox mainVBox = new VBox();
        HBox hbox = new HBox();
        //
        VBox vbox = new VBox();
        vbox.add(createChooser("polyphony mode", POLYPHONY_MODE));
        vbox.add(createChooser("unison voices", UNISON_VOICES));
        vbox.add(createLabelledDial("unison detune", UNISON_DETUNE, color), BorderLayout.EAST);
        hbox.add(vbox);

        vbox = new VBox();
        vbox.add(createChooser("keysync phase", KEY_SYNC_PHASE));
        vbox.add(createChooser("portamento mode", PORTAMENTO_MODE));
        vbox.add(createLabelledDial("portamento time", PORTAMENTO_TIME, color), BorderLayout.EAST);
        hbox.add(vbox);

        mainVBox.add(hbox);

        categoryGeneral.add(mainVBox, BorderLayout.WEST);
        return categoryGeneral;
    }

    private JComponent addAdvanced(Color color) {
        Category categoryGeneral = new Category(synth, "Advanced", color);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 3));
        // row1
        panel.add(createLabelledDial("random detune", OSCS_RANDOM_DETUNE, color));
        panel.add(createLabelledDial("preglide semitones", PREGLIDE_SEMITONES, color));
        panel.add(createLabelledDial("program output offset", PROGRAM_VOLUME, color));
        // row2
        panel.add(createLabelledDial("modwheel pitch depth", OSCS_MODWHEEL_PITCH_DEPTH, color));
        panel.add(createLabelledDial("aftertouch pitch depth", OSCS_MODWHEEL_PITCH_DEPTH, color));
        panel.add(createLabelledDial("breath pitch depth", OSCS_MODWHEEL_PITCH_DEPTH, color));
        // row3
        panel.add(createLabelledDial("modwheel lfo1 pitch depth", OSCS_MODWHEEL_PITCH_DEPTH, color));
        panel.add(createLabelledDial("aftertouch lfo1 pitch depth", OSCS_MODWHEEL_PITCH_DEPTH, color));
        panel.add(createLabelledDial("breath lfo1 pitch depth", OSCS_MODWHEEL_PITCH_DEPTH, color));
        // row4
        panel.add(createLabelledDial("modwheel amplitude depth", OSCS_MODWHEEL_PITCH_DEPTH, color));
        panel.add(createLabelledDial("aftertouch amplitude depth", OSCS_MODWHEEL_PITCH_DEPTH, color));
        panel.add(createLabelledDial("breath amplitude depth", OSCS_MODWHEEL_PITCH_DEPTH, color));

        categoryGeneral.add(panel, BorderLayout.WEST);
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
        hbox.add(createLabelledDial("pulse width", Mappings.find("OSC%d_PULSE_WIDTH", osc), color));
        // TODO - add support for 'pwm source' (and related)
        hbox.add(createLabelledDial("mod env depth", Mappings.find("OSC%d_ENV2_DEPTH", osc), color));
        hbox.add(createLabelledDial("lfo1 depth", Mappings.find("OSC%d_LFO1_DEPTH", osc), color));
        // TODO - verify restrictions first before adding synth one...
//        hbox.add(createLabelledDial("bendwheel amount", Mappings.find("OSC%d_BENDWHEEL_AMOUNT", osc), color));

        // TODO
//        if (osc == 3) {
//            comp = new LabelledDial("fm level", synth, "osc" + osc + "fmlevel", color, 0, 127);
//            hbox.add(comp);
//
//            comp = new LabelledDial("fm env depth", synth, "osc" + osc + "fmenvdepth", color, 0, 127);
//            hbox.add(comp);
//
//            and others... (check NRPN)
//        }

        category.add(hbox, BorderLayout.CENTER);
        return category;
    }

    private JComponent addMixer(Color color)
    {
        Category category = new Category(synth, "Mixer", color);

        HBox hbox = new HBox();
        hbox.add(createLabelledDial("oscillator1", MIXER_OSC1, color));
        hbox.add(createLabelledDial("oscillator2", MIXER_OSC2, color));
        hbox.add(createLabelledDial("oscillator3", MIXER_OSC3, color));
        hbox.add(createLabelledDial("noise", MIXER_NOISE, color));
        hbox.add(createLabelledDial("1*2 ring", MIXER_RING_MOD, color));
        hbox.add(createLabelledDial("external input", MIXER_EXTERNAL, color));

        category.add(hbox, BorderLayout.CENTER);
        return category;
    }

    private JComponent addFilter(Color color) {
        Category category = new Category(synth, "Filter", color);
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        vbox.add(createChooser("type", FILTER_TYPE));
        hbox.add(vbox);

        hbox.add(createLabelledDial("frequency", FILTER_FREQ, color));
        hbox.add(createLabelledDial("resonance", FILTER_RESONANCE, color));
        hbox.add(createLabelledDial("overdrive", FILTER_OVERDRIVE, color));
        // enable once NRPN is supported
        hbox.add(createLabelledDial("key track", FILTER_KEY_TRACK, color));
        hbox.add(createLabelledDial("mod env depth", FILTER_ENV2_DEPTH, color));
        hbox.add(createLabelledDial("lfo2 depth", FILTER_LFO2_DEPTH, color));

        // TODO
        // and others... (check NRPN)

        category.add(hbox, BorderLayout.CENTER);
        return category;
    }

    private JComponent addEnvelope(final int envelope, Color color)
    {
        Category category = new Category(synth, (envelope == 1 ? "Amp" : "Mod") + " Envelope", color);
        // TODO - to be verified
        // category.makePasteable("env");

        HBox hbox = new HBox();
        VBox vbox = new VBox();
        vbox.add(createChooser("env trigger", Mappings.find("ENVELOPE%d_TRIGGER", envelope)));
        hbox.add(vbox);

        Mappings mappingsAttack = Mappings.find("ENVELOPE%d_ATTACK", envelope);
        hbox.add(createLabelledDial("attack", mappingsAttack, color));

        Mappings mappingsDecay = Mappings.find("ENVELOPE%d_DECAY", envelope);
        hbox.add(createLabelledDial("decay", mappingsDecay, color));

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

        // TODO - FM envelope (AD), NRPN based

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
        hbox.add(vbox);

        // TODO - single/dynamic dial for both sync and non-sync ?
        hbox.add(createLabelledDial("speed (non-sync)", Mappings.find("LFO%d_SPEED_NON_SYNC", lfo), color));
        hbox.add(createLabelledDial("speed (sync)", Mappings.find("LFO%d_SPEED_SYNC", lfo), color));
        hbox.add(createLabelledDial("delay", Mappings.find("LFO%d_DELAY", lfo), color));

        category.add(hbox, BorderLayout.CENTER);
        return category;
    }

    // TODO
   /*
   private JComponent addARP(Color color) {
        Category category = new Category(synth, "ARP", color);

        JComponent comp;
        HBox hbox = new HBox();

        VBox vbox = new VBox();
        comp = new Chooser("pattern", synth, ARP_PATTERN.getKey(), ARP_PATTERNS);
        vbox.add(comp);
        comp = new Chooser("rate", synth, ARP_RATE.getKey(), ARP_RATES);
        vbox.add(comp);
        hbox.add(vbox);

        category.add(hbox);
        return category;
    }
    */

    private Component addDelay(Color color) {
        Category category = new Category(synth, "delay", color);
        HBox hbox = new HBox();

        hbox.add(createLabelledDial("send level", DELAY_SEND_LEVEL, color));
        hbox.add(createLabelledDial("modwheel", DELAY_SEND_MODWHEEL, color));

        // TODO - single/dynamic dial for both sync and non-sync ?
        hbox.add(createLabelledDial("time (sync)", DELAY_TIME_SYNC, color));
        hbox.add(createLabelledDial("time (non-sync)", DELAY_TIME_NON_SYNC, color));

        hbox.add(createLabelledDial("feedback", DELAY_FEEDBACK, color));

        // TODO - dropdown here ? or different dial ?
        hbox.add(createLabelledDial("ratio", DELAY_RATIO, color));
        hbox.add(createLabelledDial("stereo width", DELAY_STEREO_WIDTH, color));

        category.add(hbox);
        return category;
    }

    private Component addReverb(Color color) {
        Category category = new Category(synth, "reverb", color);
        HBox hbox0 = new HBox();
        hbox0.add(createChooser("type", REVERB_TYPE));

        HBox hbox1 = new HBox();
        hbox1.add(createLabelledDial("send level", REVERB_SEND_LEVEL, color));
        hbox1.add(createLabelledDial("modwheel", REVERB_SEND_MODWHEEL, color));
        hbox1.add(createLabelledDial("decay", REVERB_DECAY, color));

        category.add(hbox0, BorderLayout.NORTH);
        category.add(hbox1, BorderLayout.SOUTH);
        return category;
    }

    private Component addChorus(Color color) {
        Category category = new Category(synth, "chorus", color);
        HBox hbox0 = new HBox();
        hbox0.add(createChooser("type", CHORUS_TYPE));

        HBox hbox1 = new HBox();
        hbox1.add(createLabelledDial("send level", CHORUS_SEND_LEVEL, color));
        hbox1.add(createLabelledDial("modwheel", CHORUS_SEND_MODWHEEL, color));

        // TODO - single/dynamic dial for both sync and non-sync ?
        hbox1.add(createLabelledDial("rate (sync)", CHORUS_RATE_SYNC, color));
        hbox1.add(createLabelledDial("rate (non-sync)", CHORUS_RATE_NON_SYNC, color));

        hbox1.add(createLabelledDial("feedback", CHORUS_FEEDBACK, color));
        hbox1.add(createLabelledDial("depth", CHORUS_MOD_DEPTH, color));
        hbox1.add(createLabelledDial("centre point", CHORUS_MOD_CENTRE_POINT, color));

        category.add(hbox0, BorderLayout.NORTH);
        category.add(hbox1, BorderLayout.SOUTH);
        return category;
    }

    private Component addDistortion(Color color) {
        Category category = new Category(synth, "distortion", color);
        HBox hbox = new HBox();

        hbox.add(createLabelledDial("modwheel", DISTORTION_MODWHEEL, color));
        hbox.add(createLabelledDial("compensation", DISTORTION_COMPENSATION, color));

        category.add(hbox);
        return category;
    }

    private Component addPan(Color color) {
        Category category = new Category(synth, "panning", color);

        HBox hbox = new HBox();

        hbox.add(createLabelledDial("position", PANNING_POSITION, color));

        // TODO - dropdown here ? or different dial ?
        hbox.add(createLabelledDial("rate (sync)", PANNING_RATE_SYNC, color));

        // TODO - single/dynamic dial for both sync and non-sync ?
        hbox.add(createLabelledDial("rate (non-sync)", PANNING_RATE_NON_SYNC, color));
        hbox.add(createLabelledDial("depth", PANNING_MOD_DEPTH, color));

        // TODO - add global sync

        category.add(hbox);
        return category;
    }

    private Component addVocoder(Color color) {
        Category category = new Category(synth, "vocoder", color);

        HBox hbox0 = new HBox();
        hbox0.add(createChooser("sibilance type", VOCODER_SIBILANCE_TYPE));

        HBox hbox1 = new HBox();
        hbox1.add(createLabelledDial("balance", VOCODER_BALANCE, color));
        hbox1.add(createLabelledDial("stereo width", VOCODER_STEREO_WIDTH, color));
        hbox1.add(createLabelledDial("sibilance level", VOCODER_SIBILANCE_LEVEL, color));

        category.add(hbox0, BorderLayout.NORTH);
        category.add(hbox1, BorderLayout.SOUTH);
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
            @Override
            // a matter of taste, I guess..
            public boolean isLabelToLeft() { return true; }
        };
    }

    private LabelledDial createLabelledDial(String label, Mappings mappings, Color color) {
        Restrictions restrictions = mappings.getRestrictions();
        return new LabelledDial(label, synth,
                mappings.getKey(),
                color, restrictions.getMin(), restrictions.getMax(), restrictions.getOffset());
    }

    private JComponent createCheckBox(String label, Mappings mappings) {
        return new CheckBox(label, synth, mappings.getKey());
    }
}
