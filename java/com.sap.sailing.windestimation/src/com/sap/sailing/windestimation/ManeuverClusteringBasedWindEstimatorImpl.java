package com.sap.sailing.windestimation;

import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.polars.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverClassifiersCache;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverFeatures;
import com.sap.sailing.windestimation.maneuverclustering.ManeuverClusteringBasedWindEstimationTrackImpl;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverClusteringBasedWindEstimatorImpl extends AbstractManeuverForEstimationBasedWindEstimatorImpl {

    private final PolarDataService polarService;
    private final ManeuverFeatures maneuverFeatures;

    public ManeuverClusteringBasedWindEstimatorImpl(PolarDataService polarService, ManeuverFeatures maneuverFeatures) {
        this.polarService = polarService;
        this.maneuverFeatures = maneuverFeatures;
    }

    @Override
    public List<WindWithConfidence<Void>> estimateWindTrackWithManeuvers(
            RaceWithEstimationData<ManeuverForEstimation> race) {
        BoatClass boatClass = race.getCompetitorTracks().isEmpty() ? null
                : race.getCompetitorTracks().get(0).getBoatClass();
        ManeuverClusteringBasedWindEstimationTrackImpl windEstimator = new ManeuverClusteringBasedWindEstimationTrackImpl(
                race, boatClass, new ManeuverClassifiersCache(60000, maneuverFeatures, polarService), 30000);
        try {
            windEstimator.initialize();
            return windEstimator.estimateWindTrack();
        } catch (NotEnoughDataHasBeenAddedException e) {
            return Collections.emptyList();
        }
    }

}
