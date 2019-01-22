package com.sap.sailing.domain.windestimation;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.maneuverdetection.TrackTimeInfo;
import com.sap.sailing.domain.tracking.CompleteManeuverCurve;

public interface WindEstimationInteraction {

    void newManeuverSpotsDetected(Competitor competitor, Iterable<CompleteManeuverCurve> newManeuvers, TrackTimeInfo trackTimeInfo);

}
