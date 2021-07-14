/***
    Copyright 2021 by Bryan Hoyle
    Licensed under the Apache License version 2.0
*/

package edisyn.nn;
import java.util.*;
import java.io.*;


public class Network implements Layer 
    {
    ArrayList<Layer> layers = new ArrayList<Layer>();

    public Network() { }
        
    public Network(List<Layer> layers)
        {
        for (Layer layer: layers)
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
        for (Layer layer: layers)
            {
            out = layer.feed(out);
            }
        return out;
        }
        
    /*public static void main(String[] args)
      {
      Network encoder = Network.loadFromStream(Network.class);
      double[] out = new double[1];
      out = encoder.feed(new double[]
      {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1});
      for (int i = 0; i < out.length; i++)
      {
      System.out.print(out[i]);
      System.out.print(" ");
      }
      System.out.println();
      Network decoder = Network.loadFromStream();
      out = decoder.feed(out);
      for (int i = 0; i < out.length; i++)
      {
      System.out.print(out[i]);
      System.out.print(" ");
      }
      System.out.println();
        
      }*/
      
    public static Network loadFromStream(InputStream stream) 
        {
        try
            {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line = reader.readLine();
            Network network = new Network();
            while (line != null) 
                {
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
    }
