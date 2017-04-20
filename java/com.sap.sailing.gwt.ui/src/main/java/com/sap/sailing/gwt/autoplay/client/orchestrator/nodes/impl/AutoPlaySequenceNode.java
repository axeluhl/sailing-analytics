package com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.impl;

import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.AutoPlayNode;

public class AutoPlaySequenceNode extends AutoPlayLoopNode {

    public AutoPlaySequenceNode(int loopTimePerNodeInSeconds, AutoPlayNode... nodes) {
        super(1, loopTimePerNodeInSeconds, nodes);
    }

}