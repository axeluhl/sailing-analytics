package com.sap.sailing.windestimation.data;

import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;

public class LabelledTwdTransition extends TwdTransition {

    private final boolean correct;
    private boolean testDataset;

    public LabelledTwdTransition(Distance distance, Duration duration, Bearing twdChange, boolean correct,
            ManeuverTypeForClassification fromManeuverType, ManeuverTypeForClassification toManeuverType,
            boolean testDataset) {
        super(distance, duration, twdChange, fromManeuverType, toManeuverType);
        this.correct = correct;
        this.testDataset = testDataset;
    }

    public boolean isCorrect() {
        return correct;
    }

    public boolean isTestDataset() {
        return testDataset;
    }

    @Override
    public String toString() {
        return "LabelledTwdTransition [correct=" + correct + ", testDataset=" + testDataset + ", " + super.toString()
                + "]";
    }

}
