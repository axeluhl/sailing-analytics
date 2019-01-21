package com.sap.sailing.windestimation.windinference;

import java.io.Serializable;

import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverWithEstimatedType;
import com.sap.sse.common.Bearing;

public interface TwdFromManeuverCalculator extends Serializable {
    
    Bearing getTwd(ManeuverWithEstimatedType maneuverWithEstimatedType);

}
