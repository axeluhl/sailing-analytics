package com.sap.sailing.windestimation.aggregator.msthmm;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.windestimation.aggregator.hmm.GraphLevelBase;
import com.sap.sailing.windestimation.aggregator.hmm.GraphNodeTransitionProbabilitiesCalculator;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverWithProbabilisticTypeClassification;

public class MstGraphLevel extends GraphLevelBase {

    private final MstGraphLevel parent;
    private final List<MstGraphLevel> children = new ArrayList<>();
    private final double distanceToParent;

    public MstGraphLevel(ManeuverWithProbabilisticTypeClassification observation,
            GraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator) {
        super(observation, transitionProbabilitiesCalculator);
        parent = null;
        distanceToParent = 0;
    }

    private MstGraphLevel(MstGraphLevel parent, double distanceToParent,
            ManeuverWithProbabilisticTypeClassification observation,
            GraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator) {
        super(observation, transitionProbabilitiesCalculator);
        this.parent = parent;
        this.distanceToParent = distanceToParent;
    }

    public MstGraphLevel addChild(double distanceToParent, ManeuverWithProbabilisticTypeClassification observation,
            GraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator) {
        MstGraphLevel child = new MstGraphLevel(this, distanceToParent, observation,
                transitionProbabilitiesCalculator);
        children.add(child);
        return child;
    }

    public MstGraphLevel getParent() {
        return parent;
    }

    public double getDistanceToParent() {
        return distanceToParent;
    }

    public List<MstGraphLevel> getChildren() {
        return children;
    }

}
