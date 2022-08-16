package com.sap.sailing.windestimation.model.store;

/**
 * Defines all possible domains, where machine learning models of type {@link PersistableModel} are applied. The domain
 * name is used as identifier for top level folders/collections where the corresponding models are stored.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public enum ModelDomainType {
    MANEUVER_CLASSIFIER("ManeuverClassifier"), TWD_TRANSITION_CLASSIFIER(
            "TwdTransitionClassifier"), DISTANCE_BASED_TWD_DELTA_STD_REGRESSOR(
                    "DistanceBasedTwdDeltaStdRegressor"), DURATION_BASED_TWD_DELTA_STD_REGRESSOR(
                            "DurationBasedTwdDeltaStdRegressor");

    private final String domainName;

    private ModelDomainType(String domainName) {
        this.domainName = domainName;
    }

    public String getDomainName() {
        return domainName;
    }
}
