/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.novationastation;

import edisyn.Midi;
import edisyn.Model;
import edisyn.Synth;
import edisyn.gui.SelectedTextField;
import edisyn.util.StringUtility;

import javax.sound.midi.ShortMessage;
import javax.swing.*;
import java.util.Optional;
import java.util.OptionalInt;

public class NovationAStation extends Synth {
    private static final String[] BANKS = Restrictions.BANKS.getValues();
    private static final String[] PATCH_NUMBERS = Restrictions.PATCH_NUMBERS.getValues();

    public NovationAStation()
    {
        // build UI
        new UIBuilder(this).build();

        loadDefaults();
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
        if (currentBank >= 0 && currentBank <= 3) {
            bank.setSelectedIndex(currentBank);
        }

        int currentPatch = model.get("number");
        currentPatch = Math.min(currentPatch, 99);
        currentPatch = Math.max(currentPatch, 0);

        JTextField number = new SelectedTextField(String.valueOf(currentPatch), 3);

        while (true) {
            boolean result = showMultiOption(this, new String[]{"Bank", "Patch Number"},
                    new JComponent[]{bank, number}, title, "Enter the Bank and Patch number.");

            if (!result)
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
            model.set("swversion", swVersion);
            byte swIncrement = data[10];
            model.set("swversionincrement", swIncrement);
            model.set("swversionstring", parseVersion(swVersion, swIncrement));

            byte programBank = data[11];    // 1..4
            byte programNumber = data[12];  // 0..99
            if (programBank >= 1 && programBank <= 4) {
                programBank--;  // zero-indexed in model
                model.set("bank", programBank); // 0..3 in model
                model.set("number", programNumber);
            } else {
                model.set("bank", -1);
                model.set("number", -1);
            }

            // TODO - to be extended, for now supporting:
            // 0x0 : current sound dump
            // 0x1 : program sound dump
            byte messageType = data[7];
            if (messageType == 0x0 || messageType == 0x01) {
                for (int index = 13; index < 13 + 128; ++index) {
                    Optional<Convertor> convertor = Convertors.getByIndex(index-13);
                    if (convertor.isPresent()) {
                        convertor.get().toModel(model, data[index]);
                    }
                }
                return PARSE_SUCCEEDED;
            }
            return PARSE_IGNORE;
        }

    public static String getSynthName()
    {
        return "Novation A Station";
    }

    @Override
    public String getDefaultResourceFileName()
        {
            return "NovationAStation.init";
        }

    @Override
    public String getHTMLResourceFileName()
    {
        return "NovationAStation.html";
    }

    @Override
    public String getPatchLocationName(Model model) {
        int bank = model.get("bank");
        int number = model.get("number");
        if (bank >= 0 && bank <= 3 && number >= 0 && number <= 99) {
            String bankName = BANKS[bank];
            return String.format("%s%02d", bankName, number);
        }
        return null;
    }

    @Override
    public Model getNextPatchLocation(Model model) {
        int bank = model.get("bank");
        bank = Math.max(bank, 0);
        bank = Math.min(bank, 3);
        int program = model.get("number");
        int programindex = bank * 100 + program;
        ++programindex;
        bank = (programindex / 100) % 4;
        program = programindex % 100;

        Model newModel = buildModel();
        newModel.set("bank", bank);
        newModel.set("number", program);
        return newModel;
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
            if (tempModel == null)
                tempModel = getModel();

            int programBank = toWorkingMemory ? 0 : tempModel.get("bank") + 1;
            int programNumber = toWorkingMemory ? 0 : tempModel.get("number");
            // TODO - define some constants here !
            int messageType = toWorkingMemory ? 0 : 1;
            byte controlByte = toWorkingMemory ? (byte)0 : (byte)1;

            byte[] data = new byte[142];
            data[0] = (byte)0xF0;
            data[1] = (byte)0x00;
            data[2] = (byte)0x20;
            data[3] = (byte)0x29;
            data[4] = (byte)0x01;
            data[5] = (byte)0x40;
            data[6] = (byte)0x7F;
            data[7] = (byte)messageType; // message type: current sound dump
            data[8] = controlByte; // control byte
            data[9] = (byte)tempModel.get("swversion"); // SW version
            data[10] = (byte)tempModel.get("swversionincrement"); // SW version increment
            data[11] = (byte)programBank; // program bank
            data[12]= (byte)programNumber; // program number
            data[141] = (byte)0xF7;
            int startPosition = 13;
            for (int i = 0; i < 128; ++i) {
                Optional<Convertor> convertor = Convertors.getByIndex(i);
                if (convertor.isPresent()) {
                    int value = convertor.get().toSynth(tempModel);
                    data[startPosition + i] = (byte)value;
                }
            }
            return data;
        }

    @Override
    public Object[] emitAll(String key) {
            Optional<Convertor> convertor = Convertors.getByKey(key);
            if (convertor.isPresent()) {
                Convertor mapping = convertor.get();
                Integer CC = mapping.getCC();
                Integer NRPN = mapping.getNRPN();
                if (CC != null) {
                    int value = mapping.toSynth(model);
                    return buildCC(getChannelOut(), mapping.getCC(), value);
                } else if (NRPN != null){
                    int value = mapping.toSynth(model) << 7;
                    return buildNRPN(getChannelOut(), mapping.getNRPN(), value);
                }
            }
            System.err.println("Ignoring model key '" + key + "', since no convertor found");
            return super.emitAll(key);
        }

    @Override
    public byte[] requestCurrentDump() {
        // TODO - constant here
        byte messageType = 0x40; // request current sound dump
        return new byte[] { (byte)0xF0, 0x00, 0x20, 0x29, 0x01, 0x40, 0x7F, messageType, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0xF7 };
    }

    @Override
    public byte[] requestDump(Model tempModel) {
        // TODO - constant here
        byte messageType = 0x41; // request specific program dump
        // tempModel supposed to have valid bank+program
        byte bank = (byte) (1 + tempModel.get("bank")); // 1..4 (while in model: 0..3)
        byte program = (byte) tempModel.get("number");
        return new byte[] { (byte)0xF0, 0x00, 0x20, 0x29, 0x01, 0x40, 0x7F, messageType, 0x00, 0x00, 0x00, bank, program, (byte)0xF7 };
    }

    @Override
    public void parseParameter(byte[] data)
        {
        // If your synth sent you a sysex message which was not recognized via
        // the recognize() method, it gets sent here.  Typically this is 
        // a sysex message for a single parameter update.  If your synth sends 
        // such things, implement this.  See also handleCCOrNRPNData() below.
            System.err.println("Unrecognized message received: " + StringUtility.toHex(data));
        }

    @Override
    public void handleSynthCCOrNRPN(Midi.CCData data) {
        Optional<Convertor> convertor = Optional.empty();
        OptionalInt value = OptionalInt.empty();
        int type = data.type;
        if (type == Midi.CCDATA_TYPE_RAW_CC) {
            convertor = Convertors.getByCC(data.number);
            value = OptionalInt.of(data.value);
        } else if (type == Midi.CCDATA_TYPE_NRPN) {
            convertor = Convertors.getByNRPN(data.number);
            value = OptionalInt.of((data.value >> 7));
        }
        if (convertor.isPresent() && value.isPresent()) {
            convertor.get().toModel(model, value.getAsInt());
        } else {
            System.out.println("Ignoring CC/NRPN msg:" + toString(data));
        }
    }

    public JFrame sprout()
        {
        // This is a great big method in Synth.java, and handles building the JFrame and
        // constructing all of the menus.  It's called when the editor is having its GUI
        // constructed.   You may need to do some things here, such as turning off certain
        // menu options that your synthesizer cannot do.  Be sure to call super.sprout();
        // first.
        // TODO - what about doing a global request here ? as in: requesting device (non program) data
        return super.sprout();
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
   public boolean librarianTested()
        {
        // Override this method to return true to indicate that the librarian for this
        // editor has been tested reasonably well and no longer requires a warning to the
        // musician when he attempts to use it.  By default this method returns false.
        return false; 
        }

    private String parseVersion(byte swVersion, byte swIncrement)
    {
        int major = (swVersion >> 3);
        int minor = swVersion & 0x7;
        return major + "." + minor + "." + swIncrement;
    }

    private String toString(Midi.CCData data)
    {
        return "CCData {number:" +
                data.number + ", value:" + data.value + ", type:" + data.type +
                "}";
    }
}
