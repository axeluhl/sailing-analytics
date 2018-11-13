package com.sap.sailing.windestimation.windinference;

import java.util.List;

import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverWithEstimatedType;

public interface WindTrackCalculator {

    List<WindWithConfidence<Void>> getWindTrackFromManeuverClassifications(
            List<ManeuverWithEstimatedType> aggregatedManeuverClassifications);

}
