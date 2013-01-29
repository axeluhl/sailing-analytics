package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.PolarSheetsData;

public class PolarSheetsDataImpl implements PolarSheetsData {

    private static final long serialVersionUID = -4649254807341866894L;

    private Number[] values;
    
    private boolean complete;
    
    //For GWT Serialization
    PolarSheetsDataImpl() {};
   
    public PolarSheetsDataImpl(Number[] values, boolean complete) {
        this.values = values;
        this.complete = complete;
    }

    @Override
    public Number[] getValues() {
        return values;
    }

    @Override
    public boolean isComplete() {
        return complete;
    }

}
