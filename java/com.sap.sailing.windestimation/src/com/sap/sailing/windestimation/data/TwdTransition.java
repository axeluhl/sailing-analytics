package com.sap.sailing.windestimation.data;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;

public class TwdTransition extends ManeuverTransition {

    private final Bearing twdChange;
    private final Bearing intersectedTwdChange;
    private final Bearing bearingToPreviousManeuverMinusTwd;

    public TwdTransition(Distance distance, Duration duration, BoatClass boatClass, Bearing twdChange,
            Bearing intersectedTwdChange, Bearing bearingToPreviousManeuverMinusTwd) {
        super(distance, duration, boatClass);
        this.twdChange = twdChange;
        this.intersectedTwdChange = intersectedTwdChange;
        this.bearingToPreviousManeuverMinusTwd = bearingToPreviousManeuverMinusTwd;
    }

    public Bearing getTwdChange() {
        return twdChange;
    }

    public Bearing getIntersectedTwdChange() {
        return intersectedTwdChange;
    }
    
    public Bearing getBearingToPreviousManeuverMinusTwd() {
        return bearingToPreviousManeuverMinusTwd;
    }

}
