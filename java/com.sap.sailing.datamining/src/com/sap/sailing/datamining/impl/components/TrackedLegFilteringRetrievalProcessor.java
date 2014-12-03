package com.sap.sailing.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.datamining.Activator;
import com.sap.sailing.datamining.data.HasTrackedLegContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.datamining.impl.data.TrackedLegWithContext;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractSimpleRetrievalProcessor;
import com.sap.sse.datamining.shared.annotations.DataRetriever;

@DataRetriever(dataType=HasTrackedLegContext.class,
               groupName=Activator.dataRetrieverGroupName,
               level=3)
public class TrackedLegFilteringRetrievalProcessor extends
        AbstractSimpleRetrievalProcessor<HasTrackedRaceContext, HasTrackedLegContext> {

    public TrackedLegFilteringRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasTrackedLegContext, ?>> resultReceivers) {
        super(HasTrackedRaceContext.class, HasTrackedLegContext.class, executor, resultReceivers);
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
