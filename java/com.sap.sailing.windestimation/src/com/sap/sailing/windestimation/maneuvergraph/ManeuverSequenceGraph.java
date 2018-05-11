package com.sap.sailing.windestimation.maneuvergraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.windestimation.maneuvergraph.bestpath.BestPathEvaluationResult;
import com.sap.sailing.windestimation.maneuvergraph.bestpath.BestPathsCalculator;
import com.sap.sailing.windestimation.maneuvergraph.bestpath.BestPathsEvaluator;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public abstract class ManeuverSequenceGraph<T extends ManeuverNodesLevel<T>, R> {

    private T firstGraphLevel = null;
    private T lastGraphLevel = null;
    private final ManeuverNodesLevelFactory<T, R> maneuverNodesLevelFactory;
    private final PolarDataService polarService;
    private final BestPathsCalculator<T> bestPathsCalculator;
    private final BestPathsEvaluator<T> bestPathsEvaluator;

    public ManeuverSequenceGraph(Iterable<R> maneuverSequence,
            ManeuverNodesLevelFactory<T, R> maneuverNodesLevelFactory, PolarDataService polarService,
            BestPathsCalculator<T> bestPathsCalculator, BestPathsEvaluator<T> bestPathsEvaluator) {
        this(maneuverNodesLevelFactory, polarService, bestPathsCalculator, bestPathsEvaluator);
        for (R maneuver : maneuverSequence) {
            appendManeuverAsGraphLevel(maneuver);
        }
    }

    public ManeuverSequenceGraph(ManeuverNodesLevelFactory<T, R> maneuverNodesLevelFactory,
            PolarDataService polarService, BestPathsCalculator<T> bestPathsCalculator,
            BestPathsEvaluator<T> bestPathsEvaluator) {
        this.maneuverNodesLevelFactory = maneuverNodesLevelFactory;
        this.polarService = polarService;
        this.bestPathsCalculator = bestPathsCalculator;
        this.bestPathsEvaluator = bestPathsEvaluator;
    }

    protected void appendManeuverAsGraphLevel(R nodeLevelReference) {
        T newManeuverNodesLevel = maneuverNodesLevelFactory.createNewManeuverNodesLevel(nodeLevelReference);
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

    public T getFirstGraphLevel() {
        return firstGraphLevel;
    }

    public T getLastGraphLevel() {
        return lastGraphLevel;
    }

    public PolarDataService getPolarService() {
        return polarService;
    }

    public T readjustTransitionProbabilitiesByBestPathEvaluationResult(
            BestPathEvaluationResult<T> bestPathEvaluationResult) {
        T currentLevel = this.firstGraphLevel;
        while (currentLevel != null) {
            double tackProbabilityBonus = bestPathEvaluationResult
                    .getTackProbabilityBonusForManeuverOfLevel(currentLevel);
            if (Math.abs(tackProbabilityBonus) > 0.001) {
                currentLevel.setTackProbabilityBonusToManeuver(tackProbabilityBonus);
            }
            currentLevel = currentLevel.getNextLevel();
        }
        T firstReadjustedLevel = recomputeTransitionProbabilitiesAtLevelsWhereNeeded();
        return firstReadjustedLevel;
    }

    protected abstract T recomputeTransitionProbabilitiesAtLevelsWhereNeeded();

    public List<Pair<T, FineGrainedPointOfSail>> determineBestPath() {
        List<List<Pair<T, FineGrainedPointOfSail>>> alreadyEvaluatedBestPaths = new ArrayList<>();
        while (true) {
            List<Pair<T, FineGrainedPointOfSail>> bestPath = bestPathsCalculator.getBestPath(lastGraphLevel);
            // limit the number of new best path recalculations to 10
            if (alreadyEvaluatedBestPaths.contains(bestPath) || alreadyEvaluatedBestPaths.size() > 9) {
                return bestPath;
            }
            BestPathEvaluationResult<T> bestPathEvaluationResult = bestPathsEvaluator.evaluateBestPath(bestPath);
            if (bestPathEvaluationResult.hasAnyTackProbabilityBonusToOffer()) {
                alreadyEvaluatedBestPaths.add(bestPath);
                T firstReadjustedLevel = readjustTransitionProbabilitiesByBestPathEvaluationResult(
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
        T lastGraphLevel = this.lastGraphLevel;
        if (lastGraphLevel != null) {
            WindTrackFromManeuverGraphExtractor<T> windTrackFromManeuverGraphExtractor = new WindTrackFromManeuverGraphExtractor<>(
                    polarService);
            List<Pair<T, FineGrainedPointOfSail>> bestPath = determineBestPath();
            double bestPathConfidence = bestPathsCalculator.getConfidenceOfBestPath(bestPath);
            windTrack = windTrackFromManeuverGraphExtractor.getWindTrack(bestPath, bestPathConfidence);
        }
        return windTrack;
    }

}
