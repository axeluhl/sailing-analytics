package com.sap.sailing.windestimation.aggregator.msthmm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.sap.sse.common.Util.Triple;

/**
 * A generic implementation of Incremental Minimum Spanning Tree (MST) builder which is capable to parse an acyclic
 * directed graph for arbitrary instances provided that the distance between instances can be measured (see
 * {@link #getDistanceBetweenObservations(Object, Object)}).
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public abstract class AbstractMstGraphGenerator<T> {

    private final List<NodeWithNeighbors<T>> nodes;

    private static final int MAX_DEPTH_FOR_DFS = 100;

    public AbstractMstGraphGenerator() {
        this(new ArrayList<>());
    }

    // for object cloning
    protected AbstractMstGraphGenerator(List<NodeWithNeighbors<T>> nodes) {
        this.nodes = nodes;
    }

    public void addNode(T observation) {
        NodeWithNeighbors<T> newNode = new NodeWithNeighbors<>(observation);
        LinkedList<NodeWithDistance<T>> distancesFromOtherNodes = getDistancesFromNearestOtherNodes(observation);
        nodes.add(newNode);
        if (!distancesFromOtherNodes.isEmpty()) {
            Collections.sort(distancesFromOtherNodes);
            NodeWithDistance<T> closestNeighborWithDistance = distancesFromOtherNodes.get(0);
            addEdge(newNode, closestNeighborWithDistance);
            for (ListIterator<NodeWithDistance<T>> iterator = distancesFromOtherNodes.listIterator(1); iterator
                    .hasNext();) {
                NodeWithDistance<T> otherCloseNeighbor = iterator.next();
                Triple<Double, NodeWithNeighbors<T>, NodeWithNeighbors<T>> canReachAndEdgeWithHighestDistance = canReach(
                        otherCloseNeighbor.getNodeWithNeighbors(), newNode);
                if (canReachAndEdgeWithHighestDistance != null
                        && canReachAndEdgeWithHighestDistance.getA() > otherCloseNeighbor.getDistance()) {
                    removeEdge(canReachAndEdgeWithHighestDistance.getB(), canReachAndEdgeWithHighestDistance.getC());
                    addEdge(newNode, otherCloseNeighbor);
                }
            }
        }
    }

    // TODO implement with Octree so that the octree returns the nearest x elements to observation
    private LinkedList<NodeWithDistance<T>> getDistancesFromNearestOtherNodes(T observation) {
        LinkedList<NodeWithDistance<T>> result = new LinkedList<>();
        for (NodeWithNeighbors<T> node : nodes) {
            double distance = getDistanceBetweenObservations(observation, node.getObservation());
            NodeWithDistance<T> neighborWithDistance = new NodeWithDistance<>(node, distance);
            result.add(neighborWithDistance);
        }
        return result;
    }

    /**
     * Gets the distance between two provided instances.
     */
    protected abstract double getDistanceBetweenObservations(T o1, T o2);

    private void addEdge(NodeWithNeighbors<T> newNode, NodeWithDistance<T> closestNeighborWithDistance) {
        newNode.addNeighbor(closestNeighborWithDistance);
        closestNeighborWithDistance.getNodeWithNeighbors().addNeighbor(newNode,
                closestNeighborWithDistance.getDistance());
    }

    private void removeEdge(NodeWithNeighbors<T> a, NodeWithNeighbors<T> b) {
        a.removeNeighbor(b);
        b.removeNeighbor(a);
    }

    private Triple<Double, NodeWithNeighbors<T>, NodeWithNeighbors<T>> canReach(NodeWithNeighbors<T> nodeToReach,
            NodeWithNeighbors<T> nodeToStartFrom) {
        return canReach(nodeToReach, nodeToStartFrom, null, 0, new Triple<>(0.0, nodeToReach, nodeToStartFrom));
    }

    private Triple<Double, NodeWithNeighbors<T>, NodeWithNeighbors<T>> canReach(NodeWithNeighbors<T> nodeToReach,
            NodeWithNeighbors<T> nodeToStartFrom, NodeWithNeighbors<T> firstNodeOfPathToIgnore, int currentDepth,
            Triple<Double, NodeWithNeighbors<T>, NodeWithNeighbors<T>> edgeWithHighestDistance) {
        if (nodeToStartFrom == nodeToReach) {
            return edgeWithHighestDistance;
        }
        if (currentDepth < MAX_DEPTH_FOR_DFS) {
            int nextDepth = currentDepth + 1;
            for (NodeWithDistance<T> neighborWithDistance : nodeToStartFrom.getNeighbors()) {
                NodeWithNeighbors<T> node = neighborWithDistance.getNodeWithNeighbors();
                if (node != firstNodeOfPathToIgnore) {
                    double distance = neighborWithDistance.getDistance();
                    Triple<Double, NodeWithNeighbors<T>, NodeWithNeighbors<T>> newEdgeWithHighestDistance = distance > edgeWithHighestDistance
                            .getA() ? new Triple<>(distance, nodeToStartFrom, node) : edgeWithHighestDistance;
                    Triple<Double, NodeWithNeighbors<T>, NodeWithNeighbors<T>> canReachAndEdgeWithHighestDistance = canReach(
                            nodeToReach, node, nodeToStartFrom, nextDepth, newEdgeWithHighestDistance);
                    if (canReachAndEdgeWithHighestDistance != null) {
                        return canReachAndEdgeWithHighestDistance;
                    }
                }
            }
        }
        return null;
    }

    public List<NodeWithNeighbors<T>> getNodes() {
        return nodes;
    }

    public List<NodeWithNeighbors<T>> getClonedNodes() {
        List<NodeWithNeighbors<T>> clonedNodes = new ArrayList<>(nodes.size());
        Map<NodeWithNeighbors<T>, NodeWithNeighbors<T>> existingToNewNodesMapping = new HashMap<>();
        for (NodeWithNeighbors<T> node : nodes) {
            NodeWithNeighbors<T> clonedNode = existingToNewNodesMapping.get(node);
            if (clonedNode == null) {
                clonedNode = new NodeWithNeighbors<T>(node.getObservation());
                existingToNewNodesMapping.put(node, clonedNode);
            }
            for (NodeWithDistance<T> neighborNodeWithDistance : node.getNeighbors()) {
                NodeWithNeighbors<T> neighborNode = neighborNodeWithDistance.getNodeWithNeighbors();
                NodeWithNeighbors<T> clonedNeighborNode = existingToNewNodesMapping.get(neighborNode);
                if (clonedNeighborNode == null) {
                    clonedNeighborNode = new NodeWithNeighbors<T>(neighborNode.getObservation());
                    existingToNewNodesMapping.put(neighborNode, clonedNeighborNode);
                }
                NodeWithDistance<T> clonedNeighborNodeWithDistance = new NodeWithDistance<>(clonedNeighborNode,
                        neighborNodeWithDistance.getDistance());
                clonedNode.addNeighbor(clonedNeighborNodeWithDistance);
            }
            clonedNodes.add(clonedNode);
        }
        return clonedNodes;
    }

    protected static class NodeWithNeighbors<T> {
        private final List<NodeWithDistance<T>> neighbors = new ArrayList<>(2);
        private final T observation;

        public NodeWithNeighbors(T observation) {
            this.observation = observation;
        }

        public void addNeighbor(NodeWithNeighbors<T> nodeWithNeighbors, double distance) {
            NodeWithDistance<T> neighborWithDistance = new NodeWithDistance<>(nodeWithNeighbors, distance);
            addNeighbor(neighborWithDistance);
        }

        public void addNeighbor(NodeWithDistance<T> neighborWithDistance) {
            neighbors.add(neighborWithDistance);
        }

        @SuppressWarnings("unlikely-arg-type")
        public void removeNeighbor(NodeWithNeighbors<T> nodeWithNeighbors) {
            neighbors.remove(nodeWithNeighbors);
        }

        public List<NodeWithDistance<T>> getNeighbors() {
            return neighbors;
        }

        public T getObservation() {
            return observation;
        }

        @Override
        public String toString() {
            return "NodeWithNeighbors [observation=" + observation + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((observation == null) ? 0 : observation.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj instanceof NodeWithDistance<?>) {
                if (((NodeWithDistance<?>) obj).neighbor == this) {
                    return true;
                }
            }
            return false;
        }
    }

    protected static class NodeWithDistance<T> implements Comparable<NodeWithDistance<T>> {
        private final NodeWithNeighbors<T> neighbor;
        private final double distance;

        public NodeWithDistance(NodeWithNeighbors<T> neighbor, double distance) {
            this.neighbor = neighbor;
            this.distance = distance;
        }

        public NodeWithNeighbors<T> getNodeWithNeighbors() {
            return neighbor;
        }

        public double getDistance() {
            return distance;
        }

        @Override
        public int compareTo(NodeWithDistance<T> o) {
            return Double.compare(distance, o.getDistance());
        }

        @Override
        public int hashCode() {
            return neighbor.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj || this.neighbor == obj)
                return true;
            return false;
        }

        @Override
        public String toString() {
            return "NodeWithDistance [neighbor=" + neighbor + ", distance=" + distance + "]";
        }

    }

}
