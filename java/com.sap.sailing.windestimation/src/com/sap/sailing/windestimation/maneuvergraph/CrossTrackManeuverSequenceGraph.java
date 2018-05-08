package com.sap.sailing.windestimation.maneuvergraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.polars.PolarDataService;

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
                CrossTrackManeuverNodesLevel.getFactory(), polarService, new BestPathsCalculator<>());
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

}
