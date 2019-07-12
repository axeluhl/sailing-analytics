package com.sap.sailing.windestimation.aggregator.hmm;

import java.util.List;

import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverWithProbabilisticTypeClassification;

/**
 * As {@link GraphLevelBase}, but specifies that each observation is followed and preceded by maximally one next
 * observation which implies that {@link GraphLevel}-instances are considered as a sequence.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class GraphLevel extends GraphLevelBase {

    private GraphLevel previousLevel = null;
    private GraphLevel nextLevel = null;

    public GraphLevel(ManeuverWithProbabilisticTypeClassification maneuverClassification, List<GraphNode> levelNodes) {
        super(maneuverClassification, levelNodes);
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
}
