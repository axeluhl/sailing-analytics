package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.Duration;

public class MillisecondDurationImpl implements Duration {
    private final long millis;
    
    public MillisecondDurationImpl(long millis) {
        super();
        this.millis = millis;
    }

    @Override
    public long asMillis() {
        return millis;
    }
}
