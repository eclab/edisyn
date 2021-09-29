package edisyn.synth.waldorfblofeld;

import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import edisyn.*;
import edisyn.gui.*;
import edisyn.util.*;


        
/*
  ABOUT BLOFELD WAVETABLE WAVE SYSEX

  From http://www.lysator.liu.se/~norling/blofeld.html

  Each wave in each wavetable is a separate sysex dump.  Here is its format:

  Index   Label   Value           Description
  -----------------------------------------------------------
  0       EXC     F0h             Start of SysEx
  1       IDW     3Eh             Waldorf Music ID
  2       IDE     13h             Blofeld ID
  3       DEV                     Device ID
  4       IDM     12h             WTBD (Wavetable Dump)
  5       WT      50h..76h        Wavetable number
  6       WN      00h..3Fh        Wave number
  7               00h             Format
  8-391   WDATA   00h..7Fh        Data: triplets with little-endian signed 21-byte numbers
  392-405 NAME    20h..7Fh        Wavetable name (ASCII)
  406             00h             Reserved
  407             00h             Reserved
  408     CHK     WDATA & 7Fh     Checksum (add bytes 7..407 together)
  409     EOX     F7h             End of SysEx
  -----------------------------------------------------------

  NOTES FROM SEAN: the triplets are sent in order HIGH BYTE, MED BYTE, LOW BYTE.

  So ultimately a wavetable will consist of 64 sysex dumps.

*/
        
