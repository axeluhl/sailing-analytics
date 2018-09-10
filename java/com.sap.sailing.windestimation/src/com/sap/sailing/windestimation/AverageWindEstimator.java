package com.sap.sailing.windestimation;

import com.sap.sailing.domain.tracking.WindWithConfidence;

public interface AverageWindEstimator {

    WindWithConfidence<Void> estimateAverageWind();

}
