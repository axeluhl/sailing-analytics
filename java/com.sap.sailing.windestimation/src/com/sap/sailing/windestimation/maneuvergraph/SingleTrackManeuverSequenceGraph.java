package com.sap.sailing.windestimation.maneuvergraph;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.polars.PolarDataService;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class SingleTrackManeuverSequenceGraph
        extends ManeuverSequenceGraph<SingleTrackManeuverNodesLevel, SingleManeuverClassificationResult> {

    public SingleTrackManeuverSequenceGraph(BoatClass boatClass, PolarDataService polarService,
            Iterable<CompleteManeuverCurveWithEstimationData> maneuverSequence) {
        super(getClassificationResults(boatClass, polarService, maneuverSequence),
                SingleTrackManeuverNodesLevel.getFactory());
    }

    private static List<SingleManeuverClassificationResult> getClassificationResults(BoatClass boatClass,
            PolarDataService polarService, Iterable<CompleteManeuverCurveWithEstimationData> maneuverSequence) {
        SingleManeuverClassifier singleManeuverClassifier = new RulesBasedSingleManeuverClassifierImpl(boatClass,
                polarService);
        List<SingleManeuverClassificationResult> result = new ArrayList<>();
        for (CompleteManeuverCurveWithEstimationData maneuver : maneuverSequence) {
            SingleManeuverClassificationResult classificationResult = singleManeuverClassifier
                    .classifyManeuver(maneuver);
            result.add(classificationResult);
        }
        return result;
    }

}
