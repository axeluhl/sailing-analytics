package com.sap.sailing.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.datamining.impl.data.HasTrackedRaceContextImpl;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.datamining.components.FilterCriteria;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractSimpleFilteringRetrievalProcessor;

public class TrackedRaceRetrievalProcessor extends
        AbstractSimpleFilteringRetrievalProcessor<Leaderboard, HasTrackedRaceContext> {

    public TrackedRaceRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasTrackedRaceContext>> resultReceivers, FilterCriteria<HasTrackedRaceContext> criteria) {
        super(executor, resultReceivers, criteria);
    }

    @Override
    protected Iterable<HasTrackedRaceContext> retrieveData(Leaderboard element) {
        Collection<HasTrackedRaceContext> trackedRacesWithContext = new ArrayList<>();
        for (RaceColumn raceColumn : element.getRaceColumns()) {
            for (Fleet fleet : raceColumn.getFleets()) {
                TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
                if (trackedRace != null) {
                    Regatta regatta = trackedRace.getTrackedRegatta().getRegatta();
                    Event event = regatta.getEvent();
                    HasTrackedRaceContext trackedRaceWithContext = new HasTrackedRaceContextImpl(event, regatta, fleet, trackedRace);
                    trackedRacesWithContext.add(trackedRaceWithContext);
                }
            }
        }
        return trackedRacesWithContext;
    }

}
