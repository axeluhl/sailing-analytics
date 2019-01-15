package com.sap.sailing.windestimation.aggregator.msthmm;

import java.util.List;

import com.sap.sailing.windestimation.aggregator.hmm.AbstractBestPathsPerLevel;
import com.sap.sailing.windestimation.aggregator.hmm.BestNodeInfo;
import com.sap.sailing.windestimation.aggregator.hmm.GraphNode;
import com.sap.sailing.windestimation.aggregator.hmm.IntersectedWindRange;
import com.sap.sse.common.Util.Pair;

public class MstBestPathsPerLevel extends AbstractBestPathsPerLevel {

    private final MstBestManeuverNodeInfo[] bestPreviousNodeInfosPerManeuverNode;
    private final MstGraphLevel currentLevel;

    public MstBestPathsPerLevel(MstGraphLevel currentLevel) {
        this.currentLevel = currentLevel;
        this.bestPreviousNodeInfosPerManeuverNode = new MstBestManeuverNodeInfo[currentLevel.getLevelNodes()
                .size()];
    }

    @Override
    public MstBestManeuverNodeInfo getBestPreviousNodeInfo(GraphNode currentNode) {
        return bestPreviousNodeInfosPerManeuverNode[currentNode.getIndexInLevel()];
    }

    public MstBestManeuverNodeInfo addBestPreviousNodeInfo(GraphNode currentNode,
            List<Pair<MstGraphLevel, GraphNode>> previousGraphLevelsWithBestPreviousNodes,
            double probabilityFromStart, IntersectedWindRange intersectedWindRange) {
        MstBestManeuverNodeInfo bestManeuverNodeInfo = new MstBestManeuverNodeInfo(
                previousGraphLevelsWithBestPreviousNodes, probabilityFromStart, intersectedWindRange);
        bestPreviousNodeInfosPerManeuverNode[currentNode.getIndexInLevel()] = bestManeuverNodeInfo;
        return bestManeuverNodeInfo;
    }

    @Override
    public MstGraphLevel getCurrentLevel() {
        return currentLevel;
    }

    @Override
    protected BestNodeInfo[] getPreviousNodeInfosPerManeuverNode() {
        return bestPreviousNodeInfosPerManeuverNode;
    }

}
