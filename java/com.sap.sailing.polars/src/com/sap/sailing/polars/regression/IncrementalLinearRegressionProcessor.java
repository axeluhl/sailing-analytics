package com.sap.sailing.polars.regression;

public interface IncrementalLinearRegressionProcessor {
    
    double getY(double x) throws NoDataHasBeenAddedException;
    
    void addMeasuredPoint(double x, double y);

}
