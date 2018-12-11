package com.sap.sailing.windestimation.data;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;

public class LabelledTwdTransition extends TwdTransition {

    private final boolean correct;
    private final ManeuverTypeForClassification fromManeuverType;
    private final ManeuverTypeForClassification toManeuverType;
    private final String regattaName;

    public LabelledTwdTransition(Distance distance, Duration duration, BoatClass boatClass, Bearing twdChange,
            Bearing intersectedTwdChange, Bearing bearingToPreviousManeuverMinusTwd, boolean correct,
            ManeuverTypeForClassification fromManeuverType, ManeuverTypeForClassification toManeuverType,
            String regattaName) {
        super(distance, duration, boatClass, twdChange, intersectedTwdChange, bearingToPreviousManeuverMinusTwd);
        this.correct = correct;
        this.fromManeuverType = fromManeuverType;
        this.toManeuverType = toManeuverType;
        this.regattaName = regattaName;
    }

    public boolean isCorrect() {
        return correct;
    }

    public ManeuverTypeForClassification getFromManeuverType() {
        return fromManeuverType;
    }

    public ManeuverTypeForClassification getToManeuverType() {
        return toManeuverType;
    }

    public String getRegattaName() {
        return regattaName;
    }

}
