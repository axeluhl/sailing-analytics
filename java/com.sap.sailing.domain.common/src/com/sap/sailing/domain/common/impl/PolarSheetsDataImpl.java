package com.sap.sailing.domain.common.impl;

import java.util.Map;

import com.sap.sailing.domain.common.PolarSheetsData;

public class PolarSheetsDataImpl implements PolarSheetsData {

    private static final long serialVersionUID = -4649254807341866894L;

    private Map<Integer, Double> data;
    
    private boolean complete;
    
    //For GWT Serialization
    PolarSheetsDataImpl() {};
   
    public PolarSheetsDataImpl(Map<Integer, Double> data, boolean complete) {
        this.data = data;
        this.complete = complete;
    }

    @Override
    public Map<Integer, Double> getData() {
        return data;
    }

    @Override
    public boolean isComplete() {
        return complete;
    }

}
