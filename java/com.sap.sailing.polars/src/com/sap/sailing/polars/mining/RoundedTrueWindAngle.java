package com.sap.sailing.polars.mining;

import com.sap.sailing.domain.common.Bearing;

public class RoundedTrueWindAngle {

    private final int angleDeg;

    public RoundedTrueWindAngle(Bearing angleToTheWind) {
        this.angleDeg = (int) Math.round(angleToTheWind.getDegrees());
    }

    @Override
    public String toString() {
        return "" + getAngleDeg();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getAngleDeg();
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
        if (getAngleDeg() != other.getAngleDeg())
            return false;
        return true;
    }

    public int getAngleDeg() {
        return angleDeg;
    }

}
