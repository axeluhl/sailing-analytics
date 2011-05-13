package com.sap.sailing.domain.base.impl;


public class MillisecondsTimePoint extends AbstractTimePoint {
    private final long millis;
    
    public MillisecondsTimePoint(long millis) {
        super();
        this.millis = millis;
    }

    @Override
    public long asMillis() {
        return millis;
    }

}
