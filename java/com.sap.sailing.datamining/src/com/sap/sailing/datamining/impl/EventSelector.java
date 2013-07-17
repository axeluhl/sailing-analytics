package com.sap.sailing.datamining.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.datamining.GPSFixWithContext;
import com.sap.sailing.datamining.Selector;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;

public class EventSelector implements Selector {
    
    private transient RacingEventService racingEventService;
    private String[] eventNamesForSelection;

    public EventSelector(String... eventNames) {
        eventNamesForSelection = eventNames;
    }
    
    @Override
    public List<GPSFixWithContext> selectGPSFixes(RacingEventService racingEventService) {
        // TODO Auto-generated method stub
        return null;
    }

    private List<GPSFixMoving> getDataOf(Regatta regatta) {
        List<GPSFixMoving> data = new ArrayList<GPSFixMoving>();
        for (RaceDefinition race : regatta.getAllRaces()) {
            RegattaAndRaceIdentifier raceIdentifier = new RegattaNameAndRaceName(regatta.getName(), race.getName());
            TrackedRace trackedRace = racingEventService.getTrackedRace(raceIdentifier);
            for (Competitor competitor : regatta.getCompetitors()) {
                GPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(competitor);
                for (GPSFixMoving gpsFix : track.getFixes()) {
                    data.add(gpsFix);
                }
            }
        }
        return data;
    }

}
