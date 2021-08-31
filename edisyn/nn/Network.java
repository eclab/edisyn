/***
    Copyright 2021 by V. Hoyle
    Licensed under the Apache License version 2.0
*/

package edisyn.nn;

import java.util.*;
import java.io.*;

/**
   An object representing a neural network comprised of layers.
*/
public class Network implements Layer 
    {
    // The feed-forward layers of the network
    private ArrayList<Layer> layers = new ArrayList<Layer>();

    /**
       Construct an empty network
    */
    public Network() { }

    /**
       Construct a network from a list of layers.
    */
    public Network(List<Layer> layers)
        {
        for(Layer layer: layers)
            {
            addLayer(layer);
            }
        }
        
    /**
       Add a layer to the end of the network
    */
    public void addLayer(Layer layer)
        {
        layers.add(layer);
        }

    /**
       Feed a vector through all the layers of the network and return the output.
    */
    public double[] feed(double[] vec)
        {
        double[] out = vec;
        for(Layer layer: layers)
            {
            out = layer.feed(out);
            }
        return out;
        }

    /**
       Load a network from a stream of lines representing layers.

       See Linear.java or SELU.java for specific details on how each line is parsed

       Format:

       <p><tt>
       {Linear|SELU} [layer arguments]
       ...
       
       Example:

       -- FILE BEGIN -- (this line not included in file)
       Linear 2 3 1.3 0.2 -1.1 2.2 0.6 -2.6 4.2 5.3 6.8
       SELU
       Linear 1 2 -0.1 0.5 0.734 -0.65
       SELU
       --- FILE END --- (this line not included in file)
       </tt>
    */    
    public static Network loadFromStream(InputStream stream) 
        {
        try
            {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line = reader.readLine();
            Network network = new Network();
            while (line != null) {
                if (line.startsWith("Linear")) 
                    {
                    network.addLayer(Linear.readFromString(line));
                    }
                else if (line.startsWith("SELU")) 
                    {
                    network.addLayer(SELU.readFromString(line));
                    }
                else 
                    {
                    // Error, who cares
                    }
                line = reader.readLine();
                }
            return network;
            } 
        catch(Exception e)
            {
            e.printStackTrace(new java.io.PrintStream(System.err));
            return null;
            }
                
        }

    /**
       Load a network from a string reprenstation of the network. Just uses the same logic
       in loadFromStream.
    */
    public static Layer readFromString(String str)
        {
        try
            {
            return loadFromStream(new ByteArrayInputStream(str.getBytes("UTF-8")));
            }
        catch(UnsupportedEncodingException e)
            {
            return null;
            }
        }

        
    /**
       Encode an integer in the range min to max (inclusive) as a double between 0 to 1.

       Return the index into the vector of the next feature to encode
    */
    public static int encodeScaled(double[] vector, int index, int value, int min, int max)
        {
        vector[index] = ((double)value)/(max - min);
        // Next empty slot is adjacent
        return index+1;
        }

    /**
       Encode the categorical data into a one hot vector beginning at the provided index
       with the categorical bounds being between min and max (inclusive).

       Return the index into the vector of the next feature to encode
    */
    public static int encodeOneHot(double[] vector, int index, int value, int min, int max)
        {
        if (value > max)
            {
            value = max;
            }
        if (value < min)
            {
            value = min;
            }
        vector[index + (value - min)] = 1;

        // next empty slot is directly after the one-hot encoded vector
        return index + (max - min) + 1;
        }

    /**
       Decode a double value in the range 0-1 in the vector at the index given to an
       integer in the range from min to max, inclusive.

       <p><tt>
       Returns a vector composed of 
       {
       index of the next feature to decode, 
       integer value from the scaled value
       }
       </tt>
    */
    public static int[] decodeScaled(double[] vector, int index, int min, int max)
        {
        int val = (int)Math.round(vector[index]*(max-min) + min);
        if (val > max)
            {
            val = max;
            }
                
        if (val < min)
            {
            val = min;
            }
        return new int[] {index + 1, val};
        }

    /**
       Decode a one hot feature representing a class density function into a integer
       (basically argmax of the selected vector) The min and max define the possible class
       range as well as the length of the subvector. This is useful for getting
       categorical data.

       <p><tt>
       Returns an array composed of 
       {
       index of the next feature to decode,
       the class id of the highest element in the subvector
       }

       example:
       vector: 0.1 0.8 2.6 -0.5 0.76
       index:  1
       min: 11
       max: 13
       would return
       {
       4 (which is the index of 0.76),
       12 (class id 12, which means the highest value was the index 1 element of the subvector 0.8 2.6 -0.5)
       }
       </tt>
    */        
    public static int[] decodeOneHot(double[] vector, int index, int min, int max)
        {
        double maxVal = vector[index];
        int maxInd = index;
        for(int i = index; i < index + (max-min) + 1; i++)
            {
            if (vector[i] > maxVal)
                {
                maxVal = vector[i];
                maxInd = i;
                }
            }
        int val = (maxInd - index) + min;
        return new int[] {index + (max-min) + 1, val};
        }

    /**
       Adds uniform noise with size of -weight to weight to a base vector.

       Unbounded.
    */
    public static double[] shiftVectorUniform(double[] vector, Random random, double weight)
        {
        double[] out = new double[vector.length];
        for(int i = 0; i < vector.length; i++)
            {
            out[i] = vector[i]+2*(random.nextDouble()-0.5)*weight;
            }
        return out;
        }

    /**
       Performs an addition of gaussian noise with variance == weight and mean == 0 to a
       base vector. 

       Unbounded.
    */
    public static double[] shiftVectorGaussian(double[] vector, Random random, double weight)
        {
        double[] out = new double[vector.length];
        for(int i = 0; i < vector.length; i++)
            {
            out[i] = vector[i] + (random.nextGaussian() * weight);
            }
        return out;
        }

    
    // Number of tries before the rejection sampler gives up and just adds 0 for the
    // element
    static final int TRIES = 20;

    /**
       Performs an addition of gaussian noise with variance == weight and mean == 0 to a
       base vector.
       
       Keeps the resulting value within a hypercube centered at 0 with edge length ==
       2*bounds.
    */
    public static double[] shiftVectorGaussianBounded(double[] vector, Random random, double weight, double bounds)
        {
        double[] out = new double[vector.length];
        for(int i = 0; i < vector.length; i++)
            {
            double noise = 0;
            for(int tries = 0; tries < TRIES; tries++)
                {
                noise = random.nextGaussian() * weight;
                if (vector[i] + noise <= bounds && vector[i] + noise >= -bounds)
                    {
                    out[i] = vector[i] + noise;
                    break;
                    }
                }
            }
        return out;
        }

    /**
       Perform the element-wise mean between two vectors
    */
    public static double[] vectorMean(double[] vec1, double[] vec2)
        {
        double out[] = new double[vec2.length];
        for(int i = 0; i < vec1.length; i++)
            {
            out[i] = (vec1[i] + vec2[i])/2;
            }
        return out;

        }
    /**
       Perform the element-wise mean between three vectors
    */
    public static double[] vectorMean(double[] vec1, double[] vec2, double[] vec3)
        {
        double out[] = new double[vec2.length];
        for(int i = 0; i < vec1.length; i++)
            {
            out[i] = (vec1[i] + vec2[i] + vec3[i])/3;
            }
        return out;
        }

    }
