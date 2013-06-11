package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.PolarSheetsHistogramData;

public class PolarSheetsHistogramDataImpl implements PolarSheetsHistogramData {

    private static final long serialVersionUID = 7492650285773447757L;

    private Number[] yValues;
    
    private Number[] xValues;
    
    private int angle;
    
    private int dataCount;

    private double coefficiantOfVariation;

    private double confidenceMeasure = 0;
    
    //For GWT serialization
    PolarSheetsHistogramDataImpl() {}

    public PolarSheetsHistogramDataImpl(int angle, Number[] xValues, Number[] yValues, int dataCount, double coefficiantOfVariation) {
        super();
        this.angle = angle;
        this.yValues = yValues;
        this.xValues = xValues;
        this.dataCount = dataCount;
        this.coefficiantOfVariation = coefficiantOfVariation;
    }

    @Override
    public Number[] getyValues() {
        return yValues;
    }

    @Override
    public Number[] getxValues() {
        return xValues;
    }

    @Override
    public int getAngle() {
        return angle;
    }

    @Override
    public int getDataCount() {
        return dataCount;
    }

    @Override
    public double getCoefficiantOfVariation() {
        return coefficiantOfVariation;
    }

    @Override
    public double getConfidenceMeasure() {
        return confidenceMeasure;
    }

    @Override
    public void setConfidenceMeasure(double polarSheetPointConfidenceMeasure) {
        confidenceMeasure = polarSheetPointConfidenceMeasure;      
    }

}
