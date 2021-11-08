package com.sap.sailing.domain.yellowbrickadapter.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.yellowbrickadapter.YellowBrickTrackingAdapter;
import com.sap.sailing.domain.yellowbrickadapter.YellowBrickTrackingAdapterFactory;

public class YellowBrickTrackingAdapterFactoryImpl implements YellowBrickTrackingAdapterFactory {
    private final ConcurrentMap<DomainFactory, YellowBrickTrackingAdapter> adapters;
    
    public YellowBrickTrackingAdapterFactoryImpl() {
        super();
        this.adapters = new ConcurrentHashMap<>();
    }

    @Override
    public YellowBrickTrackingAdapter getYellowBrickTrackingAdapter(DomainFactory baseDomainFactory) {
        return adapters.computeIfAbsent(baseDomainFactory, YellowBrickTrackingAdapterImpl::new);
    }
}
