package com.sap.sailing.windestimation.maneuvergraph.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class AllManeuversOfRaceManeuverSequenceGraph
        extends CrossTrackManeuverSequenceGraph<SimpleCrossTrackManeuverNodesLevel> {

    public AllManeuversOfRaceManeuverSequenceGraph(
            List<CompetitorTrackWithEstimationData<CompleteManeuverCurveWithEstimationData>> competitorTracks,
            PolarDataService polarService) {
        super(getSingleTrackGraphForEachCompetitorTrack(competitorTracks, polarService),
                SimpleCrossTrackManeuverNodesLevel.getFactory(), polarService);
    }

    private static Iterable<SingleTrackManeuverSequenceGraph> getSingleTrackGraphForEachCompetitorTrack(
            List<CompetitorTrackWithEstimationData<CompleteManeuverCurveWithEstimationData>> competitorTracks,
            PolarDataService polarService) {
        List<SingleTrackManeuverSequenceGraph> singleTrackGraphs = new ArrayList<>();
        for (CompetitorTrackWithEstimationData<CompleteManeuverCurveWithEstimationData> track : competitorTracks) {
            SingleTrackManeuverSequenceGraph graph = new SingleTrackManeuverSequenceGraph(track.getBoatClass(),
                    polarService, track.getElements());
            singleTrackGraphs.add(graph);
        }
        return singleTrackGraphs;
    }

}
