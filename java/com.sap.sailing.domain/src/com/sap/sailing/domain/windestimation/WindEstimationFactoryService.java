package com.sap.sailing.domain.windestimation;

import com.sap.sailing.domain.tracking.TrackedRace;

public interface WindEstimationFactoryService {

    IncrementalWindEstimationTrack createIncrementalWindEstimationTrack(TrackedRace trackedRace);

}
