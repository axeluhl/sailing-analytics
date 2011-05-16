package com.sap.sailing.domain.tractracadapter.impl;

import java.util.ArrayList;
import java.util.List;

import com.maptrack.client.io.TypeController;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tractracadapter.DomainFactory;
import com.tractrac.clientmodule.Race;
import com.tractrac.clientmodule.RaceCompetitor;
import com.tractrac.clientmodule.data.CompetitorPositionRawData;
import com.tractrac.clientmodule.data.ICallbackData;

public class RawPositionReceiver {
    private final TrackedEvent trackedEvent;
    private final com.tractrac.clientmodule.Event tractracEvent;
    
    public RawPositionReceiver(TrackedEvent trackedEvent, com.tractrac.clientmodule.Event tractracEvent) {
        super();
        this.trackedEvent = trackedEvent;
        this.tractracEvent = tractracEvent;
    }
    
    public Iterable<TypeController> getRawPositionListeners() {
        final DomainFactory domainFactory = DomainFactory.INSTANCE;
        ICallbackData<RaceCompetitor, CompetitorPositionRawData> positionListener = new ICallbackData<RaceCompetitor, CompetitorPositionRawData>() {
            public void gotData(RaceCompetitor tracked,
                    CompetitorPositionRawData record) {
                Race race = tracked.getRace();
                RaceDefinition raceDefinition = domainFactory.getRaceDefinition(race);
                DynamicTrackedRace trackedRace = (DynamicTrackedRace) trackedEvent.getTrackedRace(raceDefinition);
                GPSFixMoving fix = domainFactory.createGPSFixMoving(record);
                Competitor competitor = domainFactory.getCompetitor(tracked.getCompetitor());
                trackedRace.recordFix(competitor, fix);
            }
        };
        List<TypeController> listeners = new ArrayList<TypeController>();
        for (Race race : tractracEvent.getRaceList()) {
            TypeController listener = CompetitorPositionRawData.subscribe(race,
                positionListener, /* fromTime */0 /* means ALL */);
            listeners.add(listener);
        }
        return listeners;
    }
}
