package com.sap.sailing.domain.maneuverdetection.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.CompleteManeuverCurve;

public interface WindEstimationInteraction {

    void newManeuverSpotsDetected(Competitor competitor, Iterable<CompleteManeuverCurve> newManeuvers, TrackTimeInfo trackTimeInfo);

}
