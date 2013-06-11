package com.sap.sailing.domain.common.impl;


public class PolarSheetsWindStepping extends WindSteppingImpl {
    
    private static final long serialVersionUID = -2207840179212727591L;

    //For GWT Serialization
    PolarSheetsWindStepping() {
        super();
    };

    public PolarSheetsWindStepping(Integer[] levels) {
        super(levels);
    }
    
    @Override
    public int getLevelIndexForValue(double speed) {
        Integer[] levels = getRawStepping();
        if (speed > levels[levels.length - 1] + 5) {
            return -1;
        }   
        return super.getLevelIndexForValue(speed);
    }
}
