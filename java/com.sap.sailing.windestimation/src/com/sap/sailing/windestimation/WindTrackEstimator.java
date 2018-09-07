package com.sap.sailing.windestimation;

import java.util.List;

import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;

public interface WindTrackEstimator {

    List<WindWithConfidence<ManeuverForEstimation>> estimateWindTrack();

}
