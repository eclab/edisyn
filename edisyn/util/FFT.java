package edisyn.util;

/**
 *       This class does a basic FFT.  Its author has declared it to be in the Public Domain
 *       (see https://stackoverflow.com/questions/3287518/reliable-and-fast-fft-in-java).
 *       
 * @author Orlando Selenu
 */

public class FFT 
    {
    public static double[] applyHanningWindow(double[] wave)
        {
        double[] d = new double[wave.length];
        System.arraycopy(wave, 0, d, 0, wave.length);

        for(int i = 0; i < d.length; i++)
            {
            double s = Math.sin((Math.PI * i) / (d.length - 1));
            d[i] = d[i] * s * s;
            }
        return d;
        }

    /** Returns the harmonics amplitudes (only) from the given wave. */
    public static double[] getHarmonics(double[] wave) 
        {
        double[] d = new double[wave.length];           // all zeros
        double[] fft0 = fft(wave, d, true);
        double[] fft = new double[fft0.length / 2];
        for(int i = 0; i < fft.length; i++)
            {
            fft[i] = Math.sqrt(fft0[i * 2] * fft0[i * 2] + fft0[i * 2 + 1] * fft0[i * 2 + 1]);
            }
        return fft;
        }

    /**
     * The Fast Fourier Transform (generic version, with NO optimizations).
     *
     * @param inputReal
     *            an array of length n, the real part
     * @param inputImag
     *            an array of length n, the imaginary part
     * @param forward
     *            TRUE = forward transform, FALSE = inverse transform
     * @return a new array of length 2n
     */
    public static double[] fft(double[] inputReal, double[] inputImag, boolean forward) 
        {
        // - n is the dimension of the problem
        // - nu is its logarithm in base e
        int n = inputReal.length;

        // If n is a power of 2, then ld is an integer (_without_ decimals)
        double ld = Math.log(n) / Math.log(2.0);

        // Here I check if n is a power of 2. If exist decimals in ld, I quit
        // from the function returning null.
        if (((int) ld) - ld != 0) 
            {
            System.err.println("The number of elements is not a power of 2.");
            return null;
            }

        // Declaration and initialization of the variables
        // ld should be an integer, actually, so I don't lose any information in
        // the cast
        int nu = (int) ld;
        int n2 = n / 2;
        int nu1 = nu - 1;
        double[] xReal = new double[n];
        double[] xImag = new double[n];
        double tReal, tImag, p, arg, c, s;

        // Here I check if I'm going to do the forward transform or the inverse
        // transform.
        double constant;
        if (forward)
            constant = -2 * Math.PI;
        else
            constant = 2 * Math.PI;

        // I don't want to overwrite the input arrays, so here I copy them. This
        // choice adds \Theta(2n) to the complexity.
        for (int i = 0; i < n; i++) 
            {
            xReal[i] = inputReal[i];
            xImag[i] = inputImag[i];
            }

        // First phase - calculation
        int k = 0;
        for (int l = 1; l <= nu; l++)
            {
            while (k < n) 
                {
                for (int i = 1; i <= n2; i++) 
                    {
                    p = bitreverseReference(k >> nu1, nu);
                    // forward FFT or inverse FFT
                    arg = constant * p / n;
                    c = Math.cos(arg);
                    s = Math.sin(arg);
                    tReal = xReal[k + n2] * c + xImag[k + n2] * s;
                    tImag = xImag[k + n2] * c - xReal[k + n2] * s;
                    xReal[k + n2] = xReal[k] - tReal;
                    xImag[k + n2] = xImag[k] - tImag;
                    xReal[k] += tReal;
                    xImag[k] += tImag;
                    k++;
                    }
                k += n2;
                }
            k = 0;
            nu1--;
            n2 /= 2;
            }

        // Second phase - recombination
        k = 0;
        int r;
        while (k < n) 
            {
            r = bitreverseReference(k, nu);
            if (r > k) 
                {
                tReal = xReal[k];
                tImag = xImag[k];
                xReal[k] = xReal[r];
                xImag[k] = xImag[r];
                xReal[r] = tReal;
                xImag[r] = tImag;
                }
            k++;
            }

        // Here I have to mix xReal and xImag to have an array (yes, it should
        // be possible to do this stuff in the earlier parts of the code, but
        // it's here to readibility).
        double[] newArray = new double[xReal.length * 2];
        double radice = 1 / Math.sqrt(n);
        for (int i = 0; i < newArray.length; i += 2) 
            {
            int i2 = i / 2;
            // I used Stephen Wolfram's Mathematica as a reference so I'm going
            // to normalize the output while I'm copying the elements.
            newArray[i] = xReal[i2] * radice;
            newArray[i + 1] = xImag[i2] * radice;
            }
        return newArray;
        }

    /**
     * The reference bitreverse function.
     */
    static int bitreverseReference(int j1, int nu) 
        {
        int j2;
        int k = 0;
        for (int i = 1; i <= nu; i++) 
            {
            j2 = j1 / 2;
            k = 2 * k + j1 - 2 * j2;
            j1 = j2;
            }
        return k;
        }
    }
