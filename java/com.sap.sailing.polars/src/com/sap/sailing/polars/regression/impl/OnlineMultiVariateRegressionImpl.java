package com.sap.sailing.polars.regression.impl;

import com.sap.sailing.polars.regression.OnlineMultiVariateRegression;

/**
 * Allows online regression for multivariate linear functions as explained in:
 * 
 * http://people.cs.pitt.edu/~milos/courses/cs2750-Spring03/lectures/class6.pdf (especially: Page 8+9)
 * 
 * @author Frederik Petersen (D054528)
 *
 */
public class OnlineMultiVariateRegressionImpl implements OnlineMultiVariateRegression {
    
    private double[] weights;
    
    private int addedPoints = 0;

    public OnlineMultiVariateRegressionImpl(int numberOfDimensions) {
        weights = initializeWeightsArray(numberOfDimensions + 1);
    }

    private double[] initializeWeightsArray(int arrayLength) {
        double[] weights = new double[arrayLength];
        for (int i = 0; i < weights.length; i++) {
            weights[i] = 0;
        }
        return weights;
    }

    @Override
    public void addData(double y, double[] x) {
        checkNumberOfDimensions(x);
        addedPoints++;
        double[] newWeights = initializeWeightsArray(weights.length);
        double alpha = 1.0/addedPoints;
        newWeights[0] = weights[0] + alpha * (y - estimateY(x));
        for (int i = 1; i < weights.length; i++) {
            newWeights[i] = weights[i] + alpha*(y - estimateY(x))*x[i-1];
        }
        weights = newWeights;
    }

    @Override
    public double estimateY(double[] x) {
        checkNumberOfDimensions(x);
        double resultSum = weights[0];
        for (int i = 1; i < weights.length; i++) {
            resultSum = resultSum + (weights[i] * x[i-1]);
        }
        return resultSum;
    }
    
    private void checkNumberOfDimensions(double[] x) {
        if (x.length + 1 != weights.length) {
            throw new IllegalArgumentException("Dimension of x needs to be the same as specified in constructor.");
        }
    }

}
