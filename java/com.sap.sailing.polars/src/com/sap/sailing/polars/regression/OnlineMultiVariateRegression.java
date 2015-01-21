package com.sap.sailing.polars.regression;

public interface OnlineMultiVariateRegression {
    
    /**
     * Adds data to the regression. This will change the weights.
     * 
     * @param y
     * @param x
     *            length of the array needs to be the dimension of the regression (depending on the implementation, the
     *            dimension could be configured in the constructor), else IllegalArgumentsException will be thrown
     */
    void addData(double y, double[] x);
    
    /**
     * After adding some data with {@link #addData(double, double[])} this method can be used to estimate y for any
     * x-vector.
     * 
     * @param x
     *            length of the array needs to be the dimension of the regression (depending on the implementation, the
     *            dimension could be configured in the constructor), else IllegalArgumentsException will be thrown
     * @return
     */
    double estimateY(double[] x);

}
