package com.sap.sailing.windestimation.aggregator.advancedhmm;

import com.sap.sailing.windestimation.aggregator.hmm.SimpleIntersectedWindRangeBasedTransitionProbabilitiesCalculator;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.regressor.twdtransition.GaussianBasedTwdTransitionDistributionCache;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;

public class DistanceAndDurationAwareWindTransitionProbabilitiesCalculator
        extends SimpleIntersectedWindRangeBasedTransitionProbabilitiesCalculator
        implements AdvancedGraphNodeTransitionProbabilitiesCalculator {

    private final GaussianBasedTwdTransitionDistributionCache gaussianBasedTwdTransitionDistributionCache;

    public DistanceAndDurationAwareWindTransitionProbabilitiesCalculator(
            GaussianBasedTwdTransitionDistributionCache gaussianBasedTwdTransitionDistributionCache,
            boolean propagateIntersectedWindRangeOfHeadupAndBearAway) {
        super(propagateIntersectedWindRangeOfHeadupAndBearAway);
        this.gaussianBasedTwdTransitionDistributionCache = gaussianBasedTwdTransitionDistributionCache;
    }

    @Override
    protected double getPenaltyFactorForTransition(TwdTransition twdTransition) {
        double penaltyFactor = gaussianBasedTwdTransitionDistributionCache.getP(twdTransition);
        return penaltyFactor;
    }

    public double getCompoundDistance(ManeuverForEstimation fromManeuver, ManeuverForEstimation toManeuver) {
        Duration durationPassed = getDuration(fromManeuver, toManeuver);
        Distance distancePassed = getDistance(fromManeuver, toManeuver);
        TwdTransition twdTransition = constructTwdTransition(durationPassed, distancePassed, 0,
                ManeuverTypeForClassification.TACK, ManeuverTypeForClassification.TACK);
        double compoundDistance = gaussianBasedTwdTransitionDistributionCache.getCompoundDistance(twdTransition);
        return compoundDistance;
    }

}
