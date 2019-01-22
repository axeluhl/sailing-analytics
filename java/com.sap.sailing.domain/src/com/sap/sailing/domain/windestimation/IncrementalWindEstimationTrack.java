package com.sap.sailing.domain.windestimation;

import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.tracking.WindTrack;

public interface IncrementalWindEstimationTrack extends WindTrack, WindEstimationInteraction {

    WindSource getWindSource();

}
