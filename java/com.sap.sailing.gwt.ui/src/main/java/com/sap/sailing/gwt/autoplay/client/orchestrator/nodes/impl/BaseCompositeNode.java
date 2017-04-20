package com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.impl;

import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.AutoPlayNode;

public abstract class BaseCompositeNode
        extends AutoPlayNodeBase {

    private AutoPlayNode currentNode;

    public AutoPlayNode getCurrentNode() {
        return currentNode;
    }

    protected void transitionTo(AutoPlayNode nextNode) {
        if (currentNode != null) {
            currentNode.stop();
        }
        currentNode = nextNode;
        nextNode.start(getBus());
    }


}