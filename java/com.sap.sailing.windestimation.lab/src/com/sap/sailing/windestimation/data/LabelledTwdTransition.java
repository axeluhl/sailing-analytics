package com.sap.sailing.windestimation.data;

import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;

public class LabelledTwdTransition extends TwdTransition {

    private final boolean correct;
    private final ManeuverTypeForClassification fromManeuverType;
    private final ManeuverTypeForClassification toManeuverType;
    private boolean testDataset;

    public LabelledTwdTransition(Distance distance, Duration duration, Bearing twdChange, Bearing intersectedTwdChange,
            Bearing bearingToPreviousManeuverMinusTwd, boolean correct, ManeuverTypeForClassification fromManeuverType,
            ManeuverTypeForClassification toManeuverType, boolean testDataset) {
        super(distance, duration, twdChange, intersectedTwdChange, bearingToPreviousManeuverMinusTwd);
        this.correct = correct;
        this.fromManeuverType = fromManeuverType;
        this.toManeuverType = toManeuverType;
        this.testDataset = testDataset;
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

    public boolean isTestDataset() {
        return testDataset;
    }

}
