package com.sap.sailing.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.datamining.data.HasWindFixContext;
import com.sap.sailing.datamining.impl.data.WindFixWithContext;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;

/**
 * Retrieves all wind fixes from a {@link TrackedRace} that are provided by wind sources that are not currently
 * {@link TrackedRace#getWindSourcesToExclude() excluded}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class WindFixRetrievalProcessor extends AbstractRetrievalProcessor<HasTrackedRaceContext, HasWindFixContext> {

    public WindFixRetrievalProcessor(ExecutorService executor, Collection<Processor<HasWindFixContext, ?>> resultReceivers, int retrievalLevel) {
        super(HasTrackedRaceContext.class, HasWindFixContext.class, executor, resultReceivers, retrievalLevel);
    }

    @Override
    protected Iterable<HasWindFixContext> retrieveData(HasTrackedRaceContext element) {
        Collection<HasWindFixContext> windFixesWithContext = new ArrayList<>();
        final TrackedRace trackedRace = element.getTrackedRace();
        for (final WindSource windSource : trackedRace.getWindSources()) {
            if (!trackedRace.getWindSourcesToExclude().contains(windSource)) {
                final WindTrack windTrack = trackedRace.getOrCreateWindTrack(windSource);
                windTrack.lockForRead();
                try {
                    for (final Wind wind : windTrack.getFixes()) {
                        windFixesWithContext.add(new WindFixWithContext(element, wind, windSource.getType()));
                    }
                } finally {
                    windTrack.unlockAfterRead();
                }
            }
        }
        return windFixesWithContext;
    }

}
