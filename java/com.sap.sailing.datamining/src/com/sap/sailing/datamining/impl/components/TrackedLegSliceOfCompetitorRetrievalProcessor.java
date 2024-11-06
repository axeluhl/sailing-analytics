package com.sap.sailing.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.datamining.data.HasTrackedLegSliceOfCompetitorContext;
import com.sap.sailing.datamining.impl.data.TrackedLegSliceOfCompetitorWithContext;
import com.sap.sailing.datamining.shared.TackTypeSegmentsDataMiningSettings;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;

/**
 * Produces "slices" of a {@link HasTrackedLegOfCompetitorContext} object, splitting up the leg in multiple consecutive parts. The parts
 * are numbered. Slicing currently happens based on wind-based distance traveled in the leg, into ten slices. Future extensions, based on
 * settings, may offer more sophisticated ways of slicing. See also bug 6064. 
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class TrackedLegSliceOfCompetitorRetrievalProcessor extends AbstractRetrievalProcessor<HasTrackedLegOfCompetitorContext, HasTrackedLegSliceOfCompetitorContext> {
    /**
     * Settings will be used to control the retrieval of tack type segments for the
     * corresponding statistics such as distance/duration relation between long and
     * short tack for the leg of the competitor
     */
    private final TackTypeSegmentsDataMiningSettings settings;

    public TrackedLegSliceOfCompetitorRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasTrackedLegSliceOfCompetitorContext, ?>> resultReceivers,
            TackTypeSegmentsDataMiningSettings settings, int retrievalLevel, String retrievedDataTypeMessageKey) {
        super(HasTrackedLegOfCompetitorContext.class, HasTrackedLegSliceOfCompetitorContext.class, executor, resultReceivers,
                retrievalLevel, retrievedDataTypeMessageKey);
        this.settings = settings;
    }

    @Override
    protected Iterable<HasTrackedLegSliceOfCompetitorContext> retrieveData(HasTrackedLegOfCompetitorContext element) {
        Collection<HasTrackedLegSliceOfCompetitorContext> trackedLegSlicesOfCompetitorsWithContext = new ArrayList<>();
        for (int i=1; i<=10; i++) {
            if (isAborted()) {
                break;
            }
            HasTrackedLegSliceOfCompetitorContext trackedLegOfCompetitorWithContext = new TrackedLegSliceOfCompetitorWithContext(element.getTrackedLegContext(), element.getTrackedLegOfCompetitor(), settings, i);
            trackedLegSlicesOfCompetitorsWithContext.add(trackedLegOfCompetitorWithContext);
        }
        return trackedLegSlicesOfCompetitorsWithContext;
    }

}
