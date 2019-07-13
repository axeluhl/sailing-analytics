package com.sap.sailing.windestimation.aggregator.msthmm.graph;

import java.util.function.Function;

public interface DijsktraShortestPathFinder<T extends ElementWithQuality> {
    /**
     * For a graph with nodes that have a "quality" metric, given a start node, a relation that maps a node to its
     * successors, and a quality metric for edges connecting two nodes, this method determines the shortest path
     * from the {@code startNode} to the {@code endNode}, assuming such a path exists. Otherwise, {@code null}
     * is returned.
     */
    Iterable<T> getShortestPath(T startNode, T endNode, Function<T, Iterable<T>> successorSupplier,
            ElementAdjacencyQualityMetric<T> edgeQualitySupplier);
}
