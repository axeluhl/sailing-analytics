package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.Duration;

public class MillisecondsDurationImpl implements Duration {
    private final long millis;
    
    public MillisecondsDurationImpl(long millis) {
        super();
        this.millis = millis;
    }

    @Override
    public long asMillis() {
        return millis;
    }

    @Override
    public Duration divide(long divisor) {
        return new MillisecondsDurationImpl(asMillis() / divisor);
    }
}
