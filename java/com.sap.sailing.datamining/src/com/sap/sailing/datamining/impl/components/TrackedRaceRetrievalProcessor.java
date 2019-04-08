package com.sap.sailing.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.datamining.data.HasLeaderboardContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.datamining.impl.data.TrackedRaceWithContext;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;

public class TrackedRaceRetrievalProcessor extends AbstractRetrievalProcessor<HasLeaderboardContext, HasTrackedRaceContext> {

    public TrackedRaceRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasTrackedRaceContext, ?>> resultReceivers, int retrievalLevel,
            String retrievedDataTypeMessageKey) {
        super(HasLeaderboardContext.class, HasTrackedRaceContext.class, executor, resultReceivers, retrievalLevel,
                retrievedDataTypeMessageKey);
    }

    @Override
    protected Iterable<HasTrackedRaceContext> retrieveData(HasLeaderboardContext element) {
        Collection<HasTrackedRaceContext> trackedRacesWithContext = new ArrayList<>();
        for (RaceColumn raceColumn : element.getLeaderboard().getRaceColumns()) {
            if (isAborted()) {
                break;
            }
            for (Fleet fleet : raceColumn.getFleets()) {
                if (isAborted()) {
                    break;
                }
                TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
                if (trackedRace != null) {
                    Regatta regatta = trackedRace.getTrackedRegatta().getRegatta();
                    HasTrackedRaceContext trackedRaceWithContext = new TrackedRaceWithContext(element, regatta, raceColumn, fleet, trackedRace);
                    trackedRacesWithContext.add(trackedRaceWithContext);
                }
            }
        }
        return trackedRacesWithContext;
    }

}
