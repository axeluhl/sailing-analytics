package com.sap.sailing.domain.yellowbrickadapter.impl;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.yellowbrickadapter.YellowBrickTrackingAdapter;
import com.sap.sailing.domain.yellowbrickadapter.YellowBrickTrackingAdapterFactory;

public class YellowBrickTrackingAdapterFactoryImpl implements YellowBrickTrackingAdapterFactory {
    @Override
    public YellowBrickTrackingAdapter getYellowBrickTrackingAdapter(DomainFactory domainFactory) {
        return new YellowBrickTrackingAdapterImpl(domainFactory);
    }
}
