package com.sap.sailing.windestimation;

import java.util.List;

import com.sap.sailing.domain.tracking.WindWithConfidence;

public interface WindEstimationComponent<InputType> {

    List<WindWithConfidence<Void>> estimateWindTrack(InputType input);

}
