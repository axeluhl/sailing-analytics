package com.sap.sailing.windestimation.aggregator.msthmm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.sap.sailing.windestimation.aggregator.graph.DijkstraShortestPathFinderImpl;
import com.sap.sailing.windestimation.aggregator.graph.DijsktraShortestPathFinder;
import com.sap.sailing.windestimation.aggregator.graph.DijsktraShortestPathFinder.Result;
import com.sap.sailing.windestimation.aggregator.graph.ElementAdjacencyQualityMetric;
import com.sap.sailing.windestimation.aggregator.graph.InnerGraphSuccessorSupplier;
import com.sap.sailing.windestimation.aggregator.graph.Tree;
import com.sap.sailing.windestimation.aggregator.hmm.GraphLevelInference;
import com.sap.sailing.windestimation.aggregator.hmm.GraphNode;
import com.sap.sailing.windestimation.aggregator.hmm.IntersectedWindRange;
import com.sap.sailing.windestimation.aggregator.hmm.WindCourseRange;
import com.sap.sailing.windestimation.aggregator.hmm.WindCourseRange.CombinationModeOnViolation;
import com.sap.sailing.windestimation.aggregator.msthmm.MstManeuverGraphGenerator.MstManeuverGraphComponents;
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;

/**
 * Infers best path within Minimum Spanning Tree (MST) using an adapted variant of Viterbi for conventional HMM models
 * which allows to label each provided maneuver with its most suitable maneuver type.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class MstBestPathsCalculatorImpl implements MstBestPathsCalculator {

    private final MstGraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator;

    public MstBestPathsCalculatorImpl(MstGraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator) {
        this.transitionProbabilitiesCalculator = transitionProbabilitiesCalculator;
    }

    @Override
    public MstGraphNodeTransitionProbabilitiesCalculator getTransitionProbabilitiesCalculator() {
        return transitionProbabilitiesCalculator;
    }

//    @Override
    public Iterable<GraphLevelInference<MstGraphLevel>> getBestNodes_Original(MstManeuverGraphComponents graphComponents) {
        Map<MstGraphLevel, MstBestPathsPerLevel> bestPathsPerLevel = new HashMap<>();
        // trace back to the graphComponents' root starting at the leaves
        for (MstGraphLevel currentLevel : graphComponents.getLeaves()) {
            MstBestPathsPerLevel bestPathsUntilLevel = new MstBestPathsPerLevel(currentLevel);
            // initialize bestPathsUntilLevel using the confidences of the maneuver classifications
            // in the leaf node:
            for (GraphNode<MstGraphLevel> currentNode : currentLevel.getLevelNodes()) {
                double probability = currentNode.getConfidence();
                bestPathsUntilLevel.addBestPreviousNodeInfo(currentNode, null, probability,
                        currentNode.getValidWindRange().toIntersected());
            }
            bestPathsPerLevel.put(currentLevel, bestPathsUntilLevel);
            while ((currentLevel = currentLevel.getParent()) != null) {
                // computeBestPathsToNextLevel will return immediately if not all children have been
                // evaluated yet and will return false in this case;
                // this leads to something like a "breadth-first search" which first
                // evaluates all children of a parent, and only after evaluating the last child can
                // progress to the common parent.
                // Ascending further after the computeBestPathsToNextLevel for currentLevel has been aborted
                // doesn't seem to make much sense because for all parents up to the root not all their children
                // have been evaluated, so all those calls again are aborted.
                if (!computeBestPathsToNextLevel(currentLevel, bestPathsPerLevel)) {
                    break;
                }
            }
        }
        List<GraphLevelInference<MstGraphLevel>> inference = inferShortestPath(graphComponents.getRoot(), bestPathsPerLevel);
        return inference;
    }
    
    /**
     * The {@code graphComponents} represent a {@link Tree} of {@link MstGraphLevel} nodes, each of which contains a
     * {@link GraphNode} per possible maneuver classification, e.g., one for the "tack" hypothesis, one for the "gybe"
     * hypothesis, and so on. These inner nodes are virtually connected with the nodes of the parent and child
     * {@link MstGraphLevel} objects. These virtual connections are rated using the
     * {@link MstGraphNodeTransitionProbabilitiesCalculator} which takes pair of {@link GraphNode}s and judges the
     * likelihood of the true wind direction (TWD) change implied by their hypothetical classifications, considering
     * their time and space distance from each other.
     * <p>
     * 
     * A path in the tree from a leaf to the root hence forms a graph over the {@link GraphNode}s contained in this
     * path, with the virtual edges connecting the nodes of adjacent overarching tree nodes. We apply a
     * {@link DijkstraShortestPathFinder Dijkstra Shortest Path algorithm} to the inner graph produced by the path from
     * each leaf to the root, equating "short" with "high quality" and considering the quality of a path to be the
     * product of the {@link GraphNode}s' {@link GraphNode#getQuality() quality} and the transition probability computed
     * by the {@link MstGraphNodeTransitionProbabilitiesCalculator}. The {@link InnerGraphSuccessorSupplier} also adds
     * artificial root and leaf nodes to the inner graph to have a single start and end node.
     * <p>
     * 
     * Solving the "shortest path" problem for each leaf-to-root path gives as many picks of a "best" {@link GraphNode}
     * per {@link MstGraphLevel} as there are paths from leaves to the tree root passing through that
     * {@link MstGraphLevel} tree node. In particular, there will be exactly one such pick for all leaves, and as many
     * for the root as there are leaves.
     * <p>
     * 
     * If several "best picks" exist for a {@link MstGraphLevel} tree node, they may not be consistent. In case there is
     * a tie (e.g., one path picking "tack" and one path picking "gybe"), no selection is made. If there is a majority,
     * the majority's decision is selected, and the ratio of paths with the winning selection over the total number of
     * paths through that node, multiplied by the {@link DijsktraShortestPathFinder.Result#getPathQuality() overall path
     * probability} is used to scale the maneuver classification's confidence.
     * 
     * @return a set of {@link GraphLevelInference} objects for a subset of the {@link MstGraphLevel} objects in the
     *         graph. For some nodes of the overarching tree no conclusion may have been reached for a valid
     *         classification which is why this may cover only a true subset of the nodes of the overarching tree.
     */
    @Override
    public Iterable<GraphLevelInference<MstGraphLevel>> getBestNodes(MstManeuverGraphComponents graphComponents) {
        // judge the quality of edges between GraphNode objects in two adjacent overarching tree nodes by
        // taking their TWD ranges and asking the transition probabilities calculator for the 
        final ElementAdjacencyQualityMetric<GraphNode<MstGraphLevel>> edgeQualityMetric = (previousNode, currentNode) -> {
            return transitionProbabilitiesCalculator.getTransitionProbability(currentNode, previousNode,
                    previousNode.getGraphLevel()==null ?
                            /*artificial node */ 0.0 :
                                previousNode.getGraphLevel().getDistanceToParent());
        };
        final Set<Result<GraphNode<MstGraphLevel>>> dijkstraShortestPathResults = new HashSet<>();
        for (MstGraphLevel leaf : graphComponents.getLeaves()) {
            final DijsktraShortestPathFinder<GraphNode<MstGraphLevel>> dijsktraShortestPathFinder = new DijkstraShortestPathFinderImpl<>();
            final InnerGraphSuccessorSupplier<GraphNode<MstGraphLevel>, MstGraphLevel> innerGraphSuccessorSupplier =
                    new InnerGraphSuccessorSupplier<GraphNode<MstGraphLevel>, MstGraphLevel>(graphComponents,
                    // supplier for artificial nodes; always full confidence and full possible wind course range
                    (final String name)->new GraphNode<MstGraphLevel>(/* maneuverType */ null, /* tackAfter */ null, new WindCourseRange(0, 360), /* confidence */ 1.0, /* indexInLevel */ 0, /* graphLevel */ null) {
                        @Override
                        public String toString() {
                            return name;
                        }
            });
            final Result<GraphNode<MstGraphLevel>> shortestPathResult = dijsktraShortestPathFinder.getShortestPath(
                    innerGraphSuccessorSupplier.getArtificialLeaf(leaf),
                    innerGraphSuccessorSupplier.getArtificialRoot(), innerGraphSuccessorSupplier, edgeQualityMetric);
            dijkstraShortestPathResults.add(shortestPathResult);
        }
        return disambiguate(dijkstraShortestPathResults);
    }

    /**
     * Receives a "shortest path", in other words a path with optimal overall "quality", for the path from each of the
     * overarching tree's leaves up to its root, having picked a {@link GraphNode} in each node in the overarching
     * tree's path from leaf to root. When an overarching tree node has multiple such paths traverse it, results from
     * the different paths may be inconsistent. In this case, disambiguation happens based on the
     * {@link DijsktraShortestPathFinder.Result#getPathQuality() overall path quality} and the ratio of paths coming to
     * the same result for the node. The {@link GraphLevelInference#getConfidence() confidence} of the disambiguation
     * result is determined from the path qualities and the ratio of votes for the disambiguation result.
     * <p>
     * 
     * For example, if one of two paths decide on tack, the other on gybe, the path qualities are compared. If one path
     * has much better quality than the other, the decision is more confident. However, if both path qualities are
     * similar, disambiguation is of low confidence.
     * <p>
     * 
     * Selection confidence is computed using the inverted probabilities/qualities of the paths, as follows: the
     * probability that selection X is wrong is the product of all inverse probabilities of all paths selecting X
     * 
     * 
     * Note that the overall path qualities already consider the quality of each node that the path contains.
     * <p>
     * 
     * Problems:
     * <ul>
     * <li>Long paths have numerically lower quality due to multiplying the qualities per step; still, each verdict
     * resulting from long paths shouldn't be less confident than a verdict from a shorter path. It seems that some
     * level of normalization may be required, e.g., by taking the n-th root with n being the path length.</li>
     * <li>How to combine the path probabilities in a level? More equal verdicts should improve a verdict's quality.
     * Path verdicts deviating from the verdict selected shall reduce the verdict's quality. It sounds a bit like an
     * inverted variance metric: the less variance, the higher the quality of a verdict.</li>
     * <li>Can a "inverse variance-based" metric be implemented by taking the normalized path probabilities, inverting
     * and multiplying them for the majority matches, then inverting again to obtain the positive probability again?
     * </ul>
     */
    private Iterable<GraphLevelInference<MstGraphLevel>> disambiguate(Set<Result<GraphNode<MstGraphLevel>>> dijkstraShortestPathResults) {
        final Map<MstGraphLevel, Map<ManeuverTypeForClassification, List<Double>>> qualitiesPerTypePerTreeNode = new HashMap<>();
        for (final Result<GraphNode<MstGraphLevel>> onePathResult : dijkstraShortestPathResults) {
            final double normalizedPathNodeQuality = Math.pow(onePathResult.getPathQuality(), -Util.size(onePathResult.getShortestPath()));
            for (final GraphNode<MstGraphLevel> nodeSelected : onePathResult.getShortestPath()) {
                if (nodeSelected.getGraphLevel() != null) { // otherwise it's an artificial root/leaf node that can be ignored here
                    Map<ManeuverTypeForClassification, List<Double>> mapForTreeNode = qualitiesPerTypePerTreeNode.computeIfAbsent(nodeSelected.getGraphLevel(),
                            k->new HashMap<>());
                    mapForTreeNode.merge(nodeSelected.getManeuverType(), new ArrayList<>(Arrays.asList(normalizedPathNodeQuality)),
                            (previousQualities, value)->{ previousQualities.add(value.get(0)); return previousQualities; });
                }
            }
        }
        // now qualitySumPerTypePerTreeNode has the sums for all MstGraphLevel tree nodes considering all paths;
        // next, find the majority verdict per MstGraphLevel
        return getMajorityVerdicts(qualitiesPerTypePerTreeNode);
    }

    private Iterable<GraphLevelInference<MstGraphLevel>> getMajorityVerdicts(
            Map<MstGraphLevel, Map<ManeuverTypeForClassification, List<Double>>> qualitiesPerTypePerTreeNode) {
        final List<GraphLevelInference<MstGraphLevel>> result = new ArrayList<>();
        for (final Entry<MstGraphLevel, Map<ManeuverTypeForClassification, List<Double>>> e : qualitiesPerTypePerTreeNode.entrySet()) {
            final MstGraphLevel treeNodeToClassify = e.getKey();
            // pass 1: find the best match based on the sums of the path qualities for each classification option found
            double maxQualitySum = -1.0;
            ManeuverTypeForClassification bestClassification = null;
            for (final Entry<ManeuverTypeForClassification, List<Double>> pathQualitiesForClassification : e.getValue().entrySet()) {
                final double probabilitySum = pathQualitiesForClassification.getValue().stream().collect(Collectors.summingDouble(d->d));
                if (probabilitySum > maxQualitySum) {
                    maxQualitySum = probabilitySum;
                    bestClassification = pathQualitiesForClassification.getKey();
                }
            }
            // pass 2: aggregate the path probabilities for the selected bestClassification option
            double invertedMatchesProduct = 1.0;
            double invertedNonMatchesProduct = 1.0;
            for (final Entry<ManeuverTypeForClassification, List<Double>> pathQualitiesForClassification : e.getValue().entrySet()) {
                // compute and multiply complement probability for those that match
                for (final Double pathQuality : pathQualitiesForClassification.getValue()) {
                    if (pathQualitiesForClassification.getKey() == bestClassification) {
                        invertedMatchesProduct *= (1.0-pathQuality);
                    } else {
                        invertedNonMatchesProduct *= (1.0-pathQuality);
                    }
                }
            }
            // then invert the product and multiply further with the complement probability of the non-matches;
            // the more paths with classifications not matching the "best" exist, the smaller the invertedNonMatchesProduct
            // will be because it is multiplied with values less than 1.0 for each non-match; the greater the
            // qualities of those paths with non-matching classifications are, the lesser the invertedNonMatchesProduct
            // value because it is based on the inverted pathQuality of the paths with non-matching classifications.
            // Conversely, the more paths with matching classifications exist, and the better the quality of those paths,
            // the less the invertedMatchesProduct will be because it aggregates the inverted path qualities.
            // This is then inverted here, thus giving better quality for more and better paths with matching
            // classifications.
            final double bestClassificationQuality = (1.0-invertedMatchesProduct) * invertedNonMatchesProduct;
            result.add(new GraphLevelInference<MstGraphLevel>(treeNodeToClassify.getLevelNodes().get(bestClassification.ordinal()),
                    bestClassificationQuality));
        }
        return result;
    }

    private List<GraphLevelInference<MstGraphLevel>> inferShortestPath(MstGraphLevel root,
            Map<MstGraphLevel, MstBestPathsPerLevel> bestPathsPerLevel) {
        MstBestPathsPerLevel bestPathsUntilLevel = bestPathsPerLevel.get(root);
        double maxProbability = 0;
        GraphNode<MstGraphLevel> bestRootNode = null;
        List<GraphLevelInference<MstGraphLevel>> result = new ArrayList<>();
        for (GraphNode<MstGraphLevel> lastNode : root.getLevelNodes()) {
            double probability = bestPathsUntilLevel.getBestPreviousNodeInfo(lastNode).getProbabilityFromStart();
            if (maxProbability < probability) {
                maxProbability = probability;
                bestRootNode = lastNode;
            }
        }
        double confidence = maxProbability;
        GraphLevelInference<MstGraphLevel> entry = new GraphLevelInference<>(bestRootNode, confidence * bestRootNode.getConfidence());
        result.add(entry);
        inferShortestPath(root, bestRootNode, confidence, result, bestPathsPerLevel);
        return result;
    }

    private void inferShortestPath(MstGraphLevel lastLevel, GraphNode<MstGraphLevel> lastNode, double confidence,
            List<GraphLevelInference<MstGraphLevel>> result, Map<MstGraphLevel, MstBestPathsPerLevel> bestPathsPerLevel) {
        if (Util.isEmpty(lastLevel.getChildren())) {
            return;
        }
        MstBestPathsPerLevel lastLevelInfo = bestPathsPerLevel.get(lastLevel);
        MstBestManeuverNodeInfo lastNodeInfo = lastLevelInfo.getBestPreviousNodeInfo(lastNode);
        for (Pair<MstGraphLevel, GraphNode<MstGraphLevel>> child : lastNodeInfo.getPreviousGraphLevelsWithBestPreviousNodes()) {
            MstGraphLevel currentLevel = child.getA();
            GraphNode<MstGraphLevel> currentNode = child.getB();
            while (currentLevel != null) {
                GraphLevelInference<MstGraphLevel> entry = new GraphLevelInference<>(currentNode, confidence * currentNode.getConfidence());
                result.add(entry);
                if (Util.size(currentLevel.getChildren()) == 1) {
                    MstBestPathsPerLevel currentLevelInfo = bestPathsPerLevel.get(currentLevel);
                    MstBestManeuverNodeInfo currentNodeInfo = currentLevelInfo.getBestPreviousNodeInfo(currentNode);
                    currentNode = currentNodeInfo.getPreviousGraphLevelsWithBestPreviousNodes().get(0).getB();
                    currentLevel = currentLevel.getChildren().iterator().next();
                } else {
                    inferShortestPath(currentLevel, currentNode, confidence, result, bestPathsPerLevel);
                    currentLevel = null;
                }
            }
        }
    }

    /**
     * @return {@code false} if no update was computed for the {@code currentLevel}; {@code true} otherwise. Callers can
     *         use this information to stop any ascending in the MST towards the root because calls for direct and
     *         transitive parent nodes will abort, too, due to missing information for {@code currentLevel}.
     */
    private boolean computeBestPathsToNextLevel(MstGraphLevel currentLevel,
            Map<MstGraphLevel, MstBestPathsPerLevel> bestPathsPerLevel) {
        // check that all branches until current (joining) node are processed, or else return immediately
        List<MstBestPathsPerLevel> bestPathsUntilPreviousLevels = new ArrayList<>();
        for (MstGraphLevel previousLevel : currentLevel.getChildren()) {
            MstBestPathsPerLevel bestPathsUntilPreviousLevel = bestPathsPerLevel.get(previousLevel);
            if (bestPathsUntilPreviousLevel == null) {
                return false;
            }
            bestPathsUntilPreviousLevels.add(bestPathsUntilPreviousLevel);
        }
        MstBestPathsPerLevel bestPathsUntilLevel = new MstBestPathsPerLevel(currentLevel);
        // the edges connecting the four nodes of the currentLevel to the four nodes of each child level
        // are now analyzed and evaluated. The probability for the path to the currentNode is determined
        // as the product of the probability for the path to the previous node (which already includes
        // the previous node's own classification confidence) and the transition probability to the
        // current node (determined by the TWD delta between the wind range carried through to the previous
        // node and the wind range computed for the current node) and the classification confidence of
        // the current node.
        for (GraphNode<MstGraphLevel> currentNode : currentLevel.getLevelNodes()) {
            List<Pair<MstGraphLevel, GraphNode<MstGraphLevel>>> currentNodeBestPreviousNodes = new ArrayList<>();
            double currentNodeProbabilityFromStart = currentNode.getConfidence();
            IntersectedWindRange finalBestIntersectedWindRange = null;
            for (MstBestPathsPerLevel bestPathsUntilPreviousLevel : bestPathsUntilPreviousLevels) {
                double bestProbabilityFromStart = 0;
                MstGraphLevel previousLevel = bestPathsUntilPreviousLevel.getCurrentLevel();
                GraphNode<MstGraphLevel> bestPreviousNode = null;
                IntersectedWindRange bestIntersectedWindRange = null;
                for (GraphNode<MstGraphLevel> previousNode : previousLevel.getLevelNodes()) {
                    IntersectedWindRange previousNodeIntersectedWindRange = bestPathsUntilPreviousLevel
                            .getBestPreviousNodeInfo(previousNode).getIntersectedWindRange();
                    Pair<IntersectedWindRange, Double> newWindRangeAndProbability = transitionProbabilitiesCalculator
                            .mergeWindRangeAndGetTransitionProbability(currentNode, currentLevel, new PreviousNodeInfo(
                                    previousLevel, previousNode, previousNodeIntersectedWindRange));
                    double transitionProbability = newWindRangeAndProbability.getB();
                    // TODO is normalization across different previousLevel objects correct here? Wouldn't we need to normalize across all children, if we normalize here at all?
                    double probabilityFromStart = bestPathsUntilPreviousLevel
                            .getBestPreviousNodeInfo(previousNode).getProbabilityFromStart() * transitionProbability;
                    if (probabilityFromStart > bestProbabilityFromStart) {
                        bestProbabilityFromStart = probabilityFromStart;
                        bestPreviousNode = previousNode;
                        bestIntersectedWindRange = newWindRangeAndProbability.getA();
                    }
                }
                currentNodeProbabilityFromStart *= bestProbabilityFromStart;
                currentNodeBestPreviousNodes.add(new Pair<>(previousLevel, bestPreviousNode));
                finalBestIntersectedWindRange = finalBestIntersectedWindRange == null ? bestIntersectedWindRange
                        : finalBestIntersectedWindRange.intersect(bestIntersectedWindRange,
                                CombinationModeOnViolation.EXPANSION);
            }
            bestPathsUntilLevel.addBestPreviousNodeInfo(currentNode, currentNodeBestPreviousNodes,
                    currentNodeProbabilityFromStart, finalBestIntersectedWindRange);
        }
        bestPathsPerLevel.put(currentLevel, bestPathsUntilLevel);
        return true;
    }

}
