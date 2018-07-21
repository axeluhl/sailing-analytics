package com.sap.sailing.domain.swisstimingreplayadapter.impl;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.swisstimingadapter.DomainFactory;
import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayService;
import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayServiceFactory;

public class SwissTimingReplayServiceFactoryImpl implements SwissTimingReplayServiceFactory {
    private final Map<DomainFactory, SwissTimingReplayService> servicesForDomainFactories;
    
    public SwissTimingReplayServiceFactoryImpl() {
        servicesForDomainFactories = new HashMap<>();
    }

    @Override
    public SwissTimingReplayService createSwissTimingReplayService(DomainFactory domainFactory, RaceLogResolver raceLogResolver) {
        SwissTimingReplayService result = servicesForDomainFactories.get(domainFactory);
        if (result == null) {
            result = new SwissTimingReplayServiceImpl(domainFactory);
            servicesForDomainFactories.put(domainFactory, result);
        }
        return result;
    }
}
