package com.sap.sailing.windestimation.aggregator.msthmm;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverWithProbabilisticTypeClassification;

public class MstManeuverGraphGenerator extends AbstractMstGraphGenerator<ManeuverWithProbabilisticTypeClassification> {

    private final MstGraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator;

    public MstManeuverGraphGenerator(MstGraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator) {
        this.transitionProbabilitiesCalculator = transitionProbabilitiesCalculator;
    }

    @Override
    protected double getDistanceBetweenObservations(ManeuverWithProbabilisticTypeClassification o1,
            ManeuverWithProbabilisticTypeClassification o2) {
        double compoundDistance = transitionProbabilitiesCalculator.getCompoundDistance(o1.getManeuver(),
                o2.getManeuver());
        return compoundDistance;
    }

    public MstManeuverGraphComponents parseGraph() {
        List<NodeWithNeighbors<ManeuverWithProbabilisticTypeClassification>> nodes = getNodes();
        if (nodes.isEmpty()) {
            return null;
        }
        List<MstGraphLevel> leafs = new ArrayList<>();
        NodeWithNeighbors<ManeuverWithProbabilisticTypeClassification> firstNode = nodes.get(0);
        MstGraphLevel firstGraphLevel = new MstGraphLevel(firstNode.getObservation(),
                transitionProbabilitiesCalculator);
        parseGraphFromNodes(firstNode, firstGraphLevel, null, leafs);
        MstManeuverGraphComponents graphComponents = new MstManeuverGraphComponents(firstGraphLevel, leafs);
        return graphComponents;
    }

    private void parseGraphFromNodes(NodeWithNeighbors<ManeuverWithProbabilisticTypeClassification> previousNode,
            MstGraphLevel previousGraphLevel,
            NodeWithNeighbors<ManeuverWithProbabilisticTypeClassification> parentOfPreviousNode,
            List<MstGraphLevel> leafs) {
        List<NodeWithDistance<ManeuverWithProbabilisticTypeClassification>> childNodes = previousNode.getNeighbors();
        if (parentOfPreviousNode != null && childNodes.size() == 1) {
            leafs.add(previousGraphLevel);
        } else {
            for (NodeWithDistance<ManeuverWithProbabilisticTypeClassification> childNodeWithDistance : childNodes) {
                NodeWithNeighbors<ManeuverWithProbabilisticTypeClassification> childNode = childNodeWithDistance
                        .getNodeWithNeighbors();
                if (childNode != parentOfPreviousNode) {
                    MstGraphLevel newGraphLevel = previousGraphLevel.addChild(childNodeWithDistance.getDistance(),
                            childNode.getObservation(), transitionProbabilitiesCalculator);
                    parseGraphFromNodes(childNode, newGraphLevel, previousNode, leafs);
                }
            }
        }
    }

    public static class MstManeuverGraphComponents {
        private final MstGraphLevel root;
        private final List<MstGraphLevel> leafs;

        public MstManeuverGraphComponents(MstGraphLevel root, List<MstGraphLevel> leafs) {
            this.root = root;
            this.leafs = leafs;
        }

        public MstGraphLevel getRoot() {
            return root;
        }

        public List<MstGraphLevel> getLeafs() {
            return leafs;
        }
    }

}
