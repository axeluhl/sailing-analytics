package com.sap.sailing.windestimation.aggregator.advancedhmm;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverWithProbabilisticTypeClassification;

public class AdvancedManeuverGraphGenerator
        extends AbstractAdvancedGraphGenerator<ManeuverWithProbabilisticTypeClassification> {

    private final AdvancedGraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator;

    public AdvancedManeuverGraphGenerator(
            AdvancedGraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator) {
        this.transitionProbabilitiesCalculator = transitionProbabilitiesCalculator;
    }

    @Override
    protected double getDistanceBetweenObservations(ManeuverWithProbabilisticTypeClassification o1,
            ManeuverWithProbabilisticTypeClassification o2) {
        double compoundDistance = transitionProbabilitiesCalculator.getCompoundDistance(o1.getManeuver(),
                o2.getManeuver());
        return compoundDistance;
    }

    public AdvancedManeuverGraphComponents parseGraph() {
        List<NodeWithNeighbors<ManeuverWithProbabilisticTypeClassification>> nodes = getNodes();
        if (nodes.isEmpty()) {
            return null;
        }
        List<AdvancedGraphLevel> leafs = new ArrayList<>();
        NodeWithNeighbors<ManeuverWithProbabilisticTypeClassification> firstNode = nodes.get(0);
        AdvancedGraphLevel firstGraphLevel = new AdvancedGraphLevel(firstNode.getObservation(),
                transitionProbabilitiesCalculator);
        parseGraphFromNodes(firstNode, firstGraphLevel, null, leafs);
        AdvancedManeuverGraphComponents graphComponents = new AdvancedManeuverGraphComponents(firstGraphLevel, leafs);
        return graphComponents;
    }

    private void parseGraphFromNodes(NodeWithNeighbors<ManeuverWithProbabilisticTypeClassification> previousNode,
            AdvancedGraphLevel previousGraphLevel,
            NodeWithNeighbors<ManeuverWithProbabilisticTypeClassification> parentOfPreviousNode,
            List<AdvancedGraphLevel> leafs) {
        List<NodeWithDistance<ManeuverWithProbabilisticTypeClassification>> childNodes = previousNode.getNeighbors();
        if (childNodes.size() == 1) {
            leafs.add(previousGraphLevel);
        } else {
            for (NodeWithDistance<ManeuverWithProbabilisticTypeClassification> childNodeWithDistance : childNodes) {
                NodeWithNeighbors<ManeuverWithProbabilisticTypeClassification> childNode = childNodeWithDistance
                        .getNodeWithNeighbors();
                if (childNode != parentOfPreviousNode) {
                    AdvancedGraphLevel newGraphLevel = previousGraphLevel.addChild(childNodeWithDistance.getDistance(),
                            childNode.getObservation(), transitionProbabilitiesCalculator);
                    parseGraphFromNodes(childNode, newGraphLevel, previousNode, leafs);
                }
            }
        }
    }

    public static class AdvancedManeuverGraphComponents {
        private final AdvancedGraphLevel root;
        private final List<AdvancedGraphLevel> leafs;

        public AdvancedManeuverGraphComponents(AdvancedGraphLevel root, List<AdvancedGraphLevel> leafs) {
            this.root = root;
            this.leafs = leafs;
        }

        public AdvancedGraphLevel getRoot() {
            return root;
        }

        public List<AdvancedGraphLevel> getLeafs() {
            return leafs;
        }
    }

}