public class WaldorfBlofeldWavetable
    {
    public static final int WAVEEDIT_WAVE_SIZE = 256;
    public static final int SERUM_WAVE_SIZE = 2048;
    public static final int BLOFELD_WAVE_SIZE = 128;
    public static final int BLOFELD_WAVETABLE_SIZE = 64;
    public static final int TWO_TO_THE_TWENTY = 1048576;
    public static final int WINDOW_SIZE = 65;
    File file = null;
        
        
    /** Select a file (WAV, sysex, or mid), and then load a wavetable from that file.
        If it's a WAV file, call uploadWAV() -- this assumes it's a WaveEdit file.
        If it's MID or SYX, call uploadSysex() -- this assumes it's one or more Blofeld wavetables in standard sysex format.
    */
                
    public void upload(final WaldorfBlofeld synth)
        {
        //// FIRST we have the user choose a file
        
        FileDialog fd = new FileDialog((Frame)(SwingUtilities.getRoot(synth)), "Load a wavetable", FileDialog.LOAD);
        fd.setFilenameFilter(new FilenameFilter()
            {
            public boolean accept(File dir, String name)
                {
                return StringUtility.ensureFileEndsWith(name, ".wav").equals(name) || StringUtility.ensureFileEndsWith(name, ".WAV").equals(name) ||
                    StringUtility.ensureFileEndsWith(name, ".syx").equals(name) || StringUtility.ensureFileEndsWith(name, ".SYX").equals(name) ||
                    StringUtility.ensureFileEndsWith(name, ".sysex").equals(name) || StringUtility.ensureFileEndsWith(name, ".mid").equals(name) ||
                    StringUtility.ensureFileEndsWith(name, ".MID").equals(name)  ;
                }
            });

        if (file != null)
            {
            fd.setFile(file.getName());
            fd.setDirectory(file.getParentFile().getPath());
            }
        else
            {
            String path = synth.getLastDirectory();
            if (path != null)
                fd.setDirectory(path);
            }
                
        synth.disableMenuBar();
        fd.setVisible(true);
        synth.enableMenuBar();

        FileInputStream is = null;
        if (fd.getFile() != null)
            try
                {
                file = new File(fd.getDirectory(), fd.getFile());
                
                if (file.getName().endsWith(".wav") || file.getName().endsWith(".WAV"))
                    {
                    uploadWav(synth, file);
                    }
                else
                    {
                    uploadSysex(synth, file);
                    }
                }
            catch (IOException ex)
                {
                synth.showSimpleError("File Error", "An error occurred on reading the file.");
                }
            catch (WavFileException ex)
                {
                synth.showSimpleError("Improper Format", "WAV files must be mono, 16-bit signed integer PCM.\nThis file is not. Perhaps it's a Serum file, which is normally\n32-bit floating point?  You can convert files to mono,\n16-bit signed integer PCM using Audacity.");
                }
        }


    /** Extract the sysex and guess whether it's one wavetable or more.  This is fragile: we're
        just grabbing the valid sysex chunks and grouping them into arrays of 64 and assuming each
        array is one wavetable.  We're not presently checking to see if the arrays are right.
        If there's only one group, then we call uploadOneSysex() to upload it.
        Else we call uploadMultiSysex() to pick a group.
    */

    void uploadSysex(WaldorfBlofeld synth, File file) throws IOException
        {
        byte[][] sysex = null;
        if (file.getName().endsWith(".mid") || file.getName().endsWith(".MID"))
            {
            sysex = synth.extractSysexFromMidFile(file);
            }
        else
            {
            long len = file.length();
            if (len > Integer.MAX_VALUE)  // uh oh
                {
                synth.showSimpleError("This file is too large", "Too large");
                return;
                }
                                                                
            byte[] data = new byte[(int)len];
            DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            input.readFully(data);
            sysex = synth.cutUpSysex(data);
            }
                        
        // At this point we have the sysex. Let's group them, throwing out specious sysex files.
        ArrayList<byte[][]> sysexA = new ArrayList<byte[][]>();
        byte[][] current = new byte[64][];
        int count = 0;
                
        for(int i = 0; i < sysex.length; i++)
            {
            if (sysex[i][0] == (byte)0xF0 &&
                sysex[i][1] == (byte)0x3E &&
                sysex[i][2] == (byte)0x13 &&
                sysex[i][4] == (byte)0x12 &&
                sysex[i].length == 410)
                {
                current[count] = sysex[i];
                count++;
                if (count == 64)
                    {
                    sysexA.add(current);
                    current = new byte[64][410];
                    count = 0;
                    }
                }
            }
                
        byte[][][] syx = sysexA.toArray(new byte[0][0][0]);
        if (syx.length == 0) // uh oh
            {
            synth.showSimpleError("No data", "This file doesn't seem to contain any wavetable data.");
            }
        else if (syx.length == 1)
            {
            uploadOneSysex(synth, syx[0]);
            }
        else    // multi
            {
            uploadMultiSysex(synth, syx);
            }
        }
                
                
    /** Ask the user to select from among N wavetables by their names.  Then based
        on the selection, we call uploadOneSysex() to upload it.
    */

    void uploadMultiSysex(WaldorfBlofeld synth, byte[][][] syx)
        {
        String[] names = new String[syx.length];
        try
            {
            for(int i = 0; i < syx.length; i++)
                {
                names[i] = new String(syx[i][0], 392, 14, "US-ASCII");
                }
            }
        catch (UnsupportedEncodingException ex) { }     // won't happen

        Color color = new JPanel().getBackground();
        HBox hbox = new HBox();
        hbox.setBackground(color);
        VBox vbox = new VBox();
        vbox.setBackground(color);
        vbox.add(new JLabel("   "));
        vbox.add(new JLabel("Select a wavetable."));

        vbox.add(new JLabel("   "));
        hbox.addLast(vbox);
        vbox = new VBox();
        vbox.setBackground(color);
        vbox.add(hbox);
        JComboBox box = new JComboBox(names);
        box.setMaximumRowCount(25);
        vbox.add(box);
                          
        int result = 0;
        synth.disableMenuBar();
        result = JOptionPane.showOptionDialog(synth, vbox, "Wavetable", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, 
            null, new String[] {  "Select", "Cancel" }, "Select");
        synth.enableMenuBar();
                        
        if (result == 1 || result < 0)  // cancel.  ESC and Close Box both return < 0
            {
            return;
            }
        else 
            {
            uploadOneSysex(synth, syx[box.getSelectedIndex()]);
            }
        }
                
    /** Ask the user to specify the device ID, wavetable number, and wavetable name.
        Then we modify the sysex files appropriately and upload them.
    */

    void uploadOneSysex(WaldorfBlofeld synth, byte[][] syx)
        {
        int _id = synth.getID();
        int _number = syx[0][5];
        String _name = "";
                
        try
            {
            _name = new String(syx[0], 392, 14, "US-ASCII");
            }
        catch (UnsupportedEncodingException ex) { }     // won't happen
                
        while(true)
            {
            JTextField id = new SelectedTextField("" + _id);
            JTextField number = new JTextField("" + _number);
            JTextField name = new JTextField(_name);

            boolean result = Synth.showMultiOption(synth, new String[] { "Device ID", "Wavetable Number", "Wavetable Name" }, 
                new JComponent[] { id, number, name }, "Write to wavetable...", "Provide the device ID, wavetable number (80 ... 118), and name.");

            if (!result) return;
                        
            _name = name.getText();
            if (_name.length() > 14)
                {
                synth.showSimpleError("The name must be no longer than 14 characters", "Wavetable Name");
                if (_name.length() >= 14)
                    _name = _name.substring(0, 14);
                continue;
                }
                                
            try
                {
                _id = Integer.parseInt(id.getText());
                if (_id < 0 || _id > 255) throw new NumberFormatException();
                }
            catch (NumberFormatException ex)
                {
                synth.showSimpleError("The Device ID must be an integer between 0 and 255", "Device ID");
                _id = synth.getID();
                continue;
                }

            try
                {
                _number = Integer.parseInt(number.getText());
                if (_number < 80 || _number > 118) throw new NumberFormatException();
                }
            catch (NumberFormatException ex)
                {
                synth.showSimpleError("The Wavetable Number must be between 80 and 118", "Wavetable Number");
                _number = 80;
                continue;
                }

            // success!
            break;
            }
                
        for(int i = 0; i < syx.length; i++)
            {
            syx[i][5] = (byte)_number;
            syx[i][3] = (byte)_id;

            /// LOAD NAME
            for(int d = 0; d < 14; d++)
                {
                if (d >= _name.length())
                    syx[i][392 + d] = (byte)' ';
                else
                    syx[i][392 + d] = (byte)(_name.charAt(d) & 127);
                }
                                
            // COMPUTE CHECKSUM
            byte checksum = syx[i][7];
            for(int c = 7; c <= 407; c++)           // Note <=
                checksum += syx[i][c];
            syx[i][408] = (byte)(checksum & 127);   // sum of bytes 7...407 inclusive
                        
            // send it along

            synth.tryToSendSysex(syx[i]);
            }
        }


    /** Ask the user to specify the device ID, wavetable number, and wavetable name.
        Then we call writeData() to upload them.
    */

    void uploadWav(WaldorfBlofeld synth, File file) throws IOException, WavFileException
        {
        int _id = synth.getID();
        int _number = 80;
        boolean _truncate = false;
        String _name = file.getName();
        try { _name = _name.substring(0, _name.lastIndexOf('.')); }
        catch (Exception e) { }  // happens if name has no suffix
        if (_name.length() >= 14)
            _name = _name.substring(0, 14);
        
        int ws;
                
        while(true)
            {
            JTextField id = new SelectedTextField("" + _id);
            JTextField number = new JTextField("" + _number);
            JTextField name = new JTextField(_name);
            JComboBox waveSize = new JComboBox(new String[] { "256 (WaveEdit)", "2048 (Serum)" });
            //JCheckBox truncate = new JCheckBox("");

            boolean result = Synth.showMultiOption(synth, new String[] { "Device ID", "Wavetable Number", "Wavetable Name", "Wave Size" },      ///  "Truncate Waves in Half" }, 
                new JComponent[] { id, number, name, waveSize }, "Write to wavetable...", "Provide wavetable information.");

            if (!result) return;
                        
            _name = name.getText();
            if (_name.length() > 14)
                {
                synth.showSimpleError("The name must be no longer than 14 characters", "Wavetable Name");
                if (_name.length() >= 14)
                    _name = _name.substring(0, 14);
                continue;
                }
                                
            try
                {
                _id = Integer.parseInt(id.getText());
                if (_id < 0 || _id > 255) throw new NumberFormatException();
                }
            catch (NumberFormatException ex)
                {
                synth.showSimpleError("The Device ID must be an integer between 0 and 255", "Device ID");
                _id = synth.getID();
                continue;
                }

            try
                {
                _number = Integer.parseInt(number.getText());
                if (_number < 80 || _number > 118) throw new NumberFormatException();
                }
            catch (NumberFormatException ex)
                {
                synth.showSimpleError("The Wavetable Number must be between 80 and 118", "Wavetable Number");
                _number = 80;
                continue;
                }

            ws = (waveSize.getSelectedIndex() == 0 ? WAVEEDIT_WAVE_SIZE : SERUM_WAVE_SIZE);
            //_truncate = truncate.isSelected();
            // success!
            break;
            }
                
        writeData(synth, file, _id, _number, WINDOW_SIZE, _name, ws, BLOFELD_WAVETABLE_SIZE);   // _truncate);
        }
                

    /** Load the data from the file and convert it into a Blofeld wavetable.  This is done by breaking
        the WAV file into chunks 256 or 2048 samples long (the length of a WaveEdit or Serum wavetable wave), then
        resampling them using Windowed Sinc Interpolation to chunks 128 samples long (the
        length of a Blofeld wavetable wave), then building the wavetable sysex from these resulting waves
        and uploading them.
    */

    void writeData(WaldorfBlofeld synth, File file, int deviceID, int wavetableNumber, int windowSize, String name, int waveSize, int wavetableSize) throws IOException, WavFileException
        {
        double[][] waves = new double[wavetableSize][waveSize];
        WavFile wavFile = WavFile.openWavFile(file);
        name = (name + "              ").substring(0, 14);              // ensure exactly 14 long
                
        // read data
        for(int i = 0; i < waves.length; i++)
            {
            // Read frames into buffer
            int framesRead = wavFile.readFrames(waves[i], waves[i].length);
            if (framesRead < waves[i].length) 
                {
                synth.showSimpleError("File Warning", "File contains data for only " + i + " waves.\nRemaining waves (up to " + wavetableSize + ") will be set to silence.");
                // clear out what we had just read
                for(int j = 0; j < waves[i].length; j++)
                    waves[i][j] = 0;
                break;
                }
            }
            
        // is it longer than expected?  Read another frame.
        int framesRead = wavFile.readFrames(new double[waveSize], waveSize);
        if (framesRead == waveSize) // uh oh
            {
            synth.showSimpleError("File Warning", "File contains more data than needed for " + wavetableSize + " wavetables.\nThe rest will be truncated. Perhaps you accidentially\nchose 256 samples per wave when you meant 2048?");
            }
                
        wavFile.close();
        
        // parse data
        for(int i = 0; i < wavetableSize; i++)
            {
            byte[] data = new byte[410];
                        
            data[0] = (byte)0xF0;
            data[1] = (byte)0x3E;
            data[2] = (byte)0x13;
            data[3] = (byte)deviceID;
            data[4] = (byte)0x12;
            data[5] = (byte)(wavetableNumber);
            data[6] = (byte)i;
            data[7] = (byte)0;
                
            /*       
            // do we cut it in half?
            if (truncate)
            {
            double[] foo = new double[waves[i].length / 2];
            System.arraycopy(waves[i], 0, foo, 0, foo.length);
            waves[i] = foo;
            }
            */
                        
            /// Resample to the Blofeld's sampling rate
            double[] newvals = WindowedSinc.interpolate(
                waves[i],
                waves[i].length,
                BLOFELD_WAVE_SIZE,
                windowSize,
                true); 

/*
/// Resample to the Blofeld's sampling rate
double[] newvals = WindowedSinc.interpolate(
waves[i],
BLOFELD_WAVE_SIZE,
waves[i].length,
windowSize,
true); 
*/
                        
            for(int d = 0; d < BLOFELD_WAVE_SIZE; d++)
                {
                double val = newvals[d];
                int v = (int)(val * TWO_TO_THE_TWENTY);
                System.err.println(v);

                // These values must be little-endian.
                // We assume our code is being run on a little-endian processor.
                data[8 + d * 3 + 2] = (byte)(v & 127);  // 7 bits
                data[8 + d * 3 + 1] = (byte)((v >>> 7) & 127);
                data[8 + d * 3 + 0] = (byte)((v >>> 14) & 127);
                }
            System.err.println("=============n\n");
                                
            /// LOAD NAME
            for(int d = 0; d < 14; d++)
                {
                if (d >= name.length())
                    data[392 + d] = (byte)' ';
                else
                    data[392 + d] = (byte)(name.charAt(d) & 127);
                }
                        
            data[406] = (byte)0;
            data[407] = (byte)0;
                        
            // COMPUTE CHECKSUM
            byte checksum = data[7];
            for(int c = 7; c <= 407; c++)           // Note <=
                checksum += data[c];
            data[408] = (byte)(checksum & 127);     // sum of bytes 7...407 inclusive
                        
            data[409] = (byte)0xF7;

            synth.tryToSendSysex(data);
            }
        }

    }
        
        
        
        
        
        
