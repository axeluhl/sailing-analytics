package com.sap.sailing.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.datamining.data.HasManeuverContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.datamining.impl.data.ManeuverWithContext;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sse.common.TimePoint;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;

public class ManeuverRetrievalProcessor extends AbstractRetrievalProcessor<HasTrackedLegOfCompetitorContext, HasManeuverContext> {

    public ManeuverRetrievalProcessor(ExecutorService executor, Collection<Processor<HasManeuverContext, ?>> resultReceivers, int retrievalLevel) {
        super(HasTrackedLegOfCompetitorContext.class, HasManeuverContext.class, executor, resultReceivers, retrievalLevel);
    }

    @Override
    protected Iterable<HasManeuverContext> retrieveData(HasTrackedLegOfCompetitorContext element) {
        Collection<HasManeuverContext> maneuversWithContext = new ArrayList<>();
        TimePoint finishTime = element.getTrackedLegOfCompetitor().getFinishTime();
        if (finishTime != null) {
            try {
                List<Maneuver> maneuvers = element.getTrackedLegOfCompetitor().getManeuvers(finishTime, false);
                for (Maneuver maneuver : maneuvers) {
                    maneuversWithContext.add(new ManeuverWithContext(element, maneuver));
                }
            } catch (NoWindException e) {
                throw new IllegalStateException("No wind retrieving the maneuvers", e);
            }
        }
        return maneuversWithContext;
    }

}
