package com.sap.sailing.windestimation.aggregator.advancedhmm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.sap.sailing.polars.windestimation.ManeuverClassification;

public class AdvancedGraphGenerator {

    private List<NodeWithNeighbors> nodes = new ArrayList<>();

    private static final int MAX_DEPTH_FOR_DFS = 10;

    public void addNode(ManeuverClassification maneuverClassification) {
        NodeWithNeighbors newNode = new NodeWithNeighbors(maneuverClassification);
        LinkedList<NeighborDistance> distancesFromOtherNodes = getDistancesFromOtherNodes(maneuverClassification);
        if (distancesFromOtherNodes.isEmpty()) {
            nodes.add(newNode);
        } else {
            Collections.sort(distancesFromOtherNodes);
            NeighborDistance closestNeighborWithDistance = distancesFromOtherNodes.removeFirst();
            nodes.add(newNode);
            addEdge(newNode, closestNeighborWithDistance);
            if (!distancesFromOtherNodes.isEmpty()) {
                optimizeEdges(newNode, distancesFromOtherNodes, closestNeighborWithDistance);
            }
        }
    }

    private void addEdge(NodeWithNeighbors newNode, NeighborDistance closestNeighborWithDistance) {
        newNode.addNeighbor(closestNeighborWithDistance);
        closestNeighborWithDistance.getNeighbor().addNeighbor(newNode, closestNeighborWithDistance.getDistance());
    }

    private void removeEdge(NodeWithNeighbors a, NodeWithNeighbors b) {
        a.removeNeighbor(b);
        b.removeNeighbor(a);
    }

    private boolean canReach(NodeWithNeighbors nodeToReach, NodeWithNeighbors nodeToStartFrom,
            NodeWithNeighbors firstNodeOfPathToIgnore) {
        return canReach(nodeToReach, nodeToStartFrom, firstNodeOfPathToIgnore, 0);
    }

    private boolean canReach(NodeWithNeighbors nodeToReach, NodeWithNeighbors nodeToStartFrom,
            NodeWithNeighbors firstNodeOfPathToIgnore, int currentDepth) {
        if (nodeToStartFrom == nodeToReach) {
            return true;
        }
        for (NeighborDistance neighborDistance : nodeToStartFrom.getNeighbors()) {
            NodeWithNeighbors node = neighborDistance.getNeighbor();
            if (nodeToReach == node) {
                return true;
            }
        }
        if (currentDepth < MAX_DEPTH_FOR_DFS) {
            int nextDepth = currentDepth + 1;
            for (NeighborDistance neighborDistance : nodeToStartFrom.getNeighbors()) {
                NodeWithNeighbors node = neighborDistance.getNeighbor();
                if (canReach(nodeToReach, node, nodeToStartFrom, nextDepth)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void optimizeEdges(NodeWithNeighbors newNode, LinkedList<NeighborDistance> distancesFromOtherNodes,
            NeighborDistance currentNeighborWithDistance) {
        NodeWithNeighbors currentNeighbor = currentNeighborWithDistance.getNeighbor();
        for (NeighborDistance neighborWithDistance : currentNeighbor.getNeighbors()) {
            NodeWithNeighbors nextNeighbor = neighborWithDistance.getNeighbor();
            double distanceToImprove = neighborWithDistance.getDistance();
            for (Iterator<NeighborDistance> iterator = distancesFromOtherNodes.iterator(); iterator.hasNext();) {
                NeighborDistance candidate = iterator.next();
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

    private LinkedList<NeighborDistance> getDistancesFromOtherNodes(ManeuverClassification maneuverClassification) {
        LinkedList<NeighborDistance> result = new LinkedList<>();
        for (NodeWithNeighbors node : nodes) {
            double distance = getDistanceBetweenManeuvers(maneuverClassification, node.getManeuverClassification());
            NeighborDistance neighborDistance = new NeighborDistance(node, distance);
            result.add(neighborDistance);
        }
        return result;
    }

    private double getDistanceBetweenManeuvers(ManeuverClassification m1, ManeuverClassification m2) {
        // TODO
        return 0;
    }

    private static class NodeWithNeighbors {
        private final List<NeighborDistance> neighbors = new ArrayList<>(2);
        private final ManeuverClassification maneuverClassification;

        public NodeWithNeighbors(ManeuverClassification maneuverClassification) {
            this.maneuverClassification = maneuverClassification;
        }

        public void addNeighbor(NodeWithNeighbors nodeWithNeighbors, double distance) {
            NeighborDistance neighborDistance = new NeighborDistance(nodeWithNeighbors, distance);
            addNeighbor(neighborDistance);
        }

        public void addNeighbor(NeighborDistance neighborDistance) {
            neighbors.add(neighborDistance);
        }

        @SuppressWarnings("unlikely-arg-type")
        public void removeNeighbor(NodeWithNeighbors nodeWithNeighbors) {
            neighbors.remove(nodeWithNeighbors);
        }

        public List<NeighborDistance> getNeighbors() {
            return neighbors;
        }

        public ManeuverClassification getManeuverClassification() {
            return maneuverClassification;
        }
    }

    private static class NeighborDistance implements Comparable<NeighborDistance> {
        private final NodeWithNeighbors neighbor;
        private final double distance;

        public NeighborDistance(NodeWithNeighbors neighbor, double distance) {
            this.neighbor = neighbor;
            this.distance = distance;
        }

        public NodeWithNeighbors getNeighbor() {
            return neighbor;
        }

        public double getDistance() {
            return distance;
        }

        @Override
        public int compareTo(NeighborDistance o) {
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

    public AdvancedGraphLevel parseGraph() {
        if (nodes.isEmpty()) {
            return null;
        }
        NodeWithNeighbors firstNode = nodes.get(0);
        AdvancedGraphLevel firstGraphLevel = new AdvancedGraphLevel(firstNode.getManeuverClassification());
        parseGraphFromNodes(firstNode.getNeighbors(), firstGraphLevel);
        return firstGraphLevel;
    }

    private void parseGraphFromNodes(List<NeighborDistance> nodes, AdvancedGraphLevel parent) {
        for (NeighborDistance nodeWithDistance : nodes) {
            NodeWithNeighbors node = nodeWithDistance.getNeighbor();
            AdvancedGraphLevel newGraphLevel = parent.addChild(nodeWithDistance.getDistance(),
                    node.getManeuverClassification());
            if (!node.getNeighbors().isEmpty()) {
                parseGraphFromNodes(node.getNeighbors(), newGraphLevel);
            }
        }
    }

}
