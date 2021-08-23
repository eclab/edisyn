/***
    Copyright 2021 by Bryan Hoyle
    Licensed under the Apache License version 2.0
*/

package edisyn.nn;

import java.util.*;
import java.io.*;

public class Network implements Layer 
    {
    private ArrayList<Layer> layers = new ArrayList<Layer>();
        
    public Network() { }
        
    public Network(List<Layer> layers)
        {
        for(Layer layer: layers)
            {
            addLayer(layer);
            }
        }
        
    public void addLayer(Layer layer)
        {
        layers.add(layer);
        }
        
    public double[] feed(double[] vec)
        {
        double[] out = vec;
        for(Layer layer: layers)
            {
            out = layer.feed(out);
            }
        return out;
        }
        
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
    public static Layer readFromString(String str)
        {
        // todo, don't care right now
        return null;
        }

        
    public static int encodeScaled(double[] vector, int index, int value, int min, int max)
        {
        vector[index] = ((double)value)/(max - min);
        return index+1;
        }
            
    public static int encodeOneHot(double[] vector, int index, int value, int min, int max)
        {
        if (value > max)
            {
            value = max;
            }
        vector[index + (value - min)] = 1;
        return index + (max - min) + 1;
        }
            
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
        return new int[]{index + 1, val};
        }
            
    public static int[] decodeOneHot(double[] vector, int index, int min, int max)
        {
        double maxVal = vector[index];
        int maxInd = index;
        for(int i = index; i < index + (max-min) + 1; i++){
            if (vector[i] > maxVal){
                maxVal = vector[i];
                maxInd = i;
                }
            }
        int val = (maxInd - index) + min;
        return new int[]{index + (max-min) + 1, val};
        }

    public static double[] shiftVectorUniform(double[] vector, Random random, double weight)
        {
        double[] out = new double[vector.length];
        for(int i = 0; i < vector.length; i++)
            {
            out[i] = vector[i]+2*(random.nextDouble()-0.5)*weight;
            }
        return out;
        }

    /*public static void main(String[] args){
      Network encoder = Network.loadFromStream(Network.class);
      double[] out = new double[1];
      out = encoder.feed(new double[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1});
      for(int i = 0; i < out.length; i++){
      System.out.print(out[i]);
      System.out.print(" ");
      }
      System.out.println();
      Network decoder = Network.loadFromStream();
      out = decoder.feed(out);
      for(int i = 0; i < out.length; i++){
      System.out.print(out[i]);
      System.out.print(" ");
      }
      System.out.println();
        
      }*/

    }
