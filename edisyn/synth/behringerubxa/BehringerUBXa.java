package edisyn.synth.behringerubxa;

import edisyn.Midi;
import edisyn.Synth;
import edisyn.gui.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

import static edisyn.synth.behringerubxa.BehringerUBXaRec.EOF;
import static edisyn.synth.behringerubxa.BehringerUBXaRec.SysExHeader;
import static edisyn.synth.behringerubxa.ParameterList.*;

public class BehringerUBXa extends Synth {

    public static final String BITMASK_SEP = "@@";

    private String lastDialEmitKey = null;
    private int lastDialEmitIdx = -1;

    private int lastDialReceiveIdx = -1;
    private final java.util.List<byte[]> patchDump = new ArrayList<>();

    private final java.util.List<String> usedKeys = new ArrayList<>();


    public static String getSynthName() {
        return "Behringer UB-Xa";
    }

    public static String[] splitAtCapitalLetter(String input, int k) {
        java.util.List<String> out = new ArrayList<>();
        StringBuilder s = new StringBuilder(input.substring(0, 1));
        for (int i = 1; i < input.length(); i++) {
            if (Character.isUpperCase(input.charAt(i))
                    && i != input.length() - 1
                    && !Character.isUpperCase(input.charAt(i + 1))
                    && Character.isLetter(input.charAt(i + 1))) {
                out.add(s.toString());
                s = new StringBuilder(input.substring(i, i + 1));
            } else {
                s.append(input.charAt(i));
            }
        }
        out.add(s.toString());
        return out.toArray(new String[0]);
    }

    public static String[] splitAtCapitalLetter_(String input, int k) {
        // Check if the input length is greater than k
        if (input.length() <= k) {
            // If not, return the input as a single string in an array
            return new String[]{input};
        }

        int midpoint = input.length() / 2;

        // Search backward from the midpoint for a capital letter
        for (int i = midpoint; i > 0; i--) {
            if (Character.isUpperCase(input.charAt(i))) {
                return new String[]{
                        input.substring(0, i),
                        input.substring(i)
                };
            }
        }

        // If no capital letter is found before midpoint, search forward
        for (int i = midpoint; i < input.length(); i++) {
            if (Character.isUpperCase(input.charAt(i))) {
                return new String[]{
                        input.substring(0, i),
                        input.substring(i)
                };
            }
        }

        // If no capital letter is found, return the entire string as a single string in the array
        return new String[]{input};
    }


    public static String longestCommonWordPrefix(String str1, String str2) {
        String[] words1 = str1.split(" ");
        String[] words2 = str2.split(" ");

        int minLength = Math.min(words1.length, words2.length);

        StringBuilder commonPrefix = new StringBuilder();

        for (int i = 0; i < minLength; i++) {
            if (!words1[i].equals(words2[i])) {
                break;
            }
            commonPrefix.append(words1[i]).append(" ");
        }

        // Remove the last extra space if any common prefix is found
        if (!commonPrefix.isEmpty()) {
            commonPrefix.setLength(commonPrefix.length() - 1);
        }

        return commonPrefix.toString();
    }


    private void addDialByKey(JComponent container, String key, String label) {
        assert !usedKeys.contains(key);
        for (int i = 0; i < dials.length; i += NUM_PARAMS_DIALS) {
            if (dials[i].equals(key)) {
                int minVal = (int) dials[i + 2];
                int maxVal = (int) dials[i + 3];
                boolean symmetric = (boolean) dials[i + 4];

                addDial(container, key, label, minVal, maxVal, symmetric);
                break;
            }
        }


    }

