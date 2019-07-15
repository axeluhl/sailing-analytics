package com.sap.sailing.windestimation.aggregator.graph;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;

import com.sap.sse.common.Util;

/**
 * This implementation of a Dijkstra Shortest ("best") Path algorithm uses as the metric for the "distance" from the
 * start to a target node the {@link ElementWithQuality#getQuality() "quality"} of the target element multiplied with
 * the "quality" measure of the edge leading from the current node to the target node, multiplied by the total quality
 * aggregated up to the current node (the "distance" from the start). Note that numerically this inverses the target
 * search. Where the original Dijkstra algorithm strives to <em>minimize</em> the distance to the end node, this
 * implementation strives to <em>maximize</em> the "quality" of a path from the start to the end node.
 * <p>
 * 
 * The definition of path quality is implemented in
 * {@link #getPathQuality(double, ElementWithQuality, ElementAdjacencyQualityMetric, ElementWithQuality)} which
 * subclasses may override. Keep in mind that "higher is better."
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <T>
 */
public class DijkstraShortestPathFinderImpl<T extends ElementWithQuality> implements DijsktraShortestPathFinder<T> {
    @Override
    public Result<T> getShortestPath(T startNode, T endNode, Function<T, Iterable<T>> successorSupplier,
            ElementAdjacencyQualityMetric<T> edgeQualitySupplier) {
        final Set<T> visited = new HashSet<>();
        final Map<T, T> predecessorsInBestPath = new HashMap<>();
        final Map<T, Double> qualityOfPathToNode = new HashMap<>();
        final Comparator<T> nodeByPathQualityComparator = (node1, node2)->
            Comparator.nullsFirst((Double q1, Double q2)->Double.compare(q1, q2)).compare(
                    qualityOfPathToNode.get(node1), qualityOfPathToNode.get(node2));
        final SortedSet<T> nodeWithBestQualitySoFar = new TreeSet<>(nodeByPathQualityComparator);
        // initialize for start node:
        qualityOfPathToNode.put(startNode, 1.0);
        visited.add(startNode);
        nodeWithBestQualitySoFar.add(startNode);
        // one round of progress:
        while (!visited.contains(endNode)) {
            final T currentNode = nodeWithBestQualitySoFar.last();
            nodeWithBestQualitySoFar.remove(currentNode);
            visited.add(currentNode);
            final Iterable<T> successors = successorSupplier.apply(currentNode);
            if (successors == null || Util.isEmpty(successors)) {
                break; // no more successors found
            }
            final double qualityOfPathToCurrent = qualityOfPathToNode.get(currentNode);
            for (final T successor : successors) {
                if (!visited.contains(successor)) {
                    final double qualityOfPathFromCurrentToSuccessor = getPathQuality(qualityOfPathToCurrent, currentNode, edgeQualitySupplier, successor);
                    final Double qualityOfPathToSuccessorSoFar = qualityOfPathToNode.get(successor);
                    if (qualityOfPathToSuccessorSoFar == null || qualityOfPathFromCurrentToSuccessor > qualityOfPathToSuccessorSoFar) {
                        nodeWithBestQualitySoFar.remove(successor); // before updating quality
                        qualityOfPathToNode.put(successor, qualityOfPathFromCurrentToSuccessor);
                        predecessorsInBestPath.put(successor, currentNode);
                        nodeWithBestQualitySoFar.add(successor);
                    }
                }
            }
        }
        return visited.contains(endNode) ? new Result<T>() {
            @Override
            public Iterable<T> getShortestPath() {
                return getPath(predecessorsInBestPath, endNode);
            }

            @Override
            public double getPathQuality() {
                return qualityOfPathToNode.get(endNode);
            }
        } : null;
    }

    protected double getPathQuality(double qualityOfPathToCurrent, T currentNode,
            ElementAdjacencyQualityMetric<T> edgeQualitySupplier, T successor) {
        return qualityOfPathToCurrent * edgeQualitySupplier.getQuality(currentNode, successor) * successor.getQuality();
    }

    private Iterable<T> getPath(Map<T, T> predecessors, T endNode) {
        final List<T> result = new LinkedList<>();
        T current = endNode;
        while (current != null) {
            result.add(0, current);
            current = predecessors.get(current);
        }
        return result;
    }
}
