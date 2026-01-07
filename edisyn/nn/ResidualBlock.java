/***
    Copyright 2025 by Sean Luke
    Licensed under the Apache License version 2.0
*/


package edisyn.nn;

public class ResidualBlock implements Layer
    {
    Linear input;
    SELU selu;
    Linear output;
    Linear skip;
    
    public ResidualBlock(int rows, int columns, int hidden, double[] inputData, double[] inputBias, 
    					 double[] outputData, double[] outputBias, double[] skipData, double[] skipBias)
        {
        input = new Linear(rows, hidden, inputData, inputBias);
        output = new Linear(hidden, columns, outputData, outputBias);
        skip = new Linear(rows, columns, skipData, skipBias);
        selu = new SELU();
        }

    public ResidualBlock(Linear input, SELU selu, Linear output, Linear skip)
    	{
    	this.input = input;
    	this.selu = selu;
    	this.output = output;
    	this.skip = skip;
        }


    public double[] feed(double[] vec)
        {
        double[] in = input.feed(vec);
        double[] mid = selu.feed(in);
        double[] out = output.feed(mid);
        double[] full = skip.feed(vec);
        
        // Sanity check
        if(full.length != out.length)
            {
            System.err.println("Skip has wrong length " + full.length + ", expected " + out.length);
            return null;
            }
            
        // Sum
        for(int i = 0; i < out.length; i++)
        	{
        	out[i] += full[i];
        	}
        	
        return selu.feed(out);
        }

    public static Layer readFromString(String _residual, String _in, String _selu, String _out, String _skip, String _selu2)
        {
        // do nothing with _residual
        Linear input = (Linear)Linear.readFromString(_in);
        SELU selu = (SELU)SELU.readFromString(_selu);
        Linear output = (Linear)Linear.readFromString(_out);
        Linear skip = (Linear)Linear.readFromString(_skip);
        SELU selu2 = (SELU)SELU.readFromString(_selu2);
        // do nothing with _selu2
        
        return new ResidualBlock(input, selu, output, skip);
        }

    }
