package com.sap.sailing.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.datamining.data.HasManeuverBoundariesContext;
import com.sap.sailing.datamining.data.HasRaceOfCompetitorContext;
import com.sap.sailing.datamining.impl.data.ManeuverBoundariesWithContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.maneuverdetection.ManeuverWithEstimationData;
import com.sap.sailing.domain.maneuverdetection.ManeuverWithEstimationDataCalculator;
import com.sap.sailing.domain.maneuverdetection.impl.ManeuverWithEstimationDataCalculatorImpl;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverBoundariesRetrievalProcessor
        extends AbstractRetrievalProcessor<HasRaceOfCompetitorContext, HasManeuverBoundariesContext> {

    public ManeuverBoundariesRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasManeuverBoundariesContext, ?>> resultReceivers, int retrievalLevel) {
        super(HasRaceOfCompetitorContext.class, HasManeuverBoundariesContext.class, executor, resultReceivers,
                retrievalLevel);
    }

    @Override
    protected Iterable<HasManeuverBoundariesContext> retrieveData(HasRaceOfCompetitorContext element) {
        List<HasManeuverBoundariesContext> result = new ArrayList<>();
        TrackedRace trackedRace = element.getTrackedRaceContext().getTrackedRace();
        Competitor competitor = element.getCompetitor();
        ManeuverWithEstimationDataCalculator calculator = new ManeuverWithEstimationDataCalculatorImpl();
        Iterable<Maneuver> maneuvers = trackedRace.getManeuvers(competitor, false);
        Iterable<ManeuverWithEstimationData> maneuversWithEstimationData = calculator
                .computeEstimationDataForManeuvers(trackedRace, competitor, maneuvers);
        for (ManeuverWithEstimationData maneuverWithEstimationData : maneuversWithEstimationData) {
            result.add(new ManeuverBoundariesWithContext(element, maneuverWithEstimationData));
        }
        return result;
    }

}
