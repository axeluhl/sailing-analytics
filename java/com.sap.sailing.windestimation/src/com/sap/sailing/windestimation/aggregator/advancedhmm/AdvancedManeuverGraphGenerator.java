package com.sap.sailing.windestimation.aggregator.advancedhmm;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.windestimation.aggregator.hmm.GraphNodeTransitionProbabilitiesCalculator;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverWithProbabilisticTypeClassification;

public class AdvancedManeuverGraphGenerator
        extends AbstractAdvancedGraphGenerator<ManeuverWithProbabilisticTypeClassification> {

    private final GraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator;

    public AdvancedManeuverGraphGenerator(
            GraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator) {
        this.transitionProbabilitiesCalculator = transitionProbabilitiesCalculator;
    }

    @Override
    protected double getDistanceBetweenObservations(ManeuverWithProbabilisticTypeClassification o1,
            ManeuverWithProbabilisticTypeClassification o2) {
        // TODO Auto-generated method stub
        return 0;
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
        parseGraphFromNodes(firstNode.getNeighbors(), firstGraphLevel, leafs);
        AdvancedManeuverGraphComponents graphComponents = new AdvancedManeuverGraphComponents(firstGraphLevel, leafs);
        return graphComponents;
    }

    private void parseGraphFromNodes(List<NeighborWithDistance<ManeuverWithProbabilisticTypeClassification>> nodes,
            AdvancedGraphLevel parent, List<AdvancedGraphLevel> leafs) {
        if (nodes.isEmpty()) {
            leafs.add(parent);
        } else {
            for (NeighborWithDistance<ManeuverWithProbabilisticTypeClassification> nodeWithDistance : nodes) {
                NodeWithNeighbors<ManeuverWithProbabilisticTypeClassification> node = nodeWithDistance.getNeighbor();
                AdvancedGraphLevel newGraphLevel = parent.addChild(nodeWithDistance.getDistance(),
                        node.getObservation(), transitionProbabilitiesCalculator);
                parseGraphFromNodes(node.getNeighbors(), newGraphLevel, leafs);
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
