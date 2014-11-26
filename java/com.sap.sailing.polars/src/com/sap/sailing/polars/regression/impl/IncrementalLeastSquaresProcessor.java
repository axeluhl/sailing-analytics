package com.sap.sailing.polars.regression.impl;

import com.sap.sailing.polars.regression.IncrementalLinearRegressionProcessor;
import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;

/**
 * Allows a linear regression of a 2 dimensional and growing dataset. It has a complexity of O(1)
 * 
 * The algorithm and variable naming is similar to the <a
 * href="https://de.wikipedia.org/wiki/Methode_der_kleinsten_Quadrate#Lineare_Modellfunktion">german wikipedia
 * article</a>
 * 
 * The difference is that some parts of the equation are memorized as fields and updated incrementally.
 * 
 * @author Frederik Petersen (D054528)
 * 
 */
public class IncrementalLeastSquaresProcessor implements IncrementalLinearRegressionProcessor {

    private double alpha1 = 0;

    private double alpha0 = 0;

    private double partA = 0;

    private double partB = 0;

    private double meanOfX = 0;

    private double meanOfY = 0;

    private int numberOfPointsAdded = 0;

    @Override
    public double getEstimatedY(double x) throws NotEnoughDataHasBeenAddedException {
        if (numberOfPointsAdded < 1) {
            throw new NotEnoughDataHasBeenAddedException();
        }

        double result;
        if (numberOfPointsAdded < 2) {
            // Return the y value of the only added data point.
            result = meanOfY;
        } else {
            result = alpha0 + alpha1 * x;
        }

        return result;
    }

    @Override
    public void addMeasuredPoint(double x, double y) {
        numberOfPointsAdded++;

        calculatePartA(x, y);
        calculatePartB(x, y);

        addToArithmeticMeanOfX(x);
        addToArithmeticMeanOfY(y);

        calculateFactor1();
        calculateFactor2();
    }

    private void calculateFactor2() {
        alpha0 = meanOfY - alpha1 * meanOfX;
    }

    private void calculateFactor1() {
        double numerator = calculateNumeratorOfFactor1();
        double denominator = calculateDenominatorOfFactor1();
        if (denominator == 0) {
            alpha1 = 0;
        } else {
            alpha1 = numerator / denominator;
        }
    }

    private double calculateNumeratorOfFactor1() {
        return partA - numberOfPointsAdded * meanOfX * meanOfY;
    }

    private double calculateDenominatorOfFactor1() {
        return partB - numberOfPointsAdded * meanOfX * meanOfX;
    }

    private void addToArithmeticMeanOfY(double y) {
        meanOfY = addToMean(y, meanOfY);
    }

    private void addToArithmeticMeanOfX(double x) {
        meanOfX = addToMean(x, meanOfX);
    }

    private double addToMean(double z, double oldMean) {
        return oldMean + (z - oldMean) / numberOfPointsAdded;
    }

    private void calculatePartB(double x, double y) {
        partB = partB + x * x;
    }

    private void calculatePartA(double x, double y) {
        partA = partA + x * y;
    }

    @Override
    public double getSlope() {
        return alpha1;
    }

    @Override
    public double getIntercept() {
        return alpha0;
    }

    @Override
    public int getDataCount() {
        return numberOfPointsAdded;
    }

    @Override
    public double getMeanOfY() throws NotEnoughDataHasBeenAddedException {
        if (numberOfPointsAdded < 1) {
            throw new NotEnoughDataHasBeenAddedException();
        }
        return meanOfY;
    }

}
