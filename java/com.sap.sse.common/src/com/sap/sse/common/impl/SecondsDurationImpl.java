package com.sap.sse.common.impl;

import com.sap.sse.common.Duration;

/**
 * Uses double for internal representation. Therefore this implementation achieves a higher accuracy but uses more
 * memory in comparison to {@link MillisecondsDurationImpl}.
 * 
 * @author Daniel Lisunkin (i505543)
 *
 */
public class SecondsDurationImpl extends AbstractDuration {
    private static final long serialVersionUID = 7575912908654324216L;
    private final double seconds;

    public SecondsDurationImpl(double seconds) {
        super();
        this.seconds = seconds;
    }
    
    @Override
    public int compareTo(Duration o) {
        double diff = seconds - o.asSeconds();
        return diff > 0. ? 1 : diff < 0. ? -1 : 0;
    }

    @Override
    public long asMillis() {
        return Math.round(seconds / Duration.ONE_MILLISECOND.asSeconds());
    }

    @Override
    public double asSeconds() {
        return seconds;
    }

    @Override
    public double asMinutes() {
        return seconds / Duration.ONE_MINUTE.asSeconds();
    }

    @Override
    public double asHours() {
        return seconds / Duration.ONE_HOUR.asSeconds();
    }

    @Override
    public double asDays() {
        return seconds / Duration.ONE_DAY.asSeconds();
    }

    @Override
    public Duration abs() {
        return seconds >= 0 ? this : new SecondsDurationImpl(-seconds);
    }

    @Override
    public Duration divide(long divisor) {
        return new SecondsDurationImpl(seconds / divisor);
    }

    @Override
    public Duration divide(double divisor) {
        return new SecondsDurationImpl(seconds / divisor);
    }

    @Override
    public double divide(Duration duration) {
        return (seconds / duration.asSeconds());
    }

    @Override
    public Duration times(long factor) {
        return new SecondsDurationImpl(seconds * factor);
    }

    @Override
    public Duration times(double factor) {
        return new SecondsDurationImpl(seconds * factor);
    }

    @Override
    public Duration minus(Duration duration) {
        return new SecondsDurationImpl(seconds - duration.asSeconds());
    }

    @Override
    public Duration minus(long milliseconds) {
        return new SecondsDurationImpl(seconds - (double) milliseconds / 1000.);
    }

    @Override
    public Duration plus(long milliseconds) {
        return new SecondsDurationImpl(seconds + milliseconds / 1000.);
    }

    @Override
    public Duration plus(Duration duration) {
        return new SecondsDurationImpl(seconds + duration.asSeconds());
    }

    @Override
    public Duration mod(Duration d) {
        return new SecondsDurationImpl(seconds % d.asSeconds());
    }
}
