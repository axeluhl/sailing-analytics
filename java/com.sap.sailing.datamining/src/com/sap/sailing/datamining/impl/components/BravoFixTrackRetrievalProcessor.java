package com.sap.sailing.datamining.impl.components;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.datamining.data.HasBravoFixTrackContext;
import com.sap.sailing.datamining.data.HasRaceOfCompetitorContext;
import com.sap.sailing.datamining.impl.data.BravoFixTrackWithContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.BravoFixTrack;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;

/**
 * Retrieves all wind fixes from a {@link TrackedRace} that are provided by wind sources that are not currently
 * {@link TrackedRace#getWindSourcesToExclude() excluded}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class BravoFixTrackRetrievalProcessor extends AbstractRetrievalProcessor<HasRaceOfCompetitorContext, HasBravoFixTrackContext> {

    public BravoFixTrackRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasBravoFixTrackContext, ?>> resultReceivers, int retrievalLevel,
            String retrievedDataTypeMessageKey) {
        super(HasRaceOfCompetitorContext.class, HasBravoFixTrackContext.class, executor, resultReceivers,
                retrievalLevel, retrievedDataTypeMessageKey);
    }

    @Override
    protected Iterable<HasBravoFixTrackContext> retrieveData(HasRaceOfCompetitorContext element) {
        final TrackedRace trackedRace = element.getTrackedRaceContext().getTrackedRace();
        final BravoFixTrack<Competitor> bravoFixTrack = trackedRace.getSensorTrack(element.getCompetitor(), BravoFixTrack.TRACK_NAME);
        return bravoFixTrack == null ? Collections.emptySet() : Collections.singleton(new BravoFixTrackWithContext(element, bravoFixTrack));
    }

}
