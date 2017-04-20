package com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.impl;

import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.AutoPlayNodeController;

public abstract class BaseCompositeNode
        extends AutoPlayNodeBase {

    private AutoPlayNodeController currentNode;


    protected void transitionTo(AutoPlayNodeController nextNode) {
        if (currentNode != null) {
            currentNode.stop();
        }
        currentNode = nextNode;
        nextNode.start(getBus());
    }


}