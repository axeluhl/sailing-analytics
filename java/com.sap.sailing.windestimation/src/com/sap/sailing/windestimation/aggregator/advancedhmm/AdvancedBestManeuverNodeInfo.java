package com.sap.sailing.windestimation.aggregator.advancedhmm;

import java.util.List;

import com.sap.sailing.windestimation.aggregator.hmm.BestNodeInfo;
import com.sap.sailing.windestimation.aggregator.hmm.GraphNode;
import com.sap.sse.common.Util.Pair;

public class AdvancedBestManeuverNodeInfo extends BestNodeInfo {

    private final List<Pair<AdvancedGraphLevel, GraphNode>> previousGraphLevelsWithBestPreviousNodes;

    public AdvancedBestManeuverNodeInfo(
            List<Pair<AdvancedGraphLevel, GraphNode>> previousGraphLevelsWithBestPreviousNodes,
            double probabilityFromStart) {
        super(probabilityFromStart);
        this.previousGraphLevelsWithBestPreviousNodes = previousGraphLevelsWithBestPreviousNodes;
    }

    public List<Pair<AdvancedGraphLevel, GraphNode>> getPreviousGraphLevelsWithBestPreviousNodes() {
        return previousGraphLevelsWithBestPreviousNodes;
    }

}
