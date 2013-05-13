package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.PolarSheetsHistogramData;

public class PolarSheetsHistogramDataImpl implements PolarSheetsHistogramData {

    private static final long serialVersionUID = 7492650285773447757L;

    private Number[] yValues;
    
    private Number[] xValues;
    
    private int angle;
    
    private int dataCount;
    
    //For GWT serialization
    PolarSheetsHistogramDataImpl() {}

    public PolarSheetsHistogramDataImpl(int angle, Number[] xValues, Number[] yValues, int dataCount) {
        super();
        this.angle = angle;
        this.yValues = yValues;
        this.xValues = xValues;
        this.dataCount = dataCount;
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

}
