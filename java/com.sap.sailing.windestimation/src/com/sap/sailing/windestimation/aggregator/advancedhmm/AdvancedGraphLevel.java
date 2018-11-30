package com.sap.sailing.windestimation.aggregator.advancedhmm;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.windestimation.aggregator.hmm.GraphLevelBase;
import com.sap.sailing.windestimation.aggregator.hmm.GraphNodeTransitionProbabilitiesCalculator;
import com.sap.sailing.windestimation.classifier.maneuver.ManeuverWithProbabilisticTypeClassification;

public class AdvancedGraphLevel extends GraphLevelBase {

    private final AdvancedGraphLevel parent;
    private final List<AdvancedGraphLevel> children = new ArrayList<>();
    private final double distanceToParent;

    public AdvancedGraphLevel(ManeuverWithProbabilisticTypeClassification observation,
            GraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator) {
        super(observation, transitionProbabilitiesCalculator);
        parent = null;
        distanceToParent = 0;
    }

    private AdvancedGraphLevel(AdvancedGraphLevel parent, double distanceToParent,
            ManeuverWithProbabilisticTypeClassification observation,
            GraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator) {
        super(observation, transitionProbabilitiesCalculator);
        this.parent = parent;
        this.distanceToParent = distanceToParent;
    }

    public AdvancedGraphLevel addChild(double distanceToParent, ManeuverWithProbabilisticTypeClassification observation,
            GraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator) {
        AdvancedGraphLevel child = new AdvancedGraphLevel(this, distanceToParent, observation,
                transitionProbabilitiesCalculator);
        children.add(child);
        return child;
    }

    public AdvancedGraphLevel getParent() {
        return parent;
    }

    public double getDistanceToParent() {
        return distanceToParent;
    }

    public List<AdvancedGraphLevel> getChildren() {
        return children;
    }

}
