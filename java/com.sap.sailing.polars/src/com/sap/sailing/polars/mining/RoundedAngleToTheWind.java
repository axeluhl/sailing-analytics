package com.sap.sailing.polars.mining;

public class RoundedAngleToTheWind {

    private final int angle;

    public RoundedAngleToTheWind(int angle) {
        this.angle = angle;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + angle;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RoundedAngleToTheWind other = (RoundedAngleToTheWind) obj;
        if (angle != other.angle)
            return false;
        return true;
    }

}
