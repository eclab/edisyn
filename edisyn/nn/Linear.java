/***
    Copyright 2021 by V. Hoyle
    Licensed under the Apache License version 2.0
*/


package edisyn.nn;

/**
   This class is a representation of a fully connected neural network layer
   consisting of an input, weights, and output.
*/
public class Linear implements Layer
    {
    private double[] data;
    private double[] bias;
    private int rows;
    private int columns;

    /**
       Constructor takes the number of columns (layer input size) and number of rows
       (layer output size) along with the weights and the bias values.

    */
    public Linear(int rows, int columns, double[] initial_data, double[] bias)
        {
        this.rows = rows;
        this.columns = columns;
        data = new double[rows * columns];
        for(int i = 0; i < initial_data.length; i++)
            {
            // row major order for cache coherency
            data[i] = initial_data[i];
            }

        this.bias = new double[rows];
        for(int i = 0; i < bias.length; i++)
            {
            this.bias[i] = bias[i];
            }
        }

    /**
       Feed in a vector to the input and receive as output the vector pushed through the
       layer (with bias included in the output).

       This is equivalent to Wv + b where W is the weight matrix and b is the bias values.

       The input vector must be equal to columns in size and the resulting vector will be
       rows in size.
    */
    public double[] feed(double[] vec)
        {
        // Sanity check
        if(vec.length != columns)
            {
            System.err.println("Bad input to feed: vec.length != columns");
            return null;
            }
        // Initialize the output vector
        double[] out = new double[rows];
        for(int r = 0; r < rows; r++)
            {
            for(int c = 0; c < columns; c++)
                {
                // row major order for cache coherency
                out[r] += data[r * columns + c] * vec[c];
                }
            // Add the bias value
            out[r] += bias[r];
            }
        return out;
        }
    /**
       Takes in a plain text string representation of a layer and return a constructed
       layer object.
       <p><tt>
       format:
       Linear {rows} {columns} [biases: rows in size] [weights: rows * columns in size, provided in row major order]
       example:
       Linear 2 3 1.3 0.2 -1.1 2.2 0.6 -2.6 4.2 5.3 6.8
       ^^^^^^ ^ ^ ^^^^^^^^^^^^ ^^^^^^^^^^^^ ^^^^^^^^^^^
       │   │ │     Bias         Row 1       Row 2                                               
       │   │ │                                                                                 
       │   │ └─────────────┐
       ┌──┘   └───────┐       │ 
       │              │       │
       Layer Type Specifier  rows  columns
       </tt>
    */
    public static Layer readFromString(String str)
        {
        String[] strs = str.split(" ");
        int rows = Integer.parseInt(strs[1]);
        int columns = Integer.parseInt(strs[2]);
        double[] weights = new double[rows*columns];
        double[] bias = new double[rows];
        // Read in the bias
        for(int i = 3; i < rows + 3; i++)
            {
            bias[i-3] = Double.parseDouble(strs[i]);
            }
        // Read in the weights
        for(int i = rows + 3; i < strs.length; i++)
            {
            weights[i-3 - rows] = Double.parseDouble(strs[i]);
            }
        return new Linear(rows, columns, weights, bias);
        }

    }
