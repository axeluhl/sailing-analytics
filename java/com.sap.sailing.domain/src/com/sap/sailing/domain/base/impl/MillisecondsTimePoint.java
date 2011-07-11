package com.sap.sailing.domain.base.impl;

import java.util.Date;


public class MillisecondsTimePoint extends AbstractTimePoint {
    private final long millis;
    
    public static MillisecondsTimePoint now() {
        return new MillisecondsTimePoint(System.currentTimeMillis());
    }
    
    public MillisecondsTimePoint(long millis) {
        super();
        this.millis = millis;
    }
    
    public MillisecondsTimePoint(Date date) {
        super();
        this.millis = date.getTime();
    }

    @Override
    public long asMillis() {
        return millis;
    }

}
