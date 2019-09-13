package com.sap.sailing.windestimation.aggregator.msthmm;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.windestimation.aggregator.graph.GroupOutOfWhichToPickTheBestElement;
import com.sap.sailing.windestimation.aggregator.hmm.GraphLevelBase;
import com.sap.sailing.windestimation.aggregator.hmm.GraphNode;
import com.sap.sailing.windestimation.aggregator.hmm.GraphNodeTransitionProbabilitiesCalculator;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverWithProbabilisticTypeClassification;

/**
 * As {@link GraphLevelBase}, but specifies that each observation is followed by 0..n next observations and preceded by
 * maximally one observation which implies an acyclic directed graph.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class MstGraphLevel extends GraphLevelBase<MstGraphLevel> implements GroupOutOfWhichToPickTheBestElement<GraphNode<MstGraphLevel>, MstGraphLevel> {

    private final MstGraphLevel parent;
    private final List<MstGraphLevel> children = new ArrayList<>();
    private final double distanceToParent;

    /**
     * Constructs a root node, setting the {@link #parent} to {@code null}.
     */
    public MstGraphLevel(ManeuverWithProbabilisticTypeClassification observation,
            GraphNodeTransitionProbabilitiesCalculator<MstGraphLevel> transitionProbabilitiesCalculator) {
        super(observation, transitionProbabilitiesCalculator);
        parent = null;
        distanceToParent = 0;
    }

    private MstGraphLevel(MstGraphLevel parent, double distanceToParent,
            ManeuverWithProbabilisticTypeClassification observation,
            GraphNodeTransitionProbabilitiesCalculator<MstGraphLevel> transitionProbabilitiesCalculator) {
        super(observation, transitionProbabilitiesCalculator);
        this.parent = parent;
        this.distanceToParent = distanceToParent;
    }

    public MstGraphLevel addChild(double distanceToParent, ManeuverWithProbabilisticTypeClassification observation,
            GraphNodeTransitionProbabilitiesCalculator<MstGraphLevel> transitionProbabilitiesCalculator) {
        MstGraphLevel child = new MstGraphLevel(this, distanceToParent, observation, transitionProbabilitiesCalculator);
        children.add(child);
        return child;
    }

    @Override
    public MstGraphLevel getParent() {
        return parent;
    }

    public double getDistanceToParent() {
        return distanceToParent;
    }

    @Override
    public Iterable<MstGraphLevel> getChildren() {
        return children;
    }

    @Override
    public Iterable<GraphNode<MstGraphLevel>> getElements() {
        return getLevelNodes();
    }

}
