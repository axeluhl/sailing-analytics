package com.sap.sailing.windestimation;

import java.util.List;

import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.transformer.ManeuverForEstimationTransformer;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverClassifiersCache;
import com.sap.sailing.windestimation.maneuvergraph.maneuvernode.ManeuverNodeBestPathsCalculator;
import com.sap.sailing.windestimation.maneuvergraph.maneuvernode.ManeuverTypeSequenceGraph;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverTypeGraphBasedWindEstimatorImpl
        extends AbstractWindEstimatorImpl<CompleteManeuverCurveWithEstimationData> {

    private final ManeuverForEstimationTransformer maneuverForEstimationTransformer = new ManeuverForEstimationTransformer();

    public ManeuverTypeGraphBasedWindEstimatorImpl(PolarDataService polarService) {
        super(polarService);
    }

    @Override
    protected List<WindWithConfidence<ManeuverForEstimation>> estimateWindByFilteredCompetitorTracks(
            List<CompetitorTrackWithEstimationData<CompleteManeuverCurveWithEstimationData>> filteredCompetitorTracks) {
        List<CompetitorTrackWithEstimationData<ManeuverForEstimation>> transformedCompetitorTracks = maneuverForEstimationTransformer
                .transform(filteredCompetitorTracks);
        ManeuverTypeSequenceGraph maneuverGraph = new ManeuverTypeSequenceGraph(transformedCompetitorTracks,
                new ManeuverClassifiersCache(60000, false, true, getPolarService()), new ManeuverNodeBestPathsCalculator());
        return maneuverGraph.estimateWindTrack();
    }

    @Override
    protected void filterOutIrrelevantElementsFromCompetitorTracks(
            List<CompetitorTrackWithEstimationData<CompleteManeuverCurveWithEstimationData>> filteredCompetitorTracks) {
    }

}
