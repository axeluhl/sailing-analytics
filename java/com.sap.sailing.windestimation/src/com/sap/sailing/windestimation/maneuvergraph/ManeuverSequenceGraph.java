package com.sap.sailing.windestimation.maneuvergraph;

import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.windestimation.ManeuverClassificationsAggregator;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.transformer.EstimationDataUtil;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverClassification;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverClassifier;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverClassifiersCache;
import com.sap.sse.common.Util.Triple;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverSequenceGraph implements ManeuverClassificationsAggregator {

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
                .getManeuversSortedByTimePoint(competitorTracks);
        for (ManeuverForEstimation maneuver : usefulManeuvers) {
            appendManeuverAsGraphLevel(maneuver);
        }
    }

    private void appendManeuverAsGraphLevel(ManeuverForEstimation maneuver) {
        ManeuverClassifier bestClassifier = maneuverClassifiersCache.getBestClassifier(maneuver);
        ManeuverClassification maneuverEstimationResult = bestClassifier.classifyManeuver(maneuver);
        GraphLevel newManeuverNodesLevel = new GraphLevel(maneuver, maneuverEstimationResult,
                bestPathsCalculator.getTransitionProbabilitiesCalculator());
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
