package edisyn.synth.behringerubxa;

import edisyn.Midi;
import edisyn.Synth;
import edisyn.gui.*;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Array;
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

    private JComponent makeEnv(String title, String a, String d, String s, String r, String mods) {
        VBox container = new VBox();
        JComponent c = new Category(this, title, Color.WHITE);
        container.add(c);
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
        addCheckboxGroupByKey(envDials, mods);
        container.add(envDials);

        return container;
    }

    public BehringerUBXa() {
        assert checkboxGroups.length % NUM_PARAMS_CHECKBOXES == 0;
        assert dials.length % NUM_PARAMS_DIALS == 0;
        assert selectors.length % NUM_PARAMS_SELECTORS == 0;

        JComponent main = new SynthPanel(this);
        JComponent vbox = new VBox();

        Category c = new Category(this,"Control",Color.WHITE);
        vbox.add(c);
        HBox controls = new HBox();
        vbox.add(controls);
        addDialByKey(controls,"ControlPortamentoAmount","Portamento Amount");
        addDialByKey(controls,"ControlUnison","Unison"); // would be nice with a button
        addDialByKey(controls,"ControlDetune","Detune");

        c = new Category(this,"Arpeggiator", Color.WHITE);
        vbox.add(c);
        HBox arp = new HBox();
        vbox.add(arp);
        addDialByKey(arp,"ArpeggiatorEnabled","Enabled"); //button?
        addDialByKey(arp,"ArpeggiatorHold","Hold"); //button?
        addDialByKey(arp,"ArpeggiatorGatetime","Gate Time");
        addDialByKey(arp,"ArpeggiatorOctave","Octave");
        addDialByKey(arp,"ArpeggiatorSwing","Swing");
        addDialByKey(arp,"ArpeggiatorRepeat","Repeat");
        addSelectorByKey(arp,"ArpeggiatorMode","Mode");
        addSelectorByKey(arp,"ArpeggiatorTime","Time");
        addSelectorByKey(arp,"ArpeggiatorSync","Sync");

        c = new Category(this, "Modulation", Color.WHITE);
        vbox.add(c);
        HBox modDials = new HBox();
        vbox.add(modDials);
        addDialByKey(modDials, "ModulationLFOTrigPoint", "LFO Trig Point");
        addDialByKey(modDials, "ModulationLFORate", "LFO Rate");
        addDialByKey(modDials, "ModulationLFOPhase", "LFO Phase");
        addDialByKey(modDials, "ModulationChannel1Amount", "Channel 1 Amount");
        addDialByKey(modDials, "ModulationChannel2Amount", "Channel 2 Amount");
        addDialByKey(modDials, "ModulationLFOTrim", "LFO Trim");
        HBox modSelsC1 = new HBox();
        vbox.add(modSelsC1);
        addCheckboxGroupByKey(modSelsC1, "ModulationChannel1Sends");
        addCheckboxGroupByKey(modSelsC1, "ModulationChannel1Mods");
        HBox modSelsC2 = new HBox();
        vbox.add(modSelsC2);
        addCheckboxGroupByKey(modSelsC2, "ModulationChannel2Sends");
        addCheckboxGroupByKey(modSelsC2, "ModulationChannel2Mods");

        HBox modShapes = new HBox();
        vbox.add(modShapes);
        addSelectorByKey(modShapes, "ModulationLFOShapes", "LFO Shapes");

        HBox modMisc = new HBox();
        vbox.add(modMisc);
        addCheckboxGroupByKey(modMisc, "ModulationLFOMods");
        addCheckboxGroupByKey(modMisc, "ModulationQuirks");

        c = new Category(this, "Oscillators", Color.WHITE);
        vbox.add(c);

        HBox oscDials = new HBox();
        addDialByKey(oscDials, "OscillatorsOSC1Transpose", "OSC1 Transpose");
        addDialByKey(oscDials, "OscillatorsOSC1PWAmount", "OSC1 PW Amount");
        addDialByKey(oscDials, "OscillatorsOSC2Transpose", "OSC2 Transpose");
        addDialByKey(oscDials, "OscillatorsOSC2PWAmount", "OSC2 PW Amount");
        vbox.add(oscDials);

        HBox oscButtons = new HBox();
        addSelectorByKey(oscButtons, "OscillatorsOSC1Shapes", "OSC1 Shapes");
        addCheckboxGroupByKey(oscButtons, "OscillatorsMode");
        addSelectorByKey(oscButtons, "OscillatorsOSC2Shapes", "OSC2 Shapes");
        vbox.add(oscButtons);

        HBox oscillatorEnableButtons = new HBox();
        addCheckboxGroupByKey(oscillatorEnableButtons, "OscillatorsOSC1State");
        addSelectorByKey(oscillatorEnableButtons, "OscillatorsOSC2State", "OSC2 State");
        vbox.add(oscillatorEnableButtons);

        c = new Category(this, "Filter", Color.WHITE);
        vbox.add(c);

        HBox filterDials = new HBox();
        addDialByKey(filterDials, "FilterFrequency", "Frequency");
        addDialByKey(filterDials, "FilterResonance", "Resonance");
        addDialByKey(filterDials, "FilterModulation", "Modulation");
        addDialByKey(filterDials, "FilterNoise", "Noise");
        vbox.add(filterDials);
        addCheckboxGroupByKey(filterDials, "FilterModes");
        JComponent filterEnv = makeEnv("Filter Envelope", "EnvelopesFilterA", "EnvelopesFilterD", "EnvelopesFilterS", "EnvelopesFilterR","EnvelopesFilterMods");
        vbox.add(filterEnv);

        JComponent loudnessEnv = makeEnv("Loudness Envelope", "EnvelopesLoudnessA", "EnvelopesLoudnessD", "EnvelopesLoudnessS", "EnvelopesLoudnessR","EnvelopesLoudnessMods");
        vbox.add(loudnessEnv);

        main.add(vbox, BorderLayout.CENTER);
        addTab("Main", main);

        for (String ctrlGrp : ctrlGroups) {
            JComponent p = new SynthPanel(this);
            JComponent box =  makeGroupedControls(ctrlGrp);
            p.add(box,BorderLayout.CENTER);
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

    private void addSelectorByKey(JComponent container, String key, String label) {
        boolean found = false;
        for (int i = 0; i < selectors.length; i += NUM_PARAMS_SELECTORS) {
            if (key.equals(selectors[i])) {
                String[] opts = (String[]) selectors[i + 2];
                addSelector(container, key, label, opts);
                usedKeys.add(key);
                found = true;
            }
        }
        assert found;
    }

    private void addCheckboxGroupByKey(JComponent container, String key) {
        for (int i = 0; i < checkboxGroups.length; i += NUM_PARAMS_CHECKBOXES) {
            if (key.equals(checkboxGroups[i])) {
                String[] labels = (String[]) checkboxGroups[i + 3];
                addCheckboxGroup(container, key, labels);
                break;
            }
        }
    }

    private void addSelector(JComponent container, String key, String label, String[] opts) {
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


        LabelledDial comp = new LabelledDial(lbl, this, key, Style.COLOR_A(), minVal, maxVal, sub) {
            public boolean isSymmetric() {
                return symmetric;
            }
        };

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
            addSelector(hbox, key, label, opts);

        }

        for (int i = 0; i < checkboxGroups.length; i += NUM_PARAMS_CHECKBOXES) {
            String key = (String) checkboxGroups[i];
            if (key.indexOf(ctrlGrp) != 0) continue;
            if (usedKeys.contains(key)) continue;
            JComponent hbox2 = new HBox();
            String subCatTitle = key.substring(ctrlGrp.length());
            Category cat = new Category(this, subCatTitle, Color.WHITE);
            String[] labels = (String[]) checkboxGroups[i + 3];
            addCheckboxGroup(hbox2, key, labels);
            vbox.add(cat);
            vbox.add(hbox2);
        }

        return vbox;

    }

    private void addCheckboxGroup(JComponent container, String key, String[] lbls) {
        for (String lbl : lbls) {
            JComponent comp;
            if (lbl.contains("~")) {
                String[] strs = lbl.split("~");
                String prefix = longestCommonWordPrefix(strs[0], strs[1]);
                String[] opts = new String[]{strs[1], strs[0]}; // order is "switched"
                comp = new Chooser(prefix, this, key + BITMASK_SEP + lbl, opts, new int[]{0, 1});
            } else {
                comp = new CheckBox(lbl, this, key + BITMASK_SEP + lbl);
            }
            container.add(comp);
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

    private Object[] emitCheckboxGroup(String keyPrefix, int i) {
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


    public byte[] requestCurrentDump()
    {
        byte[] data = new byte[36];

        data[10] = (byte)0x03; // Request
        data[11] = (byte)0x7F;
        System.arraycopy(SysExHeader,0,data,0,SysExHeader.length);
        byte[] ext = generatePaddedByteArray("BIN ",4);
        System.arraycopy(ext, 0, data, 12, ext.length);
        byte[] fileName = generatePaddedByteArray("Upper Patch",16);
        System.arraycopy(fileName, 0, data, 16, fileName.length);
        data[32] = (byte)0x00;
        data[33] = (byte)0x03;
        data[34] = (byte)0x75;
        data[35] = (byte)0xF7;
        return data;
    }

    public static String byteArrayAsHex(byte[] byteArray) {
        StringBuilder hexString = new StringBuilder();

        for (byte b : byteArray) {
            // Convert each byte to hex and append to the string builder
            hexString.append(String.format("%02X ", b));
        }

        // Print the final string, trimming the trailing space
        return hexString.toString().trim();
    }

    public boolean parsePatch(){
        int length = 0;
        for(byte[] b : this.patchDump){
            length += b.length;
        }
        byte[] data = new byte[length];
        int dstPos = 0;
        for(byte[] b : this.patchDump){
            System.arraycopy(b, 0, data, dstPos, b.length);
        }
        byte[] data2 = data;//convertTo7Bit(data);
        for(int i = 0; i<SysExPosToKeyAndSize.length; i+=3){
            int pos = (int)SysExPosToKeyAndSize[i];
            String key = (String)SysExPosToKeyAndSize[i+1];
            int size = (int)SysExPosToKeyAndSize[i+2];
            // FIXME
            int val = data2[pos];// + data[pos+1] << 2;
            int i1 = data2[pos] + (data2[pos + 1] << 1);
//            System.out.println(key+": "+ i1 + "(" +data2[pos]+","+data2[pos+1]+")");
//            getModel().set(key, i1);
        }

        return true;
    }

    byte[] convertTo7Bit(byte[] data)
    {
        // How big?
        int size = (data.length) / 7 * 8;
        if (data.length % 7 > 0)
            size += (1 + data.length % 7);
        byte[] newd = new byte[size];

        int j = 0;
        for(int i = 0; i < data.length; i+=7)
        {
            for(int x = 0; x < 7; x++)
            {
                if (j + x + 1 < newd.length)
                {
                    newd[j + x + 1] = (byte)(data[i + x] & 127);
                    // Note that I have do to & 1 because data[i + x] is promoted to an int
                    // first, and then shifted, and that makes a BIG NUMBER which requires
                    // me to mask out the 1.  I hope this isn't the case for other stuff (which
                    // is typically 7-bit).
                    newd[j] = (byte)(newd[j] | (((data[i + x] >>> 7) & 1) << x));
                }
            }
            j += 8;
        }
        return newd;
    }


    @Override
    public int parse(byte[] data, boolean fromFile) {
        if (data.length== EOF.length &&
                BehringerUBXaRec.msgStartsWith(data,BehringerUBXaRec.EOF)
        ) return parsePatch() ? PARSE_SUCCEEDED : PARSE_FAILED;

        if (data[10] == (byte)0x01){
            // Clear on header
            patchDump.clear();
        } else {
            assert data[11] == patchDump.size();


            int packageLength = data[12]+1;
            int offset = 13;
            if (patchDump.size() == 0){
                int offset2 = offset+20;

                byte[] data3 = Arrays.copyOfRange(data,offset2,offset2+10);
                System.out.println(byteArrayAsHex(data3));

            }
            patchDump.add(Arrays.copyOfRange(data, offset, packageLength+offset));
        }
        return PARSE_INCOMPLETE;
    }

}