package com.sap.sailing.polars.mining;

import com.sap.sailing.domain.common.Bearing;

public class RoundedTrueWindAngle {

    private final int angle;

    public RoundedTrueWindAngle(Bearing angleToTheWind) {
        this.angle = (int) Math.round(angleToTheWind.getDegrees());
    }

    @Override
    public String toString() {
        return "" + getAngle();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getAngle();
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
        RoundedTrueWindAngle other = (RoundedTrueWindAngle) obj;
        if (getAngle() != other.getAngle())
            return false;
        return true;
    }

    public int getAngle() {
        return angle;
    }

}
