/***
    Copyright 2021 by Bryan Hoyle
    Licensed under the Apache License version 2.0
*/

package edisyn.nn;

/** 
    Scaled Exponential Linear Unit

    See Paper: https://arxiv.org/abs/1706.02515v5

    Tl;dr: this activation function enables fully connected neural networks to self
    normalize due to fixed point attractors in regards to parameter space, but only
    with specific values of alpha and lambda. Other than that, behaves similarly to
    ReLU (because it behaves exactly like an exponential linear unit with constants
    applied).
*/
public class SELU implements Layer 
    {
    private static final double ALPHA = 1.6732632423543772848170429916717;
    private static final double LAMBDA = 1.0507009873554804934193349852946;
    /**
       Helper method that actually runs the selu function
    */
    private static double selu(double x) 
        {
        if (x > 0)
            {
            return LAMBDA * x;
            }
        // EXP is expensive here, might want to use an approximator if this is running too
        // slow in the future
        return LAMBDA * (ALPHA * Math.exp(x) - ALPHA);
        }
        
    /**
       Feed a vector into the activation. The result is the vector with selu applied element wise.
     */
    public double[] feed(double[] vec)
        {
        double[] out = new double[vec.length]; 
        for(int i = 0; i < vec.length; i++)
            {
            out[i] = selu(vec[i]);
            }
        return out;
        }
    /**
       Doesn't do anything: has no parameters to initialize
     */
    public static Layer readFromString(String str)
        {
        return new SELU();
        }
    }

