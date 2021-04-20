package edisyn.util;

/**
 * Wav file Exception class
 * A.Greensted
 * http://www.labbookpages.co.uk
 * 
 * File format is based on the information from
 * http://www.sonicspot.com/guide/wavefiles.html
 * http://www.blitter.com/~russtopia/MIDI/~jglatt/tech/wave.htm
 *
 * Version 1.0
 */

public class WavFileException extends Exception
    {
    public WavFileException()
        {
        super();
        }

    public WavFileException(String message)
        {
        super(message);
        }

    public WavFileException(String message, Throwable cause)
        {
        super(message, cause);
        }

    public WavFileException(Throwable cause) 
        {
        super(cause);
        }
    }
