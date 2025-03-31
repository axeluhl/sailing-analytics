package com.sap.sailing.server.trackfiles.impl.doublefix;

import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public final class DoubleVectorFixData {
    private long timepointInMs;
    private Double[] fix;

    public DoubleVectorFixData(long timepoint, Double[] fix) {
        super();
        this.timepointInMs = timepoint;
        this.fix = fix;
    }

    public void correctTimepointBy(long offset) {
        timepointInMs += offset;
    }

    public long getTimepointInMs() {
        return timepointInMs;
    }

    public long getFixSecond() {
        return timepointInMs / 1000;
    }

    public Double[] getFix() {
        return fix;
    }

    public TimePoint getTimepoint() {
        return new MillisecondsTimePoint(timepointInMs);
    }
}