package com.sap.sailing.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.datamining.data.HasCompleteManeuverCurveWithEstimationDataContext;
import com.sap.sailing.datamining.data.HasRaceOfCompetitorContext;
import com.sap.sailing.datamining.impl.data.CompleteManeuverCurveWithEstimationDataWithContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.maneuverdetection.ManeuverDetector;
import com.sap.sailing.domain.maneuverdetection.impl.ManeuverDetectorImpl;
import com.sap.sailing.domain.tracking.CompleteManeuverCurve;
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
        extends AbstractRetrievalProcessor<HasRaceOfCompetitorContext, HasCompleteManeuverCurveWithEstimationDataContext> {

    public ManeuverBoundariesRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasCompleteManeuverCurveWithEstimationDataContext, ?>> resultReceivers, int retrievalLevel) {
        super(HasRaceOfCompetitorContext.class, HasCompleteManeuverCurveWithEstimationDataContext.class, executor, resultReceivers,
                retrievalLevel);
    }

    @Override
    protected Iterable<HasCompleteManeuverCurveWithEstimationDataContext> retrieveData(HasRaceOfCompetitorContext element) {
        List<HasCompleteManeuverCurveWithEstimationDataContext> result = new ArrayList<>();
        TrackedRace trackedRace = element.getTrackedRaceContext().getTrackedRace();
        Competitor competitor = element.getCompetitor();
        ManeuverDetector maneuverDetector = new ManeuverDetectorImpl(trackedRace, competitor);
        Iterable<Maneuver> maneuvers = trackedRace.getManeuvers(competitor, false);
        Iterable<CompleteManeuverCurve> maneuverCurves = maneuverDetector.getCompleteManeuverCurves(maneuvers);
        Iterable<CompleteManeuverCurveWithEstimationData> maneuversWithEstimationData = maneuverDetector
                .getCompleteManeuverCurvesWithEstimationData(maneuverCurves);
        for (CompleteManeuverCurveWithEstimationData maneuverWithEstimationData : maneuversWithEstimationData) {
            result.add(new CompleteManeuverCurveWithEstimationDataWithContext(element, maneuverWithEstimationData));
        }
        return result;
    }

}
