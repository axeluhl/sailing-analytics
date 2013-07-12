package com.sap.sailing.datamining.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;

public class EventSelector extends AbstractSelector {
    private static final long serialVersionUID = 8527294635220237370L;
    
    private transient Set<Regatta> regattas;
    private transient RacingEventService racingEventService;
    
    private String[] eventNamesForSelection;

    public EventSelector(String... eventNames) {
        eventNamesForSelection = eventNames;
    }

    @Override
    protected void initializeSelection(RacingEventService racingEventService) {
        this.racingEventService = racingEventService;
        for (Event event : racingEventService.getAllEvents()) {
            for (String eventNameForSelection : eventNamesForSelection) {
                if (eventNameForSelection.equals(event.getName())) {
                    for (Regatta regatta : event.getRegattas()) {
                        regattas.add(regatta);
                    }
                    break;
                }
            }
        }
    }

    @Override
    public List<String> getXValues() {
        List<String> xValues = new ArrayList<>();
        for (Regatta regatta : regattas) {
            xValues.add(regatta.getName());
        }
        return xValues;
    }

    @Override
    public List<GPSFixMoving> getDataFor(String xValue) {
        for (Regatta regatta : regattas) {
            if (xValue.equals(regatta.getName())) {
                return getDataOf(regatta);
            }
        }
        return new ArrayList<>();
    }

    private List<GPSFixMoving> getDataOf(Regatta regatta) {
        List<GPSFixMoving> data = new ArrayList<>();
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
