package com.sap.sse.util.graph;

/**
 * When transforming a {@link DirectedGraph} into a directed acyclic graph using the
 * {@link DirectedGraph#graphWithCombinedCycleNodes()}, cycles are combined into
 * single nodes representing the cycles. In particular, if there is more than one cycle
 * with overlaps, those cycles are combined into a single node. When determining
 * {@link DirectedGraph#getLengthsOfLongestPathsFromRoot() distances from root nodes},
 * equal values are to be reported for all nodes in the same cycle cluster. For
 * clients it may be necessary to understand whether two nodes are on the same cycle
 * cluster. This interface helps in detecting this.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface CycleClusters<T> {
    CycleCluster<T> getCluster(T node);
    
    /**
     * Tests whether this object contains a {@link CycleCluster} that contains both,
     * the {@code edge}'s {@link DirectedEdge#getFrom() from} and {@link DirectedEdge#getTo() to}
     * nodes.
     */
    boolean isEdgeInCycleCluster(DirectedEdge<T> edge);

    Iterable<CycleCluster<T>> getClusters();

    boolean areDisjoint();
}
