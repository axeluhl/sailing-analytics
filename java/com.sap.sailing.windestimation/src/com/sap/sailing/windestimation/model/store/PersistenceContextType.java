package com.sap.sailing.windestimation.model.store;

public enum PersistenceContextType {
    MANEUVER_CLASSIFIER("ManeuverClassifier"), TWD_TRANSITION_CLASSIFIER(
            "TwdTransitionClassifier"), DISTANCE_BASED_TWD_DELTA_STD_REGRESSOR(
                    "DistanceBasedTwdDeltaStdRegressor"), DURATION_BASED_TWD_DELTA_STD_REGRESSOR(
                            "DurationBasedTwdDeltaStdRegressor");

    private final String contextName;

    private PersistenceContextType(String contextName) {
        this.contextName = contextName;
    }

    public String getContextName() {
        return contextName;
    }
}
