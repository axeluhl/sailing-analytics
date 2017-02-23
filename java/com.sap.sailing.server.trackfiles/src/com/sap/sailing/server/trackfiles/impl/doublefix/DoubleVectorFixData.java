package com.sap.sailing.server.trackfiles.impl.doublefix;

public final class DoubleVectorFixData {
    private long timepointInMs;
    private double[] fix;

    public DoubleVectorFixData(long timepoint, double[] fix) {
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

    public double[] getFix() {
        return fix;
    }
}