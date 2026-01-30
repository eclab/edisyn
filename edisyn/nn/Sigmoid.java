/***
    Copyright 2025 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.nn;

/**
   A standard sigmoid nonlinearity layer.
**/

public class Sigmoid implements Layer 
    {
    double beta;
    
    public Sigmoid()
        {
        this(1.0);
        }
        
    public Sigmoid(double beta)
        {
        this.beta = beta;
        }
         
    /**
       The actual sigmoid function: 1/1+e^(-bx)
    */
    static double sigmoid(double x, double beta) 
        {
        return 1.0 / (1.0 + Math.exp(-1.0 * x * beta));
        }
        
    /**
       Feed a vector into the activation. The result is the vector with sigmoid applied element wise.
    */
    public double[] feed(double[] vec)
        {
        double[] out = new double[vec.length]; 
        for(int i = 0; i < vec.length; i++)
            {
            out[i] = sigmoid(vec[i], beta);
            }
        return out;
        }
        
    /**
       One optional parameter: beta
    */
    public static Layer readFromString(String str)
        {
        String[] strs = str.split(" ");
        if (strs.length > 1)
            {
            double beta = Double.parseDouble(strs[1]);
            return new Sigmoid(beta);
            }
        else return new Sigmoid();
        }
    }

