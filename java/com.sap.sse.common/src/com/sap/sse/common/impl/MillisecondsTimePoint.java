package com.sap.sse.common.impl;

import java.util.Date;


public class MillisecondsTimePoint extends AbstractTimePoint {
    private static final long serialVersionUID = -1021748860232043166L;
    private long millis;
    private Date date;
    
    public static MillisecondsTimePoint now() {
        return new MillisecondsTimePoint(System.currentTimeMillis());
    }
    
    MillisecondsTimePoint() {}; // for serialization only

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
