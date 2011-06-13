package com.sap.sailing.domain.tractracadapter.impl;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.sap.sailing.domain.tractracadapter.RaceHandle;
import com.sap.sailing.domain.tractracadapter.RaceTracker;
import com.tractrac.clientmodule.Event;

public class RaceHandleImpl implements RaceHandle {
    private final Event tractracEvent;
    private final DomainFactory domainFactory;
    private final DynamicTrackedEvent trackedEvent;
    private final RaceTracker raceTracker;
    
    public RaceHandleImpl(DomainFactory domainFactory, Event tractracEvent, DynamicTrackedEvent trackedEvent, RaceTracker raceTracker) {
        this.domainFactory = domainFactory;
        this.tractracEvent = tractracEvent;
        this.trackedEvent = trackedEvent;
        this.raceTracker = raceTracker;
    }

    @Override
    public com.sap.sailing.domain.base.Event getEvent() {
        return domainFactory.createEvent(tractracEvent);
    }

    @Override
    public RaceDefinition getRace() {
        // we assume there is exactly one Race per TracTrac event
        return domainFactory.getRaceDefinition(tractracEvent.getRaceList().iterator().next());
    }
    
    @Override
    public DynamicTrackedEvent getTrackedEvent() {
        return trackedEvent;
    }

    @Override
    public RaceTracker getRaceTracker() {
        return raceTracker;
    }
    
}
