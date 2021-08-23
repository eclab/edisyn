/***
    Copyright 2021 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.nn;

import java.util.*;
import edisyn.*;

public interface ProvidesNN
    {
    public Model decode(double [] vector);
    public double[] encode(Model model);
    public void randomizeNNModel(double weight);
    }
        
