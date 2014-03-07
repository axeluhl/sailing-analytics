package com.sap.sailing.domain.racelogtracking.impl;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapter;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapterFactory;

public enum RaceLogTrackingAdapterFactoryImpl implements RaceLogTrackingAdapterFactory {
    INSTANCE;
    private RaceLogTrackingAdapter adapter;

    @Override
    public RaceLogTrackingAdapter getAdapter(DomainFactory baseDomainFactory) {
        if (adapter == null) {
            adapter = new RaceLogTrackingAdapterImpl(baseDomainFactory);
        }
        return adapter;
    }

}
