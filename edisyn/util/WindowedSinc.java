/***
    Copyright 2019 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.util;


/**
   This class contains a single method used to resample one array of samples to another array of samples at a different sampling rate,
   using Windowed Sinc Interpolation, a common resampling technique.  See Section 10.7 of "Computational Music Synthesis" (Sean Luke),
   https://cs.gmu.edu/~sean/book/synthesis/
        
   @author Sean Luke
**/

public class WindowedSinc
{
    static double sinc(double x)
    {
        if (x == 0) return 1;
        return Math.sin(Math.PI * x) / (Math.PI * x);
    }
                
    // Blackman window
    static double blackmanWindow(double n, double N)
    {
        return 0.42 - 0.5 * Math.cos((2 * Math.PI * n)/(N - 1)) + 0.08 * Math.cos((4 * Math.PI * n) / (N - 1));
    }

    /** Given an array of (sound) SAMPLES sampled at a CURRENT SAMPLING RATE, returns a new array of samples resampled from the original array
        at a NEW SAMPLING RATE.  The quality of the resampling -- and also the degree to which aliasing is minimized -- is determined by 
        the WINDOW SIZE, which should be an odd number, normally significantly smaller than the number of SAMPLES.  A window size of 17 would
        be a good pick if you had to select something.  Larger window sizes will increase the interpolation time linearly.  The samples in 
        question might be a LOOP of samples or just a single one-shot array.
    */
    public static double[] interpolate(double[] samples, double currentSamplingRate, double newSamplingRate, double windowSize, boolean loop)
    {
        double fa = currentSamplingRate;
        double fap = newSamplingRate;
        double N = windowSize;
        double minfa = Math.min(fa, fap);
        double[] newsamp = new double[(int)((samples.length)/ fa * fap)];        // our new samples

        int count = 0;                                                          // count is the new sample index number in its array
        for(int j = 0; j < newsamp.length; j++)
            {
                double samp = 0;
                        
                int kLo = (int)Math.ceil(fa * j/fap - (N-1)/2);
                int kHi = (int)Math.floor(fa * j/fap + (N-1)/2);

                for(int k = kLo; k <= kHi; k++)                 // go through all the old samples
                    {
                        int _k = k;                                                     // this will be the revised old sample index
                        double ak = 0;                                          // this will be the value at _k
                                
                        if (loop)                                                       // if we're looping, we need to wrap around _k toroidally
                            {
                                if (_k < 0)
                                    {
                                        _k += samples.length;
                                        if (_k < 0)                                     // this would be rare, can only happen if the window is bigger than the samples!
                                            {
                                                _k = (_k % samples.length + samples.length) % samples.length;
                                            }
                                    }
                                else if (_k >= samples.length)
                                    {
                                        _k -= samples.length;
                                        if (_k >= samples.length)       // this would be rare, can only happen if the window is bigger than the samples!
                                            {
                                                _k = _k % samples.length;
                                            }
                                    }
                                ak = samples[_k];
                            }
                        else                                                            // if we're not looping, we just need to zero-pad
                            {
                                if (_k >= 0 && _k < samples.length)
                                    {
                                        ak = samples[_k];                       // otherwise ak stays as it is, that is, 0
                                    }
                            }
                                
                        // now roll in the interpolated sample value
                        samp += sinc(minfa * (_k / fa - j/fap)) * blackmanWindow(_k - fa * j/fap + (N-1)/2, N) * ak;
                    }
                        
                // need to scale
                newsamp[count++] = samp * Math.min(1.0, fap/fa);
            }
        return newsamp;
    }
}
