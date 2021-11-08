package com.sap.sailing.racecommittee.app.domain.coursedesign;

public enum NumberOfRounds {
    TWO(2), THREE(3), FOUR(4), FIVE(5);

    private final Integer numberOfRounds;

    public Integer getNumberOfRounds() {
        return numberOfRounds;
    }

    private NumberOfRounds(Integer numberOfRounds) {
        this.numberOfRounds = numberOfRounds;
    }

    @Override
    public String toString() {
        return this.numberOfRounds.toString();
    }
}
