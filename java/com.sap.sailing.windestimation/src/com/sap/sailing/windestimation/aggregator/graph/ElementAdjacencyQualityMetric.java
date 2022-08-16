package com.sap.sailing.windestimation.aggregator.graph;

/**
 * For a {@link Tree} of {@link GroupOutOfWhichToPickTheBestElement} nodes, objects of this type can tell what the
 * quality of a connection between two nodes of type {@code T} is. This establishes a "sub-graph" within the
 * {@link Tree} that contains the nodes of type {@code T} because it defines "edges" of this graph by attributing a
 * metric to the connection.
 * <p>
 * 
 * The precondition for asking the quality metric of a connection between two nodes is that the nodes of type {@code T}
 * are part of two distinct {@link GroupOutOfWhichToPickTheBestElement}s between which a direct
 * {@link TreeNode#getChildren() parent/child} relation exists, either way.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <T>
 */
@FunctionalInterface
public interface ElementAdjacencyQualityMetric<T extends ElementWithQuality> {
    /**
     * @return the quality metric of the edge connecting the two elements; the elements are expected to be part of two
     *         distinct {@link GroupOutOfWhichToPickTheBestElement} objects {@code g1} and {@code g2} such that either
     *         {@code g1}.{@link TreeNode#getChildren() getChildren()} contains {@code g2}, or
     *         {@code g2}.{@link TreeNode#getChildren() getChildren()} contains {@code g1}; in other words, {@code g1} and
     *         {@code g2} have to be "adjacent" in the graph defined by this metric. The greater the result, the better
     *         the "quality" of the connection. This type of result has to be in the same order of magnitude
     *         as the {@link ElementWithQuality#getQuality() quality measures} of the elements handled. The metrics
     *         are multiplied, just like conditional probabilities would.
     */
    double getQuality(T element1, T element2);
}
