package com.sap.sailing.domain.tractracadapter.impl;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.RacesHandle;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.tractrac.clientmodule.Event;
import com.tractrac.clientmodule.Race;

public class RaceHandleImpl implements RacesHandle {
    private final Event tractracEvent;
    private final DomainFactory domainFactory;
    private final DynamicTrackedRegatta trackedRegatta;
    private final RaceTracker raceTracker;
    
    public RaceHandleImpl(DomainFactory domainFactory, Event tractracEvent, DynamicTrackedRegatta trackedRegatta, RaceTracker raceTracker) {
        this.domainFactory = domainFactory;
        this.tractracEvent = tractracEvent;
        this.trackedRegatta = trackedRegatta;
        this.raceTracker = raceTracker;
    }

    @Override
    public com.sap.sailing.domain.base.Regatta getRegatta() {
        return trackedRegatta.getRegatta();
    }

    @Override
    public Set<RaceDefinition> getRaces() {
        Set<RaceDefinition> result = new HashSet<RaceDefinition>();
        for (Race r : tractracEvent.getRaceList()) {
            result.add(domainFactory.getAndWaitForRaceDefinition(r.getId()));
        }
        return result;
    }
    
    @Override
    public DynamicTrackedRegatta getTrackedRegatta() {
        return trackedRegatta;
    }

    @Override
    public RaceTracker getRaceTracker() {
        return raceTracker;
    }

    @Override
    public Set<RaceDefinition> getRaces(long timeoutInMilliseconds) {
        Set<RaceDefinition> result = new HashSet<RaceDefinition>();
        for (Race race : tractracEvent.getRaceList()) {
            final RaceDefinition raceDefinition = domainFactory.getAndWaitForRaceDefinition(race.getId(), timeoutInMilliseconds);
            if (raceDefinition != null) { // may have time-outed
                result.add(raceDefinition);
            }
        }
        return result;
    }
    
}
