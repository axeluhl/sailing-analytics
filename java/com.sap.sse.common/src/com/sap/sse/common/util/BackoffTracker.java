package com.sap.sse.common.util;

import java.util.concurrent.TimeUnit;

public class BackoffTracker {
    private Long backOffUntil;
    private final int factor;
    private Long currentTimeoutInMillis;
    private final Long initialTimeoutInMillis;
    private static final Long maxTimeout = TimeUnit.MINUTES.toMillis(5);
    
    /**
     * A tracker to handle continous failures of processes. Before every process execution call {@link #backOff()} to query whether there is a timeout.
     * Every failure should call {@link #logFailure()}, which will increase the time until {@link #backOff()} returns false again.
     * @param initialTimeoutInMillis
     * @param backoffMultiplier: The factor by which the timeout will be multiplied on consecutive failures
     */
    public BackoffTracker(Long initialTimeoutInMillis, int backoffMultiplier) {
        this.initialTimeoutInMillis = initialTimeoutInMillis;
        this.factor = backoffMultiplier;
    }
    
    public void logFailure() {
        if(currentTimeoutInMillis == null) {
            currentTimeoutInMillis = initialTimeoutInMillis;
        }
        final Long newTimeOut = currentTimeoutInMillis * factor;
        currentTimeoutInMillis = newTimeOut >= maxTimeout ? maxTimeout : newTimeOut;
        backOffUntil = System.currentTimeMillis() + currentTimeoutInMillis;
    }
    
    public boolean backOff() {
        return backOffUntil == null ? false : backOffUntil > System.currentTimeMillis();
    }
    
    public void clear() {
        currentTimeoutInMillis = null;
        backOffUntil = null;
    }
}
