package com.sap.sse.common.impl;

import com.sap.sse.common.Duration;

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

    @Override
    public Duration divide(double divisor) {
        return new MillisecondsDurationImpl((long) (1. / divisor * asMillis()));
    }

    @Override
    public Duration times(long factor) {
        return new MillisecondsDurationImpl(asMillis() * factor);
    }

    @Override
    public Duration times(double factor) {
        return new MillisecondsDurationImpl((long) (factor*asMillis()));
    }

    @Override
    public double asMinutes() {
        return asSeconds() / 60;
    }

    @Override
    public double asHours() {
        return asMillis() / Duration.ONE_HOUR.asMillis();
    }

    @Override
    public double asDays() {
        return asMillis() / Duration.ONE_DAY.asMillis();
    }
    
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(asSeconds()).append("s==").append(asMillis()).append("ms");
        return result.toString();
    }

    @Override
    public Duration minus(Duration duration) {
        return new MillisecondsDurationImpl(asMillis()-duration.asMillis());
    }

    @Override
    public Duration minus(long milliseconds) {
        return new MillisecondsDurationImpl(asMillis()-milliseconds);
    }

    @Override
    public Duration plus(long milliseconds) {
        return new MillisecondsDurationImpl(asMillis()+milliseconds);
    }

    @Override
    public Duration plus(Duration duration) {
        return new MillisecondsDurationImpl(asMillis()+duration.asMillis());
    }

    @Override
    public int compareTo(Duration o) {
        long diff = asMillis() - o.asMillis();
        return diff > 0l ? 1 : diff < 0l ? -1 : 0;
    }

    @Override
    public int hashCode() {
        return (int) (asMillis() & Integer.MAX_VALUE);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof Duration) {
            return compareTo((Duration) obj) == 0;
        } else {
            return false;
        }
    }

}
