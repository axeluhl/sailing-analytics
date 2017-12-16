package com.sap.sailing.domain.maneuverdetection;

import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.Maneuver;

public interface IncrementalManeuverDetector extends ManeuverDetector {

    List<Maneuver> getAlreadyDetectedManeuvers(Competitor competitor);

    void clearState(Competitor competitor);

    void clearState();

}
