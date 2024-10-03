/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.novationastation;

import edisyn.*;
import edisyn.gui.*;

import java.awt.*;
import javax.swing.*;
import java.util.*;
import java.util.stream.IntStream;
import javax.sound.midi.*;

public class NovationAStation extends Synth {
    private static final String[] BANKS = IntStream.rangeClosed(1, 4).boxed().map(String::valueOf).toList().toArray(new String[0]);
    private static final String[] PATCH_NUMBERS = IntStream.rangeClosed(0, 99).boxed().map(String::valueOf).toList().toArray(new String[0]);
    private static final String[] PORTAMENTO_MODES = {"exponential", "linear"};
    private static final String[] UNISON_VOICES = {"off", "2","3","4","5","6","7", "8"};
    private static final String[] POLYPHONY_MODES = {"mono", "mono autoglide", "poly", "poly with stealing"};
    private static final String[] OSC_WAVE_FORMS = { "sine", "triangle", "saw", "square/pulse"};
    private static final String[] OSC_OCTAVES = { "-1", "0", "1", "2"};
    private static final String[] FILTER_TYPES = {"12dB/octave", "24dB/octave"};
    private static final String[] ENV_TRIGGERS = { "single", "multi"};
    private static final String[] LFO_WAVE_FORMS = { "triangle", "saw", "square", "S&H"};
    private static final String[] ARP_CONDITION = { "off", "on: no latch, no keysync", "on: no latch, keysync", "on: latch, no keysync", "on: latch, keysync"};
    private static final String[] SYNC_RATES = { "non-sync", "32T", "32", "16T", "16", "8T", "16.", "8", "4T", "8.", "4", "2T", "4.", "2", "1T", "2.",
            "1", "2T", "1.", "2", "4T", "3", "5T", "4", "4.", "7T", "5", "8T", "6", "7", "7.", "8", "9", "10.", "12"};
    private static final String[] ARP_RATES = SYNC_RATES;
    private static final String[] DELAY_RATIOS = { "1-1", "4-3", "3-4", "3-2", "2-3", "2-1", "1-2", "3-1", "1-3", "4-1", "1-4", "1-0", "0-1"};
    private static final String[] ARP_PATTERNS = { "up", "down", "updown1", "updown2", "order", "rand"};

    public NovationAStation() {
        /// SOUND PANEL
        JComponent soundPanel = new SynthPanel(this);
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        hbox.add(addNameGlobal(Style.COLOR_GLOBAL()));
        hbox.addLast(addGeneral(Style.COLOR_B()));
        vbox.add(hbox);

        soundPanel.add(vbox, BorderLayout.CENTER);
        addTab("General", soundPanel);

        vbox.add(addOscillator(1, Style.COLOR_A()));
        vbox.add(addOscillator(2, Style.COLOR_A()));
        vbox.add(addOscillator(3, Style.COLOR_A()));

        vbox.add(addMixer(Style.COLOR_B()));
        vbox.add(addFilter(Style.COLOR_C()));

        // ENVELOPE, LFO, ARP PANEL
        JComponent envelopeLfoArpPanel = new SynthPanel(this);
        vbox = new VBox();

        vbox.add(addEnvelope(1, Style.COLOR_B()));
        vbox.add(addEnvelope(2, Style.COLOR_B()));
        vbox.add(addLFO(1, Style.COLOR_C()));
        vbox.add(addLFO(2, Style.COLOR_C()));
        //vbox.add(addArp(Style.COLOR_D()));

        envelopeLfoArpPanel.add(vbox, BorderLayout.CENTER);
        addTab("Envelopes, LFOs, ARP", envelopeLfoArpPanel);

        // EFFECTS PANEL
        JComponent effectsPanel = new SynthPanel(this);
        vbox = new VBox();

        vbox.add(addDelay(Style.COLOR_A()));
        vbox.add(addReverb(Style.COLOR_B()));
        vbox.add(addChorus(Style.COLOR_C()));
        vbox.add(addDistortion(Style.COLOR_A()));
        vbox.add(addPan(Style.COLOR_B()));
        vbox.add(addVocoder(Style.COLOR_C()));

        // TODO add equalizer controls

        effectsPanel.add(vbox, BorderLayout.CENTER);
        addTab("Effects", effectsPanel);

        // DEVICE-GOODIES panel (non patch related)
        JComponent devicePanel = new SynthPanel(this);
        vbox = new VBox();

        vbox.add(addDeviceGoodies(Style.COLOR_A()));
        vbox.add(addDeviceDetails(Style.COLOR_B()));

        devicePanel.add(vbox, BorderLayout.CENTER);
        addTab("Device goodies", devicePanel);

        //
//        model.set("name", "Init");

        loadDefaults();                 // this tells Edisyn to load the ".init" sysex file you created.  If you haven't set that up, it won't bother
    }

    private JComponent addNameGlobal(Color color)
    {
        Category globalCategory = new Category(this, "Novation A Station", color);

        JComponent comp;
        HBox hbox = new HBox();

        VBox vbox = new VBox();
        HBox hbox2 = new HBox();
        comp = new PatchDisplay(this, 4);
        hbox2.add(comp);
        vbox.add(hbox2);

//        comp = new StringComponent("Patch Name", this, "name", 14, "Name must be up to 14 ASCII characters.")
//        {
//            public String replace(String val)
//            {
//                return revisePatchName(val);
//            }
//
//            public void update(String key, Model model)
//            {
//                super.update(key, model);
//                updateTitle();
//            }
//        };
//        vbox.addBottom(comp);  // doesn't work right :-(
        hbox.add(vbox);

        globalCategory.add(hbox, BorderLayout.WEST);
        return globalCategory;
    }

    private JComponent addGeneral(Color color) {
        Category categoryGeneral = new Category(this, "General", color);

        JComponent comp;
        HBox hbox = new HBox();

        comp = new LabelledDial("patch level", this, "programvolume", color, 52, 76, 52);
        hbox.add(comp);

        VBox vbox = new VBox();
        comp = new Chooser("polyphony mode", this, "polyphonymode", POLYPHONY_MODES);
        vbox.add(comp);
        comp = new Chooser("unison voices", this, "unisonvoices", UNISON_VOICES);
        vbox.add(comp);
        hbox.add(vbox);
        comp = new LabelledDial("unison detune", this, "unisondetune", color, 0, 127);
        hbox.add(comp);

        vbox = new VBox();
        comp = new Chooser("portamento mode", this, "portamentomode", PORTAMENTO_MODES);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("portamento time", this, "portamentotime", color, 0, 127);
        hbox.add(comp);

        categoryGeneral.add(hbox, BorderLayout.WEST);
        return categoryGeneral;
    }

    private JComponent addOscillator(final int osc, Color color)
    {
        Category category = new Category(this, "Oscillator " + osc, color);
        category.makePasteable("osc");

        JComponent comp;
        HBox hbox = new HBox();

        VBox vbox = new VBox();
        comp = new Chooser("waveform", this, "osc" + osc + "waveform", OSC_WAVE_FORMS);
        // NOTE - icons here ?
        vbox.add(comp);

        if (osc == 2) {
            comp = new CheckBox("1->2 sync", this, "osc1to2sync");
            vbox.add(comp);
        }
        hbox.add(vbox);

        comp = new Chooser("octave", this, "osc" + osc + "octave", OSC_OCTAVES);
        vbox = new VBox();
        vbox.add(comp);
        // TODO - would probably be nicer to have this integrated into the semitone dial [-24, +36]
        hbox.add(vbox);

        comp = new LabelledDial("semitone", this, "osc" + osc + "semitone", color, 52, 76, 64);
        hbox.add(comp);

        comp = new LabelledDial("detune", this, "osc" + osc + "detune", color, 14, 114, 64);
        hbox.add(comp);

        // TODO - add support for 'pwm source' (and related)
        comp = new LabelledDial("pulse width", this, "osc" + osc + "pulsewidth", color, 0, 127, 64);
        hbox.add(comp);

        comp = new LabelledDial("mod env depth", this, "osc" + osc + "modenvdepth", color, 0, 127, 64);
        hbox.add(comp);

        comp = new LabelledDial("lfo1 depth", this, "osc" + osc + "lfo1depth", color, 0, 127, 64);
        hbox.add(comp);

        comp = new LabelledDial("bendwheel amount", this, "osc" + osc + "bendwheelamount", color, 0, 127, 64);
        hbox.add(comp);

        // TODO
//        if (osc == 3) {
//            comp = new LabelledDial("fm level", this, "osc" + osc + "fmlevel", color, 0, 127);
//            hbox.add(comp);
//
//            comp = new LabelledDial("fm env depth", this, "osc" + osc + "fmenvdepth", color, 0, 127);
//            hbox.add(comp);
//
//            and others... (check NRPN)
//        }

        category.add(hbox, BorderLayout.CENTER);
        return category;
    }

