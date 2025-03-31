package com.sap.sailing.domain.swisstimingreplayadapter;

import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.swisstimingadapter.DomainFactory;
import com.sap.sailing.domain.swisstimingreplayadapter.impl.SwissTimingReplayServiceFactoryImpl;

public interface SwissTimingReplayServiceFactory {
    static SwissTimingReplayServiceFactory INSTANCE = new SwissTimingReplayServiceFactoryImpl();
    
    SwissTimingReplayService createSwissTimingReplayService(DomainFactory domainFactory, RaceLogResolver raceLogResolver);
}
