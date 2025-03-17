package com.sap.sse.security.shared.impl;

import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

public class LockingAndBanningImpl implements LockingAndBanning {
    private static final long serialVersionUID = 3547356744366236677L;
    
    public static final Duration DEFAULT_INITIAL_LOCKING_DELAY = Duration.ONE_SECOND;
    
    /**
     * An always valid time point which may be in the past. If it is in the future,
     * {@link #isPasswordAuthenticationLocked()} will return {@code true}.
     */
    private TimePoint lockedUntil;
    
    /**
     * An always valid, non-zero duration that indicates for how long into the future the {@link #lockedUntil} time
     * point will be set in case a {@link #failedPasswordAuthentication() failed password authentication} is notified.
     */
    private Duration nextLockingDelay;

    /**
     * Creates an instance that is unlocked and has a "last locking delay" of one second
     */
    public LockingAndBanningImpl() {
        this(TimePoint.BeginningOfTime, DEFAULT_INITIAL_LOCKING_DELAY);
    }
    
    public LockingAndBanningImpl(TimePoint lockedUntil, Duration nextLockingDelay) {
        super();
        this.lockedUntil = lockedUntil;
        this.nextLockingDelay = nextLockingDelay;
    }

    /**
     * Locks for the {@link #nextLockingDelay} and doubles the delay for the next failed attempt.
     */
    @Override
    public void failedPasswordAuthentication() {
        lockedUntil = TimePoint.now().plus(nextLockingDelay);
        nextLockingDelay = nextLockingDelay.times(2);
    }

    @Override
    public void successfulPasswordAuthentication() {
        nextLockingDelay = DEFAULT_INITIAL_LOCKING_DELAY;
        lockedUntil = TimePoint.BeginningOfTime;
    }

    @Override
    public boolean isPasswordAuthenticationLocked() {
        return TimePoint.now().before(lockedUntil);
    }

    public TimePoint getLockedUntil() {
        return lockedUntil;
    }

    public Duration getLastLockingDelay() {
        return nextLockingDelay;
    }
}
