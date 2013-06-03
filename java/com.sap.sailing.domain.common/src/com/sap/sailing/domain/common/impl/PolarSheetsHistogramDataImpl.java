package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.PolarSheetsHistogramData;

public class PolarSheetsHistogramDataImpl implements PolarSheetsHistogramData {

    private static final long serialVersionUID = 7492650285773447757L;

    private Number[] yValues;
    
    private Number[] xValues;
    
    private int angle;
    
    private int dataCount;

    private double standardDeviator;
    
    //For GWT serialization
    PolarSheetsHistogramDataImpl() {}

    public PolarSheetsHistogramDataImpl(int angle, Number[] xValues, Number[] yValues, int dataCount, double standardDeviator) {
        super();
        this.angle = angle;
        this.yValues = yValues;
        this.xValues = xValues;
        this.dataCount = dataCount;
        this.standardDeviator = standardDeviator;
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
    public double getStandardDeviator() {
        return standardDeviator;
    }

}
