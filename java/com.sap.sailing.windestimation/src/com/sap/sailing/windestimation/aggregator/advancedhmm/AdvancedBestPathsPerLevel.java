package com.sap.sailing.windestimation.aggregator.advancedhmm;

import java.util.List;

import com.sap.sailing.windestimation.aggregator.hmm.AbstractBestPathsPerLevel;
import com.sap.sailing.windestimation.aggregator.hmm.BestNodeInfo;
import com.sap.sailing.windestimation.aggregator.hmm.GraphNode;
import com.sap.sse.common.Util.Pair;

public class AdvancedBestPathsPerLevel extends AbstractBestPathsPerLevel {

    private final AdvancedBestManeuverNodeInfo[] bestPreviousNodeInfosPerManeuverNode;
    private final AdvancedGraphLevel currentLevel;

    public AdvancedBestPathsPerLevel(AdvancedGraphLevel currentLevel) {
        this.currentLevel = currentLevel;
        this.bestPreviousNodeInfosPerManeuverNode = new AdvancedBestManeuverNodeInfo[currentLevel.getLevelNodes()
                .size()];
    }

    @Override
    public AdvancedBestManeuverNodeInfo getBestPreviousNodeInfo(GraphNode currentNode) {
        return bestPreviousNodeInfosPerManeuverNode[currentNode.getIndexInLevel()];
    }

    public AdvancedBestManeuverNodeInfo addBestPreviousNodeInfo(GraphNode currentNode,
            List<Pair<AdvancedGraphLevel, GraphNode>> previousGraphLevelsWithBestPreviousNodes,
            double probabilityFromStart) {
        AdvancedBestManeuverNodeInfo bestManeuverNodeInfo = new AdvancedBestManeuverNodeInfo(
                previousGraphLevelsWithBestPreviousNodes, probabilityFromStart);
        bestPreviousNodeInfosPerManeuverNode[currentNode.getIndexInLevel()] = bestManeuverNodeInfo;
        return bestManeuverNodeInfo;
    }

    @Override
    public AdvancedGraphLevel getCurrentLevel() {
        return currentLevel;
    }

    @Override
    protected BestNodeInfo[] getPreviousNodeInfosPerManeuverNode() {
        return bestPreviousNodeInfosPerManeuverNode;
    }

}
