package com.sap.sse.common.util;

import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

/**
 * A tracker to handle continuous failures of processes. Before every process execution, call {@link #backOff()} to
 * query whether there is a grace period still active since the last failure. Every failure should call
 * {@link #logFailure()}, which will increase the time until {@link #backOff()} returns {@code false} again.<p>
 * 
 * The class is thread safe which is achieved by the essential methods {@link #backOff()}, {@link #logFailure()},
 * and {@link #clear()} all being {@code synchronized}.
 */
public class BackoffTracker {
    private TimePoint backOffUntil;
    private final int backoffMultiplier;
    private Duration currentTimeout;
    private final Duration initialTimeout;
    private final Duration maxTimeout;

    /**
     * Creates a new backoff tracker, initially in the "OK" state with {@link #backOff()} returning {@code false} until
     * {@link #logFailure()} is called. The maximum timeout defaults to five minutes. See also
     * {@link##BackoffTracker(Duration, int, Duration)} for setting a different maximum timeout.
     * 
     * @param initialTimeout
     *            the length of the first "grace period" after the first failure has been {@link #logFailure() logged}
     * @param backoffMultiplier
     *            The factor by which the timeout will be multiplied on consecutive failures
     */
    public BackoffTracker(Duration initialTimeout, int backoffMultiplier) {
        this(initialTimeout, backoffMultiplier, Duration.ONE_MINUTE.times(5));
    }
    
    /**
     * Like {@link #BackoffTracker(Duration, int)}, but allows the caller to set a specific maximum timeout.
     */
    public BackoffTracker(Duration initialTimeout, int backoffMultiplier, Duration maxTimeout) {
        this.initialTimeout = initialTimeout;
        this.backoffMultiplier = backoffMultiplier;
        this.maxTimeout = maxTimeout;
    }

    /**
     * Call this method after an unsuccessful attempt to invoke your service. This starts a
     * grace period during which {@link #backOff()} will return {@code true}, telling you
     * to not invoke the service again until {@link #backOff()} returns {@code false}.
     */
    public synchronized void logFailure() {
        if (currentTimeout == null) {
            currentTimeout = initialTimeout;
        } else {
            final Duration newTimeOut = currentTimeout.times(backoffMultiplier);
            currentTimeout = newTimeOut.compareTo(maxTimeout) > 0 ? maxTimeout : newTimeOut;
        }
        backOffUntil = TimePoint.now().plus(currentTimeout);
    }

    /**
     * @return {@code true} if the caller should back off and currently not try to invoke the service. A {@code true}
     *         response indicates that a failure was {@link #logFailure() logged} previously and that a grace period
     *         during which the service shouldn't be invoked again since the last failure hasn't expired yet.
     */
    public synchronized boolean backOff() {
        return backOffUntil == null ? false : backOffUntil.after(TimePoint.now());
    }

    /**
     * Call this method after a successful attempt of invoking your service. The grace period duration
     * is reset, and {@link #backOff()} will return {@code false} from here on until {@link #logFailure()}
     * is called.
     */
    public synchronized void clear() {
        currentTimeout = null;
        backOffUntil = null;
    }
}
