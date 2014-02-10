package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.Duration;

public class MillisecondsDurationImpl implements Duration {
    private static final long serialVersionUID = -4257982564719184723L;
    private long millis;
    
    MillisecondsDurationImpl() {} // for serialization only
    
    public MillisecondsDurationImpl(long millis) {
        super();
        this.millis = millis;
    }

    @Override
    public long asMillis() {
        return millis;
    }

    @Override
    public double asSeconds() {
        return ((double) asMillis()) / 1000.;
    }

    @Override
    public Duration divide(long divisor) {
        return new MillisecondsDurationImpl(asMillis() / divisor);
    }
}
