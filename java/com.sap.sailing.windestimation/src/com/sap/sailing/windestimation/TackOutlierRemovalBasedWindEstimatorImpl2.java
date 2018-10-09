package com.sap.sailing.windestimation;

import java.util.List;

import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverClassifiersCache;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverFeatures;
import com.sap.sailing.windestimation.polarsfitting.PolarsFittingWindEstimation;
import com.sap.sailing.windestimation.tackoutlierremoval.TackOutlierRemovalWindEstimator2;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class TackOutlierRemovalBasedWindEstimatorImpl2 extends AbstractManeuverForEstimationBasedWindEstimatorImpl {

    private final PolarDataService polarService;
    private final ManeuverFeatures maneuverFeatures;

    public TackOutlierRemovalBasedWindEstimatorImpl2(PolarDataService polarService, ManeuverFeatures maneuverFeatures) {
        this.polarService = polarService;
        this.maneuverFeatures = maneuverFeatures;
    }

    @Override
    public List<WindWithConfidence<Void>> estimateWindTrackWithManeuvers(
            RaceWithEstimationData<ManeuverForEstimation> race) {
        TackOutlierRemovalWindEstimator2 estimator = new TackOutlierRemovalWindEstimator2(race.getCompetitorTracks(),
                new ManeuverClassifiersCache(60000, maneuverFeatures, polarService),
                maneuverFeatures.isPolarsInformation() ? new PolarsFittingWindEstimation(polarService) : null);
        return estimator.estimateWindTrack();
    }
}
