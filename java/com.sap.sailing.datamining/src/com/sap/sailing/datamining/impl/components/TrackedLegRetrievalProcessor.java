package com.sap.sailing.datamining.impl.components;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.datamining.data.HasTrackedLegContext;
import com.sap.sailing.datamining.impl.data.HasTrackedLegContextImpl;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.datamining.components.FilterCriteria;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractSimpleFilteringRetrievalProcessor;

public class TrackedLegRetrievalProcessor extends
        AbstractSimpleFilteringRetrievalProcessor<TrackedRace, HasTrackedLegContext> {

    public TrackedLegRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasTrackedLegContext>> resultReceivers, FilterCriteria<HasTrackedLegContext> criteria) {
        super(executor, resultReceivers, criteria);
    }

    @Override
    protected Iterable<HasTrackedLegContext> retrieveData(TrackedRace element) {
        int legNumber = 1;
        for (TrackedLeg trackedLeg : element.getTrackedLegs()) {
            //TODO find a way to push the previous context through the process chain
            HasTrackedLegContext trackedLegWithContext = new HasTrackedLegContextImpl(trackedRaceContext, trackedLeg, legNumber);
        }
        return null;
    }

}
