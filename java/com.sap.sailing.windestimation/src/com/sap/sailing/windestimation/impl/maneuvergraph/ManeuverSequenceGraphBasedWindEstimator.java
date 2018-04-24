package com.sap.sailing.windestimation.impl.maneuvergraph;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.impl.ManeuverAndPolarsBasedWindEstimatorBaseImpl;
import com.sap.sse.common.TimePoint;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverSequenceGraphBasedWindEstimator extends ManeuverAndPolarsBasedWindEstimatorBaseImpl {
    
    private List<ManeuverSequenceGraph> competitorManeuverGraphs = new ArrayList<>();

    public ManeuverSequenceGraphBasedWindEstimator(PolarDataService polarService) {
        super(polarService);
    }

    @Override
    protected List<WindWithConfidence<TimePoint>> estimateWindByFilteredCompetitorTracks(
            List<CompetitorTrackWithEstimationData> filteredCompetitorTracks) {
        for (CompetitorTrackWithEstimationData track : filteredCompetitorTracks) {
            ManeuverSequenceGraph graph = new ManeuverSequenceGraph(track.getBoatClass(), getPolarService(), track.getManeuverCurves());
            graph.computePossiblePathsWithDistances();
        }
        
        return null;
    }

}
