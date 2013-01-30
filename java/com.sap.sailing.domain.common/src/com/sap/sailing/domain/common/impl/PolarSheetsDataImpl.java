package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.PolarSheetsData;

public class PolarSheetsDataImpl implements PolarSheetsData {

    private static final long serialVersionUID = -4649254807341866894L;

    private Number[] values;
    
    private boolean complete;
    
    private int dataCount;

    private Integer[] dataCountPerAngle;
    
    //For GWT Serialization
    PolarSheetsDataImpl() {};
   
    public PolarSheetsDataImpl(Number[] values, boolean complete, int dataCount, Integer[] dataCountPerAngle) {
        this.values = values;
        this.complete = complete;
        this.dataCount = dataCount;
        this.dataCountPerAngle = dataCountPerAngle;
    }

    @Override
    public Number[] getValues() {
        return values;
    }

    @Override
    public boolean isComplete() {
        return complete;
    }

    @Override
    public int getDataCount() {
        return dataCount;
    }

    @Override
    public Integer[] getDataCountPerAngle() {
        return dataCountPerAngle;
    }

}
