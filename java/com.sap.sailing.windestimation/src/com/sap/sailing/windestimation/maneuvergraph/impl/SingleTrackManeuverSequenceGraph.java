package com.sap.sailing.windestimation.maneuvergraph.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.windestimation.maneuvergraph.impl.bestpath.BestPathsCalculator;
import com.sap.sailing.windestimation.maneuvergraph.impl.bestpath.BestPathsEvaluator;
import com.sap.sailing.windestimation.maneuvergraph.impl.bestpath.SameBoatClassBestPathsEvaluator;
import com.sap.sailing.windestimation.maneuvergraph.impl.classifier.ManeuverClassificationResult;
import com.sap.sailing.windestimation.maneuvergraph.impl.classifier.ManeuverClassifier;
import com.sap.sailing.windestimation.maneuvergraph.impl.classifier.RulesBasedManeuverClassifierImpl;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class SingleTrackManeuverSequenceGraph
        extends AbstractManeuverSequenceGraphImpl<SingleTrackManeuverNodesLevel, ManeuverClassificationResult> {

    public SingleTrackManeuverSequenceGraph(BoatClass boatClass, PolarDataService polarService,
            Iterable<CompleteManeuverCurveWithEstimationData> maneuverSequence) {
        this(getClassificationResults(boatClass, polarService, maneuverSequence), polarService,
                new BestPathsCalculator<>(), new SameBoatClassBestPathsEvaluator<>());
    }

    public SingleTrackManeuverSequenceGraph(Iterable<ManeuverClassificationResult> maneuverClassificationResults,
            PolarDataService polarService, BestPathsCalculator<SingleTrackManeuverNodesLevel> bestPathsCalculator,
            BestPathsEvaluator<SingleTrackManeuverNodesLevel> bestPathsEvaluator) {
        super(maneuverClassificationResults, SingleTrackManeuverNodesLevel.getFactory(), polarService,
                bestPathsCalculator, bestPathsEvaluator);
    }

    public static List<ManeuverClassificationResult> getClassificationResults(BoatClass boatClass,
            PolarDataService polarService, Iterable<CompleteManeuverCurveWithEstimationData> maneuverSequence) {
        ManeuverClassifier singleManeuverClassifier = new RulesBasedManeuverClassifierImpl(boatClass, polarService);
        List<ManeuverClassificationResult> result = new ArrayList<>();
        Iterator<CompleteManeuverCurveWithEstimationData> iterator = maneuverSequence.iterator();
        CompleteManeuverCurveWithEstimationData previousManeuver = null;
        CompleteManeuverCurveWithEstimationData currentManeuver = iterator.hasNext() ? iterator.next() : null;
        while (currentManeuver != null) {
            CompleteManeuverCurveWithEstimationData nextManeuver = iterator.hasNext() ? iterator.next() : null;
            ManeuverClassificationResult classificationResult = singleManeuverClassifier
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
