package com.sap.sse.common.impl;

import com.sap.sse.common.Duration;

public abstract class AbstractDuration implements Duration {
    private static final long serialVersionUID = -7217998647218524638L;

    @Override
    public String toString() {
        return String.format("%02d:%02d:%2.3f", ((int) asHours()), (((int) asMinutes()%60)), asSeconds()%60);
    }
}
