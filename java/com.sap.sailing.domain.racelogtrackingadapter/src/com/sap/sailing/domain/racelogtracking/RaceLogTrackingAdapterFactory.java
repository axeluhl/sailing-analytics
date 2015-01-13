package com.sap.sailing.domain.racelogtracking;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.racelogtracking.impl.RaceLogTrackingAdapterFactoryImpl;

public interface RaceLogTrackingAdapterFactory {
    RaceLogTrackingAdapterFactory INSTANCE = RaceLogTrackingAdapterFactoryImpl.INSTANCE;
    RaceLogTrackingAdapter getAdapter(DomainFactory baseDomainFactory);
}
