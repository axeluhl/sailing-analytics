package com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.AutoPlayNode;

public class AutoPlayLoopNode extends BaseCompositeNode {

    private List<AutoPlayNode> nodes = new ArrayList<>();
    private int loopTimePerNodeInSeconds;

    public AutoPlayLoopNode(int loopTimePerNodeInSeconds, AutoPlayNode... nodes) {
        this.loopTimePerNodeInSeconds = loopTimePerNodeInSeconds;
        this.nodes.addAll(Arrays.asList(nodes));
    }

    public AutoPlayLoopNode(int nrOfLoops, int loopTimePerNodeInSeconds, AutoPlayNode... nodes) {
        this.loopTimePerNodeInSeconds = loopTimePerNodeInSeconds;
        this.nodes.addAll(Arrays.asList(nodes));
    }

    @Override
    public void onStart() {
    }


}