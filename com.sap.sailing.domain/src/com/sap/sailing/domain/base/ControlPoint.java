package com.sap.sailing.domain.base;

public interface ControlPoint extends Named {
    Iterable<Buoy> getBuoys();
}
