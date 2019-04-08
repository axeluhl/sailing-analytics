package com.sap.sse.util.impl;

import java.util.Timer;
import java.util.TimerTask;

import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class ApproximateTime {
    private static TimePoint timePointNoOlderThanOneSecond;
    
    static {
        timePointNoOlderThanOneSecond = MillisecondsTimePoint.now();
        new Timer("LockUtil timestamp generator", /* isDaemon */ true).schedule(
            new TimerTask() {
                @Override
                public void run() {
                    timePointNoOlderThanOneSecond = MillisecondsTimePoint.now();
                }
            }, /* delay 0 means immediate execution */ 0l, /* period 1s */ 1000l);
    }
    
    /**
     * A time point that is approximately the current time, up to one second late. Obtaining a time point
     * this way is much less expensive than using {@link #now()} which really generates a new time point
     * upon each invocation and as such does not qualify for high-frequency operations, particularly when
     * the timer is used for some human time-based timeout of several seconds.
     */
    public static TimePoint approximateNow() {
        return timePointNoOlderThanOneSecond;
    }
}
