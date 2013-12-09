package com.sap.sailing.domain.igtimiadapter.impl;

import java.util.WeakHashMap;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.WindTracker;
import com.sap.sailing.domain.tracking.WindTrackerFactory;

public class IgtimiWindTrackerFactory implements WindTrackerFactory {
    private final IgtimiConnectionFactoryImpl connectionFactory;
    private final WeakHashMap<RaceDefinition, WindTracker> trackersForRace;
    
    public IgtimiWindTrackerFactory(IgtimiConnectionFactoryImpl connectionFactory) {
        trackersForRace = new WeakHashMap<>();
        this.connectionFactory = connectionFactory;
    }

    @Override
    public WindTracker createWindTracker(DynamicTrackedRegatta trackedRegatta, RaceDefinition race,
            boolean correctByDeclination) throws Exception {
        DynamicTrackedRace trackedRace = trackedRegatta.getTrackedRace(race);
        IgtimiWindTracker windTracker = new IgtimiWindTracker(trackedRace, connectionFactory);
        trackersForRace.put(race, windTracker);
        return windTracker;
    }

    @Override
    public WindTracker getExistingWindTracker(RaceDefinition race) {
        return trackersForRace.get(race);
    }

}
