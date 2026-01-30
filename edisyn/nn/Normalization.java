/***
    Copyright 2025 by Sean Luke
    Licensed under the Apache License version 2.0
*/


package edisyn.nn;

/**
   A layer which renormalizes based on the original samples.
   Normalization takes four variables, per input: mean, variance, beta, and gamma.
   For each input it computes an output as:
   output = ((input - mean) / variance) * gamma + beta
*/

public class Normalization implements Layer
    {
    private double[] mean;
    private double[] variance;
    private double[] beta;
    private double[] gamma;

    public Normalization(double[] mean, double[] variance, double[] gamma, double[] beta)
        {
        this.mean = new double[mean.length];
        System.arraycopy(mean, 0, this.mean, 0, mean.length);
        this.variance = new double[variance.length];
        System.arraycopy(variance, 0, this.variance, 0, variance.length);
        this.beta = new double[beta.length];
        System.arraycopy(beta, 0, this.beta, 0, beta.length);
        this.gamma = new double[gamma.length];
        System.arraycopy(gamma, 0, this.gamma, 0, gamma.length);
        }

    /**
       For each output o , we normalize input v as ((v - mean) / variance) * gamma + beta
    */
    public double[] feed(double[] vec)
        {
        double[] out = new double[vec.length];
        for(int i = 0; i < vec.length; i++)
            {
            double a = (vec[i] - mean[i]) / variance[i];
            out[i] = a * gamma[i] + beta[i];
            }
        return out;
        }
        
    /**
       Normalization SIZE mean...., variance..., gamma..., beta...
       </tt>
    */
    public static Layer readFromString(String str)
        {
        String[] strs = str.split(" ");
        int size = Integer.parseInt(strs[1]);
        double[] mean = new double[size];
        double[] variance = new double[size];
        double[] beta = new double[size];
        double[] gamma = new double[size];
        for(int i = 0; i < size; i++)
            {
            mean[i] = Double.parseDouble(strs[i + 2]);
            }
        for(int i = 0; i < size; i++)
            {
            variance[i] = Double.parseDouble(strs[i + 2 + size]);
            }
        for(int i = 0; i < size; i++)
            {
            gamma[i] = Double.parseDouble(strs[i + 2 + size * 2]);
            }
        for(int i = 0; i < size; i++)
            {
            beta[i] = Double.parseDouble(strs[i + 2 + size * 3]);
            }
        return new Normalization(mean, variance, gamma, beta);
        }

    }
