/***
    Copyright 2021 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.nn;

import java.util.*;
import edisyn.*;

public interface ProvidesNN
    {
    /** For models which use shiftVectorGaussianBounded 
        to randomize their models, this is a typical 
        bounds provided (so the range is +/- 5.0) */
    public static final double BOUNDS = 5.0;
    
    /** For models which use shiftVectorGaussian
        or shiftVectorGaussianBounded to randomize
        their models, this is a typical scaling of the
        weight factor (so it goes 0...5). */
    public static final double WEIGHT_SCALING = 5.0;
    
    public Model decode(double [] vector);
    public double[] encode(Model model);
    
    /** Randomizes the model by a weight 0...1.0, yielding a new model */
    public Model randomizeNNModel(double weight);
    }
        
