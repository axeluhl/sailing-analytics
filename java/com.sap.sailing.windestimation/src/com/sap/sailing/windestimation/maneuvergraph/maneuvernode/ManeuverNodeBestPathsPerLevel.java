package com.sap.sailing.windestimation.maneuvergraph.maneuvernode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.windestimation.data.FineGrainedManeuverType;
import com.sap.sailing.windestimation.data.FineGrainedPointOfSail;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.polarsfitting.SailingStatistics;

class ManeuverNodeBestPathsPerLevel {

    private Map<ManeuverNode, BestManeuverNodeInfo> bestPreviousNodeInfosPerManeuverNode = new HashMap<>();
    private double probabilitiesFromStartSum = 0;
    private final ManeuverNodeGraphLevel currentLevel;

    public ManeuverNodeBestPathsPerLevel(ManeuverNodeGraphLevel currentLevel) {
        this.currentLevel = currentLevel;
    }

    public BestManeuverNodeInfo getBestPreviousNodeInfo(ManeuverNode currentNode) {
        return bestPreviousNodeInfosPerManeuverNode.get(currentNode);
    }

    public BestManeuverNodeInfo addBestPreviousNodeInfo(ManeuverNode currentNode, ManeuverNode bestPreviousNode,
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
        bestPreviousNodeInfosPerManeuverNode.put(currentNode, bestManeuverNodeInfo);
        probabilitiesFromStartSum += probabilityFromStart;
        return bestManeuverNodeInfo;
    }

    /**
     * Avoid that probability product becomes zero due to precision of Double
     */
    public double getNormalizedProbabilityToNodeFromStart(ManeuverNode currentNode) {
        return getBestPreviousNodeInfo(currentNode).getProbabilityFromStart() / probabilitiesFromStartSum;
    }

}
