package com.sap.sailing.domain.racelogtracking;

import com.sap.sailing.domain.base.DomainFactory;

public interface RaceLogTrackingAdapterFactory {
    RaceLogTrackingAdapter getAdapter(DomainFactory baseDomainFactory);
}
