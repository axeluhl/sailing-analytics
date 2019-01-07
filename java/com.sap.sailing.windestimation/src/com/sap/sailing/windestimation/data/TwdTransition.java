package com.sap.sailing.windestimation.data;

import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;

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
