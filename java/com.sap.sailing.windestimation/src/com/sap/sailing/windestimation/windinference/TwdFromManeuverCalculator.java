package com.sap.sailing.windestimation.windinference;

import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverWithEstimatedType;
import com.sap.sse.common.Bearing;

public interface TwdFromManeuverCalculator {
    
    Bearing getTwd(ManeuverWithEstimatedType maneuverWithEstimatedType);

}