    private JComponent addMixer(Color color)
    {
        Category category = new Category(this, "Mixer", color);

        JComponent comp;
        HBox hbox = new HBox();

        comp = new LabelledDial("oscillator1", this, "osc1level", color, 0, 127);
        hbox.add(comp);
        comp = new LabelledDial("oscillator2", this, "osc2level", color, 0, 127);
        hbox.add(comp);
        comp = new LabelledDial("oscillator3", this, "osc3level", color, 0, 127);
        hbox.add(comp);
        comp = new LabelledDial("noise", this, "noiselevel", color, 0, 127);
        hbox.add(comp);
        comp = new LabelledDial("1*2 ring", this, "ringmodulatorlevel", color, 0, 127);
        hbox.add(comp);
        comp = new LabelledDial("ext", this, "externalinputlevel", color, 0, 127);
        hbox.add(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
    }

    private JComponent addFilter(Color color) {
        Category category = new Category(this, "Filter", color);

        JComponent comp;
        HBox hbox = new HBox();

        VBox vbox = new VBox();
        comp = new Chooser("type", this, "filtertype", FILTER_TYPES);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("frequency", this, "filterfrequency", color, 0, 127);
        hbox.add(comp);
        comp = new LabelledDial("resonance", this, "filterresonance", color, 0, 127);
        hbox.add(comp);
        comp = new LabelledDial("overdrive", this, "filteroverdrive", color, 0, 127);
        hbox.add(comp);
        comp = new LabelledDial("key track", this, "filterkeytrack", color, 0, 127);
        hbox.add(comp);
        comp = new LabelledDial("mod env depth", this, "filtermodenvdepth", color, 0, 127, 64);
        hbox.add(comp);
        comp = new LabelledDial("lfo2 depth", this, "filterlfo2depth", color, 0, 127, 64);
        hbox.add(comp);

        // TODO
        // and others... (check NRPN)

        category.add(hbox, BorderLayout.CENTER);
        return category;
    }

    private JComponent addEnvelope(final int envelope, Color color)
    {
        Category category = new Category(this, (envelope == 1 ? "Amp" : "Mod") + " Envelope", color);
        category.makePasteable("env");

        String type = envelope == 1 ? "amplitude" : "modulation";

        JComponent comp;
        HBox hbox = new HBox();
        VBox vbox = new VBox();
        comp = new Chooser("env trigger", this, type + "envelopetrigger", ENV_TRIGGERS);
        vbox.add(comp);
        hbox.add(vbox);

        comp = new LabelledDial("attack", this, type + "envelopeattack", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("decay", this, type + "envelopedecay", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("sustain", this, type + "envelopesustain", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("release", this, type + "enveloperelease", color, 0, 127);
        hbox.add(comp);

        // ADSR
        comp = new EnvelopeDisplay(this, Color.red,
                new String[] { null, type + "envelopeattack", type + "envelopedecay", null, type + "enveloperelease" },
                new String[] { null, null, type + "envelopesustain", type + "envelopesustain", null },
                new double[] { 0, 0.25/127.0, 0.25 / 127.0,  0.25, 0.25/127.0},
                new double[] { 0, 1.0, 1.0 / 127.0, 1.0/127.0, 0 });
        hbox.addLast(comp);

        category.add(hbox, BorderLayout.CENTER);
        return category;
    }

    private JComponent addLFO(final int lfo, Color color) {
        Category category = new Category(this, "LFO " + lfo, color);
        // category.makePasteable("lfo");

        JComponent comp;
        HBox hbox = new HBox();

        VBox vbox = new VBox();
        comp = new Chooser("waveform", this, "lfo" + lfo + "waveform", LFO_WAVE_FORMS);
        // NOTE - icons here ?
        vbox.add(comp);
        hbox.add(vbox);
        // TODO - single dial for both sync and non-sync ?
        comp = new LabelledDial("speed (non-sync)", this, "lfo" + lfo + "speednonsync", color, 0, 127);
        hbox.add(comp);
        comp = new LabelledDial("speed (sync)", this, "lfo" + lfo + "speedsync", color, 0, 127);
        hbox.add(comp);
        comp = new LabelledDial("delay", this, "lfo" + lfo + "delay", color, 0, 127);
        hbox.add(comp);

        category.add(hbox);
        return category;
   }

    private JComponent addARP(Color color) {
        Category category = new Category(this, "ARP", color);

        JComponent comp;
        HBox hbox = new HBox();

        VBox vbox = new VBox();
        comp = new Chooser("pattern", this, "arppattern", ARP_PATTERNS);
        vbox.add(comp);
        comp = new Chooser("rate", this, "arprate", ARP_RATES);
        vbox.add(comp);
        hbox.add(vbox);

        category.add(hbox);
        return category;
    }

    private Component addDelay(Color color) {
        Category category = new Category(this, "delay", color);

        JComponent comp;
        HBox hbox = new HBox();

        comp = new LabelledDial("send level", this, "delaysendlevel", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("modwheel", this, "delaysendmodheel", color, 0, 127);
        hbox.add(comp);

        // TODO - dropdown here ? or different dial ?
        comp = new LabelledDial("time (sync)", this, "delaytimesync", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("time (non-sync)", this, "delaytimenonsync", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("feedback", this, "delayfeedback", color, 0, 127);
        hbox.add(comp);

        // TODO - dropdown here ? or different dial ?
        comp = new LabelledDial("ratio", this, "delayratio", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("stereo width", this, "delaystereowidth", color, 0, 127);
        hbox.add(comp);

        category.add(hbox);
        return category;
    }

    private Component addReverb(Color color) {
        Category category = new Category(this, "reverb", color);

        JComponent comp;
        HBox hbox = new HBox();

        comp = new LabelledDial("send level", this, "reverbsendlevel", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("modwheel", this, "reverbsendmodheel", color, 0, 127);
        hbox.add(comp);

        // TODO - add reverb type

        comp = new LabelledDial("decay", this, "reverbdecay", color, 0, 127);
        hbox.add(comp);

        category.add(hbox);
        return category;
    }

    private Component addChorus(Color color) {
        Category category = new Category(this, "chorus", color);

        JComponent comp;
        HBox hbox = new HBox();

        comp = new LabelledDial("send level", this, "chorussendlevel", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("modwheel", this, "chorussendmodheel", color, 0, 127);
        hbox.add(comp);

        // TODO - dropdown here ? or different dial ?
        comp = new LabelledDial("rate (sync)", this, "chorusratesync", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("rate (non-sync)", this, "chorusratenonsync", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("feedback", this, "chorusfeedback", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("depth", this, "chorusmoddepth", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("centre point", this, "chorusmodcentrepoint", color, 0, 127);
        hbox.add(comp);

        category.add(hbox);
        return category;
    }

    private Component addDistortion(Color color) {
        Category category = new Category(this, "distortion", color);

        JComponent comp;
        HBox hbox = new HBox();

        comp = new LabelledDial("modwheel", this, "distortionmodheel", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("compensation", this, "distortioncompensation", color, 0, 127);
        hbox.add(comp);

        category.add(hbox);
        return category;
    }

    private Component addPan(Color color) {
        Category category = new Category(this, "panning", color);

        JComponent comp;
        HBox hbox = new HBox();

        // TODO - dropdown here ? or different dial ?
        comp = new LabelledDial("rate (sync)", this, "panningratesync", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("rate (non-sync)", this, "panningratenonsync", color, 0, 127);
        hbox.add(comp);

        comp = new LabelledDial("depth", this, "panningmoddepth", color, 0, 127);
        hbox.add(comp);

        category.add(hbox);
        return category;
    }

    private Component addVocoder(Color color) {
        Category category = new Category(this, "vocoder", color);

        JComponent comp;
        HBox hbox = new HBox();

        comp = new LabelledDial("balance", this, "vocoderbalance", color, 0, 127);
        hbox.add(comp);

        category.add(hbox);
        return category;
    }

    private JComponent addDeviceGoodies(Color color) {
        Category category = new Category(this, "Goodies", color);

        JComponent comp;

        HBox hbox = new HBox();
        comp = new LabelledDial("device volume", this, "devicevolume", color, 0, 127);
        hbox.add(comp);

        category.add(hbox);
        return category;
    }

    private JComponent addDeviceDetails(Color color) {
        Category category = new Category(this, "Details", color);

        JComponent comp;

        HBox hbox = new HBox();
        // TODO - revisit how to present this
        comp = new ReadOnlyString("sw version", this, "swversion", 1);
        hbox.add(comp);

        category.add(hbox);
        return category;
    }

    ////// BELOW ARE DEFAULT IMPLEMENTATIONS OF COMMON HOOK METHODS THAT SYNTH EDITORS IMPLEMENT OR OVERRIDE.
    ////// If you do not need to implement or override a method, you should delete that method entirely.


    /////// SOME NOTES ABOUT RELATIONSHIPS BETWEEN CERTAIN METHODS
        

    /// There are a lot of redundant methods here.  You only have to override some of them.

    /// PARSING (LOADING OR RECEIVING)
    /// When a message is received from the synthesizser, Edisyn will do this:
    /// If the message is a Sysex Message, then
    ///     Call recognize(message data).  If it returns true, then
    ///                     Call parse(message data, fromFile) [we presume it's a dump or a load from a file]
    ///             Else
    ///                     Call parseParameter(message data) [we presume it's a parameter change, or maybe something else]
    /// Else if the message is a complete CC or NRPN message
    ///             Call handleSynthCCOrNRPN(message) [it's some CC or NRPN that your synth is sending us, maybe a parameter change?]
        
    /// SENDING A SINGLE PARAMETER OF KEY key
    /// Call emitAll(key)
    ///     This calls emit(key)
    ///
    /// You could override either of these methods, but probably not both.
        
    /// SENDING TO CURRENT
    /// Call sendAllParameters().  This does:
    ///             If getSendsAllParametersAsDump(), this calls:
    ///                     emitAll(tempModel, toWorkingMemory = true, toFile)
    ///                             This calls emit(tempModel, toWorkingMemory = true, toFile)
    ///             Else for every key it calls:
    ///             Call emitAll(key)
    ///                     This calls emit(key)
    ///
    /// You could override either of the emit...(tempModel...) methods, but probably not both.
    /// You could override either of the emit...(key...) methods, but probably not both.

    /// SENDING TO A PATCH
    /// Call gatherPatchInfo(...,tempModel,...)
    /// If successful
    ///             Call changePatch(tempModel)
    ///     Call sendAllParameters().  This does:
    ///                     If getSendsAllParametersAsDump(), this calls:
    ///                             emitAll(tempModel, toWorkingMemory = true, toFile)
    ///                                     This calls emit(tempModel, toWorkingMemory = true, toFile)
    ///                     Else for every key it calls:
    ///                     Call emitAll(key)
    ///                             This calls emit(key)
    ///     
    /// You could override either of the emit...(tempModel...) methods, but probably not both.
    /// You could override either of the emit...(key...) methods, but probably not both.
        
    /// WRITING OR SAVING
    /// Call gatherPatchInfo(...,tempModel,...)
    /// If successful
    ///     Call writeAllParameters(tempModel).  This does:
    ///         Call changePatch(tempModel)
    ///                 Call emitAll(tempModel, toWorkingMemory = false, toFile)
    ///                     This calls emit(tempModel, toWorkingMemory = false, toFile)
    ///         Call changePatch(tempModel)
    ///
    /// You could override either of the emit methods, but probably not both.
    /// Note that saving strips out the non-sysex bytes from emitAll.
        
    /// SAVING
    /// Call emitAll(tempModel, toWorkingMemory, toFile)
    ///             This calls emit(tempModel, toWorkingMemory, toFile)
    ///
    /// You could override either of the emit methods, but probably not both.
    /// Note that saving strips out the non-sysex bytes from emitAll.
        
    /// REQUESTING A PATCH 
    /// If we're requesting the CURRENT patch
    ///             Call performRequestCurrentDump()
    ///                     this then calls requestCurrentDump()
    /// Else
    ///     Call gatherPatchInfo(...,tempModel,...)
    ///             If successful
    ///                     Call performRequestDump(tempModel)
    ///                             This calls changePatch(tempModel) USUALLY
    ///                             Then it calls requestDump(tempModel)
    ///
    /// You could override performRequestCurrentDump or requestCurrentDump, but probably not both.
    /// Similarly, you could override performRequestDump or requestDump, but probably not both

    ////// YOU MUST OVERRIDE ALL OF THE FOLLOWING
    @Override
    public void changePatch(Model tempModel)
        {
        // Here you do stuff that changes patches on the synth.
        // You probably want to look at tryToSendSysex() and tryToSendMIDI()
        //
        // This method is used primariily to switch to a new patch prior to loading it
        // from the synthesizer or emitting it to the synthesizer.  Many synthesizers do 
        // not report their patch location information when emitting a dump to Edisyn.  
        // If this is the case, you might want add some code at the end of this method which
        // assumes that the patch change and subsequent parse were successful, so you can
        // just change the patch information in your model directly here in this method. 
        // You should NOT do this when changing a patch for the purpose of merging.  
        // So in this case (and ONLY in this case) you should end this method with something 
        // along the lines of:
        //
        //     // My synth doesn't report patch info in its parsed data, so here assume that we successfully did it
        //     if (!isMerging())
        //         {
        //         boolean midi = getSendMIDI();        // is MIDI turned off right now?
        //         setSendMIDI(false);                          // you should always turn off MIDI prior to messing with the model so nothing gets emitted, just in case
        //         model.set("number", number);
        //         model.set("bank", bank);
        //         setSendMIDI(midi);                           // restore to whatever state it was
        //         }
            byte bank = (byte) tempModel.get("bank");
            byte program = (byte) tempModel.get("number");
            try {
                ++bank; // 1..4 in synth, while 0..3 in model
                // Bank change is CC 32
                tryToSendMIDI(new ShortMessage(ShortMessage.CONTROL_CHANGE, getChannelOut(), 32, bank));
                // Number change is PC
                tryToSendMIDI(new ShortMessage(ShortMessage.PROGRAM_CHANGE, getChannelOut(), program, 0));
            }
            catch (Exception e) {
                Synth.handleException(e);
            }
        }

    @Override
    public boolean gatherPatchInfo(String title, Model changeThis, boolean writing) {
        JComboBox bank = new JComboBox(BANKS);
        bank.setEditable(false);
        bank.setMaximumRowCount(4);

        int currentBank = model.get("bank");     // 0..3
        if (-1 != currentBank) {
            bank.setSelectedIndex(currentBank);
        }

        int currentPatch = model.get("number");
        JTextField number = new SelectedTextField(String.valueOf(currentPatch), 3);

        while (true) {
            boolean result = showMultiOption(this, new String[]{"Bank", "Patch Number"},
                    new JComponent[]{bank, number}, title, "Enter the Bank and Patch number.");

            if (result == false)
                return false;

            int n;
            try {
                n = Integer.parseInt(number.getText());
            } catch (NumberFormatException e) {
                showSimpleError(title, "The Patch Number must be an integer 0 ... 99");
                continue;
            }
            if (n < 0 || n > 99) {
                showSimpleError(title, "The Patch Number must be an integer 0 ... 99");
                continue;
            }

            changeThis.set("bank", bank.getSelectedIndex());
            changeThis.set("number", n);
            return true;
        }
    }

    @Override
    public int parse(byte[] data, boolean fromFile) {
        // This patch data will come from a file or transmitted over sysex.
        // FROMFILE indicates that the parse is from a sysex file.
        //
        // You should parse it into the model and return one of:
        // - PARSE_SUCCEEDED if successful,
        // - PARSE_SUCCEEDED_UNTITLED if successful, but we don't want the patch's 
        // filename to be updated to reflect the loaded file.
        // - PARSE_CANCELLED if the user cancelled the parsing process (this would only 
        // make sense for certain interactive parsing mechanisms, and only certain
        // synths would have it) and the patch was not modified.
        // - PARSE_FAILED if the parse failed -- we assume this means that you did not modify
        // the editor data, or reverted it.
        // - PARSE_INCOMPLETE if the parse was successful but not complete enough to assume 
        // that we have a full patch.  This is used as follows.  For example, a Yamaha 4-op
        // synthesizer needs up to four separate sysex messages before a patch is complete,
        // and you may not have received all of them in this data dump.  You should in this case
        // return PARSE_INCOMPLETE, and only return PARSE_SUCCEEDED when all the messages have
        // arrived sufficient to declare the model finished.
        // - PARSE_IGNORE if the data wholely contains sysex messages that serve no purpose.
        // In some cases (such as the ASM Hydrasynth) these messages cannot be easily filtered
        // out at the recognition stage.
        //
        // IMPORTANT NOTE.  While parse(...) has been called, sendMIDI has been switched
        // OFF so you can update widgets without them sending out MIDI updates.  However it
        // is occasionally the case that you are required to send a MIDI message to the synth
        // to get it to send the next chunk of data to you (and also in this case you'd return
        // PARSE_INCOMPLETE probably).  To do this, you can:
        //             boolean sendMIDI = getSendMIDI();
        //             setSendMIDI(true);
        //             *** send your message here ***
        //             setSendMIDI(sendMIDI);
        //
            byte swVersion = data[9];
            byte swIncrement = data[10];
            model.set("swversion", parseVersion(swVersion, swIncrement));

            byte programBank = data[11];    // 1..4
            byte programNumber = data[12];  // 0..99
            if (programBank != 0) {
                programBank--;  // zero-indexed in model
                model.set("bank", programBank); // 0..3 in model
                model.set("number", programNumber);
            }


        // TODO - to be extended, for now supporting:
            // 0 : current sound dump
            // 1 : program sound dump
            byte messageType = data[7];
            if (messageType == 0x0 || messageType == 0x01) {
                for (int index = 13; index < 13 + 128; ++index) {
                    Optional<Mappings> mapping = Mappings.getByIndex(index-13);
                    if (mapping.isPresent()) {
                        mapping.get().toModel(model, data[index]);
                    }
                }
                return PARSE_SUCCEEDED;
            }
            return PARSE_IGNORE;
        }

    public static String getSynthName() {
        return "Novation A Station";
    }

    @Override
    public String getDefaultResourceFileName()
        {
        // Ultimately your synth will be initialized by loading a file via parse().  This is usually a
        // sysex file ending in the extension ".init", such as "WaldorfBlofeld.init",
        // and is located right next to the class file (that is, "WaldorfBlofeld.class").
        // 
        // If you return null here, this initialization step will be bypassed.  But final
        // production code should not do that.
        // TODO
        return null; 
        }

    @Override
    public String getHTMLResourceFileName() {
        return "NovationAStation.html";
    }

    @Override
    public String getPatchLocationName(Model model) {
            if (model.exists("bank") && model.exists("number")) {
                String bankName = BANKS[model.get("bank")];
                int program = model.get("number");
                return String.format("%s%02d", bankName, program);
            }
            return "...";
        }
    
//    public Model getFirstPatchLocation()
//        {
//        // Returns a model containing a patch location (bank, number, etc.) representing the
//        // "first" patch in the synth.  If your synthesizer does not have the notion of patches
//        // at all, return null.  If your synthesizer only has a single "patch", thus a single
//        // patch location, then this method should return that location.
//        //
//        // In most cases you don't have to override this method.  The default version computes
//        // whether you have a "first" patch by setting a model's number and bank to both 0, then
//        // calling getNextPatchLocation().  If that method returns null, or if it returns a model
//        // with no number, then the default version returns null. Otherwise it checks to see if
//        // a model is returned with a bank.  If there is a bank, then bank=0 number=0 is returned.
//        // If there is no bank, then number=0 is returned.  This strategy will work in most cases,
//        // but if your first number or bank, for some reason, is not 0, then it will not work
//        // properly.  In that case you'll need to override it to return the first location yourself.
//        //
//        // This method is used for doing random patch selection.
//        return super.getFirstPatchLocation();
//        }

    @Override
    public Model getNextPatchLocation(Model model) {
        int bank = model.get("bank", 0);
        int program = model.get("number", -1);
        int programindex = bank * 100 + program;
        ++programindex;
        bank = (programindex / 100) % 4;
        program = programindex % 100;

        Model newModel = buildModel();
        newModel.set("bank", bank);
        newModel.set("number", program);
        return newModel;
    }

//    public boolean patchLocationEquals(Model patch1, Model patch2)
//        {
//        // This should return true if the patch locations stored in the given two patches are the same.
//        // For example, they're both Bank B, Number 72.
//        //
//        // This method is used for doing batch downloads.
//        return super.patchLocationEquals(patch1, patch2);
//        }

    ////// YOU PROBABLY WANT TO OVERRIDE ALL OF THE FOLLOWING

//    @Override
//    public String getPatchName(Model model) {
//        int bank = model.get("bank", -1);
//        return bank != -1 ? String.valueOf(model.get("number")) : null;
//    }

//    public String revisePatchName(String name)
//        {
//        // Here you tweak the name to make sure it's a valid patch name.
//        // You probably first want to call    name = super.revisePatchName(name)
//        // As this method will remove all trailing whitespace from the name.
//        // At that point you can modify the name as you need to (converting
//        // to all uppercase, say, or removing invalid characters, or truncating
//        // to the proper length, etc.)
//
//        return super.revisePatchName(name);
//        }
        
//    public String reviseID(String id)
//        {
//        // Some synthesizers have an "id" which uniquely identifies them in their
//        // sysex so other synths of the same model ignore that sysex message.  Waldorf's
//        // synths are notable in this respect.  If your synth does this, revise the id
//        // to make sure it's valid, and return a valid String.  If your synth does NOT
//        // do this, then you should always return null.
//        return null;
//        }
    
//    public void revise()
//        {
//        // In this method you need to verify that all the keys in your model have valid values.
//        // Some synthesizers send invalid values over sysex or NRPN.  For example, the PreenFM2
//        // can send crazy stuff way out of range.
//        //
//        // The default version of this method bounds all the values to between their stated min and
//        // max values.  You might need to do more than this; for example, you might verify that
//        // the name is valid.  In this case, call super.revise() and then do further revision
//        // as you see fit.  For one way to do this, see the Waldorf Blofeld code.
//
//        super.revise();
//        }









    ////// YOU PROBABLY WANT TO OVERRIDE *ONE* OF THE FOLLOWING
    @Override
    public Object[] emitAll(Model tempModel, boolean toWorkingMemory, boolean toFile)
        {
        // This does a write of your patch to sysex (to dump to the synth or to store
        // in a file).  TOWORKINGMEMORY indicates whether the dump will go to the synth's
        // working memory, or (if false) written to a specific patch store.  TOFILE 
        // indicates that the write will be written to a sysex file.  TEMPMODEL will hold
        // data (bank, number) regarding the patch store location.  
        //
        // The Object[] array returned can consist any combination of the following:
        //
        // 1. A fully constructed and populated javax.sound.midi.ShortMessage or
        //    javax.sound.midi.SysexMessage
        // 
        // 2. A byte[] consisting of the bytes for a sysex message, including the 0xF0
        //    and 0xF7.
        //
        // 3. A java.util.Integer, which will be used to indicate a pause in milliseconds
        //    before sending the next item in the Object[] array to the synthesizer.
        //
        // 4. null, which is a no-op and is ignored.
        //
        // If emitAll(..., ..., true) then that is, if writing to a file, then the 
        // ObjectArray will be flattened after you have returned it.  This means that 
        // all non-sysex messages (things that aren't javax.sound.midi.SysexMessage 
        // or a byte[]) will be stripped out, since this is for a sysex file.
        //
        // If you need to send more than just a simple sysex message, override this one.
        
        return super.emitAll(tempModel, toWorkingMemory, toFile);
        }

    @Override
    public byte[] emit(Model tempModel, boolean toWorkingMemory, boolean toFile)
        { 
        // This does a write of your patch to sysex (to dump to the synth or to store
        // in a file).  TOWORKINGMEMORY indicates whether the dump will go to the synth's
        // working memory, or (if false) written to a specific patch store.  TOFILE 
        // indicates that the write will be written to a sysex file.  TEMPMODEL will hold
        // data (bank, number) regarding the patch store location.  The resulting byte[]
        // array should consist entirely of zero or more sysex messages.
        //
        // If you need to send just a simple sysex message, override this one.
        return new byte[0]; 
        }
    
    
    
    
    
    
    
    
    
    ////// YOU PROBABLY WANT TO OVERRIDE *ONE* OF THE FOLLOWING
    @Override
    public Object[] emitAll(String key)
        {
            Optional<Mappings> mappings = Mappings.getByKey(key);
            if (mappings.isEmpty())
                return new Object[0];
            Mappings mapping = mappings.get();
            return buildCC(getChannelOut(), mapping.getCC(), mapping.toSynth(model));

            // This writes a single parameter out to the synth.
        // If nothing should be emitted for the given key, return a zero-length array.
        // If your synth does not support emitting for individual keys at all, return null.
        //
        // If you need to send more than just a simple sysex message, override this one.

            // TODO !!!
            /*
            if (!getSendMIDI()) return new Object[0];  // MIDI turned off, don't bother
            byte DEV = (byte)(getID());

            Integer index = PARAM_TO_CC_MAP.get(key);
            if (index == null) {
                // don't bother
                return null;
            }
            byte XX = (byte)model.get(key);
            // F0: SYSEX start
            // 00: Novation ID 1
            // 20: Novation ID 2
            // 29: Novation ID 3
            // 01: DeviceType (01 = synth)
            // 40: A-Station
            // ...
            // TBI
            // ...
            // F7: END OF EXCLUSIVE
            byte[] data = new byte[] { (byte)0xF0, 0x00, 0x20, 0x29, 0x01, 0x40, index.byteValue(), XX, (byte)0xF7 };
            return new Object[] { data };
            */
        }

    @Override
    public byte[] emit(String key)
        { 
        // This writes a single parameter out to the synth.
        // If nothing should be emitted for the given key, return a zero-length array.
        // If your synth does not support emitting for individual keys at all, return null.
        //
        // If you need to send just a simple sysex message, override this one.
        return super.emit(key); 
        }









    ////// YOU PROBABLY WANT TO OVERRIDE *ONE* OF THE FOLLOWING
    
//    public void performRequestDump(Model tempModel, boolean changePatch)
//        {
//        // This asks the synth to dump a specific patch (number and bank etc. specified
//        // in tempModel).  If CHANGEPATCH is true you should first change the patch.
//        //
//        // Normally Edisyn implements this method for you, handling the patch-change,
//        // and you can just implement requestDump(...) to send the dump request.
//        // If you need to do more than a simple sysex request, reimplement this
//        // method.  The default form looks like this:
//
//        // if (changePatch)
//        //              changePatch(tempModel);
//        // tryToSendSysex(requestDump(tempModel));
//
//        // It is possible that it's impossible to request a dump without changing
//        // the patch regardless.  In this case you can ignore changePatch and just
//        // do a changePatch always.  You'd need to implement this.
//
//        super.performRequestDump(tempModel, changePatch);
//        }

    @Override
    public byte[] requestDump(Model tempModel) {
        // request specific program dump
        byte messageType = 0x41;
        // tempModel supposed to have valid bank+program
        byte bank = (byte) (1 + tempModel.get("bank")); // 1..4 (while in model: 0..3)
        byte program = (byte) tempModel.get("number");
        return new byte[] { (byte)0xF0, 0x00, 0x20, 0x29, 0x01, 0x40, 0x7F, messageType, 0x00, 0x00, 0x00, bank, program, (byte)0xF7 };
    }

    ////// YOU PROBABLY WANT TO OVERRIDE *ONE* OF THE FOLLOWING
        
//    public void performRequestCurrentDump()
//        {
//        // This asks the synth to dump the currently-playing patch
//        // (number and bank etc. specified in tempModel).
//        //
//        // Normally Edisyn implements this method for you, and you can just emit
//        // a sysex via requestCurrentDump().  But if you need something else,
//        // reimplement this method.  The default looks like this:
//
//        // tryToSendSysex(requestCurrentDump());
//
//        super.performRequestCurrentDump();
//        }

    @Override
    public byte[] requestCurrentDump() {
        // request current sound dump
        byte messageType = 0x40;
        return new byte[] { (byte)0xF0, 0x00, 0x20, 0x29, 0x01, 0x40, 0x7F, messageType, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0xF7 };
    }


    ////// YOU MAY WANT TO IMPLEMENT SOME OF THE FOLLOWING

//    public boolean getAlwaysChangesPatchesOnRequestDump()
//        {
//        // This is called by performRequestDump() to determine if it should changePatch() first.
//        // If not, then in some cases performRequestDump() can avoid changing the patch, which
//        // is good for things like the SHOW button in Multimode patches, or blending, or merging,
//        // where we're just loading a patch temporarily or in an auxiliary fashion.  However
//        // many synths do not have the ability to request a dump and must instead just do
//        // a change patch followed by requestCurrentDump ALWAYS.  In this case, override this
//        // method to return TRUE.
//        return false;
//        }

//    public int getTestNotePitch()
//        {
//        // Returns the test note pitch.  This is default whatever the user set,
//        // but you can override this to fix it to something else
//        return super.getTestNotePitch();
//        }

//    public boolean getClearsTestNotes()
//        {
//        // Returns whether test notes should be cleared via a NOTE OFF or an
//        // all sounds / all notes off.  This is normally true, except for drum
//        // patches, where the notes ought to be allowed to naturally decay.
//        return true;
//        }

//    public static boolean recognizeBulk(byte[] data)
//        {
//        // This method should return TRUE if the data is correct sysex data for a
//        // a *bulk* patch (that is, multi-patch) dump to your kind of synthesizer,
//        // and so you can receive it via parse() along with single-patch dumps.
//        //
//        // Notice that this is a STATIC method -- but you need to implement it
//        // anyway.  Edisyn will call the right static version using reflection magic.
//        //
//        // You don't have to implement this method -- it will return false by default --
//        // but you DO have to implement its complement, the recognize(data) method.
//        //
//        // Note that if you implement recognizeBulk(data), then in your parse(...)
//        // method you may need to do something with the data.  A good idea is to
//        // offer to either (1) upload the sysex to the synth (2) save the sysex to a file
//        // or (3) select a patch from the sysex to edit -- or (4) cancel.  This is
//        // the approach taken in the DX7 patch editor and you could implement it that
//        // way, just steal code from there.
//        return false;
//        }

    @Override
    public void parseParameter(byte[] data)
        {
        // If your synth sent you a sysex message which was not recognized via
        // the recognize() method, it gets sent here.  Typically this is 
        // a sysex message for a single parameter update.  If your synth sends 
        // such things, implement this.  See also handleCCOrNRPNData() below.
            System.out.println("parseParameter(" + data.length + ")");
        return;

        }

    @Override
    public void handleSynthCCOrNRPN(Midi.CCData data) {
        Mappings.getByCC(data.number)
                .ifPresent(mapping -> mapping.toModel(model, data.value));
    }
            
//    public boolean requestCloseWindow()
//        {
//        // When the user clicks on the close box of your synth editor,
//        // this method will be called.  If you return true, the window
//        // will be closed, else it will stay open.  You might use
//        // this method to verify with the user that everything is saved,
//        // but in fact none of the current synth editors do this, they
//        // just return true immediately.  This is a rare need and at
//        // present no patch editors implement this method at all.
//        return true;
//        }
        
//    public int getPauseAfterChangePatch()
//        {
//        // Some synths cannot accept MIDI messages for a while after a patch-change.
//        // For example, the Blofeld has to wait for about 200ms.
//        // Here you can specify that Edisyn must pause at least so many
//        // milliseconds before issuing another MIDI message after you have
//        // changed the patch via changePatch().
//        return 0;
//        }

//    public double getPauseBetweenMIDISends()
//        {
//        // Some synths cannot accept MIDI messages at full speed.
//        // For example, the Yamaha TX81Z has problems with sysex messages
//        // faster than 50ms.
//        // Here you can specify that Edisyn must pause at least so many
//        // milliseconds before issuing another MIDI message of *any* kind.
//        // This includes note on / note off etc., so don't expect musicality
//        // if you set this to >0.
//        return 0.0;
//        }
    
//    public int getPauseAfterWritePatch()
//        {
//        // Some synths need extra time to process a write-patch before
//        // a follow-up change-patch request.
//        // The default is to return getPauseAfterSendAllParameters();
//        return super.getPauseAfterWritePatch();
//        }
        
//    public int getPauseBetweenPatchWrites()
//        {
//        // Some synths need extra time to process a write-patch before
//        // writing a second patch (with no change patch request).
//        // The default is to return getPauseAfterWritePatch()
//        // which in turn has a default of getPauseAfterSendAllParameters()
//        return super.getPauseBetweenPatchWrites();
//        }
        
//    public int getPauseAfterSendAllParameters()
//        {
//        // Some synths need extra time after a parameter dump before
//        // they can do anything else, notably play notes properly.
//        // For example, the Kawai K4 needs about 100ms after a parameter
//        // dump or else it'll play notes in a strange truncated way.
//        // Here you can specify that Edisyn must pause at least so many
//        // milliseconds before issuing another MIDI message after it has
//        // called sendAllParmeters().
//        return 0;
//        }
        
//    public int getPauseAfterSendOneParameter()
//        {
//        // Some synths need extra time after each parameter send before another
//        // send can be made.  Here you can specify that Edisyn must pause at least so many
//        // milliseconds before issuing another MIDI message after it has
//        // sent a single parameter via emitAll(key, status).
//        return 0;
//        }
//
//    public int getPauseBetweenSysexFragments()
//        {
//        // Some synths have small MIDI buffers and are so slow that you
//        // cannot send large messages (that is, sysex) to them at full
//        // speed without them dying.  The Kawai K1 is an example of this.
//        // The methods getPauseBetweenSysexFragments() and
//        // getSysexFragmentSize() allow you to break large sysex messages
//        // into multiple fragments, each with a pause between, in order
//        // to send a message successfully.
//        //
//        // You can also manually break up a sysex message into fragments,
//        // and manually insert whatever time you'd like between them.
//        // This is done using the DividedSysex class.
//        //
//        return 0;
//        }
        
//    public int getSysexFragmentSize()
//        {
//        // Some synths have small MIDI buffers and are so slow that you
//        // cannot send large messages (that is, sysex) to them at full
//        // speed without them dying.  The Kawai K1 is an example of this.
//        // The methods getPauseBetweenSysexFragments() and
//        // getSysexFragmentSize() allow you to break large sysex messages
//        // into multiple fragments, each with a pause between, in order
//        // to send a message successfully.
//        //
//        // You can also manually break up a sysex message into fragments,
//        // and manually insert whatever time you'd like between them.
//        // This is done using the DividedSysex class.
//        //
//        return NO_SYSEX_FRAGMENT_SIZE;
//        }
        
//    public int getPauseBetweenHillClimbPlays()
//        {
//        // Some synths, such as the Korg Wavestation SR, need extra time
//        // to recover from changing patches before they can turn around
//        // and do again immediately.  Here you can specify that pause in milliseconds.
//        return 0;
//        }

//    public int getPauseAfterReceivePatch()
//        {
//        // Some synths, such as the Blofeld, need a short break after sending us
//        // a patch before we can request a second patch (with no change patch command).
//        // This pause is in milliseconds.  By default this function returns
//        // getPauseAfterChangePatch(), which is a lot but probably adequate.
//        return super.getPauseAfterReceivePatch();
//        }
        
//    public int getBatchDownloadWaitTime()
//        {
//        // Edisyn does bulk downloads by iteratively requesting a patch, then
//        // waiting for it to load, then saving it.  Edisyn will wait for up to
//        // getBatchDownloadWaitTime() milliseconds before it checks to see if the
//        // patch has arrived and try to save it; else it will issue another request.
//        //
//        // The default value is 1000 (one second).  If your synth takes more (or less!)
//        // time to respond and dump a patch to Edisyn, you may wish to change this value.
//        // You'd like it as short as possible without missing dumps.
//        return 1000;
//        }

//    public int getBatchDownloadFailureCountdown()
//        {
//        // Each getBatchDownloadWaitTime() Edisyn will count this down until
//        // it reaches -1, at which time it gives up waiting for a bulk download.
//        return 0;
//        }

//    public void startingBatchDownload(Model firstPatch, Model finalPatch)
//        {
//        // Called when a batch download is starting.  This might give your editor
//        // a chance to emit something at the beginning of the batch download.  For
//        // example, the ASM Hydrasynth requires that a header sysex command be
//        // sent before a stream of batch downloads.  You can determine if
//        // a batch download is occurring during parse() by calling isBatchDownloading()
//        super.startingBatchDownload(firstPatch, finalPatch);
//        }

//    public void stoppingBatchDownload(Model firstPatch, Model finalPatch)
//        {
//        // Called when a batch download is stopping.  This might give your editor
//        // a chance to emit something at the end of the batch download.  For
//        // example, the ASM Hydrasynth requires that a header sysex command be
//        // sent before a stream of patch downloads.  You can determine if
//        // a batch download is occurring during parse() by calling isBatchDownloading()
//        }

//    public int getTestNoteChannel()
//        {
//        // It's possible that your synth has a special channel for this patch
//        // (for example, a drum patch).  Override this to provide a custom
//        // channel for the test note to be sent on.  The default is getChannelOut().
//        return getChannelOut();
//        }

//    public void windowBecameFront()
//        {
//        // If your editor's window just became the front window, this method will
//        // be called to inform you.  For example, Waldorf Microwave synthesizers
//        // can change from multimode to single mode (or the other way) as appropriate
//        // when their window comes to the fore.
//        return;
//        }
        
//    public void windowCreated()
//        {
//        // If your editor's window was just created and the user has selected
//        // MIDI device parameters (or chose not to), this method will
//        // be called to inform you.  For example, you could use this to issue a
//        // Sysex device inquiry message to help further set up the synth.
//        return;
//        }

//    public boolean getSendsAllParametersAsDump()
//        {
//        // Normally this method returns TRUE meaning that when the user sends
//        // or writes to the synthesizer, emitAll(model,...) will be called to write
//        // a bulk write, typically a sysex message.  But some synthesizers don't
//        // use sysex.  For example, the PreenFM2 receives each of its parameters
//        // via individual NRPN messages (which are handled via emitAll(key)).
//        // If your synthesizer is of this type, you should return FALSE.
//        return true;
//        }

    public JFrame sprout()
        {
        // This is a great big method in Synth.java, and handles building the JFrame and
        // constructing all of the menus.  It's called when the editor is having its GUI
        // constructed.   You may need to do some things here, such as turning off certain
        // menu options that your synthesizer cannot do.  Be sure to call super.sprout();
        // first.
        return super.sprout();
        }
        
//    public void showedOneTimeWarning(String key)
//        {
//        // If you call doOneTimeWarning(...) to issue a one-time-only warning to the musician,
//        // after the warning is issued, this is called once so you can (for example, see
//        // ASMHydraSynth) switch to the About pane or something.
//        }
                
//    public void tabChanged()
//        {
//        // This method is called whenever the tabs are changed in case you need to do something
//        // like update a menu item in response to it etc.  Be sure to call super.tabChanged();
//        super.tabChanged();
//        }

//    public boolean getExpectsRawCCFromSynth()
//        {
//        // If your synthesizer sends individual parameter data to Edisyn not as sysex,
//        // and not as cooked CC messages (such as NRPN), but rather as raw CC messages,
//        // then you should override this method to return TRUE.  Generally it's kept FALSE,
//        // the default.
//        return false;
//        }

    public boolean getReceivesPatchesAsDumps()
        {
        // Most synthesizers send patch dumps to Edisyn via a single sysex message which
        // is handled using the parse(...) method.  But some synthesizers, such as the
        // PreenFM2, send patch dumps as multiple separate NRPN or CC messages.  If this
        // is the case, you should override this method to return FALSE so Edisyn can
        // detect this during its batch patch-download process.
        return true;
        }
        
    public boolean getSendsParametersAfterNonMergeParse()
        {
        // Some synthesizers cannot change patches via program change when in multi-mode.
        // So when you issue a patch request, they just give Edisyn the patch, but don't
        // switch to playing it.  So this command issues a sendAllParameters() on receiving
        // a (non-merge) parse from the synthesizer to keep it up to date.  Example synths
        // with this issue: Waldorf Blofeld and Microwave.
        return false;
        }

    public boolean getSendsParametersOnlyOnSendCurrentPatch()
        {
        // If this returns true, then Edisyn will only sendAllParmameters() when the user
        // directly selects "Send Current Patch", and in no other situation (such as
        // undo/redo, or hill-climbing, or on patch load, etc.)
        return false;
        }

    public boolean getShouldChangePatchAfterWrite()
        { 
        // Some synthesizers, such as the Kyra, write to patch memory but don't appear to
        // overwrite temporary memory as well.  You can set this to true to send to temporary
        // memory.
        return false; 
        }

    public int getVoiceMessageRoutedChannel(int incomingChannel, int synthChannel)
        {
        // Some synthesizers need to reroute voiced messages (messages with channels) from
        // the controller to the synthesizer along some other channel.  For example, the KawaiK4
        // needs to route drum notes to a special channel different from the standard K4
        // input channel.  If you need to customize the channel that the Controller routes
        // to, override this to return some other channel.
        return synthChannel;
        }
        
    public void messageFromController(MidiMessage message, boolean interceptedForInternalUse, boolean routedToSynth)
        { 
        // Whenever a message from the controller arrives, this message is called.  It is possible
        // for both of these parameters to be FALSE, if there was an error in reconstructing the
        // message to send it out, or (much more likely) if the user turned off routing.  It is
        // presently NOT possible for both of these values to be true; though this might be the case
        // in the future.
        return; 
        }

    public boolean getSendsParametersAfterLoad()
        {
        // This is called immediately after a successful load but before sending the parameters.
        // If your synth shouldn't return parameters after a load, perhaps because sending is
        // costly, override this to return false, or pop up a dialog asking the user what to do
        // and return that.
        return true;
        }
                
    public boolean sendAllSoundsOffWhenWindowChanges()
        {
        // When a window becomes the front window, is closed, quits, etc., then
        // by default Edisyn sends all sounds off.  Override
        // this method to return FALSE if you don't want this to happen.  [Normally you do]. 
        return true;
        }

    public boolean adjustBulkSysexForWrite(Synth window, byte[][][] data)
        {
        // Before a bank sysex file is emitted to a synthesizer, you're given the 
        // chance to modify the sysex messages, typically to modify the channel or ID.
        // If you return false, then the write is canceled.  The data arranged as:
        // byte[patch][sysexmessage][bytes], that is, each patch can have multiple
        // sysex messages, each of which is some number of bytes.  The provided
        // synthesizer is *not* the synthesizer for the data (that's you).  Instead, it
        // allows you to properly pop up a confirm dialog centered at the given window.
        // That's all it should be used for.
        return true;
        }
                 
    public Object adjustBankSysexForEmit(byte[] data, Model model)
        {
        // Before a bank sysex file is emitted to a synthesizer, you're given the 
        // chance to adjust the data, typically to modify the channel or ID
        return data;
        }
                 
    public JComponent getAdditionalBankSysexOptionsComponents(byte[] data, String[] names)
        {
        // Before asking the user what he wants to do with a bank sysex file, this method
        // is called to provide an additional JComponent you can sneak in the dialog.
        // You might use the results of this JComponent to inform what you modify in
        // adjustBankSysexForEmit.  It's a rare need though.  By default we return null.
        return null; 
        }
 
    public boolean setupBatchStartingAndEndingPatches(Model startPatch, Model endPatch)
        {
        // This method normally queries the user for start and end patch numbers/banks to
        // use for batch downloading, then sets those patch numbers/banks in the given models,
        // and returns true, else false if the user canceled the operation.  In rare cases 
        // you may need to customize this, such as to hard-code the start and end patch.
        // Otherwise, don't override it.
        return super.setupBatchStartingAndEndingPatches(startPatch, endPatch);
        }

    public int getNumberOfPastes()
        {  
        // Override this method to force Edisyn to paste multiple times to the same category or tab.
        // The reason you might want to do this is because Edisyn uses the *receiving* category to 
        // determine the parameters to paste to, and if this category contains components which dynamically
        // appear or disappear, it might require multiple pastes to cause them to appear and eventually
        // receive parameter changes.  The default returns DEFAULT_PASTES (3), which is fine for all
        // current editors.
        return DEFAULT_PASTES; 
        }
        
//    public void sendAllSoundsOff()
//        {
//        // By default this method sends All Notes Off and All Sounds Off to every single
//        // channel.  You might want to override this because
//        // this behavior causes problems for your synthesizer (ahem Hydrasynth), or because
//        // your synthesizer has some nonstandard approach to clearing sounds.  Note that if you
//        // are trying to prevent Edisyn from sending all sounds off / all notes off temporarily,
//        // often because it's hard to debug with it, you can instead just set the static final
//        // boolean sendsAllSoundsOff = false; in Synth.java.
//        super.sendAllSoundsOff();
//        }

    public boolean getRequiresNRPNMSB() 
        { 
        // Returns true if this synth will always provide the MSB in all NRPN messages.
        // When an NRPN message comes in, it may come in with just an LSB, just an MSB, or with an LSB and MSB in either order. 
        // Since normally don't know, the NRPN parser must assume that an incoming LSB may be all there is to the message
        // and update edisyn with just the LSB and the MSB set to 0 (or correspondingly with just the MSB and the LSB set to 0),
        // and only update a second time when the other part arrives.  This in turn can cause a variety of revise() problems.
        // To deal with this, if you know the synth will always produce an MSB in every NRPN message, you can override this to
        // return TRUE (it returns FALSE by default).
        return false; 
        }

    public boolean getRequiresNRPNLSB() 
        { 
        // Returns true if this synth will always provide the LSB in all NRPN messages.
        // When an NRPN message comes in, it may come in with just an LSB, just an MSB, or with an LSB and MSB in either order. 
        // Since normally don't know, the NRPN parser must assume that an incoming LSB may be all there is to the message
        // and update edisyn with just the LSB and the MSB set to 0 (or correspondingly with just the MSB and the LSB set to 0),
        // and only update a second time when the other part arrives.  This in turn can cause a variety of revise() problems.
        // To deal with this, if you know the synth will always produce an LSB in every NRPN message, you can override this to
        // return TRUE (it returns FALSE by default).
        return false; 
        }

    public boolean testVerify(Synth synth2, String key, Object obj1, Object obj2)
        {
        // The edisyn.test.SanityCheck class performs sanity-checks on synthesizer classes
        // by randomizing a synth instance, then writing it out, then reading it back in in a new synth, 
        // and comparing the two.  When parameters are different, this could be because of an emit bug 
        // or a parse bug, OR it could be entirely legitimate (perhaps you don't emit a certain 
        // parameter, or use it for a special purpose, etc.)  Before it issues an error in this case,
        // it calls this method to see if the difference is legitimate.  It calls testVerify(...)
        // on the first synth, passing in the second one.  The parameter in question is provided as
        // a key, as are the two values (as Strings or Integers) in question.  Return TRUE if the
        // difference is legitimate, else false.  By default, all differences are considered illegitimate.
        return false;
        }

    public boolean testVerify(byte[] message)
        {
        // The edisyn.test.SanityCheck class performs sanity-checks on synthesizer classes
        // by randomizing a synth instance, then writing it out, then reading it back in in a new synth, 
        // and comparing the two.  When the receiving synth instance gets a sysex message it doesn't
        // recognize, this method is called to determine if that's okay and it shoud be ignored.
        // Return TRUE if the message is acceptable and should be ignored, else false.  
        return false;
        }




    ////// LIBRARIAN SUPPORT
    //////
    ////// You will need to override some of these methods in order to support the librarian
    ////// working properly with your patch editor.  If you do not intend to permit the librarian
    ////// then you do not need to override any of them except possibly getUpdatesListenersOnDownload(),
    ////// which also affects batch downloads in general.


    /** Return a list of all patch number names, such as "1", "2", "3", etc.
        Default is null, which indicates that the patch editor does not support librarians.  */
    @Override
    public String[] getPatchNumberNames() {
        return PATCH_NUMBERS;
    }

    @Override
    public String[] getBankNames() {
        return BANKS;
    }

//    public boolean[] getWriteableBanks()
//        {
//        // This should return a list of booleans, one per bank, indicating if the
//        // bank is writeable.  You may not return null here: if getBankNames() returned null,
//        // then you should return { true } or { false } as appropriate.  The default form
//        // returns an array that is all true.
//        //
//        // Synth.buildBankBooleans(...) is a useful utility method for building this array
//        // for you if you don't want to implement it by hand.
//        return super.getWriteableBanks();
//        }

    public boolean getSupportsPatchWrites()
        {
        // Return true if the synth can receive and store individual patch writes (to actual
        // patch RAM, NOT sends to current working memory).  The default is false.
        //
        // Either this method, or getSupportsBankWrites(), or both, should be true if you are
        // supporting a librarian.
        return false; 
        }

    public boolean getSupportsBankWrites() 
        { 
        // Return true if the synth can receive and store bank writes.  The default is false.
        //
        // Either this method, or getSupportsPatchWrites(), or both, should be true if you are
        // supporting a librarian.
        return false; 
        }

    public boolean getSupportsBankReads() 
        { 
        // Return true if the synth can dump bank messages that your editor can read.  By default
        // this just returns whatever getSupportsBankWrites() returned.  However it is possible
        // that your editor can READ banks from the synth even if it cannot WRITE banks to the synth
        // and must instead write individual patches.  In this case getSupportsBankWrites() might
        // return false but getSupportsBankReads() would return true.
        return getSupportsBankWrites(); 
        }

    public boolean getSupportsDownloads() 
        {
        // Return true if the synth can respond to requests to download individual or bank patches.
        // If you return false, Edisyn won't permit users to attempt a download.  By default,
        // true is returned.
        return true; 
        }

    public int getPatchNameLength() 
        {
        return 3;
        }

//    public String reviseBankName(String name)
//        {
//        // Given a name for a bank, revises it to a valid name.  By default, this method
//        // returns null, which indicates that bank names may not be revised.  There is only
//        // one synthesizer supported by Edisyn which permits revised bank names at present:
//        // The Yamaha FB-01.
//        //
//        // Note that this method has an evil twin in your recognizer class.  See
//        // BlankRec.getBankName(...)
//        return null;
//        }
    
    public boolean isValidPatchLocation(int bank, int num) 
        {
        // Returns TRUE if the given bank and patch number define a valid patch location.  A valid
        // location is one that actually exists.
        //
        // The reason for this method is that some synthesizers have banks with different lengths.
        // For example, the Casio CZ-230s has fewer patches (4) in its final bank than in others (8).
        // Similarly, the Proteus 2000 has ragged banks -- some have 128 patches, some have 512 patches,
        // some have 1024 patches, and so on. In other cases, certain kinds of synthsizers permit more 
        // patches in banks than other synthesizers of the same family, but must share the same sysex 
        // files.  In these cases, Edisyn permits patches to be placed into "invalid" slots (defined by
        // this method), and saved to files from them, but not written to synthesizers from those locations.
        //
        // The bank values passed in will always be between 0 and the number of banks (minus 1) inclusive.
        // Similarly the patch numbers passed in will always be between 0 and getPatchNumberNames() - 1
        // inclusive.  By default this method always returns true, which is in most cases correct.
        return bank >= 0 && bank <= 3 && num >= 0 && num <= 99;
        }
        
    public boolean isAppropriatePatchLocation(int bank, int num)
        {
        // Returns TRUE if the given bank and patch number define an "appropriate" patch location.  An
        // "appropriate" location is one from which the the user is encouraged to upload and download from.
        //
        // For example, the Proteus 2000 has many "banks" corresponding to ROM SIMM Cards -- almost 20 of
        // them -- but only our cards can exist in a machine at a time.  Edisyn allows the user to load
        // and save those "banks" to/from disk even if he oes not have them installed on his machine --
        // he can even attempt to upload/download from them but it would be stupid to do so.  In this case
        // we merely want to color the patches as warning, not prevent the user from doing what he wants
        // The difference beween "appropriate" locaations and "valid" locations is that whether a location
        // is "appropriate" may depend on the particular configuration of the synth (among many possible
        // configurations), where as "invalid" locations are *always* invalid.
        //
        /// The bank values passed in will always be between 0 and the number of banks (minus 1)
        // inclusive. Similarly the patch numbers passed in will always be between 0 and
        // getPatchNumberNames() - 1 inclusive.  By default this method always returns true, which is
        // in most cases correct.
            return bank >= 0 && bank <= 3 && num >= 0 && num <= 99;
        }

//    public int getValidBankSize(int bank)
//        {
//        // Returns the actual number of valid patches in the bank (see isValidPatchLocation(...)).
//        // By default this is just the "standard" bank size as returned by getPatchNumberNames().length,
//        // indicated with a -1.
//        return -1;
//
//        // A simple but stupid O(n) way to compute this would be:
//        //
//        //String[] s = getPatchNumberNames();
//        //if (s == null) return 0;
//        //int valid = 0;
//        //for(int i = 0; i < s.length; i++)
//        //      {
//        //      if (isValidPatchLocation(bank, i))
//        //              valid++;
//        //      }
//        //return valid;
//        }
    
    public boolean getUpdatesListenersOnDownload() 
        {
        // Returns true if we should disable updating listeners on batch downloads.  This is 
        // normally only done for very large patch editors such as YamahaFS1RFseq, where 
        // such updating is extremely costly/slow or creates memory leaks.  By default, returns true.
        return true; 
        } 
    
    public boolean librarianTested() 
        {
        // Override this method to return true to indicate that the librarian for this
        // editor has been tested reasonably well and no longer requires a warning to the
        // musician when he attempts to use it.  By default this method returns false.
        return false; 
        }

    public byte[] requestAllDump() 
        { 
        // Returns a sysex message to request all patches from the synthesizer.  If your synthesizer
        // does not support this kind of request, this method should return null (the default).
        // This method is meant for synthesizers with multiple banks.  If the synthesizer has a 
        // single bank, and you support bank sysex messages (see below),
        // then you instead should override requestBankDump() instead.
        //
        // Edisyn can support all-patches dump requests in which the synthesizer responds by dumping
        // each patch individually.  If the synthesizer responds by dumping banks as bank messages,
        // this will cause Edisyn to ask the user, each time, where the bank should go, which isn't
        // great.  So if your synth only provides all-patches dump requests with bank responses
        // (and I don't know of any that do), get ahold of me first -- Sean.
        return null; 
        }
    
    public void librarianCreated(Librarian librarian) 
        {
        // This is simply a hook to let your Synth know that its Librarian has been created.
        // The Proteus 2000 editor uses this to rearrange the Librarian's columns. 
        }
        
        

    //// THE NEXT SIX METHODS WOULD ONLY BE IMPLEMENTED WHEN BANK SYSEX MESSAGES ARE SUPPORTED.
    ////
    //// YOU WILL ALSO NEED TO IMPLEMENT BANK SYSEX HANDLING IN parseAll(Model, ...) AND ALSO
    //// RECOGNIZE BANK SYSEX IN YOUR PATCH EDITOR RECOGNIZER CLASS. ALSO YOU CAN THEORETICALLY
    //// IMPLEMENT requestBankDump() EVEN IF YOU DON'T SUPPORT BANK SYSEX.

    public int parseFromBank(byte[] bankSysex, int number) 
        {
        // Given a bank sysex message, and a patch number, parses that patch from the
        // bank sysex data, and returns PARSE_SUCCEEDED or PARSE_FAILED.  The default is to
        // return PARSE_FAILED.  This method only needs to be implemented if your patch
        // editor supports bank reads (see documentation for getSupportsBankReads()
        // and getSupportsBankWrites())
        return PARSE_FAILED; 
        }

    public int getBank(byte[] bankSysex) 
        { 
        // Given a bank sysex message, returns the bank number of the given bank, else 
        // -1 if there is no number indicated.  The default is to return -1.
        // This method only needs to be implemented if your patch
        // editor supports bank reads (see documentation for getSupportsBankReads()
        // and getSupportsBankWrites())
        return -1; 
        }

    public int[] getBanks(byte[] bankSysex) 
        { 
        // Given a bank sysex message, returns the bank numbers of the banks in the message,
        // else null if there are no numbers indicated for them.  The default calls
        // getBank(...) and returns null if it returned -1, else returns an array consisting
        // of the getBank(...) value.  Normally you wouldn't override this method, it's only
        // needed for unusual synths which return more than one bank in a single bank message
        // (such as the Waldorf MicroWave).
        return super.getBanks(bankSysex); 
        }

    public Object[] emitBank(Model[] models, int bank, boolean toFile) 
        { 
        // Builds a set of models collectively comprising one bank's worth of patches,
        // and a bank number, emits sysex and MIDI messages meant to write this bank
        // as a collective bank message.  The objects which may be placed in the Object[]
        // are the same as those returned by emitAll().   This method only needs to be 
        // implemented, if your patch editor supports bank reads (see documentation for 
        // getSupportsBankReads() and getSupportsBankWrites()).  By default an empty
        // array is returned.
        return new Object[0]; 
        }
    
    public int getPauseAfterWriteBank() 
        {
        // Returns the pause, in milliseconds, after writing a bank sysex message
        // to the synthesizer.  By default this returns the value of 
        // getPauseAfterWritePatch();   This method only needs to be implemented 
        // if your patch editor supports bank reads (see documentation for 
        // getSupportsBankReads() and getSupportsBankWrites()).
        return getPauseAfterWritePatch(); 
        }    
    
    public byte[] requestBankDump(int bank) 
        { 
        // Returns a sysex message to request a given bank dump.  If your synthesizer
        // does not permit bank dump requests, return null (the default).   This method 
        // only needs to be implemented, if at all,
        // if your patch editor supports bank reads (see documentation for 
        // getSupportsBankReads() and getSupportsBankWrites()).
        //
        // It's reasonsble for the synth to respond to a dump request of this kind by
        // sending all patches one by one or by sending a bank sysex.
        return null; 
        }

    public int getRequestableBank() 
        {
        // Some synths (such Yamaha 4-op) can request individual patches from any bank, but
        // can only request a single bank via a bank sysex message provided in requestBankDump().
        // This method returns that bank, or -1 if any bank can be requested via requestBankDump().
        // The default is -1.  This method only needs to be method if your patch editor supports
        // bank reads (see documentation for getSupportsBankReads() and getSupportsBankWrites()) 
        // and also bank requests (via requestBankDump()).
        return -1; 
        }

    public Object[] startingBatchEmit(int bank, int start, int end, boolean toFile) 
        { 
        // Called before a series of patches are being emitted from the librarian 
        // (as opposed to a single patch from the Editor).  This might give your editor
        // a chance to add something to the beginning of the data.  For
        // example, the ASM Hydrasynth requires that a header sysex command be
        // sent before a stream of batch dumps.  You can determine if 
        // a series of patches is being emitted during emit() by calling isEmittingBatch(). 
        // Note that this method is NOT called if a bank is being emitted via a bank sysex message.
        // See also stoppingBatchDownload() and startingBatchDownload()
        return new Object[0]; 
        }

    public Object[] stoppingBatchEmit(int bank, int start, int end, boolean toFile) 
        { 
        // Called after a series of patches are being emitted from the librarian 
        // (as opposed to a single patch from the Editor).  This might give your editor
        // a chance to add something to the beginning of the data.  For
        // example, the ASM Hydrasynth requires that a header sysex command be
        // sent before a stream of batch dumps.  You can determine if 
        // a series of patches is being emitted during emit() by calling isEmittingBatch(). 
        // Note that this method is NOT called if a bank is being emitted via a bank sysex message.
        // See also stoppingBatchDownload() and startingBatchDownload()
        return new Object[0]; 
        }

    //// END BANK SYSEX SUPPORT

    private String parseVersion(byte swVersion, byte swIncrement) {
        int major = (swVersion >> 3);
        int minor = swVersion & 0x7;
        return major + "." + minor + "." + swIncrement;
    }
}
