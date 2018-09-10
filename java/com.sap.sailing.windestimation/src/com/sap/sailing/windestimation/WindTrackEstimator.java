package com.sap.sailing.windestimation;

import java.util.List;

import com.sap.sailing.domain.tracking.WindWithConfidence;

public interface WindTrackEstimator {

    List<WindWithConfidence<Void>> estimateWindTrack();

}
