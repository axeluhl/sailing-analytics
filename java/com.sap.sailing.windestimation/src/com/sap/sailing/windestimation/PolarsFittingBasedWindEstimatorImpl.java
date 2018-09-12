package com.sap.sailing.windestimation;

import java.util.List;

import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;
import com.sap.sailing.windestimation.data.transformer.EstimationDataUtil;
import com.sap.sailing.windestimation.polarsfitting.PolarsFittingWindEstimation;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class PolarsFittingBasedWindEstimatorImpl extends AbstractManeuverForEstimationBasedWindEstimatorImpl {

    private final PolarDataService polarService;

    public PolarsFittingBasedWindEstimatorImpl(PolarDataService polarService) {
        this.polarService = polarService;
    }

    @Override
    public List<WindWithConfidence<Void>> estimateWindTrackWithManeuvers(
            RaceWithEstimationData<ManeuverForEstimation> race) {
        PolarsFittingWindEstimation windEstimation = new PolarsFittingWindEstimation(polarService,
                race.getCompetitorTracks());
        List<ManeuverForEstimation> usefulManeuvers = EstimationDataUtil
                .getUsefulManeuversSortedByTimePoint(race.getCompetitorTracks());
        WindTrackEstimatorAdapter adapter = new WindTrackEstimatorAdapter(windEstimation, usefulManeuvers);
        return adapter.estimateWindTrack();
    }
}
