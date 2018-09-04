package com.sap.sailing.windestimation.maneuvergraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverClassifier;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverClassifiersCache;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverEstimationResult;
import com.sap.sailing.windestimation.maneuvergraph.bestpath.BestPathEvaluationResult;
import com.sap.sailing.windestimation.maneuvergraph.bestpath.BestPathsCalculator;
import com.sap.sailing.windestimation.maneuvergraph.bestpath.BestPathsEvaluator;
import com.sap.sailing.windestimation.maneuvergraph.bestpath.MultipleBoatClassBestsPathEvaluator;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class PointOfSailSequenceGraph {

    private GraphLevel firstGraphLevel = null;
    private GraphLevel lastGraphLevel = null;
    private final PolarDataService polarService;
    private final BestPathsCalculator bestPathsCalculator;
    private final BestPathsEvaluator bestPathsEvaluator;
    private ManeuverClassifiersCache maneuverClassifiersCache;

    public PointOfSailSequenceGraph(List<CompetitorTrackWithEstimationData<ManeuverForEstimation>> competitorTracks,
            ManeuverClassifiersCache maneuverClassifiersCache) {
        this.polarService = maneuverClassifiersCache.getPolarDataService();
        this.maneuverClassifiersCache = maneuverClassifiersCache;
        this.bestPathsCalculator = new BestPathsCalculator();
        this.bestPathsEvaluator = new MultipleBoatClassBestsPathEvaluator();
        List<ManeuverForEstimation> usefulManeuvers = getUsefulManeuversSortedByTimePoint(competitorTracks);
        for (ManeuverForEstimation maneuver : usefulManeuvers) {
            appendManeuverAsGraphLevel(maneuver);
        }
    }

    private List<ManeuverForEstimation> getUsefulManeuversSortedByTimePoint(
            List<CompetitorTrackWithEstimationData<ManeuverForEstimation>> competitorTracks) {
        List<ManeuverForEstimation> usefulManeuversSortedByTimePoint = new ArrayList<>();
        for (CompetitorTrackWithEstimationData<ManeuverForEstimation> competitorTrack : competitorTracks) {
            for (ManeuverForEstimation maneuver : competitorTrack.getElements()) {
                if (maneuver.isClean() || maneuver.isCleanBefore() || maneuver.isCleanAfter()) {
                    usefulManeuversSortedByTimePoint.add(maneuver);
                }
            }
        }
        Collections.sort(usefulManeuversSortedByTimePoint,
                (o1, o2) -> o1.getManeuverTimePoint().compareTo(o2.getManeuverTimePoint()));
        return usefulManeuversSortedByTimePoint;
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
        newManeuverNodesLevel.computeProbabilitiesFromPreviousLevelToThisLevel();
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

    protected GraphLevel readjustTransitionProbabilitiesByBestPathEvaluationResult(
            BestPathEvaluationResult bestPathEvaluationResult) {
        GraphLevel currentLevel = this.firstGraphLevel;
        while (currentLevel != null) {
            double tackProbabilityBonus = bestPathEvaluationResult
                    .getTackProbabilityBonusForManeuverOfLevel(currentLevel);
            if (Math.abs(tackProbabilityBonus) > 0.001) {
                currentLevel.setTackProbabilityBonusToManeuver(tackProbabilityBonus);
            }
            double upwindBeforeProbabilityBonus = bestPathEvaluationResult
                    .getUpwindBeforeProbabilityBonusForManeuverOfLevel(currentLevel);
            if (Math.abs(upwindBeforeProbabilityBonus) > 0.001) {
                currentLevel.setUpwindBeforeProbabilityBonusToManeuver(upwindBeforeProbabilityBonus);
            }
            double upwindAfterProbabilityBonus = bestPathEvaluationResult
                    .getUpwindAfterProbabilityBonusForManeuverOfLevel(currentLevel);
            if (Math.abs(upwindAfterProbabilityBonus) > 0.001) {
                currentLevel.setUpwindAfterProbabilityBonusToManeuver(upwindAfterProbabilityBonus);
            }
            currentLevel = currentLevel.getNextLevel();
        }
        GraphLevel firstReadjustedLevel = recomputeTransitionProbabilitiesAtLevelsWhereNeeded();
        return firstReadjustedLevel;
    }

    protected GraphLevel recomputeTransitionProbabilitiesAtLevelsWhereNeeded() {
        GraphLevel currentLevel = this.getLastGraphLevel();
        GraphLevel lastReadjustedLevel = null;
        while (currentLevel != null) {
            if (currentLevel.isCalculationOfTransitionProbabilitiesNeeded()) {
                currentLevel.computeProbabilitiesFromPreviousLevelToThisLevel();
                lastReadjustedLevel = currentLevel;
            }
            currentLevel = currentLevel.getPreviousLevel();
        }
        return lastReadjustedLevel;
    }

    public List<Pair<GraphLevel, FineGrainedPointOfSail>> determineBestPath() {
        List<List<Pair<GraphLevel, FineGrainedPointOfSail>>> alreadyEvaluatedBestPaths = new ArrayList<>();
        while (true) {
            List<Pair<GraphLevel, FineGrainedPointOfSail>> bestPath = bestPathsCalculator.getBestPath(lastGraphLevel);
            // limit the number of new best path recalculations to 10
            if (alreadyEvaluatedBestPaths.contains(bestPath) || alreadyEvaluatedBestPaths.size() > 9) {
                return bestPath;
            }
            BestPathEvaluationResult bestPathEvaluationResult = bestPathsEvaluator.evaluateBestPath(bestPath);
            if (bestPathEvaluationResult.hasAnyProbabilityBonusToOffer()) {
                alreadyEvaluatedBestPaths.add(bestPath);
                GraphLevel firstReadjustedLevel = readjustTransitionProbabilitiesByBestPathEvaluationResult(
                        bestPathEvaluationResult);
                if (firstReadjustedLevel != null) {
                    bestPathsCalculator.recomputeBestPathsFromLevel(firstReadjustedLevel);
                }
            } else {
                return bestPath;
            }
        }
    }

    public List<WindWithConfidence<TimePoint>> estimateWindTrack() {
        List<WindWithConfidence<TimePoint>> windTrack = Collections.emptyList();
        GraphLevel lastGraphLevel = this.lastGraphLevel;
        if (lastGraphLevel != null) {
            WindTrackFromManeuverGraphExtractor windTrackFromManeuverGraphExtractor = new WindTrackFromManeuverGraphExtractor(
                    polarService);
            List<Pair<GraphLevel, FineGrainedPointOfSail>> bestPath = determineBestPath();
            double bestPathConfidence = bestPathsCalculator.getConfidenceOfBestPath(bestPath);
            windTrack = windTrackFromManeuverGraphExtractor.getWindTrack(bestPath, bestPathConfidence);
        }
        return windTrack;
    }

}
