/***
    Copyright 2021 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn;

/**
        
   @author Sean Luke
*/

import java.util.*;

public interface ProvidesNN
    {
	    public Model decode(double [] vector);
	    public double[] encode();
	    public double[] encode(Model model);
        public void randomizeNNModel();
	    public static int encodeScaled(double[] vector, int index, int value, int min, int max)
            {
            vector[index] = ((double)value)/(max - min);
            return index+1;
            }
	    public static int encodeOneHot(double[] vector, int index, int value, int min, int max)
            {
            if(value > max){
                value = max;
            }
            vector[index + (value - min)] = 1;
            return index + (max - min) + 1;
            }
	    public static int[] decodeScaled(double[] vector, int index, int min, int max)
            {
	        int val = (int)Math.round(vector[index]*(max-min) + min);
	        if(val > max){
		        val = max;
	        }
	        if(val < min){
		        val = min;
	        }
	        return new int[]{index + 1, val};

            }
	    public static int[] decodeOneHot(double[] vector, int index, int min, int max)
            {
		    double maxVal = vector[index];
		    int maxInd = index;
		    for(int i = index; i < index + (max-min) + 1; i++){
			    if(vector[i] > maxVal){
				    maxVal = vector[i];
				    maxInd = i;
			    }
		    }
		    int val = (maxInd - index) + min;
		    return new int[]{index + (max-min) + 1, val};
            }

        public static double[] shiftVectorUniform(double[] vector, Random random, double amount)
            {
            double[] out = new double[vector.length];
            for(int i = 0; i < vector.length; i++)
                {
                out[i] = vector[i]+2*(random.nextDouble()-0.5)*amount;
                }
            return out;
            }
}
        
