package com.sap.sailing.windestimation.aggregator.hmm;

public class BestPathsPerLevel extends AbstractBestPathsPerLevel {

    private final BestManeuverNodeInfo[] bestPreviousNodeInfosPerManeuverNode;
    private final GraphLevel currentLevel;

    public BestPathsPerLevel(GraphLevel currentLevel) {
        this.currentLevel = currentLevel;
        this.bestPreviousNodeInfosPerManeuverNode = new BestManeuverNodeInfo[currentLevel.getLevelNodes().size()];
    }

    @Override
    public BestManeuverNodeInfo getBestPreviousNodeInfo(GraphNode currentNode) {
        return bestPreviousNodeInfosPerManeuverNode[currentNode.getIndexInLevel()];
    }

    public BestManeuverNodeInfo addBestPreviousNodeInfo(GraphNode currentNode, GraphNode bestPreviousNode,
            double probabilityFromStart, IntersectedWindRange intersectedWindRange) {
        BestManeuverNodeInfo bestManeuverNodeInfo = new BestManeuverNodeInfo(bestPreviousNode, probabilityFromStart, intersectedWindRange);
        bestPreviousNodeInfosPerManeuverNode[currentNode.getIndexInLevel()] = bestManeuverNodeInfo;
        return bestManeuverNodeInfo;
    }

    @Override
    public GraphLevel getCurrentLevel() {
        return currentLevel;
    }

    @Override
    protected BestNodeInfo[] getPreviousNodeInfosPerManeuverNode() {
        return bestPreviousNodeInfosPerManeuverNode;
    }

}
