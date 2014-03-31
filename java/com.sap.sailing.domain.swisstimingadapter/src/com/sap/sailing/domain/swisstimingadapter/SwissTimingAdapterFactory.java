package com.sap.sailing.domain.swisstimingadapter;

import com.sap.sailing.domain.base.DomainFactory;

public interface SwissTimingAdapterFactory {
    SwissTimingAdapter getOrCreateSwissTimingAdapter(DomainFactory baseDomainFactory);
}
