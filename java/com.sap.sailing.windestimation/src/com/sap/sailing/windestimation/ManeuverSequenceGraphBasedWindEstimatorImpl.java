package com.sap.sailing.windestimation;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.maneuvergraph.impl.CrossTrackManeuverSequenceGraph;
import com.sap.sailing.windestimation.maneuvergraph.impl.SingleTrackManeuverSequenceGraph;
import com.sap.sse.common.TimePoint;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverSequenceGraphBasedWindEstimatorImpl extends ManeuverAndPolarsBasedWindEstimatorBaseImpl {

    public ManeuverSequenceGraphBasedWindEstimatorImpl(PolarDataService polarService) {
        super(polarService);
    }

    @Override
    protected List<WindWithConfidence<TimePoint>> estimateWindByFilteredCompetitorTracks(
            List<CompetitorTrackWithEstimationData> filteredCompetitorTracks) {
        List<SingleTrackManeuverSequenceGraph> singleTrackGraphs = new ArrayList<>();
        for (CompetitorTrackWithEstimationData track : filteredCompetitorTracks) {
            SingleTrackManeuverSequenceGraph graph = new SingleTrackManeuverSequenceGraph(track.getBoatClass(),
                    getPolarService(), track.getManeuverCurves());
            singleTrackGraphs.add(graph);
        }
        CrossTrackManeuverSequenceGraph crossTrackGraph = new CrossTrackManeuverSequenceGraph(singleTrackGraphs,
                getPolarService());
        return crossTrackGraph.estimateWindTrack();
    }

}
