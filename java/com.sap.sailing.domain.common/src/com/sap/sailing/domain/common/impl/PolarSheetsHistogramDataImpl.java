package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.PolarSheetsHistogramData;

public class PolarSheetsHistogramDataImpl implements PolarSheetsHistogramData {

    private static final long serialVersionUID = 7492650285773447757L;

    private Number[] yValues;
    
    private Number[] xValues;
    
    //For GWT serialization
    PolarSheetsHistogramDataImpl() {}

    public PolarSheetsHistogramDataImpl(Number[] xValues, Number[] yValues) {
        super();
        this.yValues = yValues;
        this.xValues = xValues;
    }

    @Override
    public Number[] getyValues() {
        return yValues;
    }

    @Override
    public Number[] getxValues() {
        return xValues;
    }

}
