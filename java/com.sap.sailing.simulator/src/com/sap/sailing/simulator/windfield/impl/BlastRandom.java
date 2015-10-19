package com.sap.sailing.simulator.windfield.impl;

import java.util.Random;

public class BlastRandom extends Random {

    private static final long serialVersionUID = -6409398526247431352L;

    // random number having uniform distribution
    public double nextDouble(double min, double max) {
        return (min + (max - min) * this.nextDouble());
    }

    // random number having Gaussian distribution
    public double nextGaussian(double mean, double variance) {
        return (mean + variance * this.nextGaussian());
    }

    // random number having exponential distribution
    public double nextExponential(double lambda) {
        return -Math.log(1.0 - this.nextDouble())/lambda;
    }

    // random number having geometric distribution
    public int nextGeometric(double p) {
        return (int)Math.floor(nextExponential(-Math.log(1.0 - p)));
    }
    
}
