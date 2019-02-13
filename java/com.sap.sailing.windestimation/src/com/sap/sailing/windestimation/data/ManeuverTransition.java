package com.sap.sailing.windestimation.data;

import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;

/**
 * Represents a transition between two maneuvers which is composed of duration and distance between maneuvers, and the
 * types of both maneuvers.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
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

    @Override
    public String toString() {
        return "ManeuverTransition [distance=" + distance + ", duration=" + duration + ", fromManeuverType="
                + fromManeuverType + ", toManeuverType=" + toManeuverType + "]";
    }

}
