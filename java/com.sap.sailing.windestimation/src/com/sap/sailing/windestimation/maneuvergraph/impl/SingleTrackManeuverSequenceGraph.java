package com.sap.sailing.windestimation.maneuvergraph.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.windestimation.maneuvergraph.impl.bestpath.BestPathsCalculator;
import com.sap.sailing.windestimation.maneuvergraph.impl.bestpath.SameBoatClassBestPathsEvaluator;
import com.sap.sailing.windestimation.maneuvergraph.impl.classifier.RulesBasedSingleManeuverClassifierImpl;
import com.sap.sailing.windestimation.maneuvergraph.impl.classifier.SingleManeuverClassificationResult;
import com.sap.sailing.windestimation.maneuvergraph.impl.classifier.SingleManeuverClassifier;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class SingleTrackManeuverSequenceGraph
        extends AbstractManeuverSequenceGraphImpl<SingleTrackManeuverNodesLevel, SingleManeuverClassificationResult> {

    public SingleTrackManeuverSequenceGraph(BoatClass boatClass, PolarDataService polarService,
            Iterable<CompleteManeuverCurveWithEstimationData> maneuverSequence) {
        super(getClassificationResults(boatClass, polarService, maneuverSequence),
                SingleTrackManeuverNodesLevel.getFactory(boatClass), polarService, new BestPathsCalculator<>(),
                new SameBoatClassBestPathsEvaluator<>());
    }

    private static List<SingleManeuverClassificationResult> getClassificationResults(BoatClass boatClass,
            PolarDataService polarService, Iterable<CompleteManeuverCurveWithEstimationData> maneuverSequence) {
        SingleManeuverClassifier singleManeuverClassifier = new RulesBasedSingleManeuverClassifierImpl(boatClass,
                polarService);
        List<SingleManeuverClassificationResult> result = new ArrayList<>();
        Iterator<CompleteManeuverCurveWithEstimationData> iterator = maneuverSequence.iterator();
        CompleteManeuverCurveWithEstimationData previousManeuver = null;
        CompleteManeuverCurveWithEstimationData currentManeuver = iterator.hasNext() ? iterator.next() : null;
        while (currentManeuver != null) {
            CompleteManeuverCurveWithEstimationData nextManeuver = iterator.hasNext() ? iterator.next() : null;
            SingleManeuverClassificationResult classificationResult = singleManeuverClassifier
                    .classifyManeuver(currentManeuver, previousManeuver, nextManeuver);
            result.add(classificationResult);
            previousManeuver = currentManeuver;
            currentManeuver = nextManeuver;
        }
        return result;
    }

    @Override
    protected SingleTrackManeuverNodesLevel recomputeTransitionProbabilitiesAtLevelsWhereNeeded() {
        SingleTrackManeuverNodesLevel currentLevel = this.getLastGraphLevel();
        SingleTrackManeuverNodesLevel lastReadjustedLevel = null;
        while (currentLevel != null) {
            if (currentLevel.isCalculationOfTransitionProbabilitiesNeeded()) {
                currentLevel.computeProbabilitiesFromPreviousLevelToThisLevel();
                lastReadjustedLevel = currentLevel;
            }
            currentLevel = currentLevel.getPreviousLevel();
        }
        return lastReadjustedLevel;
    }

}
