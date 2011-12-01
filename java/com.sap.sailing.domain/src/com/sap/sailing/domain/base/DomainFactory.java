package com.sap.sailing.domain.base;

import com.sap.sailing.domain.base.impl.DomainFactoryImpl;

public interface DomainFactory {
    static DomainFactory INSTANCE = new DomainFactoryImpl();

    /**
     * Looks up or, if not found, creates a {@link Nationality} object and re-uses <code>threeLetterIOCCode</code> also as the
     * nationality's name.
     */
    Nationality getOrCreateNationality(String nationalityName);

    Buoy getOrCreateBuoy(String id);

    Gate createGate(Buoy left, Buoy right, String name);
    
    Waypoint createWaypoint(ControlPoint controlPoint);
}
