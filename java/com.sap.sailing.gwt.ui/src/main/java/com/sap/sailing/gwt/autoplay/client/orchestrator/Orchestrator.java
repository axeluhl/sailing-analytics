package com.sap.sailing.gwt.autoplay.client.orchestrator;

import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.AutoPlayNode;

public interface Orchestrator {
    void start();

    void transitionToNode(AutoPlayNode nodeToTransitionTo);
}