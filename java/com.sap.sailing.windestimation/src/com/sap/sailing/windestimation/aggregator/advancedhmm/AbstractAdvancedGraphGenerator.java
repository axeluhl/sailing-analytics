package com.sap.sailing.windestimation.aggregator.advancedhmm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractAdvancedGraphGenerator<T> {

    private List<NodeWithNeighbors<T>> nodes = new ArrayList<>();

    private static final int MAX_DEPTH_FOR_DFS = 10;

    public void addNode(T observation) {
        NodeWithNeighbors<T> newNode = new NodeWithNeighbors<>(observation);
        LinkedList<NeighborWithDistance<T>> distancesFromOtherNodes = getDistancesFromOtherNodes(observation);
        if (distancesFromOtherNodes.isEmpty()) {
            nodes.add(newNode);
        } else {
            Collections.sort(distancesFromOtherNodes);
            NeighborWithDistance<T> closestNeighborWithDistance = distancesFromOtherNodes.removeFirst();
            nodes.add(newNode);
            addEdge(newNode, closestNeighborWithDistance);
            if (!distancesFromOtherNodes.isEmpty()) {
                optimizeEdges(newNode, distancesFromOtherNodes, closestNeighborWithDistance);
            }
        }
    }

    private void optimizeEdges(NodeWithNeighbors<T> newNode,
            LinkedList<NeighborWithDistance<T>> distancesFromOtherNodes,
            NeighborWithDistance<T> currentNeighborWithDistance) {
        NodeWithNeighbors<T> currentNeighbor = currentNeighborWithDistance.getNeighbor();
        for (NeighborWithDistance<T> neighborWithDistance : currentNeighbor.getNeighbors()) {
            NodeWithNeighbors<T> nextNeighbor = neighborWithDistance.getNeighbor();
            double distanceToImprove = neighborWithDistance.getDistance();
            for (Iterator<NeighborWithDistance<T>> iterator = distancesFromOtherNodes.iterator(); iterator.hasNext();) {
                NeighborWithDistance<T> candidate = iterator.next();
                if (distanceToImprove <= candidate.getDistance()) {
                    if (canReach(newNode, currentNeighbor, newNode)) {
                        removeEdge(currentNeighbor, nextNeighbor);
                        addEdge(newNode, candidate);
                        iterator.remove();
                        optimizeEdges(newNode, distancesFromOtherNodes, candidate);
                        break;
                    }
                } else {
                    break;
                }
            }
        }
    }

    private LinkedList<NeighborWithDistance<T>> getDistancesFromOtherNodes(T observation) {
        LinkedList<NeighborWithDistance<T>> result = new LinkedList<>();
        for (NodeWithNeighbors<T> node : nodes) {
            double distance = getDistanceBetweenObservations(observation, node.getObservation());
            NeighborWithDistance<T> neighborWithDistance = new NeighborWithDistance<>(node, distance);
            result.add(neighborWithDistance);
        }
        return result;
    }

    protected abstract double getDistanceBetweenObservations(T o1, T o2);

    private void addEdge(NodeWithNeighbors<T> newNode, NeighborWithDistance<T> closestNeighborWithDistance) {
        newNode.addNeighbor(closestNeighborWithDistance);
        closestNeighborWithDistance.getNeighbor().addNeighbor(newNode, closestNeighborWithDistance.getDistance());
    }

    private void removeEdge(NodeWithNeighbors<T> a, NodeWithNeighbors<T> b) {
        a.removeNeighbor(b);
        b.removeNeighbor(a);
    }

    private boolean canReach(NodeWithNeighbors<T> nodeToReach, NodeWithNeighbors<T> nodeToStartFrom,
            NodeWithNeighbors<T> firstNodeOfPathToIgnore) {
        return canReach(nodeToReach, nodeToStartFrom, firstNodeOfPathToIgnore, 0);
    }

    private boolean canReach(NodeWithNeighbors<T> nodeToReach, NodeWithNeighbors<T> nodeToStartFrom,
            NodeWithNeighbors<T> firstNodeOfPathToIgnore, int currentDepth) {
        if (nodeToStartFrom == nodeToReach) {
            return true;
        }
        for (NeighborWithDistance<T> neighborWithDistance : nodeToStartFrom.getNeighbors()) {
            NodeWithNeighbors<T> node = neighborWithDistance.getNeighbor();
            if (nodeToReach == node) {
                return true;
            }
        }
        if (currentDepth < MAX_DEPTH_FOR_DFS) {
            int nextDepth = currentDepth + 1;
            for (NeighborWithDistance<T> neighborWithDistance : nodeToStartFrom.getNeighbors()) {
                NodeWithNeighbors<T> node = neighborWithDistance.getNeighbor();
                if (canReach(nodeToReach, node, nodeToStartFrom, nextDepth)) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<NodeWithNeighbors<T>> getNodes() {
        return nodes;
    }

    protected static class NodeWithNeighbors<T> {
        private final List<NeighborWithDistance<T>> neighbors = new ArrayList<>(2);
        private final T observation;

        public NodeWithNeighbors(T observation) {
            this.observation = observation;
        }

        public void addNeighbor(NodeWithNeighbors<T> nodeWithNeighbors, double distance) {
            NeighborWithDistance<T> neighborWithDistance = new NeighborWithDistance<>(nodeWithNeighbors, distance);
            addNeighbor(neighborWithDistance);
        }

        public void addNeighbor(NeighborWithDistance<T> neighborWithDistance) {
            neighbors.add(neighborWithDistance);
        }

        @SuppressWarnings("unlikely-arg-type")
        public void removeNeighbor(NodeWithNeighbors<T> nodeWithNeighbors) {
            neighbors.remove(nodeWithNeighbors);
        }

        public List<NeighborWithDistance<T>> getNeighbors() {
            return neighbors;
        }

        public T getObservation() {
            return observation;
        }
    }

    protected static class NeighborWithDistance<T> implements Comparable<NeighborWithDistance<T>> {
        private final NodeWithNeighbors<T> neighbor;
        private final double distance;

        public NeighborWithDistance(NodeWithNeighbors<T> neighbor, double distance) {
            this.neighbor = neighbor;
            this.distance = distance;
        }

        public NodeWithNeighbors<T> getNeighbor() {
            return neighbor;
        }

        public double getDistance() {
            return distance;
        }

        @Override
        public int compareTo(NeighborWithDistance<T> o) {
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
    }

}
