package com.sap.sailing.domain.tractracadapter.impl;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tracking.RaceHandle;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.TracTracRaceTracker;
import com.tractrac.clientmodule.Event;

public class RaceHandleImpl implements RaceHandle {
    private final Event tractracEvent;
    private final DomainFactory domainFactory;
    private final DynamicTrackedEvent trackedEvent;
    private final TracTracRaceTracker raceTracker;
    
    public RaceHandleImpl(DomainFactory domainFactory, Event tractracEvent, DynamicTrackedEvent trackedEvent, TracTracRaceTracker raceTracker) {
        this.domainFactory = domainFactory;
        this.tractracEvent = tractracEvent;
        this.trackedEvent = trackedEvent;
        this.raceTracker = raceTracker;
    }

    @Override
    public com.sap.sailing.domain.base.Event getEvent() {
        return domainFactory.getOrCreateEvent(tractracEvent);
    }

    @Override
    public RaceDefinition getRace() {
        // FIXME we assume there is exactly one Race per TracTrac event but during match racing there may be many
        return domainFactory.getAndWaitForRaceDefinition(tractracEvent.getRaceList().iterator().next());
    }
    
    @Override
    public DynamicTrackedEvent getTrackedEvent() {
        return trackedEvent;
    }

    @Override
    public TracTracRaceTracker getRaceTracker() {
        return raceTracker;
    }

    @Override
    public RaceDefinition getRace(long timeoutInMilliseconds) {
        // FIXME we assume there is exactly one Race per TracTrac event but during match racing there may be many
        return domainFactory.getAndWaitForRaceDefinition(tractracEvent.getRaceList().iterator().next(), timeoutInMilliseconds);
    }
    
}
