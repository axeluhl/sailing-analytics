package com.sap.sailing.windestimation.aggregator.msthmm;

import com.sap.sailing.windestimation.aggregator.hmm.GraphNode;
import com.sap.sailing.windestimation.aggregator.hmm.IntersectedWindRange;

public class PreviousNodeInfo {

    private final MstGraphLevel previousLevel;
    private final GraphNode previousNode;
    private final IntersectedWindRange previousNodeIntersectedWindRange;

    public PreviousNodeInfo(MstGraphLevel previousLevel, GraphNode previousNode,
            IntersectedWindRange previousNodeIntersectedWindRange) {
        this.previousLevel = previousLevel;
        this.previousNode = previousNode;
        this.previousNodeIntersectedWindRange = previousNodeIntersectedWindRange;
    }

    public MstGraphLevel getPreviousLevel() {
        return previousLevel;
    }

    public GraphNode getPreviousNode() {
        return previousNode;
    }

    public IntersectedWindRange getPreviousNodeIntersectedWindRange() {
        return previousNodeIntersectedWindRange;
    }

}
