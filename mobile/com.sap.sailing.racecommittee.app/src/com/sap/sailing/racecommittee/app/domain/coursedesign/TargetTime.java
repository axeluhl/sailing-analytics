package com.sap.sailing.racecommittee.app.domain.coursedesign;

public enum TargetTime {
    twenty(20), thirty(30), fourty(40), fifty(50), sixty(60);

    private final Integer timeInMinutes;

    public Integer getTimeInMinutes() {
        return timeInMinutes;
    }

    private TargetTime(Integer timeInMinutes) {
        this.timeInMinutes = timeInMinutes;
    }

    @Override
    public String toString() {
        return this.timeInMinutes.toString();
    }
}
