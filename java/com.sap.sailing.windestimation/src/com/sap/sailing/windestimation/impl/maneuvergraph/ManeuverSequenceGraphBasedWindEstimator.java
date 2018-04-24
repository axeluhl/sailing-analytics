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

    public ManeuverSequenceGraphBasedWindEstimator(PolarDataService polarService) {
        super(polarService);
    }

    @Override
    protected List<WindWithConfidence<TimePoint>> estimateWindByFilteredCompetitorTracks(
            List<CompetitorTrackWithEstimationData> filteredCompetitorTracks) {
        List<SingleTrackManeuverSequenceGraph> singleTrackGraphs = new ArrayList<>();
        for (CompetitorTrackWithEstimationData track : filteredCompetitorTracks) {
            SingleTrackManeuverSequenceGraph graph = new SingleTrackManeuverSequenceGraph(track.getBoatClass(),
                    getPolarService(), track.getManeuverCurves());
            graph.computePossiblePathsWithDistances();
            singleTrackGraphs.add(graph);
        }
        CrossTrackManeuverSequenceGraph globalGraph = new CrossTrackManeuverSequenceGraph(singleTrackGraphs);
        globalGraph.computePossiblePathsWithDistances();
        // TODO get best possible path, convert to wind track with confidence
        return null;
    }

}
