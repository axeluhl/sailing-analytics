package com.sap.sailing.windestimation.aggregator.hmm;

class BestPathsPerLevel {

    private final BestManeuverNodeInfo[] bestPreviousNodeInfosPerManeuverNode;
    private double probabilitiesFromStartSum = 0;
    private final GraphLevel currentLevel;

    public BestPathsPerLevel(GraphLevel currentLevel) {
        this.currentLevel = currentLevel;
        this.bestPreviousNodeInfosPerManeuverNode = new BestManeuverNodeInfo[currentLevel.getLevelNodes().size()];
    }

    public BestManeuverNodeInfo getBestPreviousNodeInfo(GraphNode currentNode) {
        return bestPreviousNodeInfosPerManeuverNode[currentNode.getIndexInLevel()];
    }

    public BestManeuverNodeInfo addBestPreviousNodeInfo(GraphNode currentNode, GraphNode bestPreviousNode,
            double probabilityFromStart, IntersectedWindRange windRange) {
        BestManeuverNodeInfo bestManeuverNodeInfo = new BestManeuverNodeInfo(bestPreviousNode, probabilityFromStart,
                windRange);
        bestPreviousNodeInfosPerManeuverNode[currentNode.getIndexInLevel()] = bestManeuverNodeInfo;
        probabilitiesFromStartSum += probabilityFromStart;
        return bestManeuverNodeInfo;
    }

    /**
     * Avoid that probability product becomes zero due to precision of Double
     */
    public double getNormalizedProbabilityToNodeFromStart(GraphNode currentNode) {
        return getBestPreviousNodeInfo(currentNode).getProbabilityFromStart() / probabilitiesFromStartSum;
    }

    public boolean isBackwardProbabilitiesComputed() {
        return getBackwardProbabilitiesSum() > 0;
    }

    public double getBackwardProbabilitiesSum() {
        double sumBackwardProbabilities = 0;
        for (BestManeuverNodeInfo nodeInfo : bestPreviousNodeInfosPerManeuverNode) {
            sumBackwardProbabilities += nodeInfo.getBackwardProbability();
        }
        return sumBackwardProbabilities;
    }

    public double getForwardProbabilitiesSum() {
        double sumForwardProbabilities = 0;
        for (BestManeuverNodeInfo nodeInfo : bestPreviousNodeInfosPerManeuverNode) {
            sumForwardProbabilities += nodeInfo.getForwardProbability();
        }
        return sumForwardProbabilities;
    }

    public double getNormalizedForwardProbability(GraphNode currentNode) {
        return getBestPreviousNodeInfo(currentNode).getForwardProbability() / getForwardProbabilitiesSum();
    }

    public double getNormalizedBackwardProbability(GraphNode currentNode) {
        return getBestPreviousNodeInfo(currentNode).getBackwardProbability() / getBackwardProbabilitiesSum();
    }

    public double getNormalizedForwardBackwardProbability(GraphNode currentNode) {
        double sumForwardBackwardProbabilities = 0;
        double currentNodeForwardBackwardProbability = -1;
        for (GraphNode node : currentLevel.getLevelNodes()) {
            double forwardBackwardProbability = getNormalizedForwardProbability(node)
                    * getNormalizedBackwardProbability(node);
            sumForwardBackwardProbabilities += forwardBackwardProbability;
            if (node == currentNode) {
                currentNodeForwardBackwardProbability = forwardBackwardProbability;
            }
        }
        if (currentNodeForwardBackwardProbability < 0) {
            throw new IllegalArgumentException();
        }
        return currentNodeForwardBackwardProbability / sumForwardBackwardProbabilities;
    }

}
