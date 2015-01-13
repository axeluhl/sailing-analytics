package com.sap.sailing.domain.common;

import java.io.Serializable;

public interface WindSpeedStepping extends Serializable {

    public abstract int getLevelIndexForValue(double speed);

    public abstract Double getSteppedValueForValue(double speed);

    double[] getRawStepping();

    public abstract int getLevelIndexFloorForValue(double speed);

    public abstract int getLevelIndexCeilingForValue(double speed);

    double getDistanceToLevelFloor(double speed);

    int hashCode();

    boolean equals(Object obj);

}