    private JComponent makeEnv(String a, String d, String s, String r, String mods) {
        HBox container = new HBox();
        VBox h = new VBox();
        addCheckboxGroupByKey(h, mods);
        container.add(h);

        HBox envDials = new HBox();
        addDialByKey(envDials, a, "Attack");
        addDialByKey(envDials, d, "Decay");
        addDialByKey(envDials, s, "Sustain");
        addDialByKey(envDials, r, "Release");

        EnvelopeDisplay ed = new EnvelopeDisplay(
                this, Style.ENVELOPE_COLOR(),
                new String[]{null, a, d, null, r},
                new String[]{null, null, s, s, null},
                new double[]{0, 0.25 / 16383, 0.25 / 16383, 0.25, 0.25 / 16383},
                new double[]{0, 1.0, 1.0 / 16383, 1.0 / 16383, 0}

        );
        envDials.add(ed);
        container.add(envDials);

        return container;
    }

    private JComponent categoryContainer(HBox parent, String title, Color color, boolean addLast) {
        VBox v = new VBox();
        if (addLast) {
            parent.addLast(v);
        } else {
            parent.add(v);
        }
        Category c = new Category(this, title, color);
        v.add(c);

        HBox h = new HBox();
        v.add(h);
        return h;
    }

    public BehringerUBXa() {
        assert checkboxGroups.length % NUM_PARAMS_CHECKBOXES == 0;
        assert dials.length % NUM_PARAMS_DIALS == 0;
        assert selectors.length % NUM_PARAMS_SELECTORS == 0;

        JComponent main = new SynthPanel(this);

        VBox mainVbox = new VBox();
        main.add(mainVbox, BorderLayout.CENTER);
        {
            HBox row1 = new HBox();
            mainVbox.add(row1);
            JComponent controlCat = categoryContainer(row1, "Behringer UB-Xa", Color.WHITE, false);

            addDialByKey(controlCat, "ControlPortamentoAmount", "Portamento\nAmount");
            addDialByKey(controlCat, "ControlUnison", "Unison"); // would be nice with a button
            addDialByKey(controlCat, "ControlDetune", "Detune");

            JComponent arpCat = categoryContainer(row1, "Arpeggiator", Style.COLOR_C(), true);
            VBox arpChoosersCol1 = new VBox();
            arpCat.add(arpChoosersCol1);
            addChooserByKey(arpChoosersCol1, "ArpeggiatorMode", "Mode");
            addChooserByKey(arpChoosersCol1, "ArpeggiatorTime", "Time");
            VBox arpChoosersCol2 = new VBox();
            arpCat.add(arpChoosersCol2);
            addChooserByKey(arpChoosersCol2, "ArpeggiatorSync", "Sync");

            HBox arpDials = new HBox();
            arpCat.add(arpDials);
            addDialByKey(arpDials, "ArpeggiatorEnabled", "Enabled"); //button?
            addDialByKey(arpDials, "ArpeggiatorHold", "Hold"); //button?
            addDialByKey(arpDials, "ArpeggiatorGatetime", "Gate Time");
            addDialByKey(arpDials, "ArpeggiatorOctave", "Octave");
            addDialByKey(arpDials, "ArpeggiatorSwing", "Swing");
            addDialByKey(arpDials, "ArpeggiatorRepeat", "Repeat");

        }
        {
            HBox row2 = new HBox();
            mainVbox.add(row2);
            JComponent oscCat = categoryContainer(row2, "Oscillators", Style.COLOR_A(), false);

            VBox osc1v = new VBox();
            oscCat.add(osc1v);

            addChooserByKey(osc1v, "OscillatorsOSC1Shapes", "Osc 1 Shapes");
            addCheckboxGroupByKey(osc1v, "OscillatorsOSC1State",new String[]{"Osc 1 State","Osc 1 VCO LFO Phase","Osc 1 VCO PWM Phase"},false);

            VBox osc2v = new VBox();
            oscCat.add(osc2v);
            addChooserByKey(osc2v, "OscillatorsOSC2Shapes", "Osc 2 Shapes");
            addChooserByKey(osc2v, "OscillatorsOSC2State", "Osc 2 State");
            addCheckboxGroupByKey(osc2v, "OscillatorsMode");

            addDialByKey(oscCat, "OscillatorsOSC1Transpose", "Osc 1\nTranspose");
            addDialByKey(oscCat, "OscillatorsOSC1PWAmount", "Osc 1\nPW Amount");
            addDialByKey(oscCat, "OscillatorsOSC2Transpose", "Osc 2\nTranspose");
            addDialByKey(oscCat, "OscillatorsOSC2PWAmount", "Osc 2\nPW Amount");

            JComponent filterCat = categoryContainer(row2, "Filter", Style.COLOR_B(), true);
            VBox filterV = new VBox();
            filterCat.add(filterV);

            addFilterModes(filterV);

            HBox filterDials = new HBox();
            addDialByKey(filterDials, "FilterFrequency", "Frequency");
            addDialByKey(filterDials, "FilterResonance", "Resonance");
            addDialByKey(filterDials, "FilterModulation", "Modulation");
            addDialByKey(filterDials, "FilterNoise", "Noise");
            filterCat.add(filterDials);
        }

        {
            HBox row3 = new HBox();
            mainVbox.add(row3);
            JComponent loudnessEnvCat = categoryContainer(row3, "Loudness Envelope", Style.COLOR_A(), false);
            JComponent loudnessEnv = makeEnv("EnvelopesLoudnessA", "EnvelopesLoudnessD", "EnvelopesLoudnessS", "EnvelopesLoudnessR", "EnvelopesLoudnessMods");
            loudnessEnvCat.add(loudnessEnv);

            JComponent filterEnvCat = categoryContainer(row3, "Filter Envelope", Style.COLOR_B(), true);
            JComponent filterEnv = makeEnv("EnvelopesFilterA", "EnvelopesFilterD", "EnvelopesFilterS", "EnvelopesFilterR", "EnvelopesFilterMods");
            filterEnvCat.add(filterEnv);
        }
// ModulationLFOMods
        {
            HBox row4 = new HBox();
            mainVbox.add(row4);
            JComponent modCat = categoryContainer(row4, "Modulation", Style.COLOR_C(), false);

            VBox modMisc = new VBox();
            addChooserByKey(modMisc, "ModulationLFOShapes", "Shapes");
            addCheckboxGroupByKey(modMisc, "ModulationLFOMods");
            modCat.add(modMisc);
            VBox modDialsAndQuirks = new VBox();
            modCat.add(modDialsAndQuirks);
            HBox modDials = new HBox();
            modDialsAndQuirks.add(modDials);
            addDialByKey(modDials, "ModulationLFOTrigPoint", "LFO\nTrig Point");
            addDialByKey(modDials, "ModulationLFORate", "LFORate");
            addDialByKey(modDials, "ModulationLFOPhase", "LFOPhase");
            addDialByKey(modDials, "ModulationLFOTrim", "LFOTrim");
            HBox modQuirks = new HBox();
            addCheckboxGroupByKey(modQuirks, "ModulationQuirks");
            modDialsAndQuirks.add(modQuirks);

            JComponent modC1Cat = categoryContainer(row4, "Mod Channel 1", Style.COLOR_C(), false);
            VBox modSelsC1 = new VBox();
            modC1Cat.add(modSelsC1);
            addCheckboxGroupByKey(modSelsC1, "ModulationChannel1Sends");
            addCheckboxGroupByKey(modSelsC1, "ModulationChannel1Mods");
            addDialByKey(modC1Cat, "ModulationChannel1Amount", "Amount");
            addDialByKey(modC1Cat, "EnvelopesModChannel1A", "Attack");
            addDialByKey(modC1Cat, "EnvelopesModChannel1Delay", "Delay");

            JComponent modC2Cat = categoryContainer(row4, "Mod Channel 2", Style.COLOR_C(), true);
            VBox modSelsC2 = new VBox();
            modC2Cat.add(modSelsC2);
            addCheckboxGroupByKey(modSelsC2, "ModulationChannel2Sends");
            addCheckboxGroupByKey(modSelsC2, "ModulationChannel2Mods");
            addDialByKey(modC2Cat, "ModulationChannel2Amount", "Amount");
            addDialByKey(modC2Cat, "EnvelopesModChannel2A", "Attack");
            addDialByKey(modC2Cat, "EnvelopesModChannel2Delay", "Delay");


        }
        addTab("Main", main);

        for (String ctrlGrp : ctrlGroups) {
            JComponent p = new SynthPanel(this);
            JComponent box = makeGroupedControls(ctrlGrp);
            p.add(box, BorderLayout.CENTER);
            addTab(ctrlGrp, p);
        }

        // Check that we've added all controls at this point
        for (int i = 0; i < dials.length; i += NUM_PARAMS_DIALS) {
            String key = (String) dials[i];
            assert (usedKeys.contains(key));
        }

        for (int i = 0; i < selectors.length; i += NUM_PARAMS_SELECTORS) {
            String key = (String) selectors[i];
            assert (usedKeys.contains(key));
        }

        for (int i = 0; i < checkboxGroups.length; i += NUM_PARAMS_CHECKBOXES) {
            String key = (String) checkboxGroups[i];
            assert (usedKeys.contains(key));
        }
    }

