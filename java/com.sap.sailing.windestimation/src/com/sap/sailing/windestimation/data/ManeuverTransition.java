package com.sap.sailing.windestimation.data;

import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;

public class ManeuverTransition {

    private final Distance distance;
    private final Duration duration;

    public ManeuverTransition(Distance distance, Duration duration) {
        this.distance = distance;
        this.duration = duration;
    }

    public Distance getDistance() {
        return distance;
    }

    public Duration getDuration() {
        return duration;
    }

}
