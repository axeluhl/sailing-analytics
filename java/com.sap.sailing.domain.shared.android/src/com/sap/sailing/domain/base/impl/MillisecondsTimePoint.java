package com.sap.sailing.domain.base.impl;

import java.util.Date;


public class MillisecondsTimePoint extends AbstractTimePoint {
    private static final long serialVersionUID = -1021748860232043166L;
    private final long millis;
    private Date date;
    
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

    protected Date getDateFromCache() {
        return date;
    }

    protected void cacheDate(Date date) {
        this.date = date;
    }

    @Override
    public long asMillis() {
        return millis;
    }

}