    private void addFilterModes(JComponent container) {
        String[] opts = new String[]{"2 Pole", "4 Pole"}; // order is "switched"
        Chooser filterType = new Chooser("Filter Type", this, "FilterModes@@4 Pole~2 Pole", opts, new int[]{0, 1});
        container.add(filterType);
        CheckBox filterTracking =  new CheckBox("Filter Tracking", this, "FilterModes@@Filter Tracking on~Filter Tracking off");
        container.add(filterTracking);
        usedKeys.add("FilterModes");
    }

    private void addChooserByKey(JComponent container, String key, String label) {
        boolean found = false;
        for (int i = 0; i < selectors.length; i += NUM_PARAMS_SELECTORS) {
            if (key.equals(selectors[i])) {
                String[] opts = (String[]) selectors[i + 2];
                addChooser(container, key, label, opts);
                usedKeys.add(key);
                found = true;
            }
        }
        assert found;
    }

    private void addCheckboxGroupByKey(JComponent container, String key) {
        addCheckboxGroupByKey(container, key, null,true);
    }

    private void addCheckboxGroupByKey(JComponent container, String key, String[] chooserLabels,boolean preferCheckbox ) {
        for (int i = 0; i < checkboxGroups.length; i += NUM_PARAMS_CHECKBOXES) {
            if (key.equals(checkboxGroups[i])) {
                String[] labels = (String[]) checkboxGroups[i + 3];
                addCheckboxGroup(container, key, labels, chooserLabels, preferCheckbox);
                return;
            }
        }
        assert false;
    }

