package com.sap.sailing.windestimation.aggregator.hmm;

/**
 * As {@link AbstractBestPathsPerLevel}, with additional compatibility to {@link ManeuverSequenceGraph}.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class BestPathsPerLevel extends AbstractBestPathsPerLevel<GraphLevel> implements Comparable<BestPathsPerLevel> {

    private final BestManeuverNodeInfo<GraphLevel>[] bestPreviousNodeInfosPerManeuverNode;
    private final GraphLevel currentLevel;

    public BestPathsPerLevel(GraphLevel currentLevel) {
        this.currentLevel = currentLevel;
        @SuppressWarnings("unchecked")
        final BestManeuverNodeInfo<GraphLevel>[] typeSafeArray = (BestManeuverNodeInfo<GraphLevel>[]) new BestManeuverNodeInfo<?>[currentLevel.getLevelNodes().size()];
        this.bestPreviousNodeInfosPerManeuverNode = typeSafeArray;
    }

    @Override
    public BestManeuverNodeInfo<GraphLevel> getBestPreviousNodeInfo(GraphNode<GraphLevel> currentNode) {
        return bestPreviousNodeInfosPerManeuverNode[currentNode.getIndexInLevel()];
    }

    public BestManeuverNodeInfo<GraphLevel> addBestPreviousNodeInfo(GraphNode<GraphLevel> currentNode, GraphNode<GraphLevel> bestPreviousNode,
            double probabilityFromStart, IntersectedWindRange intersectedWindRange) {
        BestManeuverNodeInfo<GraphLevel> bestManeuverNodeInfo = new BestManeuverNodeInfo<GraphLevel>(bestPreviousNode, probabilityFromStart,
                intersectedWindRange);
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

    @Override
    public int compareTo(BestPathsPerLevel o) {
        return toString().compareTo(o.toString());
    }

}
