package com.sap.sailing.windestimation.maneuvergraph;

import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverSequenceGraph<T extends ManeuverNodesLevel<T>, R> {

    private T firstGraphLevel = null;
    private T lastGraphLevel = null;
    private final ManeuverNodesLevelFactory<T, R> maneuverNodesLevelFactory;
    private final PolarDataService polarService;
    private final BestPathsCalculator<T> bestPathsCalculator;

    public ManeuverSequenceGraph(Iterable<R> maneuverSequence,
            ManeuverNodesLevelFactory<T, R> maneuverNodesLevelFactory, PolarDataService polarService,
            BestPathsCalculator<T> bestPathsCalculator) {
        this(maneuverNodesLevelFactory, polarService, bestPathsCalculator);
        for (R maneuver : maneuverSequence) {
            appendManeuverAsGraphLevel(maneuver);
        }
    }

    public ManeuverSequenceGraph(ManeuverNodesLevelFactory<T, R> maneuverNodesLevelFactory,
            PolarDataService polarService, BestPathsCalculator<T> bestPathsCalculator) {
        this.maneuverNodesLevelFactory = maneuverNodesLevelFactory;
        this.polarService = polarService;
        this.bestPathsCalculator = bestPathsCalculator;
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

    public void recomputePossiblePathsWithDistances() {
        T currentLevel = firstGraphLevel;
        bestPathsCalculator.resetState();
        while (currentLevel != null) {
            currentLevel.computeProbabilitiesFromPreviousLevelToThisLevel();
            bestPathsCalculator.computeBestPathsToNextLevel(currentLevel);
            currentLevel = currentLevel.getNextLevel();
        }
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

    public List<WindWithConfidence<TimePoint>> estimateWindTrack() {
        List<WindWithConfidence<TimePoint>> windTrack = Collections.emptyList();
        T lastGraphLevel = this.lastGraphLevel;
        if (lastGraphLevel != null) {
            WindTrackFromManeuverGraphExtractor<T> windTrackFromManeuverGraphExtractor = new WindTrackFromManeuverGraphExtractor<>(
                    polarService);
            List<Pair<T, FineGrainedPointOfSail>> bestPath = bestPathsCalculator.getBestPath(lastGraphLevel);
            double bestPathConfidence = bestPathsCalculator.getConfidenceOfBestPath(bestPath);
            windTrack = windTrackFromManeuverGraphExtractor.getWindTrack(bestPath, bestPathConfidence);
        }
        return windTrack;
    }

}
