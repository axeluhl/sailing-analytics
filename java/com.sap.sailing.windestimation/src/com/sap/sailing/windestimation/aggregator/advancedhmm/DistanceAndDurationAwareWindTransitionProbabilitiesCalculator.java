package com.sap.sailing.windestimation.aggregator.advancedhmm;

import com.sap.sailing.windestimation.aggregator.hmm.GraphLevelBase;
import com.sap.sailing.windestimation.aggregator.hmm.GraphNode;
import com.sap.sailing.windestimation.aggregator.hmm.IntersectedWindRange;
import com.sap.sailing.windestimation.aggregator.hmm.SimpleIntersectedWindRangeBasedTransitionProbabilitiesCalculator;
import com.sap.sailing.windestimation.aggregator.hmm.WindCourseRange;
import com.sap.sailing.windestimation.aggregator.hmm.WindCourseRange.CombinationModeOnViolation;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.regressor.twdtransition.GaussianBasedTwdTransitionDistributionCache;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.DegreeBearingImpl;

public class DistanceAndDurationAwareWindTransitionProbabilitiesCalculator
        extends SimpleIntersectedWindRangeBasedTransitionProbabilitiesCalculator
        implements AdvancedGraphNodeTransitionProbabilitiesCalculator {

    private final GaussianBasedTwdTransitionDistributionCache gaussianBasedTwdTransitionDistributionCache;

    public DistanceAndDurationAwareWindTransitionProbabilitiesCalculator(
            GaussianBasedTwdTransitionDistributionCache gaussianBasedTwdTransitionDistributionCache) {
        this.gaussianBasedTwdTransitionDistributionCache = gaussianBasedTwdTransitionDistributionCache;
    }

    @Override
    public Pair<IntersectedWindRange, Double> mergeWindRangeAndGetTransitionProbability(GraphNode previousNode,
            GraphLevelBase previousLevel, IntersectedWindRange previousIntersectedWindRange, GraphNode currentNode,
            GraphLevelBase currentLevel) {
        Duration durationPassed = getDuration(previousLevel.getManeuver(), currentLevel.getManeuver());
        Distance distancePassed = getDistance(previousLevel.getManeuver(), currentLevel.getManeuver());
        double transitionProbabilitySum = 0;
        double transitionProbabilityUntilCurrentNode = -1;
        IntersectedWindRange intersectedWindRangeUntilCurrentNode = null;
        for (GraphNode node : currentLevel.getLevelNodes()) {
            WindCourseRange previousWindCourseRange = previousNode
                    .getManeuverType() == ManeuverTypeForClassification.BEAR_AWAY
                    || previousNode.getManeuverType() == ManeuverTypeForClassification.HEAD_UP
                            ? previousIntersectedWindRange
                            : previousNode.getValidWindRange();
            IntersectedWindRange intersectedWindRange = previousWindCourseRange.intersect(node.getValidWindRange(),
                    CombinationModeOnViolation.INTERSECTION);
            double transitionProbability = getPenaltyFactorForTransition(intersectedWindRange, durationPassed,
                    distancePassed);
            transitionProbabilitySum += transitionProbability;
            if (node == currentNode) {
                transitionProbabilityUntilCurrentNode = transitionProbability;
                intersectedWindRangeUntilCurrentNode = intersectedWindRange;
            }
        }
        if (transitionProbabilityUntilCurrentNode < 0) {
            throw new IllegalArgumentException("currentNode not contained in currentLevel");
        }
        double normalizedTransitionProbabilityUntilCurrentNode = transitionProbabilityUntilCurrentNode
                / transitionProbabilitySum;
        return new Pair<>(intersectedWindRangeUntilCurrentNode, normalizedTransitionProbabilityUntilCurrentNode);
    }

    protected double getPenaltyFactorForTransition(IntersectedWindRange intersectedWindRange, Duration durationPassed,
            Distance distancePassed) {
        double violationRange = intersectedWindRange.getViolationRange();
        TwdTransition twdTransition = constructTwdTransition(durationPassed, distancePassed, violationRange);
        double penaltyFactor = gaussianBasedTwdTransitionDistributionCache.getP(twdTransition);
        return penaltyFactor;
    }

    private TwdTransition constructTwdTransition(Duration durationPassed, Distance distancePassed,
            double twdChangeInDegrees) {
        DegreeBearingImpl twdChange = new DegreeBearingImpl(twdChangeInDegrees);
        TwdTransition twdTransition = new TwdTransition(distancePassed, durationPassed, twdChange, null, null);
        return twdTransition;
    }

    public double getCompoundDistance(ManeuverForEstimation fromManeuver, ManeuverForEstimation toManeuver) {
        Duration durationPassed = getDuration(fromManeuver, toManeuver);
        Distance distancePassed = getDistance(fromManeuver, toManeuver);
        TwdTransition twdTransition = constructTwdTransition(durationPassed, distancePassed, 0);
        double compoundDistance = gaussianBasedTwdTransitionDistributionCache.getCompoundDistance(twdTransition);
        return compoundDistance;
    }

    private Distance getDistance(ManeuverForEstimation fromManeuver, ManeuverForEstimation toManeuver) {
        return fromManeuver.getManeuverPosition().getDistance(toManeuver.getManeuverPosition());
    }

    private Duration getDuration(ManeuverForEstimation fromManeuver, ManeuverForEstimation toManeuver) {
        return fromManeuver.getManeuverTimePoint().until(toManeuver.getManeuverTimePoint()).abs();
    }

}
