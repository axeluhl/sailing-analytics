package com.sap.sailing.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.datamining.data.HasWindFixContext;
import com.sap.sailing.datamining.data.HasWindTrackContext;
import com.sap.sailing.datamining.impl.data.WindFixWithContext;
import com.sap.sailing.domain.common.Wind;
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
public class WindFixRetrievalProcessor extends AbstractRetrievalProcessor<HasWindTrackContext, HasWindFixContext> {

    public WindFixRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasWindFixContext, ?>> resultReceivers, int retrievalLevel,
            String retrievedDataTypeMessageKey) {
        super(HasWindTrackContext.class, HasWindFixContext.class, executor, resultReceivers, retrievalLevel,
                retrievedDataTypeMessageKey);
    }

    @Override
    protected Iterable<HasWindFixContext> retrieveData(HasWindTrackContext element) {
        Collection<HasWindFixContext> windFixesWithContext = new ArrayList<>();
        final WindTrack windTrack = element.getWindTrack();
        windTrack.lockForRead();
        try {
            for (final Wind wind : windTrack.getFixes()) {
                if (isAborted()) {
                    break;
                }
                windFixesWithContext.add(new WindFixWithContext(element.getTrackedRaceContext(), wind, element.getWindSourceType()));
            }
        } finally {
            windTrack.unlockAfterRead();
        }
        return windFixesWithContext;
    }

}
