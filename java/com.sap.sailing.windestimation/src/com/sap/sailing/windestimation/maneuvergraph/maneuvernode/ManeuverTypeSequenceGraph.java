package com.sap.sailing.windestimation.maneuvergraph.maneuvernode;

import java.util.Collections;
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
import com.sap.sse.common.Util.Pair;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverTypeSequenceGraph implements WindTrackEstimator {

    private ManeuverNodeGraphLevel firstGraphLevel = null;
    private ManeuverNodeGraphLevel lastGraphLevel = null;
    private final PolarDataService polarService;
    private final ManeuverNodeBestPathsCalculator bestPathsCalculator;
    private ManeuverClassifiersCache maneuverClassifiersCache;

    public ManeuverTypeSequenceGraph(List<CompetitorTrackWithEstimationData<ManeuverForEstimation>> competitorTracks,
            ManeuverClassifiersCache maneuverClassifiersCache, ManeuverNodeBestPathsCalculator bestPathsCalculator) {
        this.polarService = maneuverClassifiersCache.getPolarDataService();
        this.maneuverClassifiersCache = maneuverClassifiersCache;
        this.bestPathsCalculator = bestPathsCalculator;
        List<ManeuverForEstimation> usefulManeuvers = EstimationDataUtil
                .getUsefulManeuversSortedByTimePoint(competitorTracks);
        for (ManeuverForEstimation maneuver : usefulManeuvers) {
            appendManeuverAsGraphLevel(maneuver);
        }
    }

    private void appendManeuverAsGraphLevel(ManeuverForEstimation maneuver) {
        ManeuverClassifier bestClassifier = maneuverClassifiersCache.getBestClassifier(maneuver);
        ManeuverEstimationResult maneuverEstimationResult = bestClassifier.classifyManeuver(maneuver);
        ManeuverNodeGraphLevel newManeuverNodesLevel = new ManeuverNodeGraphLevel(maneuver, maneuverEstimationResult);
        if (firstGraphLevel == null) {
            firstGraphLevel = newManeuverNodesLevel;
            lastGraphLevel = newManeuverNodesLevel;

        } else {
            lastGraphLevel.appendNextManeuverNodesLevel(newManeuverNodesLevel);
            lastGraphLevel = newManeuverNodesLevel;
        }
        bestPathsCalculator.computeBestPathsToNextLevel(newManeuverNodesLevel);
    }

    public ManeuverNodeGraphLevel getFirstGraphLevel() {
        return firstGraphLevel;
    }

    public ManeuverNodeGraphLevel getLastGraphLevel() {
        return lastGraphLevel;
    }

    public PolarDataService getPolarService() {
        return polarService;
    }

    public List<WindWithConfidence<ManeuverForEstimation>> estimateWindTrack() {
        List<WindWithConfidence<ManeuverForEstimation>> windTrack = Collections.emptyList();
        ManeuverNodeGraphLevel lastGraphLevel = this.lastGraphLevel;
        if (lastGraphLevel != null) {
            List<Pair<ManeuverNodeGraphLevel, ManeuverNode>> bestPath = bestPathsCalculator.getBestPath(lastGraphLevel);
            windTrack = bestPathsCalculator.getWindTrack(bestPath);
        }
        return windTrack;
    }

}
