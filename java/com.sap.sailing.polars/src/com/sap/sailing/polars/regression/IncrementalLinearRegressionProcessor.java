package com.sap.sailing.polars.regression;

public interface IncrementalLinearRegressionProcessor {
    
    double getY(double x) throws NotEnoughDataHasBeenAddedException;
    
    void addMeasuredPoint(double x, double y);

    double getSlope();

    double getIntercept();

}
