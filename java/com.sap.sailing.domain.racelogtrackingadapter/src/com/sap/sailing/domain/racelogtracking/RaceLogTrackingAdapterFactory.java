package com.sap.sailing.domain.racelogtracking;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.racelogtracking.impl.RaceLogTrackingAdapterFactoryImpl;

public interface RaceLogTrackingAdapterFactory {
    /**
     * A singleton instance; to be used for testing purposes only; when the containing
     * bundle has been activates, an instance can be found in the OSGi service registry.
     */
    RaceLogTrackingAdapterFactory INSTANCE = RaceLogTrackingAdapterFactoryImpl.INSTANCE;
    
    RaceLogTrackingAdapter getAdapter(DomainFactory baseDomainFactory);
}
