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
    
    private int numberOfAddedPoints = 0;
    
    private final double[] means;
    
    private final double[] sumsOfSquaresOfDifferencesFromMean;
    
    private final double[] variances;

    public OnlineMultiVariateRegressionImpl(int numberOfDimensions) {
        weights = initializeDoubleArray(numberOfDimensions + 1);
        means = initializeDoubleArray(numberOfDimensions);
        sumsOfSquaresOfDifferencesFromMean = initializeDoubleArray(numberOfDimensions);
        variances = initializeDoubleArray(numberOfDimensions);
    }

    private double[] initializeDoubleArray(int arrayLength) {
        double[] weights = new double[arrayLength];
        for (int i = 0; i < weights.length; i++) {
            weights[i] = 0;
        }
        return weights;
    }

    @Override
    public void addData(double y, double[] x) {
        checkNumberOfDimensions(x);
        numberOfAddedPoints++;
        double[] newWeights = initializeDoubleArray(weights.length);
        double alpha = 0.01;
        newWeights[0] = weights[0] + alpha * (y - estimateY(x));
        for (int i = 1; i < weights.length; i++) {
            incrementallyUpdateVariances(x, i);
            double normalizedInput = (x[i-1] - means[i-1]) / Math.sqrt(variances[i-1]);
            newWeights[i] = weights[i] + Math.pow(alpha, i)*(y - estimateY(x)) * x[i-1];//normalizedInput;
        }
        weights = newWeights;
    }

    private void incrementallyUpdateVariances(double[] x, int i) {
        //http://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Incremental_algorithm
        double oldMean = means[i-1];
        means[i-1] = means[i-1] + ((x[i-1] - means[i-1]) / numberOfAddedPoints);
        sumsOfSquaresOfDifferencesFromMean[i-1] = sumsOfSquaresOfDifferencesFromMean[i-1] + (x[i-1] - oldMean) * (x[i-1] - means[i-1]);
        variances[i-1] = sumsOfSquaresOfDifferencesFromMean[i-1] / (numberOfAddedPoints - 1);
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
