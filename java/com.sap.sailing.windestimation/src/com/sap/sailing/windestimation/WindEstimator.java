package com.sap.sailing.windestimation;

import java.util.List;

import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface WindEstimator<T> {

    List<WindWithConfidence<Void>> estimateWind(RaceWithEstimationData<T> raceWithEstimationData);

}
