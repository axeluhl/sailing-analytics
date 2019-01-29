package com.sap.sailing.windestimation.aggregator.hmm;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverWithProbabilisticTypeClassification;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class GraphLevel extends GraphLevelBase {

    private GraphLevel previousLevel = null;
    private GraphLevel nextLevel = null;
    private GraphLevel nonVirtualNodeToRepresent;

    public GraphLevel(BestPathsPerLevel nonVirtualNodeToRepresent) {
        super(nonVirtualNodeToRepresent.getCurrentLevel().getManeuverClassification(),
                getVirtualLevelNodesToRepresent(nonVirtualNodeToRepresent));
        this.nonVirtualNodeToRepresent = nonVirtualNodeToRepresent.getCurrentLevel();
    }

    public GraphLevel(ManeuverWithProbabilisticTypeClassification maneuverClassification, List<GraphNode> levelNodes) {
        super(maneuverClassification, levelNodes);
    }

    private static List<GraphNode> getVirtualLevelNodesToRepresent(BestPathsPerLevel nonVirtualNodeToRepresent) {
        List<GraphNode> levelNodes = new ArrayList<>(ManeuverTypeForClassification.values().length);
        for (GraphNode node : nonVirtualNodeToRepresent.getCurrentLevel().getLevelNodes()) {
            BestManeuverNodeInfo bestPreviousNodeInfo = nonVirtualNodeToRepresent.getBestPreviousNodeInfo(node);
            double confidence = bestPreviousNodeInfo.getProbabilityFromStart();
            IntersectedWindRange windRange = bestPreviousNodeInfo.getIntersectedWindRange();
            GraphNode newNode = new GraphNode(node.getManeuverType(), node.getTackAfter(), windRange, confidence,
                    node.getIndexInLevel());
            levelNodes.add(newNode);
        }
        return levelNodes;
    }

    public GraphLevel(ManeuverWithProbabilisticTypeClassification maneuverClassification,
            GraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator) {
        super(maneuverClassification, transitionProbabilitiesCalculator);
    }

    protected void setNextLevel(GraphLevel nextLevel) {
        this.nextLevel = nextLevel;
    }

    public GraphLevel getNextLevel() {
        return nextLevel;
    }

    protected void setPreviousLevel(GraphLevel previousLevel) {
        this.previousLevel = previousLevel;
    }

    public GraphLevel getPreviousLevel() {
        return previousLevel;
    }

    public void appendNextManeuverNodesLevel(GraphLevel nextManeuverNodesLevel) {
        setNextLevel(nextManeuverNodesLevel);
        nextManeuverNodesLevel.setPreviousLevel(this);
    }

    public GraphLevel getNonVirtualNodeToRepresent() {
        return nonVirtualNodeToRepresent;
    }

    public boolean isVirtual() {
        return nonVirtualNodeToRepresent != null;
    }

}
