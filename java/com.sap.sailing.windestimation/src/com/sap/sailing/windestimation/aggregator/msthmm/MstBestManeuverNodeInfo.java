package com.sap.sailing.windestimation.aggregator.msthmm;

import java.util.List;

import com.sap.sailing.windestimation.aggregator.hmm.BestNodeInfo;
import com.sap.sailing.windestimation.aggregator.hmm.GraphNode;
import com.sap.sailing.windestimation.aggregator.hmm.IntersectedWindRange;
import com.sap.sse.common.Util.Pair;

/**
 * Contains information about best path until the node from level represented in {@link MstBestPathsPerLevel}.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class MstBestManeuverNodeInfo extends BestNodeInfo {

    private final List<Pair<MstGraphLevel, GraphNode>> previousGraphLevelsWithBestPreviousNodes;

    public MstBestManeuverNodeInfo(List<Pair<MstGraphLevel, GraphNode>> previousGraphLevelsWithBestPreviousNodes,
            double probabilityFromStart, IntersectedWindRange intersectedWindRange) {
        super(probabilityFromStart, intersectedWindRange);
        this.previousGraphLevelsWithBestPreviousNodes = previousGraphLevelsWithBestPreviousNodes;
    }

    public List<Pair<MstGraphLevel, GraphNode>> getPreviousGraphLevelsWithBestPreviousNodes() {
        return previousGraphLevelsWithBestPreviousNodes;
    }

}
