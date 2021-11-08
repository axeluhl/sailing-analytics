package com.sap.sailing.racecommittee.app.domain.coursedesign;

public class WindRange implements Comparable<WindRange> {
    private final Integer lowerBound;
    private final Integer upperBound;

    public WindRange(Integer lowerBound, Integer upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public Integer getLowerBound() {
        return lowerBound;
    }

    public Integer getUpperBound() {
        return upperBound;
    }

    public boolean isInRange(Double windSpeed) {
        return (lowerBound <= windSpeed && windSpeed <= upperBound);
    }

    @Override
    public int compareTo(WindRange o) {
        if (o.getUpperBound() <= this.lowerBound)
            return 1;
        if (o.getLowerBound() >= this.upperBound)
            return -1;
        if (o.getLowerBound().equals(this.lowerBound) && o.getUpperBound().equals(this.upperBound))
            return 0;
        else
            throw new IllegalArgumentException("WindRanges are not comparable");

    }

}