    private void addChooser(JComponent container, String key, String label, String[] opts) {
        int[] vals = new int[opts.length];
        for (int j = 0; j < opts.length; j++) {
            vals[j] = j;
        }
        JComponent comp = new Chooser(label, this, key, opts, vals);

        container.add(comp);
        usedKeys.add(key);
    }

    private void addDial(JComponent container, String key, String lbl, int minVal, int maxVal, boolean symmetric) {
        int sub = symmetric ? maxVal / 2 + 1 : 0;
        String[] labels;
        if (lbl.contains(" ") || lbl.contains("\n")){
            labels = lbl.split("\n");
        } else {
            labels = splitAtCapitalLetter(lbl, 10);
        }

        LabelledDial comp = new LabelledDial(labels[0], this, key, Style.COLOR_A(), minVal, maxVal, sub) {
            public boolean isSymmetric() {
                return symmetric;
            }
        };
        for (int i = 1; i < labels.length; i++) {
            comp.addAdditionalLabel(labels[i]);
        }

        container.add(comp);
        usedKeys.add(key);

    }

    private JComponent makeGroupedControls(String ctrlGrp) {
        JComponent vbox = new VBox();
        JComponent hbox = null;

        int j = 0;
        for (int i = 0; i < dials.length; i += NUM_PARAMS_DIALS) {

            String key = (String) dials[i];
            if (key.indexOf(ctrlGrp) != 0) continue;
            if (usedKeys.contains(key)) continue;

            if (j % 10 == 0) {
                hbox = new HBox();
                vbox.add(hbox);
            }
            j += 1;
            int minVal = (int) dials[i + 2];
            int maxVal = (int) dials[i + 3];
            boolean symmetric = (boolean) dials[i + 4];

            String label = key.substring(ctrlGrp.length());

            addDial(hbox, key, label, minVal, maxVal, symmetric);


        }

        hbox = null;
        j = 0;
        for (int i = 0; i < selectors.length; i += NUM_PARAMS_SELECTORS) {
            String key = (String) selectors[i];
            if (key.indexOf(ctrlGrp) != 0) continue;
            if (usedKeys.contains(key)) continue;

            if (j % 4 == 0) {
                hbox = new HBox();
                vbox.add(hbox);
            }
            j += 1;
            String[] opts = (String[]) selectors[i + 2];
            String label = key.substring(ctrlGrp.length());
            addChooser(hbox, key, label, opts);

        }

        for (int i = 0; i < checkboxGroups.length; i += NUM_PARAMS_CHECKBOXES) {
            String key = (String) checkboxGroups[i];
            if (key.indexOf(ctrlGrp) != 0) continue;
            if (usedKeys.contains(key)) continue;
            JComponent hbox2 = new HBox();
            String subCatTitle = key.substring(ctrlGrp.length());
            Category cat = new Category(this, subCatTitle, Color.WHITE);
            String[] labels = (String[]) checkboxGroups[i + 3];
            addCheckboxGroup(hbox2, key, labels, null,false);
            vbox.add(cat);
            vbox.add(hbox2);
        }

        return vbox;

    }

