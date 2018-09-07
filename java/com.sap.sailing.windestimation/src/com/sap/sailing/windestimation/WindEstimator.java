package com.sap.sailing.windestimation;

import java.util.List;

import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface WindEstimator<T> {

    List<WindWithConfidence<ManeuverForEstimation>> estimateWind(
            Iterable<CompetitorTrackWithEstimationData<T>> competitorTracks);

}
