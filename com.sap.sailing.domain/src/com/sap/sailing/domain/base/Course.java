package com.sap.sailing.domain.base;

public interface Course extends Named {
    Iterable<Leg> getLegs();
}