    private void addCheckboxGroup(JComponent container, String key, String[] lbls, String[] chooserLabels,boolean preferCheckbox) {
        assert chooserLabels == null || chooserLabels.length == lbls.length;
        int i = 0;
        for (String lbl : lbls) {
            JComponent comp;
            if (lbl.contains("~")) {
                String[] strs = lbl.split("~");
                String prefix = longestCommonWordPrefix(strs[0], strs[1]);
                String chooserLabel = chooserLabels!=null ? chooserLabels[i]:prefix;
                if (preferCheckbox && strs[0].equals(prefix + " on")
                        && strs[1].equals(prefix + " off")) {
                    comp = new CheckBox(chooserLabel, this, key + BITMASK_SEP + lbl);
                } else {
                    String[] opts = new String[]{strs[1], strs[0]}; // order is "switched"
                    comp = new Chooser(chooserLabel, this, key + BITMASK_SEP + lbl, opts, new int[]{0, 1});
                }
            } else {
                comp = new CheckBox(lbl, this, key + BITMASK_SEP + lbl);
            }
            container.add(comp);
            i++;
        }
        usedKeys.add(key);

    }

    private Object[] emitDial(String key, int i) {
        int param = (int) dials[i + 1];
        int val = getModel().get(key);
        return buildNRPN(getChannelOut(),
                param, val);
    }


    private Object[] emitSelector(String key, int i) {
        int param = (int) selectors[i + 1];
        int val = getModel().get(key);
        return buildNRPN(getChannelOut(),
                param, val);
    }

    private Object[]
    emitCheckboxGroup(String keyPrefix, int i) {
        int param = (int) checkboxGroups[i + 1];
        int sum = 0;
        int n = 0;
        for (String checkbox : (String[]) checkboxGroups[i + 3]) {
            String k = (keyPrefix + BITMASK_SEP + checkbox);
            sum += getModel().get(k) << n;
            n++;
        }
        return buildNRPN(getChannelOut(), param, sum);
    }

