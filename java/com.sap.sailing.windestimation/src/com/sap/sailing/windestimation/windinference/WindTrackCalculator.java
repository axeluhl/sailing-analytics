package com.sap.sailing.windestimation.windinference;

import java.util.List;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverWithEstimatedType;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;

public interface WindTrackCalculator {

    List<WindWithConfidence<Pair<Position, TimePoint>>> getWindTrackFromManeuverClassifications(
            List<ManeuverWithEstimatedType> aggregatedManeuverClassifications);

}
