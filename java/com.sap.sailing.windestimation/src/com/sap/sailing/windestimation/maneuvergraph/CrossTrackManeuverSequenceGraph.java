package com.sap.sailing.windestimation.maneuvergraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.windestimation.maneuvergraph.bestpath.BestPathsCalculator;
import com.sap.sailing.windestimation.maneuvergraph.bestpath.MultipleBoatClassBestsPathEvaluator;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class CrossTrackManeuverSequenceGraph
        extends ManeuverSequenceGraph<CrossTrackManeuverNodesLevel, SingleTrackManeuverNodesLevel> {

    public CrossTrackManeuverSequenceGraph(Iterable<SingleTrackManeuverSequenceGraph> singleTrackManeuverSequenceGraphs,
            PolarDataService polarService) {
        super(getSingleTrackManeuverNodeLevels(singleTrackManeuverSequenceGraphs),
                CrossTrackManeuverNodesLevel.getFactory(), polarService, new BestPathsCalculator<>(),
                new MultipleBoatClassBestsPathEvaluator<>());
    }

    private static List<SingleTrackManeuverNodesLevel> getSingleTrackManeuverNodeLevels(
            Iterable<SingleTrackManeuverSequenceGraph> singleTrackManeuverSequenceGraphs) {
        List<SingleTrackManeuverNodesLevel> singleTrackManeuverNodesLevels = new ArrayList<>();
        for (SingleTrackManeuverSequenceGraph graph : singleTrackManeuverSequenceGraphs) {
            SingleTrackManeuverNodesLevel currentLevel = graph.getFirstGraphLevel();
            while (currentLevel != null) {
                singleTrackManeuverNodesLevels.add(currentLevel);
                currentLevel = currentLevel.getPreviousLevel();
            }
        }
        Collections.sort(singleTrackManeuverNodesLevels,
                (o1, o2) -> o1.getManeuver().getTimePoint().compareTo(o2.getManeuver().getTimePoint()));
        return singleTrackManeuverNodesLevels;
    }

    @Override
    protected CrossTrackManeuverNodesLevel recomputeTransitionProbabilitiesAtLevelsWhereNeeded() {
        CrossTrackManeuverNodesLevel currentLevel = this.getLastGraphLevel();
        CrossTrackManeuverNodesLevel lastReadjustedLevel = null;
        while (currentLevel != null) {
            SingleTrackManeuverNodesLevel singleTrackManeuverNodesLevel = currentLevel
                    .getSingleTrackManeuverNodesLevel();
            if (singleTrackManeuverNodesLevel.isCalculationOfTransitionProbabilitiesNeeded()) {
                singleTrackManeuverNodesLevel.computeProbabilitiesFromPreviousLevelToThisLevel();
            }
            if (currentLevel.isCalculationOfTransitionProbabilitiesNeeded()) {
                currentLevel.computeProbabilitiesFromPreviousLevelToThisLevel();
                lastReadjustedLevel = currentLevel;
            }
            currentLevel = currentLevel.getPreviousLevel();
        }
        return lastReadjustedLevel;
    }

}
