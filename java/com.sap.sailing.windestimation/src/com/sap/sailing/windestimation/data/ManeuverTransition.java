package com.sap.sailing.windestimation.data;

import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;

public class ManeuverTransition {

    private final Distance distance;
    private final Duration duration;
    private final ManeuverTypeForClassification fromManeuverType;
    private final ManeuverTypeForClassification toManeuverType;

    public ManeuverTransition(Distance distance, Duration duration, ManeuverTypeForClassification fromManeuverType,
            ManeuverTypeForClassification toManeuverType) {
        this.distance = distance;
        this.duration = duration;
        this.fromManeuverType = fromManeuverType;
        this.toManeuverType = toManeuverType;
    }

    public Distance getDistance() {
        return distance;
    }

    public Duration getDuration() {
        return duration;
    }

    public ManeuverTypeForClassification getFromManeuverType() {
        return fromManeuverType;
    }

    public ManeuverTypeForClassification getToManeuverType() {
        return toManeuverType;
    }

}