    @Override
    public Object[] emitAll(String key) {
        if (key.equals(lastDialEmitKey)) {
            // Caching
            return emitDial(key, lastDialEmitIdx);
        }

        for (int i = 0; i < dials.length; i += NUM_PARAMS_DIALS) {
            String label = (String) dials[i];
            if (label.equals(key)) {
                lastDialEmitKey = key;
                lastDialEmitIdx = i;
                return emitDial(key, i);
            }
        }

        String keyPrefix = key.split(BITMASK_SEP)[0];

        for (int i = 0; i < checkboxGroups.length; i += NUM_PARAMS_CHECKBOXES) {
            if (checkboxGroups[i].equals(keyPrefix)) {
                return emitCheckboxGroup(keyPrefix, i);
            }
        }

        for (int i = 0; i < selectors.length; i += NUM_PARAMS_SELECTORS) {
            if (selectors[i].equals(keyPrefix)) {
                return emitSelector(keyPrefix, i);
            }
        }

        assert false;
        return null;
    }

    private void setValueForDial(int dialIdx, int value) {
        String label = (String) dials[dialIdx];
        getModel().set(label, value);

    }

    private static String toBinaryString(int number) {
        // Convert the integer to a binary string
        String binaryString = Integer.toBinaryString(number);
        // Add the 0b prefix
        return "0b" + binaryString;
    }

    @Override
    public boolean getRequiresNRPNLSB() {
        return true;
    }

    @Override
    public boolean getRequiresNRPNMSB() {
        return true;
    }

    @Override
    public void handleSynthCCOrNRPN(Midi.CCData data) {

        if (dials[lastDialReceiveIdx + 1].equals(data.number)) {
            // Caching
            setValueForDial(lastDialReceiveIdx, data.value);
            return;
        }

        for (int i = 0; i < dials.length; i += NUM_PARAMS_DIALS) {
            if (dials[i + 1].equals(data.number)) {
                lastDialReceiveIdx = i;
                setValueForDial(i, data.value);
                return;
            }


        }


        for (int i = 0; i < checkboxGroups.length; i += NUM_PARAMS_CHECKBOXES) {
            if (checkboxGroups[i + 1].equals(data.number)) {
                String prefix = (String) checkboxGroups[i];
                for (int j = 0; j < (int) checkboxGroups[i + 2]; j++) {
                    String suffix = ((String[]) checkboxGroups[i + 3])[j];
                    String key = prefix + BITMASK_SEP + suffix;
                    int val = (data.value & (1 << j)) >> j;
                    getModel().set(key, val);
                }
                return;
            }
        }

        for (int i = 0; i < selectors.length; i += NUM_PARAMS_SELECTORS) {
            if (selectors[i + 1].equals(data.number)) {
                String key = (String) selectors[i];
                getModel().set(key, data.value);
            }
        }
    }

    public static byte[] generatePaddedByteArray(String asciiString, int n) {
        // Ensure the string is not longer than n characters
        if (asciiString.length() > n) {
            asciiString = asciiString.substring(0, n);
        }

        // Create a byte array of length n
        byte[] byteArray = new byte[n];

        // Copy the characters of the string into the byte array
        for (int i = 0; i < asciiString.length(); i++) {
            byteArray[i] = (byte) asciiString.charAt(i);
        }

        // Fill the remaining part of the array with spaces (ASCII value 32)
        for (int i = asciiString.length(); i < n; i++) {
            byteArray[i] = (byte) ' ';
        }

        return byteArray;
    }


    public byte[] requestCurrentDump() {
        byte[] data = new byte[36];

        data[10] = (byte) 0x03; // Request
        data[11] = (byte) 0x7F;
        System.arraycopy(SysExHeader, 0, data, 0, SysExHeader.length);
        byte[] ext = generatePaddedByteArray("BIN ", 4);
        System.arraycopy(ext, 0, data, 12, ext.length);
        byte[] fileName = generatePaddedByteArray("Upper Patch", 16);
        System.arraycopy(fileName, 0, data, 16, fileName.length);
        data[32] = (byte) 0x00;
        data[33] = (byte) 0x03;
        data[34] = (byte) 0x75;
        data[35] = (byte) 0xF7;
        return data;
    }

