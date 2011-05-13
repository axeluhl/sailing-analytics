package com.sap.sailing.domain.base.impl;

public class NanosecondTimePoint extends AbstractTimePoint {
    private final long nanos;
    
    public NanosecondTimePoint(long nanos) {
        super();
        this.nanos = nanos;
    }

    @Override
    public long asNanos() {
        return nanos;
    }
    
}
