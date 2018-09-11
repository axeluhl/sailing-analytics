package com.sap.sailing.windestimation.maneuvergraph;

import java.util.List;

import com.sap.sailing.windestimation.data.FineGrainedManeuverType;
import com.sap.sailing.windestimation.data.FineGrainedPointOfSail;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.polarsfitting.SailingStatistics;

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
            double probabilityFromStart, IntersectedWindRange windRange, SailingStatistics previousNodePathStats) {
        BestManeuverNodeInfo bestManeuverNodeInfo = new BestManeuverNodeInfo(bestPreviousNode, probabilityFromStart,
                windRange);
        SailingStatistics currentNodePathStats = previousNodePathStats == null ? new SailingStatistics()
                : previousNodePathStats.clone();
        ManeuverForEstimation maneuver = currentLevel.getManeuver();
        if (maneuver.isCleanBefore() || maneuver.isCleanAfter()) {
            List<FineGrainedPointOfSail> bestSuitablePointOfSails = windRange.getBestSuitablePointOfSails(currentNode,
                    currentLevel.getManeuver(), currentLevel.getManeuver().getSpeedWithBearingAfter().getBearing());
            for (FineGrainedPointOfSail pointOfSailAfterManeuver : bestSuitablePointOfSails) {
                if (currentLevel.getManeuver().isCleanBefore()) {
                    FineGrainedPointOfSail pointOfSailBeforeManeuver = pointOfSailAfterManeuver
                            .getNextPointOfSail(currentLevel.getManeuver().getCourseChangeInDegrees() * -1);
                    currentNodePathStats.addRecordToStatistics(
                            currentLevel.getManeuver().getAverageSpeedWithBearingBefore(), pointOfSailBeforeManeuver);
                }
                if (currentLevel.getManeuver().isCleanAfter()) {
                    currentNodePathStats.addRecordToStatistics(
                            currentLevel.getManeuver().getAverageSpeedWithBearingAfter(), pointOfSailAfterManeuver);
                }
            }
        }
        if (maneuver.isClean()) {
            List<FineGrainedManeuverType> bestSuitableManeuverTypes = windRange
                    .getBestSuitableManeuverTypes(currentNode, maneuver);
            for (FineGrainedManeuverType maneuverType : bestSuitableManeuverTypes) {
                currentNodePathStats.addRecordToStatistics(maneuver, maneuverType);
            }
        }
        bestManeuverNodeInfo.setPathSailingStatistics(currentLevel.getManeuver().getBoatClass(), currentNodePathStats);
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

}
