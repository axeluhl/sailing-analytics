package com.sap.sailing.domain.racelog.tracking;

import com.sap.sailing.domain.base.DomainFactory;

public interface RaceLogTrackingAdapterFactory {
    RaceLogTrackingAdapter getAdapter(DomainFactory baseDomainFactory);
}
