package com.sap.sailing.domain.common;

public interface TimingConstants {
    /**
     * The time after the end of a race during which it is still considered and displayed as "live"
     */
    final long IS_LIVE_GRACE_PERIOD_IN_MILLIS = 3 * 60 * 1000; // three minutes
}
