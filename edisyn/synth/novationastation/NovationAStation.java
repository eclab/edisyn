/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.synth.novationastation;

import edisyn.Librarian;
import edisyn.Midi;
import edisyn.Model;
import edisyn.Synth;
import edisyn.gui.*;
import edisyn.util.StringUtility;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.swing.*;
import java.awt.*;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;

import static edisyn.synth.novationastation.Mappings.*;

public class NovationAStation extends Synth {
    private static final String[] BANKS = IntStream.rangeClosed(1, 4).boxed().map(String::valueOf).toList().toArray(new String[0]);
    private static final String[] PATCH_NUMBERS = IntStream.rangeClosed(0, 99).boxed().map(String::valueOf).toList().toArray(new String[0]);
    private static final String[] ARP_CONDITION = { "off", "on: no latch, no keysync", "on: no latch, keysync", "on: latch, no keysync", "on: latch, keysync"};
    private static final String[] SYNC_RATES = { "non-sync", "32T", "32", "16T", "16", "8T", "16.", "8", "4T", "8.", "4", "2T", "4.", "2", "1T", "2.",
            "1", "2T", "1.", "2", "4T", "3", "5T", "4", "4.", "7T", "5", "8T", "6", "7", "7.", "8", "9", "10.", "12"};
    private static final String[] ARP_RATES = SYNC_RATES;
    private static final String[] DELAY_RATIOS = { "1-1", "4-3", "3-4", "3-2", "2-3", "2-1", "1-2", "3-1", "1-3", "4-1", "1-4", "1-0", "0-1"};
    private static final String[] ARP_PATTERNS = { "up", "down", "updown1", "updown2", "order", "rand"};

    public NovationAStation() {
        // build UI
        new UIBuilder(this).build();

        loadDefaults();                 // this tells Edisyn to load the ".init" sysex file you created.  If you haven't set that up, it won't bother
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
        if (-1 != currentBank) {
            bank.setSelectedIndex(currentBank);
        }

        int currentPatch = model.get("number");
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
            if (model.exists("bank") && model.exists("number")) {
                String bankName = BANKS[model.get("bank")];
                int program = model.get("number");
                return String.format("%s%02d", bankName, program);
            }
            return "...";
        }

    @Override
    public Model getFirstPatchLocation()
        {
            Model newModel = buildModel();
            newModel.set("bank", 0);
            newModel.set("number", 0);
            return newModel;
        }

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
    public byte[] requestDump(Model tempModel) {
        // request specific program dump
        // TODO - constant here
        byte messageType = 0x41;
        // tempModel supposed to have valid bank+program
        byte bank = (byte) (1 + tempModel.get("bank")); // 1..4 (while in model: 0..3)
        byte program = (byte) tempModel.get("number");
        return new byte[] { (byte)0xF0, 0x00, 0x20, 0x29, 0x01, 0x40, 0x7F, messageType, 0x00, 0x00, 0x00, bank, program, (byte)0xF7 };
    }

    @Override
    public byte[] requestCurrentDump() {
        // request current sound dump
        // TODO - constant here
        byte messageType = 0x40;
        return new byte[] { (byte)0xF0, 0x00, 0x20, 0x29, 0x01, 0x40, 0x7F, messageType, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0xF7 };
    }


    ////// YOU MAY WANT TO IMPLEMENT SOME OF THE FOLLOWING
    // TODO - TBI
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

    private String toString(Midi.CCData data) {
        return "CCData {number:" +
                data.number + ", value:" + data.value + ", type:" + data.type +
                "}";
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
