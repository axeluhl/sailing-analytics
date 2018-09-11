package com.sap.sailing.windestimation.maneuvergraph;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.windestimation.WindTrackEstimator;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.transformer.EstimationDataUtil;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverClassifier;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverClassifiersCache;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverEstimationResult;
import com.sap.sse.common.Util.Triple;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverSequenceGraph implements WindTrackEstimator {

    private GraphLevel firstGraphLevel = null;
    private GraphLevel lastGraphLevel = null;
    private final PolarDataService polarService;
    private final BestPathsCalculator bestPathsCalculator;
    private ManeuverClassifiersCache maneuverClassifiersCache;

    public ManeuverSequenceGraph(List<CompetitorTrackWithEstimationData<ManeuverForEstimation>> competitorTracks,
            ManeuverClassifiersCache maneuverClassifiersCache, BestPathsCalculator bestPathsCalculator) {
        this.polarService = maneuverClassifiersCache.getPolarDataService();
        this.maneuverClassifiersCache = maneuverClassifiersCache;
        this.bestPathsCalculator = bestPathsCalculator;
        List<ManeuverForEstimation> usefulManeuvers = EstimationDataUtil
                .getUsefulManeuversSortedByTimePoint(competitorTracks);
        for (ManeuverForEstimation maneuver : usefulManeuvers) {
            appendManeuverAsGraphLevel(maneuver);
        }
        upgradeNodeConfidencesWithPenaltyCircleInfo(competitorTracks);
    }

    private void upgradeNodeConfidencesWithPenaltyCircleInfo(
            List<CompetitorTrackWithEstimationData<ManeuverForEstimation>> competitorTracks) {
        List<ManeuverForEstimation> penaltyCircles = EstimationDataUtil
                .getUsefulPenaltyCirclesSortedByTimePoint(competitorTracks);
        if (!penaltyCircles.isEmpty()) {
            Iterator<ManeuverForEstimation> penaltyCirclesIterator = penaltyCircles.iterator();
            ManeuverForEstimation currentPenaltyCircle = penaltyCirclesIterator.next();
            GraphLevel previousLevel = null;
            GraphLevel currentLevel = firstGraphLevel;
            double closestSecondsToUsefulManeuver = Double.MAX_VALUE;
            while (currentLevel != null) {
                double secondsToNextUsefulManeuver = Math.abs(currentLevel.getManeuver().getManeuverTimePoint()
                        .until(currentPenaltyCircle.getManeuverTimePoint()).asSeconds());
                if (secondsToNextUsefulManeuver < closestSecondsToUsefulManeuver) {
                    closestSecondsToUsefulManeuver = secondsToNextUsefulManeuver;
                } else {
                    GraphLevel levelToUpdate = previousLevel == null ? currentLevel : previousLevel;
                    levelToUpdate.upgradeLevelNodesConsideringPenaltyCircle(currentPenaltyCircle);
                    if (penaltyCirclesIterator.hasNext()) {
                        currentPenaltyCircle = penaltyCirclesIterator.next();
                        continue;
                    } else {
                        currentPenaltyCircle = null;
                        break;
                    }
                }
                previousLevel = currentLevel;
                currentLevel = currentLevel.getNextLevel();
            }
            while (currentPenaltyCircle != null) {
                lastGraphLevel.upgradeLevelNodesConsideringPenaltyCircle(currentPenaltyCircle);
                currentPenaltyCircle = penaltyCirclesIterator.hasNext() ? penaltyCirclesIterator.next() : null;
            }
        }
    }

    private void appendManeuverAsGraphLevel(ManeuverForEstimation maneuver) {
        ManeuverClassifier bestClassifier = maneuverClassifiersCache.getBestClassifier(maneuver);
        ManeuverEstimationResult maneuverEstimationResult = bestClassifier.classifyManeuver(maneuver);
        GraphLevel newManeuverNodesLevel = new GraphLevel(maneuver, maneuverEstimationResult);
        if (firstGraphLevel == null) {
            firstGraphLevel = newManeuverNodesLevel;
            lastGraphLevel = newManeuverNodesLevel;

        } else {
            lastGraphLevel.appendNextManeuverNodesLevel(newManeuverNodesLevel);
            lastGraphLevel = newManeuverNodesLevel;
        }
        bestPathsCalculator.computeBestPathsToNextLevel(newManeuverNodesLevel);
    }

    public GraphLevel getFirstGraphLevel() {
        return firstGraphLevel;
    }

    public GraphLevel getLastGraphLevel() {
        return lastGraphLevel;
    }

    public PolarDataService getPolarService() {
        return polarService;
    }

    public List<WindWithConfidence<Void>> estimateWindTrack() {
        List<WindWithConfidence<Void>> windTrack = Collections.emptyList();
        GraphLevel lastGraphLevel = this.lastGraphLevel;
        if (lastGraphLevel != null) {
            List<Triple<GraphLevel, GraphNode, Double>> bestPath = bestPathsCalculator.getBestPath(lastGraphLevel);
            windTrack = bestPathsCalculator.getWindTrack(bestPath);
        }
        return windTrack;
    }

}
