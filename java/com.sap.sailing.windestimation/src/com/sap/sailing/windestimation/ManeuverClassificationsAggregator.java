package com.sap.sailing.windestimation;

import java.util.List;

import com.sap.sailing.domain.tracking.WindWithConfidence;

public interface ManeuverClassificationsAggregator {

    List<WindWithConfidence<Void>> estimateWindTrack();
    
//    List<ManeuverClassification> estimateWindTrack(RaceWithEstimationData<ManeuverClassification> race);

}
