package com.sap.sailing.windestimation;

import java.util.List;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;

public interface WindEstimationComponent<InputType> {

    List<WindWithConfidence<Pair<Position, TimePoint>>> estimateWindTrack(InputType input);

}
