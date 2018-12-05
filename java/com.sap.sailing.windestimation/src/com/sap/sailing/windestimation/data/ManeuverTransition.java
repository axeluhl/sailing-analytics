package com.sap.sailing.windestimation.data;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;

public class ManeuverTransition {

    private final Distance distance;
    private final Duration duration;
    private final BoatClass boatClass;

    public ManeuverTransition(Distance distance, Duration duration, BoatClass boatClass) {
        this.distance = distance;
        this.duration = duration;
        this.boatClass = boatClass;
    }

    public Distance getDistance() {
        return distance;
    }

    public Duration getDuration() {
        return duration;
    }

    public BoatClass getBoatClass() {
        return boatClass;
    }

}
