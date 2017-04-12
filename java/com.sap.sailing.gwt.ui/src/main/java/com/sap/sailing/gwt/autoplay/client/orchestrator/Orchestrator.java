package com.sap.sailing.gwt.autoplay.client.orchestrator;

import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.AutoPlayNodeController;

public interface Orchestrator {
    void start();

    void transitionToNode(AutoPlayNodeController nodeToTransitionTo);
}