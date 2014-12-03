package com.sap.sailing.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.datamining.impl.data.TrackedRaceWithContext;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractSimpleRetrievalProcessor;
import com.sap.sse.datamining.shared.annotations.DataRetriever;

@DataRetriever(dataType=HasTrackedRaceContext.class,
               groupName="Sailing",
               level=2)
public class TrackedRaceFilteringRetrievalProcessor extends
        AbstractSimpleRetrievalProcessor<Leaderboard, HasTrackedRaceContext> {

    public TrackedRaceFilteringRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasTrackedRaceContext, ?>> resultReceivers) {
        super(Leaderboard.class, HasTrackedRaceContext.class, executor, resultReceivers);
    }

    @Override
    protected Iterable<HasTrackedRaceContext> retrieveData(Leaderboard element) {
        Collection<HasTrackedRaceContext> trackedRacesWithContext = new ArrayList<>();
        for (RaceColumn raceColumn : element.getRaceColumns()) {
            for (Fleet fleet : raceColumn.getFleets()) {
                TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
                if (trackedRace != null) {
                    Regatta regatta = trackedRace.getTrackedRegatta().getRegatta();
                    HasTrackedRaceContext trackedRaceWithContext = new TrackedRaceWithContext(regatta, fleet, trackedRace);
                    trackedRacesWithContext.add(trackedRaceWithContext);
                }
            }
        }
        return trackedRacesWithContext;
    }

}
