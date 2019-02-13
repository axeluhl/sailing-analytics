package com.sap.sailing.windestimation.aggregator.hmm;

/**
 * Contains information about best previous paths for the each of the nodes contained in the provided
 * {@link GraphLevel}.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public abstract class AbstractBestPathsPerLevel {

    public abstract BestNodeInfo getBestPreviousNodeInfo(GraphNode currentNode);

    public abstract GraphLevelBase getCurrentLevel();

    protected abstract BestNodeInfo[] getPreviousNodeInfosPerManeuverNode();

    public double getProbabilitiesFromStartSum() {
        double probabilitiesFromStartSum = 0;
        for (BestNodeInfo bestNodeInfo : getPreviousNodeInfosPerManeuverNode()) {
            probabilitiesFromStartSum += bestNodeInfo.getProbabilityFromStart();
        }
        return probabilitiesFromStartSum;
    }

    /**
     * Avoid that probability product becomes zero due to precision of Double
     */
    public double getNormalizedProbabilityToNodeFromStart(GraphNode currentNode) {
        return getBestPreviousNodeInfo(currentNode).getProbabilityFromStart() / getProbabilitiesFromStartSum();
    }

    public boolean isBackwardProbabilitiesComputed() {
        return getBackwardProbabilitiesSum() > 0;
    }

    public double getBackwardProbabilitiesSum() {
        double sumBackwardProbabilities = 0;
        for (BestNodeInfo nodeInfo : getPreviousNodeInfosPerManeuverNode()) {
            sumBackwardProbabilities += nodeInfo.getBackwardProbability();
        }
        return sumBackwardProbabilities;
    }

    public double getForwardProbabilitiesSum() {
        double sumForwardProbabilities = 0;
        for (BestNodeInfo nodeInfo : getPreviousNodeInfosPerManeuverNode()) {
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
        for (GraphNode node : getCurrentLevel().getLevelNodes()) {
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
