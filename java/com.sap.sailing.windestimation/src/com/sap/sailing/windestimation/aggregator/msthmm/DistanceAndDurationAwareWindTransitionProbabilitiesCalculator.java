package com.sap.sailing.windestimation.aggregator.msthmm;

import com.sap.sailing.windestimation.aggregator.hmm.GraphNode;
import com.sap.sailing.windestimation.aggregator.hmm.IntersectedWindRange;
import com.sap.sailing.windestimation.aggregator.hmm.SimpleIntersectedWindRangeBasedTransitionProbabilitiesCalculator;
import com.sap.sailing.windestimation.aggregator.hmm.WindCourseRange.CombinationModeOnViolation;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.regressor.twdtransition.GaussianBasedTwdTransitionDistributionCache;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Util.Pair;

/**
 * Transition probabilities calculator which makes use of {@link GaussianBasedTwdTransitionDistributionCache}.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class DistanceAndDurationAwareWindTransitionProbabilitiesCalculator
        extends SimpleIntersectedWindRangeBasedTransitionProbabilitiesCalculator<MstGraphLevel>
        implements MstGraphNodeTransitionProbabilitiesCalculator {

    // La place is used to prevent the probabilities from being zero
    // Gaussian probabilities get very small between 30 and 180 deg TWD delta
    private static final double LA_PLACE_TRANSITION_PROBABILITY = 0.0000000000000001;
    private final GaussianBasedTwdTransitionDistributionCache gaussianBasedTwdTransitionDistributionCache;

    public DistanceAndDurationAwareWindTransitionProbabilitiesCalculator(
            GaussianBasedTwdTransitionDistributionCache gaussianBasedTwdTransitionDistributionCache,
            boolean propagateIntersectedWindRangeOfHeadupAndBearAway) {
        super(propagateIntersectedWindRangeOfHeadupAndBearAway);
        this.gaussianBasedTwdTransitionDistributionCache = gaussianBasedTwdTransitionDistributionCache;
    }

    @Override
    protected double getPenaltyFactorForTransition(TwdTransition twdTransition) {
        double penaltyFactor = gaussianBasedTwdTransitionDistributionCache.getProbability(twdTransition);
        return penaltyFactor + LA_PLACE_TRANSITION_PROBABILITY;
    }

    public double getCompoundDistance(ManeuverForEstimation fromManeuver, ManeuverForEstimation toManeuver) {
        Duration durationPassed = getDuration(fromManeuver, toManeuver);
        Distance distancePassed = getDistance(fromManeuver, toManeuver);
        TwdTransition twdTransition = constructTwdTransition(durationPassed, distancePassed, 0,
                ManeuverTypeForClassification.TACK, ManeuverTypeForClassification.TACK);
        double compoundDistance = gaussianBasedTwdTransitionDistributionCache.getCompoundDistance(twdTransition);
        return compoundDistance;
    }

    @Override
    public double getTransitionProbability(GraphNode<MstGraphLevel> currentNode, GraphNode<MstGraphLevel> previousNode,
            double combinedDistanceBetweenNodes) {
        final IntersectedWindRange intersectedWindRange = previousNode.getValidWindRange()
                .intersect(currentNode.getValidWindRange(), CombinationModeOnViolation.INTERSECTION);
        final double transitionProbability = gaussianBasedTwdTransitionDistributionCache
                .getGaussianProbability(combinedDistanceBetweenNodes, intersectedWindRange.getViolationRange())
                + LA_PLACE_TRANSITION_PROBABILITY;
        return transitionProbability;
    }
    
    @Override
    public Pair<IntersectedWindRange, Double> mergeWindRangeAndGetTransitionProbability(GraphNode<MstGraphLevel> currentNode,
            MstGraphLevel currentLevel, PreviousNodeInfo previousNodeInfo) {
        IntersectedWindRange[] intersectedWindRanges = new IntersectedWindRange[currentLevel.getLevelNodes().size()];
        MstGraphLevel previousLevel = previousNodeInfo.getPreviousLevel();
        double stdSum = previousLevel.getDistanceToParent();
        for (GraphNode<MstGraphLevel> node : currentLevel.getLevelNodes()) {
            IntersectedWindRange intersectedWindRange = previousNodeInfo.getPreviousNodeIntersectedWindRange()
                    .intersect(node.getValidWindRange(), CombinationModeOnViolation.INTERSECTION);
            int nodeIndex = node.getIndexInLevel();
            intersectedWindRanges[nodeIndex] = intersectedWindRanges[nodeIndex] == null ? intersectedWindRange
                    : intersectedWindRanges[nodeIndex].intersect(intersectedWindRange,
                            CombinationModeOnViolation.EXPANSION);
        }
        double transitionProbabilitySum = 0;
        double transitionProbabilityUntilCurrentNode = -1;
        IntersectedWindRange intersectedWindRangeUntilCurrentNode = null;
        for (int i = 0; i < intersectedWindRanges.length; i++) {
            IntersectedWindRange intersectedWindRange = intersectedWindRanges[i];
            double transitionProbability = gaussianBasedTwdTransitionDistributionCache.getGaussianProbability(stdSum,
                    intersectedWindRange.getViolationRange());
            transitionProbability += LA_PLACE_TRANSITION_PROBABILITY;
            transitionProbabilitySum += transitionProbability;
            if (i == currentNode.getIndexInLevel()) {
                transitionProbabilityUntilCurrentNode = transitionProbability;
                intersectedWindRangeUntilCurrentNode = propagateIntersectedWindRangeOfHeadupAndBearAway
                        && (currentNode.getManeuverType() == ManeuverTypeForClassification.BEAR_AWAY
                                || currentNode.getManeuverType() == ManeuverTypeForClassification.HEAD_UP)
                                        ? intersectedWindRange
                                        : currentNode.getValidWindRange().toIntersected();
            }
        }
        if (transitionProbabilityUntilCurrentNode < 0) {
            throw new IllegalArgumentException("currentNode not contained in currentLevel");
        }
        double normalizedTransitionProbabilityUntilCurrentNode = transitionProbabilityUntilCurrentNode
                / transitionProbabilitySum;
        return new Pair<>(intersectedWindRangeUntilCurrentNode, normalizedTransitionProbabilityUntilCurrentNode);
    }

}
