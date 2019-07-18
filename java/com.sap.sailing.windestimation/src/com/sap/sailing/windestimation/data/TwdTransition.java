package com.sap.sailing.windestimation.data;

import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;

/**
 * Represents a transition between two maneuvers which is composed of duration and distance between maneuvers, the TWD
 * delta which occurred within the transition and the types of both maneuvers.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class TwdTransition extends ManeuverTransition {

    private final Bearing twdChange;

    public TwdTransition(Distance distance, Duration duration, Bearing twdChange,
            ManeuverTypeForClassification fromManeuverType, ManeuverTypeForClassification toManeuverType) {
        super(distance, duration, fromManeuverType, toManeuverType);
        this.twdChange = twdChange;
    }

    public Bearing getTwdChange() {
        return twdChange;
    }

    @Override
    public String toString() {
        return "TwdTransition [twdChange=" + twdChange + ", " + super.toString() + "]";
    }

}
