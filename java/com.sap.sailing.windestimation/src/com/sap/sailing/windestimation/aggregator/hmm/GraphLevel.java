package com.sap.sailing.windestimation.aggregator.hmm;

import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverWithProbabilisticTypeClassification;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class GraphLevel extends GraphLevelBase {

    private GraphLevel previousLevel = null;
    private GraphLevel nextLevel = null;

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
