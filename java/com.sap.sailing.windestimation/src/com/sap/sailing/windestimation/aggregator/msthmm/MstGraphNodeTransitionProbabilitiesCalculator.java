package com.sap.sailing.windestimation.aggregator.msthmm;

import com.sap.sailing.windestimation.aggregator.hmm.GraphLevelBase;
import com.sap.sailing.windestimation.aggregator.hmm.GraphNode;
import com.sap.sailing.windestimation.aggregator.hmm.GraphNodeTransitionProbabilitiesCalculator;
import com.sap.sailing.windestimation.aggregator.hmm.IntersectedWindRange;
import com.sap.sailing.windestimation.aggregator.hmm.WindCourseRange;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sse.common.Util.Pair;

/**
 * {@link MstBestPathsCalculator}-compatible variant of {@link GraphNodeTransitionProbabilitiesCalculator}.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface MstGraphNodeTransitionProbabilitiesCalculator extends GraphNodeTransitionProbabilitiesCalculator {

    double getCompoundDistance(ManeuverForEstimation fromManeuver, ManeuverForEstimation toManeuver);

    /**
     * For two maneuver classifications ({@code previousNodeInfo} and {@code currentNode}) estimates the probability of
     * the second maneuver following the first, taking into account a "wind range" that has been built along the
     * sequence of maneuver classifications leading up to the {@code previousNode}. The idea is that true wind direction
     * changes correlate with the time difference and distance between the measurements, with measurements in close time
     * and distance vicinity yielding similar results. Based on the core capability of guessing a true wind direction
     * range from a classified maneuver ({@link GraphNode#getValidWindRange()}, if the wind ranges of two subsequent
     * maneuvers don't match up, the likelihood of this to happen can be estimated based on the time difference and
     * distance between the two maneuvers. This probability is the {@link Pair#getB() second} component of the
     * {@link Pair} returned.
     * <p>
     * 
     * The wind range built up to the {@code previousNode} maneuver is then
     * "{@link IntersectedWindRange#intersect(WindCourseRange, com.sap.sailing.windestimation.aggregator.hmm.WindCourseRange.CombinationModeOnViolation)
     * intersected}" with the wind range {@link GraphNode#getValidWindRange() produced} by the {@code currentNode}
     * maneuver and returned as the {@link Pair#getA() first} part of the {@link Pair} returned.
     * 
     * @param previousNodeInfo
     *            the {@link PreviousNodeInfo#getPreviousNode() maneuver classification} at the
     *            {@link PreviousNodeInfo#getPreviousLevel() previous level} which {@link GraphLevelBase#getManeuver()
     *            holds} the maneuver data based on which the previous node classification was determined and to which
     *            therefore the previous node {@link GraphLevelBase#getLevelNodes() belongs}; furthermore the
     *            {@link PreviousNodeInfo#getPreviousNodeIntersectedWindRange() wind range} built up to that node by
     *            intersecting with the previous node on the path leading there
     * @param currentNode
     *            the maneuver classification that "follows" the {@code previousNode} classification and that is used to
     *            compare the two true wind direction hypotheses resulting from them. It
     *            {@link GraphLevelBase#getLevelNodes() belongs} to the {@code currentLevel}.
     * @param currentLevel
     *            contains the maneuver data based on which the {@code currentNode} classification probability was
     *            calculated
     * @return the intersected wind range as the first element, the probability of the two maneuver classifications
     *         {@link previousNode} and {@link currentNode} being correct based on the true wind direction change this
     *         would imply
     */
    Pair<IntersectedWindRange, Double> mergeWindRangeAndGetTransitionProbability(GraphNode currentNode,
            MstGraphLevel currentLevel, PreviousNodeInfo previousNodeInfo);

}
