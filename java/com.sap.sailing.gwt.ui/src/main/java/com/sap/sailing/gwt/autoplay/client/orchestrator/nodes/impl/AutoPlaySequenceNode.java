package com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.impl;

import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.AutoPlayNodeController;

public class AutoPlaySequenceNode extends AutoPlayLoopNode {

    public AutoPlaySequenceNode(int loopTimePerNodeInSeconds, AutoPlayNodeController... nodes) {
        super(1, loopTimePerNodeInSeconds, nodes);
    }

}