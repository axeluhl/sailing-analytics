package com.sap.sailing.windestimation;

import java.util.List;

import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.transformer.ManeuverForEstimationTransformer;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverClassifiersCache;
import com.sap.sailing.windestimation.maneuvergraph.PointOfSailSequenceGraph;
import com.sap.sailing.windestimation.maneuvergraph.bestpath.BestPathsCalculator;
import com.sap.sse.common.TimePoint;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverSequenceGraphBasedWindEstimatorImpl
        extends AbstractWindEstimatorImpl<CompleteManeuverCurveWithEstimationData> {

    private final ManeuverForEstimationTransformer maneuverForEstimationTransformer = new ManeuverForEstimationTransformer();

    public ManeuverSequenceGraphBasedWindEstimatorImpl(PolarDataService polarService) {
        super(polarService);
    }

    @Override
    protected List<WindWithConfidence<TimePoint>> estimateWindByFilteredCompetitorTracks(
            List<CompetitorTrackWithEstimationData<CompleteManeuverCurveWithEstimationData>> filteredCompetitorTracks) {
        List<CompetitorTrackWithEstimationData<ManeuverForEstimation>> transformedCompetitorTracks = maneuverForEstimationTransformer
                .transform(filteredCompetitorTracks);
        PointOfSailSequenceGraph maneuverGraph = new PointOfSailSequenceGraph(transformedCompetitorTracks,
                new ManeuverClassifiersCache(60000, false, getPolarService()), new BestPathsCalculator());
        return maneuverGraph.estimateWindTrack();
    }

    @Override
    protected void filterOutIrrelevantElementsFromCompetitorTracks(
            List<CompetitorTrackWithEstimationData<CompleteManeuverCurveWithEstimationData>> filteredCompetitorTracks) {
    }

}
