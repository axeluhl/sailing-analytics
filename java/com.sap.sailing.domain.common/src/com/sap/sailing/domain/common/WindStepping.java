package com.sap.sailing.domain.common;

import java.io.Serializable;

public interface WindStepping extends Serializable{

    public abstract int getLevelIndexForValue(double speed);

    public abstract int getSteppedValueForValue(double speed);

    Integer[] getRawStepping();

}