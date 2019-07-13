package com.sap.sailing.windestimation.aggregator.msthmm.graph;

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

public class DijkstraShortestPathFinderImpl<T extends ElementWithQuality> implements DijsktraShortestPathFinder<T> {
    @Override
    public Iterable<T> getShortestPath(T startNode, T endNode, Function<T, Iterable<T>> successorSupplier,
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
        return visited.contains(endNode) ? getPath(predecessorsInBestPath, endNode) : null;
    }

    private double getPathQuality(double qualityOfPathToCurrent, T currentNode,
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