    public static String byteArrayAsHex(byte[] byteArray, int skip) {
        StringBuilder hexString = new StringBuilder();

        for (int i = 0; i < byteArray.length; i++) {
            byte b = byteArray[i];
            // Convert each byte to hex and append to the string builder
            if (i >= skip) {
                hexString.append(String.format("%02d: %02X ", i, b));
            }
        }

        // Print the final string, trimming the trailing space
        return hexString.toString().trim();
    }

    public boolean parsePatch() {
        int length = 0;
        for (byte[] b : this.patchDump) {
            length += b.length;
        }
        byte[] data = new byte[length];
        int dstPos = 0;
        for (byte[] b : this.patchDump) {
            System.arraycopy(b, 0, data, dstPos, b.length);
        }
        for (int i = 0; i < SysExPosToKeyAndSize.length; i += 3) {
            int pos = (int) SysExPosToKeyAndSize[i];
            String key = (String) SysExPosToKeyAndSize[i + 1];
            int size = (int) SysExPosToKeyAndSize[i + 2];
            // FIXME
            int val = data[pos];// + data[pos+1] << 2;
            int i1 = data[pos] + (data[pos + 1] << 1);
//            System.out.println(key+": "+ i1 + "(" +data2[pos]+","+data2[pos+1]+")");
//            getModel().set(key, i1);
        }

        return true;
    }


    static void verify7bitizedData(byte[] in) {
        for (byte b : in) {
            assert (b & 0b10000000) == 0b00000000;
        }
    }

    static byte[] convertGrp(byte[] in) {
        assert in.length == 8; // todo - support remainder
        int l = in.length - 1;
        byte[] out = new byte[l];
        out[0] = (byte) ((0b0100000 & in[0]) + (0b01111111 & in[1]));
        out[1] = (byte) ((0b0010000 & in[0]) + (0b01111111 & in[2]));
        out[2] = (byte) ((0b0010000 & in[0]) + (0b01111111 & in[3]));
        out[3] = (byte) ((0b0001000 & in[0]) + (0b01111111 & in[4]));
        out[4] = (byte) ((0b0000100 & in[0]) + (0b01111111 & in[5]));
        out[5] = (byte) ((0b0000010 & in[0]) + (0b01111111 & in[6]));
        out[6] = (byte) ((0b0000001 & in[0]) + (0b01111111 & in[7]));
        return out;
    }

    static byte[] unpack7b(byte[] in) {
        int groups = in.length / 8;
        int remainder = in.length % 8;
        assert remainder == 0 : "Remainder not yet supported";
        int add = remainder == 0 ? 0 : remainder - 1;
        byte[] out = new byte[groups * 7 + add];
        for (int g = 0; g < groups; g++) {
            byte[] grp = new byte[8];
            System.arraycopy(in, g * 8, grp, 0, 8);
            byte[] res = convertGrp(grp);
            System.arraycopy(res, 0, out, g * 7, 7);
        }
        return out;
    }

    @Override
    public int parse(byte[] data, boolean fromFile) {
        if (data.length == EOF.length &&
                BehringerUBXaRec.msgStartsWith(data, BehringerUBXaRec.EOF)
        ) return parsePatch() ? PARSE_SUCCEEDED : PARSE_FAILED;
        int fileDumpMessageType = data[SysExHeader.length];

        if (fileDumpMessageType == (byte) 0x01) {
            // Clear on header
            patchDump.clear();
        } else {
            assert fileDumpMessageType == (byte) 0x02;
            int packetNum = data[11];
            assert packetNum == patchDump.size();


            int packageLength = data[12] + 1;
            int offset = 13; // the extra
            if (packetNum == 0) {


                byte[] encodedData = Arrays.copyOfRange(data, offset, offset + packageLength);
                verify7bitizedData(encodedData);
                byte[] decodedData = unpack7b(encodedData);

                // TODO - how to interpret the decoded data

            }
            patchDump.add(Arrays.copyOfRange(data, offset, packageLength + offset));
        }
        return PARSE_INCOMPLETE;
    }

}