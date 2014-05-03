package com.sap.sailing.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.datamining.data.HasTrackedLegContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.datamining.impl.data.TrackedLegWithContext;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sse.datamining.components.FilterCriteria;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractSimpleFilteringRetrievalProcessor;

public class TrackedLegFilteringRetrievalProcessor extends
        AbstractSimpleFilteringRetrievalProcessor<HasTrackedRaceContext, HasTrackedLegContext> {

    public TrackedLegFilteringRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasTrackedLegContext>> resultReceivers, FilterCriteria<HasTrackedLegContext> criteria) {
        super(executor, resultReceivers, criteria);
    }

    @Override
    protected Iterable<HasTrackedLegContext> retrieveData(HasTrackedRaceContext element) {
        Collection<HasTrackedLegContext> trackedLegsWithContext = new ArrayList<>();
        int legNumber = 1;
        for (TrackedLeg trackedLeg : element.getTrackedRace().getTrackedLegs()) {
            HasTrackedLegContext trackedLegWithContext = new TrackedLegWithContext(element, trackedLeg, legNumber);
            trackedLegsWithContext.add(trackedLegWithContext);
            legNumber++;
        }
        return trackedLegsWithContext;
    }

}
