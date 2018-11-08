package com.sap.sailing.windestimation;

import java.util.List;

import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;
import com.sap.sailing.windestimation.data.transformer.ManeuverForEstimationTransformer;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public abstract class AbstractManeuverForEstimationBasedWindEstimatorImpl
        extends AbstractWindEstimatorImpl<CompleteManeuverCurveWithEstimationData> {

    private final ManeuverForEstimationTransformer maneuverForEstimationTransformer = new ManeuverForEstimationTransformer();

    @Override
    protected List<WindWithConfidence<Void>> estimateWindByFilteredCompetitorTracks(
            RaceWithEstimationData<CompleteManeuverCurveWithEstimationData> raceWithEstimationData) {
        List<CompetitorTrackWithEstimationData<ManeuverForEstimation>> transformedCompetitorTracks = maneuverForEstimationTransformer
                .transform(raceWithEstimationData.getCompetitorTracks());
        RaceWithEstimationData<ManeuverForEstimation> newRace = new RaceWithEstimationData<>(
                raceWithEstimationData.getRegattaName(), raceWithEstimationData.getRaceName(),
                transformedCompetitorTracks);
        return estimateWindTrackWithManeuvers(newRace);
    }
    
    public abstract List<WindWithConfidence<Void>> estimateWindTrackWithManeuvers(RaceWithEstimationData<ManeuverForEstimation> race);

    @Override
    protected void filterOutIrrelevantElementsFromCompetitorTracks(
            RaceWithEstimationData<CompleteManeuverCurveWithEstimationData> raceWithEstimationData) {
    }

}
