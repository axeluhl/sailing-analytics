package com.sap.sailing.polars.regression.impl;

import org.apache.commons.math.stat.regression.SimpleRegression;

import com.sap.sailing.polars.regression.IncrementalLinearRegressionProcessor;
import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;

public class ApacheSimpleRegressionWrapper implements IncrementalLinearRegressionProcessor {

    private final SimpleRegression regression;

    public ApacheSimpleRegressionWrapper() {
        regression = new SimpleRegression();
    }

    @Override
    public double getY(double x) throws NotEnoughDataHasBeenAddedException {
        double prediction = regression.predict(x);
        if (Double.isNaN(prediction)) {
            throw new NotEnoughDataHasBeenAddedException();
        }
        return prediction;
    }

    @Override
    public void addMeasuredPoint(double x, double y) {
        regression.addData(x, y);
    }

    @Override
    public double getSlope() {
        return regression.getSlope();
    }

    @Override
    public double getIntercept() {
        return regression.getIntercept();
    }

}
