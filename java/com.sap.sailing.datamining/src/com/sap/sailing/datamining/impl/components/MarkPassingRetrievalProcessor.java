package com.sap.sailing.datamining.impl.components;

import java.util.Collection;
import java.util.NavigableSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import java.util.HashSet;
import com.sap.sailing.datamining.data.HasMarkPassingContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.datamining.impl.data.MarkPassingWithContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractSimpleRetrievalProcessor;

public class MarkPassingRetrievalProcessor extends
        AbstractSimpleRetrievalProcessor<HasTrackedRaceContext, HasMarkPassingContext> {

    public MarkPassingRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasMarkPassingContext, ?>> resultReceivers) {
        super(HasTrackedRaceContext.class, HasMarkPassingContext.class, executor, resultReceivers);
    }

    @Override
    protected Iterable<HasMarkPassingContext> retrieveData(HasTrackedRaceContext element) {
        TrackedRace trackedRace = element.getTrackedRace();
        Set<HasMarkPassingContext> result = new HashSet<>();
        trackedRace.getRace().getCourse().lockForRead();
        try {
            for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
                NavigableSet<MarkPassing> markPassingsForCompetitor = trackedRace.getMarkPassings(competitor);
                trackedRace.lockForRead(markPassingsForCompetitor);
                try {
                    markPassingsForCompetitor.stream().forEach(mp->result.add(new MarkPassingWithContext(element, mp)));
                } finally {
                    trackedRace.unlockAfterRead(markPassingsForCompetitor);
                }
            }
        } finally {
            trackedRace.getRace().getCourse().unlockAfterRead();
        }
        return result;
    }

}
